package com.mediatek.ims.internal;

import android.content.Context;
import android.os.RemoteException;
import android.telephony.Rlog;
import com.android.ims.ImsConfigListener;
import com.android.ims.ImsException;

public class MtkImsConfig {
    public static final int MTK_CONFIG_START = 1000;
    private static final String TAG = "MtkImsConfig";
    private boolean DBG = true;
    private Context mContext;
    private final IMtkImsConfig miConfig;

    public static class ConfigConstants {
        public static final int CONFIG_START = 1000;
        public static final int EPDG_ADDRESS = 1000;
        public static final int PROVISIONED_CONFIG_END = 1002;
        public static final int PROVISIONED_CONFIG_START = 1000;
        public static final int PUBLISH_ERROR_RETRY_TIMER = 1001;
        public static final int VOICE_OVER_WIFI_MDN = 1002;
    }

    public static class WfcModeFeatureValueConstants {
        public static final int CELLULAR_ONLY = 3;
        public static final int CELLULAR_PREFERRED = 1;
        public static final int WIFI_ONLY = 0;
        public static final int WIFI_PREFERRED = 2;
    }

    public MtkImsConfig(IMtkImsConfig iconfig, Context context) {
        this.miConfig = iconfig;
        this.mContext = context;
    }

    public int getProvisionedValue(int item) throws ImsException {
        try {
            int ret = this.miConfig.getProvisionedValue(item);
            if (this.DBG) {
                Rlog.d(TAG, "getProvisionedValue(): item = " + item + ", ret =" + ret);
            }
            return ret;
        } catch (RemoteException e) {
            throw new ImsException("getValue()", e, 131);
        }
    }

    public String getProvisionedStringValue(int item) throws ImsException {
        try {
            String ret = this.miConfig.getProvisionedStringValue(item);
            if (this.DBG) {
                Rlog.d(TAG, "getProvisionedStringValue(): item = " + item + ", ret =" + ret);
            }
            return ret;
        } catch (RemoteException e) {
            throw new ImsException("getProvisionedStringValue()", e, 131);
        }
    }

    public int setProvisionedValue(int item, int value) throws ImsException {
        try {
            return this.miConfig.setProvisionedValue(item, value);
        } catch (RemoteException e) {
            throw new ImsException("setProvisionedValue()", e, 131);
        }
    }

    public int setProvisionedStringValue(int item, String value) throws ImsException {
        try {
            return this.miConfig.setProvisionedStringValue(item, value);
        } catch (RemoteException e) {
            throw new ImsException("setProvisionedStringValue()", e, 131);
        }
    }

    public void getFeatureValue(int feature, int network, ImsConfigListener listener) throws ImsException {
        if (this.DBG) {
            Rlog.d(TAG, "getFeatureValue: feature = " + feature + ", network =" + network + ", listener =" + listener);
        }
        try {
            this.miConfig.getFeatureValue(feature, network, listener);
        } catch (RemoteException e) {
            throw new ImsException("getFeatureValue()", e, 131);
        }
    }

    public void setFeatureValue(int feature, int network, int value, ImsConfigListener listener) throws ImsException {
        try {
            this.miConfig.setFeatureValue(feature, network, value, listener);
        } catch (RemoteException e) {
            throw new ImsException("setFeatureValue()", e, 131);
        }
    }

    public void setMultiFeatureValues(int[] feature, int[] network, int[] value, ImsConfigListener listener) throws ImsException {
        try {
            Rlog.d(TAG, "setMultiFeatureValues()");
            this.miConfig.setMultiFeatureValues(feature, network, value, listener);
        } catch (RemoteException e) {
            throw new ImsException("setMultiFeatureValues()", e, 131);
        }
    }

    public void setImsResCapability(int feature, int value) throws ImsException {
        try {
            this.miConfig.setImsResCapability(feature, value);
        } catch (RemoteException e) {
            throw new ImsException("setImsResCapability()", e, 131);
        }
    }

    public int getImsResCapability(int feature) throws ImsException {
        try {
            return this.miConfig.getImsResCapability(feature);
        } catch (RemoteException e) {
            throw new ImsException("getImsResCapability()", e, 131);
        }
    }

    public void setWfcMode(int mode) throws ImsException {
        try {
            this.miConfig.setWfcMode(mode);
        } catch (RemoteException e) {
            throw new ImsException("setWfcMode()", e, 131);
        }
    }

    public void setVoltePreference(int mode) throws ImsException {
        try {
            this.miConfig.setVoltePreference(mode);
        } catch (RemoteException e) {
            throw new ImsException("setVoltePreference()", e, 131);
        }
    }

    public int[] setModemImsCfg(String[] keys, String[] values, int phoneId) throws ImsException {
        try {
            return this.miConfig.setModemImsCfg(keys, values, phoneId);
        } catch (RemoteException e) {
            throw new ImsException("setModemImsCfg()", e, 131);
        }
    }

    public int[] setModemImsWoCfg(String[] keys, String[] values, int phoneId) throws ImsException {
        try {
            return this.miConfig.setModemImsWoCfg(keys, values, phoneId);
        } catch (RemoteException e) {
            throw new ImsException("setModemImsWoCfg()", e, 131);
        }
    }

    public int[] setModemImsIwlanCfg(String[] keys, String[] values, int phoneId) throws ImsException {
        try {
            return this.miConfig.setModemImsIwlanCfg(keys, values, phoneId);
        } catch (RemoteException e) {
            throw new ImsException("setImsModemIwlanCfg()", e, 131);
        }
    }
}
