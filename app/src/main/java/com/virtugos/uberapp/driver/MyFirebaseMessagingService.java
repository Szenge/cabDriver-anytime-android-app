/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.virtugos.uberapp.driver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.virtugos.uberapp.driver.gcm.CommonUtilities;
import com.virtugos.uberapp.driver.utills.AndyConstants;
import com.virtugos.uberapp.driver.utills.AppLog;
import com.virtugos.uberapp.driver.utills.PreferenceHelper;

import org.json.JSONException;
import org.json.JSONObject;


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private PreferenceHelper preferenceHelper;

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        AppLog.Log("FCM", "push Response : " + remoteMessage.getData());
//        AndyUtils.showToast("callled onMessageReceived", this);
        // notifies user
//        publishResults(registrationId, Activity.RESULT_OK);
        String message = remoteMessage.getData().get("message");
        String team = remoteMessage.getData().get("team");
        AppLog.Log("Notificaton", message);
        AppLog.Log("Team", team);
        Intent pushIntent = new Intent(AndyConstants.NEW_REQUEST);
        pushIntent.putExtra(AndyConstants.NEW_REQUEST, team);
        // String messageBedge = intent.getExtras().getString("bedge");
        CommonUtilities.displayMessage(this, message);
        if (!TextUtils.isEmpty(message)) {

            try {
                JSONObject jsonObject = new JSONObject(team);
                preferenceHelper = new PreferenceHelper(this);
                if (jsonObject.getInt(AndyConstants.Params.UNIQUE_ID) == 1) {
                    Intent mapIntent = new Intent();
                    mapIntent.setAction(AndyConstants.ACTION_START_MAP_ACTIVITY);
                    mapIntent.addCategory("com.virtugos.uberapp.driver.category.MAPACTIVITY");
                    sendBroadcast(mapIntent);
                    generateNotificationNew(this, message);
                } else if (jsonObject.getInt(AndyConstants.Params.UNIQUE_ID) == 2) {
                    preferenceHelper.clearRequestData();
                    Intent i = new Intent("CANCEL_REQUEST");
                    this.sendBroadcast(i);
                    generateNotificationNew(this, message);
                } else if (jsonObject.getInt(AndyConstants.Params.UNIQUE_ID) == 3) {
                    preferenceHelper.putPaymentType(jsonObject.getJSONObject(
                            "owner_data").getInt("payment_type"));
                    Intent i = new Intent("PAYMENT_MODE");
                    this.sendBroadcast(i);
                } else if (jsonObject.getInt(AndyConstants.Params.UNIQUE_ID) == 5) {

                    preferenceHelper.putIsApproved(jsonObject
                            .getString(AndyConstants.Params.IS_APPROVED));
                    Intent i = new Intent("IS_APPROVED");
                    generateNotificationNew(this, message);
                    this.sendBroadcast(i);

                } else {
                    JSONObject ownerObject = jsonObject.getJSONObject(
                            "request_data").getJSONObject("owner");
                    try {
                        if (ownerObject.getString("dest_latitude").length() != 0) {
                            LatLng destLatLng = new LatLng(
                                    ownerObject.getDouble("dest_latitude"),
                                    ownerObject.getDouble("dest_longitude"));
                            preferenceHelper.putClientDestination(destLatLng);

                            Intent i = new Intent("CLIENT_DESTINATION");
                            this.sendBroadcast(i);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            generateNotificationNew(this, message);
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(pushIntent);

    }

    private void publishResults(String regid, int result) {
        Intent publishIntent = new Intent(
                CommonUtilities.DISPLAY_MESSAGE_REGISTER);
        publishIntent.putExtra(CommonUtilities.RESULT, result);
        publishIntent.putExtra(CommonUtilities.REGID, regid);
        System.out.println("sending broad cast");
        sendBroadcast(publishIntent);
    }
    private void generateNotificationNew(Context context, String message) {
        final Notification.Builder builder = new Notification.Builder(this);
        builder.setDefaults(Notification.DEFAULT_SOUND
                | Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE);
        builder.setStyle(
                new Notification.BigTextStyle(builder).bigText(message)
                        .setBigContentTitle(
                                context.getString(R.string.app_name)))
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(message).setSmallIcon(R.drawable.ic_launcher);
        builder.setAutoCancel(true);
        Intent notificationIntent = new Intent(context, MapActivity.class);
        notificationIntent.putExtra("fromNotification", "notification"); // set
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(context, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(intent);
        final NotificationManager nm = (NotificationManager) getSystemService(Context
                .NOTIFICATION_SERVICE);
        nm.notify(0, builder.build());
    }

}
