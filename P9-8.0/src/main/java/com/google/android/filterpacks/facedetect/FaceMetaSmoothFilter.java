package com.google.android.filterpacks.facedetect;

import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;
import android.filterfw.core.Frame;
import android.filterfw.core.FrameFormat;
import android.filterfw.core.FrameManager;
import android.filterfw.format.ObjectFormat;
import android.os.SystemClock;
import java.util.HashMap;
import java.util.Vector;

public class FaceMetaSmoothFilter extends Filter {
    private HashMap<Integer, FacePos> mLastPositions = new HashMap();
    final long mMaximumGap = 300;
    final long mMaximumPredictionGap = 300;
    final float mSmoothingRate = 0.5f;

    public class FacePos implements Cloneable {
        public float face_x0;
        public float face_x1;
        public float face_y0;
        public float face_y1;
        public int id;
        public long last_seen;
        public float left_eye_x;
        public float left_eye_y;
        public float lower_lip_x;
        public float lower_lip_y;
        public float mouth_x;
        public float mouth_y;
        public float right_eye_x;
        public float right_eye_y;
        public float speed_x;
        public float speed_y;
        public float upper_lip_x;
        public float upper_lip_y;

        public FacePos clone() {
            try {
                return (FacePos) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException("Clone not supported!");
            }
        }
    }

    private Vector<FacePos> getCurrentPositions(FaceMeta face) {
        FacePos p;
        float dy;
        Vector<FacePos> retFaces = new Vector();
        int num_face = face.count();
        long t = SystemClock.elapsedRealtime();
        for (int i = 0; i < num_face; i++) {
            p = new FacePos();
            p.left_eye_x = face.getLeftEyeX(i);
            p.left_eye_y = face.getLeftEyeY(i);
            p.right_eye_x = face.getRightEyeX(i);
            p.right_eye_y = face.getRightEyeY(i);
            p.mouth_x = face.getMouthX(i);
            p.mouth_y = face.getMouthY(i);
            p.face_x0 = face.getFaceX0(i);
            p.face_y0 = face.getFaceY0(i);
            p.face_x1 = face.getFaceX1(i);
            p.face_y1 = face.getFaceY1(i);
            p.upper_lip_x = face.getUpperLipX(i);
            p.upper_lip_y = face.getUpperLipY(i);
            p.lower_lip_x = face.getLowerLipX(i);
            p.lower_lip_y = face.getLowerLipY(i);
            p.id = face.getId(i);
            p.last_seen = t;
            p.speed_x = 0.0f;
            p.speed_y = 0.0f;
            if (this.mLastPositions.containsKey(Integer.valueOf(p.id))) {
                FacePos v = (FacePos) this.mLastPositions.get(Integer.valueOf(p.id));
                dy = ((p.left_eye_y - v.left_eye_y) + (p.right_eye_y - v.right_eye_y)) / 2.0f;
                float dt = ((float) (t - v.last_seen)) / 1000.0f;
                p.speed_x = v.speed_x + ((((((p.left_eye_x - v.left_eye_x) + (p.right_eye_x - v.right_eye_x)) / 2.0f) / dt) - v.speed_x) * 0.5f);
                p.speed_y = v.speed_y + (((dy / dt) - v.speed_y) * 0.5f);
                this.mLastPositions.put(Integer.valueOf(p.id), p);
            } else {
                this.mLastPositions.put(Integer.valueOf(p.id), p);
            }
            retFaces.add(p);
        }
        Vector<Integer> removeList = new Vector();
        for (FacePos f : this.mLastPositions.values()) {
            if (f.last_seen < t - 300) {
                removeList.add(Integer.valueOf(f.id));
            } else if (f.last_seen != t && f.last_seen > t - 300) {
                p = f.clone();
                float dx = (f.speed_x / 1000.0f) * ((float) (t - f.last_seen));
                dy = (f.speed_x / 1000.0f) * ((float) (t - f.last_seen));
                p.left_eye_x += dx;
                p.left_eye_y += dy;
                p.right_eye_x += dx;
                p.right_eye_y += dy;
                p.mouth_x += dx;
                p.mouth_y += dy;
                p.face_x0 += dx;
                p.face_y0 += dy;
                p.face_x1 += dx;
                p.face_y1 += dy;
                p.upper_lip_x += dx;
                p.upper_lip_y += dy;
                p.lower_lip_x += dx;
                p.lower_lip_y += dy;
                p.id = f.id;
                retFaces.add(p);
            }
        }
        for (Integer i2 : removeList) {
            this.mLastPositions.remove(i2);
        }
        return retFaces;
    }

    public FaceMetaSmoothFilter(String name) {
        super(name);
    }

    public void setupPorts() {
        FrameFormat facesFormat = ObjectFormat.fromClass(FaceMeta.class, 2);
        addMaskedInputPort("faces", facesFormat);
        addOutputPort("faces", facesFormat);
    }

    public void process(FilterContext context) {
        FrameManager frameManager = context.getFrameManager();
        Vector<FacePos> vnewfaces = getCurrentPositions((FaceMeta) pullInput("faces").getObjectValue());
        FacePos[] newfaces = new FacePos[vnewfaces.size()];
        vnewfaces.toArray(newfaces);
        Frame output = frameManager.newFrame(ObjectFormat.fromClass(FaceMeta.class, newfaces.length, 2));
        FaceMeta outfaces = (FaceMeta) output.getObjectValue();
        for (int i = 0; i < outfaces.count(); i++) {
            outfaces.setId(i, newfaces[i].id);
            outfaces.setFaceX0(i, newfaces[i].face_x0);
            outfaces.setFaceY0(i, newfaces[i].face_y0);
            outfaces.setFaceX1(i, newfaces[i].face_x1);
            outfaces.setFaceY1(i, newfaces[i].face_y1);
            outfaces.setLeftEyeX(i, newfaces[i].left_eye_x);
            outfaces.setLeftEyeY(i, newfaces[i].left_eye_y);
            outfaces.setRightEyeX(i, newfaces[i].right_eye_x);
            outfaces.setRightEyeY(i, newfaces[i].right_eye_y);
            outfaces.setMouthX(i, newfaces[i].mouth_x);
            outfaces.setMouthY(i, newfaces[i].mouth_y);
            outfaces.setUpperLipX(i, newfaces[i].upper_lip_x);
            outfaces.setUpperLipY(i, newfaces[i].upper_lip_y);
            outfaces.setLowerLipX(i, newfaces[i].lower_lip_x);
            outfaces.setLowerLipY(i, newfaces[i].lower_lip_y);
        }
        pushOutput("faces", output);
        output.release();
    }
}
