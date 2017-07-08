package android.filterpacks.videoproc;

import android.bluetooth.BluetoothAssignedNumbers;
import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;
import android.filterfw.core.Frame;
import android.filterfw.core.FrameFormat;
import android.filterfw.core.GLFrame;
import android.filterfw.core.GenerateFieldPort;
import android.filterfw.core.GenerateFinalPort;
import android.filterfw.core.MutableFrameFormat;
import android.filterfw.core.ShaderProgram;
import android.filterfw.format.ImageFormat;
import android.opengl.GLES20;
import android.os.Process;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.telecom.AudioState;
import android.util.Log;
import java.util.Arrays;

public class BackDropperFilter extends Filter {
    private static final float DEFAULT_ACCEPT_STDDEV = 0.85f;
    private static final float DEFAULT_ADAPT_RATE_BG = 0.0f;
    private static final float DEFAULT_ADAPT_RATE_FG = 0.0f;
    private static final String DEFAULT_AUTO_WB_SCALE = "0.25";
    private static final float[] DEFAULT_BG_FIT_TRANSFORM = null;
    private static final float DEFAULT_EXPOSURE_CHANGE = 1.0f;
    private static final int DEFAULT_HIER_LRG_EXPONENT = 3;
    private static final float DEFAULT_HIER_LRG_SCALE = 0.7f;
    private static final int DEFAULT_HIER_MID_EXPONENT = 2;
    private static final float DEFAULT_HIER_MID_SCALE = 0.6f;
    private static final int DEFAULT_HIER_SML_EXPONENT = 0;
    private static final float DEFAULT_HIER_SML_SCALE = 0.5f;
    private static final float DEFAULT_LEARNING_ADAPT_RATE = 0.2f;
    private static final int DEFAULT_LEARNING_DONE_THRESHOLD = 20;
    private static final int DEFAULT_LEARNING_DURATION = 40;
    private static final int DEFAULT_LEARNING_VERIFY_DURATION = 10;
    private static final float DEFAULT_MASK_BLEND_BG = 0.65f;
    private static final float DEFAULT_MASK_BLEND_FG = 0.95f;
    private static final int DEFAULT_MASK_HEIGHT_EXPONENT = 8;
    private static final float DEFAULT_MASK_VERIFY_RATE = 0.25f;
    private static final int DEFAULT_MASK_WIDTH_EXPONENT = 8;
    private static final float DEFAULT_UV_SCALE_FACTOR = 1.35f;
    private static final float DEFAULT_WHITE_BALANCE_BLUE_CHANGE = 0.0f;
    private static final float DEFAULT_WHITE_BALANCE_RED_CHANGE = 0.0f;
    private static final int DEFAULT_WHITE_BALANCE_TOGGLE = 0;
    private static final float DEFAULT_Y_SCALE_FACTOR = 0.4f;
    private static final String DISTANCE_STORAGE_SCALE = "0.6";
    private static final String MASK_SMOOTH_EXPONENT = "2.0";
    private static final String MIN_VARIANCE = "3.0";
    private static final String RGB_TO_YUV_MATRIX = "0.299, -0.168736,  0.5,      0.000, 0.587, -0.331264, -0.418688, 0.000, 0.114,  0.5,      -0.081312, 0.000, 0.000,  0.5,       0.5,      1.000 ";
    private static final String TAG = "BackDropperFilter";
    private static final String VARIANCE_STORAGE_SCALE = "5.0";
    private static final String mAutomaticWhiteBalance = "uniform sampler2D tex_sampler_0;\nuniform sampler2D tex_sampler_1;\nuniform float pyramid_depth;\nuniform bool autowb_toggle;\nvarying vec2 v_texcoord;\nvoid main() {\n   vec4 mean_video = texture2D(tex_sampler_0, v_texcoord, pyramid_depth);\n   vec4 mean_bg = texture2D(tex_sampler_1, v_texcoord, pyramid_depth);\n   float green_normalizer = mean_video.g / mean_bg.g;\n   vec4 adjusted_value = vec4(mean_bg.r / mean_video.r * green_normalizer, 1., \n                         mean_bg.b / mean_video.b * green_normalizer, 1.) * auto_wb_scale; \n   gl_FragColor = autowb_toggle ? adjusted_value : vec4(auto_wb_scale);\n}\n";
    private static final String mBgDistanceShader = "uniform sampler2D tex_sampler_0;\nuniform sampler2D tex_sampler_1;\nuniform sampler2D tex_sampler_2;\nuniform float subsample_level;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec4 fg_rgb = texture2D(tex_sampler_0, v_texcoord, subsample_level);\n  vec4 fg = coeff_yuv * vec4(fg_rgb.rgb, 1.);\n  vec4 mean = texture2D(tex_sampler_1, v_texcoord);\n  vec4 variance = inv_var_scale * texture2D(tex_sampler_2, v_texcoord);\n\n  float dist_y = gauss_dist_y(fg.r, mean.r, variance.r);\n  float dist_uv = gauss_dist_uv(fg.gb, mean.gb, variance.gb);\n  gl_FragColor = vec4(0.5*fg.rg, dist_scale*dist_y, dist_scale*dist_uv);\n}\n";
    private static final String mBgMaskShader = "uniform sampler2D tex_sampler_0;\nuniform float accept_variance;\nuniform vec2 yuv_weights;\nuniform float scale_lrg;\nuniform float scale_mid;\nuniform float scale_sml;\nuniform float exp_lrg;\nuniform float exp_mid;\nuniform float exp_sml;\nvarying vec2 v_texcoord;\nbool is_fg(vec2 dist_yc, float accept_variance) {\n  return ( dot(yuv_weights, dist_yc) >= accept_variance );\n}\nvoid main() {\n  vec4 dist_lrg_sc = texture2D(tex_sampler_0, v_texcoord, exp_lrg);\n  vec4 dist_mid_sc = texture2D(tex_sampler_0, v_texcoord, exp_mid);\n  vec4 dist_sml_sc = texture2D(tex_sampler_0, v_texcoord, exp_sml);\n  vec2 dist_lrg = inv_dist_scale * dist_lrg_sc.ba;\n  vec2 dist_mid = inv_dist_scale * dist_mid_sc.ba;\n  vec2 dist_sml = inv_dist_scale * dist_sml_sc.ba;\n  vec2 norm_dist = 0.75 * dist_sml / accept_variance;\n  bool is_fg_lrg = is_fg(dist_lrg, accept_variance * scale_lrg);\n  bool is_fg_mid = is_fg_lrg || is_fg(dist_mid, accept_variance * scale_mid);\n  float is_fg_sml =\n      float(is_fg_mid || is_fg(dist_sml, accept_variance * scale_sml));\n  float alpha = 0.5 * is_fg_sml + 0.3 * float(is_fg_mid) + 0.2 * float(is_fg_lrg);\n  gl_FragColor = vec4(alpha, norm_dist, is_fg_sml);\n}\n";
    private static final String mBgSubtractForceShader = "  vec4 ghost_rgb = (fg_adjusted * 0.7 + vec4(0.3,0.3,0.4,0.))*0.65 + \n                   0.35*bg_rgb;\n  float glow_start = 0.75 * mask_blend_bg; \n  float glow_max   = mask_blend_bg; \n  gl_FragColor = mask.a < glow_start ? bg_rgb : \n                 mask.a < glow_max ? mix(bg_rgb, vec4(0.9,0.9,1.0,1.0), \n                                     (mask.a - glow_start) / (glow_max - glow_start) ) : \n                 mask.a < mask_blend_fg ? mix(vec4(0.9,0.9,1.0,1.0), ghost_rgb, \n                                    (mask.a - glow_max) / (mask_blend_fg - glow_max) ) : \n                 ghost_rgb;\n}\n";
    private static final String mBgSubtractShader = "uniform mat3 bg_fit_transform;\nuniform float mask_blend_bg;\nuniform float mask_blend_fg;\nuniform float exposure_change;\nuniform float whitebalancered_change;\nuniform float whitebalanceblue_change;\nuniform sampler2D tex_sampler_0;\nuniform sampler2D tex_sampler_1;\nuniform sampler2D tex_sampler_2;\nuniform sampler2D tex_sampler_3;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec2 bg_texcoord = (bg_fit_transform * vec3(v_texcoord, 1.)).xy;\n  vec4 bg_rgb = texture2D(tex_sampler_1, bg_texcoord);\n  vec4 wb_auto_scale = texture2D(tex_sampler_3, v_texcoord) * exposure_change / auto_wb_scale;\n  vec4 wb_manual_scale = vec4(1. + whitebalancered_change, 1., 1. + whitebalanceblue_change, 1.);\n  vec4 fg_rgb = texture2D(tex_sampler_0, v_texcoord);\n  vec4 fg_adjusted = fg_rgb * wb_manual_scale * wb_auto_scale;\n  vec4 mask = texture2D(tex_sampler_2, v_texcoord, \n                      2.0);\n  float alpha = smoothstep(mask_blend_bg, mask_blend_fg, mask.a);\n  gl_FragColor = mix(bg_rgb, fg_adjusted, alpha);\n";
    private static final String[] mDebugOutputNames = null;
    private static final String[] mInputNames = null;
    private static final String mMaskVerifyShader = "uniform sampler2D tex_sampler_0;\nuniform sampler2D tex_sampler_1;\nuniform float verify_rate;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec4 lastmask = texture2D(tex_sampler_0, v_texcoord);\n  vec4 mask = texture2D(tex_sampler_1, v_texcoord);\n  float newmask = mix(lastmask.a, mask.a, verify_rate);\n  gl_FragColor = vec4(0., 0., 0., newmask);\n}\n";
    private static final String[] mOutputNames = null;
    private static String mSharedUtilShader = null;
    private static final String mUpdateBgModelMeanShader = "uniform sampler2D tex_sampler_0;\nuniform sampler2D tex_sampler_1;\nuniform sampler2D tex_sampler_2;\nuniform float subsample_level;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec4 fg_rgb = texture2D(tex_sampler_0, v_texcoord, subsample_level);\n  vec4 fg = coeff_yuv * vec4(fg_rgb.rgb, 1.);\n  vec4 mean = texture2D(tex_sampler_1, v_texcoord);\n  vec4 mask = texture2D(tex_sampler_2, v_texcoord, \n                      2.0);\n\n  float alpha = local_adapt_rate(mask.a);\n  vec4 new_mean = mix(mean, fg, alpha);\n  gl_FragColor = new_mean;\n}\n";
    private static final String mUpdateBgModelVarianceShader = "uniform sampler2D tex_sampler_0;\nuniform sampler2D tex_sampler_1;\nuniform sampler2D tex_sampler_2;\nuniform sampler2D tex_sampler_3;\nuniform float subsample_level;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec4 fg_rgb = texture2D(tex_sampler_0, v_texcoord, subsample_level);\n  vec4 fg = coeff_yuv * vec4(fg_rgb.rgb, 1.);\n  vec4 mean = texture2D(tex_sampler_1, v_texcoord);\n  vec4 variance = inv_var_scale * texture2D(tex_sampler_2, v_texcoord);\n  vec4 mask = texture2D(tex_sampler_3, v_texcoord, \n                      2.0);\n\n  float alpha = local_adapt_rate(mask.a);\n  vec4 cur_variance = (fg-mean)*(fg-mean);\n  vec4 new_variance = mix(variance, cur_variance, alpha);\n  new_variance = max(new_variance, vec4(min_variance));\n  gl_FragColor = var_scale * new_variance;\n}\n";
    private final int BACKGROUND_FILL_CROP;
    private final int BACKGROUND_FIT;
    private final int BACKGROUND_STRETCH;
    private ShaderProgram copyShaderProgram;
    private boolean isOpen;
    @GenerateFieldPort(hasDefault = true, name = "acceptStddev")
    private float mAcceptStddev;
    @GenerateFieldPort(hasDefault = true, name = "adaptRateBg")
    private float mAdaptRateBg;
    @GenerateFieldPort(hasDefault = true, name = "adaptRateFg")
    private float mAdaptRateFg;
    @GenerateFieldPort(hasDefault = true, name = "learningAdaptRate")
    private float mAdaptRateLearning;
    private GLFrame mAutoWB;
    @GenerateFieldPort(hasDefault = true, name = "autowbToggle")
    private int mAutoWBToggle;
    private ShaderProgram mAutomaticWhiteBalanceProgram;
    private MutableFrameFormat mAverageFormat;
    @GenerateFieldPort(hasDefault = true, name = "backgroundFitMode")
    private int mBackgroundFitMode;
    private boolean mBackgroundFitModeChanged;
    private ShaderProgram mBgDistProgram;
    private GLFrame mBgInput;
    private ShaderProgram mBgMaskProgram;
    private GLFrame[] mBgMean;
    private ShaderProgram mBgSubtractProgram;
    private ShaderProgram mBgUpdateMeanProgram;
    private ShaderProgram mBgUpdateVarianceProgram;
    private GLFrame[] mBgVariance;
    @GenerateFieldPort(hasDefault = true, name = "chromaScale")
    private float mChromaScale;
    private ShaderProgram mCopyOutProgram;
    private GLFrame mDistance;
    @GenerateFieldPort(hasDefault = true, name = "exposureChange")
    private float mExposureChange;
    private int mFrameCount;
    @GenerateFieldPort(hasDefault = true, name = "hierLrgExp")
    private int mHierarchyLrgExp;
    @GenerateFieldPort(hasDefault = true, name = "hierLrgScale")
    private float mHierarchyLrgScale;
    @GenerateFieldPort(hasDefault = true, name = "hierMidExp")
    private int mHierarchyMidExp;
    @GenerateFieldPort(hasDefault = true, name = "hierMidScale")
    private float mHierarchyMidScale;
    @GenerateFieldPort(hasDefault = true, name = "hierSmlExp")
    private int mHierarchySmlExp;
    @GenerateFieldPort(hasDefault = true, name = "hierSmlScale")
    private float mHierarchySmlScale;
    @GenerateFieldPort(hasDefault = true, name = "learningDoneListener")
    private LearningDoneListener mLearningDoneListener;
    @GenerateFieldPort(hasDefault = true, name = "learningDuration")
    private int mLearningDuration;
    @GenerateFieldPort(hasDefault = true, name = "learningVerifyDuration")
    private int mLearningVerifyDuration;
    private final boolean mLogVerbose;
    @GenerateFieldPort(hasDefault = true, name = "lumScale")
    private float mLumScale;
    private GLFrame mMask;
    private GLFrame mMaskAverage;
    @GenerateFieldPort(hasDefault = true, name = "maskBg")
    private float mMaskBg;
    @GenerateFieldPort(hasDefault = true, name = "maskFg")
    private float mMaskFg;
    private MutableFrameFormat mMaskFormat;
    @GenerateFieldPort(hasDefault = true, name = "maskHeightExp")
    private int mMaskHeightExp;
    private GLFrame[] mMaskVerify;
    private ShaderProgram mMaskVerifyProgram;
    @GenerateFieldPort(hasDefault = true, name = "maskWidthExp")
    private int mMaskWidthExp;
    private MutableFrameFormat mMemoryFormat;
    @GenerateFieldPort(hasDefault = true, name = "mirrorBg")
    private boolean mMirrorBg;
    @GenerateFieldPort(hasDefault = true, name = "orientation")
    private int mOrientation;
    private FrameFormat mOutputFormat;
    private boolean mPingPong;
    @GenerateFinalPort(hasDefault = true, name = "provideDebugOutputs")
    private boolean mProvideDebugOutputs;
    private int mPyramidDepth;
    private float mRelativeAspect;
    private boolean mStartLearning;
    private int mSubsampleLevel;
    @GenerateFieldPort(hasDefault = true, name = "useTheForce")
    private boolean mUseTheForce;
    @GenerateFieldPort(hasDefault = true, name = "maskVerifyRate")
    private float mVerifyRate;
    private GLFrame mVideoInput;
    @GenerateFieldPort(hasDefault = true, name = "whitebalanceblueChange")
    private float mWhiteBalanceBlueChange;
    @GenerateFieldPort(hasDefault = true, name = "whitebalanceredChange")
    private float mWhiteBalanceRedChange;
    private long startTime;

