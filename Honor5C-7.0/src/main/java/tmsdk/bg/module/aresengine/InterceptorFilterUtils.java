package tmsdk.bg.module.aresengine;

import tmsdk.bg.creator.ManagerCreatorB;
import tmsdk.bg.module.wifidetect.WifiDetectManager;
import tmsdk.common.module.aresengine.FilterConfig;
import tmsdk.common.module.aresengine.SystemCallLogFilterConsts;
import tmsdk.fg.module.spacemanager.SpaceManager;

/* compiled from: Unknown */
public final class InterceptorFilterUtils {
    public static final int INTERCEPTOR_MODE_ACCEPTED_ONLY_WHITELIST = 2;
    public static final int INTERCEPTOR_MODE_REJECTED_ONLY_BLACKLIST = 1;
    public static final int INTERCEPTOR_MODE_STANDARD = 0;

    private InterceptorFilterUtils() {
    }

    private static void dH() {
        FilterConfig config;
        FilterConfig defalutFilterConfig;
        AresEngineManager aresEngineManager = (AresEngineManager) ManagerCreatorB.getManager(AresEngineManager.class);
        DataInterceptor findInterceptor = aresEngineManager.findInterceptor(DataInterceptorBuilder.TYPE_INCOMING_SMS);
        if (findInterceptor != null) {
            DataFilter dataFilter = findInterceptor.dataFilter();
            config = dataFilter.getConfig();
            defalutFilterConfig = dataFilter.defalutFilterConfig();
            if (config != null) {
                defalutFilterConfig.set(INTERCEPTOR_MODE_REJECTED_ONLY_BLACKLIST, config.get(INTERCEPTOR_MODE_REJECTED_ONLY_BLACKLIST));
            }
            dataFilter.setConfig(defalutFilterConfig);
        }
        findInterceptor = aresEngineManager.findInterceptor(DataInterceptorBuilder.TYPE_OUTGOING_SMS);
        if (findInterceptor != null) {
            dataFilter = findInterceptor.dataFilter();
            config = dataFilter.getConfig();
            defalutFilterConfig = dataFilter.defalutFilterConfig();
            if (config != null) {
                defalutFilterConfig.set(INTERCEPTOR_MODE_REJECTED_ONLY_BLACKLIST, config.get(INTERCEPTOR_MODE_REJECTED_ONLY_BLACKLIST));
            }
            dataFilter.setConfig(defalutFilterConfig);
        }
        findInterceptor = aresEngineManager.findInterceptor(DataInterceptorBuilder.TYPE_INCOMING_CALL);
        if (findInterceptor != null) {
            dataFilter = findInterceptor.dataFilter();
            config = dataFilter.getConfig();
            defalutFilterConfig = dataFilter.defalutFilterConfig();
            if (config != null) {
                defalutFilterConfig.set(INTERCEPTOR_MODE_REJECTED_ONLY_BLACKLIST, config.get(INTERCEPTOR_MODE_REJECTED_ONLY_BLACKLIST));
            }
            dataFilter.setConfig(defalutFilterConfig);
        }
        DataInterceptor findInterceptor2 = aresEngineManager.findInterceptor(DataInterceptorBuilder.TYPE_SYSTEM_CALL);
        if (findInterceptor2 != null) {
            DataFilter dataFilter2 = findInterceptor2.dataFilter();
            FilterConfig config2 = dataFilter2.getConfig();
            config = dataFilter2.defalutFilterConfig();
            if (config2 != null) {
                config.set(INTERCEPTOR_MODE_REJECTED_ONLY_BLACKLIST, config2.get(INTERCEPTOR_MODE_REJECTED_ONLY_BLACKLIST));
            }
            dataFilter2.setConfig(config);
        }
    }

