package io.yeomans.notiforward;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //private NotificationReceiver nReceiver;
    public final static String MAIN_PREF = "main_pref";
    public static final String PREF_FIREBASE_URL = "firebase_url";
    public static final String PREF_FIREBASE_DB = "firebase_db";
    public static final String PREF_FIREBASE_EMAIL = "firebase_email";

    private SharedPreferences pref;
    private TextView errorText;
    private TextInputLayout firebaseUrlInput, emailInput, passwordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Firebase.setAndroidContext(getApplicationContext());

        setContentView(R.layout.activity_main);

        pref = getSharedPreferences(MAIN_PREF, 0);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle("Notiforward");

        findViewById(R.id.setAndAuthButton).setOnClickListener(this);
        errorText = (TextView) findViewById(R.id.errorText);
        firebaseUrlInput = (TextInputLayout) findViewById(R.id.firebaseUrlEditWrapper);
        emailInput = (TextInputLayout) findViewById(R.id.emailEditWrapper);
        passwordInput = (TextInputLayout) findViewById(R.id.passwordEditWrapper);
        String fbDb = pref.getString(PREF_FIREBASE_DB, "");
        firebaseUrlInput.getEditText().setText(fbDb);
        emailInput.getEditText().setText(pref.getString(PREF_FIREBASE_EMAIL, ""));
        ////Display the notification access settings hint if the user has come back to the app after already being authenticated
        if (!fbDb.equals("")) {
            findViewById(R.id.notifAccessHint).setVisibility(View.VISIBLE);
        }
    }

    public void onClick(View v) {
        if (v.getId() == R.id.setAndAuthButton) {
            ////Save firebase URL, DB name, and email into shared preferences
            SharedPreferences.Editor edit = pref.edit();
            String fDb = firebaseUrlInput.getEditText().getText().toString();
            edit.putString(PREF_FIREBASE_DB, fDb);
            ////Take the database name that the user entered and convert it to the full firebase URL
            String fUrl = "https://" + fDb + ".firebaseio.com";
            edit.putString(PREF_FIREBASE_URL, fUrl);
            Firebase ref = new Firebase(fUrl);
            String email = emailInput.getEditText().getText().toString();
            edit.putString(PREF_FIREBASE_EMAIL, email).apply();
            String pass = passwordInput.getEditText().getText().toString();
            ////Auth with firebase using the entered values (url, email, password)
            ref.authWithPassword(email, pass, new Firebase.AuthResultHandler() {
                @Override
                public void onAuthenticated(AuthData authData) {
                    errorText.setTextColor(Color.BLACK);
                    errorText.setText("Successful Authentication");
                    findViewById(R.id.notifAccessHint).setVisibility(View.VISIBLE);
                    passwordInput.getEditText().setText("");
                    ////Restart the NotificationListener service to use new Firebase settings after successful auth
                    Intent intent = new Intent(MainActivity.this, NLService.class);
                    stopService(intent);
                    startService(intent);
                }

                @Override
                public void onAuthenticationError(FirebaseError firebaseError) {
                    errorText.setTextColor(Color.RED);
                    errorText.setText(firebaseError.getMessage());
                }
            });
        }
    }
}