    public interface LearningDoneListener {
        void onLearningDone(BackDropperFilter backDropperFilter);
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.filterpacks.videoproc.BackDropperFilter.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.filterpacks.videoproc.BackDropperFilter.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.filterpacks.videoproc.BackDropperFilter.<clinit>():void");
    }

    public BackDropperFilter(String name) {
        super(name);
        this.BACKGROUND_STRETCH = DEFAULT_WHITE_BALANCE_TOGGLE;
        this.BACKGROUND_FIT = 1;
        this.BACKGROUND_FILL_CROP = DEFAULT_HIER_MID_EXPONENT;
        this.mBackgroundFitMode = DEFAULT_HIER_MID_EXPONENT;
        this.mLearningDuration = DEFAULT_LEARNING_DURATION;
        this.mLearningVerifyDuration = DEFAULT_LEARNING_VERIFY_DURATION;
        this.mAcceptStddev = DEFAULT_ACCEPT_STDDEV;
        this.mHierarchyLrgScale = DEFAULT_HIER_LRG_SCALE;
        this.mHierarchyMidScale = DEFAULT_HIER_MID_SCALE;
        this.mHierarchySmlScale = DEFAULT_HIER_SML_SCALE;
        this.mMaskWidthExp = DEFAULT_MASK_WIDTH_EXPONENT;
        this.mMaskHeightExp = DEFAULT_MASK_WIDTH_EXPONENT;
        this.mHierarchyLrgExp = DEFAULT_HIER_LRG_EXPONENT;
        this.mHierarchyMidExp = DEFAULT_HIER_MID_EXPONENT;
        this.mHierarchySmlExp = DEFAULT_WHITE_BALANCE_TOGGLE;
        this.mLumScale = DEFAULT_Y_SCALE_FACTOR;
        this.mChromaScale = DEFAULT_UV_SCALE_FACTOR;
        this.mMaskBg = DEFAULT_MASK_BLEND_BG;
        this.mMaskFg = DEFAULT_MASK_BLEND_FG;
        this.mExposureChange = DEFAULT_EXPOSURE_CHANGE;
        this.mWhiteBalanceRedChange = DEFAULT_WHITE_BALANCE_RED_CHANGE;
        this.mWhiteBalanceBlueChange = DEFAULT_WHITE_BALANCE_RED_CHANGE;
        this.mAutoWBToggle = DEFAULT_WHITE_BALANCE_TOGGLE;
        this.mAdaptRateLearning = DEFAULT_LEARNING_ADAPT_RATE;
        this.mAdaptRateBg = DEFAULT_WHITE_BALANCE_RED_CHANGE;
        this.mAdaptRateFg = DEFAULT_WHITE_BALANCE_RED_CHANGE;
        this.mVerifyRate = DEFAULT_MASK_VERIFY_RATE;
        this.mLearningDoneListener = null;
        this.mUseTheForce = false;
        this.mProvideDebugOutputs = false;
        this.mMirrorBg = false;
        this.mOrientation = DEFAULT_WHITE_BALANCE_TOGGLE;
        this.startTime = -1;
        this.mLogVerbose = Log.isLoggable(TAG, DEFAULT_HIER_MID_EXPONENT);
        String adjStr = SystemProperties.get("ro.media.effect.bgdropper.adj");
        if (adjStr.length() > 0) {
            try {
                this.mAcceptStddev += Float.parseFloat(adjStr);
                if (this.mLogVerbose) {
                    Log.v(TAG, "Adjusting accept threshold by " + adjStr + ", now " + this.mAcceptStddev);
                }
            } catch (NumberFormatException e) {
                Log.e(TAG, "Badly formatted property ro.media.effect.bgdropper.adj: " + adjStr);
            }
        }
    }

