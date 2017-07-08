package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class cf extends fs {
    static ArrayList<String> fb;
    static ArrayList<String> fc;
    public String apkName;
    public String apkPackage;
    public String certMD5;
    public String description;
    public String developer;
    public int downloadCount;
    public String iconUrl;
    public ArrayList<String> imageUrls;
    public boolean isInSoftwareDB;
    public int official;
    public ArrayList<String> sensitivePermissions;
    public long size;
    public String source;
    public int versionCode;
    public String versionName;
    public String virsusDescription;
    public String virsusName;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.cf.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.cf.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.cf.<clinit>():void");
    }

    public cf() {
        this.apkPackage = "";
        this.apkName = "";
        this.iconUrl = "";
        this.versionCode = 0;
        this.versionName = "";
        this.size = 0;
        this.official = 0;
        this.developer = "";
        this.certMD5 = "";
        this.isInSoftwareDB = true;
        this.description = "";
        this.imageUrls = null;
        this.downloadCount = 0;
        this.source = "";
        this.sensitivePermissions = null;
        this.virsusName = "";
        this.virsusDescription = "";
    }

    public fs newInit() {
        return new cf();
    }

    public void readFrom(fq fqVar) {
        this.apkPackage = fqVar.a(0, false);
        this.apkName = fqVar.a(1, false);
        this.iconUrl = fqVar.a(2, false);
        this.versionCode = fqVar.a(this.versionCode, 3, false);
        this.versionName = fqVar.a(4, false);
        this.size = fqVar.a(this.size, 5, false);
        this.official = fqVar.a(this.official, 6, false);
        this.developer = fqVar.a(7, false);
        this.certMD5 = fqVar.a(8, false);
        this.isInSoftwareDB = fqVar.a(this.isInSoftwareDB, 9, false);
        this.description = fqVar.a(10, false);
        this.imageUrls = (ArrayList) fqVar.b(fb, 11, false);
        this.downloadCount = fqVar.a(this.downloadCount, 12, false);
        this.source = fqVar.a(13, false);
        this.sensitivePermissions = (ArrayList) fqVar.b(fc, 14, false);
        this.virsusName = fqVar.a(15, false);
        this.virsusDescription = fqVar.a(16, false);
    }

    public void writeTo(fr frVar) {
        if (this.apkPackage != null) {
            frVar.a(this.apkPackage, 0);
        }
        if (this.apkName != null) {
            frVar.a(this.apkName, 1);
        }
        if (this.iconUrl != null) {
            frVar.a(this.iconUrl, 2);
        }
        if (this.versionCode != 0) {
            frVar.write(this.versionCode, 3);
        }
        if (this.versionName != null) {
            frVar.a(this.versionName, 4);
        }
        if (this.size != 0) {
            frVar.b(this.size, 5);
        }
        if (this.official != 0) {
            frVar.write(this.official, 6);
        }
        if (this.developer != null) {
            frVar.a(this.developer, 7);
        }
        if (this.certMD5 != null) {
            frVar.a(this.certMD5, 8);
        }
        if (!this.isInSoftwareDB) {
            frVar.a(this.isInSoftwareDB, 9);
        }
        if (this.description != null) {
            frVar.a(this.description, 10);
        }
        if (this.imageUrls != null) {
            frVar.a(this.imageUrls, 11);
        }
        if (this.downloadCount != 0) {
            frVar.write(this.downloadCount, 12);
        }
        if (this.source != null) {
            frVar.a(this.source, 13);
        }
        if (this.sensitivePermissions != null) {
            frVar.a(this.sensitivePermissions, 14);
        }
        if (this.virsusName != null) {
            frVar.a(this.virsusName, 15);
        }
        if (this.virsusDescription != null) {
            frVar.a(this.virsusDescription, 16);
        }
    }
}
