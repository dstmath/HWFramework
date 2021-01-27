package com.android.server.wm;

import android.graphics.Matrix;
import android.view.DisplayInfo;
import android.view.SurfaceControl;
import com.android.server.wm.utils.CoordinateTransforms;
import java.io.PrintWriter;
import java.io.StringWriter;

public class SeamlessRotator {
    private final float[] mFloat9 = new float[9];
    private final int mNewRotation;
    private final int mOldRotation;
    private final Matrix mTransform = new Matrix();

    public SeamlessRotator(int oldRotation, int newRotation, DisplayInfo info) {
        this.mOldRotation = oldRotation;
        this.mNewRotation = newRotation;
        boolean flipped = true;
        if (!(info.rotation == 1 || info.rotation == 3)) {
            flipped = false;
        }
        int h = flipped ? info.logicalWidth : info.logicalHeight;
        int w = flipped ? info.logicalHeight : info.logicalWidth;
        Matrix tmp = new Matrix();
        CoordinateTransforms.transformLogicalToPhysicalCoordinates(oldRotation, w, h, this.mTransform);
        CoordinateTransforms.transformPhysicalToLogicalCoordinates(newRotation, w, h, tmp);
        this.mTransform.postConcat(tmp);
    }

    public void unrotate(SurfaceControl.Transaction transaction, WindowState win) {
        transaction.setMatrix(win.getSurfaceControl(), this.mTransform, this.mFloat9);
        float[] winSurfacePos = {(float) win.mLastSurfacePosition.x, (float) win.mLastSurfacePosition.y};
        this.mTransform.mapPoints(winSurfacePos);
        transaction.setPosition(win.getSurfaceControl(), winSurfacePos[0], winSurfacePos[1]);
    }

    public int getOldRotation() {
        return this.mOldRotation;
    }

    public void finish(WindowState win, boolean timeout) {
        this.mTransform.reset();
        SurfaceControl.Transaction t = win.getPendingTransaction();
        t.setMatrix(win.mSurfaceControl, this.mTransform, this.mFloat9);
        t.setPosition(win.mSurfaceControl, (float) win.mLastSurfacePosition.x, (float) win.mLastSurfacePosition.y);
        if (win.mWinAnimator.mSurfaceController != null && !timeout) {
            t.deferTransactionUntil(win.mSurfaceControl, win.mWinAnimator.mSurfaceController.getHandle(), win.getFrameNumber());
            t.deferTransactionUntil(win.mWinAnimator.mSurfaceController.mSurfaceControl, win.mWinAnimator.mSurfaceController.getHandle(), win.getFrameNumber());
        }
    }

    public void dump(PrintWriter pw) {
        pw.print("{old=");
        pw.print(this.mOldRotation);
        pw.print(", new=");
        pw.print(this.mNewRotation);
        pw.print("}");
    }

    public String toString() {
        StringWriter sw = new StringWriter();
        dump(new PrintWriter(sw));
        return "ForcedSeamlessRotator" + sw.toString();
    }
}
