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
    private Vector m_variables;
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

        int addVariableName(QName qname) {
            int pos = this.m_variableNames.size();
            this.m_variableNames.addElement(qname);
            if (this.m_variableNames.size() - getGlobalsSize() > this.m_maxStackFrameSize) {
                this.m_maxStackFrameSize++;
            }
            return pos;
        }

        void resetStackFrameSize() {
            this.m_maxStackFrameSize = 0;
        }

        int getFrameSize() {
            return this.m_maxStackFrameSize;
        }

        int getCurrentStackFrameSize() {
            return this.m_variableNames.size();
        }

        void setCurrentStackFrameSize(int sz) {
            this.m_variableNames.setSize(sz);
        }

        int getGlobalsSize() {
            return StylesheetRoot.this.m_variables.size();
        }

        void pushStackMark() {
            this.m_marks.push(getCurrentStackFrameSize());
        }

        void popStackMark() {
            setCurrentStackFrameSize(this.m_marks.pop());
        }

        Vector getVariableNames() {
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
            this.m_selectDefault = new XPath("node()", this, this, 0, errorListener);
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
        return this.m_extNsMgr != null ? this.m_extNsMgr.getExtensions() : null;
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
        int i;
        int j;
        Vector recomposableElements = new Vector();
        if (this.m_globalImportList == null) {
            Vector importList = new Vector();
            addImports(this, true, importList);
            this.m_globalImportList = new StylesheetComposed[importList.size()];
            i = 0;
            j = importList.size() - 1;
            while (i < importList.size()) {
                this.m_globalImportList[j] = (StylesheetComposed) importList.elementAt(i);
                this.m_globalImportList[j].recomposeIncludes(this.m_globalImportList[j]);
                int j2 = j - 1;
                this.m_globalImportList[j].recomposeImports();
                i++;
                j = j2;
            }
        }
        int n = getGlobalImportCount();
        for (i = 0; i < n; i++) {
            getGlobalImport(i).recompose(recomposableElements);
        }
        QuickSort2(recomposableElements, 0, recomposableElements.size() - 1);
        this.m_outputProperties = new OutputProperties("");
        this.m_attrSets = new HashMap();
        this.m_decimalFormatSymbols = new Hashtable();
        this.m_keyDecls = new Vector();
        this.m_namespaceAliasComposed = new Hashtable();
        this.m_templateList = new TemplateList();
        this.m_variables = new Vector();
        for (i = recomposableElements.size() - 1; i >= 0; i--) {
            ((ElemTemplateElement) recomposableElements.elementAt(i)).recompose(this);
        }
        initComposeState();
        this.m_templateList.compose(this);
        this.m_outputProperties.compose(this);
        this.m_outputProperties.endCompose(this);
        n = getGlobalImportCount();
        for (i = 0; i < n; i++) {
            StylesheetComposed imported = getGlobalImport(i);
            int includedCount = imported.getIncludeCountComposed();
            for (j = -1; j < includedCount; j++) {
                composeTemplates(imported.getIncludeComposed(j));
            }
        }
        if (this.m_extNsMgr != null) {
            this.m_extNsMgr.registerUnregisteredNamespaces();
        }
        clearComposeState();
    }

    void composeTemplates(ElemTemplateElement templ) throws TransformerException {
        templ.compose(this);
        for (ElemTemplateElement child = templ.getFirstChildElem(); child != null; child = child.getNextSiblingElem()) {
            composeTemplates(child);
        }
        templ.endCompose(this);
    }

    protected void addImports(Stylesheet stylesheet, boolean addToList, Vector importList) {
        int i;
        int n = stylesheet.getImportCount();
        if (n > 0) {
            for (i = 0; i < n; i++) {
                addImports(stylesheet.getImport(i), true, importList);
            }
        }
        n = stylesheet.getIncludeCount();
        if (n > 0) {
            for (i = 0; i < n; i++) {
                addImports(stylesheet.getInclude(i), false, importList);
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

    void recomposeOutput(OutputProperties oprops) throws TransformerException {
        this.m_outputProperties.copyFrom(oprops);
    }

    public OutputProperties getOutputComposed() {
        return this.m_outputProperties;
    }

    public boolean isOutputMethodSet() {
        return this.m_outputMethodSet;
    }

    void recomposeAttributeSets(ElemAttributeSet attrSet) {
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

    void recomposeDecimalFormats(DecimalFormatProperties dfp) {
        DecimalFormatSymbols oldDfs = (DecimalFormatSymbols) this.m_decimalFormatSymbols.get(dfp.getName());
        if (oldDfs == null) {
            this.m_decimalFormatSymbols.put(dfp.getName(), dfp.getDecimalFormatSymbols());
        } else if (!dfp.getDecimalFormatSymbols().equals(oldDfs)) {
            String themsg;
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

    void recomposeKeys(KeyDeclaration keyDecl) {
        this.m_keyDecls.addElement(keyDecl);
    }

    public Vector getKeysComposed() {
        return this.m_keyDecls;
    }

    void recomposeNamespaceAliases(NamespaceAlias nsAlias) {
        this.m_namespaceAliasComposed.put(nsAlias.getStylesheetNamespace(), nsAlias);
    }

    public NamespaceAlias getNamespaceAliasComposed(String uri) {
        Object obj = null;
        if (this.m_namespaceAliasComposed != null) {
            obj = this.m_namespaceAliasComposed.get(uri);
        }
        return (NamespaceAlias) obj;
    }

    void recomposeTemplates(ElemTemplate template) {
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

    void recomposeVariables(ElemVariable elemVar) {
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

    void recomposeWhiteSpaceInfo(WhiteSpaceInfo wsi) {
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
                if (-1 == parent || (short) 1 != dtm.getNodeType(parent)) {
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
        this.m_defaultRule.setMatch(new XPath("*", this, this, 1, errorListener));
        ElemApplyTemplates childrenElement = new ElemApplyTemplates();
        childrenElement.setIsDefaultTemplate(true);
        childrenElement.setSelect(this.m_selectDefault);
        this.m_defaultRule.appendChild((ElemTemplateElement) childrenElement);
        this.m_startRule = this.m_defaultRule;
        this.m_defaultTextRule = new ElemTemplate();
        this.m_defaultTextRule.setStylesheet(this);
        this.m_defaultTextRule.setMatch(new XPath("text() | @*", this, this, 1, errorListener));
        ElemValueOf elemValueOf = new ElemValueOf();
        this.m_defaultTextRule.appendChild((ElemTemplateElement) elemValueOf);
        elemValueOf.setSelect(new XPath(Constants.ATTRVAL_THIS, this, this, 0, errorListener));
        this.m_defaultRootRule = new ElemTemplate();
        this.m_defaultRootRule.setStylesheet(this);
        this.m_defaultRootRule.setMatch(new XPath(PsuedoNames.PSEUDONAME_ROOT, this, this, 1, errorListener));
        childrenElement = new ElemApplyTemplates();
        childrenElement.setIsDefaultTemplate(true);
        this.m_defaultRootRule.appendChild((ElemTemplateElement) childrenElement);
        childrenElement.setSelect(this.m_selectDefault);
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
                    ElemTemplateElement node = (ElemTemplateElement) v.elementAt(lo);
                    v.setElementAt(v.elementAt(hi), lo);
                    v.setElementAt(node, hi);
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

    void initComposeState() {
        this.m_composeState = new ComposeState();
    }

    ComposeState getComposeState() {
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
