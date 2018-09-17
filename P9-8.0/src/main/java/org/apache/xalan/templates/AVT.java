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

    /* Code decompiled incorrectly, please refer to instructions dump. */
    public AVT(StylesheetHandler handler, String uri, String name, String rawName, String stringedValue, ElemTemplateElement owner) throws TransformerException {
        this.m_uri = uri;
        this.m_name = name;
        this.m_rawName = rawName;
        StringTokenizer tokenizer = new StringTokenizer(stringedValue, "{}\"'", true);
        int nTokens = tokenizer.countTokens();
        if (nTokens < 2) {
            this.m_simpleString = stringedValue;
        } else {
            FastStringBuffer buffer = new FastStringBuffer(6);
            FastStringBuffer exprBuffer = new FastStringBuffer(6);
            String error;
            try {
                this.m_parts = new Vector(nTokens + 1);
                String lookahead = null;
                error = null;
                while (tokenizer.hasMoreTokens()) {
                    String t;
                    if (lookahead != null) {
                        t = lookahead;
                        lookahead = null;
                    } else {
                        t = tokenizer.nextToken();
                    }
                    if (t.length() == 1) {
                        switch (t.charAt(0)) {
                            case '\"':
                            case '\'':
                                buffer.append(t);
                                continue;
                            case '{':
                                lookahead = tokenizer.nextToken();
                                if (lookahead.equals("{")) {
                                    buffer.append(lookahead);
                                    lookahead = null;
                                    continue;
                                } else {
                                    if (buffer.length() > 0) {
                                        this.m_parts.addElement(new AVTPartSimple(buffer.toString()));
                                        buffer.setLength(0);
                                    }
                                    exprBuffer.setLength(0);
                                    while (lookahead != null) {
                                        if (lookahead.length() == 1) {
                                            switch (lookahead.charAt(0)) {
                                                case '\"':
                                                case '\'':
                                                    exprBuffer.append(lookahead);
                                                    String quote = lookahead;
                                                    lookahead = tokenizer.nextToken();
                                                    while (!lookahead.equals(quote)) {
                                                        exprBuffer.append(lookahead);
                                                        lookahead = tokenizer.nextToken();
                                                    }
                                                    exprBuffer.append(lookahead);
                                                    lookahead = tokenizer.nextToken();
                                                    break;
                                                case '{':
                                                    error = XSLMessages.createMessage(XSLTErrorResources.ER_NO_CURLYBRACE, null);
                                                    lookahead = null;
                                                    break;
                                                case '}':
                                                    buffer.setLength(0);
                                                    this.m_parts.addElement(new AVTPartXPath(handler.createXPath(exprBuffer.toString(), owner)));
                                                    lookahead = null;
                                                    break;
                                                default:
                                                    exprBuffer.append(lookahead);
                                                    lookahead = tokenizer.nextToken();
                                                    break;
                                            }
                                        }
                                        exprBuffer.append(lookahead);
                                        lookahead = tokenizer.nextToken();
                                    }
                                    continue;
                                }
                            case '}':
                                lookahead = tokenizer.nextToken();
                                if (lookahead.equals("}")) {
                                    buffer.append(lookahead);
                                    lookahead = null;
                                    continue;
                                } else {
                                    handler.warn(XSLTErrorResources.WG_FOUND_CURLYBRACE, null);
                                    buffer.append("}");
                                    continue;
                                }
                            default:
                                buffer.append(t);
                                continue;
                        }
                    } else {
                        buffer.append(t);
                        continue;
                    }
                    if (error != null) {
                        handler.warn(XSLTErrorResources.WG_ATTR_TEMPLATE, new Object[]{error});
                    }
                }
            } catch (SAXException se) {
                throw new TransformerException(se);
            } catch (SAXException se2) {
                throw new TransformerException(se2);
            } catch (NoSuchElementException e) {
                error = XSLMessages.createMessage(XSLTErrorResources.ER_ILLEGAL_ATTRIBUTE_VALUE, new Object[]{name, stringedValue});
                continue;
            } catch (Throwable th) {
            }
            if (buffer.length() > 0) {
                this.m_parts.addElement(new AVTPartSimple(buffer.toString()));
                buffer.setLength(0);
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
