package ohos.miscservices.timeutility;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import ohos.aafwk.content.Intent;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.timeutility.timer.TimerProxy;

public class Timer {
    public static final int ABILITY_TYPE_COMMON_EVENT = 2;
    public static final int ABILITY_TYPE_PAGE = 1;
    public static final int ABILITY_TYPE_SERVICE = 3;
    private static final String LOG_TAG = "Timer";
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, LOG_TAG);
    public static final int TIMER_TYPE_EXACT = 4;
    public static final int TIMER_TYPE_IDLE = 8;
    public static final int TIMER_TYPE_REALTIME = 1;
    public static final int TIMER_TYPE_WAKEUP = 2;

    @Retention(RetentionPolicy.SOURCE)
    private @interface AbilityType {
    }

    public interface ITimerListener {
        void onTrigger();
    }

    @Target({ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.SOURCE)
    private @interface IntgerDef {
        String[] prefix() default {};

        int[] value() default {};
    }

    @Retention(RetentionPolicy.SOURCE)
    private @interface TimerType {
    }

    public static class OneShotTimer extends BaseTimer {
        private OneShotTimer(Context context) {
            super(context);
        }

        public static OneShotTimer getTimer(Context context, TimerIntent timerIntent) {
            if (context == null || timerIntent == null || !timerIntent.isValid()) {
                return null;
            }
            OneShotTimer oneShotTimer = new OneShotTimer(context);
            oneShotTimer.setOperation(timerIntent);
            return oneShotTimer;
        }

        public static OneShotTimer getTimer(Context context, ITimerListener iTimerListener) {
            if (context == null || iTimerListener == null) {
                return null;
            }
            OneShotTimer oneShotTimer = new OneShotTimer(context);
            oneShotTimer.setListener(iTimerListener);
            return oneShotTimer;
        }

        public void start(int i, long j) {
            setType(i);
            setTimeAtTrigger(j);
            startOneShot();
        }

        @Override // ohos.miscservices.timeutility.Timer.BaseTimer
        public void stop() {
            super.stop();
        }
    }

    public static class RepeatTimer extends BaseTimer {
        private RepeatTimer(Context context) {
            super(context);
        }

        public static RepeatTimer getTimer(Context context, TimerIntent timerIntent) {
            if (context == null || timerIntent == null || !timerIntent.isValid()) {
                return null;
            }
            RepeatTimer repeatTimer = new RepeatTimer(context);
            repeatTimer.setOperation(timerIntent);
            return repeatTimer;
        }

        public void start(int i, long j, long j2) {
            setType(i);
            setTimeAtTrigger(j);
            setInterval(j2);
            startRepeat();
        }

        @Override // ohos.miscservices.timeutility.Timer.BaseTimer
        public void stop() {
            super.stop();
        }
    }

    private static class BaseTimer {
        private TimerProxy mService;
        private TimerInfo mTimerInfo;

        BaseTimer(Context context) {
            this.mService = new TimerProxy(context);
            this.mTimerInfo = new TimerInfo(context);
        }

        /* access modifiers changed from: package-private */
        public void stop() {
            TimerInfo timerInfo = this.mTimerInfo;
            if (timerInfo == null) {
                return;
            }
            if (timerInfo.getOperation() != null) {
                this.mService.delete(this.mTimerInfo.getContext(), this.mTimerInfo.getOperation());
            } else if (this.mTimerInfo.getListener() != null) {
                this.mService.delete(this.mTimerInfo.getContext(), this.mTimerInfo.getListener());
            } else {
                HiLog.warn(Timer.TAG, "stop should not be here.", new Object[0]);
            }
        }

        /* access modifiers changed from: package-private */
        public void startOneShot() {
            if (this.mTimerInfo.getOperation() != null) {
                this.mService.start(this.mTimerInfo.getType(), this.mTimerInfo.getTimeAtTrigger(), this.mTimerInfo.getOperation());
            } else if (this.mTimerInfo.getListener() != null) {
                this.mService.start(this.mTimerInfo.getType(), this.mTimerInfo.getTimeAtTrigger(), this.mTimerInfo.getListener());
            } else {
                HiLog.warn(Timer.TAG, "startOneShot should not be here.", new Object[0]);
            }
        }

        /* access modifiers changed from: package-private */
        public void startRepeat() {
            this.mService.startRepeat(this.mTimerInfo.getType(), this.mTimerInfo.getTimeAtTrigger(), this.mTimerInfo.getInterval(), this.mTimerInfo.getOperation());
        }

        /* access modifiers changed from: package-private */
        public final void setOperation(TimerIntent timerIntent) {
            this.mTimerInfo.setOperation(timerIntent);
        }

        /* access modifiers changed from: package-private */
        public final void setListener(ITimerListener iTimerListener) {
            this.mTimerInfo.setListener(iTimerListener);
        }

        /* access modifiers changed from: package-private */
        public final void setInterval(long j) {
            this.mTimerInfo.setInterval(j);
        }

        /* access modifiers changed from: package-private */
        public final void setTimeAtTrigger(long j) {
            this.mTimerInfo.setTimeAtTrigger(j);
        }

        /* access modifiers changed from: package-private */
        public final void setType(int i) {
            this.mTimerInfo.setType(i);
        }
    }

    /* access modifiers changed from: private */
    public static class TimerInfo {
        Context mContext;
        long mInterval;
        ITimerListener mListener;
        TimerIntent mOperation;
        long mTimeAtTrigger;
        int mType;

        TimerInfo(Context context) {
            this.mContext = context;
        }

        /* access modifiers changed from: package-private */
        public Context getContext() {
            return this.mContext;
        }

        /* access modifiers changed from: package-private */
        public int getType() {
            return this.mType;
        }

        /* access modifiers changed from: package-private */
        public void setType(int i) {
            this.mType = i;
        }

        /* access modifiers changed from: package-private */
        public long getTimeAtTrigger() {
            return this.mTimeAtTrigger;
        }

        /* access modifiers changed from: package-private */
        public void setTimeAtTrigger(long j) {
            this.mTimeAtTrigger = j;
        }

        /* access modifiers changed from: package-private */
        public long getInterval() {
            return this.mInterval;
        }

        /* access modifiers changed from: package-private */
        public void setInterval(long j) {
            this.mInterval = j;
        }

        /* access modifiers changed from: package-private */
        public ITimerListener getListener() {
            return this.mListener;
        }

        /* access modifiers changed from: package-private */
        public void setListener(ITimerListener iTimerListener) {
            this.mListener = iTimerListener;
        }

        /* access modifiers changed from: package-private */
        public TimerIntent getOperation() {
            return this.mOperation;
        }

        /* access modifiers changed from: package-private */
        public void setOperation(TimerIntent timerIntent) {
            this.mOperation = timerIntent;
        }
    }

    public static class TimerIntent {
        private int mAbilityType;
        private Intent mIntent;

        private static boolean isValidType(int i) {
            return i == 1 || i == 2 || i == 3;
        }

        public TimerIntent(Intent intent, int i) {
            this.mIntent = intent;
            this.mAbilityType = i;
        }

        public Intent getIntent() {
            return this.mIntent;
        }

        public int getAbilityType() {
            return this.mAbilityType;
        }

        public boolean isValid() {
            return this.mIntent != null && isValidType(this.mAbilityType);
        }
    }
}
