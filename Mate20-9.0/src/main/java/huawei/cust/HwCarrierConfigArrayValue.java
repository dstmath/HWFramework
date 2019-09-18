package huawei.cust;

import java.util.ArrayList;
import java.util.List;

public class HwCarrierConfigArrayValue extends HWCarrierConfigComplexValue<List> {
    public HwCarrierConfigArrayValue() {
        super(new ArrayList());
    }

    public void addData(String key, Object val) {
        ((List) getData()).add(val);
    }
}
