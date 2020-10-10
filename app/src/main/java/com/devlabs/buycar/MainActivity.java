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

import org.jetbrains.annotations.NotNull;

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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);



        InputStream imageStream = null;
        try {
            imageStream = getContentResolver().openInputStream(imageUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
        String encodedImage = encodeImage(selectedImage);

        Log.d(TAG,  "++++++encodedImage+++++++++" + encodedImage );

        System.out.println(encodedImage);

        OkHttpClient client = new OkHttpClient();

        String str_enc = "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";

        String str_enc_t = "/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAA0JCgsKCA0LCgsODg0PEyAVExISEyccHhcgLikxMC4p\n" +
                "LSwzOko+MzZGNywtQFdBRkxOUlNSMj5aYVpQYEpRUk//2wBDAQ4ODhMREyYVFSZPNS01T09PT09P\n" +
                "T09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT0//wAARCAPAAtADASIA\n" +
                "AhEBAxEB/8QAGwAAAwEBAQEBAAAAAAAAAAAAAAECAwQFBgf/xAA4EAACAgIBAwMDAwQCAQMEAwEA\n" +
                "AQIRAyExBBJBE1FhBSJxMoGRFCNCoTNSFWJysSQlgsFDRFPh/8QAGAEBAQEBAQAAAAAAAAAAAAAA\n" +
                "AAECAwT/xAAeEQEBAQEAAwEBAQEAAAAAAAAAARECEiExA0FRE//aAAwDAQACEQMRAD8A+JyYnyqo\n" +
                "y7JW2laNXJ1RKydj0SNIetUKzR5O/TVGdbKEAwaCFYJjoKoBL4KW0T2spaAmhoGGwGL9wBcEAP8A\n" +
                "AkPwUCbRpDI6puzNIAOmM1LXBd+Tkd1pFKTQHUuAdu7MFka5VlrKvKaBTeO1ohwZtaoQHO7RcJtG\n" +
                "jhGWyHGvAVoppvkfdZz8McZNP4Ca6b0O9GUJ9yLDR6FfuMVALyH+QVQmtoYhvktLyQ9PaNVwBnKk\n" +
                "rHjewkiIup2B0SBWuAW4htPaKiNqw7nZrFeWh9kXppGtRnDJOE+6EpJ/Do68P1XqsT3Lvj7SOf0o\n" +
                "bpNfuS8Xsx6HrYvrkf8A+bA1/wC1nZi+o9Jl0sva/wD1aPm5Y5J1V/gmpJNST5J4xX18JRmrhOMl\n" +
                "8Oyt1wfIRyTx/wDHOUH8M6cP1bq8Kr1O9f8Aq2YsH01h4PGxfXVVZsL/ADE68P1Po8q/5ex+09Ew\n" +
                "13edDWzPHkhkX9ucZfhl7RKqrVAiLtlLTAK2OkK9AmA/Au2xggF2ryhKG7KACHBb0T6WzUAMJY1x\n" +
                "RPo3R00rCguuV4nbvRKxNc7OxpPkTimqCOJ43X5DsaR2di/gn0kvkDk7WKKo63i5IeL3CudPY1I1\n" +
                "eL+CezfAEJuy06F2pcCa3YRfdXkqMqRi7H3PSIOhTVcofcjmt2UpUtFV0A1swWR+5SyfbVjUaVXA\n" +
                "rmndgp+47RRPe14D1Fe7RTSqifTRALLjb/UWt8NMwl098bM54px2r/YqusSZxp5I8OQ1my7Tf+gO\n" +
                "tiOaOea06aNI5r09BHxVktXsa4H4NDNqmCZUkmSgDkfyVGPdwU4SS2gjKxrZpGOOXOjddNj7f1X+\n" +
                "AOS9FOqRu+lXd9stfJnPp5xhfKXkDKtlJWODq9Wa4XGUnGgsRqqcRxx42t2jr6fp8U39y3+THq8P\n" +
                "pZap9r4GDJ9OlByhkTSMUmelLpYPA5xk1rRz48bx1JtAcyp7Q68HZiXrZe1wTiuTeXRYZK0nH8BH\n" +
                "mAd76GHdqUjOXR212S18hXKnspSVFvpssVtX+DPtadNNBMXGSS5KU4+5jXIVfAR0Jq+R2vdHN2sK\n" +
                "lZNVu4KSszlGtLglSmlStDU3dtWUNNx4NI5E+eSO6LVkPb5A6k7BswjPt09mykpR0FIJa2MJK0A+\n" +
                "Yo0jxyZvg0x/oCJlGzJtJ0zoa0LHixy/VthRHt7eTS3WnY/Qg3SbRP8ATyT1NUFWm2uBrm6M+zLH\n" +
                "af8AsXfkjqUb/YupjW70EfgzWRcuI45IXt0NRe72xp3phJptNNCS38jVwdsXzFMl9Pje4tr4G5UO\n" +
                "M1wQYvp9XGX8kvFJcq/wdSaasadEwcanPHK4SlFr2dHb0/1PrMUUvUU17T2Gm9xRHpQ7qqrKj0cX\n" +
                "1qNf38LXzE7MP1DpcytZO1t8S0eDLCq0yVBrQH1CcZfokpfhlcHzuLI4PU5LXhm0eu6jF+mff8S2\n" +
                "Zw17idjs8WH1tqfZm6e/mLOnH9Z6Ocu2XfD5khivSCzDF1GHKv7WeEvwzVWTBXgCUx2MDAm9lADY\n" +
                "vAMYAnsCbHYDoVAACqw7NDQMCHG3wT6cX4NQAx9LQvS8G4UFcvY1poSg1Z1UqFWiDlcWJJo6u1cE\n" +
                "+mvBYjnTdD72jaWNMl47fsAlk3dFLJsl4nZPZJcoDVSG2YvuTrY1NpFGlR8ol44N2kClfI0yKh4o\n" +
                "rwhOMWq4NH+CUtaIsfEcIcXaJkODpnRk2tipMpu2TYF+m4ruhs0jO3XkiGRpEydyuIHT6MJKmv4J\n" +
                "eBpXB7RMc840+UdOLJHLriXsUYLLkxq5Qs3xZ8eSNaXwwz5FgpSTdnFlcZTcoqr9gjqydNUu/HVP\n" +
                "dGmHHDLjqSaa9jkw9TkxqnUvz4H/AFM232/bfsMF5cawZO6E7aLySy9Rjj3Q171Rr9PhHLGUp/dJ\n" +
                "Pyds4KcHEivLffBds5a8HRPonPEnHJ9yVpC6zD29rftR3xpRSrgDw1PLhm13OMlyj0eizvPDtl+p\n" +
                "G+Xp8OZffDfuViwY8UahBIIcoJStken7M2exV7Ac8oNWRLDCX6lT+DspEPGmwsee+mim6IeCKeo0\n" +
                "ehPHS5/0ZvH7gxwenquBel7M7HhtKnZn2VeiYjmWL5E8Mm3tUdDVCoq4x9JU1f8AAnhXh7N+0VAY\n" +
                "ejLm0yKcX8nVTSJ7E7vYGcMlPZfKtcEzxeU/5HiTWOmEar9Jpj3G7MluNGmLQFNasyfJuzGXJqFK\n" +
                "M5J1fHBUc80/DXyR54AYa2Wd/wCUV+xSzQ1bZz2CJhrqcsctSaYPDjlwv4ZyjbdctfglitnhXdp1\n" +
                "XuJ48iVqS/kzjlnz3X+RvqZJbUWRdOUZ1UosiWtXTNIdWq3Fo0WTHKNypX8AZY21Hbs07taGo45P\n" +
                "Uk/hEvCuUwNFJtcjT9zJRyJ0pJg55IviymNu5BZh6urcWUskWTTG1IPJipVwx971saYjqVS76r5O\n" +
                "e7ezqzTTxOMk9nIkUXGSvivwdWHrepxP+3nlXs9nKhrQR62L6xnjrLjjNfGjqx/V+nnUZwnB/g8J\n" +
                "bY782B9Nj6np8v6Msf3ZrvxtHyqd8m0c+aFenmmv/wAmTB9LdDs8TF9V6iGpRjNfwdMPq2Fpepjl\n" +
                "H3rYweknY6vycuHrOlyRuOZK/D0dEZKSuMoyXwyYKE1sLfkLAAvYBVsgLAKDklAgQkgTED5ALEAw\n" +
                "q2LwHBQUAxAAtBehWUDSsTivYE2uTLLnq1HwUXJQjbk0jD11bSVoynJzl3SeydK6GDqWaHDdMpST\n" +
                "WpI8nqevw4U1GXdJPhHmZ+v6jPJ1Nxj4SGEc0oRWNNvZlw9F1KtszdoocpN8lKLcbohXfua4ZuPc\n" +
                "qAvp1Fy7Z8M6cnTQf6FWjhb3ov8AqcsYdql+4NGSKg+21ZFuD7l4M7bdt7Ku40VHa5x6npbm6cUc\n" +
                "TTXyLaNJOKhpWBmrLUdCizXpoKeVpuklZNHV0GR4G45I1GXk9JVVp6Z50MkPU7HUo+51Y5dipbiF\n" +
                "jTJjWVJS2k7RdcjXuOtgKvI6opCqwiaBFia2AvINeSq0C/T8giWrJeMtLQ9UFc7xu3rRDhzrZ1Ne\n" +
                "SXGwOGUNUZyWqO6ePV0ZOC4aIOWiTeWKotpmbg74AhcBQ+2th4YgihpLtfgbQVoomHBpivgzhu15\n" +
                "Lgn3fuVGr4MpXZs7Mpc/gsMSIqhNUyhUAJWFb0MCC/cbF42ZsISvwDK8aFSI0lKmWwSQfIQradrQ\n" +
                "/UyLib/cGSyEaLqJq20mWuoj5g7Oavca2B1xyQnHeh9mN7ST/DOaPGi1a/8AkYrR40uGyeyaX6jO\n" +
                "U33XfIetJapDEE/UapohaZoptqiHdvRVCLRMV8lEQ0wukJbH5KLTC3ehIaYDTfkfgnjQ09k1ZFQ4\n" +
                "2a48ksbbjJx/DMk6Dbek3+AO3F9U6mK32yS9+T0cHXQyxucHBv8Ag83B0qi1LJv4OnjSRUejGcJf\n" +
                "pnF/uUmeXaq+Bf1WaL1kdEwetYHBi67J/wDyQjXujRdfg7ql3L5rRPEdbEZwz4Z/pyRZpGpfpaf4\n" +
                "YwAAxWTA/Ar2K7tAA6FoTZnOHeqUmn7gaicoxb7pLR53ULqsCba7486MIdfiesicX/KNQd+TM5P7\n" +
                "dIxbfbo5cv1LpMatTc37RRwdV9Vy5bhhXZH/AGVHp9R1eHp4d03cvZHk9T9RzZ3UPsg/bk5d5ZXJ\n" +
                "ttnXg6GWSNyfbH5JWpHHGHdL3bO/p+gcknlfbfhHdi6fFiWop/Jo3SpEXXzrtojtbRoNI0yy7WmC\n" +
                "jK9M2a2CVbAyjjko20Pt07NbtUxdqrgDnUTTJinjScuGV6aNJ3NUwOXxyVBObrgv0vYlYpJ33BFw\n" +
                "wTnPtTWvc0j0mWUpRhTlXuRBzi3T2zfpcssOXuknJPTA06ToJLHL1HUvCPQx4lDGovaXkiPWdO9d\n" +
                "zT9qHHqsHmf+gpvuxpuMbiXjnHIri9+wv6np+1/3YnPkeNPvwZUpL2ZUdi5sEc2DrIy+3LqXv7nT\n" +
                "FqStNUQC+RsIxdC3dMoaduq4AGmCIDjQVsGgqgDnQNeAQ7CpqiZQcvNGgAc7jSqiHBctHVVkvGvZ\n" +
                "EwcE8a3XBlKFHoyxK6SRk8dv7vBVcLTDXk6pYk/P8GMoOLerQ1GEP1P5NIfr2Zr9TNF+r5CNTOfm\n" +
                "jV7Rk1yaiktIQ17BxyNQqEymKhoQq2MCWqQkVVIEZDig4ChukgM+HoGN7CiqlAuQqgS2RFQ5NPO0\n" +
                "RFo0KIml4RlTNpcGT5oihPZRKWiv2GqaRV+CUP5GsmHgSuhpWUUuCle9EJ+xaZFlSVGhFRi1tqxg\n" +
                "36fpsmeWvth7s9TBhhhh2xSfuzix9d6aUXj+1Lwax6+L12tfkqV1ShF7MppR8k/1PemlSYK3tgZy\n" +
                "jKT0xem487NnOEU2YtyyPTpAZznUXa2RCMpbtV7Hdh6en4/c1WHHX6F+UByQhS4NIaquTf0Y3yyf\n" +
                "Re62BKnOL1J/yXHPNcysiUJLlUS7A6cfUXzFL5G88O6tr5OV60CJiO3u17h4OVScVfdSOfJ9ZwYp\n" +
                "djTyP3iTFepZzdR0fT9T/wAmNXXK0yOm+pdL1GoycZVdSN3mxqNuSIPE6v6FOLlLp8ikue2XJy4u\n" +
                "gzd9ZIKK92e9kzKT+3g53tmlY4OmxYf0xTa8mtaoOE2zPL1GHDG8k1dcLkI0fwYZuoxYFeSS+Eef\n" +
                "n+p5Z2sMe2Pv5OOXdkfdJttmo1FryP4LePYkq5MaidLkGikgrRoJ8B4H2+43ACRofbfA1HdBE0HC\n" +
                "KURuAMQh8D7Wh9vwAkt2O9aBp8DUXRNC/KBP4HVgollAm7salXloVNBQMNSktqUl+5ccuSPE5fyZ\n" +
                "8IavyExsupzLib/fZa6zNTX2/wAHP5AGOr+typaa2vKKXWzX6oKX7nGNBXauuhpSxyX4K/rsL/xm\n" +
                "n+DgrYedAelHqsD/AMnx7FLqME3rIl+TzE98g+eAPVWTG77ckX+5Sp8NHkargStPl/yDHsNC7L5P\n" +
                "LUpricl+GV6mT/8A0l/IHbLHvgzyRaxv8GEepzridr5Q5dRkcHF9u/NAcjVTdlx/UmJw+67HTtAa\n" +
                "W27E02wutAnouhduw7SrF5siFWhU1wUOqXJVZNfAqNfGxdq7dGVxn4GkV2hQMJa2D/IP2FRUKga0\n" +
                "PwCugIoGqKoWiARoiCk7QRVWmYyXk2XGzJkUkMP2GkFAwEIGlzRSVInYzSKtF4cUs2RRi0rM4q2V\n" +
                "GUoP7ZUB6cOgw+mlJu/cUuhj/jkf7nFDqMy2ssm/nZrDq+o/7J/sBWTppQ8p/gWLC57ape7N4d0o\n" +
                "pz5ZXeqpMCsWPHji/gJSUk1Fkds5t+x0YsFJNrwBnjwTm7clo6Y4FF7V0OK7VSL7gDtDafA0/cdx\n" +
                "5bSCanyK+SZ5N6MnJ7YVeSdrxRhNvVMb2Y58+Hp4t5JpP28gWrbMc/XYOn033yXhHm9T9QyZYuGL\n" +
                "7Y+/k5sWKeaXbGLk/cGNuo6zP1T+59sfCRODp8maSjjjfu3wjuwfToxffld/CO2KjCKjFJL4KOfp\n" +
                "ujj07cpSuTVfB0WuAbVEZs2PFDunJJGcVabIyZsWJd05r8HndR9Td9vTRa/9TOGcp5Z9+RttlHb1\n" +
                "P1PJN9uFKEfnbOJuU5XLbfkqOPelf4OvD0cm33pxTQXHGoNukrOrF0k5PaUUdmPFDGvtX7l8CRqO\n" +
                "WWFrdGTxS3o9GrVOgWDuX3cMwzHl9tcoXY2z1H0i32pv8mEukkmqa4LqOOmvAUdMunmuVb+DKUJR\n" +
                "1JUVcZr4KSBqtUFUEwUCHHgPihDAh0kLjgFYCf6rK5QrVjICqQJbG98C1WywDSYu3ehj8lE9uh0O\n" +
                "/IwJSYU64KTSYLb0ExNX5EUD/AXCF5spfIqQAh+RcBurBA1QXrQWJc7IGmHkBedE1VJ09B5FewNI\n" +
                "puw5I3ZSdIB6sK2J8BdaMqdDSS4FarkL+QDtBqh26CyoELfsO/kFyQLzwFFOqE6KqXXsMpJWJ1sl\n" +
                "oVL+RNIf7guRKYjt9g7S6BJFRn2tDii2LgJgfBnJUzTwS1bIsiUtDXwPtCtlCAbVDUfDAS4FspLw\n" +
                "OgJ7R0yqs6MOBXcuPYqMYY5PlG0I9ts2cIp2o/wXDBKf6k0grH1Jy1Fs1xQd2zoj0q8fwV6MkuEE\n" +
                "LFJRe0bLKq9qMvTl/wBWS7XNoDpU4tfqRdHE5JcspZZNV3OgOiU4rzwZSyOXBFr8sTpRbk0kt2wK\n" +
                "tsieSGKN5JKK+Th6n6pjxrt6f75e/hHmzy5eoyXkm5N+AR29V9UbuHTKv/UcHbkzTttzkzq6b6fK\n" +
                "a7sn2r/Z6WHFjwx7ccEq8hXD0305v787r4PQxwhjgowSVDk7WmRPJDHG8k1FfIF88EZMsMUG8kkk\n" +
                "kefn+p8x6fn3aOGUsmWVzk5P5CO7N9U/xwQ/eRwTlkzS7pyb+CoY25ai2deHonL9bUUvAXHJGFtU\n" +
                "nfsdWLpJTqUvtR2wxY4QqEF+S/8AGqIMsWHHjX2x3fJr4H5GuNlEDVUNq2h15C66lgjGV02N432s\n" +
                "6O3Y+1M5suWnX4FGCfKOrtXtoOxJaQWOV4U/APo4S0469zqUUVX2N+xVeXl6OKtKK97OOfTzT3E9\n" +
                "hJybb8hKCktoprwpQcZtVQl+D18vSRk21q0cebo8uNLtj3L3QRyqLfAu13s07XF7tDSXI1cZ9nny\n" +
                "EYtmoccE1cZKL5H2Wy6H5LEYuLuxqLs0oF8jRk1yKqRskvYTjaAyoODWkKk2D+M2wts07RdqCM7Y\n" +
                "Wy+3egaLohtitvkvs1YJDSI2G0a9u6oFBv5AzW9CrRq8bT0ilhnp0qM0YJAdK6eeqX+io9HlltpR\n" +
                "Xuyyjl27oFa0ztX0/NuqqtMr/wAflu68DyHC1oXk7/8Ax2Ru+5KvFEvoMi8r9kTVcSbC96Z2/wBB\n" +
                "kp20hf0M6/VEDjTfuO/c6X0eSPi/wJ9LkUL7JFMc92NP3No9PPzFr2sF08rqv5Aw/AXs2XTzStxY\n" +
                "LDLlppE0ZdzsLNHjq9CUHf5+AITHetFvG1+41ik/0q/wTBl5G+Db0cvHY7D+nyNai3+xRgg/c6P6\n" +
                "XJu4v+AfSZltQ0Uc/gPwdH9Llr7oNfgf9PO/0NfLCuaqY/c6Vgn5iP8ApslNtKyajlpt8F98v06O\n" +
                "iPTzStp3XsXHA3ScX8so4u629cCvZ6i6eNVSf5RSxwivtgv4Go4MUoqtbZ1wxyyOoNfNnVGEa1CK\n" +
                "/YtRS0kl+wlRlj6Rrmn8m6g4OkhpyXkTlk8bLpp8D5WzN5nH/kxuvdEyzxmqx6KNXOMVyc+Wfdeq\n" +
                "E7smrdARXkaVsjPmxdOm8k1fheWeZn+oZsz7cb7IP25A7+o67D09r9c/ZHlZ+qz9R+udRfhcBg6X\n" +
                "JnlSVL3PRwdHhxq5LukvLA4em6LJmXc/tjfLPSw9LiwbjuXuzS2kTPIoJvJJRj8k+qpv5Jnkjjjc\n" +
                "5KKODN9SSTjgjb/7M4sksueXdOTZTHZ1H1FU49Ot/wDZnDKWTLK8k3J/JWPHb0rfsjsw9HJu8ul7\n" +
                "ImrjkhhcnUVbOvB0b5m6XsdmLFDFGoKi+dMmiIY4Y1UI0UlsdDpFQkqYNcj8cArKgXAVdlUFaIJS\n" +
                "opewUlbBfgD0wSItPhlRM4HWuQ/Iw0MCob3jcfcGFBWcYjrZpWuQ0wkZteRdtmtaF4KObJ00MkWp\n" +
                "LfucOboZ49w+6J66jsTiRrXgTTjppolzils9zL02PLBxml8M4Mv03Kk3jUZK9e4NcVoLOj+g6pr/\n" +
                "AI/9jX0/qLqSr5LFc9+4u5dumdcfpk27eR/wUvpjSalL8UEcPcJSbb9j0P8Axqqk3fuNfTZfn9gP\n" +
                "Ou9IX3LhHqr6Wl+X8lL6ZBS22/3GpHk7S2xLd1s9v+ih3V2RZa6SKXCX4QHiRxye+1mkOmyyeoM9\n" +
                "v0Ir3sFjV7RB5MegyTdtpKzT/wAfGL/U/wBkep2IfYm9gecujit8l/0kee2vwd3ZH2GoKwOOPTpc\n" +
                "L+S44K/xR1do+3ZBjDClO0jSUFxSLqhNCQZuK9iaZqlfgFGyjJrzQqfsbdjfgOwDB4622L000zo7\n" +
                "AUEDXOsaXCE4barR09gdgVy+lfjgTw3ekdnb7i7aA41gSVbob6ZV5Z19vuNrRE1xLpo7uJUemh28\n" +
                "U/c6aCimuePTQ/6JmkcMEuEl8GvkPINZrHFO0V2JeCtE91IQHYmhdiapofqJLgn1o+bBqvTXHsL0\n" +
                "U9NJolZ13XehPqY750F1XoQ0lFL8B6MDP+pjy7E+qrjQxGywx8WL0Ulujnl10VzKn8Evr0t9too6\n" +
                "ljiuEHpJeDi/r5ONqCE/qOXxFDEd3YDicH/kMr9k/wAFR67f9xV8oDsaaQvgzhnx5HXqR/dmsbb0\n" +
                "0yGEvZkZOkxZV9r7Je6NaKjxwVXmZ49f0qbillh+L/8A+nlZ/qfUSn2qsdeyPqN79jj6rH02SVSx\n" +
                "QlL3o0j52GPJnne5yfk78HQQx/dmfdL2XB1xhCGoRS/BM5xgnKUkkvcBxqOoqkgnKONd05KKRxZ/\n" +
                "qUY/bgjcv+z4PPySy5pd2STf76Irvz/UoxTjgjb934ODJPLndzyORWPE5SqMW38Hbi6PzkdP2RNX\n" +
                "HDjxN/bFOTO3D0Mmu7K0l/1R144RhFKMUqHuzSpx4seNVCNF0xpVsN2RlL+WOtDoKvgYoQJDSpDW\n" +
                "whVRQVsCobQkhjS8sgWmFIdA+CjpSHTOP1ZJL7n+So55eXYHUm7Gpz4s511L39qKj1EHzF8DEdCm\n" +
                "3yUsi9jn9aD81+SlKP8A2Qw1ssi4oakr5MU4tWmhp2TBsmvdD8GLVhv3GK3+AMFfuylKSQwaBvgj\n" +
                "vY/UJgtMdszU15KU4vyQV3aF3C9TG5dqmrXI6T2igeSPlB6uOtyoUoJieK0Av6jDe8lfsw/qOnv/\n" +
                "AJomcunX7Gc+ji98EV0f1PTf/wC8P5Guo6Z//wBjH/J52ToWm6uvejnn0bSvb/CKj2llwz/Tmxv8\n" +
                "SRfaq00z519LNeNfBMVkhtSkqfhkWR9Ko/ANKz5v+o6hSVZ8n8s0XX9XF6zP+APf0PXueJH6p1cV\n" +
                "90oP8otfVsy/VjhJfwEeva9xpr3PKh9XS/X0/wDDLX1bA1vFkX8BXpXFeULRwx+pdK9SU4/lGkOs\n" +
                "6SfGZL86COuLguZR/kpSh5nH+Tl9TpZfp6jHf/uRSjjmrU4yXwwOi4t2pL+RpXujm9H2F6M/DoDq\n" +
                "r4BI5e3Lx6ktfIL1YvU2B0+4kjnc864bf7C/qM68R/gg6qJaOddXO9xi0P8ArEv1Y3+zLit6FXsZ\n" +
                "rq8TW1NP8FR6nC3SlX5QQ6fIUP1MUtRyRf7jSTWmv5AhccGcpO+Dfta8CeO3sDncpbojudM6vTV8\n" +
                "IPRjWkBxPuIafNnc8K4aD0Y+EkTR51S9ie2a8HpPCn7fsL+njpBY8yUZvy7M/TyeU6PX9CN3QLpo\n" +
                "lV5ChJ6inYPHP/qewsMVehqHskQeP6GSlrXwOPTZPNL8nr9nbwkhdrrRdR5a6WV3Yn0mRvxR6bg/\n" +
                "KIkmpNJDUcUOktV3U+CZ/TM6/T1SX8nbG1kWjWeSEF90v2KuvGyfR+ok7/q1+9jj0WfDt9bJ/Gzu\n" +
                "ydQ5OoLtRi3SuWl7suIItqCTlJ/LYpVBOUpKK92cuf6hig+3GnJrz4PPy5svUSucr+Cjtz/UYRfb\n" +
                "hXe/fwcGSeXPO8jsePFKT+2NnZh6Jc5Jb9kFxwxwuU0oxt/B14+i49V18I7YwhBVCKQ6VksNTjhH\n" +
                "GmoKkyuXYdrHQBQlzsYKgD5K8AlofhAKvgEiuRccCKQ1w7GCKgQVbsEtjCDQIErYAAAPVcD0OZWV\n" +
                "eh+nJaBxa8P+AFbHbXAU/CCgguxx0IAYalRSnKtNkD8FGkc2Sv1bLj1E0naTMAd+AOhdS6+6Kv4K\n" +
                "/qN7icy5C7A6/Xi/DGssH5ORPdBuwrsWTG/80NNP/JfycYKTT5Ii8Lvr5L4OzaPJxdy6ly72r+Tq\n" +
                "WWXib0B2qUk+SlklXJxrqJ+6Gs8k1dPRMHYp+6H3Rs5F1KT3FstZ8b90MHSpRa0xdvyjKE8cv0zX\n" +
                "7spK+H/AxVPFFrgzfTQbfH8GiUkCb8kw1yT6Fc0mjCfQX+lHp99eA7920TF14v8AQZd0lr5E+jyV\n" +
                "+mq5Z7b7H45DtXgmGvE/oZpO3/An0cvZ8Ht+lFk+ik9MqvEfTZFX2sl9O1t3/B7rwuttMTwapIiP\n" +
                "D/pp9t0vwHoTTa4fsez/AE7t62P0HttIo8devBVHJkSXs2bQ6jrI8ZZv87PR9BNNtBHElwiaOTH1\n" +
                "nWLlRf5RrHrM7j92OD/Y6FjS3Q1ij/1RRnHq7X3YmvwzRZ4S/wAJL9hxhFao1VJUvYQQlGS4ol4s\n" +
                "fx+5raoH2NboDB4Yv9MkS+lfiRrLHCXx+DN4Jf4ZWqIM30ja09/Il0mRcOq9i66mP6Zp/kl9R1MG\n" +
                "++Ef3RQnDPB/rm/wyvU6iG+9/uKPXb+/H/DK/rcTbThIGJfV9Qr1F/saQ62T1PGv2YLqcEuZJflF\n" +
                "+pgbpThsiGuqxy/xmn+CvVg+HX5M16L0pwv8l9sXVOP8lFdyfDB2JY6Ton0370FNqTdpkd075aK7\n" +
                "ZLzYux+GArye7FebjZX9xcB3ZFyloCe/Le3/AKFkzzxY3OUoJLyyrk3bRx/U+ly9XhjGE4xUXtN8\n" +
                "lkHVg6vF1KqGbHKXsmayko/qPA6b6fLFk7pzjr2O6323KWl7sYjoyZ421jX7nPNtruk6+Wcmbr8O\n" +
                "PUH3v44ODN1GbqH97peEjSyOzJ1+LHKSx/fL38HDkz5s/wCuf7IWPDLI2optnbh6BKN5JbfhBccW\n" +
                "PDKcqjFtnXi6GmpZGq9kdsIRhqCSQ15M0RDGoRqKSQ6H4oDSFQcDGlfJESiktAkMA7dCSTKBL4Ks\n" +
                "CQAOkAkgoa9ga2AJBWgS9gqggS2FlLQasBB4G0he4ANoBomB1se2qC0NNXoBKK9hOCfgq9jKM3jT\n" +
                "d/AljVUaJDSAy9Fb2L0XXKNqDkDH0ZbWiVjfk6F8hQHO4SXhi7WdNBVIo5afswdnT2poPTjzRNHN\n" +
                "+QfudDxJuyZ40oSafCCVwdPO+pe9bOo5eij3dR/+LO70XXKAz42PdFenKvcHCXlATfuOxqG2kKr8\n" +
                "lC4ehxbXDYVsVuwL9TJx3y/kuPUZY8Sv8mXlgB1LrZpK4Rf4G+qhV9kjk3Y7A6l1GPypIazwb8nH\n" +
                "5BNjB3LLB8SWy1L2kn+5wN6En+xMV6O0Hc/B5/fLjuf8lLJNcSYwdzlKrY/UaXBwLLNa7mV68/cY\n" +
                "O5ZF/wBQ74tcM4l1EkrpMr+pV7X8EwdblGguC8nOs+OWnaH6mNr9YwdC7X5QmtmPdHlSRS8tOxhq\n" +
                "+1sn071Yra8sfc7GLo7KDtdch3PyLuJiGoyrkKF3L3C9cjF03ixy/VFGMukxt2pNM0p3yK2TDXPL\n" +
                "o5eJozl004PdHZb5BTflExNcLxNLgIwktpNHoKcP8lX7BFY3vSKriSy3py/k1j69J9zOl414QdlA\n" +
                "ZwyZP8jVSdbOTr+pfS9JLJFJyvVnHi+uR7Es2F35cSyD1pTSTbdE/wBRhSp5Ip/Jz4euwZ8fdFS/\n" +
                "dGWXsySbUIr5GDrl1EFSX3fg5p5ZSdPSOPP1uHD9q++S8Lwefm6vPnvulUfZFHoZ+tw4r7ful7I8\n" +
                "7P1ObOqf2x9kRjxTyOops7cXQpK8jt+yKuOHHilN/YrZ24ujVf3OfZHXGKj+lJDa2E0oQhCKUYlu\n" +
                "qBcaE0Q0LgBrgXLASHRSSFVFQUNc0C4GiBVsfkPI/lgJoAH5KCgSGFAIdb0L9x+AQhggoKACvKAI\n" +
                "GC9gQcMBoQw8UA1RS0Wo6F2kgmk9hTsvs9gcfYojY1dDphTAWwTHTFT8gAwDwEAWvcOQpBTVe40r\n" +
                "XJNBWwiqE4ppp+VQt72G60wOLpMTx9VO1qKo7aCK592VdKgElsEvAX8DTvxQAKk/A1Q04gQ4Lmhe\n" +
                "ktmgqCs/SjzsXpL3Na2DQSsfSYnjlejfhAtgc7hJci7XR1VYtewHMJI6qJcU/AVzgdHZFqqE8S5Q\n" +
                "GHyBt6Lrkn0ZV4AjYk/gtwl4i2Ltd8NFCugTpB2vehcEFJ09DU5riUl+5HgPARqs+RcSv8l/1M1u\n" +
                "os5/AFV1LqYvmDT/ACNdRien3J/KOOwTIO71MTWsiH3QpffH+Tz06Y9BHclq0FM4VJpum0Us2RcT\n" +
                "exg69i7qs5l1E/Mv9CeebVaJit3kitcszcnLkzTcqY5zUYtyfBRayTX6ZMMn1KOBP1kpPwlyeX1H\n" +
                "Xyku3CnH3ZlixZOyWXJpc2xiyDruuydXO39sFxE16fo7ipZXp+EedKbbtKtnVk6zLkjSl2r4Cu+f\n" +
                "UYOmg4KrX+KODN1mbO6TUY+yM8eGeR/ZFyO7D0EY/dldv2QJHDjwzyS7YKzsw9Ao/dllfwjrjGMN\n" +
                "QSSKqyCVGMY9sVSH4GFeAgoTKvwJlQkMdaBIgNAhqI1ZVTRVaBDCFQIY/JAkr2A6FXuAh0NAAcML\n" +
                "CxFUUkAO6D9ggqwehrQNaCl4CtDQAAMaAIS4HQCJqrTfuNy9iVfsCvyisq7n7DUmRexqVAXY+5EX\n" +
                "sLCtLVjuPujJMdgaKg7VRmmEeXyEadqF2L3JuqH3MKfbsTiwU35DvSWwg7WKn7E+p3uo8GqoCXFp\n" +
                "8A1TL7vA7j5AzHyitDSQGdAkXSQdoImkFexXZ8i7QpeA+R9r8BRAt0CsHafA+ShX8BaB2gWwC0w0\n" +
                "FAl7oBi2NISAdCqg2GwgXImrHv2D8kUVaJ7IvwirQa8FEvHBvaJeKHizS0KkBk8a8MHi/BtXsKvc\n" +
                "Dn9NicJJ/pZ01sGByOLS2mJaOtaE0vZAcnIl5+DrcV7Ih4ovx/AHMOFNmzxR7dMXppbAS4M86axS\n" +
                "kml8s0m448cpviKs8jP1WXqU48RvhBqNsT6TCnNyU5+DPqOqlnTgl2w/+TnhC5KNbZ6GDoIqpZd/\n" +
                "BKPPWNX+Dt6TpY5F3z/SuF7nW+lwN32f7NYwUVUVSQlLUxxxgqhFRXwPzooGVGa54KAXkIGF+w/c\n" +
                "CQFbCqGkFFCrY0NIpIgSsfgAAKCvYYFAkABRAr9g+BisBgIYEpbHQ0BRPgaH4EQAXfIeA58FAPkX\n" +
                "gOAGAIZAIVDBrygG37DMPXjf6XopZ4P3RUa34DRmssXuxrLDzICx0iVOL8ldyekwFS8MfbsEOgoU\n" +
                "X4YqYwCElKg2vA0xSbrQEuVcqjK3klW6Nacnb8Fr2ASUYqkhp+w18jpewAuBMaSbFSvkEFse62FL\n" +
                "ww7XfIDsLonaHvyA+5h3Ct1wJPWwNO4FIzv2HeiRWnchaohMFwVF6eh1HwiLHYU+1B2oV0CbAfZ8\n" +
                "h2i7mnofdQCcWgp0PuGpIIz4CzTuiDois/yCSNFTF2oolpCpWW0g7NWBm0G6L7RdrQE7E2VTCiCb\n" +
                "+AHQigtNhoQmtAHnQmtWNJA9RYGWTGsuOUG67lR5sfp2WORp1Xuj1K0BK0wxdLjx1JK5LyzWiheS\n" +
                "JooKEMsQpE+RtXsRQvcK0VQJEUhrgfbsdFQq0Oh0BAUCQ1yHkASdiHXyHkBIEN8gABz5FyNALgB+\n" +
                "QCkCAAgDwAAAeLBoBAg+RoChUwb0MAAFyOgAEHkAXJBxsa4HQUUALYLkPIQ1oO75ARRSk9/cylOX\n" +
                "iTIQ0BaySv8AUUss/gzEkMGvre6/gp5l4TMeEFgb+quaY1li97MAQHQskWtMrujV9yOXYIg7ItNa\n" +
                "aYVfByJtBb+S4rrSpj5OVTkv8mNZZryRHQCezBZZfA/Wdfo/2BugtmKzr/qylmXlMC18laZmssWi\n" +
                "lkh/2QU6XsFJ6EpxfEkNNNaYAopeQa1yHuAAk65DYAAKw8jsAEFoYtXwALYLmg0AB5GLtp8jr5AP\n" +
                "3C2Kn7i2kEVYX7ivXAr9yKq7C1wT3CvZRdx9gaRNhxwQOl7E9qbDnQroQDj7A1UQbYSb7SjMGPng\n" +
                "RAn8C8lfAkkiA8C8DD4AkKCtjo0BcBQwIAaENAAfgBAMAGUC4EOvYAEDqgQUQCAKBKyg5Ct2D0AC\n" +
                "GgAAoVDEAANcCrYACGAAtAFDSAFwCVjXsFECr3BL2GhbKF6WN/4k+lD5NaBL3Ay9GPyHor3NaGo7\n" +
                "CMPRt6khPA6/UjooEtAc3pTX+NoPTl/1Z1V4BIDl7JeYipp8HXWgqkijkph4Oql5ig7ItU4oDlrQ\n" +
                "U6On04eYj9KFcMGOZBRv6MfFpAsK5TIrAPJt6HyL0mi6jKgRq8UvYXpyS4YlGYx9kv8Aqw7X7ALw\n" +
                "CsGmHgAX5DlhQ6IpLTG2FaFXuVDtpcspTlW5MkAq/UmlpjeZ3b2Zi8AarM/ZFesv+rMfAeCI3WWP\n" +
                "s0NZIswvQkwrpU4f9kHcl5X8nMHD4COpfyM5Ltj7m/LQHSBz+pJLTGskvIVuHkw9WS8JlLLq+1Aa\n" +
                "2GjJZdW1/A1li/DAvXsKiVki+WNOLWpIIdbE18jtMTIpUwd0MTqmFSDAXkILFQwXAwIBiAAAAoAa\n" +
                "AIKCgB8gAkrH4H4AS4GGrGAroNB5HQE7oOBh4AQw/AgDyFAikQT5AoXnRYFoBhQBQcMYgEuQoqgK\n" +
                "F5DhjoK9glFDQh0AAABSrQ0PtaY+1kRO/cN2V27oO1lIW9grKprkKIpW/YP2HWx1ZUKwtD8ioA00\n" +
                "GvcdIVIpDASWh0RT7QoSVeRhAAbDYAkAtjt2ABVrYN/AWFKl7C7V5SK0GvcCPTi/8UHpRqjTXuHk\n" +
                "DL0U/LQekvc2qwS1wBz+i1exPDLjR0UFOwOb0pPVB6ckuGdNBQHN2PyiWjq7R0ByJe4Ujq7VfCBw\n" +
                "jfCBjlCrOj0ob0L0o+FQHPQVZ0PDFv8AUyfRf/ZBGPDDg19GV6aE8U/JVZcB+S/TnX6QUJpbiwIC\n" +
                "ilF+wnogWxIe/AUEK9UCbXDaCgAffL/sylNvTICPIVohoQEDASHyUAvA6ABMKH5CqIBBoA8ACAPA\n" +
                "UA0FbGtAtsAoTHYUAkMXAfgAphVMBgGhBoaAQwGUTQLkbEiBsK+Rh5ABMYgBb5CtgmMoKAACABgF\n" +
                "LyMBIC+4ruTVeTJME9kRopJjVckIdlF2mGiFyMC9chSM+BpgXSF2pk2xpgV2i7VQW7DuKH2+wu1+\n" +
                "ATKUiCe1h2urKUgUtgT2sKZV+R2nwBFCNFXBMs3TwfbPPjUvZsKnkaRUJY5pvHOMq9mV2qgM6Ci6\n" +
                "VAogRQUW4qtA4ewEV+Q/cvsdA46Ah37hsrtYq0Ar2O37Cq/A0AlLfA7XsFBQBqwtXyCQ6QCrYUht\n" +
                "CoArQKLoAp+4BQNBTXkNryAqCh/d8BbRBLiLtXsVd+A1XBRHavZC9NNcI0tewrQGfpRvgn0oe7Nm\n" +
                "KgMVhV6YvTcfY3rZM+CKyodDYtgKgHQ/AQloEMQCYcIdB5YCGkCGAqBcDoQB5AYtWMDALAAEHka4\n" +
                "AXLDQwoBAOgSKAKGABXuLyDVAAACGAeBclVoQQkMKCtBQkNCXIwCgSAaAXkAB2ECSCkK1XKK8fIB\n" +
                "SsfaHO+QQAo65Dt9mMQBT9wSkvYrwAE1IateAXI7AW/YFfkY/ACC9lXrYlyAm98mU+pxQ1fc/gw6\n" +
                "jI82X0MaquWbYsGPGv0py92UR62eb/s4/wB6JcOtnzJx/ejrTrQd3gDil0XUSjvqP2ts8jqMLw9R\n" +
                "PG9uPk+kT2jw+pqefJK3bZZEtZdHmn0+eM48N0/wfQxkpJSi7T3ZxYMMJdAodqtr/Zf06UpdO4Nr\n" +
                "7HRGnZbsab9yadBUv2MilJ0O34I37CbZRopth3Gal7gprYRp3InuSTIctGTyJOrCt3kSVLkxl1KT\n" +
                "qNN+5y5cz27+DnlkbVJIi49H15ctWvg6YSjKNo8nHjyXbyUvazvxOSjtlR1JRaDtM09FJgV2ryHZ\n" +
                "XAr2Hc/AB2PwHaxqTDud7AXaJxfsV3B3AT2sTizRSti7kwM0vANGndHgHT1oDNpio10HamBjWgNX\n" +
                "Be4nBAYpa2KapI27PkzyxaS4IrL4AFwMBcgHkAg+RFCYBwLyMAEA6AqgAAiBcAwQIoFwJAOgF4BD\n" +
                "CtEAFgAAMEFFACBIKAPcQ6ABD4QUABYcgHAB4C9AFaAEHgBgCC9AAAgBaDyCORX40O3dt7FtBRYi\n" +
                "k2vLH3yv9TJp2DdPdJAWpyX+TK9adcr+Dkn1OGPORP8ABlP6hBahByIPQWWS8If9RP8A6o8t/UJL\n" +
                "fpL+SP67M2mu1L4QXHsLN59x+tuu08d9dm3XbX4B9ZlapOvwDHsrMn4H60a2meN/V5u7Ul/AS6vJ\n" +
                "GEvu5KuPUzddgw6fdKXsiI/UoSf/AByR48Ztu5O2/c1i/IxZHp9B9yyzb25HWjxcOaWKXdCVX/s9\n" +
                "Lp+rhmjTajL2Il5dNbCvkV2+TLJmhDTnH+Ssrm+2EnfCs+a9aT29Wel1vXY4wljg+5tVo8uDj7F3\n" +
                "0Y93Bnx4+mx92SKqJP03LGWXqFHhtM8lyJwZHDJKUXVmVfTxknG4tMo87o+rTxKOu7ymdPrS57UB\n" +
                "rPIoLZyZOunGX2Qi18iytzlsMcYrlX+Qqf8Aydfrwr9mb4uqw5n/AG5b9jHPixTTbhv4PNyxlgyX\n" +
                "G17MD23LdNHPnmozOTB17drMvHKKz5IzxXGRFRnlukZqa5rgwnkVtuWyFNkV6OLNKUu2S+074zhB\n" +
                "V3X+DyY5FBeH8nRhjPK/tloRHpxyKtWaJ2tM4Ywnje2dWKVmka02OmOwsIW/YFfsxoLAm9j0kMYE\n" +
                "WgTRT/AtewCoLHSfgKQC+RWVSF2qqbAL0JP5Gl8icfkgE2jPLbSsuiMirkDNIbAQUIYlooIQhiSA\n" +
                "KCtjAoQwDwQIYAgEkNAFFBQACIFwAxAAIEOigAaCtAACGAh0A/AEhQx0BIUMKAQ0H5BEA1sAH4Kh\n" +
                "eRiGhFDQhthegPmXOT/yf8gpTS1OS/DJRf5FVpDqs8FSyOvklynkvuk3fyZvTHCVchMX6arkzaKc\n" +
                "rIvwGpDv3H3KiG6iTFhcbJpoTv3Jix2AJvlsbfgmwKNISVG8do5IOpUjqhtbK1D4HHTsPAuUZXBO\n" +
                "WRrWSa/c5p/at8nSuCZwjONPn3DFjjc/gUZMqeGSvaM6pUzWemNauegxGKas3xLgysdeOrT3ZvHN\n" +
                "khVStfJhDksNY3/qvfH/ALK/qY8JNM5hEMbS6mVNK9nPmzKSqSd/I7M5xUuRpjFumylPVEyh23uz\n" +
                "NughylbJjJ2xWQB0Rnrg0x9Tlxy/tTaOWwUtjEe30vWev9k3Ul/s64OSWmfPwm4yUoupI9vpMiy4\n" +
                "7u2uUVHWss/LGsskvBmhoDVZZV4D1pJ7SZl+AQG3r7rt/wBh6y/6sxAo3WWL5TD1YXu1+xh5CyDo\n" +
                "9SL8jU4v/JHMIDq7lXKEpJnM+AA6xfk5ba4bC34b/kDqM8iuKZj3S57mVbaAEAlsaIBDEhgIBiKA\n" +
                "AAAoNgBAUAAAAAFAgACBDAAAAGUAAFAAAwAEAAwAfIgTYDfIIVhYDoEgQiYG1oQ0x6KhAH4AKHwL\n" +
                "wOmII+YiUSuBphsMiLVmj4IpBD8iZaVCkixpEtxIjpFv2JSGKqPJb0TFbKltFZTWw8DBhSwbyv8A\n" +
                "B1LSMsMVFaNG6RG4d7GjO/uG5qK2yYlrQTaUeTCWZvUV+5n90ntssYtaZJxaqznls3jiWmx+km9K\n" +
                "jTLjo6sHG2VlwwikkttbMo/25fkzYsdsVWyjKErNFwRsxA2IihkjsGlQRHhmE47bN2Yz0iowlomx\n" +
                "zdkhkWNOiRhGsJM9T6fNrMlepKjyInd0jUXB7uzKvoFidPaK9KVWaY9xTW0zRIsRzelO3oXZL/qd\n" +
                "SQ+0o4+x+Q7X7HZVMKsDjrwKjtaXlInsi1wgOSgo6uyF/pQvTj/1A5qYq2dXow/6/wCxejB3yn8M\n" +
                "DmaFR0+jHVNi9CO6bA5xv9Js8C/7MmWPt0BC4HwCHRAgGwAXCoYAAgGBQvADAgQwEQPkBWBYDgAG\n" +
                "UJAxiAPI0IaQDExioA8AD0ICvAvA/AgAQ6BAHgB0FAH4DkTGAAFgA0IbEgDwFeQGhDHyaZSeiV8l\n" +
                "LguKaY1RLWgg6CtKG1aEpFrguq55E+TTIvNGS5Ka0iUxQWhsgSDgaB+wFQkU3ZkrQ7si2rtVZirl\n" +
                "Ic3UaKgqNMKUaXBoopIixd2tGRqTfsZ92hd+yxFN3yZTKsmXGiVV4ZHXF2jhwtqfwdsdpMjcPgQw\n" +
                "CpaVAxioIloymlTs2IkgjkkjM6Mis55eQwXkpLQkh8IBx1s7ejnWSDfvRxRN8X542Sq+rwJxxqL8\n" +
                "cG2zl+nZlm6WMk7a0zsKhWx3oAAOPAvPAwAL+BWiq0IA0JtLljoKAnzyPXuFBSQC/cArQUAJWZ5l\n" +
                "TiadpnlTtIDKgqxtAtEC8bF+ChUUAqKE+CAVUAIChB5GACAaAgVAPwIoOAsfgVAAUPjYrANBEBoA\n" +
                "ExrkHsgQBQANBYhpFAA0FAAhsAJY0FB4AGCGqAAuwCqDkBAgGgPkrKi9GZcWaFeAoEF2RTTNLpUZ\n" +
                "p09j7hAT3Ew3Zq5aJUblbNCouo0MBXszqmUloSTZSWiKloaRaiDj5KuMJ8o0TFkWk14BP7QycpUZ\n" +
                "uW9FPaIrYQOTJcvYclSM2wi1Jvlmi2jBbZ0RWgsZRfbkaZ3467VXscFf3Dsw/p5I1y2rQmiktCrY\n" +
                "aIORbsdBEsh+xozNoJWcl4OecdnTJGOQM1itBLgPINa2EEeDbHwYrUTXG7XyGtex9IzRx5XC67v/\n" +
                "AJPeR8n083Gafs7PqsMlkwxmnygjQEPwCIhVsKKQ2iia0FDqkCAigouhUBNA0MCCaDgqhOgFVmWV\n" +
                "NNL4NkR1Fd8fwBgCGBQqFQwAVBQ/yIKKBAvYAhAkAeQH4EDAAoPIAAeQDwKyBiHxwIoEPwIa4AEt\n" +
                "B4AAFQA2ABsaEUgAL0FbAAAPAeAAVbGNAIKGACAYAJiGGkgPkFwUuSFwMo0tUImx2UNP3HyLRaIq\n" +
                "O0d0WkDj7AStlqPuJL4NEiAiiqGkNFWBL2BotULt2VqMpRu9aMGpY5NVo62qM5R1siWOdTVbGqb0\n" +
                "VKF8E+m7NMCSfaYuLSs29K+W0TLGvcuIzgt7NHkUY6ezNwaXJKdWmjNWLht22deLaTRy4lZ1YU+D\n" +
                "LpzHQn9obsEtDQVLBDensPAQmQy3wRJ6CVlLkxn5N5caMZcOwlYpW7GxpJIiTCCTLxWjPk0xv/QI\n" +
                "3g0nbPpegXbj+xuq4PmY7Pf+i5PU6anzF0QerGScbTsaOJ92HK6s68U1kgpUVForwEaFk1GwJm2/\n" +
                "tSHFVoI0lZKl91oDTwJjTtESbW0AxGPfmbfY1+KLhOb1KFMCgHQEgm0tsjqH/cX/ALUVkj3QpGeZ\n" +
                "ff8AsiiBB+QoBAMRAMQwAAaEHJQg8DoABcbFyxhQAJ86GhAAvA2FALwAxEDXsHAUNLQ0JiWyqEAg\n" +
                "GwrQCKQtWNAMEAFAw8AMBUIoVAAJ+4BQAgoaABUJooAPjUCGhv4NpAgBMDKqiaKrM0Wii0CVCQyK\n" +
                "pIpISLXwFkCKqwQJDVUtMA+RpaDSZIlrRr42S1dgrHtE0qLfIqNRixEqo56ts6JrkhR2VjEKNJ2Y\n" +
                "Pk65L7X+Dl7WSwaYkzsxR9zDBDSbZ0xSRiuvPxaQ/gmxoigVMPI3ZUS/JEi2ZyYKzbaMpsub9jNs\n" +
                "Mok/BDKqxNfwESlovHqxNFR0gNMe5V7nt/QpKOWeJy5Vo8PHpo7ely+l1OOd1UkB9RPGpxp8+Cem\n" +
                "TWOq8lrLCbpO2XFUghk5ZLheRt+xLxp8tgXGMXGrshwjGVKyG545NRdGuNSf647AqCQ3EaSQ3wBj\n" +
                "6cVPuXIxgQIT5GYxyd3UuL4Soo18mOd/3peTaSbWjHMv70l+AMxACIBgAFAACABDBAAC8jQAgYAw\n" +
                "EFbHQvIAFDBECaBIPI0APgSH+QAQwoQAwT2AIB0g4DyCooEAxfIACGHwAAFAAeQoB+AEA/AL5ASA\n" +
                "YgPjrGmR4KjdbNIY0IZKQ0tlomJdIKdjUW1sEh7CyGtGkXoi74KiyNLQ+RJ/I0wsNFJ6FYFUcgkC\n" +
                "29D8hEtL2Io1oTWrGoxkrdEtUzWREjUrDLI/sdmSqtmmTUHfg5nPRrfSO/Cl6aVGtUZ4XUVa8Gle\n" +
                "TlXaAqiR3omAB8CbJlJJFQmyGwlJGUpWETOVMzdFSICAkp6JVhDGS3oqOwLibx3DXJhdG+LUQr6P\n" +
                "os//ANJjnSdqnXNnfFqUbTtHl/QpRn0s8b20z0owUL7eCRlokNIS4KRRE4d8X7+C4JqO2Tkl2Q+W\n" +
                "PHLvgmApzgnV00Q8zcko8C6mri1yZwaTthHQIz9RuLfA/UqrV2FaaRw41XVL8s7eNnPgxv1Hkkvw\n" +
                "EdSao5s+s8jdM589etIKyAENrQCBAAAJjBgIBgQIAAoEAAQHgQ60FgCAAEAAAULlDWhIYAACAbAP\n" +
                "gABcjASCGAIXkKYJANcAMQeQ8gIOBguQBB4AfgBeAfAeA8AfFplLklbDaZUaDSXkhMpbCxaLSVER\n" +
                "qikw1i1wPwJMKChclolDtIg0itbKRmnou6QVSGuCUx2FVHSGSmUmmUC5E9jT3+BMJiGjOZq9oykh\n" +
                "Esc+b9DOdcm/UP7aMI/qX5LazPr0cfHyabRMCjLqBd2tilKmQ5oM1blq0ZylvZLndmTnbCaqTsix\n" +
                "OTboTCB8isaFVsglJtlJfBcYpBOSuogZNWy1SVDURVbCHFW6OlLVIyxquTVBY9X6E3HqZxvTie4t\n" +
                "ng/Sl2dZFrzGj6BIRmmkADKOfM23VcEwm4PXHsdEkmtnPKFP4CHmlGTi17GYxFB4GnbViAg6L7vI\n" +
                "1HVEYI0rfk2oKynHtVt0YZHeSTWzoyQtLdnNk/5JfkCRDfIAAh/AmAAHIAAAAC8AOgAQJDAgSBJ2\n" +
                "MACgAAATGBQqChg/ggQMA8AJDBIYACAEUA/AVsfCASGAkEHkdAAAFAHkAEMPIAKh0HgK+KqgsbFW\n" +
                "gGtoqtkxvwUBS4KRMeC0gsCkWtolIqK+7kNHtDQwSCmh37ioPyEWn7D3fJC0Wn4CxaGiU9jT2FUx\n" +
                "X7g2N8BUydmM3SZqZZb/AP2VnpyZ39pnhSlkivkrM9k4P+ZUKxPr0Yu0U5JIzukZznS0SN+R5JmL\n" +
                "mQ5kWGLWjkTy6Qls1h2xT9wQ4w8vRL+Ac70bQwpbZK0yjF2WoqKtnT2Y4QcltnNkkm98kEuV+KIt\n" +
                "WOTaVJCjDezSKWylEXAd7WqIKXP4NcduVGGPbOrEqkmFjv8Ap0r6pJvhaPoo8I+e6HE5ZIziv8ls\n" +
                "+gjvgRmm2kHIKC8lJIqI1Yp04uzQTqtgcklQjSW38E+SoSFwyyeQLxTk5U+Dc58b7cis6E0RT7Uz\n" +
                "kyJepL8nVfscs/1S/IGbQh8AAhDaoAEAwASGAgBbGwWhBACQDCjwIAAAAAAAAgEgDYFAFDS2HggE\n" +
                "gSAaKFQIekHgAGFB4CFSAdaCgAAGBI/IcD8gINDCgEIYVoK+MaF2m0lrglRYXEJAU4k1sGKj8lIl\n" +
                "cFx2RTV3suJPgadBVfAIXmx2UVYr2LY/yA0yk1ZFjQVpeyrM0/KHfkixadDbM7oieWkVdbNowyTS\n" +
                "87M3mXNUY5cl7LGOqzyu2GCVS2ZydscQw63k+10rMpTbJvRLZAWAhgHdXA7EFgb4Ir9UuDSWRt23\n" +
                "SOZZGlT4G8rfsTFi5ZpyfOjXFH/Kfky6fG8krf6UdTjqkGox7XJ2NJJ7KyTWNUnsy7vdlBN/doEt\n" +
                "DhHu2ysipV5IDHFOR3YoUjmwK0l7Hdhxt+K/JFej9KxJxjzp2eucf05R7JNeNHaWMUhjQUVGeRuM\n" +
                "bRk5uSo3lFSjTIWHd3oIyarklx0aZVtL2FBd0XXKKMw/JTVOmJ/gDSEI1a5L+GGNVFBkT9NuPK2R\n" +
                "RVPRyyf3SfuzbFl71pbXJi+XYEiY/ImtgC2LyMAAVUMAhB4HQBSFwMGAAC+QAK2FAtB4IFQLkYAM\n" +
                "AQwJBDEALYDqwooSAegoB0goF8jAQAMiF4GFBwVC5GkALkAYAAUAAAFBQxBXyjjaBqkV8iqyOmIo\n" +
                "hw3s6O1US1oI5+2hxNO1MXYwCPyMVMFSAqxkumxq62UNOht2CRSiBCHeh1sK5IoTZS4M2nsSUkgq\n" +
                "3Je5zZJP8msk0Q18FZYOToluy5Qd6RnKyxmpZUeCRpFRV0IkaZA7GhIdEDBIKGkAdr8IvHgc5U3S\n" +
                "CMt0dMXGKDUWnDFHsgrM59Q4uowT/LJlk0+2H7s51bbZIKlNzk2+WOEe522VCHcvlmzx+nG2hoXc\n" +
                "ow0QrbsTTk/tRthxSljcqSaYHR08H2OTSPRhBx6ePuzDFicemV1bR6PSYlkcItfalsyrs6HE8OD7\n" +
                "uZbOpAqrgaNSMH8joRE5VJRXLKLVWFq62Ci0qirEm1KmBjnVZL8NBgV2zaePvS+BRioqkgM8uO9x\n" +
                "5MaabXlHYTKEZbaCIxW4KyvyNJRWkAVx5MfpZe5cMje2d04qcWmcjWtAQAxAAq2MCCQKWxFAIYUA\n" +
                "mKh34BALwCYNBQDCgAmAAYvJQDoAAKAACCtCKCgJQ0OgaABUNAAg2UCAQDBcgGgG0KgDwJDQAADo\n" +
                "AFQAHIwfKpOiktFqKY+0zruzoKNOy+AoiVk1YqNO17IZtMJr2E0UmBEZ1QXvgtpEtFWCM1w0bdra\n" +
                "0TjhHlo14WguM0gopJ2K47XsAq2NRVD/AANLRGk9qsXZFt2ihLQiYiXTp8HJlw9t7PQi0zLNGDRp\n" +
                "jqPMcaY0tDyfraVCtjWEjSErZdaAENDjFvgr03+SLiUFmkcTFOKi6AUWzaDvkxgr3Z0Yo3wRY0hi\n" +
                "lk1HRsvp6i05ZLtCxNwbrlnR2zVznJpIijF0uNbkzHqMcH9sbtGmLqE8lO6Ofrc8nncYaS9vISso\n" +
                "JY5X7nrdHh+1qSdfKOH6d03qZPVyfpjwn5Z6859u0FhPGpT7f2PT6TF2Qu0cnQ4/Vk5yel8HpY8f\n" +
                "b5ssZqlxodv2KBFQvufCMu2XrJyfHsbrkbXmtsBKa4RDhe7KSrwNuwE7S0ZyyqLqUWvk1RE4KaqQ\n" +
                "BFqStbH4M8UJY/tu0aAIBgBMv0s5Zcs7PBxvlgQIpokBANiIAAAoQMGFECCtFCRQkOh0JogQ6Chr\n" +
                "goQWAeAgGCQAAhsPAAAIAAEAwAXkYeAAACgGD+A8AgAfAVoKAQDC9ALwAeR0AgaAdFlHymLIuJM6\n" +
                "oqLVp2eRHJRtj6hw4/c5WOsr0a2DjYsWfHkenT9jar4EaY9tKjOUaR0yxmWSDppclRzcMNClom0l\n" +
                "yaiGwJ7hdxVjog9UX4OfFJNnQGx4MeG7Nltk5IrtcgiVLt3VhLI2/ZCVMUi4hObXBUJuSuiHzSNu\n" +
                "2o68ESJumTOPei+1tDqt2EeXlg4ZKaomjt62FwU0uDi8hiwLRcVYkikn4A1xpG0cTW3eyMUK35Kf\n" +
                "3Pc6XsgqZKnqREo38m0oQhG7ITXKQRmoVwqNsNxfwVji58NGmLA++5NUiNNsOfBDlS7veiOq6nul\n" +
                "S/Sv9lPHgbrvjFr5B4O+SSXcqCajpGs2Zdqprk6s3SY8mZTXPkz6fCunytpNWdKbclsgnH3Rl6aS\n" +
                "7V7HVixzyy1szWKHe33Pfsep0WGKxrTVu/yMXW/TYHiw1J7bs3SGkVRphNDqmNAAILCgAa4JaJyS\n" +
                "ceENPuimAwGACoVFITAmgHQUBMv0v8HGuDslqDOMgTEUSUIAYALwAAAACAIEAUAUAAAAJAMBAMAE\n" +
                "A6EACGAQhgNAADoAEA0CAKBrQDQCGuAGAB4AKASCh68AAqGwoAEA0gqixHwTjKPKaBSo63KDxpOm\n" +
                "TPpou+2QsXWUMjjtcnd0vXxWs3HuefPp8sV+h17oyafDRnxanT6GXWdKoX6sTz+o6+EpNQvk81iE\n" +
                "h5Ol9S219v8AsXryapJIwRaTouGqeSXuJZJLyTQUDWkcskrTp2duDP3xqTVnnVsqLcX3RdNBrnrH\n" +
                "qNhtwo58HUeou2dKS/2bph1ntG7CrNfsb2H9tPgLiYQto2rTM+72X7EynNryvgmk5aycYrkxyTt6\n" +
                "4HHHOWlGX8HX0nRNy781JIqWSOLq8cl0dvy0efVHp/VOpx5sqx4XcIeVwzz0thxt9pijROhqNh2q\n" +
                "97CKWR1oFCU5VFAo1wjbDJQl3NrjyRppi6WW5ZJJITUG6irsby97rv18GuOePGvthcho4pY3GT8U\n" +
                "VixzyTSTbXnZvkyRyc1Z29N0kFgi033c37lHDPA7X2tP5K6OMozk7/Y9HHPtdNFKMXk7owVsiBKM\n" +
                "sSvcjfp+n9Z1GH5YsPSyy5KVx37Hs9P0uPDFRjfu98iJWWPo8OH/ABTfuzVw1rVGsqqvYiUoxe2V\n" +
                "FY39pZEZxfDLXAC8DQ6ABeQIydy2ghkvTWwjPJ+plYm/0+w5r7pSoi6WuWUayko8sItSWjndmuG0\n" +
                "mQaUDQ0TK6dc0FAjLHnTpSVM2QE5NYZM42duVNYJHEwF4JY7oQCAEBAAgEUCGAAAhgAgBAAwAABB\n" +
                "YAkEAimtCSAQx0HIBQeB0FAHgXkqgoBeArWwGAUAxAAAMBAAIBoAEgh0AB5CigAAY+HjGylNx0ny\n" +
                "RHOktrfwOLi92aHRHOnpuqFLFjyQv/aMLTe+C4RlVwlQkES6N91Y5NmMsOSL+6DR6GKcoW5RfHJ1\n" +
                "YskZpJRb+GgPCivuo6FC48Hq9T0cYxeT+mkn+DinGSX6JV+DKxzOBLizpjGUtdst/ASglpc/IHL2\n" +
                "6Ekbdttr2D02kFZVTs2xdQ4amrRPbaJ7Qstj0MefpZxuWSn8l+r0fY/7p5lIpKiNf9K7JdZhT+zH\n" +
                "Nv5M31vmOKvyzm87KUU3sHnW3/kepX6XBfsZ5Oq6jLanmn2v/FOkT2RBpPgrO2pWkUmHaHY/AQ1I\n" +
                "al7kqLTqjSEG3wTVkNNz4Ljik+UOMaezox4skvFL5IrGMFDnk1hFeTdYot129zN49N27eNssHE+m\n" +
                "lP8AR2/lmuPDnwrWXXsmduPpsuWVQx0jpX0/N2ruSj+4RydHDJNNuLb96PT6Xp/8nS+Ei8PTY8ai\n" +
                "pTk6O7D2q+2is6whBKVpnViVx4E9vSRcbQRMo2+DKWNxnraOohrYGSxY7utlxVIpIAoDkJNRi2xQ\n" +
                "dwTAfanpmXp9sqX8mk5OK4uyoduTV0yxGWfSSXlEYod8qfCNc+Op/sNyjgxdsVc3yVGObH2VXDDC\n" +
                "pXfg2knlwW60rOa23piq6AFC+3YzKuTPi7H3x4f+jbBLuwxbey5q4Sj7oWOPZiUfYBZb9N70cd6O\n" +
                "vM/7TRyeAJoTRQgE+NCGAEgOg4ABANfIAAAAhpAABWx1QkGwhoEmuQQ0ADECAdBQAgHQcDrQmAB4\n" +
                "DwMBUA6YUACHQAIBiAVMY6oVANiGIBoQ/gAChD4DyB8B2OrFGLR6f9O4/NCl0rb7lE0OBcnRhuap\n" +
                "G+P6flzZKxxTvl+x7PRfRoYIr1Gm/ItHJ0vRzyPtUX8s9vpulxdPFdsV3eXRrGMcce2CSRSTIM8q\n" +
                "c1SVkJOP64pL5R0pBOKlGmgMPUx2opL+Bx6XBkfdKELfmioRUZV2m8YqtBXk9b9Ex5254n2S+Fpn\n" +
                "l5vo/W4r+xSXho+sa9hS+3khr4h9Llh+rDNv4TIeJX9yab90fbPFG7ash4IS5xxf5QNfEvEkuR+n\n" +
                "+D7J9D001XpQS+Ec0/ofSSt3JeyXgD5V4klyJY5H0a+jdG5qLc9+zOuH0Po1ypfGymvkHjfuHal5\n" +
                "Pr39C6V19z58kZfoODt/sxj3fJCV8qmuCopH0sPoMmrnHEX/AOD7V9mGG+XYXXzkYcWbYumlN/Yk\n" +
                "0e3/AOGit9yVfB09P9JxuW8tL2SJi+Tw49Pkja7NnTj6XK198lG/CVs+hj9MwRXbLvX78mr6Hp8M\n" +
                "bxKmvLGJ5PGxdA4pVHt+ZHVh6SCptub/AIR2Sxtt70R3dk+10kVNWscIxqKoTx9zp8FpWVQERxQX\n" +
                "MTOeOSyUm1F+x0pDcO4IyxY3F2pWvk3qkKEKRUmoq2XEOtbIkqZWaSlKCj5CUW5tJNtDFZ0DRfpy\n" +
                "S4E4taaogw6i+xIOm3j/AAw6n9KXkrBDsxpe+wNkgtrhIlypEyyRXuBrk7ZpOvuRz5ILuVyr3Nk7\n" +
                "4FOEZx3yVEuUHDsg7VbMvTSWuSZRcXRWOUuJJte5RajJLgUm1xFlrQGVZRf3fc/2Kt+ETlx98ftd\n" +
                "SXAY3L9M1sKnOl6En5OTwduf/ha+TkfGgJaJZTEBIDABeBDAIQDB/AUgHQqAAB6DwAAAWENDEFgM\n" +
                "EAIBjWhAAxACAY0IaAaFTsY0Akgod2gAVBQxBAwGAVLGFe4wEIoKCJoaCgrQHykcmRuntvxR7P0/\n" +
                "oU8ay9Srb4iy/p/RQxx9Wf3Tfv4O9DRMccIqoQUV8Iyn3YndXE6ENpVQVGOcZq0X5J7FF3EugK5K\n" +
                "ola4KTKB9raXlFJIwnd2bY5KS52EZ5e5SVXQn3TlRWVSv4FGXaUbdvgFFPV7M8CnPI2/Y2xxdObX\n" +
                "4Jgl464YmilK9mbt+NAY4YXk72qo6USoutDcZOOmAOVSJ9Zd1RMskpd1N/wa48EGr7pAaxkq2axk\n" +
                "0qMvSSX2P+RJzv7gLniXlqIQwOT+2caLpzjt8Dx4u5UnUii5y7IKLabMW23bNpY5RxP1KtcGJAUT\n" +
                "OCmqkXQEVGOPYquzQKGUOLKpuLrwJLwdUcLWL7KbfIiOaLvTFPmmh9kvU7WvuOrpemlkjKc0u2F8\n" +
                "mpBzRSj98+FwXglLLmdLSVmWXcnS/CPQ6Xp/TwPuj98ufgDnWod788GWRqrbNurzR1jxpOvJKWKU\n" +
                "FFrbJg42u+VvwO2+ItmtbBqlpEVy5ptUnBoIYpzd9yROaMpZN/sb9PGUf1JrQAscsav9X4HGV+KN\n" +
                "bW9kwSasDDLFqbfhiTlGrdpm8qemSo9ultAHgOBpCoikKiqADHqP+Bv5OQ6+p/4K+TlYEskoQEhs\n" +
                "YECDyAXRQCobEA0FB5CyBOg+Bi8lALyOgrYQBoYUAAkOgoACxiAEikgrQ6ATAdDAPAkigCEMQMAr\n" +
                "YUNDAVB4GNlEBQ2gRAuAGwAVbAYFis8aqCRZj08/Uwwn/wBlZsjIBoARQxrQhrYQ0h+AADOUXy7F\n" +
                "G07Tpo2UtU1Y3jpWlo0Kcb6dTtWzm33b8nRJN4YwQ8UMMJd2XIteAKxLsx15fJOTK+3tiduKPT5p\n" +
                "XGSfwjnz9PDHtSe+AMsc6jTouW4p1yRDHGrlJGjSduLTS9iUKMfcU03FpcDTHVoDlWJyk0joxwlG\n" +
                "NSFODUG48lYpuUalyBSlHgrEk7Mpfr0i4qtp0BUqi9M0xTU5drW/cwJlGXMG017DR051eV/sZ/kx\n" +
                "hlk3U23L3Zt5IpcjoaAAoYBdABcMkscu6L48GY0io9PB2dUu6K7ckVx7m6i8fSZEluT8nL0OHIpx\n" +
                "yRlFe6+D0slvG+3bo1KPN6fpIRyKeecUlwm6PRcceWDSacXr7WebnwycnePI/mjXoMWbHk7nBxg+\n" +
                "bFSMOo6GOFpxyOTfijGONR5ku72PU+oYvU6ZyWpR2eWsUWl99SH8VLXaxcmmdKMox9lyZrRlWUoa\n" +
                "k63QYsncu2Spo1fwT26vygInSnSRS+2P5G007YrtEEtJmcsksUqkrizVClFSi4tFExkpK0yuTmTe\n" +
                "DJT3FnQmmrXkgOQYXQmwrLqa9H/8jlZ0Z4/2k2/JzsCWLwMTAQAACoVFABI+AEgGIYAIPI0FbAAG\n" +
                "FBB4DwA6AEA0FAC2FB+BoAQ0NIEgENDoKoBDQRG/gImhgMCUhtAkVQEoY1wJlCChgBPkbGJkAg4Q\n" +
                "DpcAcH0+al0kIrlLg60eX0twxRmj0MOT1I65XJFbIAQ7soB2IXZbtsIq74BDSSGihx5NsepK+GZI\n" +
                "vG5dyfyINOpi9OC18HHKNf4s7c8XJJpujBZZ4+Gn+TSH0ePL68Xjg+dutUer1WGT6XdJo4cPXTgq\n" +
                "lFP8GufrVPA4pvfh+AOBb5OjDHtx5JWmmvBg97SZpiTXd4VEU4l6XJm7SC3ZkW9oO32JRSlSKCmu\n" +
                "QB7AAQ/AF+lPtUnB0/IVz5If5LwGKdvtZv4MZY0m5JBGyGRGTa4LSIoEo+4xoqBJFISR6P0yON+o\n" +
                "5qNqtso4sSk8sVjvus97xs5o5Iev2YccPlpHTeubrkDinmzYW6alH5NMPWwn9s12y/0zTNghmW7T\n" +
                "90cWXpp4pN03H3NfUdHWZ+3GoxV93J57xzlN1BnWpOWKLlX26tg++UJRlSVcolVxZn+lea2Ytsrt\n" +
                "vZDdOmZUxp0hciZA3sBWNcgEU3pITjRpiSeT8EK5SnRRnkh3xrVrhmWJyj9rR0U3wEsaSsghL3Ch\n" +
                "gFYdUv7Uf/ccrOvq/wDiX5ORgIkpksgGIfgRQeBWPyFAS0A62PwAlyMAZEALkBlDQgTHYCSGMPkA\n" +
                "oEhhWghDQhoKpAJFAFCYWAQIa5BAADoKGAqAAAfgQ2IoASAaQCrYDfAiBUA/IUB53QwjPo4qSH2P\n" +
                "BO0V0LrpYo6O1SX3K0A1NSVopKyVFRX2lphTSGg5AIY0Ia0UU3GK2rHDIpOoxpfJnKHc7TKxx7U7\n" +
                "LB0Sn2peUzOXT+pFyxv/APEnKnGMHfIYpU3vkoz7ZQtSXBpGScGmOTa+USpRV6YGsJuNJR0PJFqT\n" +
                "IwSfqL2+TTK7ySsyMrpDi0wezGLcJVJ6A3oOOQik9pjoBrgdE0xq2gK4O6b7Y4l8HJhh35Yxflnb\n" +
                "lubSS40IOXqMfp5Wq09oWHBkzSqC17s9PL00UlLI7aXA+3IofZjdfBrB5eXC8WRwda9iUjp6xVn2\n" +
                "90rOeiUIYeS4PeyBGmHIscra09MlpeBLko64Z8WJ98bb9jpjKU+inklru2keW2uEdUMkng7O51rQ\n" +
                "HVgzSVR9lsc5ynBZIXKPsY4Wo903wkc2DqJ4ZadxfKLBtly90XFRrY1Jvo572jWMoZl9tMn0vSlO\n" +
                "UlUKLR57McqdqSNhNWtmFZxfciq0RFdsvg1AhxrgFfFlir2AvFFrHKa8aDBBtym1Rv0MPVWTE/yL\n" +
                "NP0l6WNrXL+TUg56b1RPY4xd+5vgwZc8m+6NfkjJCUcs4zVUSjEB0IyrDq79KK+TkdnX1f6I/k5X\n" +
                "YEsnyUL3AQihVYQXqhBQEAAAVQDGIIEAAAIYJDYDSHWhIpBCBDABDQhgOhIaRVaAXgRQACQ0gQwE\n" +
                "AUOgEIqtCAABoCgGhDXABWhD8CAehAhpDB53TT7eninzRqptvk58X6V8HQnTTIN0MmLtWUBSDyC4\n" +
                "DyBSQVsSdBKVtdpRolSBNe6JbVU2EFFgGebk4p+Awr7+PA5wcuHRcUoKlyND+GLsT3Qhp0wJpqS7\n" +
                "fc2yJJ8cijNLwDk5O2BNE5Id0flFoAM8OrRqkSklJv3LQAPtDgemIN+ilXV401Vuj2I4YY7m1dbP\n" +
                "M6LBeeGSb7Yp2vk9Tqb9F1Fy+EWCP6pt1HFf7lpZO2U56daSOGGSamvTVS9j0odzgu9JSrZaPL62\n" +
                "N9uScakzkZ6P1KCeKM23adI85koVAHArIpSnT7fI4Jxu02zKSfrJtUdMWkrtFEXUto6MM4Rdz3H2\n" +
                "RzuLlJvuKUXFUEdHU9VHIvTxQ7Y+W+Wc3BMpNDTsiqjKUXcJNP4KyZsuVds5tpEAECGAgpS8FC8j\n" +
                "IEi44sklag+2rslHRjl6mJYnJqv9mojT6c0s8opbceTHq4T9SXa0/deR4a6afddtqtFZsLnOUscn\n" +
                "b27NCOiwZIS9aTcEt17k5snfknNJ7Zop5PQcZOq0c1NRdvyYtVDsmy3RLIrn6u+2GvJzM6urrsj+\n" +
                "TlYCJKZIAIYggAA/ACHQeAQAgChLkBhwPQcgNCH+BUAFJiSHQDHWhIoIXkABVYFLgYlwPwULyOgC\n" +
                "gAaQIdEBWxghlE7sXkuhUAmhUVQUAq0P4GOgIoVbKABDQUIDzMfbGK1ZvSktaMsddvuX3exB0Q0q\n" +
                "KIitFbAoLJVldoEzlUR4Nq3yEsdxvyCUsdKPJRq0lG6M4uSlxyXFSe5FeNgF62xOcVzJDInjjJfI\n" +
                "Fpp8MaOaPdjldaOmO1YDQ1sSKQAgQWNAKrlZQJDXADR2dE8XZL+2pZF7nGOEnCSlF00B6PbmeVOc\n" +
                "eH/B35ZSlibxO2jx5dZnkmm47+Dr6Jzh0cnu3LRqDXBlXqv1IJP3o6cmTtWts4eq6ztmoRgm0k2Y\n" +
                "5OvzTf2pQX8kv0a/UpOXpL8to4GVKUpScpybbEQFDS2CGkApQTfc1vgmONU327s1exxWmUZp14C3\n" +
                "wNrYJEVNJglRdB2gSHJSQUEJLYUVQEEtCotAkUTQvBqkZtU6ICCi5rudI2y5IqUu2V2q0YByVRbq\n" +
                "rZLHQMgkllXsTA5+q4i/azlOnqf8Ucz9gEIYgpNCGIIQw8AAAAcsAfAIf/wJAFjBchQAhoIjQQAA\n" +
                "WBQErZQCocUC5GuQGhhQ0UCXkY0qBgFANcDQCSAY0AJEliqwJHQx1oCaHSodBVeAICqKS2MCfBNF\n" +
                "0KtAebjhLhotY3d2XHgpcEDjwUT4KXADGhIaAtbCrZKY0yiq1oXyPwIgBgNIBVocYpcB4KWig0Hk\n" +
                "aGiBJDQIaVgBVWKi4gJRsKouK5FIBLRsupzRxqEclRXwZJOg4RVPy23djRPgFaCKBBYAMaYgoCk9\n" +
                "HZ0nSrLheTI5JPhI40tHsRrH00Y/+miwef1XTrE4yi24s53yesoQyweOav2OCPS5pp9sHr3LRgGz\n" +
                "qn0eWONyuLpbSOXwZAAgAd2OOyRoC2r4Ekxx0hSfbdEC7qYdtyvwSotLuk9vwaLgKlxIfJpJ0jKU\n" +
                "ktt0UAExyRb5GQLgAfyFgcvWbyRXtE5mvc6eqbfUNviqRzv5AngQxMBPgXIwAOBDEAAAAA6EmNAA\n" +
                "kOgoBoEw8CCKDkVFIKaBAqoLAcRoEikggGg/I0AcAhgAUNB4AoBoS5KQDFWyhUArod2LkdAOtAk6\n" +
                "CwQCoB1sO0omtgMK8kHDHgaJjpUikyCkPYkx2A0MmxoBjuhA3SATm+60zRcX7mF2zXE32texcFoZ\n" +
                "LlCKtv8AgcJqcmkiCrCg4YwGtaBEt0rKi7VgPyVFElR5AtLQ48CQncvtRQu9uVI1XuQoqEdfyVB6\n" +
                "Aol80V4M5SrlAMER3S8JMqMre1sCgQ0CAdWenHpemjCPdDvbXNnmo9Hosnq43il+qK0y6M8nRx/V\n" +
                "ievZnc4p9q8JCjCnyacIWolKKbaSRnKbdrwaQ3YOEWnoSjHHbm0vY8xr7fwevjuMqrk8/q2v6qa8\n" +
                "Cq518kSbT0aESja+SBRkmUlsyjqVGqAtLRmpfc78FOVRfwZ4+1tyk1+ALyzTSSexRnb2VOMO1uvw\n" +
                "TH7VwBUtmGXG07W/g6FtClyQc8ccXUki6rRQgqWti8CyTUItsMb7op+6CObqf1ps52dPVrty0/Y5\n" +
                "nwFIT5GKyAekTspiKBcB+RVoAChpCHYCfwMfgQDQVsQwGFDAASAfgNhB8DSphQ0AxoSKACkhJe4y\n" +
                "gGA0gCgSKSHQCpAx1oEAIEhjogmh+KGkOiwRQ1yVQqoBeR3oZIAwSsVbHYHmp6KTOBLLypNm2KeW\n" +
                "/vWqIrqtVsLM1IadBGngmbajaYu6xXqmA4Za5dmjmnC15OaSraLxPVM1Ban2rgpZF2utWY5Jffrh\n" +
                "GmGKm7fHBaGvk2w/qJnh7Mdp3RMLbpGaOlqmMm0nVlpx5ZBMouUXGK2GLDlv7l2r5NYZElrZXq+4\n" +
                "B2dumCJnmiladmX9ReaMWkosDf8ABEJVJ6thkkowe9kYskI822yjTJOX/VpBBycinLT2TBMDZWJq\n" +
                "1scbop1QGMY1LT0aIX4BPYFeBAROTWkBqju6LBl7vUT7Y/8AycENo7P6mfpwjD7VFV+QPTr32xS/\n" +
                "SzCPV4Vjj3z+5rejoVNWnaZAoqkMz9VSm4Q3Xk0XAEyfbxyRlxxnhl6iTpc+xo0nyDipRcZK01Rd\n" +
                "HjNaJN+pxPDklGMW4+Gcrb2tlDaudjFGu3Q6IE05WuEZTx9se6Kumb+NDrVMDnj3zprdeDpitK1R\n" +
                "EcXbNtGqrzwAvkienxybd8X0q1uxZYSlHH2q9bGDnEavHJR3X4InFxdEHL1T0vwX0u8EWyOpXc4x\n" +
                "W3RpBdkIxXhAYdZrIvlHKzo6yV5F+DmCgAEAMXgYvIB4Exh4AFwKh/gAEO0IKAoZNDTAqx2JcjrQ\n" +
                "QeCkJLQIBrkYkvYYBRSQlyUvgBoBodFgEhh4GgGNCoaAfgaWhIa0AqdlUPkEgEOh0D4AlcAPxwFC\n" +
                "BCoqgoCUgGlodAfP412qrNEyCkRV2CYjPNJxikvIRsnfAOdLZl08nKFPlG3YnyBHf3PgaVMfpK9O\n" +
                "jWGH3ei6OfbdaOvE/Tx9q2y4YsceIq/c1yPtgmNGKm3FxnuxpqMNcmbdsCDTHtmtaM8VKL9ynLQG\n" +
                "kXSJ7tmbm6IlNUwKm/FmUpLm+DN5beiNt7A9PHOOfGpabXITg+20qOb6fPszOD/TL/5PQa32gc+P\n" +
                "GpStzpo6scaVXZPpfddGsVVFFwg52o+FYlCU1LtV0bdLOMc8n47WVg7nGckvFGsHFQzqj0ORpOU4\n" +
                "qP8As1/8f3TTWR9rVkweeRPTN8uKWPJKD3TOeablSA1xP7V+DW6ZklWhyk1Hgg0bs2WbJ2dqyS7U\n" +
                "qo86eZynzS+DbFkdVV/LA9no4pYe69scszUmk0zy4z8KTXxZ6Hq4oYksNS92yyDWGa3UlX4LyZIY\n" +
                "o3kkkjOORxim4Kn5RydbFuUcnc3F+H4Jgzz9Q83+NbMHtDEgM+2UZWno0W0D4BaAFo0i03SMzo6X\n" +
                "D3vuk+HwIMnal92gk12srqvU9S3GlwvkfT4pTyxhKqki4moxO/1ajHZSyzz54pKor/4J6qoZXCK+\n" +
                "2OjfpcTjjeStyWvwUZydKc7+Ecrk3bdsvqssX/bx+OWZxyNtRSRKqO23bH2ve6LrYSVpoyPP6jc+\n" +
                "boxN+oVSVaMWFSAhgFCHsQAAAAwrYkN2AUCC2CAPIJAhoIEykJIKoC6F5EmO7YDRWhIdAA4pth4K\n" +
                "gihxTKSKSHWyhIaWxpDWkAgSH5HQCS8Dr3HVD2AJDQIEAwoYUQTQ60NIPIE1oFotLewcSiGvYmqZ\n" +
                "dBRB8+tFIic1Be7MfXk9JUYV0mGZfcTOcn5om/csMbYV2r8m3c75OOGfHBtSf8GWTrpKVY6S+Uax\n" +
                "HoqfijSOdRj92vls8X+q6nI2k7v2RePBmy08sml8uxg9iPUpr7ZJ/KF3yl+qTde5zYlDFHtTuvc0\n" +
                "9RPhmRvF6HZzrK/CDvb5ZR0qeqWhqVoxjJUHd8gaTeuTMTlbHfgqEoqx1oa4GltMBR01XKPXxyWT\n" +
                "FHIuWjyXydnQZWpLE+HtBXZbTadorwTlcvK/crHFyhF3yyorEpOVLybyzvHi9LGl8s16nHHp8MYx\n" +
                "3J+Tn6XE8+ZR/wAVtsqO7Bjn/Sq3uWy88pQUYx0qNMuSGKHdN0lwed688mf1JNJLx8Ce1Z9ZHI8s\n" +
                "pdkq1ujmij1ZdSpYZ0vt7atnnRjb2RU9l82c2dpT7YtuuTsa+aOeUPv2QV07T4STOh7WzLHGKVxH\n" +
                "KUk+dMCo405PtvRvhzLD9s1cXzoywzcJPu4ZMpubtvRUevg7e37Jd0X/AKODq8sp5ZY9KMHSoww9\n" +
                "TLp8vfTlHyrFLL6uSWSq7ndEUWArDgBggAB+BKTj/k1+4nL2EvliDrw545P7WZafDOnFiePL3viK\n" +
                "Zw48am9zUV7s9TB/xdvd3V5NDz1jxeq8vUTSTd17nZj6nps0vTjLjhNVZ588cp5JUlJ3XJeDo80p\n" +
                "ptKCXm7Aj6hhxY6lCPb3N2ckZwitJ2/J631OMJdH7tPR4ypckG16GtkqmlQ1OtGRxdXrK0nwc7N+\n" +
                "rd522uUc7CkAaAAT0LyHgKAA8jBIABugfsC4ABoX5GggQxDQDQ6BcjAloEmUNACsaZUVf5F2lAuD\n" +
                "SKIimuTSPAFoBLRSQgFZXgIodFC/Ya2NbHQBQ0hoaQCrY9LgoKIJp0ND+ArRQlQaHQUAqCtUN8gB\n" +
                "LQFtULgD5SbbbIobdgrOalWyZQTf3MumQ+5uqdFgrHGC2oo1lOKadJfsc90qC5SfJdGjyLu+1CeW\n" +
                "TfCM6plIBubCMmm6E0TtMDojMpS0YKVFOaj5CNlJx8spZfjVHM5t+dB6nIHVCV8lp8Hn+u06rRpH\n" +
                "J3cMGO5O3Romkjkjkfk0jN+4XG+jSD7ZJrwYRZM2/DY1MfQdP1eLJFRmr3TOv0XGKjFWr1R4f0iS\n" +
                "nGeJ/qu1Z9D9PcoJ48kk/MSyojq4PP1FXSiqs1wZelwQ7FljflmPXRyTytKDa8b5OR9LnbqOCWy0\n" +
                "key4482PaUovaPPz48WPK032qzq6LDkw4XHI/wAL2MfqMV3RlW62OaYzc8GSKxRckvc5ssXiyOF3\n" +
                "ReKWJTSlFrfNk9Q288n4dCqmKdW2JpU7BNJCbsgjHCUN3+xUvuf4KjVCdeAHaS0IQ1wANJ8owd45\n" +
                "Nq6N/ImrjXuUKM1NWijnjePJT4N7vglDb0J20OhkCSQ6QMvG6yRb4TVlE1bqjv6Co4pQd29mXU9a\n" +
                "u5xx1+6F0+Xtg277pf6NDl6hOM5O2vZoiHU5oXWWVfk3x5YTfbKmmYTxwUn2ut+QkV1GbJmhG3pe\n" +
                "xjHE5bctB+mDtlYqbbsipqtD0MwlNQzdstJ8MyMer/5q+DBmvUO80t2YhSD9w2HgA8AHjYJANAAw\n" +
                "EwoNhsA8jr2BAA6GuBJFeACKHTEuCkECGgXA6KGtDQqKSAEikOKH22AkUmJLY+GBpFjXuQWmkUVQ\n" +
                "yVsoBpFJa0KJQArQ0tjQ6ATivYXaUtspqgM68jq0NoEgJcUKtltaEkwEkFMqtDQHx1drdiKyPe7J\n" +
                "ijmsCQO2OwAyUaexpDl7ozt3RVhyewhKmS0hwW7Aty2xL5FNO9Bbi9/wTQO7oiWlstTt8BNXyXSM\n" +
                "Y5G3xZrZKg60jSEKjsJUximCpX2j1uvBMVK/gocs8opOUePY6MeVSgnHg51B5PtSbN8fTThBRSf8";

        encodedImage = encodedImage.replace("\n", "").replace("\r", "");

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\"content\": \"" + encodedImage + "\"}");
//        RequestBody body = RequestBody.create(mediaType, "{\"content\": \"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\"}");

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

                    Log.d(TAG, myResponse);

                }

            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }
        });


        Log.d(TAG, "Test");
        Log.d(TAG, imageUri.toString());


        if (resultCode == RESULT_OK) {
            //set the image captured to our ImageView
            imageView.setImageURI(imageUri);
        }
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

//    public void onClickToCarCalculatorActivity(View view) {
//
//        Log.d("DbMain", "click to Car calculaor");
//        Intent intent = new Intent(MainActivity.this, CarListActivity.class);
//        intent.putExtra("id", )
//        startActivity(intent);
//    }
}