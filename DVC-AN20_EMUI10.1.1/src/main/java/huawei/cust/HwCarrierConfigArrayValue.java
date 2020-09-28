package huawei.cust;

import java.util.ArrayList;
import java.util.List;

public class HwCarrierConfigArrayValue extends HWCarrierConfigComplexValue<List> {
    public HwCarrierConfigArrayValue() {
        super(new ArrayList());
    }

    @Override // huawei.cust.HWCarrierConfigComplexValue
    public void addData(String key, Object val) {
        ((List) getData()).add(val);
    }
}
