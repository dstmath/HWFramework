package org.apache.xpath;

import javax.xml.transform.TransformerException;
import org.apache.xalan.templates.ElemTemplateElement;
import org.apache.xalan.templates.ElemVariable;
import org.apache.xalan.templates.Stylesheet;
import org.apache.xml.dtm.DTMFilter;
import org.apache.xml.utils.PrefixResolver;
import org.apache.xml.utils.QName;
import org.apache.xpath.axes.WalkerFactory;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.res.XPATHErrorResources;
import org.apache.xpath.res.XPATHMessages;

public class VariableStack implements Cloneable {
    public static final int CLEARLIMITATION = 1024;
    private static XObject[] m_nulls = new XObject[1024];
    private int _currentFrameBottom;
    int _frameTop;
    int[] _links;
    int _linksTop;
    XObject[] _stackFrames;

    public VariableStack() {
        reset();
    }

    public VariableStack(int initStackSize) {
        reset(initStackSize, initStackSize * 2);
    }

    public synchronized Object clone() throws CloneNotSupportedException {
        VariableStack vs;
        vs = (VariableStack) super.clone();
        vs._stackFrames = (XObject[]) this._stackFrames.clone();
        vs._links = (int[]) this._links.clone();
        return vs;
    }

    public XObject elementAt(int i) {
        return this._stackFrames[i];
    }

    public int size() {
        return this._frameTop;
    }

    public void reset() {
        int linksSize;
        int varArraySize;
        if (this._links == null) {
            linksSize = 4096;
        } else {
            linksSize = this._links.length;
        }
        if (this._stackFrames == null) {
            varArraySize = WalkerFactory.BIT_ANCESTOR;
        } else {
            varArraySize = this._stackFrames.length;
        }
        reset(linksSize, varArraySize);
    }

    protected void reset(int linksSize, int varArraySize) {
        this._frameTop = 0;
        this._linksTop = 0;
        if (this._links == null) {
            this._links = new int[linksSize];
        }
        int[] iArr = this._links;
        int i = this._linksTop;
        this._linksTop = i + 1;
        iArr[i] = 0;
        this._stackFrames = new XObject[varArraySize];
    }

    public void setStackFrame(int sf) {
        this._currentFrameBottom = sf;
    }

    public int getStackFrame() {
        return this._currentFrameBottom;
    }

    public int link(int size) {
        this._currentFrameBottom = this._frameTop;
        this._frameTop += size;
        if (this._frameTop >= this._stackFrames.length) {
            XObject[] newsf = new XObject[((this._stackFrames.length + 4096) + size)];
            System.arraycopy(this._stackFrames, 0, newsf, 0, this._stackFrames.length);
            this._stackFrames = newsf;
        }
        if (this._linksTop + 1 >= this._links.length) {
            int[] newlinks = new int[(this._links.length + DTMFilter.SHOW_NOTATION)];
            System.arraycopy(this._links, 0, newlinks, 0, this._links.length);
            this._links = newlinks;
        }
        int[] iArr = this._links;
        int i = this._linksTop;
        this._linksTop = i + 1;
        iArr[i] = this._currentFrameBottom;
        return this._currentFrameBottom;
    }

    public void unlink() {
        int[] iArr = this._links;
        int i = this._linksTop - 1;
        this._linksTop = i;
        this._frameTop = iArr[i];
        this._currentFrameBottom = this._links[this._linksTop - 1];
    }

    public void unlink(int currentFrame) {
        int[] iArr = this._links;
        int i = this._linksTop - 1;
        this._linksTop = i;
        this._frameTop = iArr[i];
        this._currentFrameBottom = currentFrame;
    }

    public void setLocalVariable(int index, XObject val) {
        this._stackFrames[this._currentFrameBottom + index] = val;
    }

    public void setLocalVariable(int index, XObject val, int stackFrame) {
        this._stackFrames[index + stackFrame] = val;
    }

