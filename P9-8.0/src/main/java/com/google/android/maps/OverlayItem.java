package com.google.android.maps;

import android.graphics.drawable.Drawable;

public class OverlayItem {
    public static final int ITEM_STATE_FOCUSED_MASK = 4;
    public static final int ITEM_STATE_PRESSED_MASK = 1;
    public static final int ITEM_STATE_SELECTED_MASK = 2;
    private static final int[][] ITEM_STATE_TO_STATE_SET;
    protected Drawable mMarker = null;
    protected final GeoPoint mPoint;
    protected final String mSnippet;
    protected final String mTitle;

    static {
        r0 = new int[8][];
        r0[0] = new int[]{-attr.state_focused, -attr.state_selected, -attr.state_pressed};
        r0[1] = new int[]{-attr.state_focused, -attr.state_selected, attr.state_pressed};
        r0[2] = new int[]{-attr.state_focused, attr.state_selected, -attr.state_pressed};
        r0[3] = new int[]{-attr.state_focused, attr.state_selected, attr.state_pressed};
        r0[4] = new int[]{attr.state_focused, -attr.state_selected, -attr.state_pressed};
        r0[5] = new int[]{attr.state_focused, -attr.state_selected, attr.state_pressed};
        r0[6] = new int[]{attr.state_focused, attr.state_selected, -attr.state_pressed};
        r0[7] = new int[]{attr.state_focused, attr.state_selected, attr.state_pressed};
        ITEM_STATE_TO_STATE_SET = r0;
    }

    public OverlayItem(GeoPoint point, String title, String snippet) {
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
