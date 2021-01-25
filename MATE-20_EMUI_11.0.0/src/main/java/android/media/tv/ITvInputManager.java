package android.media.tv;

import android.content.Intent;
import android.graphics.Rect;
import android.media.PlaybackParams;
import android.media.tv.ITvInputClient;
import android.media.tv.ITvInputHardware;
import android.media.tv.ITvInputHardwareCallback;
import android.media.tv.ITvInputManagerCallback;
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

    public static class Default implements ITvInputManager {
        @Override // android.media.tv.ITvInputManager
        public List<TvInputInfo> getTvInputList(int userId) throws RemoteException {
            return null;
        }

        @Override // android.media.tv.ITvInputManager
        public TvInputInfo getTvInputInfo(String inputId, int userId) throws RemoteException {
            return null;
        }

        @Override // android.media.tv.ITvInputManager
        public void updateTvInputInfo(TvInputInfo inputInfo, int userId) throws RemoteException {
        }

        @Override // android.media.tv.ITvInputManager
        public int getTvInputState(String inputId, int userId) throws RemoteException {
            return 0;
        }

        @Override // android.media.tv.ITvInputManager
        public List<TvContentRatingSystemInfo> getTvContentRatingSystemList(int userId) throws RemoteException {
            return null;
        }

        @Override // android.media.tv.ITvInputManager
        public void registerCallback(ITvInputManagerCallback callback, int userId) throws RemoteException {
        }

        @Override // android.media.tv.ITvInputManager
        public void unregisterCallback(ITvInputManagerCallback callback, int userId) throws RemoteException {
        }

        @Override // android.media.tv.ITvInputManager
        public boolean isParentalControlsEnabled(int userId) throws RemoteException {
            return false;
        }

        @Override // android.media.tv.ITvInputManager
        public void setParentalControlsEnabled(boolean enabled, int userId) throws RemoteException {
        }

        @Override // android.media.tv.ITvInputManager
        public boolean isRatingBlocked(String rating, int userId) throws RemoteException {
            return false;
        }

        @Override // android.media.tv.ITvInputManager
        public List<String> getBlockedRatings(int userId) throws RemoteException {
            return null;
        }

        @Override // android.media.tv.ITvInputManager
        public void addBlockedRating(String rating, int userId) throws RemoteException {
        }

        @Override // android.media.tv.ITvInputManager
        public void removeBlockedRating(String rating, int userId) throws RemoteException {
        }

        @Override // android.media.tv.ITvInputManager
        public void createSession(ITvInputClient client, String inputId, boolean isRecordingSession, int seq, int userId) throws RemoteException {
        }

        @Override // android.media.tv.ITvInputManager
        public void releaseSession(IBinder sessionToken, int userId) throws RemoteException {
        }

        @Override // android.media.tv.ITvInputManager
        public void setMainSession(IBinder sessionToken, int userId) throws RemoteException {
        }

        @Override // android.media.tv.ITvInputManager
        public void setSurface(IBinder sessionToken, Surface surface, int userId) throws RemoteException {
        }

        @Override // android.media.tv.ITvInputManager
        public void dispatchSurfaceChanged(IBinder sessionToken, int format, int width, int height, int userId) throws RemoteException {
        }

        @Override // android.media.tv.ITvInputManager
        public void setVolume(IBinder sessionToken, float volume, int userId) throws RemoteException {
        }

        @Override // android.media.tv.ITvInputManager
        public void tune(IBinder sessionToken, Uri channelUri, Bundle params, int userId) throws RemoteException {
        }

        @Override // android.media.tv.ITvInputManager
        public void setCaptionEnabled(IBinder sessionToken, boolean enabled, int userId) throws RemoteException {
        }

        @Override // android.media.tv.ITvInputManager
        public void selectTrack(IBinder sessionToken, int type, String trackId, int userId) throws RemoteException {
        }

        @Override // android.media.tv.ITvInputManager
        public void sendAppPrivateCommand(IBinder sessionToken, String action, Bundle data, int userId) throws RemoteException {
        }

        @Override // android.media.tv.ITvInputManager
        public void createOverlayView(IBinder sessionToken, IBinder windowToken, Rect frame, int userId) throws RemoteException {
        }

        @Override // android.media.tv.ITvInputManager
        public void relayoutOverlayView(IBinder sessionToken, Rect frame, int userId) throws RemoteException {
        }

        @Override // android.media.tv.ITvInputManager
        public void removeOverlayView(IBinder sessionToken, int userId) throws RemoteException {
        }

        @Override // android.media.tv.ITvInputManager
        public void unblockContent(IBinder sessionToken, String unblockedRating, int userId) throws RemoteException {
        }

        @Override // android.media.tv.ITvInputManager
        public void timeShiftPlay(IBinder sessionToken, Uri recordedProgramUri, int userId) throws RemoteException {
        }

        @Override // android.media.tv.ITvInputManager
        public void timeShiftPause(IBinder sessionToken, int userId) throws RemoteException {
        }

        @Override // android.media.tv.ITvInputManager
        public void timeShiftResume(IBinder sessionToken, int userId) throws RemoteException {
        }

        @Override // android.media.tv.ITvInputManager
        public void timeShiftSeekTo(IBinder sessionToken, long timeMs, int userId) throws RemoteException {
        }

        @Override // android.media.tv.ITvInputManager
        public void timeShiftSetPlaybackParams(IBinder sessionToken, PlaybackParams params, int userId) throws RemoteException {
        }

        @Override // android.media.tv.ITvInputManager
        public void timeShiftEnablePositionTracking(IBinder sessionToken, boolean enable, int userId) throws RemoteException {
        }

        @Override // android.media.tv.ITvInputManager
        public void startRecording(IBinder sessionToken, Uri programUri, int userId) throws RemoteException {
        }

        @Override // android.media.tv.ITvInputManager
        public void stopRecording(IBinder sessionToken, int userId) throws RemoteException {
        }

        @Override // android.media.tv.ITvInputManager
        public List<TvInputHardwareInfo> getHardwareList() throws RemoteException {
            return null;
        }

        @Override // android.media.tv.ITvInputManager
        public ITvInputHardware acquireTvInputHardware(int deviceId, ITvInputHardwareCallback callback, TvInputInfo info, int userId) throws RemoteException {
            return null;
        }

        @Override // android.media.tv.ITvInputManager
        public void releaseTvInputHardware(int deviceId, ITvInputHardware hardware, int userId) throws RemoteException {
        }

        @Override // android.media.tv.ITvInputManager
        public List<TvStreamConfig> getAvailableTvStreamConfigList(String inputId, int userId) throws RemoteException {
            return null;
        }

        @Override // android.media.tv.ITvInputManager
        public boolean captureFrame(String inputId, Surface surface, TvStreamConfig config, int userId) throws RemoteException {
            return false;
        }

        @Override // android.media.tv.ITvInputManager
        public boolean isSingleSessionActive(int userId) throws RemoteException {
            return false;
        }

        @Override // android.media.tv.ITvInputManager
        public List<DvbDeviceInfo> getDvbDeviceList() throws RemoteException {
            return null;
        }

        @Override // android.media.tv.ITvInputManager
        public ParcelFileDescriptor openDvbDevice(DvbDeviceInfo info, int device) throws RemoteException {
            return null;
        }

        @Override // android.media.tv.ITvInputManager
        public void sendTvInputNotifyIntent(Intent intent, int userId) throws RemoteException {
        }

        @Override // android.media.tv.ITvInputManager
        public void requestChannelBrowsable(Uri channelUri, int userId) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "getTvInputList";
                case 2:
                    return "getTvInputInfo";
                case 3:
                    return "updateTvInputInfo";
                case 4:
                    return "getTvInputState";
                case 5:
                    return "getTvContentRatingSystemList";
                case 6:
                    return "registerCallback";
                case 7:
                    return "unregisterCallback";
                case 8:
                    return "isParentalControlsEnabled";
                case 9:
                    return "setParentalControlsEnabled";
                case 10:
                    return "isRatingBlocked";
                case 11:
                    return "getBlockedRatings";
                case 12:
                    return "addBlockedRating";
                case 13:
                    return "removeBlockedRating";
                case 14:
                    return "createSession";
                case 15:
                    return "releaseSession";
                case 16:
                    return "setMainSession";
                case 17:
                    return "setSurface";
                case 18:
                    return "dispatchSurfaceChanged";
                case 19:
                    return "setVolume";
                case 20:
                    return "tune";
                case 21:
                    return "setCaptionEnabled";
                case 22:
                    return "selectTrack";
                case 23:
                    return "sendAppPrivateCommand";
                case 24:
                    return "createOverlayView";
                case 25:
                    return "relayoutOverlayView";
                case 26:
                    return "removeOverlayView";
                case 27:
                    return "unblockContent";
                case 28:
                    return "timeShiftPlay";
                case 29:
                    return "timeShiftPause";
                case 30:
                    return "timeShiftResume";
                case 31:
                    return "timeShiftSeekTo";
                case 32:
                    return "timeShiftSetPlaybackParams";
                case 33:
                    return "timeShiftEnablePositionTracking";
                case 34:
                    return "startRecording";
                case 35:
                    return "stopRecording";
                case 36:
                    return "getHardwareList";
                case 37:
                    return "acquireTvInputHardware";
                case 38:
                    return "releaseTvInputHardware";
                case 39:
                    return "getAvailableTvStreamConfigList";
                case 40:
                    return "captureFrame";
                case 41:
                    return "isSingleSessionActive";
                case 42:
                    return "getDvbDeviceList";
                case 43:
                    return "openDvbDevice";
                case 44:
                    return "sendTvInputNotifyIntent";
                case 45:
                    return "requestChannelBrowsable";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            TvInputInfo _arg0;
            Surface _arg1;
            Uri _arg12;
            Bundle _arg2;
            Bundle _arg22;
            Rect _arg23;
            Rect _arg13;
            Uri _arg14;
            PlaybackParams _arg15;
            Uri _arg16;
            TvInputInfo _arg24;
            Surface _arg17;
            TvStreamConfig _arg25;
            DvbDeviceInfo _arg02;
            Intent _arg03;
            Uri _arg04;
            if (code != 1598968902) {
                boolean _arg18 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        List<TvInputInfo> _result = getTvInputList(data.readInt());
                        reply.writeNoException();
                        reply.writeTypedList(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        TvInputInfo _result2 = getTvInputInfo(data.readString(), data.readInt());
                        reply.writeNoException();
                        if (_result2 != null) {
                            reply.writeInt(1);
                            _result2.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = TvInputInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        updateTvInputInfo(_arg0, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = getTvInputState(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        List<TvContentRatingSystemInfo> _result4 = getTvContentRatingSystemList(data.readInt());
                        reply.writeNoException();
                        reply.writeTypedList(_result4);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        registerCallback(ITvInputManagerCallback.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterCallback(ITvInputManagerCallback.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isParentalControlsEnabled = isParentalControlsEnabled(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isParentalControlsEnabled ? 1 : 0);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg18 = true;
                        }
                        setParentalControlsEnabled(_arg18, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isRatingBlocked = isRatingBlocked(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isRatingBlocked ? 1 : 0);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result5 = getBlockedRatings(data.readInt());
                        reply.writeNoException();
                        reply.writeStringList(_result5);
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
                        createSession(ITvInputClient.Stub.asInterface(data.readStrongBinder()), data.readString(), data.readInt() != 0, data.readInt(), data.readInt());
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
                        IBinder _arg05 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg1 = Surface.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        setSurface(_arg05, _arg1, data.readInt());
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
                        IBinder _arg06 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg12 = Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg2 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        tune(_arg06, _arg12, _arg2, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg07 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg18 = true;
                        }
                        setCaptionEnabled(_arg07, _arg18, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        selectTrack(data.readStrongBinder(), data.readInt(), data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg08 = data.readStrongBinder();
                        String _arg19 = data.readString();
                        if (data.readInt() != 0) {
                            _arg22 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg22 = null;
                        }
                        sendAppPrivateCommand(_arg08, _arg19, _arg22, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg09 = data.readStrongBinder();
                        IBinder _arg110 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg23 = Rect.CREATOR.createFromParcel(data);
                        } else {
                            _arg23 = null;
                        }
                        createOverlayView(_arg09, _arg110, _arg23, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg010 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg13 = Rect.CREATOR.createFromParcel(data);
                        } else {
                            _arg13 = null;
                        }
                        relayoutOverlayView(_arg010, _arg13, data.readInt());
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
                        IBinder _arg011 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg14 = Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg14 = null;
                        }
                        timeShiftPlay(_arg011, _arg14, data.readInt());
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
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg012 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg15 = PlaybackParams.CREATOR.createFromParcel(data);
                        } else {
                            _arg15 = null;
                        }
                        timeShiftSetPlaybackParams(_arg012, _arg15, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 33:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg013 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg18 = true;
                        }
                        timeShiftEnablePositionTracking(_arg013, _arg18, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 34:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg014 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg16 = Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg16 = null;
                        }
                        startRecording(_arg014, _arg16, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 35:
                        data.enforceInterface(DESCRIPTOR);
                        stopRecording(data.readStrongBinder(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 36:
                        data.enforceInterface(DESCRIPTOR);
                        List<TvInputHardwareInfo> _result6 = getHardwareList();
                        reply.writeNoException();
                        reply.writeTypedList(_result6);
                        return true;
                    case 37:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg015 = data.readInt();
                        ITvInputHardwareCallback _arg111 = ITvInputHardwareCallback.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg24 = TvInputInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg24 = null;
                        }
                        ITvInputHardware _result7 = acquireTvInputHardware(_arg015, _arg111, _arg24, data.readInt());
                        reply.writeNoException();
                        reply.writeStrongBinder(_result7 != null ? _result7.asBinder() : null);
                        return true;
                    case 38:
                        data.enforceInterface(DESCRIPTOR);
                        releaseTvInputHardware(data.readInt(), ITvInputHardware.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 39:
                        data.enforceInterface(DESCRIPTOR);
                        List<TvStreamConfig> _result8 = getAvailableTvStreamConfigList(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeTypedList(_result8);
                        return true;
                    case 40:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg016 = data.readString();
                        if (data.readInt() != 0) {
                            _arg17 = Surface.CREATOR.createFromParcel(data);
                        } else {
                            _arg17 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg25 = TvStreamConfig.CREATOR.createFromParcel(data);
                        } else {
                            _arg25 = null;
                        }
                        boolean captureFrame = captureFrame(_arg016, _arg17, _arg25, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(captureFrame ? 1 : 0);
                        return true;
                    case 41:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isSingleSessionActive = isSingleSessionActive(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isSingleSessionActive ? 1 : 0);
                        return true;
                    case 42:
                        data.enforceInterface(DESCRIPTOR);
                        List<DvbDeviceInfo> _result9 = getDvbDeviceList();
                        reply.writeNoException();
                        reply.writeTypedList(_result9);
                        return true;
                    case 43:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = DvbDeviceInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        ParcelFileDescriptor _result10 = openDvbDevice(_arg02, data.readInt());
                        reply.writeNoException();
                        if (_result10 != null) {
                            reply.writeInt(1);
                            _result10.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 44:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = Intent.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        sendTvInputNotifyIntent(_arg03, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 45:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg04 = null;
                        }
                        requestChannelBrowsable(_arg04, data.readInt());
                        reply.writeNoException();
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements ITvInputManager {
            public static ITvInputManager sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // android.media.tv.ITvInputManager
            public List<TvInputInfo> getTvInputList(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTvInputList(userId);
                    }
                    _reply.readException();
                    List<TvInputInfo> _result = _reply.createTypedArrayList(TvInputInfo.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputManager
            public TvInputInfo getTvInputInfo(String inputId, int userId) throws RemoteException {
                TvInputInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(inputId);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTvInputInfo(inputId, userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = TvInputInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputManager
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
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateTvInputInfo(inputInfo, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputManager
            public int getTvInputState(String inputId, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(inputId);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTvInputState(inputId, userId);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputManager
            public List<TvContentRatingSystemInfo> getTvContentRatingSystemList(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTvContentRatingSystemList(userId);
                    }
                    _reply.readException();
                    List<TvContentRatingSystemInfo> _result = _reply.createTypedArrayList(TvContentRatingSystemInfo.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputManager
            public void registerCallback(ITvInputManagerCallback callback, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerCallback(callback, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputManager
            public void unregisterCallback(ITvInputManagerCallback callback, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterCallback(callback, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputManager
            public boolean isParentalControlsEnabled(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    boolean _result = false;
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isParentalControlsEnabled(userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputManager
            public void setParentalControlsEnabled(boolean enabled, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enabled ? 1 : 0);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setParentalControlsEnabled(enabled, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputManager
            public boolean isRatingBlocked(String rating, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(rating);
                    _data.writeInt(userId);
                    boolean _result = false;
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isRatingBlocked(rating, userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputManager
            public List<String> getBlockedRatings(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getBlockedRatings(userId);
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputManager
            public void addBlockedRating(String rating, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(rating);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(12, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addBlockedRating(rating, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputManager
            public void removeBlockedRating(String rating, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(rating);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(13, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeBlockedRating(rating, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputManager
            public void createSession(ITvInputClient client, String inputId, boolean isRecordingSession, int seq, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(client != null ? client.asBinder() : null);
                    _data.writeString(inputId);
                    _data.writeInt(isRecordingSession ? 1 : 0);
                    _data.writeInt(seq);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(14, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().createSession(client, inputId, isRecordingSession, seq, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputManager
            public void releaseSession(IBinder sessionToken, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sessionToken);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(15, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().releaseSession(sessionToken, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputManager
            public void setMainSession(IBinder sessionToken, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sessionToken);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(16, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setMainSession(sessionToken, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputManager
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
                    if (this.mRemote.transact(17, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setSurface(sessionToken, surface, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputManager
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
                    if (this.mRemote.transact(18, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().dispatchSurfaceChanged(sessionToken, format, width, height, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputManager
            public void setVolume(IBinder sessionToken, float volume, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sessionToken);
                    _data.writeFloat(volume);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(19, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setVolume(sessionToken, volume, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputManager
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
                    if (this.mRemote.transact(20, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().tune(sessionToken, channelUri, params, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputManager
            public void setCaptionEnabled(IBinder sessionToken, boolean enabled, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sessionToken);
                    _data.writeInt(enabled ? 1 : 0);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(21, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setCaptionEnabled(sessionToken, enabled, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputManager
            public void selectTrack(IBinder sessionToken, int type, String trackId, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sessionToken);
                    _data.writeInt(type);
                    _data.writeString(trackId);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(22, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().selectTrack(sessionToken, type, trackId, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputManager
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
                    if (this.mRemote.transact(23, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().sendAppPrivateCommand(sessionToken, action, data, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputManager
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
                    if (this.mRemote.transact(24, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().createOverlayView(sessionToken, windowToken, frame, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputManager
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
                    if (this.mRemote.transact(25, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().relayoutOverlayView(sessionToken, frame, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputManager
            public void removeOverlayView(IBinder sessionToken, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sessionToken);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(26, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeOverlayView(sessionToken, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputManager
            public void unblockContent(IBinder sessionToken, String unblockedRating, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sessionToken);
                    _data.writeString(unblockedRating);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(27, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unblockContent(sessionToken, unblockedRating, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputManager
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
                    if (this.mRemote.transact(28, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().timeShiftPlay(sessionToken, recordedProgramUri, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputManager
            public void timeShiftPause(IBinder sessionToken, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sessionToken);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(29, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().timeShiftPause(sessionToken, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputManager
            public void timeShiftResume(IBinder sessionToken, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sessionToken);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(30, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().timeShiftResume(sessionToken, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputManager
            public void timeShiftSeekTo(IBinder sessionToken, long timeMs, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sessionToken);
                    _data.writeLong(timeMs);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(31, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().timeShiftSeekTo(sessionToken, timeMs, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputManager
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
                    if (this.mRemote.transact(32, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().timeShiftSetPlaybackParams(sessionToken, params, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputManager
            public void timeShiftEnablePositionTracking(IBinder sessionToken, boolean enable, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sessionToken);
                    _data.writeInt(enable ? 1 : 0);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(33, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().timeShiftEnablePositionTracking(sessionToken, enable, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputManager
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
                    if (this.mRemote.transact(34, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().startRecording(sessionToken, programUri, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputManager
            public void stopRecording(IBinder sessionToken, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(sessionToken);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(35, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().stopRecording(sessionToken, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputManager
            public List<TvInputHardwareInfo> getHardwareList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(36, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHardwareList();
                    }
                    _reply.readException();
                    List<TvInputHardwareInfo> _result = _reply.createTypedArrayList(TvInputHardwareInfo.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputManager
            public ITvInputHardware acquireTvInputHardware(int deviceId, ITvInputHardwareCallback callback, TvInputInfo info, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(deviceId);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(37, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().acquireTvInputHardware(deviceId, callback, info, userId);
                    }
                    _reply.readException();
                    ITvInputHardware _result = ITvInputHardware.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputManager
            public void releaseTvInputHardware(int deviceId, ITvInputHardware hardware, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(deviceId);
                    _data.writeStrongBinder(hardware != null ? hardware.asBinder() : null);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(38, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().releaseTvInputHardware(deviceId, hardware, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputManager
            public List<TvStreamConfig> getAvailableTvStreamConfigList(String inputId, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(inputId);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(39, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAvailableTvStreamConfigList(inputId, userId);
                    }
                    _reply.readException();
                    List<TvStreamConfig> _result = _reply.createTypedArrayList(TvStreamConfig.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputManager
            public boolean captureFrame(String inputId, Surface surface, TvStreamConfig config, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(inputId);
                    boolean _result = true;
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
                    if (!this.mRemote.transact(40, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().captureFrame(inputId, surface, config, userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputManager
            public boolean isSingleSessionActive(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    boolean _result = false;
                    if (!this.mRemote.transact(41, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isSingleSessionActive(userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputManager
            public List<DvbDeviceInfo> getDvbDeviceList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(42, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDvbDeviceList();
                    }
                    _reply.readException();
                    List<DvbDeviceInfo> _result = _reply.createTypedArrayList(DvbDeviceInfo.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputManager
            public ParcelFileDescriptor openDvbDevice(DvbDeviceInfo info, int device) throws RemoteException {
                ParcelFileDescriptor _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(device);
                    if (!this.mRemote.transact(43, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().openDvbDevice(info, device);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ParcelFileDescriptor.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputManager
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
                    if (this.mRemote.transact(44, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().sendTvInputNotifyIntent(intent, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputManager
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
                    if (this.mRemote.transact(45, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().requestChannelBrowsable(channelUri, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ITvInputManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ITvInputManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
