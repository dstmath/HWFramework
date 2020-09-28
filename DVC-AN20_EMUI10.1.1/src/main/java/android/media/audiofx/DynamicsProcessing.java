package android.media.audiofx;

import android.media.audiofx.AudioEffect;
import android.util.Log;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.StringTokenizer;

public final class DynamicsProcessing extends AudioEffect {
    private static final int CHANNEL_COUNT_MAX = 32;
    private static final float CHANNEL_DEFAULT_INPUT_GAIN = 0.0f;
    private static final int CONFIG_DEFAULT_MBC_BANDS = 6;
    private static final int CONFIG_DEFAULT_POSTEQ_BANDS = 6;
    private static final int CONFIG_DEFAULT_PREEQ_BANDS = 6;
    private static final boolean CONFIG_DEFAULT_USE_LIMITER = true;
    private static final boolean CONFIG_DEFAULT_USE_MBC = true;
    private static final boolean CONFIG_DEFAULT_USE_POSTEQ = true;
    private static final boolean CONFIG_DEFAULT_USE_PREEQ = true;
    private static final int CONFIG_DEFAULT_VARIANT = 0;
    private static final float CONFIG_PREFERRED_FRAME_DURATION_MS = 10.0f;
    private static final float DEFAULT_MAX_FREQUENCY = 20000.0f;
    private static final float DEFAULT_MIN_FREQUENCY = 220.0f;
    private static final float EQ_DEFAULT_GAIN = 0.0f;
    private static final float LIMITER_DEFAULT_ATTACK_TIME = 1.0f;
    private static final boolean LIMITER_DEFAULT_ENABLED = true;
    private static final int LIMITER_DEFAULT_LINK_GROUP = 0;
    private static final float LIMITER_DEFAULT_POST_GAIN = 0.0f;
    private static final float LIMITER_DEFAULT_RATIO = 10.0f;
    private static final float LIMITER_DEFAULT_RELEASE_TIME = 60.0f;
    private static final float LIMITER_DEFAULT_THRESHOLD = -2.0f;
    private static final float MBC_DEFAULT_ATTACK_TIME = 3.0f;
    private static final boolean MBC_DEFAULT_ENABLED = true;
    private static final float MBC_DEFAULT_EXPANDER_RATIO = 1.0f;
    private static final float MBC_DEFAULT_KNEE_WIDTH = 0.0f;
    private static final float MBC_DEFAULT_NOISE_GATE_THRESHOLD = -90.0f;
    private static final float MBC_DEFAULT_POST_GAIN = 0.0f;
    private static final float MBC_DEFAULT_PRE_GAIN = 0.0f;
    private static final float MBC_DEFAULT_RATIO = 1.0f;
    private static final float MBC_DEFAULT_RELEASE_TIME = 80.0f;
    private static final float MBC_DEFAULT_THRESHOLD = -45.0f;
    private static final int PARAM_ENGINE_ARCHITECTURE = 48;
    private static final int PARAM_GET_CHANNEL_COUNT = 16;
    private static final int PARAM_INPUT_GAIN = 32;
    private static final int PARAM_LIMITER = 112;
    private static final int PARAM_MBC = 80;
    private static final int PARAM_MBC_BAND = 85;
    private static final int PARAM_POST_EQ = 96;
    private static final int PARAM_POST_EQ_BAND = 101;
    private static final int PARAM_PRE_EQ = 64;
    private static final int PARAM_PRE_EQ_BAND = 69;
    private static final boolean POSTEQ_DEFAULT_ENABLED = true;
    private static final boolean PREEQ_DEFAULT_ENABLED = true;
    private static final String TAG = "DynamicsProcessing";
    public static final int VARIANT_FAVOR_FREQUENCY_RESOLUTION = 0;
    public static final int VARIANT_FAVOR_TIME_RESOLUTION = 1;
    private static final float mMaxFreqLog = ((float) Math.log10(20000.0d));
    private static final float mMinFreqLog = ((float) Math.log10(220.0d));
    private BaseParameterListener mBaseParamListener;
    private int mChannelCount;
    private OnParameterChangeListener mParamListener;
    private final Object mParamListenerLock;

    public interface OnParameterChangeListener {
        void onParameterChange(DynamicsProcessing dynamicsProcessing, int i, int i2);
    }

    public DynamicsProcessing(int audioSession) {
        this(0, audioSession);
    }

    public DynamicsProcessing(int priority, int audioSession) {
        this(priority, audioSession, null);
    }

    public DynamicsProcessing(int priority, int audioSession, Config cfg) {
        super(EFFECT_TYPE_DYNAMICS_PROCESSING, EFFECT_TYPE_NULL, priority, audioSession);
        Config config;
        this.mChannelCount = 0;
        this.mParamListener = null;
        this.mBaseParamListener = null;
        this.mParamListenerLock = new Object();
        if (audioSession == 0) {
            Log.w(TAG, "WARNING: attaching a DynamicsProcessing to global output mix isdeprecated!");
        }
        this.mChannelCount = getChannelCount();
        if (cfg == null) {
            config = new Config.Builder(0, this.mChannelCount, true, 6, true, 6, true, 6, true).build();
        } else {
            config = new Config(this.mChannelCount, cfg);
        }
        setEngineArchitecture(config.getVariant(), config.getPreferredFrameDuration(), config.isPreEqInUse(), config.getPreEqBandCount(), config.isMbcInUse(), config.getMbcBandCount(), config.isPostEqInUse(), config.getPostEqBandCount(), config.isLimiterInUse());
        for (int ch = 0; ch < this.mChannelCount; ch++) {
            updateEngineChannelByChannelIndex(ch, config.getChannelByChannelIndex(ch));
        }
    }

    public Config getConfig() {
        Number[] values = {0, Float.valueOf(0.0f), 0, 0, 0, 0, 0, 0, 0};
        byte[] paramBytes = numberArrayToByteArray(new Number[]{48});
        byte[] valueBytes = numberArrayToByteArray(values);
        getParameter(paramBytes, valueBytes);
        byteArrayToNumberArray(valueBytes, values);
        Config config = new Config.Builder(values[0].intValue(), this.mChannelCount, values[2].intValue() > 0, values[3].intValue(), values[4].intValue() > 0, values[5].intValue(), values[6].intValue() > 0, values[7].intValue(), values[8].intValue() > 0).setPreferredFrameDuration(values[1].floatValue()).build();
        for (int ch = 0; ch < this.mChannelCount; ch++) {
            config.setChannelTo(ch, queryEngineByChannelIndex(ch));
        }
        return config;
    }

    public static class Stage {
        private boolean mEnabled;
        private boolean mInUse;

        public Stage(boolean inUse, boolean enabled) {
            this.mInUse = inUse;
            this.mEnabled = enabled;
        }

        public boolean isEnabled() {
            return this.mEnabled;
        }

        public void setEnabled(boolean enabled) {
            this.mEnabled = enabled;
        }

