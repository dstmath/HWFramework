package org.bouncycastle.crypto.examples;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.security.SecureRandom;
import org.bouncycastle.asn1.eac.CertificateHolderAuthorization;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.KeyGenerationParameters;
import org.bouncycastle.crypto.engines.DESedeEngine;
import org.bouncycastle.crypto.generators.DESedeKeyGenerator;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.encoders.Hex;

public class DESExample {
    private PaddedBufferedBlockCipher cipher = null;
    private boolean encrypt = true;
    private BufferedInputStream in = null;
    private byte[] key = null;
    private BufferedOutputStream out = null;

    public DESExample() {
    }

    public DESExample(String str, String str2, String str3, boolean z) {
        String str4;
        StringBuilder sb;
        PrintStream printStream;
        SecureRandom secureRandom;
        this.encrypt = z;
        try {
            this.in = new BufferedInputStream(new FileInputStream(str));
        } catch (FileNotFoundException e) {
            System.err.println("Input file not found [" + str + "]");
            System.exit(1);
        }
        try {
            this.out = new BufferedOutputStream(new FileOutputStream(str2));
        } catch (IOException e2) {
            System.err.println("Output file not created [" + str2 + "]");
            System.exit(1);
        }
        if (z) {
            try {
                secureRandom = new SecureRandom();
                try {
                    secureRandom.setSeed("www.bouncycastle.org".getBytes());
                } catch (Exception e3) {
                }
            } catch (Exception e4) {
                secureRandom = null;
                try {
                    System.err.println("Hmmm, no SHA1PRNG, you need the Sun implementation");
                    System.exit(1);
                    KeyGenerationParameters keyGenerationParameters = new KeyGenerationParameters(secureRandom, CertificateHolderAuthorization.CVCA);
                    DESedeKeyGenerator dESedeKeyGenerator = new DESedeKeyGenerator();
                    dESedeKeyGenerator.init(keyGenerationParameters);
                    this.key = dESedeKeyGenerator.generateKey();
                    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(str3));
                    byte[] encode = Hex.encode(this.key);
                    bufferedOutputStream.write(encode, 0, encode.length);
                    bufferedOutputStream.flush();
                    bufferedOutputStream.close();
                    return;
                } catch (IOException e5) {
                    printStream = System.err;
                    sb = new StringBuilder();
                    str4 = "Could not decryption create key file [";
                    sb.append(str4);
                    sb.append(str3);
                    sb.append("]");
                    printStream.println(sb.toString());
                    System.exit(1);
                    return;
                }
            }
            KeyGenerationParameters keyGenerationParameters2 = new KeyGenerationParameters(secureRandom, CertificateHolderAuthorization.CVCA);
            DESedeKeyGenerator dESedeKeyGenerator2 = new DESedeKeyGenerator();
            dESedeKeyGenerator2.init(keyGenerationParameters2);
            this.key = dESedeKeyGenerator2.generateKey();
            BufferedOutputStream bufferedOutputStream2 = new BufferedOutputStream(new FileOutputStream(str3));
            byte[] encode2 = Hex.encode(this.key);
            bufferedOutputStream2.write(encode2, 0, encode2.length);
            bufferedOutputStream2.flush();
            bufferedOutputStream2.close();
            return;
        }
        try {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(str3));
            int available = bufferedInputStream.available();
            byte[] bArr = new byte[available];
            bufferedInputStream.read(bArr, 0, available);
            this.key = Hex.decode(bArr);
        } catch (IOException e6) {
            printStream = System.err;
            sb = new StringBuilder();
            str4 = "Decryption key file not found, or not valid [";
        }
    }

    public static void main(String[] strArr) {
        String str;
        if (strArr.length < 2) {
            DESExample dESExample = new DESExample();
            System.err.println("Usage: java " + dESExample.getClass().getName() + " infile outfile [keyfile]");
            System.exit(1);
        }
        boolean z = false;
        String str2 = strArr[0];
        String str3 = strArr[1];
        if (strArr.length > 2) {
            str = strArr[2];
        } else {
            str = "deskey.dat";
            z = true;
        }
        new DESExample(str2, str3, str, z).process();
    }

    private void performDecrypt(byte[] bArr) {
        this.cipher.init(false, new KeyParameter(bArr));
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.in));
        byte[] bArr2 = null;
        while (true) {
            try {
                String readLine = bufferedReader.readLine();
                if (readLine != null) {
                    byte[] decode = Hex.decode(readLine);
                    bArr2 = new byte[this.cipher.getOutputSize(decode.length)];
                    int processBytes = this.cipher.processBytes(decode, 0, decode.length, bArr2, 0);
                    if (processBytes > 0) {
                        this.out.write(bArr2, 0, processBytes);
                    }
                } else {
                    try {
                        break;
                    } catch (CryptoException e) {
                        return;
                    }
                }
            } catch (IOException e2) {
                e2.printStackTrace();
                return;
            }
        }
        int doFinal = this.cipher.doFinal(bArr2, 0);
        if (doFinal > 0) {
            this.out.write(bArr2, 0, doFinal);
        }
    }

    private void performEncrypt(byte[] bArr) {
        this.cipher.init(true, new KeyParameter(bArr));
        byte[] bArr2 = new byte[47];
        byte[] bArr3 = new byte[this.cipher.getOutputSize(47)];
        while (true) {
            try {
                int read = this.in.read(bArr2, 0, 47);
                if (read > 0) {
                    int processBytes = this.cipher.processBytes(bArr2, 0, read, bArr3, 0);
                    if (processBytes > 0) {
                        byte[] encode = Hex.encode(bArr3, 0, processBytes);
                        this.out.write(encode, 0, encode.length);
                        this.out.write(10);
                    }
                } else {
                    try {
                        break;
                    } catch (CryptoException e) {
                        return;
                    }
                }
            } catch (IOException e2) {
                e2.printStackTrace();
                return;
            }
        }
        int doFinal = this.cipher.doFinal(bArr3, 0);
        if (doFinal > 0) {
            byte[] encode2 = Hex.encode(bArr3, 0, doFinal);
            this.out.write(encode2, 0, encode2.length);
            this.out.write(10);
        }
    }

    private void process() {
        this.cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new DESedeEngine()));
        if (this.encrypt) {
            performEncrypt(this.key);
        } else {
            performDecrypt(this.key);
        }
        try {
            this.in.close();
            this.out.flush();
            this.out.close();
        } catch (IOException e) {
            PrintStream printStream = System.err;
            printStream.println("exception closing resources: " + e.getMessage());
        }
    }
}
