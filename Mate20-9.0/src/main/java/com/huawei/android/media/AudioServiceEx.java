package com.huawei.android.media;

import android.media.AudioRoutesInfo;
import android.media.IAudioRoutesObserver;
import android.media.IAudioService;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

public class AudioServiceEx {
    private static final String TAG = "AudioServiceEx";
    private static AudioServiceEx instance;
    private IAudioRoutesObserver.Stub mAudioRoutesObserver = new IAudioRoutesObserver.Stub() {
        public void dispatchAudioRoutesChanged(AudioRoutesInfo audioRoutesInfo) throws RemoteException {
        }
    };
    private IAudioService mAudioService = IAudioService.Stub.asInterface(getServiceBinder());

    private AudioServiceEx() {
    }

    public static AudioServiceEx getInstance() {
        if (instance == null) {
            instance = new AudioServiceEx();
        }
        return instance;
    }

    public AudioRoutesInfoEx startWatchingRoutes() {
        try {
            return new AudioRoutesInfoEx(this.mAudioService.startWatchingRoutes(this.mAudioRoutesObserver));
        } catch (RemoteException e) {
            Log.e(TAG, "new startWatchingRoutes failed");
            return null;
        }
    }

    public boolean startRecognition(boolean start) {
        IBinder serviceBinder = ServiceManager.getService("audio");
        boolean z = false;
        if (serviceBinder == null) {
            Log.i(TAG, "startRecognition audio service is null!");
            return false;
        }
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        boolean _result = false;
        try {
            _data.writeInterfaceToken("android.media.IAudioService");
            _data.writeInt(start);
            serviceBinder.transact(1103, _data, _reply, 0);
            _reply.readException();
            if (_reply.readInt() != 0) {
                z = true;
            }
            _result = z;
        } catch (RemoteException e) {
            Log.e(TAG, "startRecognition error: " + e);
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
        return _result;
    }

    public void setBluetoothA2dpOn(boolean on) throws RemoteException {
        this.mAudioService.setBluetoothA2dpOn(on);
    }

    private IBinder getServiceBinder() {
        return ServiceManager.getService("audio");
    }
}
