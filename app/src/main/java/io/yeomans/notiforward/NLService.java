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
    //    private NLServiceReceiver nlservicereciver;
    private Firebase ref;

    @Override
    public void onCreate() {
        super.onCreate();
//        nlservicereciver = new NLServiceReceiver();
//        IntentFilter filter = new IntentFilter();
//        filter.addAction("io.yeomans.notiforward.NOTIFICATION_ACTION_SERVICE");
//        registerReceiver(nlservicereciver, filter);

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

//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        unregisterReceiver(nlservicereciver);
//    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

        Log.i(TAG, "**********  onNotificationPosted");
        Log.i(TAG, "ID :" + sbn.getId() + "\t" + "Key :" + sbn.getKey() + "\t" + sbn.getNotification().tickerText + "\t" + sbn.getPackageName());
//        Intent i = new Intent("io.yeomans.notiforward.NOTIFICATION_ACTION");
//        i.putExtra("notification_event", "onNotificationPosted :" + sbn.getPackageName() + "\n");
//        sendBroadcast(i);

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
        if (!sbn.getPackageName().equals("android")) {
            Map<String, Object> notif = new HashMap<>();
            notif.put("package", sbn.getPackageName().replace('.', '_'));
//        if (ba1 != null) {
//            notif.put("icon", ba1);
//        }
            notif.put("ticker", sbn.getNotification().tickerText);
            Bundle extras = sbn.getNotification().extras;
            notif.put("title", extras.getString("android.title"));
            CharSequence text = extras.getCharSequence("android.text");
            if (text != null) {
                notif.put("text", text.toString());
            } else {
                notif.put("text", null);
            }
            //Firebase pushRef = ref.child("notifications").push();
            //pushRef.setValue(notif);
            ref.child(ref.getAuth().getUid() + "/notifications/" + sbn.getPostTime()).setValue(notif);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i(TAG, "********** onNOtificationRemoved");
        Log.i(TAG, "ID :" + sbn.getId() + "\t" + "Key :" + sbn.getKey() + "\t" + sbn.getNotification().tickerText + "\t" + sbn.getPackageName());
//        Intent i = new Intent("io.yeomans.notiforward.NOTIFICATION_ACTION");
//        i.putExtra("notification_event", "onNotificationRemoved :" + sbn.getPackageName() + "\n");
//        sendBroadcast(i);
        ref.child(ref.getAuth().getUid() + "/notifications/" + sbn.getPostTime()).removeValue();
    }

//    public Bitmap convertToBitmap(Drawable drawable, int widthPixels, int heightPixels) {
//        Bitmap mutableBitmap = Bitmap.createBitmap(widthPixels, heightPixels, Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(mutableBitmap);
//        drawable.setBounds(0, 0, widthPixels, heightPixels);
//        drawable.draw(canvas);
//
//        return mutableBitmap;
//    }

//    class NLServiceReceiver extends BroadcastReceiver {
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            if (intent.getStringExtra("command").equals("clearall")) {
//                NLService.this.cancelAllNotifications();
//            } else if (intent.getStringExtra("command").equals("list")) {
//                Intent i1 = new Intent("io.yeomans.notiforward.NOTIFICATION_ACTION");
//                i1.putExtra("notification_event", "=====================");
//                sendBroadcast(i1);
//                int i = 1;
//                for (StatusBarNotification sbn : NLService.this.getActiveNotifications()) {
//                    Intent i2 = new Intent("io.yeomans.notiforward.NOTIFICATION_ACTION");
//                    i2.putExtra("notification_event", i + " " + sbn.getPackageName() + "\n");
//                    sendBroadcast(i2);
//                    i++;
//                }
//                Intent i3 = new Intent("io.yeomans.notiforward.NOTIFICATION_ACTION");
//                i3.putExtra("notification_event", "===== Notification List ====");
//                sendBroadcast(i3);
//
//            }
//
//        }
//    }
}