package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.security.cert.CertificateException;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;
import sun.misc.HexDumpEncoder;
import sun.security.util.Debug;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class CertificateExtensions implements CertAttrSet<Extension> {
    public static final String IDENT = "x509.info.extensions";
    public static final String NAME = "extensions";
    private static Class[] PARAMS = new Class[]{Boolean.class, Object.class};
    private static final Debug debug = Debug.getInstance(X509CertImpl.NAME);
    private Map<String, Extension> map = Collections.synchronizedMap(new TreeMap());
    private Map<String, Extension> unparseableExtensions;
    private boolean unsupportedCritExt = false;

    public CertificateExtensions(DerInputStream in) throws IOException {
        init(in);
    }

    private void init(DerInputStream in) throws IOException {
        DerValue[] exts = in.getSequence(5);
        for (DerValue extension : exts) {
            parseExtension(new Extension(extension));
        }
    }

    private void parseExtension(Extension ext) throws IOException {
        try {
            Class<?> extClass = OIDMap.getClass(ext.getExtensionId());
            if (extClass == null) {
                if (ext.isCritical()) {
                    this.unsupportedCritExt = true;
                }
                if (this.map.put(ext.getExtensionId().toString(), ext) != null) {
                    throw new IOException("Duplicate extensions not allowed");
                }
                return;
            }
            CertAttrSet<?> certExt = (CertAttrSet) extClass.getConstructor(PARAMS).newInstance(Boolean.valueOf(ext.isCritical()), ext.getExtensionValue());
            if (this.map.put(certExt.getName(), (Extension) certExt) != null) {
                throw new IOException("Duplicate extensions not allowed");
            }
        } catch (InvocationTargetException invk) {
            Throwable e = invk.getTargetException();
            if (!ext.isCritical()) {
                if (this.unparseableExtensions == null) {
                    this.unparseableExtensions = new TreeMap();
                }
                this.unparseableExtensions.put(ext.getExtensionId().toString(), new UnparseableExtension(ext, e));
                if (debug != null) {
                    debug.println("Error parsing extension: " + ext);
                    e.printStackTrace();
                    System.err.println(new HexDumpEncoder().encodeBuffer(ext.getExtensionValue()));
                }
            } else if (e instanceof IOException) {
                throw ((IOException) e);
            } else {
                throw new IOException(e);
            }
        } catch (IOException e2) {
            throw e2;
        } catch (Throwable e3) {
            throw new IOException(e3);
        }
    }

    public void encode(OutputStream out) throws CertificateException, IOException {
        encode(out, false);
    }

    public void encode(OutputStream out, boolean isCertReq) throws CertificateException, IOException {
        DerOutputStream tmp;
        DerOutputStream extOut = new DerOutputStream();
        Object[] objs = this.map.values().toArray();
        for (int i = 0; i < objs.length; i++) {
            if (objs[i] instanceof CertAttrSet) {
                ((CertAttrSet) objs[i]).encode(extOut);
            } else if (objs[i] instanceof Extension) {
                ((Extension) objs[i]).encode(extOut);
            } else {
                throw new CertificateException("Illegal extension object");
            }
        }
        DerOutputStream seq = new DerOutputStream();
        seq.write((byte) 48, extOut);
        if (isCertReq) {
            tmp = seq;
        } else {
            tmp = new DerOutputStream();
            tmp.write(DerValue.createTag(Byte.MIN_VALUE, true, (byte) 3), seq);
        }
        out.write(tmp.toByteArray());
    }

    public void set(String name, Object obj) throws IOException {
        if (obj instanceof Extension) {
            this.map.put(name, (Extension) obj);
            return;
        }
        throw new IOException("Unknown extension type.");
    }

    public Extension get(String name) throws IOException {
        Extension obj = (Extension) this.map.get(name);
        if (obj != null) {
            return obj;
        }
        throw new IOException("No extension found with name " + name);
    }

    Extension getExtension(String name) {
        return (Extension) this.map.get(name);
    }

    public void delete(String name) throws IOException {
        if (this.map.get(name) == null) {
            throw new IOException("No extension found with name " + name);
        }
        this.map.remove(name);
    }

    public String getNameByOid(ObjectIdentifier oid) throws IOException {
        for (String name : this.map.keySet()) {
            if (((Extension) this.map.get(name)).getExtensionId().equals((Object) oid)) {
                return name;
            }
        }
        return null;
    }

    public Enumeration<Extension> getElements() {
        return Collections.enumeration(this.map.values());
    }

    public Collection<Extension> getAllExtensions() {
        return this.map.values();
    }

    public Map<String, Extension> getUnparseableExtensions() {
        if (this.unparseableExtensions == null) {
            return Collections.emptyMap();
        }
        return this.unparseableExtensions;
    }

    public String getName() {
        return "extensions";
    }

    public boolean hasUnsupportedCriticalExtension() {
        return this.unsupportedCritExt;
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CertificateExtensions)) {
            return false;
        }
        Object[] objs = ((CertificateExtensions) other).getAllExtensions().toArray();
        int len = objs.length;
        if (len != this.map.size()) {
            return false;
        }
        String key = null;
        for (int i = 0; i < len; i++) {
            if (objs[i] instanceof CertAttrSet) {
                key = ((CertAttrSet) objs[i]).getName();
            }
            Extension otherExt = objs[i];
            if (key == null) {
                key = otherExt.getExtensionId().toString();
            }
            Extension thisExt = (Extension) this.map.get(key);
            if (thisExt == null || !thisExt.equals(otherExt)) {
                return false;
            }
        }
        return getUnparseableExtensions().equals(((CertificateExtensions) other).getUnparseableExtensions());
    }

    public int hashCode() {
        return this.map.hashCode() + getUnparseableExtensions().hashCode();
    }

    public String toString() {
        return this.map.toString();
    }
}
