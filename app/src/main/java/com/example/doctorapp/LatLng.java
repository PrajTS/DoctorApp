package com.example.doctorapp;

public class LatLng {
    int day,month,year,hour,min,sec;
    String lat,lon, reportStatus;
    boolean falseAlarm;
    String medic;

    public LatLng(){}

    public LatLng (String lat,String lon,int day,int month,int year,int hour,int min,int sec, boolean falseAlarm, String reportStatus)
    {
        this.day=day;
        this.month=month;
        this.year=year;
        this.hour=hour;
        this.min=min;
        this.sec=sec;
        this.lat=lat;
        this.lon=lon;
        this.falseAlarm = falseAlarm;
        this.reportStatus = reportStatus;
        this.medic = "";
    }

    public String getMedic() {
        return medic;
    }

    public void setMedic(String medic) {
        this.medic = medic;
    }

    public void setDay(int day)
    {
        this.day=day;
    }

    public void setMonth(int month)
    {
        this.month=month;
    }
    public void setYear(int year)
    {
        this.year = year;
    }

    public void setHour(int hour)
    {
        this.hour = hour;
    }

    public void setMin (int min)
    {
        this.min = min;
    }

    public void setSec (int sec)
    {
        this.sec = sec;
    }

    public void setLat(String lat)
    {
        this.lat = lat;
    }

    public void setLon(String lon)
    {
        this.lon = lon;
    }

    public void setFalseAlarm(boolean falseAlarm) {
        this.falseAlarm = falseAlarm;
    }


    public boolean getFalseAlarm()
    {
        return falseAlarm;
    }
    public int getDay()
    {
        return day;
    }
    public int getMonth(){return month;}

    public int getYear()
    {
        return year;
    }

    public int getHour()
    {
        return hour;
    }
    public int getMin(){
        return min;
    }

    public int getSec(){
        return sec;
    }

    public String getLat(){return lat;}

    public String getLon(){return lon;}

    public void setReportStatus(String responseStatus) {
        this.reportStatus = responseStatus;
    }

    public String getReportStatus() {
        return reportStatus;
    }
}
