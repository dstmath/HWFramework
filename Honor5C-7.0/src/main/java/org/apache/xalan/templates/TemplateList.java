package org.apache.xalan.templates;

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
import org.apache.xpath.compiler.OpCodes;
import org.apache.xpath.compiler.PsuedoNames;
import org.apache.xpath.patterns.NodeTest;
import org.apache.xpath.patterns.StepPattern;
import org.apache.xpath.patterns.UnionPattern;

public class TemplateList implements Serializable {
    static final boolean DEBUG = false;
    static final long serialVersionUID = 5803675288911728791L;
    private TemplateSubPatternAssociation m_commentPatterns;
    private TemplateSubPatternAssociation m_docPatterns;
    private Hashtable m_namedTemplates;
    private Hashtable m_patternTable;
    private TemplateSubPatternAssociation m_textPatterns;
    private TemplateSubPatternAssociation m_wildCardPatterns;

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

        public ElemTemplate next() {
            ElemTemplate elemTemplate = null;
            do {
                if (this.inPatterns) {
                    if (this.curPattern != null) {
                        this.curPattern = this.curPattern.getNext();
                    }
                    if (this.curPattern != null) {
                        elemTemplate = this.curPattern.getTemplate();
                    } else if (this.hashIterator.hasMoreElements()) {
                        this.curPattern = (TemplateSubPatternAssociation) this.hashIterator.nextElement();
                        elemTemplate = this.curPattern.getTemplate();
                    } else {
                        this.inPatterns = TemplateList.DEBUG;
                        this.hashIterator = TemplateList.this.m_namedTemplates.elements();
                    }
                }
                if (!this.inPatterns) {
                    if (!this.hashIterator.hasMoreElements()) {
                        return null;
                    }
                    elemTemplate = (ElemTemplate) this.hashIterator.nextElement();
                }
            } while (((ElemTemplate) this.m_compilerCache.get(new Integer(elemTemplate.getUid()))) != null);
            this.m_compilerCache.put(new Integer(elemTemplate.getUid()), elemTemplate);
            return elemTemplate;
        }
    }

    public TemplateList() {
        this.m_namedTemplates = new Hashtable(89);
        this.m_patternTable = new Hashtable(89);
        this.m_wildCardPatterns = null;
        this.m_textPatterns = null;
        this.m_docPatterns = null;
        this.m_commentPatterns = null;
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

    void dumpAssociationTables() {
        TemplateSubPatternAssociation head;
        Enumeration associations = this.m_patternTable.elements();
        while (associations.hasMoreElements()) {
            for (head = (TemplateSubPatternAssociation) associations.nextElement(); head != null; head = head.getNext()) {
                System.out.print("(" + head.getTargetString() + ", " + head.getPattern() + ")");
            }
            System.out.println("\n.....");
        }
        System.out.print("wild card list: ");
        for (head = this.m_wildCardPatterns; head != null; head = head.getNext()) {
            System.out.print("(" + head.getTargetString() + ", " + head.getPattern() + ")");
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
        double workPriority;
        boolean insertBefore;
        double priority = getPriorityOrScore(item);
        int importLevel = item.getImportLevel();
        int docOrder = item.getDocOrderPos();
        TemplateSubPatternAssociation insertPoint = head;
        while (true) {
            TemplateSubPatternAssociation next = insertPoint.getNext();
            if (next != null) {
                workPriority = getPriorityOrScore(next);
                if (importLevel <= next.getImportLevel()) {
                    if (importLevel >= next.getImportLevel()) {
                        if (priority <= workPriority) {
                            if (priority >= workPriority) {
                                if (docOrder >= next.getDocOrderPos()) {
                                    break;
                                }
                                insertPoint = next;
                            } else {
                                insertPoint = next;
                            }
                        } else {
                            break;
                        }
                    }
                    insertPoint = next;
                } else {
                    break;
                }
            }
            break;
        }
        if (next == null || insertPoint == head) {
            workPriority = getPriorityOrScore(insertPoint);
            if (importLevel > insertPoint.getImportLevel()) {
                insertBefore = true;
            } else if (importLevel < insertPoint.getImportLevel()) {
                insertBefore = DEBUG;
            } else if (priority > workPriority) {
                insertBefore = true;
            } else if (priority < workPriority) {
                insertBefore = DEBUG;
            } else if (docOrder >= insertPoint.getDocOrderPos()) {
                insertBefore = true;
            } else {
                insertBefore = DEBUG;
            }
        } else {
            insertBefore = DEBUG;
        }
        if (isWildCardInsert) {
            if (insertBefore) {
                item.setNext(insertPoint);
                String key = insertPoint.getTargetString();
                item.setTargetString(key);
                putHead(key, item);
                return item;
            }
            item.setNext(next);
            insertPoint.setNext(item);
            return head;
        } else if (insertBefore) {
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
        String target = pattern.getTargetString();
        if (target != null) {
            TemplateSubPatternAssociation head;
            TemplateSubPatternAssociation association = new TemplateSubPatternAssociation(template, pattern, template.getMatch().getPatternString());
            boolean isWildCard = association.isWild();
            if (isWildCard) {
                head = this.m_wildCardPatterns;
            } else {
                head = getHead(target);
            }
            if (head != null) {
                insertAssociationIntoList(head, association, DEBUG);
            } else if (isWildCard) {
                this.m_wildCardPatterns = association;
            } else {
                putHead(target, association);
            }
        }
    }

    private double getPriorityOrScore(TemplateSubPatternAssociation matchPat) {
        double priority = matchPat.getTemplate().getPriority();
        if (priority == XPath.MATCH_SCORE_NONE) {
            Expression ex = matchPat.getStepPattern();
            if (ex instanceof NodeTest) {
                return ((NodeTest) ex).getDefaultScore();
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
            case OpCodes.OP_XPATH /*1*/:
            case OpCodes.OP_OR /*2*/:
                head = (TemplateSubPatternAssociation) this.m_patternTable.get(dtm.getLocalName(targetNode));
                break;
            case OpCodes.OP_AND /*3*/:
            case OpCodes.OP_NOTEQUALS /*4*/:
                head = this.m_textPatterns;
                break;
            case OpCodes.OP_EQUALS /*5*/:
            case OpCodes.OP_LTE /*6*/:
                head = (TemplateSubPatternAssociation) this.m_patternTable.get(dtm.getNodeName(targetNode));
                break;
            case OpCodes.OP_LT /*7*/:
                head = (TemplateSubPatternAssociation) this.m_patternTable.get(dtm.getLocalName(targetNode));
                break;
            case OpCodes.OP_GTE /*8*/:
                head = this.m_commentPatterns;
                break;
            case OpCodes.OP_GT /*9*/:
            case OpCodes.OP_MINUS /*11*/:
                head = this.m_docPatterns;
                break;
            default:
                head = (TemplateSubPatternAssociation) this.m_patternTable.get(dtm.getNodeName(targetNode));
                break;
        }
        return head == null ? this.m_wildCardPatterns : head;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public ElemTemplate getTemplateFast(XPathContext xctxt, int targetNode, int expTypeID, QName mode, int maxImportLevel, boolean quietConflictWarnings, DTM dtm) throws TransformerException {
        TemplateSubPatternAssociation templateSubPatternAssociation;
        switch (dtm.getNodeType(targetNode)) {
            case OpCodes.OP_XPATH /*1*/:
            case OpCodes.OP_OR /*2*/:
                templateSubPatternAssociation = (TemplateSubPatternAssociation) this.m_patternTable.get(dtm.getLocalNameFromExpandedNameID(expTypeID));
                break;
            case OpCodes.OP_AND /*3*/:
            case OpCodes.OP_NOTEQUALS /*4*/:
                templateSubPatternAssociation = this.m_textPatterns;
                break;
            case OpCodes.OP_EQUALS /*5*/:
            case OpCodes.OP_LTE /*6*/:
                templateSubPatternAssociation = (TemplateSubPatternAssociation) this.m_patternTable.get(dtm.getNodeName(targetNode));
                break;
            case OpCodes.OP_LT /*7*/:
                templateSubPatternAssociation = (TemplateSubPatternAssociation) this.m_patternTable.get(dtm.getLocalName(targetNode));
                break;
            case OpCodes.OP_GTE /*8*/:
                templateSubPatternAssociation = this.m_commentPatterns;
                break;
            case OpCodes.OP_GT /*9*/:
            case OpCodes.OP_MINUS /*11*/:
                templateSubPatternAssociation = this.m_docPatterns;
                break;
            default:
                templateSubPatternAssociation = (TemplateSubPatternAssociation) this.m_patternTable.get(dtm.getNodeName(targetNode));
                break;
        }
        if (templateSubPatternAssociation == null) {
            templateSubPatternAssociation = this.m_wildCardPatterns;
            if (templateSubPatternAssociation == null) {
                return null;
            }
        }
        xctxt.pushNamespaceContextNull();
        do {
            if (maxImportLevel > -1) {
            }
            try {
                ElemTemplate template = templateSubPatternAssociation.getTemplate();
                xctxt.setNamespaceContext(template);
                if (templateSubPatternAssociation.m_stepPattern.execute(xctxt, targetNode, dtm, expTypeID) != NodeTest.SCORE_NONE && templateSubPatternAssociation.matchMode(mode)) {
                    if (quietConflictWarnings) {
                        checkConflicts(templateSubPatternAssociation, xctxt, targetNode, mode);
                    }
                    xctxt.popNamespaceContext();
                    return template;
                }
                templateSubPatternAssociation = templateSubPatternAssociation.getNext();
            } catch (Throwable th) {
                xctxt.popNamespaceContext();
            }
        } while (templateSubPatternAssociation != null);
        xctxt.popNamespaceContext();
        return null;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public ElemTemplate getTemplate(XPathContext xctxt, int targetNode, QName mode, boolean quietConflictWarnings, DTM dtm) throws TransformerException {
        TemplateSubPatternAssociation head = getHead(xctxt, targetNode, dtm);
        if (head != null) {
            xctxt.pushNamespaceContextNull();
            xctxt.pushCurrentNodeAndExpression(targetNode, targetNode);
            while (true) {
                ElemTemplate template = head.getTemplate();
                xctxt.setNamespaceContext(template);
                if (head.m_stepPattern.execute(xctxt, targetNode) != NodeTest.SCORE_NONE && head.matchMode(mode)) {
                    break;
                }
                try {
                    head = head.getNext();
                    if (head == null) {
                        break;
                    }
                } finally {
                    xctxt.popCurrentNodeAndExpression();
                    xctxt.popNamespaceContext();
                }
            }
        }
        return null;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public ElemTemplate getTemplate(XPathContext xctxt, int targetNode, QName mode, int maxImportLevel, int endImportLevel, boolean quietConflictWarnings, DTM dtm) throws TransformerException {
        TemplateSubPatternAssociation head = getHead(xctxt, targetNode, dtm);
        if (head != null) {
            xctxt.pushNamespaceContextNull();
            xctxt.pushCurrentNodeAndExpression(targetNode, targetNode);
            do {
                if (maxImportLevel > -1) {
                    try {
                    } catch (Throwable th) {
                        xctxt.popCurrentNodeAndExpression();
                        xctxt.popNamespaceContext();
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
                head = head.getNext();
            } while (head != null);
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
        for (int i = 0; i < n; i++) {
            if (v.elementAt(i) == obj) {
                addIt = DEBUG;
                break;
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
