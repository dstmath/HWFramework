package com.android.internal.telephony.dataconnection;

import android.database.Cursor;
import android.net.NetworkUtils;
import android.provider.HwTelephony.NumMatchs;
import android.telephony.Rlog;

public class HwApnSetting extends ApnSetting {
    public static final int APN_PRESET = 1;
    public static final String DB_PRESET = "visible";
    protected static final String LOG_TAG = "HwApnSetting";
    protected static final boolean VDBG = true;
    public int preset;

    public HwApnSetting(Cursor cursor, String[] types) {
        super(cursor.getInt(cursor.getColumnIndexOrThrow("_id")), cursor.getString(cursor.getColumnIndexOrThrow("numeric")), cursor.getString(cursor.getColumnIndexOrThrow(NumMatchs.NAME)), cursor.getString(cursor.getColumnIndexOrThrow("apn")), NetworkUtils.trimV4AddrZeros(cursor.getString(cursor.getColumnIndexOrThrow("proxy"))), cursor.getString(cursor.getColumnIndexOrThrow("port")), NetworkUtils.trimV4AddrZeros(cursor.getString(cursor.getColumnIndexOrThrow("mmsc"))), NetworkUtils.trimV4AddrZeros(cursor.getString(cursor.getColumnIndexOrThrow("mmsproxy"))), cursor.getString(cursor.getColumnIndexOrThrow("mmsport")), cursor.getString(cursor.getColumnIndexOrThrow("user")), cursor.getString(cursor.getColumnIndexOrThrow("password")), cursor.getInt(cursor.getColumnIndexOrThrow("authtype")), types, cursor.getString(cursor.getColumnIndexOrThrow("protocol")), cursor.getString(cursor.getColumnIndexOrThrow("roaming_protocol")), cursor.getInt(cursor.getColumnIndexOrThrow("carrier_enabled")) == 1, cursor.getInt(cursor.getColumnIndexOrThrow("bearer")), cursor.getInt(cursor.getColumnIndexOrThrow("bearer_bitmask")), cursor.getInt(cursor.getColumnIndexOrThrow("profile_id")), cursor.getInt(cursor.getColumnIndexOrThrow("modem_cognitive")) == 1, cursor.getInt(cursor.getColumnIndexOrThrow("max_conns")), cursor.getInt(cursor.getColumnIndexOrThrow("wait_time")), cursor.getInt(cursor.getColumnIndexOrThrow("max_conns_time")), cursor.getInt(cursor.getColumnIndexOrThrow("mtu")), cursor.getString(cursor.getColumnIndexOrThrow("mvno_type")), cursor.getString(cursor.getColumnIndexOrThrow("mvno_match_data")));
        this.preset = 1;
        int apn_preset = 1;
        try {
            apn_preset = cursor.getInt(cursor.getColumnIndexOrThrow(DB_PRESET));
        } catch (Exception e) {
            log("query apn_preset/visible column got exception");
        }
        this.preset = apn_preset;
    }

    public boolean isPreset() {
        return this.preset == 1;
    }

    protected void log(String s) {
        Rlog.d(LOG_TAG, s);
    }
}
