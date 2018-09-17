package android.hardware.hdmi;

import android.hardware.hdmi.HdmiRecordListener.TimerStatusData;
import android.hardware.hdmi.HdmiRecordSources.RecordSource;
import android.hardware.hdmi.HdmiTimerRecordSources.TimerRecordSource;
import android.hardware.hdmi.IHdmiControlCallback.Stub;
import android.os.RemoteException;
import android.util.Log;
import java.util.Collections;
import java.util.List;
import libcore.util.EmptyArray;

public final class HdmiTvClient extends HdmiClient {
    private static final String TAG = "HdmiTvClient";
    public static final int VENDOR_DATA_SIZE = 16;

    public interface HdmiMhlVendorCommandListener {
        void onReceived(int i, int i2, int i3, byte[] bArr);
    }

    public interface InputChangeListener {
        void onChanged(HdmiDeviceInfo hdmiDeviceInfo);
    }

    public interface SelectCallback {
        void onComplete(int i);
    }

    HdmiTvClient(IHdmiControlService service) {
        super(service);
    }

    static HdmiTvClient create(IHdmiControlService service) {
        return new HdmiTvClient(service);
    }

    public int getDeviceType() {
        return 0;
    }

    public void deviceSelect(int logicalAddress, SelectCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("callback must not be null.");
        }
        try {
            this.mService.deviceSelect(logicalAddress, getCallbackWrapper(callback));
        } catch (RemoteException e) {
            Log.e(TAG, "failed to select device: ", e);
        }
    }

    private static IHdmiControlCallback getCallbackWrapper(final SelectCallback callback) {
        return new Stub() {
            public void onComplete(int result) {
                callback.onComplete(result);
            }
        };
    }

    public void portSelect(int portId, SelectCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback must not be null");
        }
        try {
            this.mService.portSelect(portId, getCallbackWrapper(callback));
        } catch (RemoteException e) {
            Log.e(TAG, "failed to select port: ", e);
        }
    }

    public void setInputChangeListener(InputChangeListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener must not be null.");
        }
        try {
            this.mService.setInputChangeListener(getListenerWrapper(listener));
        } catch (RemoteException e) {
            Log.e("TAG", "Failed to set InputChangeListener:", e);
        }
    }

    private static IHdmiInputChangeListener getListenerWrapper(final InputChangeListener listener) {
        return new IHdmiInputChangeListener.Stub() {
            public void onChanged(HdmiDeviceInfo info) {
                listener.onChanged(info);
            }
        };
    }

    public List<HdmiDeviceInfo> getDeviceList() {
        try {
            return this.mService.getDeviceList();
        } catch (RemoteException e) {
            Log.e("TAG", "Failed to call getDeviceList():", e);
            return Collections.emptyList();
        }
    }

    public void setSystemAudioMode(boolean enabled, SelectCallback callback) {
        try {
            this.mService.setSystemAudioMode(enabled, getCallbackWrapper(callback));
        } catch (RemoteException e) {
            Log.e(TAG, "failed to set system audio mode:", e);
        }
    }

    public void setSystemAudioVolume(int oldIndex, int newIndex, int maxIndex) {
        try {
            this.mService.setSystemAudioVolume(oldIndex, newIndex, maxIndex);
        } catch (RemoteException e) {
            Log.e(TAG, "failed to set volume: ", e);
        }
    }

    public void setSystemAudioMute(boolean mute) {
        try {
            this.mService.setSystemAudioMute(mute);
        } catch (RemoteException e) {
            Log.e(TAG, "failed to set mute: ", e);
        }
    }

    public void setRecordListener(HdmiRecordListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener must not be null.");
        }
        try {
            this.mService.setHdmiRecordListener(getListenerWrapper(listener));
        } catch (RemoteException e) {
            Log.e(TAG, "failed to set record listener.", e);
        }
    }

    public void sendStandby(int deviceId) {
        try {
            this.mService.sendStandby(getDeviceType(), deviceId);
        } catch (RemoteException e) {
            Log.e(TAG, "sendStandby threw exception ", e);
        }
    }

    private static IHdmiRecordListener getListenerWrapper(final HdmiRecordListener callback) {
        return new IHdmiRecordListener.Stub() {
            public byte[] getOneTouchRecordSource(int recorderAddress) {
                RecordSource source = callback.onOneTouchRecordSourceRequested(recorderAddress);
                if (source == null) {
                    return EmptyArray.BYTE;
                }
                byte[] data = new byte[source.getDataSize(true)];
                source.toByteArray(true, data, 0);
                return data;
            }

            public void onOneTouchRecordResult(int recorderAddress, int result) {
                callback.onOneTouchRecordResult(recorderAddress, result);
            }

            public void onTimerRecordingResult(int recorderAddress, int result) {
                callback.onTimerRecordingResult(recorderAddress, TimerStatusData.parseFrom(result));
            }

            public void onClearTimerRecordingResult(int recorderAddress, int result) {
                callback.onClearTimerRecordingResult(recorderAddress, result);
            }
        };
    }

    public void startOneTouchRecord(int recorderAddress, RecordSource source) {
        if (source == null) {
            throw new IllegalArgumentException("source must not be null.");
        }
        try {
            byte[] data = new byte[source.getDataSize(true)];
            source.toByteArray(true, data, 0);
            this.mService.startOneTouchRecord(recorderAddress, data);
        } catch (RemoteException e) {
            Log.e(TAG, "failed to start record: ", e);
        }
    }

    public void stopOneTouchRecord(int recorderAddress) {
        try {
            this.mService.stopOneTouchRecord(recorderAddress);
        } catch (RemoteException e) {
            Log.e(TAG, "failed to stop record: ", e);
        }
    }

    public void startTimerRecording(int recorderAddress, int sourceType, TimerRecordSource source) {
        if (source == null) {
            throw new IllegalArgumentException("source must not be null.");
        }
        checkTimerRecordingSourceType(sourceType);
        try {
            byte[] data = new byte[source.getDataSize()];
            source.toByteArray(data, 0);
            this.mService.startTimerRecording(recorderAddress, sourceType, data);
        } catch (RemoteException e) {
            Log.e(TAG, "failed to start record: ", e);
        }
    }

    private void checkTimerRecordingSourceType(int sourceType) {
        switch (sourceType) {
            case 1:
            case 2:
            case 3:
                return;
            default:
                throw new IllegalArgumentException("Invalid source type:" + sourceType);
        }
    }

    public void clearTimerRecording(int recorderAddress, int sourceType, TimerRecordSource source) {
        if (source == null) {
            throw new IllegalArgumentException("source must not be null.");
        }
        checkTimerRecordingSourceType(sourceType);
        try {
            byte[] data = new byte[source.getDataSize()];
            source.toByteArray(data, 0);
            this.mService.clearTimerRecording(recorderAddress, sourceType, data);
        } catch (RemoteException e) {
            Log.e(TAG, "failed to start record: ", e);
        }
    }

    public void setHdmiMhlVendorCommandListener(HdmiMhlVendorCommandListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener must not be null.");
        }
        try {
            this.mService.addHdmiMhlVendorCommandListener(getListenerWrapper(listener));
        } catch (RemoteException e) {
            Log.e(TAG, "failed to set hdmi mhl vendor command listener: ", e);
        }
    }

    private IHdmiMhlVendorCommandListener getListenerWrapper(final HdmiMhlVendorCommandListener listener) {
        return new IHdmiMhlVendorCommandListener.Stub() {
            public void onReceived(int portId, int offset, int length, byte[] data) {
                listener.onReceived(portId, offset, length, data);
            }
        };
    }

    public void sendMhlVendorCommand(int portId, int offset, int length, byte[] data) {
        if (data == null || data.length != 16) {
            throw new IllegalArgumentException("Invalid vendor command data.");
        } else if (offset < 0 || offset >= 16) {
            throw new IllegalArgumentException("Invalid offset:" + offset);
        } else if (length < 0 || offset + length > 16) {
            throw new IllegalArgumentException("Invalid length:" + length);
        } else {
            try {
                this.mService.sendMhlVendorCommand(portId, offset, length, data);
            } catch (RemoteException e) {
                Log.e(TAG, "failed to send vendor command: ", e);
            }
        }
    }
}
