package io.yeomans.notiforward;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
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
//        nReceiver = new NotificationReceiver();
//        IntentFilter filter = new IntentFilter();
//        filter.addAction("io.yeomans.notiforward.NOTIFICATION_ACTION");
//        registerReceiver(nReceiver, filter);

        pref = getSharedPreferences(MAIN_PREF, 0);

        findViewById(R.id.setAndAuthButton).setOnClickListener(this);
        errorText = (TextView) findViewById(R.id.errorText);
        firebaseUrlInput = (TextInputLayout) findViewById(R.id.firebaseUrlEditWrapper);
        emailInput = (TextInputLayout) findViewById(R.id.emailEditWrapper);
        passwordInput = (TextInputLayout) findViewById(R.id.passwordEditWrapper);

        firebaseUrlInput.getEditText().setText(pref.getString(PREF_FIREBASE_DB, ""));
        emailInput.getEditText().setText(pref.getString(PREF_FIREBASE_EMAIL, ""));
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        //unregisterReceiver(nReceiver);
//    }


    public void onClick(View v) {
        if (v.getId() == R.id.setAndAuthButton) {
            SharedPreferences.Editor edit = pref.edit();
            String fDb = firebaseUrlInput.getEditText().getText().toString();
            edit.putString(PREF_FIREBASE_DB, fDb);
            String fUrl = "https://" + fDb + ".firebaseio.com";
            edit.putString(PREF_FIREBASE_URL, fUrl);
            Firebase ref = new Firebase(fUrl);
            String email = emailInput.getEditText().getText().toString();
            edit.putString(PREF_FIREBASE_EMAIL, email).apply();
            String pass = passwordInput.getEditText().getText().toString();
            ref.authWithPassword(email, pass, new Firebase.AuthResultHandler() {
                @Override
                public void onAuthenticated(AuthData authData) {
                    errorText.setTextColor(Color.BLACK);
                    errorText.setText("Successful Authentication");
                    passwordInput.getEditText().setText("");
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

//    class NotificationReceiver extends BroadcastReceiver {
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String temp = intent.getStringExtra("notification_event") + "\n" + txtView.getText();
//            txtView.setText(temp);
//        }
//    }


}