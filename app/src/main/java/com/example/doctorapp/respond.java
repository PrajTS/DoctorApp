package com.example.doctorapp;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.example.doctorapp.AccidentFragment.dis;

public class respond extends AppCompatActivity {

    Button respond, location, update;
    String id;
    LatLng latlng;
    TextView curr_stat, label_cond;
    EditText condition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_respond);

        curr_stat = (TextView)findViewById(R.id.textView_curr_stat);
        condition = (EditText) findViewById(R.id.editText_condition);
        label_cond = (TextView)findViewById(R.id.textView_label_condition);

        Intent intent = getIntent();
        id = intent.getStringExtra("id");

        location = (Button)findViewById(R.id.button_location);
        respond = (Button)findViewById(R.id.button_respond);
        update = findViewById(R.id.button_update);
        condition.setVisibility(View.INVISIBLE);
        label_cond.setVisibility(View.INVISIBLE);

        MainActivity.myRef.child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                latlng = dataSnapshot.getValue(LatLng.class);
                curr_stat.setText(latlng.getReportStatus().replace("Arrived","Ambulance Arrived"));
                setup(latlng.getReportStatus());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri gmmIntentUri = Uri.parse("https://www.google.com/maps/dir/?api=1&origin="+MainActivity.lastLat+","+MainActivity.lastLon+"&destination="+latlng.getLat()+","+latlng.getLon());
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });

        respond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String status = respond.getText().toString();

                if(status.equals("RESPOND")) {
                    MainActivity.myRef.child(id).child("medic").setValue(MainActivity.uid);
                    if (latlng.getReportStatus().equals("Accident Detected"))
                        MainActivity.myRef.child(id).child("reportStatus").setValue("Medic Enroute");
                    else if (latlng.getReportStatus().contains("Ambulance Dispatched"))
                        MainActivity.myRef.child(id).child("reportStatus").setValue("Ambulance Dispatched,Medic Enroute");

                    MainActivity.myRef.child("" + id).child("report").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            final long num = dataSnapshot.getChildrenCount();
                            Date date = new Date();
                            SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
                            SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");
                            final String today = dateFormatter.format(date);
                            final String now = timeFormatter.format(date);
                            final String remarks = "Started from a distance of " + dis;
                            final String value = "Medic Enroute";
                            MainActivity.myRef.child("" + id).child("report").child((num + 1) + "").setValue(new Report(today, remarks, now, value));
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {}
                    });
                }
                else if(status.equals("REACHED SITE")){
                    String stat = latlng.getReportStatus().replace("Medic Enroute", "Medic Reached");
                    MainActivity.myRef.child(id).child("reportStatus").setValue(stat);
                    MainActivity.myRef.child("" + id).child("report").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            final long num = dataSnapshot.getChildrenCount();
                            Date date = new Date();
                            SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
                            SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");
                            final String today = dateFormatter.format(date);
                            final String now = timeFormatter.format(date);
                            final String remarks = "Examining patient.";
                            final String value = "Medic Reached";
                            MainActivity.myRef.child("" + id).child("report").child((num + 1) + "").setValue(new Report(today, remarks, now, value));
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {}
                    });
                }
                else if(status.equals("NO HOSPITALISATION")){
                    MainActivity.myRef.child(id).child("reportStatus").setValue("No Hospitalisation");
                    MainActivity.database.getReference("Users").child(id).child("accident").setValue(false);
                    MainActivity.myRef.child("" + id).child("report").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            final long num = dataSnapshot.getChildrenCount();
                            Date date = new Date();
                            SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
                            SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");
                            final String today = dateFormatter.format(date);
                            final String now = timeFormatter.format(date);
                            final String remarks = "No hospitalisation Necessary - Medic ";
                            final String value = "No Hospitalisation";
                            MainActivity.myRef.child("" + id).child("report").child((num + 1) + "").setValue(new Report(today, remarks, now, value));
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {}
                    });
                }

            }
        });
    }

    void setup(String reportStatus)
    {
        if(reportStatus.equals("Accident Detected")){
            condition.setVisibility(View.INVISIBLE);
            label_cond.setVisibility(View.INVISIBLE);
            update.setVisibility(View.INVISIBLE);
            respond.setText("RESPOND");
        }
        else if(reportStatus.contains("Medic Enroute")){
            condition.setVisibility(View.INVISIBLE);
            label_cond.setVisibility(View.INVISIBLE);
            update.setVisibility(View.INVISIBLE);
            respond.setText("REACHED SITE");
        }
        else if(reportStatus.contains("Medic Reached")){
            condition.setVisibility(View.VISIBLE);
            label_cond.setVisibility(View.VISIBLE);
            update.setVisibility(View.VISIBLE);
            MainActivity.myRef.child(id).child("report").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                        if(snapshot.child("value").getValue().toString().equals("Medic Reached")){
                            final String key = snapshot.getKey();
                            Log.e("val","keysa : "+key);
                            String text = snapshot.child("remarks").getValue().toString();
                            if(!text.equals("Examining patient.")){
                                condition.setText(text);
                            }
                            update.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(!condition.getText().toString().isEmpty())
                                        MainActivity.myRef.child(id).child("report").child(key).child("remarks").setValue(condition.getText().toString());
                                }
                            });
                            break;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            respond.setText("NO HOSPITALISATION");
        }
        else if(reportStatus.contains("Ambulance Dispatched")){
            condition.setVisibility(View.INVISIBLE);
            label_cond.setVisibility(View.INVISIBLE);
            update.setVisibility(View.INVISIBLE);
            respond.setText("RESPOND");
        }
        else{
            condition.setVisibility(View.INVISIBLE);
            label_cond.setVisibility(View.INVISIBLE);
            update.setVisibility(View.INVISIBLE);
            respond.setText("Thank you");
            respond.setEnabled(false);
        }
    }



}
