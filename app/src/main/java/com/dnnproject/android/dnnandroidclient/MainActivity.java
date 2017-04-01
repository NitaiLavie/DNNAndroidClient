package com.dnnproject.android.dnnandroidclient;

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
    private TextView ipTitleText;
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
        ipTitleText = (TextView) findViewById(R.id.ip_title_text);
        ipEditText = (EditText) findViewById(R.id.ip_edit_box);

        this.setLayout();
    }

    public void onClick(View view) {
        Intent intent = new Intent(this, DnnService.class);
        if(dnnServiceStarted == false) {
            dnnServiceStarted = true;
            // send the ip address to the DnnService
            intent.putExtra(DnnService.IP, ipEditText.getText().toString());

            startService(intent);
            Toast.makeText(this,getText(R.string.toast_start),Toast.LENGTH_SHORT).show();
        } else {
            dnnServiceStarted = false;
            stopService(intent);
            Toast.makeText(this,getText(R.string.toast_stop),Toast.LENGTH_SHORT).show();
        }
        this.setLayout();

    }

    private void setLayout(){
        if(dnnServiceStarted == true){
            serviceButton.setText(getText(R.string.button_stop));
            serviceText.setText(getText(R.string.text_stop));
            ipTitleText.setText(getText(R.string.current_ip_text));
            ipEditText.setEnabled(false);
        } else {
            serviceButton.setText(getText(R.string.button_start));
            serviceText.setText(getText(R.string.text_start));
            ipTitleText.setText(getText(R.string.enter_ip_text));
            ipEditText.setEnabled(true);
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
