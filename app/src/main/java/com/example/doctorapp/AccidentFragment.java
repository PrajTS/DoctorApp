package com.example.doctorapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;


public class AccidentFragment extends Fragment implements OnMapReadyCallback {

    private static Context context;
    static String id;

    static ArrayList<Marker> m = new ArrayList<>();
    public static GoogleMap gMap;

    static Button but;
    static TextView txt;

    public AccidentFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_accident, container, false);

        but = view.findViewById(R.id.button_respond);
        txt = view.findViewById(R.id.textview_info);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.frg);
        mapFragment.getMapAsync(this);

//        setUpLocation();

        noAcc();

//        but.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View v) {
//                Log.e("btn","clicked --- "+id+" --- "+txt.getText().toString().toUpperCase());
//                if (id != null && !id.isEmpty()) {
//                    if (but.getText().toString().toUpperCase().equals("RESPOND")) {
//                        but.setText("REACHED");
//                        MainActivity.myRef.child(""+id).child("report").addListenerForSingleValueEvent(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                final long num = dataSnapshot.getChildrenCount();
//                                Date date = new Date();
//                                String [] months = {"January","February","March","April","May","June","July","August","September","October","November","December"};
//                                SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
//                                SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");
//                                final String today = dateFormatter.format(date);
//                                final String now = timeFormatter.format(date);
//                                /*String today = date.getDate()+" "+months[date.getMonth()]+", "+ date.ge();
//                                String now = date.getHours()+" : "+date.getMinutes()+" : "+date.getSeconds();*/
//                                final String remarks = "Started from a distance of "+dis;
//                                final String value = "Medic Enroute";
//                                MainActivity.myRef.child(""+id).child("reportStatus").addListenerForSingleValueEvent(new ValueEventListener() {
//                                    @Override
//                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                        String prevReportStatus = dataSnapshot.getValue().toString();
//                                        if(prevReportStatus.contains("Accident Detected")) {
//                                            MainActivity.myRef.child("" + id).child("reportStatus").setValue("Medic Enroute");
//                                            MainActivity.myRef.child(""+id).child("report").child((num+1)+"").setValue(new Report(today,remarks,now,value));
//                                        }
//                                        else if(prevReportStatus.contains("Ambulance Dispatched")) {
//                                            MainActivity.myRef.child("" + id).child("reportStatus").setValue(prevReportStatus + "," +"Medic Enroute");
//                                            MainActivity.myRef.child(""+id).child("report").child((num+1)+"").setValue(new Report(today,remarks,now,value));
//                                        }
//                                    }
//
//                                    @Override
//                                    public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                                    }
//                                });
//
//
//                            }
//
//                            @Override
//                            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                            }
//                        });
//                    }
//                    else if (but.getText().toString().toUpperCase().equals("REACHED")) {
//                        but.setText("No Hospitalisation");
//                        txt.setText("Check the patient");
//                        MainActivity.myRef.child(""+id).child("report").addListenerForSingleValueEvent(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                final long num = dataSnapshot.getChildrenCount();
//                                Date date = new Date();
//                                String [] months = {"January","February","March","April","May","June","July","August","September","October","November","December"};
//                                SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
//                                SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");
//                                final String today = dateFormatter.format(date);
//                                final String now = timeFormatter.format(date);
//                                /*String today = date.getDate()+" "+months[date.getMonth()]+", "+ date.ge();
//                                String now = date.getHours()+" : "+date.getMinutes()+" : "+date.getSeconds();*/
//                                final String remarks = "Checking patient. "+dis;
//                                final String value = "Medic Reached";
//                                MainActivity.myRef.child(""+id).child("reportStatus").addListenerForSingleValueEvent(new ValueEventListener() {
//                                    @Override
//                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                        String prevReportStatus = dataSnapshot.getValue().toString();
//                                        if(prevReportStatus.contains("Medic Enroute") || prevReportStatus.contains("Accident Detected")) {
//                                            MainActivity.myRef.child("" + id).child("reportStatus").setValue("Medic Reached");
//                                            MainActivity.myRef.child(""+id).child("report").child((num+1)+"").setValue(new Report(today,remarks,now,value));
//                                        }
//                                        else if(prevReportStatus.contains("Ambulance Dispatched")) {
//                                            MainActivity.myRef.child("" + id).child("reportStatus").setValue(prevReportStatus + "," +"Medic Reached");
//                                            MainActivity.myRef.child(""+id).child("report").child((num+1)+"").setValue(new Report(today,remarks,now,value));
//                                        }
//                                    }
//                                    @Override
//                                    public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                                    }
//                                });
//                            }
//
//                            @Override
//                            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                            }
//                        });
//                    }
//                    else if (but.getText().toString().toUpperCase().equals("NO HOSPITALISATION")) {
//                        but.setVisibility(View.INVISIBLE);
//                        txt.setText("");
//                        MainActivity.myRef.child(""+id).child("report").addListenerForSingleValueEvent(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                final long num = dataSnapshot.getChildrenCount();
//                                Date date = new Date();
//                                String [] months = {"January","February","March","April","May","June","July","August","September","October","November","December"};
//                                SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
//                                SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");
//                                final String today = dateFormatter.format(date);
//                                final String now = timeFormatter.format(date);
//                                /*String today = date.getDate()+" "+months[date.getMonth()]+", "+ date.ge();
//                                String now = date.getHours()+" : "+date.getMinutes()+" : "+date.getSeconds();*/
//                                final String remarks = "No hospitalisation Necessary - Medic "+dis;
//                                final String value = "No Hospitalisation";
//                                MainActivity.myRef.child(""+id).child("reportStatus").addListenerForSingleValueEvent(new ValueEventListener() {
//                                    @Override
//                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                        String prevReportStatus = dataSnapshot.getValue().toString();
//                                        MainActivity.myRef.child("" + id).child("reportStatus").setValue("No Hospitalisation");
//                                        MainActivity.myRef.child(""+id).child("report").child((num+1)+"").setValue(new Report(today,remarks,now,value));
//                                        MainActivity.myRef.child(""+id).child("status").setValue(false);
//
//                                    }
//                                    @Override
//                                    public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                                    }
//                                });
//
//
//                            }
//
//                            @Override
//                            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                            }
//                        });
//                    }
//                }
//            }
//        });

        but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.getAppContext(), respond.class);
                intent.putExtra("id",id);
                Log.e("intent","id: "+id);
                startActivity(intent);
            }
        });

        return view;

    }

    static void noAcc(){
        but.setVisibility(View.INVISIBLE);
        if(MainActivity.nearAcc.size() == 0) {
            txt.setText("No Accidents in the Locality :)");
        }
        else
            txt.setText(MainActivity.nearAcc.size() + " accident(s) in the Locality.");
    }

