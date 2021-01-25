package android.net;

public final class TcpRepairWindow {
    public final int maxWindow;
    public final int rcvWnd;
    public final int rcvWndScale;
    public final int rcvWup;
    public final int sndWl1;
    public final int sndWnd;

    public TcpRepairWindow(int sndWl12, int sndWnd2, int maxWindow2, int rcvWnd2, int rcvWup2, int rcvWndScale2) {
        this.sndWl1 = sndWl12;
        this.sndWnd = sndWnd2;
        this.maxWindow = maxWindow2;
        this.rcvWnd = rcvWnd2;
        this.rcvWup = rcvWup2;
        this.rcvWndScale = rcvWndScale2;
    }
}
