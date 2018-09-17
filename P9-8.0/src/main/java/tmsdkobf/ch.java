package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;

public final class ch extends JceStruct {
    static ArrayList<ci> eU = new ArrayList();
    public float fScore = 0.0f;
    public String sRiskClassify = "";
    public String sRiskName = "";
    public String sRiskReach = "";
    public String sRiskUrl = "";
    public String sRule = "";
    public ArrayList<ci> stRuleTypeID = null;
    public int uiActionReason = 0;
    public int uiContentType = 0;
    public int uiFinalAction = 0;
    public int uiMatchCnt = 0;
    public int uiShowRiskName = 0;

    static {
        eU.add(new ci());
    }

    public JceStruct newInit() {
        return new ch();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.uiFinalAction = jceInputStream.read(this.uiFinalAction, 0, true);
        this.uiContentType = jceInputStream.read(this.uiContentType, 1, true);
        this.uiMatchCnt = jceInputStream.read(this.uiMatchCnt, 2, true);
        this.fScore = jceInputStream.read(this.fScore, 3, true);
        this.uiActionReason = jceInputStream.read(this.uiActionReason, 4, true);
        this.stRuleTypeID = (ArrayList) jceInputStream.read(eU, 5, false);
        this.sRule = jceInputStream.readString(6, false);
        this.uiShowRiskName = jceInputStream.read(this.uiShowRiskName, 7, false);
        this.sRiskClassify = jceInputStream.readString(8, false);
        this.sRiskUrl = jceInputStream.readString(9, false);
        this.sRiskName = jceInputStream.readString(10, false);
        this.sRiskReach = jceInputStream.readString(11, false);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.uiFinalAction, 0);
        jceOutputStream.write(this.uiContentType, 1);
        jceOutputStream.write(this.uiMatchCnt, 2);
        jceOutputStream.write(this.fScore, 3);
        jceOutputStream.write(this.uiActionReason, 4);
        if (this.stRuleTypeID != null) {
            jceOutputStream.write(this.stRuleTypeID, 5);
        }
        if (this.sRule != null) {
            jceOutputStream.write(this.sRule, 6);
        }
        if (this.uiShowRiskName != 0) {
            jceOutputStream.write(this.uiShowRiskName, 7);
        }
        if (this.sRiskClassify != null) {
            jceOutputStream.write(this.sRiskClassify, 8);
        }
        if (this.sRiskUrl != null) {
            jceOutputStream.write(this.sRiskUrl, 9);
        }
        if (this.sRiskName != null) {
            jceOutputStream.write(this.sRiskName, 10);
        }
        if (this.sRiskReach != null) {
            jceOutputStream.write(this.sRiskReach, 11);
        }
    }
}
