package sun.security.ssl;

import java.io.IOException;
import java.io.OutputStream;
import sun.security.util.DerValue;

public class HandshakeOutStream extends OutputStream {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private SSLEngineImpl engine;
    OutputRecord r;
    private SSLSocketImpl socket;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.ssl.HandshakeOutStream.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.ssl.HandshakeOutStream.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: sun.security.ssl.HandshakeOutStream.<clinit>():void");
    }

    HandshakeOutStream(ProtocolVersion protocolVersion, ProtocolVersion helloVersion, HandshakeHash handshakeHash, SSLSocketImpl socket) {
        this.socket = socket;
        this.r = new OutputRecord(DerValue.tag_IA5String);
        init(protocolVersion, helloVersion, handshakeHash);
    }

    HandshakeOutStream(ProtocolVersion protocolVersion, ProtocolVersion helloVersion, HandshakeHash handshakeHash, SSLEngineImpl engine) {
        this.engine = engine;
        this.r = new EngineOutputRecord(DerValue.tag_IA5String, engine);
        init(protocolVersion, helloVersion, handshakeHash);
    }

    private void init(ProtocolVersion protocolVersion, ProtocolVersion helloVersion, HandshakeHash handshakeHash) {
        this.r.setVersion(protocolVersion);
        this.r.setHelloVersion(helloVersion);
        this.r.setHandshakeHash(handshakeHash);
    }

    void doHashes() {
        this.r.doHashes();
    }

    public void write(byte[] buf, int off, int len) throws IOException {
        while (len > 0) {
            int howmuch = Math.min(len, this.r.availableDataBytes());
            if (howmuch == 0) {
                flush();
            } else {
                this.r.write(buf, off, howmuch);
                off += howmuch;
                len -= howmuch;
            }
        }
    }

    public void write(int i) throws IOException {
        if (this.r.availableDataBytes() < 1) {
            flush();
        }
        this.r.write(i);
    }

    public void flush() throws IOException {
        if (this.socket != null) {
            try {
                this.socket.writeRecord(this.r);
                return;
            } catch (IOException e) {
                this.socket.waitForClose(true);
                throw e;
            }
        }
        this.engine.writeRecord((EngineOutputRecord) this.r);
    }

    void setFinishedMsg() {
        if (!-assertionsDisabled) {
            if ((this.socket == null ? 1 : null) == null) {
                throw new AssertionError();
            }
        }
        ((EngineOutputRecord) this.r).setFinishedMsg();
    }

    void putInt8(int i) throws IOException {
        checkOverflow(i, Record.maxPadding);
        this.r.write(i);
    }

    void putInt16(int i) throws IOException {
        checkOverflow(i, Record.OVERFLOW_OF_INT16);
        if (this.r.availableDataBytes() < 2) {
            flush();
        }
        this.r.write(i >> 8);
        this.r.write(i);
    }

    void putInt24(int i) throws IOException {
        checkOverflow(i, Record.OVERFLOW_OF_INT24);
        if (this.r.availableDataBytes() < 3) {
            flush();
        }
        this.r.write(i >> 16);
        this.r.write(i >> 8);
        this.r.write(i);
    }

    void putInt32(int i) throws IOException {
        if (this.r.availableDataBytes() < 4) {
            flush();
        }
        this.r.write(i >> 24);
        this.r.write(i >> 16);
        this.r.write(i >> 8);
        this.r.write(i);
    }

    void putBytes8(byte[] b) throws IOException {
        if (b == null) {
            putInt8(0);
            return;
        }
        checkOverflow(b.length, Record.maxPadding);
        putInt8(b.length);
        write(b, 0, b.length);
    }

    public void putBytes16(byte[] b) throws IOException {
        if (b == null) {
            putInt16(0);
            return;
        }
        checkOverflow(b.length, Record.OVERFLOW_OF_INT16);
        putInt16(b.length);
        write(b, 0, b.length);
    }

    void putBytes24(byte[] b) throws IOException {
        if (b == null) {
            putInt24(0);
            return;
        }
        checkOverflow(b.length, Record.OVERFLOW_OF_INT24);
        putInt24(b.length);
        write(b, 0, b.length);
    }

    private void checkOverflow(int length, int overflow) {
        if (length >= overflow) {
            throw new RuntimeException("Field length overflow, the field length (" + length + ") should be less than " + overflow);
        }
    }
}
