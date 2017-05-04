package com.example.vuquang.goncheck;


import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.*;

import com.example.vuquang.goncheck.DAO.CheckedPlaceDAO;
import com.example.vuquang.goncheck.model.CheckedPlace;
import com.google.android.gms.location.places.Place;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    TextView tx;
    Button btnLogin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        tx = (TextView)findViewById(R.id.login);

        Typeface custom_font = Typeface.createFromAsset(getAssets(),  "fonts/Nabila.ttf");

        tx.setTypeface(custom_font);

        btnLogin=(Button)findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(this);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        googleMapAct();

    }


    private void googleMapAct() {
        Intent intent = new Intent(this,MapsActivity.class);
        startActivity(intent);
    }
}
