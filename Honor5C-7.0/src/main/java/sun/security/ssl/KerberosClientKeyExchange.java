package sun.security.ssl;

import java.io.IOException;
import java.io.PrintStream;
import java.security.AccessControlContext;
import java.security.Principal;
import java.security.SecureRandom;
import javax.crypto.SecretKey;

public class KerberosClientKeyExchange extends HandshakeMessage {
    private static final String IMPL_CLASS = "sun.security.ssl.krb5.KerberosClientKeyExchangeImpl";
    private static final Class<?> implClass = null;
    private final KerberosClientKeyExchange impl;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.ssl.KerberosClientKeyExchange.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.ssl.KerberosClientKeyExchange.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.security.ssl.KerberosClientKeyExchange.<clinit>():void");
    }

    private KerberosClientKeyExchange createImpl() {
        if (getClass() != KerberosClientKeyExchange.class) {
            return null;
        }
        try {
            return (KerberosClientKeyExchange) implClass.newInstance();
        } catch (Object e) {
            throw new AssertionError(e);
        } catch (Object e2) {
            throw new AssertionError(e2);
        }
    }

    public KerberosClientKeyExchange() {
        this.impl = createImpl();
    }

    public KerberosClientKeyExchange(String serverName, boolean isLoopback, AccessControlContext acc, ProtocolVersion protocolVersion, SecureRandom rand) throws IOException {
        this.impl = createImpl();
        if (this.impl != null) {
            init(serverName, isLoopback, acc, protocolVersion, rand);
            return;
        }
        throw new IllegalStateException("Kerberos is unavailable");
    }

    public KerberosClientKeyExchange(ProtocolVersion protocolVersion, ProtocolVersion clientVersion, SecureRandom rand, HandshakeInStream input, SecretKey[] serverKeys) throws IOException {
        this.impl = createImpl();
        if (this.impl != null) {
            init(protocolVersion, clientVersion, rand, input, serverKeys);
            return;
        }
        throw new IllegalStateException("Kerberos is unavailable");
    }

    int messageType() {
        return 16;
    }

    public int messageLength() {
        return this.impl.messageLength();
    }

    public void send(HandshakeOutStream s) throws IOException {
        this.impl.send(s);
    }

    public void print(PrintStream p) throws IOException {
        this.impl.print(p);
    }

    public void init(String serverName, boolean isLoopback, AccessControlContext acc, ProtocolVersion protocolVersion, SecureRandom rand) throws IOException {
        if (this.impl != null) {
            this.impl.init(serverName, isLoopback, acc, protocolVersion, rand);
        }
    }

    public void init(ProtocolVersion protocolVersion, ProtocolVersion clientVersion, SecureRandom rand, HandshakeInStream input, SecretKey[] serverKeys) throws IOException {
        if (this.impl != null) {
            this.impl.init(protocolVersion, clientVersion, rand, input, serverKeys);
        }
    }

    public byte[] getUnencryptedPreMasterSecret() {
        return this.impl.getUnencryptedPreMasterSecret();
    }

    public Principal getPeerPrincipal() {
        return this.impl.getPeerPrincipal();
    }

    public Principal getLocalPrincipal() {
        return this.impl.getLocalPrincipal();
    }
}