    private static void dI() {
        FilterConfig config;
        FilterConfig defalutFilterConfig;
        AresEngineManager aresEngineManager = (AresEngineManager) ManagerCreatorB.getManager(AresEngineManager.class);
        DataInterceptor findInterceptor = aresEngineManager.findInterceptor(DataInterceptorBuilder.TYPE_INCOMING_SMS);
        if (findInterceptor != null) {
            DataFilter dataFilter = findInterceptor.dataFilter();
            config = dataFilter.getConfig();
            defalutFilterConfig = dataFilter.defalutFilterConfig();
            if (config != null) {
                defalutFilterConfig.set(INTERCEPTOR_MODE_REJECTED_ONLY_BLACKLIST, config.get(INTERCEPTOR_MODE_REJECTED_ONLY_BLACKLIST));
            }
            defalutFilterConfig.set(INTERCEPTOR_MODE_ACCEPTED_ONLY_WHITELIST, 3);
            defalutFilterConfig.set(4, INTERCEPTOR_MODE_REJECTED_ONLY_BLACKLIST);
            defalutFilterConfig.set(8, 3);
            defalutFilterConfig.set(16, 3);
            defalutFilterConfig.set(32, INTERCEPTOR_MODE_ACCEPTED_ONLY_WHITELIST);
            defalutFilterConfig.set(64, 3);
            defalutFilterConfig.set(SystemCallLogFilterConsts.NOTIFY_SHORT_CALL, 3);
            dataFilter.setConfig(defalutFilterConfig);
        }
        findInterceptor = aresEngineManager.findInterceptor(DataInterceptorBuilder.TYPE_OUTGOING_SMS);
        if (findInterceptor != null) {
            dataFilter = findInterceptor.dataFilter();
            config = dataFilter.getConfig();
            defalutFilterConfig = dataFilter.defalutFilterConfig();
            if (config != null) {
                defalutFilterConfig.set(INTERCEPTOR_MODE_REJECTED_ONLY_BLACKLIST, config.get(INTERCEPTOR_MODE_REJECTED_ONLY_BLACKLIST));
            }
            dataFilter.setConfig(defalutFilterConfig);
        }
        findInterceptor = aresEngineManager.findInterceptor(DataInterceptorBuilder.TYPE_INCOMING_CALL);
        if (findInterceptor != null) {
            dataFilter = findInterceptor.dataFilter();
            config = dataFilter.getConfig();
            defalutFilterConfig = dataFilter.defalutFilterConfig();
            if (config != null) {
                defalutFilterConfig.set(INTERCEPTOR_MODE_REJECTED_ONLY_BLACKLIST, config.get(INTERCEPTOR_MODE_REJECTED_ONLY_BLACKLIST));
            }
            defalutFilterConfig.set(INTERCEPTOR_MODE_ACCEPTED_ONLY_WHITELIST, 3);
            defalutFilterConfig.set(4, INTERCEPTOR_MODE_REJECTED_ONLY_BLACKLIST);
            defalutFilterConfig.set(8, 3);
            defalutFilterConfig.set(16, 3);
            defalutFilterConfig.set(32, 3);
            defalutFilterConfig.set(64, 0);
            dataFilter.setConfig(defalutFilterConfig);
        }
        DataInterceptor findInterceptor2 = aresEngineManager.findInterceptor(DataInterceptorBuilder.TYPE_SYSTEM_CALL);
        if (findInterceptor2 != null) {
            DataFilter dataFilter2 = findInterceptor2.dataFilter();
            FilterConfig config2 = dataFilter2.getConfig();
            config = dataFilter2.defalutFilterConfig();
            if (config2 != null) {
                config.set(INTERCEPTOR_MODE_REJECTED_ONLY_BLACKLIST, config2.get(INTERCEPTOR_MODE_REJECTED_ONLY_BLACKLIST));
            }
            config.set(INTERCEPTOR_MODE_ACCEPTED_ONLY_WHITELIST, 3);
            config.set(4, INTERCEPTOR_MODE_REJECTED_ONLY_BLACKLIST);
            config.set(8, 3);
            config.set(16, 3);
            config.set(32, 0);
            config.set(64, 3);
            config.set(SystemCallLogFilterConsts.NOTIFY_SHORT_CALL, 3);
            config.set(WifiDetectManager.SECURITY_NONE, INTERCEPTOR_MODE_ACCEPTED_ONLY_WHITELIST);
            dataFilter2.setConfig(config);
        }
    }

