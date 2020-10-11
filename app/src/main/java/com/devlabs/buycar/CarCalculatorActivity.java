package com.devlabs.buycar;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.devlabs.buycar.calculator.data.CalculateRequest;
import com.devlabs.buycar.calculator.data.CalculateResponse;
import com.devlabs.buycar.calculator.data.PaymentsGraphRequest;
import com.devlabs.buycar.calculator.data.PaymentsGraphResponse;
import com.devlabs.buycar.retrofit.NetworkService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CarCalculatorActivity extends AppCompatActivity {
    private static final String TAG = CarCalculatorActivity.class.getSimpleName();
    private Button buttonDoReq;
    private EditText editTextResponse;

    Button buttonRegisterUser;
    SeekBar seekBarCredit;

    ImageView imageView;

    SharedPreferences sharedPreferences;

    // From Intent
    String imageUrl = "https://207231.selcdn.ru/locator-media/models_desktop_250_q90/tradeins.space-uploads-photo-83651-novaya-octavia_1.png";
    String carModel = "Haval";
    double minPrice = Double.parseDouble("850000");

    private static final String SHARED_PREF_NAME = "buycar";
    private static final String KEY_FIRST_NAME = "first_name";
    private static final String KEY_SERNAME = "sername";
    private static final String KEY_SECOND_NAME = "second_name";
    private static final String KEY_BIRTHDAY = "birthday";
    private static final String KEY_CITY = "city";
    private static final String KEY_SALARY = "salary";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_EMAIL = "email";

    Intent intent;
    int car_id;
    String car_photo_url;
    String car_alias;
    String car_models_bodies_alias;
    String car_models_alias;
    String car_country_code;
    int car_minprice;
    int initialPrice;
    int termMonths;

    int term = 5;

    TextView textViewCarPrice;
    TextView textViewContractRate;
    EditText editContractRate;
    EditText editInitialPrice;
    EditText editTerm;

    String first_name;
    String sername;
    String second_name;
    String salary;
    String city;
    String birthday;
    String phone;
    String email;

    String date_time;

    EditText editTextLastPayment;
    EditText editTextLoanAmount;
    EditText editTextPayment;
    EditText editTextContractRate;
    Button buttonPayments;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_calculator);

        LocalDateTime dateTime = LocalDateTime.now(); // Gets the current date and time

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        System.out.println(dateTime.format(formatter));

        editTextLastPayment = findViewById(R.id.editTextLastPayment);
        editTextLoanAmount = findViewById(R.id.editTextLoanAmount);
        editTextPayment = findViewById(R.id.editTextPayment);
        buttonPayments = findViewById(R.id.buttonPayments);
        editTextContractRate = findViewById(R.id.editTextContractRate);

        date_time = dateTime.format(formatter);

        intent = getIntent();
        car_id = intent.getIntExtra("car_id", 0);
        car_photo_url = intent.getStringExtra("car_photo_url");
        car_alias = intent.getStringExtra("car_alias");
        car_models_bodies_alias = intent.getStringExtra("car_models_bodies_alias");
        car_models_alias = intent.getStringExtra("car_models_alias");
        car_country_code = intent.getStringExtra("car_country_code");
        car_minprice = intent.getIntExtra("car_minprice", 0);

        buttonDoReq = findViewById(R.id.buttonDoReq);
        buttonDoReq.setOnClickListener(buttonClickListener);

