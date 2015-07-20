package com.example.khsingh.stockysingh;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.app.ActionBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.concurrent.ExecutionException;

/**
 * The application calculates, on selling the stocks, the actual amount to be remitted into account after deducting various fees and charges.
 */

public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    //Edittext for  Number of Stocks and Stock Trader
    EditText mStock;

    //Textview for actual amount to be remitted into account, the Currency code for the final amount/
    TextView mFinalAmount, mCurrencyCode,mStockSymbol, mCurrencyCode2,mReminder;

    //Integer to hold the number of stocks entered by user
    int mStockValue = 0;

    //Double to specify the current trading price of the stock, exchange rate for selected currency, and the final amount after conversion from USD to local currency
    Double mStockTradingAt = 0.00, mLocalCurrencyExchangeRate = 64.0415, mAmountInLocalCurrency;

    //Strings to set the value of stocks and currency symbols saved when the app was closed last time.
    String StockSelected,CurrencySelected;

    //Whether the correct currency and stock symbols code are returned from the selection activity
    final int REQ_CODE_CURRENCY_SYMBOL = 2, REQ_CODE_STOCK_SYMBOL=3;

    // Name of the Preference file. The file saves the number of stocks, stock symbol, and currency code selected by a user.
    public static final String PREFS_NAME = "StockySinghPreferenceFiles";

    private final int TRIGGER_CALCULATION = 1;

    // Where did 1000 come from? It's arbitrary, since I can't find average android typing speed.
    private final long CALCULATION_TRIGGER_DELAY_IN_MS = 1000;



    private HandlerThread mHandlerThread;

    private ProgressBar mProgress;
    private int progressStatus = 0;
    private Handler handler2 = new Handler();

    //Yahoo Finance Query to extract data for ADBE
    private String mYQLQuery2;
    private static String mYQLQuery;
    private static String mYQLQueryToGetUSDtoLocalCurrencyConversion;
    private String mYQLQuery_head = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.quote%20where%20symbol%20in%20%28%22";
    private String mYQLQuery_tail  =        "%22%29&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=";
    //private static String mYQLQuery = "http://api.androidhive.info/contacts/";

    private String mYQLQueryToGetUSDtoLocalCurrencyConversion_head = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.xchange%20where%20pair%20in%20(%22USD%22%2C%20%22";
    private String mYQLQueryToGetUSDtoLocalCurrencyConversion_tail = "%22)&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=";
    @Override

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setInitialPreferences();

        initViews();
        // Calculate the final amount
        mStock.addTextChangedListener(watcher);
        mCurrencyCode.addTextChangedListener(watcher);
        mCurrencyCode2.addTextChangedListener(watcher);
        mStockSymbol.addTextChangedListener(watcher);

        // Sends the KeyEvent Action Down and it causes the calculation to happen for stored value of mStock. It is only for the value stored in preferences
        mStock.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_0 ));
        mStock.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_0 ));
        // removes the 0 added by last line
        mStock.setText((String.valueOf(mStockValue)).replaceFirst("^0+(?!$)", ""));

        // Opens a new activity to select currency codes and stockSymbols
        mCurrencyCode.setOnClickListener(this);
        mCurrencyCode2.setOnClickListener(this);
        mStockSymbol.setOnClickListener(this);
        mReminder.setOnClickListener(this);
        ShowTicker();


    } // onCreate Ends

    private void initViews() {


        mStock = (EditText) findViewById(R.id.et_Stocks);
        mFinalAmount = (TextView) findViewById(R.id.FinalAmount_textview);
        mCurrencyCode = (TextView) findViewById(R.id.tv_CurrencyCode);
        mCurrencyCode2 = (TextView) findViewById(R.id.tv_CurrencyCode2);
        mReminder = (TextView) findViewById(R.id.tv_Reminder);
        mStockSymbol = (TextView) findViewById(R.id.tv_StocksSymbol);
        mProgress = (ProgressBar) findViewById(R.id.progress_bar);
        //mProgress.setVisibility(View.GONE);


        // Reads the default values from preferences and passes the value to corresponding fields.
         SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

         mStockValue = settings.getInt("StockCount", 0);
         Log.d("StockCount", String.valueOf(mStockValue));
         mStock.setText((String.valueOf(mStockValue)).replaceFirst("^0+(?!$)", ""));

         StockSelected = settings.getString("StockSelected", "ADBE");
         setPreferences("StockSelected", StockSelected);
         Log.d("StockSelected", String.valueOf(StockSelected));
         mStockSymbol.setText(String.valueOf(StockSelected));

         CurrencySelected = settings.getString("CurrencySelected", "INR");
         setPreferences("CurrencySelected", CurrencySelected);
         Log.d("CurrencySelected", String.valueOf(CurrencySelected));
         mCurrencyCode.setText(String.valueOf(CurrencySelected));
         mCurrencyCode2.setText(String.valueOf(CurrencySelected));


        mYQLQuery = mYQLQuery_head + String.valueOf(StockSelected) + mYQLQuery_tail;
        mYQLQueryToGetUSDtoLocalCurrencyConversion = mYQLQueryToGetUSDtoLocalCurrencyConversion_head + String.valueOf(CurrencySelected) + mYQLQueryToGetUSDtoLocalCurrencyConversion_tail;

      //  ShowTicker();

    } //init views ends



