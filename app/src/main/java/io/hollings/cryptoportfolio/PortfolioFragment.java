package io.hollings.cryptoportfolio;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;


public class PortfolioFragment extends Fragment {

    SharedPreferences spref;
    SharedPreferences.Editor prefEditor;
    public static Double totalPortfolioValue = 0.0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_portfolio, container, false);
        Log.e("Portfolio Fragment", "Portfolio JSON");
        spref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        prefEditor = spref.edit();
        UpdatePortfolioData(new Preference(getActivity()).getCrypto(), rootView);

//        final FloatingActionButton myFab = (FloatingActionButton) rootView.findViewById(R.id.edit_fab);
//        myFab.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                save(rootView, myFab);
//            }
//        });

        return rootView;
    }

    public static ArrayList<View> getViewsByTag(ViewGroup root, String tag) {
        ArrayList<View> views = new ArrayList<View>();
        final int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = root.getChildAt(i);
            if (child instanceof ViewGroup) {
                views.addAll(getViewsByTag((ViewGroup) child, tag));
            }

            final Object tagObj = child.getTag();
            if (tagObj != null && tagObj.equals(tag)) {
                views.add(child);
            }

        }
        return views;
    }

    private void save(String symbol, String amount){
        Log.e("saving","saving"+ symbol +" as " + amount);
        prefEditor.putString(symbol,amount);
        prefEditor.commit();
    }

    private String load(String symbol){

        String portfolioAmount = spref.getString(symbol, "0.0");
        return portfolioAmount;
    }

    Handler handler;
    public PortfolioFragment(){
        handler = new Handler();
    }

    public double something(View rootview){
        return getTotalPortfolioValue(rootview);
    }

    private double getTotalPortfolioValue(View rootview){
        Double total = 0.0;
        JSONArray json = RemoteFetch.CmcJson(getActivity());
        try {
            for (int i = 0; i < json.length(); i++) {

                final JSONObject jsonobject = json.getJSONObject(i);
                JSONObject quote = jsonobject.getJSONObject("quote");
                JSONObject usd = quote.getJSONObject("USD");
                String symbol = jsonobject.getString("symbol");
                String amount = load(jsonobject.getString("symbol"));
                total += getCryptoAmountInUsd(json,symbol,Double.parseDouble(amount));
            }
        }catch(Exception e){
            Log.e("CryptoPortfolio", "Home Fragment 118 - " + e.getMessage());
        }
        Log.e("total portfolio",total.toString());
        return total;
    };

    private void UpdatePortfolioData(final String crypto, final View rootview){
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
                            totalPortfolioValue = getTotalPortfolioValue(rootview);
                            renderPortfolio(json, rootview);
                        }
                    });
                }
            }
        }.start();
    }

    private static double getCryptoAmountInUsd(JSONArray json, String symbol, double amount){
        try{
            for (int i = 0; i < json.length(); i++) {
                JSONObject jsonobject = json.getJSONObject(i);
                JSONObject quote = jsonobject.getJSONObject("quote");
                JSONObject usd = quote.getJSONObject("USD");

                if (jsonobject.getString("symbol")==symbol){
                    double price = usd.getDouble("price");
                    double totalPrice = price * amount;
                    return totalPrice;
                }
            }
        }catch (Exception e){
            Log.e("CryptoPortfolio", "Home Fragment 73 - " + e.getMessage());
        }
        return 0.0;


    }
    private void addPortfolioCard(final JSONArray json, LinearLayout linearLayout, LinearLayout.LayoutParams layoutParams, int i){
        try{
            final JSONObject jsonobject = json.getJSONObject(i);
            JSONObject quote = jsonobject.getJSONObject("quote");
            JSONObject usd = quote.getJSONObject("USD");
            String symbol = jsonobject.getString("symbol");
            String amount = load(jsonobject.getString("symbol"));
            CardView crypto_card = (CardView) getLayoutInflater().inflate(R.layout.portfolio_card, null);

            TextView title = crypto_card.findViewById(R.id.title);
            title.setText(symbol + " - " + jsonobject.getString("name"));
            final EditText amountEdit = crypto_card.findViewById(R.id.amount_edit);

            amountEdit.setText(amount.toString());
            TextView total_in_usd = crypto_card.findViewById(R.id.total_in_usd);

            total_in_usd.setText("$" + Double.toString(HomeFragment.round(getCryptoAmountInUsd(json,symbol,Double.parseDouble(amount)), 2)));

            crypto_card.setId(i);
            crypto_card.setLayoutParams(layoutParams);

            linearLayout.addView(crypto_card);

            amountEdit.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {


                    ViewGroup card = (ViewGroup)amountEdit.getParent().getParent();
                    ArrayList<View> editTexts = getViewsByTag((ViewGroup) card,"calculated_price");

                    String portfolioAmount = s.toString();
                    String symbol;
                    Double amount;

                    try{
                        symbol = jsonobject.getString("symbol");
                        amount = Double.parseDouble(portfolioAmount);
                    }catch (Exception e){
                        symbol = "none";
                        amount = 0.0;
                    }

                    for (int i=0;i<editTexts.size();i++){
                        Log.e("asdf", Integer.toString(i));
                            Log.e("onTextChanged", portfolioAmount);
                            try{

                                ((TextView)editTexts.get(i)).setText("$" + Double.toString(HomeFragment.round(getCryptoAmountInUsd(json,symbol,amount), 2)));
                                save(symbol,portfolioAmount);

                            }catch (Exception e){
                                Log.e("asdf",e.getMessage());
                            }
                            break;
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });


        }catch (Exception e){
            Log.e("CryptoPortfolio", "Home Fragment 99  - " + e.getMessage());
        }
    };

    private void renderPortfolio(JSONArray json, View rootView){
        Log.e("CryptoPortfolio","Rendering Crypto");

        LinearLayout linearLayout = rootView.findViewById(R.id.crypto_list_layout);
        LinearLayout.LayoutParams relativeLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        relativeLayoutParams.setMargins(15, (int)HomeFragment.convertDpToPixel((float)10,getActivity()), 15, (int)HomeFragment.convertDpToPixel((float)10,getActivity()));

        linearLayout.removeAllViews();

        try {
            for (int i = 0; i < json.length(); i++) {
                addPortfolioCard(json,linearLayout,relativeLayoutParams,i);
            }
        }catch(Exception e){
            Log.e("CryptoPortfolio", "Home Fragment 118 - " + e.getMessage());
        }
    }




}
