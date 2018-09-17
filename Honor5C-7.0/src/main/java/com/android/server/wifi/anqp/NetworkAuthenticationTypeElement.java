package com.android.server.wifi.anqp;

import com.android.server.wifi.anqp.Constants.ANQPElementType;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NetworkAuthenticationTypeElement extends ANQPElement {
    private final List<NetworkAuthentication> m_authenticationTypes;

    public static class NetworkAuthentication {
        private final NwkAuthTypeEnum m_type;
        private final String m_url;

        private NetworkAuthentication(NwkAuthTypeEnum type, String url) {
            this.m_type = type;
            this.m_url = url;
        }

        public NwkAuthTypeEnum getType() {
            return this.m_type;
        }

        public String getURL() {
            return this.m_url;
        }

        public String toString() {
            return "NetworkAuthentication{m_type=" + this.m_type + ", m_url='" + this.m_url + '\'' + '}';
        }
    }

    public enum NwkAuthTypeEnum {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.anqp.NetworkAuthenticationTypeElement.NwkAuthTypeEnum.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.anqp.NetworkAuthenticationTypeElement.NwkAuthTypeEnum.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.anqp.NetworkAuthenticationTypeElement.NwkAuthTypeEnum.<clinit>():void");
        }
    }

    public NetworkAuthenticationTypeElement(ANQPElementType infoID, ByteBuffer payload) throws ProtocolException {
        super(infoID);
        this.m_authenticationTypes = new ArrayList();
        while (payload.hasRemaining()) {
            NwkAuthTypeEnum type;
            int typeNumber = payload.get() & Constants.BYTE_MASK;
            if (typeNumber >= NwkAuthTypeEnum.values().length) {
                type = NwkAuthTypeEnum.Reserved;
            } else {
                type = NwkAuthTypeEnum.values()[typeNumber];
            }
            this.m_authenticationTypes.add(new NetworkAuthentication(Constants.getPrefixedString(payload, 2, StandardCharsets.UTF_8), null));
        }
    }

    public List<NetworkAuthentication> getAuthenticationTypes() {
        return Collections.unmodifiableList(this.m_authenticationTypes);
    }

    public String toString() {
        return "NetworkAuthenticationType{m_authenticationTypes=" + this.m_authenticationTypes + '}';
    }
}
