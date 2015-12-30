package com.kryptos.kryptosbarcodereader.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.kryptos.kryptosbarcodereader.R;
import com.kryptos.kryptosbarcodereader.Utilities.KryptosPreference;

public class SettingsActivity extends ActionBarActivity {

    View.OnClickListener SaveButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (myIpAddressEDT.length() > 0) {

                mySession.setIpConfig(myIpAddressEDT.getText().toString());              

                Toast.makeText(SettingsActivity.this, "IP address successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {

                Toast.makeText(SettingsActivity.this, "Empty Values not allowed", Toast.LENGTH_SHORT).show();
            }

        }
    };
    View.OnClickListener aStartSettingsScannerListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            Intent i = new Intent(SettingsActivity.this, ScannerActivity.class);
            
            i.putExtra("PURPOSE", "setting");
            startActivity(i);


        }
    };
    private EditText myIpAddressEDT;
    private Button mySaveButton;
    private ImageButton myStartScannerBtn;
    private String myIPAddress;
    private KryptosPreference mySession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_settings);


        Intent i = getIntent();

        if (i != null) {

            myIPAddress = i.getStringExtra("IP_CONFIG");
        }

        mySession = new KryptosPreference(SettingsActivity.this);

        myIpAddressEDT = (EditText) findViewById(R.id.screen_settings_ip_address_EDT);

        myStartScannerBtn = (ImageButton) findViewById(R.id.screen_settings_start_scanner);

        mySaveButton = (Button) findViewById(R.id.screen_settings_save_button);

        mySaveButton.setOnClickListener(SaveButtonListener);

        myStartScannerBtn .setOnClickListener(aStartSettingsScannerListener);


        if (myIPAddress != null) {
        	
        	

            myIpAddressEDT.setText(myIPAddress);

            myIpAddressEDT.setEnabled(false);

            myIpAddressEDT.setFocusable(false);
        }

    }

}
