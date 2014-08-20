package com.emanga.emanga.app.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.emanga.emanga.app.R;
import com.emanga.emanga.app.activities.MainActivity;
import com.emanga.emanga.app.activities.ReaderActivity;
import com.emanga.emanga.app.controllers.App;
import com.emanga.emanga.app.database.OrmliteIntentService;
import com.emanga.emanga.app.models.Chapter;
import com.emanga.emanga.app.models.Manga;
import com.emanga.emanga.app.utils.Notification;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Ciro on 16/07/2014.
 */
public class GcmIntentService extends OrmliteIntentService {

    public static final String TAG = GcmIntentService.class.getName();

    public static final int NOTIFICATION_ID = 1;

    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "Message GCM recived");
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (extras != null && !extras.isEmpty()) {  // has effect of unparcelling Bundle
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " +
                        extras.toString());
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {

                // Post notification of received message.
                String manga_id = extras.getString("mangaId");
                int number = extras.getString("number") != null ? Integer.valueOf(extras.getString("number")) : 1;
                Manga m = getHelper().getMangaRunDao().queryForId(manga_id);
                if (m != null) {
                    Bitmap cover = getBitmapFromURL(m.cover);
                    cover = adaptLargeIcon(cover);
                    sendNotification(extras.getString("title"), extras.getString("msg"), cover, m, number);
                    Log.i(TAG, "Received: " + extras.toString());
                } else {
                    Log.i(TAG, "Manga doesn't match in database");
                }
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_gcm)
                        .setContentTitle("GCM Notification")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String title, String msg, Bitmap icon, Manga m, int number) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(this, ReaderActivity.class);
        intent.putExtra(ReaderActivity.ACTION_OPEN_MANGA, m);
        intent.putExtra(ReaderActivity.ACTION_OPEN_CHAPTER_NUMBER, number);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                intent, 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setLargeIcon(icon)
                        .setSmallIcon(R.drawable.ic_gcm)
                        .setContentTitle(title)
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mBuilder.setAutoCancel(true);

        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    private Bitmap getBitmapFromURL(String strUrl) {
        try {
            URL url = new URL(strUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            Log.e(TAG, e.getLocalizedMessage());
            return null;
        }
    }

    private Bitmap adaptLargeIcon(Bitmap icon){
        Resources res = App.getInstance().getApplicationContext().getResources();
        int height = (int) res.getDimension(android.R.dimen.notification_large_icon_height);
        int width = (int) res.getDimension(android.R.dimen.notification_large_icon_width);
        return Bitmap.createScaledBitmap(icon, width, height, false);
    }
}
