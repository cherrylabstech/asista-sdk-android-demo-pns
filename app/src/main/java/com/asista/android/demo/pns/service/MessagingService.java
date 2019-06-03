package com.asista.android.demo.pns.service;

import android.util.Log;

import com.asista.android.demo.pns.db.DBHelper;
import com.asista.android.demo.pns.model.Message;
import com.asista.android.pns.AsistaPNS;
import com.asista.android.pns.Result;
import com.asista.android.pns.exceptions.AsistaException;
import com.asista.android.pns.interfaces.Callback;
import com.asista.android.pns.webservice.services.AsistaFCMService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by Benjamin J on 27-05-2019.
 */
public class MessagingService extends AsistaFCMService {
    private static final String TAG = MessagingService.class.getSimpleName();

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.i(TAG, "onNewToken: newToken: "+s);
        AsistaPNS.setDeviceToken(s);
        try {
            AsistaPNS.updateDeviceToken(s, new Callback<Void>() {
                @Override
                public void onSuccess(Result<Void> result) {
                    Log.i(TAG, "onSuccess: new device token updated successfully");
                }

                @Override
                public void onFailed(AsistaException exception) {
                    Log.e(TAG, "onFailed: unable to update new device token" );
                    exception.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (null != remoteMessage) {
            Log.i(TAG, "onMessageReceived: remoteMessage-custom: " + remoteMessage.getNotification().getTitle() + "\n body: " + remoteMessage.getNotification().getBody() + ",\n data: " + remoteMessage.getData());
            Message message = new Message();
            message.setId(remoteMessage.getMessageId());
            if (null != remoteMessage.getNotification()) {
                message.setTitle(remoteMessage.getNotification().getTitle());
                message.setBody(remoteMessage.getNotification().getBody());
            }
            DBHelper.getInstance(getApplicationContext()).saveMessage(message);
            Log.i(TAG, "onMessageReceived: message saved... "+DBHelper.getInstance(getApplicationContext()).fetchMessages());

            if (remoteMessage.getData() != null) {
                Log.i(TAG, "onMessageReceived:message data... ");
                for (String key : remoteMessage.getData().keySet()) {
                    Object value = remoteMessage.getData().get(key);
                    Log.e(TAG, "Key-custom: " + key + " Value-custom: " + value);
                }
            }
        }
    }
}
