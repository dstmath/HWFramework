package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class CRLDistributionPointsExtension extends Extension implements CertAttrSet<String> {
    public static final String IDENT = "x509.info.extensions.CRLDistributionPoints";
    public static final String NAME = "CRLDistributionPoints";
    public static final String POINTS = "points";
    private List<DistributionPoint> distributionPoints;
    private String extensionName;

    public CRLDistributionPointsExtension(List<DistributionPoint> distributionPoints2) throws IOException {
        this(false, distributionPoints2);
    }

    public CRLDistributionPointsExtension(boolean isCritical, List<DistributionPoint> distributionPoints2) throws IOException {
        this(PKIXExtensions.CRLDistributionPoints_Id, isCritical, distributionPoints2, NAME);
    }

    protected CRLDistributionPointsExtension(ObjectIdentifier extensionId, boolean isCritical, List<DistributionPoint> distributionPoints2, String extensionName2) throws IOException {
        this.extensionId = extensionId;
        this.critical = isCritical;
        this.distributionPoints = distributionPoints2;
        encodeThis();
        this.extensionName = extensionName2;
    }

    public CRLDistributionPointsExtension(Boolean critical, Object value) throws IOException {
        this(PKIXExtensions.CRLDistributionPoints_Id, critical, value, NAME);
    }

    protected CRLDistributionPointsExtension(ObjectIdentifier extensionId, Boolean critical, Object value, String extensionName2) throws IOException {
        this.extensionId = extensionId;
        this.critical = critical.booleanValue();
        if (value instanceof byte[]) {
            this.extensionValue = (byte[]) value;
            DerValue val = new DerValue(this.extensionValue);
            if (val.tag == 48) {
                this.distributionPoints = new ArrayList();
                while (val.data.available() != 0) {
                    this.distributionPoints.add(new DistributionPoint(val.data.getDerValue()));
                }
                this.extensionName = extensionName2;
                return;
            }
            throw new IOException("Invalid encoding for " + extensionName2 + " extension.");
        }
        throw new IOException("Illegal argument type");
    }

    public String getName() {
        return this.extensionName;
    }

    public void encode(OutputStream out) throws IOException {
        encode(out, PKIXExtensions.CRLDistributionPoints_Id, false);
    }

    /* access modifiers changed from: protected */
    public void encode(OutputStream out, ObjectIdentifier extensionId, boolean isCritical) throws IOException {
        DerOutputStream tmp = new DerOutputStream();
        if (this.extensionValue == null) {
            this.extensionId = extensionId;
            this.critical = isCritical;
            encodeThis();
        }
        super.encode(tmp);
        out.write(tmp.toByteArray());
    }

    public void set(String name, Object obj) throws IOException {
        if (!name.equalsIgnoreCase(POINTS)) {
            throw new IOException("Attribute name [" + name + "] not recognized by CertAttrSet:" + this.extensionName + ".");
        } else if (obj instanceof List) {
            this.distributionPoints = (List) obj;
            encodeThis();
        } else {
            throw new IOException("Attribute value should be of type List.");
        }
    }

    public List<DistributionPoint> get(String name) throws IOException {
        if (name.equalsIgnoreCase(POINTS)) {
            return this.distributionPoints;
        }
        throw new IOException("Attribute name [" + name + "] not recognized by CertAttrSet:" + this.extensionName + ".");
    }

    public void delete(String name) throws IOException {
        if (name.equalsIgnoreCase(POINTS)) {
            this.distributionPoints = Collections.emptyList();
            encodeThis();
            return;
        }
        throw new IOException("Attribute name [" + name + "] not recognized by CertAttrSet:" + this.extensionName + '.');
    }

    public Enumeration<String> getElements() {
        AttributeNameEnumeration elements = new AttributeNameEnumeration();
        elements.addElement(POINTS);
        return elements.elements();
    }

    private void encodeThis() throws IOException {
        if (this.distributionPoints.isEmpty()) {
            this.extensionValue = null;
            return;
        }
        DerOutputStream pnts = new DerOutputStream();
        for (DistributionPoint point : this.distributionPoints) {
            point.encode(pnts);
        }
        DerOutputStream seq = new DerOutputStream();
        seq.write((byte) 48, pnts);
        this.extensionValue = seq.toByteArray();
    }

    public String toString() {
        return super.toString() + this.extensionName + " [\n  " + this.distributionPoints + "]\n";
    }
}
