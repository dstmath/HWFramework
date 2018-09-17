package sun.security.util;

import java.io.IOException;
import java.security.CodeSigner;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarException;
import java.util.jar.Manifest;
import sun.misc.BASE64Decoder;

public class ManifestEntryVerifier {
    private static final Debug debug = null;
    private static final char[] hexc = null;
    HashMap<String, MessageDigest> createdDigests;
    private BASE64Decoder decoder;
    ArrayList<MessageDigest> digests;
    private JarEntry entry;
    private Manifest man;
    ArrayList<byte[]> manifestHashes;
    private String name;
    private CodeSigner[] signers;
    private boolean skip;

    private static class SunProviderHolder {
        private static final Provider instance = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.util.ManifestEntryVerifier.SunProviderHolder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.util.ManifestEntryVerifier.SunProviderHolder.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.security.util.ManifestEntryVerifier.SunProviderHolder.<clinit>():void");
        }

        private SunProviderHolder() {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.util.ManifestEntryVerifier.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.util.ManifestEntryVerifier.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.security.util.ManifestEntryVerifier.<clinit>():void");
    }

    public ManifestEntryVerifier(Manifest man) {
        this.decoder = null;
        this.name = null;
        this.skip = true;
        this.signers = null;
        this.createdDigests = new HashMap(11);
        this.digests = new ArrayList();
        this.manifestHashes = new ArrayList();
        this.decoder = new BASE64Decoder();
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
                        this.manifestHashes.add(this.decoder.decodeBuffer((String) se.getValue()));
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
