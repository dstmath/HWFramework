package ohos.data.rdb.impl;

import java.util.Arrays;
import ohos.app.Context;
import ohos.data.utils.SignManager;

public class SqliteEncryptKeyLoader {
    private byte[] encryptKey;

    private SqliteEncryptKeyLoader(byte[] bArr) {
        if (bArr != null) {
            this.encryptKey = Arrays.copyOf(bArr, bArr.length);
        }
    }

    public SqliteEncryptKeyLoader(SqliteEncryptKeyLoader sqliteEncryptKeyLoader) {
        if (sqliteEncryptKeyLoader != null) {
            this.encryptKey = sqliteEncryptKeyLoader.getEncryptKey();
        }
    }

    public static SqliteEncryptKeyLoader generate(Context context, byte[] bArr) {
        if (bArr == null || bArr.length == 0) {
            return new SqliteEncryptKeyLoader(bArr);
        }
        byte[] signEncryptKey = SignManager.signEncryptKey(context, bArr);
        SqliteEncryptKeyLoader sqliteEncryptKeyLoader = new SqliteEncryptKeyLoader(signEncryptKey);
        Arrays.fill(signEncryptKey, (byte) 0);
        return sqliteEncryptKeyLoader;
    }

    public byte[] getEncryptKey() {
        byte[] bArr = this.encryptKey;
        if (bArr != null) {
            return Arrays.copyOf(bArr, bArr.length);
        }
        return null;
    }

    public boolean isEmpty() {
        byte[] bArr = this.encryptKey;
        return bArr == null || bArr.length == 0;
    }

    public void destroy() {
        byte[] bArr = this.encryptKey;
        if (bArr != null) {
            Arrays.fill(bArr, (byte) 0);
            this.encryptKey = null;
        }
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return Arrays.equals(this.encryptKey, ((SqliteEncryptKeyLoader) obj).encryptKey);
    }

    public int hashCode() {
        return Arrays.hashCode(this.encryptKey);
    }
}
