package com.huawei.hwtransition.algorithm;

import android.util.Log;
import android.view.View;
import com.huawei.hwtransition.AlgorithmUtil;

public class WindMillTransition extends BaseTransition {
    private static final float COORDINATE_Y_FACTOR = 2.0f;
    private static int pageAngle;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.hwtransition.algorithm.WindMillTransition.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.hwtransition.algorithm.WindMillTransition.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.hwtransition.algorithm.WindMillTransition.<clinit>():void");
    }

    public WindMillTransition() {
        this.mAnimationType = "2D";
    }

    public boolean transform(int part, boolean isOverScrollFirst, boolean isOverScrollLast, float scrollProgress, View child) {
        int cw = child.getWidth();
        int ch = child.getHeight();
        this.mTransformationInfo.mRotation = (-scrollProgress) * ((float) pageAngle);
        float pivotX = ((float) cw) / COORDINATE_Y_FACTOR;
        this.mTransformationInfo.mPivotX = AlgorithmUtil.transformPivotX(child, pivotX);
        if (ch <= cw) {
            ch = cw;
        }
        float pivotY = ((float) ch) * COORDINATE_Y_FACTOR;
        this.mTransformationInfo.mPivotY = AlgorithmUtil.transformPivotY(child, pivotY);
        if (!(this.mLayout_type != 0 || isOverScrollFirst || isOverScrollLast)) {
            this.mTransformationInfo.mTranslationX = (((float) cw) * scrollProgress) * child.getScaleX();
        }
        this.mTransformationInfo.mMatrixDirty = true;
        return true;
    }

    public static void setPageAngle(int wPageAngle) {
        pageAngle = wPageAngle;
        Log.d(BaseTransition.TAG, "windmill transition page angle is " + pageAngle);
    }
}
