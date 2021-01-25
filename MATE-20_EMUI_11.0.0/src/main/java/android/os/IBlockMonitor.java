package android.os;

import android.view.InputEvent;

public interface IBlockMonitor {
    void checkBinderTime(long j);

    void checkInputReceiveTime(int i, long j);

    void checkInputTime(long j);

    void initialize();

    void notifyInputEvent(InputEvent inputEvent);
}
