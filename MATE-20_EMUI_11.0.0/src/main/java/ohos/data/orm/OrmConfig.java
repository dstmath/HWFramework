package ohos.data.orm;

public class OrmConfig {
    public static final int MAX_ENCRYPT_KEY_SIZE = 10485760;
    private String alias;
    private byte[] encryptKey;
    private String name;

    private OrmConfig(Builder builder) {
        this.alias = builder.alias;
        this.name = builder.name;
        this.encryptKey = builder.encryptKey;
    }

    public String getAlias() {
        return this.alias;
    }

    public String getName() {
        return this.name;
    }

    public byte[] getEncryptKey() {
        return this.encryptKey;
    }

    public static final class Builder {
        private String alias;
        private byte[] encryptKey;
        private String name;

        public Builder() {
        }

        public Builder(OrmConfig ormConfig) {
            this.alias = ormConfig.alias;
            this.name = ormConfig.name;
            this.encryptKey = ormConfig.encryptKey;
        }

        public Builder setAlias(String str) {
            this.alias = str;
            return this;
        }

        public Builder setName(String str) {
            this.name = str;
            return this;
        }

        public Builder setEncryptKey(byte[] bArr) {
            if (bArr == null || bArr.length <= 10485760) {
                this.encryptKey = bArr;
                return this;
            }
            throw new IllegalArgumentException("Encrypt Key size exceeds maximum limit.");
        }

        public OrmConfig build() {
            return new OrmConfig(this);
        }
    }
}
