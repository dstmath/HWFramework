package tmsdk.common.module.numbermarker;

import tmsdkobf.cc;

/* compiled from: Unknown */
public class NumQueryRet {
    public static final int PROP_Tag = 1;
    public static final int PROP_Tag_Yellow = 3;
    public static final int PROP_Yellow = 2;
    public static final int USED_FOR_Called = 18;
    public static final int USED_FOR_Calling = 17;
    public static final int USED_FOR_Common = 16;
    public String name;
    public String number;
    public int property;
    public int tagCount;
    public int tagType;
    public int usedFor;
    public String warning;

    public NumQueryRet() {
        this.property = -1;
        this.number = "";
        this.name = "";
        this.tagType = 0;
        this.tagCount = 0;
        this.warning = "";
        this.usedFor = -1;
    }

    protected void a(cc ccVar) {
        if (ccVar != null) {
            this.property = -1;
            if (ccVar.eG == 0) {
                this.property = PROP_Tag;
            } else if (ccVar.eG == PROP_Tag) {
                this.property = PROP_Yellow;
            } else if (ccVar.eG == PROP_Yellow) {
                this.property = PROP_Tag_Yellow;
            }
            this.number = ccVar.ej;
            this.name = ccVar.eC;
            this.tagType = ccVar.tagType;
            this.tagCount = ccVar.tagCount;
            this.warning = ccVar.eJ;
            this.usedFor = -1;
            if (ccVar.eH == 0) {
                this.usedFor = USED_FOR_Common;
            } else if (ccVar.eH == PROP_Tag) {
                this.usedFor = USED_FOR_Calling;
            } else if (ccVar.eH == PROP_Yellow) {
                this.usedFor = USED_FOR_Called;
            }
        }
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        if (this.property == PROP_Tag) {
            stringBuilder.append("\u6807\u8bb0\n");
        } else if (this.property == PROP_Yellow) {
            stringBuilder.append("\u9ec4\u9875\n");
        } else if (this.property == PROP_Tag_Yellow) {
            stringBuilder.append("\u6807\u8bb0\u9ec4\u9875\n");
        }
        stringBuilder.append("\u53f7\u7801:[" + this.number + "]\n");
        stringBuilder.append("\u540d\u79f0:[" + this.name + "]\n");
        stringBuilder.append("\u6807\u8bb0\u7c7b\u578b:[" + this.tagType + "]\n");
        stringBuilder.append("\u6807\u8bb0\u6570\u91cf:[" + this.tagCount + "]\n");
        stringBuilder.append("\u8b66\u544a\u4fe1\u606f:[" + this.warning + "]\n");
        if (this.usedFor == USED_FOR_Common) {
            stringBuilder.append("\u901a\u7528\n");
        } else if (this.usedFor == USED_FOR_Calling) {
            stringBuilder.append("\u4e3b\u53eb\n");
        } else if (this.usedFor == USED_FOR_Called) {
            stringBuilder.append("\u88ab\u53eb\n");
        }
        return stringBuilder.toString();
    }
}
