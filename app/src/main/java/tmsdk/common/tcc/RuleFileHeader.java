package tmsdk.common.tcc;

/* compiled from: Unknown */
public class RuleFileHeader {
    public String md5str;
    public int time;
    public int version;

    public RuleFileHeader(int i, int i2, String str) {
        this.version = i;
        this.time = i2;
        this.md5str = str;
    }
}
