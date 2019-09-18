package org.bouncycastle.crypto;

public enum PasswordConverter implements CharToByteConverter {
    ASCII {
        public byte[] convert(char[] cArr) {
            return PBEParametersGenerator.PKCS5PasswordToBytes(cArr);
        }

        public String getType() {
            return "ASCII";
        }
    },
    UTF8 {
        public byte[] convert(char[] cArr) {
            return PBEParametersGenerator.PKCS5PasswordToUTF8Bytes(cArr);
        }

        public String getType() {
            return "UTF8";
        }
    },
    PKCS12 {
        public byte[] convert(char[] cArr) {
            return PBEParametersGenerator.PKCS12PasswordToBytes(cArr);
        }

        public String getType() {
            return "PKCS12";
        }
    }
}
