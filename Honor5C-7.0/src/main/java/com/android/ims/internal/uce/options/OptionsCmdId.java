package com.android.ims.internal.uce.options;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class OptionsCmdId implements Parcelable {
    public static final Creator<OptionsCmdId> CREATOR = null;
    public static final int UCE_OPTIONS_CMD_GETCONTACTCAP = 2;
    public static final int UCE_OPTIONS_CMD_GETCONTACTLISTCAP = 3;
    public static final int UCE_OPTIONS_CMD_GETMYCDINFO = 0;
    public static final int UCE_OPTIONS_CMD_GET_VERSION = 5;
    public static final int UCE_OPTIONS_CMD_RESPONSEINCOMINGOPTIONS = 4;
    public static final int UCE_OPTIONS_CMD_SETMYCDINFO = 1;
    public static final int UCE_OPTIONS_CMD_UNKNOWN = 6;
    private int mCmdId;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.ims.internal.uce.options.OptionsCmdId.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.ims.internal.uce.options.OptionsCmdId.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.ims.internal.uce.options.OptionsCmdId.<clinit>():void");
    }

    public int getCmdId() {
        return this.mCmdId;
    }

    public void setCmdId(int nCmdId) {
        this.mCmdId = nCmdId;
    }

    public OptionsCmdId() {
        this.mCmdId = UCE_OPTIONS_CMD_UNKNOWN;
    }

    public int describeContents() {
        return UCE_OPTIONS_CMD_GETMYCDINFO;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mCmdId);
    }

    private OptionsCmdId(Parcel source) {
        this.mCmdId = UCE_OPTIONS_CMD_UNKNOWN;
        readFromParcel(source);
    }

    public void readFromParcel(Parcel source) {
        this.mCmdId = source.readInt();
    }
}
