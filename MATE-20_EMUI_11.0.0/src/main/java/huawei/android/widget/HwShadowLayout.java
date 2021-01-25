package huawei.android.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import huawei.android.widget.effect.engine.HwShadowEngine;

public class HwShadowLayout extends FrameLayout {
    private HwShadowEngine mShadowEngine = new HwShadowEngine(this, HwShadowEngine.ShadowType.Medium);

    public HwShadowLayout(Context context) {
        super(context);
    }

    public HwShadowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HwShadowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.FrameLayout, android.view.View, android.view.ViewGroup
    public void onLayout(boolean isChanged, int left, int top, int right, int bottom) {
        super.onLayout(isChanged, left, top, right, bottom);
        if (this.mShadowEngine.isEnable() && (getParent() instanceof ViewGroup)) {
            ((ViewGroup) getParent()).setClipChildren(false);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        this.mShadowEngine.renderShadow(canvas);
        super.onDraw(canvas);
    }

    public boolean isShadowEnable() {
        return this.mShadowEngine.isEnable();
    }

    public void setShadowEnable(boolean isEnable) {
        this.mShadowEngine.setEnable(isEnable);
    }
}
