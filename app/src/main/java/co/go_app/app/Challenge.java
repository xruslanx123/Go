package co.go_app.app;

import android.location.Location;
import android.location.LocationManager;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Anis on Dec 8.
 */

public class Challenge {

    public String key;
    public String creator;
    public double latitude;
    public double longitude;
    public String title;
    public String description;
    public int reward;

    public Challenge() {
        // Default constructor required for calls to DataSnapshot.getValue(Challenge.class)
    }

    public Challenge(String creator,
                     double latitude,
                     double longitude,
                     String title,
                     String description,
                     Integer reward) {
        this.key = "";
        this.creator = creator;
        this.latitude = latitude;
        this.longitude = longitude;
        this.title = title;
        this.description = description;
        this.reward = reward;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public LatLng retrieveLatLng() {
        return new LatLng(this.latitude, this.longitude);
    }

    public void setLatLng(LatLng latLng) {
        this.latitude = latLng.latitude;
        this.longitude = latLng.longitude;
    }

    public Location retrieveLocation() {
        Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        return location;
    }

    public void setLocation(Location location) {
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getReward() {
        return reward;
    }

    public void setReward(int reward) {
        this.reward = reward;
    }
}
