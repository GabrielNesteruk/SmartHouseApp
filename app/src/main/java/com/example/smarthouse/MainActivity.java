package com.example.smarthouse;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.smarthouse.network.ESP32Service;
import com.example.smarthouse.network.PingService;
import com.example.smarthouse.utility.RoomCardView.CardViewAdapter;
import com.example.smarthouse.utility.RoomCardView.Room;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, CardViewAdapter.OnRoomListener {

    private RecyclerView featuredRecycler;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView menuBurger;
    private ProgressBar loadingBar;
    private ImageView refreshImg;

    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;

    private PingService pingService;
    private Handler mainHandler;


    private Snackbar connectionSnackbar;
    private Boolean noConnection;
    private ESP32Service esp32Service;

    private ArrayList rooms;
    private static final String TAG = "MainAcvitivty";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getSupportActionBar() != null)
            this.getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        Window window = this.getWindow();
        window.setStatusBarColor(getResources().getColor(R.color.black));

        featuredRecycler = findViewById(R.id.featured_recycler);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        menuBurger = findViewById(R.id.menu_burger);
        loadingBar = findViewById(R.id.loadingProgressBar);
        refreshImg = findViewById(R.id.refreshImg);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = mPreferences.edit();
        checkSharedPreferences();

        rooms = new ArrayList();
        esp32Service = new ESP32Service(this);

        mainHandler = new Handler(Looper.getMainLooper());
        pingService = new PingService(mPreferences.getString(getString(R.string.serverIp), ""), mainHandler, loadingBar);
        loadingBar.setVisibility(View.VISIBLE);
        pingService.pingServer((r) -> {
            handlePingCallback(r);
        });

        refreshImg.setOnClickListener(v -> {
            loadingBar.setVisibility(View.VISIBLE);
            pingService.pingServer((r) -> {
                handlePingCallback(r);
            });
        });

        ImageView connectionImg = findViewById(R.id.connectionImg);

        navigationDrawer();
    }

    private void navigationDrawer() {
        navigationView.bringToFront();
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_home);

        menuBurger.setOnClickListener(v -> {
            if(drawerLayout.isDrawerVisible(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            }
            else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    private void featuredRecycler() {
        featuredRecycler.setLayoutManager(new GridLayoutManager(this, 2));
        CardViewAdapter adapter = new CardViewAdapter(rooms, this, this);
        featuredRecycler.setAdapter(adapter);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        if(noConnection && item.getItemId() != R.id.nav_settings)
            return false;

        switch(item.getItemId())
        {
            case R.id.nav_settings:
            {
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                settingsIntent.putExtra("serverIp", mPreferences.getString(getString(R.string.serverIp), ""));
                startActivityForResult(settingsIntent, 1);
                break;
            }
        }

        drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    private void checkSharedPreferences() {
        String serverAddress = mPreferences.getString(getString(R.string.serverIp), "");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case 1:
            {
                if(resultCode == Activity.RESULT_OK)
                {
                    navigationView.setCheckedItem(R.id.nav_home);
                    String ip = data.getStringExtra("ip");
                    mEditor.putString("serverIp", ip);
                    mEditor.commit();
                    pingService.updateIp(mPreferences.getString(getString(R.string.serverIp), ""));
                    loadingBar.setVisibility(View.VISIBLE);
                    pingService.pingServer((r) -> {
                        handlePingCallback(r);
                    });
                }
                break;
            }
        }
    }

    private void handlePingCallback(int result) {
        loadingBar.setVisibility(View.INVISIBLE);

        if (result == 0) {

            if(connectionSnackbar != null) connectionSnackbar.dismiss();

            ((ImageView)(findViewById(R.id.connectionImg)))
                    .setImageDrawable(getDrawable(R.drawable.ic_wifi_on));
            Toast.makeText(MainActivity.this, "Połączono z serwerem", Toast.LENGTH_SHORT).show();
            noConnection = false;
            rooms = new ArrayList();
            esp32Service.getRoomsTemperature(rooms, new ESP32Service.ESP32ResponseListener() {
                @Override
                public void OnError() {

                }

                @Override
                public void OnResponse(Object obj) {
                    featuredRecycler();
                }
            });
        } else {
            connectionSnackbar = Snackbar.make(drawerLayout, "Brak połączenia z serwerm", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Ponów", v -> {
                        pingService.updateIp(mPreferences.getString(getString(R.string.serverIp), ""));
                        loadingBar.setVisibility(View.VISIBLE);
                        pingService.pingServer((r) -> {
                            handlePingCallback(r);
                        });
                    })
                    .setActionTextColor(Color.RED);
            connectionSnackbar.show();
            ((ImageView)(findViewById(R.id.connectionImg)))
                    .setImageDrawable(getDrawable(R.drawable.ic_wifi_off));
            noConnection = true;
        }
    }

    private void showTemperatureDialog(int position) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.change_temp_dialog);

        ImageView btnClose = dialog.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> {
            dialog.dismiss();
        });

        EditText temperatureValue = dialog.findViewById(R.id.editTextNumberDecimal);

        esp32Service.getSetRoomTemperature(position, new ESP32Service.ESP32ResponseListener() {
            @Override
            public void OnError() {

            }

            @Override
            public void OnResponse(Object obj) {
                temperatureValue.setText(((Double)obj).toString());
                dialog.show();
            }
        });

        Button btnSet = dialog.findViewById(R.id.btnSet);
        btnSet.setOnClickListener(v -> {

            try {
                esp32Service.setRoomTemperature(position, Double.parseDouble(temperatureValue.getText().toString()), new ESP32Service.ESP32ResponseListener() {
                    @Override
                    public void OnError() {
                        Toast.makeText(MainActivity.this, "Wystąpił błąd", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void OnResponse(Object obj) {
                        dialog.dismiss();
                    }
                });
            } catch (NumberFormatException ex) {
                Toast.makeText(MainActivity.this, "Zły format liczby", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRoomClick(int position) {
        if(position >= 0 && position <= 3)
            showTemperatureDialog(position);
    }
}