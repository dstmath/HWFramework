package tmsdk.fg.module.qscanner;

import tmsdkobf.fq;
import tmsdkobf.fr;
import tmsdkobf.fs;

/* compiled from: Unknown */
public final class ApkKey extends fs {
    public int apkType;
    public String certMd5;
    public String path;
    public String pkgName;
    public int size;
    public String softName;
    public String version;
    public int versionCode;

    public ApkKey() {
        this.pkgName = "";
        this.softName = "";
        this.certMd5 = "";
        this.version = "";
        this.versionCode = 0;
        this.size = 0;
        this.path = "";
        this.apkType = 0;
    }

    public ApkKey(String str, String str2, String str3, String str4, int i, int i2, String str5, int i3) {
        this.pkgName = "";
        this.softName = "";
        this.certMd5 = "";
        this.version = "";
        this.versionCode = 0;
        this.size = 0;
        this.path = "";
        this.apkType = 0;
        this.pkgName = str;
        this.softName = str2;
        this.certMd5 = str3;
        this.version = str4;
        this.versionCode = i;
        this.size = i2;
        this.path = str5;
        this.apkType = i3;
    }

    public void readFrom(fq fqVar) {
        this.pkgName = fqVar.a(0, true);
        this.softName = fqVar.a(1, true);
        this.certMd5 = fqVar.a(2, true);
        this.version = fqVar.a(3, true);
        this.versionCode = fqVar.a(this.versionCode, 4, true);
        this.size = fqVar.a(this.size, 5, true);
        this.path = fqVar.a(6, false);
        this.apkType = fqVar.a(this.apkType, 7, false);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.pkgName, 0);
        frVar.a(this.softName, 1);
        frVar.a(this.certMd5, 2);
        frVar.a(this.version, 3);
        frVar.write(this.versionCode, 4);
        frVar.write(this.size, 5);
        if (this.path != null) {
            frVar.a(this.path, 6);
        }
        frVar.write(this.apkType, 7);
    }
}
