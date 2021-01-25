package ohos.data.dataability;

import ohos.rpc.IRemoteBroker;

public interface IRemoteDataAbility extends IRemoteBroker {
    public static final int BATCH_INSERT_TRANSACTION_ID = 2;
    public static final int CALL_TRANSACTION_ID = 3;
    public static final int DELETE_TRANSACTION_ID = 4;
    public static final int DENORMALIZE_URI_TRANSACTION_ID = 14;
    public static final int EXECUTE_BATCH_TRANSACTION_ID = 5;
    public static final int GET_FILE_TYPES_TRANSACTION_ID = 6;
    public static final int GET_TYPE_TRANSACTION_ID = 7;
    public static final int INSERT_TRANSACTION_ID = 8;
    public static final int NORMALIZE_URI_TRANSACTION_ID = 13;
    public static final int QUERY_TRANSACTION_ID = 9;
    public static final int REGISTER_OBSERVER_TRANSACTION_ID = 10;
    public static final int UNREGISTER_OBSERVER_TRANSACTION_ID = 11;
    public static final int UPDATE_TRANSACTION_ID = 12;
}
