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
import android.system.ErrnoException;
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
import org.xml.sax.ErrorHandler;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.zip.Inflater;


public class HomeFragment extends Fragment {


    Handler handler;
    public HomeFragment(){
        handler = new Handler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

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
                final JSONArray json = RemoteFetch.CmcJson(getActivity());
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
    private void addTotalCard(LinearLayout linearLayout,LinearLayout.LayoutParams layoutParams){
        CardView crypto_card = (CardView) getLayoutInflater().inflate(R.layout.crypto_card, null);

        TextView title = crypto_card.findViewById(R.id.title);
        title.setText("Total Portfolio Value");

        TextView price = crypto_card.findViewById(R.id.price);
        price.setText("$"+Double.toString(HomeFragment.round(PortfolioFragment.totalPortfolioValue,2)));

        TextView details = crypto_card.findViewById(R.id.details);
        details.setVisibility(View.GONE);

        crypto_card.setId(0);
        crypto_card.setLayoutParams(layoutParams);

        linearLayout.addView(crypto_card);
    }
    private void addCryptoCard(JSONArray json,LinearLayout linearLayout,LinearLayout.LayoutParams layoutParams, int i){
        try{
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
            crypto_card.setLayoutParams(layoutParams);

            linearLayout.addView(crypto_card);
        }catch (Exception e){
            Log.e("CryptoPortfolio", "Home Fragment - " + e.getMessage());
        }
    };

    private void renderCrypto(JSONArray json, View rootView){
        Log.e("CryptoPortfolio","Rendering Crypto");

        LinearLayout linearLayout = rootView.findViewById(R.id.crypto_list_layout);
        LinearLayout.LayoutParams relativeLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        relativeLayoutParams.setMargins(15, (int)convertDpToPixel((float)10,getActivity()), 15, (int)convertDpToPixel((float)10,getActivity()));

        linearLayout.removeAllViews();
        addTotalCard(linearLayout,relativeLayoutParams);
        try {
            for (int i = 0; i < json.length(); i++) {
                addCryptoCard(json,linearLayout,relativeLayoutParams,i);
            }
        }catch(Exception e){
            Log.e("CryptoPortfolio", "Home Fragment - " + e.getMessage());
        }
    }

}
