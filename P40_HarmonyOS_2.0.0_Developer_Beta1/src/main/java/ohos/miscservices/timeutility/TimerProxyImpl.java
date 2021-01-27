package ohos.miscservices.timeutility;

import ohos.app.Context;
import ohos.miscservices.timeutility.ITimerSysAbility;
import ohos.miscservices.timeutility.Timer;
import ohos.rpc.IRemoteObject;

public class TimerProxyImpl implements ITimerSysAbility, ITimerSysAbility.ITimer {
    private static final Object INSTANCE_LOCK = new Object();
    private static volatile TimerProxyImpl sInstance;
    private IRemoteObject mIRemoteObject;
    private ITimerSysAbility.ITimer mTimerInnerAbility;

    private TimerProxyImpl(Context context, IRemoteObject iRemoteObject) {
        this.mTimerInnerAbility = new TimerDefaultImpl(context.getApplicationContext());
        this.mIRemoteObject = iRemoteObject;
    }

    public static TimerProxyImpl getInstance(Context context, IRemoteObject iRemoteObject) {
        if (sInstance == null) {
            synchronized (INSTANCE_LOCK) {
                if (sInstance == null) {
                    sInstance = new TimerProxyImpl(context, iRemoteObject);
                }
            }
        }
        return sInstance;
    }

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this.mIRemoteObject;
    }

    @Override // ohos.miscservices.timeutility.ITimerSysAbility.ITimer
    public void delete(Context context, Timer.TimerIntent timerIntent) {
        this.mTimerInnerAbility.delete(context, timerIntent);
    }

    @Override // ohos.miscservices.timeutility.ITimerSysAbility.ITimer
    public void delete(Context context, Timer.ITimerListener iTimerListener) {
        this.mTimerInnerAbility.delete(context, iTimerListener);
    }

    @Override // ohos.miscservices.timeutility.ITimerSysAbility.ITimer
    public void start(int i, long j, Timer.TimerIntent timerIntent) {
        this.mTimerInnerAbility.start(i, j, timerIntent);
    }

    @Override // ohos.miscservices.timeutility.ITimerSysAbility.ITimer
    public void start(int i, long j, Timer.ITimerListener iTimerListener) {
        this.mTimerInnerAbility.start(i, j, iTimerListener);
    }

    @Override // ohos.miscservices.timeutility.ITimerSysAbility.ITimer
    public void startRepeat(int i, long j, long j2, Timer.TimerIntent timerIntent) {
        this.mTimerInnerAbility.startRepeat(i, j, j2, timerIntent);
    }
}
