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

public class DisplaySmoothFeature extends RFeature {
    private static final int DISABLE_FEATUE = 0;
    private static final int ENABLE_FEATUE = 1;
    private static final String FEATURE_DSS_BOOST = "DssBoost";
    private static final int FEATURE_MIN_VERSION = 6;
    private static final String FEATURE_NAME = "DisplaySmooth";
    private static final String FEATURE_SWITCH = "Switch";
    private static final String FEATURE_TYPE = "type";
    private static final int MSG_DSS_BOOST_SWITCH = 251;
    private static final int NUMBER_OF_INT = 2;
    private static final int SIZE_OF_INT = 4;
    private static final String TAG = "DisplaySmooth";
    private boolean hasDssBoostFeature = false;

    public DisplaySmoothFeature(Context context, AwareConstant.FeatureType type, IRDataRegister dataRegister) {
        super(context, type, dataRegister);
        loadFeatureConfig();
        if (!this.hasDssBoostFeature) {
            sendSwitchCmd(0);
        }
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean reportData(CollectData data) {
        return false;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean enable() {
        return true;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean enableFeatureEx(int realVersion) {
        if (realVersion < 6) {
            AwareLog.i("DisplaySmooth", "the min version of FEATURE_MIN_VERSION is 6, but current version is " + realVersion + ", don't allow enable!");
            return true;
        }
        enableDssBoostFeature();
        return true;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean disable() {
        disableDssBoostFeature();
        AwareLog.d("DisplaySmooth", "Feature disable");
        return true;
    }

    private void enableDssBoostFeature() {
        if (this.hasDssBoostFeature) {
            sendSwitchCmd(1);
        }
    }

    private void disableDssBoostFeature() {
        if (!this.hasDssBoostFeature) {
            sendSwitchCmd(0);
        }
    }

    private void sendSwitchCmd(int on) {
        AwareLog.d("DisplaySmooth", "sendSwitchCmd, switch : " + on);
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putInt(MSG_DSS_BOOST_SWITCH);
        buffer.putInt(on);
        IAwaredConnection.getInstance().sendPacket(buffer.array());
    }

    private AwareConfig getConfig(String featureName, String configName) {
        if (featureName == null || featureName.isEmpty() || configName == null || configName.isEmpty()) {
            AwareLog.w("DisplaySmooth", "featureName or configName is null");
            return null;
        }
        try {
            IBinder awareService = IAwareCMSManager.getICMSManager();
            if (awareService != null) {
                return IAwareCMSManager.getConfig(awareService, featureName, configName);
            }
            AwareLog.w("DisplaySmooth", "can not find service awareService.");
            return null;
        } catch (RemoteException e) {
            AwareLog.e("DisplaySmooth", "getConfig RemoteException!");
            return null;
        }
    }

    private void loadFeatureConfig() {
        AwareConfig configList = getConfig("DisplaySmooth", FEATURE_DSS_BOOST);
        if (configList == null) {
            AwareLog.w("DisplaySmooth", "loadFeatureConfig failure, configList is null!");
            return;
        }
        for (AwareConfig.Item item : configList.getConfigList()) {
            if (item == null || item.getProperties() == null) {
                AwareLog.w("DisplaySmooth", "loadFeatureConfig skip a item because it is null!");
            } else {
                String typeName = item.getProperties().get(FEATURE_TYPE);
                if (typeName != null) {
                    char c = 65535;
                    if (typeName.hashCode() == -1805606060 && typeName.equals(FEATURE_SWITCH)) {
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
                        if (itemName.hashCode() == -1805606060 && itemName.equals(FEATURE_SWITCH)) {
                            c = 0;
                        }
                        if (c != 0) {
                            AwareLog.w("DisplaySmooth", "applyFeatureSwitchConfig no such configuration. " + itemName);
                        } else {
                            boolean z = true;
                            if (Integer.parseInt(itemValue.trim()) != 1) {
                                z = false;
                            }
                            this.hasDssBoostFeature = z;
                            AwareLog.i("DisplaySmooth", "applyFeatureSwitchConfig DssBoost = " + itemValue);
                        }
                    } catch (NumberFormatException e) {
                        AwareLog.e("DisplaySmooth", "parse applyFeatureSwitchConfig error!");
                    }
                }
            }
        }
    }
}
