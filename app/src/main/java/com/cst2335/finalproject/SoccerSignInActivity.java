package com.cst2335.finalproject;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

public class SoccerSignInActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soccer_sign_in);

        Button create = findViewById(R.id.username_button);

        create.setOnClickListener(c->{
            EditText et = findViewById(R.id.username_input);
            if (et.getText().equals("")) {

            }
            else {
                SharedPreferences prefs = getSharedPreferences("soccer_prefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("username", String.valueOf(et.getText()));
                edit.commit();

                String toastMessage = et.getText() + " logged in";
                Toast toast = Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT);
                toast.show();

                setResult(RESULT_OK, getIntent().putExtra("new_username", et.getText()));
                finish();
            }
        });
    }
}