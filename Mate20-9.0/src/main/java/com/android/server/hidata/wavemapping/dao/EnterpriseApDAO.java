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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EnterpriseApDAO {
    private static final String TAG = ("WMapping." + EnterpriseApDAO.class.getSimpleName());
    private SQLiteDatabase db = DatabaseSingleton.getInstance();

    public boolean insert(ApInfo apInfo) {
        if (findBySsid(apInfo.getSsid()) != null) {
            return true;
        }
        ContentValues cValue = new ContentValues();
        cValue.put("SSID", apInfo.getSsid());
        cValue.put("UPTIME", apInfo.getUptime());
        try {
            this.db.insert(Constant.ENTERPRISE_AP_TABLE_NAME, null, cValue);
            return true;
        } catch (Exception e) {
            LogUtil.e("insert exception: " + e.getMessage());
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0059, code lost:
        if (r3 == null) goto L_0x005c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x0019, code lost:
        if (r3 != null) goto L_0x001b;
     */
    public int findAllCount() {
        int cnt = 0;
        Cursor cursor = null;
        try {
            cursor = this.db.rawQuery("SELECT count(1) FROM ENTERPRISE_AP WHERE 1 = 1", null);
            while (cursor.moveToNext()) {
                cnt = cursor.getInt(0);
            }
        } catch (IllegalArgumentException e) {
            LogUtil.d("SELECT count(1) FROM ENTERPRISE_AP WHERE 1 = 1");
            LogUtil.e("findAll IllegalArgumentException: " + e.getMessage());
        } catch (Exception e2) {
            LogUtil.e("findAll Exception: " + e2.getMessage());
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

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0064, code lost:
        if (r3 == null) goto L_0x0067;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:6:0x0024, code lost:
        if (r3 != null) goto L_0x0026;
     */
    public Set<String> findAll() {
        Set<String> aps = new HashSet<>();
        Cursor cursor = null;
        try {
            cursor = this.db.rawQuery("SELECT SSID FROM ENTERPRISE_AP WHERE 1 = 1", null);
            while (cursor.moveToNext()) {
                aps.add(cursor.getString(cursor.getColumnIndexOrThrow("SSID")));
            }
        } catch (IllegalArgumentException e) {
            LogUtil.d("SELECT SSID FROM ENTERPRISE_AP WHERE 1 = 1");
            LogUtil.e("findAll IllegalArgumentException: " + e.getMessage());
        } catch (Exception e2) {
            LogUtil.e("findAll Exception: " + e2.getMessage());
            if (cursor != null) {
                cursor.close();
            }
            return aps;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0073, code lost:
        if (r3 == null) goto L_0x0076;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:6:0x0033, code lost:
        if (r3 != null) goto L_0x0035;
     */
    public List<ApInfo> findAllAps() {
        List<ApInfo> apInfos = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = this.db.rawQuery("SELECT SSID,MAC,UPTIME FROM ENTERPRISE_AP WHERE 1 = 1", null);
            while (cursor.moveToNext()) {
                apInfos.add(new ApInfo(cursor.getString(cursor.getColumnIndexOrThrow("SSID")), cursor.getString(cursor.getColumnIndexOrThrow("UPTIME"))));
            }
        } catch (IllegalArgumentException e) {
            LogUtil.d("SELECT SSID,MAC,UPTIME FROM ENTERPRISE_AP WHERE 1 = 1");
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

    public boolean update(ApInfo apInfo) {
        if (apInfo == null) {
            LogUtil.d("update failure,null == place");
            return false;
        }
        try {
            this.db.execSQL("UPDATE ENTERPRISE_AP SET UPTIME = ? WHERE SSID = ? ", new Object[]{apInfo.getUptime(), apInfo.getSsid()});
            return true;
        } catch (SQLException e) {
            LogUtil.e("UPDATE exception: " + e.getMessage());
            return false;
        }
    }

    public boolean remove(String ssid) {
        if (ssid == null || ssid.equals("")) {
            return false;
        }
        try {
            this.db.execSQL("DELETE FROM ENTERPRISE_AP  WHERE SSID = ? ", new Object[]{ssid});
            return true;
        } catch (SQLException e) {
            LogUtil.e("remove exception: " + e.getMessage());
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0042, code lost:
        if (r2 != null) goto L_0x0044;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x007f, code lost:
        if (r2 == null) goto L_0x0082;
     */
    public ApInfo findBySsid(String ssid) {
        ApInfo apInfo = null;
        if (ssid == null || ssid.equals("")) {
            LogUtil.d("findBySsid ssid=null or ssid=");
            return null;
        }
        Cursor cursor = null;
        if (this.db == null) {
            return null;
        }
        try {
            cursor = this.db.rawQuery("SELECT SSID,UPTIME FROM ENTERPRISE_AP WHERE SSID = ? ", new String[]{ssid});
            if (cursor.moveToNext()) {
                apInfo = new ApInfo(cursor.getString(cursor.getColumnIndexOrThrow("SSID")), cursor.getString(cursor.getColumnIndexOrThrow("UPTIME")));
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

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0042, code lost:
        if (r2 != null) goto L_0x0044;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0086, code lost:
        if (r2 == null) goto L_0x0089;
     */
    public ApInfo findBySsidForUpdateTime(String ssid) {
        ApInfo apInfo = null;
        if (ssid == null || ssid.equals("")) {
            LogUtil.d("findBySsidForUpdateTime ssid=null or ssid=");
            return null;
        }
        Cursor cursor = null;
        if (this.db == null) {
            return null;
        }
        try {
            cursor = this.db.rawQuery("SELECT SSID,UPTIME FROM ENTERPRISE_AP WHERE SSID = ? ", new String[]{ssid});
            if (cursor.moveToNext()) {
                apInfo = new ApInfo(cursor.getString(cursor.getColumnIndexOrThrow("SSID")), cursor.getString(cursor.getColumnIndexOrThrow("UPTIME")));
            } else {
                if (cursor != null) {
                    cursor.close();
                }
                return null;
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
