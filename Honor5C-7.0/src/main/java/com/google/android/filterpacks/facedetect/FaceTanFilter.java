package com.google.android.filterpacks.facedetect;

import android.filterfw.core.FilterContext;
import android.filterfw.core.Frame;
import android.filterfw.core.FrameFormat;
import android.filterfw.core.GLFrame;
import android.filterfw.core.ShaderProgram;
import android.graphics.Rect;
import android.opengl.GLES20;

public class FaceTanFilter extends FaceMaskEffectFilter {
    private static final String TAN_SKIN_MATRIX = "1.0, 0.0, 0.0, 0.0, 0.6043903, -0.7966884, 0.0, 0.7966884, 0.6043903";
    private static final String TAN_SKIN_MEAN = "0.50137526, 0.4253831, 0.6052874";
    private static final String TAN_SKIN_SINGULAR_VALUES = "0.1549749, 0.05620472, 0.0084233275";
    private static final String mFaceTanShader = "precision highp float;\nuniform sampler2D tex_sampler_0;\nuniform sampler2D tex_sampler_1;\nuniform sampler2D tex_sampler_2;\nuniform sampler2D tex_sampler_3;\nconst vec3 tan_mean = vec3(0.50137526, 0.4253831, 0.6052874);\nconst vec3 tan_scale = vec3(0.1549749, 0.05620472, 0.0084233275);\nconst mat3 tan_mat = mat3(1.0, 0.0, 0.0, 0.0, 0.6043903, -0.7966884, 0.0, 0.7966884, 0.6043903);\nconst mat4 coeff_rgb = mat4( 1.0,       1.0,       1.0,   0.0, -0.000001, -0.344135,  1.772, 0.0,  1.401999, -0.714136,  0.0,   0.0, -0.700999,  0.529135, -0.886, 1.000);\nuniform vec3 yuv_mean;\nuniform mat3 yuv_var;\nuniform vec3 yuv_scale;\nuniform float tbl_offset;\nuniform float tbl_scale;\nuniform vec2 face_center;\nuniform vec2 face_scale;\nuniform vec2 body_center;\nuniform vec2 body_scale;\nvarying vec2 v_texcoord;\nfloat computeSpatialWeight(sampler2D sampler, float dist) {\n  float value = min(dist * 0.2, 1.0);\n  float tbl_value = tbl_offset + tbl_scale * value;\n  vec4 weight_byte = texture2D(sampler, vec2(tbl_value, 0.5));\n  return weight_byte.g + 0.00390625 * weight_byte.r;\n}\nvoid main() {\n  vec3 yuv = texture2D(tex_sampler_0, v_texcoord).xyz;\n  float gray = yuv.r;\n  vec3 proj = yuv_var * (yuv - yuv_mean);\n  proj = yuv_scale * proj;\n  vec3 scaled_proj = tan_scale * proj;\n  vec3 new_yuv =  tan_mat * scaled_proj + tan_mean;\n  vec4 new_color = coeff_rgb * vec4(new_yuv, 1.0);\n  new_color = min(max(new_color, 0.0), 1.0);\n  float dist = sqrt(0.5 * dot(proj, proj));\n  float color_weight = computeSpatialWeight(tex_sampler_1, dist);\n  vec2 diff = v_texcoord - face_center;\n  dist = length(face_scale * diff);\n  float face_weight = computeSpatialWeight(tex_sampler_2, dist);\n  diff = v_texcoord - body_center;\n  dist = length(body_scale * diff);\n  float body_weight = computeSpatialWeight(tex_sampler_2, dist);\n  float alpha = max(face_weight, body_weight) * color_weight;\n  if (gray < 0.1) {\n    alpha = 0.0;\n  } else {\n    if (gray < 0.5) {\n      alpha *=  2.5 * (gray - 0.1);\n    }\n  }\n  vec4 orig = texture2D(tex_sampler_3, v_texcoord);\n  if (orig.a > alpha) {\n     gl_FragColor = orig;\n  } else { \n    gl_FragColor = vec4(new_color.rgb, alpha);\n  }\n}\n";
    private Frame mColorTanhFrame;
    private ShaderProgram mFaceTanProgram;
    private Frame mSpatialTanhFrame;

    public FaceTanFilter(String name) {
        super(name);
    }

    public void tearDown(FilterContext context) {
    }

    protected void initPrograms(FilterContext context) {
        this.mFaceTanProgram = new ShaderProgram(context, mFaceTanShader);
    }

