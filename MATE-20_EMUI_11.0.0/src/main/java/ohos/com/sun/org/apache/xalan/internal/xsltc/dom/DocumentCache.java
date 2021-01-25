package ohos.com.sun.org.apache.xalan.internal.xsltc.dom;

import java.io.File;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import ohos.ai.asr.util.AsrConstants;
import ohos.com.sun.org.apache.xalan.internal.xsltc.DOM;
import ohos.com.sun.org.apache.xalan.internal.xsltc.DOMCache;
import ohos.com.sun.org.apache.xalan.internal.xsltc.DOMEnhancedForDTM;
import ohos.com.sun.org.apache.xalan.internal.xsltc.Translet;
import ohos.com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import ohos.com.sun.org.apache.xalan.internal.xsltc.runtime.BasisLibrary;
import ohos.com.sun.org.apache.xml.internal.utils.SystemIDResolver;
import ohos.javax.xml.parsers.ParserConfigurationException;
import ohos.javax.xml.parsers.SAXParser;
import ohos.javax.xml.parsers.SAXParserFactory;
import ohos.javax.xml.transform.TransformerException;
import ohos.javax.xml.transform.sax.SAXSource;
import ohos.org.xml.sax.InputSource;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.XMLReader;

public final class DocumentCache implements DOMCache {
    private static final int REFRESH_INTERVAL = 1000;
    private String[] _URIs;
    private int _count;
    private int _current;
    private XSLTCDTMManager _dtmManager;
    private SAXParser _parser;
    private XMLReader _reader;
    private Map<String, CachedDocument> _references;
    private int _size;

    public final class CachedDocument {
        private long _accessCount;
        private long _buildTime;
        private DOMEnhancedForDTM _dom = null;
        private long _firstReferenced;
        private long _lastChecked;
        private long _lastModified;
        private long _lastReferenced;

        public CachedDocument(String str) {
            long currentTimeMillis = System.currentTimeMillis();
            this._firstReferenced = currentTimeMillis;
            this._lastReferenced = currentTimeMillis;
            this._accessCount = 0;
            loadDocument(str);
            this._buildTime = System.currentTimeMillis() - currentTimeMillis;
        }

        public void loadDocument(String str) {
            try {
                long currentTimeMillis = System.currentTimeMillis();
                this._dom = (DOMEnhancedForDTM) DocumentCache.this._dtmManager.getDTM(new SAXSource(DocumentCache.this._reader, new InputSource(str)), false, null, true, false);
                this._dom.setDocumentURI(str);
                long currentTimeMillis2 = System.currentTimeMillis() - currentTimeMillis;
                if (this._buildTime > 0) {
                    this._buildTime = (this._buildTime + currentTimeMillis2) >>> 1;
                } else {
                    this._buildTime = currentTimeMillis2;
                }
            } catch (Exception unused) {
                this._dom = null;
            }
        }

        public DOM getDocument() {
            return this._dom;
        }

        public long getFirstReferenced() {
            return this._firstReferenced;
        }

        public long getLastReferenced() {
            return this._lastReferenced;
        }

        public long getAccessCount() {
            return this._accessCount;
        }

        public void incAccessCount() {
            this._accessCount++;
        }

        public long getLastModified() {
            return this._lastModified;
        }

        public void setLastModified(long j) {
            this._lastModified = j;
        }

        public long getLatency() {
            return this._buildTime;
        }

        public long getLastChecked() {
            return this._lastChecked;
        }

        public void setLastChecked(long j) {
            this._lastChecked = j;
        }

        public long getEstimatedSize() {
            DOMEnhancedForDTM dOMEnhancedForDTM = this._dom;
            if (dOMEnhancedForDTM != null) {
                return (long) (dOMEnhancedForDTM.getSize() << 5);
            }
            return 0;
        }
    }

    public DocumentCache(int i) throws SAXException {
        this(i, null);
        try {
            this._dtmManager = XSLTCDTMManager.createNewDTMManagerInstance();
        } catch (Exception e) {
            throw new SAXException(e);
        }
    }

