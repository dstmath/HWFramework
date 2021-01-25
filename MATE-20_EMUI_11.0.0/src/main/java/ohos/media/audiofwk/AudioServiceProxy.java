package ohos.media.audiofwk;

import java.util.ArrayList;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;

public class AudioServiceProxy implements IAudioServiceProxy {
    private static final int GET_CURRENT_VOLUME = 1;
    private static final int GET_MAX_VOLUME = 3;
    private static final int GET_MIN_VOLUME = 2;
    private static final int GET_VOLUME_INFO_CMD_ID = 22;
    private static final int INVALID_VOLUME_INDEX = -1;
    private static final Logger LOGGER = LoggerFactory.getAudioLogger(AudioServiceProxy.class);
    private static final int MAX_AUDIO_STREAM_TYPE_COUNT = 11;
    private static final int SET_VOLUME_CALLBACK_CMD_ID = 21;
    private static final int SET_VOLUME_CMD_ID = 20;
    private final IRemoteObject remote;

    AudioServiceProxy(IRemoteObject iRemoteObject) {
        this.remote = iRemoteObject;
    }

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this.remote;
    }

    @Override // ohos.media.audiofwk.IAudioServiceProxy
    public int setVolumeByStreamType(int i, int i2, ArrayList<Integer> arrayList) {
        int i3 = -1;
        if (this.remote == null) {
            LOGGER.error("setVolumeByStreamType error, remote object is null", new Object[0]);
            return -1;
        } else if (arrayList == null) {
            LOGGER.error("setVolumeByStreamType error, allTypeChanged is null", new Object[0]);
            return -1;
        } else {
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption();
            obtain.writeInt(i);
            obtain.writeInt(i2);
            try {
                if (!this.remote.sendRequest(20, obtain, obtain2, messageOption)) {
                    LOGGER.error("SetVolumeByStreamType get error from service, can not set volume", new Object[0]);
                } else {
                    i3 = obtain2.readInt();
                    int readInt = obtain2.readInt();
                    if (readInt > 0 && readInt <= 11) {
                        for (int i4 = 0; i4 < readInt; i4++) {
                            arrayList.add(Integer.valueOf(obtain2.readInt()));
                        }
                    }
                }
            } catch (RemoteException unused) {
                LOGGER.error("SetVolumeByStreamType set volume failed", new Object[0]);
            } catch (Throwable th) {
                obtain2.reclaim();
                obtain.reclaim();
                throw th;
            }
            obtain2.reclaim();
            obtain.reclaim();
            return i3;
        }
    }

    @Override // ohos.media.audiofwk.IAudioServiceProxy
    public int getVolumeByStreamType(int i) {
        int i2 = -1;
        if (this.remote == null) {
            LOGGER.error("getVolumeByStreamType error, remote object is null", new Object[0]);
            return -1;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        obtain.writeInt(i);
        obtain.writeInt(1);
        LOGGER.debug("getVolumeByStreamType sendRequest to service", new Object[0]);
        try {
            if (!this.remote.sendRequest(22, obtain, obtain2, messageOption)) {
                LOGGER.error("getVolumeByStreamType sendRequest failed", new Object[0]);
            } else {
                i2 = obtain2.readInt();
            }
        } catch (RemoteException unused) {
            LOGGER.error("getVolumeByStreamType get volume failed", new Object[0]);
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            throw th;
        }
        obtain2.reclaim();
        obtain.reclaim();
        return i2;
    }

    @Override // ohos.media.audiofwk.IAudioServiceProxy
    public int getMinVolumeByStreamType(int i) {
        int i2 = -1;
        if (this.remote == null) {
            LOGGER.error("getMinVolumeByStreamType error, remote object is null", new Object[0]);
            return -1;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        obtain.writeInt(i);
        obtain.writeInt(2);
        LOGGER.debug("getMinVolumeByStreamType sendRequest to service", new Object[0]);
        try {
            if (!this.remote.sendRequest(22, obtain, obtain2, messageOption)) {
                LOGGER.error("getMinVolumeByStreamType sendRequest failed", new Object[0]);
            } else {
                i2 = obtain2.readInt();
            }
        } catch (RemoteException unused) {
            LOGGER.error("getMinVolumeByStreamType get min volume failed", new Object[0]);
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            throw th;
        }
        obtain2.reclaim();
        obtain.reclaim();
        return i2;
    }

    @Override // ohos.media.audiofwk.IAudioServiceProxy
    public int getMaxVolumeByStreamType(int i) {
        int i2 = -1;
        if (this.remote == null) {
            LOGGER.error("getMaxVolumeByStreamType error, remote object is null", new Object[0]);
            return -1;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        obtain.writeInt(i);
        obtain.writeInt(3);
        LOGGER.debug("getMaxVolumeByStreamType sendRequest to service", new Object[0]);
        try {
            if (!this.remote.sendRequest(22, obtain, obtain2, messageOption)) {
                LOGGER.error("getMaxVolumeByStreamType sendRequest failed", new Object[0]);
            } else {
                i2 = obtain2.readInt();
            }
        } catch (RemoteException unused) {
            LOGGER.error("getMaxVolumeByStreamType get max volume failed", new Object[0]);
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            throw th;
        }
        obtain2.reclaim();
        obtain.reclaim();
        return i2;
    }
}
