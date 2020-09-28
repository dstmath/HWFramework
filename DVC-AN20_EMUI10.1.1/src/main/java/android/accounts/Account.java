package android.accounts;

import android.accounts.IAccountManager;
import android.annotation.UnsupportedAppUsage;
import android.os.BadParcelableException;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.transition.EpicenterTranslateClipReveal;
import java.util.Set;

public class Account implements Parcelable {
    public static final Parcelable.Creator<Account> CREATOR = new Parcelable.Creator<Account>() {
        /* class android.accounts.Account.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public Account createFromParcel(Parcel source) {
            return new Account(source);
        }

        @Override // android.os.Parcelable.Creator
        public Account[] newArray(int size) {
            return new Account[size];
        }
    };
    @UnsupportedAppUsage
    private static final String TAG = "Account";
    @GuardedBy({"sAccessedAccounts"})
    private static final Set<Account> sAccessedAccounts = new ArraySet();
    @UnsupportedAppUsage
    private final String accessId;
    private String mSafeName;
    public final String name;
    public final String type;

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Account)) {
            return false;
        }
        Account other = (Account) o;
        if (!this.name.equals(other.name) || !this.type.equals(other.type)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return (((17 * 31) + this.name.hashCode()) * 31) + this.type.hashCode();
    }

    public Account(String name2, String type2) {
        this(name2, type2, null);
    }

    public Account(Account other, String accessId2) {
        this(other.name, other.type, accessId2);
    }

    public Account(String name2, String type2, String accessId2) {
        if (TextUtils.isEmpty(name2)) {
            throw new IllegalArgumentException("the name must not be empty: " + name2);
        } else if (!TextUtils.isEmpty(type2)) {
            this.name = name2;
            this.type = type2;
            this.accessId = accessId2;
        } else {
            throw new IllegalArgumentException("the type must not be empty: " + type2);
        }
    }

    public Account(Parcel in) {
        this.name = in.readString();
        this.type = in.readString();
        if (TextUtils.isEmpty(this.name)) {
            throw new BadParcelableException("the name must not be empty: " + this.name);
        } else if (!TextUtils.isEmpty(this.type)) {
            this.accessId = in.readString();
            if (this.accessId != null) {
                synchronized (sAccessedAccounts) {
                    if (sAccessedAccounts.add(this)) {
                        try {
                            IAccountManager.Stub.asInterface(ServiceManager.getService("account")).onAccountAccessed(this.accessId);
                        } catch (RemoteException e) {
                            Log.e(TAG, "Error noting account access", e);
                        }
                    }
                }
            }
        } else {
            throw new BadParcelableException("the type must not be empty: " + this.type);
        }
    }

    public String getAccessId() {
        return this.accessId;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.type);
        dest.writeString(this.accessId);
    }

    public String toString() {
        return "Account {name=XXXXXXXXX, type=" + this.type + "}";
    }

    public String toSafeString() {
        if (this.mSafeName == null) {
            this.mSafeName = toSafeName(this.name, EpicenterTranslateClipReveal.StateProperty.TARGET_X);
        }
        return "Account {name=" + this.mSafeName + ", type=" + this.type + "}";
    }

    public static String toSafeName(String name2, char replacement) {
        StringBuilder builder = new StringBuilder(64);
        int len = name2.length();
        for (int i = 0; i < len; i++) {
            char c = name2.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                builder.append(replacement);
            } else {
                builder.append(c);
            }
        }
        return builder.toString();
    }
}
