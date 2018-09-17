package com.android.server.pfw.autostartup.xmlparser;

import com.android.server.pfw.autostartup.comm.XmlConst.ControlScope;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

class PackageListParser implements IStartupParser<List<String>> {
    PackageListParser() {
    }

    public List<String> parseDOMElement(Element ele) {
        List<String> result = new ArrayList();
        NodeList packageNodes = ele.getElementsByTagName(ControlScope.PACKAGE_ELEMENT_KEY);
        for (int j = 0; j < packageNodes.getLength(); j++) {
            result.add(((Element) packageNodes.item(j)).getAttribute(MemoryConstant.MEM_POLICY_ACTIONNAME));
        }
        return result;
    }
}
