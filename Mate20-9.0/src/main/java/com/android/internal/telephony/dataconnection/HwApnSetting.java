package com.android.internal.telephony.dataconnection;

import android.database.Cursor;
import android.net.NetworkUtils;
import android.provider.HwTelephony;
import android.telephony.Rlog;
import com.android.internal.telephony.HuaweiTelephonyConfigs;

public class HwApnSetting extends ApnSetting {
    public static final int ALL_TYPE_BIT = 32506879;
    public static final int APN_PRESET = 1;
    public static final String DB_PRESET = "visible";
    protected static final String LOG_TAG = "HwApnSetting";
    protected static final boolean VDBG = true;
    public static final int XCAP_TYPE_BIT = 2097152;
    public int preset;

    /* JADX WARNING: Illegal instructions before constructor call */
    public HwApnSetting(Cursor cursor, String[] types) {
        super(r14.getInt(r14.getColumnIndexOrThrow("_id")), r14.getString(r14.getColumnIndexOrThrow("numeric")), r14.getString(r14.getColumnIndexOrThrow(HwTelephony.NumMatchs.NAME)), r14.getString(r14.getColumnIndexOrThrow("apn")), NetworkUtils.trimV4AddrZeros(r14.getString(r14.getColumnIndexOrThrow("proxy"))), r14.getString(r14.getColumnIndexOrThrow("port")), NetworkUtils.trimV4AddrZeros(r14.getString(r14.getColumnIndexOrThrow("mmsc"))), NetworkUtils.trimV4AddrZeros(r14.getString(r14.getColumnIndexOrThrow("mmsproxy"))), r14.getString(r14.getColumnIndexOrThrow("mmsport")), r14.getString(r14.getColumnIndexOrThrow("user")), r14.getString(r14.getColumnIndexOrThrow("password")), r14.getInt(r14.getColumnIndexOrThrow("authtype")), types, r14.getString(r14.getColumnIndexOrThrow("protocol")), r14.getString(r14.getColumnIndexOrThrow("roaming_protocol")), r14.getInt(r14.getColumnIndexOrThrow("carrier_enabled")) == 1, r14.getInt(r14.getColumnIndexOrThrow("bearer")), r14.getInt(r14.getColumnIndexOrThrow("bearer_bitmask")), r14.getInt(r14.getColumnIndexOrThrow("profile_id")), r14.getInt(r14.getColumnIndexOrThrow("modem_cognitive")) == 1, r14.getInt(r14.getColumnIndexOrThrow("max_conns")), r14.getInt(r14.getColumnIndexOrThrow("wait_time")), r14.getInt(r14.getColumnIndexOrThrow("max_conns_time")), r14.getInt(r14.getColumnIndexOrThrow("mtu")), r14.getString(r14.getColumnIndexOrThrow("mvno_type")), r14.getString(r14.getColumnIndexOrThrow("mvno_match_data")));
        Cursor cursor2 = cursor;
        int i = 0;
        this.preset = 1;
        int apn_preset = 1;
        Cursor cursor3 = cursor;
        try {
            apn_preset = cursor3.getInt(cursor3.getColumnIndexOrThrow(DB_PRESET));
        } catch (Exception e) {
            log("query apn_preset/visible column got exception");
        }
        this.preset = apn_preset;
        int apnBitmap = 0;
        while (true) {
            int i2 = i;
            if (i2 < this.types.length) {
                apnBitmap |= getApnBitmaskHw(this.types[i2]);
                i = i2 + 1;
            } else {
                this.typesBitmap = apnBitmap;
                return;
            }
        }
    }

    public boolean isPreset() {
        return this.preset == 1;
    }

    /* access modifiers changed from: protected */
    public void log(String s) {
        Rlog.d(LOG_TAG, s);
    }

    protected static int getApnBitmaskHw(String apnType) {
        int type = ApnSetting.getApnBitmaskEx(apnType);
        if ((type == 0 || type == 1023) && HuaweiTelephonyConfigs.isMTKPlatform()) {
            char c = 65535;
            int hashCode = apnType.hashCode();
            if (hashCode != 42) {
                if (hashCode == 3673178 && apnType.equals("xcap")) {
                    c = 0;
                }
            } else if (apnType.equals("*")) {
                c = 1;
            }
            switch (c) {
                case 0:
                    return XCAP_TYPE_BIT;
                case 1:
                    return ALL_TYPE_BIT;
            }
        }
        return type;
    }
}
