package ohos.com.sun.org.apache.xml.internal.utils;

import java.util.StringTokenizer;
import java.util.Vector;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.javax.xml.transform.Source;
import ohos.javax.xml.transform.TransformerException;
import ohos.javax.xml.transform.URIResolver;
import ohos.javax.xml.transform.sax.SAXSource;
import ohos.org.xml.sax.Attributes;
import ohos.org.xml.sax.InputSource;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.helpers.DefaultHandler;

public class StylesheetPIHandler extends DefaultHandler {
    String m_baseID;
    String m_charset;
    String m_media;
    Vector m_stylesheets = new Vector();
    String m_title;
    URIResolver m_uriResolver;

    public void setURIResolver(URIResolver uRIResolver) {
        this.m_uriResolver = uRIResolver;
    }

    public URIResolver getURIResolver() {
        return this.m_uriResolver;
    }

    public StylesheetPIHandler(String str, String str2, String str3, String str4) {
        this.m_baseID = str;
        this.m_media = str2;
        this.m_title = str3;
        this.m_charset = str4;
    }

    public Source getAssociatedStylesheet() {
        int size = this.m_stylesheets.size();
        if (size > 0) {
            return (Source) this.m_stylesheets.elementAt(size - 1);
        }
        return null;
    }

    public void processingInstruction(String str, String str2) throws SAXException {
        boolean z;
        String str3;
        if (str.equals("xml-stylesheet")) {
            StringTokenizer stringTokenizer = new StringTokenizer(str2, " \t=\n", true);
            String str4 = null;
            String str5 = null;
            String str6 = null;
            String str7 = null;
            String str8 = null;
            Source source = null;
            String str9 = "";
            boolean z2 = false;
            while (stringTokenizer.hasMoreTokens()) {
                if (!z2) {
                    str9 = stringTokenizer.nextToken();
                } else {
                    z2 = false;
                }
                if (!stringTokenizer.hasMoreTokens() || (!str9.equals(" ") && !str9.equals("\t") && !str9.equals("="))) {
                    if (str9.equals("type")) {
                        String nextToken = stringTokenizer.nextToken();
                        while (stringTokenizer.hasMoreTokens() && (nextToken.equals(" ") || nextToken.equals("\t") || nextToken.equals("="))) {
                            nextToken = stringTokenizer.nextToken();
                        }
                        str9 = nextToken;
                        str4 = nextToken.substring(1, nextToken.length() - 1);
                    } else if (str9.equals(Constants.ATTRNAME_HREF)) {
                        str9 = stringTokenizer.nextToken();
                        while (stringTokenizer.hasMoreTokens() && (str9.equals(" ") || str9.equals("\t") || str9.equals("="))) {
                            str9 = stringTokenizer.nextToken();
                        }
                        if (stringTokenizer.hasMoreTokens()) {
                            z = z2;
                            str3 = str9;
                            str9 = stringTokenizer.nextToken();
                            while (str9.equals("=") && stringTokenizer.hasMoreTokens()) {
                                str3 = str3 + str9 + stringTokenizer.nextToken();
                                if (!stringTokenizer.hasMoreTokens()) {
                                    break;
                                }
                                str9 = stringTokenizer.nextToken();
                                z = true;
                            }
                        } else {
                            z = z2;
                            str3 = str9;
                        }
                        String substring = str3.substring(1, str3.length() - 1);
                        try {
                            if (this.m_uriResolver != null) {
                                source = this.m_uriResolver.resolve(substring, this.m_baseID);
                            } else {
                                substring = SystemIDResolver.getAbsoluteURI(substring, this.m_baseID);
                                source = new SAXSource(new InputSource(substring));
                            }
                            str5 = substring;
                            z2 = z;
                        } catch (TransformerException e) {
                            throw new SAXException(e);
                        }
                    } else if (str9.equals("title")) {
                        str9 = stringTokenizer.nextToken();
                        while (stringTokenizer.hasMoreTokens() && (str9.equals(" ") || str9.equals("\t") || str9.equals("="))) {
                            str9 = stringTokenizer.nextToken();
                        }
                        str8 = str9.substring(1, str9.length() - 1);
                    } else if (str9.equals("media")) {
                        str9 = stringTokenizer.nextToken();
                        while (stringTokenizer.hasMoreTokens() && (str9.equals(" ") || str9.equals("\t") || str9.equals("="))) {
                            str9 = stringTokenizer.nextToken();
                        }
                        str6 = str9.substring(1, str9.length() - 1);
                    } else if (str9.equals("charset")) {
                        str9 = stringTokenizer.nextToken();
                        while (stringTokenizer.hasMoreTokens() && (str9.equals(" ") || str9.equals("\t") || str9.equals("="))) {
                            str9 = stringTokenizer.nextToken();
                        }
                        str7 = str9.substring(1, str9.length() - 1);
                    } else if (str9.equals("alternate")) {
                        str9 = stringTokenizer.nextToken();
                        while (stringTokenizer.hasMoreTokens() && (str9.equals(" ") || str9.equals("\t") || str9.equals("="))) {
                            str9 = stringTokenizer.nextToken();
                        }
                        str9.substring(1, str9.length() - 1).equals("yes");
                    }
                }
            }
            if (str4 == null) {
                return;
            }
            if ((str4.equals("text/xsl") || str4.equals("text/xml") || str4.equals("application/xml+xslt")) && str5 != null) {
                String str10 = this.m_media;
                if (str10 == null || (str6 != null && str6.equals(str10))) {
                    String str11 = this.m_charset;
                    if (str11 == null || (str7 != null && str7.equals(str11))) {
                        String str12 = this.m_title;
                        if (str12 == null || (str8 != null && str8.equals(str12))) {
                            this.m_stylesheets.addElement(source);
                        }
                    }
                }
            }
        }
    }

    /* JADX WARN: Type inference failed for: r0v1, types: [java.lang.Throwable, ohos.com.sun.org.apache.xml.internal.utils.StopParseException] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public void startElement(String str, String str2, String str3, Attributes attributes) throws SAXException {
        throw new StopParseException();
    }

    public void setBaseId(String str) {
        this.m_baseID = str;
    }

    public String getBaseId() {
        return this.m_baseID;
    }
}
