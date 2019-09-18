package com.android.server.wm;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.view.Surface;
import android.view.SurfaceControl;
import java.io.PrintWriter;

public class BlackFrame {
    final BlackSurface[] mBlackSurfaces = new BlackSurface[4];
    final boolean mForceDefaultOrientation;
    final Rect mInnerRect;
    final Rect mOuterRect;
    final float[] mTmpFloats = new float[9];
    final Matrix mTmpMatrix = new Matrix();

    class BlackSurface {
        final int layer;
        final int left;
        final SurfaceControl surface;
        final int top;

        BlackSurface(SurfaceControl.Transaction transaction, int layer2, int l, int t, int r, int b, DisplayContent dc) throws Surface.OutOfResourcesException {
            this.left = l;
            this.top = t;
            this.layer = layer2;
            this.surface = dc.makeOverlay().setName("BlackSurface").setSize(r - l, b - t).setColorLayer(true).setParent(null).build();
            transaction.setAlpha(this.surface, 1.0f);
            transaction.setLayer(this.surface, layer2);
            transaction.show(this.surface);
        }

        /* access modifiers changed from: package-private */
        public void setAlpha(SurfaceControl.Transaction t, float alpha) {
            t.setAlpha(this.surface, alpha);
        }

        /* access modifiers changed from: package-private */
        public void setMatrix(SurfaceControl.Transaction t, Matrix matrix) {
            BlackFrame.this.mTmpMatrix.setTranslate((float) this.left, (float) this.top);
            BlackFrame.this.mTmpMatrix.postConcat(matrix);
            BlackFrame.this.mTmpMatrix.getValues(BlackFrame.this.mTmpFloats);
            t.setPosition(this.surface, BlackFrame.this.mTmpFloats[2], BlackFrame.this.mTmpFloats[5]);
            t.setMatrix(this.surface, BlackFrame.this.mTmpFloats[0], BlackFrame.this.mTmpFloats[3], BlackFrame.this.mTmpFloats[1], BlackFrame.this.mTmpFloats[4]);
        }

        /* access modifiers changed from: package-private */
        public void clearMatrix(SurfaceControl.Transaction t) {
            t.setMatrix(this.surface, 1.0f, 0.0f, 0.0f, 1.0f);
        }
    }

    public void printTo(String prefix, PrintWriter pw) {
        pw.print(prefix);
        pw.print("Outer: ");
        this.mOuterRect.printShortString(pw);
        pw.print(" / Inner: ");
        this.mInnerRect.printShortString(pw);
        pw.println();
        for (int i = 0; i < this.mBlackSurfaces.length; i++) {
            BlackSurface bs = this.mBlackSurfaces[i];
            pw.print(prefix);
            pw.print("#");
            pw.print(i);
            pw.print(": ");
            pw.print(bs.surface);
            pw.print(" left=");
            pw.print(bs.left);
            pw.print(" top=");
            pw.println(bs.top);
        }
    }

    public BlackFrame(SurfaceControl.Transaction t, Rect outer, Rect inner, int layer, DisplayContent dc, boolean forceDefaultOrientation) throws Surface.OutOfResourcesException {
        Rect rect = outer;
        Rect rect2 = inner;
        boolean success = false;
        this.mForceDefaultOrientation = forceDefaultOrientation;
        this.mOuterRect = new Rect(rect);
        this.mInnerRect = new Rect(rect2);
        try {
            if (rect.top < rect2.top) {
                BlackSurface[] blackSurfaceArr = this.mBlackSurfaces;
                BlackSurface blackSurface = new BlackSurface(t, layer, rect.left, rect.top, rect2.right, rect2.top, dc);
                blackSurfaceArr[0] = blackSurface;
            }
            if (rect.left < rect2.left) {
                BlackSurface[] blackSurfaceArr2 = this.mBlackSurfaces;
                BlackSurface blackSurface2 = new BlackSurface(t, layer, rect.left, rect2.top, rect2.left, rect.bottom, dc);
                blackSurfaceArr2[1] = blackSurface2;
            }
            if (rect.bottom > rect2.bottom) {
                BlackSurface[] blackSurfaceArr3 = this.mBlackSurfaces;
                BlackSurface blackSurface3 = new BlackSurface(t, layer, rect2.left, rect2.bottom, rect.right, rect.bottom, dc);
                blackSurfaceArr3[2] = blackSurface3;
            }
            if (rect.right > rect2.right) {
                BlackSurface[] blackSurfaceArr4 = this.mBlackSurfaces;
                BlackSurface blackSurface4 = new BlackSurface(t, layer, rect2.right, rect.top, rect.right, rect2.bottom, dc);
                blackSurfaceArr4[3] = blackSurface4;
            }
            success = true;
        } finally {
            if (!success) {
                kill();
            }
        }
    }

    public void kill() {
        if (this.mBlackSurfaces != null) {
            for (int i = 0; i < this.mBlackSurfaces.length; i++) {
                if (this.mBlackSurfaces[i] != null) {
                    this.mBlackSurfaces[i].surface.destroy();
                    this.mBlackSurfaces[i] = null;
                }
            }
        }
    }

    public void hide(SurfaceControl.Transaction t) {
        if (this.mBlackSurfaces != null) {
            for (int i = 0; i < this.mBlackSurfaces.length; i++) {
                if (this.mBlackSurfaces[i] != null) {
                    t.hide(this.mBlackSurfaces[i].surface);
                }
            }
        }
    }

    public void setAlpha(SurfaceControl.Transaction t, float alpha) {
        for (int i = 0; i < this.mBlackSurfaces.length; i++) {
            if (this.mBlackSurfaces[i] != null) {
                this.mBlackSurfaces[i].setAlpha(t, alpha);
            }
        }
    }

    public void setMatrix(SurfaceControl.Transaction t, Matrix matrix) {
        for (int i = 0; i < this.mBlackSurfaces.length; i++) {
            if (this.mBlackSurfaces[i] != null) {
                this.mBlackSurfaces[i].setMatrix(t, matrix);
            }
        }
    }

    public void clearMatrix(SurfaceControl.Transaction t) {
        for (int i = 0; i < this.mBlackSurfaces.length; i++) {
            if (this.mBlackSurfaces[i] != null) {
                this.mBlackSurfaces[i].clearMatrix(t);
            }
        }
    }
}
