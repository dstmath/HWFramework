package tmsdkobf;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;
import tmsdk.common.TMSDKContext;

public class gm {
    private jv om = ((kf) fj.D(9)).ap("MeriExtProvider");

    public static class a {
        public String mData;
        public int mType;

        public String toString() {
            return this.mData != null ? "mType: " + this.mType + " mData: " + this.mData : "null";
        }
    }

    private a N(int i) {
        a aVar = null;
        Cursor cursor = null;
        try {
            cursor = this.om.a("gd_info", null, String.format("%s='%s'", new Object[]{"type", Integer.valueOf(i)}), null, null);
            if (cursor != null && cursor.moveToNext()) {
                aVar = a(cursor);
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return aVar;
    }

    private a a(Cursor cursor) {
        a aVar = new a();
        aVar.mType = cursor.getInt(1);
        aVar.mData = cursor.getString(2);
        return aVar;
    }

    private boolean a(a aVar) {
        long a = this.om.a("gd_info", d(aVar));
        this.om.close();
        return !((a > 0 ? 1 : (a == 0 ? 0 : -1)) <= 0);
    }

    private boolean b(a aVar) {
        if (aVar != null) {
            return N(aVar.mType) != null ? c(aVar) : a(aVar);
        } else {
            return false;
        }
    }

    private boolean c(a aVar) {
        int update = this.om.update("gd_info", d(aVar), "type=?", new String[]{Long.toString((long) aVar.mType)});
        this.om.close();
        return update > 0;
    }

    private ContentValues d(a aVar) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("type", Integer.valueOf(aVar.mType));
        contentValues.put("data", aVar.mData);
        return contentValues;
    }

    public String L(int i) {
        a N = N(i);
        return (N == null || TextUtils.isEmpty(N.mData)) ? null : kk.d(TMSDKContext.getApplicaionContext(), N.mData);
    }

    public byte[] M(int i) {
        a N = N(i);
        return (N == null || TextUtils.isEmpty(N.mData)) ? null : com.tencent.tcuser.util.a.at(N.mData);
    }

    public boolean a(int i, byte[] bArr) {
        String bytesToHexString = com.tencent.tcuser.util.a.bytesToHexString(bArr);
        a aVar = new a();
        aVar.mType = i;
        aVar.mData = bytesToHexString;
        return b(aVar);
    }

    public boolean b(int i, String str) {
        String c = kk.c(TMSDKContext.getApplicaionContext(), str);
        a aVar = new a();
        aVar.mType = i;
        aVar.mData = c;
        return b(aVar);
    }

    public int g(int i, int i2) {
        a N = N(i);
        return (N == null || TextUtils.isEmpty(N.mData)) ? i2 : Integer.parseInt(kk.d(TMSDKContext.getApplicaionContext(), N.mData));
    }
}
