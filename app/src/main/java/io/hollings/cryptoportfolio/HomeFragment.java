package io.hollings.cryptoportfolio;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.zip.Inflater;


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
        UpdateCryptoData(new Preference(getActivity()).getCrypto(), rootView);

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    private void UpdateCryptoData(final String crypto, final View rootview){
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
                            renderCrypto(json, rootview);
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
    public static float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }
    private void renderCrypto(JSONArray json, View rootView){
        Log.e("CryptoPortfolio","Rendering Crypto");

        LinearLayout relativeLayout = rootView.findViewById(R.id.crypto_list_layout);
        LinearLayout.LayoutParams relativeLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        relativeLayoutParams.setMargins(15, (int)convertDpToPixel((float)10,getActivity()), 15, (int)convertDpToPixel((float)10,getActivity()));

//        ScrollView scrollView = rootView.findViewById(R.id.crypto_scroll_view);
//        ScrollView.LayoutParams scrollViewParams = new ScrollView.LayoutParams(ScrollView.LayoutParams.WRAP_CONTENT,
//                ScrollView.LayoutParams.WRAP_CONTENT);
        relativeLayout.removeAllViews();

        try {
            int x = 0;
            for (int i = 0; i < json.length(); i++) {

                JSONObject jsonobject = json.getJSONObject(i);
                JSONObject quote = jsonobject.getJSONObject("quote");
                JSONObject usd = quote.getJSONObject("USD");

                CardView crypto_card = (CardView) getLayoutInflater().inflate(R.layout.crypto_card, null);



                TextView title = crypto_card.findViewById(R.id.title);
                title.setText(jsonobject.getString("symbol") + " - " + jsonobject.getString("name"));

                TextView price = crypto_card.findViewById(R.id.price);
                price.setText("$"+Double.toString(round(usd.getDouble("price"),2)));

                TextView details = crypto_card.findViewById(R.id.details);
                details.setText("24h: " + Double.toString(round(usd.getDouble("percent_change_24h"),2))+"% | Market Cap: $"+round((double)(usd.getDouble("market_cap")/1000000000),2)+"b");

                crypto_card.setId(i);
                crypto_card.setLayoutParams(relativeLayoutParams);

                relativeLayout.addView(crypto_card);
                // Set Crypto Title
//                TextView crypto_title = (TextView) getLayoutInflater().inflate(R.layout.crypto_title, null);
//                crypto_title.setId(x++);
//                crypto_title.setLayoutParams(relativeLayoutParams);
//                crypto_title.setText(jsonobject.getString("name"));
//
//                // Set Crypto Price
//                TextView crypto_Price = (TextView) getLayoutInflater().inflate(R.layout.crypto_price, null);
//                crypto_Price.setId(x++);
//                crypto_Price.setLayoutParams(relativeLayoutParams);
//                crypto_Price.setText("$"+Double.toString(round(usd.getDouble("price"),2)));
//
//                //Set Crypto Percentage
//                TextView crypto_details = (TextView) getLayoutInflater().inflate(R.layout.crypto_details, null);
//                crypto_details.setId(x++);
//                crypto_details.setLayoutParams(relativeLayoutParams);
//                crypto_details.setText(Double.toString(round(usd.getDouble("percent_change_24h"),2))+"%");
//
//                if (usd.getDouble("percent_change_24h")<0.0){
//                    crypto_details.setTextColor(Color.parseColor("#B00020"));
//                    crypto_Price.setTextColor(Color.parseColor("#B00020"));
//                }else {
//                    crypto_details.setTextColor(Color.parseColor("#388E3C"));
//                    crypto_Price.setTextColor(Color.parseColor("#388E3C"));
//                }
//                relativeLayout.addView(crypto_title);
//                relativeLayout.addView(crypto_Price);
//                relativeLayout.addView(crypto_details);
            }

        }catch(Exception e){
            Log.e("CryptoPortfolio", "Home Fragment - " + e.getMessage());
        }
    }

}