// Handle to avoid avoid multiple triggers on EditText while user is typing.

    private Handler handler = new Handler();
//        @Override
//        public void handleMessage(Message msg) {
//            if (msg.what == TRIGGER_CALCULATION) {
//                TriggerCalculation();
//
//
//            }
//        }

    // Receives result from CurrencyCode/StockSymbol activities and sets the value for corresponding fields.
    TextWatcher watcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
           // TriggerCalculation();


        }

        @Override
        public void afterTextChanged(Editable s) {
            handler.removeCallbacks(mlauncher);
            //handler.removeMessages(TRIGGER_CALCULATION);
            handler.postDelayed(mlauncher,500);
            mProgress.setVisibility(View.VISIBLE);
            //handler.sendEmptyMessageDelayed(TRIGGER_CALCULATION, CALCULATION_TRIGGER_DELAY_IN_MS);

        }
    };// Text watcher ends

    private Runnable mlauncher =  new Runnable() {
        @Override
        public void run() {

            TriggerCalculation();
            mProgress.setVisibility(View.GONE);


        }
    };

    private void TriggerCalculation() {
        Log.d("Debug", "onTextChanged reached");

        try{

            if (!isEmpty(mStock)) {
                mStockValue = Integer.parseInt(mStock.getText().toString());


            } else {
                Log.d("Debug","isEmpty: The mStock is empty and set to 0");
                mStockValue = 0;

            }
            setPreferences();



        } catch (Exception e){
            e.printStackTrace();
        }finally
        {
            try {
                Boolean networkStatus = isNetworkOnline();
                if(networkStatus){
                    mStockTradingAt = new RetrieveLastTradePriceOnly().execute(mYQLQuery).get();
                    setPreferences("StockTradingAt", mStockTradingAt.toString());

                    mLocalCurrencyExchangeRate = new RetrieveLocalCurrencyRate().execute(mYQLQueryToGetUSDtoLocalCurrencyConversion).get();
                    setPreferences("LocalCurrencyExchangeRate", mLocalCurrencyExchangeRate.toString());
                    ShowTicker();

                    Log.d("Debug"," MainActivity" + mLocalCurrencyExchangeRate);
                } else
                {
                    Log.d("Debug"," NetworkDown");

                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            mAmountInLocalCurrency = ((double) mStockValue * mStockTradingAt) * mLocalCurrencyExchangeRate;


            DecimalFormat df = new DecimalFormat("#.##");
            if (df instanceof DecimalFormat) {
                ((DecimalFormat) df).setMinimumFractionDigits(2);
                mFinalAmount.setText((df.format(mAmountInLocalCurrency)).toString());
            }



        }
    }

    //Evaluates the result returned by StockSymbol and Currency Selection activities
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("Debug","requestCode: " + requestCode);
        Log.d("Debug", "resultCode: " + resultCode);

        if (requestCode == REQ_CODE_CURRENCY_SYMBOL && Activity.RESULT_OK == resultCode) {
            String message = data.getStringExtra("MESSAGE");
            mYQLQueryToGetUSDtoLocalCurrencyConversion = mYQLQueryToGetUSDtoLocalCurrencyConversion_head + message + mYQLQueryToGetUSDtoLocalCurrencyConversion_tail;
            Log.d("Debug", "mYQLQueryToGetUSDtoLocalCurrencyConversion: " + mYQLQueryToGetUSDtoLocalCurrencyConversion);
            mCurrencyCode.setText(message);
            mCurrencyCode2.setText(message);
            setPreferences("CurrencySelected",message);
            ShowTicker();



        }else if(requestCode == REQ_CODE_STOCK_SYMBOL && Activity.RESULT_OK == resultCode){
            String message = data.getStringExtra("MESSAGE");
            mYQLQuery = mYQLQuery_head + message + mYQLQuery_tail;
            mStockSymbol.setText(message);
            setPreferences("StockSelected", message);
            ShowTicker();
            Log.d("Debug", "mYQLQuery: " + mYQLQuery);

        }
    } // onActivityResult ends


    // validates if an EditText is empty
    private boolean isEmpty(EditText etText) {
        Log.d("Debug", "isEmpty Length of stocks:" + etText.getText().toString().trim().length());
        return etText.getText().toString().trim().length() == 0;
    }

    // Starts the Activities to select currency code and stock symbol
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.tv_CurrencyCode: {
                Intent intent = new Intent(MainActivity.this, MainCurrencyCodeSelector.class);
                startActivityForResult(intent, REQ_CODE_CURRENCY_SYMBOL);

            }
            break;
            case R.id.tv_StocksSymbol: {
                Intent intent = new Intent(MainActivity.this, MainStockSymbolSelector.class);
                startActivityForResult(intent,REQ_CODE_STOCK_SYMBOL);

            }
            break;
            case R.id.tv_CurrencyCode2:{
                Intent intent = new Intent(MainActivity.this, MainCurrencyCodeSelector.class);
                startActivityForResult(intent, REQ_CODE_CURRENCY_SYMBOL);
                Log.d("Debug", "tv_CurrencyCode2");

            }
            break;
            case R.id.tv_Reminder:{
                Intent intent = new Intent(MainActivity.this, ReminderActivity.class);
                startActivity(intent);
                Log.d("Debug", "tv_setReminder");
            }

            default:
            {
                Log.d("Debug", "onClick(View v): No view selected yet");
            }

        }
    }//OnClick Ends

    // Saves the value in preferences when the application stops
    @Override
    protected void onStop() {
        super.onStop();

        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        Log.d("Stop","onStop Called");
        setPreferences();
    }



    class RetrieveLastTradePriceOnly extends AsyncTask<String, Void, Double> {


        String mLastTradePriceOnly;
        private Exception exception;

        protected Double doInBackground(String... URL) {

            StockyJSONParser stockyJSONParser = new StockyJSONParser();
            JSONObject jsonObject = stockyJSONParser.getJSONFromURL(URL[0]);
            if(!jsonObject.isNull("query")){
                try{
                    JSONObject mQuery = jsonObject.getJSONObject("query");
                    JSONObject mResult = mQuery.getJSONObject("results");
                    JSONObject mQuote = mResult.getJSONObject("quote");
                    mLastTradePriceOnly = mQuote.getString("LastTradePriceOnly");

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }else {
                Log.d("Debug","jsonObject.isNull " + String.valueOf(mLastTradePriceOnly ));

            }

            Log.d("Debug",String.valueOf(mLastTradePriceOnly ));

            return Double.parseDouble(mLastTradePriceOnly);
        }


        protected void onPostExecute(Double result) {
            // TODO: check this.exception
            // TODO: do something with the feed



        }
    }


    class RetrieveLocalCurrencyRate extends AsyncTask<String, Void, Double> {

        String mlocalCurrencyTradingAT;
        private Exception exception;

        protected Double doInBackground(String... URL) {

            StockyJSONParser stockyJSONParser = new StockyJSONParser();
            JSONObject jsonObject = stockyJSONParser.getJSONFromURL(URL[0]);
            try{
                JSONObject mQuery = jsonObject.getJSONObject("query");
                JSONObject mResult = mQuery.getJSONObject("results");
                JSONArray rate = mResult.getJSONArray("rate");
                JSONObject localCurrencyTradingAT = rate.getJSONObject(1);
                mlocalCurrencyTradingAT = localCurrencyTradingAT.getString("Rate");

            } catch (JSONException e) {
                e.printStackTrace();
            }


            Log.d("mlocalCurrencyTradingAT",String.valueOf(mlocalCurrencyTradingAT ));

            return Double.parseDouble(mlocalCurrencyTradingAT);
        }


        protected void onPostExecute(Double result) {
            // TODO: check this.exception
            // TODO: do something with the feed


        }
    }

    void ShowTicker(){


            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();

            TextView textView = (TextView) findViewById(R.id.MarqueeText);

            textView.setText(settings.getString("StockSelected", "#Error retrieving value#") + getString(R.string.tab) + settings.getString("StockTradingAt", "#Error retrieving value#") + getString(R.string.tab) + getString(R.string.tab) + getString(R.string.tab) + getString(R.string.tab) + settings.getString("CurrencySelected", "#Error retrieving value#") + getString(R.string.tab) + settings.getString("LocalCurrencyExchangeRate", "#Error retrieving value#") + "    Brokage USD20   Wire Transfer USD25 ");


            textView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            textView.setSelected(true);

        }



    public boolean isNetworkOnline() {
        boolean status=false;
        try{
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getNetworkInfo(0);
            if (netInfo != null && netInfo.getState()==NetworkInfo.State.CONNECTED) {
                status= true;
            }else {
                netInfo = cm.getNetworkInfo(1);
                if(netInfo!=null && netInfo.getState()==NetworkInfo.State.CONNECTED)
                    status= true;
            }
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return status;

    }

    public void setInitialPreferences(){
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        SharedPreferences wmbPreference = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isFirstRun = wmbPreference.getBoolean("FIRSTRUN", true);
        if (isFirstRun)
        {
            // Code to run once
            Log.d("Debug: isFirstRun"," isFirstRun code is running");
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt("StockCount", 0);
            editor.putString("StockTradingAt", "0");
            editor.putString("LocalCurrencyExchangeRate", "0");
            editor.putString("CurrencySelected", "INR");
            editor.putString("StockSelected", "ADBE");
        }


    }
    public void setPreferences() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();

        if (!isEmpty(mStock)){
            editor.putInt("StockCount", Integer.parseInt(mStock.getText().toString()));
            Log.d("Debug", "onStop StockCount" + Integer.parseInt(mStock.getText().toString()));
        }else{
            editor.putInt("StockCount", 0);
            Log.d("Debug","onStop StockCount is empty so set to " + "0");
        }
        // Commit the edits!
        editor.commit();
    }

        public void setPreferences(String key, String value) {
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();

            editor.putString(key, value);

            Log.d("Debug", "Key and Value" + key + value);
            Log.d("Debug","onStop CurrencySelected" + mCurrencyCode.getText().toString());


            // Commit the edits!
            editor.commit();
        }





}//MainActivity Class ends here function e




