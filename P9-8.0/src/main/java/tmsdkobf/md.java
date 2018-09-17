package tmsdkobf;

import tmsdk.common.TMServiceFactory;

public final class md {
    private String mName;
    private String zJ = ("TMSProperties" + this.mName);

    public md(String str) {
        this.mName = str;
    }

    private jx T() {
        return TMServiceFactory.getPreferenceService(this.zJ);
    }

    private String bV(String str) {
        return str;
    }

    public void a(String str, int i, boolean z) {
        T().putInt(bV(str), i);
    }

    public void a(String str, long j, boolean z) {
        T().putLong(bV(str), j);
    }

    public void a(String str, String str2, boolean z) {
        T().putString(bV(str), str2);
    }

    public void a(String str, boolean z, boolean z2) {
        T().putBoolean(bV(str), z);
    }

    public void beginTransaction() {
        T().beginTransaction();
    }

    public void endTransaction() {
        T().endTransaction();
    }

    public boolean getBoolean(String str, boolean z) {
        return T().getBoolean(bV(str), z);
    }

    public int getInt(String str, int i) {
        return T().getInt(bV(str), i);
    }

    public long getLong(String str, long j) {
        return T().getLong(bV(str), j);
    }

    public String getString(String str, String str2) {
        return T().getString(bV(str), str2);
    }

    public void remove(String str) {
        T().remove(str);
    }
}
