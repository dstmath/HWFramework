package tmsdk.common.module.urlcheck;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

/* compiled from: Unknown */
public class WebPageDetail implements Parcelable {
    public static Creator<WebPageDetail> CREATOR;
    public String description;
    public String flawName;
    public String maliceBody;
    public String maliceTitle;
    public long maliceType;
    public String screenshotUrl;
    public String title;
    public String webIconUrl;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdk.common.module.urlcheck.WebPageDetail.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdk.common.module.urlcheck.WebPageDetail.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdk.common.module.urlcheck.WebPageDetail.<clinit>():void");
    }

    public WebPageDetail() {
        this.title = "";
        this.description = "";
        this.webIconUrl = "";
        this.screenshotUrl = "";
        this.maliceType = 0;
        this.maliceTitle = "";
        this.maliceBody = "";
        this.flawName = "";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.title);
        parcel.writeString(this.description);
        parcel.writeString(this.webIconUrl);
        parcel.writeString(this.screenshotUrl);
        parcel.writeLong(this.maliceType);
        parcel.writeString(this.maliceTitle);
        parcel.writeString(this.maliceBody);
        parcel.writeString(this.flawName);
    }
}
