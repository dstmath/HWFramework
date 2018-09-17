package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class CRLExtensions {
    private static final Class[] PARAMS = new Class[]{Boolean.class, Object.class};
    private Map<String, Extension> map = Collections.synchronizedMap(new TreeMap());
    private boolean unsupportedCritExt = false;

    public CRLExtensions(DerInputStream in) throws CRLException {
        init(in);
    }

    private void init(DerInputStream derStrm) throws CRLException {
        DerInputStream str = derStrm;
        try {
            byte nextByte = (byte) derStrm.peekByte();
            if ((nextByte & 192) == 128 && (nextByte & 31) == 0) {
                str = derStrm.getDerValue().data;
            }
            DerValue[] exts = str.getSequence(5);
            for (DerValue extension : exts) {
                parseExtension(new Extension(extension));
            }
        } catch (IOException e) {
            throw new CRLException("Parsing error: " + e.toString());
        }
    }

    private void parseExtension(Extension ext) throws CRLException {
        try {
            Class<?> extClass = OIDMap.getClass(ext.getExtensionId());
            if (extClass == null) {
                if (ext.isCritical()) {
                    this.unsupportedCritExt = true;
                }
                if (this.map.put(ext.getExtensionId().toString(), ext) != null) {
                    throw new CRLException("Duplicate extensions not allowed");
                }
                return;
            }
            CertAttrSet<?> crlExt = (CertAttrSet) extClass.getConstructor(PARAMS).newInstance(Boolean.valueOf(ext.isCritical()), ext.getExtensionValue());
            if (this.map.put(crlExt.getName(), (Extension) crlExt) != null) {
                throw new CRLException("Duplicate extensions not allowed");
            }
        } catch (InvocationTargetException invk) {
            throw new CRLException(invk.getTargetException().getMessage());
        } catch (Exception e) {
            throw new CRLException(e.toString());
        }
    }

    public void encode(OutputStream out, boolean isExplicit) throws CRLException {
        try {
            DerOutputStream extOut = new DerOutputStream();
            Object[] objs = this.map.values().toArray();
            for (int i = 0; i < objs.length; i++) {
                if (objs[i] instanceof CertAttrSet) {
                    ((CertAttrSet) objs[i]).encode(extOut);
                } else if (objs[i] instanceof Extension) {
                    ((Extension) objs[i]).encode(extOut);
                } else {
                    throw new CRLException("Illegal extension object");
                }
            }
            DerOutputStream seq = new DerOutputStream();
            seq.write((byte) 48, extOut);
            DerOutputStream tmp = new DerOutputStream();
            if (isExplicit) {
                tmp.write(DerValue.createTag(Byte.MIN_VALUE, true, (byte) 0), seq);
            } else {
                tmp = seq;
            }
            out.write(tmp.toByteArray());
        } catch (IOException e) {
            throw new CRLException("Encoding error: " + e.toString());
        } catch (CertificateException e2) {
            throw new CRLException("Encoding error: " + e2.toString());
        }
    }

    public Extension get(String alias) {
        String name;
        if (new X509AttributeName(alias).getPrefix().equalsIgnoreCase(X509CertImpl.NAME)) {
            name = alias.substring(alias.lastIndexOf(".") + 1);
        } else {
            name = alias;
        }
        return (Extension) this.map.get(name);
    }

    public void set(String alias, Object obj) {
        this.map.put(alias, (Extension) obj);
    }

    public void delete(String alias) {
        this.map.remove(alias);
    }

    public Enumeration<Extension> getElements() {
        return Collections.enumeration(this.map.values());
    }

    public Collection<Extension> getAllExtensions() {
        return this.map.values();
    }

    public boolean hasUnsupportedCriticalExtension() {
        return this.unsupportedCritExt;
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CRLExtensions)) {
            return false;
        }
        Object[] objs = ((CRLExtensions) other).getAllExtensions().toArray();
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
        return true;
    }

    public int hashCode() {
        return this.map.hashCode();
    }

    public String toString() {
        return this.map.toString();
    }
}
