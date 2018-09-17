package com.android.server.wifipro;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class PortalDbHelper extends SQLiteOpenHelper {
    public static final String CREATE_TABLE1 = "CREATE TABLE if not exists UPLOADED_PORTAL_WEB_PAGE_INFO(id integer PRIMARY KEY autoincrement,ssid, pageLocation, firstStepId, secondStepId, bssid, cellId, sndBtnId, loginBtnId, btnNumber)";
    public static final String CREATE_TABLE2 = "CREATE TABLE if not exists COLLECTED_PORTAL_WEB_PAGE_INFO(id integer PRIMARY KEY autoincrement,ssid, pageLocation, firstStepId, firstStepName, secondStepId, secondStepName, secondStepValue, sndBtnId, loginBtnId, loginBtnName, loginBtnValue, loginNodeType)";
    public static final String CREATE_TABLE3 = "CREATE TABLE if not exists LAST_INPUT_PHONE_NUM_ON_SSID(id integer PRIMARY KEY autoincrement,ssid, phoneNumber)";
    public static final String CREATE_TABLE4 = "CREATE TABLE if not exists STANDARD_PORTAL_302 (id integer PRIMARY KEY autoincrement,ssid, checkTimestamp, checkLAC integer)";
    public static final String CREATE_TABLE5 = "CREATE TABLE if not exists DHCP_RESULTS_INTERNET_OK (id integer PRIMARY KEY autoincrement,bssid, dhcpResults)";
    public static final String ITEM_BSSID = "bssid";
    public static final String ITEM_BTN_NUM = "btnNumber";
    public static final String ITEM_CELLID = "cellId";
    public static final String ITEM_CHECK_LAC = "checkLAC";
    public static final String ITEM_CHECK_TIMESTAMP = "checkTimestamp";
    public static final String ITEM_DHCP_RESULTS = "dhcpResults";
    public static final String ITEM_LG_BTN = "loginBtnId";
    public static final String ITEM_LG_BTN_NAME = "loginBtnName";
    public static final String ITEM_LG_BTN_VALUE = "loginBtnValue";
    public static final String ITEM_LG_NODE_TYPE = "loginNodeType";
    public static final String ITEM_PHONE = "firstStepId";
    public static final String ITEM_PHONE_NAME = "firstStepName";
    public static final String ITEM_PHONE_NUM = "phoneNumber";
    public static final String ITEM_PW = "secondStepId";
    public static final String ITEM_PW_NAME = "secondStepName";
    public static final String ITEM_PW_VALUE = "secondStepValue";
    public static final String ITEM_SND_BTN = "sndBtnId";
    public static final String ITEM_SSID = "ssid";
    public static final String ITEM_URL = "pageLocation";
    public static final String QUERY_TABLE1 = "SELECT * FROM UPLOADED_PORTAL_WEB_PAGE_INFO";
    public static final String QUERY_TABLE2 = "SELECT * FROM COLLECTED_PORTAL_WEB_PAGE_INFO";
    public static final String QUERY_TABLE3 = "SELECT * FROM LAST_INPUT_PHONE_NUM_ON_SSID";
    public static final String QUERY_TABLE4 = "SELECT * FROM STANDARD_PORTAL_302 ";
    public static final String QUERY_TABLE5 = "SELECT * FROM DHCP_RESULTS_INTERNET_OK ";
    public static final String TABLE_COLLECTED_PORTAL_WEB_PAGE_INFO = "COLLECTED_PORTAL_WEB_PAGE_INFO";
    public static final String TABLE_DHCP_RESULTS_INTERNET_OK = "DHCP_RESULTS_INTERNET_OK ";
    public static final String TABLE_LAST_INPUT_PHONE_NUM_ON_SSID = "LAST_INPUT_PHONE_NUM_ON_SSID";
    public static final String TABLE_STANDARD_PORTAL_302 = "STANDARD_PORTAL_302 ";
    public static final String TABLE_UPLOADED_PORTAL_WEB_PAGE_INFO = "UPLOADED_PORTAL_WEB_PAGE_INFO";

    public PortalDbHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE1);
        db.execSQL(CREATE_TABLE2);
        db.execSQL(CREATE_TABLE3);
        db.execSQL(CREATE_TABLE4);
        db.execSQL(CREATE_TABLE5);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS UPLOADED_PORTAL_WEB_PAGE_INFO");
        db.execSQL("DROP TABLE IF EXISTS COLLECTED_PORTAL_WEB_PAGE_INFO");
        db.execSQL("DROP TABLE IF EXISTS LAST_INPUT_PHONE_NUM_ON_SSID");
        db.execSQL("DROP TABLE IF EXISTS STANDARD_PORTAL_302 ");
        db.execSQL("DROP TABLE IF EXISTS DHCP_RESULTS_INTERNET_OK ");
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS UPLOADED_PORTAL_WEB_PAGE_INFO");
        db.execSQL("DROP TABLE IF EXISTS COLLECTED_PORTAL_WEB_PAGE_INFO");
        db.execSQL("DROP TABLE IF EXISTS LAST_INPUT_PHONE_NUM_ON_SSID");
        db.execSQL("DROP TABLE IF EXISTS STANDARD_PORTAL_302 ");
        db.execSQL("DROP TABLE IF EXISTS DHCP_RESULTS_INTERNET_OK ");
        onCreate(db);
    }
}
