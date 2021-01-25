package jcifs.ntlmssp;

import java.io.IOException;
import java.net.UnknownHostException;
import jcifs.Config;
import jcifs.netbios.NbtAddress;
import jcifs.util.Hexdump;

public class Type1Message extends NtlmMessage {
    private static final String DEFAULT_DOMAIN = Config.getProperty("jcifs.smb.client.domain", null);
    private static final int DEFAULT_FLAGS;
    private static final String DEFAULT_WORKSTATION;
    private String suppliedDomain;
    private String suppliedWorkstation;

    static {
        int i = 1;
        if (!Config.getBoolean("jcifs.smb.client.useUnicode", true)) {
            i = 2;
        }
        DEFAULT_FLAGS = i | 512;
        String defaultWorkstation = null;
        try {
            defaultWorkstation = NbtAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
        }
        DEFAULT_WORKSTATION = defaultWorkstation;
    }

    public Type1Message() {
        this(getDefaultFlags(), getDefaultDomain(), getDefaultWorkstation());
    }

    public Type1Message(int flags, String suppliedDomain2, String suppliedWorkstation2) {
        setFlags(getDefaultFlags() | flags);
        setSuppliedDomain(suppliedDomain2);
        setSuppliedWorkstation(suppliedWorkstation2 == null ? getDefaultWorkstation() : suppliedWorkstation2);
    }

    public Type1Message(byte[] material) throws IOException {
        parse(material);
    }

    public String getSuppliedDomain() {
        return this.suppliedDomain;
    }

    public void setSuppliedDomain(String suppliedDomain2) {
        this.suppliedDomain = suppliedDomain2;
    }

    public String getSuppliedWorkstation() {
        return this.suppliedWorkstation;
    }

    public void setSuppliedWorkstation(String suppliedWorkstation2) {
        this.suppliedWorkstation = suppliedWorkstation2;
    }

    @Override // jcifs.ntlmssp.NtlmMessage
    public byte[] toByteArray() {
        int flags;
        int flags2;
        int i = 16;
        try {
            String suppliedDomain2 = getSuppliedDomain();
            String suppliedWorkstation2 = getSuppliedWorkstation();
            int flags3 = getFlags();
            boolean hostInfo = false;
            byte[] domain = new byte[0];
            if (suppliedDomain2 == null || suppliedDomain2.length() == 0) {
                flags = flags3 & -4097;
            } else {
                hostInfo = true;
                flags = flags3 | 4096;
                domain = suppliedDomain2.toUpperCase().getBytes(getOEMEncoding());
            }
            byte[] workstation = new byte[0];
            if (suppliedWorkstation2 == null || suppliedWorkstation2.length() == 0) {
                flags2 = flags & -8193;
            } else {
                hostInfo = true;
                flags2 = flags | 8192;
                workstation = suppliedWorkstation2.toUpperCase().getBytes(getOEMEncoding());
            }
            if (hostInfo) {
                i = domain.length + 32 + workstation.length;
            }
            byte[] type1 = new byte[i];
            System.arraycopy(NTLMSSP_SIGNATURE, 0, type1, 0, 8);
            writeULong(type1, 8, 1);
            writeULong(type1, 12, flags2);
            if (hostInfo) {
                writeSecurityBuffer(type1, 16, 32, domain);
                writeSecurityBuffer(type1, 24, domain.length + 32, workstation);
            }
            return type1;
        } catch (IOException ex) {
            throw new IllegalStateException(ex.getMessage());
        }
    }

    public String toString() {
        String suppliedDomain2 = getSuppliedDomain();
        String suppliedWorkstation2 = getSuppliedWorkstation();
        StringBuilder append = new StringBuilder().append("Type1Message[suppliedDomain=");
        if (suppliedDomain2 == null) {
            suppliedDomain2 = "null";
        }
        StringBuilder append2 = append.append(suppliedDomain2).append(",suppliedWorkstation=");
        if (suppliedWorkstation2 == null) {
            suppliedWorkstation2 = "null";
        }
        return append2.append(suppliedWorkstation2).append(",flags=0x").append(Hexdump.toHexString(getFlags(), 8)).append("]").toString();
    }

    public static int getDefaultFlags() {
        return DEFAULT_FLAGS;
    }

    public static String getDefaultDomain() {
        return DEFAULT_DOMAIN;
    }

    public static String getDefaultWorkstation() {
        return DEFAULT_WORKSTATION;
    }

    private void parse(byte[] material) throws IOException {
        for (int i = 0; i < 8; i++) {
            if (material[i] != NTLMSSP_SIGNATURE[i]) {
                throw new IOException("Not an NTLMSSP message.");
            }
        }
        if (readULong(material, 8) != 1) {
            throw new IOException("Not a Type 1 message.");
        }
        int flags = readULong(material, 12);
        String suppliedDomain2 = null;
        if ((flags & 4096) != 0) {
            suppliedDomain2 = new String(readSecurityBuffer(material, 16), getOEMEncoding());
        }
        String suppliedWorkstation2 = null;
        if ((flags & 8192) != 0) {
            suppliedWorkstation2 = new String(readSecurityBuffer(material, 24), getOEMEncoding());
        }
        setFlags(flags);
        setSuppliedDomain(suppliedDomain2);
        setSuppliedWorkstation(suppliedWorkstation2);
    }
}
