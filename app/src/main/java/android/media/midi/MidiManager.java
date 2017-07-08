package android.media.midi;

import android.bluetooth.BluetoothDevice;
import android.media.midi.IMidiDeviceOpenCallback.Stub;
import android.media.midi.MidiDeviceServer.Callback;
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
    private ConcurrentHashMap<DeviceCallback, DeviceListener> mDeviceListeners;
    private final IMidiManager mService;
    private final IBinder mToken;

    /* renamed from: android.media.midi.MidiManager.1 */
    class AnonymousClass1 implements Runnable {
        final /* synthetic */ MidiDevice val$device;
        final /* synthetic */ OnDeviceOpenedListener val$listener;

        AnonymousClass1(OnDeviceOpenedListener val$listener, MidiDevice val$device) {
            this.val$listener = val$listener;
            this.val$device = val$device;
        }

        public void run() {
            this.val$listener.onDeviceOpened(this.val$device);
        }
    }

    /* renamed from: android.media.midi.MidiManager.2 */
    class AnonymousClass2 extends Stub {
        final /* synthetic */ MidiDeviceInfo val$deviceInfoF;
        final /* synthetic */ Handler val$handlerF;
        final /* synthetic */ OnDeviceOpenedListener val$listenerF;

        AnonymousClass2(MidiDeviceInfo val$deviceInfoF, OnDeviceOpenedListener val$listenerF, Handler val$handlerF) {
            this.val$deviceInfoF = val$deviceInfoF;
            this.val$listenerF = val$listenerF;
            this.val$handlerF = val$handlerF;
        }

        public void onDeviceOpened(IMidiDeviceServer server, IBinder deviceToken) {
            MidiDevice midiDevice;
            if (server != null) {
                midiDevice = new MidiDevice(this.val$deviceInfoF, server, MidiManager.this.mService, MidiManager.this.mToken, deviceToken);
            } else {
                midiDevice = null;
            }
            MidiManager.this.sendOpenDeviceResponse(midiDevice, this.val$listenerF, this.val$handlerF);
        }
    }

    /* renamed from: android.media.midi.MidiManager.3 */
    class AnonymousClass3 extends Stub {
        final /* synthetic */ Handler val$handlerF;
        final /* synthetic */ OnDeviceOpenedListener val$listenerF;

        AnonymousClass3(OnDeviceOpenedListener val$listenerF, Handler val$handlerF) {
            this.val$listenerF = val$listenerF;
            this.val$handlerF = val$handlerF;
        }

        public void onDeviceOpened(IMidiDeviceServer server, IBinder deviceToken) {
            MidiDevice device;
            if (server != null) {
                try {
                    device = new MidiDevice(server.getDeviceInfo(), server, MidiManager.this.mService, MidiManager.this.mToken, deviceToken);
                } catch (RemoteException e) {
                    Log.e(MidiManager.TAG, "remote exception in getDeviceInfo()");
                    device = null;
                }
            } else {
                device = null;
            }
            MidiManager.this.sendOpenDeviceResponse(device, this.val$listenerF, this.val$handlerF);
        }
    }

    public static class DeviceCallback {
        public void onDeviceAdded(MidiDeviceInfo device) {
        }

        public void onDeviceRemoved(MidiDeviceInfo device) {
        }

        public void onDeviceStatusChanged(MidiDeviceStatus status) {
        }
    }

    private class DeviceListener extends IMidiDeviceListener.Stub {
        private final DeviceCallback mCallback;
        private final Handler mHandler;

        /* renamed from: android.media.midi.MidiManager.DeviceListener.1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ MidiDeviceInfo val$deviceF;

            AnonymousClass1(MidiDeviceInfo val$deviceF) {
                this.val$deviceF = val$deviceF;
            }

            public void run() {
                DeviceListener.this.mCallback.onDeviceAdded(this.val$deviceF);
            }
        }

        /* renamed from: android.media.midi.MidiManager.DeviceListener.2 */
        class AnonymousClass2 implements Runnable {
            final /* synthetic */ MidiDeviceInfo val$deviceF;

            AnonymousClass2(MidiDeviceInfo val$deviceF) {
                this.val$deviceF = val$deviceF;
            }

            public void run() {
                DeviceListener.this.mCallback.onDeviceRemoved(this.val$deviceF);
            }
        }

        /* renamed from: android.media.midi.MidiManager.DeviceListener.3 */
        class AnonymousClass3 implements Runnable {
            final /* synthetic */ MidiDeviceStatus val$statusF;

            AnonymousClass3(MidiDeviceStatus val$statusF) {
                this.val$statusF = val$statusF;
            }

            public void run() {
                DeviceListener.this.mCallback.onDeviceStatusChanged(this.val$statusF);
            }
        }

        public DeviceListener(DeviceCallback callback, Handler handler) {
            this.mCallback = callback;
            this.mHandler = handler;
        }

        public void onDeviceAdded(MidiDeviceInfo device) {
            if (this.mHandler != null) {
                MidiDeviceInfo deviceF = device;
                this.mHandler.post(new AnonymousClass1(device));
                return;
            }
            this.mCallback.onDeviceAdded(device);
        }

        public void onDeviceRemoved(MidiDeviceInfo device) {
            if (this.mHandler != null) {
                MidiDeviceInfo deviceF = device;
                this.mHandler.post(new AnonymousClass2(device));
                return;
            }
            this.mCallback.onDeviceRemoved(device);
        }

        public void onDeviceStatusChanged(MidiDeviceStatus status) {
            if (this.mHandler != null) {
                MidiDeviceStatus statusF = status;
                this.mHandler.post(new AnonymousClass3(status));
                return;
            }
            this.mCallback.onDeviceStatusChanged(status);
        }
    }

    public interface OnDeviceOpenedListener {
        void onDeviceOpened(MidiDevice midiDevice);
    }

    public MidiManager(IMidiManager service) {
        this.mToken = new Binder();
        this.mDeviceListeners = new ConcurrentHashMap();
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
        DeviceListener deviceListener = (DeviceListener) this.mDeviceListeners.remove(callback);
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

    private void sendOpenDeviceResponse(MidiDevice device, OnDeviceOpenedListener listener, Handler handler) {
        if (handler != null) {
            handler.post(new AnonymousClass1(listener, device));
        } else {
            listener.onDeviceOpened(device);
        }
    }

    public void openDevice(MidiDeviceInfo deviceInfo, OnDeviceOpenedListener listener, Handler handler) {
        MidiDeviceInfo deviceInfoF = deviceInfo;
        OnDeviceOpenedListener listenerF = listener;
        Handler handlerF = handler;
        try {
            this.mService.openDevice(this.mToken, deviceInfo, new AnonymousClass2(deviceInfo, listener, handler));
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void openBluetoothDevice(BluetoothDevice bluetoothDevice, OnDeviceOpenedListener listener, Handler handler) {
        OnDeviceOpenedListener listenerF = listener;
        Handler handlerF = handler;
        try {
            this.mService.openBluetoothDevice(this.mToken, bluetoothDevice, new AnonymousClass3(listener, handler));
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public MidiDeviceServer createDeviceServer(MidiReceiver[] inputPortReceivers, int numOutputPorts, String[] inputPortNames, String[] outputPortNames, Bundle properties, int type, Callback callback) {
        try {
            MidiDeviceServer server = new MidiDeviceServer(this.mService, inputPortReceivers, numOutputPorts, callback);
            if (this.mService.registerDeviceServer(server.getBinderInterface(), inputPortReceivers.length, numOutputPorts, inputPortNames, outputPortNames, properties, type) != null) {
                return server;
            }
            Log.e(TAG, "registerVirtualDevice failed");
            return null;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
}
