package tmsdkobf;

import java.util.HashMap;

public class ov extends ib {
    private HashMap<String, Object> Jb = new HashMap();

    private String e(Object obj) {
        if (obj != null) {
            return !(obj instanceof String) ? obj.toString() : (String) obj;
        } else {
            return null;
        }
    }

    public void P(boolean z) {
        this.Jb.put("isApk", Boolean.valueOf(z));
    }

    public void cm(String str) {
        this.Jb.put("pkgName", str);
    }

    public void cn(String str) {
        this.Jb.put("apkPath", str);
    }

    public Object get(String str) {
        return this.Jb.get(str);
    }

    public String getAppName() {
        return e(this.Jb.get("appName"));
    }

    public String getPackageName() {
        return e(this.Jb.get("pkgName"));
    }

    public long getSize() {
        Object obj = this.Jb.get("size");
        return obj == null ? 0 : ((Long) obj).longValue();
    }

    public int getUid() {
        Object obj = this.Jb.get("uid");
        return obj == null ? 0 : ((Integer) obj).intValue();
    }

    public String getVersion() {
        return e(this.Jb.get("version"));
    }

    public int getVersionCode() {
        Object obj = this.Jb.get("versionCode");
        return obj == null ? 0 : ((Integer) obj).intValue();
    }

    public String[] hA() {
        Object obj = this.Jb.get("permissions");
        return obj == null ? null : (String[]) obj;
    }

    public String hB() {
        return e(this.Jb.get("apkPath"));
    }

    public boolean hC() {
        Object obj = this.Jb.get("isApk");
        return obj == null ? false : ((Boolean) obj).booleanValue();
    }

    public boolean hD() {
        Object obj = this.Jb.get("installedOnSdcard");
        return obj == null ? false : ((Boolean) obj).booleanValue();
    }

    public boolean hx() {
        Object obj = this.Jb.get("isSystem");
        return obj == null ? false : ((Boolean) obj).booleanValue();
    }

    public long hy() {
        Object obj = this.Jb.get("lastModified");
        return obj == null ? 0 : ((Long) obj).longValue();
    }

    public String hz() {
        return e(this.Jb.get("signatureCermMD5"));
    }

    public void put(String str, Object obj) {
        this.Jb.put(str, obj);
    }

    public void setAppName(String str) {
        this.Jb.put("appName", str);
    }
}
