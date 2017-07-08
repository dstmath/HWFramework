package com.android.server.pfw.autostartup.xmlparser;

import com.android.server.pfw.autostartup.comm.XmlConst.PreciseIgnore;

class PreciseReceiverParser extends AbsPreciseParser {
    PreciseReceiverParser() {
    }

    protected int getPreciseType() {
        return 1;
    }

    protected String getXmlSubElementKey() {
        return PreciseIgnore.RECEIVER_ACTION_RECEIVER_ELEMENT_KEY;
    }
}
