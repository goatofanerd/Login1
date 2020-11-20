package com.example.login1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.*;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;

import com.google.android.gms.drive.*;
import com.google.android.gms.tasks.*;
import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {

    public int RC_SIGN_IN = 2401;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        configureGoogle();

    }

    private void configureGoogle()
    {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso;
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(Scopes.PROFILE))
                .requestScopes(new Scope(Scopes.PLUS_ME))
                .requestEmail()
                .requestServerAuthCode("153743634273-3lls4nv158hqnqa0913jrlb90kqg6tu5.apps.googleusercontent.com")
                .build();


        // Build a GoogleSignInClient with the options specified by gso.
        GoogleSignInClient googleClient = GoogleSignIn.getClient(this, gso);
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        //GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);



        //On Google Click
        View.OnClickListener onGoogleClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ProgressBar)findViewById(R.id.load)).setVisibility(View.VISIBLE);
                googleClient.signOut();
                signIn(googleClient);
            }
        };
        findViewById(R.id.signInButton).setOnClickListener(onGoogleClick);
    }

    public void signIn(GoogleSignInClient googleClient)
    {
        Intent signInIntent = googleClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            Log.d("success", "signInResult:success");

            updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.d("failure", "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }

    public void updateUI(GoogleSignInAccount googleAccount){
        findViewById(R.id.errorLoginText).setVisibility(View.VISIBLE);
        if(googleAccount == null)
        {
            ((ProgressBar)findViewById(R.id.load)).setVisibility(View.INVISIBLE);
            ((TextView)findViewById(R.id.errorLoginText)).setText("Error logging in. Please close the app and reopen it.");
            ((TextView)findViewById(R.id.errorLoginText)).setTextColor(Color.rgb(255, 0, 0));
        }
        else
        {
            String name = googleAccount.getDisplayName();
            ((TextView)findViewById(R.id.errorLoginText)).setTextColor(Color.rgb(0, 255, 255));
            ((TextView)findViewById(R.id.errorLoginText)).setText("Logging into " + name + "'s account...");

            //Going into activity page.
            Intent nextPage = new Intent(this, ProfileAndImage.class);
            nextPage.putExtra("account", googleAccount);
            startActivity(nextPage);
        }
    }

}