    private void initParameters(FilterContext context) {
        this.mColorTanhFrame = createTanhTable(context, 500, 5.0f, 1.15f, 2.5f);
        this.mSpatialTanhFrame = createTanhTable(context, 500, 5.0f, 1.25f, 0.75f);
        this.mFaceTanProgram.setHostValue("tbl_offset", Float.valueOf(0.001f));
        this.mFaceTanProgram.setHostValue("tbl_scale", Float.valueOf(499.0f / 500.0f));
    }

    private Rect createBodyRect(Rect faceRect) {
        int centerX = faceRect.centerX() + (faceRect.width() * 0);
        int centerY = faceRect.centerX() + (faceRect.height() * 2);
        int width = faceRect.width() * 3;
        int height = faceRect.height() * 3;
        return new Rect(centerX - (width / 2), centerY - (height / 2), (width / 2) + centerX, (height / 2) + centerY);
    }

    public void process(FilterContext context) {
        Frame input = pullInput("image");
        FrameFormat inputFormat = input.getFormat();
        FaceMeta faces = (FaceMeta) pullInput("faces").getObjectValue();
        int faceCount = faces.count();
        if (this.mIdentityProgram == null || this.mColorPcaProgram == null || this.mRgbToYuvProgram == null || this.mFaceTanProgram == null) {
            throw new RuntimeException("programs are missing at process.");
        }
        Frame yuvFrame = context.getFrameManager().newFrame(inputFormat);
        this.mRgbToYuvProgram.process(input, yuvFrame);
        Frame bufferOne = context.getFrameManager().newFrame(inputFormat);
        Frame bufferTwo = context.getFrameManager().newFrame(inputFormat);
        ((GLFrame) bufferOne).focus();
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(16384);
        ((GLFrame) bufferTwo).focus();
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(16384);
        if (!(inputFormat.getWidth() == this.mWidth && inputFormat.getHeight() == this.mHeight)) {
            this.mWidth = inputFormat.getWidth();
            this.mHeight = inputFormat.getHeight();
            initParameters(context);
        }
        Frame source = bufferOne;
        Frame target = bufferTwo;
        float[] color_mean = new float[3];
        float[] eigen_values = new float[3];
        float[] eigen_vectors = new float[9];
        Rect faceRect = new Rect();
        for (int i = 0; i < faceCount; i++) {
            source = target;
            target = target == bufferOne ? bufferTwo : bufferOne;
            faceRect.left = Math.round(((float) this.mWidth) * faces.getFaceX0(i));
            faceRect.top = Math.round(((float) this.mHeight) * faces.getFaceY0(i));
            faceRect.right = Math.round(((float) this.mWidth) * faces.getFaceX1(i));
            faceRect.bottom = Math.round(((float) this.mHeight) * faces.getFaceY1(i));
            computeFaceColorPCA(context, yuvFrame, faceRect, color_mean, eigen_values, eigen_vectors);
            Rect bodyRect = createBodyRect(faceRect);
            float[] faceCenter = new float[]{((float) faceRect.centerX()) / ((float) this.mWidth), ((float) faceRect.centerY()) / ((float) this.mHeight)};
            Object faceScale = new float[]{(((float) this.mWidth) * 1.75f) / ((float) faceRect.width()), (((float) this.mHeight) * 1.75f) / ((float) faceRect.height())};
            float[] bodyCenter = new float[]{((float) bodyRect.centerX()) / ((float) this.mWidth), ((float) bodyRect.centerY()) / ((float) this.mHeight)};
            float[] bodyScale = new float[]{(((float) this.mWidth) * 1.75f) / ((float) bodyRect.width()), (((float) this.mHeight) * 1.75f) / ((float) bodyRect.height())};
            Object rgb_scale = new float[]{1.0f / eigen_values[0], 1.0f / eigen_values[1], 1.0f / eigen_values[2]};
            this.mFaceTanProgram.setHostValue("yuv_mean", color_mean);
            this.mFaceTanProgram.setHostValue("yuv_var", eigen_vectors);
            this.mFaceTanProgram.setHostValue("yuv_scale", rgb_scale);
            this.mFaceTanProgram.setHostValue("face_center", faceCenter);
            this.mFaceTanProgram.setHostValue("face_scale", faceScale);
            this.mFaceTanProgram.setHostValue("body_center", faceCenter);
            this.mFaceTanProgram.setHostValue("body_scale", faceScale);
            this.mFaceTanProgram.process(new Frame[]{yuvFrame, this.mColorTanhFrame, this.mSpatialTanhFrame, source}, target);
        }
        pushOutput("image", target);
        bufferOne.release();
        bufferTwo.release();
        yuvFrame.release();
    }
}
