package com.android.org.conscrypt;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.security.auth.x500.X500Principal;
import libcore.io.IoUtils;
import org.conscrypt.OpenSSLX509Certificate;

public class TrustedCertificateStore {
    private static final CertificateFactory CERT_FACTORY;
    private static final String PREFIX_SYSTEM = "system:";
    private static final String PREFIX_USER = "user:";
    private final File addedDir;
    private final File deletedDir;
    private final File systemDir;

    private interface CertSelector {
        boolean match(X509Certificate x509Certificate);
    }

    private static class PreloadHolder {
        private static File defaultCaCertsAddedDir;
        private static File defaultCaCertsDeletedDir;
        private static File defaultCaCertsSystemDir;

        private PreloadHolder() {
        }

        static {
            String ANDROID_ROOT = System.getenv("ANDROID_ROOT");
            String ANDROID_DATA = System.getenv("ANDROID_DATA");
            defaultCaCertsSystemDir = new File(ANDROID_ROOT + "/etc/security/cacerts");
            TrustedCertificateStore.setDefaultUserDirectory(new File(ANDROID_DATA + "/misc/keychain"));
        }
    }

    public static final boolean isSystem(String alias) {
        return alias.startsWith(PREFIX_SYSTEM);
    }

    public static final boolean isUser(String alias) {
        return alias.startsWith(PREFIX_USER);
    }

    static {
        try {
            CERT_FACTORY = CertificateFactory.getInstance("X509");
        } catch (CertificateException e) {
            throw new AssertionError(e);
        }
    }

    public static void setDefaultUserDirectory(File root) {
        PreloadHolder.defaultCaCertsAddedDir = new File(root, "cacerts-added");
        PreloadHolder.defaultCaCertsDeletedDir = new File(root, "cacerts-removed");
    }

    public TrustedCertificateStore() {
        this(PreloadHolder.defaultCaCertsSystemDir, PreloadHolder.defaultCaCertsAddedDir, PreloadHolder.defaultCaCertsDeletedDir);
    }

    public TrustedCertificateStore(File systemDir, File addedDir, File deletedDir) {
        this.systemDir = systemDir;
        this.addedDir = addedDir;
        this.deletedDir = deletedDir;
    }

    public Certificate getCertificate(String alias) {
        return getCertificate(alias, false);
    }

    public Certificate getCertificate(String alias, boolean includeDeletedSystem) {
        File file = fileForAlias(alias);
        if (file == null || (isUser(alias) && isTombstone(file))) {
            return null;
        }
        X509Certificate cert = readCertificate(file);
        if (cert == null || (isSystem(alias) && (includeDeletedSystem ^ 1) != 0 && isDeletedSystemCertificate(cert))) {
            return null;
        }
        return cert;
    }

    private File fileForAlias(String alias) {
        if (alias == null) {
            throw new NullPointerException("alias == null");
        }
        File file;
        if (isSystem(alias)) {
            file = new File(this.systemDir, alias.substring(PREFIX_SYSTEM.length()));
        } else if (!isUser(alias)) {
            return null;
        } else {
            file = new File(this.addedDir, alias.substring(PREFIX_USER.length()));
        }
        if (!file.exists() || isTombstone(file)) {
            return null;
        }
        return file;
    }

    private boolean isTombstone(File file) {
        return file.length() == 0;
    }

    private X509Certificate readCertificate(File file) {
        Throwable th;
        if (!file.isFile()) {
            return null;
        }
        InputStream is = null;
        try {
            InputStream is2 = new BufferedInputStream(new FileInputStream(file));
            try {
                X509Certificate x509Certificate = (X509Certificate) CERT_FACTORY.generateCertificate(is2);
                IoUtils.closeQuietly(is2);
                return x509Certificate;
            } catch (IOException e) {
                is = is2;
                IoUtils.closeQuietly(is);
                return null;
            } catch (CertificateException e2) {
                is = is2;
                IoUtils.closeQuietly(is);
                return null;
            } catch (Throwable th2) {
                th = th2;
                is = is2;
                IoUtils.closeQuietly(is);
                throw th;
            }
        } catch (IOException e3) {
            IoUtils.closeQuietly(is);
            return null;
        } catch (CertificateException e4) {
            IoUtils.closeQuietly(is);
            return null;
        } catch (Throwable th3) {
            th = th3;
            IoUtils.closeQuietly(is);
            throw th;
        }
    }

