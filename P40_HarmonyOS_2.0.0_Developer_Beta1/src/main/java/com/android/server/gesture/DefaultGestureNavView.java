package com.android.server.gesture;

import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.IWindow;
import android.view.SurfaceView;
import com.huawei.android.view.IWindowEx;

public class DefaultGestureNavView extends SurfaceView {
    public DefaultGestureNavView(Context context) {
        super(context);
    }

    public DefaultGestureNavView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public boolean canReceivePointerEvents() {
        return super.canReceivePointerEvents();
    }

    @Override // android.view.View
    public void transformMatrixToGlobal(Matrix matrix) {
        super.transformMatrixToGlobal(matrix);
    }

    @Override // android.view.View
    public void transformMatrixToLocal(Matrix matrix) {
        super.transformMatrixToLocal(matrix);
    }

    public IWindowEx getWindowEx() {
        IWindow window = getWindow();
        if (window == null) {
            return null;
        }
        IWindowEx windowEx = new IWindowEx();
        windowEx.setIWindow(window);
        return windowEx;
    }
}
