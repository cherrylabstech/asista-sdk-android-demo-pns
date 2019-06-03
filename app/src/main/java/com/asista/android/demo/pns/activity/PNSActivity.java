package com.asista.android.demo.pns.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.asista.android.demo.pns.R;
import com.asista.android.demo.pns.db.DBHelper;
import com.asista.android.demo.pns.model.Message;
import com.asista.android.pns.AsistaPNS;
import com.asista.android.pns.Result;
import com.asista.android.pns.exceptions.AsistaException;
import com.asista.android.pns.interfaces.Callback;
import com.asista.android.pns.model.RegistrationRequestDetails;
import com.asista.android.pns.util.CommonUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Benjamin J on 30-05-2019.
 */
public class PNSActivity extends AppCompatActivity {
    private static final String TAG = PNSActivity.class.getSimpleName();
    private static final String TOPIC = "news";
    private static final String BUNDLE = "bundle";

    private PNSActivity context;
    private Bundle bundle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: oncreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pns);
        context = this;

        bundle = getIntent().getExtras();

        setToolbar();
        if (null != bundle) {
            Message message = new Message();
            if (bundle != null) {
                for (String key : bundle.keySet()) {
                    Object value = bundle.get(key);
                    Log.e(TAG, "KEY: " + key + "   VALUE: " + value);
                    if ("google.message_id".equals(key)) {
                        message.setId(String.valueOf(value));
                        message.setTitle(String.valueOf(value));
                        message.setBody(getResources().getString(R.string.background_notification_body));
                        DBHelper.getInstance(context).saveMessage(message);
                        Log.i(TAG, "onCreate: message saved... "+DBHelper.getInstance(context).fetchMessages());
                        break;
                    }
                }
            }
        }else
            Log.e(TAG, "onCreate: bundle is null");

        AsistaPNS.init(context, new Callback<Void>() {
            @Override
            public void onSuccess(Result<Void> result) {
                Log.i(TAG, "onSuccess: SDK initialisation success... ");
                RegistrationRequestDetails details = new RegistrationRequestDetails();
                details.setUserCustomData("asista PNS user");
                Map<String, Object> moreUserData = new HashMap<>();
                moreUserData.put("DeviceType", "Android");
                moreUserData.put("Type", "GeneralUser");
                details.setMore(moreUserData);
                AsistaPNS.register(details, new Callback<Void>() {
                    @Override
                    public void onSuccess(Result<Void> result) {
                        Log.i(TAG, "onSuccess: user registration successful... ");
                    }

                    @Override
                    public void onFailed(AsistaException exception) {
                        Log.e(TAG, "onFailed: user registeration failed... " );
                        exception.printStackTrace();
                    }
                });

                initViews();
            }

            @Override
            public void onFailed(AsistaException exception) {
                Log.e(TAG, "onFailed: SDK initialisation failed... " );
                exception.printStackTrace();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        showNotificationIcon();
    }

    private void setToolbar(){
        if (null != getSupportActionBar()){
            getSupportActionBar().setTitle(getResources().getString(R.string.activity_asista_pns_toolbar_title));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_back_wht);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void initViews(){
        findViewById(R.id.topic_subscribe_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableClicks(false);
                AsistaPNS.subscribe(TOPIC, new Callback<Void>() {
                    @Override
                    public void onSuccess(Result<Void> result) {
                        Log.i(TAG, "onSuccess: subscribed successfully... result: "+result.data);
                        enableClicks(true);
                        CommonUtil.showToast("Subscribed to topic "+TOPIC, context);
                    }

                    @Override
                    public void onFailed(AsistaException exception) {
                        Log.e(TAG, "onFailed: topic subscription failed... ");
                        exception.printStackTrace();
                        enableClicks(true);
                        CommonUtil.displayDialog(exception.getMessage(), context);
                    }
                });
            }
        });

        findViewById(R.id.topic_unsubscibe_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableClicks(false);
                AsistaPNS.unsubscribe(TOPIC, new Callback<Void>() {
                    @Override
                    public void onSuccess(Result<Void> result) {
                        Log.i(TAG, "onSuccess: unsubscribed successfully... result: "+result.data);
                        enableClicks(true);
                        CommonUtil.showToast("Unsubscribed from topic "+TOPIC, context);
                    }

                    @Override
                    public void onFailed(AsistaException exception) {
                        Log.e(TAG, "onFailed: topic unsubscription failed... ");
                        exception.printStackTrace();
                        enableClicks(true);
                        CommonUtil.displayDialog(exception.getMessage(), context);
                    }
                });
            }
        });

        findViewById(R.id.pns_unregister_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableClicks(false);
                AsistaPNS.unRegister(new Callback<Void>() {
                    @Override
                    public void onSuccess(Result<Void> result) {
                        Log.i(TAG, "onSuccess: unregistering successful... result: "+result.data);
                        enableClicks(true);
                        CommonUtil.showToast("Device Unregistered", context);
                    }

                    @Override
                    public void onFailed(AsistaException exception) {
                        Log.e(TAG, "onFailed: unregistering failed... " );
                        exception.printStackTrace();
                        enableClicks(true);
                        CommonUtil.displayDialog(exception.getMessage(), context);
                    }
                });
            }
        });

        findViewById(R.id.copy_token).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!CommonUtil.checkIsEmpty(AsistaPNS.getDeviceToken())) {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("fcm_deviceToken", AsistaPNS.getDeviceToken());
                    clipboard.setPrimaryClip(clip);
                    CommonUtil.showToast("DeviceToken copied to clipBoard ", context);
                }else
                    CommonUtil.showToast("DeviceToken is empty", context);
            }
        });
    }

    private void showNotificationIcon(){
        Log.i(TAG, "showNotificationIcon: ");
        List messages = DBHelper.getInstance(context).fetchMessages();
        if (!CommonUtil.checkIsEmpty(messages)){
            findViewById(R.id.notification_icon_lyout).setVisibility(View.VISIBLE);
            ((TextView)findViewById(R.id.notification_count)).setText(String.valueOf(messages.size()));

            findViewById(R.id.notification_icon).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(context, NotificationListActivity.class));
                }
            });

        }else
            findViewById(R.id.notification_icon_lyout).setVisibility(View.GONE);
    }

    private void enableClicks(boolean isEnable){
        if (isEnable)
            findViewById(R.id.progressbar).setVisibility(View.GONE);
        else
            findViewById(R.id.progressbar).setVisibility(View.VISIBLE);

        findViewById(R.id.topic_subscribe_tv).setEnabled(isEnable);
        findViewById(R.id.topic_unsubscibe_tv).setEnabled(isEnable);
        findViewById(R.id.pns_unregister_tv).setEnabled(isEnable);
    }

}
