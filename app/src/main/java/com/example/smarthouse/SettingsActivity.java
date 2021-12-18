package com.example.smarthouse;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.example.smarthouse.network.RequestSingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SettingsActivity extends AppCompatActivity {

    private TextView ip;
    private Button saveSettingsBtn;
    private ImageView arrowBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getSupportActionBar() != null)
            this.getSupportActionBar().hide();
        setContentView(R.layout.activity_settings);

        Window window = this.getWindow();
        window.setStatusBarColor(getResources().getColor(R.color.black));

        String previousIp = getIntent().getStringExtra("serverIp");

        ip = findViewById(R.id.ipValueText);
        ip.setText(previousIp);
        saveSettingsBtn = findViewById(R.id.btnSaveSettings);
        arrowBack = findViewById(R.id.arrow_back);

        saveSettingsBtn.setOnClickListener(v -> {
            if(ip.getText().toString() != null && !(ip.getText().toString().isEmpty()))
            {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("ip", ip.getText().toString());
                setResult(RESULT_OK, resultIntent);
                finish();
            }
            else
                Toast.makeText(SettingsActivity.this, "Uzupełnij adres IP", Toast.LENGTH_SHORT).show();
        });

        arrowBack.setOnClickListener(v -> {
            finish();
        });
    }
}