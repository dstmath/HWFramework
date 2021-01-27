package ohos.nfc.tag;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import java.util.Optional;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.nfc.NfcKitsUtils;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;
import ohos.utils.net.Uri;

public class MessageRecord implements Sequenceable {
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    private static final byte FLAG_CF = 32;
    private static final byte FLAG_IL = 8;
    private static final byte FLAG_MB = Byte.MIN_VALUE;
    private static final byte FLAG_ME = 64;
    private static final byte FLAG_SR = 16;
    private static final HiLogLabel LABEL = new HiLogLabel(3, NfcKitsUtils.NFC_DOMAIN_ID, "MessageRecord");
    private static final int MAX_PAYLOAD_SIZE = 10485760;
    public static final byte[] RTD_HARMONY_APP = "harmony.com:pkg".getBytes();
    public static final byte[] RTD_SMART_POSTER = {83, 112};
    public static final byte[] RTD_TEXT = {84};
    public static final byte[] RTD_URI = {85};
    public static final short TNF_ABSOLUTE_URI = 3;
    public static final short TNF_EMPTY = 0;
    public static final short TNF_EXTERNAL_TYPE = 4;
    public static final short TNF_MIME_MEDIA = 2;
    public static final short TNF_RESERVED = 7;
    public static final short TNF_UNCHANGED = 6;
    public static final short TNF_UNKNOWN = 5;
    public static final short TNF_WELL_KNOWN = 1;
    private static final String[] URI_PREFIX_MAP = {"", "http://www.", "https://www.", "http://", "https://", "tel:", "mailto:", "ftp://anonymous:anonymous@", "ftp://ftp.", "ftps://", "sftp://", "smb://", "nfs://", "ftp://", "dav://", "news:", "telnet://", "imap:", "rtsp://", "urn:", "pop:", "sip:", "sips:", "tftp:", "btspp://", "btl2cap://", "btgoep://", "tcpobex://", "irdaobex://", "file://", "urn:epc:id:", "urn:epc:tag:", "urn:epc:pat:", "urn:epc:raw:", "urn:epc:", "urn:nfc:"};
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
        if (obj == null || getClass() != obj.getClass() || !(obj instanceof MessageRecord)) {
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

    public Optional<Uri> buildUri() {
        return buildUri(false);
    }

    private Optional<Uri> buildUri(boolean z) {
        short s = this.mTnf;
        if (s != 1) {
            if (s == 3) {
                return Optional.of(Uri.parse(new String(this.mType, StandardCharsets.UTF_8)).getLowerCaseScheme());
            }
            if (s != 4) {
                return Optional.empty();
            }
            if (!z) {
                return Optional.of(Uri.parse("vnd.harmony.nfc://ext/" + new String(this.mType, StandardCharsets.US_ASCII)));
            }
        } else if (Arrays.equals(this.mType, RTD_SMART_POSTER) && !z) {
            try {
                for (MessageRecord messageRecord : new NdefMessage(this.mPayload).getRecords()) {
                    Optional<Uri> buildUri = messageRecord.buildUri(true);
                    if (buildUri.isPresent()) {
                        return buildUri;
                    }
                }
            } catch (IllegalArgumentException unused) {
                HiLog.error(LABEL, "buildUri catch IllegalArgumentException", new Object[0]);
            }
        } else if (Arrays.equals(this.mType, RTD_URI)) {
            Optional<Uri> marshallingWktUri = marshallingWktUri();
            return marshallingWktUri.isPresent() ? Optional.of(marshallingWktUri.get().getLowerCaseScheme()) : Optional.empty();
        }
        return Optional.empty();
    }

    private Optional<Uri> marshallingWktUri() {
        byte[] bArr = this.mPayload;
        if (bArr.length < 2) {
            return Optional.empty();
        }
        int i = bArr[0] & -1;
        if (i >= 0) {
            String[] strArr = URI_PREFIX_MAP;
            if (i < strArr.length) {
                String str = strArr[i];
                String str2 = new String(Arrays.copyOfRange(bArr, 1, bArr.length), StandardCharsets.UTF_8);
                return Optional.of(Uri.parse(str + str2));
            }
        }
        return Optional.empty();
    }

    public static MessageRecord[] parse(ByteBuffer byteBuffer, boolean z) throws IllegalArgumentException {
        boolean z2;
        ArrayList arrayList = new ArrayList();
        byte[] bArr = null;
        byte[] bArr2 = null;
        boolean z3 = false;
        boolean z4 = false;
        while (true) {
            if (z3) {
                break;
            }
            try {
                byte b = byteBuffer.get();
                boolean z5 = (b & Byte.MIN_VALUE) != 0;
                boolean z6 = (b & FLAG_ME) != 0;
                boolean z7 = (b & 32) != 0;
                boolean z8 = (b & 16) != 0;
                boolean z9 = (b & 8) != 0;
                short s = (short) (b & 7);
                if (!z5 && arrayList.size() == 0 && !z4) {
                    if (!z) {
                        throw new IllegalArgumentException("expected MB flag");
                    }
                }
                if (z5 && (arrayList.size() != 0 || z4)) {
                    if (!z) {
                        throw new IllegalArgumentException("unexpected MB flag");
                    }
                }
                if (z4) {
                    if (z9) {
                        throw new IllegalArgumentException("unexpected IL flag in non-leading chunk");
                    }
                }
                if (z7) {
                    if (z6) {
                        throw new IllegalArgumentException("unexpected ME flag in non-trailing chunk");
                    }
                }
                if (z4) {
                    if (s != 6) {
                        throw new IllegalArgumentException("expected TNF_UNCHANGED in non-leading chunk");
                    }
                }
                if (!z4) {
                    if (s == 6) {
                        throw new IllegalArgumentException("unexpected TNF_UNCHANGED in first chunk or unchunked record");
                    }
                }
                int i = byteBuffer.get() & 255;
                long j = z8 ? (long) (byteBuffer.get() & 255) : ((long) byteBuffer.getInt()) & 4294967295L;
                int i2 = z9 ? byteBuffer.get() & 255 : 0;
                if (z4) {
                    if (i != 0) {
                        throw new IllegalArgumentException("expected zero-length type in non-leading chunk");
                    }
                }
                if (!z4) {
                    bArr = i > 0 ? new byte[i] : EMPTY_BYTE_ARRAY;
                    bArr2 = i2 > 0 ? new byte[i2] : EMPTY_BYTE_ARRAY;
                    byteBuffer.get(bArr);
                    byteBuffer.get(bArr2);
                }
                ensureSanePayloadSize(j);
                long j2 = 0;
                byte[] bArr3 = j > 0 ? new byte[((int) j)] : EMPTY_BYTE_ARRAY;
                byteBuffer.get(bArr3);
                ArrayList arrayList2 = new ArrayList();
                short s2 = -1;
                if (z7 && !z4) {
                    if (i == 0) {
                        if (s != 5) {
                            throw new IllegalArgumentException("expected non-zero type length in first chunk");
                        }
                    }
                    arrayList2.clear();
                    s2 = s;
                }
                if (z7 || z4) {
                    arrayList2.add(bArr3);
                }
                if (z7 || !z4) {
                    z2 = false;
                    s2 = s;
                } else {
                    Iterator it = arrayList2.iterator();
                    while (it.hasNext()) {
                        j2 += (long) ((byte[]) it.next()).length;
                    }
                    ensureSanePayloadSize(j2);
                    bArr3 = new byte[((int) j2)];
                    Iterator it2 = arrayList2.iterator();
                    int i3 = 0;
                    while (it2.hasNext()) {
                        byte[] bArr4 = (byte[]) it2.next();
                        System.arraycopy(bArr4, 0, bArr3, i3, bArr4.length);
                        i3 += bArr4.length;
                    }
                    z2 = false;
                }
                if (z7) {
                    z3 = z6;
                    z4 = true;
                } else {
                    Optional<String> validateTnf = validateTnf(s2, bArr, bArr2, bArr3);
                    if (!validateTnf.isPresent()) {
                        arrayList.add(new MessageRecord(s2, bArr, bArr2, bArr3));
                        if (z) {
                            break;
                        }
                        z3 = z6;
                        z4 = z2;
                    } else {
                        throw new IllegalArgumentException(validateTnf.get());
                    }
                }
            } catch (BufferUnderflowException e) {
                throw new IllegalArgumentException("expected more data", e);
            }
        }
        return (MessageRecord[]) arrayList.toArray(new MessageRecord[arrayList.size()]);
    }

    private static void ensureSanePayloadSize(long j) throws IllegalArgumentException {
        if (j > 10485760) {
            throw new IllegalArgumentException("payload size larger than limit: 10485760");
        }
    }

    private static Optional<String> validateTnf(short s, byte[] bArr, byte[] bArr2, byte[] bArr3) {
        switch (s) {
            case 0:
                if (bArr.length == 0 && bArr2.length == 0 && bArr3.length == 0) {
                    return Optional.empty();
                }
                return Optional.of("unexpected data in TNF_EMPTY record");
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
                return Optional.of(String.format(Locale.ROOT, "unexpected tnf value: 0x%02x", Short.valueOf(s)));
        }
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
