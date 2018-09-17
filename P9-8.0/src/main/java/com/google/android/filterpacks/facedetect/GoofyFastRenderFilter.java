package com.google.android.filterpacks.facedetect;

import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;
import android.filterfw.core.Frame;
import android.filterfw.core.FrameFormat;
import android.filterfw.core.FrameManager;
import android.filterfw.core.GenerateFieldPort;
import android.filterfw.core.ShaderProgram;
import android.filterfw.core.VertexFrame;
import android.filterfw.format.ImageFormat;
import android.filterfw.format.ObjectFormat;
import android.filterfw.format.PrimitiveFormat;
import android.filterfw.geometry.Quad;
import android.filterfw.geometry.Rectangle;

public class GoofyFastRenderFilter extends Filter {
    private static final int BIG_EYES = 1;
    private static final int BIG_MOUTH = 2;
    private static final int BIG_NOSE = 4;
    private static final int NUM_EFFECTS = 6;
    private static final int SMALL_EYES = 5;
    private static final int SMALL_MOUTH = 3;
    private static final int SQUEEZE = 0;
    private static final String TAG = "GoofyFastRenderFilter";
    private float mAnimateCurrent = 0.0f;
    private long mAnimationStartTimeStamp;
    private float[] mAspect = new float[]{1.0f, 1.0f};
    private ShaderProgram mBigEyesProgram;
    @GenerateFieldPort(hasDefault = true, name = "currentEffect")
    private int mCurrentEffect = SQUEEZE;
    private long mCurrentTimeStamp;
    @GenerateFieldPort(hasDefault = true, name = "distortionAmount")
    private float mDistortionAmount = 0.0f;
    private final String mDistortionVertexShader = "uniform vec2 center;\nuniform vec2 weight;\nuniform mat2 rotate;\nuniform float amount;\nattribute vec4 positions;\nattribute vec2 texcoords;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec2 mesh_point = (rotate * positions.xy) * weight *2.0 +\n                    2.0 * (center - vec2(0.5, 0.5));\n  gl_Position = positions;\n  gl_Position.x = mesh_point.x;\n  gl_Position.y = mesh_point.y;\n  vec2 p = (1.0 + texcoords * amount) * positions.xy;\n  v_texcoord = (rotate * p) * weight  + center;\n}\n";
    private final String mDistortionVertexShader2 = "uniform vec2 center;\nuniform mat2 rotate;\nuniform vec2 weight;\nuniform float amount;\nattribute vec4 positions;\nattribute vec2 texcoords;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec2 mesh_point = (rotate * (positions.xy * vec2(3.0, 2.0))) * weight +\n                    2.0 * (center - vec2(0.5, 0.5));\n  gl_Position = positions;\n  gl_Position.x = mesh_point.x;\n  gl_Position.y = mesh_point.y;\n  float x = (1.0 + amount * texcoords.x) * positions.x + amount * texcoords.y;\n  float y = positions.y * (1.0 + texcoords.x * amount);\n  vec2 p = vec2(x,y);\n  v_texcoord = (rotate * (p * vec2(3.0,2.0))) * weight * 0.5 + center;\n}\n";
    @GenerateFieldPort(hasDefault = true, name = "enableAnimation")
    private boolean mEnableAnimation = true;
    private ShaderProgram mIdentityProgram;
    private final String mIdentityShader = "precision mediump float;\nuniform sampler2D tex_sampler_0;\nvarying vec2 v_texcoord;\nvoid main() {\n  gl_FragColor = texture2D(tex_sampler_0, v_texcoord);\n}\n";
    private VertexFrame mMeshDistortionFrame;
    private ShaderProgram mPureIdentityProgram;
    @GenerateFieldPort(hasDefault = true, name = "smoothness")
    private float mSmoothness = 0.3f;

    public GoofyFastRenderFilter(String name) {
        super(name);
    }

    public void setupPorts() {
        FrameFormat imageFormat = ImageFormat.create(SMALL_MOUTH, SMALL_MOUTH);
        FrameFormat facesFormat = ObjectFormat.fromClass(FaceMeta.class, BIG_MOUTH);
        addMaskedInputPort("image", imageFormat);
        addMaskedInputPort("faces", facesFormat);
        addOutputBasedOnInput("outimage", "image");
    }

