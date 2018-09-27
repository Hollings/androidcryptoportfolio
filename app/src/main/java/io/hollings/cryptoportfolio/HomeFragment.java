package io.hollings.cryptoportfolio;

import android.graphics.Color;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;


public class HomeFragment extends Fragment {

    TextView cryptoTitle;
    TextView cryptoPrice;
    TextView detailsField;
    LinearLayout mainLayout;

    Handler handler;

    public HomeFragment(){
        handler = new Handler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        cryptoTitle = (TextView)rootView.findViewById(R.id.crypto_title);
        cryptoPrice = (TextView)rootView.findViewById(R.id.crypto_price);
        detailsField = (TextView)rootView.findViewById(R.id.details_field);

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        UpdateCryptoData(new Preference(getActivity()).getCrypto());
    }

    private void UpdateCryptoData(final String crypto){
        new Thread(){
            public void run(){
                final JSONArray json = RemoteFetch.getJSON(getActivity());
                if(json == null){
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(getActivity(),
                                    getActivity().getString(R.string.crypto_not_found),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    handler.post(new Runnable() {
                        public void run() {
                            renderCrypto(json);
                        }
                    });
                }
            }
        }.start();
    }
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private void renderCrypto(JSONArray json){
        Log.e("CryptoPortfolio","Rendering Crypto");

        try {

            for (int i = 0; i < json.length(); i++) {

                JSONObject jsonobject = json.getJSONObject(i);
                Log.e("CryptoPortfolio", "Home Fragment -" + jsonobject.toString() + "-");
                cryptoTitle.setText(jsonobject.getString("name"));
                JSONObject quote = jsonobject.getJSONObject("quote");
                JSONObject usd = quote.getJSONObject("USD");
                Log.e("CryptoPortfolio","Remote Fetch" + usd.toString());
                cryptoPrice.setText("$"+Double.toString(round(usd.getDouble("price"),2)));
                detailsField.setText(Double.toString(round(usd.getDouble("percent_change_24h"),2))+"%");
                if (usd.getDouble("percent_change_24h")<0.0){
                    detailsField.setTextColor(Color.parseColor("#B00020"));
                    cryptoPrice.setTextColor(Color.parseColor("#B00020"));

                }else {
                    detailsField.setTextColor(Color.parseColor("#388E3C"));
                    cryptoPrice.setTextColor(Color.parseColor("#388E3C"));

                }
            }

        }catch(Exception e){
            Log.e("CryptoPortfolio", "Home Fragment - " + e.getMessage());
        }
    }

}
