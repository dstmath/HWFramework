package com.huawei.android.media.soundtrigger;

import android.content.Context;
import android.media.soundtrigger.SoundTriggerManager;
import android.util.Log;
import java.util.UUID;

public class SoundTriggerManagerEx {
    private static final String TAG = "SoundTriggerManagerEx";
    private static volatile SoundTriggerManagerEx instance;
    private SoundTriggerManager mSoundTriggerManager;

    private SoundTriggerManagerEx(Context context) {
        Object object = context.getApplicationContext() != null ? context.getApplicationContext().getSystemService("soundtrigger") : null;
        if (object != null && (object instanceof SoundTriggerManager)) {
            this.mSoundTriggerManager = (SoundTriggerManager) object;
        }
    }

    public static SoundTriggerManagerEx getInstance(Context context) {
        if (instance == null) {
            instance = new SoundTriggerManagerEx(context);
        }
        return instance;
    }

    public static class Model {
        private SoundTriggerManager.Model mModel;

        private Model(UUID uid, UUID vid, byte[] data) {
            this.mModel = SoundTriggerManager.Model.create(uid, vid, data);
        }

        private Model(SoundTriggerManager.Model model) {
            this.mModel = model;
        }

        public static Model create(UUID modelUuid, UUID vendorUuid, byte[] data) {
            return new Model(modelUuid, vendorUuid, data);
        }
    }

    public void updateModel(Model model) {
        Log.i(TAG, "updateModel");
        this.mSoundTriggerManager.updateModel(model.mModel);
    }

    public Model getModel(UUID soundModelId) {
        Log.i(TAG, "getModel");
        return new Model(this.mSoundTriggerManager.getModel(soundModelId));
    }

    public void deleteModel(UUID soundModelId) {
        Log.i(TAG, "deleteModel");
        this.mSoundTriggerManager.deleteModel(soundModelId);
    }
}
