package com.Jhinmugen.vspan.beproductive.db;

import android.provider.BaseColumns;

/**
 * Created by vspan on 6/11/2017.
 */

public class TaskContract {
    public static final String DB_NAME = "com.vspan.pebroductive.db";
    public static final int DB_VERSION = 10;

    public class TaskEntry implements BaseColumns {
        public static final String TABLE = "tasks";
        public static final String COL_TASK_TITLE = "title";
        public static final String PROCESS_DATE = "dateTime";
        public static final String COL_TASK_DESCRIPTION = "description";
        public static final String COL_TASK_URGENCY = "urgency";
    }

}
