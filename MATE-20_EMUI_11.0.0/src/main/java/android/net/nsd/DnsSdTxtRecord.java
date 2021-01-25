package android.net.nsd;

import android.os.Parcel;
import android.os.Parcelable;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class DnsSdTxtRecord implements Parcelable {
    public static final Parcelable.Creator<DnsSdTxtRecord> CREATOR = new Parcelable.Creator<DnsSdTxtRecord>() {
        /* class android.net.nsd.DnsSdTxtRecord.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DnsSdTxtRecord createFromParcel(Parcel in) {
            DnsSdTxtRecord info = new DnsSdTxtRecord();
            in.readByteArray(info.mData);
            return info;
        }

        @Override // android.os.Parcelable.Creator
        public DnsSdTxtRecord[] newArray(int size) {
            return new DnsSdTxtRecord[size];
        }
    };
    private static final byte mSeperator = 61;
    private byte[] mData;

    public DnsSdTxtRecord() {
        this.mData = new byte[0];
    }

    public DnsSdTxtRecord(byte[] data) {
        this.mData = (byte[]) data.clone();
    }

    public DnsSdTxtRecord(DnsSdTxtRecord src) {
        byte[] bArr;
        if (src != null && (bArr = src.mData) != null) {
            this.mData = (byte[]) bArr.clone();
        }
    }

    public void set(String key, String value) {
        int valLen;
        byte[] valBytes;
        if (value != null) {
            valBytes = value.getBytes();
            valLen = valBytes.length;
        } else {
            valBytes = null;
            valLen = 0;
        }
        try {
            byte[] keyBytes = key.getBytes("US-ASCII");
            for (byte b : keyBytes) {
                if (b == 61) {
                    throw new IllegalArgumentException("= is not a valid character in key");
                }
            }
            if (keyBytes.length + valLen < 255) {
                int currentLoc = remove(key);
                if (currentLoc == -1) {
                    currentLoc = keyCount();
                }
                insert(keyBytes, valBytes, currentLoc);
                return;
            }
            throw new IllegalArgumentException("Key and Value length cannot exceed 255 bytes");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("key should be US-ASCII");
        }
    }

    public String get(String key) {
        byte[] val = getValue(key);
        if (val != null) {
            return new String(val);
        }
        return null;
    }

    public int remove(String key) {
        int avStart = 0;
        int i = 0;
        while (true) {
            byte[] bArr = this.mData;
            if (avStart >= bArr.length) {
                return -1;
            }
            byte b = bArr[avStart];
            if (key.length() > b || !((key.length() == b || this.mData[key.length() + avStart + 1] == 61) && key.compareToIgnoreCase(new String(this.mData, avStart + 1, key.length())) == 0)) {
                avStart += (b + 1) & 255;
                i++;
            } else {
                byte[] oldBytes = this.mData;
                this.mData = new byte[((oldBytes.length - b) - 1)];
                System.arraycopy(oldBytes, 0, this.mData, 0, avStart);
                System.arraycopy(oldBytes, avStart + b + 1, this.mData, avStart, ((oldBytes.length - avStart) - b) - 1);
                return i;
            }
        }
    }

    public int keyCount() {
        int count = 0;
        int nextKey = 0;
        while (true) {
            byte[] bArr = this.mData;
            if (nextKey >= bArr.length) {
                return count;
            }
            nextKey += (bArr[nextKey] + 1) & 255;
            count++;
        }
    }

    public boolean contains(String key) {
        int i = 0;
        while (true) {
            String s = getKey(i);
            if (s == null) {
                return false;
            }
            if (key.compareToIgnoreCase(s) == 0) {
                return true;
            }
            i++;
        }
    }

    public int size() {
        return this.mData.length;
    }

    public byte[] getRawData() {
        return (byte[]) this.mData.clone();
    }

    private void insert(byte[] keyBytes, byte[] value, int index) {
        byte[] oldBytes = this.mData;
        int valLen = value != null ? value.length : 0;
        int insertion = 0;
        for (int i = 0; i < index; i++) {
            byte[] bArr = this.mData;
            if (insertion >= bArr.length) {
                break;
            }
            insertion += (bArr[insertion] + 1) & 255;
        }
        int avLen = keyBytes.length + valLen + (value != null ? 1 : 0);
        int newLen = oldBytes.length + avLen + 1;
        this.mData = new byte[newLen];
        System.arraycopy(oldBytes, 0, this.mData, 0, insertion);
        int secondHalfLen = oldBytes.length - insertion;
        System.arraycopy(oldBytes, insertion, this.mData, newLen - secondHalfLen, secondHalfLen);
        byte[] bArr2 = this.mData;
        bArr2[insertion] = (byte) avLen;
        System.arraycopy(keyBytes, 0, bArr2, insertion + 1, keyBytes.length);
        if (value != null) {
            byte[] bArr3 = this.mData;
            bArr3[insertion + 1 + keyBytes.length] = mSeperator;
            System.arraycopy(value, 0, bArr3, keyBytes.length + insertion + 2, valLen);
        }
    }

    private String getKey(int index) {
        int avStart = 0;
        for (int i = 0; i < index; i++) {
            byte[] bArr = this.mData;
            if (avStart >= bArr.length) {
                break;
            }
            avStart += bArr[avStart] + 1;
        }
        byte[] bArr2 = this.mData;
        if (avStart >= bArr2.length) {
            return null;
        }
        byte b = bArr2[avStart];
        int aLen = 0;
        while (aLen < b && this.mData[avStart + aLen + 1] != 61) {
            aLen++;
        }
        return new String(this.mData, avStart + 1, aLen);
    }

    private byte[] getValue(int index) {
        int avStart = 0;
        for (int i = 0; i < index; i++) {
            byte[] bArr = this.mData;
            if (avStart >= bArr.length) {
                break;
            }
            avStart += bArr[avStart] + 1;
        }
        byte[] bArr2 = this.mData;
        if (avStart >= bArr2.length) {
            return null;
        }
        byte b = bArr2[avStart];
        for (int aLen = 0; aLen < b; aLen++) {
            byte[] bArr3 = this.mData;
            if (bArr3[avStart + aLen + 1] == 61) {
                byte[] value = new byte[((b - aLen) - 1)];
                System.arraycopy(bArr3, avStart + aLen + 2, value, 0, (b - aLen) - 1);
                return value;
            }
        }
        return null;
    }

    private String getValueAsString(int index) {
        byte[] value = getValue(index);
        if (value != null) {
            return new String(value);
        }
        return null;
    }

    private byte[] getValue(String forKey) {
        int i = 0;
        while (true) {
            String s = getKey(i);
            if (s == null) {
                return null;
            }
            if (forKey.compareToIgnoreCase(s) == 0) {
                return getValue(i);
            }
            i++;
        }
    }

    public String toString() {
        String av;
        String result = null;
        int i = 0;
        while (true) {
            String a = getKey(i);
            if (a == null) {
                break;
            }
            String av2 = "{" + a;
            String val = getValueAsString(i);
            if (val != null) {
                av = av2 + "=" + val + "}";
            } else {
                av = av2 + "}";
            }
            if (result == null) {
                result = av;
            } else {
                result = result + ", " + av;
            }
            i++;
        }
        return result != null ? result : "";
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof DnsSdTxtRecord)) {
            return false;
        }
        return Arrays.equals(((DnsSdTxtRecord) o).mData, this.mData);
    }

    public int hashCode() {
        return Arrays.hashCode(this.mData);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByteArray(this.mData);
    }
}
