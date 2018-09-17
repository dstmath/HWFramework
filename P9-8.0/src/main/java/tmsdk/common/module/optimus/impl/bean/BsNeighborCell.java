package tmsdk.common.module.optimus.impl.bean;

public class BsNeighborCell {
    public short bsss;
    public int cid;
    public int lac;
    public short networkType;

    public String toString() {
        return "BsNeighborCell [networkType=" + this.networkType + ", cid=" + this.cid + ", lac=" + this.lac + ", bsss=" + this.bsss + "]";
    }
}