//        seekBarCredit = findViewById(R.id.seekBarCredit);
        buttonRegisterUser = findViewById(R.id.buttonRegisterUser);
        imageView = findViewById(R.id.imageView);

        //Credits
        textViewCarPrice = findViewById(R.id.textViewCarPrice);
        textViewContractRate = findViewById(R.id.textViewContractRate);
        editInitialPrice = findViewById(R.id.editInitialPrice);
        editTerm = findViewById(R.id.editTerm);

        Picasso.with(CarCalculatorActivity.this).load(intent.getStringExtra("car_photo_url")).into(imageView);

        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
         first_name = sharedPreferences.getString(KEY_FIRST_NAME, null);
         sername = sharedPreferences.getString(KEY_SERNAME, null);
         second_name = sharedPreferences.getString(KEY_SECOND_NAME, null);
         salary = sharedPreferences.getString(KEY_SALARY, null);
         city = sharedPreferences.getString(KEY_CITY, null);
         birthday = sharedPreferences.getString(KEY_FIRST_NAME, null);
         phone = sharedPreferences.getString(KEY_PHONE, null);
         email = sharedPreferences.getString(KEY_EMAIL, null);

        //Clear shared pref
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.clear();


        car_minprice = 850000;
        car_alias = "Haval";
        //get initial val
        initialPrice = (int) (car_minprice * 0.2);

        termMonths = term * 12;

        textViewCarPrice.setText(String.format("%d", car_minprice));
        editInitialPrice.setText(String.format("%d", initialPrice));
        editTerm.setText(String.format("%d", term));

        if (first_name == null) {
//            seekBarCredit.setVisibility(View.GONE);
            buttonRegisterUser.setVisibility(View.VISIBLE);
        } else {

//            seekBarCredit.setVisibility(View.VISIBLE);
            buttonDoReq.setVisibility(View.VISIBLE);
            editInitialPrice.setVisibility(View.VISIBLE);
            editTerm.setVisibility(View.VISIBLE);
            textViewContractRate.setVisibility(View.VISIBLE);
            buttonPayments.setVisibility(View.VISIBLE);
            buttonRegisterUser.setVisibility(View.GONE);

        }


        ArrayList<String> clientTypes = new ArrayList<>();
        clientTypes.add("ac43d7e4-cd8c-4f6f-b18a-5ccbc1356f75");
        ArrayList<String> specialConditions = new ArrayList<>();
        specialConditions.add("57ba0183-5988-4137-86a6-3d30a4ed8dc9");
        specialConditions.add("b907b476-5a26-4b25-b9c0-8091e9d5c65f");
        specialConditions.add("cbfc4ef3-af70-4182-8cf6-e73f361d1e68");

        System.out.println("CAR MIN PRICE");
        System.out.println(car_minprice);


        CalculateRequest req = new CalculateRequest(clientTypes, car_minprice, initialPrice,
                64412219, "en", 86.96731273,
                car_alias, specialConditions, term);

