package tmsdk.bg.module.aresengine;

import tmsdk.common.module.aresengine.SmsEntity;

/* compiled from: Unknown */
public interface ISpecialSmsChecker {
    boolean isBlocked(SmsEntity smsEntity);

    boolean isMatch(SmsEntity smsEntity);
}
