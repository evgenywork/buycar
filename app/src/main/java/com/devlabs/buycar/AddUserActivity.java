package com.devlabs.buycar;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddUserActivity extends AppCompatActivity {

    EditText editTextFirstName;
    EditText editTextSername;
    EditText editTextSecondName;
    EditText editTextBirthday;
    EditText editTextCity;
    EditText editTextSalary;
    EditText editTextPhone;
    EditText editTextEmail;

    Button buttonAddUser;

    SharedPreferences sharedPreferences;

    private static final String SHARED_PREF_NAME = "buycar";
    private static final String KEY_FIRST_NAME = "first_name";
    private static final String KEY_SERNAME = "sername";
    private static final String KEY_SECOND_NAME = "second_name";
    private static final String KEY_BIRTHDAY = "birthday";
    private static final String KEY_CITY = "city";
    private static final String KEY_SALARY = "salary";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_EMAIL = "email";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextSername = findViewById(R.id.editTextSername);
        editTextSecondName = findViewById(R.id.editTextSecondName);
        editTextBirthday = findViewById(R.id.editTextBirthday);
        editTextCity = findViewById(R.id.editTextCity);
        editTextSalary = findViewById(R.id.editTextSalary);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextEmail = findViewById(R.id.editTextEmail);
        buttonAddUser = findViewById(R.id.buttonAddUser);

        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
        //When open Activity check if data available

        String first_name = sharedPreferences.getString(KEY_FIRST_NAME, null);

        if (first_name != null) {
            editTextFirstName.setText(first_name);
        }

        buttonAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Fill shared preference
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(KEY_FIRST_NAME, editTextFirstName.getText().toString());
                editor.putString(KEY_SERNAME, editTextSername.getText().toString());
                editor.putString(KEY_SECOND_NAME, editTextSecondName.getText().toString());
                editor.putString(KEY_BIRTHDAY, editTextBirthday.getText().toString());
                editor.putString(KEY_CITY, editTextCity.getText().toString());
                editor.putString(KEY_SALARY, editTextSalary.getText().toString());
                editor.putString(KEY_PHONE, editTextPhone.getText().toString());
                editor.putString(KEY_EMAIL, editTextEmail.getText().toString());
                editor.apply();

                Intent intent = new Intent(AddUserActivity.this, CarCalculatorActivity.class);
                startActivity(intent);

                Toast.makeText(AddUserActivity.this, "User added", Toast.LENGTH_SHORT).show();
            }
        });

    }
}