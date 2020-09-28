package com.huawei.security.keymaster;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import java.nio.charset.StandardCharsets;
import org.json.JSONException;
import org.json.JSONObject;

public class HwKeymasterBlob implements Parcelable {
    public static final Parcelable.Creator<HwKeymasterBlob> CREATOR = new Parcelable.Creator<HwKeymasterBlob>() {
        /* class com.huawei.security.keymaster.HwKeymasterBlob.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public HwKeymasterBlob createFromParcel(Parcel in) {
            return new HwKeymasterBlob(in);
        }

        @Override // android.os.Parcelable.Creator
        public HwKeymasterBlob[] newArray(int length) {
            return new HwKeymasterBlob[length];
        }
    };
    private static final String TAG = "HwKeymasterBlob";
    public byte[] blob;

    public HwKeymasterBlob(byte[] blob2) {
        this.blob = blob2;
    }

    protected HwKeymasterBlob(Parcel in) {
        this.blob = in.createByteArray();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeByteArray(this.blob);
    }

    public JSONObject getBlob() {
        Log.d(TAG, "getBlob");
        byte[] bArr = this.blob;
        if (bArr.length <= 0 || bArr.length >= 4096) {
            Log.e(TAG, "invalid blob length: " + this.blob.length);
            return null;
        }
        String str = new String(bArr, StandardCharsets.ISO_8859_1);
        Log.d(TAG, "blob data = " + str);
        try {
            return new JSONObject(str);
        } catch (JSONException e) {
            Log.e(TAG, "create json exception");
            return null;
        }
    }
}
