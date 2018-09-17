package com.android.server.rms.iaware.appmng;

public final class AppStartPolicyCfg {
    public static final int APPSTART_DEFAULT = -1;

    public enum AppStartCallerAction {
        PUSHSDK(0),
        BTMEDIABROWSER(1),
        NOTIFYLISTENER(2);
        
        int mPriority;

        private AppStartCallerAction(int priority) {
            this.mPriority = priority;
        }

        public int getPriority() {
            return this.mPriority;
        }
    }

    public enum AppStartCallerStatus {
        BACKGROUND(0);
        
        int mPriority;

        private AppStartCallerStatus(int priority) {
            this.mPriority = priority;
        }

        public int getPriority() {
            return this.mPriority;
        }
    }

    public enum AppStartTargetStat {
        ALIVE(0),
        WIDGET(1),
        MUSIC(2),
        RECORD(3),
        GUIDE(4),
        UPDOWNLOAD(5),
        HEALTH(6),
        FGACTIVITY(7),
        WALLPAPER(8),
        INPUTMETHOD(9);
        
        int mPriority;

        private AppStartTargetStat(int priority) {
            this.mPriority = priority;
        }

        public int getPriority() {
            return this.mPriority;
        }
    }

    public enum AppStartTargetType {
        IM(0),
        BLIND(1),
        TTS(2),
        CLOCK(3),
        PAY(4),
        SHARE(5),
        BUSINESS(6),
        EMAIL(7);
        
        int mPriority;

        private AppStartTargetType(int priority) {
            this.mPriority = priority;
        }

        public int getPriority() {
            return this.mPriority;
        }
    }

    private AppStartPolicyCfg() {
    }
}
