package android.media;

import android.os.Parcel;
import android.util.Log;
import android.util.MathUtils;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.TimeZone;

@Deprecated
public class Metadata {
    public static final int ALBUM = 8;
    public static final int ALBUM_ART = 18;
    public static final int ANY = 0;
    public static final int ARTIST = 9;
    public static final int AUDIO_BIT_RATE = 21;
    public static final int AUDIO_CODEC = 26;
    public static final int AUDIO_SAMPLE_RATE = 23;
    public static final int AUTHOR = 10;
    public static final int BIT_RATE = 20;
    public static final int BOOLEAN_VAL = 3;
    public static final int BYTE_ARRAY_VAL = 7;
    public static final int CD_TRACK_MAX = 16;
    public static final int CD_TRACK_NUM = 15;
    public static final int COMMENT = 6;
    public static final int COMPOSER = 11;
    public static final int COPYRIGHT = 7;
    public static final int DATE = 13;
    public static final int DATE_VAL = 6;
    public static final int DOUBLE_VAL = 5;
    public static final int DRM_CRIPPLED = 31;
    public static final int DURATION = 14;
    private static final int FIRST_CUSTOM = 8192;
    public static final int GENRE = 12;
    public static final int INTEGER_VAL = 2;
    private static final int LAST_SYSTEM = 31;
    private static final int LAST_TYPE = 7;
    public static final int LONG_VAL = 4;
    public static final Set<Integer> MATCH_ALL = null;
    public static final Set<Integer> MATCH_NONE = null;
    public static final int MIME_TYPE = 25;
    public static final int NUM_TRACKS = 30;
    public static final int PAUSE_AVAILABLE = 1;
    public static final int RATING = 17;
    public static final int SEEK_AVAILABLE = 4;
    public static final int SEEK_BACKWARD_AVAILABLE = 2;
    public static final int SEEK_FORWARD_AVAILABLE = 3;
    public static final int STRING_VAL = 1;
    private static final String TAG = "media.Metadata";
    public static final int TITLE = 5;
    public static final int VIDEO_BIT_RATE = 22;
    public static final int VIDEO_CODEC = 27;
    public static final int VIDEO_FRAME = 19;
    public static final int VIDEO_FRAME_RATE = 24;
    public static final int VIDEO_HEIGHT = 28;
    public static final int VIDEO_WIDTH = 29;
    private static final int kInt32Size = 4;
    private static final int kMetaHeaderSize = 8;
    private static final int kMetaMarker = 1296389185;
    private static final int kRecordHeaderSize = 12;
    private final HashMap<Integer, Integer> mKeyToPosMap;
    private Parcel mParcel;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.Metadata.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.Metadata.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.Metadata.<clinit>():void");
    }

    public Metadata() {
        this.mKeyToPosMap = new HashMap();
    }

    private boolean scanAllRecords(Parcel parcel, int bytesLeft) {
        int recCount = ANY;
        boolean error = false;
        this.mKeyToPosMap.clear();
        while (bytesLeft > kRecordHeaderSize) {
            int start = parcel.dataPosition();
            int size = parcel.readInt();
            if (size <= kRecordHeaderSize) {
                Log.e(TAG, "Record is too short");
                error = true;
                break;
            }
            int metadataId = parcel.readInt();
            if (!checkMetadataId(metadataId)) {
                error = true;
                break;
            } else if (this.mKeyToPosMap.containsKey(Integer.valueOf(metadataId))) {
                Log.e(TAG, "Duplicate metadata ID found");
                error = true;
                break;
            } else {
                this.mKeyToPosMap.put(Integer.valueOf(metadataId), Integer.valueOf(parcel.dataPosition()));
                int metadataType = parcel.readInt();
                if (metadataType <= 0 || metadataType > LAST_TYPE) {
                    Log.e(TAG, "Invalid metadata type " + metadataType);
                    error = true;
                    break;
                }
                try {
                    parcel.setDataPosition(MathUtils.addOrThrow(start, size));
                    bytesLeft -= size;
                    recCount += STRING_VAL;
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Invalid size: " + e.getMessage());
                    error = true;
                }
            }
        }
        if (bytesLeft == 0 && !error) {
            return true;
        }
        Log.e(TAG, "Ran out of data or error on record " + recCount);
        this.mKeyToPosMap.clear();
        return false;
    }

    public boolean parse(Parcel parcel) {
        if (parcel.dataAvail() < kMetaHeaderSize) {
            Log.e(TAG, "Not enough data " + parcel.dataAvail());
            return false;
        }
        int pin = parcel.dataPosition();
        int size = parcel.readInt();
        if (parcel.dataAvail() + kInt32Size < size || size < kMetaHeaderSize) {
            Log.e(TAG, "Bad size " + size + " avail " + parcel.dataAvail() + " position " + pin);
            parcel.setDataPosition(pin);
            return false;
        }
        int kShouldBeMetaMarker = parcel.readInt();
        if (kShouldBeMetaMarker != kMetaMarker) {
            Log.e(TAG, "Marker missing " + Integer.toHexString(kShouldBeMetaMarker));
            parcel.setDataPosition(pin);
            return false;
        } else if (scanAllRecords(parcel, size - 8)) {
            this.mParcel = parcel;
            return true;
        } else {
            parcel.setDataPosition(pin);
            return false;
        }
    }

    public Set<Integer> keySet() {
        return this.mKeyToPosMap.keySet();
    }

    public boolean has(int metadataId) {
        if (checkMetadataId(metadataId)) {
            return this.mKeyToPosMap.containsKey(Integer.valueOf(metadataId));
        }
        throw new IllegalArgumentException("Invalid key: " + metadataId);
    }

    public String getString(int key) {
        checkType(key, STRING_VAL);
        return this.mParcel.readString();
    }

    public int getInt(int key) {
        checkType(key, SEEK_BACKWARD_AVAILABLE);
        return this.mParcel.readInt();
    }

    public boolean getBoolean(int key) {
        checkType(key, SEEK_FORWARD_AVAILABLE);
        if (this.mParcel.readInt() == STRING_VAL) {
            return true;
        }
        return false;
    }

    public long getLong(int key) {
        checkType(key, kInt32Size);
        return this.mParcel.readLong();
    }

    public double getDouble(int key) {
        checkType(key, TITLE);
        return this.mParcel.readDouble();
    }

    public byte[] getByteArray(int key) {
        checkType(key, LAST_TYPE);
        return this.mParcel.createByteArray();
    }

    public Date getDate(int key) {
        checkType(key, DATE_VAL);
        long timeSinceEpoch = this.mParcel.readLong();
        String timeZone = this.mParcel.readString();
        if (timeZone.length() == 0) {
            return new Date(timeSinceEpoch);
        }
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
        cal.setTimeInMillis(timeSinceEpoch);
        return cal.getTime();
    }

    public static int lastSytemId() {
        return LAST_SYSTEM;
    }

    public static int firstCustomId() {
        return FIRST_CUSTOM;
    }

    public static int lastType() {
        return LAST_TYPE;
    }

    private boolean checkMetadataId(int val) {
        if (val > 0 && (LAST_SYSTEM >= val || val >= FIRST_CUSTOM)) {
            return true;
        }
        Log.e(TAG, "Invalid metadata ID " + val);
        return false;
    }

    private void checkType(int key, int expectedType) {
        this.mParcel.setDataPosition(((Integer) this.mKeyToPosMap.get(Integer.valueOf(key))).intValue());
        int type = this.mParcel.readInt();
        if (type != expectedType) {
            throw new IllegalStateException("Wrong type " + expectedType + " but got " + type);
        }
    }

    public void recycleParcel() {
        if (this.mParcel != null) {
            this.mParcel.recycle();
        }
    }
}
