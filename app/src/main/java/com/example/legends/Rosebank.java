package com.example.legends;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class Rosebank extends AppCompatActivity {

    private Spinner rosebankBarber;
    private Spinner rosebankHaircuts;
    private CalendarView rosebankCalender;
    private Button rosebankCardPay;
    private DatabaseReference databaseReference;
    private DatabaseReference paymentRef;
    private String selectedDate;
    private CompletableFuture<Void> paymentDataFuture;
    String SECRET_kEY = "sk_test_51NzEJyE8vrIx9K9VbqzwKC8euB30VkhfLlJKOBdLonHxTQf8X1uw2IlT6u6Ej5HRn0LGqlGjR9yRo8brt9PAUJ8p00cUQAWYsE";
    String PUBLIC_KEY = "pk_test_51NzEJyE8vrIx9K9VqvrxogwmFvIhw8jlMniW9OoxNjDmmYyTfRpFtoGJIjQqvvtkDJGvW4vixR5W0kulpkZ8ERJ900mjGXlrBO";
    PaymentSheet paymentSheet;
    String customerID;
    String EpherialKey;
    String ClientSecret;
    String[] cuts;
    private Spinner haircutTimesDropdown;
    private ArrayList<String> availableHaircutTimes;
    private ArrayList<String> bookedHaircutTimes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rosebank);

        rosebankBarber = findViewById(R.id.barbers_rosebank);
        rosebankHaircuts = findViewById(R.id.haircuts_time_rosebank);
        rosebankCalender = findViewById(R.id.calendar_rosebank);
        rosebankCardPay = findViewById(R.id.cardPayment_rosebank);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            paymentDataFuture = new CompletableFuture<>();
        }

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("dbLegends").child("Locations").child("Rosebank");
        paymentRef = firebaseDatabase.getReference("dbLegends");

        fetchEastRandBarber();
        intoEastRandHair();
        fetchCustomerID();

        haircutTimesDropdown = findViewById(R.id.haircutTimesDropdown_rosebank);
        availableHaircutTimes = new ArrayList<>();
