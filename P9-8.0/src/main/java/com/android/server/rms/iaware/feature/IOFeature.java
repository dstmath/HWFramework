package com.android.server.rms.iaware.feature;

import android.content.Context;
import android.rms.iaware.AwareConstant.FeatureType;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import android.rms.iaware.IAwaredConnection;
import com.android.server.rms.iaware.IRDataRegister;
import java.nio.ByteBuffer;

public class IOFeature extends RFeature {
    private static final int DISABLE_IOFEATUE = 0;
    private static final int ENABLE_IOFEATUE = 1;
    private static final int MSB_IO_BASE_VALUE = 200;
    private static final int MSG_IO_SWITCH = 201;
    private static final String TAG = "IOFeature";

    public IOFeature(Context context, FeatureType type, IRDataRegister dataRegister) {
        super(context, type, dataRegister);
    }

    public boolean reportData(CollectData data) {
        return false;
    }

    public boolean enable() {
        setIOSwitch(true);
        AwareLog.d(TAG, "IOFeature enabled");
        return true;
    }

    public boolean disable() {
        setIOSwitch(false);
        AwareLog.d(TAG, "IOFeature  disable");
        return true;
    }

    private void sendPacket(ByteBuffer buffer) {
        if (buffer == null) {
            AwareLog.e(TAG, "sendPacket ByteBuffer null!");
        } else {
            IAwaredConnection.getInstance().sendPacket(buffer.array(), 0, buffer.position());
        }
    }

    private void setIOSwitch(boolean isEnable) {
        AwareLog.d(TAG, "setIOSwitch switch = " + isEnable);
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putInt(MSG_IO_SWITCH);
        buffer.putInt(isEnable ? 1 : 0);
        sendPacket(buffer);
    }
}
