package com.huawei.security.deviceauth;

import android.os.Parcel;
import android.os.Parcelable;

public class ConfirmParams implements Parcelable {
    public static final Parcelable.Creator<ConfirmParams> CREATOR = new Parcelable.Creator<ConfirmParams>() {
        /* class com.huawei.security.deviceauth.ConfirmParams.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ConfirmParams createFromParcel(Parcel in) {
            return new ConfirmParams(in);
        }

        @Override // android.os.Parcelable.Creator
        public ConfirmParams[] newArray(int size) {
            return new ConfirmParams[size];
        }
    };
    private int mConfirmation;
    private int mKeyLength;
    private byte[] mPeerId;
    private int mPeerIdLen;
    private int mPeerType;
    private String mPin;
    private byte[] mSelfId;
    private int mSelfIdLen;
    private int mSelfType;
    private String mServiceType;

    public ConfirmParams() {
        this.mSelfIdLen = 0;
        this.mSelfId = new byte[0];
        this.mPeerIdLen = 0;
        this.mPeerId = new byte[0];
    }

    public ConfirmParams(int confirmation, String pin, int keyLength) {
        this.mConfirmation = confirmation;
        this.mPin = pin;
        this.mKeyLength = keyLength;
        this.mSelfIdLen = 0;
        this.mSelfId = new byte[0];
        this.mPeerIdLen = 0;
        this.mPeerId = new byte[0];
    }

    protected ConfirmParams(Parcel in) {
        this.mConfirmation = in.readInt();
        this.mPin = in.readString();
        this.mKeyLength = in.readInt();
        this.mServiceType = in.readString();
        this.mSelfIdLen = in.readInt();
        this.mSelfType = in.readInt();
        this.mPeerIdLen = in.readInt();
        this.mPeerType = in.readInt();
        int i = this.mSelfIdLen;
        if (i >= 0) {
            this.mSelfId = new byte[i];
            in.readByteArray(this.mSelfId);
        } else {
            this.mSelfId = new byte[0];
            this.mSelfIdLen = 0;
        }
        int i2 = this.mPeerIdLen;
        if (i2 >= 0) {
            this.mPeerId = new byte[i2];
            in.readByteArray(this.mPeerId);
            return;
        }
        this.mPeerId = new byte[0];
        this.mPeerIdLen = 0;
    }

    public int getConfirmation() {
        return this.mConfirmation;
    }

    public void setConfirmation(int confirmation) {
        this.mConfirmation = confirmation;
    }

    public String getPin() {
        return this.mPin;
    }

    public void setPin(String pin) {
        this.mPin = pin;
    }

    public int getKeyLength() {
        return this.mKeyLength;
    }

    public void setKeyLength(int keyLength) {
        this.mKeyLength = keyLength;
    }

    public String getServiceType() {
        return this.mServiceType;
    }

    public void setServiceType(String serviceType) {
        this.mServiceType = serviceType;
    }

    public byte[] getSelfId() {
        return (byte[]) this.mSelfId.clone();
    }

    public int getPeerType() {
        return this.mPeerType;
    }

    public void setPeerType(int peerType) {
        this.mPeerType = peerType;
    }

    public int getSelfType() {
        return this.mSelfType;
    }

    public void setSelfType(int selfType) {
        this.mSelfType = selfType;
    }

    public byte[] getPeerId() {
        return (byte[]) this.mPeerId.clone();
    }

    public void setSelfId(byte[] selfId) {
        if (selfId != null) {
            this.mSelfId = (byte[]) selfId.clone();
            this.mSelfIdLen = selfId.length;
            return;
        }
        this.mSelfId = new byte[0];
        this.mSelfIdLen = 0;
    }

    public void setPeerId(byte[] peerId) {
        if (peerId != null) {
            this.mPeerId = (byte[]) peerId.clone();
            this.mPeerIdLen = peerId.length;
            return;
        }
        this.mPeerId = new byte[0];
        this.mPeerIdLen = 0;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mConfirmation);
        dest.writeString(this.mPin);
        dest.writeInt(this.mKeyLength);
        dest.writeString(this.mServiceType);
        dest.writeInt(this.mSelfIdLen);
        dest.writeInt(this.mSelfType);
        dest.writeInt(this.mPeerIdLen);
        dest.writeInt(this.mPeerType);
        dest.writeByteArray(this.mSelfId);
        dest.writeByteArray(this.mPeerId);
    }
}