    public DocumentCache(int i, XSLTCDTMManager xSLTCDTMManager) throws SAXException {
        this._dtmManager = xSLTCDTMManager;
        this._count = 0;
        this._current = 0;
        this._size = i;
        this._references = new HashMap(this._size + 2);
        this._URIs = new String[this._size];
        try {
            SAXParserFactory newInstance = SAXParserFactory.newInstance();
            try {
                newInstance.setFeature("http://xml.org/sax/features/namespaces", true);
            } catch (Exception unused) {
                newInstance.setNamespaceAware(true);
            }
            this._parser = newInstance.newSAXParser();
            this._reader = this._parser.getXMLReader();
        } catch (ParserConfigurationException unused2) {
            BasisLibrary.runTimeError(BasisLibrary.NAMESPACES_SUPPORT_ERR);
        }
    }

    private final long getLastModified(String str) {
        try {
            URL url = new URL(str);
            long lastModified = url.openConnection().getLastModified();
            return (lastModified != 0 || !AsrConstants.ASR_SRC_FILE.equals(url.getProtocol())) ? lastModified : new File(URLDecoder.decode(url.getFile())).lastModified();
        } catch (Exception unused) {
            return System.currentTimeMillis();
        }
    }

    private CachedDocument lookupDocument(String str) {
        return this._references.get(str);
    }

    private synchronized void insertDocument(String str, CachedDocument cachedDocument) {
        if (this._count < this._size) {
            String[] strArr = this._URIs;
            int i = this._count;
            this._count = i + 1;
            strArr[i] = str;
            this._current = 0;
        } else {
            this._references.remove(this._URIs[this._current]);
            this._URIs[this._current] = str;
            int i2 = this._current + 1;
            this._current = i2;
            if (i2 >= this._size) {
                this._current = 0;
            }
        }
        this._references.put(str, cachedDocument);
    }

    private synchronized void replaceDocument(String str, CachedDocument cachedDocument) {
        if (cachedDocument == null) {
            insertDocument(str, cachedDocument);
        } else {
            this._references.put(str, cachedDocument);
        }
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOMCache
    public DOM retrieveDocument(String str, String str2, Translet translet) {
        if (str != null && !str.equals("")) {
            try {
                str2 = SystemIDResolver.getAbsoluteURI(str2, str);
            } catch (TransformerException unused) {
            }
        }
        CachedDocument lookupDocument = lookupDocument(str2);
        if (lookupDocument == null) {
            lookupDocument = new CachedDocument(str2);
            lookupDocument.setLastModified(getLastModified(str2));
            insertDocument(str2, lookupDocument);
        } else {
            long currentTimeMillis = System.currentTimeMillis();
            long lastChecked = lookupDocument.getLastChecked();
            lookupDocument.setLastChecked(currentTimeMillis);
            if (currentTimeMillis > lastChecked + 1000) {
                lookupDocument.setLastChecked(currentTimeMillis);
                if (getLastModified(str2) > lookupDocument.getLastModified()) {
                    lookupDocument = new CachedDocument(str2);
                    lookupDocument.setLastModified(getLastModified(str2));
                    replaceDocument(str2, lookupDocument);
                }
            }
        }
        DOM document = lookupDocument.getDocument();
        if (document == null) {
            return null;
        }
        lookupDocument.incAccessCount();
        ((AbstractTranslet) translet).prepassDocument(document);
        return lookupDocument.getDocument();
    }

    public void getStatistics(PrintWriter printWriter) {
        printWriter.println("<h2>DOM cache statistics</h2><center><table border=\"2\"><tr><td><b>Document URI</b></td><td><center><b>Build time</b></center></td><td><center><b>Access count</b></center></td><td><center><b>Last accessed</b></center></td><td><center><b>Last modified</b></center></td></tr>");
        for (int i = 0; i < this._count; i++) {
            CachedDocument cachedDocument = this._references.get(this._URIs[i]);
            printWriter.print("<tr><td><a href=\"" + this._URIs[i] + "\"><font size=-1>" + this._URIs[i] + "</font></a></td>");
            StringBuilder sb = new StringBuilder();
            sb.append("<td><center>");
            sb.append(cachedDocument.getLatency());
            sb.append("ms</center></td>");
            printWriter.print(sb.toString());
            printWriter.print("<td><center>" + cachedDocument.getAccessCount() + "</center></td>");
            printWriter.print("<td><center>" + new Date(cachedDocument.getLastReferenced()) + "</center></td>");
            printWriter.print("<td><center>" + new Date(cachedDocument.getLastModified()) + "</center></td>");
            printWriter.println("</tr>");
        }
        printWriter.println("</table></center>");
    }
}