    private static void dJ() {
        FilterConfig config;
        FilterConfig defalutFilterConfig;
        AresEngineManager aresEngineManager = (AresEngineManager) ManagerCreatorB.getManager(AresEngineManager.class);
        DataInterceptor findInterceptor = aresEngineManager.findInterceptor(DataInterceptorBuilder.TYPE_INCOMING_SMS);
        if (findInterceptor != null) {
            DataFilter dataFilter = findInterceptor.dataFilter();
            config = dataFilter.getConfig();
            defalutFilterConfig = dataFilter.defalutFilterConfig();
            if (config != null) {
                defalutFilterConfig.set(INTERCEPTOR_MODE_REJECTED_ONLY_BLACKLIST, config.get(INTERCEPTOR_MODE_REJECTED_ONLY_BLACKLIST));
            }
            defalutFilterConfig.set(INTERCEPTOR_MODE_ACCEPTED_ONLY_WHITELIST, 0);
            defalutFilterConfig.set(4, 3);
            defalutFilterConfig.set(8, 3);
            defalutFilterConfig.set(16, 3);
            defalutFilterConfig.set(32, 3);
            defalutFilterConfig.set(64, 3);
            defalutFilterConfig.set(SystemCallLogFilterConsts.NOTIFY_SHORT_CALL, INTERCEPTOR_MODE_REJECTED_ONLY_BLACKLIST);
            dataFilter.setConfig(defalutFilterConfig);
        }
        findInterceptor = aresEngineManager.findInterceptor(DataInterceptorBuilder.TYPE_OUTGOING_SMS);
        if (findInterceptor != null) {
            dataFilter = findInterceptor.dataFilter();
            config = dataFilter.getConfig();
            defalutFilterConfig = dataFilter.defalutFilterConfig();
            if (config != null) {
                defalutFilterConfig.set(INTERCEPTOR_MODE_REJECTED_ONLY_BLACKLIST, config.get(INTERCEPTOR_MODE_REJECTED_ONLY_BLACKLIST));
            }
            dataFilter.setConfig(defalutFilterConfig);
        }
        findInterceptor = aresEngineManager.findInterceptor(DataInterceptorBuilder.TYPE_INCOMING_CALL);
        if (findInterceptor != null) {
            dataFilter = findInterceptor.dataFilter();
            config = dataFilter.getConfig();
            defalutFilterConfig = dataFilter.defalutFilterConfig();
            if (config != null) {
                defalutFilterConfig.set(INTERCEPTOR_MODE_REJECTED_ONLY_BLACKLIST, config.get(INTERCEPTOR_MODE_REJECTED_ONLY_BLACKLIST));
            }
            defalutFilterConfig.set(64, 0);
            defalutFilterConfig.set(INTERCEPTOR_MODE_ACCEPTED_ONLY_WHITELIST, 0);
            defalutFilterConfig.set(4, 3);
            defalutFilterConfig.set(8, 3);
            defalutFilterConfig.set(16, 3);
            defalutFilterConfig.set(32, INTERCEPTOR_MODE_REJECTED_ONLY_BLACKLIST);
            dataFilter.setConfig(defalutFilterConfig);
        }
        DataInterceptor findInterceptor2 = aresEngineManager.findInterceptor(DataInterceptorBuilder.TYPE_SYSTEM_CALL);
        if (findInterceptor2 != null) {
            DataFilter dataFilter2 = findInterceptor2.dataFilter();
            FilterConfig config2 = dataFilter2.getConfig();
            config = dataFilter2.defalutFilterConfig();
            if (config2 != null) {
                config.set(INTERCEPTOR_MODE_REJECTED_ONLY_BLACKLIST, config2.get(INTERCEPTOR_MODE_REJECTED_ONLY_BLACKLIST));
            }
            config.set(INTERCEPTOR_MODE_ACCEPTED_ONLY_WHITELIST, 0);
            config.set(4, 3);
            config.set(8, 3);
            config.set(16, 3);
            config.set(32, INTERCEPTOR_MODE_REJECTED_ONLY_BLACKLIST);
            config.set(64, 3);
            config.set(SystemCallLogFilterConsts.NOTIFY_SHORT_CALL, 3);
            config.set(WifiDetectManager.SECURITY_NONE, INTERCEPTOR_MODE_ACCEPTED_ONLY_WHITELIST);
            dataFilter2.setConfig(config);
        }
    }

    public static void setInterceptorMode(int i) {
        switch (i) {
            case SpaceManager.ERROR_CODE_OK /*0*/:
                dH();
            case INTERCEPTOR_MODE_REJECTED_ONLY_BLACKLIST /*1*/:
                dI();
            case INTERCEPTOR_MODE_ACCEPTED_ONLY_WHITELIST /*2*/:
                dJ();
            default:
        }
    }
}
