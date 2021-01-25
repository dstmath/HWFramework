package com.android.server.rms.iaware.feature;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import android.rms.iaware.IAwareCMSManager;
import android.rms.iaware.IAwaredConnection;
import com.android.server.rms.iaware.IRDataRegister;
import java.nio.ByteBuffer;

public class IoFeature extends RFeature {
    private static final int DISABLE_IOFEATUE = 0;
    private static final int ENABLE_IOFEATUE = 1;
    private static final String IO_FEATURE_EMPTY_STRING = "";
    private static final String IO_FEATURE_NAME = "IOFeature";
    private static final String IO_FEATURE_QOS = "IOQos";
    private static final String IO_FEATURE_SWITCH = "Switch";
    private static final String IO_FEATURE_TYPE = "type";
    private static final int MSB_IO_BASE_VALUE = 200;
    private static final int MSG_IO_QOS_SWITCH = 250;
    private static final int MSG_IO_SWITCH = 201;
    private static final int NUMBER_OF_INT = 2;
    private static final int QOS_FEATURE_MIN_VERSION = 5;
    private static final int SIZE_OF_INT = 4;
    private static final String TAG = "IoFeature";
    private boolean hasQosFeature = false;

    public IoFeature(Context context, AwareConstant.FeatureType type, IRDataRegister dataRegister) {
        super(context, type, dataRegister);
        loadFeatureConfig(false);
        loadFeatureConfig(true);
        if (!this.hasQosFeature) {
            ioFeatureSendSwitchCmd(0);
        }
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean reportData(CollectData data) {
        return false;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean enable() {
        setIoSwitch(true);
        AwareLog.d(TAG, "IoFeature enabled");
        return true;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean enableFeatureEx(int realVersion) {
        setIoSwitch(true);
        if (realVersion < 5) {
            AwareLog.i(TAG, "the min version of QOS_FEATURE_MIN_VERSION is 5, but current version is " + realVersion + ", don't allow enable!");
        } else {
            enableQosFeature();
        }
        return true;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean disable() {
        setIoSwitch(false);
        disableQosFeature();
        AwareLog.d(TAG, "IoFeature disable");
        return true;
    }

    private void sendPacket(ByteBuffer buffer) {
        if (buffer == null) {
            AwareLog.e(TAG, "sendPacket ByteBuffer null!");
        } else {
            IAwaredConnection.getInstance().sendPacket(buffer.array(), 0, buffer.position());
        }
    }

    private void setIoSwitch(boolean isEnable) {
        AwareLog.d(TAG, "setIoSwitch switch = " + isEnable);
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putInt(MSG_IO_SWITCH);
        buffer.putInt(isEnable ? 1 : 0);
        sendPacket(buffer);
    }

    private void enableQosFeature() {
        if (this.hasQosFeature) {
            ioFeatureSendSwitchCmd(1);
        }
    }

    private void disableQosFeature() {
        if (this.hasQosFeature) {
            ioFeatureSendSwitchCmd(0);
        }
    }

    private void ioFeatureSendSwitchCmd(int on) {
        AwareLog.d(TAG, "ioFeatureSendSwitchCmd, switch : " + on);
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putInt(MSG_IO_QOS_SWITCH);
        buffer.putInt(on);
        IAwaredConnection.getInstance().sendPacket(buffer.array());
    }

    private AwareConfig getConfig(String featureName, String configName, boolean isCustConfig) {
        if (featureName == null || "".equals(featureName) || configName == null || "".equals(configName)) {
            AwareLog.w(TAG, "featureName or configName is null");
            return null;
        }
        try {
            IBinder awareService = IAwareCMSManager.getICMSManager();
            if (awareService == null) {
                AwareLog.w(TAG, "can not find service awareService.");
                return null;
            } else if (isCustConfig) {
                return IAwareCMSManager.getCustConfig(awareService, featureName, configName);
            } else {
                return IAwareCMSManager.getConfig(awareService, featureName, configName);
            }
        } catch (RemoteException e) {
            AwareLog.e(TAG, "getConfig RemoteException!");
            return null;
        }
    }

    private void loadFeatureConfig(boolean isCustConfig) {
        AwareLog.i(TAG, "loadFeatureConfig isCustConfig = " + isCustConfig);
        AwareConfig configList = getConfig(IO_FEATURE_NAME, IO_FEATURE_QOS, isCustConfig);
        if (configList == null) {
            AwareLog.w(TAG, "loadFeatureConfig failure, configList is null!");
            return;
        }
        for (AwareConfig.Item item : configList.getConfigList()) {
            if (item == null || item.getProperties() == null) {
                AwareLog.w(TAG, "loadFeatureConfig skip a item because it is null!");
            } else {
                String typeName = item.getProperties().get(IO_FEATURE_TYPE);
                if (typeName != null) {
                    char c = 65535;
                    if (typeName.hashCode() == -1805606060 && typeName.equals(IO_FEATURE_SWITCH)) {
                        c = 0;
                    }
                    if (c == 0) {
                        applyFeatureSwitchConfig(item);
                    }
                }
            }
        }
    }

    private void applyFeatureSwitchConfig(AwareConfig.Item item) {
        for (AwareConfig.SubItem subItem : item.getSubItemList()) {
            if (subItem != null) {
                String itemName = subItem.getName();
                String itemValue = subItem.getValue();
                if (!(itemName == null || itemValue == null)) {
                    char c = 65535;
                    try {
                        if (itemName.hashCode() == -1805606060 && itemName.equals(IO_FEATURE_SWITCH)) {
                            c = 0;
                        }
                        if (c != 0) {
                            AwareLog.w(TAG, "applyFeatureSwitchConfig no such configuration. " + itemName);
                        } else {
                            boolean z = true;
                            if (Integer.parseInt(itemValue.trim()) != 1) {
                                z = false;
                            }
                            this.hasQosFeature = z;
                            AwareLog.i(TAG, "applyFeatureSwitchConfig IOQos = " + itemValue);
                        }
                    } catch (NumberFormatException e) {
                        AwareLog.e(TAG, "parse applyFeatureSwitchConfig error!");
                    }
                }
            }
        }
    }
}
