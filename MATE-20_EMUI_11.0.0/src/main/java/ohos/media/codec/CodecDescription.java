package ohos.media.codec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import ohos.media.audio.AudioStreamInfo;
import ohos.media.common.Format;
import ohos.media.common.Utils;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;
import ohos.media.utils.trace.Tracer;
import ohos.miscservices.httpaccess.HttpConstant;
import ohos.telephony.TelephonyUtils;
import ohos.utils.Dimension;
import ohos.utils.Pair;
import ohos.utils.RationalNumber;
import ohos.utils.Scope;
import ohos.utils.system.safwk.java.SystemAbilityDefinition;
import ohos.workscheduler.WorkInfo;

public final class CodecDescription {
    private static final Scope<Integer> BITRATE_RANGE = Scope.create(0, 500000000);
    private static final int DEFAULT_MAX_SUPPORTED_INSTANCES = 32;
    private static final int ERROR_COMPLEXITY = -1;
    private static final int ERROR_NONE_SUPPORTED = 4;
    private static final int ERROR_UNRECOGNIZED = 1;
    private static final int ERROR_UNSUPPORTED = 2;
    private static final int FLAG_IS_ENCODER = 1;
    private static final Scope<Integer> FRAME_RATE_RANGE = Scope.create(0, 960);
    private static final Logger LOGGER = LoggerFactory.getMediaLogger(CodecDescription.class);
    private static final int MAX_SUPPORTED_INSTANCES_LIMIT = 256;
    private static final Scope<Integer> POSITIVE_INTEGERS = Scope.create(1, Integer.MAX_VALUE);
    private static final Scope<Long> POSITIVE_LONGS = Scope.create(1L, Long.MAX_VALUE);
    private static final Scope<RationalNumber> POSITIVE_RATIONALS = Scope.create(new RationalNumber(1, Integer.MAX_VALUE), new RationalNumber(Integer.MAX_VALUE, 1));
    private static final Scope<Integer> SIZE_RANGE = Scope.create(1, 32768);
    private Map<String, CodecAbilities> abilities = new HashMap();
    private String canonicalName;
    private int flags;
    private String name;

    CodecDescription(String str, String str2, int i, CodecAbilities[] codecAbilitiesArr) {
        this.name = str;
        this.canonicalName = str2;
        this.flags = i;
        for (CodecAbilities codecAbilities : codecAbilitiesArr) {
            this.abilities.put(codecAbilities.getMime(), codecAbilities);
        }
    }

    /* access modifiers changed from: private */
    public static int checkPowerOfTwo(int i, String str) {
        if (((i - 1) & i) == 0) {
            return i;
        }
        throw new IllegalArgumentException(str);
    }

    public String getName() {
        return this.name;
    }

    public boolean isEncoder() {
        return (this.flags & 1) != 0;
    }

    public String[] getSupportedTypes() {
        Set<String> keySet = this.abilities.keySet();
        String[] strArr = (String[]) keySet.toArray(new String[keySet.size()]);
        Arrays.sort(strArr);
        return strArr;
    }

    public CodecAbilities getAbilitiesForType(String str) {
        CodecAbilities codecAbilities = this.abilities.get(str);
        if (codecAbilities == null) {
            return null;
        }
        return codecAbilities.copy();
    }

    public CodecDescription createNormalMode() {
        ArrayList arrayList = new ArrayList();
        for (CodecAbilities codecAbilities : this.abilities.values()) {
            if (codecAbilities.isNormalMode()) {
                arrayList.add(codecAbilities);
            }
        }
        if (arrayList.size() == 0) {
            return null;
        }
        if (arrayList.size() == this.abilities.size()) {
            return this;
        }
        return new CodecDescription(this.name, this.canonicalName, this.flags, (CodecAbilities[]) arrayList.toArray(new CodecAbilities[arrayList.size()]));
    }

    /* access modifiers changed from: private */
    public static class Category {
        private boolean isDefault;
        private String name;
        private int value;

        Category(String str, int i, boolean z) {
            this.name = str;
            this.value = i;
            this.isDefault = z;
        }

        public String getName() {
            return this.name;
        }

        public int getValue() {
            return this.value;
        }

        public boolean isDefault() {
            return this.isDefault;
        }
    }

    public static final class CodecAbilities {
        public static final String CATEGORY_ADAPTIVE_PLAYBACK = "adaptive-playback";
        public static final String CATEGORY_DYNAMICTIMESTAMP = "dynamic-timestamp";
        public static final String CATEGORY_FRAMEPARSING = "frame-parsing";
        public static final String CATEGORY_INTRAREFRESH = "intra-refresh";
        public static final String CATEGORY_MULTIPLEFRAMES = "multiple-frames";
        public static final String CATEGORY_PARTIALFRAME = "partial-frame";
        public static final String CATEGORY_SECURE_PLAYBACK = "secure-playback";
        public static final String CATEGORY_TUNNELEDPLAYBACK = "tunneled-playback";
        public static final int COLOR_FORMAT_16BIT_RGB565 = 6;
        public static final int COLOR_FORMAT_24BIT_BGR888 = 12;
        public static final int COLOR_FORMAT_32BIT_ABGR8888 = 2130747392;
        public static final int COLOR_FORMAT_L16 = 36;
        public static final int COLOR_FORMAT_L8 = 35;
        public static final int COLOR_FORMAT_RAW_BAYER_10BIT = 31;
        public static final int COLOR_FORMAT_RAW_BAYER_8BIT = 30;
        public static final int COLOR_FORMAT_RAW_BAYER_8BIT_COMPRESSED = 32;
        public static final int COLOR_FORMAT_RGBA_FLEXIBLE = 2134288520;
        public static final int COLOR_FORMAT_RGB_FLEXIBLE = 2134292616;
        public static final int COLOR_FORMAT_SURFACE = 2130708361;
        public static final int COLOR_FORMAT_YUV420_FLEXIBLE = 2135033992;
        public static final int COLOR_FORMAT_YUV422_FLEXIBLE = 2135042184;
        public static final int COLOR_FORMAT_YUV444_FLEXIBLE = 2135181448;
        private static final Category[] DECODER_CATEGORIES = {new Category(CATEGORY_ADAPTIVE_PLAYBACK, 1, true), new Category(CATEGORY_SECURE_PLAYBACK, 2, false), new Category(CATEGORY_TUNNELEDPLAYBACK, 4, false), new Category(CATEGORY_PARTIALFRAME, 8, false), new Category(CATEGORY_FRAMEPARSING, 16, false), new Category(CATEGORY_MULTIPLEFRAMES, 32, false), new Category(CATEGORY_DYNAMICTIMESTAMP, 64, false)};
        private static final Category[] ENCODER_CATEGORIES = {new Category(CATEGORY_INTRAREFRESH, 1, false), new Category(CATEGORY_MULTIPLEFRAMES, 2, false), new Category(CATEGORY_DYNAMICTIMESTAMP, 4, false)};
        private AudioAbilities audioAbilities;
        private Format capabilitiesInfo;
        private Format defaultFormat;
        private EncoderAbilities encoderAbilities;
        int error;
        private int flagsRequired;
        private int flagsSupported;
        private int maxConcurrentInstances;
        public int[] mediaColorFormats;
        private String mime;
        public ProfileLevel[] profileLevels;
        private VideoAbilities videoAbilities;

        public CodecAbilities() {
            this.maxConcurrentInstances = 32;
        }

        CodecAbilities(ProfileLevel[] profileLevelArr, int[] iArr, boolean z, Map<String, Object> map, Map<String, Object> map2) {
            this(profileLevelArr, iArr, z, new Format(map), new Format(map2));
        }

        CodecAbilities(ProfileLevel[] profileLevelArr, int[] iArr, boolean z, Format format, Format format2) {
            this.maxConcurrentInstances = 32;
            this.mediaColorFormats = iArr;
            this.defaultFormat = format;
            this.capabilitiesInfo = format2;
            this.mime = this.defaultFormat.getStringValue(Format.MIME);
            if (profileLevelArr.length == 0 && this.mime.equalsIgnoreCase(Format.VIDEO_VP9)) {
                ProfileLevel profileLevel = new ProfileLevel();
                profileLevel.profile = 1;
                profileLevel.level = VideoAbilities.setVp9Level(format2);
                profileLevelArr = new ProfileLevel[]{profileLevel};
            }
            this.profileLevels = profileLevelArr;
            if (this.mime.toLowerCase(Locale.ENGLISH).startsWith("audio/")) {
                this.audioAbilities = AudioAbilities.create(format2, this);
                this.audioAbilities.getFormat(this.defaultFormat);
            }
            if (this.mime.toLowerCase(Locale.ENGLISH).startsWith("video/")) {
                this.videoAbilities = VideoAbilities.create(format2, this);
            }
            if (z) {
                this.encoderAbilities = EncoderAbilities.create(format2, this);
                this.encoderAbilities.getFormat(this.defaultFormat);
            }
            this.maxConcurrentInstances = Utils.parseInt(CodecDescriptionList.getGlobalSettings().get("max-concurrent-instances"), 32);
            HashMap<String, Object> formatMap = format2.getFormatMap();
            this.maxConcurrentInstances = ((Integer) Scope.create(1, 256).clamp(Integer.valueOf(Utils.parseInt(formatMap.get("max-concurrent-instances"), this.maxConcurrentInstances)))).intValue();
            Category[] validCategories = getValidCategories();
            for (Category category : validCategories) {
                String str = Format.CODEC_FEATURE + category.name;
                Object obj = formatMap.get(str);
                if (obj instanceof Integer) {
                    if (((Integer) obj).intValue() > 0) {
                        this.flagsRequired |= category.value;
                    }
                    this.flagsSupported = category.value | this.flagsSupported;
                    this.defaultFormat.putIntValue(str, 1);
                }
            }
        }

        /* access modifiers changed from: private */
        public static boolean supportsBitrate(Scope<Integer> scope, Format format) {
            HashMap<String, Object> formatMap = format.getFormatMap();
            Object obj = formatMap.get(Format.MAX_BIT_RATE);
            Integer num = null;
            Integer num2 = obj instanceof Integer ? (Integer) obj : null;
            Object obj2 = formatMap.get(Format.BIT_RATE);
            if (obj2 instanceof Integer) {
                num = (Integer) obj2;
            }
            if (num != null) {
                if (num2 == null) {
                    return false;
                }
                num2 = Integer.valueOf(Math.max(num.intValue(), num2.intValue()));
            }
            if (num2 == null || num2.intValue() <= 0) {
                return true;
            }
            return scope.contains((Scope<Integer>) num2);
        }

        public static CodecAbilities createFromProfileLevel(String str, int i, int i2) {
            ProfileLevel profileLevel = new ProfileLevel();
            profileLevel.profile = i;
            profileLevel.level = i2;
            Format format = new Format();
            format.putStringValue(Format.MIME, str);
            CodecAbilities codecAbilities = new CodecAbilities(new ProfileLevel[]{profileLevel}, new int[0], true, format, new Format());
            if (codecAbilities.error == 0) {
                return codecAbilities;
            }
            CodecDescription.LOGGER.error("createFromProfileLevel failed, ret.error is %{public}d, mime is %{public}s, profile is %{public}d, level is %{public}d", Integer.valueOf(codecAbilities.error), str, Integer.valueOf(i), Integer.valueOf(i2));
            return null;
        }

        public boolean isCategorySupported(String str) {
            return checkCategory(str, this.flagsSupported);
        }

        private boolean isCategoryRequired(String str) {
            return checkCategory(str, this.flagsRequired);
        }

        private Category[] getValidCategories() {
            if (!isEncoder()) {
                return DECODER_CATEGORIES;
            }
            return ENCODER_CATEGORIES;
        }

