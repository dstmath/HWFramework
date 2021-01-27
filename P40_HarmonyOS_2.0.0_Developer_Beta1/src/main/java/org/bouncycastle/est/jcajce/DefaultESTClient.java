package org.bouncycastle.est.jcajce;

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

    /* access modifiers changed from: private */
    public class PrintingOutputStream extends OutputStream {
        private final OutputStream tgt;

        public PrintingOutputStream(OutputStream outputStream) {
            this.tgt = outputStream;
        }

        @Override // java.io.OutputStream
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

    @Override // org.bouncycastle.est.ESTClient
    public ESTResponse doRequest(ESTRequest eSTRequest) throws IOException {
        ESTResponse performRequest;
        int i = 15;
        while (true) {
            performRequest = performRequest(eSTRequest);
            ESTRequest redirectURL = redirectURL(performRequest);
            if (redirectURL == null || i - 1 <= 0) {
                break;
            }
            eSTRequest = redirectURL;
        }
        if (i != 0) {
            return performRequest;
        }
        throw new ESTException("Too many redirects..");
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x0066 A[Catch:{ all -> 0x0151 }] */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x006f A[Catch:{ all -> 0x0151 }] */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0087 A[Catch:{ all -> 0x0151 }] */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x009a  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x00b8  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x00f3  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x013b  */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x014b  */
    public ESTResponse performRequest(ESTRequest eSTRequest) throws IOException {
        OutputStream outputStream;
        ESTRequest build;
        Source source = null;
        try {
            Source makeSource = this.sslSocketProvider.makeSource(eSTRequest.getURL().getHost(), eSTRequest.getURL().getPort());
            if (eSTRequest.getListener() != null) {
                eSTRequest = eSTRequest.getListener().onConnection(makeSource, eSTRequest);
            }
            Set<String> asKeySet = Properties.asKeySet("org.bouncycastle.debug.est");
            if (!asKeySet.contains("output")) {
                if (!asKeySet.contains("all")) {
                    outputStream = makeSource.getOutputStream();
                    StringBuilder sb = new StringBuilder();
                    sb.append(eSTRequest.getURL().getPath());
                    sb.append(eSTRequest.getURL().getQuery() == null ? eSTRequest.getURL().getQuery() : "");
                    String sb2 = sb.toString();
                    ESTRequestBuilder eSTRequestBuilder = new ESTRequestBuilder(eSTRequest);
                    if (!eSTRequest.getHeaders().containsKey("Connection")) {
                        eSTRequestBuilder.addHeader("Connection", "close");
                    }
                    URL url = eSTRequest.getURL();
                    eSTRequestBuilder.setHeader("Host", url.getPort() <= -1 ? String.format("%s:%d", url.getHost(), Integer.valueOf(url.getPort())) : url.getHost());
                    build = eSTRequestBuilder.build();
                    writeLine(outputStream, build.getMethod() + " " + sb2 + " HTTP/1.1");
                    for (Map.Entry<String, String[]> entry : build.getHeaders().entrySet()) {
                        String[] value = entry.getValue();
                        for (int i = 0; i != value.length; i++) {
                            writeLine(outputStream, entry.getKey() + ": " + value[i]);
                        }
                    }
                    outputStream.write(CRLF);
                    outputStream.flush();
                    build.writeData(outputStream);
                    outputStream.flush();
                    if (build.getHijacker() != null) {
                        return new ESTResponse(build, makeSource);
                    }
                    ESTResponse hijack = build.getHijacker().hijack(build, makeSource);
                    if (makeSource != null && hijack == null) {
                        makeSource.close();
                    }
                    return hijack;
                }
            }
            outputStream = new PrintingOutputStream(makeSource.getOutputStream());
            StringBuilder sb3 = new StringBuilder();
            sb3.append(eSTRequest.getURL().getPath());
            sb3.append(eSTRequest.getURL().getQuery() == null ? eSTRequest.getURL().getQuery() : "");
            String sb22 = sb3.toString();
            ESTRequestBuilder eSTRequestBuilder2 = new ESTRequestBuilder(eSTRequest);
            if (!eSTRequest.getHeaders().containsKey("Connection")) {
            }
            URL url2 = eSTRequest.getURL();
            eSTRequestBuilder2.setHeader("Host", url2.getPort() <= -1 ? String.format("%s:%d", url2.getHost(), Integer.valueOf(url2.getPort())) : url2.getHost());
            build = eSTRequestBuilder2.build();
            writeLine(outputStream, build.getMethod() + " " + sb22 + " HTTP/1.1");
            while (r0.hasNext()) {
            }
            outputStream.write(CRLF);
            outputStream.flush();
            build.writeData(outputStream);
            outputStream.flush();
            if (build.getHijacker() != null) {
            }
        } catch (Throwable th) {
            if (0 != 0) {
                source.close();
            }
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    public ESTRequest redirectURL(ESTResponse eSTResponse) throws IOException {
        ESTRequest eSTRequest;
        ESTRequestBuilder eSTRequestBuilder;
        if (eSTResponse.getStatusCode() < 300 || eSTResponse.getStatusCode() > 399) {
            eSTRequest = null;
        } else {
            switch (eSTResponse.getStatusCode()) {
                case 304:
                case 305:
                default:
                    throw new ESTException("Client does not handle http status code: " + eSTResponse.getStatusCode());
                case 301:
                case 302:
                case 303:
                case 306:
                case 307:
                    String header = eSTResponse.getHeader("Location");
                    if (!"".equals(header)) {
                        ESTRequestBuilder eSTRequestBuilder2 = new ESTRequestBuilder(eSTResponse.getOriginalRequest());
                        if (header.startsWith("http")) {
                            eSTRequestBuilder = eSTRequestBuilder2.withURL(new URL(header));
                        } else {
                            URL url = eSTResponse.getOriginalRequest().getURL();
                            eSTRequestBuilder = eSTRequestBuilder2.withURL(new URL(url.getProtocol(), url.getHost(), url.getPort(), header));
                        }
                        eSTRequest = eSTRequestBuilder.build();
                        break;
                    } else {
                        throw new ESTException("Redirect status type: " + eSTResponse.getStatusCode() + " but no location header");
                    }
            }
        }
        if (eSTRequest != null) {
            eSTResponse.close();
        }
        return eSTRequest;
    }
}
