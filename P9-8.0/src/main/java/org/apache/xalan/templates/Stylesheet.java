package org.apache.xalan.templates;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;
import org.apache.xml.utils.QName;
import org.apache.xml.utils.StringVector;
import org.apache.xml.utils.SystemIDResolver;
import org.apache.xml.utils.WrappedRuntimeException;

public class Stylesheet extends ElemTemplateElement implements Serializable {
    public static final String STYLESHEET_EXT = ".lxc";
    static final long serialVersionUID = 2085337282743043776L;
    Stack m_DecimalFormatDeclarations;
    private StringVector m_ExcludeResultPrefixs;
    private StringVector m_ExtensionElementURIs;
    private String m_Id;
    private Hashtable m_NonXslTopLevel;
    private String m_Version;
    private String m_XmlnsXsl;
    private Vector m_attributeSets;
    private String m_href = null;
    private Vector m_imports;
    private Vector m_includes;
    private boolean m_isCompatibleMode = false;
    private Vector m_keyDeclarations;
    private Vector m_output;
    private Vector m_prefix_aliases;
    private String m_publicId;
    private Stylesheet m_stylesheetParent;
    private StylesheetRoot m_stylesheetRoot;
    private String m_systemId;
    private Vector m_templates;
    private Vector m_topLevelVariables;
    private Vector m_whitespacePreservingElements;
    private Vector m_whitespaceStrippingElements;

    public Stylesheet(Stylesheet parent) {
        if (parent != null) {
            this.m_stylesheetParent = parent;
            this.m_stylesheetRoot = parent.getStylesheetRoot();
        }
    }

    public Stylesheet getStylesheet() {
        return this;
    }

    public boolean isAggregatedType() {
        return false;
    }

    public boolean isRoot() {
        return false;
    }

    private void readObject(ObjectInputStream stream) throws IOException, TransformerException {
        try {
            stream.defaultReadObject();
        } catch (ClassNotFoundException cnfe) {
            throw new TransformerException(cnfe);
        }
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
    }

    public void setXmlnsXsl(String v) {
        this.m_XmlnsXsl = v;
    }

    public String getXmlnsXsl() {
        return this.m_XmlnsXsl;
    }

    public void setExtensionElementPrefixes(StringVector v) {
        this.m_ExtensionElementURIs = v;
    }

