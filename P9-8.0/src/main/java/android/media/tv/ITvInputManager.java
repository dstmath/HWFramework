package android.media.tv;

import android.content.Intent;
import android.graphics.Rect;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.view.Surface;
import java.util.List;

public interface ITvInputManager extends IInterface {

    public static abstract class Stub extends Binder implements ITvInputManager {
        private static final String DESCRIPTOR = "android.media.tv.ITvInputManager";
        static final int TRANSACTION_acquireTvInputHardware = 37;
        static final int TRANSACTION_addBlockedRating = 12;
        static final int TRANSACTION_captureFrame = 40;
        static final int TRANSACTION_createOverlayView = 24;
        static final int TRANSACTION_createSession = 14;
        static final int TRANSACTION_dispatchSurfaceChanged = 18;
        static final int TRANSACTION_getAvailableTvStreamConfigList = 39;
        static final int TRANSACTION_getBlockedRatings = 11;
        static final int TRANSACTION_getDvbDeviceList = 42;
        static final int TRANSACTION_getHardwareList = 36;
        static final int TRANSACTION_getTvContentRatingSystemList = 5;
        static final int TRANSACTION_getTvInputInfo = 2;
        static final int TRANSACTION_getTvInputList = 1;
        static final int TRANSACTION_getTvInputState = 4;
        static final int TRANSACTION_isParentalControlsEnabled = 8;
        static final int TRANSACTION_isRatingBlocked = 10;
        static final int TRANSACTION_isSingleSessionActive = 41;
        static final int TRANSACTION_openDvbDevice = 43;
        static final int TRANSACTION_registerCallback = 6;
        static final int TRANSACTION_relayoutOverlayView = 25;
        static final int TRANSACTION_releaseSession = 15;
        static final int TRANSACTION_releaseTvInputHardware = 38;
        static final int TRANSACTION_removeBlockedRating = 13;
        static final int TRANSACTION_removeOverlayView = 26;
        static final int TRANSACTION_requestChannelBrowsable = 45;
        static final int TRANSACTION_selectTrack = 22;
        static final int TRANSACTION_sendAppPrivateCommand = 23;
        static final int TRANSACTION_sendTvInputNotifyIntent = 44;
        static final int TRANSACTION_setCaptionEnabled = 21;
        static final int TRANSACTION_setMainSession = 16;
        static final int TRANSACTION_setParentalControlsEnabled = 9;
        static final int TRANSACTION_setSurface = 17;
        static final int TRANSACTION_setVolume = 19;
        static final int TRANSACTION_startRecording = 34;
        static final int TRANSACTION_stopRecording = 35;
        static final int TRANSACTION_timeShiftEnablePositionTracking = 33;
        static final int TRANSACTION_timeShiftPause = 29;
        static final int TRANSACTION_timeShiftPlay = 28;
        static final int TRANSACTION_timeShiftResume = 30;
        static final int TRANSACTION_timeShiftSeekTo = 31;
        static final int TRANSACTION_timeShiftSetPlaybackParams = 32;
        static final int TRANSACTION_tune = 20;
        static final int TRANSACTION_unblockContent = 27;
        static final int TRANSACTION_unregisterCallback = 7;
        static final int TRANSACTION_updateTvInputInfo = 3;

        private static class Proxy implements ITvInputManager {
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

