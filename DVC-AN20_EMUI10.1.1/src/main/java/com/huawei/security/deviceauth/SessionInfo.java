package com.huawei.security.deviceauth;

import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;

public class SessionInfo implements Parcelable {
    public static final Parcelable.Creator<SessionInfo> CREATOR = new Parcelable.Creator<SessionInfo>() {
        /* class com.huawei.security.deviceauth.SessionInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public SessionInfo createFromParcel(Parcel in) {
            return new SessionInfo(in);
        }

        @Override // android.os.Parcelable.Creator
        public SessionInfo[] newArray(int size) {
            return new SessionInfo[size];
        }
    };
    private byte[] mPeerId;
    private int mPeerIdLen;
    private int mPeerType;
    private byte[] mSelfId;
    private int mSelfIdLen;
    private int mSelfType;
    private String mServiceType;
    private String mSessionId;
    private IBinder mToken;

    public SessionInfo(String sessionId, String serviceType, byte[] selfId, int selfType, byte[] peerId, int peerType, IBinder token) {
        this.mSessionId = sessionId;
        this.mServiceType = serviceType;
        if (selfId != null) {
            this.mSelfId = (byte[]) selfId.clone();
            this.mSelfIdLen = selfId.length;
        } else {
            this.mSelfId = new byte[0];
            this.mSelfIdLen = 0;
        }
        this.mSelfType = selfType;
        if (peerId != null) {
            this.mPeerId = (byte[]) peerId.clone();
            this.mPeerIdLen = peerId.length;
        } else {
            this.mPeerId = new byte[0];
            this.mPeerIdLen = 0;
        }
        this.mPeerType = peerType;
        this.mToken = token;
    }

    protected SessionInfo(Parcel in) {
        this.mSessionId = in.readString();
        this.mServiceType = in.readString();
        this.mSelfIdLen = in.readInt();
        this.mSelfType = in.readInt();
        this.mPeerIdLen = in.readInt();
        this.mPeerType = in.readInt();
        this.mToken = in.readStrongBinder();
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

    public String getSessionId() {
        return this.mSessionId;
    }

    public void setSessionId(String sessionId) {
        this.mSessionId = sessionId;
    }

    public String getServiceType() {
        return this.mServiceType;
    }

    public void setServiceType(String serviceType) {
        this.mServiceType = serviceType;
    }

    public byte[] getSelfId() {
        byte[] bArr = this.mSelfId;
        if (bArr == null) {
            return null;
        }
        return (byte[]) bArr.clone();
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
        byte[] bArr = this.mPeerId;
        if (bArr == null) {
            return null;
        }
        return (byte[]) bArr.clone();
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

    public IBinder getToken() {
        return this.mToken;
    }

    public void setToken(IBinder token) {
        this.mToken = token;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mSessionId);
        dest.writeString(this.mServiceType);
        dest.writeInt(this.mSelfIdLen);
        dest.writeInt(this.mSelfType);
        dest.writeInt(this.mPeerIdLen);
        dest.writeInt(this.mPeerType);
        dest.writeStrongBinder(this.mToken);
        dest.writeByteArray(this.mSelfId);
        dest.writeByteArray(this.mPeerId);
    }
}
