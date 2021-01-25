package org.ksoap2.transport;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import libcore.util.XmlObjectFactory;
import org.ksoap2.SoapEnvelope;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public abstract class Transport {
    protected static final String CONTENT_TYPE_SOAP_XML_CHARSET_UTF_8 = "application/soap+xml;charset=utf-8";
    protected static final String CONTENT_TYPE_XML_CHARSET_UTF_8 = "text/xml;charset=utf-8";
    protected static final String USER_AGENT = "ksoap2-android/2.6.0+";
    private int bufferLength;
    public boolean debug;
    private HashMap prefixes;
    protected Proxy proxy;
    public String requestDump;
    public String responseDump;
    protected int timeout;
    protected String url;
    private String xmlVersionTag;

    public abstract List call(String str, SoapEnvelope soapEnvelope, List list) throws IOException, XmlPullParserException;

    public abstract List call(String str, SoapEnvelope soapEnvelope, List list, File file) throws IOException, XmlPullParserException;

    public abstract ServiceConnection getServiceConnection() throws IOException;

    public HashMap getPrefixes() {
        return this.prefixes;
    }

    public Transport() {
        this.timeout = 20000;
        this.debug = true;
        this.xmlVersionTag = "";
        this.bufferLength = ServiceConnection.DEFAULT_BUFFER_SIZE;
        this.prefixes = new HashMap();
    }

    public Transport(String url2) {
        this((Proxy) null, url2);
    }

    public Transport(String url2, int timeout2) {
        this.timeout = 20000;
        this.debug = true;
        this.xmlVersionTag = "";
        this.bufferLength = ServiceConnection.DEFAULT_BUFFER_SIZE;
        this.prefixes = new HashMap();
        this.url = url2;
        this.timeout = timeout2;
    }

    public Transport(String url2, int timeout2, int bufferLength2) {
        this.timeout = 20000;
        this.debug = true;
        this.xmlVersionTag = "";
        this.bufferLength = ServiceConnection.DEFAULT_BUFFER_SIZE;
        this.prefixes = new HashMap();
        this.url = url2;
        this.timeout = timeout2;
        this.bufferLength = bufferLength2;
    }

    public Transport(Proxy proxy2, String url2) {
        this.timeout = 20000;
        this.debug = true;
        this.xmlVersionTag = "";
        this.bufferLength = ServiceConnection.DEFAULT_BUFFER_SIZE;
        this.prefixes = new HashMap();
        this.proxy = proxy2;
        this.url = url2;
    }

    public Transport(Proxy proxy2, String url2, int timeout2) {
        this.timeout = 20000;
        this.debug = true;
        this.xmlVersionTag = "";
        this.bufferLength = ServiceConnection.DEFAULT_BUFFER_SIZE;
        this.prefixes = new HashMap();
        this.proxy = proxy2;
        this.url = url2;
        this.timeout = timeout2;
    }

    public Transport(Proxy proxy2, String url2, int timeout2, int bufferLength2) {
        this.timeout = 20000;
        this.debug = true;
        this.xmlVersionTag = "";
        this.bufferLength = ServiceConnection.DEFAULT_BUFFER_SIZE;
        this.prefixes = new HashMap();
        this.proxy = proxy2;
        this.url = url2;
        this.timeout = timeout2;
        this.bufferLength = bufferLength2;
    }

    /* access modifiers changed from: protected */
    public void parseResponse(SoapEnvelope envelope, InputStream is) throws XmlPullParserException, IOException {
        XmlPullParser xp = XmlObjectFactory.newXmlPullParser();
        xp.setFeature("http://xmlpull.org/v1/doc/features.html#process-namespaces", true);
        xp.setInput(is, null);
        envelope.parse(xp);
        is.close();
    }

    /* access modifiers changed from: protected */
    public byte[] createRequestData(SoapEnvelope envelope, String encoding) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(this.bufferLength);
        bos.write(this.xmlVersionTag.getBytes());
        XmlSerializer xw = XmlObjectFactory.newXmlSerializer();
        xw.setOutput(bos, encoding);
        for (String key : this.prefixes.keySet()) {
            xw.setPrefix(key, (String) this.prefixes.get(key));
        }
        envelope.write(xw);
        xw.flush();
        bos.write(13);
        bos.write(10);
        bos.flush();
        return bos.toByteArray();
    }

    /* access modifiers changed from: protected */
    public byte[] createRequestData(SoapEnvelope envelope) throws IOException {
        return createRequestData(envelope, null);
    }

    public void setUrl(String url2) {
        this.url = url2;
    }

    public String getUrl() {
        return this.url;
    }

    public void setXmlVersionTag(String tag) {
        this.xmlVersionTag = tag;
    }

    public void reset() {
    }

    public void call(String soapAction, SoapEnvelope envelope) throws IOException, XmlPullParserException {
        call(soapAction, envelope, null);
    }

    public String getHost() throws MalformedURLException {
        return new URL(this.url).getHost();
    }

    public int getPort() throws MalformedURLException {
        return new URL(this.url).getPort();
    }

    public String getPath() throws MalformedURLException {
        return new URL(this.url).getPath();
    }
}
