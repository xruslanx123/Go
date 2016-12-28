package co.go_app.app;

import java.util.ArrayList;

/**
 * Created by Ruslan on 12/22/2016 at 11:54 PM.
 */

public class User {

    private String uID;
    private String photoUrl;
    private String emailAddress;
    private ArrayList<String> acceptedChallenges;
    private ArrayList<String> createdChallenges;
    
    //setters
    public void setUID(String uID){
        this.uID = uID;
    }
    public void setPhotoUrl(String url){
        this.photoUrl = url;
    }
    public void setEmailAddress(String address){
        this.emailAddress = address;
    }
    public void setAcceptedChallenges(ArrayList<String> acceptedChallenges){
        this.acceptedChallenges = acceptedChallenges;
    }
    public void setCreatedChallenges(ArrayList<String> createdChallenges){
        this.createdChallenges = createdChallenges;
    }
    
    //getters
    public String getUID(){
        return this.uID;
    }
    public String getPhotoUrl(){
        return this.photoUrl;
    }
    public String getEmailAddress(){
        return this.emailAddress;
    }
    public ArrayList<String> getAcceptedChallenges(){
        return this.acceptedChallenges;
    }
    public ArrayList<String> getCreatedChallenges(){
        return this.createdChallenges;
    }

}
