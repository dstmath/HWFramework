package com.google.android.filterpacks.facedetect;

import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;
import android.filterfw.core.Frame;
import android.filterfw.core.FrameFormat;
import android.filterfw.core.NativeProgram;
import android.filterfw.core.ShaderProgram;
import android.filterfw.format.ImageFormat;
import android.filterfw.format.ObjectFormat;
import android.filterfw.format.PrimitiveFormat;
import android.graphics.Rect;

public abstract class FaceMaskEffectFilter extends Filter {
    private static final int PCA_DATA_SIZE = 15;
    protected static final String RGB_TO_YUV_MATRIX = "0.299, -0.168736,  0.5,      0.000, 0.587, -0.331264, -0.418688, 0.000, 0.114,  0.5,      -0.081312, 0.000, 0.000,  0.5,       0.5,      1.000 ";
    protected static final String YUV_TO_RGB_MATRIX = " 1.0,       1.0,       1.0,   0.0, -0.000001, -0.344135,  1.772, 0.0,  1.401999, -0.714136,  0.0,   0.0, -0.700999,  0.529135, -0.886, 1.000";
    private static final String mRgbToYuvShader = "precision mediump float;\nuniform sampler2D tex_sampler_0;\nconst mat4 coeff_yuv = mat4(0.299, -0.168736,  0.5,      0.000, 0.587, -0.331264, -0.418688, 0.000, 0.114,  0.5,      -0.081312, 0.000, 0.000,  0.5,       0.5,      1.000 );\nvarying vec2 v_texcoord;\nvoid main() {\n  vec4 rgb = vec4(texture2D(tex_sampler_0, v_texcoord).rgb, 1.);\n  gl_FragColor = coeff_yuv * rgb;\n}\n";
    protected NativeProgram mColorPcaProgram;
    protected int mHeight = 0;
    protected ShaderProgram mIdentityProgram;
    protected ShaderProgram mRgbToYuvProgram;
    protected int mWidth = 0;

    protected abstract void initPrograms(FilterContext filterContext);

    public FaceMaskEffectFilter(String name) {
        super(name);
    }

    public void setupPorts() {
        addMaskedInputPort("faces", ObjectFormat.fromClass(FaceMeta.class, 2));
        addMaskedInputPort("image", ImageFormat.create(3));
        addOutputBasedOnInput("image", "image");
    }

    public FrameFormat getOutputFormat(String portName, FrameFormat inputFormat) {
        return inputFormat;
    }

    protected void prepare(FilterContext context) {
        this.mRgbToYuvProgram = new ShaderProgram(context, mRgbToYuvShader);
        this.mIdentityProgram = ShaderProgram.createIdentity(context);
        this.mColorPcaProgram = new NativeProgram("filterpack_facedetect", "color_pca");
        initPrograms(context);
    }

    protected Frame createTanhTable(FilterContext context, int tableSize, float maxValue, float offset, float scale) {
        float stepSize = maxValue / (((float) tableSize) - 1.0f);
        int[] array = new int[tableSize];
        for (int i = 0; i < tableSize; i++) {
            array[i] = (int) (65535.0f * ((((float) Math.tanh((double) ((-scale) * ((((float) i) * stepSize) - offset)))) * 0.5f) + 0.5f));
        }
        Frame tableFrame = context.getFrameManager().newFrame(ImageFormat.create(tableSize, 1, 3, 3));
        tableFrame.setInts(array);
        return tableFrame;
    }

    protected Rect createBoundedRect(Frame input, Rect rect) {
        return new Rect(Math.max(rect.left, 0), Math.max(rect.top, 0), Math.min(rect.right, input.getFormat().getWidth() - 1), Math.min(rect.bottom, input.getFormat().getHeight() - 1));
    }

    protected Frame cropRectRegion(FilterContext context, Frame input, Rect rect) {
        Rect cropRect = createBoundedRect(input, rect);
        int width = cropRect.width();
        int height = cropRect.height();
        int inputWidth = input.getFormat().getWidth();
        int inputHeight = input.getFormat().getHeight();
        Frame output = context.getFrameManager().newFrame(ImageFormat.create(width, height, 3, 3));
        this.mIdentityProgram.setSourceRect(((float) cropRect.left) / ((float) inputWidth), ((float) cropRect.top) / ((float) inputHeight), ((float) width) / ((float) inputWidth), ((float) height) / ((float) inputHeight));
        this.mIdentityProgram.setTargetRect(0.0f, 0.0f, 1.0f, 1.0f);
        this.mIdentityProgram.process(input, output);
        return output;
    }

    protected void computeFaceColorPCA(FilterContext context, Frame input, Rect faceRect, float[] mean_vec, float[] eigen_values, float[] eigen_vectors) {
        Frame faceFrame = cropRectRegion(context, input, faceRect);
        Frame nativeFace = context.getFrameManager().duplicateFrameToTarget(faceFrame, 2);
        Frame output = context.getFrameManager().newFrame(PrimitiveFormat.createFloatFormat(PCA_DATA_SIZE, 2));
        this.mColorPcaProgram.setHostValue("width", Integer.valueOf(faceFrame.getFormat().getWidth()));
        this.mColorPcaProgram.setHostValue("height", Integer.valueOf(faceFrame.getFormat().getHeight()));
        this.mColorPcaProgram.process(nativeFace, output);
        float[] pcaParams = output.getFloats();
        for (int i = 0; i < 3; i++) {
            mean_vec[i] = pcaParams[i];
            eigen_values[i] = pcaParams[i + 3];
            for (int j = 0; j < 3; j++) {
                int idx = (i * 3) + j;
                eigen_vectors[idx] = pcaParams[idx + 6];
            }
        }
        output.release();
        faceFrame.release();
    }
}
