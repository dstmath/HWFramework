package tmsdk.bg.module.aresengine;

import tmsdk.common.module.aresengine.SmsEntity;
import tmsdk.common.module.intelli_sms.IntelliSmsCheckResult;

public abstract class IntelliSmsChecker {
    public abstract IntelliSmsCheckResult check(SmsEntity smsEntity);

    public abstract IntelliSmsCheckResult check(SmsEntity smsEntity, Boolean bool);

    public abstract boolean isChargingSms(SmsEntity smsEntity);
}
