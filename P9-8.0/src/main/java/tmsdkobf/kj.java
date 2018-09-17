package tmsdkobf;

import java.io.Serializable;
import java.util.List;
import tmsdk.common.utils.f;

public class kj implements Serializable {
    private List<Long> tH;

    public boolean d(int i, long j) {
        if (nu.br(i) || nu.bs(i)) {
            return true;
        }
        boolean z = true;
        if (this.tH != null) {
            z = this.tH.contains(Long.valueOf(j));
            if (!z) {
                f.f("VipRule", "[shark_vip] request not allow currently, cmd: " + i + " ident: " + j + " mVipIdents: " + this.tH);
            }
        }
        return z;
    }

    public String toString() {
        return "mVipIdents|" + this.tH;
    }
}
