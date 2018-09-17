package com.google.android.filterpacks.facedetect;

import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;
import android.filterfw.core.Frame;
import android.filterfw.core.FrameFormat;
import android.filterfw.core.FrameManager;
import android.filterfw.core.MutableFrameFormat;
import android.filterfw.core.NativeProgram;
import android.filterfw.core.Program;
import android.filterfw.core.ShaderProgram;
import android.filterfw.format.ImageFormat;
import android.filterfw.format.ObjectFormat;
import android.filterfw.format.PrimitiveFormat;
import android.filterfw.geometry.Point;
import android.filterfw.geometry.Quad;
import android.filterfw.geometry.Rectangle;

public class LipDiffer extends Filter {
    private static final boolean LOGV = false;
    private static final String TAG = "LipDiffer";
    private final float MOUTH_TO_EYES_HORIZ_RATIO = 0.9f;
    private final float MOUTH_TO_NOSE_VERT_RATIO = 0.5f;
    private final String mDenoiseFragShader = "precision mediump float;\nconst mat3 kernel = mat3(0.09, 0.12, 0.09,\n                         0.12, 0.16, 0.12,\n                         0.09, 0.12, 0.09);\nuniform sampler2D tex_sampler_0;\nuniform float pix_width;\nuniform float pix_height;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec3 color = vec3(0.0, 0.0, 0.0);\n  for (int i = 0; i < 3; i++) {\n    for (int j = 0; j < 3; j++) {\n      vec2 coord = v_texcoord + vec2(float(i-1) * pix_width,\n                                     float(j-1) * pix_height);\n      color = color + kernel[i][j] * texture2D(tex_sampler_0, coord).rgb;\n    }\n  }\n  gl_FragColor = vec4(color, 1.0);\n}\n";
    private ShaderProgram mDenoiseProgram;
    private final String mFragShader = "precision mediump float;\nconst float pi = 3.141593;\nconst vec3 band_coefs = vec3(0.299, 0.587, 0.114);\nconst vec4 no_signal = vec4(0.0, 0.0, 0.0, 1.0);\nuniform sampler2D tex_sampler_0;\nuniform float pix_width;\nuniform float pix_height;\nvarying vec2 v_texcoord;\nvarying vec2 v_vertcoord;\nvoid main() {\n  vec3 pix = texture2D(tex_sampler_0, v_texcoord).rgb;\n  float intensity = dot(band_coefs, pix);\n  vec2 next_x = v_texcoord + vec2(pix_width, 0.0);\n  vec3 grad_x = texture2D(tex_sampler_0, next_x).rgb - pix;\n  vec2 next_y = v_texcoord + vec2(0.0, pix_height);\n  vec3 grad_y = texture2D(tex_sampler_0, next_y).rgb - pix;\n  vec3 sign_y = 2.0 * step(0.0, grad_y) - 1.0;\n  vec2 grad = vec2(dot(band_coefs, sign_y * grad_x),\n                   dot(band_coefs, sign_y * grad_y));\n  float grad_mag = length(grad);\n  float grad_dir = grad_mag > 0.0 ? atan(grad.y, grad.x) / pi : 0.0;\n  vec4 signal = vec4(grad_mag, grad_dir, 0.5 * v_vertcoord.y + 0.5, intensity);\n  gl_FragColor = length(v_vertcoord) < 1.0 ? signal : no_signal;\n}\n";
    private Program mLipDiffProgram;
    private MutableFrameFormat mLipFrameFormat;
    private FrameSize[] mLipFrameSizes;
    private MutableFrameFormat mLipSignalFormat;
    private ShaderProgram mLipSignalProgram;
    private final String mVertexShader = "attribute vec4 a_position;\nattribute vec2 a_texcoord;\nvarying vec2 v_texcoord;\nvarying vec2 v_vertcoord;\nvoid main() {\n  gl_Position = a_position;\n  v_texcoord = a_texcoord;\n  v_vertcoord = a_position.xy;\n}\n";

    private class FrameSize {
        public int height;
        public int width;

        public FrameSize(int w, int h) {
            this.width = w;
            this.height = h;
        }