    public FrameFormat getOutputFormat(String portName, FrameFormat inputFormat) {
        return inputFormat;
    }

    private void createProgram(FilterContext context) {
        this.mIdentityProgram = new ShaderProgram(context, "uniform vec2 center;\nuniform vec2 weight;\nuniform mat2 rotate;\nuniform float amount;\nattribute vec4 positions;\nattribute vec2 texcoords;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec2 mesh_point = (rotate * positions.xy) * weight *2.0 +\n                    2.0 * (center - vec2(0.5, 0.5));\n  gl_Position = positions;\n  gl_Position.x = mesh_point.x;\n  gl_Position.y = mesh_point.y;\n  vec2 p = (1.0 + texcoords * amount) * positions.xy;\n  v_texcoord = (rotate * p) * weight  + center;\n}\n", "precision mediump float;\nuniform sampler2D tex_sampler_0;\nvarying vec2 v_texcoord;\nvoid main() {\n  gl_FragColor = texture2D(tex_sampler_0, v_texcoord);\n}\n");
        this.mBigEyesProgram = new ShaderProgram(context, "uniform vec2 center;\nuniform mat2 rotate;\nuniform vec2 weight;\nuniform float amount;\nattribute vec4 positions;\nattribute vec2 texcoords;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec2 mesh_point = (rotate * (positions.xy * vec2(3.0, 2.0))) * weight +\n                    2.0 * (center - vec2(0.5, 0.5));\n  gl_Position = positions;\n  gl_Position.x = mesh_point.x;\n  gl_Position.y = mesh_point.y;\n  float x = (1.0 + amount * texcoords.x) * positions.x + amount * texcoords.y;\n  float y = positions.y * (1.0 + texcoords.x * amount);\n  vec2 p = vec2(x,y);\n  v_texcoord = (rotate * (p * vec2(3.0,2.0))) * weight * 0.5 + center;\n}\n", "precision mediump float;\nuniform sampler2D tex_sampler_0;\nvarying vec2 v_texcoord;\nvoid main() {\n  gl_FragColor = texture2D(tex_sampler_0, v_texcoord);\n}\n");
    }

    public void fieldPortValueUpdated(String name, FilterContext context) {
        this.mAnimateCurrent = 0.0f;
        if (this.mIdentityProgram != null && this.mBigEyesProgram != null) {
            createMesh(context);
        }
    }

    private float getDistortionScale(float x, float amount) {
        float value = x;
        float dist = x;
        switch (this.mCurrentEffect) {
            case SQUEEZE /*0*/:
                return amount * (2.0f - (2.0f / ((((float) Math.exp((double) (((-(x + 0.2f)) * (0.2f + x)) * 3.0f))) / 0.887f) + 1.0f)));
            case BIG_EYES /*1*/:
            case BIG_MOUTH /*2*/:
            case BIG_NOSE /*4*/:
                float scale = (float) Math.sqrt((Math.log(1024.0d) * 0.699999988079071d) * 0.699999988079071d);
                if (scale > 1.0f) {
                    dist = x * scale;
                }
                return (-0.65f * amount) * ((float) Math.exp((double) (((-dist) * dist) / 0.48999998f)));
            case SMALL_MOUTH /*3*/:
            case SMALL_EYES /*5*/:
                return (0.8f * amount) * ((2.0f - ((float) Math.exp((double) (((-x) * x) / 0.09f)))) - (2.0f / (((float) Math.exp((-Math.pow((double) (0.25f + x), 4.0d)) * 2.0d)) + 1.0f)));
            default:
                return value;
        }
    }

