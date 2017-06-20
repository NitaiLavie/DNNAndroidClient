package com.dnnproject.android.dnnandroidclient;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;

import static android.support.v7.appcompat.R.id.wrap_content;

public class MainActivity extends AppCompatActivity implements DnnServiceCallbacks {
    private static final String TAG = "MainActivity";

    private SharedPreferences mPrefs;
    private SharedPreferences.Editor mPrefsEditor;

    private Toast mToast;

    private LinearLayout inputLayout;

    private TextView ipTitleText;
    private EditText ipEditText;

    private TextView usernameTitleText;
    private EditText usernameEditText;

    private TextView bigUsername;

    private TextView serviceMessage;

    private TextView serviceText;
    private Button serviceButton;

    private static boolean dnnServiceStarted = false;

    private boolean mServiceBound = false;
    private DnnService mDnnService;

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DnnService.DNNServiceBinder myBinder = (DnnService.DNNServiceBinder) service;
            mDnnService = myBinder.getService();
            mDnnService.setCallbacks(MainActivity.this);
            mDnnService.startMainThread();
            mServiceBound = true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToast = Toast.makeText(this,"",Toast.LENGTH_SHORT);

        inputLayout = (LinearLayout) findViewById(R.id.input_layout);

        ipTitleText = (TextView) findViewById(R.id.ip_title_text);
        ipEditText = (EditText) findViewById(R.id.ip_edit_box);

        usernameTitleText = (TextView) findViewById(R.id.username_title_text);
        usernameEditText = (EditText) findViewById(R.id.username_edit_box);

        bigUsername = (TextView) findViewById(R.id.big_username);

        serviceMessage = (TextView) findViewById(R.id.service_message);

        serviceText = (TextView) findViewById(R.id.status_text);
        serviceButton = (Button) findViewById(R.id.service_button);


        this.setLayout();
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(dnnServiceStarted){
            Intent intent = new Intent(this, DnnService.class);
            bindService(intent,mServiceConnection, Context.BIND_AUTO_CREATE);
            mServiceBound = true;
        }
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mPrefsEditor = mPrefs.edit();

        ipEditText.setText(mPrefs.getString(getText(R.string.PrefSavedIP).toString(),""));
        usernameEditText.setText(mPrefs.getString(getText(R.string.PrefSavedUsername).toString(),""));
        bigUsername.setText(mPrefs.getString(getText(R.string.PrefSavedUsername).toString(),""));
    }

    @Override
    protected void onPause(){
        if (mServiceBound) {
            unbindService(mServiceConnection);
            mServiceBound = false;
        }
        super.onPause();
        mPrefsEditor.putString(getText(R.string.PrefSavedIP).toString(), ipEditText.getText().toString());
        mPrefsEditor.putString(getText(R.string.PrefSavedUsername).toString(), usernameEditText.getText().toString());
        mPrefsEditor.commit();
    }



    public void onClick(View view) {
        Intent intent = new Intent(this, DnnService.class);
        if(usernameEditText.getText().toString().equals("")){
            mToast.setText(getText(R.string.toast_username));
            mToast.show();
        } else {
            if(dnnServiceStarted == false) {
                dnnServiceStarted = true;
                // send the ip address to the DnnService
                intent.putExtra(DnnService.IP, ipEditText.getText().toString());
                intent.putExtra(DnnService.USERNAME, usernameEditText.getText().toString());

                startService(intent);
                mToast.setText(getText(R.string.toast_start));
                mToast.show();
                bindService(intent,mServiceConnection, Context.BIND_AUTO_CREATE);
            } else {
                dnnServiceStarted = false;
                if (mServiceBound) {
                    unbindService(mServiceConnection);
                    mServiceBound = false;
                }
                stopService(intent);
                mToast.setText(getText(R.string.toast_stop));
                mToast.show();
            }
            this.setLayout();
        }
    }

    private void setLayout(){
        if(dnnServiceStarted == true){
            serviceButton.setText(getText(R.string.button_stop));
            serviceText.setText(getText(R.string.text_stop));
            ipTitleText.setText(getText(R.string.current_ip_text));
            usernameTitleText.setText(getText(R.string.username_text));
            ipEditText.setEnabled(false);
            usernameEditText.setEnabled(false);
            inputLayout.setVisibility(View.GONE);
            bigUsername.setText(usernameEditText.getText());
            bigUsername.setVisibility(View.VISIBLE);
        } else {
            serviceButton.setText(getText(R.string.button_start));
            serviceText.setText(getText(R.string.text_start));
            ipTitleText.setText(getText(R.string.enter_ip_text));
            usernameTitleText.setText(getText(R.string.enter_username_text));
            ipEditText.setEnabled(true);
            usernameEditText.setEnabled(true);
            inputLayout.setVisibility(View.VISIBLE);
            bigUsername.setVisibility(View.GONE);
            serviceMessage.setText("");
        }
    }

    @Override
    public void printMessage(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                serviceMessage.setText(message);
                //setupFadeAnimation(serviceMessage, message);
            }
        });
    }

    @Override
    public void serverDisconnect() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(dnnServiceStarted == true) {
                    serviceButton.callOnClick();
                }
            }
        });
    }

    // text view animation for service messages:
//    private void setupFadeAnimation(final TextView textView, final String message) {
//        // Start from 0.1f if you desire 90% fade animation
//        final Animation fadeIn = new AlphaAnimation(0.0f, 1.0f);
//        fadeIn.setDuration(250);
//        fadeIn.setStartOffset(250);
//        final Animation fadeOut = new AlphaAnimation(1.0f, 0.0f);
//        fadeOut.setDuration(250);
//        fadeOut.setStartOffset(0);
//        fadeIn.setAnimationListener(new Animation.AnimationListener(){
//            @Override
//            public void onAnimationEnd(Animation arg0) {}
//            @Override
//            public void onAnimationRepeat(Animation arg0) {}
//            @Override
//            public void onAnimationStart(Animation arg0) {
//                textView.setText(message);
//            }
//        });
//        fadeOut.setAnimationListener(new Animation.AnimationListener(){
//            @Override
//            public void onAnimationEnd(Animation arg0) {
//                textView.startAnimation(fadeIn);
//            }
//            @Override
//            public void onAnimationRepeat(Animation arg0) {}
//            @Override
//            public void onAnimationStart(Animation arg0) {}
//        });
//        if(message != null) {
//            if(textView.getText().toString().equals("")){
//                textView.setAlpha(1.0f);
//                textView.startAnimation(fadeIn);
//            } else {
//                textView.startAnimation(fadeOut);
//            }
//        }
//    }
}
