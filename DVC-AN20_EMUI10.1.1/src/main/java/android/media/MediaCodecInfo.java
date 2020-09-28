package android.media;

import android.annotation.UnsupportedAppUsage;
import android.bluetooth.BluetoothHealth;
import android.content.pm.IHwPluginManager;
import android.hardware.camera2.legacy.LegacyCameraDevice;
import android.media.MediaPlayer;
import android.os.SystemProperties;
import android.rms.iaware.AwareConstant;
import android.telephony.SmsManager;
import android.util.Log;
import android.util.Pair;
import android.util.Range;
import android.util.Rational;
import android.util.Size;
import android.view.SurfaceControl;
import android.view.autofill.AutofillManager;
import com.android.internal.content.NativeLibraryHelper;
import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.util.Protocol;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public final class MediaCodecInfo {
    private static final Range<Integer> BITRATE_RANGE = Range.create(0, 500000000);
    private static final int DEFAULT_MAX_SUPPORTED_INSTANCES = 32;
    private static final int ERROR_NONE_SUPPORTED = 4;
    private static final int ERROR_UNRECOGNIZED = 1;
    private static final int ERROR_UNSUPPORTED = 2;
    private static final int FLAG_IS_ENCODER = 1;
    private static final int FLAG_IS_HARDWARE_ACCELERATED = 8;
    private static final int FLAG_IS_SOFTWARE_ONLY = 4;
    private static final int FLAG_IS_VENDOR = 2;
    private static final Range<Integer> FRAME_RATE_RANGE = Range.create(0, 960);
    private static final int MAX_SUPPORTED_INSTANCES_LIMIT = 256;
    private static final Range<Integer> POSITIVE_INTEGERS = Range.create(1, Integer.MAX_VALUE);
    private static final Range<Long> POSITIVE_LONGS = Range.create(1L, Long.MAX_VALUE);
    private static final Range<Rational> POSITIVE_RATIONALS = Range.create(new Rational(1, Integer.MAX_VALUE), new Rational(Integer.MAX_VALUE, 1));
    private static final Range<Integer> SIZE_RANGE = Range.create(1, 32768);
    private static final String TAG = "MediaCodecInfo";
    private String mCanonicalName;
    private Map<String, CodecCapabilities> mCaps = new HashMap();
    private int mFlags;
    private String mName;

    MediaCodecInfo(String name, String canonicalName, int flags, CodecCapabilities[] caps) {
        this.mName = name;
        this.mCanonicalName = canonicalName;
        this.mFlags = flags;
        for (CodecCapabilities c : caps) {
            this.mCaps.put(c.getMimeType(), c);
        }
    }

    public final String getName() {
        return this.mName;
    }

    public final String getCanonicalName() {
        return this.mCanonicalName;
    }

    public final boolean isAlias() {
        return !this.mName.equals(this.mCanonicalName);
    }

    public final boolean isEncoder() {
        return (this.mFlags & 1) != 0;
    }

    public final boolean isVendor() {
        return (this.mFlags & 2) != 0;
    }

    public final boolean isSoftwareOnly() {
        return (this.mFlags & 4) != 0;
    }

    public final boolean isHardwareAccelerated() {
        return (this.mFlags & 8) != 0;
    }

    public final String[] getSupportedTypes() {
        Set<String> typeSet = this.mCaps.keySet();
        String[] types = (String[]) typeSet.toArray(new String[typeSet.size()]);
        Arrays.sort(types);
        return types;
    }

    /* access modifiers changed from: private */
    public static int checkPowerOfTwo(int value, String message) {
        if (((value - 1) & value) == 0) {
            return value;
        }
        throw new IllegalArgumentException(message);
    }

    /* access modifiers changed from: private */
    public static class Feature {
        public boolean mDefault;
        public String mName;
        public int mValue;

        public Feature(String name, int value, boolean def) {
            this.mName = name;
            this.mValue = value;
            this.mDefault = def;
        }
    }

    public static final class CodecCapabilities {
        public static final int COLOR_Format12bitRGB444 = 3;
        public static final int COLOR_Format16bitARGB1555 = 5;
        public static final int COLOR_Format16bitARGB4444 = 4;
        public static final int COLOR_Format16bitBGR565 = 7;
        public static final int COLOR_Format16bitRGB565 = 6;
        public static final int COLOR_Format18BitBGR666 = 41;
        public static final int COLOR_Format18bitARGB1665 = 9;
        public static final int COLOR_Format18bitRGB666 = 8;
        public static final int COLOR_Format19bitARGB1666 = 10;
        public static final int COLOR_Format24BitABGR6666 = 43;
        public static final int COLOR_Format24BitARGB6666 = 42;
        public static final int COLOR_Format24bitARGB1887 = 13;
        public static final int COLOR_Format24bitBGR888 = 12;
        public static final int COLOR_Format24bitRGB888 = 11;
        public static final int COLOR_Format25bitARGB1888 = 14;
        public static final int COLOR_Format32bitABGR8888 = 2130747392;
        public static final int COLOR_Format32bitARGB8888 = 16;
        public static final int COLOR_Format32bitBGRA8888 = 15;
        public static final int COLOR_Format8bitRGB332 = 2;
        public static final int COLOR_FormatCbYCrY = 27;
        public static final int COLOR_FormatCrYCbY = 28;
        public static final int COLOR_FormatL16 = 36;
        public static final int COLOR_FormatL2 = 33;
        public static final int COLOR_FormatL24 = 37;
        public static final int COLOR_FormatL32 = 38;
        public static final int COLOR_FormatL4 = 34;
        public static final int COLOR_FormatL8 = 35;
        public static final int COLOR_FormatMonochrome = 1;
        public static final int COLOR_FormatRGBAFlexible = 2134288520;
        public static final int COLOR_FormatRGBFlexible = 2134292616;
        public static final int COLOR_FormatRawBayer10bit = 31;
        public static final int COLOR_FormatRawBayer8bit = 30;
        public static final int COLOR_FormatRawBayer8bitcompressed = 32;
        public static final int COLOR_FormatSurface = 2130708361;
        public static final int COLOR_FormatYCbYCr = 25;
        public static final int COLOR_FormatYCrYCb = 26;
        public static final int COLOR_FormatYUV411PackedPlanar = 18;
        public static final int COLOR_FormatYUV411Planar = 17;
        public static final int COLOR_FormatYUV420Flexible = 2135033992;
        public static final int COLOR_FormatYUV420PackedPlanar = 20;
        public static final int COLOR_FormatYUV420PackedSemiPlanar = 39;
        public static final int COLOR_FormatYUV420Planar = 19;
        public static final int COLOR_FormatYUV420SemiPlanar = 21;
        public static final int COLOR_FormatYUV422Flexible = 2135042184;
        public static final int COLOR_FormatYUV422PackedPlanar = 23;
        public static final int COLOR_FormatYUV422PackedSemiPlanar = 40;
        public static final int COLOR_FormatYUV422Planar = 22;
        public static final int COLOR_FormatYUV422SemiPlanar = 24;
        public static final int COLOR_FormatYUV444Flexible = 2135181448;
        public static final int COLOR_FormatYUV444Interleaved = 29;
        public static final int COLOR_QCOM_FormatYUV420SemiPlanar = 2141391872;
        public static final int COLOR_TI_FormatYUV420PackedSemiPlanar = 2130706688;
        public static final String FEATURE_AdaptivePlayback = "adaptive-playback";
        public static final String FEATURE_DynamicTimestamp = "dynamic-timestamp";
        public static final String FEATURE_FrameParsing = "frame-parsing";
        public static final String FEATURE_IntraRefresh = "intra-refresh";
        public static final String FEATURE_MultipleFrames = "multiple-frames";
        public static final String FEATURE_PartialFrame = "partial-frame";
        public static final String FEATURE_SecurePlayback = "secure-playback";
        public static final String FEATURE_TunneledPlayback = "tunneled-playback";
        private static final String TAG = "CodecCapabilities";
        private static final Feature[] decoderFeatures = {new Feature(FEATURE_AdaptivePlayback, 1, true), new Feature(FEATURE_SecurePlayback, 2, false), new Feature(FEATURE_TunneledPlayback, 4, false), new Feature(FEATURE_PartialFrame, 8, false), new Feature(FEATURE_FrameParsing, 16, false), new Feature(FEATURE_MultipleFrames, 32, false), new Feature(FEATURE_DynamicTimestamp, 64, false)};
        private static final Feature[] encoderFeatures = {new Feature(FEATURE_IntraRefresh, 1, false), new Feature(FEATURE_MultipleFrames, 2, false), new Feature(FEATURE_DynamicTimestamp, 4, false)};
        public int[] colorFormats;
        private AudioCapabilities mAudioCaps;
        private MediaFormat mCapabilitiesInfo;
        private MediaFormat mDefaultFormat;
        private EncoderCapabilities mEncoderCaps;
        int mError;
        private int mFlagsRequired;
        private int mFlagsSupported;
        private int mFlagsVerified;
        private int mMaxSupportedInstances;
        private String mMime;
        private VideoCapabilities mVideoCaps;
        public CodecProfileLevel[] profileLevels;

        public CodecCapabilities() {
        }

        public final boolean isFeatureSupported(String name) {
            return checkFeature(name, this.mFlagsSupported);
        }

        public final boolean isFeatureRequired(String name) {
            return checkFeature(name, this.mFlagsRequired);
        }

        public String[] validFeatures() {
            Feature[] features = getValidFeatures();
            String[] res = new String[features.length];
            for (int i = 0; i < res.length; i++) {
                res[i] = features[i].mName;
            }
            return res;
        }

        private Feature[] getValidFeatures() {
            if (!isEncoder()) {
                return decoderFeatures;
            }
            return encoderFeatures;
        }

        private boolean checkFeature(String name, int flags) {
            Feature[] validFeatures = getValidFeatures();
            for (Feature feat : validFeatures) {
                if (feat.mName.equals(name)) {
                    if ((feat.mValue & flags) != 0) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }
            return false;
        }

        public boolean isRegular() {
            Feature[] validFeatures = getValidFeatures();
            for (Feature feat : validFeatures) {
                if (!feat.mDefault && isFeatureRequired(feat.mName)) {
                    return false;
                }
            }
            return true;
        }

        public final boolean isFormatSupported(MediaFormat format) {
            Map<String, Object> map = format.getMap();
            String mime = (String) map.get(MediaFormat.KEY_MIME);
            if (!(mime == null || this.mMime.equalsIgnoreCase(mime))) {
                return false;
            }
            Feature[] validFeatures = getValidFeatures();
            for (Feature feat : validFeatures) {
                Integer yesNo = (Integer) map.get(MediaFormat.KEY_FEATURE_ + feat.mName);
                if (yesNo != null && ((yesNo.intValue() == 1 && !isFeatureSupported(feat.mName)) || (yesNo.intValue() == 0 && isFeatureRequired(feat.mName)))) {
                    return false;
                }
            }
            Integer profile = (Integer) map.get(MediaFormat.KEY_PROFILE);
            Integer level = (Integer) map.get("level");
            if (profile != null) {
                if (!supportsProfileLevel(profile.intValue(), level)) {
                    return false;
                }
                CodecProfileLevel[] codecProfileLevelArr = this.profileLevels;
                int maxLevel = 0;
                for (CodecProfileLevel pl : codecProfileLevelArr) {
                    if (pl.profile == profile.intValue() && pl.level > maxLevel) {
                        maxLevel = pl.level;
                    }
                }
                CodecCapabilities levelCaps = createFromProfileLevel(this.mMime, profile.intValue(), maxLevel);
                Map<String, Object> mapWithoutProfile = new HashMap<>(map);
                mapWithoutProfile.remove(MediaFormat.KEY_PROFILE);
                MediaFormat formatWithoutProfile = new MediaFormat(mapWithoutProfile);
                if (levelCaps != null && !levelCaps.isFormatSupported(formatWithoutProfile)) {
                    return false;
                }
            }
            AudioCapabilities audioCapabilities = this.mAudioCaps;
            if (!(audioCapabilities == null || audioCapabilities.supportsFormat(format))) {
                return false;
            }
            VideoCapabilities videoCapabilities = this.mVideoCaps;
            if (!(videoCapabilities == null || videoCapabilities.supportsFormat(format))) {
                return false;
            }
            EncoderCapabilities encoderCapabilities = this.mEncoderCaps;
            if (encoderCapabilities == null || encoderCapabilities.supportsFormat(format)) {
                return true;
            }
            return false;
        }

        /* access modifiers changed from: private */
        public static boolean supportsBitrate(Range<Integer> bitrateRange, MediaFormat format) {
            Map<String, Object> map = format.getMap();
            Integer maxBitrate = (Integer) map.get(MediaFormat.KEY_MAX_BIT_RATE);
            Integer bitrate = (Integer) map.get(MediaFormat.KEY_BIT_RATE);
            if (bitrate == null) {
                bitrate = maxBitrate;
            } else if (maxBitrate != null) {
                bitrate = Integer.valueOf(Math.max(bitrate.intValue(), maxBitrate.intValue()));
            }
            if (bitrate == null || bitrate.intValue() <= 0) {
                return true;
            }
            return bitrateRange.contains(bitrate);
        }

        private boolean supportsProfileLevel(int profile, Integer level) {
            CodecProfileLevel[] codecProfileLevelArr = this.profileLevels;
            for (CodecProfileLevel pl : codecProfileLevelArr) {
                if (pl.profile == profile) {
                    if (level == null || this.mMime.equalsIgnoreCase(MediaFormat.MIMETYPE_AUDIO_AAC)) {
                        return true;
                    }
                    if ((!this.mMime.equalsIgnoreCase(MediaFormat.MIMETYPE_VIDEO_H263) || pl.level == level.intValue() || pl.level != 16 || level.intValue() <= 1) && (!this.mMime.equalsIgnoreCase(MediaFormat.MIMETYPE_VIDEO_MPEG4) || pl.level == level.intValue() || pl.level != 4 || level.intValue() <= 1)) {
                        if (this.mMime.equalsIgnoreCase(MediaFormat.MIMETYPE_VIDEO_HEVC)) {
                            boolean supportsHighTier = (pl.level & 44739242) != 0;
                            if (((44739242 & level.intValue()) != 0) && !supportsHighTier) {
                            }
                        }
                        if (pl.level >= level.intValue()) {
                            if (createFromProfileLevel(this.mMime, profile, pl.level) == null) {
                                return true;
                            }
                            if (createFromProfileLevel(this.mMime, profile, level.intValue()) != null) {
                                return true;
                            }
                            return false;
                        }
                    }
                }
            }
            return false;
        }

        public MediaFormat getDefaultFormat() {
            return this.mDefaultFormat;
        }

        public String getMimeType() {
            return this.mMime;
        }

        public int getMaxSupportedInstances() {
            return this.mMaxSupportedInstances;
        }

        private boolean isAudio() {
            return this.mAudioCaps != null;
        }

        public AudioCapabilities getAudioCapabilities() {
            return this.mAudioCaps;
        }

        private boolean isEncoder() {
            return this.mEncoderCaps != null;
        }

        public EncoderCapabilities getEncoderCapabilities() {
            return this.mEncoderCaps;
        }

        private boolean isVideo() {
            return this.mVideoCaps != null;
        }

        public VideoCapabilities getVideoCapabilities() {
            return this.mVideoCaps;
        }

        public CodecCapabilities dup() {
            CodecCapabilities caps = new CodecCapabilities();
            CodecProfileLevel[] codecProfileLevelArr = this.profileLevels;
            caps.profileLevels = (CodecProfileLevel[]) Arrays.copyOf(codecProfileLevelArr, codecProfileLevelArr.length);
            int[] iArr = this.colorFormats;
            caps.colorFormats = Arrays.copyOf(iArr, iArr.length);
            caps.mMime = this.mMime;
            caps.mMaxSupportedInstances = this.mMaxSupportedInstances;
            caps.mFlagsRequired = this.mFlagsRequired;
            caps.mFlagsSupported = this.mFlagsSupported;
            caps.mFlagsVerified = this.mFlagsVerified;
            caps.mAudioCaps = this.mAudioCaps;
            caps.mVideoCaps = this.mVideoCaps;
            caps.mEncoderCaps = this.mEncoderCaps;
            caps.mDefaultFormat = this.mDefaultFormat;
            caps.mCapabilitiesInfo = this.mCapabilitiesInfo;
            return caps;
        }

        public static CodecCapabilities createFromProfileLevel(String mime, int profile, int level) {
            CodecProfileLevel pl = new CodecProfileLevel();
            pl.profile = profile;
            pl.level = level;
            MediaFormat defaultFormat = new MediaFormat();
            defaultFormat.setString(MediaFormat.KEY_MIME, mime);
            CodecCapabilities ret = new CodecCapabilities(new CodecProfileLevel[]{pl}, new int[0], true, defaultFormat, new MediaFormat());
            if (ret.mError != 0) {
                return null;
            }
            return ret;
        }

        CodecCapabilities(CodecProfileLevel[] profLevs, int[] colFmts, boolean encoder, Map<String, Object> defaultFormatMap, Map<String, Object> capabilitiesMap) {
            this(profLevs, colFmts, encoder, new MediaFormat(defaultFormatMap), new MediaFormat(capabilitiesMap));
        }

        CodecCapabilities(CodecProfileLevel[] profLevs, int[] colFmts, boolean encoder, MediaFormat defaultFormat, MediaFormat info) {
            boolean z;
            Map<String, Object> map = info.getMap();
            this.colorFormats = colFmts;
            int i = 0;
            this.mFlagsVerified = 0;
            this.mDefaultFormat = defaultFormat;
            this.mCapabilitiesInfo = info;
            this.mMime = this.mDefaultFormat.getString(MediaFormat.KEY_MIME);
            CodecProfileLevel[] profLevs2 = profLevs;
            boolean z2 = true;
            if (profLevs2.length == 0 && this.mMime.equalsIgnoreCase(MediaFormat.MIMETYPE_VIDEO_VP9)) {
                CodecProfileLevel profLev = new CodecProfileLevel();
                profLev.profile = 1;
                profLev.level = VideoCapabilities.equivalentVP9Level(info);
                profLevs2 = new CodecProfileLevel[]{profLev};
            }
            this.profileLevels = profLevs2;
            if (this.mMime.toLowerCase().startsWith("audio/")) {
                this.mAudioCaps = AudioCapabilities.create(info, this);
                this.mAudioCaps.getDefaultFormat(this.mDefaultFormat);
            } else if (this.mMime.toLowerCase().startsWith("video/") || this.mMime.equalsIgnoreCase(MediaFormat.MIMETYPE_IMAGE_ANDROID_HEIC)) {
                this.mVideoCaps = VideoCapabilities.create(info, this);
            }
            if (encoder) {
                this.mEncoderCaps = EncoderCapabilities.create(info, this);
                this.mEncoderCaps.getDefaultFormat(this.mDefaultFormat);
            }
            this.mMaxSupportedInstances = Utils.parseIntSafely(MediaCodecList.getGlobalSettings().get("max-concurrent-instances"), 32);
            this.mMaxSupportedInstances = ((Integer) Range.create(1, 256).clamp(Integer.valueOf(Utils.parseIntSafely(map.get("max-concurrent-instances"), this.mMaxSupportedInstances)))).intValue();
            Feature[] validFeatures = getValidFeatures();
            int length = validFeatures.length;
            while (i < length) {
                Feature feat = validFeatures[i];
                String key = MediaFormat.KEY_FEATURE_ + feat.mName;
                Integer yesNo = (Integer) map.get(key);
                if (yesNo == null) {
                    z = z2;
                } else {
                    if (yesNo.intValue() > 0) {
                        this.mFlagsRequired = feat.mValue | this.mFlagsRequired;
                    }
                    this.mFlagsSupported |= feat.mValue;
                    z = true;
                    this.mDefaultFormat.setInteger(key, 1);
                }
                i++;
                z2 = z;
            }
        }
    }

    public static final class AudioCapabilities {
        private static final int MAX_INPUT_CHANNEL_COUNT = 30;
        private static final String TAG = "AudioCapabilities";
        private Range<Integer> mBitrateRange;
        private int mMaxInputChannelCount;
        private CodecCapabilities mParent;
        private Range<Integer>[] mSampleRateRanges;
        private int[] mSampleRates;

        public Range<Integer> getBitrateRange() {
            return this.mBitrateRange;
        }

        public int[] getSupportedSampleRates() {
            int[] iArr = this.mSampleRates;
            if (iArr != null) {
                return Arrays.copyOf(iArr, iArr.length);
            }
            return null;
        }

        public Range<Integer>[] getSupportedSampleRateRanges() {
            Range<Integer>[] rangeArr = this.mSampleRateRanges;
            return (Range[]) Arrays.copyOf(rangeArr, rangeArr.length);
        }

        public int getMaxInputChannelCount() {
            return this.mMaxInputChannelCount;
        }

        private AudioCapabilities() {
        }

        public static AudioCapabilities create(MediaFormat info, CodecCapabilities parent) {
            AudioCapabilities caps = new AudioCapabilities();
            caps.init(info, parent);
            return caps;
        }

        private void init(MediaFormat info, CodecCapabilities parent) {
            this.mParent = parent;
            initWithPlatformLimits();
            applyLevelLimits();
            parseFromInfo(info);
        }

        private void initWithPlatformLimits() {
            this.mBitrateRange = Range.create(0, Integer.MAX_VALUE);
            this.mMaxInputChannelCount = 30;
            this.mSampleRateRanges = new Range[]{Range.create(Integer.valueOf(SystemProperties.getInt("ro.mediacodec.min_sample_rate", 7350)), Integer.valueOf(SystemProperties.getInt("ro.mediacodec.max_sample_rate", AudioFormat.SAMPLE_RATE_HZ_MAX)))};
            this.mSampleRates = null;
        }

        private boolean supports(Integer sampleRate, Integer inputChannels) {
            if (inputChannels == null || (inputChannels.intValue() >= 1 && inputChannels.intValue() <= this.mMaxInputChannelCount)) {
                return sampleRate == null || Utils.binarySearchDistinctRanges(this.mSampleRateRanges, sampleRate) >= 0;
            }
            return false;
        }

        public boolean isSampleRateSupported(int sampleRate) {
            return supports(Integer.valueOf(sampleRate), null);
        }

        private void limitSampleRates(int[] rates) {
            Arrays.sort(rates);
            ArrayList<Range<Integer>> ranges = new ArrayList<>();
            for (int rate : rates) {
                if (supports(Integer.valueOf(rate), null)) {
                    ranges.add(Range.create(Integer.valueOf(rate), Integer.valueOf(rate)));
                }
            }
            this.mSampleRateRanges = (Range[]) ranges.toArray(new Range[ranges.size()]);
            createDiscreteSampleRates();
        }

        private void createDiscreteSampleRates() {
            this.mSampleRates = new int[this.mSampleRateRanges.length];
            int i = 0;
            while (true) {
                Range<Integer>[] rangeArr = this.mSampleRateRanges;
                if (i < rangeArr.length) {
                    this.mSampleRates[i] = rangeArr[i].getLower().intValue();
                    i++;
                } else {
                    return;
                }
            }
        }

        private void limitSampleRates(Range<Integer>[] rateRanges) {
            Utils.sortDistinctRanges(rateRanges);
            this.mSampleRateRanges = Utils.intersectSortedDistinctRanges(this.mSampleRateRanges, rateRanges);
            Range<Integer>[] rangeArr = this.mSampleRateRanges;
            for (Range<Integer> range : rangeArr) {
                if (!range.getLower().equals(range.getUpper())) {
                    this.mSampleRates = null;
                    return;
                }
            }
            createDiscreteSampleRates();
        }

        private void applyLevelLimits() {
            int[] sampleRates = null;
            Range<Integer> sampleRateRange = null;
            Range<Integer> bitRates = null;
            int maxChannels = 30;
            String mime = this.mParent.getMimeType();
            boolean equalsIgnoreCase = mime.equalsIgnoreCase(MediaFormat.MIMETYPE_AUDIO_MPEG);
            Integer valueOf = Integer.valueOf((int) AwareConstant.SYSTEM_MANAGER);
            if (equalsIgnoreCase) {
                sampleRates = new int[]{AwareConstant.SYSTEM_MANAGER, 11025, 12000, 16000, 22050, 24000, 32000, 44100, 48000};
                bitRates = Range.create(valueOf, 320000);
                maxChannels = 2;
            } else if (mime.equalsIgnoreCase(MediaFormat.MIMETYPE_AUDIO_AMR_NB)) {
                sampleRates = new int[]{8000};
                bitRates = Range.create(4750, 12200);
                maxChannels = 1;
            } else if (mime.equalsIgnoreCase(MediaFormat.MIMETYPE_AUDIO_AMR_WB)) {
                sampleRates = new int[]{16000};
                bitRates = Range.create(6600, 23850);
                maxChannels = 1;
            } else if (mime.equalsIgnoreCase(MediaFormat.MIMETYPE_AUDIO_AAC)) {
                sampleRates = new int[]{7350, AwareConstant.SYSTEM_MANAGER, 11025, 12000, 16000, 22050, 24000, 32000, 44100, 48000, 64000, 88200, 96000};
                bitRates = Range.create(valueOf, 510000);
                maxChannels = 48;
            } else if (mime.equalsIgnoreCase(MediaFormat.MIMETYPE_AUDIO_VORBIS)) {
                bitRates = Range.create(32000, 500000);
                sampleRateRange = Range.create(valueOf, Integer.valueOf((int) AudioFormat.SAMPLE_RATE_HZ_MAX));
                maxChannels = 255;
            } else if (mime.equalsIgnoreCase(MediaFormat.MIMETYPE_AUDIO_OPUS)) {
                bitRates = Range.create(Integer.valueOf((int) BluetoothHealth.HEALTH_OPERATION_SUCCESS), 510000);
                sampleRates = new int[]{AwareConstant.SYSTEM_MANAGER, 12000, 16000, 24000, 48000};
                maxChannels = 255;
            } else if (mime.equalsIgnoreCase(MediaFormat.MIMETYPE_AUDIO_RAW)) {
                sampleRateRange = Range.create(1, 96000);
                bitRates = Range.create(1, Integer.valueOf((int) IHwPluginManager.VERSION_APIMAJOR_POS));
                maxChannels = AudioSystem.OUT_CHANNEL_COUNT_MAX;
            } else if (mime.equalsIgnoreCase(MediaFormat.MIMETYPE_AUDIO_FLAC)) {
                sampleRateRange = Range.create(1, 655350);
                maxChannels = 255;
            } else if (mime.equalsIgnoreCase(MediaFormat.MIMETYPE_AUDIO_G711_ALAW) || mime.equalsIgnoreCase(MediaFormat.MIMETYPE_AUDIO_G711_MLAW)) {
                sampleRates = new int[]{8000};
                bitRates = Range.create(64000, 64000);
            } else if (mime.equalsIgnoreCase(MediaFormat.MIMETYPE_AUDIO_MSGSM)) {
                sampleRates = new int[]{8000};
                bitRates = Range.create(13000, 13000);
                maxChannels = 1;
            } else if (mime.equalsIgnoreCase(MediaFormat.MIMETYPE_AUDIO_AC3)) {
                maxChannels = 6;
            } else if (mime.equalsIgnoreCase(MediaFormat.MIMETYPE_AUDIO_EAC3)) {
                maxChannels = 16;
            } else if (mime.equalsIgnoreCase(MediaFormat.MIMETYPE_AUDIO_EAC3_JOC)) {
                sampleRates = new int[]{48000};
                bitRates = Range.create(32000, 6144000);
                maxChannels = 16;
            } else if (mime.equalsIgnoreCase(MediaFormat.MIMETYPE_AUDIO_AC4)) {
                sampleRates = new int[]{44100, 48000, 96000, AudioFormat.SAMPLE_RATE_HZ_MAX};
                bitRates = Range.create(16000, 2688000);
                maxChannels = 24;
            } else {
                Log.w(TAG, "Unsupported mime " + mime);
                CodecCapabilities codecCapabilities = this.mParent;
                codecCapabilities.mError = codecCapabilities.mError | 2;
            }
            if (sampleRates != null) {
                limitSampleRates(sampleRates);
            } else if (sampleRateRange != null) {
                limitSampleRates(new Range[]{sampleRateRange});
            }
            applyLimits(maxChannels, bitRates);
        }

        private void applyLimits(int maxInputChannels, Range<Integer> bitRates) {
            this.mMaxInputChannelCount = ((Integer) Range.create(1, Integer.valueOf(this.mMaxInputChannelCount)).clamp(Integer.valueOf(maxInputChannels))).intValue();
            if (bitRates != null) {
                this.mBitrateRange = this.mBitrateRange.intersect(bitRates);
            }
        }

        private void parseFromInfo(MediaFormat info) {
            int maxInputChannels = 30;
            Range<Integer> bitRates = MediaCodecInfo.POSITIVE_INTEGERS;
            if (info.containsKey("sample-rate-ranges")) {
                String[] rateStrings = info.getString("sample-rate-ranges").split(SmsManager.REGEX_PREFIX_DELIMITER);
                Range<Integer>[] rateRanges = new Range[rateStrings.length];
                for (int i = 0; i < rateStrings.length; i++) {
                    rateRanges[i] = Utils.parseIntRange(rateStrings[i], null);
                }
                limitSampleRates(rateRanges);
            }
            if (info.containsKey("max-channel-count")) {
                maxInputChannels = Utils.parseIntSafely(info.getString("max-channel-count"), 30);
            } else if ((this.mParent.mError & 2) != 0) {
                maxInputChannels = 0;
            }
            if (info.containsKey("bitrate-range")) {
                bitRates = bitRates.intersect(Utils.parseIntRange(info.getString("bitrate-range"), bitRates));
            }
            applyLimits(maxInputChannels, bitRates);
        }

        public void getDefaultFormat(MediaFormat format) {
            if (this.mBitrateRange.getLower().equals(this.mBitrateRange.getUpper())) {
                format.setInteger(MediaFormat.KEY_BIT_RATE, this.mBitrateRange.getLower().intValue());
            }
            if (this.mMaxInputChannelCount == 1) {
                format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);
            }
            int[] iArr = this.mSampleRates;
            if (iArr != null && iArr.length == 1) {
                format.setInteger(MediaFormat.KEY_SAMPLE_RATE, iArr[0]);
            }
        }

        public boolean supportsFormat(MediaFormat format) {
            Map<String, Object> map = format.getMap();
            if (supports((Integer) map.get(MediaFormat.KEY_SAMPLE_RATE), (Integer) map.get(MediaFormat.KEY_CHANNEL_COUNT)) && CodecCapabilities.supportsBitrate(this.mBitrateRange, format)) {
                return true;
            }
            return false;
        }
    }

    public static final class VideoCapabilities {
        private static final String TAG = "VideoCapabilities";
        private boolean mAllowMbOverride;
        private Range<Rational> mAspectRatioRange;
        private Range<Integer> mBitrateRange;
        private Range<Rational> mBlockAspectRatioRange;
        private Range<Integer> mBlockCountRange;
        private int mBlockHeight;
        private int mBlockWidth;
        private Range<Long> mBlocksPerSecondRange;
        private Range<Integer> mFrameRateRange;
        private int mHeightAlignment;
        private Range<Integer> mHeightRange;
        private Range<Integer> mHorizontalBlockRange;
        private Map<Size, Range<Long>> mMeasuredFrameRates;
        private CodecCapabilities mParent;
        private List<PerformancePoint> mPerformancePoints;
        private int mSmallerDimensionUpperLimit;
        private Range<Integer> mVerticalBlockRange;
        private int mWidthAlignment;
        private Range<Integer> mWidthRange;

        public Range<Integer> getBitrateRange() {
            return this.mBitrateRange;
        }

        public Range<Integer> getSupportedWidths() {
            return this.mWidthRange;
        }

        public Range<Integer> getSupportedHeights() {
            return this.mHeightRange;
        }

        public int getWidthAlignment() {
            return this.mWidthAlignment;
        }

        public int getHeightAlignment() {
            return this.mHeightAlignment;
        }

        public int getSmallerDimensionUpperLimit() {
            return this.mSmallerDimensionUpperLimit;
        }

        public Range<Integer> getSupportedFrameRates() {
            return this.mFrameRateRange;
        }

        public Range<Integer> getSupportedWidthsFor(int height) {
            try {
                Range<Integer> range = this.mWidthRange;
                if (!this.mHeightRange.contains(Integer.valueOf(height)) || height % this.mHeightAlignment != 0) {
                    throw new IllegalArgumentException("unsupported height");
                }
                int heightInBlocks = Utils.divUp(height, this.mBlockHeight);
                Range<Integer> range2 = range.intersect(Integer.valueOf(((Math.max(Utils.divUp(this.mBlockCountRange.getLower().intValue(), heightInBlocks), (int) Math.ceil(this.mBlockAspectRatioRange.getLower().doubleValue() * ((double) heightInBlocks))) - 1) * this.mBlockWidth) + this.mWidthAlignment), Integer.valueOf(this.mBlockWidth * Math.min(this.mBlockCountRange.getUpper().intValue() / heightInBlocks, (int) (this.mBlockAspectRatioRange.getUpper().doubleValue() * ((double) heightInBlocks)))));
                if (height > this.mSmallerDimensionUpperLimit) {
                    range2 = range2.intersect(1, Integer.valueOf(this.mSmallerDimensionUpperLimit));
                }
                return range2.intersect(Integer.valueOf((int) Math.ceil(this.mAspectRatioRange.getLower().doubleValue() * ((double) height))), Integer.valueOf((int) (this.mAspectRatioRange.getUpper().doubleValue() * ((double) height))));
            } catch (IllegalArgumentException e) {
                Log.v(TAG, "could not get supported widths for " + height);
                throw new IllegalArgumentException("unsupported height");
            }
        }

        public Range<Integer> getSupportedHeightsFor(int width) {
            try {
                Range<Integer> range = this.mHeightRange;
                if (!this.mWidthRange.contains(Integer.valueOf(width)) || width % this.mWidthAlignment != 0) {
                    throw new IllegalArgumentException("unsupported width");
                }
                int widthInBlocks = Utils.divUp(width, this.mBlockWidth);
                Range<Integer> range2 = range.intersect(Integer.valueOf(((Math.max(Utils.divUp(this.mBlockCountRange.getLower().intValue(), widthInBlocks), (int) Math.ceil(((double) widthInBlocks) / this.mBlockAspectRatioRange.getUpper().doubleValue())) - 1) * this.mBlockHeight) + this.mHeightAlignment), Integer.valueOf(this.mBlockHeight * Math.min(this.mBlockCountRange.getUpper().intValue() / widthInBlocks, (int) (((double) widthInBlocks) / this.mBlockAspectRatioRange.getLower().doubleValue()))));
                if (width > this.mSmallerDimensionUpperLimit) {
                    range2 = range2.intersect(1, Integer.valueOf(this.mSmallerDimensionUpperLimit));
                }
                return range2.intersect(Integer.valueOf((int) Math.ceil(((double) width) / this.mAspectRatioRange.getUpper().doubleValue())), Integer.valueOf((int) (((double) width) / this.mAspectRatioRange.getLower().doubleValue())));
            } catch (IllegalArgumentException e) {
                Log.v(TAG, "could not get supported heights for " + width);
                throw new IllegalArgumentException("unsupported width");
            }
        }

        public Range<Double> getSupportedFrameRatesFor(int width, int height) {
            Range<Integer> range = this.mHeightRange;
            if (supports(Integer.valueOf(width), Integer.valueOf(height), null)) {
                int blockCount = Utils.divUp(width, this.mBlockWidth) * Utils.divUp(height, this.mBlockHeight);
                return Range.create(Double.valueOf(Math.max(((double) this.mBlocksPerSecondRange.getLower().longValue()) / ((double) blockCount), (double) this.mFrameRateRange.getLower().intValue())), Double.valueOf(Math.min(((double) this.mBlocksPerSecondRange.getUpper().longValue()) / ((double) blockCount), (double) this.mFrameRateRange.getUpper().intValue())));
            }
            throw new IllegalArgumentException("unsupported size");
        }

        private int getBlockCount(int width, int height) {
            return Utils.divUp(width, this.mBlockWidth) * Utils.divUp(height, this.mBlockHeight);
        }

        private Size findClosestSize(int width, int height) {
            int targetBlockCount = getBlockCount(width, height);
            Size closestSize = null;
            int minDiff = Integer.MAX_VALUE;
            for (Size size : this.mMeasuredFrameRates.keySet()) {
                int diff = Math.abs(targetBlockCount - getBlockCount(size.getWidth(), size.getHeight()));
                if (diff < minDiff) {
                    minDiff = diff;
                    closestSize = size;
                }
            }
            return closestSize;
        }

        private Range<Double> estimateFrameRatesFor(int width, int height) {
            Size size = findClosestSize(width, height);
            Range<Long> range = this.mMeasuredFrameRates.get(size);
            Double ratio = Double.valueOf(((double) getBlockCount(size.getWidth(), size.getHeight())) / ((double) Math.max(getBlockCount(width, height), 1)));
            return Range.create(Double.valueOf(((double) range.getLower().longValue()) * ratio.doubleValue()), Double.valueOf(((double) range.getUpper().longValue()) * ratio.doubleValue()));
        }

        public Range<Double> getAchievableFrameRatesFor(int width, int height) {
            if (supports(Integer.valueOf(width), Integer.valueOf(height), null)) {
                Map<Size, Range<Long>> map = this.mMeasuredFrameRates;
                if (map != null && map.size() > 0) {
                    return estimateFrameRatesFor(width, height);
                }
                Log.w(TAG, "Codec did not publish any measurement data.");
                return null;
            }
            throw new IllegalArgumentException("unsupported size");
        }

        public static final class PerformancePoint {
            public static final PerformancePoint FHD_100 = new PerformancePoint(LegacyCameraDevice.MAX_DIMEN_FOR_ROUNDING, 1080, 100);
            public static final PerformancePoint FHD_120 = new PerformancePoint(LegacyCameraDevice.MAX_DIMEN_FOR_ROUNDING, 1080, 120);
            public static final PerformancePoint FHD_200 = new PerformancePoint(LegacyCameraDevice.MAX_DIMEN_FOR_ROUNDING, 1080, 200);
            public static final PerformancePoint FHD_24 = new PerformancePoint(LegacyCameraDevice.MAX_DIMEN_FOR_ROUNDING, 1080, 24);
            public static final PerformancePoint FHD_240 = new PerformancePoint(LegacyCameraDevice.MAX_DIMEN_FOR_ROUNDING, 1080, 240);
            public static final PerformancePoint FHD_25 = new PerformancePoint(LegacyCameraDevice.MAX_DIMEN_FOR_ROUNDING, 1080, 25);
            public static final PerformancePoint FHD_30 = new PerformancePoint(LegacyCameraDevice.MAX_DIMEN_FOR_ROUNDING, 1080, 30);
            public static final PerformancePoint FHD_50 = new PerformancePoint(LegacyCameraDevice.MAX_DIMEN_FOR_ROUNDING, 1080, 50);
            public static final PerformancePoint FHD_60 = new PerformancePoint(LegacyCameraDevice.MAX_DIMEN_FOR_ROUNDING, 1080, 60);
            public static final PerformancePoint HD_100 = new PerformancePoint(1280, 720, 100);
            public static final PerformancePoint HD_120 = new PerformancePoint(1280, 720, 120);
            public static final PerformancePoint HD_200 = new PerformancePoint(1280, 720, 200);
            public static final PerformancePoint HD_24 = new PerformancePoint(1280, 720, 24);
            public static final PerformancePoint HD_240 = new PerformancePoint(1280, 720, 240);
            public static final PerformancePoint HD_25 = new PerformancePoint(1280, 720, 25);
            public static final PerformancePoint HD_30 = new PerformancePoint(1280, 720, 30);
            public static final PerformancePoint HD_50 = new PerformancePoint(1280, 720, 50);
            public static final PerformancePoint HD_60 = new PerformancePoint(1280, 720, 60);
            public static final PerformancePoint SD_24 = new PerformancePoint(720, 480, 24);
            public static final PerformancePoint SD_25 = new PerformancePoint(720, 576, 25);
            public static final PerformancePoint SD_30 = new PerformancePoint(720, 480, 30);
            public static final PerformancePoint SD_48 = new PerformancePoint(720, 480, 48);
            public static final PerformancePoint SD_50 = new PerformancePoint(720, 576, 50);
            public static final PerformancePoint SD_60 = new PerformancePoint(720, 480, 60);
            public static final PerformancePoint UHD_100 = new PerformancePoint(3840, 2160, 100);
            public static final PerformancePoint UHD_120 = new PerformancePoint(3840, 2160, 120);
            public static final PerformancePoint UHD_200 = new PerformancePoint(3840, 2160, 200);
            public static final PerformancePoint UHD_24 = new PerformancePoint(3840, 2160, 24);
            public static final PerformancePoint UHD_240 = new PerformancePoint(3840, 2160, 240);
            public static final PerformancePoint UHD_25 = new PerformancePoint(3840, 2160, 25);
            public static final PerformancePoint UHD_30 = new PerformancePoint(3840, 2160, 30);
            public static final PerformancePoint UHD_50 = new PerformancePoint(3840, 2160, 50);
            public static final PerformancePoint UHD_60 = new PerformancePoint(3840, 2160, 60);
            private Size mBlockSize;
            private int mHeight;
            private int mMaxFrameRate;
            private long mMaxMacroBlockRate;
            private int mWidth;

            public int getMaxMacroBlocks() {
                return saturateLongToInt(((long) this.mWidth) * ((long) this.mHeight));
            }

            public int getMaxFrameRate() {
                return this.mMaxFrameRate;
            }

            public long getMaxMacroBlockRate() {
                return this.mMaxMacroBlockRate;
            }

            public String toString() {
                int blockWidth = this.mBlockSize.getWidth() * 16;
                int blockHeight = this.mBlockSize.getHeight() * 16;
                int origRate = (int) Utils.divUp(this.mMaxMacroBlockRate, (long) getMaxMacroBlocks());
                String info = (this.mWidth * 16) + "x" + (this.mHeight * 16) + "@" + origRate;
                if (origRate < this.mMaxFrameRate) {
                    info = info + ", max " + this.mMaxFrameRate + "fps";
                }
                if (blockWidth > 16 || blockHeight > 16) {
                    info = info + ", " + blockWidth + "x" + blockHeight + " blocks";
                }
                return "PerformancePoint(" + info + ")";
            }

            public int hashCode() {
                return this.mMaxFrameRate;
            }

            public PerformancePoint(int width, int height, int frameRate, int maxFrameRate, Size blockSize) {
                MediaCodecInfo.checkPowerOfTwo(blockSize.getWidth(), "block width");
                MediaCodecInfo.checkPowerOfTwo(blockSize.getHeight(), "block height");
                this.mBlockSize = new Size(Utils.divUp(blockSize.getWidth(), 16), Utils.divUp(blockSize.getHeight(), 16));
                this.mWidth = (int) (Utils.divUp(Math.max(1L, (long) width), (long) Math.max(blockSize.getWidth(), 16)) * ((long) this.mBlockSize.getWidth()));
                this.mHeight = (int) (Utils.divUp(Math.max(1L, (long) height), (long) Math.max(blockSize.getHeight(), 16)) * ((long) this.mBlockSize.getHeight()));
                this.mMaxFrameRate = Math.max(1, Math.max(frameRate, maxFrameRate));
                this.mMaxMacroBlockRate = (long) (Math.max(1, frameRate) * getMaxMacroBlocks());
            }

            public PerformancePoint(PerformancePoint pp, Size newBlockSize) {
                this(pp.mWidth * 16, pp.mHeight * 16, (int) Utils.divUp(pp.mMaxMacroBlockRate, (long) pp.getMaxMacroBlocks()), pp.mMaxFrameRate, new Size(Math.max(newBlockSize.getWidth(), pp.mBlockSize.getWidth() * 16), Math.max(newBlockSize.getHeight(), pp.mBlockSize.getHeight() * 16)));
            }

            public PerformancePoint(int width, int height, int frameRate) {
                this(width, height, frameRate, frameRate, new Size(16, 16));
            }

            private int saturateLongToInt(long value) {
                if (value < -2147483648L) {
                    return Integer.MIN_VALUE;
                }
                if (value > 2147483647L) {
                    return Integer.MAX_VALUE;
                }
                return (int) value;
            }

            private int align(int value, int alignment) {
                return Utils.divUp(value, alignment) * alignment;
            }

            private void checkPowerOfTwo2(int value, String description) {
                if (value == 0 || ((value - 1) & value) != 0) {
                    throw new IllegalArgumentException(description + " (" + value + ") must be a power of 2");
                }
            }

            public boolean covers(MediaFormat format) {
                return covers(new PerformancePoint(format.getInteger("width", 0), format.getInteger("height", 0), Math.round((float) Math.ceil(format.getNumber(MediaFormat.KEY_FRAME_RATE, 0).doubleValue()))));
            }

            public boolean covers(PerformancePoint other) {
                Size commonSize = getCommonBlockSize(other);
                PerformancePoint aligned = new PerformancePoint(this, commonSize);
                PerformancePoint otherAligned = new PerformancePoint(other, commonSize);
                return aligned.getMaxMacroBlocks() >= otherAligned.getMaxMacroBlocks() && aligned.mMaxFrameRate >= otherAligned.mMaxFrameRate && aligned.mMaxMacroBlockRate >= otherAligned.mMaxMacroBlockRate;
            }

            private Size getCommonBlockSize(PerformancePoint other) {
                return new Size(Math.max(this.mBlockSize.getWidth(), other.mBlockSize.getWidth()) * 16, Math.max(this.mBlockSize.getHeight(), other.mBlockSize.getHeight()) * 16);
            }

            public boolean equals(Object o) {
                if (!(o instanceof PerformancePoint)) {
                    return false;
                }
                PerformancePoint other = (PerformancePoint) o;
                Size commonSize = getCommonBlockSize(other);
                PerformancePoint aligned = new PerformancePoint(this, commonSize);
                PerformancePoint otherAligned = new PerformancePoint(other, commonSize);
                if (aligned.getMaxMacroBlocks() == otherAligned.getMaxMacroBlocks() && aligned.mMaxFrameRate == otherAligned.mMaxFrameRate && aligned.mMaxMacroBlockRate == otherAligned.mMaxMacroBlockRate) {
                    return true;
                }
                return false;
            }
        }

        public List<PerformancePoint> getSupportedPerformancePoints() {
            return this.mPerformancePoints;
        }

        public boolean areSizeAndRateSupported(int width, int height, double frameRate) {
            return supports(Integer.valueOf(width), Integer.valueOf(height), Double.valueOf(frameRate));
        }

        public boolean isSizeSupported(int width, int height) {
            return supports(Integer.valueOf(width), Integer.valueOf(height), null);
        }

        private boolean supports(Integer width, Integer height, Number rate) {
            boolean ok = true;
            boolean ok2 = true;
            if (!(1 == 0 || width == null)) {
                ok = this.mWidthRange.contains(width) && width.intValue() % this.mWidthAlignment == 0;
            }
            if (ok && height != null) {
                ok = this.mHeightRange.contains(height) && height.intValue() % this.mHeightAlignment == 0;
            }
            if (ok && rate != null) {
                ok = this.mFrameRateRange.contains(Utils.intRangeFor(rate.doubleValue()));
            }
            if (!ok || height == null || width == null) {
                return ok;
            }
            boolean ok3 = Math.min(height.intValue(), width.intValue()) <= this.mSmallerDimensionUpperLimit;
            int widthInBlocks = Utils.divUp(width.intValue(), this.mBlockWidth);
            int heightInBlocks = Utils.divUp(height.intValue(), this.mBlockHeight);
            int blockCount = widthInBlocks * heightInBlocks;
            if (!ok3 || !this.mBlockCountRange.contains(Integer.valueOf(blockCount)) || !this.mBlockAspectRatioRange.contains(new Rational(widthInBlocks, heightInBlocks)) || !this.mAspectRatioRange.contains(new Rational(width.intValue(), height.intValue()))) {
                ok2 = false;
            }
            if (!ok2 || rate == null) {
                return ok2;
            }
            return this.mBlocksPerSecondRange.contains(Utils.longRangeFor(((double) blockCount) * rate.doubleValue()));
        }

        public boolean supportsFormat(MediaFormat format) {
            Map<String, Object> map = format.getMap();
            if (supports((Integer) map.get("width"), (Integer) map.get("height"), (Number) map.get(MediaFormat.KEY_FRAME_RATE)) && CodecCapabilities.supportsBitrate(this.mBitrateRange, format)) {
                return true;
            }
            return false;
        }

        private VideoCapabilities() {
        }

        @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
        public static VideoCapabilities create(MediaFormat info, CodecCapabilities parent) {
            VideoCapabilities caps = new VideoCapabilities();
            caps.init(info, parent);
            return caps;
        }

        private void init(MediaFormat info, CodecCapabilities parent) {
            this.mParent = parent;
            initWithPlatformLimits();
            applyLevelLimits();
            parseFromInfo(info);
            updateLimits();
        }

        public Size getBlockSize() {
            return new Size(this.mBlockWidth, this.mBlockHeight);
        }

        public Range<Integer> getBlockCountRange() {
            return this.mBlockCountRange;
        }

        public Range<Long> getBlocksPerSecondRange() {
            return this.mBlocksPerSecondRange;
        }

        public Range<Rational> getAspectRatioRange(boolean blocks) {
            return blocks ? this.mBlockAspectRatioRange : this.mAspectRatioRange;
        }

        private void initWithPlatformLimits() {
            this.mBitrateRange = MediaCodecInfo.BITRATE_RANGE;
            this.mWidthRange = MediaCodecInfo.SIZE_RANGE;
            this.mHeightRange = MediaCodecInfo.SIZE_RANGE;
            this.mFrameRateRange = MediaCodecInfo.FRAME_RATE_RANGE;
            this.mHorizontalBlockRange = MediaCodecInfo.SIZE_RANGE;
            this.mVerticalBlockRange = MediaCodecInfo.SIZE_RANGE;
            this.mBlockCountRange = MediaCodecInfo.POSITIVE_INTEGERS;
            this.mBlocksPerSecondRange = MediaCodecInfo.POSITIVE_LONGS;
            this.mBlockAspectRatioRange = MediaCodecInfo.POSITIVE_RATIONALS;
            this.mAspectRatioRange = MediaCodecInfo.POSITIVE_RATIONALS;
            this.mWidthAlignment = 2;
            this.mHeightAlignment = 2;
            this.mBlockWidth = 2;
            this.mBlockHeight = 2;
            this.mSmallerDimensionUpperLimit = ((Integer) MediaCodecInfo.SIZE_RANGE.getUpper()).intValue();
        }

        private List<PerformancePoint> getPerformancePoints(Map<String, Object> map) {
            Vector<PerformancePoint> ret = new Vector<>();
            String prefix = "performance-point-";
            Set<String> keys = map.keySet();
            Iterator<String> it = keys.iterator();
            while (it.hasNext()) {
                String key = it.next();
                if (key.startsWith("performance-point-")) {
                    if (key.substring("performance-point-".length()).equals("none") && ret.size() == 0) {
                        return Collections.unmodifiableList(ret);
                    }
                    String[] temp = key.split(NativeLibraryHelper.CLEAR_ABI_OVERRIDE);
                    if (temp.length == 4) {
                        Size size = Utils.parseSize(temp[2], null);
                        if (size != null) {
                            if (size.getWidth() * size.getHeight() > 0) {
                                Range<Long> range = Utils.parseLongRange(map.get(key), null);
                                if (range != null && range.getLower().longValue() >= 0) {
                                    if (range.getUpper().longValue() >= 0) {
                                        PerformancePoint given = new PerformancePoint(size.getWidth(), size.getHeight(), range.getLower().intValue(), range.getUpper().intValue(), new Size(this.mBlockWidth, this.mBlockHeight));
                                        PerformancePoint rotated = new PerformancePoint(size.getHeight(), size.getWidth(), range.getLower().intValue(), range.getUpper().intValue(), new Size(this.mBlockWidth, this.mBlockHeight));
                                        ret.add(given);
                                        if (!given.covers(rotated)) {
                                            ret.add(rotated);
                                        }
                                        it = it;
                                        prefix = prefix;
                                        keys = keys;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (ret.size() == 0) {
                return null;
            }
            ret.sort($$Lambda$MediaCodecInfo$VideoCapabilities$DpgwEngVFZT9EtP3qcxpiA2G0M.INSTANCE);
            return Collections.unmodifiableList(ret);
        }

        static /* synthetic */ int lambda$getPerformancePoints$0(PerformancePoint a, PerformancePoint b) {
            int i = -1;
            if (a.getMaxMacroBlocks() != b.getMaxMacroBlocks()) {
                if (a.getMaxMacroBlocks() >= b.getMaxMacroBlocks()) {
                    i = 1;
                }
            } else if (a.getMaxMacroBlockRate() != b.getMaxMacroBlockRate()) {
                if (a.getMaxMacroBlockRate() >= b.getMaxMacroBlockRate()) {
                    i = 1;
                }
            } else if (a.getMaxFrameRate() == b.getMaxFrameRate()) {
                i = 0;
            } else if (a.getMaxFrameRate() >= b.getMaxFrameRate()) {
                i = 1;
            }
            return -i;
        }

        private Map<Size, Range<Long>> getMeasuredFrameRates(Map<String, Object> map) {
            Size size;
            Range<Long> range;
            Map<Size, Range<Long>> ret = new HashMap<>();
            for (String key : map.keySet()) {
                if (key.startsWith("measured-frame-rate-")) {
                    key.substring("measured-frame-rate-".length());
                    String[] temp = key.split(NativeLibraryHelper.CLEAR_ABI_OVERRIDE);
                    if (temp.length == 5 && (size = Utils.parseSize(temp[3], null)) != null && size.getWidth() * size.getHeight() > 0 && (range = Utils.parseLongRange(map.get(key), null)) != null && range.getLower().longValue() >= 0 && range.getUpper().longValue() >= 0) {
                        ret.put(size, range);
                    }
                }
            }
            return ret;
        }

        private static Pair<Range<Integer>, Range<Integer>> parseWidthHeightRanges(Object o) {
            Pair<Size, Size> range = Utils.parseSizeRange(o);
            if (range == null) {
                return null;
            }
            try {
                return Pair.create(Range.create(Integer.valueOf(range.first.getWidth()), Integer.valueOf(range.second.getWidth())), Range.create(Integer.valueOf(range.first.getHeight()), Integer.valueOf(range.second.getHeight())));
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "could not parse size range '" + o + "'");
                return null;
            }
        }

        public static int equivalentVP9Level(MediaFormat info) {
            Map<String, Object> map = info.getMap();
            Size blockSize = Utils.parseSize(map.get("block-size"), new Size(8, 8));
            int BS = blockSize.getWidth() * blockSize.getHeight();
            Range<Integer> counts = Utils.parseIntRange(map.get("block-count-range"), null);
            int BR = 0;
            int FS = counts == null ? 0 : counts.getUpper().intValue() * BS;
            Range<Long> blockRates = Utils.parseLongRange(map.get("blocks-per-second-range"), null);
            long SR = blockRates == null ? 0 : ((long) BS) * blockRates.getUpper().longValue();
            Pair<Range<Integer>, Range<Integer>> dimensionRanges = parseWidthHeightRanges(map.get("size-range"));
            int D = dimensionRanges == null ? 0 : Math.max(((Integer) dimensionRanges.first.getUpper()).intValue(), ((Integer) dimensionRanges.second.getUpper()).intValue());
            Range<Integer> bitRates = Utils.parseIntRange(map.get("bitrate-range"), null);
            if (bitRates != null) {
                BR = Utils.divUp(bitRates.getUpper().intValue(), 1000);
            }
            if (SR <= 829440 && FS <= 36864 && BR <= 200 && D <= 512) {
                return 1;
            }
            if (SR <= 2764800 && FS <= 73728 && BR <= 800 && D <= 768) {
                return 2;
            }
            if (SR <= 4608000 && FS <= 122880 && BR <= 1800 && D <= 960) {
                return 4;
            }
            if (SR <= 9216000 && FS <= 245760 && BR <= 3600 && D <= 1344) {
                return 8;
            }
            if (SR <= 20736000 && FS <= 552960 && BR <= 7200 && D <= 2048) {
                return 16;
            }
            if (SR <= 36864000 && FS <= 983040 && BR <= 12000 && D <= 2752) {
                return 32;
            }
            if (SR <= 83558400 && FS <= 2228224 && BR <= 18000 && D <= 4160) {
                return 64;
            }
            if (SR <= 160432128 && FS <= 2228224 && BR <= 30000 && D <= 4160) {
                return 128;
            }
            if (SR <= 311951360 && FS <= 8912896 && BR <= 60000 && D <= 8384) {
                return 256;
            }
            if (SR <= 588251136 && FS <= 8912896 && BR <= 120000 && D <= 8384) {
                return 512;
            }
            if (SR <= 1176502272 && FS <= 8912896 && BR <= 180000 && D <= 8384) {
                return 1024;
            }
            if (SR <= 1176502272 && FS <= 35651584 && BR <= 180000 && D <= 16832) {
                return 2048;
            }
            if (SR > 2353004544L || FS > 35651584 || BR > 240000 || D > 16832) {
                return (SR > 4706009088L || FS > 35651584 || BR > 480000 || D <= 16832) ? 8192 : 8192;
            }
            return 4096;
        }

        /* JADX INFO: Multiple debug info for r0v47 android.util.Range<java.lang.Integer>: [D('widths' android.util.Range<java.lang.Integer>), D('heights' android.util.Range<java.lang.Integer>)] */
        /* JADX WARNING: Removed duplicated region for block: B:11:0x00fb A[SYNTHETIC, Splitter:B:11:0x00fb] */
        /* JADX WARNING: Removed duplicated region for block: B:16:0x012c  */
        /* JADX WARNING: Removed duplicated region for block: B:19:0x013b A[SYNTHETIC, Splitter:B:19:0x013b] */
        /* JADX WARNING: Removed duplicated region for block: B:24:0x0169  */
        /* JADX WARNING: Removed duplicated region for block: B:27:0x01d7  */
        /* JADX WARNING: Removed duplicated region for block: B:51:0x027c  */
        /* JADX WARNING: Removed duplicated region for block: B:53:0x0288  */
        /* JADX WARNING: Removed duplicated region for block: B:55:0x0294  */
        /* JADX WARNING: Removed duplicated region for block: B:57:0x02a0  */
        /* JADX WARNING: Removed duplicated region for block: B:59:0x02bf  */
        /* JADX WARNING: Removed duplicated region for block: B:61:0x02df  */
        /* JADX WARNING: Removed duplicated region for block: B:63:0x02fd  */
        /* JADX WARNING: Removed duplicated region for block: B:65:0x0309  */
        /* JADX WARNING: Removed duplicated region for block: B:67:0x0315  */
        private void parseFromInfo(MediaFormat info) {
            Range<Integer> heights;
            Range<Integer> widths;
            Range<Integer> frameRates;
            Range<Integer> heights2;
            Range<Integer> bitRates;
            Range<Integer> bitRates2;
            Range<Integer> frameRates2;
            Range<Integer> heights3;
            Range<Integer> widths2;
            Range<Rational> ratios;
            Range<Rational> blockRatios;
            Map<String, Object> map = info.getMap();
            Size blockSize = new Size(this.mBlockWidth, this.mBlockHeight);
            Size alignment = new Size(this.mWidthAlignment, this.mHeightAlignment);
            Range<Integer> widths3 = null;
            Range<Integer> heights4 = null;
            Size blockSize2 = Utils.parseSize(map.get("block-size"), blockSize);
            Size alignment2 = Utils.parseSize(map.get("alignment"), alignment);
            Range<Integer> counts = Utils.parseIntRange(map.get("block-count-range"), null);
            Range<Long> blockRates = Utils.parseLongRange(map.get("blocks-per-second-range"), null);
            this.mMeasuredFrameRates = getMeasuredFrameRates(map);
            this.mPerformancePoints = getPerformancePoints(map);
            Pair<Range<Integer>, Range<Integer>> sizeRanges = parseWidthHeightRanges(map.get("size-range"));
            if (sizeRanges != null) {
                widths3 = sizeRanges.first;
                heights4 = sizeRanges.second;
            }
            if (map.containsKey("feature-can-swap-width-height")) {
                if (widths3 != null) {
                    this.mSmallerDimensionUpperLimit = Math.min(widths3.getUpper().intValue(), heights4.getUpper().intValue());
                    Range<Integer> heights5 = widths3.extend(heights4);
                    heights = heights5;
                    widths = heights5;
                    Range<Rational> ratios2 = Utils.parseRationalRange(map.get("block-aspect-ratio-range"), null);
                    Range<Rational> blockRatios2 = Utils.parseRationalRange(map.get("pixel-aspect-ratio-range"), null);
                    frameRates = Utils.parseIntRange(map.get("frame-rate-range"), null);
                    if (frameRates == null) {
                        try {
                            frameRates = frameRates.intersect(MediaCodecInfo.FRAME_RATE_RANGE);
                            heights2 = widths;
                        } catch (IllegalArgumentException e) {
                            StringBuilder sb = new StringBuilder();
                            heights2 = widths;
                            sb.append("frame rate range (");
                            sb.append(frameRates);
                            sb.append(") is out of limits: ");
                            sb.append(MediaCodecInfo.FRAME_RATE_RANGE);
                            Log.w(TAG, sb.toString());
                            frameRates = null;
                        }
                    } else {
                        heights2 = widths;
                    }
                    bitRates = Utils.parseIntRange(map.get("bitrate-range"), null);
                    if (bitRates == null) {
                        try {
                            bitRates2 = bitRates.intersect(MediaCodecInfo.BITRATE_RANGE);
                        } catch (IllegalArgumentException e2) {
                            Log.w(TAG, "bitrate range (" + bitRates + ") is out of limits: " + MediaCodecInfo.BITRATE_RANGE);
                            bitRates2 = null;
                        }
                    } else {
                        bitRates2 = bitRates;
                    }
                    MediaCodecInfo.checkPowerOfTwo(blockSize2.getWidth(), "block-size width must be power of two");
                    MediaCodecInfo.checkPowerOfTwo(blockSize2.getHeight(), "block-size height must be power of two");
                    MediaCodecInfo.checkPowerOfTwo(alignment2.getWidth(), "alignment width must be power of two");
                    MediaCodecInfo.checkPowerOfTwo(alignment2.getHeight(), "alignment height must be power of two");
                    applyMacroBlockLimits(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Long.MAX_VALUE, blockSize2.getWidth(), blockSize2.getHeight(), alignment2.getWidth(), alignment2.getHeight());
                    if ((this.mParent.mError & 2) == 0) {
                        heights3 = heights2;
                        widths2 = heights;
                        frameRates2 = frameRates;
                        ratios = ratios2;
                        blockRatios = blockRatios2;
                    } else if (this.mAllowMbOverride) {
                        heights3 = heights2;
                        widths2 = heights;
                        frameRates2 = frameRates;
                        ratios = ratios2;
                        blockRatios = blockRatios2;
                    } else {
                        if (heights != null) {
                            this.mWidthRange = this.mWidthRange.intersect(heights);
                        }
                        if (heights2 != null) {
                            this.mHeightRange = this.mHeightRange.intersect(heights2);
                        }
                        if (counts != null) {
                            this.mBlockCountRange = this.mBlockCountRange.intersect(Utils.factorRange(counts, ((this.mBlockWidth * this.mBlockHeight) / blockSize2.getWidth()) / blockSize2.getHeight()));
                        }
                        if (blockRates != null) {
                            this.mBlocksPerSecondRange = this.mBlocksPerSecondRange.intersect(Utils.factorRange(blockRates, (long) (((this.mBlockWidth * this.mBlockHeight) / blockSize2.getWidth()) / blockSize2.getHeight())));
                        }
                        if (blockRatios2 != null) {
                            this.mBlockAspectRatioRange = this.mBlockAspectRatioRange.intersect(Utils.scaleRange(blockRatios2, this.mBlockHeight / blockSize2.getHeight(), this.mBlockWidth / blockSize2.getWidth()));
                        }
                        if (ratios2 != null) {
                            this.mAspectRatioRange = this.mAspectRatioRange.intersect(ratios2);
                        }
                        if (frameRates != null) {
                            this.mFrameRateRange = this.mFrameRateRange.intersect(frameRates);
                        }
                        if (bitRates2 != null) {
                            this.mBitrateRange = this.mBitrateRange.intersect(bitRates2);
                        }
                        updateLimits();
                    }
                    if (widths2 != null) {
                        this.mWidthRange = MediaCodecInfo.SIZE_RANGE.intersect(widths2);
                    }
                    if (heights3 != null) {
                        this.mHeightRange = MediaCodecInfo.SIZE_RANGE.intersect(heights3);
                    }
                    if (counts != null) {
                        this.mBlockCountRange = MediaCodecInfo.POSITIVE_INTEGERS.intersect(Utils.factorRange(counts, ((this.mBlockWidth * this.mBlockHeight) / blockSize2.getWidth()) / blockSize2.getHeight()));
                    }
                    if (blockRates != null) {
                        this.mBlocksPerSecondRange = MediaCodecInfo.POSITIVE_LONGS.intersect(Utils.factorRange(blockRates, (long) (((this.mBlockWidth * this.mBlockHeight) / blockSize2.getWidth()) / blockSize2.getHeight())));
                    }
                    if (blockRatios != null) {
                        this.mBlockAspectRatioRange = MediaCodecInfo.POSITIVE_RATIONALS.intersect(Utils.scaleRange(blockRatios, this.mBlockHeight / blockSize2.getHeight(), this.mBlockWidth / blockSize2.getWidth()));
                    }
                    if (ratios != null) {
                        this.mAspectRatioRange = MediaCodecInfo.POSITIVE_RATIONALS.intersect(ratios);
                    }
                    if (frameRates2 != null) {
                        this.mFrameRateRange = MediaCodecInfo.FRAME_RATE_RANGE.intersect(frameRates2);
                    }
                    if (bitRates2 != null) {
                        if ((this.mParent.mError & 2) != 0) {
                            this.mBitrateRange = MediaCodecInfo.BITRATE_RANGE.intersect(bitRates2);
                        } else {
                            this.mBitrateRange = this.mBitrateRange.intersect(bitRates2);
                        }
                    }
                    updateLimits();
                }
                Log.w(TAG, "feature can-swap-width-height is best used with size-range");
                this.mSmallerDimensionUpperLimit = Math.min(this.mWidthRange.getUpper().intValue(), this.mHeightRange.getUpper().intValue());
                Range<Integer> extend = this.mWidthRange.extend(this.mHeightRange);
                this.mHeightRange = extend;
                this.mWidthRange = extend;
            }
            heights = widths3;
            widths = heights4;
            Range<Rational> ratios22 = Utils.parseRationalRange(map.get("block-aspect-ratio-range"), null);
            Range<Rational> blockRatios22 = Utils.parseRationalRange(map.get("pixel-aspect-ratio-range"), null);
            frameRates = Utils.parseIntRange(map.get("frame-rate-range"), null);
            if (frameRates == null) {
            }
            bitRates = Utils.parseIntRange(map.get("bitrate-range"), null);
            if (bitRates == null) {
            }
            MediaCodecInfo.checkPowerOfTwo(blockSize2.getWidth(), "block-size width must be power of two");
            MediaCodecInfo.checkPowerOfTwo(blockSize2.getHeight(), "block-size height must be power of two");
            MediaCodecInfo.checkPowerOfTwo(alignment2.getWidth(), "alignment width must be power of two");
            MediaCodecInfo.checkPowerOfTwo(alignment2.getHeight(), "alignment height must be power of two");
            applyMacroBlockLimits(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Long.MAX_VALUE, blockSize2.getWidth(), blockSize2.getHeight(), alignment2.getWidth(), alignment2.getHeight());
            if ((this.mParent.mError & 2) == 0) {
            }
            if (widths2 != null) {
            }
            if (heights3 != null) {
            }
            if (counts != null) {
            }
            if (blockRates != null) {
            }
            if (blockRatios != null) {
            }
            if (ratios != null) {
            }
            if (frameRates2 != null) {
            }
            if (bitRates2 != null) {
            }
            updateLimits();
        }

        private void applyBlockLimits(int blockWidth, int blockHeight, Range<Integer> counts, Range<Long> rates, Range<Rational> ratios) {
            MediaCodecInfo.checkPowerOfTwo(blockWidth, "blockWidth must be a power of two");
            MediaCodecInfo.checkPowerOfTwo(blockHeight, "blockHeight must be a power of two");
            int newBlockWidth = Math.max(blockWidth, this.mBlockWidth);
            int newBlockHeight = Math.max(blockHeight, this.mBlockHeight);
            int factor = ((newBlockWidth * newBlockHeight) / this.mBlockWidth) / this.mBlockHeight;
            if (factor != 1) {
                this.mBlockCountRange = Utils.factorRange(this.mBlockCountRange, factor);
                this.mBlocksPerSecondRange = Utils.factorRange(this.mBlocksPerSecondRange, (long) factor);
                this.mBlockAspectRatioRange = Utils.scaleRange(this.mBlockAspectRatioRange, newBlockHeight / this.mBlockHeight, newBlockWidth / this.mBlockWidth);
                this.mHorizontalBlockRange = Utils.factorRange(this.mHorizontalBlockRange, newBlockWidth / this.mBlockWidth);
                this.mVerticalBlockRange = Utils.factorRange(this.mVerticalBlockRange, newBlockHeight / this.mBlockHeight);
            }
            int factor2 = ((newBlockWidth * newBlockHeight) / blockWidth) / blockHeight;
            if (factor2 != 1) {
                counts = Utils.factorRange(counts, factor2);
                rates = Utils.factorRange(rates, (long) factor2);
                ratios = Utils.scaleRange(ratios, newBlockHeight / blockHeight, newBlockWidth / blockWidth);
            }
            this.mBlockCountRange = this.mBlockCountRange.intersect(counts);
            this.mBlocksPerSecondRange = this.mBlocksPerSecondRange.intersect(rates);
            this.mBlockAspectRatioRange = this.mBlockAspectRatioRange.intersect(ratios);
            this.mBlockWidth = newBlockWidth;
            this.mBlockHeight = newBlockHeight;
        }

        private void applyAlignment(int widthAlignment, int heightAlignment) {
            MediaCodecInfo.checkPowerOfTwo(widthAlignment, "widthAlignment must be a power of two");
            MediaCodecInfo.checkPowerOfTwo(heightAlignment, "heightAlignment must be a power of two");
            if (widthAlignment > this.mBlockWidth || heightAlignment > this.mBlockHeight) {
                applyBlockLimits(Math.max(widthAlignment, this.mBlockWidth), Math.max(heightAlignment, this.mBlockHeight), MediaCodecInfo.POSITIVE_INTEGERS, MediaCodecInfo.POSITIVE_LONGS, MediaCodecInfo.POSITIVE_RATIONALS);
            }
            this.mWidthAlignment = Math.max(widthAlignment, this.mWidthAlignment);
            this.mHeightAlignment = Math.max(heightAlignment, this.mHeightAlignment);
            this.mWidthRange = Utils.alignRange(this.mWidthRange, this.mWidthAlignment);
            this.mHeightRange = Utils.alignRange(this.mHeightRange, this.mHeightAlignment);
        }

        private void updateLimits() {
            this.mHorizontalBlockRange = this.mHorizontalBlockRange.intersect(Utils.factorRange(this.mWidthRange, this.mBlockWidth));
            this.mHorizontalBlockRange = this.mHorizontalBlockRange.intersect(Range.create(Integer.valueOf(this.mBlockCountRange.getLower().intValue() / this.mVerticalBlockRange.getUpper().intValue()), Integer.valueOf(this.mBlockCountRange.getUpper().intValue() / this.mVerticalBlockRange.getLower().intValue())));
            this.mVerticalBlockRange = this.mVerticalBlockRange.intersect(Utils.factorRange(this.mHeightRange, this.mBlockHeight));
            this.mVerticalBlockRange = this.mVerticalBlockRange.intersect(Range.create(Integer.valueOf(this.mBlockCountRange.getLower().intValue() / this.mHorizontalBlockRange.getUpper().intValue()), Integer.valueOf(this.mBlockCountRange.getUpper().intValue() / this.mHorizontalBlockRange.getLower().intValue())));
            this.mBlockCountRange = this.mBlockCountRange.intersect(Range.create(Integer.valueOf(this.mHorizontalBlockRange.getLower().intValue() * this.mVerticalBlockRange.getLower().intValue()), Integer.valueOf(this.mHorizontalBlockRange.getUpper().intValue() * this.mVerticalBlockRange.getUpper().intValue())));
            this.mBlockAspectRatioRange = this.mBlockAspectRatioRange.intersect(new Rational(this.mHorizontalBlockRange.getLower().intValue(), this.mVerticalBlockRange.getUpper().intValue()), new Rational(this.mHorizontalBlockRange.getUpper().intValue(), this.mVerticalBlockRange.getLower().intValue()));
            this.mWidthRange = this.mWidthRange.intersect(Integer.valueOf(((this.mHorizontalBlockRange.getLower().intValue() - 1) * this.mBlockWidth) + this.mWidthAlignment), Integer.valueOf(this.mHorizontalBlockRange.getUpper().intValue() * this.mBlockWidth));
            this.mHeightRange = this.mHeightRange.intersect(Integer.valueOf(((this.mVerticalBlockRange.getLower().intValue() - 1) * this.mBlockHeight) + this.mHeightAlignment), Integer.valueOf(this.mVerticalBlockRange.getUpper().intValue() * this.mBlockHeight));
            this.mAspectRatioRange = this.mAspectRatioRange.intersect(new Rational(this.mWidthRange.getLower().intValue(), this.mHeightRange.getUpper().intValue()), new Rational(this.mWidthRange.getUpper().intValue(), this.mHeightRange.getLower().intValue()));
            this.mSmallerDimensionUpperLimit = Math.min(this.mSmallerDimensionUpperLimit, Math.min(this.mWidthRange.getUpper().intValue(), this.mHeightRange.getUpper().intValue()));
            this.mBlocksPerSecondRange = this.mBlocksPerSecondRange.intersect(Long.valueOf(((long) this.mBlockCountRange.getLower().intValue()) * ((long) this.mFrameRateRange.getLower().intValue())), Long.valueOf(((long) this.mBlockCountRange.getUpper().intValue()) * ((long) this.mFrameRateRange.getUpper().intValue())));
            this.mFrameRateRange = this.mFrameRateRange.intersect(Integer.valueOf((int) (this.mBlocksPerSecondRange.getLower().longValue() / ((long) this.mBlockCountRange.getUpper().intValue()))), Integer.valueOf((int) (((double) this.mBlocksPerSecondRange.getUpper().longValue()) / ((double) this.mBlockCountRange.getLower().intValue()))));
        }

        private void applyMacroBlockLimits(int maxHorizontalBlocks, int maxVerticalBlocks, int maxBlocks, long maxBlocksPerSecond, int blockWidth, int blockHeight, int widthAlignment, int heightAlignment) {
            applyMacroBlockLimits(1, 1, maxHorizontalBlocks, maxVerticalBlocks, maxBlocks, maxBlocksPerSecond, blockWidth, blockHeight, widthAlignment, heightAlignment);
        }

        private void applyMacroBlockLimits(int minHorizontalBlocks, int minVerticalBlocks, int maxHorizontalBlocks, int maxVerticalBlocks, int maxBlocks, long maxBlocksPerSecond, int blockWidth, int blockHeight, int widthAlignment, int heightAlignment) {
            applyAlignment(widthAlignment, heightAlignment);
            applyBlockLimits(blockWidth, blockHeight, Range.create(1, Integer.valueOf(maxBlocks)), Range.create(1L, Long.valueOf(maxBlocksPerSecond)), Range.create(new Rational(1, maxVerticalBlocks), new Rational(maxHorizontalBlocks, 1)));
            this.mHorizontalBlockRange = this.mHorizontalBlockRange.intersect(Integer.valueOf(Utils.divUp(minHorizontalBlocks, this.mBlockWidth / blockWidth)), Integer.valueOf(maxHorizontalBlocks / (this.mBlockWidth / blockWidth)));
            this.mVerticalBlockRange = this.mVerticalBlockRange.intersect(Integer.valueOf(Utils.divUp(minVerticalBlocks, this.mBlockHeight / blockHeight)), Integer.valueOf(maxVerticalBlocks / (this.mBlockHeight / blockHeight)));
        }

        /* JADX WARNING: Removed duplicated region for block: B:376:0x029e A[SYNTHETIC] */
        /* JADX WARNING: Removed duplicated region for block: B:54:0x029a  */
        private void applyLevelLimits() {
            Integer num;
            int maxBps;
            int errors;
            String str;
            int D;
            int BR;
            long SR;
            int D2;
            double FR;
            Integer num2;
            CodecProfileLevel[] profileLevels;
            String str2;
            int BR2;
            long SR2;
            int FS;
            int D3;
            String str3;
            String str4;
            int FR2;
            int MBPS;
            String str5;
            String str6;
            int W;
            int minWidth;
            int minHeight;
            String str7;
            int MBPS2;
            String str8;
            String str9;
            int i;
            String str10;
            CodecProfileLevel[] profileLevels2;
            int FS2;
            String str11;
            int H;
            int W2;
            int FR3;
            int MBPS3;
            int MBPS4;
            int i2;
            String str12;
            String str13;
            String str14;
            int H2;
            int FS3;
            int W3;
            int FR4;
            int MBPS5;
            int BR3;
            String str15;
            int MBPS6;
            int DPB;
            int BR4;
            int FS4;
            int BR5;
            CodecProfileLevel[] profileLevels3 = this.mParent.profileLevels;
            String mime = this.mParent.getMimeType();
            boolean equalsIgnoreCase = mime.equalsIgnoreCase(MediaFormat.MIMETYPE_VIDEO_AVC);
            String str16 = "Unrecognized level ";
            String str17 = "Unrecognized profile ";
            String str18 = " for ";
            String str19 = TAG;
            int i3 = 1;
            if (equalsIgnoreCase) {
                errors = 4;
                int maxBlocks = 99;
                int maxBps2 = 64000;
                long maxBlocksPerSecond = 1485;
                int maxDPBBlocks = 396;
                int i4 = 0;
                for (int maxDPBBlocks2 = profileLevels3.length; i4 < maxDPBBlocks2; maxDPBBlocks2 = maxDPBBlocks2) {
                    CodecProfileLevel profileLevel = profileLevels3[i4];
                    boolean supported = true;
                    int i5 = profileLevel.level;
                    if (i5 == 1) {
                        MBPS6 = 1485;
                        BR4 = 64;
                        DPB = 396;
                        str15 = str16;
                        FS4 = 99;
                    } else if (i5 != 2) {
                        switch (i5) {
                            case 4:
                                MBPS6 = 3000;
                                BR4 = 192;
                                DPB = 900;
                                str15 = str16;
                                FS4 = 396;
                                break;
                            case 8:
                                MBPS6 = 6000;
                                BR4 = 384;
                                DPB = 2376;
                                str15 = str16;
                                FS4 = 396;
                                break;
                            case 16:
                                MBPS6 = 11880;
                                BR4 = 768;
                                DPB = 2376;
                                str15 = str16;
                                FS4 = 396;
                                break;
                            case 32:
                                MBPS6 = 11880;
                                BR4 = 2000;
                                DPB = 2376;
                                str15 = str16;
                                FS4 = 396;
                                break;
                            case 64:
                                MBPS6 = 19800;
                                BR4 = 4000;
                                DPB = 4752;
                                str15 = str16;
                                FS4 = 792;
                                break;
                            case 128:
                                MBPS6 = 20250;
                                BR4 = 4000;
                                DPB = 8100;
                                str15 = str16;
                                FS4 = 1620;
                                break;
                            case 256:
                                MBPS6 = 40500;
                                BR4 = 10000;
                                DPB = 8100;
                                str15 = str16;
                                FS4 = 1620;
                                break;
                            case 512:
                                MBPS6 = 108000;
                                BR4 = 14000;
                                DPB = 18000;
                                str15 = str16;
                                FS4 = 3600;
                                break;
                            case 1024:
                                MBPS6 = 216000;
                                BR4 = 20000;
                                DPB = 20480;
                                str15 = str16;
                                FS4 = 5120;
                                break;
                            case 2048:
                                MBPS6 = 245760;
                                BR4 = 20000;
                                DPB = 32768;
                                str15 = str16;
                                FS4 = 8192;
                                break;
                            case 4096:
                                MBPS6 = 245760;
                                BR4 = 50000;
                                DPB = 32768;
                                str15 = str16;
                                FS4 = 8192;
                                break;
                            case 8192:
                                MBPS6 = 522240;
                                BR4 = 50000;
                                DPB = 34816;
                                str15 = str16;
                                FS4 = 8704;
                                break;
                            case 16384:
                                MBPS6 = 589824;
                                BR4 = 135000;
                                DPB = 110400;
                                str15 = str16;
                                FS4 = 22080;
                                break;
                            case 32768:
                                MBPS6 = 983040;
                                BR4 = 240000;
                                DPB = 184320;
                                str15 = str16;
                                FS4 = 36864;
                                break;
                            case 65536:
                                MBPS6 = 2073600;
                                BR4 = 240000;
                                DPB = 184320;
                                str15 = str16;
                                FS4 = 36864;
                                break;
                            case 131072:
                                MBPS6 = 4177920;
                                BR4 = 240000;
                                DPB = 696320;
                                str15 = str16;
                                FS4 = 139264;
                                break;
                            case 262144:
                                MBPS6 = 8355840;
                                BR4 = 480000;
                                DPB = 696320;
                                str15 = str16;
                                FS4 = 139264;
                                break;
                            case 524288:
                                MBPS6 = 16711680;
                                BR4 = 800000;
                                DPB = 696320;
                                str15 = str16;
                                FS4 = 139264;
                                break;
                            default:
                                Log.w(str19, str16 + profileLevel.level + str18 + mime);
                                errors |= 1;
                                MBPS6 = 0;
                                BR4 = 0;
                                DPB = 0;
                                str15 = str16;
                                FS4 = 0;
                                break;
                        }
                    } else {
                        MBPS6 = 1485;
                        BR4 = 128;
                        DPB = 396;
                        str15 = str16;
                        FS4 = 99;
                    }
                    int i6 = profileLevel.profile;
                    if (!(i6 == 1 || i6 == 2)) {
                        if (i6 != 4) {
                            if (i6 != 8) {
                                if (i6 == 16) {
                                    BR5 = BR4 * 3000;
                                } else if (!(i6 == 32 || i6 == 64)) {
                                    if (i6 != 65536) {
                                        if (i6 != 524288) {
                                            Log.w(str19, str17 + profileLevel.profile + str18 + mime);
                                            errors |= 1;
                                            BR5 = BR4 * 1000;
                                        }
                                    }
                                }
                                if (supported) {
                                    errors &= -5;
                                }
                                maxBlocksPerSecond = Math.max((long) MBPS6, maxBlocksPerSecond);
                                maxBlocks = Math.max(FS4, maxBlocks);
                                maxBps2 = Math.max(BR5, maxBps2);
                                maxDPBBlocks = Math.max(maxDPBBlocks, DPB);
                                i4++;
                                str19 = str19;
                                str16 = str15;
                                profileLevels3 = profileLevels3;
                                i3 = i3;
                            }
                            BR5 = BR4 * MetricsProto.MetricsEvent.FIELD_SELECTION_RANGE_START;
                            if (supported) {
                            }
                            maxBlocksPerSecond = Math.max((long) MBPS6, maxBlocksPerSecond);
                            maxBlocks = Math.max(FS4, maxBlocks);
                            maxBps2 = Math.max(BR5, maxBps2);
                            maxDPBBlocks = Math.max(maxDPBBlocks, DPB);
                            i4++;
                            str19 = str19;
                            str16 = str15;
                            profileLevels3 = profileLevels3;
                            i3 = i3;
                        }
                        Log.w(str19, "Unsupported profile " + profileLevel.profile + str18 + mime);
                        errors |= 2;
                        supported = false;
                    }
                    BR5 = BR4 * 1000;
                    if (supported) {
                    }
                    maxBlocksPerSecond = Math.max((long) MBPS6, maxBlocksPerSecond);
                    maxBlocks = Math.max(FS4, maxBlocks);
                    maxBps2 = Math.max(BR5, maxBps2);
                    maxDPBBlocks = Math.max(maxDPBBlocks, DPB);
                    i4++;
                    str19 = str19;
                    str16 = str15;
                    profileLevels3 = profileLevels3;
                    i3 = i3;
                }
                int maxLengthInBlocks = (int) Math.sqrt((double) (maxBlocks * 8));
                maxBps = maxBps2;
                num = i3;
                applyMacroBlockLimits(maxLengthInBlocks, maxLengthInBlocks, maxBlocks, maxBlocksPerSecond, 16, 16, 1, 1);
            } else {
                String str20 = str16;
                num = 1;
                String str21 = str19;
                String str22 = "/";
                String str23 = "Unrecognized profile/level ";
                if (mime.equalsIgnoreCase(MediaFormat.MIMETYPE_VIDEO_MPEG2)) {
                    CodecProfileLevel[] profileLevels4 = profileLevels3;
                    int length = profileLevels4.length;
                    errors = 4;
                    int MBPS7 = 0;
                    int maxBlocks2 = 99;
                    int maxBps3 = 64000;
                    long maxBlocksPerSecond2 = 1485;
                    int maxWidth = 11;
                    int maxRate = 15;
                    int maxHeight = 9;
                    while (MBPS7 < length) {
                        CodecProfileLevel profileLevel2 = profileLevels4[MBPS7];
                        boolean supported2 = true;
                        int i7 = profileLevel2.profile;
                        if (i7 != 0) {
                            i2 = MBPS7;
                            if (i7 == 1) {
                                int i8 = profileLevel2.level;
                                if (i8 == 0) {
                                    MBPS5 = 11880;
                                    FS3 = 396;
                                    BR3 = 4000;
                                    str14 = str22;
                                    FR4 = 30;
                                    str13 = str23;
                                    W3 = 22;
                                    str12 = str21;
                                    H2 = 18;
                                } else if (i8 == 1) {
                                    MBPS5 = 40500;
                                    FS3 = 1620;
                                    BR3 = 15000;
                                    str14 = str22;
                                    FR4 = 30;
                                    str13 = str23;
                                    W3 = 45;
                                    str12 = str21;
                                    H2 = 36;
                                } else if (i8 == 2) {
                                    MBPS5 = 183600;
                                    FS3 = 6120;
                                    BR3 = 60000;
                                    str14 = str22;
                                    FR4 = 60;
                                    str13 = str23;
                                    W3 = 90;
                                    str12 = str21;
                                    H2 = 68;
                                } else if (i8 == 3) {
                                    MBPS5 = 244800;
                                    FS3 = 8160;
                                    BR3 = 80000;
                                    str14 = str22;
                                    FR4 = 60;
                                    str13 = str23;
                                    W3 = 120;
                                    str12 = str21;
                                    H2 = 68;
                                } else if (i8 != 4) {
                                    Log.w(str21, str23 + profileLevel2.profile + str22 + profileLevel2.level + str18 + mime);
                                    errors |= 1;
                                    MBPS5 = 0;
                                    FS3 = 0;
                                    BR3 = 0;
                                    str14 = str22;
                                    FR4 = 0;
                                    str13 = str23;
                                    W3 = 0;
                                    str12 = str21;
                                    H2 = 0;
                                } else {
                                    MBPS5 = 489600;
                                    FS3 = 8160;
                                    BR3 = 80000;
                                    str14 = str22;
                                    FR4 = 60;
                                    str13 = str23;
                                    W3 = 120;
                                    str12 = str21;
                                    H2 = 68;
                                }
                            } else if (i7 == 2 || i7 == 3 || i7 == 4 || i7 == 5) {
                                Log.i(str21, "Unsupported profile " + profileLevel2.profile + str18 + mime);
                                errors |= 2;
                                supported2 = false;
                                MBPS5 = 0;
                                FS3 = 0;
                                BR3 = 0;
                                str14 = str22;
                                FR4 = 0;
                                str13 = str23;
                                W3 = 0;
                                str12 = str21;
                                H2 = 0;
                            } else {
                                Log.w(str21, str17 + profileLevel2.profile + str18 + mime);
                                errors |= 1;
                                MBPS5 = 0;
                                FS3 = 0;
                                BR3 = 0;
                                str14 = str22;
                                FR4 = 0;
                                str13 = str23;
                                W3 = 0;
                                str12 = str21;
                                H2 = 0;
                            }
                        } else {
                            i2 = MBPS7;
                            if (profileLevel2.level != 1) {
                                Log.w(str21, str23 + profileLevel2.profile + str22 + profileLevel2.level + str18 + mime);
                                errors |= 1;
                                MBPS5 = 0;
                                FS3 = 0;
                                BR3 = 0;
                                str14 = str22;
                                FR4 = 0;
                                str13 = str23;
                                W3 = 0;
                                str12 = str21;
                                H2 = 0;
                            } else {
                                MBPS5 = 40500;
                                FS3 = 1620;
                                BR3 = 15000;
                                str14 = str22;
                                FR4 = 30;
                                str13 = str23;
                                W3 = 45;
                                str12 = str21;
                                H2 = 36;
                            }
                        }
                        if (supported2) {
                            errors &= -5;
                        }
                        maxBlocksPerSecond2 = Math.max((long) MBPS5, maxBlocksPerSecond2);
                        maxBlocks2 = Math.max(FS3, maxBlocks2);
                        maxBps3 = Math.max(BR3 * 1000, maxBps3);
                        maxWidth = Math.max(W3, maxWidth);
                        maxHeight = Math.max(H2, maxHeight);
                        maxRate = Math.max(FR4, maxRate);
                        MBPS7 = i2 + 1;
                        length = length;
                        str22 = str14;
                        str23 = str13;
                        str21 = str12;
                        profileLevels4 = profileLevels4;
                        str18 = str18;
                    }
                    applyMacroBlockLimits(maxWidth, maxHeight, maxBlocks2, maxBlocksPerSecond2, 16, 16, 1, 1);
                    this.mFrameRateRange = this.mFrameRateRange.intersect(12, Integer.valueOf(maxRate));
                    maxBps = maxBps3;
                } else {
                    String str24 = str18;
                    String str25 = str22;
                    String str26 = str23;
                    String str27 = str21;
                    if (mime.equalsIgnoreCase(MediaFormat.MIMETYPE_VIDEO_MPEG4)) {
                        CodecProfileLevel[] profileLevels5 = profileLevels3;
                        int length2 = profileLevels5.length;
                        int maxBlocks3 = 99;
                        errors = 4;
                        int maxRate2 = 11;
                        int FR5 = 9;
                        int maxHeight2 = 15;
                        long maxBlocksPerSecond3 = 1485;
                        int maxBps4 = 64000;
                        int i9 = 0;
                        while (i9 < length2) {
                            CodecProfileLevel profileLevel3 = profileLevels5[i9];
                            boolean strict = false;
                            boolean supported3 = true;
                            int MBPS8 = profileLevel3.profile;
                            if (MBPS8 != 1) {
                                if (MBPS8 != 2) {
                                    switch (MBPS8) {
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
                                            int i10 = profileLevel3.level;
                                            i = length2;
                                            if (i10 != 1 && i10 != 4) {
                                                if (i10 != 8) {
                                                    if (i10 != 16) {
                                                        if (i10 != 24) {
                                                            if (i10 != 32) {
                                                                if (i10 != 128) {
                                                                    StringBuilder sb = new StringBuilder();
                                                                    str11 = str26;
                                                                    sb.append(str11);
                                                                    str10 = str17;
                                                                    sb.append(profileLevel3.profile);
                                                                    sb.append(str25);
                                                                    profileLevels2 = profileLevels5;
                                                                    sb.append(profileLevel3.level);
                                                                    sb.append(str24);
                                                                    sb.append(mime);
                                                                    Log.w(str27, sb.toString());
                                                                    errors |= 1;
                                                                    str24 = str24;
                                                                    MBPS3 = 0;
                                                                    FR3 = 0;
                                                                    FS2 = 0;
                                                                    str9 = str27;
                                                                    W2 = 0;
                                                                    MBPS4 = 0;
                                                                    str8 = str25;
                                                                    H = 0;
                                                                    break;
                                                                } else {
                                                                    str11 = str26;
                                                                    str10 = str17;
                                                                    profileLevels2 = profileLevels5;
                                                                    FS2 = 1620;
                                                                    str24 = str24;
                                                                    FR3 = 30;
                                                                    str9 = str27;
                                                                    W2 = 45;
                                                                    str8 = str25;
                                                                    H = 36;
                                                                    MBPS4 = 48600;
                                                                    MBPS3 = 8000;
                                                                    break;
                                                                }
                                                            } else {
                                                                str11 = str26;
                                                                str10 = str17;
                                                                profileLevels2 = profileLevels5;
                                                                FS2 = 792;
                                                                str24 = str24;
                                                                FR3 = 30;
                                                                str9 = str27;
                                                                W2 = 44;
                                                                str8 = str25;
                                                                H = 36;
                                                                MBPS4 = 23760;
                                                                MBPS3 = 3000;
                                                                break;
                                                            }
                                                        } else {
                                                            str11 = str26;
                                                            str10 = str17;
                                                            profileLevels2 = profileLevels5;
                                                            FS2 = 396;
                                                            str24 = str24;
                                                            FR3 = 30;
                                                            str9 = str27;
                                                            W2 = 22;
                                                            str8 = str25;
                                                            H = 18;
                                                            MBPS4 = 11880;
                                                            MBPS3 = 1500;
                                                            break;
                                                        }
                                                    } else {
                                                        str11 = str26;
                                                        str10 = str17;
                                                        profileLevels2 = profileLevels5;
                                                        FS2 = 396;
                                                        str24 = str24;
                                                        FR3 = 30;
                                                        str9 = str27;
                                                        W2 = 22;
                                                        str8 = str25;
                                                        H = 18;
                                                        MBPS4 = 11880;
                                                        MBPS3 = 768;
                                                        break;
                                                    }
                                                } else {
                                                    str11 = str26;
                                                    str10 = str17;
                                                    profileLevels2 = profileLevels5;
                                                    FS2 = 396;
                                                    str24 = str24;
                                                    FR3 = 30;
                                                    str9 = str27;
                                                    W2 = 22;
                                                    str8 = str25;
                                                    H = 18;
                                                    MBPS4 = 5940;
                                                    MBPS3 = 384;
                                                    break;
                                                }
                                            } else {
                                                str11 = str26;
                                                str10 = str17;
                                                profileLevels2 = profileLevels5;
                                                FS2 = 99;
                                                str24 = str24;
                                                FR3 = 30;
                                                str9 = str27;
                                                W2 = 11;
                                                str8 = str25;
                                                H = 9;
                                                MBPS4 = 2970;
                                                MBPS3 = 128;
                                                break;
                                            }
                                            break;
                                        default:
                                            Log.w(str27, str17 + profileLevel3.profile + str24 + mime);
                                            errors |= 1;
                                            i = length2;
                                            MBPS3 = 0;
                                            str11 = str26;
                                            FR3 = 0;
                                            str9 = str27;
                                            str10 = str17;
                                            W2 = 0;
                                            H = 0;
                                            MBPS4 = 0;
                                            str8 = str25;
                                            profileLevels2 = profileLevels5;
                                            FS2 = 0;
                                            break;
                                    }
                                }
                                i = length2;
                                str11 = str26;
                                str10 = str17;
                                profileLevels2 = profileLevels5;
                                Log.i(str27, "Unsupported profile " + profileLevel3.profile + str24 + mime);
                                errors |= 2;
                                supported3 = false;
                                str24 = str24;
                                MBPS3 = 0;
                                FR3 = 0;
                                FS2 = 0;
                                str9 = str27;
                                W2 = 0;
                                MBPS4 = 0;
                                str8 = str25;
                                H = 0;
                            } else {
                                i = length2;
                                str11 = str26;
                                str10 = str17;
                                profileLevels2 = profileLevels5;
                                int i11 = profileLevel3.level;
                                if (i11 == 1) {
                                    strict = true;
                                    FS2 = 99;
                                    str24 = str24;
                                    FR3 = 15;
                                    str9 = str27;
                                    W2 = 11;
                                    str8 = str25;
                                    H = 9;
                                    MBPS4 = 1485;
                                    MBPS3 = 64;
                                } else if (i11 == 2) {
                                    strict = true;
                                    FS2 = 99;
                                    str24 = str24;
                                    FR3 = 15;
                                    str9 = str27;
                                    W2 = 11;
                                    str8 = str25;
                                    H = 9;
                                    MBPS4 = 1485;
                                    MBPS3 = 128;
                                } else if (i11 == 4) {
                                    FS2 = 99;
                                    str24 = str24;
                                    FR3 = 30;
                                    str9 = str27;
                                    W2 = 11;
                                    str8 = str25;
                                    H = 9;
                                    MBPS4 = 1485;
                                    MBPS3 = 64;
                                } else if (i11 == 8) {
                                    FS2 = 396;
                                    str24 = str24;
                                    FR3 = 30;
                                    str9 = str27;
                                    W2 = 22;
                                    str8 = str25;
                                    H = 18;
                                    MBPS4 = 5940;
                                    MBPS3 = 128;
                                } else if (i11 == 16) {
                                    FS2 = 396;
                                    str24 = str24;
                                    FR3 = 30;
                                    str9 = str27;
                                    W2 = 22;
                                    str8 = str25;
                                    H = 18;
                                    MBPS4 = 11880;
                                    MBPS3 = 384;
                                } else if (i11 == 64) {
                                    FS2 = 1200;
                                    str24 = str24;
                                    FR3 = 30;
                                    str9 = str27;
                                    W2 = 40;
                                    str8 = str25;
                                    H = 30;
                                    MBPS4 = 36000;
                                    MBPS3 = 4000;
                                } else if (i11 == 128) {
                                    FS2 = 1620;
                                    str24 = str24;
                                    FR3 = 30;
                                    str9 = str27;
                                    W2 = 45;
                                    str8 = str25;
                                    H = 36;
                                    MBPS4 = 40500;
                                    MBPS3 = 8000;
                                } else if (i11 != 256) {
                                    Log.w(str27, str11 + profileLevel3.profile + str25 + profileLevel3.level + str24 + mime);
                                    errors |= 1;
                                    str24 = str24;
                                    MBPS3 = 0;
                                    FR3 = 0;
                                    FS2 = 0;
                                    str9 = str27;
                                    W2 = 0;
                                    MBPS4 = 0;
                                    str8 = str25;
                                    H = 0;
                                } else {
                                    FS2 = 3600;
                                    str24 = str24;
                                    FR3 = 30;
                                    str9 = str27;
                                    W2 = 80;
                                    str8 = str25;
                                    H = 45;
                                    MBPS4 = 108000;
                                    MBPS3 = 12000;
                                }
                            }
                            if (supported3) {
                                errors &= -5;
                            }
                            maxBlocksPerSecond3 = Math.max((long) MBPS4, maxBlocksPerSecond3);
                            maxBlocks3 = Math.max(FS2, maxBlocks3);
                            maxBps4 = Math.max(MBPS3 * 1000, maxBps4);
                            if (strict) {
                                int maxWidth2 = Math.max(W2, maxRate2);
                                FR5 = Math.max(H, FR5);
                                maxHeight2 = Math.max(FR3, maxHeight2);
                                maxRate2 = maxWidth2;
                            } else {
                                int maxDim = (int) Math.sqrt((double) (FS2 * 2));
                                int maxWidth3 = Math.max(maxDim, maxRate2);
                                int maxHeight3 = Math.max(maxDim, FR5);
                                maxHeight2 = Math.max(Math.max(FR3, 60), maxHeight2);
                                FR5 = maxHeight3;
                                maxRate2 = maxWidth3;
                            }
                            i9++;
                            profileLevels5 = profileLevels2;
                            str17 = str10;
                            str25 = str8;
                            str26 = str11;
                            length2 = i;
                            str27 = str9;
                        }
                        applyMacroBlockLimits(maxRate2, FR5, maxBlocks3, maxBlocksPerSecond3, 16, 16, 1, 1);
                        this.mFrameRateRange = this.mFrameRateRange.intersect(12, Integer.valueOf(maxHeight2));
                        maxBps = maxBps4;
                    } else {
                        VideoCapabilities videoCapabilities = this;
                        String str28 = str26;
                        String str29 = str27;
                        String str30 = str17;
                        String str31 = str25;
                        if (mime.equalsIgnoreCase(MediaFormat.MIMETYPE_VIDEO_H263)) {
                            int minHeight2 = 9;
                            CodecProfileLevel[] profileLevels6 = profileLevels3;
                            int length3 = profileLevels6.length;
                            int errors2 = 4;
                            int minAlignment = 16;
                            int MBPS9 = 0;
                            int minAlignment2 = 11;
                            int maxBlocks4 = 99;
                            int maxBps5 = 64000;
                            long maxBlocksPerSecond4 = 1485;
                            int maxWidth4 = 11;
                            int maxHeight4 = 9;
                            int maxHeight5 = 15;
                            while (MBPS9 < length3) {
                                CodecProfileLevel profileLevel4 = profileLevels6[MBPS9];
                                int BR6 = 0;
                                int minW = minAlignment2;
                                int minH = minHeight2;
                                boolean strict2 = false;
                                int i12 = profileLevel4.level;
                                if (i12 == 1) {
                                    str5 = str28;
                                    str7 = str29;
                                    str6 = str31;
                                    strict2 = true;
                                    BR6 = 1;
                                    MBPS2 = 11 * 9 * 15;
                                    W = 11;
                                    MBPS = minHeight2;
                                    minHeight = 9;
                                    FR2 = minAlignment2;
                                    minWidth = 15;
                                } else if (i12 == 2) {
                                    str5 = str28;
                                    str7 = str29;
                                    str6 = str31;
                                    strict2 = true;
                                    BR6 = 2;
                                    MBPS2 = 22 * 18 * 15;
                                    W = 22;
                                    MBPS = minHeight2;
                                    minHeight = 18;
                                    FR2 = minAlignment2;
                                    minWidth = 30;
                                } else if (i12 == 4) {
                                    str5 = str28;
                                    str7 = str29;
                                    str6 = str31;
                                    strict2 = true;
                                    BR6 = 6;
                                    MBPS2 = 22 * 18 * 30;
                                    W = 22;
                                    MBPS = minHeight2;
                                    minHeight = 18;
                                    FR2 = minAlignment2;
                                    minWidth = 30;
                                } else if (i12 == 8) {
                                    str5 = str28;
                                    str7 = str29;
                                    str6 = str31;
                                    strict2 = true;
                                    BR6 = 32;
                                    MBPS2 = 22 * 18 * 30;
                                    W = 22;
                                    MBPS = minHeight2;
                                    minHeight = 18;
                                    FR2 = minAlignment2;
                                    minWidth = 30;
                                } else if (i12 == 16) {
                                    str5 = str28;
                                    str7 = str29;
                                    str6 = str31;
                                    strict2 = profileLevel4.profile == 1 || profileLevel4.profile == 4;
                                    if (!strict2) {
                                        minW = 1;
                                        minH = 1;
                                        minAlignment = 4;
                                    }
                                    BR6 = 2;
                                    MBPS2 = 11 * 9 * 15;
                                    W = 11;
                                    MBPS = minHeight2;
                                    minHeight = 9;
                                    FR2 = minAlignment2;
                                    minWidth = 15;
                                } else if (i12 == 32) {
                                    str5 = str28;
                                    str7 = str29;
                                    minW = 1;
                                    minH = 1;
                                    minAlignment = 4;
                                    BR6 = 64;
                                    str6 = str31;
                                    MBPS2 = 22 * 18 * 50;
                                    W = 22;
                                    MBPS = minHeight2;
                                    minHeight = 18;
                                    FR2 = minAlignment2;
                                    minWidth = 60;
                                } else if (i12 == 64) {
                                    str5 = str28;
                                    str7 = str29;
                                    minW = 1;
                                    minH = 1;
                                    minAlignment = 4;
                                    BR6 = 128;
                                    str6 = str31;
                                    MBPS2 = 45 * 18 * 50;
                                    W = 45;
                                    MBPS = minHeight2;
                                    minHeight = 18;
                                    FR2 = minAlignment2;
                                    minWidth = 60;
                                } else if (i12 != 128) {
                                    StringBuilder sb2 = new StringBuilder();
                                    sb2.append(str28);
                                    sb2.append(profileLevel4.profile);
                                    sb2.append(str31);
                                    str5 = str28;
                                    sb2.append(profileLevel4.level);
                                    sb2.append(str24);
                                    sb2.append(mime);
                                    str7 = str29;
                                    Log.w(str7, sb2.toString());
                                    errors2 |= 1;
                                    str6 = str31;
                                    MBPS2 = 0;
                                    W = 0;
                                    MBPS = minHeight2;
                                    minHeight = 0;
                                    FR2 = minAlignment2;
                                    minWidth = 0;
                                } else {
                                    str5 = str28;
                                    str7 = str29;
                                    minW = 1;
                                    minH = 1;
                                    minAlignment = 4;
                                    BR6 = 256;
                                    str6 = str31;
                                    MBPS2 = 45 * 36 * 50;
                                    W = 45;
                                    MBPS = minHeight2;
                                    minHeight = 36;
                                    FR2 = minAlignment2;
                                    minWidth = 60;
                                }
                                int maxRate3 = profileLevel4.profile;
                                if (!(maxRate3 == 1 || maxRate3 == 2 || maxRate3 == 4 || maxRate3 == 8 || maxRate3 == 16 || maxRate3 == 32 || maxRate3 == 64 || maxRate3 == 128 || maxRate3 == 256)) {
                                    Log.w(str7, str30 + profileLevel4.profile + str24 + mime);
                                    errors2 |= 1;
                                }
                                if (strict2) {
                                    minW = 11;
                                    minH = 9;
                                } else {
                                    videoCapabilities.mAllowMbOverride = true;
                                }
                                errors2 &= -5;
                                maxBlocksPerSecond4 = Math.max((long) MBPS2, maxBlocksPerSecond4);
                                maxBlocks4 = Math.max(W * minHeight, maxBlocks4);
                                maxBps5 = Math.max(64000 * BR6, maxBps5);
                                maxWidth4 = Math.max(W, maxWidth4);
                                maxHeight4 = Math.max(minHeight, maxHeight4);
                                int maxRate4 = Math.max(minWidth, maxHeight5);
                                int minWidth2 = Math.min(minW, FR2);
                                minHeight2 = Math.min(minH, MBPS);
                                MBPS9++;
                                minAlignment2 = minWidth2;
                                length3 = length3;
                                maxHeight5 = maxRate4;
                                profileLevels6 = profileLevels6;
                                videoCapabilities = this;
                                str29 = str7;
                                str28 = str5;
                                str31 = str6;
                            }
                            if (!this.mAllowMbOverride) {
                                this.mBlockAspectRatioRange = Range.create(new Rational(11, 9), new Rational(11, 9));
                            }
                            applyMacroBlockLimits(minAlignment2, minHeight2, maxWidth4, maxHeight4, maxBlocks4, maxBlocksPerSecond4, 16, 16, minAlignment, minAlignment);
                            this.mFrameRateRange = Range.create(1, Integer.valueOf(maxHeight5));
                            maxBps = maxBps5;
                            errors = errors2;
                        } else {
                            Integer num3 = 1;
                            if (mime.equalsIgnoreCase(MediaFormat.MIMETYPE_VIDEO_VP8)) {
                                int length4 = profileLevels3.length;
                                errors = 4;
                                int i13 = 0;
                                while (i13 < length4) {
                                    CodecProfileLevel profileLevel5 = profileLevels3[i13];
                                    int i14 = profileLevel5.level;
                                    if (i14 == 1 || i14 == 2 || i14 == 4 || i14 == 8) {
                                        str3 = str20;
                                    } else {
                                        StringBuilder sb3 = new StringBuilder();
                                        str3 = str20;
                                        sb3.append(str3);
                                        sb3.append(profileLevel5.level);
                                        sb3.append(str24);
                                        sb3.append(mime);
                                        Log.w(str29, sb3.toString());
                                        errors |= 1;
                                    }
                                    if (profileLevel5.profile != 1) {
                                        StringBuilder sb4 = new StringBuilder();
                                        str4 = str30;
                                        sb4.append(str4);
                                        sb4.append(profileLevel5.profile);
                                        sb4.append(str24);
                                        sb4.append(mime);
                                        Log.w(str29, sb4.toString());
                                        errors |= 1;
                                    } else {
                                        str4 = str30;
                                    }
                                    errors &= -5;
                                    i13++;
                                    str20 = str3;
                                    str30 = str4;
                                }
                                applyMacroBlockLimits(32767, 32767, Integer.MAX_VALUE, 2147483647L, 16, 16, 1, 1);
                                num = 1;
                                maxBps = 100000000;
                            } else {
                                String str32 = str20;
                                String str33 = str30;
                                CodecProfileLevel[] profileLevels7 = profileLevels3;
                                if (mime.equalsIgnoreCase(MediaFormat.MIMETYPE_VIDEO_VP9)) {
                                    int maxBlocks5 = 36864;
                                    int length5 = profileLevels7.length;
                                    int errors3 = 4;
                                    int maxDim2 = 512;
                                    int maxBps6 = 200000;
                                    long maxBlocksPerSecond5 = 829440;
                                    int D4 = 0;
                                    while (D4 < length5) {
                                        CodecProfileLevel profileLevel6 = profileLevels7[D4];
                                        int i15 = profileLevel6.level;
                                        if (i15 == 1) {
                                            num2 = num3;
                                            profileLevels = profileLevels7;
                                            FS = 36864;
                                            BR2 = 200;
                                            SR2 = 829440;
                                            str2 = str32;
                                            D3 = 512;
                                        } else if (i15 != 2) {
                                            switch (i15) {
                                                case 4:
                                                    num2 = num3;
                                                    profileLevels = profileLevels7;
                                                    FS = 122880;
                                                    BR2 = 1800;
                                                    SR2 = 4608000;
                                                    str2 = str32;
                                                    D3 = 960;
                                                    break;
                                                case 8:
                                                    num2 = num3;
                                                    profileLevels = profileLevels7;
                                                    FS = 245760;
                                                    BR2 = 3600;
                                                    SR2 = 9216000;
                                                    str2 = str32;
                                                    D3 = 1344;
                                                    break;
                                                case 16:
                                                    num2 = num3;
                                                    profileLevels = profileLevels7;
                                                    FS = 552960;
                                                    BR2 = 7200;
                                                    SR2 = 20736000;
                                                    str2 = str32;
                                                    D3 = 2048;
                                                    break;
                                                case 32:
                                                    num2 = num3;
                                                    profileLevels = profileLevels7;
                                                    FS = 983040;
                                                    BR2 = 12000;
                                                    SR2 = 36864000;
                                                    str2 = str32;
                                                    D3 = 2752;
                                                    break;
                                                case 64:
                                                    num2 = num3;
                                                    profileLevels = profileLevels7;
                                                    FS = 2228224;
                                                    BR2 = 18000;
                                                    SR2 = 83558400;
                                                    str2 = str32;
                                                    D3 = 4160;
                                                    break;
                                                case 128:
                                                    num2 = num3;
                                                    profileLevels = profileLevels7;
                                                    FS = 2228224;
                                                    BR2 = 30000;
                                                    SR2 = 160432128;
                                                    str2 = str32;
                                                    D3 = 4160;
                                                    break;
                                                case 256:
                                                    num2 = num3;
                                                    profileLevels = profileLevels7;
                                                    FS = 8912896;
                                                    BR2 = 60000;
                                                    SR2 = 311951360;
                                                    str2 = str32;
                                                    D3 = 8384;
                                                    break;
                                                case 512:
                                                    num2 = num3;
                                                    profileLevels = profileLevels7;
                                                    FS = 8912896;
                                                    BR2 = 120000;
                                                    SR2 = 588251136;
                                                    str2 = str32;
                                                    D3 = 8384;
                                                    break;
                                                case 1024:
                                                    num2 = num3;
                                                    profileLevels = profileLevels7;
                                                    FS = 8912896;
                                                    BR2 = 180000;
                                                    SR2 = 1176502272;
                                                    str2 = str32;
                                                    D3 = 8384;
                                                    break;
                                                case 2048:
                                                    num2 = num3;
                                                    profileLevels = profileLevels7;
                                                    FS = 35651584;
                                                    BR2 = 180000;
                                                    SR2 = 1176502272;
                                                    str2 = str32;
                                                    D3 = 16832;
                                                    break;
                                                case 4096:
                                                    num2 = num3;
                                                    profileLevels = profileLevels7;
                                                    FS = 35651584;
                                                    BR2 = 240000;
                                                    SR2 = 2353004544L;
                                                    str2 = str32;
                                                    D3 = 16832;
                                                    break;
                                                case 8192:
                                                    num2 = num3;
                                                    profileLevels = profileLevels7;
                                                    FS = 35651584;
                                                    BR2 = 480000;
                                                    SR2 = 4706009088L;
                                                    str2 = str32;
                                                    D3 = 16832;
                                                    break;
                                                default:
                                                    Log.w(str29, str32 + profileLevel6.level + str24 + mime);
                                                    errors |= 1;
                                                    num2 = num3;
                                                    profileLevels = profileLevels7;
                                                    FS = 0;
                                                    BR2 = 0;
                                                    SR2 = 0;
                                                    str2 = str32;
                                                    D3 = 0;
                                                    break;
                                            }
                                        } else {
                                            num2 = num3;
                                            profileLevels = profileLevels7;
                                            FS = 73728;
                                            BR2 = 800;
                                            SR2 = 2764800;
                                            str2 = str32;
                                            D3 = 768;
                                        }
                                        int i16 = profileLevel6.profile;
                                        if (!(i16 == 1 || i16 == 2 || i16 == 4 || i16 == 8 || i16 == 4096 || i16 == 8192 || i16 == 16384 || i16 == 32768)) {
                                            Log.w(str29, str33 + profileLevel6.profile + str24 + mime);
                                            errors |= 1;
                                        }
                                        errors3 = errors & -5;
                                        maxBlocksPerSecond5 = Math.max(SR2, maxBlocksPerSecond5);
                                        maxBlocks5 = Math.max(FS, maxBlocks5);
                                        maxBps6 = Math.max(BR2 * 1000, maxBps6);
                                        maxDim2 = Math.max(D3, maxDim2);
                                        D4++;
                                        str32 = str2;
                                        length5 = length5;
                                        profileLevels7 = profileLevels;
                                        num3 = num2;
                                    }
                                    num = num3;
                                    int maxLengthInBlocks2 = Utils.divUp(maxDim2, 8);
                                    applyMacroBlockLimits(maxLengthInBlocks2, maxLengthInBlocks2, Utils.divUp(maxBlocks5, 64), Utils.divUp(maxBlocksPerSecond5, 64), 8, 8, 1, 1);
                                    maxBps = maxBps6;
                                } else {
                                    num = 1;
                                    if (mime.equalsIgnoreCase(MediaFormat.MIMETYPE_VIDEO_HEVC)) {
                                        CodecProfileLevel[] profileLevels8 = profileLevels7;
                                        int length6 = profileLevels8.length;
                                        long maxBlocksPerSecond6 = (long) (576 * 15);
                                        int maxBlocks6 = 576;
                                        int maxBps7 = 128000;
                                        int errors4 = 4;
                                        int i17 = 0;
                                        while (i17 < length6) {
                                            CodecProfileLevel profileLevel7 = profileLevels8[i17];
                                            int FS5 = 0;
                                            int BR7 = 0;
                                            int i18 = profileLevel7.level;
                                            if (i18 != 1 && i18 != 2) {
                                                switch (i18) {
                                                    case 4:
                                                    case 8:
                                                        FR = 30.0d;
                                                        FS5 = 122880;
                                                        BR7 = 1500;
                                                        break;
                                                    case 16:
                                                    case 32:
                                                        FR = 30.0d;
                                                        FS5 = 245760;
                                                        BR7 = 3000;
                                                        break;
                                                    case 64:
                                                    case 128:
                                                        FR = 30.0d;
                                                        FS5 = 552960;
                                                        BR7 = BluetoothHealth.HEALTH_OPERATION_SUCCESS;
                                                        break;
                                                    case 256:
                                                    case 512:
                                                        FR = 33.75d;
                                                        FS5 = SurfaceControl.FX_SURFACE_MASK;
                                                        BR7 = 10000;
                                                        break;
                                                    case 1024:
                                                        FR = 30.0d;
                                                        FS5 = 2228224;
                                                        BR7 = 12000;
                                                        break;
                                                    case 2048:
                                                        FR = 30.0d;
                                                        FS5 = 2228224;
                                                        BR7 = 30000;
                                                        break;
                                                    case 4096:
                                                        FR = 60.0d;
                                                        FS5 = 2228224;
                                                        BR7 = 20000;
                                                        break;
                                                    case 8192:
                                                        FR = 60.0d;
                                                        FS5 = 2228224;
                                                        BR7 = 50000;
                                                        break;
                                                    case 16384:
                                                        FR = 30.0d;
                                                        FS5 = 8912896;
                                                        BR7 = 25000;
                                                        break;
                                                    case 32768:
                                                        FR = 30.0d;
                                                        FS5 = 8912896;
                                                        BR7 = 100000;
                                                        break;
                                                    case 65536:
                                                        FR = 60.0d;
                                                        FS5 = 8912896;
                                                        BR7 = 40000;
                                                        break;
                                                    case 131072:
                                                        FR = 60.0d;
                                                        FS5 = 8912896;
                                                        BR7 = Protocol.BASE_WIFI_SCANNER_SERVICE;
                                                        break;
                                                    case 262144:
                                                        FR = 120.0d;
                                                        FS5 = 8912896;
                                                        BR7 = MediaPlayer.ProvisioningThread.TIMEOUT_MS;
                                                        break;
                                                    case 524288:
                                                        FR = 120.0d;
                                                        FS5 = 8912896;
                                                        BR7 = 240000;
                                                        break;
                                                    case 1048576:
                                                        FR = 30.0d;
                                                        FS5 = 35651584;
                                                        BR7 = MediaPlayer.ProvisioningThread.TIMEOUT_MS;
                                                        break;
                                                    case 2097152:
                                                        FR = 30.0d;
                                                        FS5 = 35651584;
                                                        BR7 = 240000;
                                                        break;
                                                    case 4194304:
                                                        FR = 60.0d;
                                                        FS5 = 35651584;
                                                        BR7 = AutofillManager.MAX_TEMP_AUGMENTED_SERVICE_DURATION_MS;
                                                        break;
                                                    case 8388608:
                                                        FR = 60.0d;
                                                        FS5 = 35651584;
                                                        BR7 = 480000;
                                                        break;
                                                    case 16777216:
                                                        FR = 120.0d;
                                                        FS5 = 35651584;
                                                        BR7 = 240000;
                                                        break;
                                                    case 33554432:
                                                        FR = 120.0d;
                                                        FS5 = 35651584;
                                                        BR7 = 800000;
                                                        break;
                                                    default:
                                                        Log.w(str29, str32 + profileLevel7.level + str24 + mime);
                                                        errors |= 1;
                                                        FR = 0.0d;
                                                        break;
                                                }
                                            } else {
                                                FR = 15.0d;
                                                FS5 = 36864;
                                                BR7 = 128;
                                            }
                                            int i19 = profileLevel7.profile;
                                            if (!(i19 == 1 || i19 == 2 || i19 == 4 || i19 == 4096 || i19 == 8192)) {
                                                Log.w(str29, str33 + profileLevel7.profile + str24 + mime);
                                                errors |= 1;
                                            }
                                            int FS6 = FS5 >> 6;
                                            errors4 = errors & -5;
                                            maxBlocksPerSecond6 = Math.max((long) ((int) (((double) FS6) * FR)), maxBlocksPerSecond6);
                                            maxBlocks6 = Math.max(FS6, maxBlocks6);
                                            maxBps7 = Math.max(BR7 * 1000, maxBps7);
                                            i17++;
                                            length6 = length6;
                                            str33 = str33;
                                            profileLevels8 = profileLevels8;
                                        }
                                        int maxLengthInBlocks3 = (int) Math.sqrt((double) (maxBlocks6 * 8));
                                        maxBps = maxBps7;
                                        applyMacroBlockLimits(maxLengthInBlocks3, maxLengthInBlocks3, maxBlocks6, maxBlocksPerSecond6, 8, 8, 1, 1);
                                    } else {
                                        String str34 = str32;
                                        if (mime.equalsIgnoreCase(MediaFormat.MIMETYPE_VIDEO_AV1)) {
                                            int maxBlocks7 = 36864;
                                            CodecProfileLevel[] profileLevels9 = profileLevels7;
                                            int length7 = profileLevels9.length;
                                            int maxBps8 = 200000;
                                            int errors5 = 4;
                                            int maxDim3 = 512;
                                            long maxBlocksPerSecond7 = 829440;
                                            int i20 = 0;
                                            while (i20 < length7) {
                                                CodecProfileLevel profileLevel8 = profileLevels9[i20];
                                                int FS7 = profileLevel8.level;
                                                if (FS7 != 1) {
                                                    if (FS7 != 2) {
                                                        switch (FS7) {
                                                            case 4:
                                                            case 8:
                                                                break;
                                                            case 16:
                                                                str = str34;
                                                                D2 = 4352;
                                                                D = 6000;
                                                                BR = 665856;
                                                                SR = 24969600;
                                                                break;
                                                            case 32:
                                                            case 64:
                                                            case 128:
                                                                str = str34;
                                                                D2 = 5504;
                                                                D = 10000;
                                                                BR = 1065024;
                                                                SR = 39938400;
                                                                break;
                                                            case 256:
                                                                str = str34;
                                                                D2 = 6144;
                                                                D = 12000;
                                                                BR = 2359296;
                                                                SR = 77856768;
                                                                break;
                                                            case 512:
                                                            case 1024:
                                                            case 2048:
                                                                str = str34;
                                                                D2 = 6144;
                                                                D = 20000;
                                                                BR = 2359296;
                                                                SR = 155713536;
                                                                break;
                                                            case 4096:
                                                                str = str34;
                                                                D2 = 8192;
                                                                D = 30000;
                                                                BR = 8912896;
                                                                SR = 273715200;
                                                                break;
                                                            case 8192:
                                                                str = str34;
                                                                D2 = 8192;
                                                                D = 40000;
                                                                BR = 8912896;
                                                                SR = 547430400;
                                                                break;
                                                            case 16384:
                                                                str = str34;
                                                                D2 = 8192;
                                                                D = 60000;
                                                                BR = 8912896;
                                                                SR = 1094860800;
                                                                break;
                                                            case 32768:
                                                                str = str34;
                                                                D2 = 8192;
                                                                D = 60000;
                                                                BR = 8912896;
                                                                SR = 1176502272;
                                                                break;
                                                            case 65536:
                                                                str = str34;
                                                                D2 = 16384;
                                                                D = 60000;
                                                                BR = 35651584;
                                                                SR = 1176502272;
                                                                break;
                                                            case 131072:
                                                                str = str34;
                                                                D2 = 16384;
                                                                D = 100000;
                                                                BR = 35651584;
                                                                SR = 2189721600L;
                                                                break;
                                                            case 262144:
                                                                str = str34;
                                                                D2 = 16384;
                                                                D = 160000;
                                                                BR = 35651584;
                                                                SR = 4379443200L;
                                                                break;
                                                            case 524288:
                                                                str = str34;
                                                                D2 = 16384;
                                                                D = 160000;
                                                                BR = 35651584;
                                                                SR = 4706009088L;
                                                                break;
                                                            default:
                                                                Log.w(str29, str34 + profileLevel8.level + str24 + mime);
                                                                errors |= 1;
                                                                str = str34;
                                                                D2 = 0;
                                                                SR = 0;
                                                                D = 0;
                                                                BR = 0;
                                                                break;
                                                        }
                                                    }
                                                    str = str34;
                                                    D2 = 2816;
                                                    D = 3000;
                                                    BR = 278784;
                                                    SR = 10454400;
                                                } else {
                                                    str = str34;
                                                    D2 = 2048;
                                                    D = 1500;
                                                    BR = 147456;
                                                    SR = 5529600;
                                                }
                                                int i21 = profileLevel8.profile;
                                                if (!(i21 == 1 || i21 == 2 || i21 == 4096 || i21 == 8192)) {
                                                    Log.w(str29, str33 + profileLevel8.profile + str24 + mime);
                                                    errors |= 1;
                                                }
                                                errors5 = errors & -5;
                                                maxBlocksPerSecond7 = Math.max(SR, maxBlocksPerSecond7);
                                                maxBlocks7 = Math.max(BR, maxBlocks7);
                                                maxBps8 = Math.max(D * 1000, maxBps8);
                                                maxDim3 = Math.max(D2, maxDim3);
                                                i20++;
                                                length7 = length7;
                                                str34 = str;
                                                profileLevels9 = profileLevels9;
                                            }
                                            int maxLengthInBlocks4 = Utils.divUp(maxDim3, 8);
                                            applyMacroBlockLimits(maxLengthInBlocks4, maxLengthInBlocks4, Utils.divUp(maxBlocks7, 64), Utils.divUp(maxBlocksPerSecond7, 64), 8, 8, 1, 1);
                                            maxBps = maxBps8;
                                        } else {
                                            Log.w(str29, "Unsupported mime " + mime);
                                            errors = 4 | 2;
                                            maxBps = 64000;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            this.mBitrateRange = Range.create(num, Integer.valueOf(maxBps));
            this.mParent.mError |= errors;
        }
    }

    public static final class EncoderCapabilities {
        public static final int BITRATE_MODE_CBR = 2;
        public static final int BITRATE_MODE_CQ = 0;
        public static final int BITRATE_MODE_VBR = 1;
        private static final Feature[] bitrates = {new Feature("VBR", 1, true), new Feature("CBR", 2, false), new Feature("CQ", 0, false)};
        private int mBitControl;
        private Range<Integer> mComplexityRange;
        private Integer mDefaultComplexity;
        private Integer mDefaultQuality;
        private CodecCapabilities mParent;
        private Range<Integer> mQualityRange;
        private String mQualityScale;

        public Range<Integer> getQualityRange() {
            return this.mQualityRange;
        }

        public Range<Integer> getComplexityRange() {
            return this.mComplexityRange;
        }

        private static int parseBitrateMode(String mode) {
            Feature[] featureArr = bitrates;
            for (Feature feat : featureArr) {
                if (feat.mName.equalsIgnoreCase(mode)) {
                    return feat.mValue;
                }
            }
            return 0;
        }

        public boolean isBitrateModeSupported(int mode) {
            for (Feature feat : bitrates) {
                if (mode == feat.mValue) {
                    if ((this.mBitControl & (1 << mode)) != 0) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }
            return false;
        }

        private EncoderCapabilities() {
        }

        public static EncoderCapabilities create(MediaFormat info, CodecCapabilities parent) {
            EncoderCapabilities caps = new EncoderCapabilities();
            caps.init(info, parent);
            return caps;
        }

        private void init(MediaFormat info, CodecCapabilities parent) {
            this.mParent = parent;
            this.mComplexityRange = Range.create(0, 0);
            this.mQualityRange = Range.create(0, 0);
            this.mBitControl = 2;
            applyLevelLimits();
            parseFromInfo(info);
        }

        private void applyLevelLimits() {
            String mime = this.mParent.getMimeType();
            if (mime.equalsIgnoreCase(MediaFormat.MIMETYPE_AUDIO_FLAC)) {
                this.mComplexityRange = Range.create(0, 8);
                this.mBitControl = 1;
            } else if (mime.equalsIgnoreCase(MediaFormat.MIMETYPE_AUDIO_AMR_NB) || mime.equalsIgnoreCase(MediaFormat.MIMETYPE_AUDIO_AMR_WB) || mime.equalsIgnoreCase(MediaFormat.MIMETYPE_AUDIO_G711_ALAW) || mime.equalsIgnoreCase(MediaFormat.MIMETYPE_AUDIO_G711_MLAW) || mime.equalsIgnoreCase(MediaFormat.MIMETYPE_AUDIO_MSGSM)) {
                this.mBitControl = 4;
            }
        }

        private void parseFromInfo(MediaFormat info) {
            Map<String, Object> map = info.getMap();
            if (info.containsKey("complexity-range")) {
                this.mComplexityRange = Utils.parseIntRange(info.getString("complexity-range"), this.mComplexityRange);
            }
            if (info.containsKey("quality-range")) {
                this.mQualityRange = Utils.parseIntRange(info.getString("quality-range"), this.mQualityRange);
            }
            if (info.containsKey("feature-bitrate-modes")) {
                String[] split = info.getString("feature-bitrate-modes").split(SmsManager.REGEX_PREFIX_DELIMITER);
                int length = split.length;
                for (int i = 0; i < length; i++) {
                    this.mBitControl |= 1 << parseBitrateMode(split[i]);
                }
            }
            try {
                this.mDefaultComplexity = Integer.valueOf(Integer.parseInt((String) map.get("complexity-default")));
            } catch (NumberFormatException e) {
            }
            try {
                this.mDefaultQuality = Integer.valueOf(Integer.parseInt((String) map.get("quality-default")));
            } catch (NumberFormatException e2) {
            }
            this.mQualityScale = (String) map.get("quality-scale");
        }

        private boolean supports(Integer complexity, Integer quality, Integer profile) {
            boolean ok = true;
            if (!(1 == 0 || complexity == null)) {
                ok = this.mComplexityRange.contains(complexity);
            }
            if (ok && quality != null) {
                ok = this.mQualityRange.contains(quality);
            }
            if (!ok || profile == null) {
                return ok;
            }
            CodecProfileLevel[] codecProfileLevelArr = this.mParent.profileLevels;
            int length = codecProfileLevelArr.length;
            boolean ok2 = false;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                } else if (codecProfileLevelArr[i].profile == profile.intValue()) {
                    profile = null;
                    break;
                } else {
                    i++;
                }
            }
            if (profile == null) {
                ok2 = true;
            }
            return ok2;
        }

        public void getDefaultFormat(MediaFormat format) {
            Integer num;
            Integer num2;
            if (!this.mQualityRange.getUpper().equals(this.mQualityRange.getLower()) && (num2 = this.mDefaultQuality) != null) {
                format.setInteger(MediaFormat.KEY_QUALITY, num2.intValue());
            }
            if (!this.mComplexityRange.getUpper().equals(this.mComplexityRange.getLower()) && (num = this.mDefaultComplexity) != null) {
                format.setInteger(MediaFormat.KEY_COMPLEXITY, num.intValue());
            }
            Feature[] featureArr = bitrates;
            for (Feature feat : featureArr) {
                if ((this.mBitControl & (1 << feat.mValue)) != 0) {
                    format.setInteger(MediaFormat.KEY_BITRATE_MODE, feat.mValue);
                    return;
                }
            }
        }

        public boolean supportsFormat(MediaFormat format) {
            Map<String, Object> map = format.getMap();
            String mime = this.mParent.getMimeType();
            Integer mode = (Integer) map.get(MediaFormat.KEY_BITRATE_MODE);
            if (mode != null && !isBitrateModeSupported(mode.intValue())) {
                return false;
            }
            Integer complexity = (Integer) map.get(MediaFormat.KEY_COMPLEXITY);
            if (MediaFormat.MIMETYPE_AUDIO_FLAC.equalsIgnoreCase(mime)) {
                Integer flacComplexity = (Integer) map.get(MediaFormat.KEY_FLAC_COMPRESSION_LEVEL);
                if (complexity == null) {
                    complexity = flacComplexity;
                } else if (flacComplexity != null && !complexity.equals(flacComplexity)) {
                    throw new IllegalArgumentException("conflicting values for complexity and flac-compression-level");
                }
            }
            Integer profile = (Integer) map.get(MediaFormat.KEY_PROFILE);
            if (MediaFormat.MIMETYPE_AUDIO_AAC.equalsIgnoreCase(mime)) {
                Integer aacProfile = (Integer) map.get(MediaFormat.KEY_AAC_PROFILE);
                if (profile == null) {
                    profile = aacProfile;
                } else if (aacProfile != null && !aacProfile.equals(profile)) {
                    throw new IllegalArgumentException("conflicting values for profile and aac-profile");
                }
            }
            return supports(complexity, (Integer) map.get(MediaFormat.KEY_QUALITY), profile);
        }
    }

    public static final class CodecProfileLevel {
        public static final int AACObjectELD = 39;
        public static final int AACObjectERLC = 17;
        public static final int AACObjectERScalable = 20;
        public static final int AACObjectHE = 5;
        public static final int AACObjectHE_PS = 29;
        public static final int AACObjectLC = 2;
        public static final int AACObjectLD = 23;
        public static final int AACObjectLTP = 4;
        public static final int AACObjectMain = 1;
        public static final int AACObjectSSR = 3;
        public static final int AACObjectScalable = 6;
        public static final int AACObjectXHE = 42;
        public static final int AV1Level2 = 1;
        public static final int AV1Level21 = 2;
        public static final int AV1Level22 = 4;
        public static final int AV1Level23 = 8;
        public static final int AV1Level3 = 16;
        public static final int AV1Level31 = 32;
        public static final int AV1Level32 = 64;
        public static final int AV1Level33 = 128;
        public static final int AV1Level4 = 256;
        public static final int AV1Level41 = 512;
        public static final int AV1Level42 = 1024;
        public static final int AV1Level43 = 2048;
        public static final int AV1Level5 = 4096;
        public static final int AV1Level51 = 8192;
        public static final int AV1Level52 = 16384;
        public static final int AV1Level53 = 32768;
        public static final int AV1Level6 = 65536;
        public static final int AV1Level61 = 131072;
        public static final int AV1Level62 = 262144;
        public static final int AV1Level63 = 524288;
        public static final int AV1Level7 = 1048576;
        public static final int AV1Level71 = 2097152;
        public static final int AV1Level72 = 4194304;
        public static final int AV1Level73 = 8388608;
        public static final int AV1ProfileMain10 = 2;
        public static final int AV1ProfileMain10HDR10 = 4096;
        public static final int AV1ProfileMain10HDR10Plus = 8192;
        public static final int AV1ProfileMain8 = 1;
        public static final int AVCLevel1 = 1;
        public static final int AVCLevel11 = 4;
        public static final int AVCLevel12 = 8;
        public static final int AVCLevel13 = 16;
        public static final int AVCLevel1b = 2;
        public static final int AVCLevel2 = 32;
        public static final int AVCLevel21 = 64;
        public static final int AVCLevel22 = 128;
        public static final int AVCLevel3 = 256;
        public static final int AVCLevel31 = 512;
        public static final int AVCLevel32 = 1024;
        public static final int AVCLevel4 = 2048;
        public static final int AVCLevel41 = 4096;
        public static final int AVCLevel42 = 8192;
        public static final int AVCLevel5 = 16384;
        public static final int AVCLevel51 = 32768;
        public static final int AVCLevel52 = 65536;
        public static final int AVCLevel6 = 131072;
        public static final int AVCLevel61 = 262144;
        public static final int AVCLevel62 = 524288;
        public static final int AVCProfileBaseline = 1;
        public static final int AVCProfileConstrainedBaseline = 65536;
        public static final int AVCProfileConstrainedHigh = 524288;
        public static final int AVCProfileExtended = 4;
        public static final int AVCProfileHigh = 8;
        public static final int AVCProfileHigh10 = 16;
        public static final int AVCProfileHigh422 = 32;
        public static final int AVCProfileHigh444 = 64;
        public static final int AVCProfileMain = 2;
        public static final int DolbyVisionLevelFhd24 = 4;
        public static final int DolbyVisionLevelFhd30 = 8;
        public static final int DolbyVisionLevelFhd60 = 16;
        public static final int DolbyVisionLevelHd24 = 1;
        public static final int DolbyVisionLevelHd30 = 2;
        public static final int DolbyVisionLevelUhd24 = 32;
        public static final int DolbyVisionLevelUhd30 = 64;
        public static final int DolbyVisionLevelUhd48 = 128;
        public static final int DolbyVisionLevelUhd60 = 256;
        public static final int DolbyVisionProfileDvavPen = 2;
        public static final int DolbyVisionProfileDvavPer = 1;
        public static final int DolbyVisionProfileDvavSe = 512;
        public static final int DolbyVisionProfileDvheDen = 8;
        public static final int DolbyVisionProfileDvheDer = 4;
        public static final int DolbyVisionProfileDvheDtb = 128;
        public static final int DolbyVisionProfileDvheDth = 64;
        public static final int DolbyVisionProfileDvheDtr = 16;
        public static final int DolbyVisionProfileDvheSt = 256;
        public static final int DolbyVisionProfileDvheStn = 32;
        public static final int H263Level10 = 1;
        public static final int H263Level20 = 2;
        public static final int H263Level30 = 4;
        public static final int H263Level40 = 8;
        public static final int H263Level45 = 16;
        public static final int H263Level50 = 32;
        public static final int H263Level60 = 64;
        public static final int H263Level70 = 128;
        public static final int H263ProfileBackwardCompatible = 4;
        public static final int H263ProfileBaseline = 1;
        public static final int H263ProfileH320Coding = 2;
        public static final int H263ProfileHighCompression = 32;
        public static final int H263ProfileHighLatency = 256;
        public static final int H263ProfileISWV2 = 8;
        public static final int H263ProfileISWV3 = 16;
        public static final int H263ProfileInterlace = 128;
        public static final int H263ProfileInternet = 64;
        public static final int HEVCHighTierLevel1 = 2;
        public static final int HEVCHighTierLevel2 = 8;
        public static final int HEVCHighTierLevel21 = 32;
        public static final int HEVCHighTierLevel3 = 128;
        public static final int HEVCHighTierLevel31 = 512;
        public static final int HEVCHighTierLevel4 = 2048;
        public static final int HEVCHighTierLevel41 = 8192;
        public static final int HEVCHighTierLevel5 = 32768;
        public static final int HEVCHighTierLevel51 = 131072;
        public static final int HEVCHighTierLevel52 = 524288;
        public static final int HEVCHighTierLevel6 = 2097152;
        public static final int HEVCHighTierLevel61 = 8388608;
        public static final int HEVCHighTierLevel62 = 33554432;
        private static final int HEVCHighTierLevels = 44739242;
        public static final int HEVCMainTierLevel1 = 1;
        public static final int HEVCMainTierLevel2 = 4;
        public static final int HEVCMainTierLevel21 = 16;
        public static final int HEVCMainTierLevel3 = 64;
        public static final int HEVCMainTierLevel31 = 256;
        public static final int HEVCMainTierLevel4 = 1024;
        public static final int HEVCMainTierLevel41 = 4096;
        public static final int HEVCMainTierLevel5 = 16384;
        public static final int HEVCMainTierLevel51 = 65536;
        public static final int HEVCMainTierLevel52 = 262144;
        public static final int HEVCMainTierLevel6 = 1048576;
        public static final int HEVCMainTierLevel61 = 4194304;
        public static final int HEVCMainTierLevel62 = 16777216;
        public static final int HEVCProfileMain = 1;
        public static final int HEVCProfileMain10 = 2;
        public static final int HEVCProfileMain10HDR10 = 4096;
        public static final int HEVCProfileMain10HDR10Plus = 8192;
        public static final int HEVCProfileMainStill = 4;
        public static final int MPEG2LevelH14 = 2;
        public static final int MPEG2LevelHL = 3;
        public static final int MPEG2LevelHP = 4;
        public static final int MPEG2LevelLL = 0;
        public static final int MPEG2LevelML = 1;
        public static final int MPEG2Profile422 = 2;
        public static final int MPEG2ProfileHigh = 5;
        public static final int MPEG2ProfileMain = 1;
        public static final int MPEG2ProfileSNR = 3;
        public static final int MPEG2ProfileSimple = 0;
        public static final int MPEG2ProfileSpatial = 4;
        public static final int MPEG4Level0 = 1;
        public static final int MPEG4Level0b = 2;
        public static final int MPEG4Level1 = 4;
        public static final int MPEG4Level2 = 8;
        public static final int MPEG4Level3 = 16;
        public static final int MPEG4Level3b = 24;
        public static final int MPEG4Level4 = 32;
        public static final int MPEG4Level4a = 64;
        public static final int MPEG4Level5 = 128;
        public static final int MPEG4Level6 = 256;
        public static final int MPEG4ProfileAdvancedCoding = 4096;
        public static final int MPEG4ProfileAdvancedCore = 8192;
        public static final int MPEG4ProfileAdvancedRealTime = 1024;
        public static final int MPEG4ProfileAdvancedScalable = 16384;
        public static final int MPEG4ProfileAdvancedSimple = 32768;
        public static final int MPEG4ProfileBasicAnimated = 256;
        public static final int MPEG4ProfileCore = 4;
        public static final int MPEG4ProfileCoreScalable = 2048;
        public static final int MPEG4ProfileHybrid = 512;
        public static final int MPEG4ProfileMain = 8;
        public static final int MPEG4ProfileNbit = 16;
        public static final int MPEG4ProfileScalableTexture = 32;
        public static final int MPEG4ProfileSimple = 1;
        public static final int MPEG4ProfileSimpleFBA = 128;
        public static final int MPEG4ProfileSimpleFace = 64;
        public static final int MPEG4ProfileSimpleScalable = 2;
        public static final int VP8Level_Version0 = 1;
        public static final int VP8Level_Version1 = 2;
        public static final int VP8Level_Version2 = 4;
        public static final int VP8Level_Version3 = 8;
        public static final int VP8ProfileMain = 1;
        public static final int VP9Level1 = 1;
        public static final int VP9Level11 = 2;
        public static final int VP9Level2 = 4;
        public static final int VP9Level21 = 8;
        public static final int VP9Level3 = 16;
        public static final int VP9Level31 = 32;
        public static final int VP9Level4 = 64;
        public static final int VP9Level41 = 128;
        public static final int VP9Level5 = 256;
        public static final int VP9Level51 = 512;
        public static final int VP9Level52 = 1024;
        public static final int VP9Level6 = 2048;
        public static final int VP9Level61 = 4096;
        public static final int VP9Level62 = 8192;
        public static final int VP9Profile0 = 1;
        public static final int VP9Profile1 = 2;
        public static final int VP9Profile2 = 4;
        public static final int VP9Profile2HDR = 4096;
        public static final int VP9Profile2HDR10Plus = 16384;
        public static final int VP9Profile3 = 8;
        public static final int VP9Profile3HDR = 8192;
        public static final int VP9Profile3HDR10Plus = 32768;
        public int level;
        public int profile;

        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof CodecProfileLevel)) {
                return false;
            }
            CodecProfileLevel other = (CodecProfileLevel) obj;
            if (other.profile == this.profile && other.level == this.level) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return Long.hashCode((((long) this.profile) << 32) | ((long) this.level));
        }
    }

    public final CodecCapabilities getCapabilitiesForType(String type) {
        CodecCapabilities caps = this.mCaps.get(type);
        if (caps != null) {
            return caps.dup();
        }
        throw new IllegalArgumentException("codec does not support type");
    }

    public MediaCodecInfo makeRegular() {
        ArrayList<CodecCapabilities> caps = new ArrayList<>();
        for (CodecCapabilities c : this.mCaps.values()) {
            if (c.isRegular()) {
                caps.add(c);
            }
        }
        if (caps.size() == 0) {
            return null;
        }
        if (caps.size() == this.mCaps.size()) {
            return this;
        }
        return new MediaCodecInfo(this.mName, this.mCanonicalName, this.mFlags, (CodecCapabilities[]) caps.toArray(new CodecCapabilities[caps.size()]));
    }
}
