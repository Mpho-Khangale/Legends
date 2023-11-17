package com.example.legends;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    EditText lastName, Email, number, Birthday, Password, ConPassword;
    Button Register;
    Button Signin;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lastName = findViewById(R.id.lastname);
        Email = findViewById(R.id.email);
        number = findViewById(R.id.phone);
        Birthday = findViewById(R.id.birthday);
        Password = findViewById(R.id.password);
        ConPassword = findViewById(R.id.cpassword);

        Register = findViewById(R.id.register);
        Signin = findViewById(R.id.signIn);

        Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String last = lastName.getText().toString();
                String em = Email.getText().toString();
                String num = number.getText().toString();
                String birth = Birthday.getText().toString();
                String pass = Password.getText().toString();
                String conPass = ConPassword.getText().toString();

                if (TextUtils.isEmpty(last) || TextUtils.isEmpty(em) || TextUtils.isEmpty(num) || TextUtils.isEmpty(birth) || TextUtils.isEmpty(pass) || TextUtils.isEmpty(conPass)) {
                    Toast.makeText(MainActivity.this, "All fields required", Toast.LENGTH_SHORT).show();
                }else if (!isNameValid(last)) {
                    Toast.makeText(MainActivity.this, "Invalid name. Please enter a valid name.", Toast.LENGTH_SHORT).show();
                }
                else if (!isPasswordValid(pass)) {
                    Toast.makeText(MainActivity.this, "Password must contain uppercase and lowercase letters, numbers, and symbols", Toast.LENGTH_SHORT).show();
                } else {
                    // Continue with user registration
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(em, pass)
                            .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // User registration successful
                                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                        if (user != null) {
                                            // Send email verification
                                            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        // Verification email sent successfully
                                                        Toast.makeText(MainActivity.this, "Registered Successfully. Verification email sent.", Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(getApplicationContext(), Home.class);
                                                        startActivity(intent);
                                                    } else {
                                                        // If verification email sending fails, display a message to the user.
                                                        Toast.makeText(MainActivity.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }

                                        // Save user data to the database
                                        String userId = user.getUid();
                                        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference()
                                                .child("dbLegends").child("Customers").child(last);

                                        HashMap<String, Object> userData = new HashMap<>();
                                        userData.put("LastName", last);
                                        userData.put("Email", em);
                                        userData.put("Number", num);
                                        userData.put("Birthday", birth);
                                        userData.put("Password", pass);

                                        userReference.setValue(userData);
                                    } else {
                                        // If registration fails, display a message to the user.
                                        Toast.makeText(MainActivity.this, "Registration failed.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });


        Signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);

            }
        });
    }
    private boolean isPasswordValid(String password) {
        // Add your password complexity requirements here
        // For example, check if the password contains at least one uppercase letter, one lowercase letter, one digit, and one special character.
        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        return password.matches(passwordRegex);
    }
    private boolean isNameValid(String name) {
        // Add your name validation criteria here
        // For example, check if the name contains at least two characters and does not contain numbers
        return name.length() >= 2 && !name.matches(".*\\d.*");
    }
}


