package android.media.midi;

import android.bluetooth.BluetoothDevice;
import android.media.midi.IMidiDeviceListener;
import android.media.midi.IMidiDeviceOpenCallback;
import android.media.midi.MidiDeviceServer;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import java.util.concurrent.ConcurrentHashMap;

public final class MidiManager {
    public static final String BLUETOOTH_MIDI_SERVICE_CLASS = "com.android.bluetoothmidiservice.BluetoothMidiService";
    public static final String BLUETOOTH_MIDI_SERVICE_INTENT = "android.media.midi.BluetoothMidiService";
    public static final String BLUETOOTH_MIDI_SERVICE_PACKAGE = "com.android.bluetoothmidiservice";
    private static final String TAG = "MidiManager";
    private ConcurrentHashMap<DeviceCallback, DeviceListener> mDeviceListeners = new ConcurrentHashMap<>();
    /* access modifiers changed from: private */
    public final IMidiManager mService;
    /* access modifiers changed from: private */
    public final IBinder mToken = new Binder();

    public static class DeviceCallback {
        public void onDeviceAdded(MidiDeviceInfo device) {
        }

        public void onDeviceRemoved(MidiDeviceInfo device) {
        }

        public void onDeviceStatusChanged(MidiDeviceStatus status) {
        }
    }

    private class DeviceListener extends IMidiDeviceListener.Stub {
        /* access modifiers changed from: private */
        public final DeviceCallback mCallback;
        private final Handler mHandler;

        public DeviceListener(DeviceCallback callback, Handler handler) {
            this.mCallback = callback;
            this.mHandler = handler;
        }

        public void onDeviceAdded(MidiDeviceInfo device) {
            if (this.mHandler != null) {
                final MidiDeviceInfo deviceF = device;
                this.mHandler.post(new Runnable() {
                    public void run() {
                        DeviceListener.this.mCallback.onDeviceAdded(deviceF);
                    }
                });
                return;
            }
            this.mCallback.onDeviceAdded(device);
        }

        public void onDeviceRemoved(MidiDeviceInfo device) {
            if (this.mHandler != null) {
                final MidiDeviceInfo deviceF = device;
                this.mHandler.post(new Runnable() {
                    public void run() {
                        DeviceListener.this.mCallback.onDeviceRemoved(deviceF);
                    }
                });
                return;
            }
            this.mCallback.onDeviceRemoved(device);
        }

        public void onDeviceStatusChanged(MidiDeviceStatus status) {
            if (this.mHandler != null) {
                final MidiDeviceStatus statusF = status;
                this.mHandler.post(new Runnable() {
                    public void run() {
                        DeviceListener.this.mCallback.onDeviceStatusChanged(statusF);
                    }
                });
                return;
            }
            this.mCallback.onDeviceStatusChanged(status);
        }
    }

    public interface OnDeviceOpenedListener {
        void onDeviceOpened(MidiDevice midiDevice);
    }

    public MidiManager(IMidiManager service) {
        this.mService = service;
    }

    public void registerDeviceCallback(DeviceCallback callback, Handler handler) {
        DeviceListener deviceListener = new DeviceListener(callback, handler);
        try {
            this.mService.registerListener(this.mToken, deviceListener);
            this.mDeviceListeners.put(callback, deviceListener);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void unregisterDeviceCallback(DeviceCallback callback) {
        DeviceListener deviceListener = this.mDeviceListeners.remove(callback);
        if (deviceListener != null) {
            try {
                this.mService.unregisterListener(this.mToken, deviceListener);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public MidiDeviceInfo[] getDevices() {
        try {
            return this.mService.getDevices();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /* access modifiers changed from: private */
    public void sendOpenDeviceResponse(final MidiDevice device, final OnDeviceOpenedListener listener, Handler handler) {
        if (handler != null) {
            handler.post(new Runnable() {
                public void run() {
                    listener.onDeviceOpened(device);
                }
            });
        } else {
            listener.onDeviceOpened(device);
        }
    }

    public void openDevice(MidiDeviceInfo deviceInfo, OnDeviceOpenedListener listener, Handler handler) {
        final MidiDeviceInfo deviceInfoF = deviceInfo;
        final OnDeviceOpenedListener listenerF = listener;
        final Handler handlerF = handler;
        try {
            this.mService.openDevice(this.mToken, deviceInfo, new IMidiDeviceOpenCallback.Stub() {
                public void onDeviceOpened(IMidiDeviceServer server, IBinder deviceToken) {
                    MidiDevice device;
                    if (server != null) {
                        device = new MidiDevice(deviceInfoF, server, MidiManager.this.mService, MidiManager.this.mToken, deviceToken);
                    } else {
                        device = null;
                    }
                    MidiManager.this.sendOpenDeviceResponse(device, listenerF, handlerF);
                }
            });
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void openBluetoothDevice(BluetoothDevice bluetoothDevice, OnDeviceOpenedListener listener, Handler handler) {
        final OnDeviceOpenedListener listenerF = listener;
        final Handler handlerF = handler;
        try {
            this.mService.openBluetoothDevice(this.mToken, bluetoothDevice, new IMidiDeviceOpenCallback.Stub() {
                public void onDeviceOpened(IMidiDeviceServer server, IBinder deviceToken) {
                    MidiDevice device = null;
                    if (server != null) {
                        try {
                            MidiDevice midiDevice = new MidiDevice(server.getDeviceInfo(), server, MidiManager.this.mService, MidiManager.this.mToken, deviceToken);
                            device = midiDevice;
                        } catch (RemoteException e) {
                            Log.e(MidiManager.TAG, "remote exception in getDeviceInfo()");
                        }
                    }
                    MidiManager.this.sendOpenDeviceResponse(device, listenerF, handlerF);
                }
            });
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public MidiDeviceServer createDeviceServer(MidiReceiver[] inputPortReceivers, int numOutputPorts, String[] inputPortNames, String[] outputPortNames, Bundle properties, int type, MidiDeviceServer.Callback callback) {
        MidiReceiver[] midiReceiverArr = inputPortReceivers;
        try {
            int i = numOutputPorts;
            try {
                MidiDeviceServer server = new MidiDeviceServer(this.mService, midiReceiverArr, i, callback);
                if (this.mService.registerDeviceServer(server.getBinderInterface(), midiReceiverArr.length, i, inputPortNames, outputPortNames, properties, type) != null) {
                    return server;
                }
                Log.e(TAG, "registerVirtualDevice failed");
                return null;
            } catch (RemoteException e) {
                e = e;
                throw e.rethrowFromSystemServer();
            }
        } catch (RemoteException e2) {
            e = e2;
            int i2 = numOutputPorts;
            MidiDeviceServer.Callback callback2 = callback;
            throw e.rethrowFromSystemServer();
        }
    }
}
