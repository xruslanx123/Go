package co.go_app.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class NewChallengeActivity extends AppCompatActivity {

    public static final String SAVED_FIELDS = "SAVED_FIELDS_FOR_LOCATION_UPDATE";
    private boolean titleFlag = false, locationFlag = false;
    public static final int TYPE_SINGLE = 1;
    public static final int TYPE_SPREAD = 2;
    public static final int TYPE_LINEAR = 3;

    public Double latitude = null;
    public Double longitude = null;
    public String title;
    public String description;
    public int reward = 0;
    public int type = TYPE_SINGLE;

    private EditText titleInput, descriptionInput, rewardInput;
    private Button changeLocationButton, finishButton;
    private TextView locationDisplay, pointsWarning;
    private Button typeSingle, typeSpread, typeLinear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_challenge);

        titleInput = (EditText) findViewById(R.id.new_challenge_title);
        descriptionInput = (EditText) findViewById(R.id.new_challenge_description);
        rewardInput = (EditText) findViewById(R.id.new_challenge_reward);

        changeLocationButton = (Button) findViewById(R.id.new_challenge_location_change_button);
        changeLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeLocation();
            }
        });
        finishButton = (Button) findViewById(R.id.new_challenge_finish);
        finishButton.setEnabled(false);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishBtn();
            }
        });

        locationDisplay = (TextView) findViewById(R.id.new_challenge_location_display);
        pointsWarning = (TextView) findViewById(R.id.new_challenge_points_warning);

        if (getIntent().hasExtra("LATITUDE") && getIntent().hasExtra("LONGITUDE")){
            latitude = getIntent().getDoubleExtra("LATITUDE", 0);
            longitude = getIntent().getDoubleExtra("LONGITUDE", 0);
            locationDisplay.setText(printLatLng(latitude, longitude));
                    locationFlag = true;
        }
        if(getIntent().hasExtra("TITLE")){
            title = getIntent().getStringExtra("TITLE");
            if(!title.equals(""))
                titleFlag = true;
        }
        if(getIntent().hasExtra("DESCRIPTION")){
            description = getIntent().getStringExtra("DESCRIPTION");
        }
        if(getIntent().hasExtra("REWARD")){
            reward = getIntent().getIntExtra("REWARD", 0);
        }
        if(getIntent().hasExtra("TYPE")){
            type = getIntent().getIntExtra("TYPE", 1);
        }


        titleInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.toString().equals("")){
                    titleFlag = false;
                    finishButton.setEnabled(false);
                }else {
                    titleFlag = true;
                    if(locationFlag){
                        finishButton.setEnabled(true);
                    }
                }
            }
        });

    }

    private String printLatLng(double latitude, double longitude){
        return "("+latitude+", "+longitude+")";
    }

    //TODO changeLocation needs to open map to point to the location.
    private void changeLocation(){
        SharedPreferences.Editor sharedPreferences = getSharedPreferences(SAVED_FIELDS, MODE_PRIVATE).edit();
        sharedPreferences.putString("TITLE", title);
        sharedPreferences.putString("DESCRIPTION", description);
        sharedPreferences.putInt("REWARD", reward);
        sharedPreferences.putInt("TYPE", type);
        sharedPreferences.apply();
        setResult(RESULT_FIRST_USER);
        finish();
    }

    private void updateFields(){
        title = titleInput.getText().toString();
        description = descriptionInput.getText().toString();
        String rewardText = rewardInput.getText().toString();
        if(!rewardText.equals("")){
            reward = Integer.valueOf(rewardText);
        }
    }

    public void finishBtn(){
        SharedPreferences sharedPreferences = getSharedPreferences("GO_P", MODE_PRIVATE);
        if (sharedPreferences.getBoolean("NO_WARNING_FLAG", false)){
            //TODO create warning fragment.
        }

        updateFields();

        Intent data = new Intent();
        data.putExtra("LATITUDE", latitude);
        data.putExtra("LONGITUDE", longitude);
        data.putExtra("TITLE", title);
        data.putExtra("DESCRIPTION", description);
        data.putExtra("REWARD", reward);
        data.putExtra("TYPE", type);
        setResult(RESULT_OK, data);
        finish();
    }
    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    public void typeChangeBtn(View view) {
        type = Integer.valueOf(view.getTag().toString());
    }
}
