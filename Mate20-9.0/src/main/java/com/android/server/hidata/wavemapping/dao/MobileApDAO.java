package com.android.server.hidata.wavemapping.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.hidata.wavemapping.entity.ApInfo;
import com.android.server.hidata.wavemapping.util.LogUtil;
import com.android.server.hidata.wavemapping.util.TimeUtil;
import java.util.ArrayList;
import java.util.List;

public class MobileApDAO {
    private static final String TAG = ("WMapping." + MobileApDAO.class.getSimpleName());
    private SQLiteDatabase db = DatabaseSingleton.getInstance();

    public boolean insert(ApInfo apInfo) {
        if (apInfo == null || apInfo.getSsid() == null || apInfo.getSsid().equals("") || apInfo.getMac() == null || apInfo.getMac().equals("")) {
            return false;
        }
        if (findBySsid(apInfo.getSsid(), apInfo.getMac()) != null) {
            return true;
        }
        ContentValues cValue = new ContentValues();
        cValue.put("SSID", apInfo.getSsid());
        cValue.put("MAC", apInfo.getMac());
        cValue.put("UPTIME", apInfo.getUptime());
        cValue.put("SRCTYPE", Integer.valueOf(apInfo.getSrcType()));
        try {
            this.db.insert(Constant.MOBILE_AP_TABLE_NAME, null, cValue);
            return true;
        } catch (Exception e) {
            LogUtil.e("insert exception: " + e.getMessage());
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x007d, code lost:
        if (r3 == null) goto L_0x0080;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:6:0x003d, code lost:
        if (r3 != null) goto L_0x003f;
     */
    public List<ApInfo> findAllAps() {
        List<ApInfo> apInfos = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = this.db.rawQuery("SELECT SSID,MAC,UPTIME FROM MOBILE_AP WHERE 1 = 1", null);
            while (cursor.moveToNext()) {
                apInfos.add(new ApInfo(cursor.getString(cursor.getColumnIndexOrThrow("SSID")), cursor.getString(cursor.getColumnIndexOrThrow("MAC")), cursor.getString(cursor.getColumnIndexOrThrow("UPTIME"))));
            }
        } catch (IllegalArgumentException e) {
            LogUtil.d("SELECT SSID,MAC,UPTIME FROM MOBILE_AP WHERE 1 = 1");
            LogUtil.e("findAllAps IllegalArgumentException: " + e.getMessage());
        } catch (Exception e2) {
            LogUtil.e("findAllAps Exception: " + e2.getMessage());
            if (cursor != null) {
                cursor.close();
            }
            return apInfos;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0061, code lost:
        if (r2 == null) goto L_0x0064;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x0021, code lost:
        if (r2 != null) goto L_0x0023;
     */
    public int findAllCountBySrctype(int srctype) {
        int cnt = 0;
        Cursor cursor = null;
        try {
            cursor = this.db.rawQuery("SELECT count(1) FROM MOBILE_AP WHERE SRCTYPE = ? ", new String[]{String.valueOf(srctype)});
            while (cursor.moveToNext()) {
                cnt = cursor.getInt(0);
            }
        } catch (IllegalArgumentException e) {
            LogUtil.d("SELECT count(1) FROM MOBILE_AP WHERE SRCTYPE = ? ");
            LogUtil.e("findAllCountBySrctype IllegalArgumentException: " + e.getMessage());
        } catch (Exception e2) {
            LogUtil.e("findAllCountBySrctype Exception: " + e2.getMessage());
            if (cursor != null) {
                cursor.close();
            }
            return cnt;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    public boolean update(ApInfo apInfo) {
        if (apInfo == null) {
            LogUtil.d("update failure,null == place");
            return false;
        }
        Object[] args = {apInfo.getUptime(), apInfo.getSsid(), apInfo.getMac()};
        LogUtil.i("update begin:" + "UPDATE MOBILE_AP SET UPTIME = ? WHERE SSID = ? AND MAC = ? " + "apInfo:" + apInfo.toString());
        try {
            this.db.execSQL("UPDATE MOBILE_AP SET UPTIME = ? WHERE SSID = ? AND MAC = ? ", args);
            return true;
        } catch (SQLException e) {
            LogUtil.e("UPDATE exception: " + e.getMessage());
            return false;
        }
    }

    public boolean remove(String ssid, String mac) {
        if (ssid == null || ssid.equals("") || mac == null || mac.equals("")) {
            return false;
        }
        try {
            this.db.execSQL("DELETE FROM MOBILE_AP  WHERE SSID = ? AND MAC = ? ", new Object[]{ssid, mac});
            return true;
        } catch (SQLException e) {
            LogUtil.e("remove exception: " + e.getMessage());
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x005b, code lost:
        if (r2 != null) goto L_0x005d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0098, code lost:
        if (r2 == null) goto L_0x009b;
     */
    public ApInfo findBySsid(String ssid, String mac) {
        ApInfo apInfo = null;
        if (ssid == null || ssid.equals("")) {
            LogUtil.d("findBySsid ssid=null or ssid=");
            return null;
        } else if (mac == null || mac.equals("")) {
            LogUtil.d("findBySsid mac=null or mac=");
            return null;
        } else {
            Cursor cursor = null;
            if (this.db == null) {
                return null;
            }
            try {
                cursor = this.db.rawQuery("SELECT SSID,MAC,UPTIME FROM MOBILE_AP WHERE SSID = ? AND MAC = ? ", new String[]{ssid, mac});
                if (cursor.moveToNext()) {
                    apInfo = new ApInfo(cursor.getString(cursor.getColumnIndexOrThrow("SSID")), cursor.getString(cursor.getColumnIndexOrThrow("MAC")), cursor.getString(cursor.getColumnIndexOrThrow("UPTIME")));
                }
            } catch (IllegalArgumentException e) {
                LogUtil.e("findBySsid IllegalArgumentException: " + e.getMessage());
            } catch (Exception e2) {
                LogUtil.e("findBySsid Exception: " + e2.getMessage());
                if (cursor != null) {
                    cursor.close();
                }
                return apInfo;
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
                throw th;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x005b, code lost:
        if (r2 != null) goto L_0x005d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0098, code lost:
        if (r2 == null) goto L_0x009b;
     */
    public ApInfo findBySsidForUpdateTime(String ssid, String bssid) {
        ApInfo apInfo = null;
        if (ssid == null || ssid.equals("")) {
            LogUtil.d("findBySsidForUpdateTime ssid=null or ssid =");
            return null;
        } else if (bssid == null || bssid.equals("")) {
            LogUtil.d("findBySsidForUpdateTime bssid=null or bssid =");
            return null;
        } else {
            Cursor cursor = null;
            if (this.db == null) {
                return null;
            }
            try {
                cursor = this.db.rawQuery("SELECT SSID,MAC,UPTIME FROM MOBILE_AP WHERE SSID = ? AND MAC = ? ", new String[]{ssid, bssid});
                if (cursor.moveToNext()) {
                    apInfo = new ApInfo(cursor.getString(cursor.getColumnIndexOrThrow("SSID")), cursor.getString(cursor.getColumnIndexOrThrow("MAC")), cursor.getString(cursor.getColumnIndexOrThrow("UPTIME")));
                }
            } catch (IllegalArgumentException e) {
                LogUtil.e("findBySsidForUpdateTime IllegalArgumentException: " + e.getMessage());
            } catch (Exception e2) {
                LogUtil.e("findBySsidForUpdateTime Exception: " + e2.getMessage());
                if (cursor != null) {
                    cursor.close();
                }
                if (apInfo != null) {
                    apInfo.setUptime(TimeUtil.getTime());
                    if (!update(apInfo)) {
                        LogUtil.d("findBySsidForUpdateTime update failure");
                        LogUtil.i("                                      ,apinfo: " + apInfo.toString());
                    }
                }
                return apInfo;
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
                throw th;
            }
        }
    }
}
