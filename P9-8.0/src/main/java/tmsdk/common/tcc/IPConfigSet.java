package tmsdk.common.tcc;

public class IPConfigSet {
    public String iDefaultIPNums;
    public String iIPnum;
    public String iMSelfdefIPNum;
    public String iMyNumLocation;
    public String iNotUseIPAreas;
    public String iNotUseIPNums;
    public String iSelfdefIPNum;
    public int iUseIPnumStyle;

    public IPConfigSet(int i, String str, String str2, String str3, String str4, String str5, String str6, String str7) {
        this.iUseIPnumStyle = i;
        this.iIPnum = str;
        this.iMyNumLocation = str2;
        this.iNotUseIPAreas = str3;
        this.iNotUseIPNums = str4;
        this.iDefaultIPNums = str5;
        this.iSelfdefIPNum = str6;
        this.iMSelfdefIPNum = str7;
    }
}