    public void setupPorts() {
        int i;
        int i2 = DEFAULT_WHITE_BALANCE_TOGGLE;
        FrameFormat imageFormat = ImageFormat.create(DEFAULT_HIER_LRG_EXPONENT, DEFAULT_WHITE_BALANCE_TOGGLE);
        String[] strArr = mInputNames;
        int length = strArr.length;
        for (i = DEFAULT_WHITE_BALANCE_TOGGLE; i < length; i++) {
            addMaskedInputPort(strArr[i], imageFormat);
        }
        strArr = mOutputNames;
        length = strArr.length;
        for (i = DEFAULT_WHITE_BALANCE_TOGGLE; i < length; i++) {
            addOutputBasedOnInput(strArr[i], "video");
        }
        if (this.mProvideDebugOutputs) {
            String[] strArr2 = mDebugOutputNames;
            int length2 = strArr2.length;
            while (i2 < length2) {
                addOutputBasedOnInput(strArr2[i2], "video");
                i2++;
            }
        }
    }

    public FrameFormat getOutputFormat(String portName, FrameFormat inputFormat) {
        MutableFrameFormat format = inputFormat.mutableCopy();
        if (!Arrays.asList(mOutputNames).contains(portName)) {
            format.setDimensions(DEFAULT_WHITE_BALANCE_TOGGLE, DEFAULT_WHITE_BALANCE_TOGGLE);
        }
        return format;
    }

