package com.example.legends;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {
    private EditText emailEditText;
    private Button resetPasswordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        emailEditText = findViewById(R.id.email_reset);
        resetPasswordButton = findViewById(R.id.btn_reset_password);

        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Enter your registered email ID", Toast.LENGTH_LONG).show();
                    return;
                }

                // Send password reset email
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ForgotPassword.this,
                                            "Password reset email sent. Check your email inbox.",
                                            Toast.LENGTH_LONG).show();
                                    Intent customerIntent = new Intent(ForgotPassword.this, Login.class);
                                    startActivity(customerIntent);
                                } else {
                                    Toast.makeText(ForgotPassword.this,
                                            "Failed to send reset email. Check your email address.",
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });
    }
}