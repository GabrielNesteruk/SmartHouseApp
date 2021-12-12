package com.example.smarthouse;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.smarthouse.utility.RoomCardView.CardViewAdapter;
import com.example.smarthouse.utility.RoomCardView.CardViewHelper;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private RecyclerView featuredRecycler;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView menuBurger;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getSupportActionBar() != null)
            this.getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        featuredRecycler = findViewById(R.id.featured_recycler);
        featuredRecycler();

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        menuBurger = findViewById(R.id.menu_burger);

        ImageView connectionImg = findViewById(R.id.connectionImg);
        connectionImg.setOnClickListener(v -> {
            Toast.makeText(this, "Jesteś połączony z siecią.", Toast.LENGTH_SHORT).show();
        });

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
        return true;
    }
}