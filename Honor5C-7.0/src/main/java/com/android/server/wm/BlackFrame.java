package com.android.server.wm;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.view.Surface.OutOfResourcesException;
import android.view.SurfaceControl;
import android.view.SurfaceSession;
import java.io.PrintWriter;

public class BlackFrame {
    final BlackSurface[] mBlackSurfaces;
    final boolean mForceDefaultOrientation;
    final Rect mInnerRect;
    final Rect mOuterRect;
    final float[] mTmpFloats;
    final Matrix mTmpMatrix;

    class BlackSurface {
        final int layer;
        final int left;
        final SurfaceControl surface;
        final int top;

        BlackSurface(SurfaceSession session, int layer, int l, int t, int r, int b, int layerStack) throws OutOfResourcesException {
            this.left = l;
            this.top = t;
            this.layer = layer;
            SurfaceSession surfaceSession = session;
            this.surface = new SurfaceControl(surfaceSession, "BlackSurface", r - l, b - t, -1, 131076);
            this.surface.setAlpha(1.0f);
            this.surface.setLayerStack(layerStack);
            this.surface.setLayer(layer);
            this.surface.show();
        }

        void setAlpha(float alpha) {
            this.surface.setAlpha(alpha);
        }

        void setMatrix(Matrix matrix) {
            BlackFrame.this.mTmpMatrix.setTranslate((float) this.left, (float) this.top);
            BlackFrame.this.mTmpMatrix.postConcat(matrix);
            BlackFrame.this.mTmpMatrix.getValues(BlackFrame.this.mTmpFloats);
            this.surface.setPosition(BlackFrame.this.mTmpFloats[2], BlackFrame.this.mTmpFloats[5]);
            this.surface.setMatrix(BlackFrame.this.mTmpFloats[0], BlackFrame.this.mTmpFloats[3], BlackFrame.this.mTmpFloats[1], BlackFrame.this.mTmpFloats[4]);
        }

        void clearMatrix() {
            this.surface.setMatrix(1.0f, 0.0f, 0.0f, 1.0f);
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

    public BlackFrame(SurfaceSession session, Rect outer, Rect inner, int layer, int layerStack, boolean forceDefaultOrientation) throws OutOfResourcesException {
        this.mTmpMatrix = new Matrix();
        this.mTmpFloats = new float[9];
        this.mBlackSurfaces = new BlackSurface[4];
        this.mForceDefaultOrientation = forceDefaultOrientation;
        this.mOuterRect = new Rect(outer);
        this.mInnerRect = new Rect(inner);
        try {
            if (outer.top < inner.top) {
                this.mBlackSurfaces[0] = new BlackSurface(session, layer, outer.left, outer.top, inner.right, inner.top, layerStack);
            }
            if (outer.left < inner.left) {
                this.mBlackSurfaces[1] = new BlackSurface(session, layer, outer.left, inner.top, inner.left, outer.bottom, layerStack);
            }
            if (outer.bottom > inner.bottom) {
                this.mBlackSurfaces[2] = new BlackSurface(session, layer, inner.left, inner.bottom, outer.right, outer.bottom, layerStack);
            }
            if (outer.right > inner.right) {
                this.mBlackSurfaces[3] = new BlackSurface(session, layer, inner.right, outer.top, outer.right, inner.bottom, layerStack);
            }
            if (!true) {
                kill();
            }
        } catch (Throwable th) {
            if (!false) {
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

    public void hide() {
        if (this.mBlackSurfaces != null) {
            for (int i = 0; i < this.mBlackSurfaces.length; i++) {
                if (this.mBlackSurfaces[i] != null) {
                    this.mBlackSurfaces[i].surface.hide();
                }
            }
        }
    }

    public void setAlpha(float alpha) {
        for (int i = 0; i < this.mBlackSurfaces.length; i++) {
            if (this.mBlackSurfaces[i] != null) {
                this.mBlackSurfaces[i].setAlpha(alpha);
            }
        }
    }

    public void setMatrix(Matrix matrix) {
        for (int i = 0; i < this.mBlackSurfaces.length; i++) {
            if (this.mBlackSurfaces[i] != null) {
                this.mBlackSurfaces[i].setMatrix(matrix);
            }
        }
    }

    public void clearMatrix() {
        for (int i = 0; i < this.mBlackSurfaces.length; i++) {
            if (this.mBlackSurfaces[i] != null) {
                this.mBlackSurfaces[i].clearMatrix();
            }
        }
    }
}
