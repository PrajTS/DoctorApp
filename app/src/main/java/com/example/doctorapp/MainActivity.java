package com.example.doctorapp;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

import static com.example.doctorapp.GeofenceTransitionsIntentService.CHANNEL_ID;


public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener{

    public static final int RC_SIGN_IN = 1;
    public static final int MY_PERMISSION_REQUEST_CODE = 1234;
    public static final int PLAY_SERVICES_RESOLUTION_REQUEST = 300193;

    public static double lastLat;
    public static double lastLon;

    private static GeofencingClient geofencingClient;
    static ArrayList<Geofence> geofenceList;
    private  PendingIntent geofencePendingIntent;

    static LinkedHashMap<String, LatLng> allAcc;
    static LinkedHashMap<String, LatLng> nearAcc;
    static LinkedHashMap<String, Geofence> geoFenceMap;

    static FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    static FirebaseDatabase database;
    static DatabaseReference myRef;

    private LocationRequest mLocationRequest;
    public static GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private static int UPDATE_INTERVAL = 5000;
    private static int FASTEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    public static String uid;
    public static FirebaseUser user;

    BottomNavigationView navView;

    public static boolean loggedin = false;

    static NearAccResultReceiver receiver;
    private static Context context;

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        Fragment fragment = null;

        switch (item.getItemId()) {
            case R.id.navigation_accident:
                if(loggedin)
                    fragment = new AccidentFragment();
                else {
                    fragment = new ProfileFragment();
                    Toast.makeText(this, "Complete Profile", Toast.LENGTH_SHORT).show();
                    navView.setSelectedItemId(R.id.navigation_profile);
                }
                break;
            case R.id.navigation_profile:
                fragment = new ProfileFragment();

        }
        return loadFragment(fragment);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.context = getApplicationContext();
        receiver = new NearAccResultReceiver(new Handler());

        mAuth = FirebaseAuth.getInstance();

        geofenceList = new ArrayList<>();
        createNotificationChannel();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    onSignedInInitialize(user.getDisplayName());
                    uid = user.getUid();
                    if(loggedin)
                        loadFragment(new AccidentFragment());
                    else
                        loadFragment(new ProfileFragment());
                } else {
                    onSignedOutCleanup();
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.GoogleBuilder().build(),
                                            new AuthUI.IdpConfig.EmailBuilder().build(),
                                            new AuthUI.IdpConfig.PhoneBuilder().build()))
                                    .setLogo(R.drawable.icon)
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };
        geofencePendingIntent = null;

        setContentView(R.layout.activity_main);

        navView = findViewById(R.id.nav_view);
        navView.setSelectedItemId(R.id.navigation_profile);
        navView.setOnNavigationItemSelectedListener(this);

        allAcc = new LinkedHashMap<>();
        nearAcc = new LinkedHashMap<>();

        geoFenceMap = new LinkedHashMap<>();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("/adetected");

        setUpLocation();

        ChildEventListener mNewAccidentListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.e("Accident Location KeyA", dataSnapshot.getKey());
                findAcc(dataSnapshot);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.e("Accident Location KeyC", dataSnapshot.getKey());
                findAcc(dataSnapshot);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

            public void findAcc(final DataSnapshot dataSnapshot) {
                String key = dataSnapshot.getKey();
                Log.e("geo","Key: " +key);
                database.getReference("/Users/"+key).child("accident").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnap) {
                        try {
                            Log.e("geo","accident stat : "+dataSnap.getValue());
                            if((Boolean)dataSnap.getValue()){
                                LatLng newAccident = dataSnapshot.getValue(LatLng.class);
                                Geofence geofenceIndv = new Geofence.Builder()
                                        .setRequestId(dataSnapshot.getKey())
                                        .setExpirationDuration(3 * 60 * 60 * 1000)
                                        .setCircularRegion(
                                                Double.parseDouble(newAccident.getLat()),
                                                Double.parseDouble(newAccident.getLon()),
                                                1000
                                        )
                                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                                                Geofence.GEOFENCE_TRANSITION_EXIT)
                                        .build();
                                Log.e("geo","Lat : " + newAccident.getLat()+ " Lng : "+ newAccident.getLon());
                                Log.e("geo","report status : " + dataSnapshot.child("reportStatus").getValue().toString());
                                if(dataSnapshot.child("reportStatus").getValue().toString().contains("Accident Detected")||
                                        ((dataSnapshot.child("reportStatus").getValue().toString().contains("Medic Enroute") ||
                                                dataSnapshot.child("reportStatus").getValue().toString().contains("Medic Reached"))&&
                                                dataSnapshot.child("medic").getValue().toString().equals(uid))){
                                    allAcc.put(dataSnapshot.getKey(), newAccident);
                                    geoFenceMap.put(dataSnapshot.getKey(),geofenceIndv);
                                }
                                else {
                                    geoFenceMap.remove(dataSnapshot.getKey());
                                    nearAcc.remove(dataSnapshot.getKey());
                                }
                            }
                            else {
                                allAcc.remove(dataSnapshot.getKey());
                                geoFenceMap.remove(dataSnapshot.getKey());
                                nearAcc.remove(dataSnapshot.getKey());
                            }
                            geofenceList.clear();
                            geofenceList = new ArrayList<>(geoFenceMap.values());
                            Log.e("GeofenceMap", geoFenceMap.toString());
                            Log.e("GeofenceList", geofenceList.toString());

                        } catch (Exception e) {
                            Log.e("dshf",e.toString());
                        }
                        addGeofences();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }
        };
        myRef.addChildEventListener(mNewAccidentListener);
        geofencingClient = LocationServices.getGeofencingClient(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.e("requestCode", requestCode + "");
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    buildGoogleApiClient();
                    createLocationRequest();
                    displayLocation();
                }
                break;
        }
    }

    private void setUpLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSION_REQUEST_CODE);
        } else {
            if (checkPlayServices()) {
                buildGoogleApiClient();
                createLocationRequest();
                displayLocation();
            }
        }
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mGoogleApiClient.connect();

        if (mGoogleApiClient.isConnected()) {
            Log.e("in", "built");
        }
        else
            Log.e("in", "not");

    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    public void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();

            lastLat = latitude;
            lastLon = longitude;

            Log.e("EDMTDEV", String.format("Your location was changed : %f %f", latitude, longitude));

        } else {

            Log.e("EDMTDEV", "Cannot get location");
        }
    }

    void addGeofences() {
        if(mGoogleApiClient != null) {
            if (!mGoogleApiClient.isConnected()) {
//                Toast.makeText(this, getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
                return;
            }
            if(geofenceList != null) {
                Log.e("addG", "Attemping to remove geofences ");
                geofencingClient.removeGeofences(getGeofencePendingIntent())
                        .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.e("addG", "Geofences removed");
                                createGeofences();
                            }
                        })
                        .addOnFailureListener(this, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("addG", "Geofences remove failed.");
                                createGeofences();
                            }
                        });
            }
        }
    }

    private void createGeofences(){
        Log.e("addG","Attemping to create geofences ");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        try{
            geofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                    .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.e("addG", "Geofences added\t" + geofenceList.toString());
                            AccidentFragment.repop();
                        }
                    })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("addG", "Geofences not added");
                            AccidentFragment.repop();
                        }
                    });
        }
        catch(Exception e){
            Log.e("addG/E",e.toString());
            AccidentFragment.repop();
        }
        return;
    }

    private PendingIntent getGeofencePendingIntent() {

        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        Log.e("receiver",receiver.toString());
        intent.putExtra("nearAcc",receiver);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling addgeoFences()
        geofencePendingIntent = PendingIntent.getService(this, 123, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Log.e("intent","inside");
        return geofencePendingIntent;
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofenceList);
        return builder.build();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN){
            if(resultCode == RESULT_CANCELED){
                finish();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null)
            mAuth.removeAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    public void onSignedInInitialize(String username){

    }

    private void onSignedOutCleanup(){

    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if(resultCode != ConnectionResult.SUCCESS){
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode,this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            else
            {
                Toast.makeText(this,"This device is not supported", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
        if(mGoogleApiClient.isConnected()) {
            Log.e("in1", "built");
//            addGeofences();
        }
        else
            Log.e("in1","not");
    }


    private void startLocationUpdates() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displayLocation();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Accident Detect";
            String description = "Notify when accident is detected";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static Context getAppContext() {
        return MainActivity.context;
    }

}
