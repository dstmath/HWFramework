package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class UiccAuthResponse implements Parcelable {
    public static final int AUTH_RESP_FAIL = 1;
    public static final int AUTH_RESP_SUCCESS = 0;
    public static final int AUTH_RESP_SYNC_FAIL = 2;
    public static final int AUTH_RESP_UNSUPPORTED = 3;
    public static final Creator<UiccAuthResponse> CREATOR = null;
    public int mResult;
    public UiccAuthChallenge mUiccAuthChallenge;
    public UiccAuthResponseData mUiccAuthSyncFail;

    public static class UiccAuthChallenge {
        public UiccAuthResponseData mCkData;
        public UiccAuthResponseData mIkData;
        public UiccAuthResponseData mResData;
    }

    public static class UiccAuthResponseData {
        public byte[] data;
        public int len;
        public int present;
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.telephony.UiccAuthResponse.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.telephony.UiccAuthResponse.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.telephony.UiccAuthResponse.<clinit>():void");
    }

    public UiccAuthResponse() {
        this.mUiccAuthChallenge = new UiccAuthChallenge();
        this.mUiccAuthSyncFail = new UiccAuthResponseData();
    }

    private UiccAuthResponse(Parcel in) {
        this.mUiccAuthChallenge = new UiccAuthChallenge();
        this.mUiccAuthSyncFail = new UiccAuthResponseData();
        this.mResult = in.readInt();
        if (this.mResult == 0) {
            this.mUiccAuthChallenge.mResData = new UiccAuthResponseData();
            this.mUiccAuthChallenge.mResData.present = in.readInt();
            this.mUiccAuthChallenge.mResData.len = in.readInt();
            if (this.mUiccAuthChallenge.mResData.len > 0) {
                this.mUiccAuthChallenge.mResData.data = in.createByteArray();
            } else {
                this.mUiccAuthChallenge.mResData.data = null;
                in.readInt();
            }
            this.mUiccAuthChallenge.mIkData = new UiccAuthResponseData();
            this.mUiccAuthChallenge.mIkData.present = in.readInt();
            this.mUiccAuthChallenge.mIkData.len = in.readInt();
            if (this.mUiccAuthChallenge.mIkData.len > 0) {
                this.mUiccAuthChallenge.mIkData.data = in.createByteArray();
            } else {
                this.mUiccAuthChallenge.mIkData.data = null;
                in.readInt();
            }
            this.mUiccAuthChallenge.mCkData = new UiccAuthResponseData();
            this.mUiccAuthChallenge.mCkData.present = in.readInt();
            this.mUiccAuthChallenge.mCkData.len = in.readInt();
            if (this.mUiccAuthChallenge.mCkData.len > 0) {
                this.mUiccAuthChallenge.mCkData.data = in.createByteArray();
                return;
            }
            this.mUiccAuthChallenge.mCkData.data = null;
            return;
        }
        this.mUiccAuthSyncFail.present = in.readInt();
        this.mUiccAuthSyncFail.len = in.readInt();
        if (this.mUiccAuthSyncFail.len > 0) {
            this.mUiccAuthSyncFail.data = in.createByteArray();
            return;
        }
        this.mUiccAuthSyncFail.data = null;
    }

    public int getResult() {
        return this.mResult;
    }

    public UiccAuthChallenge getAuthChallenge() {
        return this.mUiccAuthChallenge;
    }

    public UiccAuthResponseData getAuthSyncFail() {
        return this.mUiccAuthSyncFail;
    }

    public int describeContents() {
        return AUTH_RESP_SUCCESS;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mResult);
        if (this.mResult == 0) {
            out.writeInt(this.mUiccAuthChallenge.mResData.present);
            out.writeInt(this.mUiccAuthChallenge.mResData.len);
            if (this.mUiccAuthChallenge.mResData.len != 0) {
                out.writeByteArray(this.mUiccAuthChallenge.mResData.data, AUTH_RESP_SUCCESS, this.mUiccAuthChallenge.mResData.len);
            } else {
                out.writeInt(AUTH_RESP_SUCCESS);
            }
            out.writeInt(this.mUiccAuthChallenge.mIkData.present);
            out.writeInt(this.mUiccAuthChallenge.mIkData.len);
            if (this.mUiccAuthChallenge.mIkData.len != 0) {
                out.writeByteArray(this.mUiccAuthChallenge.mIkData.data, AUTH_RESP_SUCCESS, this.mUiccAuthChallenge.mIkData.len);
            } else {
                out.writeInt(AUTH_RESP_SUCCESS);
            }
            out.writeInt(this.mUiccAuthChallenge.mCkData.present);
            out.writeInt(this.mUiccAuthChallenge.mCkData.len);
            if (this.mUiccAuthChallenge.mCkData.len != 0) {
                out.writeByteArray(this.mUiccAuthChallenge.mCkData.data, AUTH_RESP_SUCCESS, this.mUiccAuthChallenge.mCkData.len);
                return;
            } else {
                out.writeInt(AUTH_RESP_SUCCESS);
                return;
            }
        }
        out.writeInt(this.mUiccAuthSyncFail.present);
        out.writeInt(this.mUiccAuthSyncFail.len);
        if (this.mUiccAuthSyncFail.len != 0) {
            out.writeByteArray(this.mUiccAuthSyncFail.data, AUTH_RESP_SUCCESS, this.mUiccAuthSyncFail.len);
        } else {
            out.writeInt(AUTH_RESP_SUCCESS);
        }
    }
}
