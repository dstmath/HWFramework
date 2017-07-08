package tmsdk.bg.module.aresengine;

import tmsdk.bg.creator.ManagerCreatorB;

/* compiled from: Unknown */
public final class Proguard {
    public void callAllMethods() {
        AresEngineManager aresEngineManager = (AresEngineManager) ManagerCreatorB.getManager(AresEngineManager.class);
        aresEngineManager.addInterceptor(null);
        aresEngineManager.findInterceptor(null);
        aresEngineManager.getAresEngineFactor();
        aresEngineManager.setAresEngineFactor(null);
        aresEngineManager.interceptors();
        aresEngineManager.getIntelligentSmsChecker();
        DataInterceptorBuilder.createInComingCallInterceptorBuilder();
        DataInterceptorBuilder.createInComingSmsInterceptorBuilder();
        DataInterceptorBuilder.createOutgoingSmsInterceptorBuilder();
        DataInterceptorBuilder.createSystemCallLogInterceptorBuilder();
    }
}
