package org.apache.xalan.templates;

import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.DTMIterator;
import org.apache.xml.serializer.SerializationHandler;
import org.apache.xml.utils.IntStack;
import org.apache.xml.utils.QName;
import org.apache.xpath.VariableStack;
import org.apache.xpath.XPathContext;

public class ElemApplyTemplates extends ElemCallTemplate {
    static final long serialVersionUID = 2903125371542621004L;
    private boolean m_isDefaultTemplate = false;
    private QName m_mode = null;

    public void setMode(QName mode) {
        this.m_mode = mode;
    }

    public QName getMode() {
        return this.m_mode;
    }

    public void setIsDefaultTemplate(boolean b) {
        this.m_isDefaultTemplate = b;
    }

    public int getXSLToken() {
        return 50;
    }

    public void compose(StylesheetRoot sroot) throws TransformerException {
        super.compose(sroot);
    }

    public String getNodeName() {
        return Constants.ELEMNAME_APPLY_TEMPLATES_STRING;
    }

    /* JADX WARNING: Missing block: B:14:0x002f, code:
            if ((r0.equals(r3.m_mode) ^ 1) != 0) goto L_0x0013;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void execute(TransformerImpl transformer) throws TransformerException {
        transformer.pushCurrentTemplateRuleIsNull(false);
        boolean pushMode = false;
        try {
            QName mode = transformer.getMode();
            if (!this.m_isDefaultTemplate) {
                if (mode != null || this.m_mode == null) {
                    if (mode != null) {
                    }
                }
                pushMode = true;
                transformer.pushMode(this.m_mode);
            }
            transformSelectedNodes(transformer);
        } finally {
            if (pushMode) {
                transformer.popMode();
            }
            transformer.popCurrentTemplateRuleIsNull();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:98:0x0157 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x014f A:{Catch:{ all -> 0x0204, SAXException -> 0x017b, all -> 0x01bb }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void transformSelectedNodes(TransformerImpl transformer) throws TransformerException {
        XPathContext xctxt = transformer.getXPathContext();
        int sourceNode = xctxt.getCurrentNode();
        DTMIterator sourceNodes = this.m_selectExpression.asIterator(xctxt, sourceNode);
        VariableStack vars = xctxt.getVarStack();
        int nParams = getParamElemCount();
        int thisframe = vars.getStackFrame();
        boolean pushContextNodeListFlag = false;
        try {
            Vector keys;
            int i;
            xctxt.pushCurrentNode(-1);
            xctxt.pushCurrentExpressionNode(-1);
            xctxt.pushSAXLocatorNull();
            transformer.pushElemTemplateElement(null);
            if (this.m_sortElems == null) {
                keys = null;
            } else {
                keys = transformer.processSortKeys(this, sourceNode);
            }
            if (keys != null) {
                sourceNodes = sortNodes(xctxt, keys, sourceNodes);
            }
            SerializationHandler rth = transformer.getSerializationHandler();
            StylesheetRoot sroot = transformer.getStylesheet();
            TemplateList tl = sroot.getTemplateListComposed();
            boolean quiet = transformer.getQuietConflictWarnings();
            DTM dtm = xctxt.getDTM(sourceNode);
            int argsFrame = -1;
            if (nParams > 0) {
                argsFrame = vars.link(nParams);
                vars.setStackFrame(thisframe);
                for (i = 0; i < nParams; i++) {
                    vars.setLocalVariable(i, this.m_paramElems[i].getValue(transformer, sourceNode), argsFrame);
                }
                vars.setStackFrame(argsFrame);
            }
            xctxt.pushContextNodeList(sourceNodes);
            pushContextNodeListFlag = true;
            IntStack currentNodes = xctxt.getCurrentNodeStack();
            IntStack currentExpressionNodes = xctxt.getCurrentExpressionNodeStack();
            while (true) {
                int child = sourceNodes.nextNode();
                if (-1 != child) {
                    int currentFrameBottom;
                    currentNodes.setTop(child);
                    currentExpressionNodes.setTop(child);
                    if (xctxt.getDTM(child) != dtm) {
                        dtm = xctxt.getDTM(child);
                    }
                    int exNodeType = dtm.getExpandedTypeID(child);
                    int nodeType = dtm.getNodeType(child);
                    ElemTemplateElement template = tl.getTemplateFast(xctxt, child, exNodeType, transformer.getMode(), -1, quiet, dtm);
                    if (template == null) {
                        switch (nodeType) {
                            case 1:
                            case 11:
                                template = sroot.getDefaultRule();
                            case 2:
                            case 3:
                            case 4:
                                transformer.pushPairCurrentMatched(sroot.getDefaultTextRule(), child);
                                transformer.setCurrentElement(sroot.getDefaultTextRule());
                                dtm.dispatchCharactersEvents(child, rth, false);
                                transformer.popCurrentMatched();
                                continue;
                            case 9:
                                template = sroot.getDefaultRootRule();
                            default:
                                continue;
                        }
                    } else {
                        transformer.setCurrentElement(template);
                    }
                    transformer.pushPairCurrentMatched(template, child);
                    if (template.m_frameSize > 0) {
                        xctxt.pushRTFContext();
                        currentFrameBottom = vars.getStackFrame();
                        vars.link(template.m_frameSize);
                        if (template.m_inArgsSize > 0) {
                            int paramIndex = 0;
                            ElemTemplateElement elem = template.getFirstChildElem();
                            while (elem != null && 41 == elem.getXSLToken()) {
                                ElemParam ep = (ElemParam) elem;
                                i = 0;
                                while (i < nParams) {
                                    if (this.m_paramElems[i].m_qnameID == ep.m_qnameID) {
                                        vars.setLocalVariable(paramIndex, vars.getLocalVariable(i, argsFrame));
                                        if (i != nParams) {
                                            vars.setLocalVariable(paramIndex, null);
                                        }
                                        paramIndex++;
                                        elem = elem.getNextSiblingElem();
                                    } else {
                                        i++;
                                    }
                                }
                                if (i != nParams) {
                                }
                                paramIndex++;
                                elem = elem.getNextSiblingElem();
                            }
                        }
                    } else {
                        currentFrameBottom = 0;
                    }
                    for (ElemTemplateElement t = template.m_firstChild; t != null; t = t.m_nextSibling) {
                        xctxt.setSAXLocator(t);
                        transformer.pushElemTemplateElement(t);
                        t.execute(transformer);
                        transformer.popElemTemplateElement();
                    }
                    if (template.m_frameSize > 0) {
                        vars.unlink(currentFrameBottom);
                        xctxt.popRTFContext();
                    }
                    transformer.popCurrentMatched();
                } else {
                    if (nParams > 0) {
                        vars.unlink(thisframe);
                    }
                    xctxt.popSAXLocator();
                    if (1 != null) {
                        xctxt.popContextNodeList();
                    }
                    transformer.popElemTemplateElement();
                    xctxt.popCurrentExpressionNode();
                    xctxt.popCurrentNode();
                    sourceNodes.detach();
                    return;
                }
            }
        } catch (Throwable se) {
            transformer.getErrorListener().fatalError(new TransformerException(se));
            if (nParams > 0) {
                vars.unlink(thisframe);
            }
            xctxt.popSAXLocator();
            if (pushContextNodeListFlag) {
                xctxt.popContextNodeList();
            }
            transformer.popElemTemplateElement();
            xctxt.popCurrentExpressionNode();
            xctxt.popCurrentNode();
            sourceNodes.detach();
        } catch (Throwable th) {
            if (nParams > 0) {
                vars.unlink(thisframe);
            }
            xctxt.popSAXLocator();
            if (pushContextNodeListFlag) {
                xctxt.popContextNodeList();
            }
            transformer.popElemTemplateElement();
            xctxt.popCurrentExpressionNode();
            xctxt.popCurrentNode();
            sourceNodes.detach();
            throw th;
        }
    }
}