//            PaymentsGraphRequest reqPayment = new PaymentsGraphRequest();


        NetworkService.getInstance().getCalculatorApi().calculate(req).enqueue(calculateCallback);


    }

    View.OnClickListener buttonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {


            initialPrice = Integer.parseInt(String.valueOf(editInitialPrice.getText()));

            int loan_price = car_minprice - initialPrice;

            OkHttpClient client = new OkHttpClient();

            MediaType mediaType = MediaType.parse("application/json");
//            RequestBody body = RequestBody.create(mediaType, "{\"comment\":\"Получение наличных\",\"customer_party\":{\"email\":\"" + email + "\",\"income_amount\": "+ salary +",\"person\":{\"birth_date_time\":\""+ birthday +"\",\"birth_place\":\""+ city +"\",\"family_name\":\""+ sername +"\",\"first_name\":\""+ first_name +"\",\"gender\":\"male\",\"middle_name\":\""+ second_name +"\",\"nationality_country_code\":\"RU\"},\"phone\":\""+ phone +"\"},\"datetime\":\"2020-10-11T10:30:47Z\",\"interest_rate\":15.7,\"requested_amount\": "+ loan_price +",\"requested_term\": "+ termMonths +",\"trade_mark\":\""+ car_alias +"\",\"vehicle_cost\": "+ car_minprice+"}");
            RequestBody body = RequestBody.create(mediaType, "{\"comment\":\"Комментарий\",\"customer_party\":{\"email\":\""+ email +"\",\"income_amount\":"+ salary +",\"person\":{\"birth_date_time\":\"1981-11-01\",\"birth_place\":\"г. Воронеж\",\"family_name\":\"Иванов\",\"first_name\":\"Иван\",\"gender\":\"male\",\"middle_name\":\"Иванович\",\"nationality_country_code\":\"RU\"},\"phone\":\"+99999999999\"},\"datetime\":\"2020-10-10T08:15:47Z\",\"interest_rate\":15.7,\"requested_amount\":300000,\"requested_term\":36,\"trade_mark\":\"Nissan\",\"vehicle_cost\":600000}");
            Request request = new Request.Builder()
                    .url("https://gw.hackathon.vtb.ru/vtb/hackathon/carloan")
                    .post(body)
                    .addHeader("x-ibm-client-id", "e5eae11009f8c33623d87ae6447ff7f2")
                    .addHeader("content-type", "application/json")
                    .addHeader("accept", "application/json")
                    .build();
            System.out.println("START REQUEST");
            System.out.println(request);

            client.newCall(request).enqueue(new okhttp3.Callback() {

                @Override
                public void onResponse(@NotNull okhttp3.Call call, @NotNull okhttp3.Response response) throws IOException {


                    String myResponse = response.body().string();

                    System.out.println(myResponse);
                    if (response.isSuccessful()) {
//
                        System.out.println(myResponse);
                        try {
                            JSONObject jsonObject = new JSONObject(myResponse);
                            String application = jsonObject.getString("application").toString();

                            JSONObject jsonObject1 = new JSONObject(application);
                            String decision_report = jsonObject1.getString("decision_report").toString();

                            JSONObject carsObject2 = new JSONObject(decision_report);

                            String application_status = carsObject2.getString("application_status");
                            System.out.println(application_status);

                            String answer =null;

                            if (application_status == "prescore_approved") {
                                answer = "Your credit was aproved!";
                            } else  if (application_status == "prescore_denied") {
                                answer = "Your credit was denied!";
                            } else {
                                answer = "Processing";
                            }

                            Toast.makeText(CarCalculatorActivity.this, answer, Toast.LENGTH_LONG).show();

                            Map<String, String> result = new ObjectMapper().readValue(decision_report, HashMap.class);

                            System.out.println(result);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                }

                @Override
                public void onFailure(@NotNull okhttp3.Call call, @NotNull IOException e) {
                    e.printStackTrace();
                }
            });
//            NetworkService.getInstance().getCalculatorApi().calculate(reqPayment).enqueue(paymentsCallback);


        }
    };

    Callback<CalculateResponse> calculateCallback = new Callback<CalculateResponse>() {

        @Override
        public void onResponse(Call<CalculateResponse> call, Response<CalculateResponse> response) {
            CalculateResponse resp = response.body(); // тут твой класс
            System.out.println("RESPONSE");
            System.out.println(resp.getResult());
            System.out.println(resp.getResult().getContractRate());
            double contractRate = resp.getResult().getContractRate();
            double kaskoCost = resp.getResult().getKaskoCost();
            double lastPayment = resp.getResult().getLastPayment();
            double loanAmount = resp.getResult().getLoanAmount();
            double payment = resp.getResult().getPayment();
            double subsidy = resp.getResult().getSubsidy();
            int term = resp.getResult().getTerm();


            textViewContractRate.setText(contractRate + " %");
            editTextContractRate.setText(contractRate + " %");
            editTextLastPayment.setText(lastPayment + " %");
            editTextLoanAmount.setText(loanAmount + " %");
            editTextPayment.setText(payment + " %");
//            try {
//                editTextResponse.setText(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(resp));
//            } catch (JsonProcessingException e) {
//                Log.e(TAG, "error parsing data", e);
//            }
        }

        @Override
        public void onFailure(Call<CalculateResponse> call, Throwable t) {
            Log.e(TAG, "error parsing data", t);
        }
    };


    public void onClickGetPayments(View view) {
        /*{"contractRate":10.1,"lastPayment":41.3378801,"loanAmount":1000000,"payment":50000,"term":5}*/

        double ctRate = Double.parseDouble(editTextContractRate.getText().toString());
        double ctLP = Double.parseDouble(editTextLastPayment.getText().toString());
        double ctLA = Double.parseDouble(editTextLoanAmount.getText().toString());
        double ctPayment = Double.parseDouble(editTextPayment.getText().toString());
        int ctTerm = Integer.parseInt(editTerm.getText().toString());


        PaymentsGraphRequest req = new PaymentsGraphRequest(ctRate, ctLP, ctLA, ctPayment, ctTerm);

        NetworkService.getInstance().getCalculatorApi().calculate(req).enqueue(paymentsCallback);
    }



    Callback<PaymentsGraphResponse> paymentsCallback = new Callback<PaymentsGraphResponse>() {
        @Override
        public void onResponse(Call<PaymentsGraphResponse> call, Response<PaymentsGraphResponse> response) {
            PaymentsGraphResponse resp = response.body(); // тут твой класс
            System.out.println("RESPONSE");
            System.out.println(resp.getPayments());
        }

        @Override
        public void onFailure(Call<PaymentsGraphResponse> call, Throwable t) {

        }
    };


    public void onCklickRegisterUser(View view) {
        Intent intent = new Intent(CarCalculatorActivity.this, AddUserActivity.class);
        startActivity(intent);
    }

}