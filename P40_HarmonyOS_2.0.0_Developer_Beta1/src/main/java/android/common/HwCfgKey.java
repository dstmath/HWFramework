package android.common;

public class HwCfgKey {
    public String fvalue;
    public String ifkey;
    public String iskey;
    public String itkey;
    public String key;
    public String rkey;
    public int slotid;
    public String svalue;
    public String tvalue;

    public HwCfgKey(String key2, String ifkey2, String iskey2, String itkey2, String rkey2, String fvalue2, String svalue2, String tvalue2, int slotid2) {
        this.key = key2;
        this.ifkey = ifkey2;
        this.iskey = iskey2;
        this.itkey = itkey2;
        this.rkey = rkey2;
        this.fvalue = fvalue2;
        this.svalue = svalue2;
        this.tvalue = tvalue2;
        this.slotid = slotid2;
    }
}
