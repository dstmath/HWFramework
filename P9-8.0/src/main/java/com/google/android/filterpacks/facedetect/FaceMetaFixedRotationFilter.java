package com.google.android.filterpacks.facedetect;

import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;
import android.filterfw.core.Frame;
import android.filterfw.core.FrameFormat;
import android.filterfw.core.FrameManager;
import android.filterfw.core.GenerateFieldPort;
import android.filterfw.format.ObjectFormat;
import android.filterfw.geometry.Point;

public class FaceMetaFixedRotationFilter extends Filter {
    @GenerateFieldPort(name = "rotation")
    private int mRotation = 0;

    public FaceMetaFixedRotationFilter(String name) {
        super(name);
    }

    public void setupPorts() {
        FrameFormat facesFormat = ObjectFormat.fromClass(FaceMeta.class, 2);
        addMaskedInputPort("faces", facesFormat);
        addOutputPort("faces", facesFormat);
    }

    public void process(FilterContext context) {
        FrameManager frameManager = context.getFrameManager();
        Frame facesFrame = pullInput("faces");
        if (this.mRotation == 0) {
            pushOutput("faces", facesFrame);
            return;
        }
        FaceMeta faces = (FaceMeta) facesFrame.getObjectValue();
        Frame output = frameManager.newFrame(ObjectFormat.fromClass(FaceMeta.class, faces.count(), 2));
        FaceMeta outfaces = (FaceMeta) output.getObjectValue();
        for (int i = 0; i < outfaces.count(); i++) {
            outfaces.setId(i, faces.getId(i));
            Point p = RotatePointInFrame(this.mRotation, faces.getFaceX0(i), faces.getFaceY0(i));
            outfaces.setFaceX0(i, p.x);
            outfaces.setFaceY0(i, p.y);
            p = RotatePointInFrame(this.mRotation, faces.getFaceX1(i), faces.getFaceY1(i));
            outfaces.setFaceX1(i, p.x);
            outfaces.setFaceY1(i, p.y);
            p = RotatePointInFrame(this.mRotation, faces.getLeftEyeX(i), faces.getLeftEyeY(i));
            outfaces.setLeftEyeX(i, p.x);
            outfaces.setLeftEyeY(i, p.y);
            p = RotatePointInFrame(this.mRotation, faces.getRightEyeX(i), faces.getRightEyeY(i));
            outfaces.setRightEyeX(i, p.x);
            outfaces.setRightEyeY(i, p.y);
            p = RotatePointInFrame(this.mRotation, faces.getMouthX(i), faces.getMouthY(i));
            outfaces.setMouthX(i, p.x);
            outfaces.setMouthY(i, p.y);
            p = RotatePointInFrame(this.mRotation, faces.getUpperLipX(i), faces.getUpperLipY(i));
            outfaces.setUpperLipX(i, p.x);
            outfaces.setUpperLipY(i, p.y);
            p = RotatePointInFrame(this.mRotation, faces.getLowerLipX(i), faces.getLowerLipY(i));
            outfaces.setLowerLipX(i, p.x);
            outfaces.setLowerLipY(i, p.y);
        }
        pushOutput("faces", output);
        output.release();
    }

    private Point RotatePointInFrame(int rotation, float x, float y) {
        switch (rotation) {
            case 90:
                return new Point(1.0f - y, x);
            case 180:
                return new Point(1.0f - x, 1.0f - y);
            case 270:
                return new Point(y, 1.0f - x);
            default:
                return new Point(x, y);
        }
    }
}
