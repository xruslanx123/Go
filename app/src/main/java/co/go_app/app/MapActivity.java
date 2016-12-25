package co.go_app.app;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Inspiration from: http://stackoverflow.com/a/34582595
 */
public class MapActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMapLongClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        GoogleMap.OnMapClickListener {

    public static final int NEW_CHALLENGE_REQUEST_CODE = 101;
    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Marker mMarker;
    private Marker mPlusMarker;
    private Marker newChallengePointer;
    private DatabaseReference mDatabase;
    private SlidingUpPanelLayout slidingUpPanelLayout;
    private ImageView toolbarArrowImg, toolbarLogoImg;
    private HashMap<Marker, Challenge> mMarkers;
    private Activity thisActivity;
    private boolean newChallengeMode = false;
    private Button changeLocationForNewChallengeBtn;
    private boolean firstLocation, userPhotoAvalible;
    private TabHost tabHost;
    private TabWidget tabWidget;
    private ArrayList<ImageView> widgetButtons;
    private Challenge savedData, selectedChallenge;
    private Marker selectedMarker;
    private LinearLayout infoWindowButtons;
    private FirebaseUser currentUser;
    private Bitmap userPhoto;
    private ImageView acctPhotoHolder;
    private ListView myChallengesListView;
    private ArrayList<Challenge> myChallenges;


