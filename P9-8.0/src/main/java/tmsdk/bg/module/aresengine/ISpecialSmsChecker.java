package tmsdk.bg.module.aresengine;

import tmsdk.common.module.aresengine.SmsEntity;

public interface ISpecialSmsChecker {
    boolean isBlocked(SmsEntity smsEntity);

    boolean isMatch(SmsEntity smsEntity);
}
