package org.apache.xalan.templates;

import java.io.Serializable;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.xalan.extensions.ExtensionNamespacesManager;
import org.apache.xalan.processor.XSLTSchema;
import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.ref.ExpandedNameTable;
import org.apache.xml.utils.IntStack;
import org.apache.xml.utils.QName;
import org.apache.xpath.XPath;
import org.apache.xpath.XPathContext;
import org.apache.xpath.compiler.PsuedoNames;

public class StylesheetRoot extends StylesheetComposed implements Serializable, Templates {
    static final long serialVersionUID = 3875353123529147855L;
    private HashMap m_attrSets;
    private HashMap m_availElems;
    private transient ComposeState m_composeState;
    private Hashtable m_decimalFormatSymbols;
    private ElemTemplate m_defaultRootRule;
    private ElemTemplate m_defaultRule;
    private ElemTemplate m_defaultTextRule;
    private transient ExtensionNamespacesManager m_extNsMgr;
    private String m_extensionHandlerClass;
    private StylesheetComposed[] m_globalImportList;
    private boolean m_incremental;
    private boolean m_isSecureProcessing;
    private Vector m_keyDecls;
    private Hashtable m_namespaceAliasComposed;
    private boolean m_optimizer;
    private boolean m_outputMethodSet;
    private OutputProperties m_outputProperties;
    XPath m_selectDefault;
    private boolean m_source_location;
    private ElemTemplate m_startRule;
    private TemplateList m_templateList;
    /* access modifiers changed from: private */
    public Vector m_variables;
    private TemplateList m_whiteSpaceInfoList;

    class ComposeState {
        private ExpandedNameTable m_ent = new ExpandedNameTable();
        IntStack m_marks = new IntStack();
        private int m_maxStackFrameSize;
        private Vector m_variableNames = new Vector();

        ComposeState() {
            int size = StylesheetRoot.this.m_variables.size();
            for (int i = 0; i < size; i++) {
                this.m_variableNames.addElement(((ElemVariable) StylesheetRoot.this.m_variables.elementAt(i)).getName());
            }
        }

        public int getQNameID(QName qname) {
            return this.m_ent.getExpandedTypeID(qname.getNamespace(), qname.getLocalName(), 1);
        }

        /* access modifiers changed from: package-private */
        public int addVariableName(QName qname) {
            int pos = this.m_variableNames.size();
            this.m_variableNames.addElement(qname);
            if (this.m_variableNames.size() - getGlobalsSize() > this.m_maxStackFrameSize) {
                this.m_maxStackFrameSize++;
            }
            return pos;
        }

        /* access modifiers changed from: package-private */
        public void resetStackFrameSize() {
            this.m_maxStackFrameSize = 0;
        }

        /* access modifiers changed from: package-private */
        public int getFrameSize() {
            return this.m_maxStackFrameSize;
        }

        /* access modifiers changed from: package-private */
        public int getCurrentStackFrameSize() {
            return this.m_variableNames.size();
        }

        /* access modifiers changed from: package-private */
        public void setCurrentStackFrameSize(int sz) {
            this.m_variableNames.setSize(sz);
        }

        /* access modifiers changed from: package-private */
        public int getGlobalsSize() {
            return StylesheetRoot.this.m_variables.size();
        }

        /* access modifiers changed from: package-private */
        public void pushStackMark() {
            this.m_marks.push(getCurrentStackFrameSize());
        }

        /* access modifiers changed from: package-private */
        public void popStackMark() {
            setCurrentStackFrameSize(this.m_marks.pop());
        }

        /* access modifiers changed from: package-private */
        public Vector getVariableNames() {
            return this.m_variableNames;
        }
    }

