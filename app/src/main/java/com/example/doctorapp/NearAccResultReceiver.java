package com.example.doctorapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.util.Log;

public class NearAccResultReceiver extends android.os.ResultReceiver {
    public NearAccResultReceiver(Handler handler) {
        super(handler);
    }

    @Override
    public void send(int resultCode, Bundle resultData) {
        super.send(resultCode, resultData);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        Log.e("Rec","in");
        if(resultCode == 1)
            AccidentFragment.repop();
        super.onReceiveResult(resultCode,resultData);
    }

    @Override
    public int describeContents() {
        return super.describeContents();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
    }
}