    float[] getEffectAspectRatio() {
        switch (this.mCurrentEffect) {
            case SQUEEZE /*0*/:
                return new float[]{1.0f, 0.7f};
            case BIG_EYES /*1*/:
                return new float[]{1.0f, 1.0f};
            case BIG_MOUTH /*2*/:
                return new float[]{1.0f, 0.6f};
            case SMALL_MOUTH /*3*/:
                return new float[]{1.0f, 0.6f};
            case BIG_NOSE /*4*/:
                return new float[]{1.0f, 0.8f};
            case SMALL_EYES /*5*/:
                return new float[]{1.0f, 0.4f};
            default:
                return new float[]{1.0f, 1.0f};
        }
    }

    private float[] getTexturePosition(float x, float y, float amount) {
        int i = BIG_MOUTH;
        float[] texture_pos = new float[]{0.0f, 0.0f};
        float dist;
        if (this.mCurrentEffect != BIG_EYES) {
            float[] e_aspect = getEffectAspectRatio();
            float as_ratio;
            if (e_aspect[SQUEEZE] < e_aspect[BIG_EYES]) {
                as_ratio = e_aspect[BIG_EYES];
            } else {
                as_ratio = e_aspect[SQUEEZE];
            }
            dist = (float) Math.sqrt((double) ((((x * x) / e_aspect[SQUEEZE]) / e_aspect[SQUEEZE]) + (((y * y) / e_aspect[BIG_EYES]) / e_aspect[BIG_EYES])));
            if (dist <= 1.0f) {
                float scale = getDistortionScale(dist, 1.0f);
                texture_pos[SQUEEZE] = scale;
                texture_pos[BIG_EYES] = scale;
            }
        } else {
            float size = 2.0f + 1.0f;
            float left = 1.0f / size;
            float right = 1.0f - left;
            left = (left - 0.5f) * 2.0f;
            float dL = x - left;
            dist = (size / 2.0f) * ((float) Math.sqrt((double) ((dL * dL) + (y * y))));
            float dR = x - ((right - 0.5f) * 2.0f);
            float dist2 = (size / 2.0f) * ((float) Math.sqrt((double) ((dR * dR) + (y * y))));
            if (dist < 1.0f || dist2 < 1.0f) {
                float scale1 = getDistortionScale(dist, 1.0f);
                float scale2 = getDistortionScale(dist2, 1.0f);
                texture_pos[SQUEEZE] = ((scale1 * scale1) + (scale2 * scale2)) / (scale1 + scale2);
                texture_pos[BIG_EYES] = (-(scale1 - scale2)) * left;
            }
        }
        return texture_pos;
    }

