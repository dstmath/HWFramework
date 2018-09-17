package com.android.server.pfw.autostartup.xmlparser;

import com.android.server.pfw.autostartup.comm.PreciseComponent;
import com.android.server.pfw.autostartup.comm.XmlConst.PreciseIgnore;
import com.android.server.pfw.log.HwPFWLogger;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

abstract class AbsPreciseParser implements IStartupParser<PreciseComponent> {
    private static final String TAG = "AbsPreciseParser";

    protected abstract int getPreciseType();

    protected abstract String getXmlSubElementKey();

    AbsPreciseParser() {
    }

    public PreciseComponent parseDOMElement(Element ele) {
        PreciseComponent result = new PreciseComponent(getPreciseType(), ele.getAttribute(MemoryConstant.MEM_POLICY_ACTIONNAME), parseScope(ele.getAttribute(PreciseIgnore.COMP_COMM_SCOPE_ATTR)), parseScreenStatus(ele.getAttribute(PreciseIgnore.COMP_COMM_SCREEN_ATTR)));
        parseRelatedPackages(ele, result);
        if (result.valid()) {
            return result;
        }
        HwPFWLogger.e(TAG, "parseDOMElement invalid result: " + result);
        return null;
    }

    private int parseScope(String value) {
        if (PreciseIgnore.COMP_SCREEN_ALL_VALUE.equals(value)) {
            return 0;
        }
        if (PreciseIgnore.COMP_SCOPE_INDIVIDUAL_VALUE.equals(value)) {
            return 1;
        }
        return -1;
    }

    private int parseScreenStatus(String value) {
        if (PreciseIgnore.COMP_SCREEN_ALL_VALUE.equals(value)) {
            return 0;
        }
        if (PreciseIgnore.COMP_SCREEN_ON_VALUE_.equals(value)) {
            return 1;
        }
        return -1;
    }

    private void parseRelatedPackages(Element ele, PreciseComponent result) {
        NodeList nodeList = ele.getElementsByTagName(getXmlSubElementKey());
        for (int i = 0; i < nodeList.getLength(); i++) {
            result.addRelatedPkg(((Element) nodeList.item(i)).getAttribute(PreciseIgnore.COMP_COMM_RELATED_PACKAGE_ATTR_KEY));
        }
    }
}
