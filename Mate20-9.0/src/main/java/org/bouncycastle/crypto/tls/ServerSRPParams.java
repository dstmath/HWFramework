package org.bouncycastle.crypto.tls;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import org.bouncycastle.util.Arrays;

public class ServerSRPParams {
    protected BigInteger B;
    protected BigInteger N;
    protected BigInteger g;
    protected byte[] s;

    public ServerSRPParams(BigInteger bigInteger, BigInteger bigInteger2, byte[] bArr, BigInteger bigInteger3) {
        this.N = bigInteger;
        this.g = bigInteger2;
        this.s = Arrays.clone(bArr);
        this.B = bigInteger3;
    }

    public static ServerSRPParams parse(InputStream inputStream) throws IOException {
        return new ServerSRPParams(TlsSRPUtils.readSRPParameter(inputStream), TlsSRPUtils.readSRPParameter(inputStream), TlsUtils.readOpaque8(inputStream), TlsSRPUtils.readSRPParameter(inputStream));
    }

    public void encode(OutputStream outputStream) throws IOException {
        TlsSRPUtils.writeSRPParameter(this.N, outputStream);
        TlsSRPUtils.writeSRPParameter(this.g, outputStream);
        TlsUtils.writeOpaque8(this.s, outputStream);
        TlsSRPUtils.writeSRPParameter(this.B, outputStream);
    }

    public BigInteger getB() {
        return this.B;
    }

    public BigInteger getG() {
        return this.g;
    }

    public BigInteger getN() {
        return this.N;
    }

    public byte[] getS() {
        return this.s;
    }
}