        public boolean isInUse() {
            return this.mInUse;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format(" Stage InUse: %b\n", Boolean.valueOf(isInUse())));
            if (isInUse()) {
                sb.append(String.format(" Stage Enabled: %b\n", Boolean.valueOf(this.mEnabled)));
            }
            return sb.toString();
        }
    }

    public static class BandStage extends Stage {
        private int mBandCount;

        public BandStage(boolean inUse, boolean enabled, int bandCount) {
            super(inUse, enabled);
            this.mBandCount = isInUse() ? bandCount : 0;
        }

        public int getBandCount() {
            return this.mBandCount;
        }

        @Override // android.media.audiofx.DynamicsProcessing.Stage
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(super.toString());
            if (isInUse()) {
                sb.append(String.format(" Band Count: %d\n", Integer.valueOf(this.mBandCount)));
            }
            return sb.toString();
        }
    }

    public static class BandBase {
        private float mCutoffFrequency;
        private boolean mEnabled;

        public BandBase(boolean enabled, float cutoffFrequency) {
            this.mEnabled = enabled;
            this.mCutoffFrequency = cutoffFrequency;
        }

        public String toString() {
            return String.format(" Enabled: %b\n", Boolean.valueOf(this.mEnabled)) + String.format(" CutoffFrequency: %f\n", Float.valueOf(this.mCutoffFrequency));
        }

        public boolean isEnabled() {
            return this.mEnabled;
        }

        public void setEnabled(boolean enabled) {
            this.mEnabled = enabled;
        }

        public float getCutoffFrequency() {
            return this.mCutoffFrequency;
        }

        public void setCutoffFrequency(float frequency) {
            this.mCutoffFrequency = frequency;
        }
    }

    public static final class EqBand extends BandBase {
        private float mGain;

        public EqBand(boolean enabled, float cutoffFrequency, float gain) {
            super(enabled, cutoffFrequency);
            this.mGain = gain;
        }

        public EqBand(EqBand cfg) {
            super(cfg.isEnabled(), cfg.getCutoffFrequency());
            this.mGain = cfg.mGain;
        }

        @Override // android.media.audiofx.DynamicsProcessing.BandBase
        public String toString() {
            return super.toString() + String.format(" Gain: %f\n", Float.valueOf(this.mGain));
        }

        public float getGain() {
            return this.mGain;
        }

        public void setGain(float gain) {
            this.mGain = gain;
        }
    }

    public static final class MbcBand extends BandBase {
        private float mAttackTime;
        private float mExpanderRatio;
        private float mKneeWidth;
        private float mNoiseGateThreshold;
        private float mPostGain;
        private float mPreGain;
        private float mRatio;
        private float mReleaseTime;
        private float mThreshold;

        public MbcBand(boolean enabled, float cutoffFrequency, float attackTime, float releaseTime, float ratio, float threshold, float kneeWidth, float noiseGateThreshold, float expanderRatio, float preGain, float postGain) {
            super(enabled, cutoffFrequency);
            this.mAttackTime = attackTime;
            this.mReleaseTime = releaseTime;
            this.mRatio = ratio;
            this.mThreshold = threshold;
            this.mKneeWidth = kneeWidth;
            this.mNoiseGateThreshold = noiseGateThreshold;
            this.mExpanderRatio = expanderRatio;
            this.mPreGain = preGain;
            this.mPostGain = postGain;
        }

        public MbcBand(MbcBand cfg) {
            super(cfg.isEnabled(), cfg.getCutoffFrequency());
            this.mAttackTime = cfg.mAttackTime;
            this.mReleaseTime = cfg.mReleaseTime;
            this.mRatio = cfg.mRatio;
            this.mThreshold = cfg.mThreshold;
            this.mKneeWidth = cfg.mKneeWidth;
            this.mNoiseGateThreshold = cfg.mNoiseGateThreshold;
            this.mExpanderRatio = cfg.mExpanderRatio;
            this.mPreGain = cfg.mPreGain;
            this.mPostGain = cfg.mPostGain;
        }

        @Override // android.media.audiofx.DynamicsProcessing.BandBase
        public String toString() {
            return super.toString() + String.format(" AttackTime: %f (ms)\n", Float.valueOf(this.mAttackTime)) + String.format(" ReleaseTime: %f (ms)\n", Float.valueOf(this.mReleaseTime)) + String.format(" Ratio: 1:%f\n", Float.valueOf(this.mRatio)) + String.format(" Threshold: %f (dB)\n", Float.valueOf(this.mThreshold)) + String.format(" NoiseGateThreshold: %f(dB)\n", Float.valueOf(this.mNoiseGateThreshold)) + String.format(" ExpanderRatio: %f:1\n", Float.valueOf(this.mExpanderRatio)) + String.format(" PreGain: %f (dB)\n", Float.valueOf(this.mPreGain)) + String.format(" PostGain: %f (dB)\n", Float.valueOf(this.mPostGain));
        }

        public float getAttackTime() {
            return this.mAttackTime;
        }

        public void setAttackTime(float attackTime) {
            this.mAttackTime = attackTime;
        }

        public float getReleaseTime() {
            return this.mReleaseTime;
        }

        public void setReleaseTime(float releaseTime) {
            this.mReleaseTime = releaseTime;
        }

        public float getRatio() {
            return this.mRatio;
        }

        public void setRatio(float ratio) {
            this.mRatio = ratio;
        }

        public float getThreshold() {
            return this.mThreshold;
        }

        public void setThreshold(float threshold) {
            this.mThreshold = threshold;
        }

        public float getKneeWidth() {
            return this.mKneeWidth;
        }

        public void setKneeWidth(float kneeWidth) {
            this.mKneeWidth = kneeWidth;
        }

        public float getNoiseGateThreshold() {
            return this.mNoiseGateThreshold;
        }

        public void setNoiseGateThreshold(float noiseGateThreshold) {
            this.mNoiseGateThreshold = noiseGateThreshold;
        }

        public float getExpanderRatio() {
            return this.mExpanderRatio;
        }

        public void setExpanderRatio(float expanderRatio) {
            this.mExpanderRatio = expanderRatio;
        }

        public float getPreGain() {
            return this.mPreGain;
        }

        public void setPreGain(float preGain) {
            this.mPreGain = preGain;
        }

        public float getPostGain() {
            return this.mPostGain;
        }

        public void setPostGain(float postGain) {
            this.mPostGain = postGain;
        }
    }

    public static final class Eq extends BandStage {
        private final EqBand[] mBands;

        public Eq(boolean inUse, boolean enabled, int bandCount) {
            super(inUse, enabled, bandCount);
            if (isInUse()) {
                this.mBands = new EqBand[bandCount];
                for (int b = 0; b < bandCount; b++) {
                    float freq = 20000.0f;
                    if (bandCount > 1) {
                        freq = (float) Math.pow(10.0d, (double) (DynamicsProcessing.mMinFreqLog + ((((float) b) * (DynamicsProcessing.mMaxFreqLog - DynamicsProcessing.mMinFreqLog)) / ((float) (bandCount - 1)))));
                    }
                    this.mBands[b] = new EqBand(true, freq, 0.0f);
                }
                return;
            }
            this.mBands = null;
        }

        public Eq(Eq cfg) {
            super(cfg.isInUse(), cfg.isEnabled(), cfg.getBandCount());
            if (isInUse()) {
                this.mBands = new EqBand[cfg.mBands.length];
                int b = 0;
                while (true) {
                    EqBand[] eqBandArr = this.mBands;
                    if (b < eqBandArr.length) {
                        eqBandArr[b] = new EqBand(cfg.mBands[b]);
                        b++;
                    } else {
                        return;
                    }
                }
            } else {
                this.mBands = null;
            }
        }

        @Override // android.media.audiofx.DynamicsProcessing.BandStage, android.media.audiofx.DynamicsProcessing.Stage
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(super.toString());
            if (isInUse()) {
                sb.append("--->EqBands: " + this.mBands.length + "\n");
                for (int b = 0; b < this.mBands.length; b++) {
                    sb.append(String.format("  Band %d\n", Integer.valueOf(b)));
                    sb.append(this.mBands[b].toString());
                }
            }
            return sb.toString();
        }

        private void checkBand(int band) {
            EqBand[] eqBandArr = this.mBands;
            if (eqBandArr == null || band < 0 || band >= eqBandArr.length) {
                throw new IllegalArgumentException("band index " + band + " out of bounds");
            }
        }

        public void setBand(int band, EqBand bandCfg) {
            checkBand(band);
            this.mBands[band] = new EqBand(bandCfg);
        }

        public EqBand getBand(int band) {
            checkBand(band);
            return this.mBands[band];
        }
    }

    public static final class Mbc extends BandStage {
        private final MbcBand[] mBands;

        public Mbc(boolean inUse, boolean enabled, int bandCount) {
            super(inUse, enabled, bandCount);
            if (isInUse()) {
                this.mBands = new MbcBand[bandCount];
                for (int b = 0; b < bandCount; b++) {
                    float freq = 20000.0f;
                    if (bandCount > 1) {
                        freq = (float) Math.pow(10.0d, (double) (DynamicsProcessing.mMinFreqLog + ((((float) b) * (DynamicsProcessing.mMaxFreqLog - DynamicsProcessing.mMinFreqLog)) / ((float) (bandCount - 1)))));
                    }
                    this.mBands[b] = new MbcBand(true, freq, DynamicsProcessing.MBC_DEFAULT_ATTACK_TIME, DynamicsProcessing.MBC_DEFAULT_RELEASE_TIME, 1.0f, DynamicsProcessing.MBC_DEFAULT_THRESHOLD, 0.0f, DynamicsProcessing.MBC_DEFAULT_NOISE_GATE_THRESHOLD, 1.0f, 0.0f, 0.0f);
                }
                return;
            }
            this.mBands = null;
        }

        public Mbc(Mbc cfg) {
            super(cfg.isInUse(), cfg.isEnabled(), cfg.getBandCount());
            if (isInUse()) {
                this.mBands = new MbcBand[cfg.mBands.length];
                int b = 0;
                while (true) {
                    MbcBand[] mbcBandArr = this.mBands;
                    if (b < mbcBandArr.length) {
                        mbcBandArr[b] = new MbcBand(cfg.mBands[b]);
                        b++;
                    } else {
                        return;
                    }
                }
            } else {
                this.mBands = null;
            }
        }

        @Override // android.media.audiofx.DynamicsProcessing.BandStage, android.media.audiofx.DynamicsProcessing.Stage
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(super.toString());
            if (isInUse()) {
                sb.append("--->MbcBands: " + this.mBands.length + "\n");
                for (int b = 0; b < this.mBands.length; b++) {
                    sb.append(String.format("  Band %d\n", Integer.valueOf(b)));
                    sb.append(this.mBands[b].toString());
                }
            }
            return sb.toString();
        }

        private void checkBand(int band) {
            MbcBand[] mbcBandArr = this.mBands;
            if (mbcBandArr == null || band < 0 || band >= mbcBandArr.length) {
                throw new IllegalArgumentException("band index " + band + " out of bounds");
            }
        }

        public void setBand(int band, MbcBand bandCfg) {
            checkBand(band);
            this.mBands[band] = new MbcBand(bandCfg);
        }

        public MbcBand getBand(int band) {
            checkBand(band);
            return this.mBands[band];
        }
    }

    public static final class Limiter extends Stage {
        private float mAttackTime;
        private int mLinkGroup;
        private float mPostGain;
        private float mRatio;
        private float mReleaseTime;
        private float mThreshold;

        public Limiter(boolean inUse, boolean enabled, int linkGroup, float attackTime, float releaseTime, float ratio, float threshold, float postGain) {
            super(inUse, enabled);
            this.mLinkGroup = linkGroup;
            this.mAttackTime = attackTime;
            this.mReleaseTime = releaseTime;
            this.mRatio = ratio;
            this.mThreshold = threshold;
            this.mPostGain = postGain;
        }

        public Limiter(Limiter cfg) {
            super(cfg.isInUse(), cfg.isEnabled());
            this.mLinkGroup = cfg.mLinkGroup;
            this.mAttackTime = cfg.mAttackTime;
            this.mReleaseTime = cfg.mReleaseTime;
            this.mRatio = cfg.mRatio;
            this.mThreshold = cfg.mThreshold;
            this.mPostGain = cfg.mPostGain;
        }

        @Override // android.media.audiofx.DynamicsProcessing.Stage
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(super.toString());
            if (isInUse()) {
                sb.append(String.format(" LinkGroup: %d (group)\n", Integer.valueOf(this.mLinkGroup)));
                sb.append(String.format(" AttackTime: %f (ms)\n", Float.valueOf(this.mAttackTime)));
                sb.append(String.format(" ReleaseTime: %f (ms)\n", Float.valueOf(this.mReleaseTime)));
                sb.append(String.format(" Ratio: 1:%f\n", Float.valueOf(this.mRatio)));
                sb.append(String.format(" Threshold: %f (dB)\n", Float.valueOf(this.mThreshold)));
                sb.append(String.format(" PostGain: %f (dB)\n", Float.valueOf(this.mPostGain)));
            }
            return sb.toString();
        }

        public int getLinkGroup() {
            return this.mLinkGroup;
        }

        public void setLinkGroup(int linkGroup) {
            this.mLinkGroup = linkGroup;
        }

        public float getAttackTime() {
            return this.mAttackTime;
        }

        public void setAttackTime(float attackTime) {
            this.mAttackTime = attackTime;
        }

        public float getReleaseTime() {
            return this.mReleaseTime;
        }

        public void setReleaseTime(float releaseTime) {
            this.mReleaseTime = releaseTime;
        }

        public float getRatio() {
            return this.mRatio;
        }

        public void setRatio(float ratio) {
            this.mRatio = ratio;
        }

        public float getThreshold() {
            return this.mThreshold;
        }

        public void setThreshold(float threshold) {
            this.mThreshold = threshold;
        }

        public float getPostGain() {
            return this.mPostGain;
        }

        public void setPostGain(float postGain) {
            this.mPostGain = postGain;
        }
    }

    public static final class Channel {
        private float mInputGain;
        private Limiter mLimiter;
        private Mbc mMbc;
        private Eq mPostEq;
        private Eq mPreEq;

        public Channel(float inputGain, boolean preEqInUse, int preEqBandCount, boolean mbcInUse, int mbcBandCount, boolean postEqInUse, int postEqBandCount, boolean limiterInUse) {
            this.mInputGain = inputGain;
            this.mPreEq = new Eq(preEqInUse, true, preEqBandCount);
            this.mMbc = new Mbc(mbcInUse, true, mbcBandCount);
            this.mPostEq = new Eq(postEqInUse, true, postEqBandCount);
            this.mLimiter = new Limiter(limiterInUse, true, 0, 1.0f, 60.0f, 10.0f, DynamicsProcessing.LIMITER_DEFAULT_THRESHOLD, 0.0f);
        }

        public Channel(Channel cfg) {
            this.mInputGain = cfg.mInputGain;
            this.mPreEq = new Eq(cfg.mPreEq);
            this.mMbc = new Mbc(cfg.mMbc);
            this.mPostEq = new Eq(cfg.mPostEq);
            this.mLimiter = new Limiter(cfg.mLimiter);
        }

        public String toString() {
            return String.format(" InputGain: %f\n", Float.valueOf(this.mInputGain)) + "-->PreEq\n" + this.mPreEq.toString() + "-->MBC\n" + this.mMbc.toString() + "-->PostEq\n" + this.mPostEq.toString() + "-->Limiter\n" + this.mLimiter.toString();
        }

        public float getInputGain() {
            return this.mInputGain;
        }

        public void setInputGain(float inputGain) {
            this.mInputGain = inputGain;
        }

        public Eq getPreEq() {
            return this.mPreEq;
        }

        public void setPreEq(Eq preEq) {
            if (preEq.getBandCount() == this.mPreEq.getBandCount()) {
                this.mPreEq = new Eq(preEq);
                return;
            }
            throw new IllegalArgumentException("PreEqBandCount changed from " + this.mPreEq.getBandCount() + " to " + preEq.getBandCount());
        }

        public EqBand getPreEqBand(int band) {
            return this.mPreEq.getBand(band);
        }

        public void setPreEqBand(int band, EqBand preEqBand) {
            this.mPreEq.setBand(band, preEqBand);
        }

        public Mbc getMbc() {
            return this.mMbc;
        }

        public void setMbc(Mbc mbc) {
            if (mbc.getBandCount() == this.mMbc.getBandCount()) {
                this.mMbc = new Mbc(mbc);
                return;
            }
            throw new IllegalArgumentException("MbcBandCount changed from " + this.mMbc.getBandCount() + " to " + mbc.getBandCount());
        }

        public MbcBand getMbcBand(int band) {
            return this.mMbc.getBand(band);
        }

        public void setMbcBand(int band, MbcBand mbcBand) {
            this.mMbc.setBand(band, mbcBand);
        }

        public Eq getPostEq() {
            return this.mPostEq;
        }

        public void setPostEq(Eq postEq) {
            if (postEq.getBandCount() == this.mPostEq.getBandCount()) {
                this.mPostEq = new Eq(postEq);
                return;
            }
            throw new IllegalArgumentException("PostEqBandCount changed from " + this.mPostEq.getBandCount() + " to " + postEq.getBandCount());
        }

        public EqBand getPostEqBand(int band) {
            return this.mPostEq.getBand(band);
        }

        public void setPostEqBand(int band, EqBand postEqBand) {
            this.mPostEq.setBand(band, postEqBand);
        }

        public Limiter getLimiter() {
            return this.mLimiter;
        }

        public void setLimiter(Limiter limiter) {
            this.mLimiter = new Limiter(limiter);
        }
    }

    public static final class Config {
        private final Channel[] mChannel;
        private final int mChannelCount;
        private final boolean mLimiterInUse;
        private final int mMbcBandCount;
        private final boolean mMbcInUse;
        private final int mPostEqBandCount;
        private final boolean mPostEqInUse;
        private final int mPreEqBandCount;
        private final boolean mPreEqInUse;
        private final float mPreferredFrameDuration;
        private final int mVariant;

        public Config(int variant, float frameDurationMs, int channelCount, boolean preEqInUse, int preEqBandCount, boolean mbcInUse, int mbcBandCount, boolean postEqInUse, int postEqBandCount, boolean limiterInUse, Channel[] channel) {
            this.mVariant = variant;
            this.mPreferredFrameDuration = frameDurationMs;
            this.mChannelCount = channelCount;
            this.mPreEqInUse = preEqInUse;
            this.mPreEqBandCount = preEqBandCount;
            this.mMbcInUse = mbcInUse;
            this.mMbcBandCount = mbcBandCount;
            this.mPostEqInUse = postEqInUse;
            this.mPostEqBandCount = postEqBandCount;
            this.mLimiterInUse = limiterInUse;
            this.mChannel = new Channel[this.mChannelCount];
            for (int ch = 0; ch < this.mChannelCount; ch++) {
                if (ch < channel.length) {
                    this.mChannel[ch] = new Channel(channel[ch]);
                }
            }
        }

        public Config(int channelCount, Config cfg) {
            this.mVariant = cfg.mVariant;
            this.mPreferredFrameDuration = cfg.mPreferredFrameDuration;
            this.mChannelCount = cfg.mChannelCount;
            this.mPreEqInUse = cfg.mPreEqInUse;
            this.mPreEqBandCount = cfg.mPreEqBandCount;
            this.mMbcInUse = cfg.mMbcInUse;
            this.mMbcBandCount = cfg.mMbcBandCount;
            this.mPostEqInUse = cfg.mPostEqInUse;
            this.mPostEqBandCount = cfg.mPostEqBandCount;
            this.mLimiterInUse = cfg.mLimiterInUse;
            if (this.mChannelCount != cfg.mChannel.length) {
                throw new IllegalArgumentException("configuration channel counts differ " + this.mChannelCount + " !=" + cfg.mChannel.length);
            } else if (channelCount >= 1) {
                this.mChannel = new Channel[channelCount];
                for (int ch = 0; ch < channelCount; ch++) {
                    int i = this.mChannelCount;
                    if (ch < i) {
                        this.mChannel[ch] = new Channel(cfg.mChannel[ch]);
                    } else {
                        this.mChannel[ch] = new Channel(cfg.mChannel[i - 1]);
                    }
                }
            } else {
                throw new IllegalArgumentException("channel resizing less than 1 not allowed");
            }
        }

        public Config(Config cfg) {
            this(cfg.mChannelCount, cfg);
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Variant: %d\n", Integer.valueOf(this.mVariant)));
            sb.append(String.format("PreferredFrameDuration: %f\n", Float.valueOf(this.mPreferredFrameDuration)));
            sb.append(String.format("ChannelCount: %d\n", Integer.valueOf(this.mChannelCount)));
            sb.append(String.format("PreEq inUse: %b, bandCount:%d\n", Boolean.valueOf(this.mPreEqInUse), Integer.valueOf(this.mPreEqBandCount)));
            sb.append(String.format("Mbc inUse: %b, bandCount: %d\n", Boolean.valueOf(this.mMbcInUse), Integer.valueOf(this.mMbcBandCount)));
            sb.append(String.format("PostEq inUse: %b, bandCount: %d\n", Boolean.valueOf(this.mPostEqInUse), Integer.valueOf(this.mPostEqBandCount)));
            sb.append(String.format("Limiter inUse: %b\n", Boolean.valueOf(this.mLimiterInUse)));
            for (int ch = 0; ch < this.mChannel.length; ch++) {
                sb.append(String.format("==Channel %d\n", Integer.valueOf(ch)));
                sb.append(this.mChannel[ch].toString());
            }
            return sb.toString();
        }

        private void checkChannel(int channelIndex) {
            if (channelIndex < 0 || channelIndex >= this.mChannel.length) {
                throw new IllegalArgumentException("ChannelIndex out of bounds");
            }
        }

        public int getVariant() {
            return this.mVariant;
        }

        public float getPreferredFrameDuration() {
            return this.mPreferredFrameDuration;
        }

        public boolean isPreEqInUse() {
            return this.mPreEqInUse;
        }

        public int getPreEqBandCount() {
            return this.mPreEqBandCount;
        }

        public boolean isMbcInUse() {
            return this.mMbcInUse;
        }

        public int getMbcBandCount() {
            return this.mMbcBandCount;
        }

        public boolean isPostEqInUse() {
            return this.mPostEqInUse;
        }

        public int getPostEqBandCount() {
            return this.mPostEqBandCount;
        }

        public boolean isLimiterInUse() {
            return this.mLimiterInUse;
        }

        public Channel getChannelByChannelIndex(int channelIndex) {
            checkChannel(channelIndex);
            return this.mChannel[channelIndex];
        }

        public void setChannelTo(int channelIndex, Channel channel) {
            checkChannel(channelIndex);
            if (this.mMbcBandCount != channel.getMbc().getBandCount()) {
                throw new IllegalArgumentException("MbcBandCount changed from " + this.mMbcBandCount + " to " + channel.getPreEq().getBandCount());
            } else if (this.mPreEqBandCount != channel.getPreEq().getBandCount()) {
                throw new IllegalArgumentException("PreEqBandCount changed from " + this.mPreEqBandCount + " to " + channel.getPreEq().getBandCount());
            } else if (this.mPostEqBandCount == channel.getPostEq().getBandCount()) {
                this.mChannel[channelIndex] = new Channel(channel);
            } else {
                throw new IllegalArgumentException("PostEqBandCount changed from " + this.mPostEqBandCount + " to " + channel.getPostEq().getBandCount());
            }
        }

        public void setAllChannelsTo(Channel channel) {
            for (int ch = 0; ch < this.mChannel.length; ch++) {
                setChannelTo(ch, channel);
            }
        }

        public float getInputGainByChannelIndex(int channelIndex) {
            checkChannel(channelIndex);
            return this.mChannel[channelIndex].getInputGain();
        }

        public void setInputGainByChannelIndex(int channelIndex, float inputGain) {
            checkChannel(channelIndex);
            this.mChannel[channelIndex].setInputGain(inputGain);
        }

        public void setInputGainAllChannelsTo(float inputGain) {
            int ch = 0;
            while (true) {
                Channel[] channelArr = this.mChannel;
                if (ch < channelArr.length) {
                    channelArr[ch].setInputGain(inputGain);
                    ch++;
                } else {
                    return;
                }
            }
        }

        public Eq getPreEqByChannelIndex(int channelIndex) {
            checkChannel(channelIndex);
            return this.mChannel[channelIndex].getPreEq();
        }

        public void setPreEqByChannelIndex(int channelIndex, Eq preEq) {
            checkChannel(channelIndex);
            this.mChannel[channelIndex].setPreEq(preEq);
        }

        public void setPreEqAllChannelsTo(Eq preEq) {
            int ch = 0;
            while (true) {
                Channel[] channelArr = this.mChannel;
                if (ch < channelArr.length) {
                    channelArr[ch].setPreEq(preEq);
                    ch++;
                } else {
                    return;
                }
            }
        }

        public EqBand getPreEqBandByChannelIndex(int channelIndex, int band) {
            checkChannel(channelIndex);
            return this.mChannel[channelIndex].getPreEqBand(band);
        }

        public void setPreEqBandByChannelIndex(int channelIndex, int band, EqBand preEqBand) {
            checkChannel(channelIndex);
            this.mChannel[channelIndex].setPreEqBand(band, preEqBand);
        }

        public void setPreEqBandAllChannelsTo(int band, EqBand preEqBand) {
            int ch = 0;
            while (true) {
                Channel[] channelArr = this.mChannel;
                if (ch < channelArr.length) {
                    channelArr[ch].setPreEqBand(band, preEqBand);
                    ch++;
                } else {
                    return;
                }
            }
        }

        public Mbc getMbcByChannelIndex(int channelIndex) {
            checkChannel(channelIndex);
            return this.mChannel[channelIndex].getMbc();
        }

        public void setMbcByChannelIndex(int channelIndex, Mbc mbc) {
            checkChannel(channelIndex);
            this.mChannel[channelIndex].setMbc(mbc);
        }

        public void setMbcAllChannelsTo(Mbc mbc) {
            int ch = 0;
            while (true) {
                Channel[] channelArr = this.mChannel;
                if (ch < channelArr.length) {
                    channelArr[ch].setMbc(mbc);
                    ch++;
                } else {
                    return;
                }
            }
        }

        public MbcBand getMbcBandByChannelIndex(int channelIndex, int band) {
            checkChannel(channelIndex);
            return this.mChannel[channelIndex].getMbcBand(band);
        }

        public void setMbcBandByChannelIndex(int channelIndex, int band, MbcBand mbcBand) {
            checkChannel(channelIndex);
            this.mChannel[channelIndex].setMbcBand(band, mbcBand);
        }

        public void setMbcBandAllChannelsTo(int band, MbcBand mbcBand) {
            int ch = 0;
            while (true) {
                Channel[] channelArr = this.mChannel;
                if (ch < channelArr.length) {
                    channelArr[ch].setMbcBand(band, mbcBand);
                    ch++;
                } else {
                    return;
                }
            }
        }

        public Eq getPostEqByChannelIndex(int channelIndex) {
            checkChannel(channelIndex);
            return this.mChannel[channelIndex].getPostEq();
        }

        public void setPostEqByChannelIndex(int channelIndex, Eq postEq) {
            checkChannel(channelIndex);
            this.mChannel[channelIndex].setPostEq(postEq);
        }

        public void setPostEqAllChannelsTo(Eq postEq) {
            int ch = 0;
            while (true) {
                Channel[] channelArr = this.mChannel;
                if (ch < channelArr.length) {
                    channelArr[ch].setPostEq(postEq);
                    ch++;
                } else {
                    return;
                }
            }
        }

        public EqBand getPostEqBandByChannelIndex(int channelIndex, int band) {
            checkChannel(channelIndex);
            return this.mChannel[channelIndex].getPostEqBand(band);
        }

        public void setPostEqBandByChannelIndex(int channelIndex, int band, EqBand postEqBand) {
            checkChannel(channelIndex);
            this.mChannel[channelIndex].setPostEqBand(band, postEqBand);
        }

        public void setPostEqBandAllChannelsTo(int band, EqBand postEqBand) {
            int ch = 0;
            while (true) {
                Channel[] channelArr = this.mChannel;
                if (ch < channelArr.length) {
                    channelArr[ch].setPostEqBand(band, postEqBand);
                    ch++;
                } else {
                    return;
                }
            }
        }

        public Limiter getLimiterByChannelIndex(int channelIndex) {
            checkChannel(channelIndex);
            return this.mChannel[channelIndex].getLimiter();
        }

        public void setLimiterByChannelIndex(int channelIndex, Limiter limiter) {
            checkChannel(channelIndex);
            this.mChannel[channelIndex].setLimiter(limiter);
        }

        public void setLimiterAllChannelsTo(Limiter limiter) {
            int ch = 0;
            while (true) {
                Channel[] channelArr = this.mChannel;
                if (ch < channelArr.length) {
                    channelArr[ch].setLimiter(limiter);
                    ch++;
                } else {
                    return;
                }
            }
        }

        public static final class Builder {
            private Channel[] mChannel;
            private int mChannelCount;
            private boolean mLimiterInUse;
            private int mMbcBandCount;
            private boolean mMbcInUse;
            private int mPostEqBandCount;
            private boolean mPostEqInUse;
            private int mPreEqBandCount;
            private boolean mPreEqInUse;
            private float mPreferredFrameDuration = 10.0f;
            private int mVariant;

            public Builder(int variant, int channelCount, boolean preEqInUse, int preEqBandCount, boolean mbcInUse, int mbcBandCount, boolean postEqInUse, int postEqBandCount, boolean limiterInUse) {
                this.mVariant = variant;
                this.mChannelCount = channelCount;
                this.mPreEqInUse = preEqInUse;
                this.mPreEqBandCount = preEqBandCount;
                this.mMbcInUse = mbcInUse;
                this.mMbcBandCount = mbcBandCount;
                this.mPostEqInUse = postEqInUse;
                this.mPostEqBandCount = postEqBandCount;
                this.mLimiterInUse = limiterInUse;
                this.mChannel = new Channel[this.mChannelCount];
                for (int ch = 0; ch < this.mChannelCount; ch++) {
                    this.mChannel[ch] = new Channel(0.0f, this.mPreEqInUse, this.mPreEqBandCount, this.mMbcInUse, this.mMbcBandCount, this.mPostEqInUse, this.mPostEqBandCount, this.mLimiterInUse);
                }
            }

            private void checkChannel(int channelIndex) {
                if (channelIndex < 0 || channelIndex >= this.mChannel.length) {
                    throw new IllegalArgumentException("ChannelIndex out of bounds");
                }
            }

            public Builder setPreferredFrameDuration(float frameDuration) {
                if (frameDuration >= 0.0f) {
                    this.mPreferredFrameDuration = frameDuration;
                    return this;
                }
                throw new IllegalArgumentException("Expected positive frameDuration");
            }

            public Builder setInputGainByChannelIndex(int channelIndex, float inputGain) {
                checkChannel(channelIndex);
                this.mChannel[channelIndex].setInputGain(inputGain);
                return this;
            }

            public Builder setInputGainAllChannelsTo(float inputGain) {
                int ch = 0;
                while (true) {
                    Channel[] channelArr = this.mChannel;
                    if (ch >= channelArr.length) {
                        return this;
                    }
                    channelArr[ch].setInputGain(inputGain);
                    ch++;
                }
            }

            public Builder setChannelTo(int channelIndex, Channel channel) {
                checkChannel(channelIndex);
                if (this.mMbcBandCount != channel.getMbc().getBandCount()) {
                    throw new IllegalArgumentException("MbcBandCount changed from " + this.mMbcBandCount + " to " + channel.getPreEq().getBandCount());
                } else if (this.mPreEqBandCount != channel.getPreEq().getBandCount()) {
                    throw new IllegalArgumentException("PreEqBandCount changed from " + this.mPreEqBandCount + " to " + channel.getPreEq().getBandCount());
                } else if (this.mPostEqBandCount == channel.getPostEq().getBandCount()) {
                    this.mChannel[channelIndex] = new Channel(channel);
                    return this;
                } else {
                    throw new IllegalArgumentException("PostEqBandCount changed from " + this.mPostEqBandCount + " to " + channel.getPostEq().getBandCount());
                }
            }

            public Builder setAllChannelsTo(Channel channel) {
                for (int ch = 0; ch < this.mChannel.length; ch++) {
                    setChannelTo(ch, channel);
                }
                return this;
            }

            public Builder setPreEqByChannelIndex(int channelIndex, Eq preEq) {
                checkChannel(channelIndex);
                this.mChannel[channelIndex].setPreEq(preEq);
                return this;
            }

            public Builder setPreEqAllChannelsTo(Eq preEq) {
                for (int ch = 0; ch < this.mChannel.length; ch++) {
                    setPreEqByChannelIndex(ch, preEq);
                }
                return this;
            }

            public Builder setMbcByChannelIndex(int channelIndex, Mbc mbc) {
                checkChannel(channelIndex);
                this.mChannel[channelIndex].setMbc(mbc);
                return this;
            }

            public Builder setMbcAllChannelsTo(Mbc mbc) {
                for (int ch = 0; ch < this.mChannel.length; ch++) {
                    setMbcByChannelIndex(ch, mbc);
                }
                return this;
            }

            public Builder setPostEqByChannelIndex(int channelIndex, Eq postEq) {
                checkChannel(channelIndex);
                this.mChannel[channelIndex].setPostEq(postEq);
                return this;
            }

            public Builder setPostEqAllChannelsTo(Eq postEq) {
                for (int ch = 0; ch < this.mChannel.length; ch++) {
                    setPostEqByChannelIndex(ch, postEq);
                }
                return this;
            }

            public Builder setLimiterByChannelIndex(int channelIndex, Limiter limiter) {
                checkChannel(channelIndex);
                this.mChannel[channelIndex].setLimiter(limiter);
                return this;
            }

            public Builder setLimiterAllChannelsTo(Limiter limiter) {
                for (int ch = 0; ch < this.mChannel.length; ch++) {
                    setLimiterByChannelIndex(ch, limiter);
                }
                return this;
            }

            public Config build() {
                return new Config(this.mVariant, this.mPreferredFrameDuration, this.mChannelCount, this.mPreEqInUse, this.mPreEqBandCount, this.mMbcInUse, this.mMbcBandCount, this.mPostEqInUse, this.mPostEqBandCount, this.mLimiterInUse, this.mChannel);
            }
        }
    }

    public Channel getChannelByChannelIndex(int channelIndex) {
        return queryEngineByChannelIndex(channelIndex);
    }

    public void setChannelTo(int channelIndex, Channel channel) {
        updateEngineChannelByChannelIndex(channelIndex, channel);
    }

    public void setAllChannelsTo(Channel channel) {
        for (int ch = 0; ch < this.mChannelCount; ch++) {
            setChannelTo(ch, channel);
        }
    }

    public float getInputGainByChannelIndex(int channelIndex) {
        return getTwoFloat(32, channelIndex);
    }

    public void setInputGainbyChannel(int channelIndex, float inputGain) {
        setTwoFloat(32, channelIndex, inputGain);
    }

    public void setInputGainAllChannelsTo(float inputGain) {
        for (int ch = 0; ch < this.mChannelCount; ch++) {
            setInputGainbyChannel(ch, inputGain);
        }
    }

    public Eq getPreEqByChannelIndex(int channelIndex) {
        return queryEngineEqByChannelIndex(64, channelIndex);
    }

    public void setPreEqByChannelIndex(int channelIndex, Eq preEq) {
        updateEngineEqByChannelIndex(64, channelIndex, preEq);
    }

    public void setPreEqAllChannelsTo(Eq preEq) {
        for (int ch = 0; ch < this.mChannelCount; ch++) {
            setPreEqByChannelIndex(ch, preEq);
        }
    }

    public EqBand getPreEqBandByChannelIndex(int channelIndex, int band) {
        return queryEngineEqBandByChannelIndex(69, channelIndex, band);
    }

    public void setPreEqBandByChannelIndex(int channelIndex, int band, EqBand preEqBand) {
        updateEngineEqBandByChannelIndex(69, channelIndex, band, preEqBand);
    }

    public void setPreEqBandAllChannelsTo(int band, EqBand preEqBand) {
        for (int ch = 0; ch < this.mChannelCount; ch++) {
            setPreEqBandByChannelIndex(ch, band, preEqBand);
        }
    }

    public Mbc getMbcByChannelIndex(int channelIndex) {
        return queryEngineMbcByChannelIndex(channelIndex);
    }

    public void setMbcByChannelIndex(int channelIndex, Mbc mbc) {
        updateEngineMbcByChannelIndex(channelIndex, mbc);
    }

    public void setMbcAllChannelsTo(Mbc mbc) {
        for (int ch = 0; ch < this.mChannelCount; ch++) {
            setMbcByChannelIndex(ch, mbc);
        }
    }

    public MbcBand getMbcBandByChannelIndex(int channelIndex, int band) {
        return queryEngineMbcBandByChannelIndex(channelIndex, band);
    }

    public void setMbcBandByChannelIndex(int channelIndex, int band, MbcBand mbcBand) {
        updateEngineMbcBandByChannelIndex(channelIndex, band, mbcBand);
    }

    public void setMbcBandAllChannelsTo(int band, MbcBand mbcBand) {
        for (int ch = 0; ch < this.mChannelCount; ch++) {
            setMbcBandByChannelIndex(ch, band, mbcBand);
        }
    }

    public Eq getPostEqByChannelIndex(int channelIndex) {
        return queryEngineEqByChannelIndex(96, channelIndex);
    }

    public void setPostEqByChannelIndex(int channelIndex, Eq postEq) {
        updateEngineEqByChannelIndex(96, channelIndex, postEq);
    }

    public void setPostEqAllChannelsTo(Eq postEq) {
        for (int ch = 0; ch < this.mChannelCount; ch++) {
            setPostEqByChannelIndex(ch, postEq);
        }
    }

    public EqBand getPostEqBandByChannelIndex(int channelIndex, int band) {
        return queryEngineEqBandByChannelIndex(101, channelIndex, band);
    }

    public void setPostEqBandByChannelIndex(int channelIndex, int band, EqBand postEqBand) {
        updateEngineEqBandByChannelIndex(101, channelIndex, band, postEqBand);
    }

    public void setPostEqBandAllChannelsTo(int band, EqBand postEqBand) {
        for (int ch = 0; ch < this.mChannelCount; ch++) {
            setPostEqBandByChannelIndex(ch, band, postEqBand);
        }
    }

    public Limiter getLimiterByChannelIndex(int channelIndex) {
        return queryEngineLimiterByChannelIndex(channelIndex);
    }

    public void setLimiterByChannelIndex(int channelIndex, Limiter limiter) {
        updateEngineLimiterByChannelIndex(channelIndex, limiter);
    }

    public void setLimiterAllChannelsTo(Limiter limiter) {
        for (int ch = 0; ch < this.mChannelCount; ch++) {
            setLimiterByChannelIndex(ch, limiter);
        }
    }

    public int getChannelCount() {
        return getOneInt(16);
    }

    private void setEngineArchitecture(int variant, float preferredFrameDuration, boolean preEqInUse, int preEqBandCount, boolean mbcInUse, int mbcBandCount, boolean postEqInUse, int postEqBandCount, boolean limiterInUse) {
        setNumberArray(new Number[]{48}, new Number[]{Integer.valueOf(variant), Float.valueOf(preferredFrameDuration), Integer.valueOf(preEqInUse ? 1 : 0), Integer.valueOf(preEqBandCount), Integer.valueOf(mbcInUse ? 1 : 0), Integer.valueOf(mbcBandCount), Integer.valueOf(postEqInUse ? 1 : 0), Integer.valueOf(postEqBandCount), Integer.valueOf(limiterInUse ? 1 : 0)});
    }

    private void updateEngineEqBandByChannelIndex(int param, int channelIndex, int bandIndex, EqBand eqBand) {
        setNumberArray(new Number[]{Integer.valueOf(param), Integer.valueOf(channelIndex), Integer.valueOf(bandIndex)}, new Number[]{Integer.valueOf(eqBand.isEnabled() ? 1 : 0), Float.valueOf(eqBand.getCutoffFrequency()), Float.valueOf(eqBand.getGain())});
    }

    private Eq queryEngineEqByChannelIndex(int param, int channelIndex) {
        Number[] params = new Number[2];
        boolean z = false;
        params[0] = Integer.valueOf(param == 64 ? 64 : 96);
        params[1] = Integer.valueOf(channelIndex);
        Number[] values = {0, 0, 0};
        byte[] paramBytes = numberArrayToByteArray(params);
        byte[] valueBytes = numberArrayToByteArray(values);
        getParameter(paramBytes, valueBytes);
        byteArrayToNumberArray(valueBytes, values);
        int bandCount = values[2].intValue();
        boolean z2 = values[0].intValue() > 0;
        if (values[1].intValue() > 0) {
            z = true;
        }
        Eq eq = new Eq(z2, z, bandCount);
        for (int b = 0; b < bandCount; b++) {
            eq.setBand(b, queryEngineEqBandByChannelIndex(param == 64 ? 69 : 101, channelIndex, b));
        }
        return eq;
    }

    private EqBand queryEngineEqBandByChannelIndex(int param, int channelIndex, int bandIndex) {
        boolean z = false;
        Number[] params = {Integer.valueOf(param), Integer.valueOf(channelIndex), Integer.valueOf(bandIndex)};
        Float valueOf = Float.valueOf(0.0f);
        Number[] values = {0, valueOf, valueOf};
        byte[] paramBytes = numberArrayToByteArray(params);
        byte[] valueBytes = numberArrayToByteArray(values);
        getParameter(paramBytes, valueBytes);
        byteArrayToNumberArray(valueBytes, values);
        if (values[0].intValue() > 0) {
            z = true;
        }
        return new EqBand(z, values[1].floatValue(), values[2].floatValue());
    }

    private void updateEngineEqByChannelIndex(int param, int channelIndex, Eq eq) {
        int bandCount = eq.getBandCount();
        setNumberArray(new Number[]{Integer.valueOf(param), Integer.valueOf(channelIndex)}, new Number[]{Integer.valueOf(eq.isInUse() ? 1 : 0), Integer.valueOf(eq.isEnabled() ? 1 : 0), Integer.valueOf(bandCount)});
        for (int b = 0; b < bandCount; b++) {
            updateEngineEqBandByChannelIndex(param == 64 ? 69 : 101, channelIndex, b, eq.getBand(b));
        }
    }

    private Mbc queryEngineMbcByChannelIndex(int channelIndex) {
        boolean z = false;
        Number[] values = {0, 0, 0};
        byte[] paramBytes = numberArrayToByteArray(new Number[]{80, Integer.valueOf(channelIndex)});
        byte[] valueBytes = numberArrayToByteArray(values);
        getParameter(paramBytes, valueBytes);
        byteArrayToNumberArray(valueBytes, values);
        int bandCount = values[2].intValue();
        boolean z2 = values[0].intValue() > 0;
        if (values[1].intValue() > 0) {
            z = true;
        }
        Mbc mbc = new Mbc(z2, z, bandCount);
        for (int b = 0; b < bandCount; b++) {
            mbc.setBand(b, queryEngineMbcBandByChannelIndex(channelIndex, b));
        }
        return mbc;
    }

    private MbcBand queryEngineMbcBandByChannelIndex(int channelIndex, int bandIndex) {
        Number[] params = {85, Integer.valueOf(channelIndex), Integer.valueOf(bandIndex)};
        Float valueOf = Float.valueOf(0.0f);
        Number[] values = {0, valueOf, valueOf, valueOf, valueOf, valueOf, valueOf, valueOf, valueOf, valueOf, valueOf};
        byte[] paramBytes = numberArrayToByteArray(params);
        byte[] valueBytes = numberArrayToByteArray(values);
        getParameter(paramBytes, valueBytes);
        byteArrayToNumberArray(valueBytes, values);
        return new MbcBand(values[0].intValue() > 0, values[1].floatValue(), values[2].floatValue(), values[3].floatValue(), values[4].floatValue(), values[5].floatValue(), values[6].floatValue(), values[7].floatValue(), values[8].floatValue(), values[9].floatValue(), values[10].floatValue());
    }

    private void updateEngineMbcBandByChannelIndex(int channelIndex, int bandIndex, MbcBand mbcBand) {
        setNumberArray(new Number[]{85, Integer.valueOf(channelIndex), Integer.valueOf(bandIndex)}, new Number[]{Integer.valueOf(mbcBand.isEnabled() ? 1 : 0), Float.valueOf(mbcBand.getCutoffFrequency()), Float.valueOf(mbcBand.getAttackTime()), Float.valueOf(mbcBand.getReleaseTime()), Float.valueOf(mbcBand.getRatio()), Float.valueOf(mbcBand.getThreshold()), Float.valueOf(mbcBand.getKneeWidth()), Float.valueOf(mbcBand.getNoiseGateThreshold()), Float.valueOf(mbcBand.getExpanderRatio()), Float.valueOf(mbcBand.getPreGain()), Float.valueOf(mbcBand.getPostGain())});
    }

    private void updateEngineMbcByChannelIndex(int channelIndex, Mbc mbc) {
        int bandCount = mbc.getBandCount();
        setNumberArray(new Number[]{80, Integer.valueOf(channelIndex)}, new Number[]{Integer.valueOf(mbc.isInUse() ? 1 : 0), Integer.valueOf(mbc.isEnabled() ? 1 : 0), Integer.valueOf(bandCount)});
        for (int b = 0; b < bandCount; b++) {
            updateEngineMbcBandByChannelIndex(channelIndex, b, mbc.getBand(b));
        }
    }

    private void updateEngineLimiterByChannelIndex(int channelIndex, Limiter limiter) {
        setNumberArray(new Number[]{112, Integer.valueOf(channelIndex)}, new Number[]{Integer.valueOf(limiter.isInUse() ? 1 : 0), Integer.valueOf(limiter.isEnabled() ? 1 : 0), Integer.valueOf(limiter.getLinkGroup()), Float.valueOf(limiter.getAttackTime()), Float.valueOf(limiter.getReleaseTime()), Float.valueOf(limiter.getRatio()), Float.valueOf(limiter.getThreshold()), Float.valueOf(limiter.getPostGain())});
    }

    private Limiter queryEngineLimiterByChannelIndex(int channelIndex) {
        Number[] params = {112, Integer.valueOf(channelIndex)};
        Float valueOf = Float.valueOf(0.0f);
        Number[] values = {0, 0, 0, valueOf, valueOf, valueOf, valueOf, valueOf};
        byte[] paramBytes = numberArrayToByteArray(params);
        byte[] valueBytes = numberArrayToByteArray(values);
        getParameter(paramBytes, valueBytes);
        byteArrayToNumberArray(valueBytes, values);
        return new Limiter(values[0].intValue() > 0, values[1].intValue() > 0, values[2].intValue(), values[3].floatValue(), values[4].floatValue(), values[5].floatValue(), values[6].floatValue(), values[7].floatValue());
    }

    private Channel queryEngineByChannelIndex(int channelIndex) {
        float inputGain = getTwoFloat(32, channelIndex);
        Eq preEq = queryEngineEqByChannelIndex(64, channelIndex);
        Mbc mbc = queryEngineMbcByChannelIndex(channelIndex);
        Eq postEq = queryEngineEqByChannelIndex(96, channelIndex);
        Limiter limiter = queryEngineLimiterByChannelIndex(channelIndex);
        Channel channel = new Channel(inputGain, preEq.isInUse(), preEq.getBandCount(), mbc.isInUse(), mbc.getBandCount(), postEq.isInUse(), postEq.getBandCount(), limiter.isInUse());
        channel.setInputGain(inputGain);
        channel.setPreEq(preEq);
        channel.setMbc(mbc);
        channel.setPostEq(postEq);
        channel.setLimiter(limiter);
        return channel;
    }

    private void updateEngineChannelByChannelIndex(int channelIndex, Channel channel) {
        setTwoFloat(32, channelIndex, channel.getInputGain());
        updateEngineEqByChannelIndex(64, channelIndex, channel.getPreEq());
        updateEngineMbcByChannelIndex(channelIndex, channel.getMbc());
        updateEngineEqByChannelIndex(96, channelIndex, channel.getPostEq());
        updateEngineLimiterByChannelIndex(channelIndex, channel.getLimiter());
    }

    private int getOneInt(int param) {
        int[] result = new int[1];
        checkStatus(getParameter(new int[]{param}, result));
        return result[0];
    }

    private void setTwoFloat(int param, int paramA, float valueSet) {
        checkStatus(setParameter(new int[]{param, paramA}, floatToByteArray(valueSet)));
    }

    private byte[] numberArrayToByteArray(Number[] values) {
        int expectedBytes = 0;
        for (int i = 0; i < values.length; i++) {
            if (!(values[i] instanceof Integer) && !(values[i] instanceof Float)) {
                throw new IllegalArgumentException("unknown value type " + values[i].getClass());
            }
            expectedBytes += 4;
        }
        ByteBuffer converter = ByteBuffer.allocate(expectedBytes);
        converter.order(ByteOrder.nativeOrder());
        for (int i2 = 0; i2 < values.length; i2++) {
            if (values[i2] instanceof Integer) {
                converter.putInt(values[i2].intValue());
            } else if (values[i2] instanceof Float) {
                converter.putFloat(values[i2].floatValue());
            }
        }
        return converter.array();
    }

    private void byteArrayToNumberArray(byte[] valuesIn, Number[] valuesOut) {
        int inIndex = 0;
        int outIndex = 0;
        while (inIndex < valuesIn.length && outIndex < valuesOut.length) {
            if (valuesOut[outIndex] instanceof Integer) {
                valuesOut[outIndex] = Integer.valueOf(byteArrayToInt(valuesIn, inIndex));
                inIndex += 4;
                outIndex++;
            } else if (valuesOut[outIndex] instanceof Float) {
                valuesOut[outIndex] = Float.valueOf(byteArrayToFloat(valuesIn, inIndex));
                inIndex += 4;
                outIndex++;
            } else {
                throw new IllegalArgumentException("can't convert " + valuesOut[outIndex].getClass());
            }
        }
        if (outIndex != valuesOut.length) {
            throw new IllegalArgumentException("only converted " + outIndex + " values out of " + valuesOut.length + " expected");
        }
    }

    private void setNumberArray(Number[] params, Number[] values) {
        checkStatus(setParameter(numberArrayToByteArray(params), numberArrayToByteArray(values)));
    }

    private float getTwoFloat(int param, int paramA) {
        byte[] result = new byte[4];
        checkStatus(getParameter(new int[]{param, paramA}, result));
        return byteArrayToFloat(result);
    }

    private void updateEffectArchitecture() {
        this.mChannelCount = getChannelCount();
    }

    private class BaseParameterListener implements AudioEffect.OnParameterChangeListener {
        private BaseParameterListener() {
        }

        @Override // android.media.audiofx.AudioEffect.OnParameterChangeListener
        public void onParameterChange(AudioEffect effect, int status, byte[] param, byte[] value) {
            if (status == 0) {
                OnParameterChangeListener l = null;
                synchronized (DynamicsProcessing.this.mParamListenerLock) {
                    if (DynamicsProcessing.this.mParamListener != null) {
                        l = DynamicsProcessing.this.mParamListener;
                    }
                }
                if (l != null) {
                    int p = -1;
                    int v = Integer.MIN_VALUE;
                    if (param.length == 4) {
                        p = AudioEffect.byteArrayToInt(param, 0);
                    }
                    if (value.length == 4) {
                        v = AudioEffect.byteArrayToInt(value, 0);
                    }
                    if (p != -1 && v != Integer.MIN_VALUE) {
                        l.onParameterChange(DynamicsProcessing.this, p, v);
                    }
                }
            }
        }
    }

    public void setParameterListener(OnParameterChangeListener listener) {
        synchronized (this.mParamListenerLock) {
            if (this.mParamListener == null) {
                this.mBaseParamListener = new BaseParameterListener();
                super.setParameterListener(this.mBaseParamListener);
            }
            this.mParamListener = listener;
        }
    }

    public static class Settings {
        public int channelCount;
        public float[] inputGain;

        public Settings() {
        }

        public Settings(String settings) {
            StringTokenizer st = new StringTokenizer(settings, "=;");
            if (st.countTokens() == 3) {
                String key = st.nextToken();
                if (key.equals(DynamicsProcessing.TAG)) {
                    try {
                        key = st.nextToken();
                        if (key.equals("channelCount")) {
                            this.channelCount = Short.parseShort(st.nextToken());
                            if (this.channelCount > 32) {
                                throw new IllegalArgumentException("too many channels Settings:" + settings);
                            } else if (st.countTokens() == this.channelCount * 1) {
                                this.inputGain = new float[this.channelCount];
                                for (int ch = 0; ch < this.channelCount; ch++) {
                                    String key2 = st.nextToken();
                                    if (key2.equals(ch + "_inputGain")) {
                                        this.inputGain[ch] = Float.parseFloat(st.nextToken());
                                    } else {
                                        throw new IllegalArgumentException("invalid key name: " + key2);
                                    }
                                }
                            } else {
                                throw new IllegalArgumentException("settings: " + settings);
                            }
                        } else {
                            throw new IllegalArgumentException("invalid key name: " + key);
                        }
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("invalid value for key: " + key);
                    }
                } else {
                    throw new IllegalArgumentException("invalid settings for DynamicsProcessing: " + key);
                }
            } else {
                throw new IllegalArgumentException("settings: " + settings);
            }
        }

        public String toString() {
            String str = new String("DynamicsProcessing;channelCount=" + Integer.toString(this.channelCount));
            for (int ch = 0; ch < this.channelCount; ch++) {
                str = str.concat(";" + ch + "_inputGain=" + Float.toString(this.inputGain[ch]));
            }
            return str;
        }
    }

    public Settings getProperties() {
        Settings settings = new Settings();
        settings.channelCount = getChannelCount();
        if (settings.channelCount <= 32) {
            settings.inputGain = new float[settings.channelCount];
            for (int ch = 0; ch < settings.channelCount; ch++) {
            }
            return settings;
        }
        throw new IllegalArgumentException("too many channels Settings:" + settings);
    }

    public void setProperties(Settings settings) {
        if (settings.channelCount == settings.inputGain.length && settings.channelCount == this.mChannelCount) {
            for (int ch = 0; ch < this.mChannelCount; ch++) {
            }
            return;
        }
        throw new IllegalArgumentException("settings invalid channel count: " + settings.channelCount);
    }
}
