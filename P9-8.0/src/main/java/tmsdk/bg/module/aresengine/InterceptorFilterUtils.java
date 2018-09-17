package tmsdk.bg.module.aresengine;

import tmsdk.bg.creator.ManagerCreatorB;
import tmsdk.common.module.aresengine.FilterConfig;

public final class InterceptorFilterUtils {
    public static final int INTERCEPTOR_MODE_ACCEPTED_ONLY_WHITELIST = 2;
    public static final int INTERCEPTOR_MODE_REJECTED_ONLY_BLACKLIST = 1;
    public static final int INTERCEPTOR_MODE_STANDARD = 0;

    private InterceptorFilterUtils() {
    }

    private static void cP() {
        FilterConfig defalutFilterConfig;
        FilterConfig defalutFilterConfig2;
        FilterConfig defalutFilterConfig3;
        AresEngineManager aresEngineManager = (AresEngineManager) ManagerCreatorB.getManager(AresEngineManager.class);
        DataInterceptor findInterceptor = aresEngineManager.findInterceptor(DataInterceptorBuilder.TYPE_INCOMING_SMS);
        if (findInterceptor != null) {
            DataFilter dataFilter = findInterceptor.dataFilter();
            FilterConfig config = dataFilter.getConfig();
            defalutFilterConfig = dataFilter.defalutFilterConfig();
            if (config != null) {
                defalutFilterConfig.set(1, config.get(1));
            }
            dataFilter.setConfig(defalutFilterConfig);
        }
        DataInterceptor findInterceptor2 = aresEngineManager.findInterceptor(DataInterceptorBuilder.TYPE_OUTGOING_SMS);
        if (findInterceptor2 != null) {
            DataFilter dataFilter2 = findInterceptor2.dataFilter();
            defalutFilterConfig = dataFilter2.getConfig();
            defalutFilterConfig2 = dataFilter2.defalutFilterConfig();
            if (defalutFilterConfig != null) {
                defalutFilterConfig2.set(1, defalutFilterConfig.get(1));
            }
            dataFilter2.setConfig(defalutFilterConfig2);
        }
        DataInterceptor findInterceptor3 = aresEngineManager.findInterceptor(DataInterceptorBuilder.TYPE_INCOMING_CALL);
        if (findInterceptor3 != null) {
            DataFilter dataFilter3 = findInterceptor3.dataFilter();
            defalutFilterConfig2 = dataFilter3.getConfig();
            defalutFilterConfig3 = dataFilter3.defalutFilterConfig();
            if (defalutFilterConfig2 != null) {
                defalutFilterConfig3.set(1, defalutFilterConfig2.get(1));
            }
            dataFilter3.setConfig(defalutFilterConfig3);
        }
        DataInterceptor findInterceptor4 = aresEngineManager.findInterceptor(DataInterceptorBuilder.TYPE_SYSTEM_CALL);
        if (findInterceptor4 != null) {
            DataFilter dataFilter4 = findInterceptor4.dataFilter();
            defalutFilterConfig3 = dataFilter4.getConfig();
            FilterConfig defalutFilterConfig4 = dataFilter4.defalutFilterConfig();
            if (defalutFilterConfig3 != null) {
                defalutFilterConfig4.set(1, defalutFilterConfig3.get(1));
            }
            dataFilter4.setConfig(defalutFilterConfig4);
        }
    }

    private static void cQ() {
        FilterConfig defalutFilterConfig;
        FilterConfig defalutFilterConfig2;
        FilterConfig defalutFilterConfig3;
        AresEngineManager aresEngineManager = (AresEngineManager) ManagerCreatorB.getManager(AresEngineManager.class);
        DataInterceptor findInterceptor = aresEngineManager.findInterceptor(DataInterceptorBuilder.TYPE_INCOMING_SMS);
        if (findInterceptor != null) {
            DataFilter dataFilter = findInterceptor.dataFilter();
            FilterConfig config = dataFilter.getConfig();
            defalutFilterConfig = dataFilter.defalutFilterConfig();
            if (config != null) {
                defalutFilterConfig.set(1, config.get(1));
            }
            defalutFilterConfig.set(2, 3);
            defalutFilterConfig.set(4, 1);
            defalutFilterConfig.set(8, 3);
            defalutFilterConfig.set(16, 3);
            defalutFilterConfig.set(32, 2);
            defalutFilterConfig.set(64, 3);
            defalutFilterConfig.set(128, 3);
            dataFilter.setConfig(defalutFilterConfig);
        }
        DataInterceptor findInterceptor2 = aresEngineManager.findInterceptor(DataInterceptorBuilder.TYPE_OUTGOING_SMS);
        if (findInterceptor2 != null) {
            DataFilter dataFilter2 = findInterceptor2.dataFilter();
            defalutFilterConfig = dataFilter2.getConfig();
            defalutFilterConfig2 = dataFilter2.defalutFilterConfig();
            if (defalutFilterConfig != null) {
                defalutFilterConfig2.set(1, defalutFilterConfig.get(1));
            }
            dataFilter2.setConfig(defalutFilterConfig2);
        }
        DataInterceptor findInterceptor3 = aresEngineManager.findInterceptor(DataInterceptorBuilder.TYPE_INCOMING_CALL);
        if (findInterceptor3 != null) {
            DataFilter dataFilter3 = findInterceptor3.dataFilter();
            defalutFilterConfig2 = dataFilter3.getConfig();
            defalutFilterConfig3 = dataFilter3.defalutFilterConfig();
            if (defalutFilterConfig2 != null) {
                defalutFilterConfig3.set(1, defalutFilterConfig2.get(1));
            }
            defalutFilterConfig3.set(2, 3);
            defalutFilterConfig3.set(4, 1);
            defalutFilterConfig3.set(8, 3);
            defalutFilterConfig3.set(16, 3);
            defalutFilterConfig3.set(32, 3);
            defalutFilterConfig3.set(64, 0);
            dataFilter3.setConfig(defalutFilterConfig3);
        }
        DataInterceptor findInterceptor4 = aresEngineManager.findInterceptor(DataInterceptorBuilder.TYPE_SYSTEM_CALL);
        if (findInterceptor4 != null) {
            DataFilter dataFilter4 = findInterceptor4.dataFilter();
            defalutFilterConfig3 = dataFilter4.getConfig();
            FilterConfig defalutFilterConfig4 = dataFilter4.defalutFilterConfig();
            if (defalutFilterConfig3 != null) {
                defalutFilterConfig4.set(1, defalutFilterConfig3.get(1));
            }
            defalutFilterConfig4.set(2, 3);
            defalutFilterConfig4.set(4, 1);
            defalutFilterConfig4.set(8, 3);
            defalutFilterConfig4.set(16, 3);
            defalutFilterConfig4.set(32, 0);
            defalutFilterConfig4.set(64, 3);
            defalutFilterConfig4.set(128, 3);
            defalutFilterConfig4.set(256, 2);
            dataFilter4.setConfig(defalutFilterConfig4);
        }
    }

