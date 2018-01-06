package com.Jhinmugen.vspan.beproductive;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.example.vspan.beproductive.R;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.concurrent.TimeUnit;
import static java.lang.Math.log;


public class ActivityMoreStatistics extends AppCompatActivity implements Serializable {
    private TextView leastUsedApp;
    private TextView mostUsedApp;
    private TextView totalTimeUsedPhone;
    private TextView totalTimeUsedOnApp;
    private TextView percentageOfTotalTimeUsedOnPhone;
    private TextView percentageOfTotalTimeUsedOnApp;


    private ArrayList<String> applicationNames = new ArrayList<>();
    private ArrayList<Long> applicationRunTimes = new ArrayList<>();
    private ArrayList<String> applicationNamesNoDuplicates = new ArrayList<>();
    private ArrayList<Long> applicationRunTimesNoDuplicates = new ArrayList<>();
    private long maxForegroundTime;
    private long minForegroundTime;
    private long currentForegroundTime;
    private String currentAppName;
    private long lastTimeUsedFirst;
    private long lastTimeUsedLast;
    private long timeSpan;
    private String currentAppNameToDisplay;
    private long sum = 0;
    private String totalTime;
    private String totalTimeSpentOnApp;
    private String valueOfTotalTimePercentageContent;
    private String strippedMinAppName;
    private String strippedMaxAppName;
    private String valueOfAppTimePercentageContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_statistics);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        applicationNames = intent.getStringArrayListExtra("nameList");
        applicationRunTimes = (ArrayList<Long>) intent.getSerializableExtra("foreground");
        lastTimeUsedFirst = intent.getLongExtra("first last time used", 0);
        lastTimeUsedLast = intent.getLongExtra("last last time used", 0);

        initViews();


        //Creating multimap that has the connection between time in foreground and app name
        Multimap<String, Long> myMultiMap = ArrayListMultimap.create();
        for (int i = 0; i < applicationRunTimes.size(); i++) {
            myMultiMap.put(applicationNames.get(i), applicationRunTimes.get(i));
        }


        //Making two lists that have the total foreground time and the package name they are "connected"
        for (String value : myMultiMap.keys()) {
            for (long i : myMultiMap.get(value)) {
                sum += i;
            }
            applicationNamesNoDuplicates.add(value);
            applicationRunTimesNoDuplicates.add(sum);
            sum = 0;
        }

        minForegroundTime = getMinForegroundTime(applicationRunTimesNoDuplicates);

        //removing zero values from both lists
        while (minForegroundTime == 0) {
            for (int i = 0; i < applicationNamesNoDuplicates.size(); i++) {
                if (applicationRunTimesNoDuplicates.get(i) == 0) {
                    applicationRunTimesNoDuplicates.remove(i);
                    applicationNamesNoDuplicates.remove(i);
                }
            }
            minForegroundTime = getMinForegroundTime(applicationRunTimesNoDuplicates);
        }


        currentAppName = intent.getStringExtra("current name");
        currentForegroundTime = intent.getLongExtra("current time", 0);

        setTitle(currentAppName);

        currentAppNameToDisplay = stripName(currentAppName);

        //removing duplicate values
        LinkedHashSet<String> setNames = new LinkedHashSet<>();
        setNames.addAll(applicationNamesNoDuplicates);
        applicationNamesNoDuplicates.clear();
        applicationNamesNoDuplicates.addAll(setNames);

        LinkedHashSet<Long> setTimes = new LinkedHashSet<>();
        setTimes.addAll(applicationRunTimesNoDuplicates);
        applicationRunTimesNoDuplicates.clear();
        applicationRunTimesNoDuplicates.addAll(setTimes);

        long totalTimeSpent = totalTimeSpent(applicationRunTimesNoDuplicates);
        long valueOfTotalTimespentOnApp;
        totalTime = transformTime(totalTimeSpent);

        try {
            int pos = applicationNamesNoDuplicates.indexOf(currentAppName);
            totalTimeSpentOnApp = transformTime(applicationRunTimesNoDuplicates.get(pos));
            valueOfTotalTimespentOnApp = applicationRunTimesNoDuplicates.get(pos);

        } catch (ArrayIndexOutOfBoundsException e) {
            totalTimeSpentOnApp = transformTime(0);
            valueOfTotalTimespentOnApp = 0;
        }

        valueOfTotalTimePercentageContent = getPercentage(totalTimeSpent);
        valueOfAppTimePercentageContent = getPercentage(valueOfTotalTimespentOnApp);


        maxForegroundTime = getMaxForegroundTime(applicationRunTimesNoDuplicates);
        String minAppName = applicationNamesNoDuplicates.get(applicationRunTimesNoDuplicates.indexOf(minForegroundTime));
        strippedMinAppName = stripName(minAppName);
        strippedMinAppName = putLineSeparator(strippedMinAppName);

        String maxAppName = applicationNamesNoDuplicates.get(applicationRunTimesNoDuplicates.indexOf(maxForegroundTime));
        strippedMaxAppName = stripName(maxAppName);
        strippedMaxAppName = putLineSeparator(strippedMaxAppName);



        setViews();

        GraphView graph = (GraphView) findViewById(R.id.graph);
        BarGraphSeries<DataPoint> series = new BarGraphSeries<>(new DataPoint[]{
                new DataPoint(0, 0),
                new DataPoint(1, (int) log(minForegroundTime)),
                new DataPoint(2, (int) log(currentForegroundTime)),
                new DataPoint(3, (int) log(maxForegroundTime)),
                new DataPoint(4, 0),

        });

        final String[] xLabels = {null, " Least Used ", currentAppNameToDisplay, " Most Used ", null};
        graph.addSeries(series);


        graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    return xLabels[(int) value];
                } else {
                    return super.formatLabel(Math.exp(value) / (1000 * 60), isValueX) + " min";
                }

            }
        });


        series.setValueDependentColor(new ValueDependentColor<DataPoint>() {
            @Override
            public int get(DataPoint data) {
                return Color.rgb((int) data.getX() * 255 / 4, (int) Math.abs(data.getY() * 255 / 6), 100);
            }
        });

        graph.getGridLabelRenderer().setHumanRounding(false);
        graph.getGridLabelRenderer().setHorizontalAxisTitle("Applications");
        graph.getGridLabelRenderer().setVerticalAxisTitle("Time");
        graph.setTitle("Usage Stats");
        graph.getGridLabelRenderer().setTextSize(19);
        series.setSpacing(10);
        graph.getGridLabelRenderer().reloadStyles();

    }

    private long getMinForegroundTime(ArrayList<Long> runTimes) {
        long min = Collections.min(runTimes);
        return min;
    }

    private long getMaxForegroundTime(ArrayList<Long> runTimes) {
        long max = Collections.max(runTimes);
        return max;
    }


    private String stripName(String string) {
        String newString;
        String subString = "";
        String regex = "com.";
        newString = string.replaceAll(regex, subString);
        return newString;
    }

    private String putLineSeparator(String string) {
        String newString;
        String subString = ".\n";
        String regex = ".";
        newString = string.replace(regex, subString);
        return newString;
    }

    private long totalTimeSpent(ArrayList<Long> timeInForeground) {
        long totalTime = 0;
        for (long times : timeInForeground) {
            totalTime += times;
        }
        return totalTime;
    }

    private String transformTime(long milliSeconds) {
        String totalTime = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(milliSeconds),
                TimeUnit.MILLISECONDS.toMinutes(milliSeconds) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(milliSeconds) % TimeUnit.MINUTES.toSeconds(1));
        ;
        return totalTime;
    }

    private String getPercentage(long time) {
        timeSpan = lastTimeUsedFirst - lastTimeUsedLast;
        double percentageOfTotalTimeSpent = (double) time / timeSpan;
        String totalTimePercentage = (float) (percentageOfTotalTimeSpent * 100) + "%";


        return totalTimePercentage;
    }

    private void initViews() {
        percentageOfTotalTimeUsedOnPhone = findViewById(R.id.totalPercentageContent);
        leastUsedApp = findViewById(R.id.leastTimeUsedContent);
        totalTimeUsedPhone = findViewById(R.id.totalTimeSpentOnPhoneContent);
        mostUsedApp = findViewById(R.id.mostUsedContent);
        totalTimeUsedOnApp = findViewById(R.id.timeSpentOnAppContent);
        percentageOfTotalTimeUsedOnApp = findViewById(R.id.percentageUsageCurrentAppContent);
    }

    private void setViews() {
        leastUsedApp.setText(strippedMinAppName);
        mostUsedApp.setText(strippedMaxAppName);
        totalTimeUsedPhone.setText(totalTime);
        totalTimeUsedOnApp.setText(totalTimeSpentOnApp);
        percentageOfTotalTimeUsedOnPhone.setText(valueOfTotalTimePercentageContent);
        percentageOfTotalTimeUsedOnApp.setText(valueOfAppTimePercentageContent);
    }


}
