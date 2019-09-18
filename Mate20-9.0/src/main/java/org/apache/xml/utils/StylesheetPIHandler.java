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
        String token;
        String token2;
        Source source;
        if (target.equals("xml-stylesheet")) {
            String href = null;
            String type = null;
            String title = null;
            String media = null;
            String charset = null;
            int i = 1;
            StringTokenizer tokenizer = new StringTokenizer(data, " \t=\n", true);
            boolean lookedAhead = false;
            Source source2 = null;
            String token3 = "";
            while (tokenizer.hasMoreTokens()) {
                if (!lookedAhead) {
                    token3 = tokenizer.nextToken();
                } else {
                    lookedAhead = false;
                }
                if (!tokenizer.hasMoreTokens() || (!token3.equals(" ") && !token3.equals("\t") && !token3.equals("="))) {
                    String name = token3;
                    if (name.equals("type")) {
                        token3 = tokenizer.nextToken();
                        while (tokenizer.hasMoreTokens() && (token3.equals(" ") || token3.equals("\t") || token3.equals("="))) {
                            token3 = tokenizer.nextToken();
                        }
                        type = token3.substring(i, token3.length() - i);
                    } else if (name.equals(Constants.ATTRNAME_HREF)) {
                        String token4 = tokenizer.nextToken();
                        while (tokenizer.hasMoreTokens() && (token2.equals(" ") || token2.equals("\t") || token2.equals("="))) {
                            token4 = tokenizer.nextToken();
                        }
                        String href2 = token2;
                        if (tokenizer.hasMoreTokens()) {
                            token2 = tokenizer.nextToken();
                            while (token2.equals("=") && tokenizer.hasMoreTokens()) {
                                href2 = href2 + token2 + tokenizer.nextToken();
                                if (!tokenizer.hasMoreTokens()) {
                                    break;
                                }
                                token2 = tokenizer.nextToken();
                                lookedAhead = true;
                            }
                        }
                        String href3 = href2.substring(1, href2.length() - 1);
                        try {
                            if (this.m_uriResolver != null) {
                                source = this.m_uriResolver.resolve(href3, this.m_baseID);
                            } else {
                                href3 = SystemIDResolver.getAbsoluteURI(href3, this.m_baseID);
                                source = new SAXSource(new InputSource(href3));
                            }
                            source2 = source;
                            href = href3;
                        } catch (TransformerException te) {
                            throw new SAXException(te);
                        }
                    } else {
                        if (name.equals("title")) {
                            token = tokenizer.nextToken();
                            while (tokenizer.hasMoreTokens() && (token.equals(" ") || token.equals("\t") || token.equals("="))) {
                                token = tokenizer.nextToken();
                            }
                            title = token.substring(1, token.length() - 1);
                        } else if (name.equals("media")) {
                            String token5 = tokenizer.nextToken();
                            while (tokenizer.hasMoreTokens() && (token.equals(" ") || token.equals("\t") || token.equals("="))) {
                                token5 = tokenizer.nextToken();
                            }
                            media = token.substring(1, token.length() - 1);
                        } else if (name.equals("charset")) {
                            String token6 = tokenizer.nextToken();
                            while (tokenizer.hasMoreTokens() && (token.equals(" ") || token.equals("\t") || token.equals("="))) {
                                token6 = tokenizer.nextToken();
                            }
                            charset = token.substring(1, token.length() - 1);
                        } else if (name.equals("alternate")) {
                            String token7 = tokenizer.nextToken();
                            while (tokenizer.hasMoreTokens() && (token.equals(" ") || token.equals("\t") || token.equals("="))) {
                                token7 = tokenizer.nextToken();
                            }
                            boolean alternate = token.substring(1, token.length() - 1).equals("yes");
                        }
                        token3 = token;
                    }
                    i = 1;
                }
            }
            if (type != null && ((type.equals("text/xsl") || type.equals("text/xml") || type.equals("application/xml+xslt")) && href != null && (this.m_media == null || (media != null && media.equals(this.m_media))))) {
                if (this.m_charset != null && (charset == null || !charset.equals(this.m_charset))) {
                    return;
                }
                if (this.m_title == null || (title != null && title.equals(this.m_title))) {
                    this.m_stylesheets.addElement(source2);
                }
            }
        } else {
            String str = data;
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
