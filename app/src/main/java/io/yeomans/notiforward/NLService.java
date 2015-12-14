package io.yeomans.notiforward;

/**
 * Created by jason on 11/20/15.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.firebase.client.Firebase;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class NLService extends NotificationListenerService {


    //GCM
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "device_gcm_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    /**
     * Substitute you own sender ID here. This is the project number you got
     * from the API Console, as described in "Getting Started."
     */
    private String SENDER_ID = "45203521863";
    private GoogleCloudMessaging gcm;
    private String regid;

    private String TAG = this.getClass().getSimpleName();
    private Firebase ref;
    private SharedPreferences pref;
    private Context context;
    private String androidId;

    @Override
    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();
        Firebase.setAndroidContext(context);
        SharedPreferences pref = getSharedPreferences(MainActivity.MAIN_PREF, 0);
        String fUrl = pref.getString(MainActivity.PREF_FIREBASE_URL, null);
        if (fUrl != null) {
            Log.d(TAG, "Firebase URL: " + fUrl);
            ref = new Firebase(fUrl);
        } else {
            Log.d(TAG, "fUrl is null");
            this.stopSelf();
        }
        androidId = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        registerInBackground();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

        Log.i(TAG, "**********  onNotificationPosted");
        Log.i(TAG, "ID :" + sbn.getId() + "\t" + "Key :" + sbn.getKey() + "\t" + sbn.getNotification().tickerText + "\t" + sbn.getPackageName());

        ////////Attempt at getting the applications icon and converting it to base64, is there a Play Store API that allows one to get an apps icon?
//        String ba1 = null;
//        try {
//            Context packageApp = createPackageContext(sbn.getPackageName(), 0);
//            Bitmap bm = BitmapFactory.decodeResource(packageApp.getResources(), sbn.getNotification().icon);
//            Log.d(TAG, "Icon: " + sbn.getNotification().icon);
//            if (bm != null) {
//                ByteArrayOutputStream bao = new ByteArrayOutputStream();
//                bm.compress(Bitmap.CompressFormat.JPEG, 100, bao);
//                byte[] ba = bao.toByteArray();
//                ba1 = Base64.encodeToString(ba, Base64.DEFAULT);
//            }
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
        String packageName = sbn.getPackageName();
        Bundle extras = sbn.getNotification().extras;
        String title = extras.getString("android.title");
        String text = extras.getString("android.text");
        ////Doesn't send ongoing notifications or specific packages to the database. This is to prevent notification spam.
        if (!sbn.isOngoing() && !packageName.equals("android") && !packageName.contains("incallui") && !(packageName.contains("com.android.mms") && text == null) && !packageName.equals("com.android.providers.downloads") && !packageName.equals("com.google.android.gms")) {
            Map<String, Object> notif = new HashMap<>();
            notif.put("package", sbn.getPackageName().replace('.', '_'));
//        if (ba1 != null) {
//            notif.put("icon", ba1);
//        }
            notif.put("ticker", sbn.getNotification().tickerText);
            notif.put("title", title);
            notif.put("text", text);
            ref.child(ref.getAuth().getUid() + "/notifications/" + sbn.getPostTime()).setValue(notif);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i(TAG, "********** onNotificationRemoved");
        Log.i(TAG, "ID :" + sbn.getId() + "\t" + "Key :" + sbn.getKey() + "\t" + sbn.getNotification().tickerText + "\t" + sbn.getPackageName());

        ////Removes notification from the database if it was removed from the device (assuming you say and cleared the notification from the device)
        ref.child(ref.getAuth().getUid() + "/notifications/" + sbn.getPostTime()).removeValue();
    }


    /////////Accompanying method to convert drawable icon to bitmap
//    public Bitmap convertToBitmap(Drawable drawable, int widthPixels, int heightPixels) {
//        Bitmap mutableBitmap = Bitmap.createBitmap(widthPixels, heightPixels, Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(mutableBitmap);
//        drawable.setBounds(0, 0, widthPixels, heightPixels);
//        drawable.draw(canvas);
//
//        return mutableBitmap;
//    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    public boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
//                GooglePlayServicesUtil.getErrorDialog(resultCode, getAc,
//                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
            }
            return false;
        }
        return true;
    }

    /**
     * Stores the registration ID and the app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId   registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        //int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + regId);
        SharedPreferences.Editor editor = context.getSharedPreferences(MainActivity.MAIN_PREF, 0).edit();
        editor.putString(PROPERTY_REG_ID, regId);
        //editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    /**
     * Gets the current registration ID for application on GCM service, if there is one.
     * <p/>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     * registration ID.
     */
    private String getRegistrationId(Context context) {
        String registrationId = pref.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        //int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        //int currentVersion = getAppVersion(context);
        //if (registeredVersion != currentVersion) {
        //    Log.i(TAG, "App version changed.");
        //    return "";
        // }
        return registrationId;
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p/>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     */
    public void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP, so it
                    // can use GCM/HTTP or CCS to send messages to your app.
                    sendRegistrationIdToBackend(regid);

                    // For this demo: we don't need to send it because the device will send
                    // upstream messages to a server that echo back the message using the
                    // 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                //mDisplay.append(msg + "\n");
            }
        }.execute(null, null, null);
    }

    private void sendRegistrationIdToBackend(String reg) {
        Log.d("GCM", "Send to backend");
        ref.child(ref.getAuth().getUid() + "/devices/" + androidId + "/gcm_id").setValue(reg);
    }

    //This is the handler that will manager to process the broadcast intent
    private BroadcastReceiver actionGcmReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("GCM", "gcm_intent received in group activity");
            // Extract data included in the Intent
            String action = intent.getStringExtra("action");
            //do other stuff here
        }
    };
}