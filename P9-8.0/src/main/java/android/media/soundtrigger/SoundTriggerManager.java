package android.media.soundtrigger;

import android.content.Context;
import android.hardware.soundtrigger.SoundTrigger.GenericSoundModel;
import android.media.soundtrigger.SoundTriggerDetector.Callback;
import android.os.Handler;
import android.os.ParcelUuid;
import android.os.RemoteException;
import com.android.internal.app.ISoundTriggerService;
import java.util.HashMap;
import java.util.UUID;

public final class SoundTriggerManager {
    private static final boolean DBG = false;
    private static final String TAG = "SoundTriggerManager";
    private final Context mContext;
    private final HashMap<UUID, SoundTriggerDetector> mReceiverInstanceMap = new HashMap();
    private final ISoundTriggerService mSoundTriggerService;

    public static class Model {
        private GenericSoundModel mGenericSoundModel;

        Model(GenericSoundModel soundTriggerModel) {
            this.mGenericSoundModel = soundTriggerModel;
        }

        public static Model create(UUID modelUuid, UUID vendorUuid, byte[] data) {
            return new Model(new GenericSoundModel(modelUuid, vendorUuid, data));
        }

        public UUID getModelUuid() {
            return this.mGenericSoundModel.uuid;
        }

        public UUID getVendorUuid() {
            return this.mGenericSoundModel.vendorUuid;
        }

        public byte[] getModelData() {
            return this.mGenericSoundModel.data;
        }

        GenericSoundModel getGenericSoundModel() {
            return this.mGenericSoundModel;
        }
    }

    public SoundTriggerManager(Context context, ISoundTriggerService soundTriggerService) {
        this.mSoundTriggerService = soundTriggerService;
        this.mContext = context;
    }

    public void updateModel(Model model) {
        try {
            this.mSoundTriggerService.updateSoundModel(model.getGenericSoundModel());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public Model getModel(UUID soundModelId) {
        try {
            return new Model(this.mSoundTriggerService.getSoundModel(new ParcelUuid(soundModelId)));
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void deleteModel(UUID soundModelId) {
        try {
            this.mSoundTriggerService.deleteSoundModel(new ParcelUuid(soundModelId));
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public SoundTriggerDetector createSoundTriggerDetector(UUID soundModelId, Callback callback, Handler handler) {
        if (soundModelId == null) {
            return null;
        }
        SoundTriggerDetector oldInstance = (SoundTriggerDetector) this.mReceiverInstanceMap.get(soundModelId);
        SoundTriggerDetector newInstance = new SoundTriggerDetector(this.mSoundTriggerService, soundModelId, callback, handler);
        this.mReceiverInstanceMap.put(soundModelId, newInstance);
        return newInstance;
    }
}
