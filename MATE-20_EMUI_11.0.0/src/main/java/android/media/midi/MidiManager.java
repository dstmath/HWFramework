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
    private final IMidiManager mService;
    private final IBinder mToken = new Binder();

    public interface OnDeviceOpenedListener {
        void onDeviceOpened(MidiDevice midiDevice);
    }

    private class DeviceListener extends IMidiDeviceListener.Stub {
        private final DeviceCallback mCallback;
        private final Handler mHandler;

        public DeviceListener(DeviceCallback callback, Handler handler) {
            this.mCallback = callback;
            this.mHandler = handler;
        }

        @Override // android.media.midi.IMidiDeviceListener
        public void onDeviceAdded(final MidiDeviceInfo device) {
            Handler handler = this.mHandler;
            if (handler != null) {
                handler.post(new Runnable() {
                    /* class android.media.midi.MidiManager.DeviceListener.AnonymousClass1 */

                    @Override // java.lang.Runnable
                    public void run() {
                        DeviceListener.this.mCallback.onDeviceAdded(device);
                    }
                });
            } else {
                this.mCallback.onDeviceAdded(device);
            }
        }

        @Override // android.media.midi.IMidiDeviceListener
        public void onDeviceRemoved(final MidiDeviceInfo device) {
            Handler handler = this.mHandler;
            if (handler != null) {
                handler.post(new Runnable() {
                    /* class android.media.midi.MidiManager.DeviceListener.AnonymousClass2 */

                    @Override // java.lang.Runnable
                    public void run() {
                        DeviceListener.this.mCallback.onDeviceRemoved(device);
                    }
                });
            } else {
                this.mCallback.onDeviceRemoved(device);
            }
        }

        @Override // android.media.midi.IMidiDeviceListener
        public void onDeviceStatusChanged(final MidiDeviceStatus status) {
            Handler handler = this.mHandler;
            if (handler != null) {
                handler.post(new Runnable() {
                    /* class android.media.midi.MidiManager.DeviceListener.AnonymousClass3 */

                    @Override // java.lang.Runnable
                    public void run() {
                        DeviceListener.this.mCallback.onDeviceStatusChanged(status);
                    }
                });
            } else {
                this.mCallback.onDeviceStatusChanged(status);
            }
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
    /* access modifiers changed from: public */
    private void sendOpenDeviceResponse(final MidiDevice device, final OnDeviceOpenedListener listener, Handler handler) {
        if (handler != null) {
            handler.post(new Runnable() {
                /* class android.media.midi.MidiManager.AnonymousClass1 */

                @Override // java.lang.Runnable
                public void run() {
                    listener.onDeviceOpened(device);
                }
            });
        } else {
            listener.onDeviceOpened(device);
        }
    }

    public void openDevice(final MidiDeviceInfo deviceInfo, final OnDeviceOpenedListener listener, final Handler handler) {
        try {
            this.mService.openDevice(this.mToken, deviceInfo, new IMidiDeviceOpenCallback.Stub() {
                /* class android.media.midi.MidiManager.AnonymousClass2 */

                @Override // android.media.midi.IMidiDeviceOpenCallback
                public void onDeviceOpened(IMidiDeviceServer server, IBinder deviceToken) {
                    MidiDevice device;
                    if (server != null) {
                        device = new MidiDevice(deviceInfo, server, MidiManager.this.mService, MidiManager.this.mToken, deviceToken);
                    } else {
                        device = null;
                    }
                    MidiManager.this.sendOpenDeviceResponse(device, listener, handler);
                }
            });
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void openBluetoothDevice(BluetoothDevice bluetoothDevice, final OnDeviceOpenedListener listener, final Handler handler) {
        try {
            this.mService.openBluetoothDevice(this.mToken, bluetoothDevice, new IMidiDeviceOpenCallback.Stub() {
                /* class android.media.midi.MidiManager.AnonymousClass3 */

                @Override // android.media.midi.IMidiDeviceOpenCallback
                public void onDeviceOpened(IMidiDeviceServer server, IBinder deviceToken) {
                    MidiDevice device = null;
                    if (server != null) {
                        try {
                            device = new MidiDevice(server.getDeviceInfo(), server, MidiManager.this.mService, MidiManager.this.mToken, deviceToken);
                        } catch (RemoteException e) {
                            Log.e(MidiManager.TAG, "remote exception in getDeviceInfo()");
                        }
                    }
                    MidiManager.this.sendOpenDeviceResponse(device, listener, handler);
                }
            });
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public MidiDeviceServer createDeviceServer(MidiReceiver[] inputPortReceivers, int numOutputPorts, String[] inputPortNames, String[] outputPortNames, Bundle properties, int type, MidiDeviceServer.Callback callback) {
        RemoteException e;
        try {
            try {
                MidiDeviceServer server = new MidiDeviceServer(this.mService, inputPortReceivers, numOutputPorts, callback);
                if (this.mService.registerDeviceServer(server.getBinderInterface(), inputPortReceivers.length, numOutputPorts, inputPortNames, outputPortNames, properties, type) != null) {
                    return server;
                }
                Log.e(TAG, "registerVirtualDevice failed");
                return null;
            } catch (RemoteException e2) {
                e = e2;
                throw e.rethrowFromSystemServer();
            }
        } catch (RemoteException e3) {
            e = e3;
            throw e.rethrowFromSystemServer();
        }
    }
}
