package sun.security.util;

import java.io.IOException;
import java.security.CodeSigner;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarException;
import java.util.jar.Manifest;
import sun.security.jca.Providers;

public class ManifestEntryVerifier {
    private static final Debug debug = Debug.getInstance("jar");
    private static final char[] hexc = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    HashMap<String, MessageDigest> createdDigests = new HashMap(11);
    ArrayList<MessageDigest> digests = new ArrayList();
    private JarEntry entry;
    private Manifest man;
    ArrayList<byte[]> manifestHashes = new ArrayList();
    private String name = null;
    private CodeSigner[] signers = null;
    private boolean skip = true;

    private static class SunProviderHolder {
        private static final Provider instance = Providers.getSunProvider();

        private SunProviderHolder() {
        }
    }

    public ManifestEntryVerifier(Manifest man) {
        this.man = man;
    }

    public void setEntry(String name, JarEntry entry) throws IOException {
        this.digests.clear();
        this.manifestHashes.clear();
        this.name = name;
        this.entry = entry;
        this.skip = true;
        this.signers = null;
        if (this.man != null && name != null) {
            Attributes attr = this.man.getAttributes(name);
            if (attr == null) {
                attr = this.man.getAttributes("./" + name);
                if (attr == null) {
                    attr = this.man.getAttributes("/" + name);
                    if (attr == null) {
                        return;
                    }
                }
            }
            for (Entry<Object, Object> se : attr.entrySet()) {
                String key = se.getKey().toString();
                if (key.toUpperCase(Locale.ENGLISH).endsWith("-DIGEST")) {
                    String algorithm = key.substring(0, key.length() - 7);
                    MessageDigest digest = (MessageDigest) this.createdDigests.get(algorithm);
                    if (digest == null) {
                        try {
                            digest = MessageDigest.getInstance(algorithm, SunProviderHolder.instance);
                            this.createdDigests.put(algorithm, digest);
                        } catch (NoSuchAlgorithmException e) {
                        }
                    }
                    if (digest != null) {
                        this.skip = false;
                        digest.reset();
                        this.digests.add(digest);
                        this.manifestHashes.add(Base64.getMimeDecoder().decode((String) se.getValue()));
                    }
                }
            }
        }
    }

    public void update(byte buffer) {
        if (!this.skip) {
            for (int i = 0; i < this.digests.size(); i++) {
                ((MessageDigest) this.digests.get(i)).update(buffer);
            }
        }
    }

    public void update(byte[] buffer, int off, int len) {
        if (!this.skip) {
            for (int i = 0; i < this.digests.size(); i++) {
                ((MessageDigest) this.digests.get(i)).update(buffer, off, len);
            }
        }
    }

    public JarEntry getEntry() {
        return this.entry;
    }

    public CodeSigner[] verify(Hashtable<String, CodeSigner[]> verifiedSigners, Hashtable<String, CodeSigner[]> sigFileSigners) throws JarException {
        if (this.skip) {
            return null;
        }
        if (this.signers != null) {
            return this.signers;
        }
        int i = 0;
        while (i < this.digests.size()) {
            MessageDigest digest = (MessageDigest) this.digests.get(i);
            byte[] manHash = (byte[]) this.manifestHashes.get(i);
            byte[] theHash = digest.digest();
            if (debug != null) {
                debug.println("Manifest Entry: " + this.name + " digest=" + digest.getAlgorithm());
                debug.println("  manifest " + toHex(manHash));
                debug.println("  computed " + toHex(theHash));
                debug.println();
            }
            if (MessageDigest.isEqual(theHash, manHash)) {
                i++;
            } else {
                throw new SecurityException(digest.getAlgorithm() + " digest error for " + this.name);
            }
        }
        this.signers = (CodeSigner[]) sigFileSigners.remove(this.name);
        if (this.signers != null) {
            verifiedSigners.put(this.name, this.signers);
        }
        return this.signers;
    }

    static String toHex(byte[] data) {
        StringBuffer sb = new StringBuffer(data.length * 2);
        for (int i = 0; i < data.length; i++) {
            sb.append(hexc[(data[i] >> 4) & 15]);
            sb.append(hexc[data[i] & 15]);
        }
        return sb.toString();
    }
}
