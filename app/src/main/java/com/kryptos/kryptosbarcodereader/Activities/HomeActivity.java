package com.kryptos.kryptosbarcodereader.Activities;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.kryptos.kryptosbarcodereader.R;
import com.kryptos.kryptosbarcodereader.Utilities.KryptosPreference;

public class HomeActivity extends ActionBarActivity {

    private KryptosPreference myPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_home);

        myPreference = new KryptosPreference(this);
    }

    public void OnSettingsClick(View aView){

        Intent aIntent = new Intent(HomeActivity.this, SettingsActivity.class);
        startActivity(aIntent);

    }

    public void OnHomeButtonClick(View aView) {

        Intent aIntent = new Intent(HomeActivity.this, ScannerActivity.class);
        aIntent.putExtra("PURPOSE", "scanner");
        startActivity(aIntent);
    }

    public void OnExitButtonClick(View aView) {

        CallExitDialog();
    }

    private void CallExitDialog() {

        final Dialog aDialog = new Dialog(this);
        aDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        aDialog.setContentView(R.layout.dialog_exit_app_alert);

        Button aOkbutton = (Button) aDialog.findViewById(R.id.dialog_ok_button);

        Button aCancelbutton = (Button) aDialog.findViewById(R.id.dialog_cancel_button);


        aOkbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                aDialog.dismiss();

                myPreference.ClearPrefs();

                HomeActivity.this.finish();

            }
        });


        aCancelbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                aDialog.dismiss();
            }
        });

        aDialog.show();

    }

    @Override
    public void onBackPressed() {

        CallExitDialog();
    }
}
