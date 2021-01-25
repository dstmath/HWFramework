package ohos.com.sun.org.apache.xerces.internal.dom;

import ohos.com.sun.org.apache.xerces.internal.impl.RevalidationHandler;
import ohos.com.sun.org.apache.xerces.internal.parsers.DOMParserImpl;
import ohos.com.sun.org.apache.xerces.internal.parsers.DTDConfiguration;
import ohos.com.sun.org.apache.xerces.internal.parsers.XIncludeAwareParserConfiguration;
import ohos.com.sun.org.apache.xerces.internal.util.XMLChar;
import ohos.com.sun.org.apache.xerces.internal.utils.ObjectFactory;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarDescription;
import ohos.com.sun.org.apache.xml.internal.serialize.DOMSerializerImpl;
import ohos.org.w3c.dom.DOMException;
import ohos.org.w3c.dom.DOMImplementation;
import ohos.org.w3c.dom.Document;
import ohos.org.w3c.dom.DocumentType;
import ohos.org.w3c.dom.ls.DOMImplementationLS;
import ohos.org.w3c.dom.ls.LSInput;
import ohos.org.w3c.dom.ls.LSOutput;
import ohos.org.w3c.dom.ls.LSParser;
import ohos.org.w3c.dom.ls.LSSerializer;

public class CoreDOMImplementationImpl implements DOMImplementation, DOMImplementationLS {
    private static final int SIZE = 2;
    static CoreDOMImplementationImpl singleton = new CoreDOMImplementationImpl();
    private int currentSize = 2;
    private int docAndDoctypeCounter = 0;
    private RevalidationHandler[] dtdValidators = new RevalidationHandler[2];
    private int freeDTDValidatorIndex = -1;
    private int freeValidatorIndex = -1;
    private RevalidationHandler[] validators = new RevalidationHandler[2];

    public static DOMImplementation getDOMImplementation() {
        return singleton;
    }

    public boolean hasFeature(String str, String str2) {
        boolean z = str2 == null || str2.length() == 0;
        if (!str.equalsIgnoreCase("+XPath") || (!z && !str2.equals("3.0"))) {
            if (str.startsWith("+")) {
                str = str.substring(1);
            }
            if ((!str.equalsIgnoreCase("Core") || (!z && !str2.equals("1.0") && !str2.equals("2.0") && !str2.equals("3.0"))) && (!str.equalsIgnoreCase("XML") || (!z && !str2.equals("1.0") && !str2.equals("2.0") && !str2.equals("3.0")))) {
                if (!str.equalsIgnoreCase("LS")) {
                    return false;
                }
                if (!z && !str2.equals("3.0")) {
                    return false;
                }
            }
            return true;
        }
        try {
            Class<?>[] interfaces = ObjectFactory.findProviderClass("ohos.com.sun.org.apache.xpath.internal.domapi.XPathEvaluatorImpl", true).getInterfaces();
            int i = 0;
            while (i < interfaces.length && !interfaces[i].getName().equals("ohos.org.w3c.dom.xpath.XPathEvaluator")) {
                i++;
            }
            return true;
        } catch (Exception unused) {
            return false;
        }
    }

    public DocumentType createDocumentType(String str, String str2, String str3) {
        checkQName(str);
        return new DocumentTypeImpl(null, str, str2, str3);
    }