    private boolean createMemoryFormat(FrameFormat inputFormat) {
        if (this.mMemoryFormat != null) {
            return false;
        }
        if (inputFormat.getWidth() == 0 || inputFormat.getHeight() == 0) {
            throw new RuntimeException("Attempting to process input frame with unknown size");
        }
        this.mMaskFormat = inputFormat.mutableCopy();
        int maskWidth = (int) Math.pow(2.0d, (double) this.mMaskWidthExp);
        int maskHeight = (int) Math.pow(2.0d, (double) this.mMaskHeightExp);
        this.mMaskFormat.setDimensions(maskWidth, maskHeight);
        this.mPyramidDepth = Math.max(this.mMaskWidthExp, this.mMaskHeightExp);
        this.mMemoryFormat = this.mMaskFormat.mutableCopy();
        int widthExp = Math.max(this.mMaskWidthExp, pyramidLevel(inputFormat.getWidth()));
        int heightExp = Math.max(this.mMaskHeightExp, pyramidLevel(inputFormat.getHeight()));
        this.mPyramidDepth = Math.max(widthExp, heightExp);
        int memWidth = Math.max(maskWidth, (int) Math.pow(2.0d, (double) widthExp));
        int memHeight = Math.max(maskHeight, (int) Math.pow(2.0d, (double) heightExp));
        this.mMemoryFormat.setDimensions(memWidth, memHeight);
        this.mSubsampleLevel = this.mPyramidDepth - Math.max(this.mMaskWidthExp, this.mMaskHeightExp);
        if (this.mLogVerbose) {
            Log.v(TAG, "Mask frames size " + maskWidth + " x " + maskHeight);
            Log.v(TAG, "Pyramid levels " + widthExp + " x " + heightExp);
            Log.v(TAG, "Memory frames size " + memWidth + " x " + memHeight);
        }
        this.mAverageFormat = inputFormat.mutableCopy();
        this.mAverageFormat.setDimensions(1, 1);
        return true;
    }

    public void prepare(FilterContext context) {
        if (this.mLogVerbose) {
            Log.v(TAG, "Preparing BackDropperFilter!");
        }
        this.mBgMean = new GLFrame[DEFAULT_HIER_MID_EXPONENT];
        this.mBgVariance = new GLFrame[DEFAULT_HIER_MID_EXPONENT];
        this.mMaskVerify = new GLFrame[DEFAULT_HIER_MID_EXPONENT];
        this.copyShaderProgram = ShaderProgram.createIdentity(context);
    }

