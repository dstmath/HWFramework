package com.android.server.devicepolicy;

import android.content.ComponentName;
import android.content.Context;
import android.os.Parcel;
import android.util.Slog;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

public class HwDevicePolicyFactory {
    private static final String DEVICE_POLICY_FACTORY_IMPL_NAME = "com.android.server.devicepolicy.HwDevicePolicyFactoryImpl";
    private static final Object LOCK = new Object();
    private static final String LOG_TAG = "HwDevicePolicyFactory";
    private static HwDevicePolicyFactory sFactory;
    private static volatile Object sObject = null;

    private static Object getImplObject() {
        if (sObject == null) {
            synchronized (LOCK) {
                if (sObject == null) {
                    try {
                        sObject = Class.forName(DEVICE_POLICY_FACTORY_IMPL_NAME).newInstance();
                    } catch (Exception e) {
                        Slog.e(LOG_TAG, "reflection exception to getImplObject");
                    }
                }
            }
            Slog.v(LOG_TAG, "get allimpl object = " + sObject);
        }
        return sObject;
    }

    public static BaseIDevicePolicyManager getHwPolicyManagerInnerEx(Context context) {
        if (getImplObject() != null) {
            return HwDevicePolicyManagerInnerEx.getDefault(context);
        }
        return null;
    }

    public static HwDevicePolicyFactory loadFactory() {
        HwDevicePolicyFactory hwDevicePolicyFactory = sFactory;
        if (hwDevicePolicyFactory != null) {
            return hwDevicePolicyFactory;
        }
        Object object = getImplObject();
        if (object == null || !(object instanceof HwDevicePolicyFactory)) {
            Slog.i(LOG_TAG, "Create default factory for mdm part is not exist.");
            sFactory = new HwDevicePolicyFactory();
        } else {
            Slog.i(LOG_TAG, "Create actual factory for mdm part.");
            sFactory = (HwDevicePolicyFactory) object;
        }
        return sFactory;
    }

    public IHwDevicePolicyManagerService getHuaweiDevicePolicyManagerService(Context context, IHwDevicePolicyManagerInner inner) {
        return new DefaultHwDevicePolicyManagerServiceImpl(context, inner);
    }

    public class DefaultHwDevicePolicyManagerServiceImpl implements IHwDevicePolicyManagerService {
        public DefaultHwDevicePolicyManagerServiceImpl(Context context, IHwDevicePolicyManagerInner inner) {
        }

        @Override // com.android.server.devicepolicy.IHwDevicePolicyManagerService
        public void init() {
        }

        @Override // com.android.server.devicepolicy.IHwDevicePolicyManagerService
        public void notifyPlugins(ComponentName who, int userHandle) {
        }

        @Override // com.android.server.devicepolicy.IHwDevicePolicyManagerService
        public void notifyActiveAdmin(ComponentName who, int userHandle) {
        }

        @Override // com.android.server.devicepolicy.IHwDevicePolicyManagerService
        public void removeActiveAdminCompleted(ComponentName who) {
        }

        @Override // com.android.server.devicepolicy.IHwDevicePolicyManagerService
        public void syncHwDeviceSettingsLocked(int userHandle) {
        }

        @Override // com.android.server.devicepolicy.IHwDevicePolicyManagerService
        public boolean processTransaction(int code, Parcel data, Parcel reply, int flags) {
            return false;
        }

        @Override // com.android.server.devicepolicy.IHwDevicePolicyManagerService
        public void loadHwSpecialPolicyFromXml(XmlPullParser parser) {
        }

        @Override // com.android.server.devicepolicy.IHwDevicePolicyManagerService
        public void setHwSpecialPolicyToXml(XmlSerializer out) {
        }

        @Override // com.android.server.devicepolicy.IHwDevicePolicyManagerService
        public boolean isMdmApiDeviceOwner() {
            return false;
        }

        @Override // com.android.server.devicepolicy.IHwDevicePolicyManagerService
        public void systemReady(int phase) {
        }

        @Override // com.android.server.devicepolicy.IHwDevicePolicyManagerService
        public long getUsrSetExtendTime() {
            return -1;
        }

        @Override // com.android.server.devicepolicy.IHwDevicePolicyManagerService
        public boolean isDeprecatedPolicyEnabled(int reqPolicy, int userId) {
            return false;
        }
    }
}
