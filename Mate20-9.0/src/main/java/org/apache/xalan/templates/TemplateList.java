package org.apache.xalan.templates;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xml.dtm.DTM;
import org.apache.xml.utils.QName;
import org.apache.xpath.Expression;
import org.apache.xpath.XPath;
import org.apache.xpath.XPathContext;
import org.apache.xpath.compiler.PsuedoNames;
import org.apache.xpath.patterns.NodeTest;
import org.apache.xpath.patterns.StepPattern;
import org.apache.xpath.patterns.UnionPattern;

public class TemplateList implements Serializable {
    static final boolean DEBUG = false;
    static final long serialVersionUID = 5803675288911728791L;
    private TemplateSubPatternAssociation m_commentPatterns = null;
    private TemplateSubPatternAssociation m_docPatterns = null;
    /* access modifiers changed from: private */
    public Hashtable m_namedTemplates = new Hashtable(89);
    /* access modifiers changed from: private */
    public Hashtable m_patternTable = new Hashtable(89);
    private TemplateSubPatternAssociation m_textPatterns = null;
    private TemplateSubPatternAssociation m_wildCardPatterns = null;

    public class TemplateWalker {
        private TemplateSubPatternAssociation curPattern;
        private Enumeration hashIterator;
        private boolean inPatterns;
        private Hashtable m_compilerCache;

        private TemplateWalker() {
            this.m_compilerCache = new Hashtable();
            this.hashIterator = TemplateList.this.m_patternTable.elements();
            this.inPatterns = true;
            this.curPattern = null;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v8, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v4, resolved type: org.apache.xalan.templates.ElemTemplate} */
        /* JADX WARNING: Multi-variable type inference failed */
        public ElemTemplate next() {
            ElemTemplate retValue = null;
            do {
                if (this.inPatterns) {
                    if (this.curPattern != null) {
                        this.curPattern = this.curPattern.getNext();
                    }
                    if (this.curPattern != null) {
                        retValue = this.curPattern.getTemplate();
                    } else if (this.hashIterator.hasMoreElements()) {
                        this.curPattern = (TemplateSubPatternAssociation) this.hashIterator.nextElement();
                        retValue = this.curPattern.getTemplate();
                    } else {
                        this.inPatterns = false;
                        this.hashIterator = TemplateList.this.m_namedTemplates.elements();
                    }
                }
                if (!this.inPatterns) {
                    if (!this.hashIterator.hasMoreElements()) {
                        return null;
                    }
                    retValue = this.hashIterator.nextElement();
                }
            } while (((ElemTemplate) this.m_compilerCache.get(new Integer(retValue.getUid()))) != null);
            this.m_compilerCache.put(new Integer(retValue.getUid()), retValue);
            return retValue;
        }
    }

