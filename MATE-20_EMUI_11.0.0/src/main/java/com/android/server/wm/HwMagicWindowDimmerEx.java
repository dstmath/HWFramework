package com.android.server.wm;

public class HwMagicWindowDimmerEx {
    private HwMagicWindowDimmer mHwMagicWindowDimmer;
    private WindowStateEx mWinStateEx;

    public HwMagicWindowDimmerEx(WindowStateEx windowStateEx) {
        this.mWinStateEx = windowStateEx;
        this.mHwMagicWindowDimmer = new HwMagicWindowDimmer(windowStateEx.getWindowState());
    }

    public void setHwWindowStateExMwDimmer() {
        DimmerEx dimmerEx = new DimmerEx();
        dimmerEx.setDimmer(this.mHwMagicWindowDimmer);
        this.mWinStateEx.setMwDimmer(dimmerEx);
    }

    public void destoryMagicWindowDimmer() {
        if (this.mWinStateEx.getMwDimmer() != null && (this.mWinStateEx.getMwDimmer().getDimmer() instanceof HwMagicWindowDimmer)) {
            this.mHwMagicWindowDimmer.destroyDimmer(this.mWinStateEx.getPendingTransaction());
        }
    }
}