    private static void cR() {
        FilterConfig defalutFilterConfig;
        FilterConfig defalutFilterConfig2;
        FilterConfig defalutFilterConfig3;
        AresEngineManager aresEngineManager = (AresEngineManager) ManagerCreatorB.getManager(AresEngineManager.class);
        DataInterceptor findInterceptor = aresEngineManager.findInterceptor(DataInterceptorBuilder.TYPE_INCOMING_SMS);
        if (findInterceptor != null) {
            DataFilter dataFilter = findInterceptor.dataFilter();
            FilterConfig config = dataFilter.getConfig();
            defalutFilterConfig = dataFilter.defalutFilterConfig();
            if (config != null) {
                defalutFilterConfig.set(1, config.get(1));
            }
            defalutFilterConfig.set(2, 0);
            defalutFilterConfig.set(4, 3);
            defalutFilterConfig.set(8, 3);
            defalutFilterConfig.set(16, 3);
            defalutFilterConfig.set(32, 3);
            defalutFilterConfig.set(64, 3);
            defalutFilterConfig.set(128, 1);
            dataFilter.setConfig(defalutFilterConfig);
        }
        DataInterceptor findInterceptor2 = aresEngineManager.findInterceptor(DataInterceptorBuilder.TYPE_OUTGOING_SMS);
        if (findInterceptor2 != null) {
            DataFilter dataFilter2 = findInterceptor2.dataFilter();
            defalutFilterConfig = dataFilter2.getConfig();
            defalutFilterConfig2 = dataFilter2.defalutFilterConfig();
            if (defalutFilterConfig != null) {
                defalutFilterConfig2.set(1, defalutFilterConfig.get(1));
            }
            dataFilter2.setConfig(defalutFilterConfig2);
        }
        DataInterceptor findInterceptor3 = aresEngineManager.findInterceptor(DataInterceptorBuilder.TYPE_INCOMING_CALL);
        if (findInterceptor3 != null) {
            DataFilter dataFilter3 = findInterceptor3.dataFilter();
            defalutFilterConfig2 = dataFilter3.getConfig();
            defalutFilterConfig3 = dataFilter3.defalutFilterConfig();
            if (defalutFilterConfig2 != null) {
                defalutFilterConfig3.set(1, defalutFilterConfig2.get(1));
            }
            defalutFilterConfig3.set(64, 0);
            defalutFilterConfig3.set(2, 0);
            defalutFilterConfig3.set(4, 3);
            defalutFilterConfig3.set(8, 3);
            defalutFilterConfig3.set(16, 3);
            defalutFilterConfig3.set(32, 1);
            dataFilter3.setConfig(defalutFilterConfig3);
        }
        DataInterceptor findInterceptor4 = aresEngineManager.findInterceptor(DataInterceptorBuilder.TYPE_SYSTEM_CALL);
        if (findInterceptor4 != null) {
            DataFilter dataFilter4 = findInterceptor4.dataFilter();
            defalutFilterConfig3 = dataFilter4.getConfig();
            FilterConfig defalutFilterConfig4 = dataFilter4.defalutFilterConfig();
            if (defalutFilterConfig3 != null) {
                defalutFilterConfig4.set(1, defalutFilterConfig3.get(1));
            }
            defalutFilterConfig4.set(2, 0);
            defalutFilterConfig4.set(4, 3);
            defalutFilterConfig4.set(8, 3);
            defalutFilterConfig4.set(16, 3);
            defalutFilterConfig4.set(32, 1);
            defalutFilterConfig4.set(64, 3);
            defalutFilterConfig4.set(128, 3);
            defalutFilterConfig4.set(256, 2);
            dataFilter4.setConfig(defalutFilterConfig4);
        }
    }

    public static void setInterceptorMode(int i) {
        switch (i) {
            case 0:
                cP();
                return;
            case 1:
                cQ();
                return;
            case 2:
                cR();
                return;
            default:
                return;
        }
    }
}
