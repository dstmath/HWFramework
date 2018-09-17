package android.view;

import android.util.AndroidRuntimeException;

/* compiled from: WindowManagerGlobal */
final class WindowLeaked extends AndroidRuntimeException {
    public WindowLeaked(String msg) {
        super(msg);
    }
}