    private void createMesh(FilterContext context) {
        float amount = this.mDistortionAmount;
        int nrows = (int) (this.mSmoothness * 100.0f);
        int ncols = (int) (this.mSmoothness * 100.0f);
        int num_floats = ((nrows * ncols) * NUM_EFFECTS) * BIG_NOSE;
        float[] positions = new float[num_floats];
        for (int i = SQUEEZE; i < nrows; i += BIG_EYES) {
            for (int j = SQUEEZE; j < ncols; j += BIG_EYES) {
                int p = (((i * ncols) + j) * NUM_EFFECTS) * BIG_NOSE;
                float x0 = ((float) j) / ((float) ncols);
                float y0 = ((float) i) / ((float) nrows);
                float x1 = ((float) (j + BIG_EYES)) / ((float) ncols);
                float y1 = ((float) (i + BIG_EYES)) / ((float) nrows);
                for (int k = SQUEEZE; k < NUM_EFFECTS; k += BIG_EYES) {
                    float x = 0.0f;
                    float y = 0.0f;
                    switch (k) {
                        case SQUEEZE /*0*/:
                            x = x0;
                            y = y0;
                            break;
                        case BIG_EYES /*1*/:
                        case SMALL_MOUTH /*3*/:
                            x = x0;
                            y = y1;
                            break;
                        case BIG_MOUTH /*2*/:
                        case SMALL_EYES /*5*/:
                            x = x1;
                            y = y0;
                            break;
                        case BIG_NOSE /*4*/:
                            x = x1;
                            y = y1;
                            break;
                        default:
                            break;
                    }
                    x = (x - 0.5f) * 2.0f;
                    y = (y - 0.5f) * 2.0f;
                    positions[(k * BIG_NOSE) + p] = x;
                    positions[((k * BIG_NOSE) + p) + BIG_EYES] = y;
                    float[] texture_pos = getTexturePosition(x, y, amount);
                    positions[((k * BIG_NOSE) + p) + BIG_MOUTH] = texture_pos[SQUEEZE];
                    positions[((k * BIG_NOSE) + p) + SMALL_MOUTH] = texture_pos[BIG_EYES];
                }
            }
        }
        FrameFormat vertexFormat = PrimitiveFormat.createFloatFormat(num_floats, BIG_NOSE);
        FrameManager frameManager = context.getFrameManager();
        if (this.mMeshDistortionFrame != null) {
            this.mMeshDistortionFrame.release();
        }
        this.mMeshDistortionFrame = (VertexFrame) frameManager.newFrame(vertexFormat);
        this.mMeshDistortionFrame.setFloats(positions);
        if (this.mCurrentEffect == BIG_EYES) {
            this.mBigEyesProgram.setAttributeValues("positions", this.mMeshDistortionFrame, 5126, BIG_MOUTH, 16, SQUEEZE, false);
            this.mBigEyesProgram.setAttributeValues("texcoords", this.mMeshDistortionFrame, 5126, BIG_MOUTH, 16, 8, false);
            this.mBigEyesProgram.setVertexCount((nrows * ncols) * NUM_EFFECTS);
            this.mBigEyesProgram.setDrawMode(BIG_NOSE);
            return;
        }
        this.mIdentityProgram.setAttributeValues("positions", this.mMeshDistortionFrame, 5126, BIG_MOUTH, 16, SQUEEZE, false);
        this.mIdentityProgram.setAttributeValues("texcoords", this.mMeshDistortionFrame, 5126, BIG_MOUTH, 16, 8, false);
        this.mIdentityProgram.setVertexCount((nrows * ncols) * NUM_EFFECTS);
        this.mIdentityProgram.setDrawMode(BIG_NOSE);
    }

