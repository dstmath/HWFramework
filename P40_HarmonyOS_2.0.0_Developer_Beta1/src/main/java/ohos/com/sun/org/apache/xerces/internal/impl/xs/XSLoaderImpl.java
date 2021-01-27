package ohos.com.sun.org.apache.xerces.internal.impl.xs;

import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.XSGrammarPool;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.Grammar;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarDescription;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XSGrammar;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import ohos.com.sun.org.apache.xerces.internal.xs.LSInputList;
import ohos.com.sun.org.apache.xerces.internal.xs.StringList;
import ohos.com.sun.org.apache.xerces.internal.xs.XSLoader;
import ohos.com.sun.org.apache.xerces.internal.xs.XSModel;
import ohos.com.sun.org.apache.xerces.internal.xs.XSNamedMap;
import ohos.com.sun.org.apache.xerces.internal.xs.XSObjectList;
import ohos.com.sun.org.apache.xerces.internal.xs.XSTypeDefinition;
import ohos.org.w3c.dom.DOMConfiguration;
import ohos.org.w3c.dom.DOMException;
import ohos.org.w3c.dom.DOMStringList;
import ohos.org.w3c.dom.ls.LSInput;

public final class XSLoaderImpl implements XSLoader, DOMConfiguration {
    private final XSGrammarPool fGrammarPool = new XSGrammarMerger();
    private final XMLSchemaLoader fSchemaLoader = new XMLSchemaLoader();

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSLoader
    public DOMConfiguration getConfig() {
        return this;
    }

