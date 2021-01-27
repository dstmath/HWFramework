package android.media.midi;

import android.app.Service;
import android.content.Intent;
import android.media.midi.IMidiManager;
import android.media.midi.MidiDeviceServer;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

public abstract class MidiDeviceService extends Service {
    public static final String SERVICE_INTERFACE = "android.media.midi.MidiDeviceService";
    private static final String TAG = "MidiDeviceService";
    private final MidiDeviceServer.Callback mCallback = new MidiDeviceServer.Callback() {
        /* class android.media.midi.MidiDeviceService.AnonymousClass1 */

        @Override // android.media.midi.MidiDeviceServer.Callback
        public void onDeviceStatusChanged(MidiDeviceServer server, MidiDeviceStatus status) {
            MidiDeviceService.this.onDeviceStatusChanged(status);
        }

        @Override // android.media.midi.MidiDeviceServer.Callback
        public void onClose() {
            MidiDeviceService.this.onClose();
        }
    };
    private MidiDeviceInfo mDeviceInfo;
    private IMidiManager mMidiManager;
    private MidiDeviceServer mServer;

    public abstract MidiReceiver[] onGetInputPortReceivers();

    @Override // android.app.Service
    public void onCreate() {
        MidiDeviceServer server;
        this.mMidiManager = IMidiManager.Stub.asInterface(ServiceManager.getService("midi"));
        try {
            MidiDeviceInfo deviceInfo = this.mMidiManager.getServiceDeviceInfo(getPackageName(), getClass().getName());
            if (deviceInfo == null) {
                Log.e(TAG, "Could not find MidiDeviceInfo for MidiDeviceService " + this);
                return;
            }
            this.mDeviceInfo = deviceInfo;
            MidiReceiver[] inputPortReceivers = onGetInputPortReceivers();
            if (inputPortReceivers == null) {
                inputPortReceivers = new MidiReceiver[0];
            }
            server = new MidiDeviceServer(this.mMidiManager, inputPortReceivers, deviceInfo, this.mCallback);
            this.mServer = server;
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in IMidiManager.getServiceDeviceInfo");
            server = null;
        }
    }

    public final MidiReceiver[] getOutputPortReceivers() {
        MidiDeviceServer midiDeviceServer = this.mServer;
        if (midiDeviceServer == null) {
            return null;
        }
        return midiDeviceServer.getOutputPortReceivers();
    }

    public final MidiDeviceInfo getDeviceInfo() {
        return this.mDeviceInfo;
    }

    public void onDeviceStatusChanged(MidiDeviceStatus status) {
    }

    public void onClose() {
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        MidiDeviceServer midiDeviceServer;
        if (!SERVICE_INTERFACE.equals(intent.getAction()) || (midiDeviceServer = this.mServer) == null) {
            return null;
        }
        return midiDeviceServer.getBinderInterface().asBinder();
    }
}
