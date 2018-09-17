package com.google.android.filterpacks.facedetect;

import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;
import android.filterfw.core.Frame;
import android.filterfw.core.FrameFormat;
import android.filterfw.core.FrameManager;
import android.filterfw.core.GenerateFieldPort;
import android.filterfw.core.ShaderProgram;
import android.filterfw.format.ImageFormat;
import android.filterfw.format.ObjectFormat;
import android.filterfw.geometry.Point;
import android.filterfw.geometry.Quad;

public class AndroidMaskFilter extends Filter {
    @GenerateFieldPort(name = "EyeCenterX")
    private float mAndroidCenterX;
    @GenerateFieldPort(name = "EyeCenterY")
    private float mAndroidCenterY;
    @GenerateFieldPort(name = "EyeDistance")
    private float mAndroidEyeDist;
    private ShaderProgram mOverlayProgram;

    public AndroidMaskFilter(String name) {
        super(name);
    }

    public void setupPorts() {
        FrameFormat imageFormat = ImageFormat.create(3, 3);
        FrameFormat facesFormat = ObjectFormat.fromClass(FaceMeta.class, 2);
        addMaskedInputPort("image", imageFormat);
        addMaskedInputPort("faces", facesFormat);
        addMaskedInputPort("android", imageFormat);
        addOutputBasedOnInput("image", "image");
    }

    public FrameFormat getOutputFormat(String portName, FrameFormat inputFormat) {
        return inputFormat;
    }

    public void prepare(FilterContext context) {
        this.mOverlayProgram = ShaderProgram.createIdentity(context);
        this.mOverlayProgram.setBlendEnabled(true);
        this.mOverlayProgram.setBlendFunc(770, 771);
    }

    private float[] rotate2D(float[] x, float[] center, float angle) {
        float[] xx = new float[]{x[0] - center[0], x[1] - center[1]};
        return new float[]{(xx[0] * ((float) Math.cos((double) angle))) - (xx[1] * ((float) Math.sin((double) angle))), (xx[0] * ((float) Math.sin((double) angle))) + (xx[1] * ((float) Math.cos((double) angle)))};
    }

    public void process(FilterContext context) {
        FrameManager frameManager = context.getFrameManager();
        Frame imageFrame = pullInput("image");
        Frame facesFrame = pullInput("faces");
        Frame androidFrame = pullInput("android");
        FrameFormat outputFormat = imageFrame.getFormat();
        FrameFormat androidFormat = androidFrame.getFormat();
        Frame output = frameManager.newFrame(outputFormat);
        output.setDataFromFrame(imageFrame);
        FaceMeta face = (FaceMeta) facesFrame.getObjectValue();
        this.mOverlayProgram.setSourceRect(0.0f, 0.0f, 1.0f, 1.0f);
        this.mOverlayProgram.setTargetRect(0.0f, 0.0f, 1.0f, 1.0f);
        int num_face = face.count();
        for (int i = 0; i < num_face; i++) {
            Point point = new Point(face.getFaceX0(i), face.getFaceY0(i));
            point = new Point(face.getFaceX1(i), face.getFaceY1(i));
            point = new Point(face.getLeftEyeX(i), face.getLeftEyeY(i));
            point = new Point(face.getRightEyeX(i), face.getRightEyeY(i));
            Point center = point.plus(point).times(0.5f);
            float s = point.minus(point).length() / this.mAndroidEyeDist;
            float s2 = ((float) outputFormat.getWidth()) / ((float) outputFormat.getHeight());
            float sy = s / (((float) androidFormat.getWidth()) / ((float) androidFormat.getHeight()));
            Point c = new Point(this.mAndroidCenterX * s, this.mAndroidCenterY * sy);
            Point c0 = new Point(this.mAndroidCenterX, this.mAndroidCenterY);
            Point d = point.minus(point);
            float angle = (float) Math.atan2((double) d.y, (double) d.x);
            Point p0 = new Point(0.0f, 0.0f).minus(c).rotated(angle).mult(1.0f, s2);
            Point p1 = new Point(0.0f, sy).minus(c).rotated(angle).mult(1.0f, s2);
            Point p2 = new Point(s, sy).minus(c).rotated(angle).mult(1.0f, s2);
            this.mOverlayProgram.setTargetRegion(new Quad(p0, new Point(s, 0.0f).minus(c).rotated(angle).mult(1.0f, s2), p1, p2).translated(center));
            this.mOverlayProgram.process(androidFrame, output);
        }
        pushOutput("image", output);
        output.release();
    }
}
