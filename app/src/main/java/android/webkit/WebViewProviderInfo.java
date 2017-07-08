package android.webkit;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class WebViewProviderInfo implements Parcelable {
    public static final Creator<WebViewProviderInfo> CREATOR = null;
    public final boolean availableByDefault;
    public final String description;
    public final boolean isFallback;
    public final String packageName;
    public final String[] signatures;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.webkit.WebViewProviderInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.webkit.WebViewProviderInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.webkit.WebViewProviderInfo.<clinit>():void");
    }

    public WebViewProviderInfo(String packageName, String description, boolean availableByDefault, boolean isFallback, String[] signatures) {
        this.packageName = packageName;
        this.description = description;
        this.availableByDefault = availableByDefault;
        this.isFallback = isFallback;
        this.signatures = signatures;
    }

    private WebViewProviderInfo(Parcel in) {
        boolean z;
        boolean z2 = true;
        this.packageName = in.readString();
        this.description = in.readString();
        if (in.readInt() > 0) {
            z = true;
        } else {
            z = false;
        }
        this.availableByDefault = z;
        if (in.readInt() <= 0) {
            z2 = false;
        }
        this.isFallback = z2;
        this.signatures = in.createStringArray();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        int i;
        int i2 = 1;
        out.writeString(this.packageName);
        out.writeString(this.description);
        if (this.availableByDefault) {
            i = 1;
        } else {
            i = 0;
        }
        out.writeInt(i);
        if (!this.isFallback) {
            i2 = 0;
        }
        out.writeInt(i2);
        out.writeStringArray(this.signatures);
    }
}
