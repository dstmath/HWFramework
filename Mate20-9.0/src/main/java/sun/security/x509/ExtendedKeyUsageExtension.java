package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class ExtendedKeyUsageExtension extends Extension implements CertAttrSet<String> {
    public static final String IDENT = "x509.info.extensions.ExtendedKeyUsage";
    public static final String NAME = "ExtendedKeyUsage";
    private static final int[] OCSPSigningOidData = {1, 3, 6, 1, 5, 5, 7, 3, 9};
    public static final String USAGES = "usages";
    private static final int[] anyExtendedKeyUsageOidData = {2, 5, 29, 37, 0};
    private static final int[] clientAuthOidData = {1, 3, 6, 1, 5, 5, 7, 3, 2};
    private static final int[] codeSigningOidData = {1, 3, 6, 1, 5, 5, 7, 3, 3};
    private static final int[] emailProtectionOidData = {1, 3, 6, 1, 5, 5, 7, 3, 4};
    private static final int[] ipsecEndSystemOidData = {1, 3, 6, 1, 5, 5, 7, 3, 5};
    private static final int[] ipsecTunnelOidData = {1, 3, 6, 1, 5, 5, 7, 3, 6};
    private static final int[] ipsecUserOidData = {1, 3, 6, 1, 5, 5, 7, 3, 7};
    private static final Map<ObjectIdentifier, String> map = new HashMap();
    private static final int[] serverAuthOidData = {1, 3, 6, 1, 5, 5, 7, 3, 1};
    private static final int[] timeStampingOidData = {1, 3, 6, 1, 5, 5, 7, 3, 8};
    private Vector<ObjectIdentifier> keyUsages;

    static {
        map.put(ObjectIdentifier.newInternal(anyExtendedKeyUsageOidData), "anyExtendedKeyUsage");
        map.put(ObjectIdentifier.newInternal(serverAuthOidData), "serverAuth");
        map.put(ObjectIdentifier.newInternal(clientAuthOidData), "clientAuth");
        map.put(ObjectIdentifier.newInternal(codeSigningOidData), "codeSigning");
        map.put(ObjectIdentifier.newInternal(emailProtectionOidData), "emailProtection");
        map.put(ObjectIdentifier.newInternal(ipsecEndSystemOidData), "ipsecEndSystem");
        map.put(ObjectIdentifier.newInternal(ipsecTunnelOidData), "ipsecTunnel");
        map.put(ObjectIdentifier.newInternal(ipsecUserOidData), "ipsecUser");
        map.put(ObjectIdentifier.newInternal(timeStampingOidData), "timeStamping");
        map.put(ObjectIdentifier.newInternal(OCSPSigningOidData), "OCSPSigning");
    }

    private void encodeThis() throws IOException {
        if (this.keyUsages == null || this.keyUsages.isEmpty()) {
            this.extensionValue = null;
            return;
        }
        DerOutputStream os = new DerOutputStream();
        DerOutputStream tmp = new DerOutputStream();
        for (int i = 0; i < this.keyUsages.size(); i++) {
            tmp.putOID(this.keyUsages.elementAt(i));
        }
        os.write((byte) 48, tmp);
        this.extensionValue = os.toByteArray();
    }

    public ExtendedKeyUsageExtension(Vector<ObjectIdentifier> keyUsages2) throws IOException {
        this(Boolean.FALSE, keyUsages2);
    }

    public ExtendedKeyUsageExtension(Boolean critical, Vector<ObjectIdentifier> keyUsages2) throws IOException {
        this.keyUsages = keyUsages2;
        this.extensionId = PKIXExtensions.ExtendedKeyUsage_Id;
        this.critical = critical.booleanValue();
        encodeThis();
    }

    public ExtendedKeyUsageExtension(Boolean critical, Object value) throws IOException {
        this.extensionId = PKIXExtensions.ExtendedKeyUsage_Id;
        this.critical = critical.booleanValue();
        this.extensionValue = (byte[]) value;
        DerValue val = new DerValue(this.extensionValue);
        if (val.tag == 48) {
            this.keyUsages = new Vector<>();
            while (val.data.available() != 0) {
                this.keyUsages.addElement(val.data.getDerValue().getOID());
            }
            return;
        }
        throw new IOException("Invalid encoding for ExtendedKeyUsageExtension.");
    }

    public String toString() {
        if (this.keyUsages == null) {
            return "";
        }
        String usage = "  ";
        boolean first = true;
        Iterator<ObjectIdentifier> it = this.keyUsages.iterator();
        while (it.hasNext()) {
            ObjectIdentifier oid = it.next();
            if (!first) {
                usage = usage + "\n  ";
            }
            if (map.get(oid) != null) {
                usage = usage + result;
            } else {
                usage = usage + oid.toString();
            }
            first = false;
        }
        return super.toString() + "ExtendedKeyUsages [\n" + usage + "\n]\n";
    }

    public void encode(OutputStream out) throws IOException {
        DerOutputStream tmp = new DerOutputStream();
        if (this.extensionValue == null) {
            this.extensionId = PKIXExtensions.ExtendedKeyUsage_Id;
            this.critical = false;
            encodeThis();
        }
        super.encode(tmp);
        out.write(tmp.toByteArray());
    }

    public void set(String name, Object obj) throws IOException {
        if (!name.equalsIgnoreCase(USAGES)) {
            throw new IOException("Attribute name [" + name + "] not recognized by CertAttrSet:ExtendedKeyUsageExtension.");
        } else if (obj instanceof Vector) {
            this.keyUsages = (Vector) obj;
            encodeThis();
        } else {
            throw new IOException("Attribute value should be of type Vector.");
        }
    }

    public Vector<ObjectIdentifier> get(String name) throws IOException {
        if (name.equalsIgnoreCase(USAGES)) {
            return this.keyUsages;
        }
        throw new IOException("Attribute name [" + name + "] not recognized by CertAttrSet:ExtendedKeyUsageExtension.");
    }

    public void delete(String name) throws IOException {
        if (name.equalsIgnoreCase(USAGES)) {
            this.keyUsages = null;
            encodeThis();
            return;
        }
        throw new IOException("Attribute name [" + name + "] not recognized by CertAttrSet:ExtendedKeyUsageExtension.");
    }

    public Enumeration<String> getElements() {
        AttributeNameEnumeration elements = new AttributeNameEnumeration();
        elements.addElement(USAGES);
        return elements.elements();
    }

    public String getName() {
        return NAME;
    }

    public List<String> getExtendedKeyUsage() {
        List<String> al = new ArrayList<>(this.keyUsages.size());
        Iterator<ObjectIdentifier> it = this.keyUsages.iterator();
        while (it.hasNext()) {
            al.add(it.next().toString());
        }
        return al;
    }
}
