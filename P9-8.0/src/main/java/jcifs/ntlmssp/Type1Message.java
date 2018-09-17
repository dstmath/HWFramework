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

    public Type1Message(int flags, String suppliedDomain, String suppliedWorkstation) {
        setFlags(getDefaultFlags() | flags);
        setSuppliedDomain(suppliedDomain);
        if (suppliedWorkstation == null) {
            suppliedWorkstation = getDefaultWorkstation();
        }
        setSuppliedWorkstation(suppliedWorkstation);
    }

    public Type1Message(byte[] material) throws IOException {
        parse(material);
    }

    public String getSuppliedDomain() {
        return this.suppliedDomain;
    }

    public void setSuppliedDomain(String suppliedDomain) {
        this.suppliedDomain = suppliedDomain;
    }

    public String getSuppliedWorkstation() {
        return this.suppliedWorkstation;
    }

    public void setSuppliedWorkstation(String suppliedWorkstation) {
        this.suppliedWorkstation = suppliedWorkstation;
    }

    public byte[] toByteArray() {
        int i = 16;
        try {
            String suppliedDomain = getSuppliedDomain();
            String suppliedWorkstation = getSuppliedWorkstation();
            int flags = getFlags();
            boolean hostInfo = false;
            byte[] domain = new byte[0];
            if (suppliedDomain == null || suppliedDomain.length() == 0) {
                flags &= -4097;
            } else {
                hostInfo = true;
                flags |= 4096;
                domain = suppliedDomain.toUpperCase().getBytes(NtlmMessage.getOEMEncoding());
            }
            byte[] workstation = new byte[0];
            if (suppliedWorkstation == null || suppliedWorkstation.length() == 0) {
                flags &= -8193;
            } else {
                hostInfo = true;
                flags |= 8192;
                workstation = suppliedWorkstation.toUpperCase().getBytes(NtlmMessage.getOEMEncoding());
            }
            if (hostInfo) {
                i = (domain.length + 32) + workstation.length;
            }
            byte[] type1 = new byte[i];
            System.arraycopy(NTLMSSP_SIGNATURE, 0, type1, 0, 8);
            NtlmMessage.writeULong(type1, 8, 1);
            NtlmMessage.writeULong(type1, 12, flags);
            if (hostInfo) {
                NtlmMessage.writeSecurityBuffer(type1, 16, 32, domain);
                NtlmMessage.writeSecurityBuffer(type1, 24, domain.length + 32, workstation);
            }
            return type1;
        } catch (IOException ex) {
            throw new IllegalStateException(ex.getMessage());
        }
    }

    public String toString() {
        String suppliedDomain = getSuppliedDomain();
        String suppliedWorkstation = getSuppliedWorkstation();
        StringBuilder append = new StringBuilder().append("Type1Message[suppliedDomain=");
        if (suppliedDomain == null) {
            suppliedDomain = "null";
        }
        append = append.append(suppliedDomain).append(",suppliedWorkstation=");
        if (suppliedWorkstation == null) {
            suppliedWorkstation = "null";
        }
        return append.append(suppliedWorkstation).append(",flags=0x").append(Hexdump.toHexString(getFlags(), 8)).append("]").toString();
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
        if (NtlmMessage.readULong(material, 8) != 1) {
            throw new IOException("Not a Type 1 message.");
        }
        int flags = NtlmMessage.readULong(material, 12);
        String suppliedDomain = null;
        if ((flags & 4096) != 0) {
            suppliedDomain = new String(NtlmMessage.readSecurityBuffer(material, 16), NtlmMessage.getOEMEncoding());
        }
        String suppliedWorkstation = null;
        if ((flags & 8192) != 0) {
            suppliedWorkstation = new String(NtlmMessage.readSecurityBuffer(material, 24), NtlmMessage.getOEMEncoding());
        }
        setFlags(flags);
        setSuppliedDomain(suppliedDomain);
        setSuppliedWorkstation(suppliedWorkstation);
    }
}
