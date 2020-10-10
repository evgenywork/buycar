package com.devlabs.buycar;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class CarListActivity extends AppCompatActivity {

    private TextView messageTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_list);

//        messageTextView = findViewById(R.id.textView2);

    }



    public void showResponse(String response) {
        messageTextView.setText(response);
    }
}