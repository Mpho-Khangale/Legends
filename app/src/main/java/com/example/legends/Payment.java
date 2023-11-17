package com.example.legends;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Payment {
    private String barberName;
    private String haircut;
    private String amount;
    private String date;
    private String time; // Add this field for storing the selected time

    public Payment() {
        // Default constructor required for Firebase Realtime Database
    }

    public Payment(String barberName, String haircut, String amount, String date, String time) {
        this.barberName = barberName;
        this.haircut = haircut;
        this.amount = amount;
        this.date = date;
        this.time = time;
    }

    public String getBarberName() {
        return barberName;
    }

    public void setBarberName(String barberName) {
        this.barberName = barberName;
    }

    public String getHaircut() {
        return haircut;
    }

    public void setHaircut(String haircut) {
        this.haircut = haircut;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
