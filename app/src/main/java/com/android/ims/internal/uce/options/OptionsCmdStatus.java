package com.android.ims.internal.uce.options;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.android.ims.internal.uce.common.CapInfo;
import com.android.ims.internal.uce.common.StatusCode;

public class OptionsCmdStatus implements Parcelable {
    public static final Creator<OptionsCmdStatus> CREATOR = null;
    private CapInfo mCapInfo;
    private OptionsCmdId mCmdId;
    private StatusCode mStatus;
    private int mUserData;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.ims.internal.uce.options.OptionsCmdStatus.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.ims.internal.uce.options.OptionsCmdStatus.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.ims.internal.uce.options.OptionsCmdStatus.<clinit>():void");
    }

    public OptionsCmdId getCmdId() {
        return this.mCmdId;
    }

    public void setCmdId(OptionsCmdId cmdId) {
        this.mCmdId = cmdId;
    }

    public int getUserData() {
        return this.mUserData;
    }

    public void setUserData(int userData) {
        this.mUserData = userData;
    }

    public StatusCode getStatus() {
        return this.mStatus;
    }

    public void setStatus(StatusCode status) {
        this.mStatus = status;
    }

    public OptionsCmdStatus() {
        this.mStatus = new StatusCode();
        this.mCapInfo = new CapInfo();
        this.mCmdId = new OptionsCmdId();
        this.mUserData = 0;
    }

    public CapInfo getCapInfo() {
        return this.mCapInfo;
    }

    public void setCapInfo(CapInfo capInfo) {
        this.mCapInfo = capInfo;
    }

    public static OptionsCmdStatus getOptionsCmdStatusInstance() {
        return new OptionsCmdStatus();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mUserData);
        dest.writeParcelable(this.mCmdId, flags);
        dest.writeParcelable(this.mStatus, flags);
        dest.writeParcelable(this.mCapInfo, flags);
    }

    private OptionsCmdStatus(Parcel source) {
        readFromParcel(source);
    }

    public void readFromParcel(Parcel source) {
        this.mUserData = source.readInt();
        this.mCmdId = (OptionsCmdId) source.readParcelable(OptionsCmdId.class.getClassLoader());
        this.mStatus = (StatusCode) source.readParcelable(StatusCode.class.getClassLoader());
        this.mCapInfo = (CapInfo) source.readParcelable(CapInfo.class.getClassLoader());
    }
}
