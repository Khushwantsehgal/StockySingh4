package com.example.khsingh.stockysingh;

import android.app.Activity;
import android.app.Notification;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.cocosw.bottomsheet.BottomSheet;

/**
 * Created by khsingh on 6/6/15.
 */
public class ReminderActivity extends ActionBarActivity implements View.OnClickListener{
    Button mReminderButton;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reminder_blue);
        initviews();


    }

    private void initviews(){
        mReminderButton = (Button) findViewById(R.id.bt_reminder);
        mReminderButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_reminder:{
                new BottomSheet.Builder(this).title("Remind me, When?").sheet(R.menu.reminder_bottom_sheet).listener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case R.id.bt_CalendarReachesADefinedDate: {
                                Log.d("Debug", "Debug" + "bt_CalendarReachesADefinedDate");
                            }
                            break;
                            case R.id.bt_StockPriceReachesALimit: {
                                Log.d("Debug", "Debug" + "bt_StockPriceReachesALimit");
                            }
                            break;
                            case R.id.bt_FinalAmountIReceiveReachesALimit: {
                                Log.d("Debug", "Debug" + "bt_StockPriceReachesALimit");
                            }
                            break;
                        }
                    }
                }).show();

            }
        }

    }
}
