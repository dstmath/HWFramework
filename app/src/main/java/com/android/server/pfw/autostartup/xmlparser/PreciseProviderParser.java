package com.android.server.pfw.autostartup.xmlparser;

import com.android.server.pfw.autostartup.comm.XmlConst.PreciseIgnore;

class PreciseProviderParser extends AbsPreciseParser {
    PreciseProviderParser() {
    }

    protected int getPreciseType() {
        return 0;
    }

    protected String getXmlSubElementKey() {
        return PreciseIgnore.SERVICE_CLAZZ_CALLER_ELEMENT_KEY;
    }
}
