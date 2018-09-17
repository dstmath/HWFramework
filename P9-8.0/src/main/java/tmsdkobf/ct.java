package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;

public final class ct extends JceStruct {
    static ArrayList<String> fV = new ArrayList();
    static ArrayList<String> fW = new ArrayList();
    public String apkName = "";
    public String apkPackage = "";
    public String certMD5 = "";
    public String description = "";
    public String developer = "";
    public int downloadCount = 0;
    public String iconUrl = "";
    public ArrayList<String> imageUrls = null;
    public boolean isInSoftwareDB = true;
    public int official = 0;
    public ArrayList<String> sensitivePermissions = null;
    public long size = 0;
    public String source = "";
    public int versionCode = 0;
    public String versionName = "";
    public String virsusDescription = "";
    public String virsusName = "";

    static {
        fV.add("");
        fW.add("");
    }

    public JceStruct newInit() {
        return new ct();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.apkPackage = jceInputStream.readString(0, false);
        this.apkName = jceInputStream.readString(1, false);
        this.iconUrl = jceInputStream.readString(2, false);
        this.versionCode = jceInputStream.read(this.versionCode, 3, false);
        this.versionName = jceInputStream.readString(4, false);
        this.size = jceInputStream.read(this.size, 5, false);
        this.official = jceInputStream.read(this.official, 6, false);
        this.developer = jceInputStream.readString(7, false);
        this.certMD5 = jceInputStream.readString(8, false);
        this.isInSoftwareDB = jceInputStream.read(this.isInSoftwareDB, 9, false);
        this.description = jceInputStream.readString(10, false);
        this.imageUrls = (ArrayList) jceInputStream.read(fV, 11, false);
        this.downloadCount = jceInputStream.read(this.downloadCount, 12, false);
        this.source = jceInputStream.readString(13, false);
        this.sensitivePermissions = (ArrayList) jceInputStream.read(fW, 14, false);
        this.virsusName = jceInputStream.readString(15, false);
        this.virsusDescription = jceInputStream.readString(16, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        if (this.apkPackage != null) {
            jceOutputStream.write(this.apkPackage, 0);
        }
        if (this.apkName != null) {
            jceOutputStream.write(this.apkName, 1);
        }
        if (this.iconUrl != null) {
            jceOutputStream.write(this.iconUrl, 2);
        }
        if (this.versionCode != 0) {
            jceOutputStream.write(this.versionCode, 3);
        }
        if (this.versionName != null) {
            jceOutputStream.write(this.versionName, 4);
        }
        if (this.size != 0) {
            jceOutputStream.write(this.size, 5);
        }
        if (this.official != 0) {
            jceOutputStream.write(this.official, 6);
        }
        if (this.developer != null) {
            jceOutputStream.write(this.developer, 7);
        }
        if (this.certMD5 != null) {
            jceOutputStream.write(this.certMD5, 8);
        }
        if (!this.isInSoftwareDB) {
            jceOutputStream.write(this.isInSoftwareDB, 9);
        }
        if (this.description != null) {
            jceOutputStream.write(this.description, 10);
        }
        if (this.imageUrls != null) {
            jceOutputStream.write(this.imageUrls, 11);
        }
        if (this.downloadCount != 0) {
            jceOutputStream.write(this.downloadCount, 12);
        }
        if (this.source != null) {
            jceOutputStream.write(this.source, 13);
        }
        if (this.sensitivePermissions != null) {
            jceOutputStream.write(this.sensitivePermissions, 14);
        }
        if (this.virsusName != null) {
            jceOutputStream.write(this.virsusName, 15);
        }
        if (this.virsusDescription != null) {
            jceOutputStream.write(this.virsusDescription, 16);
        }
    }
}
