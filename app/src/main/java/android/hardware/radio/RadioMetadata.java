package android.hardware.radio;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;
import java.util.Set;

public final class RadioMetadata implements Parcelable {
    public static final Creator<RadioMetadata> CREATOR = null;
    private static final ArrayMap<String, Integer> METADATA_KEYS_TYPE = null;
    public static final String METADATA_KEY_ALBUM = "android.hardware.radio.metadata.ALBUM";
    public static final String METADATA_KEY_ART = "android.hardware.radio.metadata.ART";
    public static final String METADATA_KEY_ARTIST = "android.hardware.radio.metadata.ARTIST";
    public static final String METADATA_KEY_CLOCK = "android.hardware.radio.metadata.CLOCK";
    public static final String METADATA_KEY_GENRE = "android.hardware.radio.metadata.GENRE";
    public static final String METADATA_KEY_ICON = "android.hardware.radio.metadata.ICON";
    public static final String METADATA_KEY_RBDS_PTY = "android.hardware.radio.metadata.RBDS_PTY";
    public static final String METADATA_KEY_RDS_PI = "android.hardware.radio.metadata.RDS_PI";
    public static final String METADATA_KEY_RDS_PS = "android.hardware.radio.metadata.RDS_PS";
    public static final String METADATA_KEY_RDS_PTY = "android.hardware.radio.metadata.RDS_PTY";
    public static final String METADATA_KEY_RDS_RT = "android.hardware.radio.metadata.RDS_RT";
    public static final String METADATA_KEY_TITLE = "android.hardware.radio.metadata.TITLE";
    private static final int METADATA_TYPE_BITMAP = 2;
    private static final int METADATA_TYPE_CLOCK = 3;
    private static final int METADATA_TYPE_INT = 0;
    private static final int METADATA_TYPE_INVALID = -1;
    private static final int METADATA_TYPE_TEXT = 1;
    private static final int NATIVE_KEY_ALBUM = 7;
    private static final int NATIVE_KEY_ART = 10;
    private static final int NATIVE_KEY_ARTIST = 6;
    private static final int NATIVE_KEY_CLOCK = 11;
    private static final int NATIVE_KEY_GENRE = 8;
    private static final int NATIVE_KEY_ICON = 9;
    private static final int NATIVE_KEY_INVALID = -1;
    private static final SparseArray<String> NATIVE_KEY_MAPPING = null;
    private static final int NATIVE_KEY_RBDS_PTY = 3;
    private static final int NATIVE_KEY_RDS_PI = 0;
    private static final int NATIVE_KEY_RDS_PS = 1;
    private static final int NATIVE_KEY_RDS_PTY = 2;
    private static final int NATIVE_KEY_RDS_RT = 4;
    private static final int NATIVE_KEY_TITLE = 5;
    private static final String TAG = "RadioMetadata";
    private final Bundle mBundle;

    public static final class Builder {
        private final Bundle mBundle;

        public Builder() {
            this.mBundle = new Bundle();
        }

        public Builder(RadioMetadata source) {
            this.mBundle = new Bundle(source.mBundle);
        }

        public Builder(RadioMetadata source, int maxBitmapSize) {
            this(source);
            for (String key : this.mBundle.keySet()) {
                Bitmap value = this.mBundle.get(key);
                if (value != null && (value instanceof Bitmap)) {
                    Bitmap bmp = value;
                    if (bmp.getHeight() > maxBitmapSize || bmp.getWidth() > maxBitmapSize) {
                        putBitmap(key, scaleBitmap(bmp, maxBitmapSize));
                    }
                }
            }
        }

        public Builder putString(String key, String value) {
            if (RadioMetadata.METADATA_KEYS_TYPE.containsKey(key) && ((Integer) RadioMetadata.METADATA_KEYS_TYPE.get(key)).intValue() == RadioMetadata.NATIVE_KEY_RDS_PS) {
                this.mBundle.putString(key, value);
                return this;
            }
            throw new IllegalArgumentException("The " + key + " key cannot be used to put a String");
        }

        public Builder putInt(String key, int value) {
            if (RadioMetadata.METADATA_KEYS_TYPE.containsKey(key) && ((Integer) RadioMetadata.METADATA_KEYS_TYPE.get(key)).intValue() == 0) {
                this.mBundle.putInt(key, value);
                return this;
            }
            throw new IllegalArgumentException("The " + key + " key cannot be used to put a long");
        }

        public Builder putBitmap(String key, Bitmap value) {
            if (RadioMetadata.METADATA_KEYS_TYPE.containsKey(key) && ((Integer) RadioMetadata.METADATA_KEYS_TYPE.get(key)).intValue() == RadioMetadata.NATIVE_KEY_RDS_PTY) {
                this.mBundle.putParcelable(key, value);
                return this;
            }
            throw new IllegalArgumentException("The " + key + " key cannot be used to put a Bitmap");
        }

        public Builder putClock(String key, long utcSecondsSinceEpoch, int timezoneOffsetMinutes) {
            if (RadioMetadata.METADATA_KEYS_TYPE.containsKey(key) && ((Integer) RadioMetadata.METADATA_KEYS_TYPE.get(key)).intValue() == RadioMetadata.NATIVE_KEY_RBDS_PTY) {
                this.mBundle.putParcelable(key, new Clock(utcSecondsSinceEpoch, timezoneOffsetMinutes));
                return this;
            }
            throw new IllegalArgumentException("The " + key + " key cannot be used to put a RadioMetadata.Clock.");
        }

        public RadioMetadata build() {
            return new RadioMetadata(this.mBundle, null);
        }

