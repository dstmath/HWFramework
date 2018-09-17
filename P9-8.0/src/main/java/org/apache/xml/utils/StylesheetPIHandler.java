package org.apache.xml.utils;

import java.util.StringTokenizer;
import java.util.Vector;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;
import org.apache.xalan.templates.Constants;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class StylesheetPIHandler extends DefaultHandler {
    String m_baseID;
    String m_charset;
    String m_media;
    Vector m_stylesheets = new Vector();
    String m_title;
    URIResolver m_uriResolver;

    public void setURIResolver(URIResolver resolver) {
        this.m_uriResolver = resolver;
    }

    public URIResolver getURIResolver() {
        return this.m_uriResolver;
    }

    public StylesheetPIHandler(String baseID, String media, String title, String charset) {
        this.m_baseID = baseID;
        this.m_media = media;
        this.m_title = title;
        this.m_charset = charset;
    }

    public Source getAssociatedStylesheet() {
        int sz = this.m_stylesheets.size();
        if (sz > 0) {
            return (Source) this.m_stylesheets.elementAt(sz - 1);
        }
        return null;
    }

    public void processingInstruction(String target, String data) throws SAXException {
        if (target.equals("xml-stylesheet")) {
            String href = null;
            String type = null;
            String title = null;
            String media = null;
            String charset = null;
            StringTokenizer tokenizer = new StringTokenizer(data, " \t=\n", true);
            boolean lookedAhead = false;
            Object source = null;
            String token = "";
            while (tokenizer.hasMoreTokens()) {
                if (lookedAhead) {
                    lookedAhead = false;
                } else {
                    token = tokenizer.nextToken();
                }
                if (!(tokenizer.hasMoreTokens() && (token.equals(" ") || token.equals("\t") || token.equals("=")))) {
                    String name = token;
                    if (name.equals("type")) {
                        token = tokenizer.nextToken();
                        while (tokenizer.hasMoreTokens() && (token.equals(" ") || token.equals("\t") || token.equals("="))) {
                            token = tokenizer.nextToken();
                        }
                        type = token.substring(1, token.length() - 1);
                    } else if (name.equals(Constants.ATTRNAME_HREF)) {
                        token = tokenizer.nextToken();
                        while (tokenizer.hasMoreTokens() && (token.equals(" ") || token.equals("\t") || token.equals("="))) {
                            token = tokenizer.nextToken();
                        }
                        href = token;
                        if (tokenizer.hasMoreTokens()) {
                            token = tokenizer.nextToken();
                            while (token.equals("=") && tokenizer.hasMoreTokens()) {
                                href = href + token + tokenizer.nextToken();
                                if (!tokenizer.hasMoreTokens()) {
                                    break;
                                }
                                token = tokenizer.nextToken();
                                lookedAhead = true;
                            }
                        }
                        href = href.substring(1, href.length() - 1);
                        try {
                            if (this.m_uriResolver != null) {
                                source = this.m_uriResolver.resolve(href, this.m_baseID);
                            } else {
                                href = SystemIDResolver.getAbsoluteURI(href, this.m_baseID);
                                source = new SAXSource(new InputSource(href));
                            }
                        } catch (TransformerException te) {
                            throw new SAXException(te);
                        }
                    } else if (name.equals("title")) {
                        token = tokenizer.nextToken();
                        while (tokenizer.hasMoreTokens() && (token.equals(" ") || token.equals("\t") || token.equals("="))) {
                            token = tokenizer.nextToken();
                        }
                        title = token.substring(1, token.length() - 1);
                    } else if (name.equals("media")) {
                        token = tokenizer.nextToken();
                        while (tokenizer.hasMoreTokens() && (token.equals(" ") || token.equals("\t") || token.equals("="))) {
                            token = tokenizer.nextToken();
                        }
                        media = token.substring(1, token.length() - 1);
                    } else if (name.equals("charset")) {
                        token = tokenizer.nextToken();
                        while (tokenizer.hasMoreTokens() && (token.equals(" ") || token.equals("\t") || token.equals("="))) {
                            token = tokenizer.nextToken();
                        }
                        charset = token.substring(1, token.length() - 1);
                    } else if (name.equals("alternate")) {
                        token = tokenizer.nextToken();
                        while (tokenizer.hasMoreTokens() && (token.equals(" ") || token.equals("\t") || token.equals("="))) {
                            token = tokenizer.nextToken();
                        }
                        boolean alternate = token.substring(1, token.length() - 1).equals("yes");
                    }
                }
            }
            if (type != null && ((type.equals("text/xsl") || type.equals("text/xml") || type.equals("application/xml+xslt")) && href != null && (this.m_media == null || (media != null && media.equals(this.m_media))))) {
                if (this.m_charset != null && (charset == null || !charset.equals(this.m_charset))) {
                    return;
                }
                if (this.m_title == null || (title != null && title.equals(this.m_title))) {
                    this.m_stylesheets.addElement(source);
                }
            }
        }
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        throw new StopParseException();
    }

    public void setBaseId(String baseId) {
        this.m_baseID = baseId;
    }

    public String getBaseId() {
        return this.m_baseID;
    }
}
