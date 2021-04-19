package com.zendrive.phonegap;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
//import android.support.v4.app.NotificationCompat;
//import com.zendrive.sdk.ZendriveLocationSettingsResult;
import androidx.annotation.RequiresApi;

import org.apache.cordova.BuildConfig;
import com.zendrive.R;
import com.zendrive.sdk.ZendriveOperationResult;

/**
 * Utility to create notifications to show to the user when the Zendrive SDK has
 * something interesting to report.
 */
public class NotificationUtility {
    // Notification related constants
    public static final int FOREGROUND_MODE_NOTIFICATION_ID = 98;
    public static final int LOCATION_DISABLED_NOTIFICATION_ID = 99;
    public static final int LOCATION_PERMISSION_DENIED_NOTIFICATION_ID = 100;

    // channel keys (id) are used to sort the channels in the notification
    // settings page. Meaningful ids and descriptions tell the user
    // about which notifications are safe to toggle on/off for the application.
    private static final String FOREGROUND_CHANNEL_KEY = "Foreground";
    private static final String LOCATION_CHANNEL_KEY = "Location";

    /**
     * Create a notification that is displayed when the Zendrive SDK detects a
     * possible drive.
     *
     * @param context App context
     * @return the created notification.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static Notification createMaybeInDriveNotification(Context context) {
        createNotificationChannels(context);

        // suppresses deprecated warning for setPriority(PRIORITY_MIN)
        // noinspection deprecation
        return new Notification.Builder(context, FOREGROUND_CHANNEL_KEY).setContentTitle("Zendrive")
                .setDefaults(0).setPriority(Notification.PRIORITY_MIN)
                .setCategory(Notification.CATEGORY_SERVICE).setContentText("Detecting possible drive.")
                .setContentIntent(getNotificationClickIntent(context)).build();
    }
    /**
     * Create a notification that is displayed when the Zendrive SDK detects a
     * possible drive.
     *
     * @param context App context
     * @return the created notification.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static Notification createWaitingForDriveNotification(Context context) {
        createNotificationChannels(context);

        // suppresses deprecated warning for setPriority(PRIORITY_MIN)
        // noinspection deprecation
        return new Notification.Builder(context, FOREGROUND_CHANNEL_KEY).setContentTitle("Zendrive")
                .setDefaults(0).setPriority(Notification.PRIORITY_MIN)
                .setCategory(Notification.CATEGORY_SERVICE).setContentText("Detecting waiting for drive.")
                .setContentIntent(getNotificationClickIntent(context)).build();
    }

    /**
     * Create a notification that is displayed when the Zendrive SDK determines that
     * the user is driving.
     *
     * @param context App context
     * @return the created notification.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static Notification createInDriveNotification(Context context) {
        createNotificationChannels(context);
        return new Notification.Builder(context, FOREGROUND_CHANNEL_KEY).setContentTitle("Zendrive")
                .setCategory(Notification.CATEGORY_SERVICE).setContentText("Drive started.")
                .setContentIntent(getNotificationClickIntent(context)).build();
    }

    private static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            NotificationChannel lowPriorityNotificationChannel = new NotificationChannel(FOREGROUND_CHANNEL_KEY,
                    "Zendrive trip tracking", NotificationManager.IMPORTANCE_MIN);
            lowPriorityNotificationChannel.setShowBadge(false);
            manager.createNotificationChannel(lowPriorityNotificationChannel);

            NotificationChannel defaultNotificationChannel = new NotificationChannel(LOCATION_CHANNEL_KEY, "Problems",
                    NotificationManager.IMPORTANCE_DEFAULT);
            defaultNotificationChannel.setShowBadge(true);
            manager.createNotificationChannel(defaultNotificationChannel);
        }
    }

    private static PendingIntent getNotificationClickIntent(Context context) {
        Intent notificationIntent = new Intent(context.getApplicationContext(),
                ZendriveCordovaPlugin.getCordovaInstance().getActivity().getClass());
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(context.getApplicationContext(), 0, notificationIntent, 0);
    }

    /**
     * Create a notification when high accuracy location is disabled on the device.
     * @param context App context
     * @param settingsResult to get potential resolution from play services
     * @return the created notification.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static Notification createLocationSettingDisabledNotification(Context context,
                                                                         ZendriveOperationResult settingsResult) {
        createNotificationChannels(context);
        if (BuildConfig.DEBUG && settingsResult.isSuccess()) {
            throw new AssertionError("Only expected failed settings result");
        }
        // TODO: use the result from the callback and show appropriate message and intent
        Intent callGPSSettingIntent = new Intent(
                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), 0,
                callGPSSettingIntent, 0);

        return new Notification.Builder(context.getApplicationContext(), LOCATION_CHANNEL_KEY)
                .setContentTitle(context.getResources().getString(context.getResources().getIdentifier("R.string.location_disabled", "string", context.getPackageName())))
                .setTicker(context.getResources().getString(context.getResources().getIdentifier("R.string.location_disabled", "string", context.getPackageName())))
                .setContentText(context.getResources().getString(context.getResources().getIdentifier("R.string.enable_location", "string", context.getPackageName())))
                .setSmallIcon(context.getResources().getIdentifier("R.drawable.ic_notification", "drawable", context.getPackageName()))
                .setPriority(Notification.PRIORITY_MAX)
                .setContentIntent(pendingIntent)
                .setCategory(Notification.CATEGORY_ERROR)
                .build();
    }


    /**
     * Create a notification when location permission is denied to the application.
     * @param context App context
     * @return the created notification.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static Notification createLocationPermissionDeniedNotification(Context context) {
        createNotificationChannels(context);
        // TODO: The click intent should not point to location settings. Perhaps we can load
        // the app permissions tab.
        Intent callGPSSettingIntent = new Intent(
                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), 0,
                callGPSSettingIntent, 0);

        return new Notification.Builder(context.getApplicationContext(), LOCATION_CHANNEL_KEY)
                .setContentTitle(context.getResources().getString(context.getResources().getIdentifier("R.string.location_disabled", "string", context.getPackageName())))
                .setTicker(context.getResources().getString(context.getResources().getIdentifier("R.string.location_disabled", "string", context.getPackageName())))
                .setContentText(context.getResources().getString(context.getResources().getIdentifier("R.string.enable_location", "string", context.getPackageName())))
                .setSmallIcon(context.getResources().getIdentifier("R.drawable.ic_notification", "drawable", context.getPackageName()))
                .setPriority(Notification.PRIORITY_MAX)
                .setContentIntent(pendingIntent)
                .setCategory(Notification.CATEGORY_ERROR)
                .build();
    }
}