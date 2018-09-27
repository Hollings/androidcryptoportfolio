package io.hollings.cryptoportfolio;

import android.app.Activity;
import android.content.SharedPreferences;

import org.json.JSONArray;

import java.util.List;

public class Preference {
    SharedPreferences prefs;

    public Preference(Activity activity){
        prefs = activity.getPreferences(Activity.MODE_PRIVATE);
    }

    String getCrypto(){
        String[] crypto = {"BTC","ETH"};
        JSONArray jsonArray = new JSONArray();
        jsonArray.put("BTC");
        jsonArray.put("ETH");
        return prefs.getString("crypto",crypto.toString());
    }

    void setCrypto(String crypto){
        prefs.edit().putString("crypto",crypto).commit();
    }

}
