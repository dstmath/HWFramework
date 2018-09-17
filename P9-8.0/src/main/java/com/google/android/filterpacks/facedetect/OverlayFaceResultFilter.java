package com.google.android.filterpacks.facedetect;

import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;
import android.filterfw.core.Frame;
import android.filterfw.core.FrameFormat;
import android.filterfw.core.FrameManager;
import android.filterfw.core.ShaderProgram;
import android.filterfw.format.ImageFormat;
import android.filterfw.format.ObjectFormat;
import android.util.Log;

public class OverlayFaceResultFilter extends Filter {
    private final String mBlendShader = "precision mediump float;\nuniform int num_face;\nuniform float alpha;\nuniform vec4 blend_color;\nuniform vec4 face_rect;\nuniform vec2 left_eye;\nuniform vec2 right_eye;\nuniform vec2 mouth_pos;\nuniform vec2 upper_lip_pos;\nuniform vec2 lower_lip_pos;\nuniform sampler2D tex_sampler_0;\nfloat eye_size;\nvec2 face_size;\nvec2 face_center;\nfloat dist;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec4 color = texture2D(tex_sampler_0, v_texcoord);\n  if (num_face!=0) {\n    face_center[0] = (face_rect[0] + face_rect[2]) / 2.0;\n    face_center[1] = (face_rect[1] + face_rect[3]) / 2.0;\n    face_size[0] = (face_rect[2] - face_rect[0]) / 2.0;\n    face_size[1] = (face_rect[3] - face_rect[1]) / 2.0;\n    dist = length((v_texcoord - face_center) / face_size);\n    if (dist<1.0) {\n       eye_size = distance(left_eye, right_eye) / 6.0;\n       if ( distance(left_eye, v_texcoord) < eye_size ||\n            distance(right_eye, v_texcoord) < eye_size) { \n         gl_FragColor = mix(color, vec4(1.0,0,0,1.0), alpha);\n       } else if (distance(mouth_pos, v_texcoord) < 0.5*eye_size ) { \n         gl_FragColor = mix(color, vec4(0,0,1.0,1.0), alpha);\n       } else if (distance(upper_lip_pos, v_texcoord) < 0.5*eye_size ) { \n         gl_FragColor = mix(color, vec4(1.0,1.0,0,1.0), alpha);\n       } else if (distance(lower_lip_pos, v_texcoord) < 0.5*eye_size ) { \n         gl_FragColor = mix(color, vec4(1.0,1.0,0,1.0), alpha);\n       }\n       else gl_FragColor = mix(color, blend_color, alpha);\n    }\n    else {\n      gl_FragColor = color;\n    }\n  } else gl_FragColor = color;\n}\n";
    private ShaderProgram mOverlayProgram;

    public OverlayFaceResultFilter(String name) {
        super(name);
    }

    public void setupPorts() {
        FrameFormat imageFormat = ImageFormat.create(3, 3);
        FrameFormat facesFormat = ObjectFormat.fromClass(FaceMeta.class, 2);
        addMaskedInputPort("image", imageFormat);
        addMaskedInputPort("faces", facesFormat);
        addOutputBasedOnInput("image", "image");
    }

    public FrameFormat getOutputFormat(String portName, FrameFormat inputFormat) {
        return inputFormat;
    }

