package com.example.smarthouse;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smarthouse.model.HeatPump;
import com.example.smarthouse.model.Recuperator;
import com.example.smarthouse.network.ESP32Service;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class RecuperatorActivity extends AppCompatActivity {

    private TextView tempSet;
    private TextView tempPanel;
    private TextView tempDiff;
    private TextView tempExtract;
    private TextView tempOutdoor;
    private TextView tempSupply;

    private TextView modeValue;
    private TextView modeName;

    private SwitchMaterial recuperatorSwitch;

    private ESP32Service esp32Service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getSupportActionBar() != null)
            this.getSupportActionBar().hide();
        setContentView(R.layout.activity_recuperator);

        Window window = this.getWindow();
        window.setStatusBarColor(getResources().getColor(R.color.black));

        tempSet = findViewById(R.id.temp_set);
        tempPanel = findViewById(R.id.temp_panel);
        tempDiff = findViewById(R.id.temp_diff);
        tempExtract = findViewById(R.id.temp_extract);
        tempOutdoor = findViewById(R.id.temp_outdoor);
        tempSupply = findViewById(R.id.temp_supply);

        modeValue = findViewById(R.id.mode_value);
        modeName = findViewById(R.id.mode_text);

        recuperatorSwitch = findViewById(R.id.recuperator_status);

        esp32Service = new ESP32Service(this);

        ImageView arrowBack = findViewById(R.id.arrow_back);
        arrowBack.setOnClickListener(v -> {
            finish();
        });

        handleStatusChange();
        getData();

    }

    private void getData() {
            esp32Service.getRecuperatorData(new ESP32Service.ESP32ResponseListener() {
            @Override
            public void OnError() {
                Toast.makeText(RecuperatorActivity.this, "Nie udało się wczytać danych", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void OnResponse(Object obj) {
                loadData((Recuperator) obj);
            }
        });
    }

    private void loadData(Recuperator obj) {
        if(obj.isStatus()) recuperatorSwitch.setChecked(true);
        else recuperatorSwitch.setChecked(false);

        modeValue.setText(String.format("%d", obj.getMode()));
        modeName.setText(String.format("%s", obj.getModeName()));

        tempSet.setText(String.format("%s °C", obj.getTemperatureSet().toString()));
        tempPanel.setText(String.format("%s °C", obj.getTemperaturePanel().toString()));
        tempDiff.setText(String.format("%s °C", obj.getTemperatureDifference().toString()));
        tempExtract.setText(String.format("%s °C", obj.getTemperatureExtract().toString()));
        tempOutdoor.setText(String.format("%s °C", obj.getTemperatureOutdoor().toString()));
        tempSupply.setText(String.format("%s °C", obj.getTemperatureSupply().toString()));
    }

    private void handleStatusChange() {
        recuperatorSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                esp32Service.turnRecuperator(isChecked, new ESP32Service.ESP32ResponseListener() {
                    @Override
                    public void OnError() {
                        Toast.makeText(RecuperatorActivity.this, "Błąd podczas transmisji", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void OnResponse(Object obj) {
                        Toast.makeText(RecuperatorActivity.this, "Zmieniono status", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}