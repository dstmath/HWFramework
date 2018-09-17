package com.huawei.android.media;

import android.media.AudioRoutesInfo;
import android.media.IAudioRoutesObserver.Stub;
import android.media.IAudioService;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

public class AudioServiceEx {
    private static final String TAG = "AudioServiceEx";
    private static AudioServiceEx instance;
    private Stub mAudioRoutesObserver = new Stub() {
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
        int i = 0;
        IBinder serviceBinder = ServiceManager.getService("audio");
        if (serviceBinder == null) {
            Log.i(TAG, "startRecognition audio service is null!");
            return false;
        }
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        boolean _result = false;
        try {
            _data.writeInterfaceToken("android.media.IAudioService");
            if (start) {
                i = 1;
            }
            _data.writeInt(i);
            serviceBinder.transact(1103, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readInt() != 0;
            _reply.recycle();
            _data.recycle();
        } catch (RemoteException e) {
            Log.e(TAG, "startRecognition error: " + e);
            _reply.recycle();
            _data.recycle();
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        return _result;
    }

    private IBinder getServiceBinder() {
        return ServiceManager.getService("audio");
    }
}
