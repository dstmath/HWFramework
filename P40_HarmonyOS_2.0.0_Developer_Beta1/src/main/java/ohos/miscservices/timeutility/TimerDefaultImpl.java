package ohos.miscservices.timeutility;

import android.app.AlarmManager;
import android.app.PendingIntent;
import java.util.HashMap;
import java.util.Map;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.timeutility.ITimerSysAbility;
import ohos.miscservices.timeutility.Timer;

public class TimerDefaultImpl implements ITimerSysAbility.ITimer {
    private static final int LISTENER_INIT_CAPACITY = 6;
    private static final String LOG_TAG = "TimerA";
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, LOG_TAG);
    private final Map<Timer.ITimerListener, AlarmManager.OnAlarmListener> mListeners = new HashMap(6);
    private AlarmManager mService;
    private Context mZidaneContext;

    TimerDefaultImpl(Context context) {
        this.mZidaneContext = context;
        HiLog.info(TAG, "Trying get timer service.", new Object[0]);
        Context context2 = this.mZidaneContext;
        if (context2 == null) {
            HiLog.error(TAG, "TimerDefaultImpl mZidaneContext is null.", new Object[0]);
            return;
        }
        Object hostContext = context2.getHostContext();
        android.content.Context context3 = hostContext instanceof android.content.Context ? (android.content.Context) hostContext : null;
        if (context3 == null) {
            HiLog.error(TAG, "get null context.", new Object[0]);
            return;
        }
        Object systemService = context3.getSystemService("alarm");
        if (systemService instanceof AlarmManager) {
            this.mService = (AlarmManager) systemService;
        }
        if (this.mService == null) {
            HiLog.error(TAG, "get droid timer service failed.", new Object[0]);
        }
    }

    @Override // ohos.miscservices.timeutility.ITimerSysAbility.ITimer
    public void delete(Context context, Timer.TimerIntent timerIntent) {
        if (context == null || timerIntent == null) {
            HiLog.warn(TAG, "delete operation failed. get invalid parameters.", new Object[0]);
            return;
        }
        PendingIntent pendingIntent = TranslateUtils.getPendingIntent(context, timerIntent.getIntent(), timerIntent.getAbilityType());
        if (pendingIntent != null) {
            this.mService.cancel(pendingIntent);
        } else {
            HiLog.warn(TAG, "delete operation failed. get null PendingIntent.", new Object[0]);
        }
    }

    @Override // ohos.miscservices.timeutility.ITimerSysAbility.ITimer
    public void delete(Context context, Timer.ITimerListener iTimerListener) {
        synchronized (this.mListeners) {
            if (iTimerListener != null) {
                if (this.mListeners.get(iTimerListener) != null) {
                    this.mService.cancel(this.mListeners.remove(iTimerListener));
                    return;
                }
            }
            HiLog.warn(TAG, "delete listener failed. get invalid parameters.", new Object[0]);
        }
    }

    @Override // ohos.miscservices.timeutility.ITimerSysAbility.ITimer
    public void start(int i, long j, Timer.TimerIntent timerIntent) {
        if (timerIntent != null && timerIntent.isValid()) {
            int timerType = TranslateUtils.getTimerType(i);
            boolean isExactType = TranslateUtils.isExactType(i);
            boolean isIdleType = TranslateUtils.isIdleType(i);
            PendingIntent pendingIntent = TranslateUtils.getPendingIntent(this.mZidaneContext, timerIntent.getIntent(), timerIntent.getAbilityType());
            if (isExactType) {
                if (isIdleType) {
                    HiLog.info(TAG, "start exact timer while idle.", new Object[0]);
                    this.mService.setExactAndAllowWhileIdle(timerType, j, pendingIntent);
                    return;
                }
                HiLog.info(TAG, "start exact timer.", new Object[0]);
                this.mService.setExact(timerType, j, pendingIntent);
            } else if (isIdleType) {
                HiLog.info(TAG, "start timer while idle.", new Object[0]);
                this.mService.setAndAllowWhileIdle(timerType, j, pendingIntent);
            } else {
                HiLog.info(TAG, "start timer.", new Object[0]);
                this.mService.set(timerType, j, pendingIntent);
            }
        }
    }

    @Override // ohos.miscservices.timeutility.ITimerSysAbility.ITimer
    public void start(int i, long j, Timer.ITimerListener iTimerListener) {
        int timerType = TranslateUtils.getTimerType(i);
        boolean isExactType = TranslateUtils.isExactType(i);
        AlarmManager.OnAlarmListener andCacheListener = getAndCacheListener(iTimerListener);
        if (isExactType) {
            this.mService.setExact(timerType, j, null, andCacheListener, null);
        } else {
            this.mService.set(timerType, j, null, andCacheListener, null);
        }
    }

    @Override // ohos.miscservices.timeutility.ITimerSysAbility.ITimer
    public void startRepeat(int i, long j, long j2, Timer.TimerIntent timerIntent) {
        if (timerIntent != null && timerIntent.isValid()) {
            int timerType = TranslateUtils.getTimerType(i);
            boolean isExactType = TranslateUtils.isExactType(i);
            PendingIntent pendingIntent = TranslateUtils.getPendingIntent(this.mZidaneContext, timerIntent.getIntent(), timerIntent.getAbilityType());
            if (isExactType) {
                this.mService.setRepeating(timerType, j, j2, pendingIntent);
            } else {
                this.mService.setInexactRepeating(timerType, j, j2, pendingIntent);
            }
        }
    }

    private AlarmManager.OnAlarmListener getAndCacheListener(Timer.ITimerListener iTimerListener) {
        AlarmManager.OnAlarmListener onAlarmListener;
        synchronized (this.mListeners) {
            onAlarmListener = this.mListeners.get(iTimerListener);
            if (onAlarmListener == null && (onAlarmListener = TranslateUtils.getOnAlarmListener(iTimerListener)) != null) {
                this.mListeners.put(iTimerListener, onAlarmListener);
            }
        }
        return onAlarmListener;
    }
}