    public void process(FilterContext context) {
        float[] aspectRatio;
        Frame input = pullInput("image");
        this.mCurrentTimeStamp = input.getTimestamp();
        FrameFormat inputFormat = input.getFormat();
        int width = inputFormat.getWidth();
        int height = inputFormat.getHeight();
        if (width > height) {
            aspectRatio = new float[BIG_MOUTH];
            aspectRatio[SQUEEZE] = 1.0f;
            aspectRatio[BIG_EYES] = ((float) height) / ((float) width);
        } else {
            aspectRatio = new float[BIG_MOUTH];
            aspectRatio[SQUEEZE] = ((float) width) / ((float) height);
            aspectRatio[BIG_EYES] = 1.0f;
        }
        if (this.mBigEyesProgram == null) {
            createProgram(context);
            this.mAspect = aspectRatio;
            createMesh(context);
        } else if (!(aspectRatio[SQUEEZE] == this.mAspect[SQUEEZE] && aspectRatio[BIG_EYES] == this.mAspect[BIG_EYES])) {
            this.mAspect = aspectRatio;
            createMesh(context);
        }
        FaceMeta faces = (FaceMeta) pullInput("faces").getObjectValue();
        float[] center = new float[BIG_MOUTH];
        Object weight = new float[BIG_MOUTH];
        int face_count = faces.count();
        if (face_count > 0) {
            float amount = this.mDistortionAmount;
            if (this.mEnableAnimation && this.mAnimateCurrent < this.mDistortionAmount) {
                if (this.mCurrentTimeStamp <= 0) {
                    this.mAnimateCurrent += 0.03f;
                } else if (this.mAnimateCurrent == 0.0f) {
                    this.mAnimationStartTimeStamp = this.mCurrentTimeStamp;
                    this.mAnimateCurrent = 0.001f;
                } else {
                    this.mAnimateCurrent = (((float) ((this.mCurrentTimeStamp - this.mAnimationStartTimeStamp) / 1000000)) / 2000.0f) * this.mDistortionAmount;
                }
                amount = this.mAnimateCurrent;
                if (amount > this.mDistortionAmount) {
                    amount = this.mDistortionAmount;
                }
            }
            Frame buf1 = context.getFrameManager().newFrame(input.getFormat());
            buf1.setDataFromFrame(input);
            Frame buf2 = null;
            if (face_count > BIG_EYES) {
                buf2 = context.getFrameManager().newFrame(input.getFormat());
            }
            Frame source = input;
            Frame output = buf1;
            int i = SQUEEZE;
            while (i < face_count) {
                if (face_count > BIG_EYES && i > 0) {
                    if (i % BIG_MOUTH == BIG_EYES) {
                        source = buf1;
                        output = buf2;
                        buf2.setDataFromFrame(buf1);
                    } else {
                        source = buf2;
                        output = buf1;
                        buf1.setDataFromFrame(buf2);
                    }
                }
                float[] leftEye = new float[BIG_MOUTH];
                leftEye[SQUEEZE] = faces.getLeftEyeX(i);
                leftEye[BIG_EYES] = faces.getLeftEyeY(i);
                float[] rightEye = new float[BIG_MOUTH];
                rightEye[SQUEEZE] = faces.getRightEyeX(i);
                rightEye[BIG_EYES] = faces.getRightEyeY(i);
                float[] mouth = new float[BIG_MOUTH];
                mouth[SQUEEZE] = faces.getMouthX(i);
                mouth[BIG_EYES] = faces.getMouthY(i);
                float angleEyes = (float) Math.atan2((double) ((rightEye[BIG_EYES] - leftEye[BIG_EYES]) * aspectRatio[BIG_EYES]), (double) ((rightEye[SQUEEZE] - leftEye[SQUEEZE]) * aspectRatio[SQUEEZE]));
                float v_axis_x = ((rightEye[SQUEEZE] + leftEye[SQUEEZE]) / 2.0f) - mouth[SQUEEZE];
                float v_axis_y = ((rightEye[BIG_EYES] + leftEye[BIG_EYES]) / 2.0f) - mouth[BIG_EYES];
                float angleFace = (float) (((double) ((float) Math.atan2((double) v_axis_y, (double) v_axis_x))) - 1.5707963267948966d);
                float dx = (leftEye[SQUEEZE] - rightEye[SQUEEZE]) * aspectRatio[SQUEEZE];
                float dy = (leftEye[BIG_EYES] - rightEye[BIG_EYES]) * aspectRatio[BIG_EYES];
                float length = (float) Math.sqrt((double) ((dx * dx) + (dy * dy)));
                Quad region;
                Object rotate;
                if (this.mCurrentEffect == BIG_EYES) {
                    float top = Math.min(leftEye[BIG_EYES] - length, rightEye[BIG_EYES] - length);
                    float left = Math.min(leftEye[SQUEEZE] - length, rightEye[SQUEEZE] - length);
                    region = new Rectangle(left, top, Math.max(leftEye[SQUEEZE] + length, rightEye[SQUEEZE] + length) - left, Math.max(leftEye[BIG_EYES] + length, rightEye[BIG_EYES] + length) - top).translated(0.0f, 0.0f);
                    center[SQUEEZE] = (leftEye[SQUEEZE] + rightEye[SQUEEZE]) / 2.0f;
                    center[BIG_EYES] = (leftEye[BIG_EYES] + rightEye[BIG_EYES]) / 2.0f;
                    this.mBigEyesProgram.setHostValue("center", center);
                    rotate = new float[BIG_NOSE];
                    rotate[SQUEEZE] = (float) Math.cos((double) angleEyes);
                    rotate[BIG_EYES] = (float) Math.sin((double) angleEyes);
                    rotate[BIG_MOUTH] = (float) (-Math.sin((double) angleEyes));
                    rotate[SMALL_MOUTH] = (float) Math.cos((double) angleEyes);
                    this.mBigEyesProgram.setHostValue("rotate", rotate);
                    Object scales = new float[BIG_MOUTH];
                    scales[SQUEEZE] = length / aspectRatio[SQUEEZE];
                    scales[BIG_EYES] = length / aspectRatio[BIG_EYES];
                    this.mBigEyesProgram.setHostValue("weight", scales);
                    this.mBigEyesProgram.setHostValue("amount", Float.valueOf(amount));
                    this.mBigEyesProgram.setSourceRegion(region);
                    this.mBigEyesProgram.setTargetRegion(region);
                    this.mBigEyesProgram.process(source, output);
                } else {
                    float effectSize;
                    float angle = angleFace;
                    switch (this.mCurrentEffect) {
                        case SQUEEZE /*0*/:
                            center[SQUEEZE] = ((leftEye[SQUEEZE] * 0.25f) + (rightEye[SQUEEZE] * 0.25f)) + (mouth[SQUEEZE] * 0.5f);
                            center[BIG_EYES] = ((leftEye[BIG_EYES] * 0.25f) + (rightEye[BIG_EYES] * 0.25f)) + (mouth[BIG_EYES] * 0.5f);
                            effectSize = 2.0f;
                            break;
                        case BIG_MOUTH /*2*/:
                            center[SQUEEZE] = mouth[SQUEEZE] - (0.06f * v_axis_x);
                            center[BIG_EYES] = mouth[BIG_EYES] - (0.06f * v_axis_y);
                            effectSize = 2.5f;
                            break;
                        case SMALL_MOUTH /*3*/:
                            center[SQUEEZE] = mouth[SQUEEZE] - (0.06f * v_axis_x);
                            center[BIG_EYES] = mouth[BIG_EYES] - (0.06f * v_axis_y);
                            effectSize = 0.7f;
                            break;
                        case BIG_NOSE /*4*/:
                            center[SQUEEZE] = ((leftEye[SQUEEZE] * 0.25f) + (rightEye[SQUEEZE] * 0.25f)) + (mouth[SQUEEZE] * 0.5f);
                            center[BIG_EYES] = ((leftEye[BIG_EYES] * 0.25f) + (rightEye[BIG_EYES] * 0.25f)) + (mouth[BIG_EYES] * 0.5f);
                            effectSize = 1.1f;
                            break;
                        case SMALL_EYES /*5*/:
                            center[SQUEEZE] = ((leftEye[SQUEEZE] * 0.5f) + (rightEye[SQUEEZE] * 0.5f)) + (mouth[SQUEEZE] * 1.0E-4f);
                            center[BIG_EYES] = ((leftEye[BIG_EYES] * 0.5f) + (rightEye[BIG_EYES] * 0.5f)) + (mouth[BIG_EYES] * 1.0E-4f);
                            effectSize = 1.0f;
                            angle = angleEyes;
                            break;
                        default:
                            throw new RuntimeException("Undefined effect: " + this.mCurrentEffect);
                    }
                    weight[SQUEEZE] = (effectSize * length) / aspectRatio[SQUEEZE];
                    weight[BIG_EYES] = (effectSize * length) / aspectRatio[BIG_EYES];
                    region = new Rectangle(weight[SQUEEZE] * -1.0f, weight[BIG_EYES] * -1.0f, weight[SQUEEZE] * 2.0f, weight[BIG_EYES] * 2.0f).translated(center[SQUEEZE], center[BIG_EYES]);
                    this.mIdentityProgram.setHostValue("center", center);
                    this.mIdentityProgram.setHostValue("weight", weight);
                    rotate = new float[BIG_NOSE];
                    rotate[SQUEEZE] = (float) Math.cos((double) angle);
                    rotate[BIG_EYES] = (float) Math.sin((double) angle);
                    rotate[BIG_MOUTH] = (float) (-Math.sin((double) angle));
                    rotate[SMALL_MOUTH] = (float) Math.cos((double) angle);
                    this.mIdentityProgram.setHostValue("rotate", rotate);
                    this.mIdentityProgram.setHostValue("amount", Float.valueOf(amount));
                    this.mIdentityProgram.setSourceRegion(region);
                    this.mIdentityProgram.setTargetRegion(region);
                    this.mIdentityProgram.process(source, output);
                }
                i += BIG_EYES;
            }
            pushOutput("outimage", output);
            buf1.release();
            if (buf2 != null) {
                buf2.release();
                return;
            }
            return;
        }
        pushOutput("outimage", input);
    }
}