    private void allocateFrames(FrameFormat inputFormat, FilterContext context) {
        if (createMemoryFormat(inputFormat)) {
            int i;
            if (this.mLogVerbose) {
                Log.v(TAG, "Allocating BackDropperFilter frames");
            }
            int numBytes = this.mMaskFormat.getSize();
            byte[] initialBgMean = new byte[numBytes];
            byte[] initialBgVariance = new byte[numBytes];
            byte[] initialMaskVerify = new byte[numBytes];
            for (i = DEFAULT_WHITE_BALANCE_TOGGLE; i < numBytes; i++) {
                initialBgMean[i] = Byte.MIN_VALUE;
                initialBgVariance[i] = (byte) 10;
                initialMaskVerify[i] = (byte) 0;
            }
            for (i = DEFAULT_WHITE_BALANCE_TOGGLE; i < DEFAULT_HIER_MID_EXPONENT; i++) {
                this.mBgMean[i] = (GLFrame) context.getFrameManager().newFrame(this.mMaskFormat);
                this.mBgMean[i].setData(initialBgMean, (int) DEFAULT_WHITE_BALANCE_TOGGLE, numBytes);
                this.mBgVariance[i] = (GLFrame) context.getFrameManager().newFrame(this.mMaskFormat);
                this.mBgVariance[i].setData(initialBgVariance, (int) DEFAULT_WHITE_BALANCE_TOGGLE, numBytes);
                this.mMaskVerify[i] = (GLFrame) context.getFrameManager().newFrame(this.mMaskFormat);
                this.mMaskVerify[i].setData(initialMaskVerify, (int) DEFAULT_WHITE_BALANCE_TOGGLE, numBytes);
            }
            if (this.mLogVerbose) {
                Log.v(TAG, "Done allocating texture for Mean and Variance objects!");
            }
            this.mDistance = (GLFrame) context.getFrameManager().newFrame(this.mMaskFormat);
            this.mMask = (GLFrame) context.getFrameManager().newFrame(this.mMaskFormat);
            this.mAutoWB = (GLFrame) context.getFrameManager().newFrame(this.mAverageFormat);
            this.mVideoInput = (GLFrame) context.getFrameManager().newFrame(this.mMemoryFormat);
            this.mBgInput = (GLFrame) context.getFrameManager().newFrame(this.mMemoryFormat);
            this.mMaskAverage = (GLFrame) context.getFrameManager().newFrame(this.mAverageFormat);
            this.mBgDistProgram = new ShaderProgram(context, mSharedUtilShader + mBgDistanceShader);
            this.mBgDistProgram.setHostValue("subsample_level", Float.valueOf((float) this.mSubsampleLevel));
            this.mBgMaskProgram = new ShaderProgram(context, mSharedUtilShader + mBgMaskShader);
            this.mBgMaskProgram.setHostValue("accept_variance", Float.valueOf(this.mAcceptStddev * this.mAcceptStddev));
            float[] yuvWeights = new float[DEFAULT_HIER_MID_EXPONENT];
            yuvWeights[DEFAULT_WHITE_BALANCE_TOGGLE] = this.mLumScale;
            yuvWeights[1] = this.mChromaScale;
            this.mBgMaskProgram.setHostValue("yuv_weights", yuvWeights);
            this.mBgMaskProgram.setHostValue("scale_lrg", Float.valueOf(this.mHierarchyLrgScale));
            this.mBgMaskProgram.setHostValue("scale_mid", Float.valueOf(this.mHierarchyMidScale));
            this.mBgMaskProgram.setHostValue("scale_sml", Float.valueOf(this.mHierarchySmlScale));
            this.mBgMaskProgram.setHostValue("exp_lrg", Float.valueOf((float) (this.mSubsampleLevel + this.mHierarchyLrgExp)));
            this.mBgMaskProgram.setHostValue("exp_mid", Float.valueOf((float) (this.mSubsampleLevel + this.mHierarchyMidExp)));
            this.mBgMaskProgram.setHostValue("exp_sml", Float.valueOf((float) (this.mSubsampleLevel + this.mHierarchySmlExp)));
            if (this.mUseTheForce) {
                this.mBgSubtractProgram = new ShaderProgram(context, mSharedUtilShader + mBgSubtractShader + mBgSubtractForceShader);
            } else {
                this.mBgSubtractProgram = new ShaderProgram(context, mSharedUtilShader + mBgSubtractShader + "}\n");
            }
            this.mBgSubtractProgram.setHostValue("bg_fit_transform", DEFAULT_BG_FIT_TRANSFORM);
            this.mBgSubtractProgram.setHostValue("mask_blend_bg", Float.valueOf(this.mMaskBg));
            this.mBgSubtractProgram.setHostValue("mask_blend_fg", Float.valueOf(this.mMaskFg));
            this.mBgSubtractProgram.setHostValue("exposure_change", Float.valueOf(this.mExposureChange));
            this.mBgSubtractProgram.setHostValue("whitebalanceblue_change", Float.valueOf(this.mWhiteBalanceBlueChange));
            this.mBgSubtractProgram.setHostValue("whitebalancered_change", Float.valueOf(this.mWhiteBalanceRedChange));
            this.mBgUpdateMeanProgram = new ShaderProgram(context, mSharedUtilShader + mUpdateBgModelMeanShader);
            this.mBgUpdateMeanProgram.setHostValue("subsample_level", Float.valueOf((float) this.mSubsampleLevel));
            this.mBgUpdateVarianceProgram = new ShaderProgram(context, mSharedUtilShader + mUpdateBgModelVarianceShader);
            this.mBgUpdateVarianceProgram.setHostValue("subsample_level", Float.valueOf((float) this.mSubsampleLevel));
            this.mCopyOutProgram = ShaderProgram.createIdentity(context);
            this.mAutomaticWhiteBalanceProgram = new ShaderProgram(context, mSharedUtilShader + mAutomaticWhiteBalance);
            this.mAutomaticWhiteBalanceProgram.setHostValue("pyramid_depth", Float.valueOf((float) this.mPyramidDepth));
            this.mAutomaticWhiteBalanceProgram.setHostValue("autowb_toggle", Integer.valueOf(this.mAutoWBToggle));
            this.mMaskVerifyProgram = new ShaderProgram(context, mSharedUtilShader + mMaskVerifyShader);
            this.mMaskVerifyProgram.setHostValue("verify_rate", Float.valueOf(this.mVerifyRate));
            if (this.mLogVerbose) {
                Log.v(TAG, "Shader width set to " + this.mMemoryFormat.getWidth());
            }
            this.mRelativeAspect = DEFAULT_EXPOSURE_CHANGE;
            this.mFrameCount = DEFAULT_WHITE_BALANCE_TOGGLE;
            this.mStartLearning = true;
        }
    }

