package com.huawei.ims;

import android.os.PersistableBundle;
import com.android.ims.ImsConfig;
import com.android.ims.ImsException;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class ImsConfigEx {
    private ImsConfig mImsConfig;

    public static class ConfigConstantsEx {
        public static final int VOICE_OVER_WIFI_MODE = 27;
        public static final int VOICE_OVER_WIFI_ROAMING = 26;
    }

    public static class FeatureValueConstantsEx {
        public static final int OFF = 0;
        public static final int ON = 1;
    }

    public static class OperationStatusConstantsEx {
        public static final int FAILED = 1;
        public static final int SUCCESS = 0;
        public static final int UNKNOWN = -1;
    }

    public static class WfcModeFeatureValueConstantsEx {
        public static final int CELLULAR_PREFERRED = 1;
        public static final int WIFI_ONLY = 0;
        public static final int WIFI_PREFERRED = 2;
    }

    public void setImsConfig(ImsConfig imsConfig) {
        this.mImsConfig = imsConfig;
    }

    public int setProvisionedValue(int item, int value) throws ImsExceptionEx {
        try {
            if (this.mImsConfig != null) {
                return this.mImsConfig.setProvisionedValue(item, value);
            }
            return -1;
        } catch (ImsException e) {
            throw getImsExceptionEx(e);
        }
    }

    public int setImsConfig(String configKey, PersistableBundle configValue) throws ImsExceptionEx {
        try {
            if (this.mImsConfig != null) {
                return this.mImsConfig.setImsConfig(configKey, configValue);
            }
            return -1;
        } catch (ImsException e) {
            throw getImsExceptionEx(e);
        }
    }

    public PersistableBundle getImsConfig(String configKey) throws ImsExceptionEx {
        try {
            if (this.mImsConfig != null) {
                return this.mImsConfig.getImsConfig(configKey);
            }
            return null;
        } catch (ImsException e) {
            throw getImsExceptionEx(e);
        }
    }

    private ImsExceptionEx getImsExceptionEx(ImsException e) {
        if (e.getCause() == null) {
            return new ImsExceptionEx(e.getMessage(), e.getCode());
        }
        return new ImsExceptionEx(e.getMessage(), e.getCause(), e.getCode());
    }
}
