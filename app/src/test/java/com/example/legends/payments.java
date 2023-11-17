package com.example.legends;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.util.Log;
import android.view.Display;
import android.view.PixelCopy;
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
import com.stripe.android.PaymentSession;
import com.stripe.android.PaymentSessionConfig;
import com.stripe.android.model.PaymentMethod;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;
import com.stripe.android.view.CardInputWidget;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import java.io.Serializable; // If you intend to use Parcelable or Serializable
import com.google.firebase.database.IgnoreExtraProperties; // If you want to ignore extra fields

public class payments extends Context {
    private Spinner eastRandBarber;
    private Spinner eastRandHaircuts;
    private CalendarView eastRandCalender;
    private Button eastRandCardPay;
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

    public void payment() {

        //databaseReference = firebaseDatabase.getReference("dbLegends").child("Locations").child("East Rand Mall");
        //fetchBookedHaircutTimes(selectedDate);
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
                            //Toast.makeText(east_rand.this, customerID, Toast.LENGTH_SHORT).show();

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
        RequestQueue requestQueue = Volley.newRequestQueue(this);
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
                            //Toast.makeText(east_rand.this, EpherialKey, Toast.LENGTH_SHORT).show();

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
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void getClientSecret(String customerID, String epherialKey) {

        String selectedHaircut = eastRandHaircuts.getSelectedItem().toString();
        String amount = calculateAmount(selectedHaircut);


        //String finalAmount = amount;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://api.stripe.com/v1/payment_intents",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object = new JSONObject(response);
                            ClientSecret = object.getString("client_secret");
                            //Toast.makeText(east_rand.this, ClientSecret, Toast.LENGTH_SHORT).show();
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
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void PaymentFlow() {
        if (paymentDataFuture != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (paymentDataFuture.isDone()) {
                    paymentSheet.presentWithPaymentIntent(
                            ClientSecret,
                            new PaymentSheet.Configuration("Legends Barbershop", new PaymentSheet.CustomerConfiguration(customerID, EpherialKey))
                    );
                }
                //String selectedTime = (String) haircutTimesDropdown.getSelectedItem();
                    /*if (selectedTime != null) {
                        // Check if it's not null before converting to a string
                        if (bookedHaircutTimes.contains(selectedTime.toString())) {
                            // Display a message to the user or handle the case where the time is already booked
                            //Toast.makeText(this, "Selected time is already booked", Toast.LENGTH_SHORT).show();
                        } else {
                            // Payment data is available, proceed with PaymentFlow
                            paymentSheet.presentWithPaymentIntent(
                                    ClientSecret,
                                    new PaymentSheet.Configuration("Legends Barbershop", new PaymentSheet.CustomerConfiguration(customerID, EpherialKey))
                            );
                        }
                    } else {
                        // Handle the case where the selected time is null
                        //Toast.makeText(east_rand.this, "No time selected", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle the case where the necessary data is not yet available
                    //Toast.makeText(east_rand.this, "Payment configuration is incomplete", Toast.LENGTH_SHORT).show();
                }
            }*/
            }
        }

    }
    private void savePaymentToFirebase() {
        // Get the selected payment information
        String selectedBarber = eastRandBarber.getSelectedItem().toString();
        String selectedHaircut = eastRandHaircuts.getSelectedItem().toString();
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
        DatabaseReference paymentsRef = database.getReference("dbLegends").child("Locations").child("East Rand Mall").child("Payments"); // Change this to your desired Firebase database node

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

    private String calculateAmount(String selectedHaircut) {
        String amount = "0"; // Default value
        selectedHaircut = selectedHaircut.toString().trim();

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


    @Override
    public AssetManager getAssets() {
        return null;
    }

    @Override
    public Resources getResources() {
        return null;
    }

    @Override
    public PackageManager getPackageManager() {
        return null;
    }

    @Override
    public ContentResolver getContentResolver() {
        return null;
    }

    @Override
    public Looper getMainLooper() {
        return null;
    }

    @Override
    public Context getApplicationContext() {
        return null;
    }

    @Override
    public void setTheme(int resid) {

    }

    @Override
    public Resources.Theme getTheme() {
        return null;
    }

    @Override
    public ClassLoader getClassLoader() {
        return null;
    }

    @Override
    public String getPackageName() {
        return null;
    }

    @Override
    public ApplicationInfo getApplicationInfo() {
        return null;
    }

    @Override
    public String getPackageResourcePath() {
        return null;
    }

    @Override
    public String getPackageCodePath() {
        return null;
    }

    @Override
    public SharedPreferences getSharedPreferences(String name, int mode) {
        return null;
    }

    @Override
    public boolean moveSharedPreferencesFrom(Context sourceContext, String name) {
        return false;
    }

    @Override
    public boolean deleteSharedPreferences(String name) {
        return false;
    }

    @Override
    public FileInputStream openFileInput(String name) throws FileNotFoundException {
        return null;
    }

    @Override
    public FileOutputStream openFileOutput(String name, int mode) throws FileNotFoundException {
        return null;
    }

    @Override
    public boolean deleteFile(String name) {
        return false;
    }

    @Override
    public File getFileStreamPath(String name) {
        return null;
    }

    @Override
    public File getDataDir() {
        return null;
    }

    @Override
    public File getFilesDir() {
        return null;
    }

    @Override
    public File getNoBackupFilesDir() {
        return null;
    }

    @Nullable
    @Override
    public File getExternalFilesDir(@Nullable String type) {
        return null;
    }

    @Override
    public File[] getExternalFilesDirs(String type) {
        return new File[0];
    }

    @Override
    public File getObbDir() {
        return null;
    }

    @Override
    public File[] getObbDirs() {
        return new File[0];
    }

    @Override
    public File getCacheDir() {
        return null;
    }

    @Override
    public File getCodeCacheDir() {
        return null;
    }

    @Nullable
    @Override
    public File getExternalCacheDir() {
        return null;
    }

    @Override
    public File[] getExternalCacheDirs() {
        return new File[0];
    }

    @Override
    public File[] getExternalMediaDirs() {
        return new File[0];
    }

    @Override
    public String[] fileList() {
        return new String[0];
    }

    @Override
    public File getDir(String name, int mode) {
        return null;
    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory) {
        return null;
    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory, @Nullable DatabaseErrorHandler errorHandler) {
        return null;
    }

    @Override
    public boolean moveDatabaseFrom(Context sourceContext, String name) {
        return false;
    }

    @Override
    public boolean deleteDatabase(String name) {
        return false;
    }

    @Override
    public File getDatabasePath(String name) {
        return null;
    }

    @Override
    public String[] databaseList() {
        return new String[0];
    }

    @Override
    public Drawable getWallpaper() {
        return null;
    }

    @Override
    public Drawable peekWallpaper() {
        return null;
    }

    @Override
    public int getWallpaperDesiredMinimumWidth() {
        return 0;
    }

    @Override
    public int getWallpaperDesiredMinimumHeight() {
        return 0;
    }

    @Override
    public void setWallpaper(Bitmap bitmap) throws IOException {

    }

    @Override
    public void setWallpaper(InputStream data) throws IOException {

    }

    @Override
    public void clearWallpaper() throws IOException {

    }

    @Override
    public void startActivity(Intent intent) {

    }

    @Override
    public void startActivity(Intent intent, @Nullable Bundle options) {

    }

    @Override
    public void startActivities(Intent[] intents) {

    }

    @Override
    public void startActivities(Intent[] intents, Bundle options) {

    }

    @Override
    public void startIntentSender(IntentSender intent, @Nullable Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags) throws IntentSender.SendIntentException {

    }

    @Override
    public void startIntentSender(IntentSender intent, @Nullable Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags, @Nullable Bundle options) throws IntentSender.SendIntentException {

    }

    @Override
    public void sendBroadcast(Intent intent) {

    }

    @Override
    public void sendBroadcast(Intent intent, @Nullable String receiverPermission) {

    }

    @Override
    public void sendOrderedBroadcast(Intent intent, @Nullable String receiverPermission) {

    }

    @Override
    public void sendOrderedBroadcast(@NonNull Intent intent, @Nullable String receiverPermission, @Nullable BroadcastReceiver resultReceiver, @Nullable Handler scheduler, int initialCode, @Nullable String initialData, @Nullable Bundle initialExtras) {

    }

    @Override
    public void sendBroadcastAsUser(Intent intent, UserHandle user) {

    }

    @Override
    public void sendBroadcastAsUser(Intent intent, UserHandle user, @Nullable String receiverPermission) {

    }

    @Override
    public void sendOrderedBroadcastAsUser(Intent intent, UserHandle user, @Nullable String receiverPermission, BroadcastReceiver resultReceiver, @Nullable Handler scheduler, int initialCode, @Nullable String initialData, @Nullable Bundle initialExtras) {

    }

    @Override
    public void sendStickyBroadcast(Intent intent) {

    }

    @Override
    public void sendStickyOrderedBroadcast(Intent intent, BroadcastReceiver resultReceiver, @Nullable Handler scheduler, int initialCode, @Nullable String initialData, @Nullable Bundle initialExtras) {

    }

    @Override
    public void removeStickyBroadcast(Intent intent) {

    }

    @Override
    public void sendStickyBroadcastAsUser(Intent intent, UserHandle user) {

    }

    @Override
    public void sendStickyOrderedBroadcastAsUser(Intent intent, UserHandle user, BroadcastReceiver resultReceiver, @Nullable Handler scheduler, int initialCode, @Nullable String initialData, @Nullable Bundle initialExtras) {

    }

    @Override
    public void removeStickyBroadcastAsUser(Intent intent, UserHandle user) {

    }

    @Nullable
    @Override
    public Intent registerReceiver(@Nullable BroadcastReceiver receiver, IntentFilter filter) {
        return null;
    }

    @Nullable
    @Override
    public Intent registerReceiver(@Nullable BroadcastReceiver receiver, IntentFilter filter, int flags) {
        return null;
    }

    @Nullable
    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, @Nullable String broadcastPermission, @Nullable Handler scheduler) {
        return null;
    }

    @Nullable
    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, @Nullable String broadcastPermission, @Nullable Handler scheduler, int flags) {
        return null;
    }

    @Override
    public void unregisterReceiver(BroadcastReceiver receiver) {

    }

    @Nullable
    @Override
    public ComponentName startService(Intent service) {
        return null;
    }

    @Nullable
    @Override
    public ComponentName startForegroundService(Intent service) {
        return null;
    }

    @Override
    public boolean stopService(Intent service) {
        return false;
    }

    @Override
    public boolean bindService(@NonNull Intent service, @NonNull ServiceConnection conn, int flags) {
        return false;
    }

    @Override
    public void unbindService(@NonNull ServiceConnection conn) {

    }

    @Override
    public boolean startInstrumentation(@NonNull ComponentName className, @Nullable String profileFile, @Nullable Bundle arguments) {
        return false;
    }

    @Override
    public Object getSystemService(@NonNull String name) {
        return null;
    }

    @Nullable
    @Override
    public String getSystemServiceName(@NonNull Class<?> serviceClass) {
        return null;
    }

    @Override
    public int checkPermission(@NonNull String permission, int pid, int uid) {
        return 0;
    }

    @Override
    public int checkCallingPermission(@NonNull String permission) {
        return 0;
    }

    @Override
    public int checkCallingOrSelfPermission(@NonNull String permission) {
        return 0;
    }

    @Override
    public int checkSelfPermission(@NonNull String permission) {
        return 0;
    }

    @Override
    public void enforcePermission(@NonNull String permission, int pid, int uid, @Nullable String message) {

    }

    @Override
    public void enforceCallingPermission(@NonNull String permission, @Nullable String message) {

    }

    @Override
    public void enforceCallingOrSelfPermission(@NonNull String permission, @Nullable String message) {

    }

    @Override
    public void grantUriPermission(String toPackage, Uri uri, int modeFlags) {

    }

    @Override
    public void revokeUriPermission(Uri uri, int modeFlags) {

    }

    @Override
    public void revokeUriPermission(String toPackage, Uri uri, int modeFlags) {

    }

    @Override
    public int checkUriPermission(Uri uri, int pid, int uid, int modeFlags) {
        return 0;
    }

    @Override
    public int checkCallingUriPermission(Uri uri, int modeFlags) {
        return 0;
    }

    @Override
    public int checkCallingOrSelfUriPermission(Uri uri, int modeFlags) {
        return 0;
    }

    @Override
    public int checkUriPermission(@Nullable Uri uri, @Nullable String readPermission, @Nullable String writePermission, int pid, int uid, int modeFlags) {
        return 0;
    }

    @Override
    public void enforceUriPermission(Uri uri, int pid, int uid, int modeFlags, String message) {

    }

    @Override
    public void enforceCallingUriPermission(Uri uri, int modeFlags, String message) {

    }

    @Override
    public void enforceCallingOrSelfUriPermission(Uri uri, int modeFlags, String message) {

    }

    @Override
    public void enforceUriPermission(@Nullable Uri uri, @Nullable String readPermission, @Nullable String writePermission, int pid, int uid, int modeFlags, @Nullable String message) {

    }

    @Override
    public Context createPackageContext(String packageName, int flags) throws PackageManager.NameNotFoundException {
        return null;
    }

    @Override
    public Context createContextForSplit(String splitName) throws PackageManager.NameNotFoundException {
        return null;
    }

    @Override
    public Context createConfigurationContext(@NonNull Configuration overrideConfiguration) {
        return null;
    }

    @Override
    public Context createDisplayContext(@NonNull Display display) {
        return null;
    }

    @Override
    public Context createDeviceProtectedStorageContext() {
        return null;
    }

    @Override
    public boolean isDeviceProtectedStorage() {
        return false;
    }
}