    public StylesheetRoot(ErrorListener errorListener) throws TransformerConfigurationException {
        super(null);
        this.m_optimizer = true;
        this.m_incremental = false;
        this.m_source_location = false;
        this.m_isSecureProcessing = false;
        this.m_extNsMgr = null;
        this.m_outputMethodSet = false;
        this.m_extensionHandlerClass = "org.apache.xalan.extensions.ExtensionHandlerExsltFunction";
        setStylesheetRoot(this);
        try {
            XPath xPath = new XPath("node()", this, this, 0, errorListener);
            this.m_selectDefault = xPath;
            initDefaultRule(errorListener);
        } catch (TransformerException se) {
            throw new TransformerConfigurationException(XSLMessages.createMessage(XSLTErrorResources.ER_CANNOT_INIT_DEFAULT_TEMPLATES, null), se);
        }
    }

    public StylesheetRoot(XSLTSchema schema, ErrorListener listener) throws TransformerConfigurationException {
        this(listener);
        this.m_availElems = schema.getElemsAvailable();
    }

    public boolean isRoot() {
        return true;
    }

    public void setSecureProcessing(boolean flag) {
        this.m_isSecureProcessing = flag;
    }

    public boolean isSecureProcessing() {
        return this.m_isSecureProcessing;
    }

    public HashMap getAvailableElements() {
        return this.m_availElems;
    }

    public ExtensionNamespacesManager getExtensionNamespacesManager() {
        if (this.m_extNsMgr == null) {
            this.m_extNsMgr = new ExtensionNamespacesManager();
        }
        return this.m_extNsMgr;
    }

    public Vector getExtensions() {
        if (this.m_extNsMgr != null) {
            return this.m_extNsMgr.getExtensions();
        }
        return null;
    }

    public Transformer newTransformer() {
        return new TransformerImpl(this);
    }

    public Properties getDefaultOutputProps() {
        return this.m_outputProperties.getProperties();
    }

    public Properties getOutputProperties() {
        return (Properties) getDefaultOutputProps().clone();
    }

    public void recompose() throws TransformerException {
        Vector recomposableElements = new Vector();
        if (this.m_globalImportList == null) {
            Vector importList = new Vector();
            addImports(this, true, importList);
            this.m_globalImportList = new StylesheetComposed[importList.size()];
            int i = 0;
            int j = importList.size() - 1;
            while (i < importList.size()) {
                this.m_globalImportList[j] = (StylesheetComposed) importList.elementAt(i);
                this.m_globalImportList[j].recomposeIncludes(this.m_globalImportList[j]);
                this.m_globalImportList[j].recomposeImports();
                i++;
                j--;
            }
        }
        int n = getGlobalImportCount();
        int i2 = 0;
        for (int i3 = 0; i3 < n; i3++) {
            getGlobalImport(i3).recompose(recomposableElements);
        }
        QuickSort2(recomposableElements, 0, recomposableElements.size() - 1);
        this.m_outputProperties = new OutputProperties("");
        this.m_attrSets = new HashMap();
        this.m_decimalFormatSymbols = new Hashtable();
        this.m_keyDecls = new Vector();
        this.m_namespaceAliasComposed = new Hashtable();
        this.m_templateList = new TemplateList();
        this.m_variables = new Vector();
        int i4 = recomposableElements.size() - 1;
        while (true) {
            int i5 = i4;
            if (i5 < 0) {
                break;
            }
            ((ElemTemplateElement) recomposableElements.elementAt(i5)).recompose(this);
            i4 = i5 - 1;
        }
        initComposeState();
        this.m_templateList.compose(this);
        this.m_outputProperties.compose(this);
        this.m_outputProperties.endCompose(this);
        int n2 = getGlobalImportCount();
        while (true) {
            int i6 = i2;
            if (i6 >= n2) {
                break;
            }
            StylesheetComposed imported = getGlobalImport(i6);
            int includedCount = imported.getIncludeCountComposed();
            for (int j2 = -1; j2 < includedCount; j2++) {
                composeTemplates(imported.getIncludeComposed(j2));
            }
            i2 = i6 + 1;
        }
        if (this.m_extNsMgr != null) {
            this.m_extNsMgr.registerUnregisteredNamespaces();
        }
        clearComposeState();
    }