    protected void prepare(FilterContext context) {
        this.mOverlayProgram = new ShaderProgram(context, "precision mediump float;\nuniform int num_face;\nuniform float alpha;\nuniform vec4 blend_color;\nuniform vec4 face_rect;\nuniform vec2 left_eye;\nuniform vec2 right_eye;\nuniform vec2 mouth_pos;\nuniform vec2 upper_lip_pos;\nuniform vec2 lower_lip_pos;\nuniform sampler2D tex_sampler_0;\nfloat eye_size;\nvec2 face_size;\nvec2 face_center;\nfloat dist;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec4 color = texture2D(tex_sampler_0, v_texcoord);\n  if (num_face!=0) {\n    face_center[0] = (face_rect[0] + face_rect[2]) / 2.0;\n    face_center[1] = (face_rect[1] + face_rect[3]) / 2.0;\n    face_size[0] = (face_rect[2] - face_rect[0]) / 2.0;\n    face_size[1] = (face_rect[3] - face_rect[1]) / 2.0;\n    dist = length((v_texcoord - face_center) / face_size);\n    if (dist<1.0) {\n       eye_size = distance(left_eye, right_eye) / 6.0;\n       if ( distance(left_eye, v_texcoord) < eye_size ||\n            distance(right_eye, v_texcoord) < eye_size) { \n         gl_FragColor = mix(color, vec4(1.0,0,0,1.0), alpha);\n       } else if (distance(mouth_pos, v_texcoord) < 0.5*eye_size ) { \n         gl_FragColor = mix(color, vec4(0,0,1.0,1.0), alpha);\n       } else if (distance(upper_lip_pos, v_texcoord) < 0.5*eye_size ) { \n         gl_FragColor = mix(color, vec4(1.0,1.0,0,1.0), alpha);\n       } else if (distance(lower_lip_pos, v_texcoord) < 0.5*eye_size ) { \n         gl_FragColor = mix(color, vec4(1.0,1.0,0,1.0), alpha);\n       }\n       else gl_FragColor = mix(color, blend_color, alpha);\n    }\n    else {\n      gl_FragColor = color;\n    }\n  } else gl_FragColor = color;\n}\n");
    }

    public void process(FilterContext context) {
        FrameManager frameManager = context.getFrameManager();
        Frame imageFrame = pullInput("image");
        FaceMeta face = (FaceMeta) pullInput("faces").getObjectValue();
        this.mOverlayProgram.setSourceRect(0.0f, 0.0f, 1.0f, 1.0f);
        this.mOverlayProgram.setTargetRect(0.0f, 0.0f, 1.0f, 1.0f);
        this.mOverlayProgram.setHostValue("blend_color", new float[]{0.0f, 1.0f, 0.0f, 1.0f});
        this.mOverlayProgram.setHostValue("alpha", Float.valueOf(0.5f));
        int num_face = face.count();
        this.mOverlayProgram.setHostValue("num_face", Integer.valueOf(num_face));
        Log.i("Overlay Result", "number of faces" + num_face);
        if (num_face != 0) {
            Frame output = frameManager.newFrame(imageFrame.getFormat());
            for (int i = 0; i < num_face; i++) {
                float[] fr = new float[]{face.getFaceX0(i), face.getFaceY0(i), face.getFaceX1(i), face.getFaceY1(i)};
                float[] le = new float[]{face.getLeftEyeX(i), face.getLeftEyeY(i)};
                float[] re = new float[]{face.getRightEyeX(i), face.getRightEyeY(i)};
                float[] mouth = new float[]{face.getMouthX(i), face.getMouthY(i)};
                float[] upper_lip = new float[]{face.getUpperLipX(i), face.getUpperLipY(i)};
                float[] lower_lip = new float[]{face.getLowerLipX(i), face.getLowerLipY(i)};
                this.mOverlayProgram.setHostValue("face_rect", fr);
                this.mOverlayProgram.setHostValue("left_eye", le);
                this.mOverlayProgram.setHostValue("right_eye", re);
                this.mOverlayProgram.setHostValue("mouth_pos", mouth);
                this.mOverlayProgram.setHostValue("upper_lip_pos", upper_lip);
                this.mOverlayProgram.setHostValue("lower_lip_pos", lower_lip);
                if (i > 0) {
                    this.mOverlayProgram.setSourceRect(fr[0], fr[1], fr[2] - fr[0], fr[3] - fr[1]);
                    this.mOverlayProgram.setTargetRect(fr[0], fr[1], fr[2] - fr[0], fr[3] - fr[1]);
                }
                this.mOverlayProgram.process(imageFrame, output);
            }
            pushOutput("image", output);
            output.release();
            return;
        }
        pushOutput("image", imageFrame);
    }
}
