package ohos.com.sun.org.apache.xalan.internal.xslt;

import java.io.File;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import ohos.com.sun.org.apache.xalan.internal.utils.ObjectFactory;
import ohos.com.sun.org.apache.xalan.internal.utils.SecuritySupport;
import ohos.event.notification.NotificationRequest;
import ohos.global.icu.impl.locale.LanguageTag;
import ohos.org.w3c.dom.Document;
import ohos.org.w3c.dom.Element;
import ohos.org.w3c.dom.Node;
import ohos.org.xml.sax.Attributes;

public class EnvironmentCheck {
    public static final String CLASS_NOTPRESENT = "not-present";
    public static final String CLASS_PRESENT = "present-unknown-version";
    public static final String ERROR = "ERROR.";
    public static final String ERROR_FOUND = "At least one error was found!";
    public static final String FOUNDCLASSES = "foundclasses.";
    private static final Map<Long, String> JARVERSIONS;
    public static final String VERSION = "version.";
    public static final String WARNING = "WARNING.";
    public String[] jarNames = {"xalan.jar", "xalansamples.jar", "xalanj1compat.jar", "xalanservlet.jar", "serializer.jar", "xerces.jar", "xercesImpl.jar", "testxsl.jar", "crimson.jar", "lotusxsl.jar", "jaxp.jar", "parser.jar", "dom.jar", "sax.jar", "xml.jar", "xml-apis.jar", "xsltc.jar"};
    protected PrintWriter outWriter = new PrintWriter((OutputStream) System.out, true);

    public static void main(String[] strArr) {
        PrintWriter printWriter = new PrintWriter((OutputStream) System.out, true);
        int i = 0;
        while (i < strArr.length) {
            if ("-out".equalsIgnoreCase(strArr[i])) {
                i++;
                if (i < strArr.length) {
                    try {
                        printWriter = new PrintWriter(new FileWriter(strArr[i], true));
                    } catch (Exception e) {
                        PrintStream printStream = System.err;
                        printStream.println("# WARNING: -out " + strArr[i] + " threw " + e.toString());
                    }
                } else {
                    System.err.println("# WARNING: -out argument should have a filename, output sent to console");
                }
            }
            i++;
        }
        new EnvironmentCheck().checkEnvironment(printWriter);
    }

    public boolean checkEnvironment(PrintWriter printWriter) {
        if (printWriter != null) {
            this.outWriter = printWriter;
        }
        if (writeEnvironmentReport(getEnvironmentHash())) {
            logMsg("# WARNING: Potential problems found in your environment!");
            logMsg("#    Check any 'ERROR' items above against the Xalan FAQs");
            logMsg("#    to correct potential problems with your classes/jars");
            logMsg("#    http://xml.apache.org/xalan-j/faq.html");
            PrintWriter printWriter2 = this.outWriter;
            if (printWriter2 == null) {
                return false;
            }
            printWriter2.flush();
            return false;
        }
        logMsg("# YAHOO! Your environment seems to be OK.");
        PrintWriter printWriter3 = this.outWriter;
        if (printWriter3 == null) {
            return true;
        }
        printWriter3.flush();
        return true;
    }

    public Map<String, Object> getEnvironmentHash() {
        HashMap hashMap = new HashMap();
        checkJAXPVersion(hashMap);
        checkProcessorVersion(hashMap);
        checkParserVersion(hashMap);
        checkAntVersion(hashMap);
        if (!checkDOML3(hashMap)) {
            checkDOMVersion(hashMap);
        }
        checkSAXVersion(hashMap);
        checkSystemProperties(hashMap);
        return hashMap;
    }

