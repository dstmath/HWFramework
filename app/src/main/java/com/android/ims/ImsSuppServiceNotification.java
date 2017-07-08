package com.android.ims;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.telephony.PhoneNumberUtils;
import java.util.Arrays;

public class ImsSuppServiceNotification implements Parcelable {
    public static final Creator<ImsSuppServiceNotification> CREATOR = null;
    private static final String TAG = "ImsSuppServiceNotification";
    public int code;
    public String[] history;
    public int index;
    public int notificationType;
    public String number;
    public int type;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.ims.ImsSuppServiceNotification.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.ims.ImsSuppServiceNotification.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.ims.ImsSuppServiceNotification.<clinit>():void");
    }

    public ImsSuppServiceNotification(Parcel in) {
        readFromParcel(in);
    }

    public String toString() {
        return "{ notificationType=" + this.notificationType + ", code=" + this.code + ", index=" + this.index + ", type=" + this.type + ", number=" + PhoneNumberUtils.toLogSafePhoneNumber(this.number) + ", history=" + PhoneNumberUtils.toLogSafePhoneNumber(Arrays.toString(this.history)) + " }";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.notificationType);
        out.writeInt(this.code);
        out.writeInt(this.index);
        out.writeInt(this.type);
        out.writeString(this.number);
        out.writeStringArray(this.history);
    }

    private void readFromParcel(Parcel in) {
        this.notificationType = in.readInt();
        this.code = in.readInt();
        this.index = in.readInt();
        this.type = in.readInt();
        this.number = in.readString();
        this.history = in.createStringArray();
    }
}
