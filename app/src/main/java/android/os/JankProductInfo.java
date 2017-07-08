package android.os;

import android.os.Parcelable.Creator;

public class JankProductInfo implements Parcelable {
    public static final Creator<JankProductInfo> CREATOR = null;
    public static final String DEFAULT_DEVICE_ID = "000000000000000";
    public String productIMEI;
    public String productName;
    public String productSN;
    public String productVersion;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.os.JankProductInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.os.JankProductInfo.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.os.JankProductInfo.<clinit>():void");
    }

    public JankProductInfo() {
        this.productName = SystemProperties.get("ro.product.name", "NULL");
        this.productSN = SystemProperties.get("ro.serialno", "NULL");
        this.productVersion = getVersionString();
    }

    private JankProductInfo(Parcel in) {
        this.productName = SystemProperties.get("ro.product.name", "NULL");
        this.productSN = SystemProperties.get("ro.serialno", "NULL");
        this.productName = in.readString();
        this.productVersion = in.readString();
        this.productSN = in.readString();
        this.productIMEI = in.readString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flag) {
        dest.writeString(this.productName);
        dest.writeString(this.productVersion);
        dest.writeString(this.productSN);
        dest.writeString(this.productIMEI);
    }

    public static String getVersionString() {
        int i = 0;
        String[] version = new String[]{SystemProperties.get("ro.build.realversion.id", "NULL"), SystemProperties.get("ro.build.cust.id", "NULL"), SystemProperties.get("ro.build.display.id", "NULL")};
        String fullVersionId = Build.DISPLAY;
        int length = version.length;
        while (i < length) {
            String s = version[i];
            if (!"NULL".equals(s)) {
                return s;
            }
            i++;
        }
        return fullVersionId;
    }
}
