package tmsdkobf;

import java.util.List;
import java.util.Map;

/* compiled from: Unknown */
public class gi {
    public String om;
    public Map<String, String> on;
    public Map<String, byte[]> oo;
    public List<gv> op;

    public gv ax(int i) {
        for (gv gvVar : this.op) {
            if (gvVar.mID == i) {
                return gvVar;
            }
        }
        return null;
    }
}
