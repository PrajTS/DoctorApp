package com.example.doctorapp;

public class Report {
    String date;
    String remarks;
    String time, value;

    public Report(){}

    public Report(String date, String remarks, String time, String value){
        this.date = date;
        this.remarks = remarks;
        this.time = time;
        this.value = value;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getValue() {
        return value;
    }

    public String getRemarks() {
        return remarks;
    }

    public String getTime() {
        return time;
    }

    public String getDate() {
        return date;
    }

}
