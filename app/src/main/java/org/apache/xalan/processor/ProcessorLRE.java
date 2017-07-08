package org.apache.xalan.processor;

import java.util.List;
import javax.xml.transform.TransformerConfigurationException;
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
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class ProcessorLRE extends ProcessorTemplateElem {
    static final long serialVersionUID = -1490218021772101404L;

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void startElement(StylesheetHandler handler, String uri, String localName, String rawName, Attributes attributes) throws SAXException {
        ElemTemplateElement stylesheet;
        try {
            ElemTemplateElement elemTemplateElement = handler.getElemTemplateElement();
            boolean excludeXSLDecl = false;
            boolean isLREAsStyleSheet = false;
            if (elemTemplateElement == null) {
                XSLTElementProcessor lreProcessor = handler.popProcessor();
                XSLTElementProcessor stylesheetProcessor = handler.getProcessorFor(Constants.S_XSLNAMESPACEURL, org.apache.xalan.templates.Constants.ELEMNAME_STYLESHEET_STRING, "xsl:stylesheet");
                handler.pushProcessor(lreProcessor);
                stylesheet = getStylesheetRoot(handler);
                SAXSourceLocator slocator = new SAXSourceLocator();
                Locator locator = handler.getLocator();
                if (locator != null) {
                    slocator.setLineNumber(locator.getLineNumber());
                    slocator.setColumnNumber(locator.getColumnNumber());
                    slocator.setPublicId(locator.getPublicId());
                    slocator.setSystemId(locator.getSystemId());
                }
                stylesheet.setLocaterInfo(slocator);
                stylesheet.setPrefixes(handler.getNamespaceSupport());
                handler.pushStylesheet(stylesheet);
                isLREAsStyleSheet = true;
                AttributesImpl stylesheetAttrs = new AttributesImpl();
                AttributesImpl lreAttrs = new AttributesImpl();
                int n = attributes.getLength();
                for (int i = 0; i < n; i++) {
                    String attrLocalName = attributes.getLocalName(i);
                    String attrUri = attributes.getURI(i);
                    String value = attributes.getValue(i);
                    if (attrUri == null || !attrUri.equals(Constants.S_XSLNAMESPACEURL)) {
                        if (attrLocalName.startsWith(org.apache.xalan.templates.Constants.ATTRNAME_XMLNS) || attrLocalName.equals(SerializerConstants.XMLNS_PREFIX)) {
                            if (value.equals(Constants.S_XSLNAMESPACEURL)) {
                            }
                        }
                        lreAttrs.addAttribute(attrUri, attrLocalName, attributes.getQName(i), attributes.getType(i), attributes.getValue(i));
                    } else {
                        stylesheetAttrs.addAttribute(null, attrLocalName, attrLocalName, attributes.getType(i), attributes.getValue(i));
                    }
                }
                attributes = lreAttrs;
                stylesheetProcessor.setPropertiesFromAttributes(handler, org.apache.xalan.templates.Constants.ELEMNAME_STYLESHEET_STRING, stylesheetAttrs, stylesheet);
                handler.pushElemTemplateElement(stylesheet);
                ElemTemplateElement template = new ElemTemplate();
                if (slocator != null) {
                    template.setLocaterInfo(slocator);
                }
                appendAndPush(handler, template);
                ElemTemplateElement elemTemplateElement2 = template;
                elemTemplateElement2.setMatch(new XPath(PsuedoNames.PSEUDONAME_ROOT, stylesheet, stylesheet, 1, handler.getStylesheetProcessor().getErrorListener()));
                stylesheet.setTemplate(template);
                elemTemplateElement = handler.getElemTemplateElement();
                excludeXSLDecl = true;
            }
            Class classObject = getElemDef().getClassObject();
            boolean isExtension = false;
            boolean isComponentDecl = false;
            boolean isUnknownTopLevel = false;
            for (elemTemplateElement = 
            /* Method generation error in method: org.apache.xalan.processor.ProcessorLRE.startElement(org.apache.xalan.processor.StylesheetHandler, java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes):void
jadx.core.utils.exceptions.CodegenException: Error generate insn: PHI: (r33_2 'elemTemplateElement' org.apache.xalan.templates.ElemTemplateElement) = (r33_0 'elemTemplateElement' org.apache.xalan.templates.ElemTemplateElement), (r33_1 'elemTemplateElement' org.apache.xalan.templates.ElemTemplateElement) binds: {(r33_1 'elemTemplateElement' org.apache.xalan.templates.ElemTemplateElement)=B:42:0x0132, (r33_0 'elemTemplateElement' org.apache.xalan.templates.ElemTemplateElement)=B:2:0x0008} in method: org.apache.xalan.processor.ProcessorLRE.startElement(org.apache.xalan.processor.StylesheetHandler, java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes):void
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:225)
	at jadx.core.codegen.RegionGen.makeLoop(RegionGen.java:184)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:61)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:93)
	at jadx.core.codegen.RegionGen.makeTryCatch(RegionGen.java:277)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:63)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:177)
	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:324)
	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:263)
	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:226)
	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:116)
	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:81)
	at jadx.core.codegen.CodeGen.visit(CodeGen.java:19)
	at jadx.core.ProcessClass.process(ProcessClass.java:43)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.CodegenException: Unknown instruction: PHI in method: org.apache.xalan.processor.ProcessorLRE.startElement(org.apache.xalan.processor.StylesheetHandler, java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes):void
	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:512)
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:219)
	... 20 more
 */

            protected Stylesheet getStylesheetRoot(StylesheetHandler handler) throws TransformerConfigurationException {
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
