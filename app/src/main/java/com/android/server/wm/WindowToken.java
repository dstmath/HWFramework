package com.android.server.wm;

import android.os.IBinder;
import java.io.PrintWriter;

class WindowToken {
    AppWindowToken appWindowToken;
    final boolean explicit;
    boolean hasVisible;
    boolean hidden;
    boolean paused;
    boolean sendingToBottom;
    final WindowManagerService service;
    String stringName;
    final IBinder token;
    boolean waitingToShow;
    final int windowType;
    final WindowList windows;

    WindowToken(WindowManagerService _service, IBinder _token, int type, boolean _explicit) {
        this.windows = new WindowList();
        this.paused = false;
        this.service = _service;
        this.token = _token;
        this.windowType = type;
        this.explicit = _explicit;
    }

    void removeAllWindows() {
        for (int winNdx = this.windows.size() - 1; winNdx >= 0; winNdx--) {
            WindowState win = (WindowState) this.windows.get(winNdx);
            win.mService.removeWindowLocked(win);
        }
        this.windows.clear();
    }

    void dump(PrintWriter pw, String prefix) {
        pw.print(prefix);
        pw.print("windows=");
        pw.println(this.windows);
        pw.print(prefix);
        pw.print("windowType=");
        pw.print(this.windowType);
        pw.print(" hidden=");
        pw.print(this.hidden);
        pw.print(" hasVisible=");
        pw.println(this.hasVisible);
        if (this.waitingToShow || this.sendingToBottom) {
            pw.print(prefix);
            pw.print("waitingToShow=");
            pw.print(this.waitingToShow);
            pw.print(" sendingToBottom=");
            pw.print(this.sendingToBottom);
        }
    }

    public String toString() {
        if (this.stringName == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("WindowToken{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(" ");
            sb.append(this.token);
            sb.append('}');
            this.stringName = sb.toString();
        }
        return this.stringName;
    }
}
