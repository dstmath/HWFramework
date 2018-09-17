package tmsdk.bg.module.aresengine;

import tmsdk.common.module.aresengine.SmsEntity;

public abstract class PhoneDeviceController {
    public abstract void blockSms(Object... objArr);

    public abstract void cancelMissCall();

    public abstract void disableRingVibration(int i);

    public abstract void hangup(int i);

    public abstract boolean hangup();

    public abstract void unBlockSms(SmsEntity smsEntity, Object... objArr);
}
