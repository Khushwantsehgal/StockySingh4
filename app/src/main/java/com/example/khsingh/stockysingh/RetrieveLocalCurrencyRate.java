package com.example.khsingh.stockysingh;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by khsingh on 5/12/15.
 */
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