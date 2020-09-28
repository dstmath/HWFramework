package com.mediatek.gba;

import android.os.Parcel;
import android.os.Parcelable;

public class NafSessionKey implements Parcelable {
    public static final Parcelable.Creator<NafSessionKey> CREATOR = new Parcelable.Creator<NafSessionKey>() {
        /* class com.mediatek.gba.NafSessionKey.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public NafSessionKey createFromParcel(Parcel in) {
            NafSessionKey nafSessionKey = new NafSessionKey();
            String btid = in.readString();
            if (btid != null) {
                nafSessionKey.setBtid(btid);
            }
            byte[] key = in.createByteArray();
            if (key != null) {
                nafSessionKey.setKey(key);
            }
            String keylifetime = in.readString();
            if (keylifetime != null) {
                nafSessionKey.setKeylifetime(keylifetime);
            }
            String nafKeyName = in.readString();
            if (nafKeyName != null) {
                nafSessionKey.setNafKeyName(nafKeyName);
            }
            String authHeader = in.readString();
            if (authHeader != null) {
                nafSessionKey.setAuthHeader(authHeader);
            }
            in.readInt();
            String exceptionString = in.readString();
            if (exceptionString != null) {
                nafSessionKey.setException(new IllegalStateException(exceptionString));
            }
            return nafSessionKey;
        }

        @Override // android.os.Parcelable.Creator
        public NafSessionKey[] newArray(int size) {
            return new NafSessionKey[size];
        }
    };
    private String mAuthHeader;
    private String mBtid;
    private Exception mException;
    private byte[] mKey;
    private String mKeylifetime;
    private byte[] mNafId;
    private String mNafKeyName;

    public NafSessionKey() {
    }

    public NafSessionKey(String btid, byte[] key, String keylifetime) {
        this.mBtid = btid;
        this.mKey = key;
        this.mKeylifetime = keylifetime;
    }

    public String getBtid() {
        return this.mBtid;
    }

    public void setBtid(String btid) {
        this.mBtid = btid;
    }

    public byte[] getKey() {
        return this.mKey;
    }

    public void setKey(byte[] key) {
        this.mKey = key;
    }

    public String getKeylifetime() {
        return this.mKeylifetime;
    }

    public void setKeylifetime(String keylifetime) {
        this.mKeylifetime = keylifetime;
    }

    public String getNafKeyName() {
        return this.mNafKeyName;
    }

    public void setNafKeyName(String nafKeyName) {
        this.mNafKeyName = nafKeyName;
    }

    public void setNafId(byte[] nafId) {
        this.mNafId = nafId;
    }

    public byte[] getNafId() {
        return this.mNafId;
    }

    public String getAuthHeader() {
        return this.mAuthHeader;
    }

    public void setAuthHeader(String authHeader) {
        this.mAuthHeader = authHeader;
    }

    public void setException(Exception e) {
        this.mException = e;
    }

    public Exception getException() {
        return this.mException;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mBtid);
        dest.writeByteArray(this.mKey);
        dest.writeString(this.mKeylifetime);
        dest.writeString(this.mNafKeyName);
        dest.writeString(this.mAuthHeader);
        Exception exc = this.mException;
        if (exc != null) {
            dest.writeException(exc);
        }
    }

    public String toString() {
        String sb;
        synchronized (this) {
            StringBuilder builder = new StringBuilder("NafSessionKey -");
            builder.append(" btid: " + this.mBtid);
            builder.append(" keylifetime: " + this.mKeylifetime);
            builder.append(" nafkeyname: " + this.mNafKeyName);
            builder.append(" authheader: " + this.mAuthHeader);
            sb = builder.toString();
        }
        return sb;
    }
}
