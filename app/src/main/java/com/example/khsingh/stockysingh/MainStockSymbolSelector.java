package com.example.khsingh.stockysingh;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import java.util.StringTokenizer;

/**
 * Created by khsingh on 4/28/2015.
 */
public class MainStockSymbolSelector extends ActionBarActivity {

    ListView mStockSymbol;
    String message, first, second;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stocksymbolselector);
        initViews();

        String[] StockSymbol = getResources().getStringArray(R.array.USA_National_Stock_Exchange);
        ArrayAdapter<String> a_StockSymbol = new ArrayAdapter<String>(this, R.layout.layout_for_stock_symbols, StockSymbol);
        mStockSymbol.setAdapter(a_StockSymbol);

        mStockSymbol.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try{
                    message = mStockSymbol.getItemAtPosition(position).toString();
                    StringTokenizer tokens = new StringTokenizer(message, " ");
                    first = tokens.nextToken();// this will contain "Fruit"
                    second = tokens.nextToken();
                    Toast.makeText(getApplication(), first, Toast.LENGTH_LONG).show();
                }catch (Exception e){
                    Toast.makeText(getApplication(), "Exception Occurred", Toast.LENGTH_LONG).show();
                }
                Intent intent = new Intent(MainStockSymbolSelector.this, MainActivity.class);
                intent.putExtra("MESSAGE", first);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });



    }

    private void initViews() {
        mStockSymbol = (ListView) findViewById(R.id.lv_StockSymbol);


    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        getMenuInflater().inflate(R.menu.stockselector_activity_actions, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        android.support.v7.widget.SearchView searchView = (android.support.v7.widget.SearchView) MenuItemCompat.getActionView(searchItem);
        return super.onCreateOptionsMenu(menu);
    }
}
