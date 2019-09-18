package com.android.systemui.shared.recents.view;

import android.app.ActivityOptions;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.IRemoteCallback;
import android.os.RemoteException;
import android.view.DisplayListCanvas;
import android.view.RenderNode;
import android.view.ThreadedRenderer;
import android.view.View;
import java.util.function.Consumer;

public class RecentsTransition {
    public static ActivityOptions createAspectScaleAnimation(Context context, Handler handler, boolean scaleUp, AppTransitionAnimationSpecsFuture animationSpecsFuture, final Runnable animationStartCallback) {
        return ActivityOptions.makeMultiThumbFutureAspectScaleAnimation(context, handler, animationSpecsFuture != null ? animationSpecsFuture.getFuture() : null, new ActivityOptions.OnAnimationStartedListener() {
            private boolean mHandled;

            public void onAnimationStarted() {
                if (!this.mHandled) {
                    this.mHandled = true;
                    if (animationStartCallback != null) {
                        animationStartCallback.run();
                    }
                }
            }
        }, scaleUp);
    }

    public static IRemoteCallback wrapStartedListener(final Handler handler, final Runnable animationStartCallback) {
        if (animationStartCallback == null) {
            return null;
        }
        return new IRemoteCallback.Stub() {
            public void sendResult(Bundle data) throws RemoteException {
                handler.post(animationStartCallback);
            }
        };
    }

    public static Bitmap drawViewIntoHardwareBitmap(int width, int height, final View view, final float scale, final int eraseColor) {
        return createHardwareBitmap(width, height, new Consumer<Canvas>() {
            public void accept(Canvas c) {
                c.scale(scale, scale);
                if (eraseColor != 0) {
                    c.drawColor(eraseColor);
                }
                if (view != null) {
                    view.draw(c);
                }
            }
        });
    }

    public static Bitmap createHardwareBitmap(int width, int height, Consumer<Canvas> consumer) {
        RenderNode node = RenderNode.create("RecentsTransition", null);
        node.setLeftTopRightBottom(0, 0, width, height);
        node.setClipToBounds(false);
        DisplayListCanvas c = node.start(width, height);
        consumer.accept(c);
        node.end(c);
        return ThreadedRenderer.createHardwareBitmap(node, width, height);
    }
}
