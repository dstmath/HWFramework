package ohos.media.codec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import ohos.media.codec.CodecDescriptionInternal;
import ohos.media.common.Format;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public final class CodecDescription {
    private static final String C2_CODEC = "C2";
    private static final String DECODER_CODEC = "Decoder";
    private static final String ENCODER_CODEC = "Encoder";
    private static final int FLAG_IS_AUDIO = 16;
    private static final int FLAG_IS_ENCODER = 1;
    private static final int FLAG_IS_HARDWARE_ACCELERATED = 8;
    private static final int FLAG_IS_SOFTWARE = 4;
    private static final int FLAG_IS_VENDOR = 2;
    private static final String HARDWARE_CODEC = "Hardware";
    private static final Logger LOGGER = LoggerFactory.getMediaLogger(CodecDescription.class);
    private static final String OMX_CODEC = "Omx";
    private static final String SOFTWARE_CODEC = "Software";
    private final Map<Format.Key<?>, Object> cachedAbilityMetadata = new ConcurrentHashMap();
    private String mCanonicalName;
    private String mCodecName;
    private int mFlags;
    private CodecDescriptionInternal mInternalDescription;
    private String[] mMimeTypes;
    private String mOriginalName;

    CodecDescription(CodecDescriptionInternal codecDescriptionInternal) {
        this.mOriginalName = codecDescriptionInternal.getName();
        this.mCanonicalName = codecDescriptionInternal.getCanonicalName();
        this.mInternalDescription = codecDescriptionInternal;
        this.mFlags = constructFlags();
        this.mCodecName = getCodecName();
        String[] supportedTypes = this.mInternalDescription.getSupportedTypes();
        this.mMimeTypes = new String[supportedTypes.length];
        int i = 0;
        for (String str : supportedTypes) {
            if (str.startsWith("audio") || str.startsWith("video")) {
                this.mMimeTypes[i] = str;
                i++;
            } else if (str.equals(Format.IMAGE_ANDROID_HEIC)) {
                this.mMimeTypes[i] = Format.VIDEO_HEVC;
                i++;
            } else {
                LOGGER.warn("ctor error type %{public}s", str);
            }
        }
    }

    private int constructFlags() {
        if (this.mOriginalName.toLowerCase().contains("google") || this.mOriginalName.toLowerCase().contains("c2")) {
            this.mFlags |= 4;
        }
        String str = this.mInternalDescription.getSupportedTypes()[0];
        if (!str.isEmpty() && str.toLowerCase().contains("audio")) {
            this.mFlags |= 16;
        }
        if (this.mOriginalName.toLowerCase().contains("encoder")) {
            this.mFlags |= 1;
        }
        return this.mFlags;
    }

    public String captureName(String str) {
        if (str.length() < 1) {
            return "";
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private String getCodecName() {
        String str;
        if ((this.mFlags & 4) != 0) {
            str = "Software";
        } else {
            str = "" + HARDWARE_CODEC;
        }
        String str2 = this.mInternalDescription.getSupportedTypes()[0];
        if (!str2.isEmpty()) {
            str = str + captureName(str2.substring(str2.indexOf("/") + 1));
        }
        if ((this.mFlags & 4) != 0) {
            if (this.mOriginalName.toLowerCase().contains("c2")) {
                str = str + C2_CODEC;
            } else if (this.mOriginalName.toLowerCase().contains("omx")) {
                str = str + OMX_CODEC;
            }
        }
        if ((this.mFlags & 1) != 0) {
            return str + ENCODER_CODEC;
        }
        return str + DECODER_CODEC;
    }

    public String getName() {
        return this.mCodecName;
    }

    public boolean isEncoder() {
        return (this.mFlags & 1) != 0;
    }

    public boolean isAudio() {
        return (this.mFlags & 16) != 0;
    }

    public boolean isSoftware() {
        return (this.mFlags & 4) != 0;
    }

    public String[] getMimeTypes() {
        return this.mMimeTypes;
    }

    public Format getSupportedFormat() {
        if (isAudio()) {
            return createAudioFormat();
        }
        return createVideoFormat();
    }

    public boolean isFormatSupported(Format format) {
        String stringValue = format.getStringValue(Format.MIME);
        String[] supportedTypes = this.mInternalDescription.getSupportedTypes();
        for (String str : supportedTypes) {
            if (str.equalsIgnoreCase(stringValue) && this.mInternalDescription.getAbilitiesForType(str).isFormatSupported(format)) {
                return true;
            }
        }
        return false;
    }

    public <T> T getPropertyValue(Format.Key<T> key) {
        Objects.requireNonNull(key, "property should not be null!");
        return Optional.ofNullable(this.cachedAbilityMetadata.get(key)).filter(new Predicate() {
            /* class ohos.media.codec.$$Lambda$CodecDescription$sR1M0jmu1zR94lN4Dg64dQfWg0g */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return CodecDescription.lambda$getPropertyValue$0(Format.Key.this, obj);
            }
        }).orElse(null);
    }

    public <T> List<T> getPropertyScope(Format.Key<T> key) {
        Optional<PropertyKeyWrapper> propertyScopeKey = getPropertyScopeKey(key);
        int i = 0;
        if (!propertyScopeKey.isPresent()) {
            LOGGER.warn("Cannot find range key for %{public}s", key.toString());
            return Collections.emptyList();
        }
        PropertyKeyWrapper propertyKeyWrapper = propertyScopeKey.get();
        if (propertyKeyWrapper.isBool) {
            Boolean bool = (Boolean) getPropertyValue(propertyKeyWrapper.key);
            if (bool == null || !bool.booleanValue()) {
                return Collections.singletonList(Boolean.FALSE);
            }
            return Arrays.asList(Boolean.TRUE, Boolean.FALSE);
        }
        Object propertyValue = getPropertyValue(propertyKeyWrapper.key);
        if (propertyValue == null) {
            LOGGER.warn("getPropertyValue returns null for key %{public}s", propertyKeyWrapper.key);
            return Collections.emptyList();
        }
        ArrayList arrayList = new ArrayList();
        if (propertyValue instanceof byte[]) {
            byte[] bArr = (byte[]) propertyValue;
            int length = bArr.length;
            while (i < length) {
                arrayList.add(Byte.valueOf(bArr[i]));
                i++;
            }
        } else if (propertyValue instanceof int[]) {
            int[] iArr = (int[]) propertyValue;
            int length2 = iArr.length;
            while (i < length2) {
                arrayList.add(Integer.valueOf(iArr[i]));
                i++;
            }
        } else if (propertyValue instanceof float[]) {
            float[] fArr = (float[]) propertyValue;
            int length3 = fArr.length;
            while (i < length3) {
                arrayList.add(Float.valueOf(fArr[i]));
                i++;
            }
        } else if ((propertyValue instanceof Float) || (propertyValue instanceof Long) || (propertyValue instanceof Integer)) {
            arrayList.add(propertyValue);
        } else {
            Object[] objArr = (Object[]) propertyValue;
            int length4 = objArr.length;
            while (i < length4) {
                arrayList.add(objArr[i]);
                i++;
            }
        }
        return arrayList;
    }

    private Optional<PropertyKeyWrapper> getPropertyScopeKey(Format.Key<?> key) {
        if (key == null || !this.cachedAbilityMetadata.containsKey(key)) {
            return Optional.empty();
        }
        if (key.checkType(Boolean.class)) {
            return Optional.of(new PropertyKeyWrapper(true, key));
        }
        return Optional.of(new PropertyKeyWrapper(false, key));
    }

    public static class PropertyKeyWrapper {
        private final boolean isBool;
        private final Format.Key<?> key;

        public PropertyKeyWrapper(boolean z, Format.Key<?> key2) {
            this.isBool = z;
            this.key = key2;
        }
    }

    public Format createAudioFormat() {
        Format format = new Format();
        String[] supportedTypes = this.mInternalDescription.getSupportedTypes();
        format.putStringValue(Format.MIME, supportedTypes[0]);
        CodecDescriptionInternal.AudioAbilities audioAbilities = this.mInternalDescription.getAbilitiesForType(supportedTypes[0]).getAudioAbilities();
        if (audioAbilities != null) {
            format.putObjectValue(Format.KEY_SAMPLE_RATE_SCOPE, audioAbilities.getSupportedSampleRateScope());
            format.putObjectValue(Format.KEY_SAMPLE_RATE_LIST, audioAbilities.getSupportedSampleRates());
            format.putObjectValue(Format.KEY_BIT_RATE_SCOPE, audioAbilities.getSupportedBitrateScope());
            format.putIntValue(Format.CHANNEL, audioAbilities.getMaxCaptureChannelNum());
        }
        return format;
    }

    public Format createSubtitleFormat(String str, String str2) {
        Format format = new Format();
        format.putStringValue(Format.MIME, str);
        format.putStringValue(Format.LANGUAGE, str2);
        return format;
    }

    public Format createVideoFormat() {
        CodecDescriptionInternal.EncoderAbilities encoderAbilities;
        Format format = new Format();
        String[] supportedTypes = this.mInternalDescription.getSupportedTypes();
        format.putStringValue(Format.MIME, supportedTypes[0]);
        CodecDescriptionInternal.CodecAbilities abilitiesForType = this.mInternalDescription.getAbilitiesForType(supportedTypes[0]);
        CodecDescriptionInternal.VideoAbilities videoAbilities = abilitiesForType.getVideoAbilities();
        if (videoAbilities != null) {
            format.putObjectValue(Format.KEY_FRAME_RATE_SCOPE, videoAbilities.getSupportedFrameRates());
            format.putObjectValue(Format.KEY_HEIGHT_SCOPE, videoAbilities.getSupportedVideoHights());
            format.putObjectValue(Format.KEY_WIDTH_SCOPE, videoAbilities.getSupportedVideoWidths());
            format.putObjectValue(Format.KEY_BIT_RATE_SCOPE, videoAbilities.getSupportedBitrateRange());
        }
        if (isEncoder() && (encoderAbilities = abilitiesForType.getEncoderAbilities()) != null) {
            format.putObjectValue(Format.KEY_CODEC_COMPLEXITY_SCOPE, encoderAbilities.getEncoderComplexityScope());
            format.putObjectValue(Format.KEY_CODEC_QUALITY_SCOPE, encoderAbilities.getEncoderQualityScope());
        }
        return format;
    }
}
