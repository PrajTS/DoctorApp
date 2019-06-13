package com.example.doctorapp;

public interface ResultReceiverCallBack<T>{
    public void onSuccess(T data);
    public void onError(Exception exception);
}
