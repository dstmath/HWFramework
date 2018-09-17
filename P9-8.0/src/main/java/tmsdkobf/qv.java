package tmsdkobf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import tmsdk.common.utils.m;

public class qv {
    public String MB;
    public qu OA;
    public List<qu> Ot;
    public Map<String, String> Oz;

    public void C(List<qu> list) {
        if (this.Ot == null) {
            this.Ot = new ArrayList();
        }
        this.Ot.addAll(list);
    }

    public qu bY(int i) {
        if (i != 0) {
            for (qu quVar : this.Ot) {
                if (quVar.mID == i) {
                    return quVar;
                }
            }
            return null;
        }
        if (this.OA == null) {
            this.OA = new qu();
            this.OA.MB = this.MB;
            this.OA.Ow = "4";
            this.OA.Ot = new ArrayList();
            this.OA.Ot.add("/");
            this.OA.Oy = true;
            this.OA.mDescription = m.cF("deep_clean_other_rubbish");
            this.OA.Nt = 1;
        }
        return this.OA;
    }
}
