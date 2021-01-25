package com.huawei.android.os;

import android.os.UEventObserver;

public class UEventObserverExt {
    private UEventObserverBridge mBridge = new UEventObserverBridge();

    public UEventObserverExt() {
        this.mBridge.setUEventObserverExt(this);
    }

    public void onUEvent(UEvent eventEx) {
    }

    public final void startObserving(String match) {
        this.mBridge.startObserving(match);
    }

    public final void stopObserving() {
        this.mBridge.stopObserving();
    }

    public UEventObserver getUEventObserverBridge() {
        return this.mBridge;
    }

    public static final class UEvent {
        private UEventObserver.UEvent mEvent;

        public void setUEvent(UEventObserver.UEvent event) {
            this.mEvent = event;
        }

        public String get(String key) {
            return this.mEvent.get(key);
        }

        public String get(String key, String defaultValue) {
            return this.mEvent.get(key, defaultValue);
        }
    }
}
