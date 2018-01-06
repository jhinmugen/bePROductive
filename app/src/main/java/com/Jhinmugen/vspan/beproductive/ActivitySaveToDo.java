package com.Jhinmugen.vspan.beproductive;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.vspan.beproductive.R;
import com.Jhinmugen.vspan.beproductive.db.TaskContract;
import com.Jhinmugen.vspan.beproductive.db.TaskDbHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ActivitySaveToDo extends AppCompatActivity {
    private EditText dateText;
    private EditText timeText;
    private EditText taskText;
    private EditText taskDescription;
    private SeekBar urgentSeekBar;
    private TextView seekBarProgress;


    private String description;
    private String task;
    private final Calendar myCalendar = Calendar.getInstance();
    private DatePickerDialog.OnDateSetListener date;
    private TimePickerDialog.OnTimeSetListener time;
    private Button cancelButton;
    private Button saveButton;
    private TaskDbHelper mHelper;
    private boolean isUpdated;
    private String taskDbName;
    private String descriptionDbName;
    private int urgencyLevel;

    private static final String TAG = "ActivitySaveToDo";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_to_do);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        taskText = (EditText) findViewById(R.id.textTask);
        taskDescription = (EditText) findViewById(R.id.textDescription);
        urgentSeekBar = (SeekBar) findViewById(R.id.urgencySeekBar);
        seekBarProgress = (TextView) findViewById(R.id.progressSeekBar);
        Intent intent = getIntent();
        isUpdated = intent.getBooleanExtra("isUpdated", false);


        if (isUpdated == true) {
            taskDbName = intent.getStringExtra("taskName");
            descriptionDbName = intent.getStringExtra("taskDescription");
            taskText.setText(taskDbName);
            taskDescription.setText(descriptionDbName);
        }

        mHelper = new TaskDbHelper(this);
        seekBarProgress.setText("This task can wait");
        dateText = (EditText) findViewById(R.id.textDate);
        dateText.setText(new SimpleDateFormat("yyyy/MM/dd").format(myCalendar.getTime()));
        date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, day);
                updateDateLabel();
            }
        };

        dateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(ActivitySaveToDo.this, date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });


        timeText = (EditText) findViewById(R.id.textTime);
        timeText.setText(new SimpleDateFormat("HH:mm").format(myCalendar.getTime()));
        time = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectHour, int selectMinutes) {
                myCalendar.set(Calendar.HOUR_OF_DAY, selectHour);
                myCalendar.set(Calendar.MINUTE, selectMinutes);
                updateTimeLabel();

            }
        };
        timeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new TimePickerDialog(ActivitySaveToDo.this, time, myCalendar.get(myCalendar.HOUR_OF_DAY), myCalendar.get(myCalendar.MINUTE), true).show();
            }
        });

        cancelButton = findViewById(R.id.buttonCancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        urgentSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String urgency;
                switch (progress) {
                    case 0:
                        urgency = "This task can wait";
                        break;
                    case 1:
                        urgency = "This task can wait, could do it today";
                        break;
                    case 2:
                        urgency = "I should do this task today";
                        break;
                    case 3:
                        urgency = "I better finish this task today";
                        break;
                    case 4:
                        urgency = "I must absolutely complete this task today";
                        break;
                    default:
                        urgency = "I must absolutely complete this task today";
                        break;

                }
                seekBarProgress.setText(urgency);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        saveButton = findViewById(R.id.buttonSave);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                urgencyLevel = urgentSeekBar.getProgress();
                task = ((EditText) findViewById(R.id.textTask)).getText().toString();
                description = (((EditText) findViewById(R.id.textDescription)).getText().toString());
                String dateTimeString = dateText.getText().toString() + " " + timeText.getText().toString();
                SQLiteDatabase db = mHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put(TaskContract.TaskEntry.COL_TASK_TITLE, task);
                values.put(TaskContract.TaskEntry.PROCESS_DATE, dateTimeString);
                values.put(TaskContract.TaskEntry.COL_TASK_DESCRIPTION, description);
                values.put(TaskContract.TaskEntry.COL_TASK_URGENCY, urgencyLevel);

                if (isUpdated == false) {
                    db.insertWithOnConflict(TaskContract.TaskEntry.TABLE,
                            null,
                            values,
                            SQLiteDatabase.CONFLICT_REPLACE);

                } else {
                    db.update(TaskContract.TaskEntry.TABLE, values, TaskContract.TaskEntry.COL_TASK_TITLE + " = ?" + " AND " + TaskContract.TaskEntry.COL_TASK_DESCRIPTION + " = ?", new String[]{taskDbName, descriptionDbName});
                }
                db.close();
                finish();
            }

        });


    }

    public void updateDateLabel() {
        String myFormat = "yyyy/MM/dd";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(myFormat, Locale.US);
        dateText.setText(simpleDateFormat.format(myCalendar.getTime()));

    }

    public void updateTimeLabel() {
        String timeFormat = "HH:mm";
        SimpleDateFormat simpleTimeFormat = new SimpleDateFormat(timeFormat);
        timeText.setText(simpleTimeFormat.format(myCalendar.getTime()));
    }


}