            public List<TvInputInfo> getTvInputList(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    List<TvInputInfo> _result = _reply.createTypedArrayList(TvInputInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public TvInputInfo getTvInputInfo(String inputId, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    TvInputInfo _result;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(inputId);
                    _data.writeInt(userId);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (TvInputInfo) TvInputInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateTvInputInfo(TvInputInfo inputInfo, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (inputInfo != null) {
                        _data.writeInt(1);
                        inputInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getTvInputState(String inputId, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(inputId);
                    _data.writeInt(userId);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<TvContentRatingSystemInfo> getTvContentRatingSystemList(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    List<TvContentRatingSystemInfo> _result = _reply.createTypedArrayList(TvContentRatingSystemInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerCallback(ITvInputManagerCallback callback, int userId) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(userId);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterCallback(ITvInputManagerCallback callback, int userId) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(userId);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isParentalControlsEnabled(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(8, _data, _reply, 0);
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

            public void setParentalControlsEnabled(boolean enabled, int userId) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (enabled) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    _data.writeInt(userId);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isRatingBlocked(String rating, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(rating);
                    _data.writeInt(userId);
                    this.mRemote.transact(10, _data, _reply, 0);
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

            public List<String> getBlockedRatings(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addBlockedRating(String rating, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(rating);
                    _data.writeInt(userId);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeBlockedRating(String rating, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(rating);
                    _data.writeInt(userId);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void createSession(ITvInputClient client, String inputId, boolean isRecordingSession, int seq, int userId) throws RemoteException {
                IBinder iBinder = null;
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (client != null) {
                        iBinder = client.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeString(inputId);
                    if (isRecordingSession) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    _data.writeInt(seq);
                    _data.writeInt(userId);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void releaseSession(IBinder sessionToken, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sessionToken);
                    _data.writeInt(userId);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setMainSession(IBinder sessionToken, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sessionToken);
                    _data.writeInt(userId);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setSurface(IBinder sessionToken, Surface surface, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sessionToken);
                    if (surface != null) {
                        _data.writeInt(1);
                        surface.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void dispatchSurfaceChanged(IBinder sessionToken, int format, int width, int height, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sessionToken);
                    _data.writeInt(format);
                    _data.writeInt(width);
                    _data.writeInt(height);
                    _data.writeInt(userId);
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setVolume(IBinder sessionToken, float volume, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sessionToken);
                    _data.writeFloat(volume);
                    _data.writeInt(userId);
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void tune(IBinder sessionToken, Uri channelUri, Bundle params, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sessionToken);
                    if (channelUri != null) {
                        _data.writeInt(1);
                        channelUri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (params != null) {
                        _data.writeInt(1);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setCaptionEnabled(IBinder sessionToken, boolean enabled, int userId) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sessionToken);
                    if (enabled) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    _data.writeInt(userId);
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void selectTrack(IBinder sessionToken, int type, String trackId, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sessionToken);
                    _data.writeInt(type);
                    _data.writeString(trackId);
                    _data.writeInt(userId);
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sendAppPrivateCommand(IBinder sessionToken, String action, Bundle data, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sessionToken);
                    _data.writeString(action);
                    if (data != null) {
                        _data.writeInt(1);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void createOverlayView(IBinder sessionToken, IBinder windowToken, Rect frame, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sessionToken);
                    _data.writeStrongBinder(windowToken);
                    if (frame != null) {
                        _data.writeInt(1);
                        frame.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void relayoutOverlayView(IBinder sessionToken, Rect frame, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sessionToken);
                    if (frame != null) {
                        _data.writeInt(1);
                        frame.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeOverlayView(IBinder sessionToken, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sessionToken);
                    _data.writeInt(userId);
                    this.mRemote.transact(26, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unblockContent(IBinder sessionToken, String unblockedRating, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sessionToken);
                    _data.writeString(unblockedRating);
                    _data.writeInt(userId);
                    this.mRemote.transact(27, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void timeShiftPlay(IBinder sessionToken, Uri recordedProgramUri, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sessionToken);
                    if (recordedProgramUri != null) {
                        _data.writeInt(1);
                        recordedProgramUri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(28, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void timeShiftPause(IBinder sessionToken, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sessionToken);
                    _data.writeInt(userId);
                    this.mRemote.transact(29, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void timeShiftResume(IBinder sessionToken, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sessionToken);
                    _data.writeInt(userId);
                    this.mRemote.transact(30, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void timeShiftSeekTo(IBinder sessionToken, long timeMs, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sessionToken);
                    _data.writeLong(timeMs);
                    _data.writeInt(userId);
                    this.mRemote.transact(31, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void timeShiftSetPlaybackParams(IBinder sessionToken, PlaybackParams params, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sessionToken);
                    if (params != null) {
                        _data.writeInt(1);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(32, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void timeShiftEnablePositionTracking(IBinder sessionToken, boolean enable, int userId) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sessionToken);
                    if (enable) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    _data.writeInt(userId);
                    this.mRemote.transact(33, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startRecording(IBinder sessionToken, Uri programUri, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sessionToken);
                    if (programUri != null) {
                        _data.writeInt(1);
                        programUri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(34, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void stopRecording(IBinder sessionToken, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sessionToken);
                    _data.writeInt(userId);
                    this.mRemote.transact(35, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<TvInputHardwareInfo> getHardwareList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(36, _data, _reply, 0);
                    _reply.readException();
                    List<TvInputHardwareInfo> _result = _reply.createTypedArrayList(TvInputHardwareInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ITvInputHardware acquireTvInputHardware(int deviceId, ITvInputHardwareCallback callback, TvInputInfo info, int userId) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(deviceId);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(37, _data, _reply, 0);
                    _reply.readException();
                    ITvInputHardware _result = android.media.tv.ITvInputHardware.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void releaseTvInputHardware(int deviceId, ITvInputHardware hardware, int userId) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(deviceId);
                    if (hardware != null) {
                        iBinder = hardware.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(userId);
                    this.mRemote.transact(38, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<TvStreamConfig> getAvailableTvStreamConfigList(String inputId, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(inputId);
                    _data.writeInt(userId);
                    this.mRemote.transact(39, _data, _reply, 0);
                    _reply.readException();
                    List<TvStreamConfig> _result = _reply.createTypedArrayList(TvStreamConfig.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean captureFrame(String inputId, Surface surface, TvStreamConfig config, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(inputId);
                    if (surface != null) {
                        _data.writeInt(1);
                        surface.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (config != null) {
                        _data.writeInt(1);
                        config.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(40, _data, _reply, 0);
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

            public boolean isSingleSessionActive(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(41, _data, _reply, 0);
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

            public List<DvbDeviceInfo> getDvbDeviceList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(42, _data, _reply, 0);
                    _reply.readException();
                    List<DvbDeviceInfo> _result = _reply.createTypedArrayList(DvbDeviceInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParcelFileDescriptor openDvbDevice(DvbDeviceInfo info, int device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ParcelFileDescriptor _result;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(device);
                    this.mRemote.transact(43, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sendTvInputNotifyIntent(Intent intent, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(44, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void requestChannelBrowsable(Uri channelUri, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (channelUri != null) {
                        _data.writeInt(1);
                        channelUri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(45, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ITvInputManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ITvInputManager)) {
                return new Proxy(obj);
            }
            return (ITvInputManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _result;
            IBinder _arg0;
            Surface _arg1;
            Uri _arg12;
            Bundle _arg2;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    List<TvInputInfo> _result2 = getTvInputList(data.readInt());
                    reply.writeNoException();
                    reply.writeTypedList(_result2);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    TvInputInfo _result3 = getTvInputInfo(data.readString(), data.readInt());
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(1);
                        _result3.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 3:
                    TvInputInfo _arg02;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg02 = (TvInputInfo) TvInputInfo.CREATOR.createFromParcel(data);
                    } else {
                        _arg02 = null;
                    }
                    updateTvInputInfo(_arg02, data.readInt());
                    reply.writeNoException();
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    int _result4 = getTvInputState(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    List<TvContentRatingSystemInfo> _result5 = getTvContentRatingSystemList(data.readInt());
                    reply.writeNoException();
                    reply.writeTypedList(_result5);
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    registerCallback(android.media.tv.ITvInputManagerCallback.Stub.asInterface(data.readStrongBinder()), data.readInt());
                    reply.writeNoException();
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    unregisterCallback(android.media.tv.ITvInputManagerCallback.Stub.asInterface(data.readStrongBinder()), data.readInt());
                    reply.writeNoException();
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isParentalControlsEnabled(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    setParentalControlsEnabled(data.readInt() != 0, data.readInt());
                    reply.writeNoException();
                    return true;
                case 10:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isRatingBlocked(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 11:
                    data.enforceInterface(DESCRIPTOR);
                    List<String> _result6 = getBlockedRatings(data.readInt());
                    reply.writeNoException();
                    reply.writeStringList(_result6);
                    return true;
                case 12:
                    data.enforceInterface(DESCRIPTOR);
                    addBlockedRating(data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 13:
                    data.enforceInterface(DESCRIPTOR);
                    removeBlockedRating(data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 14:
                    data.enforceInterface(DESCRIPTOR);
                    createSession(android.media.tv.ITvInputClient.Stub.asInterface(data.readStrongBinder()), data.readString(), data.readInt() != 0, data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 15:
                    data.enforceInterface(DESCRIPTOR);
                    releaseSession(data.readStrongBinder(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 16:
                    data.enforceInterface(DESCRIPTOR);
                    setMainSession(data.readStrongBinder(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 17:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readStrongBinder();
                    if (data.readInt() != 0) {
                        _arg1 = (Surface) Surface.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    setSurface(_arg0, _arg1, data.readInt());
                    reply.writeNoException();
                    return true;
                case 18:
                    data.enforceInterface(DESCRIPTOR);
                    dispatchSurfaceChanged(data.readStrongBinder(), data.readInt(), data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 19:
                    data.enforceInterface(DESCRIPTOR);
                    setVolume(data.readStrongBinder(), data.readFloat(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 20:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readStrongBinder();
                    if (data.readInt() != 0) {
                        _arg12 = (Uri) Uri.CREATOR.createFromParcel(data);
                    } else {
                        _arg12 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    tune(_arg0, _arg12, _arg2, data.readInt());
                    reply.writeNoException();
                    return true;
                case 21:
                    data.enforceInterface(DESCRIPTOR);
                    setCaptionEnabled(data.readStrongBinder(), data.readInt() != 0, data.readInt());
                    reply.writeNoException();
                    return true;
                case 22:
                    data.enforceInterface(DESCRIPTOR);
                    selectTrack(data.readStrongBinder(), data.readInt(), data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 23:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readStrongBinder();
                    String _arg13 = data.readString();
                    if (data.readInt() != 0) {
                        _arg2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    sendAppPrivateCommand(_arg0, _arg13, _arg2, data.readInt());
                    reply.writeNoException();
                    return true;
                case 24:
                    Rect _arg22;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readStrongBinder();
                    IBinder _arg14 = data.readStrongBinder();
                    if (data.readInt() != 0) {
                        _arg22 = (Rect) Rect.CREATOR.createFromParcel(data);
                    } else {
                        _arg22 = null;
                    }
                    createOverlayView(_arg0, _arg14, _arg22, data.readInt());
                    reply.writeNoException();
                    return true;
                case 25:
                    Rect _arg15;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readStrongBinder();
                    if (data.readInt() != 0) {
                        _arg15 = (Rect) Rect.CREATOR.createFromParcel(data);
                    } else {
                        _arg15 = null;
                    }
                    relayoutOverlayView(_arg0, _arg15, data.readInt());
                    reply.writeNoException();
                    return true;
                case 26:
                    data.enforceInterface(DESCRIPTOR);
                    removeOverlayView(data.readStrongBinder(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 27:
                    data.enforceInterface(DESCRIPTOR);
                    unblockContent(data.readStrongBinder(), data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 28:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readStrongBinder();
                    if (data.readInt() != 0) {
                        _arg12 = (Uri) Uri.CREATOR.createFromParcel(data);
                    } else {
                        _arg12 = null;
                    }
                    timeShiftPlay(_arg0, _arg12, data.readInt());
                    reply.writeNoException();
                    return true;
                case 29:
                    data.enforceInterface(DESCRIPTOR);
                    timeShiftPause(data.readStrongBinder(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 30:
                    data.enforceInterface(DESCRIPTOR);
                    timeShiftResume(data.readStrongBinder(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 31:
                    data.enforceInterface(DESCRIPTOR);
                    timeShiftSeekTo(data.readStrongBinder(), data.readLong(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 32:
                    PlaybackParams _arg16;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readStrongBinder();
                    if (data.readInt() != 0) {
                        _arg16 = (PlaybackParams) PlaybackParams.CREATOR.createFromParcel(data);
                    } else {
                        _arg16 = null;
                    }
                    timeShiftSetPlaybackParams(_arg0, _arg16, data.readInt());
                    reply.writeNoException();
                    return true;
                case 33:
                    data.enforceInterface(DESCRIPTOR);
                    timeShiftEnablePositionTracking(data.readStrongBinder(), data.readInt() != 0, data.readInt());
                    reply.writeNoException();
                    return true;
                case 34:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readStrongBinder();
                    if (data.readInt() != 0) {
                        _arg12 = (Uri) Uri.CREATOR.createFromParcel(data);
                    } else {
                        _arg12 = null;
                    }
                    startRecording(_arg0, _arg12, data.readInt());
                    reply.writeNoException();
                    return true;
                case 35:
                    data.enforceInterface(DESCRIPTOR);
                    stopRecording(data.readStrongBinder(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 36:
                    data.enforceInterface(DESCRIPTOR);
                    List<TvInputHardwareInfo> _result7 = getHardwareList();
                    reply.writeNoException();
                    reply.writeTypedList(_result7);
                    return true;
                case 37:
                    TvInputInfo _arg23;
                    data.enforceInterface(DESCRIPTOR);
                    int _arg03 = data.readInt();
                    ITvInputHardwareCallback _arg17 = android.media.tv.ITvInputHardwareCallback.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg23 = (TvInputInfo) TvInputInfo.CREATOR.createFromParcel(data);
                    } else {
                        _arg23 = null;
                    }
                    ITvInputHardware _result8 = acquireTvInputHardware(_arg03, _arg17, _arg23, data.readInt());
                    reply.writeNoException();
                    reply.writeStrongBinder(_result8 != null ? _result8.asBinder() : null);
                    return true;
                case 38:
                    data.enforceInterface(DESCRIPTOR);
                    releaseTvInputHardware(data.readInt(), android.media.tv.ITvInputHardware.Stub.asInterface(data.readStrongBinder()), data.readInt());
                    reply.writeNoException();
                    return true;
                case 39:
                    data.enforceInterface(DESCRIPTOR);
                    List<TvStreamConfig> _result9 = getAvailableTvStreamConfigList(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeTypedList(_result9);
                    return true;
                case 40:
                    TvStreamConfig _arg24;
                    data.enforceInterface(DESCRIPTOR);
                    String _arg04 = data.readString();
                    if (data.readInt() != 0) {
                        _arg1 = (Surface) Surface.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg24 = (TvStreamConfig) TvStreamConfig.CREATOR.createFromParcel(data);
                    } else {
                        _arg24 = null;
                    }
                    _result = captureFrame(_arg04, _arg1, _arg24, data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 41:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isSingleSessionActive(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 42:
                    data.enforceInterface(DESCRIPTOR);
                    List<DvbDeviceInfo> _result10 = getDvbDeviceList();
                    reply.writeNoException();
                    reply.writeTypedList(_result10);
                    return true;
                case 43:
                    DvbDeviceInfo _arg05;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg05 = (DvbDeviceInfo) DvbDeviceInfo.CREATOR.createFromParcel(data);
                    } else {
                        _arg05 = null;
                    }
                    ParcelFileDescriptor _result11 = openDvbDevice(_arg05, data.readInt());
                    reply.writeNoException();
                    if (_result11 != null) {
                        reply.writeInt(1);
                        _result11.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 44:
                    Intent _arg06;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg06 = (Intent) Intent.CREATOR.createFromParcel(data);
                    } else {
                        _arg06 = null;
                    }
                    sendTvInputNotifyIntent(_arg06, data.readInt());
                    reply.writeNoException();
                    return true;
                case 45:
                    Uri _arg07;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg07 = (Uri) Uri.CREATOR.createFromParcel(data);
                    } else {
                        _arg07 = null;
                    }
                    requestChannelBrowsable(_arg07, data.readInt());
                    reply.writeNoException();
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    ITvInputHardware acquireTvInputHardware(int i, ITvInputHardwareCallback iTvInputHardwareCallback, TvInputInfo tvInputInfo, int i2) throws RemoteException;

    void addBlockedRating(String str, int i) throws RemoteException;

    boolean captureFrame(String str, Surface surface, TvStreamConfig tvStreamConfig, int i) throws RemoteException;

    void createOverlayView(IBinder iBinder, IBinder iBinder2, Rect rect, int i) throws RemoteException;

    void createSession(ITvInputClient iTvInputClient, String str, boolean z, int i, int i2) throws RemoteException;

    void dispatchSurfaceChanged(IBinder iBinder, int i, int i2, int i3, int i4) throws RemoteException;

    List<TvStreamConfig> getAvailableTvStreamConfigList(String str, int i) throws RemoteException;

    List<String> getBlockedRatings(int i) throws RemoteException;

    List<DvbDeviceInfo> getDvbDeviceList() throws RemoteException;

    List<TvInputHardwareInfo> getHardwareList() throws RemoteException;

    List<TvContentRatingSystemInfo> getTvContentRatingSystemList(int i) throws RemoteException;

    TvInputInfo getTvInputInfo(String str, int i) throws RemoteException;

    List<TvInputInfo> getTvInputList(int i) throws RemoteException;

    int getTvInputState(String str, int i) throws RemoteException;

    boolean isParentalControlsEnabled(int i) throws RemoteException;

    boolean isRatingBlocked(String str, int i) throws RemoteException;

    boolean isSingleSessionActive(int i) throws RemoteException;

    ParcelFileDescriptor openDvbDevice(DvbDeviceInfo dvbDeviceInfo, int i) throws RemoteException;

    void registerCallback(ITvInputManagerCallback iTvInputManagerCallback, int i) throws RemoteException;

    void relayoutOverlayView(IBinder iBinder, Rect rect, int i) throws RemoteException;

    void releaseSession(IBinder iBinder, int i) throws RemoteException;

    void releaseTvInputHardware(int i, ITvInputHardware iTvInputHardware, int i2) throws RemoteException;

    void removeBlockedRating(String str, int i) throws RemoteException;

    void removeOverlayView(IBinder iBinder, int i) throws RemoteException;

    void requestChannelBrowsable(Uri uri, int i) throws RemoteException;

    void selectTrack(IBinder iBinder, int i, String str, int i2) throws RemoteException;

    void sendAppPrivateCommand(IBinder iBinder, String str, Bundle bundle, int i) throws RemoteException;

    void sendTvInputNotifyIntent(Intent intent, int i) throws RemoteException;

    void setCaptionEnabled(IBinder iBinder, boolean z, int i) throws RemoteException;

    void setMainSession(IBinder iBinder, int i) throws RemoteException;

    void setParentalControlsEnabled(boolean z, int i) throws RemoteException;

    void setSurface(IBinder iBinder, Surface surface, int i) throws RemoteException;

    void setVolume(IBinder iBinder, float f, int i) throws RemoteException;

    void startRecording(IBinder iBinder, Uri uri, int i) throws RemoteException;

    void stopRecording(IBinder iBinder, int i) throws RemoteException;

    void timeShiftEnablePositionTracking(IBinder iBinder, boolean z, int i) throws RemoteException;

    void timeShiftPause(IBinder iBinder, int i) throws RemoteException;

    void timeShiftPlay(IBinder iBinder, Uri uri, int i) throws RemoteException;

    void timeShiftResume(IBinder iBinder, int i) throws RemoteException;

    void timeShiftSeekTo(IBinder iBinder, long j, int i) throws RemoteException;

    void timeShiftSetPlaybackParams(IBinder iBinder, PlaybackParams playbackParams, int i) throws RemoteException;

    void tune(IBinder iBinder, Uri uri, Bundle bundle, int i) throws RemoteException;

    void unblockContent(IBinder iBinder, String str, int i) throws RemoteException;

    void unregisterCallback(ITvInputManagerCallback iTvInputManagerCallback, int i) throws RemoteException;

    void updateTvInputInfo(TvInputInfo tvInputInfo, int i) throws RemoteException;
}
