package com.android.server.hidata.wavemapping.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.hidata.wavemapping.entity.IdentifyResult;
import com.android.server.hidata.wavemapping.util.LogUtil;
import com.android.server.hidata.wavemapping.util.TimeUtil;
import java.util.ArrayList;
import java.util.List;

public class IdentifyResultDAO {
    private static final String TAG = ("WMapping." + IdentifyResultDAO.class.getSimpleName());
    private SQLiteDatabase db = DatabaseSingleton.getInstance();

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x009b, code lost:
        if (r2 == null) goto L_0x009e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:6:0x0057, code lost:
        if (r2 != null) goto L_0x0059;
     */
    public List<IdentifyResult> findAll() {
        List<IdentifyResult> identifyResultList = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = this.db.rawQuery("SELECT PRELABLE,SSID,SERVERMAC,UPTIME,MODELNAME FROM IDENTIFY_RESULT", null);
            while (cursor.moveToNext()) {
                IdentifyResult tempIdentifyResult = new IdentifyResult();
                tempIdentifyResult.setPreLabel(cursor.getInt(cursor.getColumnIndexOrThrow("PRELABLE")));
                tempIdentifyResult.setSsid(cursor.getString(cursor.getColumnIndexOrThrow("SSID")));
                tempIdentifyResult.setServeMac(cursor.getString(cursor.getColumnIndexOrThrow("SERVERMAC")));
                tempIdentifyResult.setModelName(cursor.getString(cursor.getColumnIndexOrThrow("MODELNAME")));
                identifyResultList.add(tempIdentifyResult);
            }
        } catch (IllegalArgumentException e) {
            LogUtil.d("SELECT PRELABLE,SSID,SERVERMAC,UPTIME,MODELNAME FROM IDENTIFY_RESULT");
            String str = TAG;
            Log.e(str, "findAll IllegalArgumentException: " + e.getMessage());
        } catch (Exception e2) {
            String str2 = TAG;
            Log.e(str2, "findAll Exception: " + e2.getMessage());
            if (cursor != null) {
                cursor.close();
            }
            return identifyResultList;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0062, code lost:
        if (r1 == null) goto L_0x0065;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x001e, code lost:
        if (r1 != null) goto L_0x0020;
     */
    public int findAllCount() {
        Cursor cursor = null;
        int allCnt = 0;
        try {
            cursor = this.db.rawQuery("SELECT count(1) as CNT FROM IDENTIFY_RESULT", null);
            while (cursor.moveToNext()) {
                allCnt = cursor.getInt(cursor.getColumnIndexOrThrow("CNT"));
            }
        } catch (IllegalArgumentException e) {
            LogUtil.d("SELECT count(1) as CNT FROM IDENTIFY_RESULT");
            String str = TAG;
            Log.e(str, "findAll IllegalArgumentException: " + e.getMessage());
        } catch (Exception e2) {
            String str2 = TAG;
            Log.e(str2, "findAll Exception: " + e2.getMessage());
            if (cursor != null) {
                cursor.close();
            }
            return allCnt;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    public boolean remove(String ssid, boolean isMainAp) {
        try {
            this.db.execSQL("DELETE FROM IDENTIFY_RESULT  WHERE SSID = ? and ISMAINAP = ? ", new Object[]{ssid, String.valueOf(isMainAp ? 1 : 0)});
            return true;
        } catch (SQLException e) {
            String str = TAG;
            Log.e(str, "remove exception: " + e.getMessage());
            return false;
        }
    }

    public boolean insert(IdentifyResult identifyResult, boolean isMainAp) {
        try {
            ContentValues cValue = new ContentValues();
            cValue.put("SSID", identifyResult.getSsid());
            cValue.put("PRELABLE", Integer.valueOf(identifyResult.getPreLabel()));
            cValue.put("SERVERMAC", identifyResult.getServeMac());
            cValue.put("MODELNAME", identifyResult.getModelName());
            cValue.put("UPTIME", TimeUtil.getTime());
            cValue.put("ISMAINAP", String.valueOf(isMainAp ? 1 : 0));
            this.db.insert(Constant.IDENTIFY_RESULT_TABLE_NAME, null, cValue);
            return true;
        } catch (Exception e) {
            String str = TAG;
            Log.e(str, "insert exception: " + e.getMessage());
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0079, code lost:
        if (r3 != null) goto L_0x007b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x00ba, code lost:
        if (r3 == null) goto L_0x00bd;
     */
    public List<IdentifyResult> findBySsid(String ssid, boolean isMainAp) {
        List<IdentifyResult> identifyResultList = new ArrayList<>();
        if (ssid == null || ssid.equals("")) {
            LogUtil.d("findBySsid ssid=null or ssid=");
            return identifyResultList;
        } else if (this.db == null) {
            return identifyResultList;
        } else {
            Cursor cursor = null;
            try {
                cursor = this.db.rawQuery("SELECT SSID,PRELABLE,SERVERMAC,UPTIME,MODELNAME FROM IDENTIFY_RESULT WHERE SSID = ? AND ISMAINAP = ? ", new String[]{ssid, String.valueOf(isMainAp ? 1 : 0)});
                while (cursor.moveToNext()) {
                    IdentifyResult tempIdentifyResult = new IdentifyResult();
                    tempIdentifyResult.setPreLabel(cursor.getInt(cursor.getColumnIndexOrThrow("PRELABLE")));
                    tempIdentifyResult.setSsid(cursor.getString(cursor.getColumnIndexOrThrow("SSID")));
                    tempIdentifyResult.setServeMac(cursor.getString(cursor.getColumnIndexOrThrow("SERVERMAC")));
                    tempIdentifyResult.setModelName(cursor.getString(cursor.getColumnIndexOrThrow("MODELNAME")));
                    identifyResultList.add(tempIdentifyResult);
                }
            } catch (IllegalArgumentException e) {
                String str = TAG;
                Log.e(str, "findBySsid IllegalArgumentException: " + e.getMessage());
            } catch (Exception e2) {
                String str2 = TAG;
                Log.e(str2, "findBySsid Exception: " + e2.getMessage());
                if (cursor != null) {
                    cursor.close();
                }
                return identifyResultList;
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
                throw th;
            }
        }
    }
}
