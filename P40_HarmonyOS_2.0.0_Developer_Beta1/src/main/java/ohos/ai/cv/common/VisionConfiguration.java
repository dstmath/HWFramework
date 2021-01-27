package ohos.ai.cv.common;

import ohos.utils.PacMap;

public class VisionConfiguration {
    public static final int APP_QUICK = 0;
    public static final int APP_TRADITIONAL = 1;
    public static final int MODE_IN = 0;
    public static final int MODE_OUT = 1;
    public static final int STATE_BACKGROUND = 1;
    public static final int STATE_FOREGROUND = 0;
    protected int appType = 1;
    protected String clientPkgName = null;
    protected int clientState = 1;
    protected String clientVersion = null;
    protected int processMode = 1;

    protected VisionConfiguration(Builder<?> builder) {
        this.appType = ((Builder) builder).appType;
        this.clientPkgName = ((Builder) builder).clientPkgName;
        this.clientVersion = ((Builder) builder).clientVersion;
        this.clientState = ((Builder) builder).clientState;
        this.processMode = ((Builder) builder).processMode;
    }

    public int getProcessMode() {
        return this.processMode;
    }

    public PacMap getParam() {
        PacMap pacMap = new PacMap();
        pacMap.putIntValue(ParamKey.APP_TYPE, this.appType);
        pacMap.putString(ParamKey.CLIENT_PKG_NAME, this.clientPkgName);
        pacMap.putString(ParamKey.CLIENT_VERSION, this.clientVersion);
        pacMap.putIntValue(ParamKey.CLIENT_STATE, this.clientState);
        pacMap.putIntValue(ParamKey.PROCESS_MODE, this.processMode);
        return pacMap;
    }

    public static abstract class Builder<T extends Builder<T>> {
        private int appType = 1;
        private String clientPkgName = null;
        private int clientState = 1;
        private String clientVersion = null;
        private int processMode = 1;

        /* access modifiers changed from: protected */
        public abstract T self();

        public T setAppType(int i) {
            this.appType = i == 0 ? 0 : 1;
            return self();
        }

        public T setClientPkgName(String str) {
            this.clientPkgName = str;
            return self();
        }

        public T setClientVersion(String str) {
            this.clientVersion = str;
            return self();
        }

        public T setClientState(int i) {
            this.clientState = i == 0 ? 0 : 1;
            return self();
        }

        public T setProcessMode(int i) {
            this.processMode = i == 0 ? 0 : 1;
            return self();
        }
    }
}