    /* access modifiers changed from: package-private */
    public void composeTemplates(ElemTemplateElement templ) throws TransformerException {
        templ.compose(this);
        for (ElemTemplateElement child = templ.getFirstChildElem(); child != null; child = child.getNextSiblingElem()) {
            composeTemplates(child);
        }
        templ.endCompose(this);
    }

    /* access modifiers changed from: protected */
    public void addImports(Stylesheet stylesheet, boolean addToList, Vector importList) {
        int n = stylesheet.getImportCount();
        if (n > 0) {
            for (int i = 0; i < n; i++) {
                addImports(stylesheet.getImport(i), true, importList);
            }
        }
        int n2 = stylesheet.getIncludeCount();
        if (n2 > 0) {
            for (int i2 = 0; i2 < n2; i2++) {
                addImports(stylesheet.getInclude(i2), false, importList);
            }
        }
        if (addToList) {
            importList.addElement(stylesheet);
        }
    }

    public StylesheetComposed getGlobalImport(int i) {
        return this.m_globalImportList[i];
    }

    public int getGlobalImportCount() {
        if (this.m_globalImportList != null) {
            return this.m_globalImportList.length;
        }
        return 1;
    }

    public int getImportNumber(StylesheetComposed sheet) {
        if (this == sheet) {
            return 0;
        }
        int n = getGlobalImportCount();
        for (int i = 0; i < n; i++) {
            if (sheet == getGlobalImport(i)) {
                return i;
            }
        }
        return -1;
    }

    /* access modifiers changed from: package-private */
    public void recomposeOutput(OutputProperties oprops) throws TransformerException {
        this.m_outputProperties.copyFrom(oprops);
    }

    public OutputProperties getOutputComposed() {
        return this.m_outputProperties;
    }

    public boolean isOutputMethodSet() {
        return this.m_outputMethodSet;
    }

    /* access modifiers changed from: package-private */
    public void recomposeAttributeSets(ElemAttributeSet attrSet) {
        ArrayList attrSetList = (ArrayList) this.m_attrSets.get(attrSet.getName());
        if (attrSetList == null) {
            attrSetList = new ArrayList();
            this.m_attrSets.put(attrSet.getName(), attrSetList);
        }
        attrSetList.add(attrSet);
    }

    public ArrayList getAttributeSetComposed(QName name) throws ArrayIndexOutOfBoundsException {
        return (ArrayList) this.m_attrSets.get(name);
    }

    /* access modifiers changed from: package-private */
    public void recomposeDecimalFormats(DecimalFormatProperties dfp) {
        String themsg;
        DecimalFormatSymbols oldDfs = (DecimalFormatSymbols) this.m_decimalFormatSymbols.get(dfp.getName());
        if (oldDfs == null) {
            this.m_decimalFormatSymbols.put(dfp.getName(), dfp.getDecimalFormatSymbols());
        } else if (!dfp.getDecimalFormatSymbols().equals(oldDfs)) {
            if (dfp.getName().equals(new QName(""))) {
                themsg = XSLMessages.createWarning(XSLTErrorResources.WG_ONE_DEFAULT_XSLDECIMALFORMAT_ALLOWED, new Object[0]);
            } else {
                themsg = XSLMessages.createWarning(XSLTErrorResources.WG_XSLDECIMALFORMAT_NAMES_MUST_BE_UNIQUE, new Object[]{dfp.getName()});
            }
            error(themsg);
        }
    }

    public DecimalFormatSymbols getDecimalFormatComposed(QName name) {
        return (DecimalFormatSymbols) this.m_decimalFormatSymbols.get(name);
    }

    /* access modifiers changed from: package-private */
    public void recomposeKeys(KeyDeclaration keyDecl) {
        this.m_keyDecls.addElement(keyDecl);
    }

    public Vector getKeysComposed() {
        return this.m_keyDecls;
    }

    /* access modifiers changed from: package-private */
    public void recomposeNamespaceAliases(NamespaceAlias nsAlias) {
        this.m_namespaceAliasComposed.put(nsAlias.getStylesheetNamespace(), nsAlias);
    }

