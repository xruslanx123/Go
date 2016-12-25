package co.go_app.app;

import android.app.Activity;
import android.location.Location;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by booni on 08/12/2016.
 */

public class ChallengeListArrayAdapter extends ArrayAdapter {

    private Activity activity;
    private ArrayList<Challenge> challengesList;
    private Location currentLocation;
    private long updateTimeStapm;
    private boolean myChallenges;

    public ChallengeListArrayAdapter(Activity activity, ArrayList<Challenge> challengesList, boolean myChallenges) {
        super(activity, R.layout.challenge_list_item, challengesList);
        this.activity = activity;
        this.challengesList = challengesList;
        this.myChallenges = myChallenges;
    }

    public void updateLocation(Location location){
        this.currentLocation = location;
        updateTimeStapm = System.currentTimeMillis();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewContainer viewContainer;
        if(convertView == null){
            viewContainer = new ViewContainer();
            convertView = activity.getLayoutInflater().inflate(R.layout.challenge_list_item, parent, false);
            viewContainer.creator = (TextView)convertView.findViewById(R.id.challenge_creator);
            viewContainer.distance = (TextView)convertView.findViewById(R.id.challenge_distance);
            viewContainer.title = (TextView)convertView.findViewById(R.id.challenge_title);
            viewContainer.description = (TextView)convertView.findViewById(R.id.challenge_description);
            viewContainer.reward = (TextView)convertView.findViewById(R.id.challenge_points_reward);
            convertView.setTag(viewContainer);
        }else{
            viewContainer = (ViewContainer) convertView.getTag();
        }
        viewContainer.creator.setText(challengesList.get(position).creator);
        if (myChallenges){
            viewContainer.distance.setVisibility(View.GONE);
        } else {
            viewContainer.distance.setVisibility(View.VISIBLE);
            if (viewContainer.updateTimeStamp < updateTimeStapm) {
                Location challengeLoc = challengesList.get(position).retrieveLocation();
                double distanceMeters = distFrom(challengeLoc.getLatitude(), challengeLoc.getLongitude(),
                        currentLocation.getLatitude(), currentLocation.getLongitude());
                String distanceText = "m";
                if (distanceMeters >= 1000) {
                    distanceMeters /= 1000;
                    distanceText = "km";
                }
                distanceText = new DecimalFormat("##.#").format(distanceMeters) + distanceText;
                viewContainer.distance.setText(distanceText);
                viewContainer.updateTimeStamp = System.currentTimeMillis();
            }
        }
        viewContainer.title.setText(challengesList.get(position).getTitle());
        viewContainer.description.setText(challengesList.get(position).getDescription());
        viewContainer.reward.setText(challengesList.get(position).getReward());
        return convertView;
    }

    private class ViewContainer {
        TextView creator;
        TextView distance;
        TextView title;
        TextView description;
        TextView reward;
        long updateTimeStamp;
    }


    public static double distFrom(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        float dist = (float) (earthRadius * c);

        return dist;
    }
}

