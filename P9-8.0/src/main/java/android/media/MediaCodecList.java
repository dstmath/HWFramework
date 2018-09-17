package android.media;

import android.media.MediaCodecInfo.CodecCapabilities;
import android.util.Log;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public final class MediaCodecList {
    public static final int ALL_CODECS = 1;
    public static final int REGULAR_CODECS = 0;
    private static final String TAG = "MediaCodecList";
    private static MediaCodecInfo[] sAllCodecInfos;
    private static Map<String, Object> sGlobalSettings;
    private static Object sInitLock = new Object();
    private static MediaCodecInfo[] sRegularCodecInfos;
    private MediaCodecInfo[] mCodecInfos;

    static final native int findCodecByName(String str);

    static final native CodecCapabilities getCodecCapabilities(int i, String str);

    static final native String getCodecName(int i);

    static final native String[] getSupportedTypes(int i);

    static final native boolean isEncoder(int i);

    private static final native int native_getCodecCount();

    static final native Map<String, Object> native_getGlobalSettings();

    private static final native void native_init();

    public static final int getCodecCount() {
        initCodecList();
        return sRegularCodecInfos.length;
    }

    public static final MediaCodecInfo getCodecInfoAt(int index) {
        initCodecList();
        if (index >= 0 && index <= sRegularCodecInfos.length) {
            return sRegularCodecInfos[index];
        }
        throw new IllegalArgumentException();
    }

    static final Map<String, Object> getGlobalSettings() {
        synchronized (sInitLock) {
            if (sGlobalSettings == null) {
                sGlobalSettings = native_getGlobalSettings();
            }
        }
        return sGlobalSettings;
    }

    static {
        System.loadLibrary("media_jni");
        native_init();
    }

    private static final void initCodecList() {
        synchronized (sInitLock) {
            if (sRegularCodecInfos == null) {
                int count = native_getCodecCount();
                ArrayList<MediaCodecInfo> regulars = new ArrayList();
                ArrayList<MediaCodecInfo> all = new ArrayList();
                for (int index = 0; index < count; index++) {
                    try {
                        MediaCodecInfo info = getNewCodecInfoAt(index);
                        all.add(info);
                        info = info.makeRegular();
                        if (info != null) {
                            regulars.add(info);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Could not get codec capabilities", e);
                    }
                }
                sRegularCodecInfos = (MediaCodecInfo[]) regulars.toArray(new MediaCodecInfo[regulars.size()]);
                sAllCodecInfos = (MediaCodecInfo[]) all.toArray(new MediaCodecInfo[all.size()]);
            }
        }
    }

    private static MediaCodecInfo getNewCodecInfoAt(int index) {
        String[] supportedTypes = getSupportedTypes(index);
        CodecCapabilities[] caps = new CodecCapabilities[supportedTypes.length];
        int i = 0;
        int length = supportedTypes.length;
        int typeIx = 0;
        while (i < length) {
            int typeIx2 = typeIx + 1;
            caps[typeIx] = getCodecCapabilities(index, supportedTypes[i]);
            i++;
            typeIx = typeIx2;
        }
        return new MediaCodecInfo(getCodecName(index), isEncoder(index), caps);
    }

    public static MediaCodecInfo getInfoFor(String codec) {
        initCodecList();
        return sAllCodecInfos[findCodecByName(codec)];
    }

    private MediaCodecList() {
        this(0);
    }

    public MediaCodecList(int kind) {
        initCodecList();
        if (kind == 0) {
            this.mCodecInfos = sRegularCodecInfos;
        } else {
            this.mCodecInfos = sAllCodecInfos;
        }
    }

    public final MediaCodecInfo[] getCodecInfos() {
        return (MediaCodecInfo[]) Arrays.copyOf(this.mCodecInfos, this.mCodecInfos.length);
    }

    public final String findDecoderForFormat(MediaFormat format) {
        return findCodecForFormat(false, format);
    }

    public final String findEncoderForFormat(MediaFormat format) {
        return findCodecForFormat(true, format);
    }

    private String findCodecForFormat(boolean encoder, MediaFormat format) {
        String mime = format.getString(MediaFormat.KEY_MIME);
        for (MediaCodecInfo info : this.mCodecInfos) {
            if (info.isEncoder() == encoder) {
                try {
                    CodecCapabilities caps = info.getCapabilitiesForType(mime);
                    if (caps != null && caps.isFormatSupported(format)) {
                        return info.getName();
                    }
                } catch (IllegalArgumentException e) {
                }
            }
        }
        return null;
    }
}
