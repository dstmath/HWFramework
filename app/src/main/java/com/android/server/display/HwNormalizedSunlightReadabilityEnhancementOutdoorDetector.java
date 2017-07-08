package com.android.server.display;

import android.util.Slog;
import com.android.server.am.HwActivityManagerService;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.security.trustcircle.IOTController;

public class HwNormalizedSunlightReadabilityEnhancementOutdoorDetector extends HwSmartBackLightOutdoorNormalizedDetector {
    private static String TAG;
    private float mAmbientLuxRealtime;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.display.HwNormalizedSunlightReadabilityEnhancementOutdoorDetector.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.display.HwNormalizedSunlightReadabilityEnhancementOutdoorDetector.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.display.HwNormalizedSunlightReadabilityEnhancementOutdoorDetector.<clinit>():void");
    }

    public HwNormalizedSunlightReadabilityEnhancementOutdoorDetector(String configFilePath) {
        super(configFilePath);
    }

    protected void loadDefaultConfig() {
        super.loadDefaultConfig();
        this.mLightSensorRateMillis = 16;
        this.mInDoorThreshold = HwActivityManagerService.SERVICE_ADJ;
        this.mOutDoorThreshold = IOTController.TYPE_MASTER;
    }

    protected void updateAmbientLux(long time) {
        boolean updateFlag = true;
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
        if (nextBrightenTransition > time && nextDarkenTransition > time) {
            updateFlag = this.mInoutFlag;
        }
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
