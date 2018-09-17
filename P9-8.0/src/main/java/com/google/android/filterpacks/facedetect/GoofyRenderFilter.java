package com.google.android.filterpacks.facedetect;

import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;
import android.filterfw.core.Frame;
import android.filterfw.core.FrameFormat;
import android.filterfw.core.GenerateFieldPort;
import android.filterfw.core.ShaderProgram;
import android.filterfw.format.ImageFormat;
import android.filterfw.format.ObjectFormat;
import android.filterfw.geometry.Quad;
import android.filterfw.geometry.Rectangle;

public class GoofyRenderFilter extends Filter {
    private static final int BIG_EYES = 1;
    private static final int BIG_MOUTH = 2;
    private static final int BIG_NOSE = 4;
    private static final int NUM_EFFECTS = 6;
    private static final int SMALL_EYES = 5;
    private static final int SMALL_MOUTH = 3;
    private static final int SQUEEZE = 0;
    private static final String TAG = "GoofyRenderFilter";
    private ShaderProgram mBigEyesProgram;
    private final String mBigEyesShader = "precision mediump float;\nuniform sampler2D tex_sampler_0;\nuniform sampler2D tex_sampler_1;\nuniform vec2 left_eye;\nuniform vec2 right_eye;\nuniform vec2 scale;\nuniform float dist_offset;\nuniform float dist_mult;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec2 left_eye_offset = (v_texcoord - left_eye); \n  float left_eye_dist = length(left_eye_offset * scale); \n  vec2 right_eye_offset = (v_texcoord - right_eye); \n  float right_eye_dist = length(right_eye_offset * scale); \n  float dist;\n  vec2 offset;\n  vec2 center;\n  if (left_eye_dist < 1.0 || right_eye_dist < 1.0){\n    float dist_left = left_eye_dist * dist_mult + dist_offset;\n    vec4 value_byte = texture2D(tex_sampler_1, vec2(dist_left, 0.5));\n    float value_left = (value_byte.g + value_byte.r * 0.00390625);\n    vec4 color_left = texture2D(tex_sampler_0,\n            left_eye + (1.0 - value_left) * left_eye_offset);\n    float dist_right = right_eye_dist * dist_mult + dist_offset;\n    value_byte = texture2D(tex_sampler_1, vec2(dist_right, 0.5));\n    float value_right = (value_byte.g + value_byte.r * 0.00390625);\n    vec4 color_right = texture2D(tex_sampler_0,\n            right_eye + (1.0 - value_right) * right_eye_offset);\n    float alpha = value_left / (value_left + value_right);\n    gl_FragColor = mix(color_right, color_left, alpha);\n  } else {\n    gl_FragColor = texture2D(tex_sampler_0, v_texcoord);\n  }\n}\n";
    @GenerateFieldPort(hasDefault = true, name = "currentEffect")
    private int mCurrentEffect = SQUEEZE;
    @GenerateFieldPort(hasDefault = true, name = "distortionAmount")
    private float mDistortionAmount = 0.0f;
    private ShaderProgram mGoofyProgram;
    private final String mGoofyShader = "precision mediump float;\nuniform sampler2D tex_sampler_0;\nuniform sampler2D tex_sampler_1;\nuniform vec2 center;\nuniform vec2 weight;\nuniform float dist_offset;\nuniform float dist_mult;\nuniform bool use_shrink;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec2 point = v_texcoord - center;\n  vec2 spoint;\n  spoint = weight * point;\n  float dist = length(spoint) * dist_mult + dist_offset;\n  vec4 scale_byte = texture2D(tex_sampler_1, vec2(dist, 0.5));\n  float scale = scale_byte.g + scale_byte.r * 0.00390625;\n  if (use_shrink) {\n    scale = 1.0 + scale;\n  } else {\n    scale = 1.0 - scale;\n  }\n  if (dist >= 1.0) { \n     scale = 1.0;\n  } \n  gl_FragColor = texture2D(tex_sampler_0, center + scale * point);\n}\n";
    private boolean mShrinkFunc = true;
    private Frame mTableFrame;

