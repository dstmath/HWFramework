package android.os;

import android.os.IPermissionController.Stub;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech.Engine;
import android.telecom.AudioState;

public abstract class ServiceManagerNative extends Binder implements IServiceManager {
    public static IServiceManager asInterface(IBinder obj) {
        if (obj == null) {
            return null;
        }
        IServiceManager in = (IServiceManager) obj.queryLocalInterface(IServiceManager.descriptor);
        if (in != null) {
            return in;
        }
        return new ServiceManagerProxy(obj);
    }

    public ServiceManagerNative() {
        attachInterface(this, IServiceManager.descriptor);
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) {
        switch (code) {
            case AudioState.ROUTE_EARPIECE /*1*/:
                try {
                    data.enforceInterface(IServiceManager.descriptor);
                    reply.writeStrongBinder(getService(data.readString()));
                    return true;
                } catch (RemoteException e) {
                    break;
                }
            case AudioState.ROUTE_BLUETOOTH /*2*/:
                data.enforceInterface(IServiceManager.descriptor);
                reply.writeStrongBinder(checkService(data.readString()));
                return true;
            case Engine.DEFAULT_STREAM /*3*/:
                boolean allowIsolated;
                data.enforceInterface(IServiceManager.descriptor);
                String name = data.readString();
                IBinder service = data.readStrongBinder();
                if (data.readInt() != 0) {
                    allowIsolated = true;
                } else {
                    allowIsolated = false;
                }
                addService(name, service, allowIsolated);
                return true;
            case AudioState.ROUTE_WIRED_HEADSET /*4*/:
                data.enforceInterface(IServiceManager.descriptor);
                reply.writeStringArray(listServices());
                return true;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT /*6*/:
                data.enforceInterface(IServiceManager.descriptor);
                setPermissionController(Stub.asInterface(data.readStrongBinder()));
                return true;
        }
        return false;
    }

    public IBinder asBinder() {
        return this;
    }
}
