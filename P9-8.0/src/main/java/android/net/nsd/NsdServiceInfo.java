package android.net.nsd;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
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
import java.util.Map.Entry;

public final class NsdServiceInfo implements Parcelable {
    public static final Creator<NsdServiceInfo> CREATOR = new Creator<NsdServiceInfo>() {
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
                Object valueArray = null;
                if (in.readInt() == 1) {
                    valueArray = new byte[in.readInt()];
                    in.readByteArray(valueArray);
                }
                info.mTxtRecord.put(in.readString(), valueArray);
            }
            return info;
        }

        public NsdServiceInfo[] newArray(int size) {
            return new NsdServiceInfo[size];
        }
    };
    private static final String TAG = "NsdServiceInfo";
    private InetAddress mHost;
    private int mPort;
    private String mServiceName;
    private String mServiceType;
    private final ArrayMap<String, byte[]> mTxtRecord = new ArrayMap();

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
            pos++;
            if (recordLen == 0) {
                try {
                    throw new IllegalArgumentException("Zero sized txt record");
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "While parsing txt records (pos = " + pos + "): " + e.getMessage());
                }
            } else {
                if (pos + recordLen > txtRecordsRawBytes.length) {
                    Log.w(TAG, "Corrupt record length (pos = " + pos + "): " + recordLen);
                    recordLen = txtRecordsRawBytes.length - pos;
                }
                String key = null;
                byte[] value = null;
                int valueLen = 0;
                for (int i = pos; i < pos + recordLen; i++) {
                    if (key != null) {
                        if (value == null) {
                            value = new byte[((recordLen - key.length()) - 1)];
                        }
                        value[valueLen] = txtRecordsRawBytes[i];
                        valueLen++;
                    } else if (txtRecordsRawBytes[i] == (byte) 61) {
                        key = new String(txtRecordsRawBytes, pos, i - pos, StandardCharsets.US_ASCII);
                    }
                }
                if (key == null) {
                    key = new String(txtRecordsRawBytes, pos, recordLen, StandardCharsets.US_ASCII);
                }
                if (TextUtils.isEmpty(key)) {
                    throw new IllegalArgumentException("Invalid txt record (key is empty)");
                } else if (getAttributes().containsKey(key)) {
                    throw new IllegalArgumentException("Invalid txt record (duplicate key \"" + key + "\")");
                } else {
                    setAttribute(key, value);
                    pos += recordLen;
                }
            }
        }
    }

    public void setAttribute(String key, byte[] value) {
        int i = 0;
        if (TextUtils.isEmpty(key)) {
            throw new IllegalArgumentException("Key cannot be empty");
        }
        int i2 = 0;
        while (i2 < key.length()) {
            char character = key.charAt(i2);
            if (character < ' ' || character > '~') {
                throw new IllegalArgumentException("Key strings must be printable US-ASCII");
            } else if (character == '=') {
                throw new IllegalArgumentException("Key strings must not include '='");
            } else {
                i2++;
            }
        }
        if ((value == null ? 0 : value.length) + key.length() >= 255) {
            throw new IllegalArgumentException("Key length + value length must be < 255 bytes");
        }
        if (key.length() > 9) {
            Log.w(TAG, "Key lengths > 9 are discouraged: " + key);
        }
        int length = key.length() + getTxtRecordSize();
        if (value != null) {
            i = value.length;
        }
        int futureSize = (i + length) + 2;
        if (futureSize > 1300) {
            throw new IllegalArgumentException("Total length of attributes must be < 1300 bytes");
        }
        if (futureSize > 400) {
            Log.w(TAG, "Total length of all attributes exceeds 400 bytes; truncation may occur");
        }
        this.mTxtRecord.put(key, value);
    }

    public void setAttribute(String key, String value) {
        byte[] bArr;
        if (value == null) {
            try {
                bArr = (byte[]) null;
            } catch (UnsupportedEncodingException e) {
                throw new IllegalArgumentException("Value must be UTF-8");
            }
        }
        bArr = value.getBytes("UTF-8");
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
        for (Entry<String, byte[]> entry : this.mTxtRecord.entrySet()) {
            byte[] value = (byte[]) entry.getValue();
            txtRecordSize = ((txtRecordSize + 2) + ((String) entry.getKey()).length()) + (value == null ? 0 : value.length);
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
        for (Entry<String, byte[]> entry : this.mTxtRecord.entrySet()) {
            String key = (String) entry.getKey();
            byte[] value = (byte[]) entry.getValue();
            int ptr2 = ptr + 1;
            txtRecord[ptr] = (byte) (((value == null ? 0 : value.length) + key.length()) + 1);
            System.arraycopy(key.getBytes(StandardCharsets.US_ASCII), 0, txtRecord, ptr2, key.length());
            ptr = ptr2 + key.length();
            ptr2 = ptr + 1;
            txtRecord[ptr] = (byte) 61;
            if (value != null) {
                System.arraycopy(value, 0, txtRecord, ptr2, value.length);
                ptr = ptr2 + value.length;
            } else {
                ptr = ptr2;
            }
        }
        return txtRecord;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("name: ").append(this.mServiceName).append(", type: ").append(this.mServiceType).append(", host: ").append(this.mHost).append(", port: ").append(this.mPort);
        byte[] txtRecord = getTxtRecord();
        if (txtRecord != null) {
            sb.append(", txtRecord: ").append(new String(txtRecord, StandardCharsets.UTF_8));
        }
        return sb.toString();
    }

    public int describeContents() {
        return 0;
    }

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
            byte[] value = (byte[]) this.mTxtRecord.get(key);
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
