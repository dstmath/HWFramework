package com.android.location.provider;

import android.location.FusedBatchOptions;

public class GmsFusedBatchOptions {
    private FusedBatchOptions mOptions;

    public static final class BatchFlags {
        public static int CALLBACK_ON_LOCATION_FIX;
        public static int WAKEUP_ON_FIFO_FULL;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.location.provider.GmsFusedBatchOptions.BatchFlags.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.location.provider.GmsFusedBatchOptions.BatchFlags.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.location.provider.GmsFusedBatchOptions.BatchFlags.<clinit>():void");
        }
    }

    public static final class SourceTechnologies {
        public static int BLUETOOTH;
        public static int CELL;
        public static int GNSS;
        public static int SENSORS;
        public static int WIFI;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.location.provider.GmsFusedBatchOptions.SourceTechnologies.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.location.provider.GmsFusedBatchOptions.SourceTechnologies.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.location.provider.GmsFusedBatchOptions.SourceTechnologies.<clinit>():void");
        }

        public SourceTechnologies() {
        }
    }

    public GmsFusedBatchOptions() {
        this.mOptions = new FusedBatchOptions();
    }

    public void setMaxPowerAllocationInMW(double value) {
        this.mOptions.setMaxPowerAllocationInMW(value);
    }

    public double getMaxPowerAllocationInMW() {
        return this.mOptions.getMaxPowerAllocationInMW();
    }

    public void setPeriodInNS(long value) {
        this.mOptions.setPeriodInNS(value);
    }

    public long getPeriodInNS() {
        return this.mOptions.getPeriodInNS();
    }

    public void setSmallestDisplacementMeters(float value) {
        this.mOptions.setSmallestDisplacementMeters(value);
    }

    public float getSmallestDisplacementMeters() {
        return this.mOptions.getSmallestDisplacementMeters();
    }

    public void setSourceToUse(int source) {
        this.mOptions.setSourceToUse(source);
    }

    public void resetSourceToUse(int source) {
        this.mOptions.resetSourceToUse(source);
    }

    public boolean isSourceToUseSet(int source) {
        return this.mOptions.isSourceToUseSet(source);
    }

    public int getSourcesToUse() {
        return this.mOptions.getSourcesToUse();
    }

    public void setFlag(int flag) {
        this.mOptions.setFlag(flag);
    }

    public void resetFlag(int flag) {
        this.mOptions.resetFlag(flag);
    }

    public boolean isFlagSet(int flag) {
        return this.mOptions.isFlagSet(flag);
    }

    public int getFlags() {
        return this.mOptions.getFlags();
    }

    public FusedBatchOptions getParcelableOptions() {
        return this.mOptions;
    }
}
