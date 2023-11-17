package com.example.legends;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class BarberReservations extends AppCompatActivity {
    private String loggedInUser;
    private ListView reservationsListView;
    private ArrayAdapter<String> reservationsAdapter;
    private static final String DB_PATH = "dbLegends/Locations/East Rand Mall/Payments";
    private FirebaseDatabase firebaseDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barber_reservations);

        reservationsListView = findViewById(R.id.reservationsListView);
        reservationsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

        // Link the ListView to the adapter
        //reservationsListView.setAdapter(reservationsAdapter);

            String barberLastName = "moloi";

            firebaseDatabase = FirebaseDatabase.getInstance();
            DatabaseReference reservationsRef = firebaseDatabase.getReference(DB_PATH);

            // Simplified query for testing without conditions
            Query query = reservationsRef;

            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // Clear the adapter to prevent duplications
                    reservationsAdapter.clear();

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String barberName = snapshot.child("barberName").getValue(String.class);
                        if (barberName != null && barberName.equals(barberLastName)) {
                            ArrayList<String> paymentList = new ArrayList<>();

                            for (DataSnapshot snapshot_ : dataSnapshot.getChildren()) {
                                // Parse payment data and add it to the list
                                Payment payment = snapshot_.getValue(Payment.class); // Assuming you have a Payment class

                                if (payment != null && payment.getBarberName().equals(barberLastName)) {
                                    // Format the payment data as needed (e.g., to a string)
                                    String paymentInfo = "Barber: " + payment.getBarberName() +
                                            "\nHaircut: " + payment.getHaircut() +
                                            "\nDate: " + payment.getDate() +
                                            "\nTime: " + payment.getTime() +
                                            "\nAmount: " + payment.getAmount();

                                    paymentList.add(paymentInfo);
                                }
                            }

                            ArrayAdapter<String> paymentAdapter = new ArrayAdapter<>(BarberReservations.this,
                                    android.R.layout.simple_list_item_1, paymentList);

                            reservationsListView.setAdapter(paymentAdapter);

                        }
                    }

                    // Notify the adapter that data has changed
                    reservationsAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle database error
                    Log.e("Firebase Error", "Error fetching reservations: " + databaseError.getMessage());
                }
            });
        }
    }


