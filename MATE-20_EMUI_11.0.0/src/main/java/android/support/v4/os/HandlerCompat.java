package android.support.v4.os;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public final class HandlerCompat {
    public static boolean postDelayed(@NonNull Handler handler, @NonNull Runnable r, @Nullable Object token, long delayMillis) {
        if (BuildCompat.isAtLeastP()) {
            return handler.postDelayed(r, token, delayMillis);
        }
        Message message = Message.obtain(handler, r);
        message.obj = token;
        return handler.sendMessageDelayed(message, delayMillis);
    }

    private HandlerCompat() {
    }
}