    public NamespaceAlias getNamespaceAliasComposed(String uri) {
        return (NamespaceAlias) (this.m_namespaceAliasComposed == null ? null : this.m_namespaceAliasComposed.get(uri));
    }

    /* access modifiers changed from: package-private */
    public void recomposeTemplates(ElemTemplate template) {
        this.m_templateList.setTemplate(template);
    }

    public final TemplateList getTemplateListComposed() {
        return this.m_templateList;
    }

    public final void setTemplateListComposed(TemplateList templateList) {
        this.m_templateList = templateList;
    }

    public ElemTemplate getTemplateComposed(XPathContext xctxt, int targetNode, QName mode, boolean quietConflictWarnings, DTM dtm) throws TransformerException {
        return this.m_templateList.getTemplate(xctxt, targetNode, mode, quietConflictWarnings, dtm);
    }

    public ElemTemplate getTemplateComposed(XPathContext xctxt, int targetNode, QName mode, int maxImportLevel, int endImportLevel, boolean quietConflictWarnings, DTM dtm) throws TransformerException {
        return this.m_templateList.getTemplate(xctxt, targetNode, mode, maxImportLevel, endImportLevel, quietConflictWarnings, dtm);
    }

    public ElemTemplate getTemplateComposed(QName qname) {
        return this.m_templateList.getTemplate(qname);
    }

    /* access modifiers changed from: package-private */
    public void recomposeVariables(ElemVariable elemVar) {
        if (getVariableOrParamComposed(elemVar.getName()) == null) {
            elemVar.setIsTopLevel(true);
            elemVar.setIndex(this.m_variables.size());
            this.m_variables.addElement(elemVar);
        }
    }

    public ElemVariable getVariableOrParamComposed(QName qname) {
        if (this.m_variables != null) {
            int n = this.m_variables.size();
            for (int i = 0; i < n; i++) {
                ElemVariable var = (ElemVariable) this.m_variables.elementAt(i);
                if (var.getName().equals(qname)) {
                    return var;
                }
            }
        }
        return null;
    }

    public Vector getVariablesAndParamsComposed() {
        return this.m_variables;
    }

    /* access modifiers changed from: package-private */
    public void recomposeWhiteSpaceInfo(WhiteSpaceInfo wsi) {
        if (this.m_whiteSpaceInfoList == null) {
            this.m_whiteSpaceInfoList = new TemplateList();
        }
        this.m_whiteSpaceInfoList.setTemplate(wsi);
    }

    public boolean shouldCheckWhitespace() {
        return this.m_whiteSpaceInfoList != null;
    }

    public WhiteSpaceInfo getWhiteSpaceInfo(XPathContext support, int targetElement, DTM dtm) throws TransformerException {
        if (this.m_whiteSpaceInfoList != null) {
            return (WhiteSpaceInfo) this.m_whiteSpaceInfoList.getTemplate(support, targetElement, null, false, dtm);
        }
        return null;
    }

    public boolean shouldStripWhiteSpace(XPathContext support, int targetElement) throws TransformerException {
        if (this.m_whiteSpaceInfoList != null) {
            while (-1 != targetElement) {
                DTM dtm = support.getDTM(targetElement);
                WhiteSpaceInfo info = (WhiteSpaceInfo) this.m_whiteSpaceInfoList.getTemplate(support, targetElement, null, false, dtm);
                if (info != null) {
                    return info.getShouldStripSpace();
                }
                int parent = dtm.getParent(targetElement);
                if (-1 == parent || 1 != dtm.getNodeType(parent)) {
                    targetElement = -1;
                } else {
                    targetElement = parent;
                }
            }
        }
        return false;
    }

    public boolean canStripWhiteSpace() {
        return this.m_whiteSpaceInfoList != null;
    }

    public final ElemTemplate getDefaultTextRule() {
        return this.m_defaultTextRule;
    }

    public final ElemTemplate getDefaultRule() {
        return this.m_defaultRule;
    }

