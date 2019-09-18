package org.apache.xalan.processor;

import java.util.List;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.templates.ElemExtensionCall;
import org.apache.xalan.templates.ElemLiteralResult;
import org.apache.xalan.templates.ElemTemplate;
import org.apache.xalan.templates.ElemTemplateElement;
import org.apache.xalan.templates.Stylesheet;
import org.apache.xalan.templates.StylesheetRoot;
import org.apache.xalan.templates.XMLNSDecl;
import org.apache.xml.utils.Constants;
import org.apache.xml.utils.SAXSourceLocator;
import org.apache.xpath.XPath;
import org.apache.xpath.compiler.PsuedoNames;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class ProcessorLRE extends ProcessorTemplateElem {
    static final long serialVersionUID = -1490218021772101404L;

    /* JADX WARNING: Code restructure failed: missing block: B:100:0x0242, code lost:
        r42 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:80:0x01e3, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:96:0x0237, code lost:
        r42 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:98:?, code lost:
        r2.error(org.apache.xalan.res.XSLTErrorResources.ER_FAILED_CREATING_ELEMLITRSLT, null, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:99:0x0241, code lost:
        r0 = e;
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:77:0x01dc, B:88:0x01fb] */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x01e3 A[Catch:{ InstantiationException -> 0x01e5, IllegalAccessException -> 0x01e3 }, ExcHandler: IllegalAccessException (r0v31 'iae' java.lang.IllegalAccessException A[CUSTOM_DECLARE, Catch:{ InstantiationException -> 0x01e5, IllegalAccessException -> 0x01e3 }]), Splitter:B:77:0x01dc] */
    public void startElement(StylesheetHandler handler, String uri, String localName, String rawName, Attributes attributes) throws SAXException {
        ElemTemplateElement p;
        ElemTemplateElement p2;
        AttributesImpl stylesheetAttrs;
        boolean isLREAsStyleSheet;
        ElemTemplateElement elem;
        Stylesheet stylesheet;
        SAXSourceLocator slocator;
        SAXSourceLocator locator;
        int n;
        int n2;
        boolean isLREAsStyleSheet2;
        boolean excludeXSLDecl;
        StylesheetHandler stylesheetHandler = handler;
        String str = uri;
        String str2 = localName;
        String str3 = rawName;
        Attributes attributes2 = attributes;
        try {
            p = handler.getElemTemplateElement();
            boolean excludeXSLDecl2 = false;
            if (p == null) {
                XSLTElementProcessor lreProcessor = handler.popProcessor();
                XSLTElementProcessor stylesheetProcessor = stylesheetHandler.getProcessorFor(Constants.S_XSLNAMESPACEURL, org.apache.xalan.templates.Constants.ELEMNAME_STYLESHEET_STRING, "xsl:stylesheet");
                stylesheetHandler.pushProcessor(lreProcessor);
                stylesheet = getStylesheetRoot(handler);
                slocator = new SAXSourceLocator();
                locator = handler.getLocator();
                if (locator != null) {
                    slocator.setLineNumber(locator.getLineNumber());
                    slocator.setColumnNumber(locator.getColumnNumber());
                    slocator.setPublicId(locator.getPublicId());
                    slocator.setSystemId(locator.getSystemId());
                }
                stylesheet.setLocaterInfo(slocator);
                stylesheet.setPrefixes(handler.getNamespaceSupport());
                stylesheetHandler.pushStylesheet(stylesheet);
                boolean isLREAsStyleSheet3 = true;
                AttributesImpl stylesheetAttrs2 = new AttributesImpl();
                AttributesImpl lreAttrs = new AttributesImpl();
                int n3 = attributes.getLength();
                int i = 0;
                while (true) {
                    n = n3;
                    if (i >= n) {
                        break;
                    }
                    String attrLocalName = attributes2.getLocalName(i);
                    String attrUri = attributes2.getURI(i);
                    String value = attributes2.getValue(i);
                    ElemTemplateElement p3 = p;
                    String attrUri2 = attrUri;
                    if (attrUri2 != null) {
                        excludeXSLDecl = excludeXSLDecl2;
                        if (attrUri2.equals(Constants.S_XSLNAMESPACEURL)) {
                            stylesheetAttrs2.addAttribute(null, attrLocalName, attrLocalName, attributes2.getType(i), attributes2.getValue(i));
                            isLREAsStyleSheet2 = isLREAsStyleSheet3;
                            n2 = n;
                            i++;
                            p = p3;
                            excludeXSLDecl2 = excludeXSLDecl;
                            isLREAsStyleSheet3 = isLREAsStyleSheet2;
                            n3 = n2;
                        }
                    } else {
                        excludeXSLDecl = excludeXSLDecl2;
                    }
                    isLREAsStyleSheet2 = isLREAsStyleSheet3;
                    String attrLocalName2 = attrLocalName;
                    if (!attrLocalName2.startsWith(org.apache.xalan.templates.Constants.ATTRNAME_XMLNS)) {
                        if (!attrLocalName2.equals("xmlns")) {
                            n2 = n;
                            String str4 = value;
                            lreAttrs.addAttribute(attrUri2, attrLocalName2, attributes2.getQName(i), attributes2.getType(i), attributes2.getValue(i));
                            i++;
                            p = p3;
                            excludeXSLDecl2 = excludeXSLDecl;
                            isLREAsStyleSheet3 = isLREAsStyleSheet2;
                            n3 = n2;
                        }
                    }
                    n2 = n;
                    if (value.equals(Constants.S_XSLNAMESPACEURL)) {
                        i++;
                        p = p3;
                        excludeXSLDecl2 = excludeXSLDecl;
                        isLREAsStyleSheet3 = isLREAsStyleSheet2;
                        n3 = n2;
                    }
                    lreAttrs.addAttribute(attrUri2, attrLocalName2, attributes2.getQName(i), attributes2.getType(i), attributes2.getValue(i));
                    i++;
                    p = p3;
                    excludeXSLDecl2 = excludeXSLDecl;
                    isLREAsStyleSheet3 = isLREAsStyleSheet2;
                    n3 = n2;
                }
                boolean z = excludeXSLDecl2;
                boolean isLREAsStyleSheet4 = isLREAsStyleSheet3;
                int i2 = n;
                AttributesImpl attributesImpl = lreAttrs;
                stylesheetProcessor.setPropertiesFromAttributes(stylesheetHandler, org.apache.xalan.templates.Constants.ELEMNAME_STYLESHEET_STRING, stylesheetAttrs2, stylesheet);
                stylesheetHandler.pushElemTemplateElement(stylesheet);
                ElemTemplate template = new ElemTemplate();
                template.setLocaterInfo(slocator);
                appendAndPush(stylesheetHandler, template);
                SAXSourceLocator sAXSourceLocator = locator;
                SAXSourceLocator sAXSourceLocator2 = slocator;
                XPath xPath = new XPath(PsuedoNames.PSEUDONAME_ROOT, stylesheet, stylesheet, 1, handler.getStylesheetProcessor().getErrorListener());
                template.setMatch(xPath);
                stylesheet.setTemplate(template);
                p2 = handler.getElemTemplateElement();
                excludeXSLDecl2 = true;
                stylesheetAttrs = attributesImpl;
                isLREAsStyleSheet = isLREAsStyleSheet4;
            } else {
                ElemTemplateElement p4 = p;
                stylesheetAttrs = attributes2;
                isLREAsStyleSheet = false;
                p2 = p4;
            }
            try {
                Class classObject = getElemDef().getClassObject();
                boolean isExtension = false;
                boolean isComponentDecl = false;
                boolean isUnknownTopLevel = false;
                while (true) {
                    boolean isUnknownTopLevel2 = isUnknownTopLevel;
                    if (p2 == null) {
                        isUnknownTopLevel = isUnknownTopLevel2;
                        break;
                    }
                    if (p2 instanceof ElemLiteralResult) {
                        isExtension = ((ElemLiteralResult) p2).containsExtensionElementURI(str);
                    } else if (p2 instanceof Stylesheet) {
                        isExtension = ((Stylesheet) p2).containsExtensionElementURI(str);
                        if (isExtension || str == null || (!str.equals("http://xml.apache.org/xalan") && !str.equals(Constants.S_BUILTIN_OLD_EXTENSIONS_URL))) {
                            isUnknownTopLevel2 = true;
                        } else {
                            isComponentDecl = true;
                        }
                    }
                    isUnknownTopLevel = isUnknownTopLevel2;
                    if (isExtension) {
                        break;
                    }
                    p2 = p2.getParentElem();
                }
                boolean isComponentDecl2 = isComponentDecl;
                boolean isExtension2 = isExtension;
                ElemTemplateElement elem2 = null;
                if (isExtension2) {
                    try {
                        elem = new ElemExtensionCall();
                    } catch (InstantiationException e) {
                        ie = e;
                        boolean z2 = isLREAsStyleSheet;
                        stylesheetHandler.error(XSLTErrorResources.ER_FAILED_CREATING_ELEMLITRSLT, null, ie);
                        setPropertiesFromAttributes(stylesheetHandler, str3, stylesheetAttrs, elem2);
                        elem2 = new ElemExtensionCall();
                        elem2.setLocaterInfo(handler.getLocator());
                        elem2.setPrefixes(handler.getNamespaceSupport());
                        ((ElemLiteralResult) elem2).setNamespace(str);
                        ((ElemLiteralResult) elem2).setLocalName(str2);
                        ((ElemLiteralResult) elem2).setRawName(str3);
                        setPropertiesFromAttributes(stylesheetHandler, str3, stylesheetAttrs, elem2);
                        appendAndPush(stylesheetHandler, elem2);
                    } catch (IllegalAccessException iae) {
                    }
                } else if (isComponentDecl2) {
                    elem = (ElemTemplateElement) classObject.newInstance();
                } else if (isUnknownTopLevel) {
                    elem = (ElemTemplateElement) classObject.newInstance();
                } else {
                    elem = (ElemTemplateElement) classObject.newInstance();
                }
                elem2 = elem;
                elem2.setDOMBackPointer(handler.getOriginatingNode());
                elem2.setLocaterInfo(handler.getLocator());
                elem2.setPrefixes(handler.getNamespaceSupport(), excludeXSLDecl2);
                if (elem2 instanceof ElemLiteralResult) {
                    ((ElemLiteralResult) elem2).setNamespace(str);
                    ((ElemLiteralResult) elem2).setLocalName(str2);
                    ((ElemLiteralResult) elem2).setRawName(str3);
                    ((ElemLiteralResult) elem2).setIsLiteralResultAsStylesheet(isLREAsStyleSheet);
                }
                boolean z3 = isLREAsStyleSheet;
                setPropertiesFromAttributes(stylesheetHandler, str3, stylesheetAttrs, elem2);
                if (!isExtension2 && (elem2 instanceof ElemLiteralResult) && ((ElemLiteralResult) elem2).containsExtensionElementURI(str)) {
                    elem2 = new ElemExtensionCall();
                    elem2.setLocaterInfo(handler.getLocator());
                    elem2.setPrefixes(handler.getNamespaceSupport());
                    ((ElemLiteralResult) elem2).setNamespace(str);
                    ((ElemLiteralResult) elem2).setLocalName(str2);
                    ((ElemLiteralResult) elem2).setRawName(str3);
                    setPropertiesFromAttributes(stylesheetHandler, str3, stylesheetAttrs, elem2);
                }
                appendAndPush(stylesheetHandler, elem2);
            } catch (TransformerException e2) {
                te = e2;
                Attributes attributes3 = stylesheetAttrs;
                throw new SAXException(te);
            }
        } catch (TransformerConfigurationException tfe) {
            ElemTemplateElement elemTemplateElement = p;
            TransformerConfigurationException transformerConfigurationException = tfe;
            throw new TransformerException(tfe);
        } catch (Exception e3) {
            SAXSourceLocator sAXSourceLocator3 = locator;
            SAXSourceLocator sAXSourceLocator4 = slocator;
            if (stylesheet.getDeclaredPrefixes() != null) {
                if (declaredXSLNS(stylesheet)) {
                    throw new SAXException(e3);
                }
            }
            throw new SAXException(XSLMessages.createWarning(XSLTErrorResources.WG_OLD_XSLT_NS, null));
        } catch (TransformerException e4) {
            te = e4;
            throw new SAXException(te);
        }
    }

    /* access modifiers changed from: protected */
    public Stylesheet getStylesheetRoot(StylesheetHandler handler) throws TransformerConfigurationException {
        StylesheetRoot stylesheet = new StylesheetRoot(handler.getSchema(), handler.getStylesheetProcessor().getErrorListener());
        if (handler.getStylesheetProcessor().isSecureProcessing()) {
            stylesheet.setSecureProcessing(true);
        }
        return stylesheet;
    }

    public void endElement(StylesheetHandler handler, String uri, String localName, String rawName) throws SAXException {
        ElemTemplateElement elem = handler.getElemTemplateElement();
        if ((elem instanceof ElemLiteralResult) && ((ElemLiteralResult) elem).getIsLiteralResultAsStylesheet()) {
            handler.popStylesheet();
        }
        super.endElement(handler, uri, localName, rawName);
    }

    private boolean declaredXSLNS(Stylesheet stylesheet) {
        List declaredPrefixes = stylesheet.getDeclaredPrefixes();
        int n = declaredPrefixes.size();
        for (int i = 0; i < n; i++) {
            if (((XMLNSDecl) declaredPrefixes.get(i)).getURI().equals(Constants.S_XSLNAMESPACEURL)) {
                return true;
            }
        }
        return false;
    }
}
