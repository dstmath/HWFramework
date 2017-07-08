package com.android.server.pfw.autostartup.comm;

public class XmlConst {

    public static class ControlScope {
        public static final String CONTROL_SCOPE_ELEMENT_KEY = "ControlScope";
        public static final String PACKAGE_ELEMENT_KEY = "package";
        public static final String PACKAGE_NAME_ATTR_KEY = "name";
        public static final String SYSTEM_BLACK_LIST_ELEMENT_KEY = "SystemBlackList";
        public static final String THIRD_PARTY_WHITE_LIST_ELEMENT_KEY = "ThirdPartyWhiteList";
    }

    public static class PreciseIgnore {
        private static final String COMM_CALLER = "caller";
        public static final String COMP_COMM_NAME_ATTR = "name";
        public static final String COMP_COMM_RELATED_PACKAGE_ATTR_KEY = "packageName";
        public static final String COMP_COMM_SCOPE_ATTR = "scope";
        public static final String COMP_COMM_SCREEN_ATTR = "screenStatus";
        public static final String COMP_SCOPE_ALL_VALUE = "all";
        public static final String COMP_SCOPE_INDIVIDUAL_VALUE = "individual";
        public static final String COMP_SCREEN_ALL_VALUE = "all";
        public static final String COMP_SCREEN_ON_VALUE_ = "on";
        public static final String PRECISE_IGNORE_ELEMENT_KEY = "PreciseIgnore";
        public static final String PROVIDERS_ELEMENT_KEY = "Providers";
        public static final String PROVIDER_AUTH_CALLER_ELEMENT_KEY = "caller";
        public static final String PROVIDER_AUTH_ELEMENT_KEY = "auth";
        public static final String RECEIVERS_ELEMENT_KEY = "Receivers";
        public static final String RECEIVER_ACTION_ELEMENT_KEY = "action";
        public static final String RECEIVER_ACTION_RECEIVER_ELEMENT_KEY = "receiver";
        public static final String SERVICES_ELEMENT_KEY = "Services";
        public static final String SERVICE_CLAZZ_CALLER_ELEMENT_KEY = "caller";
        public static final String SERVICE_CLAZZ_ELEMENT_KEY = "clazz";
    }
}
