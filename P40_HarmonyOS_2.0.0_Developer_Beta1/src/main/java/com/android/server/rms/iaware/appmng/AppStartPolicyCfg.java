package com.android.server.rms.iaware.appmng;

public final class AppStartPolicyCfg {
    public static final int APPSTART_DEFAULT = -1;

    public enum AppStartCallerAction {
        PUSHSDK(0),
        BTMEDIABROWSER(1),
        NOTIFYLISTENER(2),
        HWPUSH(3);
        
        private int mPriority;

        private AppStartCallerAction(int priority) {
            this.mPriority = priority;
        }

        public int getPriority() {
            return this.mPriority;
        }
    }

    public enum AppStartCallerStatus {
        BACKGROUND(0),
        FOREGROUND(1);
        
        private int mPriority;

        private AppStartCallerStatus(int priority) {
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
        EMAIL(7),
        RCV_MONEY(8),
        HABIT_IM(9),
        MOSTFREQIM(10),
        CLOCKTYPE(11);
        
        private int mPriority;

        private AppStartTargetType(int priority) {
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
        INPUTMETHOD(9),
        WIDGETUPDATE(10);
        
        private int mPriority;

        private AppStartTargetStat(int priority) {
            this.mPriority = priority;
        }

        public int getPriority() {
            return this.mPriority;
        }
    }

    public enum AppStartOversea {
        CHINA(0),
        OVERSEA(1);
        
        private int mPriority;

        private AppStartOversea(int priority) {
            this.mPriority = priority;
        }

        public int getPriority() {
            return this.mPriority;
        }
    }

    public enum AppStartAppOversea {
        CHINA(0),
        OVERSEA(1),
        UNKNONW(2);
        
        private int mPriority;

        private AppStartAppOversea(int priority) {
            this.mPriority = priority;
        }

        public int getPriority() {
            return this.mPriority;
        }
    }

    public enum AppStartAppSrcRange {
        PRERECG(0),
        NONPRERECG(1);
        
        private int mPriority;

        private AppStartAppSrcRange(int priority) {
            this.mPriority = priority;
        }

        public int getPriority() {
            return this.mPriority;
        }
    }

    public enum AppStartScreenStatus {
        SCREENOFF(0),
        SCREENON(1);
        
        private int mPriority;

        private AppStartScreenStatus(int priority) {
            this.mPriority = priority;
        }

        public int getPriority() {
            return this.mPriority;
        }
    }
}
