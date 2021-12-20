package com.example.smarthouse;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.graphics.Color;
import android.graphics.ColorFilter;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smarthouse.model.HeatPump;
import com.example.smarthouse.network.ESP32Service;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class HeatPumpActivity extends AppCompatActivity {

    private SwitchMaterial status;
    private ImageView acMode;
    private ImageView heatMode;
    private ImageView dryMode;
    private ImageView fanMode;

    private TextView tempSet;
    private TextView tempPanel;
    private TextView tempDiff;
    private TextView tempExtract;
    private TextView tempOutdoor;
    private TextView tempSupply;

    private TextView energyCurrent;
    private TextView energyHour;
    private TextView energyToday;

    private EditText supplyValue;
    private EditText tempSetValue;

    private ImageView refresh;

    private Button fanBtn;
    private Button tempSetBtn;

    private ESP32Service esp32Service;
    private ColorFilter defaultColorFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getSupportActionBar() != null)
            this.getSupportActionBar().hide();
        setContentView(R.layout.activity_heat_pump);

        Window window = this.getWindow();
        window.setStatusBarColor(getResources().getColor(R.color.black));

        status = findViewById(R.id.heatpumpStatus);
        acMode = findViewById(R.id.modeAc);
        heatMode = findViewById(R.id.modeHeat);
        dryMode = findViewById(R.id.modeDry);
        fanMode = findViewById(R.id.modeFan);

        tempSet = findViewById(R.id.temp_set);
        tempPanel = findViewById(R.id.temp_panel);
        tempDiff = findViewById(R.id.temp_diff);
        tempExtract = findViewById(R.id.temp_extract);
        tempOutdoor = findViewById(R.id.temp_outdoor);
        tempSupply = findViewById(R.id.temp_supply);

        energyCurrent = findViewById(R.id.energy_current);
        energyHour = findViewById(R.id.energy_hour);
        energyToday = findViewById(R.id.energy_today);

        supplyValue = findViewById(R.id.supply_value);
        tempSetValue = findViewById(R.id.temp_set_value);

        fanBtn = findViewById(R.id.fanBtn);
        tempSetBtn = findViewById(R.id.tempSetBtn);

        refresh = findViewById(R.id.refresh);

        esp32Service = new ESP32Service(this);
        defaultColorFilter = acMode.getColorFilter();

        ImageView arrowBack = findViewById(R.id.arrow_back);
        arrowBack.setOnClickListener(v -> {
            finish();
        });

        refresh.setOnClickListener(v -> {
            getData();
        });

        handleStatusChange();
        handleModeChange();
        handleButtons();

        getData();
    }

    private void loadData(HeatPump heatPump) {
        if(heatPump.isStatus()) status.setChecked(true);
        else status.setChecked(false);

        switch(heatPump.getMode()) {
            case 0:
                acMode.setColorFilter(ContextCompat.getColor(this, R.color.ac));
                break;
            case 1:
                heatMode.setColorFilter(ContextCompat.getColor(this, R.color.red));
                break;
            case 2:
                dryMode.setColorFilter(ContextCompat.getColor(this, R.color.blue));
                break;
            case 3:
                fanMode.setColorFilter(ContextCompat.getColor(this, R.color.ac));
                break;
        }

        tempSet.setText(String.format("%s °C", heatPump.getTemperatureSet().toString()));
        tempPanel.setText(String.format("%s °C", heatPump.getTemperaturePanel().toString()));
        tempDiff.setText(String.format("%s °C", heatPump.getTemperatureDifference().toString()));
        tempExtract.setText(String.format("%s °C", heatPump.getTemperatureExtract().toString()));
        tempOutdoor.setText(String.format("%s °C", heatPump.getTemperatureOutdoor().toString()));
        tempSupply.setText(String.format("%s °C", heatPump.getTemperatureSupply().toString()));


        energyCurrent.setText(Integer.valueOf(heatPump.getEnergyCurrent()).toString() + " W");
        energyHour.setText(Integer.valueOf(heatPump.getEnergyHour()).toString() + " W");
        energyToday.setText(Integer.valueOf(heatPump.getEnergyToday()).toString() + " kWh");

        supplyValue.setText(Integer.valueOf(heatPump.getFanSpeed()).toString());
        tempSetValue.setText(heatPump.getTemperatureSet().toString());
    }

    private void getData() {
        ESP32Service esp32Service = new ESP32Service(this);
        esp32Service.getHeatPumpData(new ESP32Service.ESP32ResponseListener() {
            @Override
            public void OnError() {
                Toast.makeText(HeatPumpActivity.this, "Nie udało się wczytać danych", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void OnResponse(Object obj) {
                loadData((HeatPump) obj);
            }
        });
    }

    private void handleStatusChange() {
        status.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                esp32Service.turnHeatPump(isChecked, new ESP32Service.ESP32ResponseListener() {
                    @Override
                    public void OnError() {
                        Toast.makeText(HeatPumpActivity.this, "Błąd podczas transmisji", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void OnResponse(Object obj) {
                        Toast.makeText(HeatPumpActivity.this, "Zmieniono status", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void handleModeChange() {
        acMode.setOnClickListener(v -> {
            clearModeIconsColor();
            acMode.setColorFilter(ContextCompat.getColor(this, R.color.ac));
            esp32Service.setHeatpumpMode(0);
        });

        heatMode.setOnClickListener(v -> {
            clearModeIconsColor();
            heatMode.setColorFilter(ContextCompat.getColor(this, R.color.red));
            esp32Service.setHeatpumpMode(1);
        });

        dryMode.setOnClickListener(v -> {
            clearModeIconsColor();
            dryMode.setColorFilter(ContextCompat.getColor(this, R.color.blue));
            esp32Service.setHeatpumpMode(2);
        });

        fanMode.setOnClickListener(v -> {
            clearModeIconsColor();
            fanMode.setColorFilter(ContextCompat.getColor(this, R.color.ac));
            esp32Service.setHeatpumpMode(3);
        });
    }

    private void clearModeIconsColor() {
        acMode.setColorFilter(defaultColorFilter);
        heatMode.setColorFilter(defaultColorFilter);
        dryMode.setColorFilter(defaultColorFilter);
        fanMode.setColorFilter(defaultColorFilter);
    }

    private void handleButtons() {
        fanBtn.setOnClickListener(v -> {
            esp32Service.setHeatpumpFanSpeed(Integer.parseInt(supplyValue.getText().toString()));
        });

        tempSetBtn.setOnClickListener(v -> {
            esp32Service.setHeatpumpTemperature(Integer.parseInt(tempSetValue.getText().toString()));
        });
    }
}