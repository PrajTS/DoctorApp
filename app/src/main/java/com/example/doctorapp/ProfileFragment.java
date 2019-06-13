package com.example.doctorapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.auth.api.Auth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {

    private Button logoutButton;
    private Button saveButton;
    private EditText nameET, phET, emailET, hospET, specialisationET;
    private TextView titleText;
    DatabaseReference profileRef;

    static boolean newUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, null);

        titleText = view.findViewById(R.id.titleTextBox);
        nameET = view.findViewById(R.id.inputName);
        phET = view.findViewById(R.id.inputMob);
        emailET = view.findViewById(R.id.inputEmail);
        hospET = view.findViewById(R.id.inputHospital);
        specialisationET = view.findViewById(R.id.inputSpecialisation);

        profileRef = MainActivity.database.getReference("doctors");

        logoutButton = (Button) view.findViewById(R.id.buttonLogout);
        logoutButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                AuthUI.getInstance().signOut(getContext());
            }
        });


        if(MainActivity.user != null){
            FirebaseUser u = MainActivity.user;
            String provider = u.getProviders().get(0);
            if(provider.equals("phone")){
                phET.setText(u.getPhoneNumber());
                phET.setFocusable(false);
            }
            else if(provider.equals("password")){
                nameET.setText(u.getDisplayName());
                emailET.setText(u.getEmail());
                emailET.setFocusable(false);
            }
            else if(provider.equals("google.com")){
                nameET.setText(u.getDisplayName());
                emailET.setText(u.getEmail());
                emailET.setFocusable(false);
                if(u.getPhoneNumber()!=null && !u.getPhoneNumber().isEmpty()){
                    phET.setText(u.getPhoneNumber());
                }
            }
        }
        isNewUser();

        saveButton = (Button) view.findViewById(R.id.buttonSave);
        saveButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                boolean flag = true;
                if(nameET.getText().toString().length() <= 0){
                    nameET.setError("Name can't be empty");
                    flag = false;
                }
                if(phET.getText().toString().length() <= 9){
                    phET.setError("Enter a 10 digit number");
                    flag = false;
                }
                String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
                if(emailET.getText().toString().isEmpty()){
                    emailET.setError("Email can't be empty");
                    flag = false;
                }
                else if(!emailET.getText().toString().trim().matches(emailPattern)){
                    emailET.setError("Enter a valid email");
                    flag = false;
                }
                if(hospET.getText().toString().length() <= 0){
                    hospET.setError("Hospital can't be empty");
                    flag = false;
                }
                if(specialisationET.getText().toString().length() <= 0){
                    specialisationET.setError("Specialisation can't be empty");
                    flag = false;
                }
                if(flag && !MainActivity.uid.isEmpty()){
                    profileRef.child(MainActivity.uid).setValue(new Profile(
                                                                    nameET.getText().toString(),
                                                                    Long.parseLong(phET.getText().toString()),
                                                                    emailET.getText().toString(),
                                                                    hospET.getText().toString(),
                                                                    specialisationET.getText().toString()));
                    Toast.makeText(getContext(),"Profile Updated successfully", Toast.LENGTH_SHORT).show();
                    newUser = false;
                }
                else
                    Toast.makeText(getContext(),"Profile Updated failed.", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void isNewUser() {
        MainActivity.loggedin = false;
        newUser = true;
        if(profileRef!= null && MainActivity.uid != null) {
            profileRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists() && dataSnapshot.hasChild(MainActivity.uid)) {
                        newUser = false;
                        MainActivity.loggedin = false;
                        populateForm();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }


    }

    private void populateForm() {

        profileRef.child(MainActivity.uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Profile prof = dataSnapshot.getValue(Profile.class);
                nameET.setText(prof.getName());
                phET.setText(""+prof.getPh());
                emailET.setText(prof.getEmail());
                hospET.setText(prof.getHospital());
                specialisationET.setText(prof.getSpecialisation());
                titleText.setText(prof.getName());
                if(!prof.getName().isEmpty() && !(""+prof.getPh()).isEmpty() && !(""+prof.getEmail()).isEmpty() && !(""+prof.getHospital()).isEmpty()&& !(""+prof.getSpecialisation()).isEmpty()){
                    MainActivity.loggedin = true;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


}
