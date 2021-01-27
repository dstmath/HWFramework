package com.android.server.devicepolicy;

import android.content.ComponentName;
import android.os.Parcel;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

public interface IHwDevicePolicyManagerService {
    long getUsrSetExtendTime();

    void init();

    boolean isDeprecatedPolicyEnabled(int i, int i2);

    boolean isMdmApiDeviceOwner();

    void loadHwSpecialPolicyFromXml(XmlPullParser xmlPullParser);

    void notifyActiveAdmin(ComponentName componentName, int i);

    void notifyPlugins(ComponentName componentName, int i);

    boolean processTransaction(int i, Parcel parcel, Parcel parcel2, int i2);

    void removeActiveAdminCompleted(ComponentName componentName);

    void setHwSpecialPolicyToXml(XmlSerializer xmlSerializer);

    void syncHwDeviceSettingsLocked(int i);

    void systemReady(int i);
}
