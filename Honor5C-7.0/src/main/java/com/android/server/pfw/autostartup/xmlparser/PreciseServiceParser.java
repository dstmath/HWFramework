package com.android.server.pfw.autostartup.xmlparser;

import com.android.server.pfw.autostartup.comm.XmlConst.PreciseIgnore;

class PreciseServiceParser extends AbsPreciseParser {
    PreciseServiceParser() {
    }

    protected int getPreciseType() {
        return 2;
    }

    protected String getXmlSubElementKey() {
        return PreciseIgnore.SERVICE_CLAZZ_CALLER_ELEMENT_KEY;
    }
}
