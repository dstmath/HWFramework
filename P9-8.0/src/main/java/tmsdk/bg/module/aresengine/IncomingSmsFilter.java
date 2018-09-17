package tmsdk.bg.module.aresengine;

import tmsdk.common.module.aresengine.IncomingSmsFilterConsts;
import tmsdk.common.module.aresengine.SmsEntity;

public abstract class IncomingSmsFilter extends DataFilter<SmsEntity> implements IncomingSmsFilterConsts {
    public abstract void setIntelligentSmsHandler(IntelligentSmsHandler intelligentSmsHandler);

    public abstract void setSpecialSmsChecker(ISpecialSmsChecker iSpecialSmsChecker);
}
