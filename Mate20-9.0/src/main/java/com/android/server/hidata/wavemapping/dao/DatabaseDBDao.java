package com.android.server.hidata.wavemapping.dao;

import android.database.sqlite.SQLiteDatabase;

public class DatabaseDBDao {
    private static final String TAG = ("WMapping." + DatabaseDBDao.class.getSimpleName());
    private String CREATE_TEMP_STD_DATA_TABLE_NAME = "CREATE TABLE IF NOT EXISTS _TEMP_STD (LOCATION VARCHAR(200), BATCH VARCHAR(20),DATAS TEXT)";
    private SQLiteDatabase db = DatabaseSingleton.getInstance();
}
