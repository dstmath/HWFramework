package org.apache.xalan.templates;

import java.io.Serializable;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xalan.processor.StylesheetHandler;
import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xml.utils.FastStringBuffer;
import org.apache.xml.utils.PrefixResolver;
import org.apache.xpath.XPathContext;
import org.xml.sax.SAXException;

public class AVT implements Serializable, XSLTVisitable {
    private static final int INIT_BUFFER_CHUNK_BITS = 8;
    private static final boolean USE_OBJECT_POOL = false;
    static final long serialVersionUID = 5167607155517042691L;
    private String m_name;
    private Vector m_parts = null;
    private String m_rawName;
    private String m_simpleString = null;
    private String m_uri;

    public String getRawName() {
        return this.m_rawName;
    }

    public void setRawName(String rawName) {
        this.m_rawName = rawName;
    }

    public String getName() {
        return this.m_name;
    }

    public void setName(String name) {
        this.m_name = name;
    }

    public String getURI() {
        return this.m_uri;
    }

    public void setURI(String uri) {
        this.m_uri = uri;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:92:?, code lost:
        r2.warn(org.apache.xalan.res.XSLTErrorResources.WG_ATTR_TEMPLATE, new java.lang.Object[]{r16});
     */
    /* JADX WARNING: Removed duplicated region for block: B:113:0x019a A[EDGE_INSN: B:113:0x019a->B:90:0x019a ?: BREAK  , SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:97:0x01ad A[Catch:{ SAXException -> 0x01a6, all -> 0x01de }, LOOP:0: B:6:0x004c->B:97:0x01ad, LOOP_END] */
    public AVT(StylesheetHandler handler, String uri, String name, String rawName, String stringedValue, ElemTemplateElement owner) throws TransformerException {
        String t;
        char c;
        String lookahead;
        StylesheetHandler stylesheetHandler = handler;
        String str = name;
        String str2 = stringedValue;
        Object[] objArr = null;
        this.m_uri = uri;
        this.m_name = str;
        this.m_rawName = rawName;
        int i = 1;
        StringTokenizer tokenizer = new StringTokenizer(str2, "{}\"'", true);
        int nTokens = tokenizer.countTokens();
        if (nTokens < 2) {
            this.m_simpleString = str2;
            ElemTemplateElement elemTemplateElement = owner;
        } else {
            FastStringBuffer buffer = new FastStringBuffer(6);
            FastStringBuffer exprBuffer = new FastStringBuffer(6);
            try {
                this.m_parts = new Vector(nTokens + 1);
                String lookahead2 = null;
                String t2 = null;
                while (true) {
                    String error = t2;
                    if (!tokenizer.hasMoreTokens()) {
                        ElemTemplateElement elemTemplateElement2 = owner;
                        break;
                    }
                    if (lookahead2 != null) {
                        t = lookahead2;
                        lookahead2 = null;
                    } else {
                        t = tokenizer.nextToken();
                    }
                    String t3 = t;
                    if (t3.length() == i) {
                        char charAt = t3.charAt(0);
                        if (charAt == '\"' || charAt == '\'') {
                            ElemTemplateElement elemTemplateElement3 = owner;
                            c = 2;
                            buffer.append(t3);
                        } else {
                            char c2 = '{';
                            if (charAt != '{') {
                                if (charAt != '}') {
                                    buffer.append(t3);
                                    ElemTemplateElement elemTemplateElement4 = owner;
                                } else {
                                    String lookahead3 = tokenizer.nextToken();
                                    if (lookahead3.equals("}")) {
                                        buffer.append(lookahead3);
                                        lookahead = null;
                                    } else {
                                        stylesheetHandler.warn(XSLTErrorResources.WG_FOUND_CURLYBRACE, objArr);
                                        buffer.append("}");
                                        ElemTemplateElement elemTemplateElement5 = owner;
                                        lookahead2 = lookahead3;
                                    }
                                }
                                c = 2;
                            } else {
                                try {
                                    lookahead2 = tokenizer.nextToken();
                                    if (lookahead2.equals("{")) {
                                        buffer.append(lookahead2);
                                        lookahead = null;
                                    } else {
                                        if (buffer.length() > 0) {
                                            try {
                                                this.m_parts.addElement(new AVTPartSimple(buffer.toString()));
                                                buffer.setLength(0);
                                            } catch (NoSuchElementException e) {
                                                ex = e;
                                                ElemTemplateElement elemTemplateElement6 = owner;
                                                NoSuchElementException noSuchElementException = ex;
                                                c = 2;
                                                try {
                                                    error = XSLMessages.createMessage(XSLTErrorResources.ER_ILLEGAL_ATTRIBUTE_VALUE, new Object[]{str, str2});
                                                    if (error != null) {
                                                    }
                                                } catch (SAXException se) {
                                                    throw new TransformerException(se);
                                                } catch (Throwable th) {
                                                    th = th;
                                                    throw th;
                                                }
                                            }
                                        }
                                        int i2 = 0;
                                        exprBuffer.setLength(0);
                                        while (lookahead2 != null) {
                                            if (lookahead2.length() == 1) {
                                                char charAt2 = lookahead2.charAt(i2);
                                                if (charAt2 == '\"' || charAt2 == '\'') {
                                                    ElemTemplateElement elemTemplateElement7 = owner;
                                                    exprBuffer.append(lookahead2);
                                                    String quote = lookahead2;
                                                    String lookahead4 = tokenizer.nextToken();
                                                    while (!lookahead4.equals(quote)) {
                                                        exprBuffer.append(lookahead4);
                                                        lookahead4 = tokenizer.nextToken();
                                                    }
                                                    exprBuffer.append(lookahead4);
                                                    lookahead2 = tokenizer.nextToken();
                                                } else if (charAt2 == c2) {
                                                    ElemTemplateElement elemTemplateElement8 = owner;
                                                    try {
                                                        error = XSLMessages.createMessage(XSLTErrorResources.ER_NO_CURLYBRACE, null);
                                                        lookahead2 = null;
                                                    } catch (NoSuchElementException e2) {
                                                        ex = e2;
                                                        NoSuchElementException noSuchElementException2 = ex;
                                                        c = 2;
                                                        error = XSLMessages.createMessage(XSLTErrorResources.ER_ILLEGAL_ATTRIBUTE_VALUE, new Object[]{str, str2});
                                                        if (error != null) {
                                                        }
                                                    }
                                                } else if (charAt2 != '}') {
                                                    exprBuffer.append(lookahead2);
                                                    lookahead2 = tokenizer.nextToken();
                                                    i2 = 0;
                                                } else {
                                                    buffer.setLength(0);
                                                    try {
                                                        this.m_parts.addElement(new AVTPartXPath(stylesheetHandler.createXPath(exprBuffer.toString(), owner)));
                                                        lookahead2 = null;
                                                    } catch (NoSuchElementException e3) {
                                                        ex = e3;
                                                        NoSuchElementException noSuchElementException22 = ex;
                                                        c = 2;
                                                        error = XSLMessages.createMessage(XSLTErrorResources.ER_ILLEGAL_ATTRIBUTE_VALUE, new Object[]{str, str2});
                                                        if (error != null) {
                                                        }
                                                    }
                                                }
                                            } else {
                                                ElemTemplateElement elemTemplateElement9 = owner;
                                                exprBuffer.append(lookahead2);
                                                lookahead2 = tokenizer.nextToken();
                                            }
                                            i2 = 0;
                                            c2 = '{';
                                        }
                                        ElemTemplateElement elemTemplateElement10 = owner;
                                        if (error != null) {
                                        }
                                        c = 2;
                                    }
                                } catch (NoSuchElementException e4) {
                                    ex = e4;
                                    Object[] objArr2 = objArr;
                                    ElemTemplateElement elemTemplateElement11 = owner;
                                    NoSuchElementException noSuchElementException222 = ex;
                                    c = 2;
                                    error = XSLMessages.createMessage(XSLTErrorResources.ER_ILLEGAL_ATTRIBUTE_VALUE, new Object[]{str, str2});
                                    if (error != null) {
                                    }
                                }
                            }
                            ElemTemplateElement elemTemplateElement12 = owner;
                            lookahead2 = lookahead;
                            c = 2;
                        }
                    } else {
                        ElemTemplateElement elemTemplateElement13 = owner;
                        c = 2;
                        buffer.append(t3);
                    }
                    if (error != null) {
                        break;
                    }
                    char c3 = c;
                    t2 = error;
                    objArr = null;
                    i = 1;
                }
                if (buffer.length() > 0) {
                    this.m_parts.addElement(new AVTPartSimple(buffer.toString()));
                    buffer.setLength(0);
                }
            } catch (SAXException se2) {
                throw new TransformerException(se2);
            } catch (Throwable th2) {
                th = th2;
                ElemTemplateElement elemTemplateElement14 = owner;
                throw th;
            }
        }
        if (this.m_parts == null && this.m_simpleString == null) {
            this.m_simpleString = "";
        }
    }

    public String getSimpleString() {
        if (this.m_simpleString != null) {
            return this.m_simpleString;
        }
        if (this.m_parts == null) {
            return "";
        }
        FastStringBuffer buf = getBuffer();
        int n = this.m_parts.size();
        int i = 0;
        while (i < n) {
            try {
                buf.append(((AVTPart) this.m_parts.elementAt(i)).getSimpleString());
                i++;
            } catch (Throwable th) {
                buf.setLength(0);
                throw th;
            }
        }
        String out = buf.toString();
        buf.setLength(0);
        return out;
    }

    public String evaluate(XPathContext xctxt, int context, PrefixResolver nsNode) throws TransformerException {
        if (this.m_simpleString != null) {
            return this.m_simpleString;
        }
        if (this.m_parts == null) {
            return "";
        }
        FastStringBuffer buf = getBuffer();
        int n = this.m_parts.size();
        int i = 0;
        while (i < n) {
            try {
                ((AVTPart) this.m_parts.elementAt(i)).evaluate(xctxt, buf, context, nsNode);
                i++;
            } catch (Throwable th) {
                buf.setLength(0);
                throw th;
            }
        }
        String out = buf.toString();
        buf.setLength(0);
        return out;
    }

    public boolean isContextInsensitive() {
        return this.m_simpleString != null;
    }

    public boolean canTraverseOutsideSubtree() {
        if (this.m_parts != null) {
            int n = this.m_parts.size();
            for (int i = 0; i < n; i++) {
                if (((AVTPart) this.m_parts.elementAt(i)).canTraverseOutsideSubtree()) {
                    return true;
                }
            }
        }
        return false;
    }

    public void fixupVariables(Vector vars, int globalsSize) {
        if (this.m_parts != null) {
            int n = this.m_parts.size();
            for (int i = 0; i < n; i++) {
                ((AVTPart) this.m_parts.elementAt(i)).fixupVariables(vars, globalsSize);
            }
        }
    }

    public void callVisitors(XSLTVisitor visitor) {
        if (visitor.visitAVT(this) && this.m_parts != null) {
            int n = this.m_parts.size();
            for (int i = 0; i < n; i++) {
                ((AVTPart) this.m_parts.elementAt(i)).callVisitors(visitor);
            }
        }
    }

    public boolean isSimple() {
        return this.m_simpleString != null;
    }

    private final FastStringBuffer getBuffer() {
        return new FastStringBuffer(8);
    }
}
