package org.bouncycastle.jcajce.spec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.security.spec.AlgorithmParameterSpec;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Integers;

public class SkeinParameterSpec implements AlgorithmParameterSpec {
    public static final int PARAM_TYPE_CONFIG = 4;
    public static final int PARAM_TYPE_KEY = 0;
    public static final int PARAM_TYPE_KEY_IDENTIFIER = 16;
    public static final int PARAM_TYPE_MESSAGE = 48;
    public static final int PARAM_TYPE_NONCE = 20;
    public static final int PARAM_TYPE_OUTPUT = 63;
    public static final int PARAM_TYPE_PERSONALISATION = 8;
    public static final int PARAM_TYPE_PUBLIC_KEY = 12;
    /* access modifiers changed from: private */
    public Map parameters;

    public static class Builder {
        private Map parameters = new HashMap();

        public Builder() {
        }

        public Builder(SkeinParameterSpec skeinParameterSpec) {
            for (Integer num : skeinParameterSpec.parameters.keySet()) {
                this.parameters.put(num, skeinParameterSpec.parameters.get(num));
            }
        }

        public SkeinParameterSpec build() {
            return new SkeinParameterSpec(this.parameters);
        }

        public Builder set(int i, byte[] bArr) {
            if (bArr == null) {
                throw new IllegalArgumentException("Parameter value must not be null.");
            } else if (i != 0 && (i <= 4 || i >= 63 || i == 48)) {
                throw new IllegalArgumentException("Parameter types must be in the range 0,5..47,49..62.");
            } else if (i != 4) {
                this.parameters.put(Integers.valueOf(i), bArr);
                return this;
            } else {
                throw new IllegalArgumentException("Parameter type 4 is reserved for internal use.");
            }
        }

        public Builder setKey(byte[] bArr) {
            return set(0, bArr);
        }

        public Builder setKeyIdentifier(byte[] bArr) {
            return set(16, bArr);
        }

        public Builder setNonce(byte[] bArr) {
            return set(20, bArr);
        }

        public Builder setPersonalisation(Date date, String str, String str2) {
            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(byteArrayOutputStream, "UTF-8");
                outputStreamWriter.write(new SimpleDateFormat("YYYYMMDD").format(date));
                outputStreamWriter.write(" ");
                outputStreamWriter.write(str);
                outputStreamWriter.write(" ");
                outputStreamWriter.write(str2);
                outputStreamWriter.close();
                return set(8, byteArrayOutputStream.toByteArray());
            } catch (IOException e) {
                throw new IllegalStateException("Byte I/O failed: " + e);
            }
        }

        public Builder setPersonalisation(Date date, Locale locale, String str, String str2) {
            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(byteArrayOutputStream, "UTF-8");
                outputStreamWriter.write(new SimpleDateFormat("YYYYMMDD", locale).format(date));
                outputStreamWriter.write(" ");
                outputStreamWriter.write(str);
                outputStreamWriter.write(" ");
                outputStreamWriter.write(str2);
                outputStreamWriter.close();
                return set(8, byteArrayOutputStream.toByteArray());
            } catch (IOException e) {
                throw new IllegalStateException("Byte I/O failed: " + e);
            }
        }

        public Builder setPersonalisation(byte[] bArr) {
            return set(8, bArr);
        }

        public Builder setPublicKey(byte[] bArr) {
            return set(12, bArr);
        }
    }

    public SkeinParameterSpec() {
        this(new HashMap());
    }

    private SkeinParameterSpec(Map map) {
        this.parameters = Collections.unmodifiableMap(map);
    }

    public byte[] getKey() {
        return Arrays.clone((byte[]) this.parameters.get(Integers.valueOf(0)));
    }

    public byte[] getKeyIdentifier() {
        return Arrays.clone((byte[]) this.parameters.get(Integers.valueOf(16)));
    }

    public byte[] getNonce() {
        return Arrays.clone((byte[]) this.parameters.get(Integers.valueOf(20)));
    }

    public Map getParameters() {
        return this.parameters;
    }

    public byte[] getPersonalisation() {
        return Arrays.clone((byte[]) this.parameters.get(Integers.valueOf(8)));
    }

    public byte[] getPublicKey() {
        return Arrays.clone((byte[]) this.parameters.get(Integers.valueOf(12)));
    }
}
