package tmsdk.common.module.numbermarker;

import tmsdkobf.cq;

public class NumQueryRet {
    public static final int PROP_Tag = 1;
    public static final int PROP_Tag_User = 4;
    public static final int PROP_Tag_Yellow = 3;
    public static final int PROP_Yellow = 2;
    public static final int USED_FOR_Called = 18;
    public static final int USED_FOR_Calling = 17;
    public static final int USED_FOR_Common = 16;
    public String eOperator = "";
    public String location = "";
    public String name = "";
    public String number = "";
    public int property = -1;
    public int tagCount = 0;
    public int tagType = 0;
    public int usedFor = -1;
    public String warning = "";

    protected void a(cq cqVar) {
        if (cqVar != null) {
            this.property = -1;
            if (cqVar.fB == 0) {
                this.property = 1;
            } else if (cqVar.fB == 1) {
                this.property = 2;
            } else if (cqVar.fB == 2) {
                this.property = 3;
            }
            this.number = cqVar.fe;
            this.name = cqVar.fx;
            this.tagType = cqVar.tagType;
            this.tagCount = cqVar.tagCount;
            this.warning = cqVar.fE;
            this.usedFor = -1;
            if (cqVar.fC == 0) {
                this.usedFor = 16;
            } else if (cqVar.fC == 1) {
                this.usedFor = 17;
            } else if (cqVar.fC == 2) {
                this.usedFor = 18;
            }
            this.location = cqVar.location;
            this.eOperator = cqVar.eOperator;
        }
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        if (this.property == 1) {
            stringBuilder.append("标记\n");
        } else if (this.property == 2) {
            stringBuilder.append("黄页\n");
        } else if (this.property == 3) {
            stringBuilder.append("标记黄页\n");
        }
        stringBuilder.append("号码:[" + this.number + "]\n");
        stringBuilder.append("名称:[" + this.name + "]\n");
        stringBuilder.append("标记类型:[" + this.tagType + "]\n");
        stringBuilder.append("标记数量:[" + this.tagCount + "]\n");
        stringBuilder.append("警告信息:[" + this.warning + "]\n");
        if (this.usedFor == 16) {
            stringBuilder.append("通用\n");
        } else if (this.usedFor == 17) {
            stringBuilder.append("主叫\n");
        } else if (this.usedFor == 18) {
            stringBuilder.append("被叫\n");
        }
        stringBuilder.append("归属地:[" + this.location + "]\n");
        stringBuilder.append("虚拟运营商:[" + this.eOperator + "]\n");
        return stringBuilder.toString();
    }
}
