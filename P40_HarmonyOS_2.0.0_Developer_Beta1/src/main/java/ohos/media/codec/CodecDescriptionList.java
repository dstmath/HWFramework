package ohos.media.codec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ohos.media.codec.CodecDescriptionInternal;
import ohos.media.common.Format;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class CodecDescriptionList {
    private static final Logger LOGGER = LoggerFactory.getMediaLogger(CodecDescriptionList.class);
    public static final int NORMAL_CODECS = 0;
    public static final int TOTAL_CODECS = 1;
    private static Map<String, Object> globalSettings;
    private static final Object initLock = new Object();
    private static List<CodecDescriptionInternal> normalList = new ArrayList();
    private static Map<String, List<Integer>> supportedMimeMaps = new HashMap();
    private static List<CodecDescriptionInternal> totalList = new ArrayList();
    private List<CodecDescriptionInternal> mCodecDescriptionInternalList;
    private List<CodecDescription> mCodecDescriptionList;

    private static native CodecDescriptionInternal.CodecAbilities nativeGetCodecAbility(int i, String str);

    private static native String nativeGetCodecAliaseName(int i);

    private static native int nativeGetCodecAttributes(int i);

    private static native int nativeGetCodecByName(String str);

    private static native int nativeGetCodecCount();

    private static native String nativeGetCodecName(int i);

    private static native String[] nativeGetCodecSupportedTypes(int i);

    private static native Map<String, Object> nativeGetGlobalSettings();

    static {
        System.loadLibrary("zcodec_description_jni.z");
    }

    public static CodecDescriptionInternal getCodecDescriptionInternalFor(String str) {
        if (str == null) {
            LOGGER.error("getCodecDescriptionInternalFor failed, codecName is null", new Object[0]);
            return null;
        }
        initCodecDesList();
        int nativeGetCodecByName = nativeGetCodecByName(str);
        if (nativeGetCodecByName >= 0 && nativeGetCodecByName < totalList.size()) {
            return totalList.get(nativeGetCodecByName);
        }
        LOGGER.error("getCodecDescriptionInternalFor failed, index: %{public}d ,codecName: %{public}s ", Integer.valueOf(nativeGetCodecByName), str);
        return null;
    }

    public CodecDescriptionList() {
        this(1);
    }

    public CodecDescriptionList(int i) {
        this.mCodecDescriptionList = new ArrayList();
        initCodecDesList();
        if (i == 0) {
            this.mCodecDescriptionInternalList = normalList;
        } else {
            this.mCodecDescriptionInternalList = totalList;
        }
        int size = this.mCodecDescriptionInternalList.size();
        for (int i2 = 0; i2 < size; i2++) {
            this.mCodecDescriptionList.add(new CodecDescription(this.mCodecDescriptionInternalList.get(i2)));
        }
    }

    public final List<CodecDescriptionInternal> getSupportedInternalCodecs() {
        return this.mCodecDescriptionInternalList;
    }

    public final List<CodecDescription> getSupportedCodecs() {
        return this.mCodecDescriptionList;
    }

    public final List<CodecDescription> getSupportedDecoders() {
        return getCodecDescriptionByFormat(false, null);
    }

    public final List<CodecDescription> getSupportedEncoders() {
        return getCodecDescriptionByFormat(true, null);
    }

    public final List<CodecDescription> findDecoder(Format format) {
        return getCodecDescriptionByFormat(false, format);
    }

    public final List<CodecDescription> findEncoder(Format format) {
        return getCodecDescriptionByFormat(true, format);
    }

    private List<CodecDescription> getCodecDescriptionByFormat(boolean z, Format format) {
        ArrayList arrayList = new ArrayList();
        boolean z2 = format != null;
        for (CodecDescription codecDescription : this.mCodecDescriptionList) {
            if (codecDescription.isEncoder() == z) {
                if (z2) {
                    if (z2) {
                        try {
                            if (!codecDescription.isFormatSupported(format)) {
                            }
                        } catch (IllegalArgumentException unused) {
                        }
                    }
                }
                arrayList.add(codecDescription);
            }
        }
        if (arrayList.size() == 0) {
            return null;
        }
        return arrayList;
    }

    public static List<String> getSupportedMimes() {
        initCodecDesList();
        return new ArrayList(supportedMimeMaps.keySet());
    }

    public static boolean isDecodeSupportedByMime(String str) {
        initCodecDesList();
        return isCodecSupportedByMime(false, str);
    }

    public static boolean isEncodeSupportedByMime(String str) {
        initCodecDesList();
        return isCodecSupportedByMime(true, str);
    }

    public final String getDecoderByFormat(Format format) {
        return getCodecByFormat(false, format);
    }

    public final String getEncoderByFormat(Format format) {
        return getCodecByFormat(true, format);
    }

    public static boolean isDecoderSupportedByFormat(Format format) {
        initCodecDesList();
        return isCodecSupportedByFormat(false, format);
    }

    public static boolean isEncoderSupportedByFormat(Format format) {
        initCodecDesList();
        return isCodecSupportedByFormat(true, format);
    }

    private String getCodecByFormat(boolean z, Format format) {
        if (format == null) {
            LOGGER.error("getCodecByFormat failed, format is null", new Object[0]);
            return null;
        }
        String stringValue = format.getStringValue(Format.MIME);
        for (CodecDescriptionInternal codecDescriptionInternal : this.mCodecDescriptionInternalList) {
            if (codecDescriptionInternal.isEncoder() == z) {
                try {
                    CodecDescriptionInternal.CodecAbilities abilitiesForType = codecDescriptionInternal.getAbilitiesForType(stringValue);
                    if (abilitiesForType != null && abilitiesForType.isFormatSupported(format)) {
                        return codecDescriptionInternal.getName();
                    }
                } catch (IllegalArgumentException unused) {
                    continue;
                }
            }
        }
        return null;
    }

    private static boolean isCodecSupportedByFormat(boolean z, Format format) {
        if (format == null) {
            LOGGER.error("isCodecSupportedByFormat failed, format is null", new Object[0]);
            return false;
        }
        String stringValue = format.getStringValue(Format.MIME);
        for (CodecDescriptionInternal codecDescriptionInternal : normalList) {
            if (codecDescriptionInternal.isEncoder() == z) {
                try {
                    CodecDescriptionInternal.CodecAbilities abilitiesForType = codecDescriptionInternal.getAbilitiesForType(stringValue);
                    if (abilitiesForType != null && abilitiesForType.isFormatSupported(format)) {
                        return true;
                    }
                } catch (IllegalArgumentException unused) {
                    continue;
                }
            }
        }
        return false;
    }

    private static void initCodecDesList() {
        synchronized (initLock) {
            if (normalList.isEmpty()) {
                int nativeGetCodecCount = nativeGetCodecCount();
                for (int i = 0; i < nativeGetCodecCount; i++) {
                    CodecDescriptionInternal codecDescriptionInternalAt = getCodecDescriptionInternalAt(i);
                    totalList.add(codecDescriptionInternalAt);
                    CodecDescriptionInternal createNormalMode = codecDescriptionInternalAt.createNormalMode();
                    if (createNormalMode != null) {
                        initSupportedMimeMaps(createNormalMode);
                        normalList.add(createNormalMode);
                    }
                }
            }
        }
    }

    private static void initSupportedMimeMaps(CodecDescriptionInternal codecDescriptionInternal) {
        String[] supportedTypes = codecDescriptionInternal.getSupportedTypes();
        int size = normalList.size();
        for (String str : supportedTypes) {
            if (!supportedMimeMaps.containsKey(str)) {
                supportedMimeMaps.put(str, new ArrayList(Arrays.asList(Integer.valueOf(size))));
            } else {
                supportedMimeMaps.get(str).add(Integer.valueOf(size));
            }
        }
    }

    private static boolean isCodecSupportedByMime(boolean z, String str) {
        List<Integer> list = supportedMimeMaps.get(str);
        if (list == null) {
            LOGGER.error("isCodecSupportedByMime failed, mime: %{public}s is invalid", str);
            return false;
        }
        for (Integer num : list) {
            CodecDescriptionInternal codecDescriptionInternal = normalList.get(num.intValue());
            if (codecDescriptionInternal.isEncoder() == z && codecDescriptionInternal.getAbilitiesForType(str) != null) {
                return true;
            }
        }
        return false;
    }

    private static CodecDescriptionInternal getCodecDescriptionInternalAt(int i) {
        String[] nativeGetCodecSupportedTypes = nativeGetCodecSupportedTypes(i);
        CodecDescriptionInternal.CodecAbilities[] codecAbilitiesArr = new CodecDescriptionInternal.CodecAbilities[nativeGetCodecSupportedTypes.length];
        int length = nativeGetCodecSupportedTypes.length;
        int i2 = 0;
        int i3 = 0;
        while (i2 < length) {
            codecAbilitiesArr[i3] = nativeGetCodecAbility(i, nativeGetCodecSupportedTypes[i2]);
            i2++;
            i3++;
        }
        return new CodecDescriptionInternal(nativeGetCodecName(i), nativeGetCodecAliaseName(i), nativeGetCodecAttributes(i), codecAbilitiesArr);
    }

    static final Map<String, Object> getGlobalSettings() {
        Map<String, Object> map;
        synchronized (initLock) {
            if (globalSettings == null) {
                globalSettings = nativeGetGlobalSettings();
            }
            map = globalSettings;
        }
        return map;
    }
}
