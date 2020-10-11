package com.devlabs.buycar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    public static int responseCode = 0;
    private static final int PERMISSION_CODE = 1000;
    private static final int IMAGE_CAPTURE_CODE = 1001;
    Button captureImageButton;
    ImageView imageView;


    private static final String TAG = "MyActivity";

    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        captureImageButton = findViewById(R.id.captureImageButton);


        //button click
        captureImageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.CAMERA) ==
                            PackageManager.PERMISSION_DENIED ||
                            checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                                    PackageManager.PERMISSION_DENIED) {

                        // permission not enabled, request it
                        String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        //show popup to request permissions
                        requestPermissions(permission, PERMISSION_CODE);

                    } else {
                        //permission already granted
                        openCamera();
                    }
                } else {
                    //system os < marshmellow
                    openCamera();
                }
            }
        });

    }


    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera");

        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        //Camera intent
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    //permission from popup was granted
                    openCamera();
                } else {
                    //permission from popup was denied
                    Toast.makeText(this, getResources().getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, final int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "Test");
        Log.d(TAG, imageUri.toString());

        if (resultCode == RESULT_OK) {

            InputStream imageStream = null;
            try {
                imageStream = getContentResolver().openInputStream(imageUri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
            String encodedImage = encodeImage(selectedImage);

            Log.d(TAG, "++++++encodedImage+++++++++" + encodedImage);

            System.out.println(encodedImage);

            // Create API request
            OkHttpClient client = new OkHttpClient();

            encodedImage = encodedImage.replace("\n", "").replace("\r", "");

            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, "{\"content\": \"" + encodedImage + "\"}");

            Request request = new Request.Builder()
                    .url("https://gw.hackathon.vtb.ru/vtb/hackathon/car-recognize")
                    .post(body)
                    .addHeader("x-ibm-client-id", "e5eae11009f8c33623d87ae6447ff7f2")
                    .addHeader("content-type", "application/json")
                    .addHeader("accept", "application/json")
                    .build();

            Log.d(TAG, request.toString());

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    Log.d(TAG, "----------MY RESPONSE--------------");
                    Log.d(TAG, response.toString());

                    if (response.isSuccessful()) {
                        String myResponse = response.body().string();

//                        Map<String,Object> result = new ObjectMapper().readValue(myResponse, HashMap.class);

//                        for (Map.Entry<String, Object> entry : result.entrySet()) {
//                            System.out.println(entry.getKey());
//                            System.out.println(entry.getValue());
//                        }


                        try {
                            JSONObject jsonObject = new JSONObject(myResponse);

                            System.out.println("STRING");
                            System.out.println(jsonObject.getString("probabilities"));
                            String cars = jsonObject.getString("probabilities").toString();

                            JSONObject carsObject = new JSONObject(cars);

                            Map<String, Double> result = new ObjectMapper().readValue(cars, HashMap.class);
                            Map.Entry<String, Double> maxEntry = null;
                            //Get max value
                            for (Map.Entry<String, Double> entry : result.entrySet()) {
                                System.out.println(entry.getKey());
                                System.out.println(entry.getValue());
                                if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
                                    maxEntry = entry;
                                }

                            }

                            String carModel = maxEntry.getKey();

                            System.out.println("Max value:");
                            System.out.println(maxEntry);


                            Request requestMarket = new Request.Builder()
                                    .url("https://gw.hackathon.vtb.ru/vtb/hackathon/marketplace")
                                    .get()
                                    .addHeader("x-ibm-client-id", "e5eae11009f8c33623d87ae6447ff7f2")
                                    .addHeader("accept", "application/json")
                                    .build();


                            client.newCall(requestMarket).enqueue(new Callback() {

                                @Override
                                public void onResponse(@NotNull Call call, @NotNull Response responseMarket) throws IOException {

                                    String alias = "BMW";
                                    String modelAlias = "3";
                                    if (carModel == "BMW 3") {
                                        alias = "BMW";
                                        modelAlias = "3";
                                    } else if (carModel == "BMW 5") {
                                        alias = "BMW";
                                        modelAlias = "5";
                                    } else if (carModel == "Cadillac ESCALADE") {
                                        alias = "Cadillac";
                                        modelAlias = "ESCALADE";
                                    } else if (carModel == "Chevrolet Tahoe") {
                                        alias = "Chevrolet";
                                        modelAlias = "Tahoe";
                                    } else if (carModel == "Hyundai Genesis") {
                                        alias = "Hyundai";
                                        modelAlias = "Genesis";
                                    } else if (carModel == "Jaguar F-PACE") {
                                        alias = "Jaguar";
                                        modelAlias = "F-PACE";
                                    } else if (carModel == "KIA K5") {
                                        alias = "KIA";
                                        modelAlias = "K5";
                                    } else if (carModel == "KIA Optima") {
                                        alias = "KIA";
                                        modelAlias = "Optima";
                                    } else if (carModel == "KIA Sportage") {
                                        alias = "KIA";
                                        modelAlias = "Sportage";
                                    } else if (carModel == "Land Rover RANGE ROVER VELAR") {
                                        alias = "Land Rover";
                                        modelAlias = "RANGE ROVER VELAR";
                                    } else if (carModel == "Mazda 3") {
                                        alias = "Mazda";
                                        modelAlias = "3";
                                    } else if (carModel == "Mazda 6") {
                                        alias = "Mazda";
                                        modelAlias = "6";
                                    } else if (carModel == "Mercedes A") {
                                        alias = "Mercedes";
                                        modelAlias = "A";
                                    } else if (carModel == "Toyota Camry") {
                                        alias = "Toyota";
                                        modelAlias = "Camry";
                                    }


                                    if (response.isSuccessful()) {
                                        String respMarket = responseMarket.body().string();
                                        System.out.println(respMarket);
                                        System.out.println(carModel);
                                        // Go to marketplace
                                        Intent intent = new Intent(MainActivity.this, CarListActivity.class);
                                        //alias - mazda
                                        //model-alias - model
                                        intent.putExtra("alias", alias);
                                        intent.putExtra("modelAlias", modelAlias);
                                        intent.putExtra("marketplace", respMarket);
                                        startActivity(intent);
                                    }

                                }

                                @Override
                                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                    e.printStackTrace();
                                }

                            });


                            System.out.println("RESULT");
                            System.out.println(result);

                            double bmw3 = Double.parseDouble(carsObject.getString("BMW 3"));
                            double bmw5 = Double.parseDouble(carsObject.getString("BMW 5"));
                            double cadillacEscalade = Double.parseDouble(carsObject.getString("Cadillac ESCALADE"));
                            double chevroletTahoe = Double.parseDouble(carsObject.getString("Chevrolet Tahoe"));
                            double hyundaiGenesis = Double.parseDouble(carsObject.getString("Hyundai Genesis"));
                            double jaguar = Double.parseDouble(carsObject.getString("Jaguar F-PACE"));
                            double kiaK5 = Double.parseDouble(carsObject.getString("KIA K5"));
                            double kiaOptima = Double.parseDouble(carsObject.getString("KIA Optima"));
                            double kiaSportage = Double.parseDouble(carsObject.getString("KIA Sportage"));
                            double landRoverRangeRoverVelar = Double.parseDouble(carsObject.getString("Land Rover RANGE ROVER VELAR"));
                            double mazda3 = Double.parseDouble(carsObject.getString("Mazda 3"));
                            double mazda6 = Double.parseDouble(carsObject.getString("Mazda 6"));
                            double mercedesA = Double.parseDouble(carsObject.getString("Mercedes A"));
                            double toyotaCamry = Double.parseDouble(carsObject.getString("Toyota Camry"));

//                            double[] get

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }

                }

                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    e.printStackTrace();
                }
            });


            //set the image captured to our ImageView
            imageView.setImageURI(imageUri);
        }
    }


    private String getHighestScoreCar(String json) throws JSONException, IOException {

        Map<String, Object> result = new ObjectMapper().readValue(json, HashMap.class);

        Log.d(TAG, "RESULT");
        Log.d(TAG, result.toString());

        JSONObject myjson = new JSONObject(json);
        JSONArray the_json_array = myjson.getJSONArray("probabilities");

        Log.d(TAG, "the_json_array");
        Log.d(TAG, the_json_array.toString());

        return null;
    }


    private byte[] imageToByteArray(Bitmap bitmapImage) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmapImage.compress(Bitmap.CompressFormat.JPEG, 20, baos);
        return baos.toByteArray();
    }


    // Encode Bitmap in base64
    private String encodeImage(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 60, baos);
        byte[] b = baos.toByteArray();
        String encImage = Base64.encodeToString(b, Base64.DEFAULT);

        return encImage;
    }

    // Encode from FilePath to base64
    private String encodeImage(String path) {
        File imagefile = new File(path);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(imagefile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Bitmap bm = BitmapFactory.decodeStream(fis);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 60, baos);
        byte[] b = baos.toByteArray();
        String encImage = Base64.encodeToString(b, Base64.DEFAULT);
        //Base64.de
        return encImage;

    }

    public void onClickToCarListActivity(View view) {
        Log.d("DbMain", "click to ExpandabaleListViewActivity");
        Intent intent = new Intent(MainActivity.this, CarListActivity.class);
        startActivity(intent);
    }

    public void onClickToCarCalculatorActivity(View view) {

        Log.d("DbMain", "click to Car calculaor");
        Intent intent = new Intent(MainActivity.this, AddUserActivity.class);
        startActivity(intent);
    }
}