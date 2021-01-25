package ohos.miscservices.timeutility;

import ohos.app.Context;
import ohos.miscservices.timeutility.Timer;
import ohos.rpc.IRemoteBroker;

public interface ITimerSysAbility extends IRemoteBroker {

    public interface ITimer {
        default void delete(Context context, Timer.ITimerListener iTimerListener) {
        }

        default void delete(Context context, Timer.TimerIntent timerIntent) {
        }

        default void start(int i, long j, Timer.ITimerListener iTimerListener) {
        }

        default void start(int i, long j, Timer.TimerIntent timerIntent) {
        }

        default void startRepeat(int i, long j, long j2, Timer.TimerIntent timerIntent) {
        }
    }
}
