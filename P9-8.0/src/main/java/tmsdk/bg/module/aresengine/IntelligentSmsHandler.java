package tmsdk.bg.module.aresengine;

import tmsdk.common.module.aresengine.SmsEntity;
import tmsdk.common.module.intelli_sms.IntelliSmsCheckResult;

public interface IntelligentSmsHandler {
    boolean handleCheckResult(SmsEntity smsEntity, IntelliSmsCheckResult intelliSmsCheckResult, boolean z);
}
