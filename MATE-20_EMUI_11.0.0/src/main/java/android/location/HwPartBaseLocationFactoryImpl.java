package android.location;

import android.common.HwPartBaseLocationFactory;

public class HwPartBaseLocationFactoryImpl extends HwPartBaseLocationFactory {
    public IHwInnerLocationManager createHwInnerLocationManager() {
        return HwInnerLocationManagerImpl.getDefault();
    }
}
