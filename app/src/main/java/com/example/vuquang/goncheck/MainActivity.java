package com.example.vuquang.goncheck;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.*;

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
        if (!isOnline()) {
            Toast.makeText(this,"Please turn on network",Toast.LENGTH_SHORT).show();
            return;
        }
        else
            googleMapAct();

    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }


    private void googleMapAct() {
        Intent intent = new Intent(this,MapsActivity.class);
        startActivity(intent);
    }
}
