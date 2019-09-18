package org.bouncycastle.est.jcajce;

import com.huawei.security.hccm.common.connection.HttpConnection;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;
import org.bouncycastle.est.ESTClient;
import org.bouncycastle.est.ESTClientSourceProvider;
import org.bouncycastle.est.ESTException;
import org.bouncycastle.est.ESTRequest;
import org.bouncycastle.est.ESTRequestBuilder;
import org.bouncycastle.est.ESTResponse;
import org.bouncycastle.est.Source;
import org.bouncycastle.util.Properties;

class DefaultESTClient implements ESTClient {
    private static byte[] CRLF = {13, 10};
    private static final Charset utf8 = Charset.forName("UTF-8");
    private final ESTClientSourceProvider sslSocketProvider;

    private class PrintingOutputStream extends OutputStream {
        private final OutputStream tgt;

        public PrintingOutputStream(OutputStream outputStream) {
            this.tgt = outputStream;
        }

        public void write(int i) throws IOException {
            System.out.print(String.valueOf((char) i));
            this.tgt.write(i);
        }
    }

    public DefaultESTClient(ESTClientSourceProvider eSTClientSourceProvider) {
        this.sslSocketProvider = eSTClientSourceProvider;
    }

    private static void writeLine(OutputStream outputStream, String str) throws IOException {
        outputStream.write(str.getBytes());
        outputStream.write(CRLF);
    }

    public ESTResponse doRequest(ESTRequest eSTRequest) throws IOException {
        ESTResponse performRequest;
        int i = 15;
        while (true) {
            performRequest = performRequest(eSTRequest);
            ESTRequest redirectURL = redirectURL(performRequest);
            if (redirectURL == null) {
                break;
            }
            i--;
            if (i <= 0) {
                break;
            }
            eSTRequest = redirectURL;
        }
        if (i != 0) {
            return performRequest;
        }
        throw new ESTException("Too many redirects..");
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x0064 A[Catch:{ all -> 0x0155 }] */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x006d A[Catch:{ all -> 0x0155 }] */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0087 A[Catch:{ all -> 0x0155 }] */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x009a A[Catch:{ all -> 0x0155 }] */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x00ba A[Catch:{ all -> 0x0155 }] */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x00f7 A[Catch:{ all -> 0x0155 }] */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x013f A[Catch:{ all -> 0x0155 }] */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x014f A[SYNTHETIC, Splitter:B:40:0x014f] */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x015b  */
    public ESTResponse performRequest(ESTRequest eSTRequest) throws IOException {
        Source source;
        OutputStream outputStream;
        URL url;
        String str;
        String host;
        ESTRequest build;
        try {
            source = this.sslSocketProvider.makeSource(eSTRequest.getURL().getHost(), eSTRequest.getURL().getPort());
            try {
                if (eSTRequest.getListener() != null) {
                    eSTRequest = eSTRequest.getListener().onConnection(source, eSTRequest);
                }
                Set<String> asKeySet = Properties.asKeySet("org.bouncycastle.debug.est");
                if (!asKeySet.contains("output")) {
                    if (!asKeySet.contains("all")) {
                        outputStream = source.getOutputStream();
                        StringBuilder sb = new StringBuilder();
                        sb.append(eSTRequest.getURL().getPath());
                        sb.append(eSTRequest.getURL().getQuery() == null ? eSTRequest.getURL().getQuery() : "");
                        String sb2 = sb.toString();
                        ESTRequestBuilder eSTRequestBuilder = new ESTRequestBuilder(eSTRequest);
                        if (!eSTRequest.getHeaders().containsKey(HttpConnection.HttpHeaders.CONNECTION)) {
                            eSTRequestBuilder.addHeader(HttpConnection.HttpHeaders.CONNECTION, HttpConnection.HttpHeaders.CONNECTION_CLOSE);
                        }
                        url = eSTRequest.getURL();
                        if (url.getPort() <= -1) {
                            str = "Host";
                            host = String.format("%s:%d", new Object[]{url.getHost(), Integer.valueOf(url.getPort())});
                        } else {
                            str = "Host";
                            host = url.getHost();
                        }
                        eSTRequestBuilder.setHeader(str, host);
                        build = eSTRequestBuilder.build();
                        writeLine(outputStream, build.getMethod() + " " + sb2 + " HTTP/1.1");
                        for (Map.Entry value : build.getHeaders().entrySet()) {
                            String[] strArr = (String[]) value.getValue();
                            for (int i = 0; i != strArr.length; i++) {
                                writeLine(outputStream, ((String) value.getKey()) + ": " + strArr[i]);
                            }
                        }
                        outputStream.write(CRLF);
                        outputStream.flush();
                        build.writeData(outputStream);
                        outputStream.flush();
                        if (build.getHijacker() != null) {
                            return new ESTResponse(build, source);
                        }
                        ESTResponse hijack = build.getHijacker().hijack(build, source);
                        if (source != null && hijack == null) {
                            source.close();
                        }
                        return hijack;
                    }
                }
                outputStream = new PrintingOutputStream(source.getOutputStream());
                StringBuilder sb3 = new StringBuilder();
                sb3.append(eSTRequest.getURL().getPath());
                sb3.append(eSTRequest.getURL().getQuery() == null ? eSTRequest.getURL().getQuery() : "");
                String sb22 = sb3.toString();
                ESTRequestBuilder eSTRequestBuilder2 = new ESTRequestBuilder(eSTRequest);
                if (!eSTRequest.getHeaders().containsKey(HttpConnection.HttpHeaders.CONNECTION)) {
                }
                url = eSTRequest.getURL();
                if (url.getPort() <= -1) {
                }
                eSTRequestBuilder2.setHeader(str, host);
                build = eSTRequestBuilder2.build();
                writeLine(outputStream, build.getMethod() + " " + sb22 + " HTTP/1.1");
                while (r2.hasNext()) {
                }
                outputStream.write(CRLF);
                outputStream.flush();
                build.writeData(outputStream);
                outputStream.flush();
                if (build.getHijacker() != null) {
                }
            } catch (Throwable th) {
                th = th;
                if (source != null) {
                    source.close();
                }
                throw th;
            }
        } catch (Throwable th2) {
            th = th2;
            source = null;
            if (source != null) {
            }
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    public ESTRequest redirectURL(ESTResponse eSTResponse) throws IOException {
        ESTRequest eSTRequest;
        ESTRequestBuilder withURL;
        if (eSTResponse.getStatusCode() < 300 || eSTResponse.getStatusCode() > 399) {
            eSTRequest = null;
        } else {
            switch (eSTResponse.getStatusCode()) {
                case 301:
                case 302:
                case 303:
                case 306:
                case 307:
                    String header = eSTResponse.getHeader("Location");
                    if (!"".equals(header)) {
                        ESTRequestBuilder eSTRequestBuilder = new ESTRequestBuilder(eSTResponse.getOriginalRequest());
                        if (header.startsWith("http")) {
                            withURL = eSTRequestBuilder.withURL(new URL(header));
                        } else {
                            URL url = eSTResponse.getOriginalRequest().getURL();
                            withURL = eSTRequestBuilder.withURL(new URL(url.getProtocol(), url.getHost(), url.getPort(), header));
                        }
                        eSTRequest = withURL.build();
                        break;
                    } else {
                        throw new ESTException("Redirect status type: " + eSTResponse.getStatusCode() + " but no location header");
                    }
                default:
                    throw new ESTException("Client does not handle http status code: " + eSTResponse.getStatusCode());
            }
        }
        if (eSTRequest != null) {
            eSTResponse.close();
        }
        return eSTRequest;
    }
}
