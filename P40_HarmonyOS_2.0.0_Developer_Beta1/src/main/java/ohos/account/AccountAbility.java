package ohos.account;

import java.util.List;
import ohos.annotation.SystemApi;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.media.image.PixelMap;

public class AccountAbility {
    private static final AccountAbility ACCOUNT_INSTANCE = new AccountAbility();
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) LOG_DOMAIN, TAG);
    private static final int LOG_DOMAIN = 218110720;
    private static final String TAG = "AccountManager";

    private AccountAbility() {
    }

    public DistributedInfo queryOsAccountDistributedInfo() {
        HiLog.debug(LABEL, "queryOsAccountDistributedInfo in", new Object[0]);
        return AccountProxy.getAccountProxy().queryOsAccountDistributedInfo();
    }

    @SystemApi
    public boolean updateOsAccountDistributedInfo(String str, String str2, String str3) {
        HiLog.debug(LABEL, "updateOsAccountDistributedInfo in", new Object[0]);
        return AccountProxy.getAccountProxy().updateOsAccountDistributedInfo(str, str2, str3);
    }

    @SystemApi
    public OsAccount createOsAccount(String str, OsAccountType osAccountType) {
        HiLog.debug(LABEL, "createOsAccount in", new Object[0]);
        return AccountProxy.getAccountProxy().createOsAccount(str, osAccountType);
    }

    @SystemApi
    public boolean removeOsAccount(int i) {
        HiLog.debug(LABEL, "removeOsAccount in", new Object[0]);
        return AccountProxy.getAccountProxy().removeOsAccount(i);
    }

    public boolean isMultiOSAccountEnable() {
        HiLog.debug(LABEL, "isMultiOSAccountEnable in", new Object[0]);
        return AccountProxy.getAccountProxy().isMultiOSAccountEnable();
    }

    @SystemApi
    public List<OsAccount> queryAllCreatedOsAccounts() {
        HiLog.debug(LABEL, "queryAllOsAccounts in", new Object[0]);
        return AccountProxy.getAccountProxy().queryAllCreatedOsAccounts();
    }

    @SystemApi
    public int queryMaxOsAccountNumber() {
        HiLog.debug(LABEL, "queryMaxOsAccountNumber in", new Object[0]);
        return AccountProxy.getAccountProxy().queryMaxOsAccountNumber();
    }

    @SystemApi
    public OsAccount queryOsAccountById(int i) {
        HiLog.debug(LABEL, "queryOsAccountById in", new Object[0]);
        return AccountProxy.getAccountProxy().queryOsAccountById(i);
    }

    public int getOsAccountLocalIdFromUid(int i) {
        HiLog.debug(LABEL, "getOsAccountLocalIdFromUid in", new Object[0]);
        return AccountProxy.getAccountProxy().getOsAccountLocalIdFromUid(i);
    }

    @SystemApi
    public boolean activateOsAccount(int i) {
        HiLog.debug(LABEL, "activateOsAccount in", new Object[0]);
        return AccountProxy.getAccountProxy().activateOsAccount(i);
    }

    public OsAccount queryCurrentOsAccount() {
        HiLog.debug(LABEL, "queryCurrentOsAccount in", new Object[0]);
        return AccountProxy.getAccountProxy().queryCurrentOsAccount();
    }

    @SystemApi
    public boolean setOsAccountName(int i, String str) {
        HiLog.debug(LABEL, "setOsAccountName in", new Object[0]);
        return AccountProxy.getAccountProxy().setOsAccountName(i, str);
    }

    @SystemApi
    public boolean setOsAccountConstraints(int i, List<String> list, boolean z) {
        HiLog.debug(LABEL, "setOsAccountConstraints in", new Object[0]);
        return AccountProxy.getAccountProxy().setOsAccountConstraints(i, list, z);
    }

    public boolean isOsAccountConstraintEnable(int i, String str) {
        HiLog.debug(LABEL, "isOsAccountConstraintEnable in", new Object[0]);
        return AccountProxy.getAccountProxy().isOsAccountConstraintEnable(i, str);
    }

    public List<String> getOsAccountAllConstraints(int i) {
        HiLog.debug(LABEL, "getOsAccountAllRestrictions in", new Object[0]);
        return AccountProxy.getAccountProxy().getOsAccountAllConstraints(i);
    }

    public int getAllCreatedOsAccounts() {
        HiLog.debug(LABEL, "getAllCreatedOsAccounts in", new Object[0]);
        return AccountProxy.getAccountProxy().getAllCreatedOsAccounts();
    }

    public int getOsAccountLocalIdFromProcess() {
        HiLog.debug(LABEL, "getOsAccountLocalIdFromProcess in", new Object[0]);
        return AccountProxy.getAccountProxy().getOsAccountLocalIdFromProcess();
    }

    public boolean isOsAccountActive(int i) {
        HiLog.debug(LABEL, "isOsAccountActive in", new Object[0]);
        return AccountProxy.getAccountProxy().isOsAccountActive(i);
    }

    public boolean isOsAccountKingKong() {
        HiLog.debug(LABEL, "isOsAccountKingKong in", new Object[0]);
        return AccountProxy.getAccountProxy().isOsAccountKingKong();
    }

    public boolean isOsAccountVerified() {
        HiLog.debug(LABEL, "isOsAccountVerified in", new Object[0]);
        return AccountProxy.getAccountProxy().isOsAccountVerified();
    }

    public boolean isOsAccountVerified(int i) {
        HiLog.debug(LABEL, "isOsAccountVerified in", new Object[0]);
        return AccountProxy.getAccountProxy().isOsAccountVerified(i);
    }

    public OsAccountType getOsAccountTypeFromProcess() {
        HiLog.debug(LABEL, "getOsAccountTypeFromProcess in", new Object[0]);
        return AccountProxy.getAccountProxy().getOsAccountTypeFromProcess();
    }

    @SystemApi
    public PixelMap getOsAccountProfilePhoto(int i) {
        HiLog.debug(LABEL, "getOsAccountProfilePhoto in", new Object[0]);
        return AccountProxy.getAccountProxy().getOsAccountProfilePhoto(i);
    }

    @SystemApi
    public boolean setOsAccountProfilePhoto(int i, PixelMap pixelMap) {
        HiLog.debug(LABEL, "setOsAccountProfilePhoto in", new Object[0]);
        return AccountProxy.getAccountProxy().setOsAccountProfilePhoto(i, pixelMap);
    }

    @SystemApi
    public void subscribeOsAccountEvent(IOsAccountSubscriber iOsAccountSubscriber, String str) {
        HiLog.debug(LABEL, "subscribeOsAccountEvent in", new Object[0]);
        AccountProxy.getAccountProxy().subscribeOsAccountEvent(iOsAccountSubscriber, str);
    }

    public static AccountAbility getAccountAbility() {
        return ACCOUNT_INSTANCE;
    }
}
