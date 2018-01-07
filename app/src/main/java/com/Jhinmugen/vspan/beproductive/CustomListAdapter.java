package com.Jhinmugen.vspan.beproductive;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.Jhinmugen.vspan.beproductive.db.TaskContract;
import com.example.vspan.beproductive.R;
import com.Jhinmugen.vspan.beproductive.db.TaskDbHelper;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;


/**
 * Created by vspan on 17/11/2017.
 */

public class CustomListAdapter extends ArrayAdapter<String> {

    private TaskDbHelper dbHelper;
    private final Context context;
    private ArrayList<String> tasks;
    private ArrayList<String> dates;
    private ArrayList<String> descriptions;
    private ArrayList<Integer> urgency;
    private NotificationManager notificationManager;
    private boolean isUpdated;
    private int pos;


    public CustomListAdapter(Context context, ArrayList<String> tasks, ArrayList<String> dates, ArrayList<String> description, ArrayList<Integer> urgency) {
        super(context, R.layout.item_todo, tasks);
        this.context = context;
        this.tasks = tasks;
        this.dates = dates;
        this.descriptions = description;
        this.urgency = urgency;
        dbHelper = new TaskDbHelper(context);
    }


    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        final ViewHolder mHolder;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.item_todo, parent, false);
            mHolder = new ViewHolder();
            notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mHolder.task = (TextView) view.findViewById(R.id.task_tile);
            mHolder.date = (TextView) view.findViewById(R.id.task_datetime);
            mHolder.description = (TextView) view.findViewById(R.id.taskDescription);
            mHolder.done = (CheckBox) view.findViewById(R.id.toDolist_checkBox);
            mHolder.edit = (ImageView) view.findViewById(R.id.imageEdit);
            mHolder.notify = (ImageView) view.findViewById(R.id.imageNotification);
            mHolder.backgroundColor = (LinearLayout) view.findViewById(R.id.checkboxLayout);
            view.setTag(mHolder);
        } else {
            mHolder = (ViewHolder) view.getTag();
        }
        mHolder.task.setText(tasks.get(position));
        mHolder.date.setText(dates.get(position));
        mHolder.description.setText(descriptions.get(position));
        switch (urgency.get(position)) {
            case 0:
                mHolder.backgroundColor.setBackgroundColor(Color.BLUE);
                break;
            case 1:
                mHolder.backgroundColor.setBackgroundColor(Color.CYAN);
                break;
            case 2:
                mHolder.backgroundColor.setBackgroundColor(Color.GREEN);
                break;
            case 3:
                mHolder.backgroundColor.setBackgroundColor(Color.YELLOW);
                break;
            case 4:
                mHolder.backgroundColor.setBackgroundColor(Color.RED);
                break;
            default:
                mHolder.backgroundColor.setBackgroundColor(Color.BLUE);
        }
        mHolder.done.setTag(position);
        mHolder.done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                pos = position;
                mHolder.done.setChecked(pos == (int) mHolder.done.getTag());
                if (mHolder.done.isChecked()) {
                    new AlertDialog.Builder(context)
                            .setTitle("Completed Task")
                            .setMessage("Did you finish your task and want to remove it?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    String deletedTask = String.valueOf(mHolder.task.getText());
                                    String deletedTime = String.valueOf(mHolder.date.getText());
                                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                                    db.delete(TaskContract.TaskEntry.TABLE,
                                            TaskContract.TaskEntry.COL_TASK_TITLE + " = ?" + " AND " + TaskContract.TaskEntry.PROCESS_DATE + " = ?",
                                            new String[]{deletedTask, deletedTime});
                                    db.close();
                                    mHolder.done.setChecked(false);
                                    if (view.getTag() != null) {
                                        tasks.remove((int) view.getTag());
                                        dates.remove((int) view.getTag());
                                        notifyDataSetChanged();
                                    }
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    mHolder.done.setChecked(false);
                                }
                            })
                            .show();
                }

            }
        });
        mHolder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isUpdated = true;
                Intent intent = new Intent(context, ActivitySaveToDo.class);
                intent.putExtra("isUpdated", isUpdated);
                intent.putExtra("taskName", mHolder.task.getText());
                intent.putExtra("taskDescription", mHolder.description.getText());
                context.startActivity(intent);
            }
        });

        mHolder.notify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String dateTime = dates.get(position);
                String title = tasks.get(position);
                String contentText = descriptions.get(position);
                scheduleNotification(context, title, contentText, position, dateTime);
                Toast.makeText(context,"Notification scheduled",Toast.LENGTH_LONG).show();

            }
        });


        return view;
    }

    public static void scheduleNotification(Context context, String title, String content, int pos, String date) {
        String[] dateTime;
        dateTime = date.split(" ");
        String[] dateToSet = dateTime[0].split("/");
        String[] timeToSet = dateTime[1].split(":");
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.clear();
        cal.set(Integer.parseInt(dateToSet[0]), Integer.parseInt(dateToSet[1]) - 1, Integer.parseInt(dateToSet[2]), Integer.parseInt(timeToSet[0]), Integer.parseInt(timeToSet[1]));


        ComponentName receiver = new ComponentName(context, AlarmReceiver.class);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("title", title);
        intent.putExtra("content", content);
        intent.putExtra("position", pos);
        intent.putExtra("date", date);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);

    }


    private class ViewHolder {
        private TextView task;
        private TextView date;
        private TextView description;
        private CheckBox done;
        private ImageView edit;
        private ImageView notify;
        private LinearLayout backgroundColor;

    }


}

