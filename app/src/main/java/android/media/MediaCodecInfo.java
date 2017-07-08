package android.media;

import android.bluetooth.BluetoothAssignedNumbers;
import android.bluetooth.BluetoothClass.Device;
import android.bluetooth.BluetoothHealth;
import android.mtp.MtpConstants;
import android.net.LinkQualityInfo;
import android.net.wifi.WifiManager;
import android.opengl.GLES10;
import android.opengl.GLES20;
import android.os.BatteryManager;
import android.os.BatteryStats.HistoryItem;
import android.os.Process;
import android.os.StrictMode;
import android.os.UserHandle;
import android.os.health.HealthKeys;
import android.preference.Preference;
import android.provider.DocumentsContract.Document;
import android.provider.DocumentsContract.Root;
import android.provider.Downloads.Impl;
import android.provider.MediaStore.Video.Thumbnails;
import android.rms.HwSysResource;
import android.rms.iaware.AwareNRTConstant;
import android.rms.iaware.Events;
import android.security.keymaster.KeymasterDefs;
import android.service.voice.VoiceInteractionSession;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.Engine;
import android.speech.tts.Voice;
import android.telecom.AudioState;
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
    private static final Range<Integer> BITRATE_RANGE = null;
    private static final int DEFAULT_MAX_SUPPORTED_INSTANCES = 32;
    private static final int ERROR_NONE_SUPPORTED = 4;
    private static final int ERROR_UNRECOGNIZED = 1;
    private static final int ERROR_UNSUPPORTED = 2;
    private static final Range<Integer> FRAME_RATE_RANGE = null;
    private static final int MAX_SUPPORTED_INSTANCES_LIMIT = 256;
    private static final Range<Integer> POSITIVE_INTEGERS = null;
    private static final Range<Long> POSITIVE_LONGS = null;
    private static final Range<Rational> POSITIVE_RATIONALS = null;
    private static final Range<Integer> SIZE_RANGE = null;
    private Map<String, CodecCapabilities> mCaps;
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

        public void init(MediaFormat info, CodecCapabilities parent) {
            this.mParent = parent;
            initWithPlatformLimits();
            applyLevelLimits();
            parseFromInfo(info);
        }

        private void initWithPlatformLimits() {
            this.mBitrateRange = Range.create(Integer.valueOf(0), Integer.valueOf(Preference.DEFAULT_ORDER));
            this.mMaxInputChannelCount = MAX_INPUT_CHANNEL_COUNT;
            Range[] rangeArr = new Range[MediaCodecInfo.ERROR_UNRECOGNIZED];
            rangeArr[0] = Range.create(Integer.valueOf(8000), Integer.valueOf(96000));
            this.mSampleRateRanges = rangeArr;
            this.mSampleRates = null;
        }

        private boolean supports(Integer sampleRate, Integer inputChannels) {
            if (inputChannels == null || (inputChannels.intValue() >= MediaCodecInfo.ERROR_UNRECOGNIZED && inputChannels.intValue() <= this.mMaxInputChannelCount)) {
                return sampleRate == null || Utils.binarySearchDistinctRanges(this.mSampleRateRanges, sampleRate) >= 0;
            } else {
                return false;
            }
        }

        public boolean isSampleRateSupported(int sampleRate) {
            return supports(Integer.valueOf(sampleRate), null);
        }

        private void limitSampleRates(int[] rates) {
            Arrays.sort(rates);
            ArrayList<Range<Integer>> ranges = new ArrayList();
            int length = rates.length;
            for (int i = 0; i < length; i += MediaCodecInfo.ERROR_UNRECOGNIZED) {
                int rate = rates[i];
                if (supports(Integer.valueOf(rate), null)) {
                    ranges.add(Range.create(Integer.valueOf(rate), Integer.valueOf(rate)));
                }
            }
            this.mSampleRateRanges = (Range[]) ranges.toArray(new Range[ranges.size()]);
            createDiscreteSampleRates();
        }

        private void createDiscreteSampleRates() {
            this.mSampleRates = new int[this.mSampleRateRanges.length];
            for (int i = 0; i < this.mSampleRateRanges.length; i += MediaCodecInfo.ERROR_UNRECOGNIZED) {
                this.mSampleRates[i] = ((Integer) this.mSampleRateRanges[i].getLower()).intValue();
            }
        }

        private void limitSampleRates(Range<Integer>[] rateRanges) {
            Utils.sortDistinctRanges(rateRanges);
            this.mSampleRateRanges = Utils.intersectSortedDistinctRanges(this.mSampleRateRanges, rateRanges);
            Range[] rangeArr = this.mSampleRateRanges;
            int length = rangeArr.length;
            int i = 0;
            while (i < length) {
                Range<Integer> range = rangeArr[i];
                if (((Integer) range.getLower()).equals(range.getUpper())) {
                    i += MediaCodecInfo.ERROR_UNRECOGNIZED;
                } else {
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
            int maxChannels = 0;
            String mime = this.mParent.getMimeType();
            if (mime.equalsIgnoreCase(MediaFormat.MIMETYPE_AUDIO_MPEG)) {
                sampleRates = new int[]{8000, 11025, 12000, 16000, 22050, 24000, 32000, 44100, 48000};
                bitRates = Range.create(Integer.valueOf(8000), Integer.valueOf(320000));
                maxChannels = MediaCodecInfo.ERROR_UNSUPPORTED;
            } else if (mime.equalsIgnoreCase(MediaFormat.MIMETYPE_AUDIO_AMR_NB)) {
                sampleRates = new int[MediaCodecInfo.ERROR_UNRECOGNIZED];
                sampleRates[0] = 8000;
                bitRates = Range.create(Integer.valueOf(4750), Integer.valueOf(12200));
                maxChannels = MediaCodecInfo.ERROR_UNRECOGNIZED;
            } else if (mime.equalsIgnoreCase(MediaFormat.MIMETYPE_AUDIO_AMR_WB)) {
                sampleRates = new int[MediaCodecInfo.ERROR_UNRECOGNIZED];
                sampleRates[0] = 16000;
                bitRates = Range.create(Integer.valueOf(6600), Integer.valueOf(23850));
                maxChannels = MediaCodecInfo.ERROR_UNRECOGNIZED;
            } else if (mime.equalsIgnoreCase(MediaFormat.MIMETYPE_AUDIO_AAC)) {
                sampleRates = new int[]{7350, 8000, 11025, 12000, 16000, 22050, 24000, 32000, 44100, 48000, 64000, 88200, 96000};
                bitRates = Range.create(Integer.valueOf(8000), Integer.valueOf(510000));
                maxChannels = 48;
            } else if (mime.equalsIgnoreCase(MediaFormat.MIMETYPE_AUDIO_VORBIS)) {
                bitRates = Range.create(Integer.valueOf(32000), Integer.valueOf(500000));
                sampleRateRange = Range.create(Integer.valueOf(8000), Integer.valueOf(AudioFormat.SAMPLE_RATE_HZ_MAX));
                maxChannels = Process.PROC_TERM_MASK;
            } else if (mime.equalsIgnoreCase(MediaFormat.MIMETYPE_AUDIO_OPUS)) {
                bitRates = Range.create(Integer.valueOf(BluetoothHealth.HEALTH_OPERATION_SUCCESS), Integer.valueOf(510000));
                sampleRates = new int[]{8000, 12000, 16000, 24000, 48000};
                maxChannels = Process.PROC_TERM_MASK;
            } else if (mime.equalsIgnoreCase(MediaFormat.MIMETYPE_AUDIO_RAW)) {
                sampleRateRange = Range.create(Integer.valueOf(MediaCodecInfo.ERROR_UNRECOGNIZED), Integer.valueOf(96000));
                bitRates = Range.create(Integer.valueOf(MediaCodecInfo.ERROR_UNRECOGNIZED), Integer.valueOf(10000000));
                maxChannels = AudioTrack.CHANNEL_COUNT_MAX;
            } else if (mime.equalsIgnoreCase(MediaFormat.MIMETYPE_AUDIO_FLAC)) {
                sampleRateRange = Range.create(Integer.valueOf(MediaCodecInfo.ERROR_UNRECOGNIZED), Integer.valueOf(655350));
                maxChannels = Process.PROC_TERM_MASK;
            } else if (mime.equalsIgnoreCase(MediaFormat.MIMETYPE_AUDIO_G711_ALAW) || mime.equalsIgnoreCase(MediaFormat.MIMETYPE_AUDIO_G711_MLAW)) {
                sampleRates = new int[MediaCodecInfo.ERROR_UNRECOGNIZED];
                sampleRates[0] = 8000;
                bitRates = Range.create(Integer.valueOf(64000), Integer.valueOf(64000));
            } else if (mime.equalsIgnoreCase(MediaFormat.MIMETYPE_AUDIO_MSGSM)) {
                sampleRates = new int[MediaCodecInfo.ERROR_UNRECOGNIZED];
                sampleRates[0] = 8000;
                bitRates = Range.create(Integer.valueOf(13000), Integer.valueOf(13000));
                maxChannels = MediaCodecInfo.ERROR_UNRECOGNIZED;
            } else {
                Log.w(TAG, "Unsupported mime " + mime);
                CodecCapabilities codecCapabilities = this.mParent;
                codecCapabilities.mError |= MediaCodecInfo.ERROR_UNSUPPORTED;
            }
            if (sampleRates != null) {
                limitSampleRates(sampleRates);
            } else if (sampleRateRange != null) {
                Range[] rangeArr = new Range[MediaCodecInfo.ERROR_UNRECOGNIZED];
                rangeArr[0] = sampleRateRange;
                limitSampleRates(rangeArr);
            }
            applyLimits(maxChannels, bitRates);
        }

        private void applyLimits(int maxInputChannels, Range<Integer> bitRates) {
            this.mMaxInputChannelCount = ((Integer) Range.create(Integer.valueOf(MediaCodecInfo.ERROR_UNRECOGNIZED), Integer.valueOf(this.mMaxInputChannelCount)).clamp(Integer.valueOf(maxInputChannels))).intValue();
            if (bitRates != null) {
                this.mBitrateRange = this.mBitrateRange.intersect(bitRates);
            }
        }

        private void parseFromInfo(MediaFormat info) {
            int maxInputChannels = MAX_INPUT_CHANNEL_COUNT;
            Range<Integer> bitRates = MediaCodecInfo.POSITIVE_INTEGERS;
            if (info.containsKey("sample-rate-ranges")) {
                String[] rateStrings = info.getString("sample-rate-ranges").split(",");
                Range[] rateRanges = new Range[rateStrings.length];
                for (int i = 0; i < rateStrings.length; i += MediaCodecInfo.ERROR_UNRECOGNIZED) {
                    rateRanges[i] = Utils.parseIntRange(rateStrings[i], null);
                }
                limitSampleRates(rateRanges);
            }
            if (info.containsKey("max-channel-count")) {
                maxInputChannels = Utils.parseIntSafely(info.getString("max-channel-count"), MAX_INPUT_CHANNEL_COUNT);
            }
            if (info.containsKey("bitrate-range")) {
                bitRates = bitRates.intersect(Utils.parseIntRange(info.getString("bitrate-range"), bitRates));
            }
            applyLimits(maxInputChannels, bitRates);
        }

        public void setDefaultFormat(MediaFormat format) {
            if (((Integer) this.mBitrateRange.getLower()).equals(this.mBitrateRange.getUpper())) {
                format.setInteger(MediaFormat.KEY_BIT_RATE, ((Integer) this.mBitrateRange.getLower()).intValue());
            }
            if (this.mMaxInputChannelCount == MediaCodecInfo.ERROR_UNRECOGNIZED) {
                format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, MediaCodecInfo.ERROR_UNRECOGNIZED);
            }
            if (this.mSampleRates != null && this.mSampleRates.length == MediaCodecInfo.ERROR_UNRECOGNIZED) {
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
        public static final String FEATURE_SecurePlayback = "secure-playback";
        public static final String FEATURE_TunneledPlayback = "tunneled-playback";
        private static final String TAG = "CodecCapabilities";
        private static final Feature[] decoderFeatures = null;
        private static final Feature[] encoderFeatures = null;
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

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.MediaCodecInfo.CodecCapabilities.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.MediaCodecInfo.CodecCapabilities.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaCodecInfo.CodecCapabilities.<clinit>():void");
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
            for (int i = 0; i < res.length; i += COLOR_FormatMonochrome) {
                res[i] = features[i].mName;
            }
            return res;
        }

        private Feature[] getValidFeatures() {
            if (isEncoder()) {
                return encoderFeatures;
            }
            return decoderFeatures;
        }

        private boolean checkFeature(String name, int flags) {
            boolean z = false;
            Feature[] validFeatures = getValidFeatures();
            int length = validFeatures.length;
            for (int i = 0; i < length; i += COLOR_FormatMonochrome) {
                Feature feat = validFeatures[i];
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
            Feature[] validFeatures = getValidFeatures();
            int length = validFeatures.length;
            for (int i = 0; i < length; i += COLOR_FormatMonochrome) {
                Feature feat = validFeatures[i];
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
            int i;
            Feature[] validFeatures = getValidFeatures();
            int length = validFeatures.length;
            for (i = 0; i < length; i += COLOR_FormatMonochrome) {
                Feature feat = validFeatures[i];
                Integer yesNo = (Integer) map.get(MediaFormat.KEY_FEATURE_ + feat.mName);
                if (yesNo != null && ((yesNo.intValue() == COLOR_FormatMonochrome && !isFeatureSupported(feat.mName)) || (yesNo.intValue() == 0 && isFeatureRequired(feat.mName)))) {
                    return false;
                }
            }
            Integer profile = (Integer) map.get(MediaFormat.KEY_PROFILE);
            Integer level = (Integer) map.get(BatteryManager.EXTRA_LEVEL);
            if (profile != null) {
                if (!supportsProfileLevel(profile.intValue(), level)) {
                    return false;
                }
                int maxLevel = 0;
                CodecProfileLevel[] codecProfileLevelArr = this.profileLevels;
                length = codecProfileLevelArr.length;
                for (i = 0; i < length; i += COLOR_FormatMonochrome) {
                    CodecProfileLevel pl = codecProfileLevelArr[i];
                    if (pl.profile == profile.intValue() && pl.level > maxLevel) {
                        maxLevel = pl.level;
                    }
                }
                CodecCapabilities levelCaps = createFromProfileLevel(this.mMime, profile.intValue(), maxLevel);
                Map<String, Object> mapWithoutProfile = new HashMap(map);
                mapWithoutProfile.remove(MediaFormat.KEY_PROFILE);
                MediaFormat formatWithoutProfile = new MediaFormat(mapWithoutProfile);
                if (!(levelCaps == null || levelCaps.isFormatSupported(formatWithoutProfile))) {
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

        private static boolean supportsBitrate(Range<Integer> bitrateRange, MediaFormat format) {
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
            boolean z = true;
            CodecProfileLevel[] codecProfileLevelArr = this.profileLevels;
            int length = codecProfileLevelArr.length;
            for (int i = 0; i < length; i += COLOR_FormatMonochrome) {
                CodecProfileLevel pl = codecProfileLevelArr[i];
                if (pl.profile == profile) {
                    if (level == null || this.mMime.equalsIgnoreCase(MediaFormat.MIMETYPE_AUDIO_AAC)) {
                        return true;
                    }
                    if ((!this.mMime.equalsIgnoreCase(MediaFormat.MIMETYPE_VIDEO_H263) || pl.level == level.intValue() || pl.level != COLOR_Format32bitARGB8888 || level.intValue() <= COLOR_FormatMonochrome) && (!this.mMime.equalsIgnoreCase(MediaFormat.MIMETYPE_VIDEO_MPEG4) || pl.level == level.intValue() || pl.level != COLOR_Format16bitARGB4444 || level.intValue() <= COLOR_FormatMonochrome)) {
                        if (this.mMime.equalsIgnoreCase(MediaFormat.MIMETYPE_VIDEO_HEVC)) {
                            boolean checkingHighTier;
                            boolean supportsHighTier = (pl.level & 44739242) != 0;
                            if ((level.intValue() & 44739242) != 0) {
                                checkingHighTier = true;
                            } else {
                                checkingHighTier = false;
                            }
                            if (checkingHighTier && !supportsHighTier) {
                            }
                        }
                        if (pl.level >= level.intValue()) {
                            if (createFromProfileLevel(this.mMime, profile, pl.level) == null) {
                                return true;
                            }
                            if (createFromProfileLevel(this.mMime, profile, level.intValue()) == null) {
                                z = false;
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
            return new CodecCapabilities((CodecProfileLevel[]) Arrays.copyOf(this.profileLevels, this.profileLevels.length), Arrays.copyOf(this.colorFormats, this.colorFormats.length), isEncoder(), this.mFlagsVerified, this.mDefaultFormat, this.mCapabilitiesInfo);
        }

        public static CodecCapabilities createFromProfileLevel(String mime, int profile, int level) {
            CodecProfileLevel pl = new CodecProfileLevel();
            pl.profile = profile;
            pl.level = level;
            MediaFormat defaultFormat = new MediaFormat();
            defaultFormat.setString(MediaFormat.KEY_MIME, mime);
            CodecProfileLevel[] codecProfileLevelArr = new CodecProfileLevel[COLOR_FormatMonochrome];
            codecProfileLevelArr[0] = pl;
            CodecCapabilities ret = new CodecCapabilities(codecProfileLevelArr, new int[0], true, 0, defaultFormat, new MediaFormat());
            if (ret.mError != 0) {
                return null;
            }
            return ret;
        }

        CodecCapabilities(CodecProfileLevel[] profLevs, int[] colFmts, boolean encoder, int flags, Map<String, Object> defaultFormatMap, Map<String, Object> capabilitiesMap) {
            this(profLevs, colFmts, encoder, flags, new MediaFormat(defaultFormatMap), new MediaFormat(capabilitiesMap));
        }

        CodecCapabilities(CodecProfileLevel[] profLevs, int[] colFmts, boolean encoder, int flags, MediaFormat defaultFormat, MediaFormat info) {
            Map<String, Object> map = info.getMap();
            this.colorFormats = colFmts;
            this.mFlagsVerified = flags;
            this.mDefaultFormat = defaultFormat;
            this.mCapabilitiesInfo = info;
            this.mMime = this.mDefaultFormat.getString(MediaFormat.KEY_MIME);
            if (profLevs.length == 0 && this.mMime.equalsIgnoreCase(MediaFormat.MIMETYPE_VIDEO_VP9)) {
                CodecProfileLevel profLev = new CodecProfileLevel();
                profLev.profile = COLOR_FormatMonochrome;
                profLev.level = VideoCapabilities.equivalentVP9Level(info);
                profLevs = new CodecProfileLevel[COLOR_FormatMonochrome];
                profLevs[0] = profLev;
            }
            this.profileLevels = profLevs;
            if (this.mMime.toLowerCase().startsWith("audio/")) {
                this.mAudioCaps = AudioCapabilities.create(info, this);
                this.mAudioCaps.setDefaultFormat(this.mDefaultFormat);
            } else if (this.mMime.toLowerCase().startsWith("video/")) {
                this.mVideoCaps = VideoCapabilities.create(info, this);
            }
            if (encoder) {
                this.mEncoderCaps = EncoderCapabilities.create(info, this);
                this.mEncoderCaps.setDefaultFormat(this.mDefaultFormat);
            }
            this.mMaxSupportedInstances = Utils.parseIntSafely(MediaCodecList.getGlobalSettings().get("max-concurrent-instances"), COLOR_FormatRawBayer8bitcompressed);
            this.mMaxSupportedInstances = ((Integer) Range.create(Integer.valueOf(COLOR_FormatMonochrome), Integer.valueOf(MediaCodecInfo.MAX_SUPPORTED_INSTANCES_LIMIT)).clamp(Integer.valueOf(Utils.parseIntSafely(map.get("max-concurrent-instances"), this.mMaxSupportedInstances)))).intValue();
            Feature[] validFeatures = getValidFeatures();
            int length = validFeatures.length;
            for (int i = 0; i < length; i += COLOR_FormatMonochrome) {
                Feature feat = validFeatures[i];
                String key = MediaFormat.KEY_FEATURE_ + feat.mName;
                Integer yesNo = (Integer) map.get(key);
                if (yesNo != null) {
                    if (yesNo.intValue() > 0) {
                        this.mFlagsRequired |= feat.mValue;
                    }
                    this.mFlagsSupported |= feat.mValue;
                    this.mDefaultFormat.setInteger(key, COLOR_FormatMonochrome);
                }
            }
        }
    }

    public static final class CodecProfileLevel {
        public static final int AACObjectELD = 39;
        public static final int AACObjectERLC = 17;
        public static final int AACObjectHE = 5;
        public static final int AACObjectHE_PS = 29;
        public static final int AACObjectLC = 2;
        public static final int AACObjectLD = 23;
        public static final int AACObjectLTP = 4;
        public static final int AACObjectMain = 1;
        public static final int AACObjectSSR = 3;
        public static final int AACObjectScalable = 6;
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
        public static final int DolbyVisionProfileDvheDen = 8;
        public static final int DolbyVisionProfileDvheDer = 4;
        public static final int DolbyVisionProfileDvheDtb = 128;
        public static final int DolbyVisionProfileDvheDth = 64;
        public static final int DolbyVisionProfileDvheDtr = 16;
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

        public CodecProfileLevel() {
        }
    }

    public static final class EncoderCapabilities {
        public static final int BITRATE_MODE_CBR = 2;
        public static final int BITRATE_MODE_CQ = 0;
        public static final int BITRATE_MODE_VBR = 1;
        private static final Feature[] bitrates = null;
        private int mBitControl;
        private Range<Integer> mComplexityRange;
        private Integer mDefaultComplexity;
        private Integer mDefaultQuality;
        private CodecCapabilities mParent;
        private Range<Integer> mQualityRange;
        private String mQualityScale;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.MediaCodecInfo.EncoderCapabilities.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.MediaCodecInfo.EncoderCapabilities.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaCodecInfo.EncoderCapabilities.<clinit>():void");
        }

        public Range<Integer> getQualityRange() {
            return this.mQualityRange;
        }

        public Range<Integer> getComplexityRange() {
            return this.mComplexityRange;
        }

        private static int parseBitrateMode(String mode) {
            Feature[] featureArr = bitrates;
            int length = featureArr.length;
            for (int i = BITRATE_MODE_CQ; i < length; i += BITRATE_MODE_VBR) {
                Feature feat = featureArr[i];
                if (feat.mName.equalsIgnoreCase(mode)) {
                    return feat.mValue;
                }
            }
            return BITRATE_MODE_CQ;
        }

        public boolean isBitrateModeSupported(int mode) {
            boolean z = true;
            Feature[] featureArr = bitrates;
            int length = featureArr.length;
            for (int i = BITRATE_MODE_CQ; i < length; i += BITRATE_MODE_VBR) {
                if (mode == featureArr[i].mValue) {
                    if ((this.mBitControl & (BITRATE_MODE_VBR << mode)) == 0) {
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

        public void init(MediaFormat info, CodecCapabilities parent) {
            this.mParent = parent;
            this.mComplexityRange = Range.create(Integer.valueOf(BITRATE_MODE_CQ), Integer.valueOf(BITRATE_MODE_CQ));
            this.mQualityRange = Range.create(Integer.valueOf(BITRATE_MODE_CQ), Integer.valueOf(BITRATE_MODE_CQ));
            this.mBitControl = BITRATE_MODE_CBR;
            applyLevelLimits();
            parseFromInfo(info);
        }

        private void applyLevelLimits() {
            String mime = this.mParent.getMimeType();
            if (mime.equalsIgnoreCase(MediaFormat.MIMETYPE_AUDIO_FLAC)) {
                this.mComplexityRange = Range.create(Integer.valueOf(BITRATE_MODE_CQ), Integer.valueOf(8));
                this.mBitControl = BITRATE_MODE_VBR;
            } else if (mime.equalsIgnoreCase(MediaFormat.MIMETYPE_AUDIO_AMR_NB) || mime.equalsIgnoreCase(MediaFormat.MIMETYPE_AUDIO_AMR_WB) || mime.equalsIgnoreCase(MediaFormat.MIMETYPE_AUDIO_G711_ALAW) || mime.equalsIgnoreCase(MediaFormat.MIMETYPE_AUDIO_G711_MLAW) || mime.equalsIgnoreCase(MediaFormat.MIMETYPE_AUDIO_MSGSM)) {
                this.mBitControl = MediaCodecInfo.ERROR_NONE_SUPPORTED;
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
            if (info.containsKey("feature-bitrate-control")) {
                String[] split = info.getString("feature-bitrate-control").split(",");
                int length = split.length;
                for (int i = BITRATE_MODE_CQ; i < length; i += BITRATE_MODE_VBR) {
                    this.mBitControl |= parseBitrateMode(split[i]);
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
            if (!(BITRATE_MODE_VBR == null || complexity == null)) {
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
            for (int i = BITRATE_MODE_CQ; i < length; i += BITRATE_MODE_VBR) {
                if (codecProfileLevelArr[i].profile == profile.intValue()) {
                    profile = null;
                    break;
                }
            }
            return profile == null;
        }

        public void setDefaultFormat(MediaFormat format) {
            if (!(((Integer) this.mQualityRange.getUpper()).equals(this.mQualityRange.getLower()) || this.mDefaultQuality == null)) {
                format.setInteger(MediaFormat.KEY_QUALITY, this.mDefaultQuality.intValue());
            }
            if (!(((Integer) this.mComplexityRange.getUpper()).equals(this.mComplexityRange.getLower()) || this.mDefaultComplexity == null)) {
                format.setInteger(MediaFormat.KEY_COMPLEXITY, this.mDefaultComplexity.intValue());
            }
            Feature[] featureArr = bitrates;
            int length = featureArr.length;
            for (int i = BITRATE_MODE_CQ; i < length; i += BITRATE_MODE_VBR) {
                Feature feat = featureArr[i];
                if ((this.mBitControl & (BITRATE_MODE_VBR << feat.mValue)) != 0) {
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
                } else if (!(flacComplexity == null || complexity.equals(flacComplexity))) {
                    throw new IllegalArgumentException("conflicting values for complexity and flac-compression-level");
                }
            }
            Integer profile = (Integer) map.get(MediaFormat.KEY_PROFILE);
            if (MediaFormat.MIMETYPE_AUDIO_AAC.equalsIgnoreCase(mime)) {
                Integer aacProfile = (Integer) map.get(MediaFormat.KEY_AAC_PROFILE);
                if (profile == null) {
                    profile = aacProfile;
                } else if (!(aacProfile == null || aacProfile.equals(profile))) {
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

        private void updateLimits() {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.MediaCodecInfo.VideoCapabilities.updateLimits():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.MediaCodecInfo.VideoCapabilities.updateLimits():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MediaCodecInfo.VideoCapabilities.updateLimits():void");
        }

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
                if (this.mHeightRange.contains(Integer.valueOf(height)) && height % this.mHeightAlignment == 0) {
                    int heightInBlocks = Utils.divUp(height, this.mBlockHeight);
                    range = range.intersect(Integer.valueOf(((Math.max(Utils.divUp(((Integer) this.mBlockCountRange.getLower()).intValue(), heightInBlocks), (int) Math.ceil(((Rational) this.mBlockAspectRatioRange.getLower()).doubleValue() * ((double) heightInBlocks))) - 1) * this.mBlockWidth) + this.mWidthAlignment), Integer.valueOf(this.mBlockWidth * Math.min(((Integer) this.mBlockCountRange.getUpper()).intValue() / heightInBlocks, (int) (((Rational) this.mBlockAspectRatioRange.getUpper()).doubleValue() * ((double) heightInBlocks)))));
                    if (height > this.mSmallerDimensionUpperLimit) {
                        range = range.intersect(Integer.valueOf(MediaCodecInfo.ERROR_UNRECOGNIZED), Integer.valueOf(this.mSmallerDimensionUpperLimit));
                    }
                    return range.intersect(Integer.valueOf((int) Math.ceil(((Rational) this.mAspectRatioRange.getLower()).doubleValue() * ((double) height))), Integer.valueOf((int) (((Rational) this.mAspectRatioRange.getUpper()).doubleValue() * ((double) height))));
                }
                throw new IllegalArgumentException("unsupported height");
            } catch (IllegalArgumentException e) {
                Log.v(TAG, "could not get supported widths for " + height);
                throw new IllegalArgumentException("unsupported height");
            }
        }

        public Range<Integer> getSupportedHeightsFor(int width) {
            try {
                Range<Integer> range = this.mHeightRange;
                if (this.mWidthRange.contains(Integer.valueOf(width)) && width % this.mWidthAlignment == 0) {
                    int widthInBlocks = Utils.divUp(width, this.mBlockWidth);
                    range = range.intersect(Integer.valueOf(((Math.max(Utils.divUp(((Integer) this.mBlockCountRange.getLower()).intValue(), widthInBlocks), (int) Math.ceil(((double) widthInBlocks) / ((Rational) this.mBlockAspectRatioRange.getUpper()).doubleValue())) - 1) * this.mBlockHeight) + this.mHeightAlignment), Integer.valueOf(this.mBlockHeight * Math.min(((Integer) this.mBlockCountRange.getUpper()).intValue() / widthInBlocks, (int) (((double) widthInBlocks) / ((Rational) this.mBlockAspectRatioRange.getLower()).doubleValue()))));
                    if (width > this.mSmallerDimensionUpperLimit) {
                        range = range.intersect(Integer.valueOf(MediaCodecInfo.ERROR_UNRECOGNIZED), Integer.valueOf(this.mSmallerDimensionUpperLimit));
                    }
                    return range.intersect(Integer.valueOf((int) Math.ceil(((double) width) / ((Rational) this.mAspectRatioRange.getUpper()).doubleValue())), Integer.valueOf((int) (((double) width) / ((Rational) this.mAspectRatioRange.getLower()).doubleValue())));
                }
                throw new IllegalArgumentException("unsupported width");
            } catch (IllegalArgumentException e) {
                Log.v(TAG, "could not get supported heights for " + width);
                throw new IllegalArgumentException("unsupported width");
            }
        }

        public Range<Double> getSupportedFrameRatesFor(int width, int height) {
            Range<Integer> range = this.mHeightRange;
            if (supports(Integer.valueOf(width), Integer.valueOf(height), null)) {
                int blockCount = Utils.divUp(width, this.mBlockWidth) * Utils.divUp(height, this.mBlockHeight);
                return Range.create(Double.valueOf(Math.max(((double) ((Long) this.mBlocksPerSecondRange.getLower()).longValue()) / ((double) blockCount), (double) ((Integer) this.mFrameRateRange.getLower()).intValue())), Double.valueOf(Math.min(((double) ((Long) this.mBlocksPerSecondRange.getUpper()).longValue()) / ((double) blockCount), (double) ((Integer) this.mFrameRateRange.getUpper()).intValue())));
            }
            throw new IllegalArgumentException("unsupported size");
        }

        private int getBlockCount(int width, int height) {
            return Utils.divUp(width, this.mBlockWidth) * Utils.divUp(height, this.mBlockHeight);
        }

        private Size findClosestSize(int width, int height) {
            int targetBlockCount = getBlockCount(width, height);
            Size closestSize = null;
            int minDiff = Preference.DEFAULT_ORDER;
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
            Range<Long> range = (Range) this.mMeasuredFrameRates.get(size);
            Double ratio = Double.valueOf(((double) getBlockCount(size.getWidth(), size.getHeight())) / ((double) Math.max(getBlockCount(width, height), MediaCodecInfo.ERROR_UNRECOGNIZED)));
            return Range.create(Double.valueOf(((double) ((Long) range.getLower()).longValue()) * ratio.doubleValue()), Double.valueOf(((double) ((Long) range.getUpper()).longValue()) * ratio.doubleValue()));
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
            if (!(MediaCodecInfo.ERROR_UNRECOGNIZED == null || width == null)) {
                ok = this.mWidthRange.contains(width) ? width.intValue() % this.mWidthAlignment == 0 : false;
            }
            if (ok && height != null) {
                ok = this.mHeightRange.contains(height) ? height.intValue() % this.mHeightAlignment == 0 : false;
            }
            if (ok && rate != null) {
                ok = this.mFrameRateRange.contains(Utils.intRangeFor(rate.doubleValue()));
            }
            if (!ok || height == null || width == null) {
                return ok;
            }
            ok = Math.min(height.intValue(), width.intValue()) <= this.mSmallerDimensionUpperLimit;
            int widthInBlocks = Utils.divUp(width.intValue(), this.mBlockWidth);
            int heightInBlocks = Utils.divUp(height.intValue(), this.mBlockHeight);
            int blockCount = widthInBlocks * heightInBlocks;
            if (ok && this.mBlockCountRange.contains(Integer.valueOf(blockCount)) && this.mBlockAspectRatioRange.contains(new Rational(widthInBlocks, heightInBlocks))) {
                ok = this.mAspectRatioRange.contains(new Rational(width.intValue(), height.intValue()));
            } else {
                ok = false;
            }
            if (!ok || rate == null) {
                return ok;
            }
            return this.mBlocksPerSecondRange.contains(Utils.longRangeFor(((double) blockCount) * rate.doubleValue()));
        }

        public boolean supportsFormat(MediaFormat format) {
            Map<String, Object> map = format.getMap();
            if (supports((Integer) map.get(Thumbnails.WIDTH), (Integer) map.get(Thumbnails.HEIGHT), (Number) map.get(MediaFormat.KEY_FRAME_RATE)) && CodecCapabilities.supportsBitrate(this.mBitrateRange, format)) {
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

        public void init(MediaFormat info, CodecCapabilities parent) {
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
            this.mWidthAlignment = MediaCodecInfo.ERROR_UNSUPPORTED;
            this.mHeightAlignment = MediaCodecInfo.ERROR_UNSUPPORTED;
            this.mBlockWidth = MediaCodecInfo.ERROR_UNSUPPORTED;
            this.mBlockHeight = MediaCodecInfo.ERROR_UNSUPPORTED;
            this.mSmallerDimensionUpperLimit = ((Integer) MediaCodecInfo.SIZE_RANGE.getUpper()).intValue();
        }

        private Map<Size, Range<Long>> getMeasuredFrameRates(Map<String, Object> map) {
            Map<Size, Range<Long>> ret = new HashMap();
            String prefix = "measured-frame-rate-";
            for (String key : map.keySet()) {
                if (key.startsWith("measured-frame-rate-")) {
                    String subKey = key.substring("measured-frame-rate-".length());
                    String[] temp = key.split("-");
                    if (temp.length == 5) {
                        Size size = Utils.parseSize(temp[3], null);
                        if (size != null && size.getWidth() * size.getHeight() > 0) {
                            Range<Long> range = Utils.parseLongRange(map.get(key), null);
                            if (range != null && ((Long) range.getLower()).longValue() >= 0 && ((Long) range.getUpper()).longValue() >= 0) {
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
            int FS = counts == null ? 0 : BS * ((Integer) counts.getUpper()).intValue();
            Range<Long> blockRates = Utils.parseLongRange(map.get("blocks-per-second-range"), null);
            long SR = blockRates == null ? 0 : ((long) BS) * ((Long) blockRates.getUpper()).longValue();
            Pair<Range<Integer>, Range<Integer>> dimensionRanges = parseWidthHeightRanges(map.get("size-range"));
            int D = dimensionRanges == null ? 0 : Math.max(((Integer) ((Range) dimensionRanges.first).getUpper()).intValue(), ((Integer) ((Range) dimensionRanges.second).getUpper()).intValue());
            Range<Integer> bitRates = Utils.parseIntRange(map.get("bitrate-range"), null);
            int BR = bitRates == null ? 0 : Utils.divUp(((Integer) bitRates.getUpper()).intValue(), (int) Process.SYSTEM_UID);
            if (SR <= 829440 && FS <= 36864 && BR <= Voice.QUALITY_LOW && D <= Document.FLAG_VIRTUAL_DOCUMENT) {
                return MediaCodecInfo.ERROR_UNRECOGNIZED;
            }
            if (SR <= 2764800 && FS <= 73728 && BR <= CalendarColumns.CAL_ACCESS_ROOT && D <= GLES20.GL_SRC_COLOR) {
                return MediaCodecInfo.ERROR_UNSUPPORTED;
            }
            if (SR <= 4608000 && FS <= 122880 && BR <= Device.WEARABLE_PAGER && D <= 960) {
                return MediaCodecInfo.ERROR_NONE_SUPPORTED;
            }
            if (SR <= 9216000 && FS <= 245760 && BR <= 3600 && D <= Device.PERIPHERAL_KEYBOARD) {
                return 8;
            }
            if (SR <= 20736000 && FS <= 552960 && BR <= 7200 && D <= Process.PROC_CHAR) {
                return 16;
            }
            if (SR <= 36864000 && FS <= 983040 && BR <= 12000 && D <= 2752) {
                return MediaCodecInfo.DEFAULT_MAX_SUPPORTED_INSTANCES;
            }
            if (SR <= 83558400 && FS <= 2228224 && BR <= 18000 && D <= 4160) {
                return 64;
            }
            if (SR <= 160432128 && FS <= 2228224 && BR <= HealthKeys.BASE_PROCESS && D <= 4160) {
                return KeymasterDefs.KM_ALGORITHM_HMAC;
            }
            if (SR <= 311951360 && FS <= 8912896 && BR <= 60000 && D <= 8384) {
                return MediaCodecInfo.MAX_SUPPORTED_INSTANCES_LIMIT;
            }
            if (SR <= 588251136 && FS <= 8912896 && BR <= 120000 && D <= 8384) {
                return Document.FLAG_VIRTUAL_DOCUMENT;
            }
            if (SR <= 1176502272 && FS <= 8912896 && BR <= 180000 && D <= 8384) {
                return Document.FLAG_SUPPORTS_REMOVE;
            }
            if (SR <= 1176502272 && FS <= 35651584 && BR <= 180000 && D <= 16832) {
                return Process.PROC_CHAR;
            }
            if (SR <= 2353004544L && FS <= 35651584 && BR <= 240000 && D <= 16832) {
                return StrictMode.DETECT_VM_REGISTRATION_LEAKS;
            }
            if (SR > 4706009088L || FS > 35651584 || BR > 480000 || D > 16832) {
                return Process.PROC_OUT_LONG;
            }
            return Process.PROC_OUT_LONG;
        }

        private void parseFromInfo(MediaFormat info) {
            Map<String, Object> map = info.getMap();
            Size size = new Size(this.mBlockWidth, this.mBlockHeight);
            Size alignment = new Size(this.mWidthAlignment, this.mHeightAlignment);
            Range range = null;
            Range range2 = null;
            Size blockSize = Utils.parseSize(map.get("block-size"), size);
            alignment = Utils.parseSize(map.get("alignment"), alignment);
            Range<Integer> counts = Utils.parseIntRange(map.get("block-count-range"), null);
            Range blockRates = Utils.parseLongRange(map.get("blocks-per-second-range"), null);
            this.mMeasuredFrameRates = getMeasuredFrameRates(map);
            Pair<Range<Integer>, Range<Integer>> sizeRanges = parseWidthHeightRanges(map.get("size-range"));
            if (sizeRanges != null) {
                range = sizeRanges.first;
                range2 = sizeRanges.second;
            }
            if (map.containsKey("feature-can-swap-width-height")) {
                if (range != null) {
                    this.mSmallerDimensionUpperLimit = Math.min(((Integer) range.getUpper()).intValue(), ((Integer) range2.getUpper()).intValue());
                    range2 = range.extend(range2);
                    range = range2;
                } else {
                    Log.w(TAG, "feature can-swap-width-height is best used with size-range");
                    this.mSmallerDimensionUpperLimit = Math.min(((Integer) this.mWidthRange.getUpper()).intValue(), ((Integer) this.mHeightRange.getUpper()).intValue());
                    Range extend = this.mWidthRange.extend(this.mHeightRange);
                    this.mHeightRange = extend;
                    this.mWidthRange = extend;
                }
            }
            Range<Rational> ratios = Utils.parseRationalRange(map.get("block-aspect-ratio-range"), null);
            Range<Rational> blockRatios = Utils.parseRationalRange(map.get("pixel-aspect-ratio-range"), null);
            Range<Integer> frameRates = Utils.parseIntRange(map.get("frame-rate-range"), null);
            if (frameRates != null) {
                try {
                    frameRates = frameRates.intersect(MediaCodecInfo.FRAME_RATE_RANGE);
                } catch (IllegalArgumentException e) {
                    Log.w(TAG, "frame rate range (" + frameRates + ") is out of limits: " + MediaCodecInfo.FRAME_RATE_RANGE);
                    frameRates = null;
                }
            }
            Range bitRates = Utils.parseIntRange(map.get("bitrate-range"), null);
            if (bitRates != null) {
                try {
                    bitRates = bitRates.intersect(MediaCodecInfo.BITRATE_RANGE);
                } catch (IllegalArgumentException e2) {
                    Log.w(TAG, "bitrate range (" + bitRates + ") is out of limits: " + MediaCodecInfo.BITRATE_RANGE);
                    bitRates = null;
                }
            }
            MediaCodecInfo.checkPowerOfTwo(blockSize.getWidth(), "block-size width must be power of two");
            MediaCodecInfo.checkPowerOfTwo(blockSize.getHeight(), "block-size height must be power of two");
            MediaCodecInfo.checkPowerOfTwo(alignment.getWidth(), "alignment width must be power of two");
            MediaCodecInfo.checkPowerOfTwo(alignment.getHeight(), "alignment height must be power of two");
            applyMacroBlockLimits(Preference.DEFAULT_ORDER, Preference.DEFAULT_ORDER, Preference.DEFAULT_ORDER, LinkQualityInfo.UNKNOWN_LONG, blockSize.getWidth(), blockSize.getHeight(), alignment.getWidth(), alignment.getHeight());
            if ((this.mParent.mError & MediaCodecInfo.ERROR_UNSUPPORTED) != 0 || this.mAllowMbOverride) {
                if (range != null) {
                    this.mWidthRange = MediaCodecInfo.SIZE_RANGE.intersect(range);
                }
                if (range2 != null) {
                    this.mHeightRange = MediaCodecInfo.SIZE_RANGE.intersect(range2);
                }
                if (counts != null) {
                    this.mBlockCountRange = MediaCodecInfo.POSITIVE_INTEGERS.intersect(Utils.factorRange((Range) counts, ((this.mBlockWidth * this.mBlockHeight) / blockSize.getWidth()) / blockSize.getHeight()));
                }
                if (blockRates != null) {
                    this.mBlocksPerSecondRange = MediaCodecInfo.POSITIVE_LONGS.intersect(Utils.factorRange(blockRates, (long) (((this.mBlockWidth * this.mBlockHeight) / blockSize.getWidth()) / blockSize.getHeight())));
                }
                if (blockRatios != null) {
                    this.mBlockAspectRatioRange = MediaCodecInfo.POSITIVE_RATIONALS.intersect(Utils.scaleRange(blockRatios, this.mBlockHeight / blockSize.getHeight(), this.mBlockWidth / blockSize.getWidth()));
                }
                if (ratios != null) {
                    this.mAspectRatioRange = MediaCodecInfo.POSITIVE_RATIONALS.intersect(ratios);
                }
                if (frameRates != null) {
                    this.mFrameRateRange = MediaCodecInfo.FRAME_RATE_RANGE.intersect(frameRates);
                }
                if (bitRates != null) {
                    if ((this.mParent.mError & MediaCodecInfo.ERROR_UNSUPPORTED) != 0) {
                        this.mBitrateRange = MediaCodecInfo.BITRATE_RANGE.intersect(bitRates);
                    } else {
                        this.mBitrateRange = this.mBitrateRange.intersect(bitRates);
                    }
                }
            } else {
                if (range != null) {
                    this.mWidthRange = this.mWidthRange.intersect(range);
                }
                if (range2 != null) {
                    this.mHeightRange = this.mHeightRange.intersect(range2);
                }
                if (counts != null) {
                    this.mBlockCountRange = this.mBlockCountRange.intersect(Utils.factorRange((Range) counts, ((this.mBlockWidth * this.mBlockHeight) / blockSize.getWidth()) / blockSize.getHeight()));
                }
                if (blockRates != null) {
                    this.mBlocksPerSecondRange = this.mBlocksPerSecondRange.intersect(Utils.factorRange(blockRates, (long) (((this.mBlockWidth * this.mBlockHeight) / blockSize.getWidth()) / blockSize.getHeight())));
                }
                if (blockRatios != null) {
                    this.mBlockAspectRatioRange = this.mBlockAspectRatioRange.intersect(Utils.scaleRange(blockRatios, this.mBlockHeight / blockSize.getHeight(), this.mBlockWidth / blockSize.getWidth()));
                }
                if (ratios != null) {
                    this.mAspectRatioRange = this.mAspectRatioRange.intersect(ratios);
                }
                if (frameRates != null) {
                    this.mFrameRateRange = this.mFrameRateRange.intersect(frameRates);
                }
                if (bitRates != null) {
                    this.mBitrateRange = this.mBitrateRange.intersect(bitRates);
                }
            }
            updateLimits();
        }

        private void applyBlockLimits(int blockWidth, int blockHeight, Range<Integer> counts, Range<Long> rates, Range<Rational> ratios) {
            MediaCodecInfo.checkPowerOfTwo(blockWidth, "blockWidth must be a power of two");
            MediaCodecInfo.checkPowerOfTwo(blockHeight, "blockHeight must be a power of two");
            int newBlockWidth = Math.max(blockWidth, this.mBlockWidth);
            int newBlockHeight = Math.max(blockHeight, this.mBlockHeight);
            int factor = ((newBlockWidth * newBlockHeight) / this.mBlockWidth) / this.mBlockHeight;
            if (factor != MediaCodecInfo.ERROR_UNRECOGNIZED) {
                this.mBlockCountRange = Utils.factorRange(this.mBlockCountRange, factor);
                this.mBlocksPerSecondRange = Utils.factorRange(this.mBlocksPerSecondRange, (long) factor);
                this.mBlockAspectRatioRange = Utils.scaleRange(this.mBlockAspectRatioRange, newBlockHeight / this.mBlockHeight, newBlockWidth / this.mBlockWidth);
                this.mHorizontalBlockRange = Utils.factorRange(this.mHorizontalBlockRange, newBlockWidth / this.mBlockWidth);
                this.mVerticalBlockRange = Utils.factorRange(this.mVerticalBlockRange, newBlockHeight / this.mBlockHeight);
            }
            factor = ((newBlockWidth * newBlockHeight) / blockWidth) / blockHeight;
            if (factor != MediaCodecInfo.ERROR_UNRECOGNIZED) {
                counts = Utils.factorRange((Range) counts, factor);
                rates = Utils.factorRange((Range) rates, (long) factor);
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

        private void applyMacroBlockLimits(int maxHorizontalBlocks, int maxVerticalBlocks, int maxBlocks, long maxBlocksPerSecond, int blockWidth, int blockHeight, int widthAlignment, int heightAlignment) {
            applyMacroBlockLimits(MediaCodecInfo.ERROR_UNRECOGNIZED, MediaCodecInfo.ERROR_UNRECOGNIZED, maxHorizontalBlocks, maxVerticalBlocks, maxBlocks, maxBlocksPerSecond, blockWidth, blockHeight, widthAlignment, heightAlignment);
        }

        private void applyMacroBlockLimits(int minHorizontalBlocks, int minVerticalBlocks, int maxHorizontalBlocks, int maxVerticalBlocks, int maxBlocks, long maxBlocksPerSecond, int blockWidth, int blockHeight, int widthAlignment, int heightAlignment) {
            applyAlignment(widthAlignment, heightAlignment);
            applyBlockLimits(blockWidth, blockHeight, Range.create(Integer.valueOf(MediaCodecInfo.ERROR_UNRECOGNIZED), Integer.valueOf(maxBlocks)), Range.create(Long.valueOf(1), Long.valueOf(maxBlocksPerSecond)), Range.create(new Rational(MediaCodecInfo.ERROR_UNRECOGNIZED, maxVerticalBlocks), new Rational(maxHorizontalBlocks, MediaCodecInfo.ERROR_UNRECOGNIZED)));
            this.mHorizontalBlockRange = this.mHorizontalBlockRange.intersect(Integer.valueOf(Utils.divUp(minHorizontalBlocks, this.mBlockWidth / blockWidth)), Integer.valueOf(maxHorizontalBlocks / (this.mBlockWidth / blockWidth)));
            this.mVerticalBlockRange = this.mVerticalBlockRange.intersect(Integer.valueOf(Utils.divUp(minVerticalBlocks, this.mBlockHeight / blockHeight)), Integer.valueOf(maxVerticalBlocks / (this.mBlockHeight / blockHeight)));
        }

        private void applyLevelLimits() {
            int maxBps;
            int errors = MediaCodecInfo.ERROR_NONE_SUPPORTED;
            CodecProfileLevel[] profileLevels = this.mParent.profileLevels;
            String mime = this.mParent.getMimeType();
            int maxBlocks;
            long maxBlocksPerSecond;
            int length;
            int i;
            CodecProfileLevel profileLevel;
            int MBPS;
            int FS;
            int BR;
            boolean supported;
            int maxLengthInBlocks;
            if (mime.equalsIgnoreCase(MediaFormat.MIMETYPE_VIDEO_AVC)) {
                maxBlocks = 99;
                maxBlocksPerSecond = 1485;
                maxBps = 64000;
                int maxDPBBlocks = 396;
                length = profileLevels.length;
                for (i = 0; i < length; i += MediaCodecInfo.ERROR_UNRECOGNIZED) {
                    profileLevel = profileLevels[i];
                    MBPS = 0;
                    FS = 0;
                    BR = 0;
                    int DPB = 0;
                    supported = true;
                    switch (profileLevel.level) {
                        case MediaCodecInfo.ERROR_UNRECOGNIZED /*1*/:
                            MBPS = 1485;
                            FS = 99;
                            BR = 64;
                            DPB = 396;
                            break;
                        case MediaCodecInfo.ERROR_UNSUPPORTED /*2*/:
                            MBPS = 1485;
                            FS = 99;
                            BR = KeymasterDefs.KM_ALGORITHM_HMAC;
                            DPB = 396;
                            break;
                        case MediaCodecInfo.ERROR_NONE_SUPPORTED /*4*/:
                            MBPS = AwareNRTConstant.FIRST_SDK_EVENT_ID;
                            FS = 396;
                            BR = Impl.STATUS_RUNNING;
                            DPB = MediaPlayer.MEDIA_INFO_TIMED_TEXT_ERROR;
                            break;
                        case AudioState.ROUTE_SPEAKER /*8*/:
                            MBPS = BluetoothHealth.HEALTH_OPERATION_SUCCESS;
                            FS = 396;
                            BR = 384;
                            DPB = 2376;
                            break;
                        case VoiceInteractionSession.SHOW_SOURCE_ACTIVITY /*16*/:
                            MBPS = 11880;
                            FS = 396;
                            BR = GLES20.GL_SRC_COLOR;
                            DPB = 2376;
                            break;
                        case MediaCodecInfo.DEFAULT_MAX_SUPPORTED_INSTANCES /*32*/:
                            MBPS = 11880;
                            FS = 396;
                            BR = AwareNRTConstant.FIRST_EVENT_ID;
                            DPB = 2376;
                            break;
                        case KeymasterDefs.KM_PAD_PKCS7 /*64*/:
                            MBPS = 19800;
                            FS = 792;
                            BR = AudioFormat.SAMPLE_RATE_HZ_MIN;
                            DPB = 4752;
                            break;
                        case KeymasterDefs.KM_ALGORITHM_HMAC /*128*/:
                            MBPS = 20250;
                            FS = 1620;
                            BR = AudioFormat.SAMPLE_RATE_HZ_MIN;
                            DPB = 8100;
                            break;
                        case MediaCodecInfo.MAX_SUPPORTED_INSTANCES_LIMIT /*256*/:
                            MBPS = 40500;
                            FS = 1620;
                            BR = Events.EVENT_FLAG_START;
                            DPB = 8100;
                            break;
                        case Document.FLAG_VIRTUAL_DOCUMENT /*512*/:
                            MBPS = 108000;
                            FS = 3600;
                            BR = 14000;
                            DPB = 18000;
                            break;
                        case Document.FLAG_SUPPORTS_REMOVE /*1024*/:
                            MBPS = 216000;
                            FS = GLES20.GL_BYTE;
                            BR = Events.EVENT_BASE_MEM;
                            DPB = MtpConstants.DEVICE_PROPERTY_UNDEFINED;
                            break;
                        case Process.PROC_CHAR /*2048*/:
                            MBPS = 245760;
                            FS = Process.PROC_OUT_LONG;
                            BR = Events.EVENT_BASE_MEM;
                            DPB = Document.FLAG_ARCHIVE;
                            break;
                        case StrictMode.DETECT_VM_REGISTRATION_LEAKS /*4096*/:
                            MBPS = 245760;
                            FS = Process.PROC_OUT_LONG;
                            BR = HealthKeys.BASE_SERVICE;
                            DPB = Document.FLAG_ARCHIVE;
                            break;
                        case Process.PROC_OUT_LONG /*8192*/:
                            MBPS = 522240;
                            FS = GLES10.GL_TEXTURE_ENV_MODE;
                            BR = HealthKeys.BASE_SERVICE;
                            DPB = GLES20.GL_STENCIL_BACK_FUNC;
                            break;
                        case Process.PROC_OUT_FLOAT /*16384*/:
                            MBPS = WifiManager.PPPOE_BASE;
                            FS = 22080;
                            BR = 135000;
                            DPB = 110400;
                            break;
                        case Document.FLAG_ARCHIVE /*32768*/:
                            MBPS = 983040;
                            FS = 36864;
                            BR = 240000;
                            DPB = 184320;
                            break;
                        case Root.FLAG_EMPTY /*65536*/:
                            MBPS = 2073600;
                            FS = 36864;
                            BR = 240000;
                            DPB = 184320;
                            break;
                        default:
                            Log.w(TAG, "Unrecognized level " + profileLevel.level + " for " + mime);
                            errors |= MediaCodecInfo.ERROR_UNRECOGNIZED;
                            break;
                    }
                    switch (profileLevel.profile) {
                        case MediaCodecInfo.ERROR_UNRECOGNIZED /*1*/:
                        case MediaCodecInfo.ERROR_UNSUPPORTED /*2*/:
                            break;
                        case MediaCodecInfo.ERROR_NONE_SUPPORTED /*4*/:
                        case MediaCodecInfo.DEFAULT_MAX_SUPPORTED_INSTANCES /*32*/:
                        case KeymasterDefs.KM_PAD_PKCS7 /*64*/:
                            Log.w(TAG, "Unsupported profile " + profileLevel.profile + " for " + mime);
                            errors |= MediaCodecInfo.ERROR_UNSUPPORTED;
                            supported = false;
                            break;
                        case AudioState.ROUTE_SPEAKER /*8*/:
                            BR *= 1250;
                            break;
                        case VoiceInteractionSession.SHOW_SOURCE_ACTIVITY /*16*/:
                            BR *= AwareNRTConstant.FIRST_SDK_EVENT_ID;
                            break;
                        default:
                            Log.w(TAG, "Unrecognized profile " + profileLevel.profile + " for " + mime);
                            errors |= MediaCodecInfo.ERROR_UNRECOGNIZED;
                            BR *= Process.SYSTEM_UID;
                            break;
                    }
                    BR *= Process.SYSTEM_UID;
                    if (supported) {
                        errors &= -5;
                    }
                    maxBlocksPerSecond = Math.max((long) MBPS, maxBlocksPerSecond);
                    maxBlocks = Math.max(FS, maxBlocks);
                    maxBps = Math.max(BR, maxBps);
                    maxDPBBlocks = Math.max(maxDPBBlocks, DPB);
                }
                maxLengthInBlocks = (int) Math.sqrt((double) (maxBlocks * 8));
                applyMacroBlockLimits(maxLengthInBlocks, maxLengthInBlocks, maxBlocks, maxBlocksPerSecond, 16, 16, MediaCodecInfo.ERROR_UNRECOGNIZED, MediaCodecInfo.ERROR_UNRECOGNIZED);
            } else {
                int maxWidth;
                int maxHeight;
                int maxRate;
                int FR;
                int W;
                int H;
                if (mime.equalsIgnoreCase(MediaFormat.MIMETYPE_VIDEO_MPEG2)) {
                    maxWidth = 11;
                    maxHeight = 9;
                    maxRate = 15;
                    maxBlocks = 99;
                    maxBlocksPerSecond = 1485;
                    maxBps = 64000;
                    length = profileLevels.length;
                    for (i = 0; i < length; i += MediaCodecInfo.ERROR_UNRECOGNIZED) {
                        profileLevel = profileLevels[i];
                        MBPS = 0;
                        FS = 0;
                        BR = 0;
                        FR = 0;
                        W = 0;
                        H = 0;
                        supported = true;
                        switch (profileLevel.profile) {
                            case TextToSpeech.SUCCESS /*0*/:
                                switch (profileLevel.level) {
                                    case MediaCodecInfo.ERROR_UNRECOGNIZED /*1*/:
                                        FR = 30;
                                        W = 45;
                                        H = 36;
                                        MBPS = 40500;
                                        FS = 1620;
                                        BR = 15000;
                                        break;
                                    default:
                                        Log.w(TAG, "Unrecognized profile/level " + profileLevel.profile + "/" + profileLevel.level + " for " + mime);
                                        errors |= MediaCodecInfo.ERROR_UNRECOGNIZED;
                                        break;
                                }
                            case MediaCodecInfo.ERROR_UNRECOGNIZED /*1*/:
                                switch (profileLevel.level) {
                                    case TextToSpeech.SUCCESS /*0*/:
                                        FR = 30;
                                        W = 22;
                                        H = 18;
                                        MBPS = 11880;
                                        FS = 396;
                                        BR = AudioFormat.SAMPLE_RATE_HZ_MIN;
                                        break;
                                    case MediaCodecInfo.ERROR_UNRECOGNIZED /*1*/:
                                        FR = 30;
                                        W = 45;
                                        H = 36;
                                        MBPS = 40500;
                                        FS = 1620;
                                        BR = 15000;
                                        break;
                                    case MediaCodecInfo.ERROR_UNSUPPORTED /*2*/:
                                        FR = 60;
                                        W = 90;
                                        H = 68;
                                        MBPS = 183600;
                                        FS = 6120;
                                        BR = 60000;
                                        break;
                                    case Engine.DEFAULT_STREAM /*3*/:
                                        FR = 60;
                                        W = BluetoothAssignedNumbers.NIKE;
                                        H = 68;
                                        MBPS = 244800;
                                        FS = 8160;
                                        BR = Events.EVENT_FLAG_FINISH;
                                        break;
                                    case MediaCodecInfo.ERROR_NONE_SUPPORTED /*4*/:
                                        FR = 60;
                                        W = BluetoothAssignedNumbers.NIKE;
                                        H = 68;
                                        MBPS = 489600;
                                        FS = 8160;
                                        BR = Events.EVENT_FLAG_FINISH;
                                        break;
                                    default:
                                        Log.w(TAG, "Unrecognized profile/level " + profileLevel.profile + "/" + profileLevel.level + " for " + mime);
                                        errors |= MediaCodecInfo.ERROR_UNRECOGNIZED;
                                        break;
                                }
                            case MediaCodecInfo.ERROR_UNSUPPORTED /*2*/:
                            case Engine.DEFAULT_STREAM /*3*/:
                            case MediaCodecInfo.ERROR_NONE_SUPPORTED /*4*/:
                            case AudioState.ROUTE_WIRED_OR_EARPIECE /*5*/:
                                Log.i(TAG, "Unsupported profile " + profileLevel.profile + " for " + mime);
                                errors |= MediaCodecInfo.ERROR_UNSUPPORTED;
                                supported = false;
                                break;
                            default:
                                Log.w(TAG, "Unrecognized profile " + profileLevel.profile + " for " + mime);
                                errors |= MediaCodecInfo.ERROR_UNRECOGNIZED;
                                break;
                        }
                        if (supported) {
                            errors &= -5;
                        }
                        maxBlocksPerSecond = Math.max((long) MBPS, maxBlocksPerSecond);
                        maxBlocks = Math.max(FS, maxBlocks);
                        maxBps = Math.max(BR * Process.SYSTEM_UID, maxBps);
                        maxWidth = Math.max(W, maxWidth);
                        maxHeight = Math.max(H, maxHeight);
                        maxRate = Math.max(FR, maxRate);
                    }
                    applyMacroBlockLimits(maxWidth, maxHeight, maxBlocks, maxBlocksPerSecond, 16, 16, MediaCodecInfo.ERROR_UNRECOGNIZED, MediaCodecInfo.ERROR_UNRECOGNIZED);
                    this.mFrameRateRange = this.mFrameRateRange.intersect(Integer.valueOf(12), Integer.valueOf(maxRate));
                } else {
                    boolean strict;
                    int maxDim;
                    if (mime.equalsIgnoreCase(MediaFormat.MIMETYPE_VIDEO_MPEG4)) {
                        maxWidth = 11;
                        maxHeight = 9;
                        maxRate = 15;
                        maxBlocks = 99;
                        maxBlocksPerSecond = 1485;
                        maxBps = 64000;
                        length = profileLevels.length;
                        for (i = 0; i < length; i += MediaCodecInfo.ERROR_UNRECOGNIZED) {
                            profileLevel = profileLevels[i];
                            MBPS = 0;
                            FS = 0;
                            BR = 0;
                            FR = 0;
                            W = 0;
                            H = 0;
                            strict = false;
                            supported = true;
                            switch (profileLevel.profile) {
                                case MediaCodecInfo.ERROR_UNRECOGNIZED /*1*/:
                                    switch (profileLevel.level) {
                                        case MediaCodecInfo.ERROR_UNRECOGNIZED /*1*/:
                                            strict = true;
                                            FR = 15;
                                            W = 11;
                                            H = 9;
                                            MBPS = 1485;
                                            FS = 99;
                                            BR = 64;
                                            break;
                                        case MediaCodecInfo.ERROR_UNSUPPORTED /*2*/:
                                            strict = true;
                                            FR = 15;
                                            W = 11;
                                            H = 9;
                                            MBPS = 1485;
                                            FS = 99;
                                            BR = KeymasterDefs.KM_ALGORITHM_HMAC;
                                            break;
                                        case MediaCodecInfo.ERROR_NONE_SUPPORTED /*4*/:
                                            FR = 30;
                                            W = 11;
                                            H = 9;
                                            MBPS = 1485;
                                            FS = 99;
                                            BR = 64;
                                            break;
                                        case AudioState.ROUTE_SPEAKER /*8*/:
                                            FR = 30;
                                            W = 22;
                                            H = 18;
                                            MBPS = 5940;
                                            FS = 396;
                                            BR = KeymasterDefs.KM_ALGORITHM_HMAC;
                                            break;
                                        case VoiceInteractionSession.SHOW_SOURCE_ACTIVITY /*16*/:
                                            FR = 30;
                                            W = 22;
                                            H = 18;
                                            MBPS = 11880;
                                            FS = 396;
                                            BR = 384;
                                            break;
                                        case KeymasterDefs.KM_PAD_PKCS7 /*64*/:
                                            FR = 30;
                                            W = 40;
                                            H = 30;
                                            MBPS = 36000;
                                            FS = 1200;
                                            BR = AudioFormat.SAMPLE_RATE_HZ_MIN;
                                            break;
                                        case KeymasterDefs.KM_ALGORITHM_HMAC /*128*/:
                                            FR = 30;
                                            W = 45;
                                            H = 36;
                                            MBPS = 40500;
                                            FS = 1620;
                                            BR = 8000;
                                            break;
                                        case MediaCodecInfo.MAX_SUPPORTED_INSTANCES_LIMIT /*256*/:
                                            FR = 30;
                                            W = 80;
                                            H = 45;
                                            MBPS = 108000;
                                            FS = 3600;
                                            BR = 12000;
                                            break;
                                        default:
                                            Log.w(TAG, "Unrecognized profile/level " + profileLevel.profile + "/" + profileLevel.level + " for " + mime);
                                            errors |= MediaCodecInfo.ERROR_UNRECOGNIZED;
                                            break;
                                    }
                                case MediaCodecInfo.ERROR_UNSUPPORTED /*2*/:
                                case MediaCodecInfo.ERROR_NONE_SUPPORTED /*4*/:
                                case AudioState.ROUTE_SPEAKER /*8*/:
                                case VoiceInteractionSession.SHOW_SOURCE_ACTIVITY /*16*/:
                                case MediaCodecInfo.DEFAULT_MAX_SUPPORTED_INSTANCES /*32*/:
                                case KeymasterDefs.KM_PAD_PKCS7 /*64*/:
                                case KeymasterDefs.KM_ALGORITHM_HMAC /*128*/:
                                case MediaCodecInfo.MAX_SUPPORTED_INSTANCES_LIMIT /*256*/:
                                case Document.FLAG_VIRTUAL_DOCUMENT /*512*/:
                                case Document.FLAG_SUPPORTS_REMOVE /*1024*/:
                                case Process.PROC_CHAR /*2048*/:
                                case StrictMode.DETECT_VM_REGISTRATION_LEAKS /*4096*/:
                                case Process.PROC_OUT_LONG /*8192*/:
                                case Process.PROC_OUT_FLOAT /*16384*/:
                                    Log.i(TAG, "Unsupported profile " + profileLevel.profile + " for " + mime);
                                    errors |= MediaCodecInfo.ERROR_UNSUPPORTED;
                                    supported = false;
                                    break;
                                case Document.FLAG_ARCHIVE /*32768*/:
                                    switch (profileLevel.level) {
                                        case MediaCodecInfo.ERROR_UNRECOGNIZED /*1*/:
                                        case MediaCodecInfo.ERROR_NONE_SUPPORTED /*4*/:
                                            FR = 30;
                                            W = 11;
                                            H = 9;
                                            MBPS = 2970;
                                            FS = 99;
                                            BR = KeymasterDefs.KM_ALGORITHM_HMAC;
                                            break;
                                        case AudioState.ROUTE_SPEAKER /*8*/:
                                            FR = 30;
                                            W = 22;
                                            H = 18;
                                            MBPS = 5940;
                                            FS = 396;
                                            BR = 384;
                                            break;
                                        case VoiceInteractionSession.SHOW_SOURCE_ACTIVITY /*16*/:
                                            FR = 30;
                                            W = 22;
                                            H = 18;
                                            MBPS = 11880;
                                            FS = 396;
                                            BR = GLES20.GL_SRC_COLOR;
                                            break;
                                        case HwSysResource.ANR /*24*/:
                                            FR = 30;
                                            W = 22;
                                            H = 18;
                                            MBPS = 11880;
                                            FS = 396;
                                            BR = 1500;
                                            break;
                                        case MediaCodecInfo.DEFAULT_MAX_SUPPORTED_INSTANCES /*32*/:
                                            FR = 30;
                                            W = 44;
                                            H = 36;
                                            MBPS = 23760;
                                            FS = 792;
                                            BR = AwareNRTConstant.FIRST_SDK_EVENT_ID;
                                            break;
                                        case KeymasterDefs.KM_ALGORITHM_HMAC /*128*/:
                                            FR = 30;
                                            W = 45;
                                            H = 36;
                                            MBPS = 48600;
                                            FS = 1620;
                                            BR = 8000;
                                            break;
                                        default:
                                            Log.w(TAG, "Unrecognized profile/level " + profileLevel.profile + "/" + profileLevel.level + " for " + mime);
                                            errors |= MediaCodecInfo.ERROR_UNRECOGNIZED;
                                            break;
                                    }
                                default:
                                    Log.w(TAG, "Unrecognized profile " + profileLevel.profile + " for " + mime);
                                    errors |= MediaCodecInfo.ERROR_UNRECOGNIZED;
                                    break;
                            }
                            if (supported) {
                                errors &= -5;
                            }
                            maxBlocksPerSecond = Math.max((long) MBPS, maxBlocksPerSecond);
                            maxBlocks = Math.max(FS, maxBlocks);
                            maxBps = Math.max(BR * Process.SYSTEM_UID, maxBps);
                            if (strict) {
                                maxWidth = Math.max(W, maxWidth);
                                maxHeight = Math.max(H, maxHeight);
                                maxRate = Math.max(FR, maxRate);
                            } else {
                                maxDim = (int) Math.sqrt((double) (FS * MediaCodecInfo.ERROR_UNSUPPORTED));
                                maxWidth = Math.max(maxDim, maxWidth);
                                maxHeight = Math.max(maxDim, maxHeight);
                                maxRate = Math.max(Math.max(FR, 60), maxRate);
                            }
                        }
                        applyMacroBlockLimits(maxWidth, maxHeight, maxBlocks, maxBlocksPerSecond, 16, 16, MediaCodecInfo.ERROR_UNRECOGNIZED, MediaCodecInfo.ERROR_UNRECOGNIZED);
                        this.mFrameRateRange = this.mFrameRateRange.intersect(Integer.valueOf(12), Integer.valueOf(maxRate));
                    } else {
                        if (mime.equalsIgnoreCase(MediaFormat.MIMETYPE_VIDEO_H263)) {
                            maxWidth = 11;
                            maxHeight = 9;
                            maxRate = 15;
                            int minWidth = 11;
                            int minHeight = 9;
                            int minAlignment = 16;
                            maxBlocks = 99;
                            maxBlocksPerSecond = 1485;
                            maxBps = 64000;
                            length = profileLevels.length;
                            for (i = 0; i < length; i += MediaCodecInfo.ERROR_UNRECOGNIZED) {
                                profileLevel = profileLevels[i];
                                MBPS = 0;
                                BR = 0;
                                FR = 0;
                                W = 0;
                                H = 0;
                                int minW = minWidth;
                                int minH = minHeight;
                                strict = false;
                                switch (profileLevel.level) {
                                    case MediaCodecInfo.ERROR_UNRECOGNIZED /*1*/:
                                        strict = true;
                                        FR = 15;
                                        W = 11;
                                        H = 9;
                                        BR = MediaCodecInfo.ERROR_UNRECOGNIZED;
                                        MBPS = 99 * 15;
                                        break;
                                    case MediaCodecInfo.ERROR_UNSUPPORTED /*2*/:
                                        strict = true;
                                        FR = 30;
                                        W = 22;
                                        H = 18;
                                        BR = MediaCodecInfo.ERROR_UNSUPPORTED;
                                        MBPS = 396 * 15;
                                        break;
                                    case MediaCodecInfo.ERROR_NONE_SUPPORTED /*4*/:
                                        strict = true;
                                        FR = 30;
                                        W = 22;
                                        H = 18;
                                        BR = 6;
                                        MBPS = 396 * 30;
                                        break;
                                    case AudioState.ROUTE_SPEAKER /*8*/:
                                        strict = true;
                                        FR = 30;
                                        W = 22;
                                        H = 18;
                                        BR = MediaCodecInfo.DEFAULT_MAX_SUPPORTED_INSTANCES;
                                        MBPS = 396 * 30;
                                        break;
                                    case VoiceInteractionSession.SHOW_SOURCE_ACTIVITY /*16*/:
                                        strict = profileLevel.profile != MediaCodecInfo.ERROR_UNRECOGNIZED ? profileLevel.profile == MediaCodecInfo.ERROR_NONE_SUPPORTED : true;
                                        if (!strict) {
                                            minW = MediaCodecInfo.ERROR_UNRECOGNIZED;
                                            minH = MediaCodecInfo.ERROR_UNRECOGNIZED;
                                            minAlignment = MediaCodecInfo.ERROR_NONE_SUPPORTED;
                                        }
                                        FR = 15;
                                        W = 11;
                                        H = 9;
                                        BR = MediaCodecInfo.ERROR_UNSUPPORTED;
                                        MBPS = 99 * 15;
                                        break;
                                    case MediaCodecInfo.DEFAULT_MAX_SUPPORTED_INSTANCES /*32*/:
                                        minW = MediaCodecInfo.ERROR_UNRECOGNIZED;
                                        minH = MediaCodecInfo.ERROR_UNRECOGNIZED;
                                        minAlignment = MediaCodecInfo.ERROR_NONE_SUPPORTED;
                                        FR = 60;
                                        W = 22;
                                        H = 18;
                                        BR = 64;
                                        MBPS = 396 * 50;
                                        break;
                                    case KeymasterDefs.KM_PAD_PKCS7 /*64*/:
                                        minW = MediaCodecInfo.ERROR_UNRECOGNIZED;
                                        minH = MediaCodecInfo.ERROR_UNRECOGNIZED;
                                        minAlignment = MediaCodecInfo.ERROR_NONE_SUPPORTED;
                                        FR = 60;
                                        W = 45;
                                        H = 18;
                                        BR = KeymasterDefs.KM_ALGORITHM_HMAC;
                                        MBPS = 810 * 50;
                                        break;
                                    case KeymasterDefs.KM_ALGORITHM_HMAC /*128*/:
                                        minW = MediaCodecInfo.ERROR_UNRECOGNIZED;
                                        minH = MediaCodecInfo.ERROR_UNRECOGNIZED;
                                        minAlignment = MediaCodecInfo.ERROR_NONE_SUPPORTED;
                                        FR = 60;
                                        W = 45;
                                        H = 36;
                                        BR = MediaCodecInfo.MAX_SUPPORTED_INSTANCES_LIMIT;
                                        MBPS = 1620 * 50;
                                        break;
                                    default:
                                        Log.w(TAG, "Unrecognized profile/level " + profileLevel.profile + "/" + profileLevel.level + " for " + mime);
                                        errors |= MediaCodecInfo.ERROR_UNRECOGNIZED;
                                        break;
                                }
                                switch (profileLevel.profile) {
                                    case MediaCodecInfo.ERROR_UNRECOGNIZED /*1*/:
                                    case MediaCodecInfo.ERROR_UNSUPPORTED /*2*/:
                                    case MediaCodecInfo.ERROR_NONE_SUPPORTED /*4*/:
                                    case AudioState.ROUTE_SPEAKER /*8*/:
                                    case VoiceInteractionSession.SHOW_SOURCE_ACTIVITY /*16*/:
                                    case MediaCodecInfo.DEFAULT_MAX_SUPPORTED_INSTANCES /*32*/:
                                    case KeymasterDefs.KM_PAD_PKCS7 /*64*/:
                                    case KeymasterDefs.KM_ALGORITHM_HMAC /*128*/:
                                    case MediaCodecInfo.MAX_SUPPORTED_INSTANCES_LIMIT /*256*/:
                                        break;
                                    default:
                                        Log.w(TAG, "Unrecognized profile " + profileLevel.profile + " for " + mime);
                                        errors |= MediaCodecInfo.ERROR_UNRECOGNIZED;
                                        break;
                                }
                                if (strict) {
                                    minW = 11;
                                    minH = 9;
                                } else {
                                    this.mAllowMbOverride = true;
                                }
                                errors &= -5;
                                maxBlocksPerSecond = Math.max((long) MBPS, maxBlocksPerSecond);
                                maxBlocks = Math.max(W * H, maxBlocks);
                                maxBps = Math.max(64000 * BR, maxBps);
                                maxWidth = Math.max(W, maxWidth);
                                maxHeight = Math.max(H, maxHeight);
                                maxRate = Math.max(FR, maxRate);
                                minWidth = Math.min(minW, minWidth);
                                minHeight = Math.min(minH, minHeight);
                            }
                            if (!this.mAllowMbOverride) {
                                this.mBlockAspectRatioRange = Range.create(new Rational(11, 9), new Rational(11, 9));
                            }
                            applyMacroBlockLimits(minWidth, minHeight, maxWidth, maxHeight, maxBlocks, maxBlocksPerSecond, 16, 16, minAlignment, minAlignment);
                            this.mFrameRateRange = Range.create(Integer.valueOf(MediaCodecInfo.ERROR_UNRECOGNIZED), Integer.valueOf(maxRate));
                        } else {
                            if (mime.equalsIgnoreCase(MediaFormat.MIMETYPE_VIDEO_VP8)) {
                                maxBps = 100000000;
                                length = profileLevels.length;
                                for (i = 0; i < length; i += MediaCodecInfo.ERROR_UNRECOGNIZED) {
                                    profileLevel = profileLevels[i];
                                    switch (profileLevel.level) {
                                        case MediaCodecInfo.ERROR_UNRECOGNIZED /*1*/:
                                        case MediaCodecInfo.ERROR_UNSUPPORTED /*2*/:
                                        case MediaCodecInfo.ERROR_NONE_SUPPORTED /*4*/:
                                        case AudioState.ROUTE_SPEAKER /*8*/:
                                            break;
                                        default:
                                            Log.w(TAG, "Unrecognized level " + profileLevel.level + " for " + mime);
                                            errors |= MediaCodecInfo.ERROR_UNRECOGNIZED;
                                            break;
                                    }
                                    switch (profileLevel.profile) {
                                        case MediaCodecInfo.ERROR_UNRECOGNIZED /*1*/:
                                            break;
                                        default:
                                            Log.w(TAG, "Unrecognized profile " + profileLevel.profile + " for " + mime);
                                            errors |= MediaCodecInfo.ERROR_UNRECOGNIZED;
                                            break;
                                    }
                                    errors &= -5;
                                }
                                applyMacroBlockLimits(32767, 32767, Preference.DEFAULT_ORDER, 2147483647L, 16, 16, MediaCodecInfo.ERROR_UNRECOGNIZED, MediaCodecInfo.ERROR_UNRECOGNIZED);
                            } else {
                                if (mime.equalsIgnoreCase(MediaFormat.MIMETYPE_VIDEO_VP9)) {
                                    maxBlocksPerSecond = 829440;
                                    maxBlocks = 36864;
                                    maxBps = 200000;
                                    maxDim = Document.FLAG_VIRTUAL_DOCUMENT;
                                    length = profileLevels.length;
                                    for (i = 0; i < length; i += MediaCodecInfo.ERROR_UNRECOGNIZED) {
                                        profileLevel = profileLevels[i];
                                        long SR = 0;
                                        FS = 0;
                                        BR = 0;
                                        int D = 0;
                                        switch (profileLevel.level) {
                                            case MediaCodecInfo.ERROR_UNRECOGNIZED /*1*/:
                                                SR = 829440;
                                                FS = 36864;
                                                BR = Voice.QUALITY_LOW;
                                                D = Document.FLAG_VIRTUAL_DOCUMENT;
                                                break;
                                            case MediaCodecInfo.ERROR_UNSUPPORTED /*2*/:
                                                SR = 2764800;
                                                FS = 73728;
                                                BR = CalendarColumns.CAL_ACCESS_ROOT;
                                                D = GLES20.GL_SRC_COLOR;
                                                break;
                                            case MediaCodecInfo.ERROR_NONE_SUPPORTED /*4*/:
                                                SR = 4608000;
                                                FS = 122880;
                                                BR = Device.WEARABLE_PAGER;
                                                D = 960;
                                                break;
                                            case AudioState.ROUTE_SPEAKER /*8*/:
                                                SR = 9216000;
                                                FS = 245760;
                                                BR = 3600;
                                                D = Device.PERIPHERAL_KEYBOARD;
                                                break;
                                            case VoiceInteractionSession.SHOW_SOURCE_ACTIVITY /*16*/:
                                                SR = 20736000;
                                                FS = 552960;
                                                BR = 7200;
                                                D = Process.PROC_CHAR;
                                                break;
                                            case MediaCodecInfo.DEFAULT_MAX_SUPPORTED_INSTANCES /*32*/:
                                                SR = 36864000;
                                                FS = 983040;
                                                BR = 12000;
                                                D = 2752;
                                                break;
                                            case KeymasterDefs.KM_PAD_PKCS7 /*64*/:
                                                SR = 83558400;
                                                FS = 2228224;
                                                BR = 18000;
                                                D = 4160;
                                                break;
                                            case KeymasterDefs.KM_ALGORITHM_HMAC /*128*/:
                                                SR = 160432128;
                                                FS = 2228224;
                                                BR = HealthKeys.BASE_PROCESS;
                                                D = 4160;
                                                break;
                                            case MediaCodecInfo.MAX_SUPPORTED_INSTANCES_LIMIT /*256*/:
                                                SR = 311951360;
                                                FS = 8912896;
                                                BR = 60000;
                                                D = 8384;
                                                break;
                                            case Document.FLAG_VIRTUAL_DOCUMENT /*512*/:
                                                SR = 588251136;
                                                FS = 8912896;
                                                BR = 120000;
                                                D = 8384;
                                                break;
                                            case Document.FLAG_SUPPORTS_REMOVE /*1024*/:
                                                SR = 1176502272;
                                                FS = 8912896;
                                                BR = 180000;
                                                D = 8384;
                                                break;
                                            case Process.PROC_CHAR /*2048*/:
                                                SR = 1176502272;
                                                FS = 35651584;
                                                BR = 180000;
                                                D = 16832;
                                                break;
                                            case StrictMode.DETECT_VM_REGISTRATION_LEAKS /*4096*/:
                                                SR = 2353004544L;
                                                FS = 35651584;
                                                BR = 240000;
                                                D = 16832;
                                                break;
                                            case Process.PROC_OUT_LONG /*8192*/:
                                                SR = 4706009088L;
                                                FS = 35651584;
                                                BR = 480000;
                                                D = 16832;
                                                break;
                                            default:
                                                Log.w(TAG, "Unrecognized level " + profileLevel.level + " for " + mime);
                                                errors |= MediaCodecInfo.ERROR_UNRECOGNIZED;
                                                break;
                                        }
                                        switch (profileLevel.profile) {
                                            case MediaCodecInfo.ERROR_UNRECOGNIZED /*1*/:
                                            case MediaCodecInfo.ERROR_UNSUPPORTED /*2*/:
                                            case MediaCodecInfo.ERROR_NONE_SUPPORTED /*4*/:
                                            case AudioState.ROUTE_SPEAKER /*8*/:
                                            case StrictMode.DETECT_VM_REGISTRATION_LEAKS /*4096*/:
                                            case Process.PROC_OUT_LONG /*8192*/:
                                                break;
                                            default:
                                                Log.w(TAG, "Unrecognized profile " + profileLevel.profile + " for " + mime);
                                                errors |= MediaCodecInfo.ERROR_UNRECOGNIZED;
                                                break;
                                        }
                                        errors &= -5;
                                        maxBlocksPerSecond = Math.max(SR, maxBlocksPerSecond);
                                        maxBlocks = Math.max(FS, maxBlocks);
                                        maxBps = Math.max(BR * Process.SYSTEM_UID, maxBps);
                                        maxDim = Math.max(D, maxDim);
                                    }
                                    maxLengthInBlocks = Utils.divUp(maxDim, 8);
                                    applyMacroBlockLimits(maxLengthInBlocks, maxLengthInBlocks, Utils.divUp(maxBlocks, 64), Utils.divUp(maxBlocksPerSecond, 64), 8, 8, MediaCodecInfo.ERROR_UNRECOGNIZED, MediaCodecInfo.ERROR_UNRECOGNIZED);
                                } else {
                                    if (mime.equalsIgnoreCase(MediaFormat.MIMETYPE_VIDEO_HEVC)) {
                                        maxBlocks = 576;
                                        maxBlocksPerSecond = (long) 8640;
                                        maxBps = 128000;
                                        length = profileLevels.length;
                                        for (i = 0; i < length; i += MediaCodecInfo.ERROR_UNRECOGNIZED) {
                                            profileLevel = profileLevels[i];
                                            double FR2 = 0.0d;
                                            FS = 0;
                                            BR = 0;
                                            switch (profileLevel.level) {
                                                case MediaCodecInfo.ERROR_UNRECOGNIZED /*1*/:
                                                case MediaCodecInfo.ERROR_UNSUPPORTED /*2*/:
                                                    FR2 = 15.0d;
                                                    FS = 36864;
                                                    BR = KeymasterDefs.KM_ALGORITHM_HMAC;
                                                    break;
                                                case MediaCodecInfo.ERROR_NONE_SUPPORTED /*4*/:
                                                case AudioState.ROUTE_SPEAKER /*8*/:
                                                    FR2 = 30.0d;
                                                    FS = 122880;
                                                    BR = 1500;
                                                    break;
                                                case VoiceInteractionSession.SHOW_SOURCE_ACTIVITY /*16*/:
                                                case MediaCodecInfo.DEFAULT_MAX_SUPPORTED_INSTANCES /*32*/:
                                                    FR2 = 30.0d;
                                                    FS = 245760;
                                                    BR = AwareNRTConstant.FIRST_SDK_EVENT_ID;
                                                    break;
                                                case KeymasterDefs.KM_PAD_PKCS7 /*64*/:
                                                case KeymasterDefs.KM_ALGORITHM_HMAC /*128*/:
                                                    FR2 = 30.0d;
                                                    FS = 552960;
                                                    BR = BluetoothHealth.HEALTH_OPERATION_SUCCESS;
                                                    break;
                                                case MediaCodecInfo.MAX_SUPPORTED_INSTANCES_LIMIT /*256*/:
                                                case Document.FLAG_VIRTUAL_DOCUMENT /*512*/:
                                                    FR2 = 33.75d;
                                                    FS = 983040;
                                                    BR = Events.EVENT_FLAG_START;
                                                    break;
                                                case Document.FLAG_SUPPORTS_REMOVE /*1024*/:
                                                    FR2 = 30.0d;
                                                    FS = 2228224;
                                                    BR = 12000;
                                                    break;
                                                case Process.PROC_CHAR /*2048*/:
                                                    FR2 = 30.0d;
                                                    FS = 2228224;
                                                    BR = HealthKeys.BASE_PROCESS;
                                                    break;
                                                case StrictMode.DETECT_VM_REGISTRATION_LEAKS /*4096*/:
                                                    FR2 = 60.0d;
                                                    FS = 2228224;
                                                    BR = Events.EVENT_BASE_MEM;
                                                    break;
                                                case Process.PROC_OUT_LONG /*8192*/:
                                                    FR2 = 60.0d;
                                                    FS = 2228224;
                                                    BR = HealthKeys.BASE_SERVICE;
                                                    break;
                                                case Process.PROC_OUT_FLOAT /*16384*/:
                                                    FR2 = 30.0d;
                                                    FS = 8912896;
                                                    BR = 25000;
                                                    break;
                                                case Document.FLAG_ARCHIVE /*32768*/:
                                                    FR2 = 30.0d;
                                                    FS = 8912896;
                                                    BR = UserHandle.PER_USER_RANGE;
                                                    break;
                                                case Root.FLAG_EMPTY /*65536*/:
                                                    FR2 = 60.0d;
                                                    FS = 8912896;
                                                    BR = HealthKeys.BASE_PACKAGE;
                                                    break;
                                                case Root.FLAG_ADVANCED /*131072*/:
                                                    FR2 = 60.0d;
                                                    FS = 8912896;
                                                    BR = 160000;
                                                    break;
                                                case Root.FLAG_HAS_SETTINGS /*262144*/:
                                                    FR2 = 120.0d;
                                                    FS = 8912896;
                                                    BR = 60000;
                                                    break;
                                                case Root.FLAG_REMOVABLE_SD /*524288*/:
                                                    FR2 = 120.0d;
                                                    FS = 8912896;
                                                    BR = 240000;
                                                    break;
                                                case Root.FLAG_REMOVABLE_USB /*1048576*/:
                                                    FR2 = 30.0d;
                                                    FS = 35651584;
                                                    BR = 60000;
                                                    break;
                                                case StrictMode.PENALTY_DROPBOX /*2097152*/:
                                                    FR2 = 30.0d;
                                                    FS = 35651584;
                                                    BR = 240000;
                                                    break;
                                                case StrictMode.PENALTY_GATHER /*4194304*/:
                                                    FR2 = 60.0d;
                                                    FS = 35651584;
                                                    BR = 120000;
                                                    break;
                                                case HistoryItem.STATE_SENSOR_ON_FLAG /*8388608*/:
                                                    FR2 = 60.0d;
                                                    FS = 35651584;
                                                    BR = 480000;
                                                    break;
                                                case StrictMode.PENALTY_DEATH_ON_NETWORK /*16777216*/:
                                                    FR2 = 120.0d;
                                                    FS = 35651584;
                                                    BR = 240000;
                                                    break;
                                                case StrictMode.PENALTY_DEATH_ON_CLEARTEXT_NETWORK /*33554432*/:
                                                    FR2 = 120.0d;
                                                    FS = 35651584;
                                                    BR = 800000;
                                                    break;
                                                default:
                                                    Log.w(TAG, "Unrecognized level " + profileLevel.level + " for " + mime);
                                                    errors |= MediaCodecInfo.ERROR_UNRECOGNIZED;
                                                    break;
                                            }
                                            switch (profileLevel.profile) {
                                                case MediaCodecInfo.ERROR_UNRECOGNIZED /*1*/:
                                                case MediaCodecInfo.ERROR_UNSUPPORTED /*2*/:
                                                case StrictMode.DETECT_VM_REGISTRATION_LEAKS /*4096*/:
                                                    break;
                                                default:
                                                    Log.w(TAG, "Unrecognized profile " + profileLevel.profile + " for " + mime);
                                                    errors |= MediaCodecInfo.ERROR_UNRECOGNIZED;
                                                    break;
                                            }
                                            FS >>= 6;
                                            errors &= -5;
                                            maxBlocksPerSecond = Math.max((long) ((int) (((double) FS) * FR2)), maxBlocksPerSecond);
                                            maxBlocks = Math.max(FS, maxBlocks);
                                            maxBps = Math.max(BR * Process.SYSTEM_UID, maxBps);
                                        }
                                        maxLengthInBlocks = (int) Math.sqrt((double) (maxBlocks * 8));
                                        applyMacroBlockLimits(maxLengthInBlocks, maxLengthInBlocks, maxBlocks, maxBlocksPerSecond, 8, 8, MediaCodecInfo.ERROR_UNRECOGNIZED, MediaCodecInfo.ERROR_UNRECOGNIZED);
                                    } else {
                                        Log.w(TAG, "Unsupported mime " + mime);
                                        maxBps = 64000;
                                        errors = 6;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            this.mBitrateRange = Range.create(Integer.valueOf(MediaCodecInfo.ERROR_UNRECOGNIZED), Integer.valueOf(maxBps));
            CodecCapabilities codecCapabilities = this.mParent;
            codecCapabilities.mError |= errors;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.MediaCodecInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.MediaCodecInfo.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.MediaCodecInfo.<clinit>():void");
    }

    MediaCodecInfo(String name, boolean isEncoder, CodecCapabilities[] caps) {
        this.mName = name;
        this.mIsEncoder = isEncoder;
        this.mCaps = new HashMap();
        int length = caps.length;
        for (int i = 0; i < length; i += ERROR_UNRECOGNIZED) {
            CodecCapabilities c = caps[i];
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

    private static int checkPowerOfTwo(int value, String message) {
        if (((value - 1) & value) == 0) {
            return value;
        }
        throw new IllegalArgumentException(message);
    }

    public final CodecCapabilities getCapabilitiesForType(String type) {
        CodecCapabilities caps = (CodecCapabilities) this.mCaps.get(type);
        if (caps != null) {
            return caps.dup();
        }
        throw new IllegalArgumentException("codec does not support type");
    }

    public MediaCodecInfo makeRegular() {
        ArrayList<CodecCapabilities> caps = new ArrayList();
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
