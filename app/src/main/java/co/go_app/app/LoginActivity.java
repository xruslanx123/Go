package co.go_app.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;

public class LoginActivity extends Activity {

    private static final int RC_SIGN_IN = 9123;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if(auth.getCurrentUser() != null)
            startActivity(new Intent(this, MapActivity.class));
        else
            startLogin();
    }

    public void startLogin(){
        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setProviders(Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                        new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                        new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build()))
                .setIsSmartLockEnabled(false)
                .setTheme(R.style.AuthStyle)
                .build(),RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_SIGN_IN){
            if(resultCode == RESULT_OK){
                startActivity(new Intent(this, MapActivity.class));
                finish();
            }
            if(resultCode == RESULT_CANCELED){
                System.out.println("result canceled");
                this.finishAffinity();
            }
            else{
                startLogin();
            }
        }
    }
}
