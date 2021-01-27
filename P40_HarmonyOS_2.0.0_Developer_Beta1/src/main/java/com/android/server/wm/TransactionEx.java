package com.android.server.wm;

import android.graphics.Rect;
import android.view.SurfaceControl;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class TransactionEx {
    private SurfaceControl.Transaction transaction = null;

    public TransactionEx(SurfaceControl.Transaction transaction2) {
        this.transaction = transaction2;
    }

    public SurfaceControl.Transaction getTransaction() {
        return this.transaction;
    }

    public TransactionEx setMatrix(SurfaceControl sc, float dsdx, float dtdx, float dtdy, float dsdy) {
        this.transaction.setMatrix(sc, dsdx, dtdx, dtdy, dsdy);
        return this;
    }

    public TransactionEx setPosition(SurfaceControl sc, float x, float y) {
        this.transaction.setPosition(sc, x, y);
        return this;
    }

    public TransactionEx setCornerRadius(SurfaceControl sc, float cornerRadius) {
        this.transaction.setCornerRadius(sc, cornerRadius);
        return this;
    }

    public TransactionEx setWindowCrop(SurfaceControl sc, Rect crop) {
        this.transaction.setWindowCrop(sc, crop);
        return this;
    }
}
