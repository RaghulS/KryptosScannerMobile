package com.kryptos.kryptosbarcodereader.Utilities;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Raghul.S
 */
public class KryptosPreference {

    SharedPreferences myPreference;
    SharedPreferences.Editor myEditor;
    public static final String MyPREFERENCES = "MyPrefs";
    public static final String IP_CONFIG = "ip_address";

    public KryptosPreference(Context context) {

        myPreference = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        myEditor = myPreference.edit();

    }

    public void ClearPrefs(){

        myEditor.putString(IP_CONFIG, "");
    }

    public boolean setIpConfig(String aIpaddress) {

        myEditor.putString(IP_CONFIG, aIpaddress);
       return myEditor.commit();

    }

    public String getIpConfig() {

        return "tcp://"+myPreference.getString(IP_CONFIG, "")+":8060";
    }

}
