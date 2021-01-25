package ohos.media.codec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ohos.media.codec.CodecDescription;
import ohos.media.common.Format;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class CodecDescriptionList {
    private static final Logger LOGGER = LoggerFactory.getMediaLogger(CodecDescriptionList.class);
    public static final int NORMAL_CODECS = 0;
    public static final int TOTAL_CODECS = 1;
    private static Map<String, Object> globalSettings;
    private static final Object initLock = new Object();
    private static List<CodecDescription> normalList = new ArrayList();
    private static Map<String, List<Integer>> supportedMimeMaps = new HashMap();
    private static List<CodecDescription> totalList = new ArrayList();
    private List<CodecDescription> codecDescriptionList;

    private static native CodecDescription.CodecAbilities nativeGetCodecAbility(int i, String str);

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

    public static CodecDescription getCodecDescriptionFor(String str) {
        if (str == null) {
            LOGGER.error("getCodecDescriptionFor failed, codecName is null", new Object[0]);
            return null;
        }
        initCodecDesList();
        int nativeGetCodecByName = nativeGetCodecByName(str);
        if (nativeGetCodecByName >= 0 && nativeGetCodecByName < totalList.size()) {
            return totalList.get(nativeGetCodecByName);
        }
        LOGGER.error("getCodecDescriptionFor failed, index: %{public}d is invalid, codecName: %{public}s is invalid", Integer.valueOf(nativeGetCodecByName), str);
        return null;
    }

    public CodecDescriptionList(int i) {
        initCodecDesList();
        if (i == 0) {
            this.codecDescriptionList = normalList;
        } else {
            this.codecDescriptionList = totalList;
        }
    }

    public final List<CodecDescription> getSupportedCodecs() {
        return this.codecDescriptionList;
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
        for (CodecDescription codecDescription : this.codecDescriptionList) {
            if (codecDescription.isEncoder() == z) {
                try {
                    CodecDescription.CodecAbilities abilitiesForType = codecDescription.getAbilitiesForType(stringValue);
                    if (abilitiesForType != null && abilitiesForType.isFormatSupported(format)) {
                        return codecDescription.getName();
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
        for (CodecDescription codecDescription : normalList) {
            if (codecDescription.isEncoder() == z) {
                try {
                    CodecDescription.CodecAbilities abilitiesForType = codecDescription.getAbilitiesForType(stringValue);
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
                    CodecDescription codecDescriptionAt = getCodecDescriptionAt(i);
                    totalList.add(codecDescriptionAt);
                    CodecDescription createNormalMode = codecDescriptionAt.createNormalMode();
                    if (createNormalMode != null) {
                        initSupportedMimeMaps(createNormalMode);
                        normalList.add(createNormalMode);
                    }
                }
            }
        }
    }

    private static void initSupportedMimeMaps(CodecDescription codecDescription) {
        String[] supportedTypes = codecDescription.getSupportedTypes();
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
            CodecDescription codecDescription = normalList.get(num.intValue());
            if (codecDescription.isEncoder() == z && codecDescription.getAbilitiesForType(str) != null) {
                return true;
            }
        }
        return false;
    }

    private static CodecDescription getCodecDescriptionAt(int i) {
        String[] nativeGetCodecSupportedTypes = nativeGetCodecSupportedTypes(i);
        CodecDescription.CodecAbilities[] codecAbilitiesArr = new CodecDescription.CodecAbilities[nativeGetCodecSupportedTypes.length];
        int length = nativeGetCodecSupportedTypes.length;
        int i2 = 0;
        int i3 = 0;
        while (i2 < length) {
            codecAbilitiesArr[i3] = nativeGetCodecAbility(i, nativeGetCodecSupportedTypes[i2]);
            i2++;
            i3++;
        }
        return new CodecDescription(nativeGetCodecName(i), nativeGetCodecAliaseName(i), nativeGetCodecAttributes(i), codecAbilitiesArr);
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
