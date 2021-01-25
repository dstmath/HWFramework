package ohos.nfc.tag;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class MessageRecord implements Sequenceable {
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    private static final byte FLAG_CF = 32;
    private static final byte FLAG_IL = 8;
    private static final byte FLAG_MB = Byte.MIN_VALUE;
    private static final byte FLAG_ME = 64;
    private static final byte FLAG_SR = 16;
    public static final byte[] RTD_HARMONY_APP = "harmony.com:pkg".getBytes();
    public static final byte[] RTD_TEXT = {84};
    public static final short TNF_ABSOLUTE_URI = 3;
    public static final short TNF_EMPTY = 0;
    public static final short TNF_EXTERNAL_TYPE = 4;
    public static final short TNF_MIME_MEDIA = 2;
    public static final short TNF_RESERVED = 7;
    public static final short TNF_UNCHANGED = 6;
    public static final short TNF_UNKNOWN = 5;
    public static final short TNF_WELL_KNOWN = 1;
    private byte[] mId;
    private byte[] mPayload;
    private short mTnf;
    private byte[] mType;

    public MessageRecord(short s, byte[] bArr, byte[] bArr2, byte[] bArr3) {
        bArr = bArr == null ? EMPTY_BYTE_ARRAY : bArr;
        bArr2 = bArr2 == null ? EMPTY_BYTE_ARRAY : bArr2;
        bArr3 = bArr3 == null ? EMPTY_BYTE_ARRAY : bArr3;
        Optional<String> checkTnf = checkTnf(s, bArr, bArr2, bArr3);
        if (!checkTnf.isPresent()) {
            this.mTnf = s;
            this.mType = bArr;
            this.mId = bArr2;
            this.mPayload = bArr3;
            return;
        }
        throw new IllegalArgumentException(checkTnf.get());
    }

    public short getTnf() {
        return this.mTnf;
    }

    public byte[] getType() {
        byte[] bArr = this.mType;
        return Arrays.copyOf(bArr, bArr.length);
    }

    public byte[] getId() {
        byte[] bArr = this.mId;
        return Arrays.copyOf(bArr, bArr.length);
    }

    public byte[] getPayload() {
        byte[] bArr = this.mPayload;
        return Arrays.copyOf(bArr, bArr.length);
    }

    public int hashCode() {
        return ((((((Arrays.hashCode(this.mId) + 31) * 31) + Arrays.hashCode(this.mPayload)) * 31) + this.mTnf) * 31) + Arrays.hashCode(this.mType);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        MessageRecord messageRecord = (MessageRecord) obj;
        if (Arrays.equals(this.mId, messageRecord.mId) && Arrays.equals(this.mPayload, messageRecord.mPayload) && this.mTnf == messageRecord.mTnf) {
            return Arrays.equals(this.mType, messageRecord.mType);
        }
        return false;
    }

    public static MessageRecord buildApplicationRecord(String str) {
        if (str == null) {
            throw new NullPointerException("packageName is null");
        } else if (str.length() != 0) {
            return new MessageRecord(4, RTD_HARMONY_APP, null, str.getBytes(StandardCharsets.UTF_8));
        } else {
            throw new IllegalArgumentException("packageName is empty");
        }
    }

    public static MessageRecord buildMime(String str, byte[] bArr) {
        if (str != null) {
            Optional<String> normalizeMimeType = normalizeMimeType(str);
            if (normalizeMimeType.get().length() != 0) {
                int indexOf = normalizeMimeType.get().indexOf(47);
                if (indexOf == 0) {
                    throw new IllegalArgumentException("mimeType must have major type");
                } else if (indexOf != normalizeMimeType.get().length() - 1) {
                    return new MessageRecord(2, normalizeMimeType.get().getBytes(StandardCharsets.US_ASCII), null, bArr);
                } else {
                    throw new IllegalArgumentException("mimeType must have minor type");
                }
            } else {
                throw new IllegalArgumentException("mimeType is empty");
            }
        } else {
            throw new NullPointerException("mimeType is null");
        }
    }

    public static MessageRecord buildExternal(String str, String str2, byte[] bArr) {
        if (str == null) {
            throw new NullPointerException("domain is null");
        } else if (str2 != null) {
            String lowerCase = str.trim().toLowerCase(Locale.ROOT);
            String lowerCase2 = str2.trim().toLowerCase(Locale.ROOT);
            if (lowerCase.length() == 0) {
                throw new IllegalArgumentException("domain is empty");
            } else if (lowerCase2.length() != 0) {
                byte[] bytes = lowerCase.getBytes(StandardCharsets.UTF_8);
                byte[] bytes2 = lowerCase2.getBytes(StandardCharsets.UTF_8);
                byte[] bArr2 = new byte[(bytes.length + 1 + bytes2.length)];
                System.arraycopy(bytes, 0, bArr2, 0, bytes.length);
                bArr2[bytes.length] = 58;
                System.arraycopy(bytes2, 0, bArr2, bytes.length + 1, bytes2.length);
                return new MessageRecord(4, bArr2, null, bArr);
            } else {
                throw new IllegalArgumentException("type is empty");
            }
        } else {
            throw new NullPointerException("type is null");
        }
    }

    public static MessageRecord buildTextRecord(String str, String str2) {
        byte[] bArr;
        if (str2 != null) {
            byte[] bytes = str2.getBytes(StandardCharsets.UTF_8);
            if (str == null || str.isEmpty()) {
                bArr = Locale.getDefault().getLanguage().getBytes(StandardCharsets.US_ASCII);
            } else {
                bArr = str.getBytes(StandardCharsets.US_ASCII);
            }
            if (bArr.length < 64) {
                ByteBuffer allocate = ByteBuffer.allocate(bArr.length + 1 + bytes.length);
                allocate.put((byte) (bArr.length & 255));
                allocate.put(bArr);
                allocate.put(bytes);
                return new MessageRecord(1, RTD_TEXT, null, allocate.array());
            }
            throw new IllegalArgumentException("language code is too long, must be <64 bytes.");
        }
        throw new NullPointerException("text is null");
    }

    static Optional<String> checkTnf(short s, byte[] bArr, byte[] bArr2, byte[] bArr3) {
        switch (s) {
            case 0:
                if (bArr.length == 0 && bArr2.length == 0 && bArr3.length == 0) {
                    return Optional.empty();
                }
                return Optional.of("unexpected data in TNF_EMPTY record.");
            case 1:
            case 2:
            case 3:
            case 4:
                return Optional.empty();
            case 5:
            case 7:
                if (bArr.length != 0) {
                    return Optional.of("unexpected type field in TNF_UNKNOWN or TNF_RESERVEd record");
                }
                return Optional.empty();
            case 6:
                return Optional.of("unexpected TNF_UNCHANGED in first chunk or logical record");
            default:
                return Optional.of(String.format(Locale.ENGLISH, "unexpected tnf value: 0x%02x", Short.valueOf(s)));
        }
    }

    static Optional<String> normalizeMimeType(String str) {
        if (str == null) {
            return Optional.empty();
        }
        String lowerCase = str.trim().toLowerCase(Locale.ROOT);
        int indexOf = lowerCase.indexOf(59);
        if (indexOf != -1) {
            lowerCase = lowerCase.substring(0, indexOf);
        }
        return Optional.of(lowerCase);
    }

    public int getAllRecordsLength() {
        int length = this.mType.length + 3 + this.mId.length;
        byte[] bArr = this.mPayload;
        int length2 = length + bArr.length;
        boolean z = true;
        boolean z2 = bArr.length < 256;
        if (this.mTnf != 0 && this.mId.length <= 0) {
            z = false;
        }
        if (!z2) {
            length2 += 3;
        }
        return z ? length2 + 1 : length2;
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        parcel.writeInt(this.mTnf);
        parcel.writeInt(this.mType.length);
        parcel.writeByteArray(this.mType);
        parcel.writeInt(this.mId.length);
        parcel.writeByteArray(this.mId);
        parcel.writeInt(this.mPayload.length);
        parcel.writeByteArray(this.mPayload);
        return true;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        this.mTnf = (short) parcel.readInt();
        parcel.readInt();
        this.mType = parcel.readByteArray();
        parcel.readInt();
        this.mId = parcel.readByteArray();
        parcel.readInt();
        this.mPayload = parcel.readByteArray();
        return true;
    }
}
