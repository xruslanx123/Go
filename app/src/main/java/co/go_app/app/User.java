package co.go_app.app;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by Ruslan on 12/22/2016 at 11:54 PM.
 */

public class User {

    private String uID;
    private String displayName;
    private String photoUrl;
    private String emailAddress;
    private ArrayList<String> acceptedChallenges;
    private ArrayList<String> createdChallenges;
    private ValueEventListener acceptedChallengesEventListener;
    private ValueEventListener createdChallengesEventListener;
    private final GenericTypeIndicator<ArrayList<String>> t = new GenericTypeIndicator<ArrayList<String>>() {};

    public User(){
        
    }
    public User(String uID, String photoUrl, String emailAddress, String displayName){
        this.uID = uID;
        this.photoUrl = photoUrl;
        this.emailAddress = emailAddress;
        this.acceptedChallenges = new ArrayList<>();
        this.createdChallenges = new ArrayList<>();
        this.displayName = displayName;
        acceptedChallengesEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                acceptedChallenges = dataSnapshot.getValue(t);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        createdChallengesEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                createdChallenges = dataSnapshot.getValue(t);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }
    
    public User(String uID, String photoUrl, String emailAddress, String displayName, ArrayList<String> acceptedChallenges, ArrayList<String> createdChallenges){
        this(uID, photoUrl, emailAddress, displayName);
        this.acceptedChallenges = acceptedChallenges;
        this.createdChallenges = createdChallenges;
    }

    public void addAcceptedCallenge(String challengeID, DatabaseReference ref){
        if(acceptedChallenges.contains(challengeID))
            return;
        acceptedChallenges.add(challengeID);
        ref.child("acceptedChallenges").setValue(acceptedChallenges);
        ref.child("acceptedChallenges").addValueEventListener(acceptedChallengesEventListener);
    }

    public void addCreatedChallenge(String challengeID, DatabaseReference ref){
        createdChallenges.add(challengeID);
        ref.child("createdChallenges").setValue(createdChallenges);
        ref.child("createdChallenges").addValueEventListener(createdChallengesEventListener);
    }

    //setters
    public void setuID(String uID) {this.uID = uID;}
    public void setDisplayName(String displayName) {this.displayName = displayName;}
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
    public String getuID() {return uID;}
    public String getDisplayName() {return displayName;}
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