    /* access modifiers changed from: protected */
    public boolean writeEnvironmentReport(Map<String, Object> map) {
        boolean z = false;
        if (map == null) {
            logMsg("# ERROR: writeEnvironmentReport called with null Map");
            return false;
        }
        logMsg("#---- BEGIN writeEnvironmentReport($Revision: 1.10 $): Useful stuff found: ----");
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            try {
                if (key.startsWith(FOUNDCLASSES)) {
                    z |= logFoundJars((ArrayList) entry.getValue(), key);
                } else {
                    if (key.startsWith(ERROR)) {
                        z = true;
                    }
                    logMsg(key + "=" + map.get(key));
                }
            } catch (Exception e) {
                logMsg("Reading-" + key + "= threw: " + e.toString());
            }
        }
        logMsg("#----- END writeEnvironmentReport: Useful properties found: -----");
        return z;
    }

    /* access modifiers changed from: protected */
    public boolean logFoundJars(List<Map> list, String str) {
        boolean z = false;
        if (list != null && list.size() >= 1) {
            logMsg("#---- BEGIN Listing XML-related jars in: " + str + " ----");
            for (Map map : list) {
                for (Map.Entry entry : map.entrySet()) {
                    String str2 = (String) entry.getKey();
                    try {
                        if (str2.startsWith(ERROR)) {
                            z = true;
                        }
                        logMsg(str2 + "=" + ((String) entry.getValue()));
                    } catch (Exception e) {
                        logMsg("Reading-" + str2 + "= threw: " + e.toString());
                        z = true;
                    }
                }
            }
            logMsg("#----- END Listing XML-related jars in: " + str + " -----");
        }
        return z;
    }

    public void appendEnvironmentReport(Node node, Document document, Map<String, Object> map) {
        Exception e;
        if (node != null && document != null) {
            try {
                Element createElement = document.createElement("EnvironmentCheck");
                createElement.setAttribute("version", "$Revision: 1.10 $");
                node.appendChild(createElement);
                String str = "ERROR";
                if (map == null) {
                    Element createElement2 = document.createElement(NotificationRequest.CLASSIFICATION_STATUS);
                    createElement2.setAttribute("result", str);
                    createElement2.appendChild(document.createTextNode("appendEnvironmentReport called with null Map!"));
                    createElement.appendChild(createElement2);
                    return;
                }
                boolean z = false;
                Element createElement3 = document.createElement("environment");
                createElement.appendChild(createElement3);
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    String key = entry.getKey();
                    try {
                        if (key.startsWith(FOUNDCLASSES)) {
                            try {
                                z |= appendFoundJars(createElement3, document, (List) entry.getValue(), key);
                            } catch (Exception e2) {
                                e = e2;
                                Element createElement4 = document.createElement("item");
                                createElement4.setAttribute("key", key);
                                createElement4.appendChild(document.createTextNode("ERROR. Reading " + key + " threw: " + e.toString()));
                                createElement3.appendChild(createElement4);
                                z = true;
                            }
                        } else {
                            if (key.startsWith(ERROR)) {
                                z = true;
                            }
                            Element createElement5 = document.createElement("item");
                            createElement5.setAttribute("key", key);
                            createElement5.appendChild(document.createTextNode((String) map.get(key)));
                            createElement3.appendChild(createElement5);
                        }
                    } catch (Exception e3) {
                        e = e3;
                        Element createElement42 = document.createElement("item");
                        createElement42.setAttribute("key", key);
                        createElement42.appendChild(document.createTextNode("ERROR. Reading " + key + " threw: " + e.toString()));
                        createElement3.appendChild(createElement42);
                        z = true;
                    }
                }
                Element createElement6 = document.createElement(NotificationRequest.CLASSIFICATION_STATUS);
                if (!z) {
                    str = "OK";
                }
                createElement6.setAttribute("result", str);
                createElement.appendChild(createElement6);
            } catch (Exception e4) {
                PrintStream printStream = System.err;
                printStream.println("appendEnvironmentReport threw: " + e4.toString());
                e4.printStackTrace();
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean appendFoundJars(Node node, Document document, List<Map> list, String str) {
        if (list == null || list.size() < 1) {
            return false;
        }
        boolean z = false;
        for (Map map : list) {
            for (Map.Entry entry : map.entrySet()) {
                String str2 = (String) entry.getKey();
                try {
                    if (str2.startsWith(ERROR)) {
                        z = true;
                    }
                    Element createElement = document.createElement("foundJar");
                    createElement.setAttribute("name", str2.substring(0, str2.indexOf(LanguageTag.SEP)));
                    createElement.setAttribute("desc", str2.substring(str2.indexOf(LanguageTag.SEP) + 1));
                    createElement.appendChild(document.createTextNode((String) entry.getValue()));
                    node.appendChild(createElement);
                } catch (Exception e) {
                    Element createElement2 = document.createElement("foundJar");
                    createElement2.appendChild(document.createTextNode("ERROR. Reading " + str2 + " threw: " + e.toString()));
                    node.appendChild(createElement2);
                    z = true;
                }
            }
        }
        return z;
    }

    /* access modifiers changed from: protected */
    public void checkSystemProperties(Map<String, Object> map) {
        if (map == null) {
            map = new HashMap<>();
        }
        try {
            map.put("java.version", SecuritySupport.getSystemProperty("java.version"));
        } catch (SecurityException unused) {
            map.put("java.version", "WARNING: SecurityException thrown accessing system version properties");
        }
        try {
            String systemProperty = SecuritySupport.getSystemProperty("java.class.path");
            map.put("java.class.path", systemProperty);
            List<Map> checkPathForJars = checkPathForJars(systemProperty, this.jarNames);
            if (checkPathForJars != null) {
                map.put("foundclasses.java.class.path", checkPathForJars);
            }
            String systemProperty2 = SecuritySupport.getSystemProperty("sun.boot.class.path");
            if (systemProperty2 != null) {
                map.put("sun.boot.class.path", systemProperty2);
                List<Map> checkPathForJars2 = checkPathForJars(systemProperty2, this.jarNames);
                if (checkPathForJars2 != null) {
                    map.put("foundclasses.sun.boot.class.path", checkPathForJars2);
                }
            }
            String systemProperty3 = SecuritySupport.getSystemProperty("java.ext.dirs");
            if (systemProperty3 != null) {
                map.put("java.ext.dirs", systemProperty3);
                List<Map> checkPathForJars3 = checkPathForJars(systemProperty3, this.jarNames);
                if (checkPathForJars3 != null) {
                    map.put("foundclasses.java.ext.dirs", checkPathForJars3);
                }
            }
        } catch (SecurityException unused2) {
            map.put("java.class.path", "WARNING: SecurityException thrown accessing system classpath properties");
        }
    }

    /* access modifiers changed from: protected */
    public List<Map> checkPathForJars(String str, String[] strArr) {
        if (str == null || strArr == null || str.length() == 0 || strArr.length == 0) {
            return null;
        }
        ArrayList arrayList = new ArrayList();
        StringTokenizer stringTokenizer = new StringTokenizer(str, File.pathSeparator);
        while (stringTokenizer.hasMoreTokens()) {
            String nextToken = stringTokenizer.nextToken();
            for (int i = 0; i < strArr.length; i++) {
                if (nextToken.indexOf(strArr[i]) > -1) {
                    File file = new File(nextToken);
                    if (file.exists()) {
                        try {
                            HashMap hashMap = new HashMap(2);
                            hashMap.put(strArr[i] + "-path", file.getAbsolutePath());
                            if (!"xalan.jar".equalsIgnoreCase(strArr[i])) {
                                hashMap.put(strArr[i] + "-apparent.version", getApparentVersion(strArr[i], file.length()));
                            }
                            arrayList.add(hashMap);
                        } catch (Exception unused) {
                        }
                    } else {
                        HashMap hashMap2 = new HashMap(2);
                        hashMap2.put(strArr[i] + "-path", "WARNING. Classpath entry: " + nextToken + " does not exist");
                        StringBuilder sb = new StringBuilder();
                        sb.append(strArr[i]);
                        sb.append("-apparent.version");
                        hashMap2.put(sb.toString(), CLASS_NOTPRESENT);
                        arrayList.add(hashMap2);
                    }
                }
            }
        }
        return arrayList;
    }

    /* access modifiers changed from: protected */
    public String getApparentVersion(String str, long j) {
        String str2 = JARVERSIONS.get(new Long(j));
        if (str2 != null && str2.startsWith(str)) {
            return str2;
        }
        if ("xerces.jar".equalsIgnoreCase(str) || "xercesImpl.jar".equalsIgnoreCase(str)) {
            return str + " " + WARNING + CLASS_PRESENT;
        }
        return str + " " + CLASS_PRESENT;
    }

    /* access modifiers changed from: protected */
    public void checkJAXPVersion(Map<String, Object> map) {
        if (map == null) {
            map = new HashMap<>();
        }
        try {
            ObjectFactory.findProviderClass("ohos.javax.xml.stream.XMLStreamConstants", true);
            map.put("version.JAXP", "1.4");
        } catch (Exception unused) {
            map.put("ERROR.version.JAXP", "1.3");
            map.put(ERROR, ERROR_FOUND);
        }
    }

    /* access modifiers changed from: protected */
    public void checkProcessorVersion(Map<String, Object> map) {
        if (map == null) {
            map = new HashMap<>();
        }
        try {
            Class<?> findProviderClass = ObjectFactory.findProviderClass("com.sun.org.apache.xalan.internal.xslt.XSLProcessorVersion", true);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(findProviderClass.getField("PRODUCT").get(null));
            stringBuffer.append(';');
            stringBuffer.append(findProviderClass.getField("LANGUAGE").get(null));
            stringBuffer.append(';');
            stringBuffer.append(findProviderClass.getField("S_VERSION").get(null));
            stringBuffer.append(';');
            map.put("version.xalan1", stringBuffer.toString());
        } catch (Exception unused) {
            map.put("version.xalan1", CLASS_NOTPRESENT);
        }
        try {
            Class<?> findProviderClass2 = ObjectFactory.findProviderClass("com.sun.org.apache.xalan.internal.processor.XSLProcessorVersion", true);
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append(findProviderClass2.getField("S_VERSION").get(null));
            map.put("version.xalan2x", stringBuffer2.toString());
        } catch (Exception unused2) {
            map.put("version.xalan2x", CLASS_NOTPRESENT);
        }
        try {
            map.put("version.xalan2_2", (String) ObjectFactory.findProviderClass("ohos.com.sun.org.apache.xalan.internal.Version", true).getMethod("getVersion", new Class[0]).invoke(null, new Object[0]));
        } catch (Exception unused3) {
            map.put("version.xalan2_2", CLASS_NOTPRESENT);
        }
    }

    /* access modifiers changed from: protected */
    public void checkParserVersion(Map<String, Object> map) {
        if (map == null) {
            map = new HashMap<>();
        }
        try {
            map.put("version.xerces1", (String) ObjectFactory.findProviderClass("com.sun.org.apache.xerces.internal.framework.Version", true).getField("fVersion").get(null));
        } catch (Exception unused) {
            map.put("version.xerces1", CLASS_NOTPRESENT);
        }
        try {
            map.put("version.xerces2", (String) ObjectFactory.findProviderClass("ohos.com.sun.org.apache.xerces.internal.impl.Version", true).getField("fVersion").get(null));
        } catch (Exception unused2) {
            map.put("version.xerces2", CLASS_NOTPRESENT);
        }
        try {
            ObjectFactory.findProviderClass("org.apache.crimson.parser.Parser2", true);
            map.put("version.crimson", CLASS_PRESENT);
        } catch (Exception unused3) {
            map.put("version.crimson", CLASS_NOTPRESENT);
        }
    }

    /* access modifiers changed from: protected */
    public void checkAntVersion(Map<String, Object> map) {
        if (map == null) {
            map = new HashMap<>();
        }
        try {
            map.put("version.ant", (String) ObjectFactory.findProviderClass("org.apache.tools.ant.Main", true).getMethod("getAntVersion", new Class[0]).invoke(null, new Object[0]));
        } catch (Exception unused) {
            map.put("version.ant", CLASS_NOTPRESENT);
        }
    }

    /* access modifiers changed from: protected */
    public boolean checkDOML3(Map<String, Object> map) {
        if (map == null) {
            map = new HashMap<>();
        }
        try {
            ObjectFactory.findProviderClass("ohos.org.w3c.dom.Document", true).getMethod("getDoctype", null);
            map.put("version.DOM", "3.0");
            return true;
        } catch (Exception unused) {
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public void checkDOMVersion(Map<String, Object> map) {
        if (map == null) {
            map = new HashMap<>();
        }
        Class<?>[] clsArr = {String.class, String.class};
        try {
            ObjectFactory.findProviderClass("ohos.org.w3c.dom.Document", true).getMethod("createElementNS", clsArr);
            map.put("version.DOM", "2.0");
            try {
                ObjectFactory.findProviderClass("ohos.org.w3c.dom.Node", true).getMethod("supported", clsArr);
                map.put("ERROR.version.DOM.draftlevel", "2.0wd");
                map.put(ERROR, ERROR_FOUND);
            } catch (Exception unused) {
                try {
                    ObjectFactory.findProviderClass("ohos.org.w3c.dom.Node", true).getMethod("isSupported", clsArr);
                    map.put("version.DOM.draftlevel", "2.0fd");
                } catch (Exception unused2) {
                    map.put("ERROR.version.DOM.draftlevel", "2.0unknown");
                    map.put(ERROR, ERROR_FOUND);
                }
            }
        } catch (Exception e) {
            map.put("ERROR.version.DOM", "ERROR attempting to load DOM level 2 class: " + e.toString());
            map.put(ERROR, ERROR_FOUND);
        }
    }

    /* access modifiers changed from: protected */
    public void checkSAXVersion(Map<String, Object> map) {
        if (map == null) {
            map = new HashMap<>();
        }
        Class<?>[] clsArr = {String.class};
        try {
            ObjectFactory.findProviderClass("ohos.org.xml.sax.helpers.AttributesImpl", true).getMethod("setAttributes", Attributes.class);
            map.put("version.SAX", "2.0");
        } catch (Exception e) {
            map.put("ERROR.version.SAX", "ERROR attempting to load SAX version 2 class: " + e.toString());
            map.put(ERROR, ERROR_FOUND);
            try {
                ObjectFactory.findProviderClass("ohos.org.xml.sax.XMLReader", true).getMethod("parse", clsArr);
                map.put("version.SAX-backlevel", "2.0beta2-or-earlier");
            } catch (Exception unused) {
                map.put("ERROR.version.SAX", "ERROR attempting to load SAX version 2 class: " + e.toString());
                map.put(ERROR, ERROR_FOUND);
                try {
                    ObjectFactory.findProviderClass("ohos.org.xml.sax.Parser", true).getMethod("parse", clsArr);
                    map.put("version.SAX-backlevel", "1.0");
                } catch (Exception e2) {
                    map.put("ERROR.version.SAX-backlevel", "ERROR attempting to load SAX version 1 class: " + e2.toString());
                }
            }
        }
    }

    static {
        HashMap hashMap = new HashMap();
        hashMap.put(new Long(857192), "xalan.jar from xalan-j_1_1");
        hashMap.put(new Long(440237), "xalan.jar from xalan-j_1_2");
        hashMap.put(new Long(436094), "xalan.jar from xalan-j_1_2_1");
        hashMap.put(new Long(426249), "xalan.jar from xalan-j_1_2_2");
        hashMap.put(new Long(702536), "xalan.jar from xalan-j_2_0_0");
        hashMap.put(new Long(720930), "xalan.jar from xalan-j_2_0_1");
        hashMap.put(new Long(732330), "xalan.jar from xalan-j_2_1_0");
        hashMap.put(new Long(872241), "xalan.jar from xalan-j_2_2_D10");
        hashMap.put(new Long(882739), "xalan.jar from xalan-j_2_2_D11");
        hashMap.put(new Long(923866), "xalan.jar from xalan-j_2_2_0");
        hashMap.put(new Long(905872), "xalan.jar from xalan-j_2_3_D1");
        hashMap.put(new Long(906122), "xalan.jar from xalan-j_2_3_0");
        hashMap.put(new Long(906248), "xalan.jar from xalan-j_2_3_1");
        hashMap.put(new Long(983377), "xalan.jar from xalan-j_2_4_D1");
        hashMap.put(new Long(997276), "xalan.jar from xalan-j_2_4_0");
        hashMap.put(new Long(1031036), "xalan.jar from xalan-j_2_4_1");
        hashMap.put(new Long(596540), "xsltc.jar from xalan-j_2_2_0");
        hashMap.put(new Long(590247), "xsltc.jar from xalan-j_2_3_D1");
        hashMap.put(new Long(589914), "xsltc.jar from xalan-j_2_3_0");
        hashMap.put(new Long(589915), "xsltc.jar from xalan-j_2_3_1");
        hashMap.put(new Long(1306667), "xsltc.jar from xalan-j_2_4_D1");
        hashMap.put(new Long(1328227), "xsltc.jar from xalan-j_2_4_0");
        hashMap.put(new Long(1344009), "xsltc.jar from xalan-j_2_4_1");
        hashMap.put(new Long(1348361), "xsltc.jar from xalan-j_2_5_D1");
        hashMap.put(new Long(1268634), "xsltc.jar-bundled from xalan-j_2_3_0");
        hashMap.put(new Long(100196), "xml-apis.jar from xalan-j_2_2_0 or xalan-j_2_3_D1");
        hashMap.put(new Long(108484), "xml-apis.jar from xalan-j_2_3_0, or xalan-j_2_3_1 from xml-commons-1.0.b2");
        hashMap.put(new Long(109049), "xml-apis.jar from xalan-j_2_4_0 from xml-commons RIVERCOURT1 branch");
        hashMap.put(new Long(113749), "xml-apis.jar from xalan-j_2_4_1 from factoryfinder-build of xml-commons RIVERCOURT1");
        hashMap.put(new Long(124704), "xml-apis.jar from tck-jaxp-1_2_0 branch of xml-commons");
        hashMap.put(new Long(124724), "xml-apis.jar from tck-jaxp-1_2_0 branch of xml-commons, tag: xml-commons-external_1_2_01");
        hashMap.put(new Long(194205), "xml-apis.jar from head branch of xml-commons, tag: xml-commons-external_1_3_02");
        hashMap.put(new Long(424490), "xalan.jar from Xerces Tools releases - ERROR:DO NOT USE!");
        hashMap.put(new Long(1591855), "xerces.jar from xalan-j_1_1 from xerces-1...");
        hashMap.put(new Long(1498679), "xerces.jar from xalan-j_1_2 from xerces-1_2_0.bin");
        hashMap.put(new Long(1484896), "xerces.jar from xalan-j_1_2_1 from xerces-1_2_1.bin");
        hashMap.put(new Long(804460), "xerces.jar from xalan-j_1_2_2 from xerces-1_2_2.bin");
        hashMap.put(new Long(1499244), "xerces.jar from xalan-j_2_0_0 from xerces-1_2_3.bin");
        hashMap.put(new Long(1605266), "xerces.jar from xalan-j_2_0_1 from xerces-1_3_0.bin");
        hashMap.put(new Long(904030), "xerces.jar from xalan-j_2_1_0 from xerces-1_4.bin");
        hashMap.put(new Long(904030), "xerces.jar from xerces-1_4_0.bin");
        hashMap.put(new Long(1802885), "xerces.jar from xerces-1_4_2.bin");
        hashMap.put(new Long(1734594), "xerces.jar from Xerces-J-bin.2.0.0.beta3");
        hashMap.put(new Long(1808883), "xerces.jar from xalan-j_2_2_D10,D11,D12 or xerces-1_4_3.bin");
        hashMap.put(new Long(1812019), "xerces.jar from xalan-j_2_2_0");
        hashMap.put(new Long(1720292), "xercesImpl.jar from xalan-j_2_3_D1");
        hashMap.put(new Long(1730053), "xercesImpl.jar from xalan-j_2_3_0 or xalan-j_2_3_1 from xerces-2_0_0");
        hashMap.put(new Long(1728861), "xercesImpl.jar from xalan-j_2_4_D1 from xerces-2_0_1");
        hashMap.put(new Long(972027), "xercesImpl.jar from xalan-j_2_4_0 from xerces-2_1");
        hashMap.put(new Long(831587), "xercesImpl.jar from xalan-j_2_4_1 from xerces-2_2");
        hashMap.put(new Long(891817), "xercesImpl.jar from xalan-j_2_5_D1 from xerces-2_3");
        hashMap.put(new Long(895924), "xercesImpl.jar from xerces-2_4");
        hashMap.put(new Long(1010806), "xercesImpl.jar from Xerces-J-bin.2.6.2");
        hashMap.put(new Long(1203860), "xercesImpl.jar from Xerces-J-bin.2.7.1");
        hashMap.put(new Long(37485), "xalanj1compat.jar from xalan-j_2_0_0");
        hashMap.put(new Long(38100), "xalanj1compat.jar from xalan-j_2_0_1");
        hashMap.put(new Long(18779), "xalanservlet.jar from xalan-j_2_0_0");
        hashMap.put(new Long(21453), "xalanservlet.jar from xalan-j_2_0_1");
        hashMap.put(new Long(24826), "xalanservlet.jar from xalan-j_2_3_1 or xalan-j_2_4_1");
        hashMap.put(new Long(24831), "xalanservlet.jar from xalan-j_2_4_1");
        hashMap.put(new Long(5618), "jaxp.jar from jaxp1.0.1");
        hashMap.put(new Long(136133), "parser.jar from jaxp1.0.1");
        hashMap.put(new Long(28404), "jaxp.jar from jaxp-1.1");
        hashMap.put(new Long(187162), "crimson.jar from jaxp-1.1");
        hashMap.put(new Long(801714), "xalan.jar from jaxp-1.1");
        hashMap.put(new Long(196399), "crimson.jar from crimson-1.1.1");
        hashMap.put(new Long(33323), "jaxp.jar from crimson-1.1.1 or jakarta-ant-1.4.1b1");
        hashMap.put(new Long(152717), "crimson.jar from crimson-1.1.2beta2");
        hashMap.put(new Long(88143), "xml-apis.jar from crimson-1.1.2beta2");
        hashMap.put(new Long(206384), "crimson.jar from crimson-1.1.3 or jakarta-ant-1.4.1b1");
        hashMap.put(new Long(136198), "parser.jar from jakarta-ant-1.3 or 1.2");
        hashMap.put(new Long(5537), "jaxp.jar from jakarta-ant-1.3 or 1.2");
        JARVERSIONS = Collections.unmodifiableMap(hashMap);
    }

    /* access modifiers changed from: protected */
    public void logMsg(String str) {
        this.outWriter.println(str);
    }
}
