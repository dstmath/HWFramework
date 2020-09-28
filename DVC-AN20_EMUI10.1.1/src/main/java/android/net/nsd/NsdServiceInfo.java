package android.net.nsd;

import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Base64;
import android.util.Log;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

public final class NsdServiceInfo implements Parcelable {
    public static final Parcelable.Creator<NsdServiceInfo> CREATOR = new Parcelable.Creator<NsdServiceInfo>() {
        /* class android.net.nsd.NsdServiceInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public NsdServiceInfo createFromParcel(Parcel in) {
            NsdServiceInfo info = new NsdServiceInfo();
            info.mServiceName = in.readString();
            info.mServiceType = in.readString();
            if (in.readInt() == 1) {
                try {
                    info.mHost = InetAddress.getByAddress(in.createByteArray());
                } catch (UnknownHostException e) {
                }
            }
            info.mPort = in.readInt();
            int recordCount = in.readInt();
            for (int i = 0; i < recordCount; i++) {
                byte[] valueArray = null;
                if (in.readInt() == 1) {
                    valueArray = new byte[in.readInt()];
                    in.readByteArray(valueArray);
                }
                info.mTxtRecord.put(in.readString(), valueArray);
            }
            return info;
        }

        @Override // android.os.Parcelable.Creator
        public NsdServiceInfo[] newArray(int size) {
            return new NsdServiceInfo[size];
        }
    };
    private static final String TAG = "NsdServiceInfo";
    private InetAddress mHost;
    private int mPort;
    private String mServiceName;
    private String mServiceType;
    private final ArrayMap<String, byte[]> mTxtRecord = new ArrayMap<>();

    public NsdServiceInfo() {
    }

    public NsdServiceInfo(String sn, String rt) {
        this.mServiceName = sn;
        this.mServiceType = rt;
    }

    public String getServiceName() {
        return this.mServiceName;
    }

    public void setServiceName(String s) {
        this.mServiceName = s;
    }

    public String getServiceType() {
        return this.mServiceType;
    }

    public void setServiceType(String s) {
        this.mServiceType = s;
    }

    public InetAddress getHost() {
        return this.mHost;
    }

    public void setHost(InetAddress s) {
        this.mHost = s;
    }

    public int getPort() {
        return this.mPort;
    }

    public void setPort(int p) {
        this.mPort = p;
    }

    public void setTxtRecords(String rawRecords) {
        byte[] txtRecordsRawBytes = Base64.decode(rawRecords, 0);
        int pos = 0;
        while (pos < txtRecordsRawBytes.length) {
            int recordLen = txtRecordsRawBytes[pos] & 255;
            int pos2 = pos + 1;
            if (recordLen != 0) {
                try {
                    if (pos2 + recordLen > txtRecordsRawBytes.length) {
                        Log.w(TAG, "Corrupt record length (pos = " + pos2 + "): " + recordLen);
                        recordLen = txtRecordsRawBytes.length - pos2;
                    }
                    String key = null;
                    byte[] value = null;
                    int valueLen = 0;
                    for (int i = pos2; i < pos2 + recordLen; i++) {
                        if (key != null) {
                            if (value == null) {
                                value = new byte[((recordLen - key.length()) - 1)];
                            }
                            value[valueLen] = txtRecordsRawBytes[i];
                            valueLen++;
                        } else if (txtRecordsRawBytes[i] == 61) {
                            key = new String(txtRecordsRawBytes, pos2, i - pos2, StandardCharsets.US_ASCII);
                        }
                    }
                    if (key == null) {
                        key = new String(txtRecordsRawBytes, pos2, recordLen, StandardCharsets.US_ASCII);
                    }
                    if (TextUtils.isEmpty(key)) {
                        throw new IllegalArgumentException("Invalid txt record (key is empty)");
                    } else if (!getAttributes().containsKey(key)) {
                        setAttribute(key, value);
                        pos = pos2 + recordLen;
                    } else {
                        throw new IllegalArgumentException("Invalid txt record (duplicate key \"" + key + "\")");
                    }
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "While parsing txt records (pos = " + pos2 + "): " + e.getMessage());
                }
            } else {
                throw new IllegalArgumentException("Zero sized txt record");
            }
        }
    }

    @UnsupportedAppUsage
    public void setAttribute(String key, byte[] value) {
        if (!TextUtils.isEmpty(key)) {
            for (int i = 0; i < key.length(); i++) {
                char character = key.charAt(i);
                if (character < ' ' || character > '~') {
                    throw new IllegalArgumentException("Key strings must be printable US-ASCII");
                } else if (character == '=') {
                    throw new IllegalArgumentException("Key strings must not include '='");
                }
            }
            int i2 = 0;
            if (key.length() + (value == null ? 0 : value.length) < 255) {
                if (key.length() > 9) {
                    Log.w(TAG, "Key lengths > 9 are discouraged: " + key);
                }
                int length = key.length() + getTxtRecordSize();
                if (value != null) {
                    i2 = value.length;
                }
                int futureSize = length + i2 + 2;
                if (futureSize <= 1300) {
                    if (futureSize > 400) {
                        Log.w(TAG, "Total length of all attributes exceeds 400 bytes; truncation may occur");
                    }
                    this.mTxtRecord.put(key, value);
                    return;
                }
                throw new IllegalArgumentException("Total length of attributes must be < 1300 bytes");
            }
            throw new IllegalArgumentException("Key length + value length must be < 255 bytes");
        }
        throw new IllegalArgumentException("Key cannot be empty");
    }

    public void setAttribute(String key, String value) {
        byte[] bArr;
        if (value == null) {
            try {
                bArr = null;
            } catch (UnsupportedEncodingException e) {
                throw new IllegalArgumentException("Value must be UTF-8");
            }
        } else {
            bArr = value.getBytes("UTF-8");
        }
        setAttribute(key, bArr);
    }

    public void removeAttribute(String key) {
        this.mTxtRecord.remove(key);
    }

    public Map<String, byte[]> getAttributes() {
        return Collections.unmodifiableMap(this.mTxtRecord);
    }

    private int getTxtRecordSize() {
        int txtRecordSize = 0;
        for (Map.Entry<String, byte[]> entry : this.mTxtRecord.entrySet()) {
            int txtRecordSize2 = txtRecordSize + 2 + entry.getKey().length();
            byte[] value = entry.getValue();
            txtRecordSize = txtRecordSize2 + (value == null ? 0 : value.length);
        }
        return txtRecordSize;
    }

    public byte[] getTxtRecord() {
        int txtRecordSize = getTxtRecordSize();
        if (txtRecordSize == 0) {
            return new byte[0];
        }
        byte[] txtRecord = new byte[txtRecordSize];
        int ptr = 0;
        for (Map.Entry<String, byte[]> entry : this.mTxtRecord.entrySet()) {
            String key = entry.getKey();
            byte[] value = entry.getValue();
            int ptr2 = ptr + 1;
            txtRecord[ptr] = (byte) (key.length() + (value == null ? 0 : value.length) + 1);
            System.arraycopy(key.getBytes(StandardCharsets.US_ASCII), 0, txtRecord, ptr2, key.length());
            int ptr3 = ptr2 + key.length();
            ptr = ptr3 + 1;
            txtRecord[ptr3] = 61;
            if (value != null) {
                System.arraycopy(value, 0, txtRecord, ptr, value.length);
                ptr += value.length;
            }
        }
        return txtRecord;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("name: ");
        sb.append(this.mServiceName);
        sb.append(", type: ");
        sb.append(this.mServiceType);
        sb.append(", host: ");
        sb.append(this.mHost);
        sb.append(", port: ");
        sb.append(this.mPort);
        byte[] txtRecord = getTxtRecord();
        if (txtRecord != null) {
            sb.append(", txtRecord: ");
            sb.append(new String(txtRecord, StandardCharsets.UTF_8));
        }
        return sb.toString();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mServiceName);
        dest.writeString(this.mServiceType);
        if (this.mHost != null) {
            dest.writeInt(1);
            dest.writeByteArray(this.mHost.getAddress());
        } else {
            dest.writeInt(0);
        }
        dest.writeInt(this.mPort);
        dest.writeInt(this.mTxtRecord.size());
        for (String key : this.mTxtRecord.keySet()) {
            byte[] value = this.mTxtRecord.get(key);
            if (value != null) {
                dest.writeInt(1);
                dest.writeInt(value.length);
                dest.writeByteArray(value);
            } else {
                dest.writeInt(0);
            }
            dest.writeString(key);
        }
    }
}
