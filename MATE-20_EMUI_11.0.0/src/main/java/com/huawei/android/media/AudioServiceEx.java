package com.huawei.android.media;

import android.media.AudioRoutesInfo;
import android.media.IAudioRoutesObserver;
import android.media.IAudioService;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.huawei.android.audio.HwAudioServiceManager;
import com.huawei.networkit.grs.common.ExceptionCode;
import java.util.Map;

public class AudioServiceEx {
    private static final int DISABLE_VIRTUAL_AUDIO = 0;
    private static final int ENABLE_VIRTUAL_AUDIO = 1;
    private static final int SERVICE_TYPE_MIC = 2;
    private static final int SERVICE_TYPE_SPEAKER = 3;
    private static final String TAG = "AudioServiceEx";
    private static AudioServiceEx instance;
    private IAudioRoutesObserver.Stub mAudioRoutesObserver = new IAudioRoutesObserver.Stub() {
        /* class com.huawei.android.media.AudioServiceEx.AnonymousClass1 */

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
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        boolean result = false;
        try {
            data.writeInterfaceToken("android.media.IAudioService");
            data.writeInt(start ? 1 : 0);
            serviceBinder.transact(ExceptionCode.CRASH_EXCEPTION, data, reply, 0);
            reply.readException();
            if (reply.readInt() != 0) {
                z = true;
            }
            result = z;
        } catch (RemoteException e) {
            Log.e(TAG, "startRecognition error: ");
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
            throw th;
        }
        reply.recycle();
        data.recycle();
        return result;
    }

    public void setBluetoothA2dpOn(boolean on) throws RemoteException {
        this.mAudioService.setBluetoothA2dpOn(on);
    }

    public int startVirtualAudio(String deviceId, String serviceId, int serviceType, Map<String, Object> dataMap) {
        return HwAudioServiceManager.startVirtualAudio(deviceId, serviceId, serviceType, dataMap);
    }

    public int removeVirtualAudio(String deviceId, String serviceId, int serviceType, Map<String, Object> dataMap) {
        return HwAudioServiceManager.removeVirtualAudio(deviceId, serviceId, serviceType, dataMap);
    }

    public int getSafeMediaVolumeIndex(int streamType) {
        try {
            return this.mAudioService.getSafeMediaVolumeIndex(streamType);
        } catch (RemoteException e) {
            Log.e(TAG, "getSafeMediaVolumeIndex failed");
            return 0;
        }
    }

    private IBinder getServiceBinder() {
        return ServiceManager.getService("audio");
    }
}