    public void process(FilterContext context) {
        Frame video = pullInput("video");
        Frame background = pullInput("background");
        allocateFrames(video.getFormat(), context);
        if (this.mStartLearning) {
            if (this.mLogVerbose) {
                Log.v(TAG, "Starting learning");
            }
            this.mBgUpdateMeanProgram.setHostValue("bg_adapt_rate", Float.valueOf(this.mAdaptRateLearning));
            this.mBgUpdateMeanProgram.setHostValue("fg_adapt_rate", Float.valueOf(this.mAdaptRateLearning));
            this.mBgUpdateVarianceProgram.setHostValue("bg_adapt_rate", Float.valueOf(this.mAdaptRateLearning));
            this.mBgUpdateVarianceProgram.setHostValue("fg_adapt_rate", Float.valueOf(this.mAdaptRateLearning));
            this.mFrameCount = DEFAULT_WHITE_BALANCE_TOGGLE;
        }
        int inputIndex = this.mPingPong ? DEFAULT_WHITE_BALANCE_TOGGLE : 1;
        int outputIndex = this.mPingPong ? 1 : DEFAULT_WHITE_BALANCE_TOGGLE;
        this.mPingPong = !this.mPingPong;
        updateBgScaling(video, background, this.mBackgroundFitModeChanged);
        this.mBackgroundFitModeChanged = false;
        this.copyShaderProgram.process(video, this.mVideoInput);
        this.copyShaderProgram.process(background, this.mBgInput);
        this.mVideoInput.generateMipMap();
        this.mVideoInput.setTextureParameter(GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_NEAREST);
        this.mBgInput.generateMipMap();
        this.mBgInput.setTextureParameter(GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_NEAREST);
        if (this.mStartLearning) {
            this.copyShaderProgram.process(this.mVideoInput, this.mBgMean[inputIndex]);
            this.mStartLearning = false;
        }
        Frame[] distInputs = new Frame[DEFAULT_HIER_LRG_EXPONENT];
        distInputs[DEFAULT_WHITE_BALANCE_TOGGLE] = this.mVideoInput;
        distInputs[1] = this.mBgMean[inputIndex];
        distInputs[DEFAULT_HIER_MID_EXPONENT] = this.mBgVariance[inputIndex];
        this.mBgDistProgram.process(distInputs, this.mDistance);
        this.mDistance.generateMipMap();
        this.mDistance.setTextureParameter(GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_NEAREST);
        this.mBgMaskProgram.process(this.mDistance, this.mMask);
        this.mMask.generateMipMap();
        this.mMask.setTextureParameter(GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_NEAREST);
        Frame[] autoWBInputs = new Frame[DEFAULT_HIER_MID_EXPONENT];
        autoWBInputs[DEFAULT_WHITE_BALANCE_TOGGLE] = this.mVideoInput;
        autoWBInputs[1] = this.mBgInput;
        this.mAutomaticWhiteBalanceProgram.process(autoWBInputs, this.mAutoWB);
        if (this.mFrameCount <= this.mLearningDuration) {
            pushOutput("video", video);
            if (this.mFrameCount == this.mLearningDuration - this.mLearningVerifyDuration) {
                this.copyShaderProgram.process(this.mMask, this.mMaskVerify[outputIndex]);
                this.mBgUpdateMeanProgram.setHostValue("bg_adapt_rate", Float.valueOf(this.mAdaptRateBg));
                this.mBgUpdateMeanProgram.setHostValue("fg_adapt_rate", Float.valueOf(this.mAdaptRateFg));
                this.mBgUpdateVarianceProgram.setHostValue("bg_adapt_rate", Float.valueOf(this.mAdaptRateBg));
                this.mBgUpdateVarianceProgram.setHostValue("fg_adapt_rate", Float.valueOf(this.mAdaptRateFg));
            } else {
                if (this.mFrameCount > this.mLearningDuration - this.mLearningVerifyDuration) {
                    Frame[] maskVerifyInputs = new Frame[DEFAULT_HIER_MID_EXPONENT];
                    maskVerifyInputs[DEFAULT_WHITE_BALANCE_TOGGLE] = this.mMaskVerify[inputIndex];
                    maskVerifyInputs[1] = this.mMask;
                    this.mMaskVerifyProgram.process(maskVerifyInputs, this.mMaskVerify[outputIndex]);
                    this.mMaskVerify[outputIndex].generateMipMap();
                    this.mMaskVerify[outputIndex].setTextureParameter(GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_NEAREST);
                }
            }
            if (this.mFrameCount == this.mLearningDuration) {
                this.copyShaderProgram.process(this.mMaskVerify[outputIndex], this.mMaskAverage);
                int bi = this.mMaskAverage.getData().array()[DEFAULT_HIER_LRG_EXPONENT] & Process.PROC_TERM_MASK;
                if (this.mLogVerbose) {
                    String str = TAG;
                    Object[] objArr = new Object[DEFAULT_HIER_MID_EXPONENT];
                    objArr[DEFAULT_WHITE_BALANCE_TOGGLE] = Integer.valueOf(bi);
                    objArr[1] = Integer.valueOf(DEFAULT_LEARNING_DONE_THRESHOLD);
                    Log.v(str, String.format("Mask_average is %d, threshold is %d", objArr));
                }
                if (bi >= DEFAULT_LEARNING_DONE_THRESHOLD) {
                    this.mStartLearning = true;
                } else {
                    if (this.mLogVerbose) {
                        Log.v(TAG, "Learning done");
                    }
                    if (this.mLearningDoneListener != null) {
                        this.mLearningDoneListener.onLearningDone(this);
                    }
                }
            }
        } else {
            Frame output = context.getFrameManager().newFrame(video.getFormat());
            subtractInputs = new Frame[4];
            subtractInputs[DEFAULT_HIER_MID_EXPONENT] = this.mMask;
            subtractInputs[DEFAULT_HIER_LRG_EXPONENT] = this.mAutoWB;
            this.mBgSubtractProgram.process(subtractInputs, output);
            pushOutput("video", output);
            output.release();
        }
        if (this.mFrameCount >= this.mLearningDuration - this.mLearningVerifyDuration) {
            if (((double) this.mAdaptRateBg) <= 0.0d) {
                if (((double) this.mAdaptRateFg) > 0.0d) {
                }
                if (this.mProvideDebugOutputs) {
                    Frame dbg1 = context.getFrameManager().newFrame(video.getFormat());
                    this.mCopyOutProgram.process(video, dbg1);
                    pushOutput("debug1", dbg1);
                    dbg1.release();
                    Frame dbg2 = context.getFrameManager().newFrame(this.mMemoryFormat);
                    this.mCopyOutProgram.process(this.mMask, dbg2);
                    pushOutput("debug2", dbg2);
                    dbg2.release();
                }
                this.mFrameCount++;
                if (this.mLogVerbose) {
                    if (this.mFrameCount % 30 == 0) {
                        if (this.startTime != -1) {
                            context.getGLEnvironment().activate();
                            GLES20.glFinish();
                            this.startTime = SystemClock.elapsedRealtime();
                        }
                        context.getGLEnvironment().activate();
                        GLES20.glFinish();
                        long endTime = SystemClock.elapsedRealtime();
                        str = TAG;
                        StringBuilder append = new StringBuilder().append("Avg. frame duration: ");
                        Object[] objArr2 = new Object[1];
                        objArr2[DEFAULT_WHITE_BALANCE_TOGGLE] = Double.valueOf(((double) (endTime - this.startTime)) / 30.0d);
                        append = append.append(String.format("%.2f", objArr2)).append(" ms. Avg. fps: ");
                        objArr2 = new Object[1];
                        objArr2[DEFAULT_WHITE_BALANCE_TOGGLE] = Double.valueOf(1000.0d / (((double) (endTime - this.startTime)) / 30.0d));
                        Log.v(str, append.append(String.format("%.2f", objArr2)).toString());
                        this.startTime = endTime;
                        return;
                    }
                }
            }
        }
        Frame[] meanUpdateInputs = new Frame[DEFAULT_HIER_LRG_EXPONENT];
        meanUpdateInputs[DEFAULT_WHITE_BALANCE_TOGGLE] = this.mVideoInput;
        meanUpdateInputs[1] = this.mBgMean[inputIndex];
        meanUpdateInputs[DEFAULT_HIER_MID_EXPONENT] = this.mMask;
        this.mBgUpdateMeanProgram.process(meanUpdateInputs, this.mBgMean[outputIndex]);
        this.mBgMean[outputIndex].generateMipMap();
        this.mBgMean[outputIndex].setTextureParameter(GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_NEAREST);
        varianceUpdateInputs = new Frame[4];
        varianceUpdateInputs[DEFAULT_WHITE_BALANCE_TOGGLE] = this.mVideoInput;
        varianceUpdateInputs[1] = this.mBgMean[inputIndex];
        varianceUpdateInputs[DEFAULT_HIER_MID_EXPONENT] = this.mBgVariance[inputIndex];
        varianceUpdateInputs[DEFAULT_HIER_LRG_EXPONENT] = this.mMask;
        this.mBgUpdateVarianceProgram.process(varianceUpdateInputs, this.mBgVariance[outputIndex]);
        this.mBgVariance[outputIndex].generateMipMap();
        this.mBgVariance[outputIndex].setTextureParameter(GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_NEAREST);
        if (this.mProvideDebugOutputs) {
            Frame dbg12 = context.getFrameManager().newFrame(video.getFormat());
            this.mCopyOutProgram.process(video, dbg12);
            pushOutput("debug1", dbg12);
            dbg12.release();
            Frame dbg22 = context.getFrameManager().newFrame(this.mMemoryFormat);
            this.mCopyOutProgram.process(this.mMask, dbg22);
            pushOutput("debug2", dbg22);
            dbg22.release();
        }
        this.mFrameCount++;
        if (this.mLogVerbose) {
            if (this.mFrameCount % 30 == 0) {
                if (this.startTime != -1) {
                    context.getGLEnvironment().activate();
                    GLES20.glFinish();
                    long endTime2 = SystemClock.elapsedRealtime();
                    str = TAG;
                    StringBuilder append2 = new StringBuilder().append("Avg. frame duration: ");
                    Object[] objArr22 = new Object[1];
                    objArr22[DEFAULT_WHITE_BALANCE_TOGGLE] = Double.valueOf(((double) (endTime2 - this.startTime)) / 30.0d);
                    append2 = append2.append(String.format("%.2f", objArr22)).append(" ms. Avg. fps: ");
                    objArr22 = new Object[1];
                    objArr22[DEFAULT_WHITE_BALANCE_TOGGLE] = Double.valueOf(1000.0d / (((double) (endTime2 - this.startTime)) / 30.0d));
                    Log.v(str, append2.append(String.format("%.2f", objArr22)).toString());
                    this.startTime = endTime2;
                    return;
                }
                context.getGLEnvironment().activate();
                GLES20.glFinish();
                this.startTime = SystemClock.elapsedRealtime();
            }
        }
    }