    public String getExtensionElementPrefix(int i) throws ArrayIndexOutOfBoundsException {
        if (this.m_ExtensionElementURIs != null) {
            return this.m_ExtensionElementURIs.elementAt(i);
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    public int getExtensionElementPrefixCount() {
        return this.m_ExtensionElementURIs != null ? this.m_ExtensionElementURIs.size() : 0;
    }

    public boolean containsExtensionElementURI(String uri) {
        if (this.m_ExtensionElementURIs == null) {
            return false;
        }
        return this.m_ExtensionElementURIs.contains(uri);
    }

    public void setExcludeResultPrefixes(StringVector v) {
        this.m_ExcludeResultPrefixs = v;
    }

    public String getExcludeResultPrefix(int i) throws ArrayIndexOutOfBoundsException {
        if (this.m_ExcludeResultPrefixs != null) {
            return this.m_ExcludeResultPrefixs.elementAt(i);
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    public int getExcludeResultPrefixCount() {
        return this.m_ExcludeResultPrefixs != null ? this.m_ExcludeResultPrefixs.size() : 0;
    }

    public boolean containsExcludeResultPrefix(String prefix, String uri) {
        if (this.m_ExcludeResultPrefixs == null || uri == null) {
            return false;
        }
        for (int i = 0; i < this.m_ExcludeResultPrefixs.size(); i++) {
            if (uri.equals(getNamespaceForPrefix(this.m_ExcludeResultPrefixs.elementAt(i)))) {
                return true;
            }
        }
        return false;
    }

    public void setId(String v) {
        this.m_Id = v;
    }

    public String getId() {
        return this.m_Id;
    }

    public void setVersion(String v) {
        this.m_Version = v;
        this.m_isCompatibleMode = Double.valueOf(v).doubleValue() > 1.0d;
    }

    public boolean getCompatibleMode() {
        return this.m_isCompatibleMode;
    }

    public String getVersion() {
        return this.m_Version;
    }

    public void setImport(StylesheetComposed v) {
        if (this.m_imports == null) {
            this.m_imports = new Vector();
        }
        this.m_imports.addElement(v);
    }

    public StylesheetComposed getImport(int i) throws ArrayIndexOutOfBoundsException {
        if (this.m_imports != null) {
            return (StylesheetComposed) this.m_imports.elementAt(i);
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    public int getImportCount() {
        return this.m_imports != null ? this.m_imports.size() : 0;
    }

    public void setInclude(Stylesheet v) {
        if (this.m_includes == null) {
            this.m_includes = new Vector();
        }
        this.m_includes.addElement(v);
    }

    public Stylesheet getInclude(int i) throws ArrayIndexOutOfBoundsException {
        if (this.m_includes != null) {
            return (Stylesheet) this.m_includes.elementAt(i);
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    public int getIncludeCount() {
        return this.m_includes != null ? this.m_includes.size() : 0;
    }

    public void setDecimalFormat(DecimalFormatProperties edf) {
        if (this.m_DecimalFormatDeclarations == null) {
            this.m_DecimalFormatDeclarations = new Stack();
        }
        this.m_DecimalFormatDeclarations.push(edf);
    }

    public DecimalFormatProperties getDecimalFormat(QName name) {
        if (this.m_DecimalFormatDeclarations == null) {
            return null;
        }
        for (int i = getDecimalFormatCount() - 1; i >= 0; i++) {
            DecimalFormatProperties dfp = getDecimalFormat(i);
            if (dfp.getName().equals(name)) {
                return dfp;
            }
        }
        return null;
    }

    public DecimalFormatProperties getDecimalFormat(int i) throws ArrayIndexOutOfBoundsException {
        if (this.m_DecimalFormatDeclarations != null) {
            return (DecimalFormatProperties) this.m_DecimalFormatDeclarations.elementAt(i);
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    public int getDecimalFormatCount() {
        return this.m_DecimalFormatDeclarations != null ? this.m_DecimalFormatDeclarations.size() : 0;
    }

    public void setStripSpaces(WhiteSpaceInfo wsi) {
        if (this.m_whitespaceStrippingElements == null) {
            this.m_whitespaceStrippingElements = new Vector();
        }
        this.m_whitespaceStrippingElements.addElement(wsi);
    }

    public WhiteSpaceInfo getStripSpace(int i) throws ArrayIndexOutOfBoundsException {
        if (this.m_whitespaceStrippingElements != null) {
            return (WhiteSpaceInfo) this.m_whitespaceStrippingElements.elementAt(i);
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    public int getStripSpaceCount() {
        return this.m_whitespaceStrippingElements != null ? this.m_whitespaceStrippingElements.size() : 0;
    }

    public void setPreserveSpaces(WhiteSpaceInfo wsi) {
        if (this.m_whitespacePreservingElements == null) {
            this.m_whitespacePreservingElements = new Vector();
        }
        this.m_whitespacePreservingElements.addElement(wsi);
    }

    public WhiteSpaceInfo getPreserveSpace(int i) throws ArrayIndexOutOfBoundsException {
        if (this.m_whitespacePreservingElements != null) {
            return (WhiteSpaceInfo) this.m_whitespacePreservingElements.elementAt(i);
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    public int getPreserveSpaceCount() {
        return this.m_whitespacePreservingElements != null ? this.m_whitespacePreservingElements.size() : 0;
    }

    public void setOutput(OutputProperties v) {
        if (this.m_output == null) {
            this.m_output = new Vector();
        }
        this.m_output.addElement(v);
    }

    public OutputProperties getOutput(int i) throws ArrayIndexOutOfBoundsException {
        if (this.m_output != null) {
            return (OutputProperties) this.m_output.elementAt(i);
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    public int getOutputCount() {
        return this.m_output != null ? this.m_output.size() : 0;
    }

    public void setKey(KeyDeclaration v) {
        if (this.m_keyDeclarations == null) {
            this.m_keyDeclarations = new Vector();
        }
        this.m_keyDeclarations.addElement(v);
    }

    public KeyDeclaration getKey(int i) throws ArrayIndexOutOfBoundsException {
        if (this.m_keyDeclarations != null) {
            return (KeyDeclaration) this.m_keyDeclarations.elementAt(i);
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    public int getKeyCount() {
        return this.m_keyDeclarations != null ? this.m_keyDeclarations.size() : 0;
    }

    public void setAttributeSet(ElemAttributeSet attrSet) {
        if (this.m_attributeSets == null) {
            this.m_attributeSets = new Vector();
        }
        this.m_attributeSets.addElement(attrSet);
    }

    public ElemAttributeSet getAttributeSet(int i) throws ArrayIndexOutOfBoundsException {
        if (this.m_attributeSets != null) {
            return (ElemAttributeSet) this.m_attributeSets.elementAt(i);
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    public int getAttributeSetCount() {
        return this.m_attributeSets != null ? this.m_attributeSets.size() : 0;
    }

    public void setVariable(ElemVariable v) {
        if (this.m_topLevelVariables == null) {
            this.m_topLevelVariables = new Vector();
        }
        this.m_topLevelVariables.addElement(v);
    }

    public ElemVariable getVariableOrParam(QName qname) {
        if (this.m_topLevelVariables != null) {
            int n = getVariableOrParamCount();
            for (int i = 0; i < n; i++) {
                ElemVariable var = getVariableOrParam(i);
                if (var.getName().equals(qname)) {
                    return var;
                }
            }
        }
        return null;
    }

    public ElemVariable getVariable(QName qname) {
        if (this.m_topLevelVariables != null) {
            int n = getVariableOrParamCount();
            for (int i = 0; i < n; i++) {
                ElemVariable var = getVariableOrParam(i);
                if (var.getXSLToken() == 73 && var.getName().equals(qname)) {
                    return var;
                }
            }
        }
        return null;
    }

    public ElemVariable getVariableOrParam(int i) throws ArrayIndexOutOfBoundsException {
        if (this.m_topLevelVariables != null) {
            return (ElemVariable) this.m_topLevelVariables.elementAt(i);
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    public int getVariableOrParamCount() {
        return this.m_topLevelVariables != null ? this.m_topLevelVariables.size() : 0;
    }

    public void setParam(ElemParam v) {
        setVariable(v);
    }

    public ElemParam getParam(QName qname) {
        if (this.m_topLevelVariables != null) {
            int n = getVariableOrParamCount();
            for (int i = 0; i < n; i++) {
                ElemVariable var = getVariableOrParam(i);
                if (var.getXSLToken() == 41 && var.getName().equals(qname)) {
                    return (ElemParam) var;
                }
            }
        }
        return null;
    }

    public void setTemplate(ElemTemplate v) {
        if (this.m_templates == null) {
            this.m_templates = new Vector();
        }
        this.m_templates.addElement(v);
        v.setStylesheet(this);
    }

    public ElemTemplate getTemplate(int i) throws TransformerException {
        if (this.m_templates != null) {
            return (ElemTemplate) this.m_templates.elementAt(i);
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    public int getTemplateCount() {
        return this.m_templates != null ? this.m_templates.size() : 0;
    }

    public void setNamespaceAlias(NamespaceAlias na) {
        if (this.m_prefix_aliases == null) {
            this.m_prefix_aliases = new Vector();
        }
        this.m_prefix_aliases.addElement(na);
    }

    public NamespaceAlias getNamespaceAlias(int i) throws ArrayIndexOutOfBoundsException {
        if (this.m_prefix_aliases != null) {
            return (NamespaceAlias) this.m_prefix_aliases.elementAt(i);
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    public int getNamespaceAliasCount() {
        return this.m_prefix_aliases != null ? this.m_prefix_aliases.size() : 0;
    }

    public void setNonXslTopLevel(QName name, Object obj) {
        if (this.m_NonXslTopLevel == null) {
            this.m_NonXslTopLevel = new Hashtable();
        }
        this.m_NonXslTopLevel.put(name, obj);
    }

    public Object getNonXslTopLevel(QName name) {
        return this.m_NonXslTopLevel != null ? this.m_NonXslTopLevel.get(name) : null;
    }

    public String getHref() {
        return this.m_href;
    }

    public void setHref(String baseIdent) {
        this.m_href = baseIdent;
    }

    public void setLocaterInfo(SourceLocator locator) {
        if (locator != null) {
            this.m_publicId = locator.getPublicId();
            this.m_systemId = locator.getSystemId();
            if (this.m_systemId != null) {
                try {
                    this.m_href = SystemIDResolver.getAbsoluteURI(this.m_systemId, null);
                } catch (TransformerException e) {
                }
            }
            super.setLocaterInfo(locator);
        }
    }

    public StylesheetRoot getStylesheetRoot() {
        return this.m_stylesheetRoot;
    }

    public void setStylesheetRoot(StylesheetRoot v) {
        this.m_stylesheetRoot = v;
    }

    public Stylesheet getStylesheetParent() {
        return this.m_stylesheetParent;
    }

    public void setStylesheetParent(Stylesheet v) {
        this.m_stylesheetParent = v;
    }

    public StylesheetComposed getStylesheetComposed() {
        Stylesheet sheet = this;
        while (!sheet.isAggregatedType()) {
            sheet = sheet.getStylesheetParent();
        }
        return (StylesheetComposed) sheet;
    }

    public short getNodeType() {
        return (short) 9;
    }

    public int getXSLToken() {
        return 25;
    }

    public String getNodeName() {
        return Constants.ELEMNAME_STYLESHEET_STRING;
    }

    public void replaceTemplate(ElemTemplate v, int i) throws TransformerException {
        if (this.m_templates == null) {
            throw new ArrayIndexOutOfBoundsException();
        }
        replaceChild((ElemTemplateElement) v, (ElemTemplateElement) this.m_templates.elementAt(i));
        this.m_templates.setElementAt(v, i);
        v.setStylesheet(this);
    }

    protected void callChildVisitors(XSLTVisitor visitor, boolean callAttrs) {
        int j;
        int s = getImportCount();
        for (j = 0; j < s; j++) {
            getImport(j).callVisitors(visitor);
        }
        s = getIncludeCount();
        for (j = 0; j < s; j++) {
            getInclude(j).callVisitors(visitor);
        }
        s = getOutputCount();
        for (j = 0; j < s; j++) {
            visitor.visitTopLevelInstruction(getOutput(j));
        }
        s = getAttributeSetCount();
        for (j = 0; j < s; j++) {
            ElemAttributeSet attrSet = getAttributeSet(j);
            if (visitor.visitTopLevelInstruction(attrSet)) {
                attrSet.callChildVisitors(visitor);
            }
        }
        s = getDecimalFormatCount();
        for (j = 0; j < s; j++) {
            visitor.visitTopLevelInstruction(getDecimalFormat(j));
        }
        s = getKeyCount();
        for (j = 0; j < s; j++) {
            visitor.visitTopLevelInstruction(getKey(j));
        }
        s = getNamespaceAliasCount();
        for (j = 0; j < s; j++) {
            visitor.visitTopLevelInstruction(getNamespaceAlias(j));
        }
        s = getTemplateCount();
        j = 0;
        while (j < s) {
            try {
                ElemTemplate template = getTemplate(j);
                if (visitor.visitTopLevelInstruction(template)) {
                    template.callChildVisitors(visitor);
                }
                j++;
            } catch (TransformerException te) {
                throw new WrappedRuntimeException(te);
            }
        }
        s = getVariableOrParamCount();
        for (j = 0; j < s; j++) {
            ElemVariable var = getVariableOrParam(j);
            if (visitor.visitTopLevelVariableOrParamDecl(var)) {
                var.callChildVisitors(visitor);
            }
        }
        s = getStripSpaceCount();
        for (j = 0; j < s; j++) {
            visitor.visitTopLevelInstruction(getStripSpace(j));
        }
        s = getPreserveSpaceCount();
        for (j = 0; j < s; j++) {
            visitor.visitTopLevelInstruction(getPreserveSpace(j));
        }
        if (this.m_NonXslTopLevel != null) {
            Enumeration elements = this.m_NonXslTopLevel.elements();
            while (elements.hasMoreElements()) {
                ElemTemplateElement elem = (ElemTemplateElement) elements.nextElement();
                if (visitor.visitTopLevelInstruction(elem)) {
                    elem.callChildVisitors(visitor);
                }
            }
        }
    }

    protected boolean accept(XSLTVisitor visitor) {
        return visitor.visitStylesheet(this);
    }
}
