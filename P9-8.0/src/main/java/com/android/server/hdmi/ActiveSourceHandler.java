package com.android.server.hdmi;

import android.hardware.hdmi.IHdmiControlCallback;
import android.os.RemoteException;
import android.util.Slog;

final class ActiveSourceHandler {
    private static final String TAG = "ActiveSourceHandler";
    private final IHdmiControlCallback mCallback;
    private final HdmiControlService mService = this.mSource.getService();
    private final HdmiCecLocalDeviceTv mSource;

    static ActiveSourceHandler create(HdmiCecLocalDeviceTv source, IHdmiControlCallback callback) {
        if (source != null) {
            return new ActiveSourceHandler(source, callback);
        }
        Slog.e(TAG, "Wrong arguments");
        return null;
    }

    private ActiveSourceHandler(HdmiCecLocalDeviceTv source, IHdmiControlCallback callback) {
        this.mSource = source;
        this.mCallback = callback;
    }

    void process(ActiveSource newActive, int deviceType) {
        HdmiCecLocalDeviceTv tv = this.mSource;
        if (this.mService.getDeviceInfo(newActive.logicalAddress) == null) {
            tv.startNewDeviceAction(newActive, deviceType);
        }
        if (tv.isProhibitMode()) {
            ActiveSource current = tv.getActiveSource();
            if (current.logicalAddress == getSourceAddress()) {
                this.mService.sendCecCommand(HdmiCecMessageBuilder.buildActiveSource(current.logicalAddress, current.physicalAddress));
                tv.updateActiveSource(current);
                invokeCallback(0);
                return;
            }
            tv.startRoutingControl(newActive.physicalAddress, current.physicalAddress, true, this.mCallback);
            return;
        }
        ActiveSource old = ActiveSource.of(tv.getActiveSource());
        tv.updateActiveSource(newActive);
        boolean notifyInputChange = this.mCallback == null;
        if (!old.equals(newActive)) {
            tv.setPrevPortId(tv.getActivePortId());
        }
        tv.updateActiveInput(newActive.physicalAddress, notifyInputChange);
        invokeCallback(0);
    }

    private final int getSourceAddress() {
        return this.mSource.getDeviceInfo().getLogicalAddress();
    }

    private void invokeCallback(int result) {
        if (this.mCallback != null) {
            try {
                this.mCallback.onComplete(result);
            } catch (RemoteException e) {
                Slog.e(TAG, "Callback failed:" + e);
            }
        }
    }
}
