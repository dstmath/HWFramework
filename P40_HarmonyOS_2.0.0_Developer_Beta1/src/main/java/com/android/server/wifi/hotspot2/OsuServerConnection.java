package com.android.server.wifi.hotspot2;

import android.net.Network;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;
import com.android.internal.annotations.VisibleForTesting;
import com.android.org.conscrypt.TrustManagerImpl;
import com.android.server.wifi.hotspot2.PasspointProvisioner;
import com.android.server.wifi.hotspot2.soap.HttpsServiceConnection;
import com.android.server.wifi.hotspot2.soap.HttpsTransport;
import com.android.server.wifi.hotspot2.soap.SoapParser;
import com.android.server.wifi.hotspot2.soap.SppResponseMessage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.ksoap2.HeaderProperty;
import org.ksoap2.serialization.AttributeInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;

public class OsuServerConnection {
    private static final int DNS_NAME = 2;
    private static final String TAG = "PasspointOsuServerConnection";
    public static final int TRUST_CERT_TYPE_AAA = 1;
    public static final int TRUST_CERT_TYPE_POLICY = 3;
    public static final int TRUST_CERT_TYPE_REMEDIATION = 2;
    private Handler mHandler;
    private HttpsTransport mHttpsTransport;
    private Looper mLooper;
    private Network mNetwork;
    private PasspointProvisioner.OsuServerCallbacks mOsuServerCallbacks;
    private HandlerThread mOsuServerHandlerThread;
    private HttpsServiceConnection mServiceConnection = null;
    private boolean mSetupComplete = false;
    private SSLSocketFactory mSocketFactory;
    private WFATrustManager mTrustManager;
    private URL mUrl;
    private HttpsURLConnection mUrlConnection = null;
    private boolean mVerboseLoggingEnabled = false;

    @VisibleForTesting
    OsuServerConnection(Looper looper) {
        this.mLooper = looper;
    }

    public void setEventCallback(PasspointProvisioner.OsuServerCallbacks callbacks) {
        this.mOsuServerCallbacks = callbacks;
    }

    public void init(SSLContext tlsContext, TrustManagerImpl trustManagerImpl) {
        if (tlsContext != null) {
            try {
                this.mTrustManager = new WFATrustManager(trustManagerImpl);
                tlsContext.init(null, new TrustManager[]{this.mTrustManager}, null);
                this.mSocketFactory = tlsContext.getSocketFactory();
                this.mSetupComplete = true;
                if (this.mLooper == null) {
                    this.mOsuServerHandlerThread = new HandlerThread("OsuServerHandler");
                    this.mOsuServerHandlerThread.start();
                    this.mLooper = this.mOsuServerHandlerThread.getLooper();
                }
                this.mHandler = new Handler(this.mLooper);
            } catch (KeyManagementException e) {
                Log.w(TAG, "Initialization failed");
                e.printStackTrace();
            }
        }
    }

    public boolean canValidateServer() {
        return this.mSetupComplete;
    }

    public void enableVerboseLogging(int verbose) {
        this.mVerboseLoggingEnabled = verbose > 0;
    }