// Initialize the dropdown and set an adapter for it
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, availableHaircutTimes);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        haircutTimesDropdown.setAdapter(timeAdapter);

        bookedHaircutTimes = new ArrayList<>();

        rosebankCalender.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                selectedDate = year + "/" + (month + 1) + "/" + dayOfMonth;
                fetchAvailableHaircutTimes(selectedDate);
            }
        });


        rosebankCardPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (paymentDataFuture != null) {
                    PaymentFlow();
                } else {
                    Toast.makeText(Rosebank.this, "Payment configuration is incomplete", Toast.LENGTH_SHORT).show();
                }
            }
        });

        PaymentConfiguration.init(this, PUBLIC_KEY);
        paymentSheet = new PaymentSheet(this, paymentSheetResult ->
        {
            onPaymentResult(paymentSheetResult);

        });

        databaseReference = firebaseDatabase.getReference("dbLegends").child("Locations").child("East Rand Mall");
        fetchBookedHaircutTimes(selectedDate);

    }

    private void fetchCustomerID() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                " https://api.stripe.com/v1/customers",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject object = new JSONObject(response);
                            customerID = object.getString("id");
                            Toast.makeText(Rosebank.this, customerID, Toast.LENGTH_SHORT).show();

                            getEphericalKey(customerID);

                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                header.put("Authorization", "Bearer " + SECRET_kEY);
                return header;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(Rosebank.this);
        requestQueue.add(stringRequest);

        getEphericalKey(customerID);
    }


    private void onPaymentResult(PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            savePaymentToFirebase();
            Toast.makeText(this, "Payment Successful", Toast.LENGTH_SHORT).show();
        }
    }

    private void getEphericalKey(String customerID) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                "https://api.stripe.com/v1/ephemeral_keys",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object = new JSONObject(response);
                            EpherialKey = object.getString("id");
                            Toast.makeText(Rosebank.this, EpherialKey, Toast.LENGTH_SHORT).show();

                            // Complete the paymentDataFuture when the data is available
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                paymentDataFuture.complete(null);
                            }

                            getClientSecret(customerID, EpherialKey);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle error
                Log.e("Stripe Error", error.toString());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                header.put("Authorization", "Bearer " + SECRET_kEY);
                header.put("Stripe-Version", "2023-08-16");
                return header;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                if (customerID != null) {
                    params.put("customer", customerID);
                }
                //params.put("customer", customerID);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(Rosebank.this);
        requestQueue.add(stringRequest);
    }

    private void getClientSecret(String customerID, String epherialKey) {

        String selectedHaircut = rosebankHaircuts.getSelectedItem().toString();
        String amount = calculateAmount(selectedHaircut);


        //String finalAmount = amount;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://api.stripe.com/v1/payment_intents",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object = new JSONObject(response);
                            ClientSecret = object.getString("client_secret");
                            Toast.makeText(Rosebank.this, ClientSecret, Toast.LENGTH_SHORT).show();
                            // Now that you have all the data, call PaymentFlow.
                            PaymentFlow();
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle error
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                header.put("Authorization", "Bearer " + SECRET_kEY);
                return header;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = null;
                params = new HashMap<>();
                params.put("customer", customerID);
                params.put("amount", amount);
                params.put("currency", "zar");
                params.put("automatic_payment_methods[enabled]", "true");
                return params;
            }
        };

        // Add the request to the request queue.
        RequestQueue requestQueue = Volley.newRequestQueue(Rosebank.this);
        requestQueue.add(stringRequest);
    }

    private String calculateAmount(String selectedHaircut) {
        String amount = "0"; // Default value
        selectedHaircut = selectedHaircut.trim();

        switch (selectedHaircut) {
            case "Fade & Line design ~ +-50min":
                amount = "17000";
                break;
            case "Fade, Dye & Line design ~ +-40min":
                amount = "25000";
                break;
            case "Fade & Custom Design ~ +-50min":
                amount = "19000";
                break;
            case "High Top Fade ~ +-50min":
                amount = "15000";
                break;
            case "Fade, Powder & Custom Design ~ +-40min":
                amount = "19000";
                break;
            case "Fade & Shave ~ +-50min":
                amount = "23000";
                break;
            default:
                Log.d("Selected Haircut", selectedHaircut);
                break;
            // Add more cases for other haircuts if needed
        }
        return amount;
    }


    private void PaymentFlow() {
        if (paymentDataFuture != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (paymentDataFuture.isDone()) {
                    String selectedTime = (String) haircutTimesDropdown.getSelectedItem();
                    if (selectedTime != null) {
                        // Check if it's not null before converting to a string
                        if (bookedHaircutTimes.contains(selectedTime.toString())) {
                            // Display a message to the user or handle the case where the time is already booked
                            Toast.makeText(this, "Selected time is already booked", Toast.LENGTH_SHORT).show();
                        } else {
                            // Payment data is available, proceed with PaymentFlow
                            paymentSheet.presentWithPaymentIntent(
                                    ClientSecret,
                                    new PaymentSheet.Configuration("Legends Barbershop", new PaymentSheet.CustomerConfiguration(customerID, EpherialKey))
                            );
                        }
                    } else {
                        // Handle the case where the selected time is null
                        Toast.makeText(Rosebank.this, "No time selected", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle the case where the necessary data is not yet available
                    Toast.makeText(Rosebank.this, "Payment configuration is incomplete", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    private void fetchAvailableHaircutTimes(String selectedDate) {
        // You should implement logic to retrieve available times for the selectedDate
        // For this example, we'll assume available times are hardcoded, but you can fetch them from a data source
        availableHaircutTimes.clear();
        availableHaircutTimes.add("10:30");
        availableHaircutTimes.add("12:30");
        availableHaircutTimes.add("14:30");
        availableHaircutTimes.add("16:30");

        // Notify the adapter that the data has changed
        ((ArrayAdapter) haircutTimesDropdown.getAdapter()).notifyDataSetChanged();
    }
    private void fetchBookedHaircutTimes(String selectedDate) {
        DatabaseReference bookingsRef = paymentRef.child("Locations").child("Rosebank").child("Payments"); // Replace with the correct path to your bookings data

        // Create a query to get bookings for the selected date
        Query query = bookingsRef.orderByChild("date").equalTo(selectedDate);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                bookedHaircutTimes.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Retrieve the booked time from the database
                    String bookedTime = snapshot.child("time").getValue(String.class);
                    if (bookedTime != null) {
                        bookedHaircutTimes.add(bookedTime);
                    }
                }

                // Notify the adapter that the data has changed
                ((ArrayAdapter) haircutTimesDropdown.getAdapter()).notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
                Log.e("Firebase Error", "Error fetching booked times: " + databaseError.getMessage());
            }
        });
    }



// Inside your existing code where you handle the selected date:


    private void savePaymentToFirebase() {
        // Get the selected payment information
        String selectedBarber = rosebankBarber.getSelectedItem().toString();
        String selectedHaircut = rosebankHaircuts.getSelectedItem().toString();
        String paymentAmount = calculateAmount(selectedHaircut);
        String paymentDate = selectedDate;
        String selectedTime = haircutTimesDropdown.getSelectedItem().toString();

        //fetchBookedHaircutTimes(selectedDate);

        // Check if the selected time is already booked
        if (bookedHaircutTimes.contains(selectedTime)) {
            // Display a message to the user or handle the case where the time is already booked
            Toast.makeText(this, "Selected time is already booked", Toast.LENGTH_SHORT).show();
            return;
        }

        // Initialize Firebase database reference
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference paymentsRef = database.getReference("dbLegends").child("Locations").child("Rosebank").child("Payments"); // Change this to your desired Firebase database node

        // Create a unique payment ID or use a timestamp
        String paymentId = String.valueOf(System.currentTimeMillis());

        // Create a Payment object to store payment information
        Payment payment = new Payment(selectedBarber, selectedHaircut, paymentAmount, paymentDate, selectedTime);

        // Save the payment information to Firebase
        paymentsRef.child(paymentId).setValue(payment);

        // You can also add additional logic or error handling as needed

        // Optionally, you can display a confirmation message to the user
        Toast.makeText(this, "Payment information saved to Firebase", Toast.LENGTH_SHORT).show();
    }




    private void intoEastRandHair() {
        cuts = new String[]{"Fade & Line design ~ +-50min", "Fade, Dye & Line design ~ +-40min", "Fade & Custom Design ~ +-50min", "High Top Fade ~ +-50min",
                "Fade, Powder & Custom Design ~ +-40min", "Fade & Shave ~ +-50min"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(Rosebank.this, android.R.layout.simple_spinner_item, cuts);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rosebankHaircuts.setAdapter(adapter);
    }

    private void fetchEastRandBarber() {
        databaseReference.child("Barbers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> customerLocations = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String lastName = snapshot.child("LastName").getValue(String.class);
                    if (lastName != null) {
                        customerLocations.add(lastName);
                    }
                }

                ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(Rosebank.this,
                        android.R.layout.simple_spinner_item, customerLocations);
                locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                rosebankBarber.setAdapter(locationAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
            }
        });
    }

}
