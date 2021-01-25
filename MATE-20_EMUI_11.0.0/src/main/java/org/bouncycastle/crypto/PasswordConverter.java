package org.bouncycastle.crypto;

public enum PasswordConverter implements CharToByteConverter {
    ASCII {
        @Override // org.bouncycastle.crypto.CharToByteConverter
        public byte[] convert(char[] cArr) {
            return PBEParametersGenerator.PKCS5PasswordToBytes(cArr);
        }

        @Override // org.bouncycastle.crypto.CharToByteConverter
        public String getType() {
            return "ASCII";
        }
    },
    UTF8 {
        @Override // org.bouncycastle.crypto.CharToByteConverter
        public byte[] convert(char[] cArr) {
            return PBEParametersGenerator.PKCS5PasswordToUTF8Bytes(cArr);
        }

        @Override // org.bouncycastle.crypto.CharToByteConverter
        public String getType() {
            return "UTF8";
        }
    },
    PKCS12 {
        @Override // org.bouncycastle.crypto.CharToByteConverter
        public byte[] convert(char[] cArr) {
            return PBEParametersGenerator.PKCS12PasswordToBytes(cArr);
        }

        @Override // org.bouncycastle.crypto.CharToByteConverter
        public String getType() {
            return "PKCS12";
        }
    }
}
