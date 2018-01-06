package com.Jhinmugen.vspan.beproductive;


import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;


import com.example.vspan.beproductive.R;
import com.Jhinmugen.vspan.beproductive.db.TaskContract;
import com.Jhinmugen.vspan.beproductive.db.TaskDbHelper;

import java.util.ArrayList;

public class ActivityToDo extends AppCompatActivity {

    private TaskDbHelper mHelper;
    private ListView mTaskListView;
    private CustomListAdapter mArrayAdapter;
    private boolean isUpdated;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do);
        mHelper = new TaskDbHelper(this);
        mTaskListView = (ListView) findViewById(R.id.list_todo);
        updateUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_todo, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_task:
                callActivitySave();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void updateUI() {
        ArrayList<String> taskList = new ArrayList<>();
        ArrayList<String> dateList = new ArrayList<>();
        ArrayList<String> descriptionList = new ArrayList<>();
        ArrayList<Integer> urgencyList = new ArrayList<>();
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.query(TaskContract.TaskEntry.TABLE,
                new String[]{TaskContract.TaskEntry._ID,
                        TaskContract.TaskEntry.COL_TASK_TITLE,
                        TaskContract.TaskEntry.PROCESS_DATE,
                        TaskContract.TaskEntry.COL_TASK_DESCRIPTION,
                        TaskContract.TaskEntry.COL_TASK_URGENCY},
                null, null, null, null, null);
        while (cursor.moveToNext()) {
            int idx = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_TITLE);
            taskList.add(cursor.getString(idx));

            int secondIdx = cursor.getColumnIndex(TaskContract.TaskEntry.PROCESS_DATE);
            dateList.add(cursor.getString(secondIdx));

            int thirdIdx = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_DESCRIPTION);
            descriptionList.add(cursor.getString(thirdIdx));

            int fourthIdx = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_URGENCY);
            urgencyList.add(cursor.getInt(fourthIdx));

        }


        mArrayAdapter = new CustomListAdapter(this, taskList, dateList, descriptionList, urgencyList);
        mTaskListView.setAdapter(mArrayAdapter);
        mArrayAdapter.notifyDataSetChanged();

        cursor.close();
        db.close();
    }


    private void callActivitySave() {
        isUpdated = false;
        Intent intent = new Intent(this, ActivitySaveToDo.class);
        intent.putExtra("isUpdated", isUpdated);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }
}