    private void writeCertificate(File file, X509Certificate cert) throws IOException, CertificateException {
        Throwable th;
        File dir = file.getParentFile();
        dir.mkdirs();
        dir.setReadable(true, false);
        dir.setExecutable(true, false);
        OutputStream os = null;
        try {
            OutputStream os2 = new FileOutputStream(file);
            try {
                os2.write(cert.getEncoded());
                IoUtils.closeQuietly(os2);
                file.setReadable(true, false);
            } catch (Throwable th2) {
                th = th2;
                os = os2;
                IoUtils.closeQuietly(os);
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            IoUtils.closeQuietly(os);
            throw th;
        }
    }

    private boolean isDeletedSystemCertificate(X509Certificate x) {
        return getCertificateFile(this.deletedDir, x).exists();
    }

    public Date getCreationDate(String alias) {
        if (!containsAlias(alias)) {
            return null;
        }
        File file = fileForAlias(alias);
        if (file == null) {
            return null;
        }
        long time = file.lastModified();
        if (time == 0) {
            return null;
        }
        return new Date(time);
    }

    public Set<String> aliases() {
        Set<String> result = new HashSet();
        addAliases(result, PREFIX_USER, this.addedDir);
        addAliases(result, PREFIX_SYSTEM, this.systemDir);
        return result;
    }

    public Set<String> userAliases() {
        Set<String> result = new HashSet();
        addAliases(result, PREFIX_USER, this.addedDir);
        return result;
    }

    private void addAliases(Set<String> result, String prefix, File dir) {
        String[] files = dir.list();
        if (files != null) {
            for (String filename : files) {
                String alias = prefix + filename;
                if (containsAlias(alias)) {
                    result.add(alias);
                }
            }
        }
    }

    public Set<String> allSystemAliases() {
        Set<String> result = new HashSet();
        String[] files = this.systemDir.list();
        if (files == null) {
            return result;
        }
        for (String filename : files) {
            String alias = PREFIX_SYSTEM + filename;
            if (containsAlias(alias, true)) {
                result.add(alias);
            }
        }
        return result;
    }

    public boolean containsAlias(String alias) {
        return containsAlias(alias, false);
    }

    private boolean containsAlias(String alias, boolean includeDeletedSystem) {
        return getCertificate(alias, includeDeletedSystem) != null;
    }

    public String getCertificateAlias(Certificate c) {
        return getCertificateAlias(c, false);
    }

    public String getCertificateAlias(Certificate c, boolean includeDeletedSystem) {
        if (c == null || ((c instanceof X509Certificate) ^ 1) != 0) {
            return null;
        }
        X509Certificate x = (X509Certificate) c;
        File user = getCertificateFile(this.addedDir, x);
        if (user.exists()) {
            return PREFIX_USER + user.getName();
        }
        if (!includeDeletedSystem && isDeletedSystemCertificate(x)) {
            return null;
        }
        File system = getCertificateFile(this.systemDir, x);
        if (system.exists()) {
            return PREFIX_SYSTEM + system.getName();
        }
        return null;
    }

    public boolean isUserAddedCertificate(X509Certificate cert) {
        return getCertificateFile(this.addedDir, cert).exists();
    }

    public File getCertificateFile(File dir, final X509Certificate x) {
        return (File) findCert(dir, x.getSubjectX500Principal(), new CertSelector() {
            public boolean match(X509Certificate cert) {
                return cert.equals(x);
            }
        }, File.class);
    }

    public X509Certificate getTrustAnchor(final X509Certificate c) {
        CertSelector selector = new CertSelector() {
            public boolean match(X509Certificate ca) {
                return ca.getPublicKey().equals(c.getPublicKey());
            }
        };
        X509Certificate user = (X509Certificate) findCert(this.addedDir, c.getSubjectX500Principal(), selector, X509Certificate.class);
        if (user != null) {
            return user;
        }
        X509Certificate system = (X509Certificate) findCert(this.systemDir, c.getSubjectX500Principal(), selector, X509Certificate.class);
        if (system == null || (isDeletedSystemCertificate(system) ^ 1) == 0) {
            return null;
        }
        return system;
    }

    public X509Certificate findIssuer(final X509Certificate c) {
        CertSelector selector = new CertSelector() {
            public boolean match(X509Certificate ca) {
                try {
                    c.verify(ca.getPublicKey());
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        };
        X500Principal issuer = c.getIssuerX500Principal();
        X509Certificate user = (X509Certificate) findCert(this.addedDir, issuer, selector, X509Certificate.class);
        if (user != null) {
            return user;
        }
        X509Certificate system = (X509Certificate) findCert(this.systemDir, issuer, selector, X509Certificate.class);
        if (system == null || (isDeletedSystemCertificate(system) ^ 1) == 0) {
            return null;
        }
        return system;
    }

    public Set<X509Certificate> findAllIssuers(final X509Certificate c) {
        Set<X509Certificate> issuers = null;
        CertSelector selector = new CertSelector() {
            public boolean match(X509Certificate ca) {
                try {
                    c.verify(ca.getPublicKey());
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        };
        X500Principal issuer = c.getIssuerX500Principal();
        Set<X509Certificate> userAddedCerts = (Set) findCert(this.addedDir, issuer, selector, Set.class);
        if (userAddedCerts != null) {
            issuers = userAddedCerts;
        }
        Set<X509Certificate> systemCerts = (Set) findCert(this.systemDir, issuer, new CertSelector() {
            public boolean match(X509Certificate ca) {
                try {
                    if (TrustedCertificateStore.this.isDeletedSystemCertificate(ca)) {
                        return false;
                    }
                    c.verify(ca.getPublicKey());
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        }, Set.class);
        if (systemCerts != null) {
            if (issuers != null) {
                issuers.addAll(systemCerts);
            } else {
                issuers = systemCerts;
            }
        }
        return issuers != null ? issuers : Collections.emptySet();
    }

    private static boolean isSelfIssuedCertificate(OpenSSLX509Certificate cert) {
        long ctx = cert.getContext();
        if (NativeCrypto.X509_check_issued(ctx, ctx) == 0) {
            return true;
        }
        return false;
    }

    private static OpenSSLX509Certificate convertToOpenSSLIfNeeded(X509Certificate cert) throws CertificateException {
        if (cert == null) {
            return null;
        }
        if (cert instanceof OpenSSLX509Certificate) {
            return (OpenSSLX509Certificate) cert;
        }
        try {
            return OpenSSLX509Certificate.fromX509Der(cert.getEncoded());
        } catch (Exception e) {
            throw new CertificateException(e);
        }
    }

    public List<X509Certificate> getCertificateChain(X509Certificate leaf) throws CertificateException {
        LinkedHashSet<OpenSSLX509Certificate> chain = new LinkedHashSet();
        OpenSSLX509Certificate cert = convertToOpenSSLIfNeeded(leaf);
        chain.add(cert);
        while (!isSelfIssuedCertificate(cert)) {
            cert = convertToOpenSSLIfNeeded(findIssuer(cert));
            if (cert == null || chain.contains(cert)) {
                break;
            }
            chain.add(cert);
        }
        return new ArrayList(chain);
    }

    private <T> T findCert(File dir, X500Principal subject, CertSelector selector, Class<T> desiredReturnType) {
        T certs = null;
        String hash = hash(subject);
        int index = 0;
        while (true) {
            File file = file(dir, hash, index);
            if (file.isFile()) {
                if (!isTombstone(file)) {
                    X509Certificate cert = readCertificate(file);
                    if (cert != null && selector.match(cert)) {
                        if (desiredReturnType == X509Certificate.class) {
                            return cert;
                        }
                        if (desiredReturnType == Boolean.class) {
                            return Boolean.TRUE;
                        }
                        if (desiredReturnType == File.class) {
                            return file;
                        }
                        if (desiredReturnType == Set.class) {
                            if (certs == null) {
                                certs = new HashSet();
                            }
                            certs.add(cert);
                        } else {
                            throw new AssertionError();
                        }
                    }
                }
                index++;
            } else if (desiredReturnType == Boolean.class) {
                return Boolean.FALSE;
            } else {
                if (desiredReturnType == File.class) {
                    return file;
                }
                if (desiredReturnType == Set.class) {
                    return certs;
                }
                return null;
            }
        }
    }

    private String hash(X500Principal name) {
        return Hex.intToHexString(NativeCrypto.X509_NAME_hash_old(name), 8);
    }

    private File file(File dir, String hash, int index) {
        return new File(dir, hash + '.' + index);
    }

    public void installCertificate(X509Certificate cert) throws IOException, CertificateException {
        if (cert == null) {
            throw new NullPointerException("cert == null");
        } else if (getCertificateFile(this.systemDir, cert).exists()) {
            File deleted = getCertificateFile(this.deletedDir, cert);
            if (deleted.exists() && !deleted.delete()) {
                throw new IOException("Could not remove " + deleted);
            }
        } else {
            File user = getCertificateFile(this.addedDir, cert);
            if (!user.exists()) {
                writeCertificate(user, cert);
            }
        }
    }

    public void deleteCertificateEntry(String alias) throws IOException, CertificateException {
        if (alias != null) {
            File file = fileForAlias(alias);
            if (file != null) {
                if (isSystem(alias)) {
                    X509Certificate cert = readCertificate(file);
                    if (cert != null) {
                        File deleted = getCertificateFile(this.deletedDir, cert);
                        if (!deleted.exists()) {
                            writeCertificate(deleted, cert);
                        }
                    }
                } else if (isUser(alias)) {
                    new FileOutputStream(file).close();
                    removeUnnecessaryTombstones(alias);
                }
            }
        }
    }

    private void removeUnnecessaryTombstones(String alias) throws IOException {
        if (isUser(alias)) {
            int dotIndex = alias.lastIndexOf(46);
            if (dotIndex == -1) {
                throw new AssertionError(alias);
            }
            String hash = alias.substring(PREFIX_USER.length(), dotIndex);
            int lastTombstoneIndex = Integer.parseInt(alias.substring(dotIndex + 1));
            if (!file(this.addedDir, hash, lastTombstoneIndex + 1).exists()) {
                while (lastTombstoneIndex >= 0) {
                    File file = file(this.addedDir, hash, lastTombstoneIndex);
                    if (!isTombstone(file)) {
                        break;
                    } else if (file.delete()) {
                        lastTombstoneIndex--;
                    } else {
                        throw new IOException("Could not remove " + file);
                    }
                }
                return;
            }
            return;
        }
        throw new AssertionError(alias);
    }
}