    public boolean connect(URL url, Network network) {
        if (url == null) {
            Log.e(TAG, "url is null");
            return false;
        } else if (network == null) {
            Log.e(TAG, "network is null");
            return false;
        } else {
            this.mHandler.post(new Runnable(url, network) {
                /* class com.android.server.wifi.hotspot2.$$Lambda$OsuServerConnection$ucnAsKwpZST3v0eT9hb0Vxt7p7k */
                private final /* synthetic */ URL f$1;
                private final /* synthetic */ Network f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    OsuServerConnection.this.lambda$connect$0$OsuServerConnection(this.f$1, this.f$2);
                }
            });
            return true;
        }
    }

    public boolean validateProvider(Locale locale, String friendlyName) {
        if (locale == null || TextUtils.isEmpty(friendlyName)) {
            return false;
        }
        for (Pair<Locale, String> identity : ServiceProviderVerifier.getProviderNames(this.mTrustManager.getProviderCert())) {
            if (identity.first != null && ((Locale) identity.first).getISO3Language().equals(locale.getISO3Language()) && TextUtils.equals((CharSequence) identity.second, friendlyName)) {
                if (!this.mVerboseLoggingEnabled) {
                    return true;
                }
                Log.v(TAG, "OSU certificate is valid for " + ((Locale) identity.first).getISO3Language() + "/" + ((String) identity.second));
                return true;
            }
        }
        return false;
    }

    public boolean exchangeSoapMessage(SoapSerializationEnvelope soapEnvelope) {
        if (this.mNetwork == null) {
            Log.e(TAG, "Network is not established");
            return false;
        } else if (this.mUrlConnection == null) {
            Log.e(TAG, "Server certificate is not validated");
            return false;
        } else if (soapEnvelope == null) {
            Log.e(TAG, "soapEnvelope is null");
            return false;
        } else {
            this.mHandler.post(new Runnable(soapEnvelope) {
                /* class com.android.server.wifi.hotspot2.$$Lambda$OsuServerConnection$LkMDZKBOhz834tKWWGvJllE8ko */
                private final /* synthetic */ SoapSerializationEnvelope f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    OsuServerConnection.this.lambda$exchangeSoapMessage$1$OsuServerConnection(this.f$1);
                }
            });
            return true;
        }
    }

    public boolean retrieveTrustRootCerts(Map<Integer, Map<String, byte[]>> trustCertsInfo) {
        if (this.mNetwork == null) {
            Log.e(TAG, "Network is not established");
            return false;
        } else if (this.mUrlConnection == null) {
            Log.e(TAG, "Server certificate is not validated");
            return false;
        } else if (trustCertsInfo == null || trustCertsInfo.isEmpty()) {
            Log.e(TAG, "TrustCertsInfo is not valid");
            return false;
        } else {
            this.mHandler.post(new Runnable(trustCertsInfo) {
                /* class com.android.server.wifi.hotspot2.$$Lambda$OsuServerConnection$JzmRmwm3PG3iV4rt_hNbqPSP5Uw */
                private final /* synthetic */ Map f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    OsuServerConnection.this.lambda$retrieveTrustRootCerts$2$OsuServerConnection(this.f$1);
                }
            });
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: performTlsConnection */
    public void lambda$connect$0$OsuServerConnection(URL url, Network network) {
        this.mNetwork = network;
        this.mUrl = url;
        try {
            HttpsURLConnection urlConnection = (HttpsURLConnection) this.mNetwork.openConnection(this.mUrl);
            urlConnection.setSSLSocketFactory(this.mSocketFactory);
            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(5000);
            urlConnection.connect();
            this.mUrlConnection = urlConnection;
            PasspointProvisioner.OsuServerCallbacks osuServerCallbacks = this.mOsuServerCallbacks;
            if (osuServerCallbacks != null) {
                osuServerCallbacks.onServerConnectionStatus(osuServerCallbacks.getSessionId(), true);
            }
        } catch (IOException e) {
            Log.e(TAG, "Unable to establish a URL connection: " + e);
            PasspointProvisioner.OsuServerCallbacks osuServerCallbacks2 = this.mOsuServerCallbacks;
            if (osuServerCallbacks2 != null) {
                osuServerCallbacks2.onServerConnectionStatus(osuServerCallbacks2.getSessionId(), false);
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: performSoapMessageExchange */
    public void lambda$exchangeSoapMessage$1$OsuServerConnection(SoapSerializationEnvelope soapEnvelope) {
        HttpsServiceConnection httpsServiceConnection = this.mServiceConnection;
        if (httpsServiceConnection != null) {
            httpsServiceConnection.disconnect();
        }
        this.mServiceConnection = getServiceConnection(this.mUrl, this.mNetwork);
        if (this.mServiceConnection == null) {
            Log.e(TAG, "ServiceConnection for https is null");
            PasspointProvisioner.OsuServerCallbacks osuServerCallbacks = this.mOsuServerCallbacks;
            if (osuServerCallbacks != null) {
                osuServerCallbacks.onReceivedSoapMessage(osuServerCallbacks.getSessionId(), null);
                return;
            }
            return;
        }
        try {
            this.mHttpsTransport.call("", soapEnvelope);
            Object response = soapEnvelope.bodyIn;
            if (response == null) {
                Log.e(TAG, "SoapObject is null");
                if (this.mOsuServerCallbacks != null) {
                    this.mOsuServerCallbacks.onReceivedSoapMessage(this.mOsuServerCallbacks.getSessionId(), null);
                }
            } else if (!(response instanceof SoapObject)) {
                Log.e(TAG, "Not a SoapObject instance");
                if (this.mOsuServerCallbacks != null) {
                    this.mOsuServerCallbacks.onReceivedSoapMessage(this.mOsuServerCallbacks.getSessionId(), null);
                }
                this.mServiceConnection.disconnect();
                this.mServiceConnection = null;
            } else {
                SoapObject soapResponse = (SoapObject) response;
                if (this.mVerboseLoggingEnabled) {
                    for (int i = 0; i < soapResponse.getAttributeCount(); i++) {
                        AttributeInfo attributeInfo = new AttributeInfo();
                        soapResponse.getAttributeInfo(i, attributeInfo);
                        Log.v(TAG, "Attribute : " + attributeInfo.toString());
                    }
                    Log.v(TAG, "response : " + soapResponse.toString());
                }
                SppResponseMessage sppResponse = SoapParser.getResponse(soapResponse);
                this.mServiceConnection.disconnect();
                this.mServiceConnection = null;
                PasspointProvisioner.OsuServerCallbacks osuServerCallbacks2 = this.mOsuServerCallbacks;
                if (osuServerCallbacks2 != null) {
                    osuServerCallbacks2.onReceivedSoapMessage(osuServerCallbacks2.getSessionId(), sppResponse);
                }
            }
        } catch (Exception e) {
            if (e instanceof SSLHandshakeException) {
                Log.e(TAG, "Failed to make TLS connection");
            } else {
                Log.e(TAG, "Failed to exchange the SOAP message");
            }
            if (this.mOsuServerCallbacks != null) {
                this.mOsuServerCallbacks.onReceivedSoapMessage(this.mOsuServerCallbacks.getSessionId(), null);
            }
        } finally {
            this.mServiceConnection.disconnect();
            this.mServiceConnection = null;
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x005f, code lost:
        r0.clear();
     */
    /* renamed from: performRetrievingTrustRootCerts */
    public void lambda$retrieveTrustRootCerts$2$OsuServerConnection(Map<Integer, Map<String, byte[]>> trustCertsInfo) {
        Map<Integer, List<X509Certificate>> trustRootCertificates = new HashMap<>();
        for (Map.Entry<Integer, Map<String, byte[]>> certInfoPerType : trustCertsInfo.entrySet()) {
            List<X509Certificate> certificates = new ArrayList<>();
            Iterator<Map.Entry<String, byte[]>> it = certInfoPerType.getValue().entrySet().iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                Map.Entry<String, byte[]> certInfo = it.next();
                if (certInfo.getValue() == null) {
                    trustRootCertificates.clear();
                    break;
                }
                X509Certificate certificate = getCert(certInfo.getKey());
                if (certificate == null || !ServiceProviderVerifier.verifyCertFingerprint(certificate, certInfo.getValue())) {
                    break;
                }
                certificates.add(certificate);
            }
            if (!certificates.isEmpty()) {
                trustRootCertificates.put(certInfoPerType.getKey(), certificates);
            }
        }
        PasspointProvisioner.OsuServerCallbacks osuServerCallbacks = this.mOsuServerCallbacks;
        if (osuServerCallbacks != null) {
            osuServerCallbacks.onReceivedTrustRootCertificates(osuServerCallbacks.getSessionId(), trustRootCertificates);
        }
    }

    private X509Certificate getCert(String certUrl) {
        if (certUrl == null || !certUrl.toLowerCase(Locale.US).startsWith("https://")) {
            Log.e(TAG, "invalid certUrl provided");
            return null;
        }
        try {
            URL serverUrl = new URL(certUrl);
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            if (this.mServiceConnection != null) {
                this.mServiceConnection.disconnect();
            }
            this.mServiceConnection = getServiceConnection(serverUrl, this.mNetwork);
            if (this.mServiceConnection == null) {
                this.mServiceConnection.disconnect();
                this.mServiceConnection = null;
                return null;
            }
            this.mServiceConnection.setRequestMethod("GET");
            this.mServiceConnection.setRequestProperty("Accept-Encoding", "gzip");
            if (this.mServiceConnection.getResponseCode() != 200) {
                Log.e(TAG, "The response code of the HTTPS GET to " + certUrl + " is not OK, but " + this.mServiceConnection.getResponseCode());
                this.mServiceConnection.disconnect();
                this.mServiceConnection = null;
                return null;
            }
            boolean bPkcs7 = false;
            boolean bBase64 = false;
            for (HeaderProperty property : this.mServiceConnection.getResponseProperties()) {
                if (!(property == null || property.getKey() == null || property.getValue() == null)) {
                    if (property.getKey().equalsIgnoreCase("Content-Type") && (property.getValue().equals("application/pkcs7-mime") || property.getValue().equals("application/x-x509-ca-cert"))) {
                        if (this.mVerboseLoggingEnabled) {
                            Log.v(TAG, "a certificate found in a HTTPS response from " + certUrl);
                        }
                        bPkcs7 = true;
                    }
                    if (property.getKey().equalsIgnoreCase("Content-Transfer-Encoding") && property.getValue().equalsIgnoreCase("base64")) {
                        if (this.mVerboseLoggingEnabled) {
                            Log.v(TAG, "base64 encoding content in a HTTP response from " + certUrl);
                        }
                        bBase64 = true;
                    }
                }
            }
            if (!bPkcs7) {
                Log.e(TAG, "no X509Certificate found in the HTTPS response");
                this.mServiceConnection.disconnect();
                this.mServiceConnection = null;
                return null;
            }
            InputStream in = this.mServiceConnection.openInputStream();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            while (true) {
                int rd = in.read(buf, 0, 8192);
                if (rd == -1) {
                    break;
                }
                bos.write(buf, 0, rd);
            }
            in.close();
            bos.flush();
            byte[] byteArray = bos.toByteArray();
            if (bBase64) {
                byteArray = Base64.decode(new String(byteArray), 0);
            }
            X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(byteArray));
            if (this.mVerboseLoggingEnabled) {
                Log.v(TAG, "cert : " + certificate.getSubjectDN());
            }
            this.mServiceConnection.disconnect();
            this.mServiceConnection = null;
            return certificate;
        } catch (IOException e) {
            Log.e(TAG, "Failed to get the data from " + certUrl + ": " + e);
        } catch (CertificateException e2) {
            Log.e(TAG, "Failed to get instance for CertificateFactory " + e2);
        } catch (IllegalArgumentException e3) {
            Log.e(TAG, "Failed to decode the data: " + e3);
        } catch (Throwable th) {
            this.mServiceConnection.disconnect();
            this.mServiceConnection = null;
            throw th;
        }
        this.mServiceConnection.disconnect();
        this.mServiceConnection = null;
        return null;
    }

    private HttpsServiceConnection getServiceConnection(URL url, Network network) {
        try {
            this.mHttpsTransport = HttpsTransport.createInstance(network, url);
            HttpsServiceConnection serviceConnection = (HttpsServiceConnection) this.mHttpsTransport.getServiceConnection();
            if (serviceConnection != null) {
                serviceConnection.setSSLSocketFactory(this.mSocketFactory);
            }
            return serviceConnection;
        } catch (IOException e) {
            Log.e(TAG, "Unable to establish a URL connection");
            return null;
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: cleanupConnection */
    public void lambda$cleanup$3$OsuServerConnection() {
        HttpsURLConnection httpsURLConnection = this.mUrlConnection;
        if (httpsURLConnection != null) {
            httpsURLConnection.disconnect();
            this.mUrlConnection = null;
        }
        HttpsServiceConnection httpsServiceConnection = this.mServiceConnection;
        if (httpsServiceConnection != null) {
            httpsServiceConnection.disconnect();
            this.mServiceConnection = null;
        }
    }

    public void cleanup() {
        this.mHandler.post(new Runnable() {
            /* class com.android.server.wifi.hotspot2.$$Lambda$OsuServerConnection$nvsCMt39WQHFfviXbcA6LwKj0o */

            @Override // java.lang.Runnable
            public final void run() {
                OsuServerConnection.this.lambda$cleanup$3$OsuServerConnection();
            }
        });
    }

    /* access modifiers changed from: private */
    public class WFATrustManager implements X509TrustManager {
        private TrustManagerImpl mDelegate;
        private List<X509Certificate> mServerCerts;

        WFATrustManager(TrustManagerImpl trustManagerImpl) {
            this.mDelegate = trustManagerImpl;
        }

        @Override // javax.net.ssl.X509TrustManager
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            if (OsuServerConnection.this.mVerboseLoggingEnabled) {
                Log.v(OsuServerConnection.TAG, "checkClientTrusted " + authType);
            }
        }

        @Override // javax.net.ssl.X509TrustManager
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            if (OsuServerConnection.this.mVerboseLoggingEnabled) {
                Log.v(OsuServerConnection.TAG, "checkServerTrusted " + authType);
            }
            boolean certsValid = false;
            try {
                this.mServerCerts = this.mDelegate.getTrustedChainForServer(chain, authType, (Socket) null);
                certsValid = true;
            } catch (CertificateException e) {
                Log.e(OsuServerConnection.TAG, "Unable to validate certs " + e);
                if (OsuServerConnection.this.mVerboseLoggingEnabled) {
                    e.printStackTrace();
                }
            }
            if (OsuServerConnection.this.mOsuServerCallbacks != null) {
                OsuServerConnection.this.mOsuServerCallbacks.onServerValidationStatus(OsuServerConnection.this.mOsuServerCallbacks.getSessionId(), certsValid);
            }
        }

        @Override // javax.net.ssl.X509TrustManager
        public X509Certificate[] getAcceptedIssuers() {
            if (!OsuServerConnection.this.mVerboseLoggingEnabled) {
                return null;
            }
            Log.v(OsuServerConnection.TAG, "getAcceptedIssuers ");
            return null;
        }

        public X509Certificate getProviderCert() {
            List<X509Certificate> list = this.mServerCerts;
            if (list == null || list.size() <= 0) {
                return null;
            }
            X509Certificate providerCert = null;
            String fqdn = OsuServerConnection.this.mUrl.getHost();
            try {
                for (X509Certificate certificate : this.mServerCerts) {
                    Collection<List<?>> col = certificate.getSubjectAlternativeNames();
                    if (col != null) {
                        Iterator<List<?>> it = col.iterator();
                        while (true) {
                            if (!it.hasNext()) {
                                break;
                            }
                            List<?> name = it.next();
                            if (name != null) {
                                if (name.size() >= 2 && name.get(0).getClass() == Integer.class && name.get(1).toString().equals(fqdn)) {
                                    providerCert = certificate;
                                    if (OsuServerConnection.this.mVerboseLoggingEnabled) {
                                        Log.v(OsuServerConnection.TAG, "OsuCert found");
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (CertificateParsingException e) {
                Log.e(OsuServerConnection.TAG, "Unable to match certificate to " + fqdn);
                if (OsuServerConnection.this.mVerboseLoggingEnabled) {
                    e.printStackTrace();
                }
            }
            return providerCert;
        }
    }
}
