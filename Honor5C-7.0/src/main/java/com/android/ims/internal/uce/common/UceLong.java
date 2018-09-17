package com.android.ims.internal.uce.common;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.android.internal.telephony.RILConstants;

public class UceLong implements Parcelable {
    public static final Creator<UceLong> CREATOR = null;
    private int mClientId;
    private long mUceLong;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.ims.internal.uce.common.UceLong.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.ims.internal.uce.common.UceLong.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.ims.internal.uce.common.UceLong.<clinit>():void");
    }

    public UceLong() {
        this.mClientId = RILConstants.RIL_UNSOL_RESPONSE_CALL_STATE_CHANGED;
    }

    public long getUceLong() {
        return this.mUceLong;
    }

    public void setUceLong(long uceLong) {
        this.mUceLong = uceLong;
    }

    public int getClientId() {
        return this.mClientId;
    }

    public void setClientId(int nClientId) {
        this.mClientId = nClientId;
    }

    public static UceLong getUceLongInstance() {
        return new UceLong();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        writeToParcel(dest);
    }

    private void writeToParcel(Parcel out) {
        out.writeLong(this.mUceLong);
        out.writeInt(this.mClientId);
    }

    private UceLong(Parcel source) {
        this.mClientId = RILConstants.RIL_UNSOL_RESPONSE_CALL_STATE_CHANGED;
        readFromParcel(source);
    }

    public void readFromParcel(Parcel source) {
        this.mUceLong = source.readLong();
        this.mClientId = source.readInt();
    }
}
