package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class PolicyMappingsExtension extends Extension implements CertAttrSet<String> {
    public static final String IDENT = "x509.info.extensions.PolicyMappings";
    public static final String MAP = "map";
    public static final String NAME = "PolicyMappings";
    private List<CertificatePolicyMap> maps;

    private void encodeThis() throws IOException {
        if (this.maps == null || this.maps.isEmpty()) {
            this.extensionValue = null;
            return;
        }
        DerOutputStream os = new DerOutputStream();
        DerOutputStream tmp = new DerOutputStream();
        for (CertificatePolicyMap map : this.maps) {
            map.encode(tmp);
        }
        os.write((byte) 48, tmp);
        this.extensionValue = os.toByteArray();
    }

    public PolicyMappingsExtension(List<CertificatePolicyMap> map) throws IOException {
        this.maps = map;
        this.extensionId = PKIXExtensions.PolicyMappings_Id;
        this.critical = false;
        encodeThis();
    }

    public PolicyMappingsExtension() {
        this.extensionId = PKIXExtensions.KeyUsage_Id;
        this.critical = false;
        this.maps = Collections.emptyList();
    }

    public PolicyMappingsExtension(Boolean critical, Object value) throws IOException {
        this.extensionId = PKIXExtensions.PolicyMappings_Id;
        this.critical = critical.booleanValue();
        this.extensionValue = (byte[]) value;
        DerValue val = new DerValue(this.extensionValue);
        if (val.tag == 48) {
            this.maps = new ArrayList();
            while (val.data.available() != 0) {
                this.maps.add(new CertificatePolicyMap(val.data.getDerValue()));
            }
            return;
        }
        throw new IOException("Invalid encoding for PolicyMappingsExtension.");
    }

    public String toString() {
        if (this.maps == null) {
            return "";
        }
        return super.toString() + "PolicyMappings [\n" + this.maps.toString() + "]\n";
    }

    public void encode(OutputStream out) throws IOException {
        DerOutputStream tmp = new DerOutputStream();
        if (this.extensionValue == null) {
            this.extensionId = PKIXExtensions.PolicyMappings_Id;
            this.critical = false;
            encodeThis();
        }
        super.encode(tmp);
        out.write(tmp.toByteArray());
    }

    public void set(String name, Object obj) throws IOException {
        if (!name.equalsIgnoreCase(MAP)) {
            throw new IOException("Attribute name not recognized by CertAttrSet:PolicyMappingsExtension.");
        } else if (obj instanceof List) {
            this.maps = (List) obj;
            encodeThis();
        } else {
            throw new IOException("Attribute value should be of type List.");
        }
    }

    public List<CertificatePolicyMap> get(String name) throws IOException {
        if (name.equalsIgnoreCase(MAP)) {
            return this.maps;
        }
        throw new IOException("Attribute name not recognized by CertAttrSet:PolicyMappingsExtension.");
    }

    public void delete(String name) throws IOException {
        if (name.equalsIgnoreCase(MAP)) {
            this.maps = null;
            encodeThis();
            return;
        }
        throw new IOException("Attribute name not recognized by CertAttrSet:PolicyMappingsExtension.");
    }

    public Enumeration<String> getElements() {
        AttributeNameEnumeration elements = new AttributeNameEnumeration();
        elements.addElement(MAP);
        return elements.elements();
    }

    public String getName() {
        return NAME;
    }
}
