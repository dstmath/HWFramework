package android.media;

import android.aps.IApsManager;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothHealth;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.fingerprint.FingerprintManager;
import android.mtp.MtpConstants;
import android.util.Log;
import android.util.Pair;
import android.util.Range;
import android.util.Rational;
import android.util.Size;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class MediaCodecInfo {
    /* access modifiers changed from: private */
    public static final Range<Integer> BITRATE_RANGE = Range.create(0, 500000000);
    private static final int DEFAULT_MAX_SUPPORTED_INSTANCES = 32;
    private static final int ERROR_NONE_SUPPORTED = 4;
    private static final int ERROR_UNRECOGNIZED = 1;
    private static final int ERROR_UNSUPPORTED = 2;
    /* access modifiers changed from: private */
    public static final Range<Integer> FRAME_RATE_RANGE = Range.create(0, 960);
    private static final int MAX_SUPPORTED_INSTANCES_LIMIT = 256;
    /* access modifiers changed from: private */
    public static final Range<Integer> POSITIVE_INTEGERS = Range.create(1, Integer.MAX_VALUE);
    /* access modifiers changed from: private */
    public static final Range<Long> POSITIVE_LONGS = Range.create(1L, Long.MAX_VALUE);
    /* access modifiers changed from: private */
    public static final Range<Rational> POSITIVE_RATIONALS = Range.create(new Rational(1, Integer.MAX_VALUE), new Rational(Integer.MAX_VALUE, 1));
    /* access modifiers changed from: private */
    public static final Range<Integer> SIZE_RANGE = Range.create(1, 32768);
    private Map<String, CodecCapabilities> mCaps = new HashMap();
    private boolean mIsEncoder;
    private String mName;

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
            return Arrays.copyOf(this.mSampleRates, this.mSampleRates.length);
        }

        public Range<Integer>[] getSupportedSampleRateRanges() {
            return (Range[]) Arrays.copyOf(this.mSampleRateRanges, this.mSampleRateRanges.length);
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
            this.mSampleRateRanges = new Range[]{Range.create(8000, 96000)};
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
            for (int i = 0; i < this.mSampleRateRanges.length; i++) {
                this.mSampleRates[i] = this.mSampleRateRanges[i].getLower().intValue();
            }
        }

        private void limitSampleRates(Range<Integer>[] rateRanges) {
            Utils.sortDistinctRanges(rateRanges);
            this.mSampleRateRanges = Utils.intersectSortedDistinctRanges(this.mSampleRateRanges, rateRanges);
            for (Range<Integer> range : this.mSampleRateRanges) {
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
            if (mime.equalsIgnoreCase(MediaFormat.MIMETYPE_AUDIO_MPEG)) {
                sampleRates = new int[]{8000, 11025, 12000, 16000, 22050, 24000, 32000, 44100, 48000};
                bitRates = Range.create(8000, 320000);
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
                sampleRates = new int[]{7350, 8000, 11025, 12000, 16000, 22050, 24000, 32000, 44100, 48000, 64000, 88200, 96000};
                bitRates = Range.create(8000, 510000);
                maxChannels = 48;
            } else if (mime.equalsIgnoreCase(MediaFormat.MIMETYPE_AUDIO_VORBIS)) {
                bitRates = Range.create(32000, 500000);
                sampleRateRange = Range.create(8000, Integer.valueOf(AudioFormat.SAMPLE_RATE_HZ_MAX));
                maxChannels = 255;
            } else if (mime.equalsIgnoreCase(MediaFormat.MIMETYPE_AUDIO_OPUS)) {
                bitRates = Range.create(Integer.valueOf(BluetoothHealth.HEALTH_OPERATION_SUCCESS), 510000);
                sampleRates = new int[]{8000, 12000, 16000, 24000, 48000};
                maxChannels = 255;
            } else if (mime.equalsIgnoreCase(MediaFormat.MIMETYPE_AUDIO_RAW)) {
                sampleRateRange = Range.create(1, 96000);
                bitRates = Range.create(1, 10000000);
                maxChannels = AudioTrack.CHANNEL_COUNT_MAX;
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
            } else {
                Log.w(TAG, "Unsupported mime " + mime);
                CodecCapabilities codecCapabilities = this.mParent;
                codecCapabilities.mError = codecCapabilities.mError | 2;
            }
            if (sampleRates != null) {
                limitSampleRates(sampleRates);
            } else if (sampleRateRange != null) {
                limitSampleRates((Range<Integer>[]) new Range[]{sampleRateRange});
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
                String[] rateStrings = info.getString("sample-rate-ranges").split(",");
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
            if (this.mSampleRates != null && this.mSampleRates.length == 1) {
                format.setInteger(MediaFormat.KEY_SAMPLE_RATE, this.mSampleRates[0]);
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
        public static final String FEATURE_IntraRefresh = "intra-refresh";
        public static final String FEATURE_PartialFrame = "partial-frame";
        public static final String FEATURE_SecurePlayback = "secure-playback";
        public static final String FEATURE_TunneledPlayback = "tunneled-playback";
        private static final String TAG = "CodecCapabilities";
        private static final Feature[] decoderFeatures = {new Feature(FEATURE_AdaptivePlayback, 1, true), new Feature(FEATURE_SecurePlayback, 2, false), new Feature(FEATURE_TunneledPlayback, 4, false), new Feature(FEATURE_PartialFrame, 8, false)};
        private static final Feature[] encoderFeatures = {new Feature(FEATURE_IntraRefresh, 1, false)};
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
            boolean z = false;
            for (Feature feat : getValidFeatures()) {
                if (feat.mName.equals(name)) {
                    if ((feat.mValue & flags) != 0) {
                        z = true;
                    }
                    return z;
                }
            }
            return false;
        }

        public boolean isRegular() {
            for (Feature feat : getValidFeatures()) {
                if (!feat.mDefault && isFeatureRequired(feat.mName)) {
                    return false;
                }
            }
            return true;
        }

        public final boolean isFormatSupported(MediaFormat format) {
            Map<String, Object> map = format.getMap();
            String mime = (String) map.get(MediaFormat.KEY_MIME);
            if (mime != null && !this.mMime.equalsIgnoreCase(mime)) {
                return false;
            }
            for (Feature feat : getValidFeatures()) {
                Integer yesNo = (Integer) map.get(MediaFormat.KEY_FEATURE_ + feat.mName);
                if (yesNo != null && ((yesNo.intValue() == 1 && !isFeatureSupported(feat.mName)) || (yesNo.intValue() == 0 && isFeatureRequired(feat.mName)))) {
                    return false;
                }
            }
            Integer profile = (Integer) map.get(MediaFormat.KEY_PROFILE);
            Integer level = (Integer) map.get(MediaFormat.KEY_LEVEL);
            if (profile != null) {
                if (!supportsProfileLevel(profile.intValue(), level)) {
                    return false;
                }
                int maxLevel = 0;
                for (CodecProfileLevel pl : this.profileLevels) {
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
            if (this.mAudioCaps != null && !this.mAudioCaps.supportsFormat(format)) {
                return false;
            }
            if (this.mVideoCaps != null && !this.mVideoCaps.supportsFormat(format)) {
                return false;
            }
            if (this.mEncoderCaps == null || this.mEncoderCaps.supportsFormat(format)) {
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
            boolean z = false;
            for (CodecProfileLevel pl : this.profileLevels) {
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
                                z = true;
                            }
                            return z;
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
            caps.profileLevels = (CodecProfileLevel[]) Arrays.copyOf(this.profileLevels, this.profileLevels.length);
            caps.colorFormats = Arrays.copyOf(this.colorFormats, this.colorFormats.length);
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
            CodecCapabilities ret = new CodecCapabilities(new CodecProfileLevel[]{pl}, new int[0], true, 0, defaultFormat, new MediaFormat());
            if (ret.mError != 0) {
                return null;
            }
            return ret;
        }

        CodecCapabilities(CodecProfileLevel[] profLevs, int[] colFmts, boolean encoder, int flags, Map<String, Object> defaultFormatMap, Map<String, Object> capabilitiesMap) {
            this(profLevs, colFmts, encoder, flags, new MediaFormat(defaultFormatMap), new MediaFormat(capabilitiesMap));
        }

        CodecCapabilities(CodecProfileLevel[] profLevs, int[] colFmts, boolean encoder, int flags, MediaFormat defaultFormat, MediaFormat info) {
            Map<String, Object> map;
            boolean z;
            MediaFormat mediaFormat = info;
            Map<String, Object> map2 = info.getMap();
            this.colorFormats = colFmts;
            this.mFlagsVerified = flags;
            this.mDefaultFormat = defaultFormat;
            this.mCapabilitiesInfo = mediaFormat;
            this.mMime = this.mDefaultFormat.getString(MediaFormat.KEY_MIME);
            CodecProfileLevel[] profLevs2 = profLevs;
            int i = 0;
            if (profLevs2.length == 0 && this.mMime.equalsIgnoreCase(MediaFormat.MIMETYPE_VIDEO_VP9)) {
                CodecProfileLevel profLev = new CodecProfileLevel();
                profLev.profile = 1;
                profLev.level = VideoCapabilities.equivalentVP9Level(info);
                profLevs2 = new CodecProfileLevel[]{profLev};
            }
            this.profileLevels = profLevs2;
            if (this.mMime.toLowerCase().startsWith("audio/")) {
                this.mAudioCaps = AudioCapabilities.create(mediaFormat, this);
                this.mAudioCaps.getDefaultFormat(this.mDefaultFormat);
            } else if (this.mMime.toLowerCase().startsWith("video/") || this.mMime.equalsIgnoreCase(MediaFormat.MIMETYPE_IMAGE_ANDROID_HEIC)) {
                this.mVideoCaps = VideoCapabilities.create(mediaFormat, this);
            }
            if (encoder) {
                this.mEncoderCaps = EncoderCapabilities.create(mediaFormat, this);
                this.mEncoderCaps.getDefaultFormat(this.mDefaultFormat);
            }
            this.mMaxSupportedInstances = Utils.parseIntSafely(MediaCodecList.getGlobalSettings().get("max-concurrent-instances"), 32);
            this.mMaxSupportedInstances = ((Integer) Range.create(1, 256).clamp(Integer.valueOf(Utils.parseIntSafely(map2.get("max-concurrent-instances"), this.mMaxSupportedInstances)))).intValue();
            Feature[] validFeatures = getValidFeatures();
            int length = validFeatures.length;
            while (i < length) {
                Feature feat = validFeatures[i];
                String key = MediaFormat.KEY_FEATURE_ + feat.mName;
                Integer yesNo = (Integer) map2.get(key);
                if (yesNo == null) {
                    map = map2;
                    z = true;
                } else {
                    if (yesNo.intValue() > 0) {
                        map = map2;
                        this.mFlagsRequired |= feat.mValue;
                    } else {
                        map = map2;
                    }
                    this.mFlagsSupported |= feat.mValue;
                    z = true;
                    this.mDefaultFormat.setInteger(key, 1);
                }
                i++;
                boolean z2 = z;
                map2 = map;
                MediaFormat mediaFormat2 = info;
            }
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
        public static final int VP9Profile3 = 8;
        public static final int VP9Profile3HDR = 8192;
        public int level;
        public int profile;

        public boolean equals(Object obj) {
            boolean z = false;
            if (obj == null || !(obj instanceof CodecProfileLevel)) {
                return false;
            }
            CodecProfileLevel other = (CodecProfileLevel) obj;
            if (other.profile == this.profile && other.level == this.level) {
                z = true;
            }
            return z;
        }

        public int hashCode() {
            return Long.hashCode((((long) this.profile) << 32) | ((long) this.level));
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
            for (Feature feat : bitrates) {
                if (feat.mName.equalsIgnoreCase(mode)) {
                    return feat.mValue;
                }
            }
            return 0;
        }

        public boolean isBitrateModeSupported(int mode) {
            for (Feature feat : bitrates) {
                if (mode == feat.mValue) {
                    boolean z = true;
                    if ((this.mBitControl & (1 << mode)) == 0) {
                        z = false;
                    }
                    return z;
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
                for (String mode : info.getString("feature-bitrate-modes").split(",")) {
                    this.mBitControl |= 1 << parseBitrateMode(mode);
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
            if (!this.mQualityRange.getUpper().equals(this.mQualityRange.getLower()) && this.mDefaultQuality != null) {
                format.setInteger(MediaFormat.KEY_QUALITY, this.mDefaultQuality.intValue());
            }
            if (!this.mComplexityRange.getUpper().equals(this.mComplexityRange.getLower()) && this.mDefaultComplexity != null) {
                format.setInteger(MediaFormat.KEY_COMPLEXITY, this.mDefaultComplexity.intValue());
            }
            for (Feature feat : bitrates) {
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

    private static class Feature {
        public boolean mDefault;
        public String mName;
        public int mValue;

        public Feature(String name, int value, boolean def) {
            this.mName = name;
            this.mValue = value;
            this.mDefault = def;
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
            if (!supports(Integer.valueOf(width), Integer.valueOf(height), null)) {
                throw new IllegalArgumentException("unsupported size");
            } else if (this.mMeasuredFrameRates != null && this.mMeasuredFrameRates.size() > 0) {
                return estimateFrameRatesFor(width, height);
            } else {
                Log.w(TAG, "Codec did not publish any measurement data.");
                return null;
            }
        }

        public boolean areSizeAndRateSupported(int width, int height, double frameRate) {
            return supports(Integer.valueOf(width), Integer.valueOf(height), Double.valueOf(frameRate));
        }

        public boolean isSizeSupported(int width, int height) {
            return supports(Integer.valueOf(width), Integer.valueOf(height), null);
        }

        private boolean supports(Integer width, Integer height, Number rate) {
            boolean ok = true;
            boolean z = false;
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
            boolean ok2 = Math.min(height.intValue(), width.intValue()) <= this.mSmallerDimensionUpperLimit;
            int widthInBlocks = Utils.divUp(width.intValue(), this.mBlockWidth);
            int heightInBlocks = Utils.divUp(height.intValue(), this.mBlockHeight);
            int blockCount = widthInBlocks * heightInBlocks;
            if (ok2 && this.mBlockCountRange.contains(Integer.valueOf(blockCount)) && this.mBlockAspectRatioRange.contains(new Rational(widthInBlocks, heightInBlocks)) && this.mAspectRatioRange.contains(new Rational(width.intValue(), height.intValue()))) {
                z = true;
            }
            boolean ok3 = z;
            if (!ok3 || rate == null) {
                return ok3;
            }
            return this.mBlocksPerSecondRange.contains(Utils.longRangeFor(((double) blockCount) * rate.doubleValue()));
        }

        public boolean supportsFormat(MediaFormat format) {
            Map<String, Object> map = format.getMap();
            if (supports((Integer) map.get(MediaFormat.KEY_WIDTH), (Integer) map.get(MediaFormat.KEY_HEIGHT), (Number) map.get(MediaFormat.KEY_FRAME_RATE)) && CodecCapabilities.supportsBitrate(this.mBitrateRange, format)) {
                return true;
            }
            return false;
        }

        private VideoCapabilities() {
        }

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

        private Map<Size, Range<Long>> getMeasuredFrameRates(Map<String, Object> map) {
            Map<Size, Range<Long>> ret = new HashMap<>();
            for (String key : map.keySet()) {
                if (key.startsWith("measured-frame-rate-")) {
                    String substring = key.substring("measured-frame-rate-".length());
                    String[] temp = key.split("-");
                    if (temp.length == 5) {
                        Size size = Utils.parseSize(temp[3], null);
                        if (size != null && size.getWidth() * size.getHeight() > 0) {
                            Range<Long> range = Utils.parseLongRange(map.get(key), null);
                            if (range != null && range.getLower().longValue() >= 0 && range.getUpper().longValue() >= 0) {
                                ret.put(size, range);
                            }
                        }
                    }
                }
            }
            return ret;
        }

        private static Pair<Range<Integer>, Range<Integer>> parseWidthHeightRanges(Object o) {
            Pair<Size, Size> range = Utils.parseSizeRange(o);
            if (range != null) {
                try {
                    return Pair.create(Range.create(Integer.valueOf(((Size) range.first).getWidth()), Integer.valueOf(((Size) range.second).getWidth())), Range.create(Integer.valueOf(((Size) range.first).getHeight()), Integer.valueOf(((Size) range.second).getHeight())));
                } catch (IllegalArgumentException e) {
                    Log.w(TAG, "could not parse size range '" + o + "'");
                }
            }
            return null;
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
            int D = dimensionRanges == null ? 0 : Math.max(((Integer) ((Range) dimensionRanges.first).getUpper()).intValue(), ((Integer) ((Range) dimensionRanges.second).getUpper()).intValue());
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

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v49, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v11, resolved type: android.util.Range} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v50, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v10, resolved type: android.util.Range} */
        /* JADX WARNING: Multi-variable type inference failed */
        /* JADX WARNING: Removed duplicated region for block: B:25:0x01c6  */
        /* JADX WARNING: Removed duplicated region for block: B:49:0x026b  */
        /* JADX WARNING: Removed duplicated region for block: B:51:0x0277  */
        /* JADX WARNING: Removed duplicated region for block: B:53:0x0283  */
        /* JADX WARNING: Removed duplicated region for block: B:55:0x028f  */
        /* JADX WARNING: Removed duplicated region for block: B:57:0x02ae  */
        /* JADX WARNING: Removed duplicated region for block: B:59:0x02ce  */
        /* JADX WARNING: Removed duplicated region for block: B:61:0x02ec  */
        /* JADX WARNING: Removed duplicated region for block: B:63:0x02f8  */
        /* JADX WARNING: Removed duplicated region for block: B:65:0x0304  */
        private void parseFromInfo(MediaFormat info) {
            Range<Integer> bitRates;
            Range<Long> blockRates;
            Range<Integer> frameRates;
            Range<Integer> heights;
            Range<Integer> widths;
            Range<Rational> ratios;
            Range<Rational> blockRatios;
            Map<String, Object> map = info.getMap();
            Size blockSize = new Size(this.mBlockWidth, this.mBlockHeight);
            Size alignment = new Size(this.mWidthAlignment, this.mHeightAlignment);
            Range<Integer> widths2 = null;
            Range<Integer> heights2 = null;
            Size blockSize2 = Utils.parseSize(map.get("block-size"), blockSize);
            Size alignment2 = Utils.parseSize(map.get("alignment"), alignment);
            Range<Integer> counts = Utils.parseIntRange(map.get("block-count-range"), null);
            Range<Long> blockRates2 = Utils.parseLongRange(map.get("blocks-per-second-range"), null);
            this.mMeasuredFrameRates = getMeasuredFrameRates(map);
            Pair<Range<Integer>, Range<Integer>> sizeRanges = parseWidthHeightRanges(map.get("size-range"));
            if (sizeRanges != null) {
                widths2 = sizeRanges.first;
                heights2 = sizeRanges.second;
            }
            if (map.containsKey("feature-can-swap-width-height")) {
                if (widths2 != null) {
                    this.mSmallerDimensionUpperLimit = Math.min(widths2.getUpper().intValue(), heights2.getUpper().intValue());
                    Range<Integer> extend = widths2.extend(heights2);
                    heights2 = extend;
                    widths2 = extend;
                } else {
                    Log.w(TAG, "feature can-swap-width-height is best used with size-range");
                    this.mSmallerDimensionUpperLimit = Math.min(this.mWidthRange.getUpper().intValue(), this.mHeightRange.getUpper().intValue());
                    Range<Integer> extend2 = this.mWidthRange.extend(this.mHeightRange);
                    this.mHeightRange = extend2;
                    this.mWidthRange = extend2;
                }
            }
            Range<Integer> range = heights2;
            Range<Integer> widths3 = widths2;
            Range<Integer> heights3 = range;
            Range<Rational> ratios2 = Utils.parseRationalRange(map.get("block-aspect-ratio-range"), null);
            Range<Rational> blockRatios2 = Utils.parseRationalRange(map.get("pixel-aspect-ratio-range"), null);
            Range<Integer> frameRates2 = Utils.parseIntRange(map.get("frame-rate-range"), null);
            if (frameRates2 != null) {
                try {
                    frameRates2 = frameRates2.intersect(MediaCodecInfo.FRAME_RATE_RANGE);
                } catch (IllegalArgumentException e) {
                    StringBuilder sb = new StringBuilder();
                    IllegalArgumentException illegalArgumentException = e;
                    sb.append("frame rate range (");
                    sb.append(frameRates2);
                    sb.append(") is out of limits: ");
                    sb.append(MediaCodecInfo.FRAME_RATE_RANGE);
                    Log.w(TAG, sb.toString());
                    frameRates2 = null;
                }
            }
            Range<Integer> frameRates3 = frameRates2;
            Range<Integer> bitRates2 = Utils.parseIntRange(map.get("bitrate-range"), null);
            if (bitRates2 != null) {
                try {
                    bitRates = bitRates2.intersect(MediaCodecInfo.BITRATE_RANGE);
                } catch (IllegalArgumentException e2) {
                    StringBuilder sb2 = new StringBuilder();
                    IllegalArgumentException illegalArgumentException2 = e2;
                    sb2.append("bitrate range (");
                    sb2.append(bitRates2);
                    sb2.append(") is out of limits: ");
                    sb2.append(MediaCodecInfo.BITRATE_RANGE);
                    Log.w(TAG, sb2.toString());
                    bitRates2 = null;
                }
                int unused = MediaCodecInfo.checkPowerOfTwo(blockSize2.getWidth(), "block-size width must be power of two");
                int unused2 = MediaCodecInfo.checkPowerOfTwo(blockSize2.getHeight(), "block-size height must be power of two");
                int unused3 = MediaCodecInfo.checkPowerOfTwo(alignment2.getWidth(), "alignment width must be power of two");
                int unused4 = MediaCodecInfo.checkPowerOfTwo(alignment2.getHeight(), "alignment height must be power of two");
                Range<Integer> heights4 = heights3;
                Range<Integer> frameRates4 = frameRates3;
                Range<Integer> widths4 = widths3;
                Pair<Range<Integer>, Range<Integer>> pair = sizeRanges;
                Range<Rational> ratios3 = ratios2;
                Range<Rational> blockRatios3 = blockRatios2;
                Map<String, Object> map2 = map;
                blockRates = blockRates2;
                applyMacroBlockLimits(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Long.MAX_VALUE, blockSize2.getWidth(), blockSize2.getHeight(), alignment2.getWidth(), alignment2.getHeight());
                if ((this.mParent.mError & 2) == 0) {
                    heights = heights4;
                    widths = widths4;
                    frameRates = frameRates4;
                    ratios = ratios3;
                    blockRatios = blockRatios3;
                } else if (this.mAllowMbOverride) {
                    heights = heights4;
                    widths = widths4;
                    frameRates = frameRates4;
                    ratios = ratios3;
                    blockRatios = blockRatios3;
                } else {
                    Range<Integer> widths5 = widths4;
                    if (widths5 != null) {
                        this.mWidthRange = this.mWidthRange.intersect(widths5);
                    }
                    Range<Integer> heights5 = heights4;
                    if (heights5 != null) {
                        this.mHeightRange = this.mHeightRange.intersect(heights5);
                    }
                    if (counts != null) {
                        this.mBlockCountRange = this.mBlockCountRange.intersect(Utils.factorRange(counts, ((this.mBlockWidth * this.mBlockHeight) / blockSize2.getWidth()) / blockSize2.getHeight()));
                    }
                    if (blockRates != null) {
                        this.mBlocksPerSecondRange = this.mBlocksPerSecondRange.intersect(Utils.factorRange(blockRates, (long) (((this.mBlockWidth * this.mBlockHeight) / blockSize2.getWidth()) / blockSize2.getHeight())));
                    }
                    Range<Rational> blockRatios4 = blockRatios3;
                    if (blockRatios4 != null) {
                        this.mBlockAspectRatioRange = this.mBlockAspectRatioRange.intersect(Utils.scaleRange(blockRatios4, this.mBlockHeight / blockSize2.getHeight(), this.mBlockWidth / blockSize2.getWidth()));
                    }
                    Range<Rational> ratios4 = ratios3;
                    if (ratios4 != null) {
                        this.mAspectRatioRange = this.mAspectRatioRange.intersect(ratios4);
                    }
                    Range<Integer> frameRates5 = frameRates4;
                    if (frameRates5 != null) {
                        this.mFrameRateRange = this.mFrameRateRange.intersect(frameRates5);
                    }
                    if (bitRates != null) {
                        this.mBitrateRange = this.mBitrateRange.intersect(bitRates);
                    }
                    updateLimits();
                }
                if (widths != null) {
                    this.mWidthRange = MediaCodecInfo.SIZE_RANGE.intersect(widths);
                }
                if (heights != null) {
                    this.mHeightRange = MediaCodecInfo.SIZE_RANGE.intersect(heights);
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
                if (frameRates != null) {
                    this.mFrameRateRange = MediaCodecInfo.FRAME_RATE_RANGE.intersect(frameRates);
                }
                if (bitRates != null) {
                    if ((this.mParent.mError & 2) != 0) {
                        this.mBitrateRange = MediaCodecInfo.BITRATE_RANGE.intersect(bitRates);
                    } else {
                        this.mBitrateRange = this.mBitrateRange.intersect(bitRates);
                    }
                }
                updateLimits();
            }
            bitRates = bitRates2;
            int unused5 = MediaCodecInfo.checkPowerOfTwo(blockSize2.getWidth(), "block-size width must be power of two");
            int unused6 = MediaCodecInfo.checkPowerOfTwo(blockSize2.getHeight(), "block-size height must be power of two");
            int unused7 = MediaCodecInfo.checkPowerOfTwo(alignment2.getWidth(), "alignment width must be power of two");
            int unused8 = MediaCodecInfo.checkPowerOfTwo(alignment2.getHeight(), "alignment height must be power of two");
            Range<Integer> heights42 = heights3;
            Range<Integer> frameRates42 = frameRates3;
            Range<Integer> widths42 = widths3;
            Pair<Range<Integer>, Range<Integer>> pair2 = sizeRanges;
            Range<Rational> ratios32 = ratios2;
            Range<Rational> blockRatios32 = blockRatios2;
            Map<String, Object> map22 = map;
            blockRates = blockRates2;
            applyMacroBlockLimits(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Long.MAX_VALUE, blockSize2.getWidth(), blockSize2.getHeight(), alignment2.getWidth(), alignment2.getHeight());
            if ((this.mParent.mError & 2) == 0) {
            }
            if (widths != null) {
            }
            if (heights != null) {
            }
            if (counts != null) {
            }
            if (blockRates != null) {
            }
            if (blockRatios != null) {
            }
            if (ratios != null) {
            }
            if (frameRates != null) {
            }
            if (bitRates != null) {
            }
            updateLimits();
        }

        private void applyBlockLimits(int blockWidth, int blockHeight, Range<Integer> counts, Range<Long> rates, Range<Rational> ratios) {
            int unused = MediaCodecInfo.checkPowerOfTwo(blockWidth, "blockWidth must be a power of two");
            int unused2 = MediaCodecInfo.checkPowerOfTwo(blockHeight, "blockHeight must be a power of two");
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
            int unused = MediaCodecInfo.checkPowerOfTwo(widthAlignment, "widthAlignment must be a power of two");
            int unused2 = MediaCodecInfo.checkPowerOfTwo(heightAlignment, "heightAlignment must be a power of two");
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
            int i = maxHorizontalBlocks;
            int i2 = maxVerticalBlocks;
            applyAlignment(widthAlignment, heightAlignment);
            applyBlockLimits(blockWidth, blockHeight, Range.create(1, Integer.valueOf(maxBlocks)), Range.create(1L, Long.valueOf(maxBlocksPerSecond)), Range.create(new Rational(1, i2), new Rational(i, 1)));
            this.mHorizontalBlockRange = this.mHorizontalBlockRange.intersect(Integer.valueOf(Utils.divUp(minHorizontalBlocks, this.mBlockWidth / blockWidth)), Integer.valueOf(i / (this.mBlockWidth / blockWidth)));
            this.mVerticalBlockRange = this.mVerticalBlockRange.intersect(Integer.valueOf(Utils.divUp(minVerticalBlocks, this.mBlockHeight / blockHeight)), Integer.valueOf(i2 / (this.mBlockHeight / blockHeight)));
        }

        /* JADX WARNING: Code restructure failed: missing block: B:124:0x0589, code lost:
            r35 = r0;
            r36 = r11;
            r37 = r14;
            r6 = r18;
            r7 = r19;
            r14 = r20;
            r0 = r21;
            r11 = r22;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:131:0x05eb, code lost:
            if (r24 == false) goto L_0x05ef;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:132:0x05ed, code lost:
            r17 = r17 & -5;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:133:0x05ef, code lost:
            r38 = r13;
            r4 = java.lang.Math.max((long) r1, r4);
            r8 = java.lang.Math.max(r6, r8);
            r2 = java.lang.Math.max(r7 * 1000, r2);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:134:0x0600, code lost:
            if (r23 == false) goto L_0x0610;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:135:0x0602, code lost:
            r12 = java.lang.Math.max(r0, r15);
            r9 = java.lang.Math.max(r11, r9);
            r3 = java.lang.Math.max(r14, r3);
            r15 = r12;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:136:0x0610, code lost:
            r12 = (int) java.lang.Math.sqrt((double) (r6 * 2));
            r13 = java.lang.Math.max(r12, r15);
            r9 = java.lang.Math.max(r12, r9);
            r3 = java.lang.Math.max(java.lang.Math.max(r14, 60), r3);
            r15 = r13;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:137:0x062c, code lost:
            r10 = r10 + 1;
            r11 = r36;
            r14 = r37;
            r13 = r38;
            r12 = r54;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:59:0x0252, code lost:
            r29 = r0;
            r30 = r13;
            r31 = r14;
            r8 = 0;
            r11 = 0;
            r14 = 0;
            r0 = 0;
            r13 = 0;
            r1 = r27;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:73:0x0352, code lost:
            r29 = r0;
            r30 = r13;
            r31 = r14;
            r8 = r16;
            r11 = r18;
            r14 = r19;
            r0 = r20;
            r13 = r21;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:74:0x0362, code lost:
            if (r22 == false) goto L_0x0368;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:75:0x0364, code lost:
            r17 = r17 & -5;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:76:0x0368, code lost:
            r4 = java.lang.Math.max((long) r1, r4);
            r6 = java.lang.Math.max(r8, r6);
            r2 = java.lang.Math.max(r11 * 1000, r2);
            r9 = java.lang.Math.max(r0, r9);
            r7 = java.lang.Math.max(r13, r7);
            r3 = java.lang.Math.max(r14, r3);
            r10 = r10 + 1;
            r8 = r28;
            r13 = r30;
            r14 = r31;
            r15 = r15;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:85:0x0438, code lost:
            r35 = r0;
            r36 = r11;
            r37 = r14;
            r6 = 0;
            r7 = 0;
            r14 = 0;
            r0 = 0;
            r11 = 0;
            r1 = r34;
         */
        /* JADX WARNING: Removed duplicated region for block: B:288:0x01a7 A[SYNTHETIC] */
        /* JADX WARNING: Removed duplicated region for block: B:49:0x01a3  */
        private void applyLevelLimits() {
            int errors;
            int maxDPBBlocks;
            int i;
            long SR;
            int FS;
            CodecProfileLevel[] profileLevels;
            int FR;
            int minH;
            int minW;
            int MBPS;
            int H;
            int W;
            int FR2;
            int BR;
            int FS2;
            int MBPS2;
            int errors2;
            int i2;
            int MBPS3;
            int H2;
            int W2;
            int FR3;
            int BR2;
            int FS3;
            int MBPS4;
            int i3;
            int BR3;
            VideoCapabilities videoCapabilities = this;
            int maxDPBBlocks2 = 0;
            CodecProfileLevel[] profileLevels2 = videoCapabilities.mParent.profileLevels;
            String mime = videoCapabilities.mParent.getMimeType();
            int i4 = 4;
            if (mime.equalsIgnoreCase(MediaFormat.MIMETYPE_VIDEO_AVC)) {
                int maxDPBBlocks3 = profileLevels2.length;
                errors = 4;
                int i5 = 0;
                int maxBlocks = 99;
                int maxBps = 64000;
                long maxBlocksPerSecond = 1485;
                int maxDPBBlocks4 = 396;
                while (i5 < maxDPBBlocks3) {
                    CodecProfileLevel profileLevel = profileLevels2[i5];
                    int MBPS5 = 0;
                    int FS4 = 0;
                    int BR4 = 0;
                    int DPB = 0;
                    boolean supported = true;
                    switch (profileLevel.level) {
                        case 1:
                            MBPS5 = 1485;
                            FS4 = 99;
                            BR4 = 64;
                            DPB = 396;
                            break;
                        case 2:
                            MBPS5 = 1485;
                            FS4 = 99;
                            BR4 = 128;
                            DPB = 396;
                            break;
                        case 4:
                            MBPS5 = FingerprintManager.HW_FINGERPRINT_ACQUIRED_VENDOR_BASE_END;
                            FS4 = 396;
                            BR4 = 192;
                            DPB = 900;
                            break;
                        case 8:
                            MBPS5 = BluetoothHealth.HEALTH_OPERATION_SUCCESS;
                            FS4 = 396;
                            BR4 = 384;
                            DPB = 2376;
                            break;
                        case 16:
                            MBPS5 = 11880;
                            FS4 = 396;
                            BR4 = 768;
                            DPB = 2376;
                            break;
                        case 32:
                            MBPS5 = 11880;
                            FS4 = 396;
                            BR4 = 2000;
                            DPB = 2376;
                            break;
                        case 64:
                            MBPS5 = 19800;
                            FS4 = 792;
                            BR4 = 4000;
                            DPB = 4752;
                            break;
                        case 128:
                            MBPS5 = 20250;
                            FS4 = 1620;
                            BR4 = 4000;
                            DPB = 8100;
                            break;
                        case 256:
                            MBPS5 = 40500;
                            FS4 = 1620;
                            BR4 = 10000;
                            DPB = 8100;
                            break;
                        case 512:
                            MBPS5 = 108000;
                            FS4 = 3600;
                            BR4 = 14000;
                            DPB = 18000;
                            break;
                        case 1024:
                            MBPS5 = 216000;
                            FS4 = 5120;
                            BR4 = HwMediaMonitorUtils.TYPE_MEDIA_RECORD_DTS_COUNT;
                            DPB = MtpConstants.DEVICE_PROPERTY_UNDEFINED;
                            break;
                        case 2048:
                            MBPS5 = 245760;
                            FS4 = 8192;
                            BR4 = HwMediaMonitorUtils.TYPE_MEDIA_RECORD_DTS_COUNT;
                            DPB = 32768;
                            break;
                        case 4096:
                            MBPS5 = 245760;
                            FS4 = 8192;
                            BR4 = SQLiteDatabase.SQLITE_MAX_LIKE_PATTERN_LENGTH;
                            DPB = 32768;
                            break;
                        case 8192:
                            MBPS5 = 522240;
                            FS4 = 8704;
                            BR4 = SQLiteDatabase.SQLITE_MAX_LIKE_PATTERN_LENGTH;
                            DPB = 34816;
                            break;
                        case 16384:
                            MBPS5 = 589824;
                            FS4 = 22080;
                            BR4 = 135000;
                            DPB = 110400;
                            break;
                        case 32768:
                            MBPS5 = 983040;
                            FS4 = 36864;
                            BR4 = 240000;
                            DPB = 184320;
                            break;
                        case 65536:
                            MBPS5 = 2073600;
                            FS4 = 36864;
                            BR4 = 240000;
                            DPB = 184320;
                            break;
                        default:
                            Log.w(TAG, "Unrecognized level " + profileLevel.level + " for " + mime);
                            errors |= 1;
                            break;
                    }
                    int MBPS6 = MBPS5;
                    int FS5 = FS4;
                    int BR5 = BR4;
                    int DPB2 = DPB;
                    int i6 = profileLevel.profile;
                    if (i6 != i4) {
                        if (i6 != 8) {
                            if (i6 == 16) {
                                i3 = maxDPBBlocks3;
                                BR3 = BR5 * FingerprintManager.HW_FINGERPRINT_ACQUIRED_VENDOR_BASE_END;
                            } else if (!(i6 == 32 || i6 == 64)) {
                                if (i6 != 65536) {
                                    if (i6 != 524288) {
                                        switch (i6) {
                                            case 1:
                                            case 2:
                                                break;
                                            default:
                                                StringBuilder sb = new StringBuilder();
                                                i3 = maxDPBBlocks3;
                                                sb.append("Unrecognized profile ");
                                                sb.append(profileLevel.profile);
                                                sb.append(" for ");
                                                sb.append(mime);
                                                Log.w(TAG, sb.toString());
                                                errors |= 1;
                                                BR3 = BR5 * 1000;
                                                break;
                                        }
                                    }
                                }
                                i3 = maxDPBBlocks3;
                                BR3 = BR5 * 1000;
                            }
                            if (supported) {
                                errors &= -5;
                            }
                            maxBlocksPerSecond = Math.max((long) MBPS6, maxBlocksPerSecond);
                            maxBlocks = Math.max(FS5, maxBlocks);
                            maxBps = Math.max(BR3, maxBps);
                            maxDPBBlocks4 = Math.max(maxDPBBlocks4, DPB2);
                            i5++;
                            maxDPBBlocks3 = i3;
                            profileLevels2 = profileLevels2;
                            i4 = 4;
                        }
                        i3 = maxDPBBlocks3;
                        BR3 = BR5 * 1250;
                        if (supported) {
                        }
                        maxBlocksPerSecond = Math.max((long) MBPS6, maxBlocksPerSecond);
                        maxBlocks = Math.max(FS5, maxBlocks);
                        maxBps = Math.max(BR3, maxBps);
                        maxDPBBlocks4 = Math.max(maxDPBBlocks4, DPB2);
                        i5++;
                        maxDPBBlocks3 = i3;
                        profileLevels2 = profileLevels2;
                        i4 = 4;
                    }
                    i3 = maxDPBBlocks3;
                    Log.w(TAG, "Unsupported profile " + profileLevel.profile + " for " + mime);
                    errors |= 2;
                    supported = false;
                    BR3 = BR5 * 1000;
                    if (supported) {
                    }
                    maxBlocksPerSecond = Math.max((long) MBPS6, maxBlocksPerSecond);
                    maxBlocks = Math.max(FS5, maxBlocks);
                    maxBps = Math.max(BR3, maxBps);
                    maxDPBBlocks4 = Math.max(maxDPBBlocks4, DPB2);
                    i5++;
                    maxDPBBlocks3 = i3;
                    profileLevels2 = profileLevels2;
                    i4 = 4;
                }
                int maxLengthInBlocks = (int) Math.sqrt((double) (maxBlocks * 8));
                long maxBlocksPerSecond2 = maxBlocksPerSecond;
                int i7 = maxBlocks;
                videoCapabilities.applyMacroBlockLimits(maxLengthInBlocks, maxLengthInBlocks, maxBlocks, maxBlocksPerSecond2, 16, 16, 1, 1);
                int i8 = maxDPBBlocks4;
                maxDPBBlocks = maxBps;
                CodecProfileLevel[] codecProfileLevelArr = profileLevels2;
                long j = maxBlocksPerSecond2;
                String str = mime;
            } else {
                CodecProfileLevel[] profileLevels3 = profileLevels2;
                if (mime.equalsIgnoreCase(MediaFormat.MIMETYPE_VIDEO_MPEG2)) {
                    CodecProfileLevel[] profileLevels4 = profileLevels3;
                    int length = profileLevels4.length;
                    errors = 4;
                    int maxWidth = 11;
                    int i9 = 0;
                    long maxBlocksPerSecond3 = 1485;
                    int maxBlocks2 = 99;
                    int maxBps2 = 64000;
                    int maxRate = 15;
                    int maxHeight = 9;
                    while (i9 < length) {
                        CodecProfileLevel profileLevel2 = profileLevels4[i9];
                        boolean supported2 = true;
                        switch (profileLevel2.profile) {
                            case 0:
                                MBPS4 = 0;
                                i2 = length;
                                if (profileLevel2.level == 1) {
                                    FR3 = 30;
                                    W2 = 45;
                                    H2 = 36;
                                    MBPS3 = 40500;
                                    FS3 = 1620;
                                    BR2 = 15000;
                                    break;
                                } else {
                                    Log.w(TAG, "Unrecognized profile/level " + profileLevel2.profile + "/" + profileLevel2.level + " for " + mime);
                                    errors |= 1;
                                    break;
                                }
                            case 1:
                                MBPS4 = 0;
                                i2 = length;
                                switch (profileLevel2.level) {
                                    case 0:
                                        FR3 = 30;
                                        W2 = 22;
                                        H2 = 18;
                                        MBPS3 = 11880;
                                        FS3 = 396;
                                        BR2 = 4000;
                                        break;
                                    case 1:
                                        FR3 = 30;
                                        W2 = 45;
                                        H2 = 36;
                                        MBPS3 = 40500;
                                        FS3 = 1620;
                                        BR2 = 15000;
                                        break;
                                    case 2:
                                        FR3 = 60;
                                        W2 = 90;
                                        H2 = 68;
                                        MBPS3 = 183600;
                                        FS3 = 6120;
                                        BR2 = 60000;
                                        break;
                                    case 3:
                                        FR3 = 60;
                                        W2 = 120;
                                        H2 = 68;
                                        MBPS3 = 244800;
                                        FS3 = 8160;
                                        BR2 = 80000;
                                        break;
                                    case 4:
                                        FR3 = 60;
                                        W2 = 120;
                                        H2 = 68;
                                        MBPS3 = 489600;
                                        FS3 = 8160;
                                        BR2 = 80000;
                                        break;
                                    default:
                                        Log.w(TAG, "Unrecognized profile/level " + profileLevel2.profile + "/" + profileLevel2.level + " for " + mime);
                                        errors |= 1;
                                        break;
                                }
                            case 2:
                            case 3:
                            case 4:
                            case 5:
                                MBPS4 = 0;
                                StringBuilder sb2 = new StringBuilder();
                                i2 = length;
                                sb2.append("Unsupported profile ");
                                sb2.append(profileLevel2.profile);
                                sb2.append(" for ");
                                sb2.append(mime);
                                Log.i(TAG, sb2.toString());
                                errors |= 2;
                                supported2 = false;
                                break;
                            default:
                                MBPS4 = 0;
                                i2 = length;
                                Log.w(TAG, "Unrecognized profile " + profileLevel2.profile + " for " + mime);
                                errors |= 1;
                                break;
                        }
                    }
                    long j2 = maxBlocksPerSecond3;
                    int i10 = maxBlocks2;
                    int i11 = maxHeight;
                    int i12 = maxWidth;
                    videoCapabilities.applyMacroBlockLimits(maxWidth, maxHeight, maxBlocks2, maxBlocksPerSecond3, 16, 16, 1, 1);
                    videoCapabilities.mFrameRateRange = videoCapabilities.mFrameRateRange.intersect(12, Integer.valueOf(maxRate));
                    maxDPBBlocks = maxBps2;
                    CodecProfileLevel[] codecProfileLevelArr2 = profileLevels4;
                    String str2 = mime;
                } else {
                    CodecProfileLevel[] profileLevels5 = profileLevels3;
                    String mime2 = mime;
                    if (mime2.equalsIgnoreCase(MediaFormat.MIMETYPE_VIDEO_MPEG4)) {
                        CodecProfileLevel[] profileLevels6 = profileLevels5;
                        int H3 = profileLevels6.length;
                        errors = 4;
                        int maxWidth2 = 11;
                        int i13 = 0;
                        long maxBlocksPerSecond4 = 1485;
                        int maxBlocks3 = 99;
                        int maxBps3 = 64000;
                        int maxRate2 = 15;
                        int maxHeight2 = 9;
                        while (i13 < H3) {
                            CodecProfileLevel profileLevel3 = profileLevels6[i13];
                            boolean strict = false;
                            boolean supported3 = true;
                            switch (profileLevel3.profile) {
                                case 1:
                                    MBPS2 = 0;
                                    int MBPS7 = profileLevel3.level;
                                    if (MBPS7 != 4) {
                                        if (MBPS7 != 8) {
                                            if (MBPS7 != 16) {
                                                if (MBPS7 != 64) {
                                                    if (MBPS7 != 128) {
                                                        if (MBPS7 == 256) {
                                                            FR2 = 30;
                                                            W = 80;
                                                            H = 45;
                                                            MBPS = 108000;
                                                            FS2 = 3600;
                                                            BR = 12000;
                                                            break;
                                                        } else {
                                                            switch (MBPS7) {
                                                                case 1:
                                                                    strict = true;
                                                                    FR2 = 15;
                                                                    W = 11;
                                                                    H = 9;
                                                                    MBPS = 1485;
                                                                    FS2 = 99;
                                                                    BR = 64;
                                                                    break;
                                                                case 2:
                                                                    strict = true;
                                                                    FR2 = 15;
                                                                    W = 11;
                                                                    H = 9;
                                                                    MBPS = 1485;
                                                                    FS2 = 99;
                                                                    BR = 128;
                                                                    break;
                                                                default:
                                                                    Log.w(TAG, "Unrecognized profile/level " + profileLevel3.profile + "/" + profileLevel3.level + " for " + mime2);
                                                                    errors2 = errors | 1;
                                                                    break;
                                                            }
                                                        }
                                                    } else {
                                                        FR2 = 30;
                                                        W = 45;
                                                        H = 36;
                                                        MBPS = 40500;
                                                        FS2 = 1620;
                                                        BR = 8000;
                                                        break;
                                                    }
                                                } else {
                                                    FR2 = 30;
                                                    W = 40;
                                                    H = 30;
                                                    MBPS = 36000;
                                                    FS2 = 1200;
                                                    BR = 4000;
                                                    break;
                                                }
                                            } else {
                                                FR2 = 30;
                                                W = 22;
                                                H = 18;
                                                MBPS = 11880;
                                                FS2 = 396;
                                                BR = 384;
                                                break;
                                            }
                                        } else {
                                            FR2 = 30;
                                            W = 22;
                                            H = 18;
                                            MBPS = 5940;
                                            FS2 = 396;
                                            BR = 128;
                                            break;
                                        }
                                    } else {
                                        FR2 = 30;
                                        W = 11;
                                        H = 9;
                                        MBPS = 1485;
                                        FS2 = 99;
                                        BR = 64;
                                        break;
                                    }
                                case 2:
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
                                    MBPS2 = 0;
                                    Log.i(TAG, "Unsupported profile " + profileLevel3.profile + " for " + mime2);
                                    errors2 = errors | 2;
                                    supported3 = false;
                                    break;
                                case 32768:
                                    int FS6 = profileLevel3.level;
                                    if (FS6 != 1 && FS6 != 4) {
                                        if (FS6 != 8) {
                                            if (FS6 != 16) {
                                                if (FS6 != 24) {
                                                    if (FS6 != 32) {
                                                        if (FS6 == 128) {
                                                            FR2 = 30;
                                                            W = 45;
                                                            H = 36;
                                                            MBPS = 48600;
                                                            FS2 = 1620;
                                                            BR = 8000;
                                                            break;
                                                        } else {
                                                            StringBuilder sb3 = new StringBuilder();
                                                            MBPS2 = 0;
                                                            sb3.append("Unrecognized profile/level ");
                                                            sb3.append(profileLevel3.profile);
                                                            sb3.append("/");
                                                            sb3.append(profileLevel3.level);
                                                            sb3.append(" for ");
                                                            sb3.append(mime2);
                                                            Log.w(TAG, sb3.toString());
                                                            errors2 = errors | 1;
                                                            break;
                                                        }
                                                    } else {
                                                        FR2 = 30;
                                                        W = 44;
                                                        H = 36;
                                                        MBPS = 23760;
                                                        FS2 = 792;
                                                        BR = FingerprintManager.HW_FINGERPRINT_ACQUIRED_VENDOR_BASE_END;
                                                        break;
                                                    }
                                                } else {
                                                    FR2 = 30;
                                                    W = 22;
                                                    H = 18;
                                                    MBPS = 11880;
                                                    FS2 = 396;
                                                    BR = 1500;
                                                    break;
                                                }
                                            } else {
                                                FR2 = 30;
                                                W = 22;
                                                H = 18;
                                                MBPS = 11880;
                                                FS2 = 396;
                                                BR = 768;
                                                break;
                                            }
                                        } else {
                                            FR2 = 30;
                                            W = 22;
                                            H = 18;
                                            MBPS = 5940;
                                            FS2 = 396;
                                            BR = 384;
                                            break;
                                        }
                                    } else {
                                        FR2 = 30;
                                        W = 11;
                                        H = 9;
                                        MBPS = 2970;
                                        FS2 = 99;
                                        BR = 128;
                                        break;
                                    }
                                    break;
                                default:
                                    MBPS2 = 0;
                                    Log.w(TAG, "Unrecognized profile " + profileLevel3.profile + " for " + mime2);
                                    errors2 = errors | 1;
                                    break;
                            }
                        }
                        long j3 = maxBlocksPerSecond4;
                        int i14 = maxBlocks3;
                        int i15 = maxHeight2;
                        applyMacroBlockLimits(maxWidth2, maxHeight2, maxBlocks3, maxBlocksPerSecond4, 16, 16, 1, 1);
                        this.mFrameRateRange = this.mFrameRateRange.intersect(12, Integer.valueOf(maxRate2));
                        maxDPBBlocks = maxBps3;
                        CodecProfileLevel[] codecProfileLevelArr3 = profileLevels6;
                        String str3 = mime2;
                    } else {
                        CodecProfileLevel[] profileLevels7 = profileLevels5;
                        if (mime2.equalsIgnoreCase(MediaFormat.MIMETYPE_VIDEO_H263)) {
                            int maxHeight3 = 9;
                            int maxWidth3 = 11;
                            int minH2 = 9;
                            CodecProfileLevel[] profileLevels8 = profileLevels7;
                            int length2 = profileLevels8.length;
                            int errors3 = 4;
                            int minAlignment = 16;
                            int minAlignment2 = 11;
                            long maxBlocksPerSecond5 = 1485;
                            int minW2 = 0;
                            int maxBlocks4 = 99;
                            int maxBps4 = 64000;
                            int maxRate3 = 15;
                            while (minW2 < length2) {
                                CodecProfileLevel profileLevel4 = profileLevels8[minW2];
                                int MBPS8 = 0;
                                int BR6 = 0;
                                int FR4 = 0;
                                int W3 = 0;
                                int H4 = 0;
                                int minW3 = minAlignment2;
                                int minH3 = minH2;
                                boolean strict2 = false;
                                int i16 = profileLevel4.level;
                                int i17 = length2;
                                if (i16 == 4) {
                                    profileLevels = profileLevels8;
                                    strict2 = true;
                                    FR4 = 30;
                                    W3 = 22;
                                    H4 = 18;
                                    BR6 = 6;
                                    MBPS8 = 22 * 18 * 30;
                                } else if (i16 == 8) {
                                    profileLevels = profileLevels8;
                                    strict2 = true;
                                    FR4 = 30;
                                    W3 = 22;
                                    H4 = 18;
                                    BR6 = 32;
                                    MBPS8 = 22 * 18 * 30;
                                } else if (i16 == 16) {
                                    profileLevels = profileLevels8;
                                    strict2 = profileLevel4.profile == 1 || profileLevel4.profile == 4;
                                    if (!strict2) {
                                        minW3 = 1;
                                        minH3 = 1;
                                        minAlignment = 4;
                                    }
                                    FR4 = 15;
                                    W3 = 11;
                                    H4 = 9;
                                    BR6 = 2;
                                    MBPS8 = 11 * 9 * 15;
                                } else if (i16 == 32) {
                                    profileLevels = profileLevels8;
                                    minW3 = 1;
                                    minH3 = 1;
                                    minAlignment = 4;
                                    FR4 = 60;
                                    W3 = 22;
                                    H4 = 18;
                                    BR6 = 64;
                                    MBPS8 = 22 * 18 * 50;
                                } else if (i16 == 64) {
                                    profileLevels = profileLevels8;
                                    minW3 = 1;
                                    minH3 = 1;
                                    minAlignment = 4;
                                    FR4 = 60;
                                    W3 = 45;
                                    H4 = 18;
                                    BR6 = 128;
                                    MBPS8 = 45 * 18 * 50;
                                } else if (i16 != 128) {
                                    switch (i16) {
                                        case 1:
                                            profileLevels = profileLevels8;
                                            strict2 = true;
                                            FR4 = 15;
                                            W3 = 11;
                                            H4 = 9;
                                            BR6 = 1;
                                            MBPS8 = 11 * 9 * 15;
                                            break;
                                        case 2:
                                            profileLevels = profileLevels8;
                                            strict2 = true;
                                            FR4 = 30;
                                            W3 = 22;
                                            H4 = 18;
                                            BR6 = 2;
                                            MBPS8 = 22 * 18 * 15;
                                            break;
                                        default:
                                            StringBuilder sb4 = new StringBuilder();
                                            profileLevels = profileLevels8;
                                            sb4.append("Unrecognized profile/level ");
                                            sb4.append(profileLevel4.profile);
                                            sb4.append("/");
                                            sb4.append(profileLevel4.level);
                                            sb4.append(" for ");
                                            sb4.append(mime2);
                                            Log.w(TAG, sb4.toString());
                                            errors |= 1;
                                            break;
                                    }
                                } else {
                                    profileLevels = profileLevels8;
                                    minW3 = 1;
                                    minH3 = 1;
                                    minAlignment = 4;
                                    FR4 = 60;
                                    W3 = 45;
                                    H4 = 36;
                                    BR6 = 256;
                                    MBPS8 = 45 * 36 * 50;
                                }
                                int i18 = minW2;
                                int MBPS9 = MBPS8;
                                int FR5 = FR4;
                                int W4 = W3;
                                int H5 = H4;
                                int minHeight = minH2;
                                int minHeight2 = profileLevel4.profile;
                                int minWidth = minAlignment2;
                                if (!(minHeight2 == 4 || minHeight2 == 8 || minHeight2 == 16 || minHeight2 == 32 || minHeight2 == 64 || minHeight2 == 128 || minHeight2 == 256)) {
                                    switch (minHeight2) {
                                        case 1:
                                        case 2:
                                            break;
                                        default:
                                            StringBuilder sb5 = new StringBuilder();
                                            FR = FR5;
                                            sb5.append("Unrecognized profile ");
                                            sb5.append(profileLevel4.profile);
                                            sb5.append(" for ");
                                            sb5.append(mime2);
                                            Log.w(TAG, sb5.toString());
                                            errors |= 1;
                                            break;
                                    }
                                }
                                FR = FR5;
                                if (strict2) {
                                    minW = 11;
                                    minH = 9;
                                } else {
                                    videoCapabilities.mAllowMbOverride = true;
                                    minW = minW3;
                                    minH = minH3;
                                }
                                errors3 = errors & -5;
                                maxBlocksPerSecond5 = Math.max((long) MBPS9, maxBlocksPerSecond5);
                                maxBlocks4 = Math.max(W4 * H5, maxBlocks4);
                                maxBps4 = Math.max(64000 * BR6, maxBps4);
                                maxWidth3 = Math.max(W4, maxWidth3);
                                maxHeight3 = Math.max(H5, maxHeight3);
                                maxRate3 = Math.max(FR, maxRate3);
                                int minWidth2 = Math.min(minW, minWidth);
                                int i19 = MBPS9;
                                minH2 = Math.min(minH, minHeight);
                                minW2 = i18 + 1;
                                minAlignment2 = minWidth2;
                                length2 = i17;
                                profileLevels8 = profileLevels;
                                videoCapabilities = this;
                            }
                            int minHeight3 = minH2;
                            int minWidth3 = minAlignment2;
                            CodecProfileLevel[] profileLevels9 = profileLevels8;
                            if (!this.mAllowMbOverride) {
                                this.mBlockAspectRatioRange = Range.create(new Rational(11, 9), new Rational(11, 9));
                            }
                            long maxBlocksPerSecond6 = maxBlocksPerSecond5;
                            int i20 = maxHeight3;
                            int i21 = maxWidth3;
                            int i22 = minHeight3;
                            int i23 = minWidth3;
                            applyMacroBlockLimits(minWidth3, minHeight3, maxWidth3, maxHeight3, maxBlocks4, maxBlocksPerSecond6, 16, 16, minAlignment, minAlignment);
                            this.mFrameRateRange = Range.create(1, Integer.valueOf(maxRate3));
                            maxDPBBlocks = maxBps4;
                            long j4 = maxBlocksPerSecond6;
                            int i24 = maxBlocks4;
                            CodecProfileLevel[] codecProfileLevelArr4 = profileLevels9;
                        } else {
                            VideoCapabilities videoCapabilities2 = videoCapabilities;
                            CodecProfileLevel[] profileLevels10 = profileLevels7;
                            if (mime2.equalsIgnoreCase(MediaFormat.MIMETYPE_VIDEO_VP8)) {
                                maxDPBBlocks = 100000000;
                                CodecProfileLevel[] profileLevels11 = profileLevels10;
                                int errors4 = 4;
                                for (CodecProfileLevel profileLevel5 : profileLevels11) {
                                    int i25 = profileLevel5.level;
                                    if (!(i25 == 4 || i25 == 8)) {
                                        switch (i25) {
                                            case 1:
                                            case 2:
                                                break;
                                            default:
                                                Log.w(TAG, "Unrecognized level " + profileLevel5.level + " for " + mime2);
                                                errors |= 1;
                                                break;
                                        }
                                    }
                                    if (profileLevel5.profile != 1) {
                                        Log.w(TAG, "Unrecognized profile " + profileLevel5.profile + " for " + mime2);
                                        errors |= 1;
                                    }
                                    errors4 = errors & -5;
                                }
                                videoCapabilities2.applyMacroBlockLimits(32767, 32767, Integer.MAX_VALUE, 2147483647L, 16, 16, 1, 1);
                                CodecProfileLevel[] codecProfileLevelArr5 = profileLevels11;
                            } else {
                                CodecProfileLevel[] profileLevels12 = profileLevels10;
                                if (mime2.equalsIgnoreCase(MediaFormat.MIMETYPE_VIDEO_VP9)) {
                                    int maxBlocks5 = 36864;
                                    int length3 = profileLevels12.length;
                                    maxDPBBlocks = 200000;
                                    int errors5 = 4;
                                    int maxDim = 512;
                                    long maxBlocksPerSecond7 = 829440;
                                    int i26 = 0;
                                    while (i26 < length3) {
                                        CodecProfileLevel profileLevel6 = profileLevels12[i26];
                                        int BR7 = 0;
                                        int D = 0;
                                        switch (profileLevel6.level) {
                                            case 1:
                                                SR = 829440;
                                                FS = 36864;
                                                BR7 = 200;
                                                D = 512;
                                                break;
                                            case 2:
                                                SR = 2764800;
                                                FS = 73728;
                                                BR7 = 800;
                                                D = 768;
                                                break;
                                            case 4:
                                                SR = 4608000;
                                                FS = 122880;
                                                BR7 = BluetoothClass.Device.WEARABLE_PAGER;
                                                D = 960;
                                                break;
                                            case 8:
                                                SR = 9216000;
                                                FS = 245760;
                                                BR7 = 3600;
                                                D = BluetoothClass.Device.PERIPHERAL_KEYBOARD;
                                                break;
                                            case 16:
                                                SR = 20736000;
                                                FS = 552960;
                                                BR7 = 7200;
                                                D = 2048;
                                                break;
                                            case 32:
                                                SR = 36864000;
                                                FS = 983040;
                                                BR7 = 12000;
                                                D = 2752;
                                                break;
                                            case 64:
                                                SR = 83558400;
                                                FS = 2228224;
                                                BR7 = 18000;
                                                D = 4160;
                                                break;
                                            case 128:
                                                SR = 160432128;
                                                FS = 2228224;
                                                BR7 = 30000;
                                                D = 4160;
                                                break;
                                            case 256:
                                                SR = 311951360;
                                                FS = 8912896;
                                                BR7 = 60000;
                                                D = 8384;
                                                break;
                                            case 512:
                                                SR = 588251136;
                                                FS = 8912896;
                                                BR7 = 120000;
                                                D = 8384;
                                                break;
                                            case 1024:
                                                SR = 1176502272;
                                                FS = 8912896;
                                                BR7 = 180000;
                                                D = 8384;
                                                break;
                                            case 2048:
                                                SR = 1176502272;
                                                FS = 35651584;
                                                BR7 = 180000;
                                                D = 16832;
                                                break;
                                            case 4096:
                                                SR = 2353004544L;
                                                FS = 35651584;
                                                BR7 = 240000;
                                                D = 16832;
                                                break;
                                            case 8192:
                                                SR = 4706009088L;
                                                FS = 35651584;
                                                BR7 = 480000;
                                                D = 16832;
                                                break;
                                            default:
                                                i = length3;
                                                Log.w(TAG, "Unrecognized level " + profileLevel6.level + " for " + mime2);
                                                errors |= 1;
                                                FS = 0;
                                                SR = 0;
                                                break;
                                        }
                                        i = length3;
                                        int i27 = profileLevel6.profile;
                                        CodecProfileLevel[] profileLevels13 = profileLevels12;
                                        if (!(i27 == 4 || i27 == 8 || i27 == 4096 || i27 == 8192)) {
                                            switch (i27) {
                                                case 1:
                                                case 2:
                                                    break;
                                                default:
                                                    Log.w(TAG, "Unrecognized profile " + profileLevel6.profile + " for " + mime2);
                                                    errors |= 1;
                                                    break;
                                            }
                                        }
                                        errors5 = errors & -5;
                                        maxBlocksPerSecond7 = Math.max(SR, maxBlocksPerSecond7);
                                        maxBlocks5 = Math.max(FS, maxBlocks5);
                                        maxDPBBlocks = Math.max(BR7 * 1000, maxDPBBlocks);
                                        maxDim = Math.max(D, maxDim);
                                        i26++;
                                        length3 = i;
                                        profileLevels12 = profileLevels13;
                                    }
                                    int maxLengthInBlocks2 = Utils.divUp(maxDim, 8);
                                    int maxBlocks6 = Utils.divUp(maxBlocks5, 64);
                                    applyMacroBlockLimits(maxLengthInBlocks2, maxLengthInBlocks2, maxBlocks6, Utils.divUp(maxBlocksPerSecond7, 64), 8, 8, 1, 1);
                                    int i28 = maxBlocks6;
                                    CodecProfileLevel[] codecProfileLevelArr6 = profileLevels12;
                                } else {
                                    CodecProfileLevel[] profileLevels14 = profileLevels12;
                                    if (mime2.equalsIgnoreCase(MediaFormat.MIMETYPE_VIDEO_HEVC)) {
                                        long maxBlocksPerSecond8 = (long) (576 * 15);
                                        int maxBlocks7 = 576;
                                        int maxBps5 = 128000;
                                        int errors6 = 4;
                                        for (CodecProfileLevel profileLevel7 : profileLevels14) {
                                            double FR6 = 0.0d;
                                            int FS7 = 0;
                                            int BR8 = 0;
                                            switch (profileLevel7.level) {
                                                case 1:
                                                case 2:
                                                    FR6 = 15.0d;
                                                    FS7 = 36864;
                                                    BR8 = 128;
                                                    break;
                                                case 4:
                                                case 8:
                                                    FR6 = 30.0d;
                                                    FS7 = 122880;
                                                    BR8 = 1500;
                                                    break;
                                                case 16:
                                                case 32:
                                                    FR6 = 30.0d;
                                                    FS7 = 245760;
                                                    BR8 = FingerprintManager.HW_FINGERPRINT_ACQUIRED_VENDOR_BASE_END;
                                                    break;
                                                case 64:
                                                case 128:
                                                    FR6 = 30.0d;
                                                    FS7 = 552960;
                                                    BR8 = BluetoothHealth.HEALTH_OPERATION_SUCCESS;
                                                    break;
                                                case 256:
                                                case 512:
                                                    FR6 = 33.75d;
                                                    FS7 = 983040;
                                                    BR8 = 10000;
                                                    break;
                                                case 1024:
                                                    FR6 = 30.0d;
                                                    FS7 = 2228224;
                                                    BR8 = 12000;
                                                    break;
                                                case 2048:
                                                    FR6 = 30.0d;
                                                    FS7 = 2228224;
                                                    BR8 = 30000;
                                                    break;
                                                case 4096:
                                                    FR6 = 60.0d;
                                                    FS7 = 2228224;
                                                    BR8 = HwMediaMonitorUtils.TYPE_MEDIA_RECORD_DTS_COUNT;
                                                    break;
                                                case 8192:
                                                    FR6 = 60.0d;
                                                    FS7 = 2228224;
                                                    BR8 = SQLiteDatabase.SQLITE_MAX_LIKE_PATTERN_LENGTH;
                                                    break;
                                                case 16384:
                                                    FR6 = 30.0d;
                                                    FS7 = 8912896;
                                                    BR8 = 25000;
                                                    break;
                                                case 32768:
                                                    FR6 = 30.0d;
                                                    FS7 = 8912896;
                                                    BR8 = IApsManager.APS_CALLBACK_ENLARGE_FACTOR;
                                                    break;
                                                case 65536:
                                                    FR6 = 60.0d;
                                                    FS7 = 8912896;
                                                    BR8 = 40000;
                                                    break;
                                                case 131072:
                                                    FR6 = 60.0d;
                                                    FS7 = 8912896;
                                                    BR8 = 160000;
                                                    break;
                                                case 262144:
                                                    FR6 = 120.0d;
                                                    FS7 = 8912896;
                                                    BR8 = 60000;
                                                    break;
                                                case 524288:
                                                    FR6 = 120.0d;
                                                    FS7 = 8912896;
                                                    BR8 = 240000;
                                                    break;
                                                case 1048576:
                                                    FR6 = 30.0d;
                                                    FS7 = 35651584;
                                                    BR8 = 60000;
                                                    break;
                                                case 2097152:
                                                    FR6 = 30.0d;
                                                    FS7 = 35651584;
                                                    BR8 = 240000;
                                                    break;
                                                case 4194304:
                                                    FR6 = 60.0d;
                                                    FS7 = 35651584;
                                                    BR8 = 120000;
                                                    break;
                                                case 8388608:
                                                    FR6 = 60.0d;
                                                    FS7 = 35651584;
                                                    BR8 = 480000;
                                                    break;
                                                case 16777216:
                                                    FR6 = 120.0d;
                                                    FS7 = 35651584;
                                                    BR8 = 240000;
                                                    break;
                                                case 33554432:
                                                    FR6 = 120.0d;
                                                    FS7 = 35651584;
                                                    BR8 = 800000;
                                                    break;
                                                default:
                                                    Log.w(TAG, "Unrecognized level " + profileLevel7.level + " for " + mime2);
                                                    errors |= 1;
                                                    break;
                                            }
                                            int i29 = profileLevel7.profile;
                                            if (i29 != 4096) {
                                                switch (i29) {
                                                    case 1:
                                                    case 2:
                                                        break;
                                                    default:
                                                        Log.w(TAG, "Unrecognized profile " + profileLevel7.profile + " for " + mime2);
                                                        errors |= 1;
                                                        break;
                                                }
                                            }
                                            int FS8 = FS7 >> 6;
                                            errors6 = errors & -5;
                                            maxBlocksPerSecond8 = Math.max((long) ((int) (((double) FS8) * FR6)), maxBlocksPerSecond8);
                                            maxBlocks7 = Math.max(FS8, maxBlocks7);
                                            maxBps5 = Math.max(BR8 * 1000, maxBps5);
                                        }
                                        int maxLengthInBlocks3 = (int) Math.sqrt((double) (maxBlocks7 * 8));
                                        int i30 = maxLengthInBlocks3;
                                        applyMacroBlockLimits(maxLengthInBlocks3, maxLengthInBlocks3, maxBlocks7, maxBlocksPerSecond8, 8, 8, 1, 1);
                                        int i31 = maxBlocks7;
                                        maxDPBBlocks = maxBps5;
                                        long j5 = maxBlocksPerSecond8;
                                    } else {
                                        Log.w(TAG, "Unsupported mime " + mime2);
                                        errors = 4 | 2;
                                        maxDPBBlocks = 64000;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            this.mBitrateRange = Range.create(1, Integer.valueOf(maxDPBBlocks));
            this.mParent.mError |= errors;
        }
    }

    MediaCodecInfo(String name, boolean isEncoder, CodecCapabilities[] caps) {
        this.mName = name;
        this.mIsEncoder = isEncoder;
        for (CodecCapabilities c : caps) {
            this.mCaps.put(c.getMimeType(), c);
        }
    }

    public final String getName() {
        return this.mName;
    }

    public final boolean isEncoder() {
        return this.mIsEncoder;
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
        return new MediaCodecInfo(this.mName, this.mIsEncoder, (CodecCapabilities[]) caps.toArray(new CodecCapabilities[caps.size()]));
    }
}
