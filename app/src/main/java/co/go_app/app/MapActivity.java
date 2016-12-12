package co.go_app.app;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
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
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Inspiration from: http://stackoverflow.com/a/34582595
 */
public class MapActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private Marker mMarker;
    private Marker mPlusMarker;
    private LatLng mPlusMarkerLatLng;
    private DatabaseReference mDatabase;
    private SlidingUpPanelLayout slidingUpPanelLayout;
    private ImageView toolbarArrowImgView;
    private HashMap<Marker, Challenge> mMarkers;
    private Activity thisActivity;
    private ArrayList<Challenge> challenges;

    private static final String TAG = "MapActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        challenges = new ArrayList<>();
        challenges.add(new Challenge("Ruslan", 0, 0, "Best Challenge", "This is the best Challenge", 1000));
        challenges.add(new Challenge("Niko", 0, 0, "An Ok Challenge", "This is an average Challenge", 100));
        challenges.add(new Challenge("Anis", 0, 0, "Another CHallange", "This is another Challenge", 100));
        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mMarkers = new HashMap<Marker, Challenge>();
        thisActivity = this;
        toolbarArrowImgView = (ImageView) findViewById(R.id.toolbar_arrow_img);
        slidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        slidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if (newState.equals(SlidingUpPanelLayout.PanelState.COLLAPSED) || newState.equals(SlidingUpPanelLayout.PanelState.DRAGGING)){
                    toolbarArrowImgView.setImageResource(R.drawable.toolbar_arrow);
                } else {
                    toolbarArrowImgView.setImageResource(R.drawable.logo);
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();

        //stop location updates when Activity is no longer active
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
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
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
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
        mLastLocation = location;
        if (mMarker != null) {
            mMarker.remove();
        }

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        mMarker = mMap.addMarker(markerOptions);

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

        //optionally, stop location updates if only current location is needed
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
                    markerOptions.title(challenge.getTitle());
                    markerOptions.snippet(challenge.getDescription());
                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.icon)));
                    mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                        @Override
                        public View getInfoWindow(Marker marker) {
                            return null;
                        }

                        @Override
                        public View getInfoContents(Marker marker) {
                            Challenge challenge = mMarkers.get(marker);
                            View view = thisActivity.getLayoutInflater().inflate(R.layout.challenge_map_item, null);
                            ((TextView)view.findViewById(R.id.challenge_title)).setText(challenge.getTitle());
                            //((TextView)view.findViewById(R.id.challenge_description)).setText(challenge.getDescription());
                            ((TextView)view.findViewById(R.id.challenge_creator)).setText(challenge.getCreator());
                            //((TextView)view.findViewById(R.id.challenge_points_reward)).setText(challenge.getReward());
                            ((TextView)view.findViewById(R.id.challenge_points_reward)).setText("100");
                            view.findViewById(R.id.challenge_distance).setVisibility(View.GONE);
                            return view;
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
    public void onMapClick(LatLng latLng) {
        if (mPlusMarker != null) {
            mPlusMarker.remove();
            mPlusMarker = null;
            return;
        }

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_add_circle_black_48dp));
        mPlusMarker = mMap.addMarker(markerOptions);
        mPlusMarkerLatLng = latLng;
    }

    // http://stackoverflow.com/questions/14226453/google-maps-api-v2-how-to-make-markers-clickable
    static int counter = 0;
    @Override
    public boolean onMarkerClick(final Marker marker) {
        if (marker.equals(mPlusMarker)) {
            if(counter > 3)
                return false;
            Challenge temp = challenges.get(counter++);
            addChallenge(new Challenge(
                    temp.getCreator(),
                    mPlusMarkerLatLng.latitude,
                    mPlusMarkerLatLng.longitude,
                    temp.getTitle(),
                    temp.getDescription(),
                    temp.getReward()));
            mPlusMarker.remove();
            mPlusMarker = null;
            return true;
        }
        return false;
    }
}