        public float area() {
            return (float) (this.width * this.height);
        }
    }

    public LipDiffer(String name) {
        super(name);
    }

    public void setupPorts() {
        FrameFormat imageFormat = ImageFormat.create(3, 3);
        FrameFormat faceFormat = ObjectFormat.fromClass(FaceMeta.class, 2);
        FrameFormat lipDiffFormat = ObjectFormat.fromClass(LipDiff.class, 2);
        addMaskedInputPort("image", imageFormat);
        addMaskedInputPort("faces", faceFormat);
        addOutputPort("diffs", lipDiffFormat);
    }

    public void prepare(FilterContext context) {
        this.mDenoiseProgram = new ShaderProgram(context, "precision mediump float;\nconst mat3 kernel = mat3(0.09, 0.12, 0.09,\n                         0.12, 0.16, 0.12,\n                         0.09, 0.12, 0.09);\nuniform sampler2D tex_sampler_0;\nuniform float pix_width;\nuniform float pix_height;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec3 color = vec3(0.0, 0.0, 0.0);\n  for (int i = 0; i < 3; i++) {\n    for (int j = 0; j < 3; j++) {\n      vec2 coord = v_texcoord + vec2(float(i-1) * pix_width,\n                                     float(j-1) * pix_height);\n      color = color + kernel[i][j] * texture2D(tex_sampler_0, coord).rgb;\n    }\n  }\n  gl_FragColor = vec4(color, 1.0);\n}\n");
        this.mLipSignalProgram = new ShaderProgram(context, "attribute vec4 a_position;\nattribute vec2 a_texcoord;\nvarying vec2 v_texcoord;\nvarying vec2 v_vertcoord;\nvoid main() {\n  gl_Position = a_position;\n  v_texcoord = a_texcoord;\n  v_vertcoord = a_position.xy;\n}\n", "precision mediump float;\nconst float pi = 3.141593;\nconst vec3 band_coefs = vec3(0.299, 0.587, 0.114);\nconst vec4 no_signal = vec4(0.0, 0.0, 0.0, 1.0);\nuniform sampler2D tex_sampler_0;\nuniform float pix_width;\nuniform float pix_height;\nvarying vec2 v_texcoord;\nvarying vec2 v_vertcoord;\nvoid main() {\n  vec3 pix = texture2D(tex_sampler_0, v_texcoord).rgb;\n  float intensity = dot(band_coefs, pix);\n  vec2 next_x = v_texcoord + vec2(pix_width, 0.0);\n  vec3 grad_x = texture2D(tex_sampler_0, next_x).rgb - pix;\n  vec2 next_y = v_texcoord + vec2(0.0, pix_height);\n  vec3 grad_y = texture2D(tex_sampler_0, next_y).rgb - pix;\n  vec3 sign_y = 2.0 * step(0.0, grad_y) - 1.0;\n  vec2 grad = vec2(dot(band_coefs, sign_y * grad_x),\n                   dot(band_coefs, sign_y * grad_y));\n  float grad_mag = length(grad);\n  float grad_dir = grad_mag > 0.0 ? atan(grad.y, grad.x) / pi : 0.0;\n  vec4 signal = vec4(grad_mag, grad_dir, 0.5 * v_vertcoord.y + 0.5, intensity);\n  gl_FragColor = length(v_vertcoord) < 1.0 ? signal : no_signal;\n}\n");
        this.mLipDiffProgram = new NativeProgram("filterpack_facedetect", "lip_differ");
        this.mLipFrameFormat = ImageFormat.create(3, 3);
        this.mLipSignalFormat = ImageFormat.create(3, 3);
        this.mLipFrameSizes = new FrameSize[2];
        this.mLipFrameSizes[0] = new FrameSize(60, 45);
        this.mLipFrameSizes[1] = new FrameSize(36, 27);
    }

