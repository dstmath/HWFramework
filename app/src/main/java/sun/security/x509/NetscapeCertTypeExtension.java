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
    private static final int[] CertType_data = null;
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
    private static final Vector<String> mAttributeNames = null;
    private static MapEntry[] mMapData;
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
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.x509.NetscapeCertTypeExtension.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.x509.NetscapeCertTypeExtension.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.security.x509.NetscapeCertTypeExtension.<clinit>():void");
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
        return this.bitString[position];
    }

    private void set(int position, boolean val) {
        if (position >= this.bitString.length) {
            boolean[] tmp = new boolean[(position + 1)];
            System.arraycopy(this.bitString, 0, tmp, 0, this.bitString.length);
            this.bitString = tmp;
        }
        this.bitString[position] = val;
    }

    public NetscapeCertTypeExtension(byte[] bitString) throws IOException {
        this.bitString = new BitArray(bitString.length * 8, bitString).toBooleanArray();
        this.extensionId = NetscapeCertType_Id;
        this.critical = true;
        encodeThis();
    }

    public NetscapeCertTypeExtension(boolean[] bitString) throws IOException {
        this.bitString = bitString;
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

    public Object get(String name) throws IOException {
        return Boolean.valueOf(isSet(getPosition(name)));
    }

    public void delete(String name) throws IOException {
        set(getPosition(name), false);
        encodeThis();
    }

    public String toString() {
        String s = super.toString() + "NetscapeCertType [\n";
        try {
            if (isSet(getPosition(SSL_CLIENT))) {
                s = s + "   SSL client\n";
            }
            if (isSet(getPosition(SSL_SERVER))) {
                s = s + "   SSL server\n";
            }
            if (isSet(getPosition(S_MIME))) {
                s = s + "   S/MIME\n";
            }
            if (isSet(getPosition(OBJECT_SIGNING))) {
                s = s + "   Object Signing\n";
            }
            if (isSet(getPosition(SSL_CA))) {
                s = s + "   SSL CA\n";
            }
            if (isSet(getPosition(S_MIME_CA))) {
                s = s + "   S/MIME CA\n";
            }
            if (isSet(getPosition(OBJECT_SIGNING_CA))) {
                s = s + "   Object Signing CA";
            }
        } catch (Exception e) {
        }
        return s + "]\n";
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
        Object val = Boolean.TRUE;
        try {
            if (isSet(getPosition(SSL_CLIENT)) || isSet(getPosition(S_MIME)) || isSet(getPosition(OBJECT_SIGNING))) {
                keyUsage.set(KeyUsageExtension.DIGITAL_SIGNATURE, val);
            }
            if (isSet(getPosition(SSL_SERVER))) {
                keyUsage.set(KeyUsageExtension.KEY_ENCIPHERMENT, val);
            }
            if (isSet(getPosition(SSL_CA)) || isSet(getPosition(S_MIME_CA)) || isSet(getPosition(OBJECT_SIGNING_CA))) {
                keyUsage.set(KeyUsageExtension.KEY_CERTSIGN, val);
            }
        } catch (IOException e) {
        }
        return keyUsage.getBits();
    }
}
