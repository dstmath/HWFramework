package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;

public final class ci extends JceStruct {
    public int uiRuleType = 0;
    public int uiRuleTypeId = 0;

    public JceStruct newInit() {
        return new ci();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.uiRuleType = jceInputStream.read(this.uiRuleType, 0, true);
        this.uiRuleTypeId = jceInputStream.read(this.uiRuleTypeId, 1, true);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.uiRuleType, 0);
        jceOutputStream.write(this.uiRuleTypeId, 1);
    }
}
