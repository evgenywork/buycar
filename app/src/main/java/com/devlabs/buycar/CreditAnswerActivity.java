package com.devlabs.buycar;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class CreditAnswerActivity extends AppCompatActivity {


    TextView textViewStatus;
    TextView textViewPayment;
    TextView textViewEnd;
    String answer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credit_answer);

        textViewStatus = findViewById(R.id.textViewStatus);
        textViewPayment = findViewById(R.id.textViewPayment);
        textViewEnd = findViewById(R.id.textViewEnd);
//        intent.putExtra("application_status", application_status);
//        intent.putExtra("monthly_payment", monthly_payment);
//        intent.putExtra("decision_end_date", decision_end_date);
//        intent.putExtra("decision_date", decision_date);
        Intent intent = getIntent();
        String application_status = intent.getStringExtra("application_status");
        String decision_date = intent.getStringExtra("decision_date");
        String decision_end_date = intent.getStringExtra("decision_end_date");
        double monthly_payment = intent.getDoubleExtra("monthly_payment", 0);

        System.out.println(application_status);
        System.out.println("prescore_approved");
        if (application_status.equals("prescore_approved") ) {
            answer = "Your credit was prescore aproved!";
        } else  if (application_status.equals("prescore_denied")) {
            answer = "Your credit was denied!";
        } else {
            answer = "Processing";
        }

        textViewStatus.setText(answer);
        textViewPayment.setText("Monthly payment: " + (int)monthly_payment);
        textViewEnd.setText("Date end decision: " + decision_end_date);

    }
}