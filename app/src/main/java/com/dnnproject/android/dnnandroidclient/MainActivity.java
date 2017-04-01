package com.dnnproject.android.dnnandroidclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private TextView serviceText;
    private Button serviceButton;
    private EditText ipEditText;
    private static boolean dnnServiceStarted = false;

//    private BroadcastReceiver receiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            Bundle bundle = intent.getExtras();
//            if (bundle != null) {
//                String string = bundle.getString(DownloadService.FILEPATH);
//                int resultCode = bundle.getInt(DownloadService.RESULT);
//                if (resultCode == RESULT_OK){
//                    Toast.makeText(MainActivity.this, "Download complete. Download URI: " +
//                        string, Toast.LENGTH_LONG).show();
//                    textView.setText("Downlaod done");
//                } else {
//                    Toast.makeText(MainActivity.this, "Download failed",
//                            Toast.LENGTH_LONG);
//                    textView.setText("Download failed");
//                }
//            }
//        }
//    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        serviceText = (TextView) findViewById(R.id.status_text);
        serviceButton = (Button) findViewById(R.id.service_button);
        ipEditText = (EditText) findViewById(R.id.ip_edit_box);

        this.setLayout();
    }

    public void onClick(View view) {
        Intent intent = new Intent(this, DNNService.class);
        if(dnnServiceStarted == false) {
            dnnServiceStarted = true;
            //add infos for the service which file to download and where to store
            //intent.putExtra(DownloadService.FILENAME, "david-david-hasselhoff-28106128-387-602.jpg");
            //intent.putExtra(DownloadService.URL, "http://images5.fanpop.com/image/photos/28100000/david-david-hasselhoff-28106128-387-602.jpg");
            startService(intent);
            Toast.makeText(this,getText(R.string.toast_start),Toast.LENGTH_SHORT).show();
        } else {
            dnnServiceStarted = false;
            //add infos for the service which file to download and where to store
            //intent.putExtra(DownloadService.FILENAME, "david-david-hasselhoff-28106128-387-602.jpg");
            //intent.putExtra(DownloadService.URL, "http://images5.fanpop.com/image/photos/28100000/david-david-hasselhoff-28106128-387-602.jpg");
            stopService(intent);
            Toast.makeText(this,getText(R.string.toast_stop),Toast.LENGTH_SHORT).show();
        }
        this.setLayout();

    }

    private void setLayout(){
        if(dnnServiceStarted == true){
            serviceButton.setText(getText(R.string.button_stop));
            serviceText.setText(getText(R.string.text_stop));
        } else {
            serviceButton.setText(getText(R.string.button_start));
            serviceText.setText(getText(R.string.text_start));
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
//    public native String stringFromJNI();

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }
}
