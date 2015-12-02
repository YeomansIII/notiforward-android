package io.yeomans.notiforward;

/**
 * Created by jason on 11/20/15.
 */

import android.content.SharedPreferences;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.firebase.client.Firebase;

import java.util.HashMap;
import java.util.Map;

public class NLService extends NotificationListenerService {

    private String TAG = this.getClass().getSimpleName();
    private Firebase ref;

    @Override
    public void onCreate() {
        super.onCreate();

        Firebase.setAndroidContext(getApplicationContext());
        SharedPreferences pref = getSharedPreferences(MainActivity.MAIN_PREF, 0);
        String fUrl = pref.getString(MainActivity.PREF_FIREBASE_URL, null);
        if (fUrl != null) {
            Log.d(TAG, "Firebase URL: " + fUrl);
            ref = new Firebase(fUrl);
        } else {
            Log.d(TAG, "fUrl is null");
            this.stopSelf();
        }
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
}