package ohos.media.camera.mode.impl;

import ohos.eventhandler.EventHandler;
import ohos.media.camera.mode.ActionDataCallback;
import ohos.media.camera.mode.Mode;
import ohos.media.image.Image;
import ohos.media.image.common.Size;

public class ActionDataCallbackImpl extends ActionDataCallback {
    private final ActionDataCallback callback;
    private final EventHandler handler;
    private final Mode mode;

    ActionDataCallbackImpl(Mode mode2, ActionDataCallback actionDataCallback, EventHandler eventHandler) {
        this.mode = mode2;
        this.callback = actionDataCallback;
        this.handler = eventHandler;
    }

    public static ActionDataCallbackImpl obtain(Mode mode2, ActionDataCallback actionDataCallback, EventHandler eventHandler) {
        return new ActionDataCallbackImpl(mode2, actionDataCallback, eventHandler);
    }

    public void onThumbnailAvailable(int i, Size size, byte[] bArr) {
        EventHandler eventHandler;
        if (this.callback != null && (eventHandler = this.handler) != null) {
            eventHandler.postTask(new Runnable(i, size, bArr) {
                /* class ohos.media.camera.mode.impl.$$Lambda$ActionDataCallbackImpl$tAlqZLYpOTwJTJnrSj1A1dAdM */
                private final /* synthetic */ int f$1;
                private final /* synthetic */ Size f$2;
                private final /* synthetic */ byte[] f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    ActionDataCallbackImpl.this.lambda$onThumbnailAvailable$0$ActionDataCallbackImpl(this.f$1, this.f$2, this.f$3);
                }
            });
        }
    }

    public /* synthetic */ void lambda$onThumbnailAvailable$0$ActionDataCallbackImpl(int i, Size size, byte[] bArr) {
        this.callback.onThumbnailAvailable(this.mode, i, size, bArr);
    }

    public void onImageAvailable(int i, Image image) {
        EventHandler eventHandler;
        if (this.callback != null && (eventHandler = this.handler) != null) {
            eventHandler.postTask(new Runnable(i, image) {
                /* class ohos.media.camera.mode.impl.$$Lambda$ActionDataCallbackImpl$bJC5xiklmc5TLURwdv6jPurlnvk */
                private final /* synthetic */ int f$1;
                private final /* synthetic */ Image f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    ActionDataCallbackImpl.this.lambda$onImageAvailable$1$ActionDataCallbackImpl(this.f$1, this.f$2);
                }
            });
        }
    }

    public /* synthetic */ void lambda$onImageAvailable$1$ActionDataCallbackImpl(int i, Image image) {
        this.callback.onImageAvailable(this.mode, i, image);
    }

    public void onRawImageAvailable(int i, Image image) {
        EventHandler eventHandler;
        if (this.callback != null && (eventHandler = this.handler) != null) {
            eventHandler.postTask(new Runnable(i, image) {
                /* class ohos.media.camera.mode.impl.$$Lambda$ActionDataCallbackImpl$XHsMjEyXU0wffS9kkMejNc8oDmg */
                private final /* synthetic */ int f$1;
                private final /* synthetic */ Image f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    ActionDataCallbackImpl.this.lambda$onRawImageAvailable$2$ActionDataCallbackImpl(this.f$1, this.f$2);
                }
            });
        }
    }

    public /* synthetic */ void lambda$onRawImageAvailable$2$ActionDataCallbackImpl(int i, Image image) {
        this.callback.onRawImageAvailable(this.mode, i, image);
    }
}
