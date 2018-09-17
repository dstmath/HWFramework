package sun.security.provider.certpath;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.cert.CRLSelector;
import java.security.cert.CertSelector;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.CertStoreParameters;
import java.security.cert.CertStoreSpi;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import sun.security.action.GetIntegerAction;
import sun.security.util.Cache;
import sun.security.util.Debug;
import sun.security.x509.AccessDescription;
import sun.security.x509.GeneralNameInterface;
import sun.security.x509.URIName;

class URICertStore extends CertStoreSpi {
    private static final int CACHE_SIZE = 185;
    private static final int CHECK_INTERVAL = 30000;
    private static final int CRL_CONNECT_TIMEOUT = initializeTimeout();
    private static final int DEFAULT_CRL_CONNECT_TIMEOUT = 15000;
    private static final Cache<URICertStoreParameters, CertStore> certStoreCache = Cache.newSoftMemoryCache(CACHE_SIZE);
    private static final Debug debug = Debug.getInstance("certpath");
    private Collection<X509Certificate> certs = Collections.emptySet();
    private X509CRL crl;
    private final CertificateFactory factory;
    private long lastChecked;
    private long lastModified;
    private boolean ldap = false;
    private CertStore ldapCertStore;
    private CertStoreHelper ldapHelper;
    private String ldapPath;
    private URI uri;

    private static class UCS extends CertStore {
        protected UCS(CertStoreSpi spi, Provider p, String type, CertStoreParameters params) {
            super(spi, p, type, params);
        }
    }

    static class URICertStoreParameters implements CertStoreParameters {
        private volatile int hashCode = 0;
        private final URI uri;