    public XObject getLocalVariable(XPathContext xctxt, int index) throws TransformerException {
        index += this._currentFrameBottom;
        XObject val = this._stackFrames[index];
        if (val == null) {
            throw new TransformerException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_VARIABLE_ACCESSED_BEFORE_BIND, null), xctxt.getSAXLocator());
        } else if (val.getType() != XObject.CLASS_UNRESOLVEDVARIABLE) {
            return val;
        } else {
            XObject execute = val.execute(xctxt);
            this._stackFrames[index] = execute;
            return execute;
        }
    }

    public XObject getLocalVariable(int index, int frame) throws TransformerException {
        return this._stackFrames[index + frame];
    }

    public XObject getLocalVariable(XPathContext xctxt, int index, boolean destructiveOK) throws TransformerException {
        index += this._currentFrameBottom;
        XObject val = this._stackFrames[index];
        if (val == null) {
            throw new TransformerException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_VARIABLE_ACCESSED_BEFORE_BIND, null), xctxt.getSAXLocator());
        } else if (val.getType() == XObject.CLASS_UNRESOLVEDVARIABLE) {
            XObject execute = val.execute(xctxt);
            this._stackFrames[index] = execute;
            return execute;
        } else {
            if (!destructiveOK) {
                val = val.getFresh();
            }
            return val;
        }
    }

    public boolean isLocalSet(int index) throws TransformerException {
        return this._stackFrames[this._currentFrameBottom + index] != null;
    }

    public void clearLocalSlots(int start, int len) {
        System.arraycopy(m_nulls, 0, this._stackFrames, start + this._currentFrameBottom, len);
    }

    public void setGlobalVariable(int index, XObject val) {
        this._stackFrames[index] = val;
    }

    public XObject getGlobalVariable(XPathContext xctxt, int index) throws TransformerException {
        XObject val = this._stackFrames[index];
        if (val.getType() != XObject.CLASS_UNRESOLVEDVARIABLE) {
            return val;
        }
        XObject execute = val.execute(xctxt);
        this._stackFrames[index] = execute;
        return execute;
    }

    public XObject getGlobalVariable(XPathContext xctxt, int index, boolean destructiveOK) throws TransformerException {
        XObject val = this._stackFrames[index];
        if (val.getType() == XObject.CLASS_UNRESOLVEDVARIABLE) {
            XObject execute = val.execute(xctxt);
            this._stackFrames[index] = execute;
            return execute;
        }
        if (!destructiveOK) {
            val = val.getFresh();
        }
        return val;
    }

    /* JADX WARNING: Missing block: B:15:0x0038, code:
            r1 = r2.getParentElem();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public XObject getVariableOrParam(XPathContext xctxt, QName qname) throws TransformerException {
        PrefixResolver prefixResolver = xctxt.getNamespaceContext();
        if (prefixResolver instanceof ElemTemplateElement) {
            ElemVariable vvar;
            ElemTemplateElement prev = (ElemTemplateElement) prefixResolver;
            if (!(prev instanceof Stylesheet)) {
                while (!(prev.getParentNode() instanceof Stylesheet)) {
                    ElemTemplateElement savedprev = prev;
                    while (true) {
                        prev = prev.getPreviousSiblingElem();
                        if (prev == null) {
                            break;
                        } else if (prev instanceof ElemVariable) {
                            vvar = (ElemVariable) prev;
                            if (vvar.getName().equals(qname)) {
                                return getLocalVariable(xctxt, vvar.getIndex());
                            }
                        }
                    }
                }
            }
            vvar = prev.getStylesheetRoot().getVariableOrParamComposed(qname);
            if (vvar != null) {
                return getGlobalVariable(xctxt, vvar.getIndex());
            }
        }
        throw new TransformerException(XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_VAR_NOT_RESOLVABLE, new Object[]{qname.toString()}));
    }
}