//    private void setUpLocation() {
//        if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
//            ActivityCompat.requestPermissions(getContext(),new String[]{
//                    Manifest.permission.ACCESS_FINE_LOCATION,
//                    Manifest.permission.ACCESS_COARSE_LOCATION}, MainActivity.MY_PERMISSION_REQUEST_CODE);
//        }
//    }


    @Override
    public void onMapReady(GoogleMap mMap) {
        gMap = mMap;
        context = getContext();
        repop();
    }
    static String dis;
    static void repop(){
        try{
        noAcc();}
        catch(Exception e){}
        if(gMap != null) {
            gMap.clear();
            m.clear();

            gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

            gMap.clear(); //clear old markers

            double lat = 0, lng = 0;

            markMyLocation();
            if(!MainActivity.nearAcc.isEmpty()){
                CircleOptions circleOptions1 = new CircleOptions()
                        .center(new LatLng(MainActivity.lastLat, MainActivity.lastLon))
                        .radius(1000).strokeColor(Color.BLACK)
                        .strokeWidth(2).fillColor(0x500000ff);
                gMap.addCircle(circleOptions1);
            }

            for (Map.Entry<String, com.example.doctorapp.LatLng> entry : MainActivity.nearAcc.entrySet()) {
                String key = entry.getKey();
                com.example.doctorapp.LatLng info = entry.getValue();
                lat = Double.parseDouble(info.getLat());
                lng = Double.parseDouble(info.getLon());
                String time = "@ " + info.getHour() + " : " + info.getMin() + " : " + info.getSec() + " on " + info.getDay() + "/" + info.getMonth() + "/" + info.getYear();
                Marker marker = gMap.addMarker(new MarkerOptions()
                        .position(new LatLng(lat, lng))
                        .title(time));
                marker.setTag(key);
                //marker.showInfoWindow();
//                m.add(marker);
            }

            CameraPosition googlePlex = CameraPosition.builder()
                    .target(new LatLng(MainActivity.lastLat, MainActivity.lastLon))
                    .zoom(15)
                    .bearing(0)
                    .tilt(45)
                    .build();

            gMap.animateCamera(CameraUpdateFactory.newCameraPosition(googlePlex), 100, null);
            gMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    RequestQueue mQueue;
                    mQueue = Volley.newRequestQueue(MainActivity.getAppContext());

                    if (marker.getTag() != null) {
                        id = (String) marker.getTag();
                        Log.e("tag", id);
                        if (marker.isInfoWindowShown()) {
                            marker.hideInfoWindow();
                        } else if (!marker.isInfoWindowShown())
                            marker.showInfoWindow();

                        if (id.equals("Me"))
                            noAcc();
                        else{
                            but.setVisibility(View.VISIBLE);
                            String url = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=" + MainActivity.lastLat + "," + MainActivity.lastLon + "&destinations=" + MainActivity.nearAcc.get(id).getLat() + "%2C" + MainActivity.nearAcc.get(id).getLon() + "&key=AIzaSyBN7G14eppA4YqlSr85ZCO1a7tawk9A7fo";

                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                                    @Override
                                    public void onResponse(JSONObject response) {
                                        try {
                                            JSONArray jsonArray = response.getJSONArray("rows").getJSONObject(0).getJSONArray("elements");
                                            for (int i = 0; i < jsonArray.length(); i++) {
                                                dis = jsonArray.getJSONObject(i).getJSONObject("distance").get("text").toString();

                                                txt.setText(dis + " away.");
                                            }

                                        } catch (Exception e) {
                                        }
                                    }
                                }, new Response.ErrorListener() {

                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        // TODO: Handle error

                                    }
                                });
                        mQueue.add(jsonObjectRequest);
                        return true;
                    }
                }
                    return false;
                }
            });
        }
    }

    private static void markMyLocation() {
        if(MainActivity.lastLat != 0 && MainActivity.lastLon != 0)
        {
            double lat = MainActivity.lastLat;
            double lng = MainActivity.lastLon;
            Log.e("geo","Current lat : "+lat+"  Lng : "+ lng);
            Marker myLocation = gMap.addMarker(new MarkerOptions()
                    .position(new LatLng(lat, lng))
                    .title("Me")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            myLocation.setTag("Me");
            myLocation.showInfoWindow();
            Log.e("geo","All acc :" + MainActivity.allAcc.values().toString());
            Log.e("geo","Near acc :" + MainActivity.nearAcc.values().toString());
            noAcc();
        }
    }
}
