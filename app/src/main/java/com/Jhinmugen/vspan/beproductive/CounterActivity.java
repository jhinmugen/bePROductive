package com.Jhinmugen.vspan.beproductive;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vspan.beproductive.R;

import java.util.concurrent.TimeUnit;

public class CounterActivity extends AppCompatActivity implements View.OnClickListener {
    private long timeCountMilliseconds;
    public static final String KEY_VALUE_TEXT = "EDIT TEXT VALUE";
    public static final String KEY_VALUE_TEXT_VIEW = "TEXT VIEW VALUE";
    public static final String KEY_VALUE_ENUM = "ENUM VALUE";
    public static final String KEY_VALUE_LONG = "PREVIOUS MILLISECONDS VALUE";
    public static final String KEY_VALUE_BOOLEAN = "CONTINUED";
    private long previousTimer;
    private long time = 0;
    private static int maxValue = 1 * 60 * 1000;
    private int pomodoros =0;


    private enum TimerStatus {
        STARTED,
        STOPPED
    }

    private static TimerStatus timerStatus = TimerStatus.STOPPED;

    private ProgressBar progressBarCircle;
    private EditText editTextMinute;
    private TextView textViewTime;
    private ImageView imageViewReset;
    private ImageView imageViewStartStop;
    private CountDownTimer countDownTimer;
    private boolean isContinued;
    private Switch alarmSwitch;
    private Boolean switchState;
    private static Ringtone ringtone;
    private static Uri uri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counter);

        initViews();
        initListeners();

        uri = RingtoneManager.getActualDefaultRingtoneUri(this.getApplicationContext(), RingtoneManager.TYPE_ALARM);
        ringtone = RingtoneManager.getRingtone(this, uri);

        if (savedInstanceState != null) {
            editTextMinute.setText(savedInstanceState.getCharSequence(KEY_VALUE_TEXT));
            textViewTime.setText(savedInstanceState.getCharSequence(KEY_VALUE_TEXT_VIEW));
            timerStatus = (TimerStatus) savedInstanceState.getSerializable(KEY_VALUE_ENUM);
            previousTimer = savedInstanceState.getLong(KEY_VALUE_LONG);
            isContinued = savedInstanceState.getBoolean(KEY_VALUE_BOOLEAN);


        }
        if (isContinued == true) {
            continueProcess();
        }

    }

    private void initViews() {
        progressBarCircle = (ProgressBar) findViewById(R.id.progressBarCircle);
        editTextMinute = (EditText) findViewById(R.id.editTextMinute);
        textViewTime = (TextView) findViewById(R.id.textViewTime);
        imageViewReset = (ImageView) findViewById(R.id.imageViewReset);
        imageViewStartStop = (ImageView) findViewById(R.id.imageViewStartStop);
        alarmSwitch = (Switch) findViewById(R.id.alarmSwitch);
    }

    private void initListeners() {
        imageViewReset.setOnClickListener(this);
        imageViewStartStop.setOnClickListener(this);
        alarmSwitch.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imageViewReset:
                reset();
                break;
            case R.id.imageViewStartStop:
                startStop();
                break;
            case R.id.alarmSwitch:
                stopRingtone();
                break;
        }
    }

    private void reset() {
        stopCountDownTimer();
        if (isContinued == true) {
            if (!editTextMinute.getText().toString().isEmpty()) {
                time = Integer.parseInt(editTextMinute.getText().toString().trim());
                timeCountMilliseconds = time * 60 * 1000;
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.message_minutes), Toast.LENGTH_LONG).show();
            }

        }
        startCountDownTimer();
        isContinued = false;
    }

    private void startStop() {
        if (timerStatus == TimerStatus.STOPPED) {
            setTimerValues();
            setProgressBarValues();
            imageViewReset.setVisibility(View.VISIBLE);
            imageViewStartStop.setImageResource(R.drawable.icon_stop);
            editTextMinute.setEnabled(false);
            timerStatus = TimerStatus.STARTED;
            startCountDownTimer();
        } else {
            imageViewReset.setVisibility(View.GONE);
            imageViewStartStop.setImageResource(R.drawable.icon_start);
            editTextMinute.setEnabled(true);
            timerStatus = TimerStatus.STOPPED;
            isContinued = false;
            stopCountDownTimer();
        }
    }

    private void setTimerValues() {
        if (!editTextMinute.getText().toString().isEmpty()) {
            if (isContinued == false) {
                time = Integer.parseInt(editTextMinute.getText().toString().trim());
                timeCountMilliseconds = time * 60 * 1000;
            } else {
                timeCountMilliseconds = previousTimer;
                previousTimer = 0;
            }

        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.message_minutes), Toast.LENGTH_LONG).show();
        }

    }

    private void startCountDownTimer() {


        countDownTimer = new CountDownTimer(timeCountMilliseconds, 1000) {
            @Override
            public void onTick(long l) {
                textViewTime.setText(hmsTimeFormatter(l));
                progressBarCircle.setProgress((int) (l / 1000));

            }

            @Override
            public void onFinish() {
                pomodoros+=1;
                textViewTime.setText(hmsTimeFormatter(timeCountMilliseconds));
                setProgressBarValues();
                imageViewReset.setVisibility(View.GONE);
                imageViewStartStop.setImageResource(R.drawable.icon_start);
                editTextMinute.setEnabled(true);
                if (pomodoros == 5){
                    editTextMinute.setText("15");
                    pomodoros =0;
                }else{
                    editTextMinute.setText("5");
                }
                switchState = alarmSwitch.isChecked();
                if (switchState == true) {
                    ringtone.play();
                }
                timerStatus = TimerStatus.STOPPED;
                setTimerValues();
                setProgressBarValues();


            }
        }.start();
        countDownTimer.start();
    }

    private void stopCountDownTimer() {
        countDownTimer.cancel();

    }

    private void setProgressBarValues() {
        progressBarCircle.setMax((int) timeCountMilliseconds / 1000);
        progressBarCircle.setProgress((int) timeCountMilliseconds / 1000);
    }

    private String hmsTimeFormatter(long milliSeconds) {
        previousTimer = milliSeconds;
        String hms = String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(milliSeconds),
                TimeUnit.MILLISECONDS.toMinutes(milliSeconds) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliSeconds)),
                TimeUnit.MILLISECONDS.toSeconds(milliSeconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliSeconds)));
        return hms;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
        outState.putCharSequence(KEY_VALUE_TEXT, editTextMinute.getText());
        outState.putCharSequence(KEY_VALUE_TEXT_VIEW, textViewTime.getText());
        if (timerStatus == TimerStatus.STOPPED) {
            isContinued = false;
        } else {
            isContinued = true;
        }
        outState.putSerializable(KEY_VALUE_ENUM, timerStatus);
        outState.putLong(KEY_VALUE_LONG, previousTimer);
        outState.putBoolean(KEY_VALUE_BOOLEAN, isContinued);

    }

    public void continueProcess() {
        setTimerValues();
        setProgressBarValues();
        imageViewReset.setVisibility(View.VISIBLE);
        imageViewStartStop.setImageResource(R.drawable.icon_stop);
        editTextMinute.setEnabled(false);
        timerStatus = TimerStatus.STARTED;
        if (!editTextMinute.getText().toString().isEmpty()) {
            maxValue = Integer.parseInt(editTextMinute.getText().toString().trim());
            maxValue = maxValue * 60 * 1000;
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.message_minutes), Toast.LENGTH_LONG).show();
        }
        progressBarCircle.setMax(maxValue / 1000);
        progressBarCircle.setProgress((int) timeCountMilliseconds / 1000);
        startCountDownTimer();
    }

    public void stopRingtone() {
        if (ringtone.isPlaying() == true) {
            switchState = alarmSwitch.isChecked();
            if (switchState == false) {
                ringtone.stop();
            }
        }
    }


}
