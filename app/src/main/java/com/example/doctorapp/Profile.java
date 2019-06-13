package com.example.doctorapp;

public class Profile {
    String name, email, hospital, specialisation;
    long ph;

    public Profile(){}

    public Profile(String name, long ph, String email,String hospital, String specialisation){
        this.name = name;
        this.email = email;
        this.ph = ph;
        this.hospital = hospital;
        this.specialisation = specialisation;
    }

    public long getPh() {
        return ph;
    }

    public String getEmail() {
        return email;
    }

    public String getHospital() {
        return hospital;
    }

    public String getName() {
        return name;
    }

    public String getSpecialisation() {
        return specialisation;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setHospital(String hospital) {
        this.hospital = hospital;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPh(long ph) {
        this.ph = ph;
    }

    public void setSpecialisation(String specialisation) {
        this.specialisation = specialisation;
    }
}