package com.huawei.cust;

import huawei.cust.HwCarrierConfigXmlParse;
import java.io.File;
import java.util.Map;

public class HwCarrierConfigXmlParseEx {
    private HwCarrierConfigXmlParseEx() {
    }

    public static Map parseFile(File file) {
        return HwCarrierConfigXmlParse.parseFile(file);
    }
}
