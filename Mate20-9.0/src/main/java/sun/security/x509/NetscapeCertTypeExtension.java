package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;
import sun.security.util.BitArray;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class NetscapeCertTypeExtension extends Extension implements CertAttrSet<String> {
    private static final int[] CertType_data = {2, 16, 840, 1, 113730, 1, 1};
    public static final String IDENT = "x509.info.extensions.NetscapeCertType";
    public static final String NAME = "NetscapeCertType";
    public static ObjectIdentifier NetscapeCertType_Id = null;
    public static final String OBJECT_SIGNING = "object_signing";
    public static final String OBJECT_SIGNING_CA = "object_signing_ca";
    public static final String SSL_CA = "ssl_ca";
    public static final String SSL_CLIENT = "ssl_client";
    public static final String SSL_SERVER = "ssl_server";
    public static final String S_MIME = "s_mime";
    public static final String S_MIME_CA = "s_mime_ca";
    private static final Vector<String> mAttributeNames = new Vector<>();
    private static MapEntry[] mMapData = {new MapEntry(SSL_CLIENT, 0), new MapEntry(SSL_SERVER, 1), new MapEntry(S_MIME, 2), new MapEntry(OBJECT_SIGNING, 3), new MapEntry(SSL_CA, 5), new MapEntry(S_MIME_CA, 6), new MapEntry(OBJECT_SIGNING_CA, 7)};
    private boolean[] bitString;

    private static class MapEntry {
        String mName;
        int mPosition;

        MapEntry(String name, int position) {
            this.mName = name;
            this.mPosition = position;
        }
    }

    static {
        try {
            NetscapeCertType_Id = new ObjectIdentifier(CertType_data);
        } catch (IOException e) {
        }
        for (MapEntry entry : mMapData) {
            mAttributeNames.add(entry.mName);
        }
    }

    private static int getPosition(String name) throws IOException {
        for (int i = 0; i < mMapData.length; i++) {
            if (name.equalsIgnoreCase(mMapData[i].mName)) {
                return mMapData[i].mPosition;
            }
        }
        throw new IOException("Attribute name [" + name + "] not recognized by CertAttrSet:NetscapeCertType.");
    }

    private void encodeThis() throws IOException {
        DerOutputStream os = new DerOutputStream();
        os.putTruncatedUnalignedBitString(new BitArray(this.bitString));
        this.extensionValue = os.toByteArray();
    }

    private boolean isSet(int position) {
        return position < this.bitString.length && this.bitString[position];
    }

    private void set(int position, boolean val) {
        if (position >= this.bitString.length) {
            boolean[] tmp = new boolean[(position + 1)];
            System.arraycopy((Object) this.bitString, 0, (Object) tmp, 0, this.bitString.length);
            this.bitString = tmp;
        }
        this.bitString[position] = val;
    }

    public NetscapeCertTypeExtension(byte[] bitString2) throws IOException {
        this.bitString = new BitArray(bitString2.length * 8, bitString2).toBooleanArray();
        this.extensionId = NetscapeCertType_Id;
        this.critical = true;
        encodeThis();
    }

    public NetscapeCertTypeExtension(boolean[] bitString2) throws IOException {
        this.bitString = bitString2;
        this.extensionId = NetscapeCertType_Id;
        this.critical = true;
        encodeThis();
    }

    public NetscapeCertTypeExtension(Boolean critical, Object value) throws IOException {
        this.extensionId = NetscapeCertType_Id;
        this.critical = critical.booleanValue();
        this.extensionValue = (byte[]) value;
        this.bitString = new DerValue(this.extensionValue).getUnalignedBitString().toBooleanArray();
    }

    public NetscapeCertTypeExtension() {
        this.extensionId = NetscapeCertType_Id;
        this.critical = true;
        this.bitString = new boolean[0];
    }

    public void set(String name, Object obj) throws IOException {
        if (obj instanceof Boolean) {
            set(getPosition(name), ((Boolean) obj).booleanValue());
            encodeThis();
            return;
        }
        throw new IOException("Attribute must be of type Boolean.");
    }

    public Boolean get(String name) throws IOException {
        return Boolean.valueOf(isSet(getPosition(name)));
    }

    public void delete(String name) throws IOException {
        set(getPosition(name), false);
        encodeThis();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append("NetscapeCertType [\n");
        if (isSet(0)) {
            sb.append("   SSL client\n");
        }
        if (isSet(1)) {
            sb.append("   SSL server\n");
        }
        if (isSet(2)) {
            sb.append("   S/MIME\n");
        }
        if (isSet(3)) {
            sb.append("   Object Signing\n");
        }
        if (isSet(5)) {
            sb.append("   SSL CA\n");
        }
        if (isSet(6)) {
            sb.append("   S/MIME CA\n");
        }
        if (isSet(7)) {
            sb.append("   Object Signing CA");
        }
        sb.append("]\n");
        return sb.toString();
    }

    public void encode(OutputStream out) throws IOException {
        DerOutputStream tmp = new DerOutputStream();
        if (this.extensionValue == null) {
            this.extensionId = NetscapeCertType_Id;
            this.critical = true;
            encodeThis();
        }
        super.encode(tmp);
        out.write(tmp.toByteArray());
    }

    public Enumeration<String> getElements() {
        return mAttributeNames.elements();
    }

    public String getName() {
        return NAME;
    }

    public boolean[] getKeyUsageMappedBits() {
        KeyUsageExtension keyUsage = new KeyUsageExtension();
        Boolean val = Boolean.TRUE;
        try {
            if (isSet(getPosition(SSL_CLIENT)) || isSet(getPosition(S_MIME)) || isSet(getPosition(OBJECT_SIGNING))) {
                keyUsage.set(KeyUsageExtension.DIGITAL_SIGNATURE, (Object) val);
            }
            if (isSet(getPosition(SSL_SERVER))) {
                keyUsage.set(KeyUsageExtension.KEY_ENCIPHERMENT, (Object) val);
            }
            if (isSet(getPosition(SSL_CA)) || isSet(getPosition(S_MIME_CA)) || isSet(getPosition(OBJECT_SIGNING_CA))) {
                keyUsage.set(KeyUsageExtension.KEY_CERTSIGN, (Object) val);
            }
        } catch (IOException e) {
        }
        return keyUsage.getBits();
    }
}
