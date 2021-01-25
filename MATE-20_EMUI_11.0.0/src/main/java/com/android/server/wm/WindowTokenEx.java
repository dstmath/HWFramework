package com.android.server.wm;

public class WindowTokenEx {
    private WindowToken mWindowToken;

    public void setWindowToken(WindowToken windowToken) {
        this.mWindowToken = windowToken;
    }

    public void removeImmediately() {
        this.mWindowToken.removeImmediately();
    }
}
