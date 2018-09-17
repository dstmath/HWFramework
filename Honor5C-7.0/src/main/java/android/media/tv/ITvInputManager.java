package android.media.tv;

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
        static final int TRANSACTION_selectTrack = 22;
        static final int TRANSACTION_sendAppPrivateCommand = 23;
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
                    this.mRemote.transact(Stub.TRANSACTION_getTvInputList, _data, _reply, 0);
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
                    TvInputInfo tvInputInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(inputId);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_getTvInputInfo, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        tvInputInfo = (TvInputInfo) TvInputInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        tvInputInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return tvInputInfo;
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
                        _data.writeInt(Stub.TRANSACTION_getTvInputList);
                        inputInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_updateTvInputInfo, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getTvInputState, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getTvContentRatingSystemList, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_registerCallback, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_unregisterCallback, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_isParentalControlsEnabled, _data, _reply, 0);
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
                        i = Stub.TRANSACTION_getTvInputList;
                    }
                    _data.writeInt(i);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_setParentalControlsEnabled, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_isRatingBlocked, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getBlockedRatings, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_addBlockedRating, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_removeBlockedRating, _data, _reply, 0);
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
                        i = Stub.TRANSACTION_getTvInputList;
                    }
                    _data.writeInt(i);
                    _data.writeInt(seq);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_createSession, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_releaseSession, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_setMainSession, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_getTvInputList);
                        surface.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_setSurface, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_dispatchSurfaceChanged, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_setVolume, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_getTvInputList);
                        channelUri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (params != null) {
                        _data.writeInt(Stub.TRANSACTION_getTvInputList);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_tune, _data, _reply, 0);
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
                        i = Stub.TRANSACTION_getTvInputList;
                    }
                    _data.writeInt(i);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_setCaptionEnabled, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_selectTrack, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_getTvInputList);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_sendAppPrivateCommand, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_getTvInputList);
                        frame.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_createOverlayView, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_getTvInputList);
                        frame.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_relayoutOverlayView, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_removeOverlayView, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_unblockContent, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_getTvInputList);
                        recordedProgramUri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_timeShiftPlay, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_timeShiftPause, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_timeShiftResume, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_timeShiftSeekTo, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_getTvInputList);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_timeShiftSetPlaybackParams, _data, _reply, 0);
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
                        i = Stub.TRANSACTION_getTvInputList;
                    }
                    _data.writeInt(i);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_timeShiftEnablePositionTracking, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_getTvInputList);
                        programUri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_startRecording, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_stopRecording, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getHardwareList, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_getTvInputList);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_acquireTvInputHardware, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_releaseTvInputHardware, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getAvailableTvStreamConfigList, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_getTvInputList);
                        surface.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (config != null) {
                        _data.writeInt(Stub.TRANSACTION_getTvInputList);
                        config.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_captureFrame, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_isSingleSessionActive, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getDvbDeviceList, _data, _reply, 0);
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
                    ParcelFileDescriptor parcelFileDescriptor;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (info != null) {
                        _data.writeInt(Stub.TRANSACTION_getTvInputList);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(device);
                    this.mRemote.transact(Stub.TRANSACTION_openDvbDevice, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        parcelFileDescriptor = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(_reply);
                    } else {
                        parcelFileDescriptor = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return parcelFileDescriptor;
                } catch (Throwable th) {
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
            Surface surface;
            Uri uri;
            Bundle bundle;
            switch (code) {
                case TRANSACTION_getTvInputList /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    List<TvInputInfo> _result2 = getTvInputList(data.readInt());
                    reply.writeNoException();
                    reply.writeTypedList(_result2);
                    return true;
                case TRANSACTION_getTvInputInfo /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    TvInputInfo _result3 = getTvInputInfo(data.readString(), data.readInt());
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(TRANSACTION_getTvInputList);
                        _result3.writeToParcel(reply, TRANSACTION_getTvInputList);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_updateTvInputInfo /*3*/:
                    TvInputInfo tvInputInfo;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        tvInputInfo = (TvInputInfo) TvInputInfo.CREATOR.createFromParcel(data);
                    } else {
                        tvInputInfo = null;
                    }
                    updateTvInputInfo(tvInputInfo, data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getTvInputState /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    int _result4 = getTvInputState(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case TRANSACTION_getTvContentRatingSystemList /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    List<TvContentRatingSystemInfo> _result5 = getTvContentRatingSystemList(data.readInt());
                    reply.writeNoException();
                    reply.writeTypedList(_result5);
                    return true;
                case TRANSACTION_registerCallback /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    registerCallback(android.media.tv.ITvInputManagerCallback.Stub.asInterface(data.readStrongBinder()), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_unregisterCallback /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    unregisterCallback(android.media.tv.ITvInputManagerCallback.Stub.asInterface(data.readStrongBinder()), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isParentalControlsEnabled /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isParentalControlsEnabled(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_getTvInputList : 0);
                    return true;
                case TRANSACTION_setParentalControlsEnabled /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    setParentalControlsEnabled(data.readInt() != 0, data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isRatingBlocked /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isRatingBlocked(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_getTvInputList : 0);
                    return true;
                case TRANSACTION_getBlockedRatings /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    List<String> _result6 = getBlockedRatings(data.readInt());
                    reply.writeNoException();
                    reply.writeStringList(_result6);
                    return true;
                case TRANSACTION_addBlockedRating /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    addBlockedRating(data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_removeBlockedRating /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    removeBlockedRating(data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_createSession /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    createSession(android.media.tv.ITvInputClient.Stub.asInterface(data.readStrongBinder()), data.readString(), data.readInt() != 0, data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_releaseSession /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    releaseSession(data.readStrongBinder(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setMainSession /*16*/:
                    data.enforceInterface(DESCRIPTOR);
                    setMainSession(data.readStrongBinder(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setSurface /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readStrongBinder();
                    if (data.readInt() != 0) {
                        surface = (Surface) Surface.CREATOR.createFromParcel(data);
                    } else {
                        surface = null;
                    }
                    setSurface(_arg0, surface, data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_dispatchSurfaceChanged /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    dispatchSurfaceChanged(data.readStrongBinder(), data.readInt(), data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setVolume /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    setVolume(data.readStrongBinder(), data.readFloat(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_tune /*20*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readStrongBinder();
                    if (data.readInt() != 0) {
                        uri = (Uri) Uri.CREATOR.createFromParcel(data);
                    } else {
                        uri = null;
                    }
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    tune(_arg0, uri, bundle, data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setCaptionEnabled /*21*/:
                    data.enforceInterface(DESCRIPTOR);
                    setCaptionEnabled(data.readStrongBinder(), data.readInt() != 0, data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_selectTrack /*22*/:
                    data.enforceInterface(DESCRIPTOR);
                    selectTrack(data.readStrongBinder(), data.readInt(), data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_sendAppPrivateCommand /*23*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readStrongBinder();
                    String _arg1 = data.readString();
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    sendAppPrivateCommand(_arg0, _arg1, bundle, data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_createOverlayView /*24*/:
                    Rect rect;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readStrongBinder();
                    IBinder _arg12 = data.readStrongBinder();
                    if (data.readInt() != 0) {
                        rect = (Rect) Rect.CREATOR.createFromParcel(data);
                    } else {
                        rect = null;
                    }
                    createOverlayView(_arg0, _arg12, rect, data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_relayoutOverlayView /*25*/:
                    Rect rect2;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readStrongBinder();
                    if (data.readInt() != 0) {
                        rect2 = (Rect) Rect.CREATOR.createFromParcel(data);
                    } else {
                        rect2 = null;
                    }
                    relayoutOverlayView(_arg0, rect2, data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_removeOverlayView /*26*/:
                    data.enforceInterface(DESCRIPTOR);
                    removeOverlayView(data.readStrongBinder(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_unblockContent /*27*/:
                    data.enforceInterface(DESCRIPTOR);
                    unblockContent(data.readStrongBinder(), data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_timeShiftPlay /*28*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readStrongBinder();
                    if (data.readInt() != 0) {
                        uri = (Uri) Uri.CREATOR.createFromParcel(data);
                    } else {
                        uri = null;
                    }
                    timeShiftPlay(_arg0, uri, data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_timeShiftPause /*29*/:
                    data.enforceInterface(DESCRIPTOR);
                    timeShiftPause(data.readStrongBinder(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_timeShiftResume /*30*/:
                    data.enforceInterface(DESCRIPTOR);
                    timeShiftResume(data.readStrongBinder(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_timeShiftSeekTo /*31*/:
                    data.enforceInterface(DESCRIPTOR);
                    timeShiftSeekTo(data.readStrongBinder(), data.readLong(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_timeShiftSetPlaybackParams /*32*/:
                    PlaybackParams playbackParams;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readStrongBinder();
                    if (data.readInt() != 0) {
                        playbackParams = (PlaybackParams) PlaybackParams.CREATOR.createFromParcel(data);
                    } else {
                        playbackParams = null;
                    }
                    timeShiftSetPlaybackParams(_arg0, playbackParams, data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_timeShiftEnablePositionTracking /*33*/:
                    data.enforceInterface(DESCRIPTOR);
                    timeShiftEnablePositionTracking(data.readStrongBinder(), data.readInt() != 0, data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_startRecording /*34*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readStrongBinder();
                    if (data.readInt() != 0) {
                        uri = (Uri) Uri.CREATOR.createFromParcel(data);
                    } else {
                        uri = null;
                    }
                    startRecording(_arg0, uri, data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_stopRecording /*35*/:
                    data.enforceInterface(DESCRIPTOR);
                    stopRecording(data.readStrongBinder(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getHardwareList /*36*/:
                    data.enforceInterface(DESCRIPTOR);
                    List<TvInputHardwareInfo> _result7 = getHardwareList();
                    reply.writeNoException();
                    reply.writeTypedList(_result7);
                    return true;
                case TRANSACTION_acquireTvInputHardware /*37*/:
                    TvInputInfo tvInputInfo2;
                    data.enforceInterface(DESCRIPTOR);
                    int _arg02 = data.readInt();
                    ITvInputHardwareCallback _arg13 = android.media.tv.ITvInputHardwareCallback.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        tvInputInfo2 = (TvInputInfo) TvInputInfo.CREATOR.createFromParcel(data);
                    } else {
                        tvInputInfo2 = null;
                    }
                    ITvInputHardware _result8 = acquireTvInputHardware(_arg02, _arg13, tvInputInfo2, data.readInt());
                    reply.writeNoException();
                    reply.writeStrongBinder(_result8 != null ? _result8.asBinder() : null);
                    return true;
                case TRANSACTION_releaseTvInputHardware /*38*/:
                    data.enforceInterface(DESCRIPTOR);
                    releaseTvInputHardware(data.readInt(), android.media.tv.ITvInputHardware.Stub.asInterface(data.readStrongBinder()), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getAvailableTvStreamConfigList /*39*/:
                    data.enforceInterface(DESCRIPTOR);
                    List<TvStreamConfig> _result9 = getAvailableTvStreamConfigList(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeTypedList(_result9);
                    return true;
                case TRANSACTION_captureFrame /*40*/:
                    TvStreamConfig tvStreamConfig;
                    data.enforceInterface(DESCRIPTOR);
                    String _arg03 = data.readString();
                    if (data.readInt() != 0) {
                        surface = (Surface) Surface.CREATOR.createFromParcel(data);
                    } else {
                        surface = null;
                    }
                    if (data.readInt() != 0) {
                        tvStreamConfig = (TvStreamConfig) TvStreamConfig.CREATOR.createFromParcel(data);
                    } else {
                        tvStreamConfig = null;
                    }
                    _result = captureFrame(_arg03, surface, tvStreamConfig, data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_getTvInputList : 0);
                    return true;
                case TRANSACTION_isSingleSessionActive /*41*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isSingleSessionActive(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_getTvInputList : 0);
                    return true;
                case TRANSACTION_getDvbDeviceList /*42*/:
                    data.enforceInterface(DESCRIPTOR);
                    List<DvbDeviceInfo> _result10 = getDvbDeviceList();
                    reply.writeNoException();
                    reply.writeTypedList(_result10);
                    return true;
                case TRANSACTION_openDvbDevice /*43*/:
                    DvbDeviceInfo dvbDeviceInfo;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        dvbDeviceInfo = (DvbDeviceInfo) DvbDeviceInfo.CREATOR.createFromParcel(data);
                    } else {
                        dvbDeviceInfo = null;
                    }
                    ParcelFileDescriptor _result11 = openDvbDevice(dvbDeviceInfo, data.readInt());
                    reply.writeNoException();
                    if (_result11 != null) {
                        reply.writeInt(TRANSACTION_getTvInputList);
                        _result11.writeToParcel(reply, TRANSACTION_getTvInputList);
                    } else {
                        reply.writeInt(0);
                    }
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

    void selectTrack(IBinder iBinder, int i, String str, int i2) throws RemoteException;

    void sendAppPrivateCommand(IBinder iBinder, String str, Bundle bundle, int i) throws RemoteException;

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
