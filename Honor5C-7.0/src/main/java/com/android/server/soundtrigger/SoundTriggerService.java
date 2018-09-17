package com.android.server.soundtrigger;

import android.content.Context;
import android.hardware.soundtrigger.IRecognitionStatusCallback;
import android.hardware.soundtrigger.SoundTrigger.GenericSoundModel;
import android.hardware.soundtrigger.SoundTrigger.KeyphraseSoundModel;
import android.hardware.soundtrigger.SoundTrigger.ModuleProperties;
import android.hardware.soundtrigger.SoundTrigger.RecognitionConfig;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.RemoteException;
import android.util.Slog;
import com.android.internal.app.ISoundTriggerService.Stub;
import com.android.server.SystemService;
import com.android.server.usb.UsbAudioDevice;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class SoundTriggerService extends SystemService {
    private static final boolean DEBUG = true;
    private static final String TAG = "SoundTriggerService";
    final Context mContext;
    private SoundTriggerDbHelper mDbHelper;
    private final LocalSoundTriggerService mLocalSoundTriggerService;
    private final SoundTriggerServiceStub mServiceStub;
    private SoundTriggerHelper mSoundTriggerHelper;

    public final class LocalSoundTriggerService extends SoundTriggerInternal {
        private final Context mContext;
        private SoundTriggerHelper mSoundTriggerHelper;

        LocalSoundTriggerService(Context context) {
            this.mContext = context;
        }

        synchronized void setSoundTriggerHelper(SoundTriggerHelper helper) {
            this.mSoundTriggerHelper = helper;
        }

        public int startRecognition(int keyphraseId, KeyphraseSoundModel soundModel, IRecognitionStatusCallback listener, RecognitionConfig recognitionConfig) {
            if (isInitialized()) {
                return this.mSoundTriggerHelper.startKeyphraseRecognition(keyphraseId, soundModel, listener, recognitionConfig);
            }
            return UsbAudioDevice.kAudioDeviceMeta_Alsa;
        }

        public synchronized int stopRecognition(int keyphraseId, IRecognitionStatusCallback listener) {
            if (!isInitialized()) {
                return UsbAudioDevice.kAudioDeviceMeta_Alsa;
            }
            return this.mSoundTriggerHelper.stopKeyphraseRecognition(keyphraseId, listener);
        }

        public ModuleProperties getModuleProperties() {
            if (isInitialized()) {
                return this.mSoundTriggerHelper.getModuleProperties();
            }
            return null;
        }

        public int unloadKeyphraseModel(int keyphraseId) {
            if (isInitialized()) {
                return this.mSoundTriggerHelper.unloadKeyphraseSoundModel(keyphraseId);
            }
            return UsbAudioDevice.kAudioDeviceMeta_Alsa;
        }

        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (isInitialized()) {
                this.mSoundTriggerHelper.dump(fd, pw, args);
            }
        }

        private synchronized boolean isInitialized() {
            if (this.mSoundTriggerHelper != null) {
                return SoundTriggerService.DEBUG;
            }
            Slog.e(SoundTriggerService.TAG, "SoundTriggerHelper not initialized.");
            return false;
        }
    }

    class SoundTriggerServiceStub extends Stub {
        SoundTriggerServiceStub() {
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            try {
                return super.onTransact(code, data, reply, flags);
            } catch (RuntimeException e) {
                if (!(e instanceof SecurityException)) {
                    Slog.wtf(SoundTriggerService.TAG, "SoundTriggerService Crash", e);
                }
                throw e;
            }
        }

        public int startRecognition(ParcelUuid parcelUuid, IRecognitionStatusCallback callback, RecognitionConfig config) {
            SoundTriggerService.this.enforceCallingPermission("android.permission.MANAGE_SOUND_TRIGGER");
            if (!SoundTriggerService.this.isInitialized()) {
                return UsbAudioDevice.kAudioDeviceMeta_Alsa;
            }
            Slog.i(SoundTriggerService.TAG, "startRecognition(): Uuid : " + parcelUuid);
            GenericSoundModel model = getSoundModel(parcelUuid);
            if (model != null) {
                return SoundTriggerService.this.mSoundTriggerHelper.startGenericRecognition(parcelUuid.getUuid(), model, callback, config);
            }
            Slog.e(SoundTriggerService.TAG, "Null model in database for id: " + parcelUuid);
            return UsbAudioDevice.kAudioDeviceMeta_Alsa;
        }

        public int stopRecognition(ParcelUuid parcelUuid, IRecognitionStatusCallback callback) {
            SoundTriggerService.this.enforceCallingPermission("android.permission.MANAGE_SOUND_TRIGGER");
            Slog.i(SoundTriggerService.TAG, "stopRecognition(): Uuid : " + parcelUuid);
            if (SoundTriggerService.this.isInitialized()) {
                return SoundTriggerService.this.mSoundTriggerHelper.stopGenericRecognition(parcelUuid.getUuid(), callback);
            }
            return UsbAudioDevice.kAudioDeviceMeta_Alsa;
        }

        public GenericSoundModel getSoundModel(ParcelUuid soundModelId) {
            SoundTriggerService.this.enforceCallingPermission("android.permission.MANAGE_SOUND_TRIGGER");
            Slog.i(SoundTriggerService.TAG, "getSoundModel(): id = " + soundModelId);
            return SoundTriggerService.this.mDbHelper.getGenericSoundModel(soundModelId.getUuid());
        }

        public void updateSoundModel(GenericSoundModel soundModel) {
            SoundTriggerService.this.enforceCallingPermission("android.permission.MANAGE_SOUND_TRIGGER");
            Slog.i(SoundTriggerService.TAG, "updateSoundModel(): model = " + soundModel);
            SoundTriggerService.this.mDbHelper.updateGenericSoundModel(soundModel);
        }

        public void deleteSoundModel(ParcelUuid soundModelId) {
            SoundTriggerService.this.enforceCallingPermission("android.permission.MANAGE_SOUND_TRIGGER");
            Slog.i(SoundTriggerService.TAG, "deleteSoundModel(): id = " + soundModelId);
            SoundTriggerService.this.mSoundTriggerHelper.unloadGenericSoundModel(soundModelId.getUuid());
            SoundTriggerService.this.mDbHelper.deleteGenericSoundModel(soundModelId.getUuid());
        }
    }

    public SoundTriggerService(Context context) {
        super(context);
        this.mContext = context;
        this.mServiceStub = new SoundTriggerServiceStub();
        this.mLocalSoundTriggerService = new LocalSoundTriggerService(context);
    }

    public void onStart() {
        publishBinderService("soundtrigger", this.mServiceStub);
        publishLocalService(SoundTriggerInternal.class, this.mLocalSoundTriggerService);
    }

    public void onBootPhase(int phase) {
        if (SystemService.PHASE_SYSTEM_SERVICES_READY == phase) {
            initSoundTriggerHelper();
            this.mLocalSoundTriggerService.setSoundTriggerHelper(this.mSoundTriggerHelper);
        } else if (NetdResponseCode.InterfaceChange == phase) {
            this.mDbHelper = new SoundTriggerDbHelper(this.mContext);
        }
    }

    public void onStartUser(int userHandle) {
    }

    public void onSwitchUser(int userHandle) {
    }

    private synchronized void initSoundTriggerHelper() {
        if (this.mSoundTriggerHelper == null) {
            this.mSoundTriggerHelper = new SoundTriggerHelper(this.mContext);
        }
    }

    private synchronized boolean isInitialized() {
        if (this.mSoundTriggerHelper != null) {
            return DEBUG;
        }
        Slog.e(TAG, "SoundTriggerHelper not initialized.");
        return false;
    }

    private void enforceCallingPermission(String permission) {
        if (this.mContext.checkCallingOrSelfPermission(permission) != 0) {
            throw new SecurityException("Caller does not hold the permission " + permission);
        }
    }
}
