package com.example.legends;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CustomerReservations extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_reservations);

        // Initialize Firebase in onCreate
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference paymentsRef = firebaseDatabase.getReference("dbLegends").child("Locations").child("East Rand Mall").child("Payments");

        ListView paymentListView = findViewById(R.id.paymentListView); // Link to your ListView in XML

        paymentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> paymentList = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Parse payment data and add it to the list
                    Payment payment = snapshot.getValue(Payment.class); // Assuming you have a Payment class

                    if (payment != null) {
                        // Format the payment data as needed (e.g., to a string)
                        String paymentInfo = "Barber: " + payment.getBarberName() +
                                "\nHaircut: " + payment.getHaircut() +
                                "\nDate: " + payment.getDate() +
                                "\nTime: " + payment.getTime() +
                                "\nAmount: " + payment.getAmount();

                        paymentList.add(paymentInfo);
                    }
                }

                ArrayAdapter<String> paymentAdapter = new ArrayAdapter<>(CustomerReservations.this,
                        android.R.layout.simple_list_item_1, paymentList);

                paymentListView.setAdapter(paymentAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
            }
        });



    }
}