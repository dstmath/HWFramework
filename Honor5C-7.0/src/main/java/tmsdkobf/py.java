package tmsdkobf;

import android.graphics.drawable.Drawable;
import com.huawei.systemmanager.rainbow.comm.request.util.RainbowRequestBasic.CheckVersionField;
import java.util.HashMap;

/* compiled from: Unknown */
public class py extends jf {
    private HashMap<String, Object> Jb;

    public py() {
        this.Jb = new HashMap();
    }

    private String d(Object obj) {
        return obj != null ? !(obj instanceof String) ? obj.toString() : (String) obj : null;
    }

    public void N(boolean z) {
        this.Jb.put("isSystem", Boolean.valueOf(z));
    }

    public void O(boolean z) {
        this.Jb.put("isApk", Boolean.valueOf(z));
    }

    public void aS(String str) {
        this.Jb.put("apkPath", str);
    }

    public String aZ() {
        return d(this.Jb.get("apkPath"));
    }

    public void c(String[] strArr) {
        this.Jb.put("permissions", strArr);
    }

    public void cS(String str) {
        this.Jb.put("pkgName", str);
    }

    public void cT(String str) {
        this.Jb.put("signatureCermMD5", str);
    }

    public void cU(String str) {
        this.Jb.put("signatureCompany", str);
    }

    public Object get(String str) {
        return this.Jb.get(str);
    }

    public String getAppName() {
        return d(this.Jb.get("appName"));
    }

    public Drawable getIcon() {
        Object obj = this.Jb.get("icon");
        return obj == null ? null : (Drawable) obj;
    }

    public String getPackageName() {
        return d(this.Jb.get("pkgName"));
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
        return d(this.Jb.get(CheckVersionField.CHECK_VERSION_VERSION));
    }

    public boolean hA() {
        Object obj = this.Jb.get("isSystem");
        return obj == null ? false : ((Boolean) obj).booleanValue();
    }

    public int hB() {
        Object obj = this.Jb.get("versionCode");
        return obj == null ? 0 : ((Integer) obj).intValue();
    }

    public long hC() {
        Object obj = this.Jb.get("lastModified");
        return obj == null ? 0 : ((Long) obj).longValue();
    }

    public String hD() {
        return d(this.Jb.get("signatureCermMD5"));
    }

    public String hE() {
        return d(this.Jb.get("signatureCompany"));
    }

    public String[] hF() {
        Object obj = this.Jb.get("permissions");
        return obj == null ? null : (String[]) obj;
    }

    public boolean hG() {
        Object obj = this.Jb.get("isApk");
        return obj == null ? false : ((Boolean) obj).booleanValue();
    }

    public void l(int i) {
        this.Jb.put("versionCode", Integer.valueOf(i));
    }

    public void put(String str, Object obj) {
        this.Jb.put(str, obj);
    }

    public void r(String str) {
        this.Jb.put(CheckVersionField.CHECK_VERSION_VERSION, str);
    }

    public void setAppName(String str) {
        this.Jb.put("appName", str);
    }

    public void setIcon(Drawable drawable) {
        this.Jb.put("icon", drawable);
    }

    public void setSize(long j) {
        this.Jb.put("size", Long.valueOf(j));
    }

    public void t(long j) {
        this.Jb.put("lastModified", Long.valueOf(j));
    }
}
