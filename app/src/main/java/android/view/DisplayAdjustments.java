package android.view;

import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import java.util.Objects;

public class DisplayAdjustments {
    public static final DisplayAdjustments DEFAULT_DISPLAY_ADJUSTMENTS = null;
    private volatile CompatibilityInfo mCompatInfo;
    private Configuration mConfiguration;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.DisplayAdjustments.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.DisplayAdjustments.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.view.DisplayAdjustments.<clinit>():void");
    }

    public DisplayAdjustments() {
        this.mCompatInfo = CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO;
        this.mConfiguration = Configuration.EMPTY;
    }

    public DisplayAdjustments(Configuration configuration) {
        this.mCompatInfo = CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO;
        this.mConfiguration = Configuration.EMPTY;
        this.mConfiguration = configuration;
    }

    public DisplayAdjustments(DisplayAdjustments daj) {
        this.mCompatInfo = CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO;
        this.mConfiguration = Configuration.EMPTY;
        setCompatibilityInfo(daj.mCompatInfo);
        this.mConfiguration = daj.mConfiguration;
    }

    public void setCompatibilityInfo(CompatibilityInfo compatInfo) {
        if (this == DEFAULT_DISPLAY_ADJUSTMENTS) {
            throw new IllegalArgumentException("setCompatbilityInfo: Cannot modify DEFAULT_DISPLAY_ADJUSTMENTS");
        } else if (compatInfo == null || (!compatInfo.isScalingRequired() && compatInfo.supportsScreen())) {
            this.mCompatInfo = CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO;
        } else {
            this.mCompatInfo = compatInfo;
        }
    }

    public CompatibilityInfo getCompatibilityInfo() {
        return this.mCompatInfo;
    }

    public void setConfiguration(Configuration configuration) {
        if (this == DEFAULT_DISPLAY_ADJUSTMENTS) {
            throw new IllegalArgumentException("setConfiguration: Cannot modify DEFAULT_DISPLAY_ADJUSTMENTS");
        }
        if (configuration == null) {
            configuration = Configuration.EMPTY;
        }
        this.mConfiguration = configuration;
    }

    public Configuration getConfiguration() {
        return this.mConfiguration;
    }

    public int hashCode() {
        return ((Objects.hashCode(this.mCompatInfo) + 527) * 31) + Objects.hashCode(this.mConfiguration);
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof DisplayAdjustments)) {
            return false;
        }
        DisplayAdjustments daj = (DisplayAdjustments) o;
        if (Objects.equals(daj.mCompatInfo, this.mCompatInfo)) {
            z = Objects.equals(daj.mConfiguration, this.mConfiguration);
        }
        return z;
    }
}
