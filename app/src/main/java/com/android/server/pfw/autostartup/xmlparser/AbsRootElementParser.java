package com.android.server.pfw.autostartup.xmlparser;

import com.android.server.pfw.autostartup.comm.DefaultXmlParsedResult;
import com.android.server.pfw.autostartup.comm.PreciseComponent;
import com.android.server.pfw.autostartup.comm.XmlConst.ControlScope;
import com.android.server.pfw.autostartup.comm.XmlConst.PreciseIgnore;
import com.android.server.pfw.log.HwPFWLogger;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

abstract class AbsRootElementParser implements IStartupParser<DefaultXmlParsedResult> {
    private static final String TAG = "AbsRootElementParser";

    abstract boolean needParsePreciseNode();

    AbsRootElementParser() {
    }

    public DefaultXmlParsedResult parseDOMElement(Element ele) {
        int i;
        DefaultXmlParsedResult result = new DefaultXmlParsedResult();
        NodeList scopeNodes = ele.getElementsByTagName(ControlScope.CONTROL_SCOPE_ELEMENT_KEY);
        for (i = 0; i < scopeNodes.getLength(); i++) {
            parseControlScopeNode((Element) scopeNodes.item(i), result);
        }
        if (needParsePreciseNode()) {
            NodeList preciseNodes = ele.getElementsByTagName(PreciseIgnore.PRECISE_IGNORE_ELEMENT_KEY);
            for (i = 0; i < preciseNodes.getLength(); i++) {
                parsePreciseComponents((Element) preciseNodes.item(i), result);
            }
        }
        HwPFWLogger.d(TAG, "parseDOMElement " + result);
        return result;
    }

    private void parseControlScopeNode(Element ele, DefaultXmlParsedResult result) {
        result.addSystemBlackPkgs(parseScopePackageList(ele, ControlScope.SYSTEM_BLACK_LIST_ELEMENT_KEY));
        result.addThirdWhitePkgs(parseScopePackageList(ele, ControlScope.THIRD_PARTY_WHITE_LIST_ELEMENT_KEY));
    }

    private List<String> parseScopePackageList(Element ele, String tag) {
        List<String> result = new ArrayList();
        NodeList scopeTypeNodes = ele.getElementsByTagName(tag);
        PackageListParser pkgParser = new PackageListParser();
        for (int i = 0; i < scopeTypeNodes.getLength(); i++) {
            result.addAll(pkgParser.parseDOMElement((Element) scopeTypeNodes.item(i)));
        }
        return result;
    }

    private void parsePreciseComponents(Element ele, DefaultXmlParsedResult result) {
        parsePreciseComponentGroup(ele, result, PreciseIgnore.PROVIDERS_ELEMENT_KEY, PreciseIgnore.PROVIDER_AUTH_ELEMENT_KEY, new PreciseProviderParser());
        parsePreciseComponentGroup(ele, result, PreciseIgnore.RECEIVERS_ELEMENT_KEY, PreciseIgnore.RECEIVER_ACTION_ELEMENT_KEY, new PreciseReceiverParser());
        parsePreciseComponentGroup(ele, result, PreciseIgnore.SERVICES_ELEMENT_KEY, PreciseIgnore.SERVICE_CLAZZ_ELEMENT_KEY, new PreciseServiceParser());
    }

    private void parsePreciseComponentGroup(Element ele, DefaultXmlParsedResult result, String groupTag, String instanceTag, AbsPreciseParser parser) {
        NodeList groupNodes = ele.getElementsByTagName(groupTag);
        for (int i = 0; i < groupNodes.getLength(); i++) {
            parseInstance((Element) groupNodes.item(i), result, instanceTag, parser);
        }
    }

    private void parseInstance(Element ele, DefaultXmlParsedResult result, String tag, AbsPreciseParser parser) {
        NodeList insNode = ele.getElementsByTagName(tag);
        for (int i = 0; i < insNode.getLength(); i++) {
            PreciseComponent compResult = parser.parseDOMElement((Element) insNode.item(i));
            if (compResult != null) {
                HwPFWLogger.d(TAG, "parseInstance add one precise component of " + ele.getTagName() + "-" + tag);
                result.addPreciseComponent(compResult);
            }
        }
    }
}
