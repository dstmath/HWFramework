package android.accounts;

import android.annotation.UnsupportedAppUsage;

public class AccountAndUser {
    @UnsupportedAppUsage
    public Account account;
    @UnsupportedAppUsage
    public int userId;

    @UnsupportedAppUsage
    public AccountAndUser(Account account2, int userId2) {
        this.account = account2;
        this.userId = userId2;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AccountAndUser)) {
            return false;
        }
        AccountAndUser other = (AccountAndUser) o;
        if (!this.account.equals(other.account) || this.userId != other.userId) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return this.account.hashCode() + this.userId;
    }

    public String toString() {
        return this.account.toString() + " u" + this.userId;
    }
}