    public void close(FilterContext context) {
        if (this.mMemoryFormat != null) {
            if (this.mLogVerbose) {
                Log.v(TAG, "Filter Closing!");
            }
            for (int i = DEFAULT_WHITE_BALANCE_TOGGLE; i < DEFAULT_HIER_MID_EXPONENT; i++) {
                this.mBgMean[i].release();
                this.mBgVariance[i].release();
                this.mMaskVerify[i].release();
            }
            this.mDistance.release();
            this.mMask.release();
            this.mAutoWB.release();
            this.mVideoInput.release();
            this.mBgInput.release();
            this.mMaskAverage.release();
            this.mMemoryFormat = null;
        }
    }

    public synchronized void relearn() {
        this.mStartLearning = true;
    }

    public void fieldPortValueUpdated(String name, FilterContext context) {
        if (name.equals("backgroundFitMode")) {
            this.mBackgroundFitModeChanged = true;
        } else if (name.equals("acceptStddev")) {
            this.mBgMaskProgram.setHostValue("accept_variance", Float.valueOf(this.mAcceptStddev * this.mAcceptStddev));
        } else if (name.equals("hierLrgScale")) {
            this.mBgMaskProgram.setHostValue("scale_lrg", Float.valueOf(this.mHierarchyLrgScale));
        } else if (name.equals("hierMidScale")) {
            this.mBgMaskProgram.setHostValue("scale_mid", Float.valueOf(this.mHierarchyMidScale));
        } else if (name.equals("hierSmlScale")) {
            this.mBgMaskProgram.setHostValue("scale_sml", Float.valueOf(this.mHierarchySmlScale));
        } else if (name.equals("hierLrgExp")) {
            this.mBgMaskProgram.setHostValue("exp_lrg", Float.valueOf((float) (this.mSubsampleLevel + this.mHierarchyLrgExp)));
        } else if (name.equals("hierMidExp")) {
            this.mBgMaskProgram.setHostValue("exp_mid", Float.valueOf((float) (this.mSubsampleLevel + this.mHierarchyMidExp)));
        } else if (name.equals("hierSmlExp")) {
            this.mBgMaskProgram.setHostValue("exp_sml", Float.valueOf((float) (this.mSubsampleLevel + this.mHierarchySmlExp)));
        } else if (name.equals("lumScale") || name.equals("chromaScale")) {
            float[] yuvWeights = new float[DEFAULT_HIER_MID_EXPONENT];
            yuvWeights[DEFAULT_WHITE_BALANCE_TOGGLE] = this.mLumScale;
            yuvWeights[1] = this.mChromaScale;
            this.mBgMaskProgram.setHostValue("yuv_weights", yuvWeights);
        } else if (name.equals("maskBg")) {
            this.mBgSubtractProgram.setHostValue("mask_blend_bg", Float.valueOf(this.mMaskBg));
        } else if (name.equals("maskFg")) {
            this.mBgSubtractProgram.setHostValue("mask_blend_fg", Float.valueOf(this.mMaskFg));
        } else if (name.equals("exposureChange")) {
            this.mBgSubtractProgram.setHostValue("exposure_change", Float.valueOf(this.mExposureChange));
        } else if (name.equals("whitebalanceredChange")) {
            this.mBgSubtractProgram.setHostValue("whitebalancered_change", Float.valueOf(this.mWhiteBalanceRedChange));
        } else if (name.equals("whitebalanceblueChange")) {
            this.mBgSubtractProgram.setHostValue("whitebalanceblue_change", Float.valueOf(this.mWhiteBalanceBlueChange));
        } else if (name.equals("autowbToggle")) {
            this.mAutomaticWhiteBalanceProgram.setHostValue("autowb_toggle", Integer.valueOf(this.mAutoWBToggle));
        }
    }