        private Bitmap scaleBitmap(Bitmap bmp, int maxSize) {
            float maxSizeF = (float) maxSize;
            float scale = Math.min(maxSizeF / ((float) bmp.getWidth()), maxSizeF / ((float) bmp.getHeight()));
            return Bitmap.createScaledBitmap(bmp, (int) (((float) bmp.getWidth()) * scale), (int) (((float) bmp.getHeight()) * scale), true);
        }
    }

    public static final class Clock implements Parcelable {
        public static final Creator<Clock> CREATOR = null;
        private final int mTimezoneOffsetMinutes;
        private final long mUtcEpochSeconds;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.radio.RadioMetadata.Clock.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hardware.radio.RadioMetadata.Clock.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.hardware.radio.RadioMetadata.Clock.<clinit>():void");
        }

        public int describeContents() {
            return RadioMetadata.NATIVE_KEY_RDS_PI;
        }

        public void writeToParcel(Parcel out, int flags) {
            out.writeLong(this.mUtcEpochSeconds);
            out.writeInt(this.mTimezoneOffsetMinutes);
        }

        public Clock(long utcEpochSeconds, int timezoneOffsetMinutes) {
            this.mUtcEpochSeconds = utcEpochSeconds;
            this.mTimezoneOffsetMinutes = timezoneOffsetMinutes;
        }

        private Clock(Parcel in) {
            this.mUtcEpochSeconds = in.readLong();
            this.mTimezoneOffsetMinutes = in.readInt();
        }

        public long getUtcEpochSeconds() {
            return this.mUtcEpochSeconds;
        }

        public int getTimezoneOffsetMinutes() {
            return this.mTimezoneOffsetMinutes;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.radio.RadioMetadata.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hardware.radio.RadioMetadata.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.radio.RadioMetadata.<clinit>():void");
    }

    /* synthetic */ RadioMetadata(Bundle bundle, RadioMetadata radioMetadata) {
        this(bundle);
    }

    /* synthetic */ RadioMetadata(Parcel in, RadioMetadata radioMetadata) {
        this(in);
    }

    RadioMetadata() {
        this.mBundle = new Bundle();
    }

    private RadioMetadata(Bundle bundle) {
        this.mBundle = new Bundle(bundle);
    }

    private RadioMetadata(Parcel in) {
        this.mBundle = in.readBundle();
    }

    public boolean containsKey(String key) {
        return this.mBundle.containsKey(key);
    }

    public String getString(String key) {
        return this.mBundle.getString(key);
    }

    public int getInt(String key) {
        return this.mBundle.getInt(key, NATIVE_KEY_RDS_PI);
    }

    public Bitmap getBitmap(String key) {
        Bitmap bmp = null;
        try {
            return (Bitmap) this.mBundle.getParcelable(key);
        } catch (Exception e) {
            Log.w(TAG, "Failed to retrieve a key as Bitmap.", e);
            return bmp;
        }
    }

    public Clock getClock(String key) {
        Clock clock = null;
        try {
            return (Clock) this.mBundle.getParcelable(key);
        } catch (Exception e) {
            Log.w(TAG, "Failed to retrieve a key as Clock.", e);
            return clock;
        }
    }

    public int describeContents() {
        return NATIVE_KEY_RDS_PI;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeBundle(this.mBundle);
    }

    public int size() {
        return this.mBundle.size();
    }

    public Set<String> keySet() {
        return this.mBundle.keySet();
    }

    public static String getKeyFromNativeKey(int nativeKey) {
        return (String) NATIVE_KEY_MAPPING.get(nativeKey, null);
    }

    int putIntFromNative(int nativeKey, int value) {
        String key = getKeyFromNativeKey(nativeKey);
        if (!METADATA_KEYS_TYPE.containsKey(key) || ((Integer) METADATA_KEYS_TYPE.get(key)).intValue() != 0) {
            return NATIVE_KEY_INVALID;
        }
        this.mBundle.putInt(key, value);
        return NATIVE_KEY_RDS_PI;
    }

    int putStringFromNative(int nativeKey, String value) {
        String key = getKeyFromNativeKey(nativeKey);
        if (!METADATA_KEYS_TYPE.containsKey(key) || ((Integer) METADATA_KEYS_TYPE.get(key)).intValue() != NATIVE_KEY_RDS_PS) {
            return NATIVE_KEY_INVALID;
        }
        this.mBundle.putString(key, value);
        return NATIVE_KEY_RDS_PI;
    }

    int putBitmapFromNative(int nativeKey, byte[] value) {
        String key = getKeyFromNativeKey(nativeKey);
        if (!METADATA_KEYS_TYPE.containsKey(key) || ((Integer) METADATA_KEYS_TYPE.get(key)).intValue() != NATIVE_KEY_RDS_PTY) {
            return NATIVE_KEY_INVALID;
        }
        try {
            Bitmap bmp = BitmapFactory.decodeByteArray(value, NATIVE_KEY_RDS_PI, value.length);
            if (bmp == null) {
                return NATIVE_KEY_INVALID;
            }
            this.mBundle.putParcelable(key, bmp);
            return NATIVE_KEY_RDS_PI;
        } catch (Exception e) {
            return NATIVE_KEY_INVALID;
        } catch (Throwable th) {
            return NATIVE_KEY_INVALID;
        }
    }

    int putClockFromNative(int nativeKey, long utcEpochSeconds, int timezoneOffsetInMinutes) {
        Log.d(TAG, "putClockFromNative()");
        String key = getKeyFromNativeKey(nativeKey);
        if (!METADATA_KEYS_TYPE.containsKey(key) || ((Integer) METADATA_KEYS_TYPE.get(key)).intValue() != NATIVE_KEY_RBDS_PTY) {
            return NATIVE_KEY_INVALID;
        }
        this.mBundle.putParcelable(key, new Clock(utcEpochSeconds, timezoneOffsetInMinutes));
        return NATIVE_KEY_RDS_PI;
    }
}
