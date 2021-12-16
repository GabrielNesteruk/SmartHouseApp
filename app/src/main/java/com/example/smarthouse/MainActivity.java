package com.example.smarthouse;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.smarthouse.network.PingService;
import com.example.smarthouse.utility.RoomCardView.CardViewAdapter;
import com.example.smarthouse.utility.RoomCardView.CardViewHelper;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private RecyclerView featuredRecycler;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView menuBurger;

    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;

    private PingService pingService;
    private Handler mainHandler;


    private Snackbar connectionSnackbar;
    private Boolean noConnection;
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
        featuredRecycler();

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        menuBurger = findViewById(R.id.menu_burger);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = mPreferences.edit();
        checkSharedPreferences();

        mainHandler = new Handler(Looper.getMainLooper());
        pingService = new PingService(mPreferences.getString(getString(R.string.serverIp), ""), mainHandler);
        pingService.pingServer((r) -> {
            handlePingCallback(r);
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

        ArrayList<CardViewHelper> data = new ArrayList<>();
        data.add(new CardViewHelper("https://images.unsplash.com/photo-1554995207-c18c203602cb?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxzZWFyY2h8Mnx8cm9vbXxlbnwwfHwwfHw%3D&w=1000&q=80", "Salon", "Temperatura", "20 °C"));
        data.add(new CardViewHelper("https://images.unsplash.com/photo-1554995207-c18c203602cb?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxzZWFyY2h8Mnx8cm9vbXxlbnwwfHwwfHw%3D&w=1000&q=80", "Pokój 1", "Temperatura", "22 °C"));
        data.add(new CardViewHelper("https://images.unsplash.com/photo-1554995207-c18c203602cb?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxzZWFyY2h8Mnx8cm9vbXxlbnwwfHwwfHw%3D&w=1000&q=80", "Pokój 2", "Temperatura", "25 °C"));
        data.add(new CardViewHelper("https://images.unsplash.com/photo-1554995207-c18c203602cb?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxzZWFyY2h8Mnx8cm9vbXxlbnwwfHwwfHw%3D&w=1000&q=80", "Kuchnia", "Temperatura", "21 °C"));
        data.add(new CardViewHelper("https://images.unsplash.com/photo-1554995207-c18c203602cb?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxzZWFyY2h8Mnx8cm9vbXxlbnwwfHwwfHw%3D&w=1000&q=80", "Korytarz", "Temperatura", "24 °C"));

        CardViewAdapter adapter = new CardViewAdapter(data, this);
        featuredRecycler.setAdapter(adapter);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        if(!noConnection && item.getItemId() != R.id.nav_settings)
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
                    String ip = data.getStringExtra("ip");
                    mEditor.putString("serverIp", ip);
                    mEditor.commit();
                    pingService.updateIp(mPreferences.getString(getString(R.string.serverIp), ""));
                    pingService.pingServer((r) -> {
                        handlePingCallback(r);
                    });
                }
                break;
            }
        }
    }

    private void handlePingCallback(int result) {
        if (result == 0) {

            if(connectionSnackbar != null) connectionSnackbar.dismiss();

            ((ImageView)(findViewById(R.id.connectionImg)))
                    .setImageDrawable(getDrawable(R.drawable.ic_wifi_on));
            Toast.makeText(MainActivity.this, "Połączono z serwerem", Toast.LENGTH_SHORT).show();
        } else {
            connectionSnackbar = Snackbar.make(drawerLayout, "Brak połączenia z serwerm", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Ponów", v -> {
                        pingService.updateIp(mPreferences.getString(getString(R.string.serverIp), ""));
                        pingService.pingServer((r) -> {
                            handlePingCallback(r);
                        });
                    })
                    .setActionTextColor(Color.RED);
            connectionSnackbar.show();
            ((ImageView)(findViewById(R.id.connectionImg)))
                    .setImageDrawable(getDrawable(R.drawable.ic_wifi_off));
            noConnection = false;
        }
    }
}