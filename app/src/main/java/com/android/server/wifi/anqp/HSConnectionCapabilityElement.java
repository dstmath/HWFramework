package com.android.server.wifi.anqp;

import com.android.server.wifi.anqp.Constants.ANQPElementType;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HSConnectionCapabilityElement extends ANQPElement {
    private final List<ProtocolTuple> mStatusList;

    public enum ProtoStatus {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.anqp.HSConnectionCapabilityElement.ProtoStatus.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.anqp.HSConnectionCapabilityElement.ProtoStatus.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.anqp.HSConnectionCapabilityElement.ProtoStatus.<clinit>():void");
        }
    }

    public static class ProtocolTuple {
        private final int mPort;
        private final int mProtocol;
        private final ProtoStatus mStatus;

        /* synthetic */ ProtocolTuple(ByteBuffer payload, ProtocolTuple protocolTuple) {
            this(payload);
        }

        private ProtocolTuple(ByteBuffer payload) throws ProtocolException {
            if (payload.remaining() < 4) {
                throw new ProtocolException("Runt protocol tuple: " + payload.remaining());
            }
            ProtoStatus protoStatus;
            this.mProtocol = payload.get() & Constants.BYTE_MASK;
            this.mPort = payload.getShort() & Constants.SHORT_MASK;
            int statusNumber = payload.get() & Constants.BYTE_MASK;
            if (statusNumber < ProtoStatus.values().length) {
                protoStatus = ProtoStatus.values()[statusNumber];
            } else {
                protoStatus = null;
            }
            this.mStatus = protoStatus;
        }

        public int getProtocol() {
            return this.mProtocol;
        }

        public int getPort() {
            return this.mPort;
        }

        public ProtoStatus getStatus() {
            return this.mStatus;
        }

        public String toString() {
            return "ProtocolTuple{mProtocol=" + this.mProtocol + ", mPort=" + this.mPort + ", mStatus=" + this.mStatus + '}';
        }
    }

    public HSConnectionCapabilityElement(ANQPElementType infoID, ByteBuffer payload) throws ProtocolException {
        super(infoID);
        this.mStatusList = new ArrayList();
        while (payload.hasRemaining()) {
            this.mStatusList.add(new ProtocolTuple(payload, null));
        }
    }

    public List<ProtocolTuple> getStatusList() {
        return Collections.unmodifiableList(this.mStatusList);
    }

    public String toString() {
        return "HSConnectionCapability{mStatusList=" + this.mStatusList + '}';
    }
}
