package com.example.legends;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;

public class Home extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ImageView book = findViewById(R.id.cust_booking);
        ImageView reservations = findViewById(R.id.reservations);

        book.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent tsetIntent = new Intent(Home.this, Locations.class);
                startActivity(tsetIntent);
            }
        });

        reservations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent tsetIntent = new Intent(Home.this, CustomerReservations.class);
                startActivity(tsetIntent);
            }
        });
    }


}