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
import org.xml.sax.SAXException;

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

    public void execute(TransformerImpl transformer) throws TransformerException {
        boolean pushMode = false;
        transformer.pushCurrentTemplateRuleIsNull(pushMode);
        try {
            QName mode = transformer.getMode();
            if (!this.m_isDefaultTemplate && ((mode == null && this.m_mode != null) || (mode != null && !mode.equals(this.m_mode)))) {
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

    /* JADX WARNING: Removed duplicated region for block: B:140:0x02d7  */
    /* JADX WARNING: Removed duplicated region for block: B:143:0x02df  */
    /* JADX WARNING: Removed duplicated region for block: B:148:0x02f7  */
    /* JADX WARNING: Removed duplicated region for block: B:151:0x02ff  */
    public void transformSelectedNodes(TransformerImpl transformer) throws TransformerException {
        boolean pushContextNodeListFlag;
        int thisframe;
        int argsFrame;
        SerializationHandler rth;
        int exNodeType;
        int nodeType;
        DTMIterator sourceNodes;
        QName mode;
        DTM dtm;
        Vector keys;
        IntStack currentExpressionNodes;
        int thisframe2;
        IntStack currentNodes;
        int argsFrame2;
        StylesheetRoot sroot;
        DTM dtm2;
        TemplateList tl;
        SerializationHandler rth2;
        StylesheetRoot sroot2;
        DTM dtm3;
        int currentFrameBottom;
        DTM dtm4;
        StylesheetRoot sroot3;
        QName mode2;
        TemplateList tl2;
        SerializationHandler rth3;
        ElemApplyTemplates elemApplyTemplates = this;
        TransformerImpl transformerImpl = transformer;
        XPathContext xctxt = transformer.getXPathContext();
        int sourceNode = xctxt.getCurrentNode();
        DTMIterator sourceNodes2 = elemApplyTemplates.m_selectExpression.asIterator(xctxt, sourceNode);
        VariableStack vars = xctxt.getVarStack();
        int nParams = getParamElemCount();
        int argsFrame3 = vars.getStackFrame();
        boolean pushContextNodeListFlag2 = false;
        try {
            xctxt.pushCurrentNode(-1);
            xctxt.pushCurrentExpressionNode(-1);
            xctxt.pushSAXLocatorNull();
            transformerImpl.pushElemTemplateElement(null);
            Vector keys2 = elemApplyTemplates.m_sortElems == null ? null : transformerImpl.processSortKeys(elemApplyTemplates, sourceNode);
            if (keys2 != null) {
                try {
                    sourceNodes2 = elemApplyTemplates.sortNodes(xctxt, keys2, sourceNodes2);
                } catch (SAXException e) {
                    se = e;
                    thisframe = argsFrame3;
                    try {
                        transformer.getErrorListener().fatalError(new TransformerException(se));
                        if (nParams > 0) {
                            vars.unlink(thisframe);
                        }
                        xctxt.popSAXLocator();
                        if (pushContextNodeListFlag2) {
                            xctxt.popContextNodeList();
                        }
                        transformer.popElemTemplateElement();
                        xctxt.popCurrentExpressionNode();
                        xctxt.popCurrentNode();
                        sourceNodes2.detach();
                        boolean z = pushContextNodeListFlag2;
                    } catch (Throwable th) {
                        th = th;
                        pushContextNodeListFlag = pushContextNodeListFlag2;
                        if (nParams > 0) {
                        }
                        xctxt.popSAXLocator();
                        if (pushContextNodeListFlag) {
                        }
                        transformer.popElemTemplateElement();
                        xctxt.popCurrentExpressionNode();
                        xctxt.popCurrentNode();
                        sourceNodes2.detach();
                        throw th;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    pushContextNodeListFlag = false;
                    thisframe = argsFrame3;
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
                    sourceNodes2.detach();
                    throw th;
                }
            }
            DTMIterator sourceNodes3 = sourceNodes2;
            try {
                SerializationHandler rth4 = transformer.getSerializationHandler();
                StylesheetRoot sroot4 = transformer.getStylesheet();
                TemplateList tl3 = sroot4.getTemplateListComposed();
                StylesheetRoot sroot5 = sroot4;
                boolean quiet = transformer.getQuietConflictWarnings();
                DTM dtm5 = xctxt.getDTM(sourceNode);
                if (nParams > 0) {
                    try {
                        int argsFrame4 = vars.link(nParams);
                        vars.setStackFrame(argsFrame3);
                        int i = 0;
                        while (true) {
                            int i2 = i;
                            if (i2 >= nParams) {
                                break;
                            }
                            ElemWithParam ewp = elemApplyTemplates.m_paramElems[i2];
                            ElemWithParam elemWithParam = ewp;
                            int argsFrame5 = argsFrame4;
                            vars.setLocalVariable(i2, ewp.getValue(transformerImpl, sourceNode), argsFrame5);
                            i = i2 + 1;
                            argsFrame4 = argsFrame5;
                        }
                        argsFrame = argsFrame4;
                        vars.setStackFrame(argsFrame);
                    } catch (SAXException e2) {
                        se = e2;
                        sourceNodes2 = sourceNodes3;
                        thisframe = argsFrame3;
                        transformer.getErrorListener().fatalError(new TransformerException(se));
                        if (nParams > 0) {
                        }
                        xctxt.popSAXLocator();
                        if (pushContextNodeListFlag2) {
                        }
                        transformer.popElemTemplateElement();
                        xctxt.popCurrentExpressionNode();
                        xctxt.popCurrentNode();
                        sourceNodes2.detach();
                        boolean z2 = pushContextNodeListFlag2;
                    } catch (Throwable th3) {
                        th = th3;
                        pushContextNodeListFlag = false;
                        sourceNodes2 = sourceNodes3;
                        thisframe = argsFrame3;
                        if (nParams > 0) {
                        }
                        xctxt.popSAXLocator();
                        if (pushContextNodeListFlag) {
                        }
                        transformer.popElemTemplateElement();
                        xctxt.popCurrentExpressionNode();
                        xctxt.popCurrentNode();
                        sourceNodes2.detach();
                        throw th;
                    }
                } else {
                    argsFrame = -1;
                }
                xctxt.pushContextNodeList(sourceNodes3);
                pushContextNodeListFlag = true;
                try {
                    IntStack currentNodes2 = xctxt.getCurrentNodeStack();
                    IntStack currentExpressionNodes2 = xctxt.getCurrentExpressionNodeStack();
                    DTM dtm6 = dtm5;
                    while (true) {
                        IntStack currentExpressionNodes3 = currentExpressionNodes2;
                        int nextNode = sourceNodes3.nextNode();
                        int child = nextNode;
                        int argsFrame6 = argsFrame;
                        if (-1 != nextNode) {
                            int sourceNode2 = sourceNode;
                            int child2 = child;
                            try {
                                currentNodes2.setTop(child2);
                                IntStack currentNodes3 = currentNodes2;
                                IntStack currentNodes4 = currentExpressionNodes3;
                                currentNodes4.setTop(child2);
                                if (xctxt.getDTM(child2) != dtm6) {
                                    try {
                                        dtm6 = xctxt.getDTM(child2);
                                    } catch (SAXException e3) {
                                        se = e3;
                                        sourceNodes2 = sourceNodes3;
                                        thisframe = argsFrame3;
                                        pushContextNodeListFlag2 = true;
                                        transformer.getErrorListener().fatalError(new TransformerException(se));
                                        if (nParams > 0) {
                                        }
                                        xctxt.popSAXLocator();
                                        if (pushContextNodeListFlag2) {
                                        }
                                        transformer.popElemTemplateElement();
                                        xctxt.popCurrentExpressionNode();
                                        xctxt.popCurrentNode();
                                        sourceNodes2.detach();
                                        boolean z22 = pushContextNodeListFlag2;
                                    } catch (Throwable th4) {
                                        th = th4;
                                        sourceNodes2 = sourceNodes3;
                                        thisframe = argsFrame3;
                                        if (nParams > 0) {
                                        }
                                        xctxt.popSAXLocator();
                                        if (pushContextNodeListFlag) {
                                        }
                                        transformer.popElemTemplateElement();
                                        xctxt.popCurrentExpressionNode();
                                        xctxt.popCurrentNode();
                                        sourceNodes2.detach();
                                        throw th;
                                    }
                                }
                                rth = rth4;
                                exNodeType = dtm6.getExpandedTypeID(child2);
                                nodeType = dtm6.getNodeType(child2);
                                sourceNodes = sourceNodes3;
                                mode = transformer.getMode();
                                dtm = dtm6;
                                keys = keys2;
                                currentExpressionNodes = currentNodes4;
                                thisframe2 = argsFrame3;
                                currentNodes = currentNodes3;
                                argsFrame2 = argsFrame6;
                            } catch (SAXException e4) {
                                se = e4;
                                thisframe = argsFrame3;
                                pushContextNodeListFlag2 = true;
                                sourceNodes2 = sourceNodes3;
                                transformer.getErrorListener().fatalError(new TransformerException(se));
                                if (nParams > 0) {
                                }
                                xctxt.popSAXLocator();
                                if (pushContextNodeListFlag2) {
                                }
                                transformer.popElemTemplateElement();
                                xctxt.popCurrentExpressionNode();
                                xctxt.popCurrentNode();
                                sourceNodes2.detach();
                                boolean z222 = pushContextNodeListFlag2;
                            } catch (Throwable th5) {
                                th = th5;
                                thisframe = argsFrame3;
                                sourceNodes2 = sourceNodes3;
                                if (nParams > 0) {
                                }
                                xctxt.popSAXLocator();
                                if (pushContextNodeListFlag) {
                                }
                                transformer.popElemTemplateElement();
                                xctxt.popCurrentExpressionNode();
                                xctxt.popCurrentNode();
                                sourceNodes2.detach();
                                throw th;
                            }
                            try {
                                ElemTemplate template = tl3.getTemplateFast(xctxt, child2, exNodeType, mode, -1, quiet, dtm);
                                if (template == null) {
                                    int nodeType2 = nodeType;
                                    if (nodeType2 != 9) {
                                        if (nodeType2 != 11) {
                                            switch (nodeType2) {
                                                case 1:
                                                    break;
                                                case 2:
                                                case 3:
                                                case 4:
                                                    StylesheetRoot sroot6 = sroot5;
                                                    transformerImpl.pushPairCurrentMatched(sroot6.getDefaultTextRule(), child2);
                                                    transformerImpl.setCurrentElement(sroot6.getDefaultTextRule());
                                                    tl = tl3;
                                                    rth2 = rth;
                                                    DTM dtm7 = dtm;
                                                    dtm7.dispatchCharactersEvents(child2, rth2, false);
                                                    transformer.popCurrentMatched();
                                                    dtm2 = dtm7;
                                                    sroot = sroot6;
                                                    break;
                                                default:
                                                    tl = tl3;
                                                    sroot = sroot5;
                                                    rth2 = rth;
                                                    dtm2 = dtm;
                                                    break;
                                            }
                                        }
                                        tl2 = tl3;
                                        sroot2 = sroot5;
                                        rth3 = rth;
                                        dtm3 = dtm;
                                        template = sroot2.getDefaultRule();
                                    } else {
                                        tl2 = tl3;
                                        sroot2 = sroot5;
                                        rth3 = rth;
                                        dtm3 = dtm;
                                        template = sroot2.getDefaultRootRule();
                                    }
                                } else {
                                    tl2 = tl3;
                                    sroot2 = sroot5;
                                    rth3 = rth;
                                    int i3 = nodeType;
                                    dtm3 = dtm;
                                    transformerImpl.setCurrentElement(template);
                                }
                                transformerImpl.pushPairCurrentMatched(template, child2);
                                if (template.m_frameSize > 0) {
                                    xctxt.pushRTFContext();
                                    currentFrameBottom = vars.getStackFrame();
                                    vars.link(template.m_frameSize);
                                    if (template.m_inArgsSize > 0) {
                                        int paramIndex = 0;
                                        ElemTemplateElement elem = template.getFirstChildElem();
                                        while (true) {
                                            dtm4 = dtm3;
                                            ElemTemplateElement elem2 = elem;
                                            if (elem2 != null) {
                                                sroot3 = sroot2;
                                                int exNodeType2 = exNodeType;
                                                if (41 == elem2.getXSLToken()) {
                                                    ElemParam ep = (ElemParam) elem2;
                                                    int i4 = 0;
                                                    while (true) {
                                                        if (i4 < nParams) {
                                                            mode2 = mode;
                                                            ElemWithParam ewp2 = elemApplyTemplates.m_paramElems[i4];
                                                            ElemWithParam elemWithParam2 = ewp2;
                                                            if (ewp2.m_qnameID == ep.m_qnameID) {
                                                                vars.setLocalVariable(paramIndex, vars.getLocalVariable(i4, argsFrame2));
                                                            } else {
                                                                i4++;
                                                                mode = mode2;
                                                                elemApplyTemplates = this;
                                                            }
                                                        } else {
                                                            mode2 = mode;
                                                        }
                                                    }
                                                    if (i4 == nParams) {
                                                        vars.setLocalVariable(paramIndex, null);
                                                    }
                                                    paramIndex++;
                                                    elem = elem2.getNextSiblingElem();
                                                    dtm3 = dtm4;
                                                    sroot2 = sroot3;
                                                    exNodeType = exNodeType2;
                                                    mode = mode2;
                                                    elemApplyTemplates = this;
                                                }
                                            } else {
                                                sroot3 = sroot2;
                                                int i5 = exNodeType;
                                                QName qName = mode;
                                            }
                                        }
                                    } else {
                                        dtm4 = dtm3;
                                        sroot3 = sroot2;
                                        int i6 = exNodeType;
                                        QName qName2 = mode;
                                    }
                                } else {
                                    dtm4 = dtm3;
                                    sroot3 = sroot2;
                                    int i7 = exNodeType;
                                    QName qName3 = mode;
                                    currentFrameBottom = 0;
                                }
                                int currentFrameBottom2 = currentFrameBottom;
                                ElemTemplateElement t = template.m_firstChild;
                                while (true) {
                                    ElemTemplateElement t2 = t;
                                    if (t2 != null) {
                                        xctxt.setSAXLocator(t2);
                                        transformerImpl.pushElemTemplateElement(t2);
                                        t2.execute(transformerImpl);
                                        transformer.popElemTemplateElement();
                                        t = t2.m_nextSibling;
                                    } else {
                                        if (template.m_frameSize > 0) {
                                            vars.unlink(currentFrameBottom2);
                                            xctxt.popRTFContext();
                                        }
                                        transformer.popCurrentMatched();
                                        rth4 = rth2;
                                        argsFrame = argsFrame2;
                                        keys2 = keys;
                                        currentNodes2 = currentNodes;
                                        currentExpressionNodes2 = currentExpressionNodes;
                                        sourceNode = sourceNode2;
                                        sourceNodes3 = sourceNodes;
                                        argsFrame3 = thisframe2;
                                        tl3 = tl;
                                        dtm6 = dtm2;
                                        sroot5 = sroot;
                                        elemApplyTemplates = this;
                                    }
                                }
                            } catch (SAXException e5) {
                                se = e5;
                                pushContextNodeListFlag2 = true;
                                sourceNodes2 = sourceNodes;
                                thisframe = thisframe2;
                                transformer.getErrorListener().fatalError(new TransformerException(se));
                                if (nParams > 0) {
                                }
                                xctxt.popSAXLocator();
                                if (pushContextNodeListFlag2) {
                                }
                                transformer.popElemTemplateElement();
                                xctxt.popCurrentExpressionNode();
                                xctxt.popCurrentNode();
                                sourceNodes2.detach();
                                boolean z2222 = pushContextNodeListFlag2;
                            } catch (Throwable th6) {
                                th = th6;
                                sourceNodes2 = sourceNodes;
                                thisframe = thisframe2;
                                if (nParams > 0) {
                                }
                                xctxt.popSAXLocator();
                                if (pushContextNodeListFlag) {
                                }
                                transformer.popElemTemplateElement();
                                xctxt.popCurrentExpressionNode();
                                xctxt.popCurrentNode();
                                sourceNodes2.detach();
                                throw th;
                            }
                        } else {
                            DTMIterator sourceNodes4 = sourceNodes3;
                            int i8 = sourceNode;
                            int thisframe3 = argsFrame3;
                            if (nParams > 0) {
                                vars.unlink(thisframe3);
                            } else {
                                int i9 = thisframe3;
                            }
                            xctxt.popSAXLocator();
                            if (1 != 0) {
                                xctxt.popContextNodeList();
                            }
                            transformer.popElemTemplateElement();
                            xctxt.popCurrentExpressionNode();
                            xctxt.popCurrentNode();
                            sourceNodes4.detach();
                            return;
                        }
                    }
                } catch (SAXException e6) {
                    se = e6;
                    sourceNodes2 = sourceNodes3;
                    int i10 = sourceNode;
                    thisframe = argsFrame3;
                    pushContextNodeListFlag2 = true;
                    transformer.getErrorListener().fatalError(new TransformerException(se));
                    if (nParams > 0) {
                    }
                    xctxt.popSAXLocator();
                    if (pushContextNodeListFlag2) {
                    }
                    transformer.popElemTemplateElement();
                    xctxt.popCurrentExpressionNode();
                    xctxt.popCurrentNode();
                    sourceNodes2.detach();
                    boolean z22222 = pushContextNodeListFlag2;
                } catch (Throwable th7) {
                    th = th7;
                    sourceNodes2 = sourceNodes3;
                    int i11 = sourceNode;
                    thisframe = argsFrame3;
                    if (nParams > 0) {
                    }
                    xctxt.popSAXLocator();
                    if (pushContextNodeListFlag) {
                    }
                    transformer.popElemTemplateElement();
                    xctxt.popCurrentExpressionNode();
                    xctxt.popCurrentNode();
                    sourceNodes2.detach();
                    throw th;
                }
            } catch (SAXException e7) {
                se = e7;
                sourceNodes2 = sourceNodes3;
                int i12 = sourceNode;
                thisframe = argsFrame3;
                transformer.getErrorListener().fatalError(new TransformerException(se));
                if (nParams > 0) {
                }
                xctxt.popSAXLocator();
                if (pushContextNodeListFlag2) {
                }
                transformer.popElemTemplateElement();
                xctxt.popCurrentExpressionNode();
                xctxt.popCurrentNode();
                sourceNodes2.detach();
                boolean z222222 = pushContextNodeListFlag2;
            } catch (Throwable th8) {
                th = th8;
                sourceNodes2 = sourceNodes3;
                int i13 = sourceNode;
                thisframe = argsFrame3;
                pushContextNodeListFlag = false;
                if (nParams > 0) {
                }
                xctxt.popSAXLocator();
                if (pushContextNodeListFlag) {
                }
                transformer.popElemTemplateElement();
                xctxt.popCurrentExpressionNode();
                xctxt.popCurrentNode();
                sourceNodes2.detach();
                throw th;
            }
        } catch (SAXException e8) {
            se = e8;
            int i14 = sourceNode;
            thisframe = argsFrame3;
            transformer.getErrorListener().fatalError(new TransformerException(se));
            if (nParams > 0) {
            }
            xctxt.popSAXLocator();
            if (pushContextNodeListFlag2) {
            }
            transformer.popElemTemplateElement();
            xctxt.popCurrentExpressionNode();
            xctxt.popCurrentNode();
            sourceNodes2.detach();
            boolean z2222222 = pushContextNodeListFlag2;
        } catch (Throwable th9) {
            th = th9;
            int i15 = sourceNode;
            thisframe = argsFrame3;
            pushContextNodeListFlag = false;
            if (nParams > 0) {
            }
            xctxt.popSAXLocator();
            if (pushContextNodeListFlag) {
            }
            transformer.popElemTemplateElement();
            xctxt.popCurrentExpressionNode();
            xctxt.popCurrentNode();
            sourceNodes2.detach();
            throw th;
        }
    }
}