    private void updateBgScaling(Frame video, Frame background, boolean fitModeChanged) {
        float currentRelativeAspect = (((float) video.getFormat().getWidth()) / ((float) video.getFormat().getHeight())) / (((float) background.getFormat().getWidth()) / ((float) background.getFormat().getHeight()));
        if (currentRelativeAspect != this.mRelativeAspect || fitModeChanged) {
            this.mRelativeAspect = currentRelativeAspect;
            float xMin = DEFAULT_WHITE_BALANCE_RED_CHANGE;
            float xWidth = DEFAULT_EXPOSURE_CHANGE;
            float yMin = DEFAULT_WHITE_BALANCE_RED_CHANGE;
            float yWidth = DEFAULT_EXPOSURE_CHANGE;
            switch (this.mBackgroundFitMode) {
                case AudioState.ROUTE_EARPIECE /*1*/:
                    if (this.mRelativeAspect <= DEFAULT_EXPOSURE_CHANGE) {
                        yMin = DEFAULT_HIER_SML_SCALE - (DEFAULT_HIER_SML_SCALE / this.mRelativeAspect);
                        yWidth = DEFAULT_EXPOSURE_CHANGE / this.mRelativeAspect;
                        break;
                    }
                    xMin = DEFAULT_HIER_SML_SCALE - (this.mRelativeAspect * DEFAULT_HIER_SML_SCALE);
                    xWidth = DEFAULT_EXPOSURE_CHANGE * this.mRelativeAspect;
                    break;
                case DEFAULT_HIER_MID_EXPONENT /*2*/:
                    if (this.mRelativeAspect <= DEFAULT_EXPOSURE_CHANGE) {
                        xMin = DEFAULT_HIER_SML_SCALE - (this.mRelativeAspect * DEFAULT_HIER_SML_SCALE);
                        xWidth = this.mRelativeAspect;
                        break;
                    }
                    yMin = DEFAULT_HIER_SML_SCALE - (DEFAULT_HIER_SML_SCALE / this.mRelativeAspect);
                    yWidth = DEFAULT_EXPOSURE_CHANGE / this.mRelativeAspect;
                    break;
            }
            if (this.mMirrorBg) {
                if (this.mLogVerbose) {
                    Log.v(TAG, "Mirroring the background!");
                }
                if (this.mOrientation == 0 || this.mOrientation == BluetoothAssignedNumbers.BDE_TECHNOLOGY) {
                    xWidth = -xWidth;
                    xMin = DEFAULT_EXPOSURE_CHANGE - xMin;
                } else {
                    yWidth = -yWidth;
                    yMin = DEFAULT_EXPOSURE_CHANGE - yMin;
                }
            }
            if (this.mLogVerbose) {
                Log.v(TAG, "bgTransform: xMin, yMin, xWidth, yWidth : " + xMin + ", " + yMin + ", " + xWidth + ", " + yWidth + ", mRelAspRatio = " + this.mRelativeAspect);
            }
            this.mBgSubtractProgram.setHostValue("bg_fit_transform", new float[]{xWidth, DEFAULT_WHITE_BALANCE_RED_CHANGE, DEFAULT_WHITE_BALANCE_RED_CHANGE, DEFAULT_WHITE_BALANCE_RED_CHANGE, yWidth, DEFAULT_WHITE_BALANCE_RED_CHANGE, xMin, yMin, DEFAULT_EXPOSURE_CHANGE});
        }
    }

    private int pyramidLevel(int size) {
        return ((int) Math.floor(Math.log10((double) size) / Math.log10(2.0d))) - 1;
    }
}