    private static final String TAG = "MapActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // check for user authentication and get info;
        // if no authentication is available close this activity and return to LoginActivity.
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser == null){
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
        // set account info for acct_settings
        acctPhotoHolder = (ImageView) findViewById(R.id.acct_photo);
        TextView acctName = (TextView)findViewById(R.id.acct_name);
        acctName.setText(currentUser.getDisplayName());
        checkPhoto(true);

        firstLocation = true; // boolean to center map on location on startUp;

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mMarkers = new HashMap<>();
        thisActivity = this;
        toolbarArrowImg = (ImageView) findViewById(R.id.toolbar_arrow_img);
        toolbarLogoImg = (ImageView) findViewById(R.id.toolbar_logo_img);
        slidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        slidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                toolbarArrowImg.setAlpha(1-slideOffset);
                toolbarLogoImg.setAlpha(slideOffset);
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if(newState == SlidingUpPanelLayout.PanelState.COLLAPSED){
                    widgetButtons.get(2).callOnClick();
                }
            }
        });
        changeLocationForNewChallengeBtn = (Button) findViewById(R.id.set_location_map_button);
        changeLocationForNewChallengeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLocationToNewChallenge();
            }
        });

        tabHost = (TabHost) findViewById(R.id.tabHost);
        tabWidget = (TabWidget) findViewById(android.R.id.tabs);

        infoWindowButtons = (LinearLayout) findViewById(R.id.info_window_buttons);
        findViewById(R.id.pop_up_go).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: add challenge
                selectedMarker.hideInfoWindow();
                selectedChallenge = null;
                selectedMarker = null;
                infoWindowButtons.setVisibility(View.GONE);
            }
        });
        findViewById(R.id.pop_up_reject).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedMarker.hideInfoWindow();
                selectedChallenge = null;
                selectedMarker = null;
                infoWindowButtons.setVisibility(View.GONE);
            }
        });

        tabHost.setup();
        //TODO: improve image and tab buttons placement code
        widgetButtons = new ArrayList<>();
        ImageView img = new ImageView(this);
        img.setTag(0);
        img.setImageResource(R.drawable.idle_temp_white);
        widgetButtons.add(img);
        img = new ImageView(this);
        img.setTag(1);
        img.setImageResource(R.drawable.idle_temp_white);
        widgetButtons.add(img);
        img = new ImageView(this);
        img.setTag(2);
        img.setImageResource(R.drawable.pressed_temp);
        widgetButtons.add(img);
        img = new ImageView(this);
        img.setTag(3);
        img.setImageResource(R.drawable.idle_temp_white);
        widgetButtons.add(img);
        img = new ImageView(this);
        img.setTag(4);
        img.setImageResource(R.drawable.idle_temp_white);
        widgetButtons.add(img);

        tabHost.addTab(tabHost.newTabSpec("ACCT_SETTINGS")
                .setIndicator(widgetButtons.get(0))
                .setContent(R.id.acct_settings));
        tabHost.addTab(tabHost.newTabSpec("NEARBY")
                .setIndicator(widgetButtons.get(1))
                .setContent(R.id.nearby_list));
        tabHost.addTab(tabHost.newTabSpec("MY")
                .setIndicator(widgetButtons.get(2))
                .setContent(R.id.my_list));
        tabHost.addTab(tabHost.newTabSpec("NEW_CHALLENGE")
                .setIndicator(widgetButtons.get(3))
                .setContent(R.id.new_challenge));
        tabHost.addTab(tabHost.newTabSpec("SETTINGS")
                .setIndicator(widgetButtons.get(4))
                .setContent(R.id.settings));
        tabHost.setCurrentTab(2);
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(ImageView img: widgetButtons){
                    img.setImageResource(R.drawable.idle_temp_white);
                }
                tabHost.setCurrentTab((int)view.getTag());
                ((ImageView)view).setImageResource(R.drawable.pressed_temp);
            }
        };
        tabWidget.getChildAt(0).setOnClickListener(clickListener);
        tabWidget.getChildAt(1).setOnClickListener(clickListener);
        tabWidget.getChildAt(2).setOnClickListener(clickListener);
        tabWidget.getChildAt(3).setOnClickListener(clickListener);
        tabWidget.getChildAt(4).setOnClickListener(clickListener);

        setMyChallengesTab();
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

        fetchChallenges(false);
        mMap.setOnMapClickListener(this);
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                newChallengePointer.hideInfoWindow();
            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                newChallengePointer.showInfoWindow();
            }
        });
        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerClickListener(this);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY); // TODO: check for in challenge priority
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
        if(firstLocation){
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 17));
            firstLocation = false;
        }
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MapActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void addChallenge(Challenge challenge) {
        DatabaseReference ref = mDatabase.child("challenges").push();
        challenge.setKey(ref.getKey());
        ref.setValue(challenge);
    }

    private void fetchChallenges(final boolean deleteAll) {
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                if (deleteAll) {
                    dataSnapshot.getRef().removeValue();
                } else {
                    // A new challenge has been added, add it to the displayed list.
                    Challenge challenge = dataSnapshot.getValue(Challenge.class);
                    // Add it to the map.
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(challenge.retrieveLatLng());
                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.icon)));
                    mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                        @Override
                        public View getInfoWindow(final Marker marker) {
                            if(marker.equals(mPlusMarker)){
                                createNewChallenge(marker.getPosition().latitude, marker.getPosition().longitude);
                                mPlusMarker.remove();
                                mPlusMarker = null;
                                return null;
                            }else if(marker.equals(newChallengePointer)){
                                return null;
                            }
                            selectedChallenge = mMarkers.get(marker);
                            View view = thisActivity.getLayoutInflater().inflate(R.layout.challenge_map_item, null);
                            ((TextView)view.findViewById(R.id.challenge_title)).setText(selectedChallenge.getTitle());
                            ((TextView)view.findViewById(R.id.challenge_description)).setText(selectedChallenge.getDescription());
                            ((TextView)view.findViewById(R.id.challenge_creator)).setText(selectedChallenge.getCreator());
                            ((TextView)view.findViewById(R.id.challenge_points_reward)).setText(String.valueOf(selectedChallenge.getReward()));
                            view.findViewById(R.id.challenge_distance).setVisibility(View.GONE);
                            selectedMarker = marker;
                            return view;
                        }

                        @Override
                        public View getInfoContents(Marker marker) {
                            return null;
                        }
                    });
                    mMarker = mMap.addMarker(markerOptions);
                    mMarkers.put(mMarker, challenge);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "TODO: onChildChanged()");
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "TODO: onChildRemoved()");
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "TODO: onChildMoved()");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "TODO: onCancelled()");
            }
        };

        Query challenges = mDatabase.child("challenges");
        challenges.addChildEventListener(childEventListener);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if(marker.equals(mPlusMarker)){
            createNewChallenge(marker.getPosition().latitude, marker.getPosition().longitude);
            mPlusMarker.remove();
            mPlusMarker = null;
        }
        return false;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if(!newChallengeMode && slidingUpPanelLayout.getPanelState() != SlidingUpPanelLayout.PanelState.COLLAPSED)
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        if(newChallengeMode){
            newChallengePointer.setPosition(latLng);
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            newChallengePointer.showInfoWindow();
        }
        if (mPlusMarker != null) {
            mPlusMarker.remove();
            mPlusMarker = null;
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        if(mPlusMarker != null){
            mPlusMarker.remove();
            mPlusMarker = null;
        }
        if(!newChallengeMode) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.new_challenge));
            mPlusMarker = mMap.addMarker(markerOptions);
        }
    }

    private void createNewChallenge(){
        createNewChallenge(mMap.getCameraPosition().target.latitude, mMap.getCameraPosition().target.longitude);
    }

    private void createNewChallenge(Double latitude, Double longitude){
        Intent intent = new Intent(this, NewChallengeActivity.class);
        if (latitude != null){
            intent.putExtra("LATITUDE", latitude);
        }
        if (latitude != null){
            intent.putExtra("LONGITUDE", longitude);
        }
        startActivityForResult(intent, NEW_CHALLENGE_REQUEST_CODE);
    }

    private void setNewChallengeMode(boolean setMode){
        for(Marker marker: mMarkers.keySet()){
            marker.setVisible(!setMode);
        }
        newChallengeMode = setMode;
        changeLocationForNewChallengeBtn.setEnabled(setMode);

        if(setMode) {

            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
            changeLocationForNewChallengeBtn.setVisibility(View.VISIBLE);
            if(mPlusMarker != null){
                mPlusMarker.remove();
                mPlusMarker = null;
            }
            if(newChallengePointer == null) {
                MarkerOptions newChallengePointerOptions = new MarkerOptions();
                newChallengePointerOptions.draggable(true);
                Bitmap locationPointer = BitmapFactory.decodeResource(getResources(), R.drawable.new_challenge_black);
                newChallengePointerOptions.icon(BitmapDescriptorFactory.fromBitmap(locationPointer));
                newChallengePointerOptions.position(new LatLng(savedData.getLatitude(), savedData.getLongitude()));
                newChallengePointer = mMap.addMarker(newChallengePointerOptions);
                newChallengePointer.setDraggable(true);
            }
        }
        else {
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            changeLocationForNewChallengeBtn.setVisibility(View.GONE);
            if(newChallengePointer != null){
                newChallengePointer.remove();
                newChallengePointer = null;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == NEW_CHALLENGE_REQUEST_CODE){
            if(resultCode == RESULT_OK) {
                setNewChallengeMode(false);
                if (mPlusMarker != null) {
                    mPlusMarker.remove();
                    mPlusMarker = null;
                }
                addChallenge(extractChallengeFromBundle(data));
                savedData = null;

            }else if(resultCode == RESULT_FIRST_USER){
                savedData = extractChallengeFromBundle(data);
                setNewChallengeMode(true);

            }else if(resultCode == RESULT_CANCELED){
                setNewChallengeMode(false);
                savedData = null;
            }
        }
    }

    private void setLocationToNewChallenge(){
        Intent intent = new Intent(this, NewChallengeActivity.class);
        intent.putExtra("LATITUDE", newChallengePointer.getPosition().latitude);
        intent.putExtra("LONGITUDE", newChallengePointer.getPosition().longitude);
        intent.putExtra("TITLE", savedData.title);
        intent.putExtra("DESCRIPTION", savedData.description);
        intent.putExtra("REWARD", savedData.reward);
        intent.putExtra("TYPE", savedData.type);
        startActivityForResult(intent, NEW_CHALLENGE_REQUEST_CODE);
    }

    private Challenge extractChallengeFromBundle(Intent bundle){
        if(!bundle.hasExtra("LATITUDE") || !bundle.hasExtra("LONGITUDE")){

        }
        double latitude = bundle.getDoubleExtra("LATITUDE", 0);
        double longitude = bundle.getDoubleExtra("LONGITUDE", 0);
        String title = bundle.getStringExtra("TITLE");
        String description = bundle.getStringExtra("DESCRIPTION");
        int reward = bundle.getIntExtra("REWARD", 0);
        int type = bundle.getIntExtra("TYPE", 1);
        Challenge challenge = new Challenge(currentUser.getDisplayName(),currentUser.getUid() , latitude, longitude, title, description, reward, type);
        return challenge;
    }

    @Override
    public void onBackPressed() {
        if(newChallengeMode){
            Intent intent = new Intent(this, NewChallengeActivity.class);
            intent.putExtra("LATITUDE", savedData.latitude);
            intent.putExtra("LONGITUDE", savedData.longitude);
            intent.putExtra("TITLE", savedData.title);
            intent.putExtra("DESCRIPTION", savedData.description);
            intent.putExtra("REWARD", savedData.reward);
            intent.putExtra("TYPE", savedData.type);
            startActivityForResult(intent, NEW_CHALLENGE_REQUEST_CODE);
        }
        super.onBackPressed();
    }

    @Override
    public void onPause() {
        super.onPause();

        //stop location updates when Activity is no longer active
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    private void checkPhoto(boolean checkInMemory){
        if(checkInMemory) {
            String filePath = Environment.getExternalStorageDirectory() + File.separator+"Android"+File.separator+"data"+File.separator+"co.app-go"+File.separator+"upBM.bm";
            File photoFile = new File(filePath);
            if(photoFile.exists()){
                userPhoto = BitmapFactory.decodeFile(filePath);
                userPhotoAvalible = true;
                acctPhotoHolder.setImageBitmap(userPhoto);
                return;
            }
        }
        AsyncTask<String, Void, Bitmap> photoPullAsync = new AsyncTask<String, Void, Bitmap>() {
            Bitmap photo = null;

            @Override
            protected Bitmap doInBackground(String... strings) {
                try {
                    System.out.println("get photo from url");
                    URL url = new URL(strings[0]);
                    URLConnection connection = url.openConnection();
                    connection.connect();
                    InputStream inputStream = connection.getInputStream();
                    BufferedInputStream buffer = new BufferedInputStream(inputStream);
                    photo = BitmapFactory.decodeStream(buffer);
                    buffer.close();
                    inputStream.close();
                    FileOutputStream fileOutputStream = new FileOutputStream(Environment.getExternalStorageDirectory() + File.separator+"Android"+File.separator+"data"+File.separator+"co.app-go"+File.separator+"upBM.bm");
                    photo.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                    fileOutputStream.close();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return photo;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                userPhoto = bitmap;
                userPhotoAvalible = true;
                acctPhotoHolder.setImageBitmap(userPhoto);
            }
        };
        photoPullAsync.execute(currentUser.getPhotoUrl().toString());
    }

    private void setMyChallengesTab(){
        myChallengesListView = (ListView) findViewById(R.id.my_list);
        myChallengesListView.setAdapter(new ChallengeListArrayAdapter(thisActivity, myChallenges, true));
    }


}
