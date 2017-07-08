package jcifs.ntlmssp;

import java.io.IOException;
import jcifs.util.Hexdump;

public class Type1Message extends NtlmMessage {
    private static final String DEFAULT_DOMAIN = null;
    private static final int DEFAULT_FLAGS = 0;
    private static final String DEFAULT_WORKSTATION = null;
    private String suppliedDomain;
    private String suppliedWorkstation;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: jcifs.ntlmssp.Type1Message.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: jcifs.ntlmssp.Type1Message.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: jcifs.ntlmssp.Type1Message.<clinit>():void");
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
                flags |= SmbConstants.FLAGS2_RESOLVE_PATHS_IN_DFS;
                domain = suppliedDomain.toUpperCase().getBytes(NtlmMessage.getOEMEncoding());
            }
            byte[] workstation = new byte[0];
            if (suppliedWorkstation == null || suppliedWorkstation.length() == 0) {
                flags &= -8193;
            } else {
                hostInfo = true;
                flags |= SmbConstants.FLAGS2_PERMIT_READ_IF_EXECUTE_PERM;
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
        if ((flags & SmbConstants.FLAGS2_RESOLVE_PATHS_IN_DFS) != 0) {
            suppliedDomain = new String(NtlmMessage.readSecurityBuffer(material, 16), NtlmMessage.getOEMEncoding());
        }
        String suppliedWorkstation = null;
        if ((flags & SmbConstants.FLAGS2_PERMIT_READ_IF_EXECUTE_PERM) != 0) {
            suppliedWorkstation = new String(NtlmMessage.readSecurityBuffer(material, 24), NtlmMessage.getOEMEncoding());
        }
        setFlags(flags);
        setSuppliedDomain(suppliedDomain);
        setSuppliedWorkstation(suppliedWorkstation);
    }
}
