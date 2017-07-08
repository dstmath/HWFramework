package android.media;

import android.bluetooth.BluetoothDevice;
import android.media.audiopolicy.AudioPolicyConfig;
import android.media.audiopolicy.IAudioPolicyCallback;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IAudioService extends IInterface {

    public static abstract class Stub extends Binder implements IAudioService {
        private static final String DESCRIPTOR = "android.media.IAudioService";
        static final int TRANSACTION_abandonAudioFocus = 36;
        static final int TRANSACTION_adjustStreamVolume = 2;
        static final int TRANSACTION_adjustSuggestedStreamVolume = 1;
        static final int TRANSACTION_avrcpSupportsAbsoluteVolume = 28;
        static final int TRANSACTION_disableSafeMediaVolume = 54;
        static final int TRANSACTION_forceRemoteSubmixFullVolume = 5;
        static final int TRANSACTION_forceVolumeControlStream = 42;
        static final int TRANSACTION_getActiveRecordingConfigurations = 63;
        static final int TRANSACTION_getCurrentAudioFocus = 38;
        static final int TRANSACTION_getLastAudibleStreamVolume = 11;
        static final int TRANSACTION_getMode = 22;
        static final int TRANSACTION_getRingerModeExternal = 15;
        static final int TRANSACTION_getRingerModeInternal = 16;
        static final int TRANSACTION_getRingtonePlayer = 44;
        static final int TRANSACTION_getStreamMaxVolume = 10;
        static final int TRANSACTION_getStreamMinVolume = 9;
        static final int TRANSACTION_getStreamVolume = 8;
        static final int TRANSACTION_getUiSoundsStreamType = 45;
        static final int TRANSACTION_getVibrateSetting = 19;
        static final int TRANSACTION_isBluetoothA2dpOn = 34;
        static final int TRANSACTION_isBluetoothScoOn = 32;
        static final int TRANSACTION_isCameraSoundForced = 49;
        static final int TRANSACTION_isHdmiSystemAudioSupported = 56;
        static final int TRANSACTION_isMasterMute = 6;
        static final int TRANSACTION_isSpeakerphoneOn = 30;
        static final int TRANSACTION_isStreamAffectedByMute = 53;
        static final int TRANSACTION_isStreamAffectedByRingerMode = 52;
        static final int TRANSACTION_isStreamMute = 4;
        static final int TRANSACTION_isValidRingerMode = 17;
        static final int TRANSACTION_loadSoundEffects = 25;
        static final int TRANSACTION_notifyVolumeControllerVisible = 51;
        static final int TRANSACTION_playSoundEffect = 23;
        static final int TRANSACTION_playSoundEffectVolume = 24;
        static final int TRANSACTION_registerAudioPolicy = 57;
        static final int TRANSACTION_registerRecordingCallback = 61;
        static final int TRANSACTION_reloadAudioSettings = 27;
        static final int TRANSACTION_requestAudioFocus = 35;
        static final int TRANSACTION_setBluetoothA2dpDeviceConnectionState = 47;
        static final int TRANSACTION_setBluetoothA2dpOn = 33;
        static final int TRANSACTION_setBluetoothScoOn = 31;
        static final int TRANSACTION_setFocusPropertiesForPolicy = 59;
        static final int TRANSACTION_setHdmiSystemAudioSupported = 55;
        static final int TRANSACTION_setMasterMute = 7;
        static final int TRANSACTION_setMicrophoneMute = 12;
        static final int TRANSACTION_setMode = 21;
        static final int TRANSACTION_setRingerModeExternal = 13;
        static final int TRANSACTION_setRingerModeInternal = 14;
        static final int TRANSACTION_setRingtonePlayer = 43;
        static final int TRANSACTION_setSpeakerphoneOn = 29;
        static final int TRANSACTION_setStreamVolume = 3;
        static final int TRANSACTION_setVibrateSetting = 18;
        static final int TRANSACTION_setVolumeController = 50;
        static final int TRANSACTION_setVolumePolicy = 60;
        static final int TRANSACTION_setWiredDeviceConnectionState = 46;
        static final int TRANSACTION_shouldVibrate = 20;
        static final int TRANSACTION_startBluetoothSco = 39;
        static final int TRANSACTION_startBluetoothScoVirtualCall = 40;
        static final int TRANSACTION_startWatchingRoutes = 48;
        static final int TRANSACTION_stopBluetoothSco = 41;
        static final int TRANSACTION_unloadSoundEffects = 26;
        static final int TRANSACTION_unregisterAudioFocusClient = 37;
        static final int TRANSACTION_unregisterAudioPolicyAsync = 58;
        static final int TRANSACTION_unregisterRecordingCallback = 62;

        private static class Proxy implements IAudioService {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            public void adjustSuggestedStreamVolume(int direction, int suggestedStreamType, int flags, String callingPackage, String caller) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(direction);
                    _data.writeInt(suggestedStreamType);
                    _data.writeInt(flags);
                    _data.writeString(callingPackage);
                    _data.writeString(caller);
                    this.mRemote.transact(Stub.TRANSACTION_adjustSuggestedStreamVolume, _data, null, Stub.TRANSACTION_adjustSuggestedStreamVolume);
                } finally {
                    _data.recycle();
                }
            }

            public void adjustStreamVolume(int streamType, int direction, int flags, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(streamType);
                    _data.writeInt(direction);
                    _data.writeInt(flags);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_adjustStreamVolume, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setStreamVolume(int streamType, int index, int flags, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(streamType);
                    _data.writeInt(index);
                    _data.writeInt(flags);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_setStreamVolume, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isStreamMute(int streamType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(streamType);
                    this.mRemote.transact(Stub.TRANSACTION_isStreamMute, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void forceRemoteSubmixFullVolume(boolean startForcing, IBinder cb) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (startForcing) {
                        i = Stub.TRANSACTION_adjustSuggestedStreamVolume;
                    }
                    _data.writeInt(i);
                    _data.writeStrongBinder(cb);
                    this.mRemote.transact(Stub.TRANSACTION_forceRemoteSubmixFullVolume, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isMasterMute() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isMasterMute, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setMasterMute(boolean mute, int flags, String callingPackage, int userId) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (mute) {
                        i = Stub.TRANSACTION_adjustSuggestedStreamVolume;
                    }
                    _data.writeInt(i);
                    _data.writeInt(flags);
                    _data.writeString(callingPackage);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_setMasterMute, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getStreamVolume(int streamType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(streamType);
                    this.mRemote.transact(Stub.TRANSACTION_getStreamVolume, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getStreamMinVolume(int streamType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(streamType);
                    this.mRemote.transact(Stub.TRANSACTION_getStreamMinVolume, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getStreamMaxVolume(int streamType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(streamType);
                    this.mRemote.transact(Stub.TRANSACTION_getStreamMaxVolume, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getLastAudibleStreamVolume(int streamType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(streamType);
                    this.mRemote.transact(Stub.TRANSACTION_getLastAudibleStreamVolume, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setMicrophoneMute(boolean on, String callingPackage, int userId) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (on) {
                        i = Stub.TRANSACTION_adjustSuggestedStreamVolume;
                    }
                    _data.writeInt(i);
                    _data.writeString(callingPackage);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_setMicrophoneMute, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setRingerModeExternal(int ringerMode, String caller) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(ringerMode);
                    _data.writeString(caller);
                    this.mRemote.transact(Stub.TRANSACTION_setRingerModeExternal, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setRingerModeInternal(int ringerMode, String caller) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(ringerMode);
                    _data.writeString(caller);
                    this.mRemote.transact(Stub.TRANSACTION_setRingerModeInternal, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getRingerModeExternal() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getRingerModeExternal, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getRingerModeInternal() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getRingerModeInternal, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isValidRingerMode(int ringerMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(ringerMode);
                    this.mRemote.transact(Stub.TRANSACTION_isValidRingerMode, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setVibrateSetting(int vibrateType, int vibrateSetting) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(vibrateType);
                    _data.writeInt(vibrateSetting);
                    this.mRemote.transact(Stub.TRANSACTION_setVibrateSetting, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getVibrateSetting(int vibrateType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(vibrateType);
                    this.mRemote.transact(Stub.TRANSACTION_getVibrateSetting, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean shouldVibrate(int vibrateType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(vibrateType);
                    this.mRemote.transact(Stub.TRANSACTION_shouldVibrate, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setMode(int mode, IBinder cb, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mode);
                    _data.writeStrongBinder(cb);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_setMode, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getMode, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void playSoundEffect(int effectType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(effectType);
                    this.mRemote.transact(Stub.TRANSACTION_playSoundEffect, _data, null, Stub.TRANSACTION_adjustSuggestedStreamVolume);
                } finally {
                    _data.recycle();
                }
            }

            public void playSoundEffectVolume(int effectType, float volume) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(effectType);
                    _data.writeFloat(volume);
                    this.mRemote.transact(Stub.TRANSACTION_playSoundEffectVolume, _data, null, Stub.TRANSACTION_adjustSuggestedStreamVolume);
                } finally {
                    _data.recycle();
                }
            }

            public boolean loadSoundEffects() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_loadSoundEffects, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unloadSoundEffects() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_unloadSoundEffects, _data, null, Stub.TRANSACTION_adjustSuggestedStreamVolume);
                } finally {
                    _data.recycle();
                }
            }

            public void reloadAudioSettings() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_reloadAudioSettings, _data, null, Stub.TRANSACTION_adjustSuggestedStreamVolume);
                } finally {
                    _data.recycle();
                }
            }

            public void avrcpSupportsAbsoluteVolume(String address, boolean support) throws RemoteException {
                int i = Stub.TRANSACTION_adjustSuggestedStreamVolume;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    if (!support) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_avrcpSupportsAbsoluteVolume, _data, null, Stub.TRANSACTION_adjustSuggestedStreamVolume);
                } finally {
                    _data.recycle();
                }
            }

            public void setSpeakerphoneOn(boolean on) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (on) {
                        i = Stub.TRANSACTION_adjustSuggestedStreamVolume;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setSpeakerphoneOn, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isSpeakerphoneOn() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isSpeakerphoneOn, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setBluetoothScoOn(boolean on) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (on) {
                        i = Stub.TRANSACTION_adjustSuggestedStreamVolume;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setBluetoothScoOn, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isBluetoothScoOn() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isBluetoothScoOn, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setBluetoothA2dpOn(boolean on) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (on) {
                        i = Stub.TRANSACTION_adjustSuggestedStreamVolume;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setBluetoothA2dpOn, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isBluetoothA2dpOn() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isBluetoothA2dpOn, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int requestAudioFocus(AudioAttributes aa, int durationHint, IBinder cb, IAudioFocusDispatcher fd, String clientId, String callingPackageName, int flags, IAudioPolicyCallback pcb) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (aa != null) {
                        _data.writeInt(Stub.TRANSACTION_adjustSuggestedStreamVolume);
                        aa.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(durationHint);
                    _data.writeStrongBinder(cb);
                    _data.writeStrongBinder(fd != null ? fd.asBinder() : null);
                    _data.writeString(clientId);
                    _data.writeString(callingPackageName);
                    _data.writeInt(flags);
                    if (pcb != null) {
                        iBinder = pcb.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_requestAudioFocus, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int abandonAudioFocus(IAudioFocusDispatcher fd, String clientId, AudioAttributes aa) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (fd != null) {
                        iBinder = fd.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeString(clientId);
                    if (aa != null) {
                        _data.writeInt(Stub.TRANSACTION_adjustSuggestedStreamVolume);
                        aa.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_abandonAudioFocus, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterAudioFocusClient(String clientId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(clientId);
                    this.mRemote.transact(Stub.TRANSACTION_unregisterAudioFocusClient, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getCurrentAudioFocus() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getCurrentAudioFocus, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startBluetoothSco(IBinder cb, int targetSdkVersion) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(cb);
                    _data.writeInt(targetSdkVersion);
                    this.mRemote.transact(Stub.TRANSACTION_startBluetoothSco, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startBluetoothScoVirtualCall(IBinder cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(cb);
                    this.mRemote.transact(Stub.TRANSACTION_startBluetoothScoVirtualCall, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void stopBluetoothSco(IBinder cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(cb);
                    this.mRemote.transact(Stub.TRANSACTION_stopBluetoothSco, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void forceVolumeControlStream(int streamType, IBinder cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(streamType);
                    _data.writeStrongBinder(cb);
                    this.mRemote.transact(Stub.TRANSACTION_forceVolumeControlStream, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setRingtonePlayer(IRingtonePlayer player) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (player != null) {
                        iBinder = player.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_setRingtonePlayer, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IRingtonePlayer getRingtonePlayer() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getRingtonePlayer, _data, _reply, 0);
                    _reply.readException();
                    IRingtonePlayer _result = android.media.IRingtonePlayer.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getUiSoundsStreamType() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getUiSoundsStreamType, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setWiredDeviceConnectionState(int type, int state, String address, String name, String caller) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeInt(state);
                    _data.writeString(address);
                    _data.writeString(name);
                    _data.writeString(caller);
                    this.mRemote.transact(Stub.TRANSACTION_setWiredDeviceConnectionState, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setBluetoothA2dpDeviceConnectionState(BluetoothDevice device, int state, int profile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (device != null) {
                        _data.writeInt(Stub.TRANSACTION_adjustSuggestedStreamVolume);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(state);
                    _data.writeInt(profile);
                    this.mRemote.transact(Stub.TRANSACTION_setBluetoothA2dpDeviceConnectionState, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public AudioRoutesInfo startWatchingRoutes(IAudioRoutesObserver observer) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    AudioRoutesInfo audioRoutesInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (observer != null) {
                        iBinder = observer.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_startWatchingRoutes, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        audioRoutesInfo = (AudioRoutesInfo) AudioRoutesInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        audioRoutesInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return audioRoutesInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isCameraSoundForced() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isCameraSoundForced, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setVolumeController(IVolumeController controller) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (controller != null) {
                        iBinder = controller.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_setVolumeController, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notifyVolumeControllerVisible(IVolumeController controller, boolean visible) throws RemoteException {
                IBinder iBinder = null;
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (controller != null) {
                        iBinder = controller.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (visible) {
                        i = Stub.TRANSACTION_adjustSuggestedStreamVolume;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_notifyVolumeControllerVisible, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isStreamAffectedByRingerMode(int streamType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(streamType);
                    this.mRemote.transact(Stub.TRANSACTION_isStreamAffectedByRingerMode, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isStreamAffectedByMute(int streamType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(streamType);
                    this.mRemote.transact(Stub.TRANSACTION_isStreamAffectedByMute, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void disableSafeMediaVolume(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_disableSafeMediaVolume, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setHdmiSystemAudioSupported(boolean on) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (on) {
                        i = Stub.TRANSACTION_adjustSuggestedStreamVolume;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setHdmiSystemAudioSupported, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isHdmiSystemAudioSupported() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isHdmiSystemAudioSupported, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String registerAudioPolicy(AudioPolicyConfig policyConfig, IAudioPolicyCallback pcb, boolean hasFocusListener) throws RemoteException {
                int i = Stub.TRANSACTION_adjustSuggestedStreamVolume;
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (policyConfig != null) {
                        _data.writeInt(Stub.TRANSACTION_adjustSuggestedStreamVolume);
                        policyConfig.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (pcb != null) {
                        iBinder = pcb.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (!hasFocusListener) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_registerAudioPolicy, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterAudioPolicyAsync(IAudioPolicyCallback pcb) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (pcb != null) {
                        iBinder = pcb.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_unregisterAudioPolicyAsync, _data, null, Stub.TRANSACTION_adjustSuggestedStreamVolume);
                } finally {
                    _data.recycle();
                }
            }

            public int setFocusPropertiesForPolicy(int duckingBehavior, IAudioPolicyCallback pcb) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(duckingBehavior);
                    if (pcb != null) {
                        iBinder = pcb.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_setFocusPropertiesForPolicy, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setVolumePolicy(VolumePolicy policy) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (policy != null) {
                        _data.writeInt(Stub.TRANSACTION_adjustSuggestedStreamVolume);
                        policy.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_setVolumePolicy, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerRecordingCallback(IRecordingConfigDispatcher rcdb) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (rcdb != null) {
                        iBinder = rcdb.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_registerRecordingCallback, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterRecordingCallback(IRecordingConfigDispatcher rcdb) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (rcdb != null) {
                        iBinder = rcdb.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_unregisterRecordingCallback, _data, null, Stub.TRANSACTION_adjustSuggestedStreamVolume);
                } finally {
                    _data.recycle();
                }
            }

            public List<AudioRecordingConfiguration> getActiveRecordingConfigurations() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getActiveRecordingConfigurations, _data, _reply, 0);
                    _reply.readException();
                    List<AudioRecordingConfiguration> _result = _reply.createTypedArrayList(AudioRecordingConfiguration.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAudioService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAudioService)) {
                return new Proxy(obj);
            }
            return (IAudioService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _result;
            int _result2;
            switch (code) {
                case TRANSACTION_adjustSuggestedStreamVolume /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    adjustSuggestedStreamVolume(data.readInt(), data.readInt(), data.readInt(), data.readString(), data.readString());
                    return true;
                case TRANSACTION_adjustStreamVolume /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    adjustStreamVolume(data.readInt(), data.readInt(), data.readInt(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setStreamVolume /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    setStreamVolume(data.readInt(), data.readInt(), data.readInt(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isStreamMute /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isStreamMute(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_adjustSuggestedStreamVolume : 0);
                    return true;
                case TRANSACTION_forceRemoteSubmixFullVolume /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    forceRemoteSubmixFullVolume(data.readInt() != 0, data.readStrongBinder());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isMasterMute /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isMasterMute();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_adjustSuggestedStreamVolume : 0);
                    return true;
                case TRANSACTION_setMasterMute /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    setMasterMute(data.readInt() != 0, data.readInt(), data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getStreamVolume /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getStreamVolume(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_getStreamMinVolume /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getStreamMinVolume(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_getStreamMaxVolume /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getStreamMaxVolume(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_getLastAudibleStreamVolume /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getLastAudibleStreamVolume(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_setMicrophoneMute /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    setMicrophoneMute(data.readInt() != 0, data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setRingerModeExternal /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    setRingerModeExternal(data.readInt(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setRingerModeInternal /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    setRingerModeInternal(data.readInt(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getRingerModeExternal /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getRingerModeExternal();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_getRingerModeInternal /*16*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getRingerModeInternal();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_isValidRingerMode /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isValidRingerMode(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_adjustSuggestedStreamVolume : 0);
                    return true;
                case TRANSACTION_setVibrateSetting /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    setVibrateSetting(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getVibrateSetting /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getVibrateSetting(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_shouldVibrate /*20*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = shouldVibrate(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_adjustSuggestedStreamVolume : 0);
                    return true;
                case TRANSACTION_setMode /*21*/:
                    data.enforceInterface(DESCRIPTOR);
                    setMode(data.readInt(), data.readStrongBinder(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getMode /*22*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getMode();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_playSoundEffect /*23*/:
                    data.enforceInterface(DESCRIPTOR);
                    playSoundEffect(data.readInt());
                    return true;
                case TRANSACTION_playSoundEffectVolume /*24*/:
                    data.enforceInterface(DESCRIPTOR);
                    playSoundEffectVolume(data.readInt(), data.readFloat());
                    return true;
                case TRANSACTION_loadSoundEffects /*25*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = loadSoundEffects();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_adjustSuggestedStreamVolume : 0);
                    return true;
                case TRANSACTION_unloadSoundEffects /*26*/:
                    data.enforceInterface(DESCRIPTOR);
                    unloadSoundEffects();
                    return true;
                case TRANSACTION_reloadAudioSettings /*27*/:
                    data.enforceInterface(DESCRIPTOR);
                    reloadAudioSettings();
                    return true;
                case TRANSACTION_avrcpSupportsAbsoluteVolume /*28*/:
                    data.enforceInterface(DESCRIPTOR);
                    avrcpSupportsAbsoluteVolume(data.readString(), data.readInt() != 0);
                    return true;
                case TRANSACTION_setSpeakerphoneOn /*29*/:
                    data.enforceInterface(DESCRIPTOR);
                    setSpeakerphoneOn(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isSpeakerphoneOn /*30*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isSpeakerphoneOn();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_adjustSuggestedStreamVolume : 0);
                    return true;
                case TRANSACTION_setBluetoothScoOn /*31*/:
                    data.enforceInterface(DESCRIPTOR);
                    setBluetoothScoOn(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isBluetoothScoOn /*32*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isBluetoothScoOn();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_adjustSuggestedStreamVolume : 0);
                    return true;
                case TRANSACTION_setBluetoothA2dpOn /*33*/:
                    data.enforceInterface(DESCRIPTOR);
                    setBluetoothA2dpOn(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isBluetoothA2dpOn /*34*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isBluetoothA2dpOn();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_adjustSuggestedStreamVolume : 0);
                    return true;
                case TRANSACTION_requestAudioFocus /*35*/:
                    AudioAttributes audioAttributes;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        audioAttributes = (AudioAttributes) AudioAttributes.CREATOR.createFromParcel(data);
                    } else {
                        audioAttributes = null;
                    }
                    _result2 = requestAudioFocus(audioAttributes, data.readInt(), data.readStrongBinder(), android.media.IAudioFocusDispatcher.Stub.asInterface(data.readStrongBinder()), data.readString(), data.readString(), data.readInt(), android.media.audiopolicy.IAudioPolicyCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_abandonAudioFocus /*36*/:
                    AudioAttributes audioAttributes2;
                    data.enforceInterface(DESCRIPTOR);
                    IAudioFocusDispatcher _arg0 = android.media.IAudioFocusDispatcher.Stub.asInterface(data.readStrongBinder());
                    String _arg1 = data.readString();
                    if (data.readInt() != 0) {
                        audioAttributes2 = (AudioAttributes) AudioAttributes.CREATOR.createFromParcel(data);
                    } else {
                        audioAttributes2 = null;
                    }
                    _result2 = abandonAudioFocus(_arg0, _arg1, audioAttributes2);
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_unregisterAudioFocusClient /*37*/:
                    data.enforceInterface(DESCRIPTOR);
                    unregisterAudioFocusClient(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getCurrentAudioFocus /*38*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getCurrentAudioFocus();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_startBluetoothSco /*39*/:
                    data.enforceInterface(DESCRIPTOR);
                    startBluetoothSco(data.readStrongBinder(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_startBluetoothScoVirtualCall /*40*/:
                    data.enforceInterface(DESCRIPTOR);
                    startBluetoothScoVirtualCall(data.readStrongBinder());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_stopBluetoothSco /*41*/:
                    data.enforceInterface(DESCRIPTOR);
                    stopBluetoothSco(data.readStrongBinder());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_forceVolumeControlStream /*42*/:
                    data.enforceInterface(DESCRIPTOR);
                    forceVolumeControlStream(data.readInt(), data.readStrongBinder());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setRingtonePlayer /*43*/:
                    data.enforceInterface(DESCRIPTOR);
                    setRingtonePlayer(android.media.IRingtonePlayer.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getRingtonePlayer /*44*/:
                    data.enforceInterface(DESCRIPTOR);
                    IRingtonePlayer _result3 = getRingtonePlayer();
                    reply.writeNoException();
                    reply.writeStrongBinder(_result3 != null ? _result3.asBinder() : null);
                    return true;
                case TRANSACTION_getUiSoundsStreamType /*45*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getUiSoundsStreamType();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_setWiredDeviceConnectionState /*46*/:
                    data.enforceInterface(DESCRIPTOR);
                    setWiredDeviceConnectionState(data.readInt(), data.readInt(), data.readString(), data.readString(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setBluetoothA2dpDeviceConnectionState /*47*/:
                    BluetoothDevice bluetoothDevice;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        bluetoothDevice = (BluetoothDevice) BluetoothDevice.CREATOR.createFromParcel(data);
                    } else {
                        bluetoothDevice = null;
                    }
                    _result2 = setBluetoothA2dpDeviceConnectionState(bluetoothDevice, data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_startWatchingRoutes /*48*/:
                    data.enforceInterface(DESCRIPTOR);
                    AudioRoutesInfo _result4 = startWatchingRoutes(android.media.IAudioRoutesObserver.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    if (_result4 != null) {
                        reply.writeInt(TRANSACTION_adjustSuggestedStreamVolume);
                        _result4.writeToParcel(reply, TRANSACTION_adjustSuggestedStreamVolume);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_isCameraSoundForced /*49*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isCameraSoundForced();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_adjustSuggestedStreamVolume : 0);
                    return true;
                case TRANSACTION_setVolumeController /*50*/:
                    data.enforceInterface(DESCRIPTOR);
                    setVolumeController(android.media.IVolumeController.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_notifyVolumeControllerVisible /*51*/:
                    data.enforceInterface(DESCRIPTOR);
                    notifyVolumeControllerVisible(android.media.IVolumeController.Stub.asInterface(data.readStrongBinder()), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isStreamAffectedByRingerMode /*52*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isStreamAffectedByRingerMode(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_adjustSuggestedStreamVolume : 0);
                    return true;
                case TRANSACTION_isStreamAffectedByMute /*53*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isStreamAffectedByMute(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_adjustSuggestedStreamVolume : 0);
                    return true;
                case TRANSACTION_disableSafeMediaVolume /*54*/:
                    data.enforceInterface(DESCRIPTOR);
                    disableSafeMediaVolume(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setHdmiSystemAudioSupported /*55*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = setHdmiSystemAudioSupported(data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_isHdmiSystemAudioSupported /*56*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isHdmiSystemAudioSupported();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_adjustSuggestedStreamVolume : 0);
                    return true;
                case TRANSACTION_registerAudioPolicy /*57*/:
                    AudioPolicyConfig audioPolicyConfig;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        audioPolicyConfig = (AudioPolicyConfig) AudioPolicyConfig.CREATOR.createFromParcel(data);
                    } else {
                        audioPolicyConfig = null;
                    }
                    String _result5 = registerAudioPolicy(audioPolicyConfig, android.media.audiopolicy.IAudioPolicyCallback.Stub.asInterface(data.readStrongBinder()), data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeString(_result5);
                    return true;
                case TRANSACTION_unregisterAudioPolicyAsync /*58*/:
                    data.enforceInterface(DESCRIPTOR);
                    unregisterAudioPolicyAsync(android.media.audiopolicy.IAudioPolicyCallback.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case TRANSACTION_setFocusPropertiesForPolicy /*59*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = setFocusPropertiesForPolicy(data.readInt(), android.media.audiopolicy.IAudioPolicyCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_setVolumePolicy /*60*/:
                    VolumePolicy volumePolicy;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        volumePolicy = (VolumePolicy) VolumePolicy.CREATOR.createFromParcel(data);
                    } else {
                        volumePolicy = null;
                    }
                    setVolumePolicy(volumePolicy);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_registerRecordingCallback /*61*/:
                    data.enforceInterface(DESCRIPTOR);
                    registerRecordingCallback(android.media.IRecordingConfigDispatcher.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_unregisterRecordingCallback /*62*/:
                    data.enforceInterface(DESCRIPTOR);
                    unregisterRecordingCallback(android.media.IRecordingConfigDispatcher.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case TRANSACTION_getActiveRecordingConfigurations /*63*/:
                    data.enforceInterface(DESCRIPTOR);
                    List<AudioRecordingConfiguration> _result6 = getActiveRecordingConfigurations();
                    reply.writeNoException();
                    reply.writeTypedList(_result6);
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    int abandonAudioFocus(IAudioFocusDispatcher iAudioFocusDispatcher, String str, AudioAttributes audioAttributes) throws RemoteException;

    void adjustStreamVolume(int i, int i2, int i3, String str) throws RemoteException;

    void adjustSuggestedStreamVolume(int i, int i2, int i3, String str, String str2) throws RemoteException;

    void avrcpSupportsAbsoluteVolume(String str, boolean z) throws RemoteException;

    void disableSafeMediaVolume(String str) throws RemoteException;

    void forceRemoteSubmixFullVolume(boolean z, IBinder iBinder) throws RemoteException;

    void forceVolumeControlStream(int i, IBinder iBinder) throws RemoteException;

    List<AudioRecordingConfiguration> getActiveRecordingConfigurations() throws RemoteException;

    int getCurrentAudioFocus() throws RemoteException;

    int getLastAudibleStreamVolume(int i) throws RemoteException;

    int getMode() throws RemoteException;

    int getRingerModeExternal() throws RemoteException;

    int getRingerModeInternal() throws RemoteException;

    IRingtonePlayer getRingtonePlayer() throws RemoteException;

    int getStreamMaxVolume(int i) throws RemoteException;

    int getStreamMinVolume(int i) throws RemoteException;

    int getStreamVolume(int i) throws RemoteException;

    int getUiSoundsStreamType() throws RemoteException;

    int getVibrateSetting(int i) throws RemoteException;

    boolean isBluetoothA2dpOn() throws RemoteException;

    boolean isBluetoothScoOn() throws RemoteException;

    boolean isCameraSoundForced() throws RemoteException;

    boolean isHdmiSystemAudioSupported() throws RemoteException;

    boolean isMasterMute() throws RemoteException;

    boolean isSpeakerphoneOn() throws RemoteException;

    boolean isStreamAffectedByMute(int i) throws RemoteException;

    boolean isStreamAffectedByRingerMode(int i) throws RemoteException;

    boolean isStreamMute(int i) throws RemoteException;

    boolean isValidRingerMode(int i) throws RemoteException;

    boolean loadSoundEffects() throws RemoteException;

    void notifyVolumeControllerVisible(IVolumeController iVolumeController, boolean z) throws RemoteException;

    void playSoundEffect(int i) throws RemoteException;

    void playSoundEffectVolume(int i, float f) throws RemoteException;

    String registerAudioPolicy(AudioPolicyConfig audioPolicyConfig, IAudioPolicyCallback iAudioPolicyCallback, boolean z) throws RemoteException;

    void registerRecordingCallback(IRecordingConfigDispatcher iRecordingConfigDispatcher) throws RemoteException;

    void reloadAudioSettings() throws RemoteException;

    int requestAudioFocus(AudioAttributes audioAttributes, int i, IBinder iBinder, IAudioFocusDispatcher iAudioFocusDispatcher, String str, String str2, int i2, IAudioPolicyCallback iAudioPolicyCallback) throws RemoteException;

    int setBluetoothA2dpDeviceConnectionState(BluetoothDevice bluetoothDevice, int i, int i2) throws RemoteException;

    void setBluetoothA2dpOn(boolean z) throws RemoteException;

    void setBluetoothScoOn(boolean z) throws RemoteException;

    int setFocusPropertiesForPolicy(int i, IAudioPolicyCallback iAudioPolicyCallback) throws RemoteException;

    int setHdmiSystemAudioSupported(boolean z) throws RemoteException;

    void setMasterMute(boolean z, int i, String str, int i2) throws RemoteException;

    void setMicrophoneMute(boolean z, String str, int i) throws RemoteException;

    void setMode(int i, IBinder iBinder, String str) throws RemoteException;

    void setRingerModeExternal(int i, String str) throws RemoteException;

    void setRingerModeInternal(int i, String str) throws RemoteException;

    void setRingtonePlayer(IRingtonePlayer iRingtonePlayer) throws RemoteException;

    void setSpeakerphoneOn(boolean z) throws RemoteException;

    void setStreamVolume(int i, int i2, int i3, String str) throws RemoteException;

    void setVibrateSetting(int i, int i2) throws RemoteException;

    void setVolumeController(IVolumeController iVolumeController) throws RemoteException;

    void setVolumePolicy(VolumePolicy volumePolicy) throws RemoteException;

    void setWiredDeviceConnectionState(int i, int i2, String str, String str2, String str3) throws RemoteException;

    boolean shouldVibrate(int i) throws RemoteException;

    void startBluetoothSco(IBinder iBinder, int i) throws RemoteException;

    void startBluetoothScoVirtualCall(IBinder iBinder) throws RemoteException;

    AudioRoutesInfo startWatchingRoutes(IAudioRoutesObserver iAudioRoutesObserver) throws RemoteException;

    void stopBluetoothSco(IBinder iBinder) throws RemoteException;

    void unloadSoundEffects() throws RemoteException;

    void unregisterAudioFocusClient(String str) throws RemoteException;

    void unregisterAudioPolicyAsync(IAudioPolicyCallback iAudioPolicyCallback) throws RemoteException;

    void unregisterRecordingCallback(IRecordingConfigDispatcher iRecordingConfigDispatcher) throws RemoteException;
}
