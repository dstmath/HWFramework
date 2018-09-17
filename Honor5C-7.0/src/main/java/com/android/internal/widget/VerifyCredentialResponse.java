package com.android.internal.widget;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class VerifyCredentialResponse implements Parcelable {
    public static final Creator<VerifyCredentialResponse> CREATOR = null;
    public static final VerifyCredentialResponse ERROR = null;
    public static final VerifyCredentialResponse OK = null;
    public static final int RESPONSE_ERROR = -1;
    public static final int RESPONSE_OK = 0;
    public static final int RESPONSE_RETRY = 1;
    private byte[] mPayload;
    private int mResponseCode;
    private int mTimeout;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.widget.VerifyCredentialResponse.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.widget.VerifyCredentialResponse.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.VerifyCredentialResponse.<clinit>():void");
    }

    public VerifyCredentialResponse() {
        this.mResponseCode = RESPONSE_OK;
        this.mPayload = null;
    }

    public VerifyCredentialResponse(byte[] payload) {
        this.mPayload = payload;
        this.mResponseCode = RESPONSE_OK;
    }

    public VerifyCredentialResponse(int timeout) {
        this.mTimeout = timeout;
        this.mResponseCode = RESPONSE_RETRY;
        this.mPayload = null;
    }

    private VerifyCredentialResponse(int responseCode, int timeout, byte[] payload) {
        this.mResponseCode = responseCode;
        this.mTimeout = timeout;
        this.mPayload = payload;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mResponseCode);
        if (this.mResponseCode == RESPONSE_RETRY) {
            dest.writeInt(this.mTimeout);
        } else if (this.mResponseCode == 0 && this.mPayload != null) {
            dest.writeInt(this.mPayload.length);
            dest.writeByteArray(this.mPayload);
        }
    }

    public int describeContents() {
        return RESPONSE_OK;
    }

    public byte[] getPayload() {
        return this.mPayload;
    }

    public int getTimeout() {
        return this.mTimeout;
    }

    public int getResponseCode() {
        return this.mResponseCode;
    }

    private void setTimeout(int timeout) {
        this.mTimeout = timeout;
    }

    private void setPayload(byte[] payload) {
        this.mPayload = payload;
    }
}
