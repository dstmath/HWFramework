package huawei.cust;

import java.util.HashMap;
import java.util.Map;

public class HwCarrierConfigDictValue extends HWCarrierConfigComplexValue<Map> {
    public HwCarrierConfigDictValue() {
        super(new HashMap());
    }

    @Override // huawei.cust.HWCarrierConfigComplexValue
    public void addData(String key, Object val) {
        ((Map) getData()).put(key, val);
    }
}
