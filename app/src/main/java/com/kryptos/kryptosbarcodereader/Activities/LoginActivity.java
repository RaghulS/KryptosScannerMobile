package com.kryptos.kryptosbarcodereader.Activities;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.kryptos.kryptosbarcodereader.R;
import com.kryptos.kryptosbarcodereader.Utilities.HideSoftKeyboard;

public class LoginActivity extends ActionBarActivity {

    private EditText mUsername, mPassword;

    private Button mLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_login);

        mUsername = (EditText) findViewById(R.id.screen_login_username);

        mPassword = (EditText) findViewById(R.id.screen_login_password);

        mLoginButton = (Button) findViewById(R.id.screen_login_loginbtn);

        HideSoftKeyboard.setupUI(this.getWindow().getDecorView().findViewById(android.R.id.content),this);

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mUsername.getText().toString().equals("admin") && (mPassword.getText().toString().equals("admin"))) {
                    StartLogin();
                } else {
                    Toast.makeText(getApplicationContext(), "Use admin as username & password", Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    private void StartLogin() {

        Intent aLoginIntent = new Intent(LoginActivity.this, HomeActivity.class);
        startActivity(aLoginIntent);
        finish();
    }

}
