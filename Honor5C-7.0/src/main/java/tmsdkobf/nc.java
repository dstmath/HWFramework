package tmsdkobf;

import tmsdk.common.TMServiceFactory;

/* compiled from: Unknown */
public final class nc {
    private String BX;
    private String mName;

    public nc(String str) {
        this.mName = str;
        this.BX = "TMSProperties" + this.mName;
    }

    private String cH(String str) {
        return str;
    }

    private lf x() {
        return TMServiceFactory.getPreferenceService(this.BX);
    }

    public void a(String str, int i, boolean z) {
        x().e(cH(str), i);
        if (!z) {
        }
    }

    public void a(String str, long j, boolean z) {
        x().d(cH(str), j);
        if (!z) {
        }
    }

    public void a(String str, String str2, boolean z) {
        x().m(cH(str), str2);
        if (!z) {
        }
    }

    public void a(String str, boolean z, boolean z2) {
        x().d(cH(str), z);
        if (!z2) {
        }
    }

    public void beginTransaction() {
        x().beginTransaction();
    }

    public boolean dj() {
        return x().dj();
    }

    public boolean getBoolean(String str, boolean z) {
        return x().getBoolean(cH(str), z);
    }

    public int getInt(String str, int i) {
        return x().getInt(cH(str), i);
    }

    public long getLong(String str, long j) {
        return x().getLong(cH(str), j);
    }

    public String getString(String str, String str2) {
        return x().getString(cH(str), str2);
    }
}
