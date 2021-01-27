package org.ksoap2.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.ksoap2.HeaderProperty;

public class ServiceConnectionSE implements ServiceConnection {
    private HttpURLConnection connection;

    public ServiceConnectionSE(String url) throws IOException {
        this(null, url, 20000);
    }

    public ServiceConnectionSE(Proxy proxy, String url) throws IOException {
        this(proxy, url, 20000);
    }

    public ServiceConnectionSE(String url, int timeout) throws IOException {
        this(null, url, timeout);
    }

    public ServiceConnectionSE(Proxy proxy, String url, int timeout) throws IOException {
        HttpURLConnection httpURLConnection;
        if (proxy == null) {
            httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
        } else {
            httpURLConnection = (HttpURLConnection) new URL(url).openConnection(proxy);
        }
        this.connection = httpURLConnection;
        this.connection.setUseCaches(false);
        this.connection.setDoOutput(true);
        this.connection.setDoInput(true);
        this.connection.setConnectTimeout(timeout);
        this.connection.setReadTimeout(timeout);
    }

    @Override // org.ksoap2.transport.ServiceConnection
    public void connect() throws IOException {
        this.connection.connect();
    }

    @Override // org.ksoap2.transport.ServiceConnection
    public void disconnect() {
        this.connection.disconnect();
    }

    @Override // org.ksoap2.transport.ServiceConnection
    public List getResponseProperties() throws IOException {
        List retList = new LinkedList();
        Map properties = this.connection.getHeaderFields();
        if (properties != null) {
            for (String key : properties.keySet()) {
                List<String> values = properties.get(key);
                for (int j = 0; j < values.size(); j++) {
                    retList.add(new HeaderProperty(key, values.get(j)));
                }
            }
        }
        return retList;
    }

    @Override // org.ksoap2.transport.ServiceConnection
    public int getResponseCode() throws IOException {
        return this.connection.getResponseCode();
    }

    @Override // org.ksoap2.transport.ServiceConnection
    public void setRequestProperty(String string, String soapAction) {
        this.connection.setRequestProperty(string, soapAction);
    }

    @Override // org.ksoap2.transport.ServiceConnection
    public void setRequestMethod(String requestMethod) throws IOException {
        this.connection.setRequestMethod(requestMethod);
    }

    @Override // org.ksoap2.transport.ServiceConnection
    public void setFixedLengthStreamingMode(int contentLength) {
        this.connection.setFixedLengthStreamingMode(contentLength);
    }

    @Override // org.ksoap2.transport.ServiceConnection
    public void setChunkedStreamingMode() {
        this.connection.setChunkedStreamingMode(0);
    }

    @Override // org.ksoap2.transport.ServiceConnection
    public OutputStream openOutputStream() throws IOException {
        return this.connection.getOutputStream();
    }

    @Override // org.ksoap2.transport.ServiceConnection
    public InputStream openInputStream() throws IOException {
        return this.connection.getInputStream();
    }

    @Override // org.ksoap2.transport.ServiceConnection
    public InputStream getErrorStream() {
        return this.connection.getErrorStream();
    }

    @Override // org.ksoap2.transport.ServiceConnection
    public String getHost() {
        return this.connection.getURL().getHost();
    }

    @Override // org.ksoap2.transport.ServiceConnection
    public int getPort() {
        return this.connection.getURL().getPort();
    }

    @Override // org.ksoap2.transport.ServiceConnection
    public String getPath() {
        return this.connection.getURL().getPath();
    }
}
