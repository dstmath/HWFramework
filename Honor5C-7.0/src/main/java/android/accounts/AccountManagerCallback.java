package android.accounts;

public interface AccountManagerCallback<V> {
    void run(AccountManagerFuture<V> accountManagerFuture);
}