    public void setTemplate(ElemTemplate template) {
        XPath matchXPath = template.getMatch();
        if (template.getName() == null && matchXPath == null) {
            template.error(XSLTErrorResources.ER_NEED_NAME_OR_MATCH_ATTRIB, new Object[]{"xsl:template"});
        }
        if (template.getName() != null) {
            ElemTemplate existingTemplate = (ElemTemplate) this.m_namedTemplates.get(template.getName());
            if (existingTemplate == null) {
                this.m_namedTemplates.put(template.getName(), template);
            } else {
                int existingPrecedence = existingTemplate.getStylesheetComposed().getImportCountComposed();
                int newPrecedence = template.getStylesheetComposed().getImportCountComposed();
                if (newPrecedence > existingPrecedence) {
                    this.m_namedTemplates.put(template.getName(), template);
                } else if (newPrecedence == existingPrecedence) {
                    template.error(XSLTErrorResources.ER_DUPLICATE_NAMED_TEMPLATE, new Object[]{template.getName()});
                }
            }
        }
        if (matchXPath != null) {
            Expression matchExpr = matchXPath.getExpression();
            if (matchExpr instanceof StepPattern) {
                insertPatternInTable((StepPattern) matchExpr, template);
            } else if (matchExpr instanceof UnionPattern) {
                for (StepPattern insertPatternInTable : ((UnionPattern) matchExpr).getPatterns()) {
                    insertPatternInTable(insertPatternInTable, template);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpAssociationTables() {
        Enumeration associations = this.m_patternTable.elements();
        while (associations.hasMoreElements()) {
            for (TemplateSubPatternAssociation head = (TemplateSubPatternAssociation) associations.nextElement(); head != null; head = head.getNext()) {
                PrintStream printStream = System.out;
                printStream.print("(" + head.getTargetString() + ", " + head.getPattern() + ")");
            }
            System.out.println("\n.....");
        }
        System.out.print("wild card list: ");
        for (TemplateSubPatternAssociation head2 = this.m_wildCardPatterns; head2 != null; head2 = head2.getNext()) {
            PrintStream printStream2 = System.out;
            printStream2.print("(" + head2.getTargetString() + ", " + head2.getPattern() + ")");
        }
        System.out.println("\n.....");
    }

    public void compose(StylesheetRoot sroot) {
        if (this.m_wildCardPatterns != null) {
            Enumeration associations = this.m_patternTable.elements();
            while (associations.hasMoreElements()) {
                TemplateSubPatternAssociation head = (TemplateSubPatternAssociation) associations.nextElement();
                for (TemplateSubPatternAssociation wild = this.m_wildCardPatterns; wild != null; wild = wild.getNext()) {
                    try {
                        head = insertAssociationIntoList(head, (TemplateSubPatternAssociation) wild.clone(), true);
                    } catch (CloneNotSupportedException e) {
                    }
                }
            }
        }
    }

    private TemplateSubPatternAssociation insertAssociationIntoList(TemplateSubPatternAssociation head, TemplateSubPatternAssociation item, boolean isWildCardInsert) {
        TemplateSubPatternAssociation next;
        boolean insertBefore;
        double priority = getPriorityOrScore(item);
        int importLevel = item.getImportLevel();
        int docOrder = item.getDocOrderPos();
        TemplateSubPatternAssociation insertPoint = head;
        while (true) {
            next = insertPoint.getNext();
            if (next != null) {
                double workPriority = getPriorityOrScore(next);
                if (importLevel <= next.getImportLevel()) {
                    if (importLevel >= next.getImportLevel()) {
                        if (priority > workPriority) {
                            break;
                        } else if (priority < workPriority) {
                            insertPoint = next;
                        } else if (docOrder >= next.getDocOrderPos()) {
                            break;
                        } else {
                            insertPoint = next;
                        }
                    } else {
                        insertPoint = next;
                    }
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        if (next == null || insertPoint == head) {
            double workPriority2 = getPriorityOrScore(insertPoint);
            if (importLevel > insertPoint.getImportLevel()) {
                insertBefore = true;
            } else if (importLevel < insertPoint.getImportLevel()) {
                insertBefore = false;
            } else if (priority > workPriority2) {
                insertBefore = true;
            } else if (priority < workPriority2) {
                insertBefore = false;
            } else if (docOrder >= insertPoint.getDocOrderPos()) {
                insertBefore = true;
            } else {
                insertBefore = false;
            }
        } else {
            insertBefore = false;
        }
        boolean insertBefore2 = insertBefore;
        if (isWildCardInsert) {
            if (insertBefore2) {
                item.setNext(insertPoint);
                String key = insertPoint.getTargetString();
                item.setTargetString(key);
                putHead(key, item);
                return item;
            }
            item.setNext(next);
            insertPoint.setNext(item);
            return head;
        } else if (insertBefore2) {
            item.setNext(insertPoint);
            if (insertPoint.isWild() || item.isWild()) {
                this.m_wildCardPatterns = item;
            } else {
                putHead(item.getTargetString(), item);
            }
            return item;
        } else {
            item.setNext(next);
            insertPoint.setNext(item);
            return head;
        }
    }

    private void insertPatternInTable(StepPattern pattern, ElemTemplate template) {
        TemplateSubPatternAssociation head;
        String target = pattern.getTargetString();
        if (target != null) {
            TemplateSubPatternAssociation association = new TemplateSubPatternAssociation(template, pattern, template.getMatch().getPatternString());
            boolean isWildCard = association.isWild();
            if (isWildCard) {
                head = this.m_wildCardPatterns;
            } else {
                head = getHead(target);
            }
            if (head != null) {
                insertAssociationIntoList(head, association, false);
            } else if (isWildCard) {
                this.m_wildCardPatterns = association;
            } else {
                putHead(target, association);
            }
        }
    }

    private double getPriorityOrScore(TemplateSubPatternAssociation matchPat) {
        double priority = matchPat.getTemplate().getPriority();
        if (priority == Double.NEGATIVE_INFINITY) {
            StepPattern stepPattern = matchPat.getStepPattern();
            if (stepPattern instanceof NodeTest) {
                return stepPattern.getDefaultScore();
            }
        }
        return priority;
    }

    public ElemTemplate getTemplate(QName qname) {
        return (ElemTemplate) this.m_namedTemplates.get(qname);
    }

    public TemplateSubPatternAssociation getHead(XPathContext xctxt, int targetNode, DTM dtm) {
        TemplateSubPatternAssociation head;
        switch (dtm.getNodeType(targetNode)) {
            case 1:
            case 2:
                head = (TemplateSubPatternAssociation) this.m_patternTable.get(dtm.getLocalName(targetNode));
                break;
            case 3:
            case 4:
                head = this.m_textPatterns;
                break;
            case 5:
            case 6:
                head = (TemplateSubPatternAssociation) this.m_patternTable.get(dtm.getNodeName(targetNode));
                break;
            case 7:
                head = (TemplateSubPatternAssociation) this.m_patternTable.get(dtm.getLocalName(targetNode));
                break;
            case 8:
                head = this.m_commentPatterns;
                break;
            case 9:
            case 11:
                head = this.m_docPatterns;
                break;
            default:
                head = (TemplateSubPatternAssociation) this.m_patternTable.get(dtm.getNodeName(targetNode));
                break;
        }
        return head == null ? this.m_wildCardPatterns : head;
    }

    public ElemTemplate getTemplateFast(XPathContext xctxt, int targetNode, int expTypeID, QName mode, int maxImportLevel, boolean quietConflictWarnings, DTM dtm) throws TransformerException {
        TemplateSubPatternAssociation head;
        TemplateSubPatternAssociation next;
        switch (dtm.getNodeType(targetNode)) {
            case 1:
            case 2:
                head = (TemplateSubPatternAssociation) this.m_patternTable.get(dtm.getLocalNameFromExpandedNameID(expTypeID));
                break;
            case 3:
            case 4:
                head = this.m_textPatterns;
                break;
            case 5:
            case 6:
                head = (TemplateSubPatternAssociation) this.m_patternTable.get(dtm.getNodeName(targetNode));
                break;
            case 7:
                head = (TemplateSubPatternAssociation) this.m_patternTable.get(dtm.getLocalName(targetNode));
                break;
            case 8:
                head = this.m_commentPatterns;
                break;
            case 9:
            case 11:
                head = this.m_docPatterns;
                break;
            default:
                head = (TemplateSubPatternAssociation) this.m_patternTable.get(dtm.getNodeName(targetNode));
                break;
        }
        if (head == null) {
            head = this.m_wildCardPatterns;
            if (head == null) {
                return null;
            }
        }
        xctxt.pushNamespaceContextNull();
        do {
            if (maxImportLevel > -1) {
                try {
                    if (head.getImportLevel() > maxImportLevel) {
                        next = head.getNext();
                        head = next;
                    }
                } catch (Throwable th) {
                    xctxt.popNamespaceContext();
                    throw th;
                }
            }
            ElemTemplate template = head.getTemplate();
            xctxt.setNamespaceContext(template);
            if (head.m_stepPattern.execute(xctxt, targetNode, dtm, expTypeID) != NodeTest.SCORE_NONE && head.matchMode(mode)) {
                if (quietConflictWarnings) {
                    checkConflicts(head, xctxt, targetNode, mode);
                }
                xctxt.popNamespaceContext();
                return template;
            }
            next = head.getNext();
            head = next;
        } while (next != null);
        xctxt.popNamespaceContext();
        return null;
    }

    public ElemTemplate getTemplate(XPathContext xctxt, int targetNode, QName mode, boolean quietConflictWarnings, DTM dtm) throws TransformerException {
        TemplateSubPatternAssociation next;
        TemplateSubPatternAssociation head = getHead(xctxt, targetNode, dtm);
        if (head != null) {
            xctxt.pushNamespaceContextNull();
            xctxt.pushCurrentNodeAndExpression(targetNode, targetNode);
            do {
                ElemTemplate template = head.getTemplate();
                xctxt.setNamespaceContext(template);
                if (head.m_stepPattern.execute(xctxt, targetNode) == NodeTest.SCORE_NONE || !head.matchMode(mode)) {
                    try {
                        next = head.getNext();
                        head = next;
                    } finally {
                        xctxt.popCurrentNodeAndExpression();
                        xctxt.popNamespaceContext();
                    }
                } else {
                    if (quietConflictWarnings) {
                        checkConflicts(head, xctxt, targetNode, mode);
                    }
                    xctxt.popCurrentNodeAndExpression();
                    xctxt.popNamespaceContext();
                    return template;
                }
            } while (next != null);
        }
        return null;
    }

    public ElemTemplate getTemplate(XPathContext xctxt, int targetNode, QName mode, int maxImportLevel, int endImportLevel, boolean quietConflictWarnings, DTM dtm) throws TransformerException {
        TemplateSubPatternAssociation next;
        TemplateSubPatternAssociation head = getHead(xctxt, targetNode, dtm);
        if (head != null) {
            xctxt.pushNamespaceContextNull();
            xctxt.pushCurrentNodeAndExpression(targetNode, targetNode);
            do {
                if (maxImportLevel > -1) {
                    try {
                        if (head.getImportLevel() > maxImportLevel) {
                            next = head.getNext();
                            head = next;
                        }
                    } catch (Throwable th) {
                        xctxt.popCurrentNodeAndExpression();
                        xctxt.popNamespaceContext();
                        throw th;
                    }
                }
                if (head.getImportLevel() <= maxImportLevel - endImportLevel) {
                    xctxt.popCurrentNodeAndExpression();
                    xctxt.popNamespaceContext();
                    return null;
                }
                ElemTemplate template = head.getTemplate();
                xctxt.setNamespaceContext(template);
                if (head.m_stepPattern.execute(xctxt, targetNode) != NodeTest.SCORE_NONE && head.matchMode(mode)) {
                    if (quietConflictWarnings) {
                        checkConflicts(head, xctxt, targetNode, mode);
                    }
                    xctxt.popCurrentNodeAndExpression();
                    xctxt.popNamespaceContext();
                    return template;
                }
                next = head.getNext();
                head = next;
            } while (next != null);
            xctxt.popCurrentNodeAndExpression();
            xctxt.popNamespaceContext();
        }
        return null;
    }

    public TemplateWalker getWalker() {
        return new TemplateWalker();
    }

    private void checkConflicts(TemplateSubPatternAssociation head, XPathContext xctxt, int targetNode, QName mode) {
    }

    private void addObjectIfNotFound(Object obj, Vector v) {
        int n = v.size();
        boolean addIt = true;
        int i = 0;
        while (true) {
            if (i >= n) {
                break;
            } else if (v.elementAt(i) == obj) {
                addIt = false;
                break;
            } else {
                i++;
            }
        }
        if (addIt) {
            v.addElement(obj);
        }
    }

    private Hashtable getNamedTemplates() {
        return this.m_namedTemplates;
    }

    private void setNamedTemplates(Hashtable v) {
        this.m_namedTemplates = v;
    }

    private TemplateSubPatternAssociation getHead(String key) {
        return (TemplateSubPatternAssociation) this.m_patternTable.get(key);
    }

    private void putHead(String key, TemplateSubPatternAssociation assoc) {
        if (key.equals(PsuedoNames.PSEUDONAME_TEXT)) {
            this.m_textPatterns = assoc;
        } else if (key.equals(PsuedoNames.PSEUDONAME_ROOT)) {
            this.m_docPatterns = assoc;
        } else if (key.equals(PsuedoNames.PSEUDONAME_COMMENT)) {
            this.m_commentPatterns = assoc;
        }
        this.m_patternTable.put(key, assoc);
    }
}