    public final ElemTemplate getDefaultRootRule() {
        return this.m_defaultRootRule;
    }

    public final ElemTemplate getStartRule() {
        return this.m_startRule;
    }

    private void initDefaultRule(ErrorListener errorListener) throws TransformerException {
        this.m_defaultRule = new ElemTemplate();
        this.m_defaultRule.setStylesheet(this);
        XPath xPath = new XPath("*", this, this, 1, errorListener);
        this.m_defaultRule.setMatch(xPath);
        ElemApplyTemplates childrenElement = new ElemApplyTemplates();
        childrenElement.setIsDefaultTemplate(true);
        childrenElement.setSelect(this.m_selectDefault);
        this.m_defaultRule.appendChild((ElemTemplateElement) childrenElement);
        this.m_startRule = this.m_defaultRule;
        this.m_defaultTextRule = new ElemTemplate();
        this.m_defaultTextRule.setStylesheet(this);
        XPath defMatch = new XPath("text() | @*", this, this, 1, errorListener);
        this.m_defaultTextRule.setMatch(defMatch);
        ElemValueOf elemValueOf = new ElemValueOf();
        this.m_defaultTextRule.appendChild((ElemTemplateElement) elemValueOf);
        XPath xPath2 = new XPath(Constants.ATTRVAL_THIS, this, this, 0, errorListener);
        elemValueOf.setSelect(xPath2);
        this.m_defaultRootRule = new ElemTemplate();
        this.m_defaultRootRule.setStylesheet(this);
        XPath defMatch2 = new XPath(PsuedoNames.PSEUDONAME_ROOT, this, this, 1, errorListener);
        this.m_defaultRootRule.setMatch(defMatch2);
        ElemApplyTemplates childrenElement2 = new ElemApplyTemplates();
        childrenElement2.setIsDefaultTemplate(true);
        this.m_defaultRootRule.appendChild((ElemTemplateElement) childrenElement2);
        childrenElement2.setSelect(this.m_selectDefault);
    }

    private void QuickSort2(Vector v, int lo0, int hi0) {
        int lo = lo0;
        int hi = hi0;
        if (hi0 > lo0) {
            ElemTemplateElement midNode = (ElemTemplateElement) v.elementAt((lo0 + hi0) / 2);
            while (lo <= hi) {
                while (lo < hi0 && ((ElemTemplateElement) v.elementAt(lo)).compareTo(midNode) < 0) {
                    lo++;
                }
                while (hi > lo0 && ((ElemTemplateElement) v.elementAt(hi)).compareTo(midNode) > 0) {
                    hi--;
                }
                if (lo <= hi) {
                    v.setElementAt(v.elementAt(hi), lo);
                    v.setElementAt((ElemTemplateElement) v.elementAt(lo), hi);
                    lo++;
                    hi--;
                }
            }
            if (lo0 < hi) {
                QuickSort2(v, lo0, hi);
            }
            if (lo < hi0) {
                QuickSort2(v, lo, hi0);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void initComposeState() {
        this.m_composeState = new ComposeState();
    }

    /* access modifiers changed from: package-private */
    public ComposeState getComposeState() {
        return this.m_composeState;
    }

    private void clearComposeState() {
        this.m_composeState = null;
    }

    public String setExtensionHandlerClass(String handlerClassName) {
        String oldvalue = this.m_extensionHandlerClass;
        this.m_extensionHandlerClass = handlerClassName;
        return oldvalue;
    }

    public String getExtensionHandlerClass() {
        return this.m_extensionHandlerClass;
    }

    public boolean getOptimizer() {
        return this.m_optimizer;
    }

    public void setOptimizer(boolean b) {
        this.m_optimizer = b;
    }

    public boolean getIncremental() {
        return this.m_incremental;
    }

    public boolean getSource_location() {
        return this.m_source_location;
    }

    public void setIncremental(boolean b) {
        this.m_incremental = b;
    }

    public void setSource_location(boolean b) {
        this.m_source_location = b;
    }
}
