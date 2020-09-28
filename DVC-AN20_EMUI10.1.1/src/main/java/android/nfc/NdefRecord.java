package android.nfc;

import android.annotation.UnsupportedAppUsage;
import android.content.ClipDescription;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.webkit.WebView;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public final class NdefRecord implements Parcelable {
    public static final Parcelable.Creator<NdefRecord> CREATOR = new Parcelable.Creator<NdefRecord>() {
        /* class android.nfc.NdefRecord.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public NdefRecord createFromParcel(Parcel in) {
            byte[] type = new byte[in.readInt()];
            in.readByteArray(type);
            byte[] id = new byte[in.readInt()];
            in.readByteArray(id);
            byte[] payload = new byte[in.readInt()];
            in.readByteArray(payload);
            return new NdefRecord((short) in.readInt(), type, id, payload);
        }

        @Override // android.os.Parcelable.Creator
        public NdefRecord[] newArray(int size) {
            return new NdefRecord[size];
        }
    };
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    private static final byte FLAG_CF = 32;
    private static final byte FLAG_IL = 8;
    private static final byte FLAG_MB = Byte.MIN_VALUE;
    private static final byte FLAG_ME = 64;
    private static final byte FLAG_SR = 16;
    private static final int MAX_PAYLOAD_SIZE = 10485760;
    public static final byte[] RTD_ALTERNATIVE_CARRIER = {97, 99};
    public static final byte[] RTD_ANDROID_APP = "android.com:pkg".getBytes();
    public static final byte[] RTD_HANDOVER_CARRIER = {72, 99};
    public static final byte[] RTD_HANDOVER_REQUEST = {72, 114};
    public static final byte[] RTD_HANDOVER_SELECT = {72, 115};
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
    private static final String[] URI_PREFIX_MAP = {"", "http://www.", "https://www.", "http://", "https://", WebView.SCHEME_TEL, "mailto:", "ftp://anonymous:anonymous@", "ftp://ftp.", "ftps://", "sftp://", "smb://", "nfs://", "ftp://", "dav://", "news:", "telnet://", "imap:", "rtsp://", "urn:", "pop:", "sip:", "sips:", "tftp:", "btspp://", "btl2cap://", "btgoep://", "tcpobex://", "irdaobex://", "file://", "urn:epc:id:", "urn:epc:tag:", "urn:epc:pat:", "urn:epc:raw:", "urn:epc:", "urn:nfc:"};
    @UnsupportedAppUsage
    private final byte[] mId;
    private final byte[] mPayload;
    private final short mTnf;
    private final byte[] mType;

    public static NdefRecord createApplicationRecord(String packageName) {
        if (packageName == null) {
            throw new NullPointerException("packageName is null");
        } else if (packageName.length() != 0) {
            return new NdefRecord(4, RTD_ANDROID_APP, null, packageName.getBytes(StandardCharsets.UTF_8));
        } else {
            throw new IllegalArgumentException("packageName is empty");
        }
    }

    public static NdefRecord createUri(Uri uri) {
        if (uri != null) {
            String uriString = uri.normalizeScheme().toString();
            if (uriString.length() != 0) {
                byte prefix = 0;
                int i = 1;
                while (true) {
                    String[] strArr = URI_PREFIX_MAP;
                    if (i >= strArr.length) {
                        break;
                    } else if (uriString.startsWith(strArr[i])) {
                        prefix = (byte) i;
                        uriString = uriString.substring(URI_PREFIX_MAP[i].length());
                        break;
                    } else {
                        i++;
                    }
                }
                byte[] uriBytes = uriString.getBytes(StandardCharsets.UTF_8);
                byte[] recordBytes = new byte[(uriBytes.length + 1)];
                recordBytes[0] = prefix;
                System.arraycopy(uriBytes, 0, recordBytes, 1, uriBytes.length);
                return new NdefRecord(1, RTD_URI, null, recordBytes);
            }
            throw new IllegalArgumentException("uri is empty");
        }
        throw new NullPointerException("uri is null");
    }

    public static NdefRecord createUri(String uriString) {
        return createUri(Uri.parse(uriString));
    }

    public static NdefRecord createMime(String mimeType, byte[] mimeData) {
        if (mimeType != null) {
            String mimeType2 = Intent.normalizeMimeType(mimeType);
            if (mimeType2.length() != 0) {
                int slashIndex = mimeType2.indexOf(47);
                if (slashIndex == 0) {
                    throw new IllegalArgumentException("mimeType must have major type");
                } else if (slashIndex != mimeType2.length() - 1) {
                    return new NdefRecord(2, mimeType2.getBytes(StandardCharsets.US_ASCII), null, mimeData);
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

    public static NdefRecord createExternal(String domain, String type, byte[] data) {
        if (domain == null) {
            throw new NullPointerException("domain is null");
        } else if (type != null) {
            String domain2 = domain.trim().toLowerCase(Locale.ROOT);
            String type2 = type.trim().toLowerCase(Locale.ROOT);
            if (domain2.length() == 0) {
                throw new IllegalArgumentException("domain is empty");
            } else if (type2.length() != 0) {
                byte[] byteDomain = domain2.getBytes(StandardCharsets.UTF_8);
                byte[] byteType = type2.getBytes(StandardCharsets.UTF_8);
                byte[] b = new byte[(byteDomain.length + 1 + byteType.length)];
                System.arraycopy(byteDomain, 0, b, 0, byteDomain.length);
                b[byteDomain.length] = 58;
                System.arraycopy(byteType, 0, b, byteDomain.length + 1, byteType.length);
                return new NdefRecord(4, b, null, data);
            } else {
                throw new IllegalArgumentException("type is empty");
            }
        } else {
            throw new NullPointerException("type is null");
        }
    }

    public static NdefRecord createTextRecord(String languageCode, String text) {
        byte[] languageCodeBytes;
        if (text != null) {
            byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);
            if (languageCode == null || languageCode.isEmpty()) {
                languageCodeBytes = Locale.getDefault().getLanguage().getBytes(StandardCharsets.US_ASCII);
            } else {
                languageCodeBytes = languageCode.getBytes(StandardCharsets.US_ASCII);
            }
            if (languageCodeBytes.length < 64) {
                ByteBuffer buffer = ByteBuffer.allocate(languageCodeBytes.length + 1 + textBytes.length);
                buffer.put((byte) (languageCodeBytes.length & 255));
                buffer.put(languageCodeBytes);
                buffer.put(textBytes);
                return new NdefRecord(1, RTD_TEXT, null, buffer.array());
            }
            throw new IllegalArgumentException("language code is too long, must be <64 bytes.");
        }
        throw new NullPointerException("text is null");
    }

    public NdefRecord(short tnf, byte[] type, byte[] id, byte[] payload) {
        type = type == null ? EMPTY_BYTE_ARRAY : type;
        id = id == null ? EMPTY_BYTE_ARRAY : id;
        payload = payload == null ? EMPTY_BYTE_ARRAY : payload;
        String message = validateTnf(tnf, type, id, payload);
        if (message == null) {
            this.mTnf = tnf;
            this.mType = type;
            this.mId = id;
            this.mPayload = payload;
            return;
        }
        throw new IllegalArgumentException(message);
    }

    @Deprecated
    public NdefRecord(byte[] data) throws FormatException {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        NdefRecord[] rs = parse(buffer, true);
        if (buffer.remaining() <= 0) {
            this.mTnf = rs[0].mTnf;
            this.mType = rs[0].mType;
            this.mId = rs[0].mId;
            this.mPayload = rs[0].mPayload;
            return;
        }
        throw new FormatException("data too long");
    }

    public short getTnf() {
        return this.mTnf;
    }

    public byte[] getType() {
        return (byte[]) this.mType.clone();
    }

    public byte[] getId() {
        return (byte[]) this.mId.clone();
    }

    public byte[] getPayload() {
        return (byte[]) this.mPayload.clone();
    }

    @Deprecated
    public byte[] toByteArray() {
        ByteBuffer buffer = ByteBuffer.allocate(getByteLength());
        writeToByteBuffer(buffer, true, true);
        return buffer.array();
    }

    public String toMimeType() {
        short s = this.mTnf;
        if (s != 1) {
            if (s != 2) {
                return null;
            }
            return Intent.normalizeMimeType(new String(this.mType, StandardCharsets.US_ASCII));
        } else if (Arrays.equals(this.mType, RTD_TEXT)) {
            return ClipDescription.MIMETYPE_TEXT_PLAIN;
        } else {
            return null;
        }
    }

    public Uri toUri() {
        return toUri(false);
    }

    private Uri toUri(boolean inSmartPoster) {
        Uri wktUri;
        short s = this.mTnf;
        if (s != 1) {
            if (s == 3) {
                return Uri.parse(new String(this.mType, StandardCharsets.UTF_8)).normalizeScheme();
            }
            if (s == 4 && !inSmartPoster) {
                return Uri.parse("vnd.android.nfc://ext/" + new String(this.mType, StandardCharsets.US_ASCII));
            }
        } else if (Arrays.equals(this.mType, RTD_SMART_POSTER) && !inSmartPoster) {
            try {
                for (NdefRecord nestedRecord : new NdefMessage(this.mPayload).getRecords()) {
                    Uri uri = nestedRecord.toUri(true);
                    if (uri != null) {
                        return uri;
                    }
                }
            } catch (FormatException e) {
            }
        } else if (!Arrays.equals(this.mType, RTD_URI) || (wktUri = parseWktUri()) == null) {
            return null;
        } else {
            return wktUri.normalizeScheme();
        }
        return null;
    }

    private Uri parseWktUri() {
        int prefixIndex;
        byte[] bArr = this.mPayload;
        if (bArr.length >= 2 && (prefixIndex = bArr[0] & -1) >= 0) {
            String[] strArr = URI_PREFIX_MAP;
            if (prefixIndex < strArr.length) {
                String prefix = strArr[prefixIndex];
                String suffix = new String(Arrays.copyOfRange(bArr, 1, bArr.length), StandardCharsets.UTF_8);
                return Uri.parse(prefix + suffix);
            }
        }
        return null;
    }

    static NdefRecord[] parse(ByteBuffer buffer, boolean ignoreMbMe) throws FormatException {
        byte[] type;
        byte[] id;
        long payloadLength;
        boolean me;
        short chunkTnf;
        byte[] id2;
        byte[] type2;
        short chunkTnf2;
        ByteBuffer byteBuffer = buffer;
        List<NdefRecord> records = new ArrayList<>();
        byte[] type3 = null;
        byte[] id3 = null;
        byte[] payload = null;
        try {
            ArrayList<byte[]> chunks = new ArrayList<>();
            boolean inChunk = false;
            short chunkTnf3 = -1;
            boolean id4 = false;
            while (true) {
                if (id4) {
                    break;
                }
                byte flag = buffer.get();
                boolean il = true;
                boolean mb = (flag & Byte.MIN_VALUE) != 0;
                boolean me2 = (flag & 64) != 0;
                boolean cf = (flag & FLAG_CF) != 0;
                boolean sr = (flag & 16) != 0;
                if ((flag & 8) == 0) {
                    il = false;
                }
                short tnf = (short) (flag & 7);
                if (mb || records.size() != 0 || inChunk) {
                    type = type3;
                } else if (ignoreMbMe) {
                    type = type3;
                } else {
                    throw new FormatException("expected MB flag");
                }
                if (mb && (records.size() != 0 || inChunk)) {
                    if (!ignoreMbMe) {
                        throw new FormatException("unexpected MB flag");
                    }
                }
                if (inChunk) {
                    if (il) {
                        throw new FormatException("unexpected IL flag in non-leading chunk");
                    }
                }
                if (cf) {
                    if (me2) {
                        throw new FormatException("unexpected ME flag in non-trailing chunk");
                    }
                }
                if (inChunk) {
                    if (tnf != 6) {
                        throw new FormatException("expected TNF_UNCHANGED in non-leading chunk");
                    }
                }
                if (!inChunk) {
                    if (tnf == 6) {
                        throw new FormatException("unexpected TNF_UNCHANGED in first chunk or unchunked record");
                    }
                }
                int typeLength = buffer.get() & 255;
                if (sr) {
                    id = id3;
                    payloadLength = (long) (buffer.get() & 255);
                } else {
                    id = id3;
                    payloadLength = ((long) buffer.getInt()) & 4294967295L;
                }
                int idLength = il ? buffer.get() & 255 : 0;
                if (!inChunk) {
                    chunkTnf = chunkTnf3;
                    me = me2;
                } else if (typeLength == 0) {
                    chunkTnf = chunkTnf3;
                    me = me2;
                } else {
                    throw new FormatException("expected zero-length type in non-leading chunk");
                }
                if (!inChunk) {
                    type2 = typeLength > 0 ? new byte[typeLength] : EMPTY_BYTE_ARRAY;
                    id2 = idLength > 0 ? new byte[idLength] : EMPTY_BYTE_ARRAY;
                    byteBuffer.get(type2);
                    byteBuffer.get(id2);
                } else {
                    type2 = type;
                    id2 = id;
                }
                ensureSanePayloadSize(payloadLength);
                byte[] payload2 = payloadLength > 0 ? new byte[((int) payloadLength)] : EMPTY_BYTE_ARRAY;
                byteBuffer.get(payload2);
                if (!cf || inChunk) {
                    chunkTnf2 = chunkTnf;
                } else {
                    if (typeLength == 0) {
                        if (tnf != 5) {
                            throw new FormatException("expected non-zero type length in first chunk");
                        }
                    }
                    chunks.clear();
                    chunkTnf2 = tnf;
                }
                if (cf || inChunk) {
                    chunks.add(payload2);
                }
                if (cf || !inChunk) {
                    payload = payload2;
                } else {
                    long payloadLength2 = 0;
                    Iterator<byte[]> it = chunks.iterator();
                    while (it.hasNext()) {
                        payloadLength2 += (long) it.next().length;
                        it = it;
                        payload2 = payload2;
                        inChunk = inChunk;
                        mb = mb;
                    }
                    ensureSanePayloadSize(payloadLength2);
                    byte[] payload3 = new byte[((int) payloadLength2)];
                    int i = 0;
                    Iterator<byte[]> it2 = chunks.iterator();
                    while (it2.hasNext()) {
                        byte[] p = it2.next();
                        System.arraycopy(p, 0, payload3, i, p.length);
                        i += p.length;
                        payloadLength2 = payloadLength2;
                    }
                    tnf = chunkTnf2;
                    payload = payload3;
                }
                if (cf) {
                    inChunk = true;
                    byteBuffer = buffer;
                    id3 = id2;
                    id4 = me;
                    chunkTnf3 = chunkTnf2;
                    type3 = type2;
                } else {
                    inChunk = false;
                    String error = validateTnf(tnf, type2, id2, payload);
                    if (error == null) {
                        records.add(new NdefRecord(tnf, type2, id2, payload));
                        if (ignoreMbMe) {
                            break;
                        }
                        byteBuffer = buffer;
                        id3 = id2;
                        id4 = me;
                        chunkTnf3 = chunkTnf2;
                        type3 = type2;
                    } else {
                        throw new FormatException(error);
                    }
                }
            }
            return (NdefRecord[]) records.toArray(new NdefRecord[records.size()]);
        } catch (BufferUnderflowException e) {
            throw new FormatException("expected more data", e);
        }
    }

    private static void ensureSanePayloadSize(long size) throws FormatException {
        if (size > 10485760) {
            throw new FormatException("payload above max limit: " + size + " > " + MAX_PAYLOAD_SIZE);
        }
    }

    static String validateTnf(short tnf, byte[] type, byte[] id, byte[] payload) {
        switch (tnf) {
            case 0:
                if (type.length == 0 && id.length == 0 && payload.length == 0) {
                    return null;
                }
                return "unexpected data in TNF_EMPTY record";
            case 1:
            case 2:
            case 3:
            case 4:
                return null;
            case 5:
            case 7:
                if (type.length != 0) {
                    return "unexpected type field in TNF_UNKNOWN or TNF_RESERVEd record";
                }
                return null;
            case 6:
                return "unexpected TNF_UNCHANGED in first chunk or logical record";
            default:
                return String.format("unexpected tnf value: 0x%02x", Short.valueOf(tnf));
        }
    }

    /* access modifiers changed from: package-private */
    public void writeToByteBuffer(ByteBuffer buffer, boolean mb, boolean me) {
        boolean il = true;
        int i = 0;
        boolean sr = this.mPayload.length < 256;
        if (this.mTnf != 0 && this.mId.length <= 0) {
            il = false;
        }
        int i2 = (mb ? -128 : 0) | (me ? 64 : 0) | (sr ? 16 : 0);
        if (il) {
            i = 8;
        }
        buffer.put((byte) (i | i2 | this.mTnf));
        buffer.put((byte) this.mType.length);
        if (sr) {
            buffer.put((byte) this.mPayload.length);
        } else {
            buffer.putInt(this.mPayload.length);
        }
        if (il) {
            buffer.put((byte) this.mId.length);
        }
        buffer.put(this.mType);
        buffer.put(this.mId);
        buffer.put(this.mPayload);
    }

    /* access modifiers changed from: package-private */
    public int getByteLength() {
        int length = this.mType.length + 3 + this.mId.length;
        byte[] bArr = this.mPayload;
        int length2 = length + bArr.length;
        boolean il = true;
        boolean sr = bArr.length < 256;
        if (this.mTnf != 0 && this.mId.length <= 0) {
            il = false;
        }
        if (!sr) {
            length2 += 3;
        }
        if (il) {
            return length2 + 1;
        }
        return length2;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mTnf);
        dest.writeInt(this.mType.length);
        dest.writeByteArray(this.mType);
        dest.writeInt(this.mId.length);
        dest.writeByteArray(this.mId);
        dest.writeInt(this.mPayload.length);
        dest.writeByteArray(this.mPayload);
    }

    public int hashCode() {
        return (((((((1 * 31) + Arrays.hashCode(this.mId)) * 31) + Arrays.hashCode(this.mPayload)) * 31) + this.mTnf) * 31) + Arrays.hashCode(this.mType);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        NdefRecord other = (NdefRecord) obj;
        if (Arrays.equals(this.mId, other.mId) && Arrays.equals(this.mPayload, other.mPayload) && this.mTnf == other.mTnf) {
            return Arrays.equals(this.mType, other.mType);
        }
        return false;
    }

    public String toString() {
        StringBuilder b = new StringBuilder(String.format("NdefRecord tnf=%X", Short.valueOf(this.mTnf)));
        if (this.mType.length > 0) {
            b.append(" type=");
            b.append((CharSequence) bytesToString(this.mType));
        }
        if (this.mId.length > 0) {
            b.append(" id=");
            b.append((CharSequence) bytesToString(this.mId));
        }
        if (this.mPayload.length > 0) {
            b.append(" payload=");
            b.append((CharSequence) bytesToString(this.mPayload));
        }
        return b.toString();
    }

    private static StringBuilder bytesToString(byte[] bs) {
        StringBuilder s = new StringBuilder();
        int length = bs.length;
        for (int i = 0; i < length; i++) {
            s.append(String.format("%02X", Byte.valueOf(bs[i])));
        }
        return s;
    }
}