    public XSLoaderImpl() {
        this.fSchemaLoader.setProperty("http://apache.org/xml/properties/internal/grammar-pool", this.fGrammarPool);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSLoader
    public XSModel loadURIList(StringList stringList) {
        int length = stringList.getLength();
        try {
            this.fGrammarPool.clear();
            for (int i = 0; i < length; i++) {
                this.fSchemaLoader.loadGrammar(new XMLInputSource(null, stringList.item(i), null));
            }
            return this.fGrammarPool.toXSModel();
        } catch (Exception e) {
            this.fSchemaLoader.reportDOMFatalError(e);
            return null;
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSLoader
    public XSModel loadInputList(LSInputList lSInputList) {
        int length = lSInputList.getLength();
        try {
            this.fGrammarPool.clear();
            for (int i = 0; i < length; i++) {
                this.fSchemaLoader.loadGrammar(this.fSchemaLoader.dom2xmlInputSource(lSInputList.item(i)));
            }
            return this.fGrammarPool.toXSModel();
        } catch (Exception e) {
            this.fSchemaLoader.reportDOMFatalError(e);
            return null;
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSLoader
    public XSModel loadURI(String str) {
        try {
            this.fGrammarPool.clear();
            return ((XSGrammar) this.fSchemaLoader.loadGrammar(new XMLInputSource(null, str, null))).toXSModel();
        } catch (Exception e) {
            this.fSchemaLoader.reportDOMFatalError(e);
            return null;
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xs.XSLoader
    public XSModel load(LSInput lSInput) {
        try {
            this.fGrammarPool.clear();
            return ((XSGrammar) this.fSchemaLoader.loadGrammar(this.fSchemaLoader.dom2xmlInputSource(lSInput))).toXSModel();
        } catch (Exception e) {
            this.fSchemaLoader.reportDOMFatalError(e);
            return null;
        }
    }

    public void setParameter(String str, Object obj) throws DOMException {
        this.fSchemaLoader.setParameter(str, obj);
    }

    public Object getParameter(String str) throws DOMException {
        return this.fSchemaLoader.getParameter(str);
    }

    public boolean canSetParameter(String str, Object obj) {
        return this.fSchemaLoader.canSetParameter(str, obj);
    }

    public DOMStringList getParameterNames() {
        return this.fSchemaLoader.getParameterNames();
    }

    private static final class XSGrammarMerger extends XSGrammarPool {
        @Override // ohos.com.sun.org.apache.xerces.internal.util.XMLGrammarPoolImpl
        public boolean containsGrammar(XMLGrammarDescription xMLGrammarDescription) {
            return false;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.util.XMLGrammarPoolImpl
        public Grammar getGrammar(XMLGrammarDescription xMLGrammarDescription) {
            return null;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.util.XMLGrammarPoolImpl, ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool
        public Grammar retrieveGrammar(XMLGrammarDescription xMLGrammarDescription) {
            return null;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.util.XMLGrammarPoolImpl, ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool
        public Grammar[] retrieveInitialGrammarSet(String str) {
            return new Grammar[0];
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.util.XMLGrammarPoolImpl
        public void putGrammar(Grammar grammar) {
            SchemaGrammar schemaGrammar = toSchemaGrammar(super.getGrammar(grammar.getGrammarDescription()));
            if (schemaGrammar != null) {
                SchemaGrammar schemaGrammar2 = toSchemaGrammar(grammar);
                if (schemaGrammar2 != null) {
                    mergeSchemaGrammars(schemaGrammar, schemaGrammar2);
                    return;
                }
                return;
            }
            super.putGrammar(grammar);
        }

        private SchemaGrammar toSchemaGrammar(Grammar grammar) {
            if (grammar instanceof SchemaGrammar) {
                return (SchemaGrammar) grammar;
            }
            return null;
        }

        private void mergeSchemaGrammars(SchemaGrammar schemaGrammar, SchemaGrammar schemaGrammar2) {
            XSNamedMap components = schemaGrammar2.getComponents(2);
            int length = components.getLength();
            for (int i = 0; i < length; i++) {
                XSElementDecl xSElementDecl = (XSElementDecl) components.item(i);
                if (schemaGrammar.getGlobalElementDecl(xSElementDecl.getName()) == null) {
                    schemaGrammar.addGlobalElementDecl(xSElementDecl);
                }
            }
            XSNamedMap components2 = schemaGrammar2.getComponents(1);
            int length2 = components2.getLength();
            for (int i2 = 0; i2 < length2; i2++) {
                XSAttributeDecl xSAttributeDecl = (XSAttributeDecl) components2.item(i2);
                if (schemaGrammar.getGlobalAttributeDecl(xSAttributeDecl.getName()) == null) {
                    schemaGrammar.addGlobalAttributeDecl(xSAttributeDecl);
                }
            }
            XSNamedMap components3 = schemaGrammar2.getComponents(3);
            int length3 = components3.getLength();
            for (int i3 = 0; i3 < length3; i3++) {
                XSTypeDefinition xSTypeDefinition = (XSTypeDefinition) components3.item(i3);
                if (schemaGrammar.getGlobalTypeDecl(xSTypeDefinition.getName()) == null) {
                    schemaGrammar.addGlobalTypeDecl(xSTypeDefinition);
                }
            }
            XSNamedMap components4 = schemaGrammar2.getComponents(5);
            int length4 = components4.getLength();
            for (int i4 = 0; i4 < length4; i4++) {
                XSAttributeGroupDecl xSAttributeGroupDecl = (XSAttributeGroupDecl) components4.item(i4);
                if (schemaGrammar.getGlobalAttributeGroupDecl(xSAttributeGroupDecl.getName()) == null) {
                    schemaGrammar.addGlobalAttributeGroupDecl(xSAttributeGroupDecl);
                }
            }
            XSNamedMap components5 = schemaGrammar2.getComponents(7);
            int length5 = components5.getLength();
            for (int i5 = 0; i5 < length5; i5++) {
                XSGroupDecl xSGroupDecl = (XSGroupDecl) components5.item(i5);
                if (schemaGrammar.getGlobalGroupDecl(xSGroupDecl.getName()) == null) {
                    schemaGrammar.addGlobalGroupDecl(xSGroupDecl);
                }
            }
            XSNamedMap components6 = schemaGrammar2.getComponents(11);
            int length6 = components6.getLength();
            for (int i6 = 0; i6 < length6; i6++) {
                XSNotationDecl xSNotationDecl = (XSNotationDecl) components6.item(i6);
                if (schemaGrammar.getGlobalNotationDecl(xSNotationDecl.getName()) == null) {
                    schemaGrammar.addGlobalNotationDecl(xSNotationDecl);
                }
            }
            XSObjectList annotations = schemaGrammar2.getAnnotations();
            int length7 = annotations.getLength();
            for (int i7 = 0; i7 < length7; i7++) {
                schemaGrammar.addAnnotation((XSAnnotationImpl) annotations.item(i7));
            }
        }
    }
}
