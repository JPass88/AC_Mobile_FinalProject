package com.cst2335.finalproject;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.util.Locale;

/**
 * @Author Jordan Passant
 * @since 2021-04-08
 * CarTraderMainActivity is the splash screen for the AutoTrader API
 * Contains an about button with author, version and a button to
 * advance to the next activity (Selection)
 */

public class CarTraderMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Lang = Locale.getDefault().getLanguage(); // "en" or "fr"

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_trader_main);

        // Progress to 'Selection' activity
        Button buttonGo = findViewById(R.id.buttonGo);

        // About this app
        Button buttonAbout = findViewById(R.id.buttonAbout);
        buttonAbout.setBackgroundColor(Color.rgb(1,1,1));

        // Intent for Selection Activity
        Intent goToSelection = new Intent(CarTraderMainActivity.this, CarTraderSelectionActivity.class);
        // Selection Page Launch Button
        buttonGo.setOnClickListener( bt -> {
            startActivity(goToSelection);
        });

        // Dialogbox instantiation
        buttonAbout.setOnClickListener(( e) -> {

            /**
             *   Upon clicking the 'About' button, an alert dialog box is launched
             *   (Contains basic app information)
             */

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle(R.string.AT_AboutApp)

                    //What is the message:
                    .setMessage(R.string.AT_Main_Dialog)

            /**
             *   Return button launches a helpful toast 'message'
             */

                    .setPositiveButton(R.string.AT_Return, (click, arg) -> {
                        Toast.makeText(this,
                                getString(R.string.AT_Main_Toast),
                                Toast.LENGTH_LONG).show();
                    })
                    .create().show();
        });
    }
}