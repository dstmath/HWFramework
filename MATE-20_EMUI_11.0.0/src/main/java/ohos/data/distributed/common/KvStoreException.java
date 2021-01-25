package ohos.data.distributed.common;

public class KvStoreException extends RuntimeException {
    private static final long serialVersionUID = 7886655667971879440L;
    private KvStoreErrorCode kvStoreErrorCode;

    public KvStoreException(KvStoreErrorCode kvStoreErrorCode2) {
        this.kvStoreErrorCode = kvStoreErrorCode2;
    }

    public KvStoreException(KvStoreErrorCode kvStoreErrorCode2, String str, Throwable th) {
        super(str, th);
        this.kvStoreErrorCode = kvStoreErrorCode2;
    }

    public KvStoreException(KvStoreErrorCode kvStoreErrorCode2, String str) {
        super(str);
        this.kvStoreErrorCode = kvStoreErrorCode2;
    }

    public KvStoreException(KvStoreErrorCode kvStoreErrorCode2, Throwable th) {
        super(th);
        this.kvStoreErrorCode = kvStoreErrorCode2;
    }

    public KvStoreErrorCode getKvStoreErrorCode() {
        return this.kvStoreErrorCode;
    }
}
