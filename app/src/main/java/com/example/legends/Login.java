package com.example.legends;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {

    EditText username, password;
    Button login;

    private int wrongPasswordAttempts = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username = findViewById(R.id.login_email);
        password = findViewById(R.id.login_password);

        login = findViewById(R.id.login);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = username.getText().toString();
                String passw = password.getText().toString();

                if (TextUtils.isEmpty(user) || TextUtils.isEmpty(passw)) {
                    Toast.makeText(Login.this, "All fields required", Toast.LENGTH_SHORT).show();
                } else {
                    checkUser(user, passw);
                }
            }
        });
    }

    public void checkUser(String user, String passw) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://legends-a1391-default-rtdb.firebaseio.com/");

        DatabaseReference adminRef = reference.child("dbLegends").child("Barbers");
        DatabaseReference customerRef = reference.child("dbLegends").child("Customers");

        adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(user)) {
                    String passwordFromDB = dataSnapshot.child(user).child("Password").getValue(String.class);
                    String usernameFromDB = dataSnapshot.child(user).child("LastName").getValue(String.class);
                    if (passwordFromDB != null && passwordFromDB.equals(passw) && usernameFromDB.equals(user)) {
                        Intent adminIntent = new Intent(Login.this, BarberHome.class);
                        startActivity(adminIntent);
                    } else {
                        handleWrongPassword();
                        password.setError("Invalid Credentials");
                        password.requestFocus();
                    }
                } else {
                    customerRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(user)) {
                                String passwordFromDB = dataSnapshot.child(user).child("Password").getValue(String.class);
                                String usernameFromDB = dataSnapshot.child(user).child("LastName").getValue(String.class);
                                if (passwordFromDB != null && passwordFromDB.equals(passw) && usernameFromDB.equals(user)) {
                                    Intent customerIntent = new Intent(Login.this, Home.class);
                                    startActivity(customerIntent);
                                } else {
                                    handleWrongPassword();
                                    password.setError("Invalid Credentials");
                                    password.requestFocus();
                                }
                            } else {
                                username.setError("User does not exist");
                                username.requestFocus();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.w(TAG, "Failed to read value.", error.toException());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }


    private void handleWrongPassword() {
        wrongPasswordAttempts++;

        // Check if the user has entered the wrong password three times
        if (wrongPasswordAttempts >= 3) {
            // Show a Toast to inform the user and navigate to the ForgotPassword activity
            Toast.makeText(Login.this, "Forgot Password? Click here.", Toast.LENGTH_LONG).show();

            // Navigate to the ForgotPassword activity
            Intent forgotPasswordIntent = new Intent(Login.this, ForgotPassword.class);
            startActivity(forgotPasswordIntent);
            finish(); // Optional: finish the current activity to prevent the user from going back
        }
    }
}
