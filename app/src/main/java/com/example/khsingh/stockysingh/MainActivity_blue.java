package com.example.khsingh.stockysingh;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.TextView;

/**
 * Created by khsingh on 5/31/15.
 */
public class MainActivity_blue extends ActionBarActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_blue);
        initViews();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    void initViews(){
        Marquee();
    }//initview ends

    void Marquee(){

        new Thread(new Runnable() {
            public void run() {
                TextView textView = (TextView) findViewById(R.id.MarqueeText);
                textView.setText("ADBE 79.04    INR 63.75   Brokage USD20   Wire Transfer USD25 ");
                textView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                textView.setSelected(true);
                textView.setSingleLine(true);
            }
        }).start();


    }//Marquee Ends




}//
