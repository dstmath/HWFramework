package tmsdk.bg.module.aresengine;

import tmsdk.common.module.aresengine.CallLogEntity;

public interface IShortCallChecker {
    boolean isShortCall(CallLogEntity callLogEntity, long j);
}
