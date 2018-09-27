package io.hollings.cryptoportfolio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.Buffer;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

public class RemoteFetch {

    private static final String COINMARKETCAP_API =
            "https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/latest?limit=1";

    public static JSONArray getJSON(Context context){
        try {
            // Get response and parse the json
            URL url = new URL(COINMARKETCAP_API);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.addRequestProperty("X-CMC_PRO_API_KEY",
                    context.getString(R.string.cmc_pro_key));
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            StringBuffer json = new StringBuffer(4096);
            String tmp = "";
            while ((tmp=reader.readLine())!=null)
                json.append(tmp).append("\n");
            reader.close();
            JSONObject data = new JSONObject(json.toString());
//            Log.e("CryptoPortfolio","Remote Fetch" + data.toString());
            JSONObject statusObject = data.getJSONObject("status");
            JSONArray dataJSONArray = data.getJSONArray("data");
            int statusCode = statusObject.getInt("error_code");
//            Log.e("CryptoPortfolio","Remote Fetch" + statusObject.toString());
//            Log.e("CryptoPortfolio","Remote Fetch" + statusObject.getInt("error_code"));

            if((statusObject.getInt("error_code"))!=0){
                return null;
            }

            return dataJSONArray;

        }catch (Exception e) {
            Log.e("CryptoPortfolio","Remote Fetch Failed" + e.getMessage());

            return null;
        }

    }
}
