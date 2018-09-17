package tmsdkobf;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;
import tmsdk.common.TMSDKContext;
import tmsdk.common.utils.d;

/* compiled from: Unknown */
public class hp {
    private lc ok;

    /* compiled from: Unknown */
    public static class a {
        public String mData;
        public int mType;

        public String toString() {
            return this.mData != null ? "mType: " + this.mType + " mData: " + this.mData : "null";
        }
    }

    public hp() {
        this.ok = ((ln) fe.ad(9)).bp("MeriExtProvider");
    }

    private a a(Cursor cursor) {
        a aVar = new a();
        aVar.mType = cursor.getInt(1);
        aVar.mData = cursor.getString(2);
        return aVar;
    }

    private boolean a(a aVar) {
        long a = this.ok.a("gd_info", d(aVar));
        this.ok.close();
        return !((a > 0 ? 1 : (a == 0 ? 0 : -1)) <= 0);
    }

    private a aF(int i) {
        Cursor a;
        Throwable th;
        a aVar = null;
        try {
            a = this.ok.a("gd_info", null, String.format("%s='%s'", new Object[]{"type", Integer.valueOf(i)}), null, null);
            if (a != null) {
                try {
                    if (a.moveToNext()) {
                        aVar = a(a);
                    }
                } catch (Throwable th2) {
                    th = th2;
                    try {
                        d.c("SharkGuidInfoDao", "getItem() " + th.toString());
                        if (a != null) {
                            a.close();
                        }
                        return aVar;
                    } catch (Throwable th3) {
                        th = th3;
                        if (a != null) {
                            a.close();
                        }
                        throw th;
                    }
                }
            }
            if (a != null) {
                a.close();
            }
        } catch (Throwable th4) {
            th = th4;
            a = null;
            if (a != null) {
                a.close();
            }
            throw th;
        }
        return aVar;
    }

    private boolean b(a aVar) {
        return aVar != null ? aF(aVar.mType) != null ? c(aVar) : a(aVar) : false;
    }

    private boolean c(a aVar) {
        int update = this.ok.update("gd_info", d(aVar), "type=?", new String[]{Long.toString((long) aVar.mType)});
        this.ok.close();
        return update > 0;
    }

    private ContentValues d(a aVar) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("type", Integer.valueOf(aVar.mType));
        contentValues.put("data", aVar.mData);
        return contentValues;
    }

    public boolean a(int i, byte[] bArr) {
        String bytesToHexString = mo.bytesToHexString(bArr);
        a aVar = new a();
        aVar.mType = i;
        aVar.mData = bytesToHexString;
        return b(aVar);
    }

    public String aD(int i) {
        a aF = aF(i);
        return (aF == null || TextUtils.isEmpty(aF.mData)) ? null : ls.c(TMSDKContext.getApplicaionContext(), aF.mData);
    }

    public byte[] aE(int i) {
        a aF = aF(i);
        return (aF == null || TextUtils.isEmpty(aF.mData)) ? null : mo.cw(aF.mData);
    }

    public boolean b(int i, String str) {
        String b = ls.b(TMSDKContext.getApplicaionContext(), str);
        a aVar = new a();
        aVar.mType = i;
        aVar.mData = b;
        return b(aVar);
    }

    public int d(int i, int i2) {
        a aF = aF(i);
        return (aF == null || TextUtils.isEmpty(aF.mData)) ? i2 : Integer.parseInt(ls.c(TMSDKContext.getApplicaionContext(), aF.mData));
    }
}
