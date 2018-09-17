package android_maps_conflict_avoidance.com.google.map;

public final class Zoom {
    private static int minZoomLevel;
    private static final Zoom[] zoomArray = null;
    private final int equatorPixels;
    private final int zoomLevel;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android_maps_conflict_avoidance.com.google.map.Zoom.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android_maps_conflict_avoidance.com.google.map.Zoom.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android_maps_conflict_avoidance.com.google.map.Zoom.<clinit>():void");
    }

    private Zoom(int zoomLevel, int equatorPixels) {
        this.zoomLevel = zoomLevel;
        this.equatorPixels = equatorPixels;
    }

    public int getPixelsForDistance(int meters) {
        return Math.max((int) ((((long) meters) * ((long) this.equatorPixels)) / 40076000), 1);
    }

    public static Zoom getZoom(int zoomLevel) {
        if (zoomLevel >= minZoomLevel && zoomLevel <= 22) {
            return zoomArray[zoomLevel - 1];
        }
        return null;
    }

    public int getZoomRatio(Zoom zoomIn) {
        return zoomIn.equatorPixels / this.equatorPixels;
    }

    public int getZoomLevel() {
        return this.zoomLevel;
    }

    public int getEquatorPixels() {
        return this.equatorPixels;
    }

    public boolean isMoreZoomedIn(Zoom zoom) {
        return this.zoomLevel > zoom.zoomLevel;
    }

    public Zoom getNextHigherZoom() {
        return getZoom(this.zoomLevel + 1);
    }

    public int changePixelsToTargetZoomlevel(int pixels, int zoomTargetLevel) {
        if (this.zoomLevel >= zoomTargetLevel) {
            return pixels >> (this.zoomLevel - zoomTargetLevel);
        }
        return pixels << (zoomTargetLevel - this.zoomLevel);
    }

    public Zoom getNextLowerZoom() {
        return getZoom(this.zoomLevel - 1);
    }

    public String toString() {
        return super.toString();
    }
}
