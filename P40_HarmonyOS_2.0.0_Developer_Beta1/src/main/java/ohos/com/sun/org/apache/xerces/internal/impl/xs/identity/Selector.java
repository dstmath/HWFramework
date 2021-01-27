package ohos.com.sun.org.apache.xerces.internal.impl.xs.identity;

import ohos.com.sun.org.apache.xerces.internal.impl.xpath.XPathException;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolTable;
import ohos.com.sun.org.apache.xerces.internal.util.XMLChar;
import ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext;
import ohos.com.sun.org.apache.xerces.internal.xni.QName;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes;
import ohos.com.sun.org.apache.xerces.internal.xs.ShortList;
import ohos.com.sun.org.apache.xerces.internal.xs.XSTypeDefinition;
import ohos.com.sun.org.apache.xpath.internal.compiler.PsuedoNames;

public class Selector {
    protected IdentityConstraint fIDConstraint;
    protected final IdentityConstraint fIdentityConstraint;
    protected final XPath fXPath;

    public Selector(XPath xPath, IdentityConstraint identityConstraint) {
        this.fXPath = xPath;
        this.fIdentityConstraint = identityConstraint;
    }

    public ohos.com.sun.org.apache.xerces.internal.impl.xpath.XPath getXPath() {
        return this.fXPath;
    }

    public IdentityConstraint getIDConstraint() {
        return this.fIdentityConstraint;
    }

    public XPathMatcher createMatcher(FieldActivator fieldActivator, int i) {
        return new Matcher(this.fXPath, fieldActivator, i);
    }

    public String toString() {
        return this.fXPath.toString();
    }

    public static class XPath extends ohos.com.sun.org.apache.xerces.internal.impl.xpath.XPath {
        public XPath(String str, SymbolTable symbolTable, NamespaceContext namespaceContext) throws XPathException {
            super(normalize(str), symbolTable, namespaceContext);
            for (int i = 0; i < this.fLocationPaths.length; i++) {
                if (this.fLocationPaths[i].steps[this.fLocationPaths[i].steps.length - 1].axis.type == 2) {
                    throw new XPathException("c-selector-xpath");
                }
            }
        }

        private static String normalize(String str) {
            StringBuffer stringBuffer = new StringBuffer(str.length() + 5);
            while (true) {
                if (!XMLChar.trim(str).startsWith(PsuedoNames.PSEUDONAME_ROOT) && !XMLChar.trim(str).startsWith(".")) {
                    stringBuffer.append("./");
                }
                int indexOf = str.indexOf(124);
                if (indexOf == -1) {
                    stringBuffer.append(str);
                    return stringBuffer.toString();
                }
                int i = indexOf + 1;
                stringBuffer.append(str.substring(0, i));
                str = str.substring(i, str.length());
            }
        }
    }

    public class Matcher extends XPathMatcher {
        protected int fElementDepth;
        protected final FieldActivator fFieldActivator;
        protected final int fInitialDepth;
        protected int fMatchedDepth;

        public Matcher(XPath xPath, FieldActivator fieldActivator, int i) {
            super(xPath);
            this.fFieldActivator = fieldActivator;
            this.fInitialDepth = i;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.identity.XPathMatcher
        public void startDocumentFragment() {
            super.startDocumentFragment();
            this.fElementDepth = 0;
            this.fMatchedDepth = -1;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.identity.XPathMatcher
        public void startElement(QName qName, XMLAttributes xMLAttributes) {
            super.startElement(qName, xMLAttributes);
            this.fElementDepth++;
            if (isMatched()) {
                this.fMatchedDepth = this.fElementDepth;
                this.fFieldActivator.startValueScopeFor(Selector.this.fIdentityConstraint, this.fInitialDepth);
                int fieldCount = Selector.this.fIdentityConstraint.getFieldCount();
                for (int i = 0; i < fieldCount; i++) {
                    this.fFieldActivator.activateField(Selector.this.fIdentityConstraint.getFieldAt(i), this.fInitialDepth).startElement(qName, xMLAttributes);
                }
            }
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.identity.XPathMatcher
        public void endElement(QName qName, XSTypeDefinition xSTypeDefinition, boolean z, Object obj, short s, ShortList shortList) {
            super.endElement(qName, xSTypeDefinition, z, obj, s, shortList);
            int i = this.fElementDepth;
            this.fElementDepth = i - 1;
            if (i == this.fMatchedDepth) {
                this.fMatchedDepth = -1;
                this.fFieldActivator.endValueScopeFor(Selector.this.fIdentityConstraint, this.fInitialDepth);
            }
        }

        public IdentityConstraint getIdentityConstraint() {
            return Selector.this.fIdentityConstraint;
        }

        public int getInitialDepth() {
            return this.fInitialDepth;
        }
    }
}