    private FrameSize lookupFrameSize(int inputWidth, int inputHeight, Point lipSize) {
        if (lipSize.x <= 0.0f || lipSize.y <= 0.0f) {
            throw new RuntimeException("Illegal lip size: " + lipSize.x + " x " + lipSize.y + "!");
        }
        float areaInPixels = ((((float) inputWidth) * lipSize.x) * ((float) inputHeight)) * lipSize.y;
        for (FrameSize area : this.mLipFrameSizes) {
            if (areaInPixels > area.area()) {
                return this.mLipFrameSizes[0];
            }
        }
        return null;
    }

    public void process(FilterContext context) {
        FrameManager frameManager = context.getFrameManager();
        Frame imageFrame = pullInput("image");
        int inputWidth = imageFrame.getFormat().getWidth();
        int inputHeight = imageFrame.getFormat().getHeight();
        FaceMeta faces = (FaceMeta) pullInput("faces").getObjectValue();
        Frame outputFrame = null;
        if (faces.count() == 0) {
            outputFrame = frameManager.newFrame(ObjectFormat.fromClass(LipDiff.class, 0, 2));
        } else {
            for (int i = 0; i < faces.count(); i++) {
                Frame lipSignalNativeFrame;
                Point point = new Point(0.0f, 0.0f);
                Quad lipRegion = computeSourceRegion(faces, i, point);
                FrameSize frameSize = lookupFrameSize(inputWidth, inputHeight, point);
                if (frameSize == null || !lipRegion.IsInUnitRange()) {
                    lipSignalNativeFrame = frameManager.newFrame(PrimitiveFormat.createByteFormat(0, 2));
                } else {
                    this.mLipFrameFormat.setDimensions(frameSize.width, frameSize.height);
                    Frame lipFrame = frameManager.newFrame(this.mLipFrameFormat);
                    this.mDenoiseProgram.setSourceRegion(lipRegion);
                    this.mDenoiseProgram.setHostValue("pix_width", Float.valueOf(1.0f / ((float) inputWidth)));
                    this.mDenoiseProgram.setHostValue("pix_height", Float.valueOf(1.0f / ((float) inputHeight)));
                    this.mDenoiseProgram.process(imageFrame, lipFrame);
                    this.mLipSignalFormat.setDimensions(frameSize.width, frameSize.height);
                    Frame lipSignalFrame = frameManager.newFrame(this.mLipSignalFormat);
                    this.mLipSignalProgram.setHostValue("pix_width", Float.valueOf(1.0f / ((float) frameSize.width)));
                    this.mLipSignalProgram.setHostValue("pix_height", Float.valueOf(1.0f / ((float) frameSize.height)));
                    this.mLipSignalProgram.process(lipFrame, lipSignalFrame);
                    lipFrame.release();
                    lipSignalNativeFrame = frameManager.duplicateFrameToTarget(lipSignalFrame, 2);
                    lipSignalFrame.release();
                }
                this.mLipDiffProgram.setHostValue("faceId", Integer.valueOf(faces.getId(i)));
                this.mLipDiffProgram.process(lipSignalNativeFrame, null);
                FrameFormat outputFormat = ObjectFormat.fromClass(LipDiff.class, Integer.parseInt((String) this.mLipDiffProgram.getHostValue("num_lipdiffs")), 2);
                if (faces.count() - (i + 1) == 0) {
                    outputFrame = frameManager.newFrame(outputFormat);
                    this.mLipDiffProgram.process((Frame) null, outputFrame);
                }
                lipSignalNativeFrame.release();
            }
        }
        pushOutput("diffs", outputFrame);
        outputFrame.release();
    }

    private Quad computeSourceRegion(FaceMeta faces, int index, Point size) {
        Point center = new Point(faces.getMouthX(index), faces.getMouthY(index));
        Point eye_l = new Point(faces.getLeftEyeX(index), faces.getLeftEyeY(index));
        Point eye_r = new Point(faces.getRightEyeX(index), faces.getRightEyeY(index));
        Point vert_axis = eye_l.plus(eye_r).times(0.5f).minus(center);
        size.set(0.9f * eye_l.distanceTo(eye_r), 0.5f * vert_axis.length());
        return Rectangle.fromCenterVerticalAxis(center, vert_axis, size);
    }
}
