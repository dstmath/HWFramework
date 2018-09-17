package com.google.android.maps;

import android.graphics.drawable.Drawable;

public class OverlayItem {
    public static final int ITEM_STATE_FOCUSED_MASK = 4;
    public static final int ITEM_STATE_PRESSED_MASK = 1;
    public static final int ITEM_STATE_SELECTED_MASK = 2;
    private static final int[][] ITEM_STATE_TO_STATE_SET = null;
    protected Drawable mMarker;
    protected final GeoPoint mPoint;
    protected final String mSnippet;
    protected final String mTitle;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.google.android.maps.OverlayItem.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.google.android.maps.OverlayItem.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.maps.OverlayItem.<clinit>():void");
    }

    public OverlayItem(GeoPoint point, String title, String snippet) {
        this.mMarker = null;
        this.mPoint = point;
        this.mTitle = title;
        this.mSnippet = snippet;
    }

    public void setMarker(Drawable marker) {
        this.mMarker = marker;
    }

    public Drawable getMarker(int stateBitset) {
        if (this.mMarker != null) {
            setState(this.mMarker, stateBitset);
        }
        return this.mMarker;
    }

    public static void setState(Drawable drawable, int stateBitset) {
        drawable.setState(ITEM_STATE_TO_STATE_SET[stateBitset]);
    }

    public String getTitle() {
        return this.mTitle;
    }

    public String getSnippet() {
        return this.mSnippet;
    }

    public GeoPoint getPoint() {
        return this.mPoint;
    }

    public String routableAddress() {
        StringBuilder sb = new StringBuilder();
        sb.append(((float) this.mPoint.getLatitudeE6()) / 1000000.0f);
        sb.append(", ");
        sb.append(((float) this.mPoint.getLongitudeE6()) / 1000000.0f);
        return sb.toString();
    }
}
