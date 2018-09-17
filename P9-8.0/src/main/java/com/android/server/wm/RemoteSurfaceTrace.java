package com.android.server.wm;

import android.graphics.Rect;
import android.view.SurfaceControl;
import java.io.DataOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;

class RemoteSurfaceTrace extends SurfaceControl {
    static final String TAG = "RemoteSurfaceTrace";
    final DataOutputStream mOut;
    final WindowManagerService mService = this.mWindow.mService;
    final WindowState mWindow;
    final FileDescriptor mWriteFd;

    RemoteSurfaceTrace(FileDescriptor fd, SurfaceControl wrapped, WindowState window) {
        super(wrapped);
        this.mWriteFd = fd;
        this.mOut = new DataOutputStream(new FileOutputStream(fd, false));
        this.mWindow = window;
    }

    public void setAlpha(float alpha) {
        writeFloatEvent("Alpha", alpha);
        super.setAlpha(alpha);
    }

    public void setLayer(int zorder) {
        writeIntEvent("Layer", zorder);
        super.setLayer(zorder);
    }

    public void setPosition(float x, float y) {
        writeFloatEvent("Position", x, y);
        super.setPosition(x, y);
    }

    public void setGeometryAppliesWithResize() {
        writeEvent("GeometryAppliesWithResize");
        super.setGeometryAppliesWithResize();
    }

    public void setSize(int w, int h) {
        writeIntEvent("Size", w, h);
        super.setSize(w, h);
    }

    public void setWindowCrop(Rect crop) {
        writeRectEvent("Crop", crop);
        super.setWindowCrop(crop);
    }

    public void setFinalCrop(Rect crop) {
        writeRectEvent("FinalCrop", crop);
        super.setFinalCrop(crop);
    }

    public void setLayerStack(int layerStack) {
        writeIntEvent("LayerStack", layerStack);
        super.setLayerStack(layerStack);
    }

    public void setMatrix(float dsdx, float dtdx, float dsdy, float dtdy) {
        writeFloatEvent("Matrix", dsdx, dtdx, dsdy, dtdy);
        super.setMatrix(dsdx, dtdx, dsdy, dtdy);
    }

    public void hide() {
        writeEvent("Hide");
        super.hide();
    }

    public void show() {
        writeEvent("Show");
        super.show();
    }

    private void writeEvent(String tag) {
        try {
            this.mOut.writeUTF(tag);
            this.mOut.writeUTF(this.mWindow.getWindowTag().toString());
            writeSigil();
        } catch (Exception e) {
            RemoteEventTrace.logException(e);
            this.mService.disableSurfaceTrace();
        }
    }

    private void writeIntEvent(String tag, int... values) {
        try {
            this.mOut.writeUTF(tag);
            this.mOut.writeUTF(this.mWindow.getWindowTag().toString());
            for (int value : values) {
                this.mOut.writeInt(value);
            }
            writeSigil();
        } catch (Exception e) {
            RemoteEventTrace.logException(e);
            this.mService.disableSurfaceTrace();
        }
    }

    private void writeFloatEvent(String tag, float... values) {
        try {
            this.mOut.writeUTF(tag);
            this.mOut.writeUTF(this.mWindow.getWindowTag().toString());
            for (float value : values) {
                this.mOut.writeFloat(value);
            }
            writeSigil();
        } catch (Exception e) {
            RemoteEventTrace.logException(e);
            this.mService.disableSurfaceTrace();
        }
    }

    private void writeRectEvent(String tag, Rect value) {
        writeFloatEvent(tag, (float) value.left, (float) value.top, (float) value.right, (float) value.bottom);
    }

    private void writeSigil() throws Exception {
        this.mOut.write(RemoteEventTrace.sigil, 0, 4);
    }
}
