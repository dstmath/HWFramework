package ohos.account;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import ohos.account.adapter.AccountManagerAdapter;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.media.image.PixelMap;
import ohos.os.ProcessManager;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.sysability.samgr.SysAbilityManager;

public class AccountProxy implements IRemoteBroker {
    private static final AccountManagerAdapter ACCOUNT_ADAPTER = new AccountManagerAdapter();
    private static final AccountProxy ACCOUNT_PROXY_INSTANCE = new AccountProxy();
    private static final int CMD_ACTIVATE_LOCAL_ACCOUNT = 111;
    private static final int CMD_CREATE_LOCAL_ACCOUNT = 101;
    private static final int CMD_GET_ACCOUNT_ALL_CONSTRAINTS = 115;
    private static final int CMD_IS_ACCOUNT_CONSTRAINT_ENABLE = 116;
    private static final int CMD_IS_ACTIVE_STATUS_BY_ID = 120;
    private static final int CMD_IS_KING_KONG = 125;
    private static final int CMD_IS_SUPPORT_MULT_ACCOUNT = 100;
    private static final int CMD_IS_VERIFIED_STATUS = 118;
    private static final int CMD_IS_VERIFIED_STATUS_BY_ID = 119;
    private static final int CMD_QUERY_ACCOUNT_DISTRIBUTED_INFO = 2;
    private static final int CMD_QUERY_ALL_LOCAL_ACCOUNT = 108;
    private static final int CMD_QUERY_CURRENT_LOCAL_ACCOUNT = 107;
    private static final int CMD_QUERY_LOCAL_ACCOUNT_BY_ID = 106;
    private static final int CMD_QUERY_LOCAL_ACCOUNT_ID_FROM_UID = 105;
    private static final int CMD_QUERY_LOCAL_ID_FROM_PROCESS = 123;
    private static final int CMD_QUERY_MAX_SUPPORTED_NUMBER = 117;
    private static final int CMD_QUERY_TYPE_FROM_PROCESS = 124;
    private static final int CMD_REMOVE_LOCAL_ACCOUNT = 102;
    private static final int CMD_SET_ACCOUNT_CONSTRAINTS = 113;
    private static final int CMD_SET_ACCOUNT_LOCAL_NAME = 112;
    private static final int CMD_UPDATE_ACCOUNT_DISTRIBUTED_INFO = 1;
    private static final String DESCRIPTOR = "OHOS.AccountSA.IAccount";
    private static final OsAccount FAIL_ACCOUNT = null;
    private static final DistributedInfo FAIL_DISTRIBUTED_INFO = null;
    private static final int INVALID_ACCOUNT_VALUE = -1;
    private static final int INVALID_LOCAL_ACCOUNT_ID = -1;
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) LOG_DOMAIN, TAG);
    private static final int LOG_DOMAIN = 218110720;
    private static final MarshallInterface MARSHALL_STUB_FUNC = $$Lambda$AccountProxy$wgF8mk7FpPqyT5ViCtufND6jKw.INSTANCE;
    private static final String TAG = "AccountProxy";
    private static final int USER_ID_CONVERT_FACTOR = 100000;
    private final Object REMOTE_LOCK = new Object();
    private IRemoteObject remoteObj = null;

    /* access modifiers changed from: private */
    @FunctionalInterface
    public interface MarshallInterface {
        boolean marshalling(MessageParcel messageParcel);
    }

    /* access modifiers changed from: private */
    @FunctionalInterface
    public interface UnmarshallingInterface {
        boolean unmarshalling(MessageParcel messageParcel);
    }

    static /* synthetic */ boolean lambda$static$0(MessageParcel messageParcel) {
        return true;
    }

    private AccountProxy() {
    }

    public IRemoteObject asObject() {
        synchronized (this.REMOTE_LOCK) {
            if (this.remoteObj != null) {
                return this.remoteObj;
            }
            this.remoteObj = SysAbilityManager.getSysAbility(200);
            if (this.remoteObj == null) {
                HiLog.error(LABEL, "getSysAbility account failed", new Object[0]);
                return this.remoteObj;
            }
            this.remoteObj.addDeathRecipient(new AccountProxyDeathRecipient(), 0);
            HiLog.info(LABEL, "get remote object completed", new Object[0]);
            return this.remoteObj;
        }
    }

    private boolean sendAccountRequest(int i, MarshallInterface marshallInterface, UnmarshallingInterface unmarshallingInterface) {
        IRemoteObject asObject = asObject();
        if (asObject == null) {
            HiLog.error(LABEL, "get remote object failed", new Object[0]);
            return false;
        }
        MessageParcel obtain = MessageParcel.obtain();
        if (obtain == null) {
            HiLog.error(LABEL, "data obtain fail", new Object[0]);
            return false;
        }
        MessageParcel obtain2 = MessageParcel.obtain();
        if (obtain2 == null) {
            obtain.reclaim();
            HiLog.error(LABEL, "reply obtain fail", new Object[0]);
            return false;
        } else if (!obtain.writeInterfaceToken(DESCRIPTOR)) {
            obtain.reclaim();
            obtain2.reclaim();
            HiLog.error(LABEL, "Write descriptor fail", new Object[0]);
            return false;
        } else {
            MessageOption messageOption = new MessageOption();
            boolean marshalling = marshallInterface.marshalling(obtain);
            if (!marshalling) {
                obtain.reclaim();
                obtain2.reclaim();
                HiLog.error(LABEL, "accountParcel marshalling fail", new Object[0]);
                return marshalling;
            }
            try {
                boolean sendRequest = asObject.sendRequest(i, obtain, obtain2, messageOption);
                if (!sendRequest) {
                    HiLog.error(LABEL, "sendRequest data fail", new Object[0]);
                } else {
                    sendRequest = unmarshallingInterface.unmarshalling(obtain2);
                }
                obtain.reclaim();
                obtain2.reclaim();
                HiLog.debug(LABEL, "send request end", new Object[0]);
                return sendRequest;
            } catch (RemoteException unused) {
                HiLog.error(LABEL, "send request remote exception", new Object[0]);
                obtain.reclaim();
                obtain2.reclaim();
                HiLog.debug(LABEL, "send request end", new Object[0]);
                return false;
            } catch (Throwable th) {
                obtain.reclaim();
                obtain2.reclaim();
                HiLog.debug(LABEL, "send request end", new Object[0]);
                throw th;
            }
        }
    }

    private boolean isInvalidUnmarshallLength(int i, MessageParcel messageParcel) {
        return i < 0 || i > messageParcel.getSize() - messageParcel.getReadPosition();
    }

    public DistributedInfo queryOsAccountDistributedInfo() {
        HiLog.debug(LABEL, "queryOsAccountDistributedInfo begin", new Object[0]);
        DistributedInfo distributedInfo = new DistributedInfo();
        if (!sendAccountRequest(2, MARSHALL_STUB_FUNC, new UnmarshallingInterface() {
            /* class ohos.account.$$Lambda$AccountProxy$7wxtZLLl_epEHjxh4QUKjPaJ8 */

            @Override // ohos.account.AccountProxy.UnmarshallingInterface
            public final boolean unmarshalling(MessageParcel messageParcel) {
                return DistributedInfo.this.unmarshalling(messageParcel);
            }
        })) {
            HiLog.error(LABEL, "queryOsAccountDistributedInfo sendAccountRequest fail", new Object[0]);
            return FAIL_DISTRIBUTED_INFO;
        }
        HiLog.debug(LABEL, "queryOsAccountDistributedInfo end", new Object[0]);
        return distributedInfo;
    }

    public boolean updateOsAccountDistributedInfo(String str, String str2, String str3) {
        HiLog.debug(LABEL, "updateOsAccountDistributedInfo begin", new Object[0]);
        if (str == null || str.isEmpty()) {
            HiLog.error(LABEL, "updateOsAccountDistributedInfo accountName is null", new Object[0]);
            return false;
        } else if (str2 == null || str2.isEmpty()) {
            HiLog.error(LABEL, "updateOsAccountDistributedInfo uid is null", new Object[0]);
            return false;
        } else if (str3 == null || str3.isEmpty()) {
            HiLog.error(LABEL, "updateOsAccountDistributedInfo eventStr is null", new Object[0]);
            return false;
        } else {
            boolean sendAccountRequest = sendAccountRequest(1, new MarshallInterface(str, str2, str3) {
                /* class ohos.account.$$Lambda$AccountProxy$YS9OEP5VmRVeZEdUJfOV9nLkXc */
                private final /* synthetic */ String f$0;
                private final /* synthetic */ String f$1;
                private final /* synthetic */ String f$2;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // ohos.account.AccountProxy.MarshallInterface
                public final boolean marshalling(MessageParcel messageParcel) {
                    return AccountProxy.lambda$updateOsAccountDistributedInfo$2(this.f$0, this.f$1, this.f$2, messageParcel);
                }
            }, $$Lambda$AccountProxy$5OwveoCxuY48I1Yap7rqtYJ1MsM.INSTANCE);
            if (!sendAccountRequest) {
                HiLog.error(LABEL, "updateOsAccountDistributedInfo sendAccountRequest fail", new Object[0]);
                return false;
            }
            HiLog.debug(LABEL, "updateOsAccountDistributedInfo end", new Object[0]);
            return sendAccountRequest;
        }
    }

    static /* synthetic */ boolean lambda$updateOsAccountDistributedInfo$2(String str, String str2, String str3, MessageParcel messageParcel) {
        if (messageParcel.writeString(str) && messageParcel.writeString(str2) && messageParcel.writeString(str3)) {
            return true;
        }
        HiLog.error(LABEL, "updateOsAccountDistributedInfo write data fail", new Object[0]);
        return false;
    }

    static /* synthetic */ boolean lambda$updateOsAccountDistributedInfo$3(MessageParcel messageParcel) {
        return messageParcel.readInt() == 0;
    }

    public OsAccount createOsAccount(String str, OsAccountType osAccountType) {
        HiLog.debug(LABEL, "createOsAccount begin", new Object[0]);
        if (str == null || str.isEmpty()) {
            HiLog.error(LABEL, "create account local name error", new Object[0]);
            return FAIL_ACCOUNT;
        } else if (osAccountType == OsAccountType.INVALID) {
            HiLog.error(LABEL, "create account type error", new Object[0]);
            return FAIL_ACCOUNT;
        } else {
            OsAccount osAccount = new OsAccount(str, osAccountType);
            if (!sendAccountRequest(101, new MarshallInterface() {
                /* class ohos.account.$$Lambda$AccountProxy$djYBEJG77NUlMmUncztcbS2bFw */

                @Override // ohos.account.AccountProxy.MarshallInterface
                public final boolean marshalling(MessageParcel messageParcel) {
                    return OsAccount.this.marshalling(messageParcel);
                }
            }, new UnmarshallingInterface() {
                /* class ohos.account.$$Lambda$AccountProxy$SzebvOczXZVKsOcIW12pgg4qJLY */

                @Override // ohos.account.AccountProxy.UnmarshallingInterface
                public final boolean unmarshalling(MessageParcel messageParcel) {
                    return AccountProxy.lambda$createOsAccount$5(OsAccount.this, messageParcel);
                }
            })) {
                HiLog.error(LABEL, "createOsAccount sendAccountRequest fail", new Object[0]);
                return FAIL_ACCOUNT;
            }
            HiLog.debug(LABEL, "createOsAccount end", new Object[0]);
            return osAccount;
        }
    }

    static /* synthetic */ boolean lambda$createOsAccount$5(OsAccount osAccount, MessageParcel messageParcel) {
        if (messageParcel.readInt() != 0) {
            return false;
        }
        return osAccount.unmarshalling(messageParcel);
    }

    public boolean removeOsAccount(int i) {
        HiLog.debug(LABEL, "removeOsAccount begin", new Object[0]);
        if (i < 0) {
            HiLog.error(LABEL, "removeOsAccount param error", new Object[0]);
            return false;
        }
        boolean sendAccountRequest = sendAccountRequest(102, new MarshallInterface(i) {
            /* class ohos.account.$$Lambda$AccountProxy$XEU7ootCP4kMmOx6sBUiNAiWaM */
            private final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            @Override // ohos.account.AccountProxy.MarshallInterface
            public final boolean marshalling(MessageParcel messageParcel) {
                return messageParcel.writeInt(this.f$0);
            }
        }, $$Lambda$AccountProxy$vKM449SVq9_NCNGyYdFlHGCuEZY.INSTANCE);
        if (!sendAccountRequest) {
            HiLog.error(LABEL, "removeOsAccount sendAccountRequest fail", new Object[0]);
            return false;
        }
        HiLog.debug(LABEL, "removeOsAccount end", new Object[0]);
        return sendAccountRequest;
    }

    static /* synthetic */ boolean lambda$removeOsAccount$7(MessageParcel messageParcel) {
        return messageParcel.readInt() == 0;
    }

    public boolean isMultiOSAccountEnable() {
        HiLog.debug(LABEL, "isMultiOSAccountEnable begin", new Object[0]);
        ArrayList arrayList = new ArrayList();
        boolean sendAccountRequest = sendAccountRequest(100, MARSHALL_STUB_FUNC, new UnmarshallingInterface(arrayList) {
            /* class ohos.account.$$Lambda$AccountProxy$lAwxAFEsz94yS2fw5SmlLiO0InE */
            private final /* synthetic */ List f$0;

            {
                this.f$0 = r1;
            }

            @Override // ohos.account.AccountProxy.UnmarshallingInterface
            public final boolean unmarshalling(MessageParcel messageParcel) {
                return this.f$0.add(Boolean.valueOf(messageParcel.readBoolean()));
            }
        });
        if (!sendAccountRequest) {
            HiLog.error(LABEL, "sendAccountRequest fail", new Object[0]);
            return sendAccountRequest;
        }
        HiLog.debug(LABEL, "isMultiOSAccountEnable end", new Object[0]);
        if (arrayList.isEmpty()) {
            return false;
        }
        return ((Boolean) arrayList.get(0)).booleanValue();
    }

    public int queryMaxOsAccountNumber() {
        HiLog.debug(LABEL, "queryMaxOsAccountNumber begin", new Object[0]);
        ArrayList arrayList = new ArrayList();
        if (!sendAccountRequest(117, MARSHALL_STUB_FUNC, new UnmarshallingInterface(arrayList) {
            /* class ohos.account.$$Lambda$AccountProxy$sdGJh2FBhlCmZxpUfebvnJSQ8Fc */
            private final /* synthetic */ List f$0;

            {
                this.f$0 = r1;
            }

            @Override // ohos.account.AccountProxy.UnmarshallingInterface
            public final boolean unmarshalling(MessageParcel messageParcel) {
                return this.f$0.add(Integer.valueOf(messageParcel.readInt()));
            }
        })) {
            HiLog.error(LABEL, "sendAccountRequest fail", new Object[0]);
            return -1;
        }
        HiLog.debug(LABEL, "queryMaxOsAccountNumber end", new Object[0]);
        if (arrayList.isEmpty()) {
            return -1;
        }
        return ((Integer) arrayList.get(0)).intValue();
    }

    public List<OsAccount> queryAllCreatedOsAccounts() {
        HiLog.debug(LABEL, "queryAllCreatedOsAccounts begin", new Object[0]);
        ArrayList arrayList = new ArrayList();
        if (!sendAccountRequest(108, MARSHALL_STUB_FUNC, new UnmarshallingInterface(arrayList) {
            /* class ohos.account.$$Lambda$AccountProxy$1uMMjp6GXNrPlAmhNg3K6PABQ */
            private final /* synthetic */ List f$1;

            {
                this.f$1 = r2;
            }

            @Override // ohos.account.AccountProxy.UnmarshallingInterface
            public final boolean unmarshalling(MessageParcel messageParcel) {
                return AccountProxy.this.lambda$queryAllCreatedOsAccounts$10$AccountProxy(this.f$1, messageParcel);
            }
        })) {
            HiLog.error(LABEL, "sendAccountRequest fail", new Object[0]);
            return new ArrayList();
        }
        HiLog.debug(LABEL, "queryAllCreatedOsAccounts end", new Object[0]);
        return arrayList;
    }

    public /* synthetic */ boolean lambda$queryAllCreatedOsAccounts$10$AccountProxy(List list, MessageParcel messageParcel) {
        int readInt = messageParcel.readInt();
        if (isInvalidUnmarshallLength(readInt, messageParcel)) {
            HiLog.error(LABEL, "get number error %{public}d", new Object[]{Integer.valueOf(readInt)});
            return false;
        }
        HiLog.info(LABEL, "queryAllCreatedOsAccounts num %{public}d", new Object[]{Integer.valueOf(readInt)});
        for (int i = 0; i < readInt; i++) {
            OsAccount osAccount = new OsAccount();
            if (!osAccount.unmarshalling(messageParcel)) {
                HiLog.error(LABEL, "unmarshalling LocalAccount from accounts failed", new Object[0]);
                return false;
            }
            list.add(osAccount);
        }
        return true;
    }

    public OsAccount queryOsAccountById(int i) {
        HiLog.debug(LABEL, "queryOsAccountById begin", new Object[0]);
        OsAccount osAccount = new OsAccount();
        if (i < 0) {
            HiLog.error(LABEL, "local id error", new Object[0]);
            return FAIL_ACCOUNT;
        } else if (!sendAccountRequest(106, new MarshallInterface(i) {
            /* class ohos.account.$$Lambda$AccountProxy$dv2mRA_VPe6Y6vflgmByAoFkK74 */
            private final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            @Override // ohos.account.AccountProxy.MarshallInterface
            public final boolean marshalling(MessageParcel messageParcel) {
                return AccountProxy.lambda$queryOsAccountById$11(this.f$0, messageParcel);
            }
        }, new UnmarshallingInterface() {
            /* class ohos.account.$$Lambda$AccountProxy$OihrrHcPhVzJn544uuxlZ9BAg */

            @Override // ohos.account.AccountProxy.UnmarshallingInterface
            public final boolean unmarshalling(MessageParcel messageParcel) {
                return OsAccount.this.unmarshalling(messageParcel);
            }
        })) {
            HiLog.error(LABEL, "sendAccountRequest fail", new Object[0]);
            return FAIL_ACCOUNT;
        } else {
            HiLog.debug(LABEL, "queryOsAccountById end", new Object[0]);
            return osAccount;
        }
    }

    static /* synthetic */ boolean lambda$queryOsAccountById$11(int i, MessageParcel messageParcel) {
        if (messageParcel.writeInt(i)) {
            return true;
        }
        HiLog.error(LABEL, "write data fail", new Object[0]);
        return false;
    }

    public int getOsAccountLocalIdFromUid(int i) {
        if (i >= 0) {
            return i / USER_ID_CONVERT_FACTOR;
        }
        HiLog.error(LABEL, "param uid error", new Object[0]);
        return -1;
    }

    public boolean activateOsAccount(int i) {
        HiLog.debug(LABEL, "activateOsAccount %{private}d begin", new Object[]{Integer.valueOf(i)});
        if (i < 0) {
            HiLog.error(LABEL, "activate account param error", new Object[0]);
            return false;
        }
        boolean sendAccountRequest = sendAccountRequest(111, new MarshallInterface(i) {
            /* class ohos.account.$$Lambda$AccountProxy$y_5pxhSzgtLY9o6GLAFVXCmqHug */
            private final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            @Override // ohos.account.AccountProxy.MarshallInterface
            public final boolean marshalling(MessageParcel messageParcel) {
                return AccountProxy.lambda$activateOsAccount$13(this.f$0, messageParcel);
            }
        }, $$Lambda$AccountProxy$LedNbYOyuKEc6Js9EhPrtLmhD3E.INSTANCE);
        if (!sendAccountRequest) {
            HiLog.error(LABEL, "sendAccountRequest fail", new Object[0]);
            return sendAccountRequest;
        }
        HiLog.debug(LABEL, "activateOsAccount end", new Object[0]);
        return sendAccountRequest;
    }

    static /* synthetic */ boolean lambda$activateOsAccount$13(int i, MessageParcel messageParcel) {
        if (messageParcel.writeInt(i)) {
            return true;
        }
        HiLog.error(LABEL, "activate account write data fail", new Object[0]);
        return false;
    }

    static /* synthetic */ boolean lambda$activateOsAccount$14(MessageParcel messageParcel) {
        return messageParcel.readInt() == 0;
    }

    public OsAccount queryCurrentOsAccount() {
        HiLog.debug(LABEL, "queryCurrentOsAccount begin", new Object[0]);
        OsAccount osAccount = new OsAccount();
        if (!sendAccountRequest(107, MARSHALL_STUB_FUNC, new UnmarshallingInterface() {
            /* class ohos.account.$$Lambda$AccountProxy$9HcbDiEYcEuSjtZhnX8ntUmNkAg */

            @Override // ohos.account.AccountProxy.UnmarshallingInterface
            public final boolean unmarshalling(MessageParcel messageParcel) {
                return OsAccount.this.unmarshalling(messageParcel);
            }
        })) {
            HiLog.error(LABEL, "sendAccountRequest fail", new Object[0]);
            return FAIL_ACCOUNT;
        }
        HiLog.debug(LABEL, "queryCurrentOsAccount end", new Object[0]);
        return osAccount;
    }

    public boolean setOsAccountName(int i, String str) {
        HiLog.debug(LABEL, "setOsAccountName begin", new Object[0]);
        if (i < 0) {
            HiLog.error(LABEL, "local id error", new Object[0]);
            return false;
        } else if (str == null || str.isEmpty()) {
            HiLog.error(LABEL, "local name is null", new Object[0]);
            return false;
        } else {
            boolean sendAccountRequest = sendAccountRequest(112, new MarshallInterface(i, str) {
                /* class ohos.account.$$Lambda$AccountProxy$10i5w2hGVOvMABbRBgLM7hiyeCI */
                private final /* synthetic */ int f$0;
                private final /* synthetic */ String f$1;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                }

                @Override // ohos.account.AccountProxy.MarshallInterface
                public final boolean marshalling(MessageParcel messageParcel) {
                    return AccountProxy.lambda$setOsAccountName$16(this.f$0, this.f$1, messageParcel);
                }
            }, $$Lambda$AccountProxy$JWCW8n6tgP3XYtklZEi55NhhLVY.INSTANCE);
            if (!sendAccountRequest) {
                HiLog.error(LABEL, "sendAccountRequest fail", new Object[0]);
                return sendAccountRequest;
            }
            HiLog.debug(LABEL, "setOsAccountName end", new Object[0]);
            return sendAccountRequest;
        }
    }

    static /* synthetic */ boolean lambda$setOsAccountName$16(int i, String str, MessageParcel messageParcel) {
        if (!messageParcel.writeInt(i)) {
            HiLog.error(LABEL, "write local id fail", new Object[0]);
            return false;
        } else if (messageParcel.writeString(str)) {
            return true;
        } else {
            HiLog.error(LABEL, "write local name fail", new Object[0]);
            return false;
        }
    }

    static /* synthetic */ boolean lambda$setOsAccountName$17(MessageParcel messageParcel) {
        return messageParcel.readInt() == 0;
    }

    public boolean setOsAccountConstraints(int i, List<String> list, boolean z) {
        HiLog.debug(LABEL, "setOsAccountConstraints in", new Object[0]);
        if (i < 0) {
            HiLog.error(LABEL, "local id error", new Object[0]);
            return false;
        } else if (list == null || list.isEmpty()) {
            HiLog.error(LABEL, "constraints error", new Object[0]);
            return false;
        } else {
            boolean sendAccountRequest = sendAccountRequest(113, new MarshallInterface(i, list, z) {
                /* class ohos.account.$$Lambda$AccountProxy$PO7G9OIY9IsrwNAos1oTJIYOARg */
                private final /* synthetic */ int f$0;
                private final /* synthetic */ List f$1;
                private final /* synthetic */ boolean f$2;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // ohos.account.AccountProxy.MarshallInterface
                public final boolean marshalling(MessageParcel messageParcel) {
                    return AccountProxy.lambda$setOsAccountConstraints$18(this.f$0, this.f$1, this.f$2, messageParcel);
                }
            }, $$Lambda$AccountProxy$qZ6vbOUCNVl2KpvGPAnixk5vYLc.INSTANCE);
            if (!sendAccountRequest) {
                HiLog.error(LABEL, "sendAccountRequest fail", new Object[0]);
                return sendAccountRequest;
            }
            HiLog.debug(LABEL, "setOsAccountConstraints end", new Object[0]);
            return sendAccountRequest;
        }
    }

    static /* synthetic */ boolean lambda$setOsAccountConstraints$18(int i, List list, boolean z, MessageParcel messageParcel) {
        if (!messageParcel.writeInt(i)) {
            HiLog.error(LABEL, "write local id fail", new Object[0]);
            return false;
        } else if (!messageParcel.writeInt(list.size())) {
            HiLog.error(LABEL, "contraints size fail", new Object[0]);
            return false;
        } else {
            Iterator it = list.iterator();
            while (it.hasNext()) {
                if (!messageParcel.writeString((String) it.next())) {
                    HiLog.error(LABEL, "write constraints fail", new Object[0]);
                    return false;
                }
            }
            if (messageParcel.writeBoolean(z)) {
                return true;
            }
            HiLog.error(LABEL, "write enable fail", new Object[0]);
            return false;
        }
    }

    static /* synthetic */ boolean lambda$setOsAccountConstraints$19(MessageParcel messageParcel) {
        return messageParcel.readInt() == 0;
    }

    public boolean isOsAccountConstraintEnable(int i, String str) {
        HiLog.debug(LABEL, "isOsAccountConstraintEnable begin", new Object[0]);
        ArrayList arrayList = new ArrayList();
        if (i < 0) {
            HiLog.error(LABEL, "local id error", new Object[0]);
            return false;
        } else if (str == null || str.isEmpty()) {
            HiLog.error(LABEL, "constraint is null", new Object[0]);
            return false;
        } else {
            boolean sendAccountRequest = sendAccountRequest(116, new MarshallInterface(i, str) {
                /* class ohos.account.$$Lambda$AccountProxy$TH1sCIMSza4BqFXq5wDLA3LLlLs */
                private final /* synthetic */ int f$0;
                private final /* synthetic */ String f$1;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                }

                @Override // ohos.account.AccountProxy.MarshallInterface
                public final boolean marshalling(MessageParcel messageParcel) {
                    return AccountProxy.lambda$isOsAccountConstraintEnable$20(this.f$0, this.f$1, messageParcel);
                }
            }, new UnmarshallingInterface(arrayList) {
                /* class ohos.account.$$Lambda$AccountProxy$K0Rz54QMSZuTNXlkoDyUfaUSZI */
                private final /* synthetic */ List f$0;

                {
                    this.f$0 = r1;
                }

                @Override // ohos.account.AccountProxy.UnmarshallingInterface
                public final boolean unmarshalling(MessageParcel messageParcel) {
                    return this.f$0.add(Boolean.valueOf(messageParcel.readBoolean()));
                }
            });
            if (!sendAccountRequest) {
                HiLog.error(LABEL, "sendAccountRequest fail", new Object[0]);
                return sendAccountRequest;
            }
            HiLog.debug(LABEL, "isOsAccountConstraintEnable end", new Object[0]);
            if (arrayList.isEmpty()) {
                return false;
            }
            return ((Boolean) arrayList.get(0)).booleanValue();
        }
    }

    static /* synthetic */ boolean lambda$isOsAccountConstraintEnable$20(int i, String str, MessageParcel messageParcel) {
        if (!messageParcel.writeInt(i)) {
            HiLog.error(LABEL, "write id fail", new Object[0]);
            return false;
        } else if (messageParcel.writeString(str)) {
            return true;
        } else {
            HiLog.error(LABEL, "write constraint fail", new Object[0]);
            return false;
        }
    }

    public List<String> getOsAccountAllConstraints(int i) {
        HiLog.debug(LABEL, "getOsAccountAllConstraints begin", new Object[0]);
        ArrayList arrayList = new ArrayList();
        if (i < 0) {
            HiLog.error(LABEL, "local id error", new Object[0]);
            return new ArrayList();
        } else if (!sendAccountRequest(115, new MarshallInterface(i) {
            /* class ohos.account.$$Lambda$AccountProxy$79xNFQdOiHR9lE2kHKLJOJJpT8 */
            private final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            @Override // ohos.account.AccountProxy.MarshallInterface
            public final boolean marshalling(MessageParcel messageParcel) {
                return AccountProxy.lambda$getOsAccountAllConstraints$22(this.f$0, messageParcel);
            }
        }, new UnmarshallingInterface(arrayList) {
            /* class ohos.account.$$Lambda$AccountProxy$3awXre7PLyGWhEoj3EQF8efQsu0 */
            private final /* synthetic */ List f$1;

            {
                this.f$1 = r2;
            }

            @Override // ohos.account.AccountProxy.UnmarshallingInterface
            public final boolean unmarshalling(MessageParcel messageParcel) {
                return AccountProxy.this.lambda$getOsAccountAllConstraints$23$AccountProxy(this.f$1, messageParcel);
            }
        })) {
            HiLog.error(LABEL, "sendAccountRequest fail", new Object[0]);
            return new ArrayList();
        } else {
            HiLog.debug(LABEL, "getOsAccountAllConstraints end", new Object[0]);
            return arrayList;
        }
    }

    static /* synthetic */ boolean lambda$getOsAccountAllConstraints$22(int i, MessageParcel messageParcel) {
        if (messageParcel.writeInt(i)) {
            return true;
        }
        HiLog.error(LABEL, "write id fail", new Object[0]);
        return false;
    }

    public /* synthetic */ boolean lambda$getOsAccountAllConstraints$23$AccountProxy(List list, MessageParcel messageParcel) {
        int readInt = messageParcel.readInt();
        if (isInvalidUnmarshallLength(readInt, messageParcel)) {
            HiLog.error(LABEL, "get constraints number error %{public}d", new Object[]{Integer.valueOf(readInt)});
            return false;
        }
        HiLog.info(LABEL, "getOsAccountAllConstraints num %{public}d", new Object[]{Integer.valueOf(readInt)});
        for (int i = 0; i < readInt; i++) {
            list.add(messageParcel.readString());
        }
        return true;
    }

    public int getAllCreatedOsAccounts() {
        HiLog.debug(LABEL, "getAllCreatedOsAccounts begin", new Object[0]);
        List<OsAccount> queryAllCreatedOsAccounts = queryAllCreatedOsAccounts();
        HiLog.debug(LABEL, "getAllCreatedOsAccounts end", new Object[0]);
        return queryAllCreatedOsAccounts.size();
    }

    public int getOsAccountLocalIdFromProcess() {
        HiLog.debug(LABEL, "getOsAccountLocalIdFromProcess begin", new Object[0]);
        int osAccountLocalIdFromUid = getOsAccountLocalIdFromUid(ProcessManager.getUid());
        HiLog.debug(LABEL, "getOsAccountIdFromProcess end", new Object[0]);
        return osAccountLocalIdFromUid;
    }

    public boolean isOsAccountVerified() {
        HiLog.debug(LABEL, "isOsAccountVerified begin", new Object[0]);
        ArrayList arrayList = new ArrayList();
        boolean sendAccountRequest = sendAccountRequest(118, MARSHALL_STUB_FUNC, new UnmarshallingInterface(arrayList) {
            /* class ohos.account.$$Lambda$AccountProxy$o9khp8C4QQQzT8K4dVm29m68FTM */
            private final /* synthetic */ List f$0;

            {
                this.f$0 = r1;
            }

            @Override // ohos.account.AccountProxy.UnmarshallingInterface
            public final boolean unmarshalling(MessageParcel messageParcel) {
                return this.f$0.add(Boolean.valueOf(messageParcel.readBoolean()));
            }
        });
        if (!sendAccountRequest) {
            HiLog.error(LABEL, "sendAccountRequest fail", new Object[0]);
            return sendAccountRequest;
        }
        HiLog.debug(LABEL, "isOsAccountVerified end", new Object[0]);
        if (arrayList.isEmpty()) {
            return false;
        }
        return ((Boolean) arrayList.get(0)).booleanValue();
    }

    public boolean isOsAccountVerified(int i) {
        HiLog.debug(LABEL, "isOsAccountVerified with id begin", new Object[0]);
        if (i < 0) {
            HiLog.error(LABEL, "local id error", new Object[0]);
            return false;
        }
        ArrayList arrayList = new ArrayList();
        boolean sendAccountRequest = sendAccountRequest(119, new MarshallInterface(i) {
            /* class ohos.account.$$Lambda$AccountProxy$T_Vk71EHNWEv4tgJf5CVNZ7sebE */
            private final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            @Override // ohos.account.AccountProxy.MarshallInterface
            public final boolean marshalling(MessageParcel messageParcel) {
                return AccountProxy.lambda$isOsAccountVerified$25(this.f$0, messageParcel);
            }
        }, new UnmarshallingInterface(arrayList) {
            /* class ohos.account.$$Lambda$AccountProxy$ik6iOu9kXJFujQn6dcBvm3eFXwk */
            private final /* synthetic */ List f$0;

            {
                this.f$0 = r1;
            }

            @Override // ohos.account.AccountProxy.UnmarshallingInterface
            public final boolean unmarshalling(MessageParcel messageParcel) {
                return this.f$0.add(Boolean.valueOf(messageParcel.readBoolean()));
            }
        });
        if (!sendAccountRequest) {
            HiLog.error(LABEL, "sendAccountRequest fail", new Object[0]);
            return sendAccountRequest;
        }
        HiLog.debug(LABEL, "isOsAccountVerified with id end", new Object[0]);
        if (arrayList.isEmpty()) {
            return false;
        }
        return ((Boolean) arrayList.get(0)).booleanValue();
    }

    static /* synthetic */ boolean lambda$isOsAccountVerified$25(int i, MessageParcel messageParcel) {
        if (messageParcel.writeInt(i)) {
            return true;
        }
        HiLog.error(LABEL, "write data fail", new Object[0]);
        return false;
    }

    public boolean isOsAccountKingKong() {
        HiLog.debug(LABEL, "isOsAccountKingKong begin", new Object[0]);
        ArrayList arrayList = new ArrayList();
        boolean sendAccountRequest = sendAccountRequest(125, MARSHALL_STUB_FUNC, new UnmarshallingInterface(arrayList) {
            /* class ohos.account.$$Lambda$AccountProxy$AAI4v_57EzbNWYUMalo9ICVcJz4 */
            private final /* synthetic */ List f$0;

            {
                this.f$0 = r1;
            }

            @Override // ohos.account.AccountProxy.UnmarshallingInterface
            public final boolean unmarshalling(MessageParcel messageParcel) {
                return this.f$0.add(Boolean.valueOf(messageParcel.readBoolean()));
            }
        });
        if (!sendAccountRequest) {
            HiLog.error(LABEL, "send request fail", new Object[0]);
            return sendAccountRequest;
        }
        HiLog.debug(LABEL, "isOsAccountKingKong end", new Object[0]);
        if (arrayList.isEmpty()) {
            return false;
        }
        return ((Boolean) arrayList.get(0)).booleanValue();
    }

    public boolean isOsAccountActive(int i) {
        HiLog.debug(LABEL, "isOsAccountActive id begin", new Object[0]);
        if (i < 0) {
            HiLog.error(LABEL, "local id error", new Object[0]);
            return false;
        }
        ArrayList arrayList = new ArrayList();
        boolean sendAccountRequest = sendAccountRequest(120, new MarshallInterface(i) {
            /* class ohos.account.$$Lambda$AccountProxy$YD7OE7LpcN47y91HkTYxZbQZZXg */
            private final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            @Override // ohos.account.AccountProxy.MarshallInterface
            public final boolean marshalling(MessageParcel messageParcel) {
                return AccountProxy.lambda$isOsAccountActive$28(this.f$0, messageParcel);
            }
        }, new UnmarshallingInterface(arrayList) {
            /* class ohos.account.$$Lambda$AccountProxy$_1i51v5y6hsMkeQ_D9YGRHsSdqE */
            private final /* synthetic */ List f$0;

            {
                this.f$0 = r1;
            }

            @Override // ohos.account.AccountProxy.UnmarshallingInterface
            public final boolean unmarshalling(MessageParcel messageParcel) {
                return this.f$0.add(Boolean.valueOf(messageParcel.readBoolean()));
            }
        });
        if (!sendAccountRequest) {
            HiLog.error(LABEL, "sendAccountRequest fail", new Object[0]);
            return sendAccountRequest;
        }
        HiLog.debug(LABEL, "isOsAccountActive end", new Object[0]);
        if (arrayList.isEmpty()) {
            return false;
        }
        return ((Boolean) arrayList.get(0)).booleanValue();
    }

    static /* synthetic */ boolean lambda$isOsAccountActive$28(int i, MessageParcel messageParcel) {
        if (messageParcel.writeInt(i)) {
            return true;
        }
        HiLog.error(LABEL, "write data fail", new Object[0]);
        return false;
    }

    public OsAccountType getOsAccountTypeFromProcess() {
        HiLog.debug(LABEL, "get os account type begin", new Object[0]);
        ArrayList arrayList = new ArrayList();
        if (!sendAccountRequest(124, MARSHALL_STUB_FUNC, new UnmarshallingInterface(arrayList) {
            /* class ohos.account.$$Lambda$AccountProxy$mWfekbJwh7itENNHER3bA1nZR8 */
            private final /* synthetic */ List f$0;

            {
                this.f$0 = r1;
            }

            @Override // ohos.account.AccountProxy.UnmarshallingInterface
            public final boolean unmarshalling(MessageParcel messageParcel) {
                return this.f$0.add(Integer.valueOf(messageParcel.readInt()));
            }
        })) {
            HiLog.error(LABEL, "send request fail", new Object[0]);
            return OsAccountType.INVALID;
        }
        HiLog.debug(LABEL, "get os account type end", new Object[0]);
        return arrayList.isEmpty() ? OsAccountType.INVALID : OsAccountType.getType(((Integer) arrayList.get(0)).intValue());
    }

    public PixelMap getOsAccountProfilePhoto(int i) {
        HiLog.debug(LABEL, "getOsAccountProfilePhoto in", new Object[0]);
        return ACCOUNT_ADAPTER.getOsAccountProfilePhoto(i);
    }

    public boolean setOsAccountProfilePhoto(int i, PixelMap pixelMap) {
        HiLog.debug(LABEL, "setOsAccountProfilePhoto in", new Object[0]);
        return ACCOUNT_ADAPTER.setOsAccountProfilePhoto(i, pixelMap);
    }

    public static AccountProxy getAccountProxy() {
        return ACCOUNT_PROXY_INSTANCE;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setRemoteObject(IRemoteObject iRemoteObject) {
        synchronized (this.REMOTE_LOCK) {
            getAccountProxy().remoteObj = iRemoteObject;
        }
    }

    /* access modifiers changed from: private */
    public static class AccountProxyDeathRecipient implements IRemoteObject.DeathRecipient {
        private AccountProxyDeathRecipient() {
        }

        public void onRemoteDied() {
            HiLog.warn(AccountProxy.LABEL, "AccountProxyDeathRecipient::onRemoteDied", new Object[0]);
            AccountProxy.getAccountProxy().setRemoteObject(null);
        }
    }
}