        URICertStoreParameters(URI uri) {
            this.uri = uri;
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof URICertStoreParameters)) {
                return false;
            }
            return this.uri.equals(((URICertStoreParameters) obj).uri);
        }

        public int hashCode() {
            if (this.hashCode == 0) {
                this.hashCode = this.uri.hashCode() + 629;
            }
            return this.hashCode;
        }

        public Object clone() {
            try {
                return super.clone();
            } catch (CloneNotSupportedException e) {
                throw new InternalError(e.toString(), e);
            }
        }
    }

    private static int initializeTimeout() {
        Integer tmp = (Integer) AccessController.doPrivileged(new GetIntegerAction("com.sun.security.crl.timeout"));
        if (tmp == null || tmp.lambda$-java_util_stream_IntPipeline_14709() < 0) {
            return DEFAULT_CRL_CONNECT_TIMEOUT;
        }
        return tmp.lambda$-java_util_stream_IntPipeline_14709() * 1000;
    }

    URICertStore(CertStoreParameters params) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
        super(params);
        if (params instanceof URICertStoreParameters) {
            this.uri = ((URICertStoreParameters) params).uri;
            if (this.uri.getScheme().toLowerCase(Locale.ENGLISH).equals("ldap")) {
                this.ldap = true;
                this.ldapHelper = CertStoreHelper.getInstance("LDAP");
                this.ldapCertStore = this.ldapHelper.getCertStore(this.uri);
                this.ldapPath = this.uri.getPath();
                if (this.ldapPath.charAt(0) == '/') {
                    this.ldapPath = this.ldapPath.substring(1);
                }
            }
            try {
                this.factory = CertificateFactory.getInstance("X.509");
                return;
            } catch (CertificateException e) {
                throw new RuntimeException();
            }
        }
        throw new InvalidAlgorithmParameterException("params must be instanceof URICertStoreParameters");
    }

    static synchronized CertStore getInstance(URICertStoreParameters params) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        CertStore ucs;
        synchronized (URICertStore.class) {
            if (debug != null) {
                debug.println("CertStore URI:" + params.uri);
            }
            ucs = (CertStore) certStoreCache.get(params);
            if (ucs == null) {
                ucs = new UCS(new URICertStore(params), null, "URI", params);
                certStoreCache.put(params, ucs);
            } else if (debug != null) {
                debug.println("URICertStore.getInstance: cache hit");
            }
        }
        return ucs;
    }

    static CertStore getInstance(AccessDescription ad) {
        if (!ad.getAccessMethod().equals(AccessDescription.Ad_CAISSUERS_Id)) {
            return null;
        }
        GeneralNameInterface gn = ad.getAccessLocation().getName();
        if (!(gn instanceof URIName)) {
            return null;
        }
        try {
            return getInstance(new URICertStoreParameters(((URIName) gn).getURI()));
        } catch (Object ex) {
            if (debug != null) {
                debug.println("exception creating CertStore: " + ex);
                ex.printStackTrace();
            }
            return null;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:50:0x00b7 A:{Splitter: B:29:0x005c, ExcHandler: java.io.IOException (r3_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:50:0x00b7, code:
            r3 = move-exception;
     */
    /* JADX WARNING: Missing block: B:53:0x00ba, code:
            if (debug != null) goto L_0x00bc;
     */
    /* JADX WARNING: Missing block: B:54:0x00bc, code:
            debug.println("Exception fetching certificates:");
            r3.printStackTrace();
     */
    /* JADX WARNING: Missing block: B:55:0x00c7, code:
            r17.lastModified = 0;
            r17.certs = java.util.Collections.emptySet();
     */
    /* JADX WARNING: Missing block: B:57:0x00da, code:
            return r17.certs;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized Collection<X509Certificate> engineGetCertificates(CertSelector selector) throws CertStoreException {
        Throwable th;
        InputStream inputStream;
        Throwable th2;
        if (this.ldap) {
            X509CertSelector xsel = (X509CertSelector) selector;
            try {
                return this.ldapCertStore.getCertificates(this.ldapHelper.wrap(xsel, xsel.getSubject(), this.ldapPath));
            } catch (Throwable ioe) {
                throw new CertStoreException(ioe);
            }
        }
        long time = System.currentTimeMillis();
        if (time - this.lastChecked < 30000) {
            if (debug != null) {
                debug.println("Returning certificates from cache");
            }
            return getMatchingCerts(this.certs, selector);
        }
        this.lastChecked = time;
        try {
            URLConnection connection = this.uri.toURL().openConnection();
            if (this.lastModified != 0) {
                connection.setIfModifiedSince(this.lastModified);
            }
            long oldLastModified = this.lastModified;
            th = null;
            inputStream = null;
            try {
                inputStream = connection.getInputStream();
                this.lastModified = connection.getLastModified();
                if (oldLastModified != 0) {
                    Collection<X509Certificate> matchingCerts;
                    if (oldLastModified == this.lastModified) {
                        if (debug != null) {
                            debug.println("Not modified, using cached copy");
                        }
                        matchingCerts = getMatchingCerts(this.certs, selector);
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (Throwable th3) {
                                th = th3;
                            }
                        }
                        if (th == null) {
                            return matchingCerts;
                        }
                        throw th;
                    } else if ((connection instanceof HttpURLConnection) && ((HttpURLConnection) connection).getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED) {
                        if (debug != null) {
                            debug.println("Not modified, using cached copy");
                        }
                        matchingCerts = getMatchingCerts(this.certs, selector);
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (Throwable th4) {
                                th = th4;
                            }
                        }
                        if (th == null) {
                            return matchingCerts;
                        }
                        throw th;
                    }
                }
                if (debug != null) {
                    debug.println("Downloading new certificates...");
                }
                this.certs = this.factory.generateCertificates(inputStream);
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Throwable th5) {
                        th = th5;
                    }
                }
                if (th != null) {
                    throw th;
                } else {
                    return getMatchingCerts(this.certs, selector);
                }
            } catch (Throwable th6) {
                Throwable th7 = th6;
                th6 = th2;
                th2 = th7;
            }
        } catch (Exception e) {
        }
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (Throwable th8) {
                if (th6 == null) {
                    th6 = th8;
                } else if (th6 != th8) {
                    th6.addSuppressed(th8);
                }
            }
        }
        if (th6 != null) {
            throw th6;
        }
        throw th2;
    }

    private static Collection<X509Certificate> getMatchingCerts(Collection<X509Certificate> certs, CertSelector selector) {
        if (selector == null) {
            return certs;
        }
        List<X509Certificate> matchedCerts = new ArrayList(certs.size());
        for (X509Certificate cert : certs) {
            if (selector.match(cert)) {
                matchedCerts.-java_util_stream_Collectors-mthref-2(cert);
            }
        }
        return matchedCerts;
    }

    /*  JADX ERROR: JadxRuntimeException in pass: RegionMakerVisitor
        jadx.core.utils.exceptions.JadxRuntimeException: Exception block dominator not found, method:sun.security.provider.certpath.URICertStore.engineGetCRLs(java.security.cert.CRLSelector):java.util.Collection<java.security.cert.X509CRL>, dom blocks: [B:5:0x000c, B:7:0x0020, B:32:0x006e]
        	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.searchTryCatchDominators(ProcessTryCatchRegions.java:89)
        	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.process(ProcessTryCatchRegions.java:45)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.postProcessRegions(RegionMakerVisitor.java:63)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:58)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x00db A:{Splitter: B:32:0x006e, ExcHandler: java.io.IOException (r6_0 'e' java.lang.Throwable)} */
    public synchronized java.util.Collection<java.security.cert.X509CRL> engineGetCRLs(java.security.cert.CRLSelector r22) throws java.security.cert.CertStoreException {
        /*
        r21 = this;
        monitor-enter(r21);
        r0 = r21;	 Catch:{ all -> 0x0031 }
        r15 = r0.ldap;	 Catch:{ all -> 0x0031 }
        if (r15 == 0) goto L_0x0040;	 Catch:{ all -> 0x0031 }
    L_0x0007:
        r0 = r22;	 Catch:{ all -> 0x0031 }
        r0 = (java.security.cert.X509CRLSelector) r0;	 Catch:{ all -> 0x0031 }
        r14 = r0;	 Catch:{ all -> 0x0031 }
        r0 = r21;	 Catch:{ IOException -> 0x002a }
        r15 = r0.ldapHelper;	 Catch:{ IOException -> 0x002a }
        r0 = r21;	 Catch:{ IOException -> 0x002a }
        r0 = r0.ldapPath;	 Catch:{ IOException -> 0x002a }
        r16 = r0;	 Catch:{ IOException -> 0x002a }
        r17 = 0;	 Catch:{ IOException -> 0x002a }
        r0 = r17;	 Catch:{ IOException -> 0x002a }
        r1 = r16;	 Catch:{ IOException -> 0x002a }
        r14 = r15.wrap(r14, r0, r1);	 Catch:{ IOException -> 0x002a }
        r0 = r21;	 Catch:{ CertStoreException -> 0x0034 }
        r15 = r0.ldapCertStore;	 Catch:{ CertStoreException -> 0x0034 }
        r15 = r15.getCRLs(r14);	 Catch:{ CertStoreException -> 0x0034 }
        monitor-exit(r21);
        return r15;
    L_0x002a:
        r9 = move-exception;
        r15 = new java.security.cert.CertStoreException;	 Catch:{ all -> 0x0031 }
        r15.<init>(r9);	 Catch:{ all -> 0x0031 }
        throw r15;	 Catch:{ all -> 0x0031 }
    L_0x0031:
        r15 = move-exception;
        monitor-exit(r21);
        throw r15;
    L_0x0034:
        r5 = move-exception;
        r15 = new sun.security.provider.certpath.PKIX$CertStoreTypeException;	 Catch:{ all -> 0x0031 }
        r16 = "LDAP";	 Catch:{ all -> 0x0031 }
        r0 = r16;	 Catch:{ all -> 0x0031 }
        r15.<init>(r0, r5);	 Catch:{ all -> 0x0031 }
        throw r15;	 Catch:{ all -> 0x0031 }
    L_0x0040:
        r12 = java.lang.System.currentTimeMillis();	 Catch:{ all -> 0x0031 }
        r0 = r21;	 Catch:{ all -> 0x0031 }
        r0 = r0.lastChecked;	 Catch:{ all -> 0x0031 }
        r16 = r0;	 Catch:{ all -> 0x0031 }
        r16 = r12 - r16;	 Catch:{ all -> 0x0031 }
        r18 = 30000; // 0x7530 float:4.2039E-41 double:1.4822E-319;	 Catch:{ all -> 0x0031 }
        r15 = (r16 > r18 ? 1 : (r16 == r18 ? 0 : -1));	 Catch:{ all -> 0x0031 }
        if (r15 >= 0) goto L_0x006a;	 Catch:{ all -> 0x0031 }
    L_0x0052:
        r15 = debug;	 Catch:{ all -> 0x0031 }
        if (r15 == 0) goto L_0x005e;	 Catch:{ all -> 0x0031 }
    L_0x0056:
        r15 = debug;	 Catch:{ all -> 0x0031 }
        r16 = "Returning CRL from cache";	 Catch:{ all -> 0x0031 }
        r15.println(r16);	 Catch:{ all -> 0x0031 }
    L_0x005e:
        r0 = r21;	 Catch:{ all -> 0x0031 }
        r15 = r0.crl;	 Catch:{ all -> 0x0031 }
        r0 = r22;	 Catch:{ all -> 0x0031 }
        r15 = getMatchingCRLs(r15, r0);	 Catch:{ all -> 0x0031 }
        monitor-exit(r21);
        return r15;
    L_0x006a:
        r0 = r21;	 Catch:{ all -> 0x0031 }
        r0.lastChecked = r12;	 Catch:{ all -> 0x0031 }
        r0 = r21;	 Catch:{ IOException -> 0x00db, IOException -> 0x00db }
        r15 = r0.uri;	 Catch:{ IOException -> 0x00db, IOException -> 0x00db }
        r15 = r15.toURL();	 Catch:{ IOException -> 0x00db, IOException -> 0x00db }
        r4 = r15.openConnection();	 Catch:{ IOException -> 0x00db, IOException -> 0x00db }
        r0 = r21;	 Catch:{ IOException -> 0x00db, IOException -> 0x00db }
        r0 = r0.lastModified;	 Catch:{ IOException -> 0x00db, IOException -> 0x00db }
        r16 = r0;	 Catch:{ IOException -> 0x00db, IOException -> 0x00db }
        r18 = 0;	 Catch:{ IOException -> 0x00db, IOException -> 0x00db }
        r15 = (r16 > r18 ? 1 : (r16 == r18 ? 0 : -1));	 Catch:{ IOException -> 0x00db, IOException -> 0x00db }
        if (r15 == 0) goto L_0x0091;	 Catch:{ IOException -> 0x00db, IOException -> 0x00db }
    L_0x0086:
        r0 = r21;	 Catch:{ IOException -> 0x00db, IOException -> 0x00db }
        r0 = r0.lastModified;	 Catch:{ IOException -> 0x00db, IOException -> 0x00db }
        r16 = r0;	 Catch:{ IOException -> 0x00db, IOException -> 0x00db }
        r0 = r16;	 Catch:{ IOException -> 0x00db, IOException -> 0x00db }
        r4.setIfModifiedSince(r0);	 Catch:{ IOException -> 0x00db, IOException -> 0x00db }
    L_0x0091:
        r0 = r21;	 Catch:{ IOException -> 0x00db, IOException -> 0x00db }
        r10 = r0.lastModified;	 Catch:{ IOException -> 0x00db, IOException -> 0x00db }
        r15 = CRL_CONNECT_TIMEOUT;	 Catch:{ IOException -> 0x00db, IOException -> 0x00db }
        r4.setConnectTimeout(r15);	 Catch:{ IOException -> 0x00db, IOException -> 0x00db }
        r16 = 0;
        r8 = 0;
        r8 = r4.getInputStream();	 Catch:{ Throwable -> 0x0168, all -> 0x0196 }
        r18 = r4.getLastModified();	 Catch:{ Throwable -> 0x0168, all -> 0x0196 }
        r0 = r18;	 Catch:{ Throwable -> 0x0168, all -> 0x0196 }
        r2 = r21;	 Catch:{ Throwable -> 0x0168, all -> 0x0196 }
        r2.lastModified = r0;	 Catch:{ Throwable -> 0x0168, all -> 0x0196 }
        r18 = 0;	 Catch:{ Throwable -> 0x0168, all -> 0x0196 }
        r15 = (r10 > r18 ? 1 : (r10 == r18 ? 0 : -1));	 Catch:{ Throwable -> 0x0168, all -> 0x0196 }
        if (r15 == 0) goto L_0x0142;	 Catch:{ Throwable -> 0x0168, all -> 0x0196 }
    L_0x00b1:
        r0 = r21;	 Catch:{ Throwable -> 0x0168, all -> 0x0196 }
        r0 = r0.lastModified;	 Catch:{ Throwable -> 0x0168, all -> 0x0196 }
        r18 = r0;	 Catch:{ Throwable -> 0x0168, all -> 0x0196 }
        r15 = (r10 > r18 ? 1 : (r10 == r18 ? 0 : -1));	 Catch:{ Throwable -> 0x0168, all -> 0x0196 }
        if (r15 != 0) goto L_0x010c;	 Catch:{ Throwable -> 0x0168, all -> 0x0196 }
    L_0x00bb:
        r15 = debug;	 Catch:{ Throwable -> 0x0168, all -> 0x0196 }
        if (r15 == 0) goto L_0x00c9;	 Catch:{ Throwable -> 0x0168, all -> 0x0196 }
    L_0x00bf:
        r15 = debug;	 Catch:{ Throwable -> 0x0168, all -> 0x0196 }
        r17 = "Not modified, using cached copy";	 Catch:{ Throwable -> 0x0168, all -> 0x0196 }
        r0 = r17;	 Catch:{ Throwable -> 0x0168, all -> 0x0196 }
        r15.println(r0);	 Catch:{ Throwable -> 0x0168, all -> 0x0196 }
    L_0x00c9:
        r0 = r21;	 Catch:{ Throwable -> 0x0168, all -> 0x0196 }
        r15 = r0.crl;	 Catch:{ Throwable -> 0x0168, all -> 0x0196 }
        r0 = r22;	 Catch:{ Throwable -> 0x0168, all -> 0x0196 }
        r15 = getMatchingCRLs(r15, r0);	 Catch:{ Throwable -> 0x0168, all -> 0x0196 }
        if (r8 == 0) goto L_0x00d8;
    L_0x00d5:
        r8.close();	 Catch:{ Throwable -> 0x0108 }
    L_0x00d8:
        if (r16 == 0) goto L_0x010a;
    L_0x00da:
        throw r16;	 Catch:{ IOException -> 0x00db, IOException -> 0x00db }
    L_0x00db:
        r6 = move-exception;
        r15 = debug;	 Catch:{ all -> 0x0031 }
        if (r15 == 0) goto L_0x00eb;	 Catch:{ all -> 0x0031 }
    L_0x00e0:
        r15 = debug;	 Catch:{ all -> 0x0031 }
        r16 = "Exception fetching CRL:";	 Catch:{ all -> 0x0031 }
        r15.println(r16);	 Catch:{ all -> 0x0031 }
        r6.printStackTrace();	 Catch:{ all -> 0x0031 }
    L_0x00eb:
        r16 = 0;	 Catch:{ all -> 0x0031 }
        r0 = r16;	 Catch:{ all -> 0x0031 }
        r2 = r21;	 Catch:{ all -> 0x0031 }
        r2.lastModified = r0;	 Catch:{ all -> 0x0031 }
        r15 = 0;	 Catch:{ all -> 0x0031 }
        r0 = r21;	 Catch:{ all -> 0x0031 }
        r0.crl = r15;	 Catch:{ all -> 0x0031 }
        r15 = new sun.security.provider.certpath.PKIX$CertStoreTypeException;	 Catch:{ all -> 0x0031 }
        r16 = "URI";	 Catch:{ all -> 0x0031 }
        r17 = new java.security.cert.CertStoreException;	 Catch:{ all -> 0x0031 }
        r0 = r17;	 Catch:{ all -> 0x0031 }
        r0.<init>(r6);	 Catch:{ all -> 0x0031 }
        r15.<init>(r16, r17);	 Catch:{ all -> 0x0031 }
        throw r15;	 Catch:{ all -> 0x0031 }
    L_0x0108:
        r16 = move-exception;
        goto L_0x00d8;
    L_0x010a:
        monitor-exit(r21);
        return r15;
    L_0x010c:
        r15 = r4 instanceof java.net.HttpURLConnection;	 Catch:{ Throwable -> 0x0168, all -> 0x0196 }
        if (r15 == 0) goto L_0x0142;	 Catch:{ Throwable -> 0x0168, all -> 0x0196 }
    L_0x0110:
        r0 = r4;	 Catch:{ Throwable -> 0x0168, all -> 0x0196 }
        r0 = (java.net.HttpURLConnection) r0;	 Catch:{ Throwable -> 0x0168, all -> 0x0196 }
        r7 = r0;	 Catch:{ Throwable -> 0x0168, all -> 0x0196 }
        r15 = r7.getResponseCode();	 Catch:{ Throwable -> 0x0168, all -> 0x0196 }
        r17 = 304; // 0x130 float:4.26E-43 double:1.5E-321;	 Catch:{ Throwable -> 0x0168, all -> 0x0196 }
        r0 = r17;	 Catch:{ Throwable -> 0x0168, all -> 0x0196 }
        if (r15 != r0) goto L_0x0142;	 Catch:{ Throwable -> 0x0168, all -> 0x0196 }
    L_0x011e:
        r15 = debug;	 Catch:{ Throwable -> 0x0168, all -> 0x0196 }
        if (r15 == 0) goto L_0x012c;	 Catch:{ Throwable -> 0x0168, all -> 0x0196 }
    L_0x0122:
        r15 = debug;	 Catch:{ Throwable -> 0x0168, all -> 0x0196 }
        r17 = "Not modified, using cached copy";	 Catch:{ Throwable -> 0x0168, all -> 0x0196 }
        r0 = r17;	 Catch:{ Throwable -> 0x0168, all -> 0x0196 }
        r15.println(r0);	 Catch:{ Throwable -> 0x0168, all -> 0x0196 }
    L_0x012c:
        r0 = r21;	 Catch:{ Throwable -> 0x0168, all -> 0x0196 }
        r15 = r0.crl;	 Catch:{ Throwable -> 0x0168, all -> 0x0196 }
        r0 = r22;	 Catch:{ Throwable -> 0x0168, all -> 0x0196 }
        r15 = getMatchingCRLs(r15, r0);	 Catch:{ Throwable -> 0x0168, all -> 0x0196 }
        if (r8 == 0) goto L_0x013b;
    L_0x0138:
        r8.close();	 Catch:{ Throwable -> 0x013e }
    L_0x013b:
        if (r16 == 0) goto L_0x0140;
    L_0x013d:
        throw r16;	 Catch:{ IOException -> 0x00db, IOException -> 0x00db }
    L_0x013e:
        r16 = move-exception;
        goto L_0x013b;
    L_0x0140:
        monitor-exit(r21);
        return r15;
    L_0x0142:
        r15 = debug;	 Catch:{ Throwable -> 0x0168, all -> 0x0196 }
        if (r15 == 0) goto L_0x0150;	 Catch:{ Throwable -> 0x0168, all -> 0x0196 }
    L_0x0146:
        r15 = debug;	 Catch:{ Throwable -> 0x0168, all -> 0x0196 }
        r17 = "Downloading new CRL...";	 Catch:{ Throwable -> 0x0168, all -> 0x0196 }
        r0 = r17;	 Catch:{ Throwable -> 0x0168, all -> 0x0196 }
        r15.println(r0);	 Catch:{ Throwable -> 0x0168, all -> 0x0196 }
    L_0x0150:
        r0 = r21;	 Catch:{ Throwable -> 0x0168, all -> 0x0196 }
        r15 = r0.factory;	 Catch:{ Throwable -> 0x0168, all -> 0x0196 }
        r15 = r15.generateCRL(r8);	 Catch:{ Throwable -> 0x0168, all -> 0x0196 }
        r15 = (java.security.cert.X509CRL) r15;	 Catch:{ Throwable -> 0x0168, all -> 0x0196 }
        r0 = r21;	 Catch:{ Throwable -> 0x0168, all -> 0x0196 }
        r0.crl = r15;	 Catch:{ Throwable -> 0x0168, all -> 0x0196 }
        if (r8 == 0) goto L_0x0163;
    L_0x0160:
        r8.close();	 Catch:{ Throwable -> 0x0166 }
    L_0x0163:
        if (r16 == 0) goto L_0x018a;
    L_0x0165:
        throw r16;	 Catch:{ IOException -> 0x00db, IOException -> 0x00db }
    L_0x0166:
        r16 = move-exception;
        goto L_0x0163;
    L_0x0168:
        r15 = move-exception;
        throw r15;	 Catch:{ all -> 0x016a }
    L_0x016a:
        r16 = move-exception;
        r20 = r16;
        r16 = r15;
        r15 = r20;
    L_0x0171:
        if (r8 == 0) goto L_0x0176;
    L_0x0173:
        r8.close();	 Catch:{ Throwable -> 0x0179 }
    L_0x0176:
        if (r16 == 0) goto L_0x0189;
    L_0x0178:
        throw r16;	 Catch:{ IOException -> 0x00db, IOException -> 0x00db }
    L_0x0179:
        r17 = move-exception;	 Catch:{ IOException -> 0x00db, IOException -> 0x00db }
        if (r16 != 0) goto L_0x017f;	 Catch:{ IOException -> 0x00db, IOException -> 0x00db }
    L_0x017c:
        r16 = r17;	 Catch:{ IOException -> 0x00db, IOException -> 0x00db }
        goto L_0x0176;	 Catch:{ IOException -> 0x00db, IOException -> 0x00db }
    L_0x017f:
        r0 = r16;	 Catch:{ IOException -> 0x00db, IOException -> 0x00db }
        r1 = r17;	 Catch:{ IOException -> 0x00db, IOException -> 0x00db }
        if (r0 == r1) goto L_0x0176;	 Catch:{ IOException -> 0x00db, IOException -> 0x00db }
    L_0x0185:
        r16.addSuppressed(r17);	 Catch:{ IOException -> 0x00db, IOException -> 0x00db }
        goto L_0x0176;	 Catch:{ IOException -> 0x00db, IOException -> 0x00db }
    L_0x0189:
        throw r15;	 Catch:{ IOException -> 0x00db, IOException -> 0x00db }
    L_0x018a:
        r0 = r21;	 Catch:{ IOException -> 0x00db, IOException -> 0x00db }
        r15 = r0.crl;	 Catch:{ IOException -> 0x00db, IOException -> 0x00db }
        r0 = r22;	 Catch:{ IOException -> 0x00db, IOException -> 0x00db }
        r15 = getMatchingCRLs(r15, r0);	 Catch:{ IOException -> 0x00db, IOException -> 0x00db }
        monitor-exit(r21);
        return r15;
    L_0x0196:
        r15 = move-exception;
        goto L_0x0171;
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.security.provider.certpath.URICertStore.engineGetCRLs(java.security.cert.CRLSelector):java.util.Collection<java.security.cert.X509CRL>");
    }

    private static Collection<X509CRL> getMatchingCRLs(X509CRL crl, CRLSelector selector) {
        if (selector == null || (crl != null && selector.match(crl))) {
            return Collections.singletonList(crl);
        }
        return Collections.emptyList();
    }
}
