package com.huawei.internal.util;

import com.android.internal.util.FastXmlSerializer;
import com.huawei.annotation.HwSystemApi;
import org.xmlpull.v1.XmlSerializer;

@HwSystemApi
public class FastXmlSerializerEx {
    public static XmlSerializer getFastXmlSerializer() {
        return new FastXmlSerializer();
    }
}
