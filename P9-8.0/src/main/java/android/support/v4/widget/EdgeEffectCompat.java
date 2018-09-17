package android.support.v4.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Build.VERSION;
import android.support.annotation.RequiresApi;
import android.widget.EdgeEffect;

public final class EdgeEffectCompat {
    private static final EdgeEffectBaseImpl IMPL;
    private EdgeEffect mEdgeEffect;

    static class EdgeEffectBaseImpl {
        EdgeEffectBaseImpl() {
        }

        public void onPull(EdgeEffect edgeEffect, float deltaDistance, float displacement) {
            edgeEffect.onPull(deltaDistance);
        }
    }

    @RequiresApi(21)
    static class EdgeEffectApi21Impl extends EdgeEffectBaseImpl {
        EdgeEffectApi21Impl() {
        }

        public void onPull(EdgeEffect edgeEffect, float deltaDistance, float displacement) {
            edgeEffect.onPull(deltaDistance, displacement);
        }
    }

    static {
        if (VERSION.SDK_INT >= 21) {
            IMPL = new EdgeEffectApi21Impl();
        } else {
            IMPL = new EdgeEffectBaseImpl();
        }
    }

    @Deprecated
    public EdgeEffectCompat(Context context) {
        this.mEdgeEffect = new EdgeEffect(context);
    }

    @Deprecated
    public void setSize(int width, int height) {
        this.mEdgeEffect.setSize(width, height);
    }

    @Deprecated
    public boolean isFinished() {
        return this.mEdgeEffect.isFinished();
    }

    @Deprecated
    public void finish() {
        this.mEdgeEffect.finish();
    }

    @Deprecated
    public boolean onPull(float deltaDistance) {
        this.mEdgeEffect.onPull(deltaDistance);
        return true;
    }

    @Deprecated
    public boolean onPull(float deltaDistance, float displacement) {
        IMPL.onPull(this.mEdgeEffect, deltaDistance, displacement);
        return true;
    }

    public static void onPull(EdgeEffect edgeEffect, float deltaDistance, float displacement) {
        IMPL.onPull(edgeEffect, deltaDistance, displacement);
    }

    @Deprecated
    public boolean onRelease() {
        this.mEdgeEffect.onRelease();
        return this.mEdgeEffect.isFinished();
    }

    @Deprecated
    public boolean onAbsorb(int velocity) {
        this.mEdgeEffect.onAbsorb(velocity);
        return true;
    }

    @Deprecated
    public boolean draw(Canvas canvas) {
        return this.mEdgeEffect.draw(canvas);
    }
}
