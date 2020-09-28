package com.android.ims;

import android.content.Context;
import com.android.ims.HwImsFactory;

public class HwImsFactoryImpl implements HwImsFactory.HwImsFactoryInterface {
    public IHwImsUtEx getHwImsUtEx(IHwImsUtManager imsUtManager, int phoneId) {
        return new HwImsUtExImpl(imsUtManager, phoneId);
    }

    public IHwImsCallEx getHwImsCallEx(ImsCall imsCall, Context context) {
        return new HwImsCallEx(imsCall, context);
    }
}