        private boolean checkCategory(String str, int i) {
            Category[] validCategories = getValidCategories();
            for (Category category : validCategories) {
                if (category.getName().equals(str)) {
                    if ((category.getValue() & i) != 0) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }
            return false;
        }

        public boolean isNormalMode() {
            Category[] validCategories = getValidCategories();
            for (Category category : validCategories) {
                if (!category.isDefault() && isCategoryRequired(category.getName())) {
                    return false;
                }
            }
            return true;
        }

        public boolean isFormatSupported(Format format) {
            HashMap<String, Object> formatMap = format.getFormatMap();
            Object obj = formatMap.get(Format.MIME);
            if (!((obj instanceof String) && this.mime.equalsIgnoreCase((String) obj))) {
                return false;
            }
            Category[] validCategories = getValidCategories();
            for (Category category : validCategories) {
                Object obj2 = formatMap.get(Format.CODEC_FEATURE + category.getName());
                if (obj2 instanceof Integer) {
                    Integer num = (Integer) obj2;
                    if ((num.intValue() == 1 && !isCategorySupported(category.getName())) || (num.intValue() == 0 && isCategoryRequired(category.getName()))) {
                        CodecDescription.LOGGER.error("isCategorySupported failed or isCategoryRequired failed, exist is %{public}d, categoryName is %{public}s", num, category.getName());
                        return false;
                    }
                }
            }
            if (!isProfileLevelAbilitiesSupport(formatMap)) {
                CodecDescription.LOGGER.error("isProfileLevelAbilitiesSupport failed", new Object[0]);
                return false;
            }
            AudioAbilities audioAbilities2 = this.audioAbilities;
            if (audioAbilities2 == null || audioAbilities2.isFormatSupported(format)) {
                VideoAbilities videoAbilities2 = this.videoAbilities;
                if (videoAbilities2 == null || videoAbilities2.isFormatSupported(format)) {
                    EncoderAbilities encoderAbilities2 = this.encoderAbilities;
                    if (encoderAbilities2 == null || encoderAbilities2.isFormatSupported(format)) {
                        return true;
                    }
                    CodecDescription.LOGGER.error("encoderAbilities isFormatSupported failed", new Object[0]);
                    return false;
                }
                CodecDescription.LOGGER.error("videoAbility isFormatSupported failed", new Object[0]);
                return false;
            }
            CodecDescription.LOGGER.error("audioAbilities isFormatSupported failed", new Object[0]);
            return false;
        }

        private boolean isProfileLevelAbilitiesSupport(Map<String, Object> map) {
            Integer num = (Integer) map.get(Format.CODEC_PROFILE);
            Integer num2 = (Integer) map.get(Format.CODEC_LEVEL);
            if (num != null) {
                if (!supportsProfileLevel(num.intValue(), num2)) {
                    CodecDescription.LOGGER.error("supportsProfileLevel failed ,profile is %{public}d,level is %{public}d", num, num2);
                    return false;
                }
                ProfileLevel[] profileLevelArr = this.profileLevels;
                int i = 0;
                for (ProfileLevel profileLevel : profileLevelArr) {
                    if (profileLevel.profile == num.intValue() && profileLevel.level > i) {
                        i = profileLevel.level;
                    }
                }
                CodecAbilities createFromProfileLevel = createFromProfileLevel(this.mime, num.intValue(), i);
                HashMap hashMap = new HashMap(map);
                hashMap.remove(Format.CODEC_PROFILE);
                Format format = new Format((Map<String, Object>) hashMap);
                if (!(createFromProfileLevel == null || createFromProfileLevel.isFormatSupported(format))) {
                    return false;
                }
            }
            return true;
        }

        private boolean supportsProfileLevel(int i, Integer num) {
            ProfileLevel[] profileLevelArr = this.profileLevels;
            for (ProfileLevel profileLevel : profileLevelArr) {
                if (profileLevel.profile == i) {
                    if (num == null || this.mime.equalsIgnoreCase(Format.AUDIO_AAC)) {
                        return true;
                    }
                    if ((!this.mime.equalsIgnoreCase(Format.VIDEO_H263) || profileLevel.level == num.intValue() || profileLevel.level != 16 || num.intValue() <= 1) && (!this.mime.equalsIgnoreCase(Format.VIDEO_MPEG4) || profileLevel.level == num.intValue() || profileLevel.level != 4 || num.intValue() <= 1)) {
                        if (this.mime.equalsIgnoreCase(Format.VIDEO_HEVC)) {
                            boolean z = (profileLevel.level & ProfileLevel.HEVC_HIGH_TIER_LEVELS) != 0;
                            if (((44739242 & num.intValue()) != 0) && !z) {
                            }
                        }
                        if (profileLevel.level >= num.intValue()) {
                            if (createFromProfileLevel(this.mime, i, profileLevel.level) == null) {
                                return true;
                            }
                            if (createFromProfileLevel(this.mime, i, num.intValue()) != null) {
                                return true;
                            }
                            return false;
                        }
                    }
                }
            }
            return false;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private String getMime() {
            return this.mime;
        }

        public int getMaxConcurrentInstances() {
            return this.maxConcurrentInstances;
        }

        public AudioAbilities getAudioAbilities() {
            return this.audioAbilities;
        }

        private boolean isEncoder() {
            return this.encoderAbilities != null;
        }

        public EncoderAbilities getEncoderAbilities() {
            return this.encoderAbilities;
        }

        public VideoAbilities getVideoAbilities() {
            return this.videoAbilities;
        }

        public CodecAbilities copy() {
            CodecAbilities codecAbilities = new CodecAbilities();
            ProfileLevel[] profileLevelArr = this.profileLevels;
            codecAbilities.profileLevels = (ProfileLevel[]) Arrays.copyOf(profileLevelArr, profileLevelArr.length);
            int[] iArr = this.mediaColorFormats;
            codecAbilities.mediaColorFormats = Arrays.copyOf(iArr, iArr.length);
            codecAbilities.mime = this.mime;
            codecAbilities.maxConcurrentInstances = this.maxConcurrentInstances;
            codecAbilities.flagsRequired = this.flagsRequired;
            codecAbilities.flagsSupported = this.flagsSupported;
            codecAbilities.audioAbilities = this.audioAbilities;
            codecAbilities.videoAbilities = this.videoAbilities;
            codecAbilities.encoderAbilities = this.encoderAbilities;
            codecAbilities.defaultFormat = this.defaultFormat;
            codecAbilities.capabilitiesInfo = this.capabilitiesInfo;
            return codecAbilities;
        }
    }

    public static final class AudioAbilities {
        private static final int MAX_CAPTURE_CHANNEL_COUNT = 30;
        private Scope<Integer> bitrateRange;
        private int maxCaptureChannelNum;
        private CodecAbilities parent;
        private Scope<Integer>[] sampleRateRanges;
        private int[] sampleRates;

        private AudioAbilities() {
        }

        public static AudioAbilities create(Format format, CodecAbilities codecAbilities) {
            AudioAbilities audioAbilities = new AudioAbilities();
            audioAbilities.init(format, codecAbilities);
            return audioAbilities;
        }

        public Scope<Integer> getSupportedBitrateRange() {
            return this.bitrateRange;
        }

        public int[] getSupportedSampleRates() {
            int[] iArr = this.sampleRates;
            return iArr != null ? Arrays.copyOf(iArr, iArr.length) : new int[0];
        }

        public int getMaxCaptureChannelNum() {
            return this.maxCaptureChannelNum;
        }

        private void init(Format format, CodecAbilities codecAbilities) {
            this.parent = codecAbilities;
            initWithPlatformLimits();
            setLevelProperty();
            parse(format);
        }

        private void initWithPlatformLimits() {
            this.bitrateRange = Scope.create(0, Integer.MAX_VALUE);
            this.maxCaptureChannelNum = 30;
            this.sampleRateRanges = new Scope[]{Scope.create(7350, 1920000)};
            this.sampleRates = null;
        }

        private boolean supports(Integer num, Integer num2) {
            if (num2 == null || (num2.intValue() >= 1 && num2.intValue() <= this.maxCaptureChannelNum)) {
                return num == null || Utils.searchDistinctRanges(this.sampleRateRanges, num) >= 0;
            }
            return false;
        }

        public boolean isAudioSampleRateSupported(int i) {
            return supports(Integer.valueOf(i), null);
        }

        private void setSampleRates(int[] iArr) {
            Arrays.sort(iArr);
            ArrayList arrayList = new ArrayList();
            for (int i : iArr) {
                if (supports(Integer.valueOf(i), null)) {
                    arrayList.add(Scope.create(Integer.valueOf(i), Integer.valueOf(i)));
                }
            }
            this.sampleRateRanges = (Scope[]) arrayList.toArray(new Scope[arrayList.size()]);
            createDiscreteSampleRates();
        }

        private void createDiscreteSampleRates() {
            this.sampleRates = new int[this.sampleRateRanges.length];
            int i = 0;
            while (true) {
                Scope<Integer>[] scopeArr = this.sampleRateRanges;
                if (i < scopeArr.length) {
                    this.sampleRates[i] = scopeArr[i].getLower().intValue();
                    i++;
                } else {
                    return;
                }
            }
        }

        private void setSampleRates(Scope<Integer>[] scopeArr) {
            Utils.sortDistinctRanges(scopeArr);
            this.sampleRateRanges = Utils.intersectSortedRanges(this.sampleRateRanges, scopeArr);
            Scope<Integer>[] scopeArr2 = this.sampleRateRanges;
            for (Scope<Integer> scope : scopeArr2) {
                if (!scope.getLower().equals(scope.getUpper())) {
                    this.sampleRates = null;
                    return;
                }
            }
            createDiscreteSampleRates();
        }

        private void setLevelProperty() {
            Scope<Integer> scope;
            int[] iArr;
            int[] iArr2;
            String mime = this.parent.getMime();
            int i = 2;
            Scope<Integer> scope2 = null;
            if (mime.equalsIgnoreCase(Format.AUDIO_MPEG)) {
                iArr = new int[]{8000, 11025, 12000, 16000, 22050, 24000, 32000, 44100, 48000};
                scope = Scope.create(8000, 320000);
            } else {
                if (mime.equalsIgnoreCase(Format.AUDIO_AMR_NB)) {
                    iArr = new int[]{8000};
                    scope = Scope.create(4750, 12200);
                } else if (mime.equalsIgnoreCase(Format.AUDIO_AMR_WB)) {
                    iArr = new int[]{16000};
                    scope = Scope.create(6600, 23850);
                } else if (mime.equalsIgnoreCase(Format.AUDIO_AAC)) {
                    iArr = new int[]{7350, 8000, 11025, 12000, 16000, 22050, 24000, 32000, 44100, 48000, 64000, 88200, 96000};
                    scope = Scope.create(8000, 510000);
                    i = 48;
                } else if (mime.equalsIgnoreCase(Format.AUDIO_VORBIS)) {
                    i = 255;
                    scope = Scope.create(32000, 500000);
                    iArr = null;
                    scope2 = Scope.create(8000, Integer.valueOf((int) AudioStreamInfo.SAMPLE_RATE_HZ_MAX));
                } else {
                    if (mime.equalsIgnoreCase(Format.AUDIO_OPUS)) {
                        i = 255;
                        iArr2 = new int[]{8000, 12000, 16000, 24000, 48000};
                        scope = Scope.create(Integer.valueOf((int) TelephonyUtils.MSG_ADD_OBSERVER), 510000);
                    } else if (mime.equalsIgnoreCase(Format.AUDIO_RAW)) {
                        Scope<Integer> create = Scope.create(1, 96000);
                        scope = Scope.create(1, 10000000);
                        i = 8;
                        iArr2 = null;
                        scope2 = create;
                    } else if (mime.equalsIgnoreCase(Format.AUDIO_FLAC)) {
                        i = 255;
                        scope = null;
                        scope2 = Scope.create(1, 655350);
                        iArr = null;
                    } else {
                        if (mime.equalsIgnoreCase(Format.AUDIO_G711_ALAW) || mime.equalsIgnoreCase(Format.AUDIO_G711_MLAW)) {
                            iArr = new int[]{8000};
                            scope = Scope.create(64000, 64000);
                        } else if (mime.equalsIgnoreCase(Format.MIME_AUDIO_MSGSM)) {
                            iArr = new int[]{8000};
                            scope = Scope.create(13000, 13000);
                        } else {
                            if (mime.equalsIgnoreCase(Format.AUDIO_AC3)) {
                                i = 6;
                            } else if (mime.equalsIgnoreCase(Format.AUDIO_EAC3)) {
                                i = 16;
                            } else {
                                this.parent.error |= 2;
                                iArr = null;
                                scope = null;
                            }
                            iArr = null;
                            scope = null;
                        }
                        i = 30;
                    }
                    iArr = iArr2;
                }
                i = 1;
            }
            if (iArr != null) {
                setSampleRates(iArr);
            } else if (scope2 != null) {
                setSampleRates(new Scope[]{scope2});
            } else {
                CodecDescription.LOGGER.warn("setSampleRates do nothing", new Object[0]);
            }
            applyLimits(i, scope);
        }

        private void applyLimits(int i, Scope<Integer> scope) {
            this.maxCaptureChannelNum = ((Integer) Scope.create(1, Integer.valueOf(this.maxCaptureChannelNum)).clamp(Integer.valueOf(i))).intValue();
            if (scope != null) {
                this.bitrateRange = this.bitrateRange.intersect(scope);
            }
        }

        private void parse(Format format) {
            Scope<Integer> scope = CodecDescription.POSITIVE_INTEGERS;
            int i = 0;
            if (format.hasKey("sample-rate-ranges")) {
                String[] split = format.getStringValue("sample-rate-ranges").split(",");
                Scope<Integer>[] scopeArr = new Scope[split.length];
                for (int i2 = 0; i2 < split.length; i2++) {
                    scopeArr[i2] = Utils.parseIntRange(split[i2], null);
                }
                setSampleRates(scopeArr);
            }
            if (format.hasKey("max-channel-count")) {
                i = Utils.parseInt(format.getStringValue("max-channel-count"), 30);
            } else if ((this.parent.error & 2) == 0) {
                i = 30;
            }
            if (format.hasKey("bitrate-range")) {
                scope = scope.intersect(Utils.parseIntRange(format.getStringValue("bitrate-range"), scope));
            }
            applyLimits(i, scope);
        }

        public void getFormat(Format format) {
            if (this.bitrateRange.getLower().equals(this.bitrateRange.getUpper())) {
                format.putIntValue(Format.BIT_RATE, this.bitrateRange.getLower().intValue());
            }
            if (this.maxCaptureChannelNum == 1) {
                format.putIntValue(Format.CHANNEL, 1);
            }
            int[] iArr = this.sampleRates;
            if (iArr != null && iArr.length == 1) {
                format.putIntValue(Format.SAMPLE_RATE, iArr[0]);
            }
        }

        public boolean isFormatSupported(Format format) {
            HashMap<String, Object> formatMap = format.getFormatMap();
            Object obj = formatMap.get(Format.SAMPLE_RATE);
            Integer num = null;
            Integer num2 = obj instanceof Integer ? (Integer) obj : null;
            Object obj2 = formatMap.get(Format.CHANNEL);
            if (obj2 instanceof Integer) {
                num = (Integer) obj2;
            }
            if (supports(num2, num) && CodecAbilities.supportsBitrate(this.bitrateRange, format)) {
                return true;
            }
            return false;
        }
    }

    public static final class VideoAbilities {
        private boolean allowMbOverride;
        private Scope<RationalNumber> aspectRatioRange;
        private Scope<Integer> bitrateRange;
        private Scope<RationalNumber> blockAspectRatioRange;
        private Scope<Integer> blockCountRange;
        private int blockHeight;
        private int blockWidth;
        private Scope<Long> blocksPerSecondRange;
        private Scope<Integer> frameRateRange;
        private int heightAlignment;
        private Scope<Integer> heightRange;
        private Scope<Integer> horizontalBlockRange;
        private Map<Dimension, Scope<Long>> measuredFrameRates;
        private CodecAbilities parent;
        private int smallerDimensionUpperLimit;
        private Scope<Integer> verticalBlockRange;
        private int widthAlignment;
        private Scope<Integer> widthRange;

        private VideoAbilities() {
        }

        public static VideoAbilities create(Format format, CodecAbilities codecAbilities) {
            VideoAbilities videoAbilities = new VideoAbilities();
            videoAbilities.init(format, codecAbilities);
            return videoAbilities;
        }

        private static Pair<Scope<Integer>, Scope<Integer>> parseDimensionRanges(Object obj) {
            Pair<Dimension, Dimension> parseDimensionRange = Utils.parseDimensionRange(obj);
            if (parseDimensionRange == null) {
                CodecDescription.LOGGER.error("parseDimensionRanges Scope is null", new Object[0]);
                return null;
            }
            try {
                return Pair.create(Scope.create(Integer.valueOf(parseDimensionRange.f.getWidthSize()), Integer.valueOf(parseDimensionRange.s.getWidthSize())), Scope.create(Integer.valueOf(parseDimensionRange.f.getHeightSize()), Integer.valueOf(parseDimensionRange.s.getHeightSize())));
            } catch (IllegalArgumentException unused) {
                return null;
            }
        }

        public static int setVp9Level(Format format) {
            int i;
            long j;
            int i2;
            HashMap<String, Object> formatMap = format.getFormatMap();
            Dimension parseDimension = Utils.parseDimension(formatMap.get("block-size"), new Dimension(8, 8));
            int widthSize = parseDimension.getWidthSize() * parseDimension.getHeightSize();
            Scope<Integer> parseIntRange = Utils.parseIntRange(formatMap.get("block-count-range"), null);
            int i3 = 0;
            if (parseIntRange == null) {
                i = 0;
            } else {
                i = parseIntRange.getUpper().intValue() * widthSize;
            }
            Scope<Long> parseLongRange = Utils.parseLongRange(formatMap.get("blocks-per-second-range"), null);
            if (parseLongRange == null) {
                j = 0;
            } else {
                j = ((long) widthSize) * parseLongRange.getUpper().longValue();
            }
            Pair<Scope<Integer>, Scope<Integer>> parseDimensionRanges = parseDimensionRanges(formatMap.get("size-range"));
            if (parseDimensionRanges == null) {
                i2 = 0;
            } else {
                i2 = Math.max(((Integer) parseDimensionRanges.f.getUpper()).intValue(), ((Integer) parseDimensionRanges.s.getUpper()).intValue());
            }
            Scope<Integer> parseIntRange2 = Utils.parseIntRange(formatMap.get("bitrate-range"), null);
            if (parseIntRange2 != null) {
                i3 = Utils.divUp(parseIntRange2.getUpper().intValue(), 1000);
            }
            return getVp9Level(i, j, i2, i3);
        }

        private static int getVp9Level(int i, long j, int i2, int i3) {
            long[] jArr = {829440, 2764800, 4608000, 9216000, 20736000, 36864000, 83558400, 160432128, 311951360, 588251136, 1176502272, 1176502272, 2353004544L, 4706009088L};
            int[] iArr = {36864, 73728, 122880, 245760, 552960, 983040, 2228224, 2228224, 8912896, 8912896, 8912896, 35651584, 35651584, 35651584};
            int[] iArr2 = {200, 800, SystemAbilityDefinition.SUBSYS_HBS_SYS_ABILITY_ID_BEGIN, SystemAbilityDefinition.SUBSYS_SENSORS_SYS_ABILITY_ID_BEGIN, 7200, 12000, 18000, HttpConstant.TIME_OUT, WorkInfo.MIN_IDLE_WAIT_TIME_MS, 120000, 180000, 180000, 240000, 480000};
            int[] iArr3 = {512, 768, 960, 1344, 2048, 2752, 4160, 4160, 8384, 8384, 8384, 16832, 16832, 16832};
            int[] iArr4 = {1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192};
            for (int i4 = 0; i4 < jArr.length; i4++) {
                if (j <= jArr[i4] && i <= iArr[i4] && i3 <= iArr2[i4] && i2 <= iArr3[i4]) {
                    return iArr4[i4];
                }
            }
            return 8192;
        }

        public Scope<Integer> getSupportedBitrateRange() {
            return this.bitrateRange;
        }

        public Scope<Integer> getSupportedVideoWidths() {
            return this.widthRange;
        }

        public Scope<Integer> getSupportedVideoHighs() {
            return this.heightRange;
        }

        public int getVideoWidthAlignment() {
            return this.widthAlignment;
        }

        public int getVideoHighAlignment() {
            return this.heightAlignment;
        }

        public Scope<Integer> getSupportedFrameRates() {
            return this.frameRateRange;
        }

        public Scope<Double> getFrameRatesByVideoSize(int i, int i2) {
            int divUp;
            if (!supports(Integer.valueOf(i), Integer.valueOf(i2), null) || (divUp = Utils.divUp(i, this.blockWidth) * Utils.divUp(i2, this.blockHeight)) <= 0) {
                return null;
            }
            double d = (double) divUp;
            return Scope.create(Double.valueOf(Math.max(((double) this.blocksPerSecondRange.getLower().longValue()) / d, (double) this.frameRateRange.getLower().intValue())), Double.valueOf(Math.min(((double) this.blocksPerSecondRange.getUpper().longValue()) / d, (double) this.frameRateRange.getUpper().intValue())));
        }

        public boolean isVideoSizeAndRateSupported(int i, int i2, double d) {
            return supports(Integer.valueOf(i), Integer.valueOf(i2), Double.valueOf(d));
        }

        public boolean isVideoSizeSupported(int i, int i2) {
            return supports(Integer.valueOf(i), Integer.valueOf(i2), null);
        }

        private boolean supports(Integer num, Integer num2, Number number) {
            boolean z = num == null || (this.widthRange.contains(num) && num.intValue() % this.widthAlignment == 0);
            if (z && num2 != null) {
                z = this.heightRange.contains(num2) && num2.intValue() % this.heightAlignment == 0;
            }
            if (z && number != null) {
                z = this.frameRateRange.contains(Utils.makeIntRange(number.doubleValue()));
            }
            if (!z || num2 == null || num == null) {
                return z;
            }
            boolean z2 = Math.min(num2.intValue(), num.intValue()) <= this.smallerDimensionUpperLimit;
            int divUp = Utils.divUp(num.intValue(), this.blockWidth);
            int divUp2 = Utils.divUp(num2.intValue(), this.blockHeight);
            int i = divUp * divUp2;
            boolean z3 = z2 && this.blockCountRange.contains(Integer.valueOf(i)) && this.blockAspectRatioRange.contains(new RationalNumber(divUp, divUp2)) && this.aspectRatioRange.contains(new RationalNumber(num.intValue(), num2.intValue()));
            return (!z3 || number == null) ? z3 : this.blocksPerSecondRange.contains(Utils.makeLongRange(((double) i) * number.doubleValue()));
        }

        public boolean isFormatSupported(Format format) {
            HashMap<String, Object> formatMap = format.getFormatMap();
            Object obj = formatMap.get(Format.WIDTH);
            Number number = null;
            Integer num = obj instanceof Integer ? (Integer) obj : null;
            Object obj2 = formatMap.get(Format.HEIGHT);
            Integer num2 = obj2 instanceof Integer ? (Integer) obj2 : null;
            Object obj3 = formatMap.get(Format.FRAME_RATE);
            if (obj3 instanceof Number) {
                number = (Number) obj3;
            }
            if (supports(num, num2, number) && CodecAbilities.supportsBitrate(this.bitrateRange, format)) {
                return true;
            }
            return false;
        }

        private void init(Format format, CodecAbilities codecAbilities) {
            this.parent = codecAbilities;
            initLimits();
            setLevelLimits();
            parse(format);
            updateLimits();
        }

        private void initLimits() {
            this.bitrateRange = CodecDescription.BITRATE_RANGE;
            this.widthRange = CodecDescription.SIZE_RANGE;
            this.heightRange = CodecDescription.SIZE_RANGE;
            this.frameRateRange = CodecDescription.FRAME_RATE_RANGE;
            this.horizontalBlockRange = CodecDescription.SIZE_RANGE;
            this.verticalBlockRange = CodecDescription.SIZE_RANGE;
            this.blockCountRange = CodecDescription.POSITIVE_INTEGERS;
            this.blocksPerSecondRange = CodecDescription.POSITIVE_LONGS;
            this.blockAspectRatioRange = CodecDescription.POSITIVE_RATIONALS;
            this.aspectRatioRange = CodecDescription.POSITIVE_RATIONALS;
            this.widthAlignment = 2;
            this.heightAlignment = 2;
            this.blockWidth = 2;
            this.blockHeight = 2;
            this.smallerDimensionUpperLimit = ((Integer) CodecDescription.SIZE_RANGE.getUpper()).intValue();
        }

        private Map<Dimension, Scope<Long>> getMeasuredFrameRates(Map<String, Object> map) {
            Dimension parseDimension;
            Scope<Long> parseLongRange;
            HashMap hashMap = new HashMap();
            for (String str : map.keySet()) {
                if (str.startsWith("measured-frame-rate-")) {
                    String[] split = str.split("-");
                    if (split.length == 5 && (parseDimension = Utils.parseDimension(split[3], null)) != null && parseDimension.getWidthSize() * parseDimension.getHeightSize() > 0 && (parseLongRange = Utils.parseLongRange(map.get(str), null)) != null && parseLongRange.getLower().longValue() >= 0 && parseLongRange.getUpper().longValue() >= 0) {
                        hashMap.put(parseDimension, parseLongRange);
                    }
                }
            }
            return hashMap;
        }

        private void parse(Format format) {
            F f;
            S s;
            HashMap<String, Object> formatMap = format.getFormatMap();
            this.measuredFrameRates = getMeasuredFrameRates(formatMap);
            Pair<Scope<Integer>, Scope<Integer>> parseDimensionRanges = parseDimensionRanges(formatMap.get("size-range"));
            Scope<Integer> scope = null;
            if (parseDimensionRanges != null) {
                f = parseDimensionRanges.f;
                s = parseDimensionRanges.s;
            } else {
                s = null;
                f = null;
            }
            if (formatMap.containsKey("feature-can-swap-width-height")) {
                if (f != null) {
                    this.smallerDimensionUpperLimit = Math.min(f.getUpper().intValue(), s.getUpper().intValue());
                    f = f.expand(s);
                    s = f;
                } else {
                    this.smallerDimensionUpperLimit = Math.min(this.widthRange.getUpper().intValue(), this.heightRange.getUpper().intValue());
                    Scope<Integer> expand = this.widthRange.expand(this.heightRange);
                    this.heightRange = expand;
                    this.widthRange = expand;
                }
            }
            Scope<RationalNumber> parseRationalRange = Utils.parseRationalRange(formatMap.get("block-aspect-ratio-range"), null);
            Scope<Integer> parseIntRange = Utils.parseIntRange(formatMap.get("bitrate-range"), null);
            if (parseIntRange != null) {
                try {
                    scope = parseIntRange.intersect(CodecDescription.BITRATE_RANGE);
                } catch (IllegalArgumentException unused) {
                }
            } else {
                scope = parseIntRange;
            }
            if ((this.parent.error & 2) != 0 || this.allowMbOverride) {
                if (f != null) {
                    this.widthRange = CodecDescription.SIZE_RANGE.intersect(f);
                }
                if (s != null) {
                    this.heightRange = CodecDescription.SIZE_RANGE.intersect(s);
                }
                if (parseRationalRange != null) {
                    this.aspectRatioRange = CodecDescription.POSITIVE_RATIONALS.intersect(parseRationalRange);
                }
                if (scope != null) {
                    if ((this.parent.error & 2) != 0) {
                        this.bitrateRange = CodecDescription.BITRATE_RANGE.intersect(scope);
                    } else {
                        this.bitrateRange = this.bitrateRange.intersect(scope);
                    }
                }
            } else {
                if (f != null) {
                    this.widthRange = this.widthRange.intersect(f);
                }
                if (s != null) {
                    this.heightRange = this.heightRange.intersect(s);
                }
                if (parseRationalRange != null) {
                    this.aspectRatioRange = this.aspectRatioRange.intersect(parseRationalRange);
                }
                if (scope != null) {
                    this.bitrateRange = this.bitrateRange.intersect(scope);
                }
            }
            parseFrameRatesInfo(format);
            parseBlockAlignmentInfo(format);
            updateLimits();
        }

        private void parseFrameRatesInfo(Format format) {
            Scope<Integer> scope = null;
            Scope<Integer> parseIntRange = Utils.parseIntRange(format.getFormatMap().get("frame-rate-range"), null);
            if (parseIntRange != null) {
                try {
                    scope = parseIntRange.intersect(CodecDescription.FRAME_RATE_RANGE);
                } catch (IllegalArgumentException unused) {
                }
            } else {
                scope = parseIntRange;
            }
            if ((this.parent.error & 2) != 0 || this.allowMbOverride) {
                if (scope != null) {
                    this.frameRateRange = CodecDescription.FRAME_RATE_RANGE.intersect(scope);
                }
            } else if (scope != null) {
                this.frameRateRange = this.frameRateRange.intersect(scope);
            }
        }

        private void parseBlockAlignmentInfo(Format format) {
            HashMap<String, Object> formatMap = format.getFormatMap();
            Dimension parseDimension = Utils.parseDimension(formatMap.get("block-size"), new Dimension(this.blockWidth, this.blockHeight));
            CodecDescription.checkPowerOfTwo(parseDimension.getWidthSize(), "block-size width must be power of two");
            CodecDescription.checkPowerOfTwo(parseDimension.getHeightSize(), "block-size height must be power of two");
            Dimension parseDimension2 = Utils.parseDimension(formatMap.get("alignment"), new Dimension(this.widthAlignment, this.heightAlignment));
            Scope<Integer> parseIntRange = Utils.parseIntRange(formatMap.get("block-count-range"), null);
            CodecDescription.checkPowerOfTwo(parseDimension2.getWidthSize(), "alignment width must be power of two");
            CodecDescription.checkPowerOfTwo(parseDimension2.getHeightSize(), "alignment height must be power of two");
            setMacroBlockLimits(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Long.MAX_VALUE, parseDimension.getWidthSize(), parseDimension.getHeightSize(), parseDimension2.getWidthSize(), parseDimension2.getHeightSize());
            Scope<RationalNumber> parseRationalRange = Utils.parseRationalRange(formatMap.get("pixel-aspect-ratio-range"), null);
            Scope<Long> parseLongRange = Utils.parseLongRange(formatMap.get("blocks-per-second-range"), null);
            if ((this.parent.error & 2) != 0 || this.allowMbOverride) {
                if (parseIntRange != null) {
                    this.blockCountRange = CodecDescription.POSITIVE_INTEGERS.intersect(Utils.factorRange(parseIntRange, ((this.blockWidth * this.blockHeight) / parseDimension.getWidthSize()) / parseDimension.getHeightSize()));
                }
                if (parseLongRange != null) {
                    this.blocksPerSecondRange = CodecDescription.POSITIVE_LONGS.intersect(Utils.factorRange(parseLongRange, (long) (((this.blockWidth * this.blockHeight) / parseDimension.getWidthSize()) / parseDimension.getHeightSize())));
                }
                if (parseRationalRange != null) {
                    this.blockAspectRatioRange = CodecDescription.POSITIVE_RATIONALS.intersect(Utils.scaleRange(parseRationalRange, this.blockHeight / parseDimension.getHeightSize(), this.blockWidth / parseDimension.getWidthSize()));
                    return;
                }
                return;
            }
            if (parseIntRange != null) {
                this.blockCountRange = this.blockCountRange.intersect(Utils.factorRange(parseIntRange, ((this.blockWidth * this.blockHeight) / parseDimension.getWidthSize()) / parseDimension.getHeightSize()));
            }
            if (parseLongRange != null) {
                this.blocksPerSecondRange = this.blocksPerSecondRange.intersect(Utils.factorRange(parseLongRange, (long) (((this.blockWidth * this.blockHeight) / parseDimension.getWidthSize()) / parseDimension.getHeightSize())));
            }
            if (parseRationalRange != null) {
                this.blockAspectRatioRange = this.blockAspectRatioRange.intersect(Utils.scaleRange(parseRationalRange, this.blockHeight / parseDimension.getHeightSize(), this.blockWidth / parseDimension.getWidthSize()));
            }
        }

        private void setBlockLimits(int i, int i2, Scope<Integer> scope, Scope<Long> scope2, Scope<RationalNumber> scope3) {
            CodecDescription.checkPowerOfTwo(i, "blockWidth must be a power of two");
            CodecDescription.checkPowerOfTwo(i2, "blockHeight must be a power of two");
            int max = Math.max(i, this.blockWidth);
            int max2 = Math.max(i2, this.blockHeight);
            int i3 = max * max2;
            int i4 = (i3 / this.blockWidth) / this.blockHeight;
            if (i4 != 1) {
                this.blockCountRange = Utils.factorRange(this.blockCountRange, i4);
                this.blocksPerSecondRange = Utils.factorRange(this.blocksPerSecondRange, (long) i4);
                this.blockAspectRatioRange = Utils.scaleRange(this.blockAspectRatioRange, max2 / this.blockHeight, max / this.blockWidth);
                this.horizontalBlockRange = Utils.factorRange(this.horizontalBlockRange, max / this.blockWidth);
                this.verticalBlockRange = Utils.factorRange(this.verticalBlockRange, max2 / this.blockHeight);
            }
            int i5 = (i3 / i) / i2;
            if (i5 != 1) {
                scope = Utils.factorRange(scope, i5);
                scope2 = Utils.factorRange(scope2, (long) i5);
                scope3 = Utils.scaleRange(scope3, max2 / i2, max / i);
            }
            this.blockCountRange = this.blockCountRange.intersect(scope);
            this.blocksPerSecondRange = this.blocksPerSecondRange.intersect(scope2);
            this.blockAspectRatioRange = this.blockAspectRatioRange.intersect(scope3);
            this.blockWidth = max;
            this.blockHeight = max2;
        }

        private void setAlignment(int i, int i2) {
            CodecDescription.checkPowerOfTwo(i, "widthAlignment must be a power of two");
            CodecDescription.checkPowerOfTwo(i2, "heightAlignment must be a power of two");
            if (i > this.blockWidth || i2 > this.blockHeight) {
                setBlockLimits(Math.max(i, this.blockWidth), Math.max(i2, this.blockHeight), CodecDescription.POSITIVE_INTEGERS, CodecDescription.POSITIVE_LONGS, CodecDescription.POSITIVE_RATIONALS);
            }
            this.widthAlignment = Math.max(i, this.widthAlignment);
            this.heightAlignment = Math.max(i2, this.heightAlignment);
            this.widthRange = Utils.alignRange(this.widthRange, this.widthAlignment);
            this.heightRange = Utils.alignRange(this.heightRange, this.heightAlignment);
        }

        private void updateLimits() {
            this.horizontalBlockRange = this.horizontalBlockRange.intersect(Utils.factorRange(this.widthRange, this.blockWidth));
            this.horizontalBlockRange = this.horizontalBlockRange.intersect(Scope.create(Integer.valueOf(this.blockCountRange.getLower().intValue() / this.verticalBlockRange.getUpper().intValue()), Integer.valueOf(this.blockCountRange.getUpper().intValue() / this.verticalBlockRange.getLower().intValue())));
            this.verticalBlockRange = this.verticalBlockRange.intersect(Utils.factorRange(this.heightRange, this.blockHeight));
            this.verticalBlockRange = this.verticalBlockRange.intersect(Scope.create(Integer.valueOf(this.blockCountRange.getLower().intValue() / this.horizontalBlockRange.getUpper().intValue()), Integer.valueOf(this.blockCountRange.getUpper().intValue() / this.horizontalBlockRange.getLower().intValue())));
            this.blockCountRange = this.blockCountRange.intersect(Scope.create(Integer.valueOf(this.horizontalBlockRange.getLower().intValue() * this.verticalBlockRange.getLower().intValue()), Integer.valueOf(this.horizontalBlockRange.getUpper().intValue() * this.verticalBlockRange.getUpper().intValue())));
            this.blockAspectRatioRange = this.blockAspectRatioRange.intersect(new RationalNumber(this.horizontalBlockRange.getLower().intValue(), this.verticalBlockRange.getUpper().intValue()), new RationalNumber(this.horizontalBlockRange.getUpper().intValue(), this.verticalBlockRange.getLower().intValue()));
            this.widthRange = this.widthRange.intersect(Integer.valueOf(((this.horizontalBlockRange.getLower().intValue() - 1) * this.blockWidth) + this.widthAlignment), Integer.valueOf(this.horizontalBlockRange.getUpper().intValue() * this.blockWidth));
            this.heightRange = this.heightRange.intersect(Integer.valueOf(((this.verticalBlockRange.getLower().intValue() - 1) * this.blockHeight) + this.heightAlignment), Integer.valueOf(this.verticalBlockRange.getUpper().intValue() * this.blockHeight));
            this.aspectRatioRange = this.aspectRatioRange.intersect(new RationalNumber(this.widthRange.getLower().intValue(), this.heightRange.getUpper().intValue()), new RationalNumber(this.widthRange.getUpper().intValue(), this.heightRange.getLower().intValue()));
            this.smallerDimensionUpperLimit = Math.min(this.smallerDimensionUpperLimit, Math.min(this.widthRange.getUpper().intValue(), this.heightRange.getUpper().intValue()));
            this.blocksPerSecondRange = this.blocksPerSecondRange.intersect(Long.valueOf(((long) this.blockCountRange.getLower().intValue()) * ((long) this.frameRateRange.getLower().intValue())), Long.valueOf(((long) this.blockCountRange.getUpper().intValue()) * ((long) this.frameRateRange.getUpper().intValue())));
            this.frameRateRange = this.frameRateRange.intersect(Integer.valueOf((int) (this.blocksPerSecondRange.getLower().longValue() / ((long) this.blockCountRange.getUpper().intValue()))), Integer.valueOf((int) (((double) this.blocksPerSecondRange.getUpper().longValue()) / ((double) this.blockCountRange.getLower().intValue()))));
        }

        private void setMacroBlockLimits(int i, int i2, int i3, long j, int i4, int i5, int i6, int i7) {
            setMacroBlockLimits(1, 1, i, i2, i3, j, i4, i5, i6, i7);
        }

        private void setMacroBlockLimits(int i, int i2, int i3, int i4, int i5, long j, int i6, int i7, int i8, int i9) {
            setAlignment(i8, i9);
            setBlockLimits(i6, i7, Scope.create(1, Integer.valueOf(i5)), Scope.create(1L, Long.valueOf(j)), Scope.create(new RationalNumber(1, i4), new RationalNumber(i3, 1)));
            this.horizontalBlockRange = this.horizontalBlockRange.intersect(Integer.valueOf(Utils.divUp(i, this.blockWidth / i6)), Integer.valueOf(i3 / (this.blockWidth / i6)));
            this.verticalBlockRange = this.verticalBlockRange.intersect(Integer.valueOf(Utils.divUp(i2, this.blockHeight / i7)), Integer.valueOf(i4 / (this.blockHeight / i7)));
        }

        private void setLevelLimits() {
            ProfileLevel[] profileLevelArr = this.parent.profileLevels;
            String mime = this.parent.getMime();
            if (mime.equalsIgnoreCase(Format.VIDEO_AVC)) {
                setAvcLevelLimits(profileLevelArr);
            } else if (mime.equalsIgnoreCase(Format.VIDEO_MPEG2)) {
                setMpeg2LevelLimits(profileLevelArr);
            } else if (mime.equalsIgnoreCase(Format.VIDEO_MPEG4)) {
                setMpeg4LevelLimits(profileLevelArr);
            } else if (mime.equalsIgnoreCase(Format.VIDEO_H263)) {
                setH263LevelLimits(profileLevelArr);
            } else if (mime.equalsIgnoreCase(Format.VIDEO_VP8)) {
                setVp8LevelLimits(profileLevelArr);
            } else if (mime.equalsIgnoreCase(Format.VIDEO_VP9)) {
                setVp9LevelLimits(profileLevelArr);
            } else if (mime.equalsIgnoreCase(Format.VIDEO_HEVC)) {
                setHevcLevelLimits(profileLevelArr);
            } else if (mime.equalsIgnoreCase(Format.MIME_VIDEO_AV1)) {
                setAv1LevelLimits(profileLevelArr);
            } else {
                this.bitrateRange = Scope.create(1, 64000);
                this.parent.error |= 6;
            }
        }

        private void setAv1LevelLimits(ProfileLevel[] profileLevelArr) {
            ProfileLevel[] profileLevelArr2 = profileLevelArr;
            AnonymousClass1 r1 = new HashMap<Integer, Long>() {
                /* class ohos.media.codec.CodecDescription.VideoAbilities.AnonymousClass1 */

                {
                    put(1, 5529600L);
                    put(2, 10454400L);
                    put(4, 10454400L);
                    put(8, 10454400L);
                    put(16, 24969600L);
                    put(32, 39938400L);
                    put(64, 39938400L);
                    put(128, 39938400L);
                    put(256, 77856768L);
                    put(512, 155713536L);
                    put(1024, 155713536L);
                    put(2048, 155713536L);
                    put(4096, 273715200L);
                    put(8192, 547430400L);
                    put(16384, 1094860800L);
                    put(32768, 1176502272L);
                    put(65536, 1176502272L);
                    put(131072, 2189721600L);
                    put(262144, 4379443200L);
                    put(524288, 4706009088L);
                }
            };
            AnonymousClass2 r2 = new HashMap<Integer, Integer>() {
                /* class ohos.media.codec.CodecDescription.VideoAbilities.AnonymousClass2 */

                {
                    put(1, 147456);
                    put(2, 278784);
                    put(4, 278784);
                    put(8, 278784);
                    put(16, 665856);
                    put(32, 1065024);
                    put(64, 1065024);
                    put(128, 1065024);
                    put(256, 2359296);
                    put(512, 2359296);
                    put(1024, 2359296);
                    put(2048, 2359296);
                    put(4096, 8912896);
                    put(8192, 8912896);
                    put(16384, 8912896);
                    put(32768, 8912896);
                    put(65536, 35651584);
                    put(131072, 35651584);
                    put(262144, 35651584);
                    put(524288, 35651584);
                }
            };
            AnonymousClass3 r3 = new HashMap<Integer, Integer>() {
                /* class ohos.media.codec.CodecDescription.VideoAbilities.AnonymousClass3 */

                {
                    put(1, Integer.valueOf((int) SystemAbilityDefinition.SUBSYS_DRIVERS_SYS_ABILITY_ID_BEGIN));
                    put(2, 3000);
                    put(4, 3000);
                    put(8, 3000);
                    put(16, Integer.valueOf((int) TelephonyUtils.MSG_ADD_OBSERVER));
                    put(32, 10000);
                    put(64, 10000);
                    put(128, 10000);
                    put(256, 12000);
                    put(512, 20000);
                    put(1024, 20000);
                    put(2048, 20000);
                    put(4096, Integer.valueOf((int) HttpConstant.TIME_OUT));
                    put(8192, 40000);
                    Integer valueOf = Integer.valueOf((int) WorkInfo.MIN_IDLE_WAIT_TIME_MS);
                    put(16384, valueOf);
                    put(32768, valueOf);
                    put(65536, valueOf);
                    put(131072, Integer.valueOf((int) Tracer.Camera.FIRST_FRAME));
                    put(262144, 160000);
                    put(524288, 160000);
                }
            };
            AnonymousClass4 r4 = new HashMap<Integer, Integer>() {
                /* class ohos.media.codec.CodecDescription.VideoAbilities.AnonymousClass4 */

                {
                    put(1, 2048);
                    put(2, 2816);
                    put(4, 2816);
                    put(8, 2816);
                    put(16, 4352);
                    put(32, 5504);
                    put(64, 5504);
                    put(128, 5504);
                    put(256, 6144);
                    put(512, 6144);
                    put(1024, 6144);
                    put(2048, 6144);
                    put(4096, 8192);
                    put(8192, 8192);
                    put(16384, 8192);
                    put(32768, 8192);
                    put(65536, 16384);
                    put(131072, 16384);
                    put(262144, 16384);
                    put(524288, 16384);
                }
            };
            int length = profileLevelArr2.length;
            int i = 0;
            int i2 = 0;
            long j = 829440;
            int i3 = 36864;
            int i4 = 200000;
            int i5 = 512;
            int i6 = 4;
            while (i < length) {
                ProfileLevel profileLevel = profileLevelArr2[i];
                Long l = (Long) r1.get(Integer.valueOf(profileLevel.level));
                Integer num = (Integer) r2.get(Integer.valueOf(profileLevel.level));
                Integer num2 = (Integer) r3.get(Integer.valueOf(profileLevel.level));
                Integer num3 = (Integer) r4.get(Integer.valueOf(profileLevel.level));
                if (l == null || num == null || num2 == null || num3 == null) {
                    l = 0L;
                    i6 |= 1;
                    num = i2;
                    num2 = num;
                    num3 = num2;
                }
                int i7 = profileLevel.profile;
                if (!(i7 == 1 || i7 == 2 || i7 == 4096 || i7 == 8192)) {
                    i6 |= 1;
                }
                i6 &= -5;
                j = Math.max(l.longValue(), j);
                i3 = Math.max(num.intValue(), i3);
                i4 = Math.max(num2.intValue() * 1000, i4);
                i5 = Math.max(num3.intValue(), i5);
                i++;
                profileLevelArr2 = profileLevelArr;
                r1 = r1;
                r2 = r2;
                r3 = r3;
                i2 = i2;
            }
            int divUp = Utils.divUp(i5, 8);
            setMacroBlockLimits(divUp, divUp, Utils.divUp(i3, 64), Utils.divUp(j, 64), 8, 8, 1, 1);
            this.bitrateRange = Scope.create(1, Integer.valueOf(i4));
            this.parent.error |= i6;
        }

        private void setHevcLevelLimits(ProfileLevel[] profileLevelArr) {
            ProfileLevel[] profileLevelArr2 = profileLevelArr;
            AnonymousClass5 r3 = new HashMap<Integer, Double>() {
                /* class ohos.media.codec.CodecDescription.VideoAbilities.AnonymousClass5 */

                {
                    Double valueOf = Double.valueOf(15.0d);
                    put(1, valueOf);
                    put(2, valueOf);
                    Double valueOf2 = Double.valueOf(30.0d);
                    put(4, valueOf2);
                    put(8, valueOf2);
                    put(16, valueOf2);
                    put(32, valueOf2);
                    put(64, valueOf2);
                    put(128, valueOf2);
                    Double valueOf3 = Double.valueOf(33.75d);
                    put(256, valueOf3);
                    put(512, valueOf3);
                    put(1024, valueOf2);
                    put(2048, valueOf2);
                    Double valueOf4 = Double.valueOf(60.0d);
                    put(4096, valueOf4);
                    put(8192, valueOf4);
                    put(16384, valueOf2);
                    put(32768, valueOf2);
                    put(65536, valueOf4);
                    put(131072, valueOf4);
                    Double valueOf5 = Double.valueOf(120.0d);
                    put(262144, valueOf5);
                    put(524288, valueOf5);
                    put(1048576, valueOf2);
                    put(2097152, valueOf2);
                    put(4194304, valueOf4);
                    put(8388608, valueOf4);
                    put(Integer.valueOf((int) ProfileLevel.HEVC_MAIN_TIER_LEVEL_6_2), valueOf5);
                    put(33554432, valueOf5);
                }
            };
            AnonymousClass6 r4 = new HashMap<Integer, Integer>() {
                /* class ohos.media.codec.CodecDescription.VideoAbilities.AnonymousClass6 */

                {
                    put(1, 36864);
                    put(2, 36864);
                    put(4, 122880);
                    put(8, 122880);
                    put(16, 245760);
                    put(32, 245760);
                    put(64, 552960);
                    put(128, 552960);
                    put(256, 983040);
                    put(512, 983040);
                    put(1024, 2228224);
                    put(2048, 2228224);
                    put(4096, 2228224);
                    put(8192, 2228224);
                    put(16384, 8912896);
                    put(32768, 8912896);
                    put(65536, 8912896);
                    put(131072, 8912896);
                    put(262144, 8912896);
                    put(524288, 8912896);
                    put(1048576, 35651584);
                    put(2097152, 35651584);
                    put(4194304, 35651584);
                    put(8388608, 35651584);
                    put(Integer.valueOf((int) ProfileLevel.HEVC_MAIN_TIER_LEVEL_6_2), 35651584);
                    put(33554432, 35651584);
                }
            };
            AnonymousClass7 r5 = new HashMap<Integer, Integer>() {
                /* class ohos.media.codec.CodecDescription.VideoAbilities.AnonymousClass7 */

                {
                    put(1, 128);
                    put(2, 128);
                    Integer valueOf = Integer.valueOf((int) SystemAbilityDefinition.SUBSYS_DRIVERS_SYS_ABILITY_ID_BEGIN);
                    put(4, valueOf);
                    put(8, valueOf);
                    put(16, 3000);
                    put(32, 3000);
                    Integer valueOf2 = Integer.valueOf((int) TelephonyUtils.MSG_ADD_OBSERVER);
                    put(64, valueOf2);
                    put(128, valueOf2);
                    put(256, 10000);
                    put(512, 10000);
                    put(1024, 12000);
                    put(2048, Integer.valueOf((int) HttpConstant.TIME_OUT));
                    put(4096, 20000);
                    put(8192, 50000);
                    put(16384, 25000);
                    put(32768, Integer.valueOf((int) Tracer.Camera.FIRST_FRAME));
                    put(65536, 40000);
                    put(131072, 160000);
                    Integer valueOf3 = Integer.valueOf((int) WorkInfo.MIN_IDLE_WAIT_TIME_MS);
                    put(262144, valueOf3);
                    put(524288, 240000);
                    put(1048576, valueOf3);
                    put(2097152, 240000);
                    put(4194304, 120000);
                    put(8388608, 480000);
                    put(Integer.valueOf((int) ProfileLevel.HEVC_MAIN_TIER_LEVEL_6_2), 240000);
                    put(33554432, 800000);
                }
            };
            int length = profileLevelArr2.length;
            int i = 576;
            int i2 = 128000;
            int i3 = 4;
            long j = (long) 8640;
            int i4 = 0;
            while (i4 < length) {
                ProfileLevel profileLevel = profileLevelArr2[i4];
                Double d = (Double) r3.get(Integer.valueOf(profileLevel.level));
                Integer num = (Integer) r4.get(Integer.valueOf(profileLevel.level));
                Integer num2 = (Integer) r5.get(Integer.valueOf(profileLevel.level));
                if (((d == null) | (num == null)) || (num2 == null)) {
                    d = Double.valueOf(0.0d);
                    i3 |= 1;
                    num2 = 0;
                    num = null;
                }
                int i5 = profileLevel.profile;
                if (!(i5 == 1 || i5 == 2 || i5 == 4 || i5 == 4096 || i5 == 8192)) {
                    i3 |= 1;
                }
                Integer valueOf = Integer.valueOf(num.intValue() >> 6);
                i3 &= -5;
                j = Math.max((long) ((int) (d.doubleValue() * ((double) valueOf.intValue()))), j);
                i = Math.max(valueOf.intValue(), i);
                i2 = Math.max(num2.intValue() * 1000, i2);
                i4++;
                profileLevelArr2 = profileLevelArr;
                r4 = r4;
                r3 = r3;
            }
            int sqrt = (int) Math.sqrt((double) (i * 8));
            setMacroBlockLimits(sqrt, sqrt, i, j, 8, 8, 1, 1);
            this.bitrateRange = Scope.create(1, Integer.valueOf(i2));
            this.parent.error |= i3;
        }

        private void setVp9LevelLimits(ProfileLevel[] profileLevelArr) {
            ProfileLevel[] profileLevelArr2 = profileLevelArr;
            AnonymousClass8 r1 = new HashMap<Integer, Long>() {
                /* class ohos.media.codec.CodecDescription.VideoAbilities.AnonymousClass8 */

                {
                    put(1, 829440L);
                    put(2, 2764800L);
                    put(4, 4608000L);
                    put(8, 9216000L);
                    put(16, 20736000L);
                    put(32, 36864000L);
                    put(64, 83558400L);
                    put(128, 160432128L);
                    put(256, 311951360L);
                    put(512, 588251136L);
                    put(1024, 1176502272L);
                    put(2048, 1176502272L);
                    put(4096, 2353004544L);
                    put(8192, 4706009088L);
                }
            };
            AnonymousClass9 r2 = new HashMap<Integer, Integer>() {
                /* class ohos.media.codec.CodecDescription.VideoAbilities.AnonymousClass9 */

                {
                    put(1, 36864);
                    put(2, 73728);
                    put(4, 122880);
                    put(8, 245760);
                    put(16, 552960);
                    put(32, 983040);
                    put(64, 2228224);
                    put(128, 2228224);
                    put(256, 8912896);
                    put(512, 8912896);
                    put(1024, 8912896);
                    put(2048, 35651584);
                    put(4096, 35651584);
                    put(8192, 35651584);
                }
            };
            AnonymousClass10 r3 = new HashMap<Integer, Integer>() {
                /* class ohos.media.codec.CodecDescription.VideoAbilities.AnonymousClass10 */

                {
                    put(1, 200);
                    put(2, 800);
                    put(4, Integer.valueOf((int) SystemAbilityDefinition.SUBSYS_HBS_SYS_ABILITY_ID_BEGIN));
                    put(8, Integer.valueOf((int) SystemAbilityDefinition.SUBSYS_SENSORS_SYS_ABILITY_ID_BEGIN));
                    put(16, 7200);
                    put(32, 12000);
                    put(64, 18000);
                    put(128, Integer.valueOf((int) HttpConstant.TIME_OUT));
                    put(256, Integer.valueOf((int) WorkInfo.MIN_IDLE_WAIT_TIME_MS));
                    put(512, 120000);
                    put(1024, 180000);
                    put(2048, 180000);
                    put(4096, 240000);
                    put(8192, 480000);
                }
            };
            AnonymousClass11 r4 = new HashMap<Integer, Integer>() {
                /* class ohos.media.codec.CodecDescription.VideoAbilities.AnonymousClass11 */

                {
                    put(1, 512);
                    put(2, 768);
                    put(4, 960);
                    put(8, 1344);
                    put(16, 2048);
                    put(32, 2752);
                    put(64, 4160);
                    put(128, 4160);
                    put(256, 8384);
                    put(512, 8384);
                    put(1024, 8384);
                    put(2048, 16832);
                    put(4096, 16832);
                    put(8192, 16832);
                }
            };
            int length = profileLevelArr2.length;
            int i = 0;
            int i2 = 0;
            long j = 829440;
            int i3 = 36864;
            int i4 = 200000;
            int i5 = 512;
            int i6 = 4;
            while (i < length) {
                ProfileLevel profileLevel = profileLevelArr2[i];
                Long l = (Long) r1.get(Integer.valueOf(profileLevel.level));
                Integer num = (Integer) r2.get(Integer.valueOf(profileLevel.level));
                Integer num2 = (Integer) r3.get(Integer.valueOf(profileLevel.level));
                Integer num3 = (Integer) r4.get(Integer.valueOf(profileLevel.level));
                if (l == null || num == null || num2 == null || num3 == null) {
                    l = 0L;
                    i6 |= 1;
                    num = i2;
                    num2 = num;
                    num3 = num2;
                }
                int i7 = profileLevel.profile;
                if (!(i7 == 1 || i7 == 2 || i7 == 4 || i7 == 8 || i7 == 4096 || i7 == 8192 || i7 == 16384 || i7 == 32768)) {
                    i6 |= 1;
                }
                i6 &= -5;
                j = Math.max(l.longValue(), j);
                i3 = Math.max(num.intValue(), i3);
                i4 = Math.max(num2.intValue() * 1000, i4);
                i5 = Math.max(num3.intValue(), i5);
                i++;
                profileLevelArr2 = profileLevelArr;
                r3 = r3;
                i2 = i2;
                r4 = r4;
                r1 = r1;
                r2 = r2;
            }
            int divUp = Utils.divUp(i5, 8);
            setMacroBlockLimits(divUp, divUp, Utils.divUp(i3, 64), Utils.divUp(j, 64), 8, 8, 1, 1);
            this.bitrateRange = Scope.create(1, Integer.valueOf(i4));
            this.parent.error |= i6;
        }

        private void setVp8LevelLimits(ProfileLevel[] profileLevelArr) {
            int i = 4;
            for (ProfileLevel profileLevel : profileLevelArr) {
                int i2 = profileLevel.level;
                if (!(i2 == 1 || i2 == 2 || i2 == 4 || i2 == 8)) {
                    i |= 1;
                }
                if (profileLevel.profile != 1) {
                    i |= 1;
                }
                i &= -5;
            }
            setMacroBlockLimits(32767, 32767, Integer.MAX_VALUE, 2147483647L, 16, 16, 1, 1);
            this.bitrateRange = Scope.create(1, 100000000);
            this.parent.error |= i;
        }

        private void setH263LevelLimits(ProfileLevel[] profileLevelArr) {
            int i;
            int i2;
            int i3;
            int i4;
            int i5;
            int i6;
            int i7;
            int i8;
            boolean z;
            int i9;
            int i10;
            int i11;
            int i12;
            int i13;
            ProfileLevel[] profileLevelArr2 = profileLevelArr;
            int i14 = 99;
            long j = 1485;
            int i15 = 64000;
            int i16 = 0;
            int i17 = 11;
            int i18 = 9;
            int i19 = 11;
            int i20 = 9;
            int i21 = 15;
            int i22 = 16;
            int i23 = 4;
            for (int length = profileLevelArr2.length; i16 < length; length = length) {
                ProfileLevel profileLevel = profileLevelArr2[i16];
                int i24 = profileLevel.level;
                if (i24 != 1) {
                    if (i24 == 2) {
                        i8 = 5940;
                        i6 = i17;
                        i = 30;
                        i3 = 22;
                        i2 = 18;
                        z = true;
                        i5 = i18;
                        i4 = i22;
                    } else if (i24 == 4) {
                        i8 = 11880;
                        i6 = i17;
                        i = 30;
                        i3 = 22;
                        i2 = 18;
                        i5 = i18;
                        i4 = i22;
                        i7 = 6;
                        z = true;
                    } else if (i24 == 8) {
                        i8 = 11880;
                        i6 = i17;
                        i = 30;
                        i3 = 22;
                        i2 = 18;
                        z = true;
                        i5 = i18;
                        i4 = i22;
                        i7 = 32;
                    } else if (i24 != 16) {
                        if (i24 == 32) {
                            i8 = 19800;
                            i = 60;
                            i3 = 22;
                            i2 = 18;
                            z = false;
                            i7 = 64;
                        } else if (i24 == 64) {
                            i8 = 40500;
                            i3 = 45;
                            i = 60;
                            i2 = 18;
                            z = false;
                            i7 = 128;
                        } else if (i24 != 128) {
                            i23 |= 1;
                            i6 = i17;
                            i5 = i18;
                            i4 = i22;
                            z = false;
                            i8 = 0;
                            i7 = 0;
                            i3 = 0;
                            i2 = 0;
                            i = 0;
                        } else {
                            i7 = 256;
                            i3 = 45;
                            i2 = 36;
                            i = 60;
                            i8 = 81000;
                            z = false;
                        }
                        i6 = 1;
                        i5 = 1;
                        i4 = 4;
                    } else {
                        z = profileLevel.profile == 1 || profileLevel.profile == 4;
                        if (!z) {
                            i13 = 1;
                            i12 = 1;
                            i11 = 4;
                        } else {
                            i13 = i17;
                            i11 = i22;
                            i12 = i18;
                        }
                        i4 = i11;
                        i3 = 11;
                        i2 = 9;
                        i = 15;
                        i6 = i13;
                        i8 = 1485;
                        i5 = i12;
                    }
                    i7 = 2;
                } else {
                    i8 = 1485;
                    i6 = i17;
                    i5 = i18;
                    i4 = i22;
                    z = true;
                    i7 = 1;
                    i3 = 11;
                    i2 = 9;
                    i = 15;
                }
                int i25 = profileLevel.profile;
                if (i25 != 1 && i25 != 2 && i25 != 4 && i25 != 8) {
                    if (!(i25 == 16 || i25 == 32 || i25 == 64 || i25 == 128 || i25 == 256)) {
                        i23 |= 1;
                    }
                }
                if (z) {
                    i10 = 11;
                    i9 = 9;
                } else {
                    this.allowMbOverride = true;
                    i10 = i6;
                    i9 = i5;
                }
                i23 &= -5;
                j = Math.max((long) i8, j);
                i14 = Math.max(i3 * i2, i14);
                i15 = Math.max(64000 * i7, i15);
                i19 = Math.max(i3, i19);
                i20 = Math.max(i2, i20);
                i21 = Math.max(i, i21);
                i17 = Math.min(i10, i17);
                i18 = Math.min(i9, i18);
                i16++;
                profileLevelArr2 = profileLevelArr;
                i22 = i4;
            }
            if (!this.allowMbOverride) {
                this.blockAspectRatioRange = Scope.create(new RationalNumber(11, 9), new RationalNumber(11, 9));
            }
            setMacroBlockLimits(i17, i18, i19, i20, i14, j, 16, 16, i22, i22);
            this.frameRateRange = Scope.create(1, Integer.valueOf(i21));
            this.bitrateRange = Scope.create(1, Integer.valueOf(i15));
            this.parent.error |= i23;
        }

        /* JADX WARNING: Removed duplicated region for block: B:58:0x012f  */
        /* JADX WARNING: Removed duplicated region for block: B:61:0x0145  */
        /* JADX WARNING: Removed duplicated region for block: B:62:0x0154  */
        private void setMpeg4LevelLimits(ProfileLevel[] profileLevelArr) {
            int i;
            boolean z;
            int i2;
            int i3;
            int i4;
            int i5;
            int i6;
            int i7;
            int i8;
            int i9;
            int i10;
            int i11;
            int i12;
            int i13;
            int i14;
            int i15;
            int i16;
            int i17;
            ProfileLevel[] profileLevelArr2 = profileLevelArr;
            int length = profileLevelArr2.length;
            long j = 1485;
            int i18 = 64000;
            int i19 = 0;
            int i20 = 11;
            int i21 = 9;
            int i22 = 99;
            int i23 = 15;
            int i24 = 4;
            while (true) {
                boolean z2 = true;
                if (i19 < length) {
                    ProfileLevel profileLevel = profileLevelArr2[i19];
                    int i25 = profileLevel.profile;
                    int i26 = 128;
                    if (i25 != 1) {
                        if (i25 != 2) {
                            switch (i25) {
                                case 4:
                                case 8:
                                case 16:
                                case 32:
                                case 64:
                                case 128:
                                case 256:
                                case 512:
                                case 1024:
                                case 2048:
                                case 4096:
                                case 8192:
                                case 16384:
                                    break;
                                case 32768:
                                    int i27 = profileLevel.level;
                                    if (i27 == 1 || i27 == 4) {
                                        i4 = 2970;
                                        i = length;
                                        i3 = 30;
                                        i6 = 11;
                                        i5 = 9;
                                        i2 = 99;
                                        z = false;
                                        break;
                                    } else if (i27 != 8) {
                                        if (i27 == 16) {
                                            i4 = 11880;
                                            i26 = 768;
                                        } else if (i27 != 24) {
                                            if (i27 == 32) {
                                                i17 = 44;
                                                i14 = 36;
                                                i16 = 23760;
                                                i15 = 792;
                                                i13 = 3000;
                                            } else if (i27 == 128) {
                                                i17 = 45;
                                                i14 = 36;
                                                i16 = 48600;
                                                i15 = 1620;
                                                i13 = 8000;
                                            }
                                            i = length;
                                            i6 = i17;
                                            i4 = i16;
                                            i5 = i14;
                                            i26 = i13;
                                            z = false;
                                            i2 = i15;
                                            i3 = 30;
                                            break;
                                        } else {
                                            i4 = 11880;
                                            i26 = SystemAbilityDefinition.SUBSYS_DRIVERS_SYS_ABILITY_ID_BEGIN;
                                        }
                                        i = length;
                                        i2 = 396;
                                        i5 = 18;
                                        i6 = 22;
                                        i3 = 30;
                                        z = false;
                                    } else {
                                        i4 = 5940;
                                        i26 = 384;
                                        i = length;
                                        i2 = 396;
                                        i5 = 18;
                                        i6 = 22;
                                        i3 = 30;
                                        z = false;
                                    }
                                    break;
                                default:
                                    i24 |= 1;
                                    i = length;
                                    i6 = 0;
                                    i5 = 0;
                                    i4 = 0;
                                    i26 = 0;
                                    i3 = 0;
                                    i2 = 0;
                                    z = false;
                                    break;
                            }
                            if (z2) {
                                i24 &= -5;
                            }
                            j = Math.max((long) i4, j);
                            i22 = Math.max(i2, i22);
                            i18 = Math.max(i26 * 1000, i18);
                            if (!z) {
                                int max = Math.max(i6, i20);
                                int max2 = Math.max(i5, i21);
                                i7 = Math.max(i3, i23);
                                i20 = max;
                                i21 = max2;
                            } else {
                                int sqrt = (int) Math.sqrt((double) (i2 * 2));
                                int max3 = Math.max(sqrt, i20);
                                int max4 = Math.max(sqrt, i21);
                                i7 = Math.max(Math.max(i3, 60), i23);
                                i21 = max4;
                                i20 = max3;
                            }
                            i23 = i7;
                            i19++;
                            profileLevelArr2 = profileLevelArr;
                            length = i;
                        }
                        i24 |= 2;
                        i = length;
                        i6 = 0;
                        i5 = 0;
                        i4 = 0;
                        i26 = 0;
                        i3 = 0;
                        i2 = 0;
                        z2 = false;
                        z = false;
                        if (z2) {
                        }
                        j = Math.max((long) i4, j);
                        i22 = Math.max(i2, i22);
                        i18 = Math.max(i26 * 1000, i18);
                        if (!z) {
                        }
                        i23 = i7;
                        i19++;
                        profileLevelArr2 = profileLevelArr;
                        length = i;
                    } else {
                        int i28 = profileLevel.level;
                        if (i28 == 1) {
                            i = length;
                            i26 = 64;
                        } else if (i28 == 2) {
                            i = length;
                        } else if (i28 == 4) {
                            i = length;
                            i26 = 64;
                            i4 = 1485;
                            i3 = 30;
                            i6 = 11;
                            i5 = 9;
                            i2 = 99;
                            z = false;
                            if (z2) {
                            }
                            j = Math.max((long) i4, j);
                            i22 = Math.max(i2, i22);
                            i18 = Math.max(i26 * 1000, i18);
                            if (!z) {
                            }
                            i23 = i7;
                            i19++;
                            profileLevelArr2 = profileLevelArr;
                            length = i;
                        } else if (i28 == 8) {
                            i4 = 5940;
                            i = length;
                            i2 = 396;
                            i5 = 18;
                            i6 = 22;
                            i3 = 30;
                            z = false;
                            if (z2) {
                            }
                            j = Math.max((long) i4, j);
                            i22 = Math.max(i2, i22);
                            i18 = Math.max(i26 * 1000, i18);
                            if (!z) {
                            }
                            i23 = i7;
                            i19++;
                            profileLevelArr2 = profileLevelArr;
                            length = i;
                        } else if (i28 != 16) {
                            if (i28 != 64) {
                                if (i28 == 128) {
                                    i12 = 45;
                                    i10 = 36;
                                    i11 = 40500;
                                    i9 = 1620;
                                    i8 = 8000;
                                } else if (i28 == 256) {
                                    i12 = 80;
                                    i10 = 45;
                                    i11 = 108000;
                                    i9 = SystemAbilityDefinition.SUBSYS_SENSORS_SYS_ABILITY_ID_BEGIN;
                                    i8 = 12000;
                                }
                                i6 = i12;
                                i4 = i11;
                                i26 = i8;
                                i3 = 30;
                                i = length;
                                i5 = i10;
                                i2 = i9;
                            } else {
                                i2 = SystemAbilityDefinition.SUBSYS_DFX_SYS_ABILITY_ID_BEGIN;
                                i = length;
                                i6 = 40;
                                i4 = 36000;
                                i26 = 4000;
                                i5 = 30;
                                i3 = 30;
                            }
                            z = false;
                            if (z2) {
                            }
                            j = Math.max((long) i4, j);
                            i22 = Math.max(i2, i22);
                            i18 = Math.max(i26 * 1000, i18);
                            if (!z) {
                            }
                            i23 = i7;
                            i19++;
                            profileLevelArr2 = profileLevelArr;
                            length = i;
                        } else {
                            i4 = 11880;
                            i26 = 384;
                            i = length;
                            i2 = 396;
                            i5 = 18;
                            i6 = 22;
                            i3 = 30;
                            z = false;
                            if (z2) {
                            }
                            j = Math.max((long) i4, j);
                            i22 = Math.max(i2, i22);
                            i18 = Math.max(i26 * 1000, i18);
                            if (!z) {
                            }
                            i23 = i7;
                            i19++;
                            profileLevelArr2 = profileLevelArr;
                            length = i;
                        }
                        i4 = 1485;
                        i6 = 11;
                        i5 = 9;
                        i3 = 15;
                        i2 = 99;
                        z = true;
                        if (z2) {
                        }
                        j = Math.max((long) i4, j);
                        i22 = Math.max(i2, i22);
                        i18 = Math.max(i26 * 1000, i18);
                        if (!z) {
                        }
                        i23 = i7;
                        i19++;
                        profileLevelArr2 = profileLevelArr;
                        length = i;
                    }
                    i24 |= 1;
                    i = length;
                    i6 = 0;
                    i5 = 0;
                    i4 = 0;
                    i26 = 0;
                    i3 = 0;
                    i2 = 0;
                    z = false;
                    if (z2) {
                    }
                    j = Math.max((long) i4, j);
                    i22 = Math.max(i2, i22);
                    i18 = Math.max(i26 * 1000, i18);
                    if (!z) {
                    }
                    i23 = i7;
                    i19++;
                    profileLevelArr2 = profileLevelArr;
                    length = i;
                } else {
                    setMacroBlockLimits(i20, i21, i22, j, 16, 16, 1, 1);
                    this.frameRateRange = this.frameRateRange.intersect(12, Integer.valueOf(i23));
                    this.bitrateRange = Scope.create(1, Integer.valueOf(i18));
                    this.parent.error |= i24;
                    return;
                }
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:31:0x00b8, code lost:
            if (r15.level != 1) goto L_0x0044;
         */
        /* JADX WARNING: Removed duplicated region for block: B:34:0x00cb  */
        /* JADX WARNING: Removed duplicated region for block: B:40:0x00cd A[SYNTHETIC] */
        private void setMpeg2LevelLimits(ProfileLevel[] profileLevelArr) {
            int i;
            int i2;
            int i3;
            int i4;
            int i5;
            int i6;
            int i7;
            ProfileLevel[] profileLevelArr2 = profileLevelArr;
            int length = profileLevelArr2.length;
            int i8 = 99;
            long j = 1485;
            int i9 = 64000;
            int i10 = 4;
            int i11 = 15;
            int i12 = 9;
            int i13 = 11;
            int i14 = 0;
            while (true) {
                boolean z = true;
                if (i14 < length) {
                    ProfileLevel profileLevel = profileLevelArr2[i14];
                    int i15 = profileLevel.profile;
                    if (i15 != 0) {
                        if (i15 == 1) {
                            int i16 = profileLevel.level;
                            if (i16 != 0) {
                                if (i16 != 1) {
                                    if (i16 != 2) {
                                        if (i16 == 3) {
                                            i4 = 244800;
                                        } else if (i16 == 4) {
                                            i4 = 489600;
                                        }
                                        i5 = 80000;
                                        i2 = 8160;
                                        i3 = 68;
                                        i7 = 60;
                                        i = length;
                                        i6 = 120;
                                    } else {
                                        i5 = 60000;
                                        i3 = 68;
                                        i7 = 60;
                                        i = length;
                                        i6 = 90;
                                        i4 = 183600;
                                        i2 = 6120;
                                    }
                                }
                                i = length;
                                i5 = 15000;
                                i2 = 1620;
                                i4 = 40500;
                                i3 = 36;
                                i6 = 45;
                                i7 = 30;
                            } else {
                                i3 = 18;
                                i2 = 396;
                                i5 = 4000;
                                i7 = 30;
                                i = length;
                                i6 = 22;
                                i4 = 11880;
                            }
                            if (z) {
                            }
                            j = Math.max((long) i4, j);
                            i8 = Math.max(i2, i8);
                            i9 = Math.max(i5 * 1000, i9);
                            i13 = Math.max(i6, i13);
                            i12 = Math.max(i3, i12);
                            i11 = Math.max(i7, i11);
                            i14++;
                            profileLevelArr2 = profileLevelArr;
                            length = i;
                            i10 = i10;
                        } else if (i15 == 2 || i15 == 3 || i15 == 4 || i15 == 5) {
                            i10 |= 2;
                            i = length;
                            i7 = 0;
                            i6 = 0;
                            i5 = 0;
                            i4 = 0;
                            i3 = 0;
                            z = false;
                            i2 = 0;
                            if (z) {
                                i10 &= -5;
                            }
                            j = Math.max((long) i4, j);
                            i8 = Math.max(i2, i8);
                            i9 = Math.max(i5 * 1000, i9);
                            i13 = Math.max(i6, i13);
                            i12 = Math.max(i3, i12);
                            i11 = Math.max(i7, i11);
                            i14++;
                            profileLevelArr2 = profileLevelArr;
                            length = i;
                            i10 = i10;
                        }
                    }
                    i10 |= 1;
                    i = length;
                    i7 = 0;
                    i6 = 0;
                    i5 = 0;
                    i4 = 0;
                    i3 = 0;
                    i2 = 0;
                    if (z) {
                    }
                    j = Math.max((long) i4, j);
                    i8 = Math.max(i2, i8);
                    i9 = Math.max(i5 * 1000, i9);
                    i13 = Math.max(i6, i13);
                    i12 = Math.max(i3, i12);
                    i11 = Math.max(i7, i11);
                    i14++;
                    profileLevelArr2 = profileLevelArr;
                    length = i;
                    i10 = i10;
                } else {
                    setMacroBlockLimits(i13, i12, i8, j, 16, 16, 1, 1);
                    this.frameRateRange = this.frameRateRange.intersect(12, Integer.valueOf(i11));
                    this.bitrateRange = Scope.create(1, Integer.valueOf(i9));
                    this.parent.error |= i10;
                    return;
                }
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:45:0x00f9  */
        /* JADX WARNING: Removed duplicated region for block: B:51:0x00fc A[SYNTHETIC] */
        private void setAvcLevelLimits(ProfileLevel[] profileLevelArr) {
            boolean z;
            Integer num;
            ProfileLevel[] profileLevelArr2 = profileLevelArr;
            AnonymousClass12 r1 = new HashMap<Integer, Integer>() {
                /* class ohos.media.codec.CodecDescription.VideoAbilities.AnonymousClass12 */

                {
                    put(1, 1485);
                    put(2, 1485);
                    put(4, 3000);
                    put(8, Integer.valueOf((int) TelephonyUtils.MSG_ADD_OBSERVER));
                    put(16, 11880);
                    put(32, 11880);
                    put(64, 19800);
                    put(128, 20250);
                    put(256, 40500);
                    put(512, 108000);
                    put(1024, 216000);
                    put(2048, 245760);
                    put(4096, 245760);
                    put(8192, 522240);
                    put(16384, 589824);
                    put(32768, 983040);
                    put(65536, 2073600);
                    put(131072, 4177920);
                    put(262144, 8355840);
                    put(524288, 16711680);
                }
            };
            AnonymousClass13 r2 = new HashMap<Integer, Integer>() {
                /* class ohos.media.codec.CodecDescription.VideoAbilities.AnonymousClass13 */

                {
                    put(1, 99);
                    put(2, 99);
                    put(4, 396);
                    put(8, 396);
                    put(16, 396);
                    put(32, 396);
                    put(64, 792);
                    put(128, 1620);
                    put(256, 1620);
                    put(512, Integer.valueOf((int) SystemAbilityDefinition.SUBSYS_SENSORS_SYS_ABILITY_ID_BEGIN));
                    put(1024, 5120);
                    put(2048, 8192);
                    put(4096, 8192);
                    put(8192, 8704);
                    put(16384, 22080);
                    put(32768, 36864);
                    put(65536, 36864);
                    put(131072, 139264);
                    put(262144, 139264);
                    put(524288, 139264);
                }
            };
            AnonymousClass14 r3 = new HashMap<Integer, Integer>() {
                /* class ohos.media.codec.CodecDescription.VideoAbilities.AnonymousClass14 */

                {
                    put(1, 64);
                    put(2, 128);
                    put(4, 192);
                    put(8, 384);
                    put(16, 768);
                    put(32, 2000);
                    put(64, 4000);
                    put(128, 4000);
                    put(256, 10000);
                    put(512, 14000);
                    put(1024, 20000);
                    put(2048, 20000);
                    put(4096, 50000);
                    put(8192, 50000);
                    put(16384, 135000);
                    put(32768, 240000);
                    put(65536, 240000);
                    put(131072, 240000);
                    put(262144, 480000);
                    put(524288, 800000);
                }
            };
            AnonymousClass15 r4 = new HashMap<Integer, Integer>() {
                /* class ohos.media.codec.CodecDescription.VideoAbilities.AnonymousClass15 */

                {
                    put(1, 396);
                    put(2, 396);
                    put(4, 900);
                    put(8, 2376);
                    put(16, 2376);
                    put(32, 2376);
                    put(64, 4752);
                    put(128, 8100);
                    put(256, 8100);
                    put(512, 18000);
                    put(1024, 20480);
                    put(2048, 32768);
                    put(4096, 32768);
                    put(8192, 34816);
                    put(16384, 110400);
                    put(32768, 184320);
                    put(65536, 184320);
                    put(131072, 696320);
                    put(262144, 696320);
                    put(524288, 696320);
                }
            };
            int length = profileLevelArr2.length;
            int i = 4;
            int i2 = 0;
            int i3 = 396;
            int i4 = 64000;
            long j = 1485;
            int i5 = 99;
            while (i2 < length) {
                ProfileLevel profileLevel = profileLevelArr2[i2];
                Integer num2 = (Integer) r1.get(Integer.valueOf(profileLevel.level));
                Integer num3 = (Integer) r2.get(Integer.valueOf(profileLevel.level));
                Integer num4 = (Integer) r3.get(Integer.valueOf(profileLevel.level));
                Integer num5 = (Integer) r4.get(Integer.valueOf(profileLevel.level));
                if (((num2 == null) | (num3 == null) | (num4 == null)) || (num5 == null)) {
                    i |= 1;
                    num3 = 0;
                    num4 = null;
                    num5 = null;
                    num2 = null;
                }
                int i6 = profileLevel.profile;
                if (!(i6 == 1 || i6 == 2)) {
                    if (i6 != 4) {
                        if (i6 != 8) {
                            if (i6 == 16) {
                                num = Integer.valueOf(num4.intValue() * 3000);
                            } else if (!(i6 == 32 || i6 == 64)) {
                                if (i6 != 65536) {
                                    if (i6 != 524288) {
                                        i |= 1;
                                        num = Integer.valueOf(num4.intValue() * 1000);
                                    }
                                }
                            }
                            z = true;
                            if (!z) {
                                i &= -5;
                            }
                            j = Math.max((long) num2.intValue(), j);
                            i5 = Math.max(num3.intValue(), i5);
                            i4 = Math.max(num.intValue(), i4);
                            i3 = Math.max(i3, num5.intValue());
                            i2++;
                            profileLevelArr2 = profileLevelArr;
                            r3 = r3;
                            r4 = r4;
                            r1 = r1;
                            r2 = r2;
                        }
                        num = Integer.valueOf(num4.intValue() * 1250);
                        z = true;
                        if (!z) {
                        }
                        j = Math.max((long) num2.intValue(), j);
                        i5 = Math.max(num3.intValue(), i5);
                        i4 = Math.max(num.intValue(), i4);
                        i3 = Math.max(i3, num5.intValue());
                        i2++;
                        profileLevelArr2 = profileLevelArr;
                        r3 = r3;
                        r4 = r4;
                        r1 = r1;
                        r2 = r2;
                    }
                    i |= 2;
                    num = Integer.valueOf(num4.intValue() * 1000);
                    z = false;
                    if (!z) {
                    }
                    j = Math.max((long) num2.intValue(), j);
                    i5 = Math.max(num3.intValue(), i5);
                    i4 = Math.max(num.intValue(), i4);
                    i3 = Math.max(i3, num5.intValue());
                    i2++;
                    profileLevelArr2 = profileLevelArr;
                    r3 = r3;
                    r4 = r4;
                    r1 = r1;
                    r2 = r2;
                }
                num = Integer.valueOf(num4.intValue() * 1000);
                z = true;
                if (!z) {
                }
                j = Math.max((long) num2.intValue(), j);
                i5 = Math.max(num3.intValue(), i5);
                i4 = Math.max(num.intValue(), i4);
                i3 = Math.max(i3, num5.intValue());
                i2++;
                profileLevelArr2 = profileLevelArr;
                r3 = r3;
                r4 = r4;
                r1 = r1;
                r2 = r2;
            }
            int sqrt = (int) Math.sqrt((double) (i5 * 8));
            setMacroBlockLimits(sqrt, sqrt, i5, j, 16, 16, 1, 1);
            this.bitrateRange = Scope.create(1, Integer.valueOf(i4));
            this.parent.error |= i;
        }
    }

    public static final class EncoderAbilities {
        public static final int BITRATE_MODE_CBR = 2;
        public static final int BITRATE_MODE_CQ = 0;
        public static final int BITRATE_MODE_VBR = 1;
        private static final Category[] bitrates = {new Category("VBR", 1, true), new Category("CBR", 2, false), new Category("CQ", 0, false)};
        private int bitControl;
        private Scope<Integer> complexityRange;
        private Integer defaultComplexity;
        private Integer defaultQuality;
        private CodecAbilities parent;
        private Scope<Integer> qualityRange;
        private String qualityScale;

        private EncoderAbilities() {
        }

        private static int parseBitrateMode(String str) {
            Category[] categoryArr = bitrates;
            for (Category category : categoryArr) {
                if (category.getName().equalsIgnoreCase(str)) {
                    return category.getValue();
                }
            }
            return 0;
        }

        public static EncoderAbilities create(Format format, CodecAbilities codecAbilities) {
            EncoderAbilities encoderAbilities = new EncoderAbilities();
            encoderAbilities.init(format, codecAbilities);
            return encoderAbilities;
        }

        public Scope<Integer> getEncoderComplexityRange() {
            return this.complexityRange;
        }

        public boolean isEncoderBitrateTypeSupported(int i) {
            for (Category category : bitrates) {
                if (i == category.getValue()) {
                    if ((this.bitControl & (1 << i)) != 0) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }
            return false;
        }

        private void init(Format format, CodecAbilities codecAbilities) {
            this.parent = codecAbilities;
            this.complexityRange = Scope.create(0, 0);
            this.qualityRange = Scope.create(0, 0);
            this.bitControl = 2;
            setLevelLimits();
            parse(format);
        }

        private void setLevelLimits() {
            String mime = this.parent.getMime();
            if (mime.equalsIgnoreCase(Format.AUDIO_FLAC)) {
                this.complexityRange = Scope.create(0, 8);
                this.bitControl = 1;
            } else if (mime.equalsIgnoreCase(Format.AUDIO_AMR_NB) || mime.equalsIgnoreCase(Format.AUDIO_AMR_WB) || mime.equalsIgnoreCase(Format.AUDIO_G711_ALAW) || mime.equalsIgnoreCase(Format.AUDIO_G711_MLAW) || mime.equalsIgnoreCase(Format.MIME_AUDIO_MSGSM)) {
                this.bitControl = 4;
            } else {
                this.bitControl = 0;
            }
        }

        private void parse(Format format) {
            HashMap<String, Object> formatMap = format.getFormatMap();
            if (format.hasKey("complexity-range")) {
                this.complexityRange = Utils.parseIntRange(format.getStringValue("complexity-range"), this.complexityRange);
            }
            if (format.hasKey("quality-range")) {
                this.qualityRange = Utils.parseIntRange(format.getStringValue("quality-range"), this.qualityRange);
            }
            if (format.hasKey("feature-bitrate-modes")) {
                for (String str : format.getStringValue("feature-bitrate-modes").split(",")) {
                    this.bitControl = (1 << parseBitrateMode(str)) | this.bitControl;
                }
            }
            try {
                Object obj = formatMap.get("quality-default");
                if (obj instanceof String) {
                    this.defaultComplexity = Integer.valueOf(Integer.parseInt((String) obj));
                }
            } catch (NumberFormatException unused) {
                CodecDescription.LOGGER.error("get complexity-default failed", new Object[0]);
            }
            try {
                Object obj2 = formatMap.get("quality-default");
                if (obj2 instanceof String) {
                    this.defaultQuality = Integer.valueOf(Integer.parseInt((String) obj2));
                }
            } catch (NumberFormatException unused2) {
                CodecDescription.LOGGER.error("get quality-default failed", new Object[0]);
            }
            Object obj3 = formatMap.get("quality-scale");
            if (obj3 instanceof String) {
                this.qualityScale = (String) obj3;
            }
        }

        private boolean supports(Integer num, Integer num2, Integer num3) {
            boolean contains = num != null ? this.complexityRange.contains((Scope<Integer>) num) : true;
            if (contains && num2 != null) {
                contains = this.qualityRange.contains((Scope<Integer>) num2);
            }
            if (!contains || num3 == null) {
                return contains;
            }
            ProfileLevel[] profileLevelArr = this.parent.profileLevels;
            int length = profileLevelArr.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                } else if (profileLevelArr[i].profile == num3.intValue()) {
                    num3 = null;
                    break;
                } else {
                    i++;
                }
            }
            if (num3 == null) {
                return true;
            }
            return false;
        }

        public void getFormat(Format format) {
            Integer num;
            Integer num2;
            if (!this.qualityRange.getUpper().equals(this.qualityRange.getLower()) && (num2 = this.defaultQuality) != null) {
                format.putIntValue(Format.CODEC_QUALITY, num2.intValue());
            }
            if (!this.complexityRange.getUpper().equals(this.complexityRange.getLower()) && (num = this.defaultComplexity) != null) {
                format.putIntValue(Format.CODEC_COMPLEXITY, num.intValue());
            }
            Category[] categoryArr = bitrates;
            for (Category category : categoryArr) {
                if ((this.bitControl & (1 << category.getValue())) != 0) {
                    format.putIntValue(Format.BITRATE_MODE, category.getValue());
                    return;
                }
            }
        }

        public boolean isFormatSupported(Format format) {
            HashMap<String, Object> formatMap = format.getFormatMap();
            String mime = this.parent.getMime();
            Object obj = formatMap.get(Format.BITRATE_MODE);
            if ((obj instanceof Integer) && !isEncoderBitrateTypeSupported(((Integer) obj).intValue())) {
                return false;
            }
            Integer makeComplexity = makeComplexity(formatMap, mime);
            if (Format.AUDIO_FLAC.equalsIgnoreCase(mime) && makeComplexity != null && makeComplexity.intValue() == -1) {
                return false;
            }
            Object obj2 = formatMap.get(Format.CODEC_PROFILE);
            Integer num = null;
            Integer num2 = obj2 instanceof Integer ? (Integer) obj2 : null;
            if (Format.AUDIO_AAC.equalsIgnoreCase(mime)) {
                Object obj3 = formatMap.get(Format.AAC_PROFILE);
                Integer num3 = obj3 instanceof Integer ? (Integer) obj3 : null;
                if (num2 == null) {
                    num2 = num3;
                }
                if (num3 != null && !num3.equals(num2)) {
                    CodecDescription.LOGGER.error("isFormatSupported failed, profile not equal aac profile", new Object[0]);
                    return false;
                }
            }
            Object obj4 = formatMap.get(Format.CODEC_QUALITY);
            if (obj4 instanceof Integer) {
                num = (Integer) obj4;
            }
            return supports(makeComplexity, num, num2);
        }

        private Integer makeComplexity(Map<String, Object> map, String str) {
            Object obj = map.get(Format.CODEC_COMPLEXITY);
            Integer num = null;
            Integer num2 = obj instanceof Integer ? (Integer) obj : null;
            if (!Format.AUDIO_FLAC.equalsIgnoreCase(str)) {
                return num2;
            }
            Object obj2 = map.get(Format.CODEC_FLAC_COMPRESSION_LEVEL);
            if (obj2 instanceof Integer) {
                num = (Integer) obj2;
            }
            if (num2 == null) {
                num2 = num;
            }
            if (num == null || num2.equals(num)) {
                return num2;
            }
            CodecDescription.LOGGER.error("isFormatSupported failed, complexity not equal flac complexity", new Object[0]);
            return -1;
        }
    }
}
