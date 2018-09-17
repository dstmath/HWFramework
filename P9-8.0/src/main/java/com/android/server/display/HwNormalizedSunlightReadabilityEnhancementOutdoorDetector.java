package com.android.server.display;

import android.util.Slog;
import com.android.server.display.HwSunlightReadabilityEnhancementXmlLoader.Data;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;

public class HwNormalizedSunlightReadabilityEnhancementOutdoorDetector extends HwSmartBackLightOutdoorNormalizedDetector {
    private static String TAG = "HwNormalizedSunlightReadabilityEnhancementOutdoorDetector";
    private float mAmbientLuxRealtime;

    protected void getConfig() {
        Data data = HwSunlightReadabilityEnhancementXmlLoader.getData();
        this.mInDoorThreshold = data.inDoorThreshold;
        this.mOutDoorThreshold = data.outDoorThreshold;
        this.mBrighenDebounceTime = data.brighenDebounceTime;
        this.mDarkenDebounceTime = data.darkenDebounceTime;
        this.mBrightenLinePointsList = data.brightenLinePoints;
        this.mDarkLinePointsList = data.darkenLinePoints;
    }

    protected void updateAmbientLux(long time) {
        float value = calculateAmbientLuxForNewPolicy(time);
        if (this.mAmbientLightRingBuffer.size() == 1) {
            Slog.i(TAG, "[effect] fist sensor lux and filteredlux=" + value + ",time=" + time);
        }
        this.mAmbientLightRingBufferFilter.push(time, value);
        this.mAmbientLightRingBufferFilter.prune(time - MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
        this.mAmbientLuxRealtime = value;
        long nextBrightenTransition = nextAmbientLightBrighteningTransitionForNewPolicy(time);
        long nextDarkenTransition = nextAmbientLightDarkeningTransitionForNewPolicy(time);
        this.mAmChangeFlagSBL = false;
        boolean updateFlag = (nextBrightenTransition <= time || nextDarkenTransition <= time) ? true : this.mInoutFlag;
        if (updateFlag) {
            updateParaForSBL(value);
        }
    }

    public float getAmbientLux() {
        return this.mAmbientLuxRealtime;
    }

    public float getAmbientLuxForSRE() {
        return this.mAmbientLux;
    }

    public int getAmbientThresholdForSRE() {
        return this.mInDoorThreshold;
    }

    public boolean getLuxChangedFlagForSRE() {
        return super.getLuxChangedFlagForSBL();
    }

    public int getIndoorOutdoorFlagForSRE() {
        return super.getIndoorOutdoorFlagForSBL();
    }
}
