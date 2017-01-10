package co.go_app.app;

import android.location.Location;
import android.location.LocationManager;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Ruslan on Dec 8.
 */

public class Challenge {

    public String key;
    public String creator;
    public String creatorId;
    public double latitude;
    public double longitude;
    public String title;
    public String description;
    public int reward;
    public int type;
    
    public Challenge() {
        // Default constructor
    }

    public Challenge(String creator,
                     String creatorId,
                     double latitude,
                     double longitude,
                     String title,
                     String description,
                     Integer reward,
                     Integer type) {
        this.key = "";
        this.creator = creator;
        this.creatorId = creatorId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.title = title;
        this.description = description;
        this.reward = reward;
        this.type = type;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public void setType(int type) {
        this.type = type;
    }
    public Integer getType(){
        return this.type;
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

    @Override
    public String toString() {
        return "Title: "+title+"\n"
                +"Description: " +description+"\n"
                +"("+latitude+", "+longitude+")\n"
                +"Reward: "+reward+"\n"
                +"Type: "+type;
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }
}
