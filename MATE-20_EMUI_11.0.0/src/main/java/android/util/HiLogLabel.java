package android.util;

public final class HiLogLabel {
    public int domain;
    public String tag;
    public int type;

    public HiLogLabel(int type2, int domain2, String tag2) {
        this.type = type2;
        this.domain = domain2;
        this.tag = tag2;
    }
}