    /* access modifiers changed from: package-private */
    public final void checkQName(String str) {
        int indexOf = str.indexOf(58);
        int lastIndexOf = str.lastIndexOf(58);
        int length = str.length();
        if (indexOf == 0 || indexOf == length - 1 || lastIndexOf != indexOf) {
            throw new DOMException(14, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NAMESPACE_ERR", null));
        }
        int i = 0;
        if (indexOf > 0) {
            if (XMLChar.isNCNameStart(str.charAt(0))) {
                for (int i2 = 1; i2 < indexOf; i2++) {
                    if (!XMLChar.isNCName(str.charAt(i2))) {
                        throw new DOMException(5, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INVALID_CHARACTER_ERR", null));
                    }
                }
                i = indexOf + 1;
            } else {
                throw new DOMException(5, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INVALID_CHARACTER_ERR", null));
            }
        }
        if (XMLChar.isNCNameStart(str.charAt(i))) {
            for (int i3 = i + 1; i3 < length; i3++) {
                if (!XMLChar.isNCName(str.charAt(i3))) {
                    throw new DOMException(5, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INVALID_CHARACTER_ERR", null));
                }
            }
            return;
        }
        throw new DOMException(5, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INVALID_CHARACTER_ERR", null));
    }

    public Document createDocument(String str, String str2, DocumentType documentType) throws DOMException {
        if (documentType == null || documentType.getOwnerDocument() == null) {
            CoreDocumentImpl coreDocumentImpl = new CoreDocumentImpl(documentType);
            coreDocumentImpl.appendChild(coreDocumentImpl.createElementNS(str, str2));
            return coreDocumentImpl;
        }
        throw new DOMException(4, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "WRONG_DOCUMENT_ERR", null));
    }

    public Object getFeature(String str, String str2) {
        Class<?>[] interfaces;
        if (singleton.hasFeature(str, str2)) {
            if (!str.equalsIgnoreCase("+XPath")) {
                return singleton;
            }
            try {
                Class findProviderClass = ObjectFactory.findProviderClass("ohos.com.sun.org.apache.xpath.internal.domapi.XPathEvaluatorImpl", true);
                for (Class<?> cls : findProviderClass.getInterfaces()) {
                    if (cls.getName().equals("ohos.org.w3c.dom.xpath.XPathEvaluator")) {
                        return findProviderClass.newInstance();
                    }
                }
            } catch (Exception unused) {
                return null;
            }
        }
        return null;
    }

    public LSParser createLSParser(short s, String str) throws DOMException {
        if (s != 1 || (str != null && !"http://www.w3.org/2001/XMLSchema".equals(str) && !XMLGrammarDescription.XML_DTD.equals(str))) {
            throw new DOMException(9, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_SUPPORTED_ERR", null));
        } else if (str == null || !str.equals(XMLGrammarDescription.XML_DTD)) {
            return new DOMParserImpl(new XIncludeAwareParserConfiguration(), str);
        } else {
            return new DOMParserImpl(new DTDConfiguration(), str);
        }
    }

    public LSSerializer createLSSerializer() {
        return new DOMSerializerImpl();
    }

    public LSInput createLSInput() {
        return new DOMInputImpl();
    }

    /* access modifiers changed from: package-private */
    public synchronized RevalidationHandler getValidator(String str) {
        if (str == "http://www.w3.org/2001/XMLSchema") {
            if (this.freeValidatorIndex < 0) {
                return (RevalidationHandler) ObjectFactory.newInstance("ohos.com.sun.org.apache.xerces.internal.impl.xs.XMLSchemaValidator", ObjectFactory.findClassLoader(), true);
            }
            RevalidationHandler revalidationHandler = this.validators[this.freeValidatorIndex];
            RevalidationHandler[] revalidationHandlerArr = this.validators;
            int i = this.freeValidatorIndex;
            this.freeValidatorIndex = i - 1;
            revalidationHandlerArr[i] = null;
            return revalidationHandler;
        } else if (str != XMLGrammarDescription.XML_DTD) {
            return null;
        } else {
            if (this.freeDTDValidatorIndex < 0) {
                return (RevalidationHandler) ObjectFactory.newInstance("ohos.com.sun.org.apache.xerces.internal.impl.dtd.XMLDTDValidator", ObjectFactory.findClassLoader(), true);
            }
            RevalidationHandler revalidationHandler2 = this.dtdValidators[this.freeDTDValidatorIndex];
            RevalidationHandler[] revalidationHandlerArr2 = this.dtdValidators;
            int i2 = this.freeDTDValidatorIndex;
            this.freeDTDValidatorIndex = i2 - 1;
            revalidationHandlerArr2[i2] = null;
            return revalidationHandler2;
        }
    }

    /* access modifiers changed from: package-private */
    public synchronized void releaseValidator(String str, RevalidationHandler revalidationHandler) {
        if (str == "http://www.w3.org/2001/XMLSchema") {
            this.freeValidatorIndex++;
            if (this.validators.length == this.freeValidatorIndex) {
                this.currentSize += 2;
                RevalidationHandler[] revalidationHandlerArr = new RevalidationHandler[this.currentSize];
                System.arraycopy(this.validators, 0, revalidationHandlerArr, 0, this.validators.length);
                this.validators = revalidationHandlerArr;
            }
            this.validators[this.freeValidatorIndex] = revalidationHandler;
        } else if (str == XMLGrammarDescription.XML_DTD) {
            this.freeDTDValidatorIndex++;
            if (this.dtdValidators.length == this.freeDTDValidatorIndex) {
                this.currentSize += 2;
                RevalidationHandler[] revalidationHandlerArr2 = new RevalidationHandler[this.currentSize];
                System.arraycopy(this.dtdValidators, 0, revalidationHandlerArr2, 0, this.dtdValidators.length);
                this.dtdValidators = revalidationHandlerArr2;
            }
            this.dtdValidators[this.freeDTDValidatorIndex] = revalidationHandler;
        }
    }

    /* access modifiers changed from: protected */
    public synchronized int assignDocumentNumber() {
        int i;
        i = this.docAndDoctypeCounter + 1;
        this.docAndDoctypeCounter = i;
        return i;
    }

    /* access modifiers changed from: protected */
    public synchronized int assignDocTypeNumber() {
        int i;
        i = this.docAndDoctypeCounter + 1;
        this.docAndDoctypeCounter = i;
        return i;
    }

    public LSOutput createLSOutput() {
        return new DOMOutputImpl();
    }
}
