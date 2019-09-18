package com.huawei.opcollect.strategy;

import com.huawei.opcollect.strategy.OdmfActionManager;
import java.util.Calendar;

interface ITimerTrigger {
    boolean checkTrigger(Calendar calendar, long j, long j2, OdmfActionManager.NextTimer nextTimer);

    String toString(String str);
}
