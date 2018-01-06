package com.Jhinmugen.vspan.beproductive;


import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.Jhinmugen.vspan.beproductive.db.TaskContract;
import com.example.vspan.beproductive.BuildConfig;
import com.example.vspan.beproductive.R;
import com.Jhinmugen.vspan.beproductive.db.TaskDbHelper;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;



public class MainActivity extends AppCompatActivity {
    public static final String ITEM_VALUE_KEY = "ITEM VALUE KEY";
    public static final String IS_TURN_ON = "IS TURN ON";
    public static final String ADMOB_AD_ID = "ca-app-pub-9000375043824265/7577712164";


    private String turnOff = "Alarms Off";
    private String turnOn = "Alarms On";
    private boolean isTurnOn = false;
    private AudioManager audioManager;
    private NotificationManager notificationManager;
    private String itemSave;
    private String previousTextItem;
    private Button buttonTimeUsage;
    private Button tips;
    private Button counter;
    private Button toDo;
    private TaskDbHelper dbHelper;
    private TextView tasksToBeCompleted;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        MobileAds.initialize(this,ADMOB_AD_ID);
        AdView mAdView =  (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        if (savedInstanceState != null) {
            previousTextItem = savedInstanceState.getString(ITEM_VALUE_KEY);
            isTurnOn = savedInstanceState.getBoolean(IS_TURN_ON);
        }
        buttonTimeUsage = (Button) findViewById(R.id.button_time_usage);
        tips = (Button) findViewById(R.id.button_tips);
        counter = (Button) findViewById(R.id.button_counter);
        toDo = (Button) findViewById(R.id.button_to_do);
        tasksToBeCompleted = (TextView) findViewById(R.id.tasks_to_be_completed);


        buttonTimeUsage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionTimeUUsage();
            }
        });

        tips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTips();

            }
        });
        counter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCounter();
            }
        });
        toDo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTodo();
            }
        });

        int count = getTasksCount();

        String stringTasksToBeCompleted = count + " tasks left";
        tasksToBeCompleted.setText(stringTasksToBeCompleted);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if (previousTextItem != null) {
            MenuItem item = menu.findItem(R.id.action_turn_off);
            item.setTitle(previousTextItem);
        }
        return true;

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_about:
                showAbout();
                break;

            case R.id.action_turn_off:
                notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager.isNotificationPolicyAccessGranted()) {
                    audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    actionTurnOff();
                    updateMenuItem(item);
                } else {
                    Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                    startActivity(intent);
                }
                break;
            case R.id.feedback_button:
                final Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/html");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"beproductivefeedback@gmail.com"});
                intent.putExtra(Intent.EXTRA_SUBJECT, "Be PROductive Feedback");
                String info = "System info( App v. " + BuildConfig.VERSION_CODE + "Model " + Build.MODEL + "OS " + Build.VERSION.SDK_INT + ")\n";
                intent.putExtra(Intent.EXTRA_TEXT, info);
                startActivity(Intent.createChooser(intent, "Send some feedback"));


        }

        return super.onOptionsItemSelected(item);
    }

    public void showAbout() {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    public void showTodo() {
        Intent intent = new Intent(this, ActivityToDo.class);
        startActivity(intent);
    }

    public void showCounter() {
        Intent intent = new Intent(this, CounterActivity.class);
        startActivity(intent);
    }

    public void actionTimeUUsage() {
        Intent intent = new Intent(this, UsageStatistics.class);
        startActivity(intent);
    }


    public void actionTurnOff() {
        if (isTurnOn == false) {
            audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
        } else {
            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        }

    }

    public void showTips() {
        Intent intent = new Intent(this, ActivityTips.class);
        startActivity(intent);
    }


    private void updateMenuItem(MenuItem item) {
        if (isTurnOn == false) {
            item.setTitle(turnOn);
            isTurnOn = true;
            itemSave = item.getTitle().toString();
        } else {
            item.setTitle(turnOff);
            isTurnOn = false;
            itemSave = item.getTitle().toString();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ITEM_VALUE_KEY, itemSave);
        outState.putBoolean(IS_TURN_ON, isTurnOn);
    }


    public int getTasksCount() {
        String taskQuery = "SELECT * FROM " + TaskContract.TaskEntry.TABLE;
        dbHelper = new TaskDbHelper(this);
        SQLiteDatabase sqLiteDatabase = dbHelper.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery(taskQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }
}
