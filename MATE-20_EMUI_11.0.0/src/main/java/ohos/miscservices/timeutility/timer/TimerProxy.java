package ohos.miscservices.timeutility.timer;

import ohos.app.Context;
import ohos.miscservices.timeutility.ITimerSysAbility;
import ohos.miscservices.timeutility.Timer;
import ohos.miscservices.timeutility.TimerProxyImpl;
import ohos.sysability.samgr.SysAbilityManager;

public class TimerProxy implements ITimerSysAbility.ITimer {
    private static final int TIMER_ABILITY_ID = -1;
    private TimerProxyImpl mProxy;

    public TimerProxy(Context context) {
        this.mProxy = TimerProxyImpl.getInstance(context, SysAbilityManager.getSysAbility(-1));
    }

    @Override // ohos.miscservices.timeutility.ITimerSysAbility.ITimer
    public void delete(Context context, Timer.TimerIntent timerIntent) {
        this.mProxy.delete(context, timerIntent);
    }

    @Override // ohos.miscservices.timeutility.ITimerSysAbility.ITimer
    public void delete(Context context, Timer.ITimerListener iTimerListener) {
        this.mProxy.delete(context, iTimerListener);
    }

    @Override // ohos.miscservices.timeutility.ITimerSysAbility.ITimer
    public void start(int i, long j, Timer.TimerIntent timerIntent) {
        this.mProxy.start(i, j, timerIntent);
    }

    @Override // ohos.miscservices.timeutility.ITimerSysAbility.ITimer
    public void start(int i, long j, Timer.ITimerListener iTimerListener) {
        this.mProxy.start(i, j, iTimerListener);
    }

    @Override // ohos.miscservices.timeutility.ITimerSysAbility.ITimer
    public void startRepeat(int i, long j, long j2, Timer.TimerIntent timerIntent) {
        this.mProxy.startRepeat(i, j, j2, timerIntent);
    }
}
