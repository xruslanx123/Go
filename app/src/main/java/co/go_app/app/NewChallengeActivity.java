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
        //set OnClickListeners to the type buttons.
        View.OnClickListener typeClickListener = new View.OnClickListener(){
            public void onClick(View v) {
                type = Integer.valueOf((String) v.getTag());
            }
        };
        typeSingle = (Button) findViewById(R.id.type_single);
        typeSingle.setOnClickListener(typeClickListener);
        typeSpread = (Button) findViewById(R.id.type_spread);
        typeSpread.setOnClickListener(typeClickListener);
        typeLinear = (Button) findViewById(R.id.type_linear);
        typeLinear.setOnClickListener(typeClickListener);
        
        
        titleInput = (EditText) findViewById(R.id.new_challenge_title);
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
                title = editable.toString();
            }
        });
        descriptionInput = (EditText) findViewById(R.id.new_challenge_description);
        descriptionInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                description = editable.toString();
            }
        });
        rewardInput = (EditText) findViewById(R.id.new_challenge_reward);
        rewardInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                while (editable.length() > 1 && editable.charAt(0) == '0') {
                    editable = editable.delete(0, 1);
                }
                if(editable.length() == 1 && editable.charAt(0) == '0'){
                    editable.clear();
                    reward = 0;
                    return;
                }
                if(editable.length() > 0)
                    reward = Integer.valueOf(editable.toString());
            }
        });

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
            if(title != null && !title.equals("")) {
                titleFlag = true;
                titleInput.setText(title);
            }
        }
        if(getIntent().hasExtra("DESCRIPTION")){
            description = getIntent().getStringExtra("DESCRIPTION");
            descriptionInput.setText(description);
        }
        if(getIntent().hasExtra("REWARD")){
            reward = getIntent().getIntExtra("REWARD", 0);
            rewardInput.setText(String.valueOf(reward));
        }
        if(getIntent().hasExtra("TYPE")){
            type = getIntent().getIntExtra("TYPE", 1);
        }

    }



    private String printLatLng(double latitude, double longitude){
        return "("+latitude+", "+longitude+")";
    }

    //TODO changeLocation needs to open map to point to the location.
    private void changeLocation(){
        Intent data = new Intent();
        data.putExtra("LATITUDE", latitude);
        data.putExtra("LONGITUDE", longitude);
        data.putExtra("TITLE", title);
        data.putExtra("DESCRIPTION", description);
        data.putExtra("REWARD", reward);
        data.putExtra("TYPE", type);
        setResult(RESULT_FIRST_USER, data);
        finish();
    }

    public void finishBtn(){
        SharedPreferences sharedPreferences = getSharedPreferences("GO_P", MODE_PRIVATE);
        if (sharedPreferences.getBoolean("NO_WARNING_FLAG", false)){
            //TODO create warning fragment.
        }

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
}