    public GoofyRenderFilter(String name) {
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

    protected void prepare(FilterContext context) {
        this.mGoofyProgram = new ShaderProgram(context, "precision mediump float;\nuniform sampler2D tex_sampler_0;\nuniform sampler2D tex_sampler_1;\nuniform vec2 center;\nuniform vec2 weight;\nuniform float dist_offset;\nuniform float dist_mult;\nuniform bool use_shrink;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec2 point = v_texcoord - center;\n  vec2 spoint;\n  spoint = weight * point;\n  float dist = length(spoint) * dist_mult + dist_offset;\n  vec4 scale_byte = texture2D(tex_sampler_1, vec2(dist, 0.5));\n  float scale = scale_byte.g + scale_byte.r * 0.00390625;\n  if (use_shrink) {\n    scale = 1.0 + scale;\n  } else {\n    scale = 1.0 - scale;\n  }\n  if (dist >= 1.0) { \n     scale = 1.0;\n  } \n  gl_FragColor = texture2D(tex_sampler_0, center + scale * point);\n}\n");
        this.mBigEyesProgram = new ShaderProgram(context, "precision mediump float;\nuniform sampler2D tex_sampler_0;\nuniform sampler2D tex_sampler_1;\nuniform vec2 left_eye;\nuniform vec2 right_eye;\nuniform vec2 scale;\nuniform float dist_offset;\nuniform float dist_mult;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec2 left_eye_offset = (v_texcoord - left_eye); \n  float left_eye_dist = length(left_eye_offset * scale); \n  vec2 right_eye_offset = (v_texcoord - right_eye); \n  float right_eye_dist = length(right_eye_offset * scale); \n  float dist;\n  vec2 offset;\n  vec2 center;\n  if (left_eye_dist < 1.0 || right_eye_dist < 1.0){\n    float dist_left = left_eye_dist * dist_mult + dist_offset;\n    vec4 value_byte = texture2D(tex_sampler_1, vec2(dist_left, 0.5));\n    float value_left = (value_byte.g + value_byte.r * 0.00390625);\n    vec4 color_left = texture2D(tex_sampler_0,\n            left_eye + (1.0 - value_left) * left_eye_offset);\n    float dist_right = right_eye_dist * dist_mult + dist_offset;\n    value_byte = texture2D(tex_sampler_1, vec2(dist_right, 0.5));\n    float value_right = (value_byte.g + value_byte.r * 0.00390625);\n    vec4 color_right = texture2D(tex_sampler_0,\n            right_eye + (1.0 - value_right) * right_eye_offset);\n    float alpha = value_left / (value_left + value_right);\n    gl_FragColor = mix(color_right, color_left, alpha);\n  } else {\n    gl_FragColor = texture2D(tex_sampler_0, v_texcoord);\n  }\n}\n");
        createLookupTable(context);
    }

    public void fieldPortValueUpdated(String name, FilterContext context) {
        if (this.mGoofyProgram != null && this.mBigEyesProgram != null) {
            createLookupTable(context);
        }
    }

    private void createLookupTable(FilterContext context) {
        int[] array = new int[2000];
        float scale = 1.0f;
        for (int j = SQUEEZE; j < 2000; j += BIG_EYES) {
            float dist = ((float) j) / 2000.0f;
            float value = 0.0f;
            switch (this.mCurrentEffect) {
                case SQUEEZE /*0*/:
                case SMALL_MOUTH /*3*/:
                case SMALL_EYES /*5*/:
                    if (j == 0) {
                        scale = (float) Math.sqrt((Math.log(1024.0d) * 0.30000001192092896d) * 0.30000001192092896d);
                    }
                    if (scale > 1.0f) {
                        dist *= scale;
                    }
                    value = (this.mDistortionAmount * 0.8f) * ((2.0f - ((float) Math.exp((double) (((-dist) * dist) / 0.09f)))) - (2.0f / (((float) Math.exp((-Math.pow((double) (0.25f + dist), 4.0d)) * 2.0d)) + 1.0f)));
                    this.mShrinkFunc = true;
                    break;
                case BIG_EYES /*1*/:
                case BIG_MOUTH /*2*/:
                case BIG_NOSE /*4*/:
                    if (j == 0) {
                        scale = (float) Math.sqrt((Math.log(1024.0d) * 0.699999988079071d) * 0.699999988079071d);
                    }
                    if (scale > 1.0f) {
                        dist *= scale;
                    }
                    value = (this.mDistortionAmount * 0.65f) * ((float) Math.exp((double) (((-dist) * dist) / 0.48999998f)));
                    this.mShrinkFunc = false;
                    break;
                default:
                    break;
            }
            array[j] = (int) (65535.0f * value);
        }
        FrameFormat tableFormat = ImageFormat.create(2000, BIG_EYES, SMALL_MOUTH, SMALL_MOUTH);
        if (this.mTableFrame != null) {
            this.mTableFrame.release();
        }
        this.mTableFrame = context.getFrameManager().newFrame(tableFormat);
        this.mTableFrame.setInts(array);
        if (this.mCurrentEffect == BIG_EYES) {
            this.mBigEyesProgram.setHostValue("dist_offset", Float.valueOf(5.0E-4f));
            this.mBigEyesProgram.setHostValue("dist_mult", Float.valueOf(0.9995f));
            return;
        }
        this.mGoofyProgram.setHostValue("dist_offset", Float.valueOf(5.0E-4f));
        this.mGoofyProgram.setHostValue("dist_mult", Float.valueOf(0.9995f));
        this.mGoofyProgram.setHostValue("use_shrink", Boolean.valueOf(this.mShrinkFunc));
    }

    public void process(FilterContext context) {
        float[] aspectRatio;
        Frame input = pullInput("image");
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
        FaceMeta faces = (FaceMeta) pullInput("faces").getObjectValue();
        float[] center = new float[BIG_MOUTH];
        Object weight = new float[BIG_MOUTH];
        int face_count = faces.count();
        if (face_count > 0) {
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
                Object leftEye = new float[BIG_MOUTH];
                leftEye[SQUEEZE] = faces.getLeftEyeX(i);
                leftEye[BIG_EYES] = faces.getLeftEyeY(i);
                Object rightEye = new float[BIG_MOUTH];
                rightEye[SQUEEZE] = faces.getRightEyeX(i);
                rightEye[BIG_EYES] = faces.getRightEyeY(i);
                float[] mouth = new float[BIG_MOUTH];
                mouth[SQUEEZE] = faces.getMouthX(i);
                mouth[BIG_EYES] = faces.getMouthY(i);
                float dx = (leftEye[SQUEEZE] - rightEye[SQUEEZE]) * aspectRatio[SQUEEZE];
                float dy = (leftEye[BIG_EYES] - rightEye[BIG_EYES]) * aspectRatio[BIG_EYES];
                float baseline = (float) Math.sqrt((double) ((dx * dx) + (dy * dy)));
                float face_size;
                Quad region;
                Frame[] inputs;
                if (this.mCurrentEffect == BIG_EYES) {
                    face_size = baseline;
                    float length = baseline;
                    float top = Math.min(leftEye[BIG_EYES] - baseline, rightEye[BIG_EYES] - baseline);
                    float left = Math.min(leftEye[SQUEEZE] - baseline, rightEye[SQUEEZE] - baseline);
                    region = new Rectangle(left, top, Math.max(leftEye[SQUEEZE] + baseline, rightEye[SQUEEZE] + baseline) - left, Math.max(leftEye[BIG_EYES] + baseline, rightEye[BIG_EYES] + baseline) - top).translated(0.0f, 0.0f);
                    this.mBigEyesProgram.setHostValue("left_eye", leftEye);
                    this.mBigEyesProgram.setHostValue("right_eye", rightEye);
                    Object scales = new float[BIG_MOUTH];
                    scales[SQUEEZE] = aspectRatio[SQUEEZE] / baseline;
                    scales[BIG_EYES] = aspectRatio[BIG_EYES] / baseline;
                    this.mBigEyesProgram.setHostValue("scale", scales);
                    this.mBigEyesProgram.setSourceRegion(region);
                    this.mBigEyesProgram.setTargetRegion(region);
                    inputs = new Frame[BIG_MOUTH];
                    inputs[SQUEEZE] = source;
                    inputs[BIG_EYES] = this.mTableFrame;
                    this.mBigEyesProgram.process(inputs, output);
                } else {
                    weight[SQUEEZE] = 1.0f;
                    weight[BIG_EYES] = 1.0f;
                    switch (this.mCurrentEffect) {
                        case SQUEEZE /*0*/:
                            face_size = 0.8f * baseline;
                            center[SQUEEZE] = ((leftEye[SQUEEZE] * 0.25f) + (rightEye[SQUEEZE] * 0.25f)) + (mouth[SQUEEZE] * 0.5f);
                            center[BIG_EYES] = ((leftEye[BIG_EYES] * 0.25f) + (rightEye[BIG_EYES] * 0.25f)) + (mouth[BIG_EYES] * 0.5f);
                            break;
                        case BIG_MOUTH /*2*/:
                            face_size = 2.5f * baseline;
                            center[SQUEEZE] = mouth[SQUEEZE];
                            center[BIG_EYES] = mouth[BIG_EYES];
                            break;
                        case SMALL_MOUTH /*3*/:
                            face_size = 0.5f * baseline;
                            center[SQUEEZE] = mouth[SQUEEZE];
                            center[BIG_EYES] = mouth[BIG_EYES];
                            break;
                        case BIG_NOSE /*4*/:
                            face_size = 1.1f * baseline;
                            center[SQUEEZE] = ((leftEye[SQUEEZE] * 0.25f) + (rightEye[SQUEEZE] * 0.25f)) + (mouth[SQUEEZE] * 0.5f);
                            center[BIG_EYES] = ((leftEye[BIG_EYES] * 0.25f) + (rightEye[BIG_EYES] * 0.25f)) + (mouth[BIG_EYES] * 0.5f);
                            break;
                        case SMALL_EYES /*5*/:
                            face_size = 0.8f * baseline;
                            center[SQUEEZE] = ((leftEye[SQUEEZE] * 0.5f) + (rightEye[SQUEEZE] * 0.5f)) + (mouth[SQUEEZE] * 1.0E-4f);
                            center[BIG_EYES] = ((leftEye[BIG_EYES] * 0.5f) + (rightEye[BIG_EYES] * 0.5f)) + (mouth[BIG_EYES] * 1.0E-4f);
                            weight[SQUEEZE] = (float) Math.sqrt(1.2d);
                            weight[BIG_EYES] = (float) Math.sqrt(1.2d);
                            break;
                        default:
                            throw new RuntimeException("Undefined effect: " + this.mCurrentEffect);
                    }
                    weight[SQUEEZE] = weight[SQUEEZE] / face_size;
                    weight[BIG_EYES] = weight[BIG_EYES] / face_size;
                    weight[SQUEEZE] = weight[SQUEEZE] * aspectRatio[SQUEEZE];
                    weight[BIG_EYES] = weight[BIG_EYES] * aspectRatio[BIG_EYES];
                    region = new Rectangle(-1.0f / weight[SQUEEZE], -1.0f / weight[BIG_EYES], 2.0f / weight[SQUEEZE], 2.0f / weight[BIG_EYES]).translated(center[SQUEEZE], center[BIG_EYES]);
                    this.mGoofyProgram.setHostValue("center", center);
                    this.mGoofyProgram.setHostValue("weight", weight);
                    this.mGoofyProgram.setSourceRegion(region);
                    this.mGoofyProgram.setTargetRegion(region);
                    inputs = new Frame[BIG_MOUTH];
                    inputs[SQUEEZE] = source;
                    inputs[BIG_EYES] = this.mTableFrame;
                    this.mGoofyProgram.process(inputs, output);
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
