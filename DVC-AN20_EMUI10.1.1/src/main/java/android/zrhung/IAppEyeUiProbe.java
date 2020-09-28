package android.zrhung;

import android.os.Handler;
import android.os.Message;

public interface IAppEyeUiProbe {
    void beginDispatching(Message message, Handler handler, Runnable runnable);

    void endDispatching();
}
