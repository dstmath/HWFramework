package org.bouncycastle.crypto.tls;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import org.bouncycastle.crypto.params.DHParameters;
import org.bouncycastle.crypto.params.DHPublicKeyParameters;

public class ServerDHParams {
    protected DHPublicKeyParameters publicKey;

    public ServerDHParams(DHPublicKeyParameters dHPublicKeyParameters) {
        if (dHPublicKeyParameters != null) {
            this.publicKey = dHPublicKeyParameters;
            return;
        }
        throw new IllegalArgumentException("'publicKey' cannot be null");
    }

    public static ServerDHParams parse(InputStream inputStream) throws IOException {
        return new ServerDHParams(TlsDHUtils.validateDHPublicKey(new DHPublicKeyParameters(TlsDHUtils.readDHParameter(inputStream), new DHParameters(TlsDHUtils.readDHParameter(inputStream), TlsDHUtils.readDHParameter(inputStream)))));
    }

    public void encode(OutputStream outputStream) throws IOException {
        DHParameters parameters = this.publicKey.getParameters();
        BigInteger y = this.publicKey.getY();
        TlsDHUtils.writeDHParameter(parameters.getP(), outputStream);
        TlsDHUtils.writeDHParameter(parameters.getG(), outputStream);
        TlsDHUtils.writeDHParameter(y, outputStream);
    }

    public DHPublicKeyParameters getPublicKey() {
        return this.publicKey;
    }
}
