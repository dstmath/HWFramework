package com.android.systemui.shared.recents.view;

import android.app.ActivityOptions;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.os.Bundle;
import android.os.Handler;
import android.os.IRemoteCallback;
import android.os.RemoteException;
import android.view.View;
import java.util.function.Consumer;

public class RecentsTransition {
    public static ActivityOptions createAspectScaleAnimation(Context context, Handler handler, boolean scaleUp, AppTransitionAnimationSpecsFuture animationSpecsFuture, final Runnable animationStartCallback) {
        return ActivityOptions.makeMultiThumbFutureAspectScaleAnimation(context, handler, animationSpecsFuture != null ? animationSpecsFuture.getFuture() : null, new ActivityOptions.OnAnimationStartedListener() {
            /* class com.android.systemui.shared.recents.view.RecentsTransition.AnonymousClass1 */
            private boolean mHandled;

            public void onAnimationStarted() {
                if (!this.mHandled) {
                    this.mHandled = true;
                    Runnable runnable = animationStartCallback;
                    if (runnable != null) {
                        runnable.run();
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
            /* class com.android.systemui.shared.recents.view.RecentsTransition.AnonymousClass2 */

            public void sendResult(Bundle data) throws RemoteException {
                handler.post(animationStartCallback);
            }
        };
    }

    public static Bitmap drawViewIntoHardwareBitmap(int width, int height, final View view, final float scale, final int eraseColor) {
        return createHardwareBitmap(width, height, new Consumer<Canvas>() {
            /* class com.android.systemui.shared.recents.view.RecentsTransition.AnonymousClass3 */

            public void accept(Canvas c) {
                float f = scale;
                c.scale(f, f);
                int i = eraseColor;
                if (i != 0) {
                    c.drawColor(i);
                }
                View view = view;
                if (view != null) {
                    view.draw(c);
                }
            }
        });
    }

    public static Bitmap createHardwareBitmap(int width, int height, Consumer<Canvas> consumer) {
        Picture picture = new Picture();
        consumer.accept(picture.beginRecording(width, height));
        picture.endRecording();
        return Bitmap.createBitmap(picture);
    }
}
