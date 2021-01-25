package android.content;

import android.app.ActivityThread;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.ArraySet;
import android.util.Log;
import android.view.autofill.AutofillManager;
import java.io.PrintWriter;

public final class AutofillOptions implements Parcelable {
    public static final Parcelable.Creator<AutofillOptions> CREATOR = new Parcelable.Creator<AutofillOptions>() {
        /* class android.content.AutofillOptions.AnonymousClass1 */

        /* JADX DEBUG: Type inference failed for r3v2. Raw type applied. Possible types: android.util.ArraySet<? extends java.lang.Object>, android.util.ArraySet<android.content.ComponentName> */
        @Override // android.os.Parcelable.Creator
        public AutofillOptions createFromParcel(Parcel parcel) {
            AutofillOptions options = new AutofillOptions(parcel.readInt(), parcel.readBoolean());
            options.augmentedAutofillEnabled = parcel.readBoolean();
            options.whitelistedActivitiesForAugmentedAutofill = parcel.readArraySet(null);
            return options;
        }

        @Override // android.os.Parcelable.Creator
        public AutofillOptions[] newArray(int size) {
            return new AutofillOptions[size];
        }
    };
    private static final String TAG = AutofillOptions.class.getSimpleName();
    public boolean augmentedAutofillEnabled;
    public final boolean compatModeEnabled;
    public final int loggingLevel;
    public ArraySet<ComponentName> whitelistedActivitiesForAugmentedAutofill;

    public AutofillOptions(int loggingLevel2, boolean compatModeEnabled2) {
        this.loggingLevel = loggingLevel2;
        this.compatModeEnabled = compatModeEnabled2;
    }

    public boolean isAugmentedAutofillEnabled(Context context) {
        AutofillManager.AutofillClient autofillClient;
        if (!this.augmentedAutofillEnabled || (autofillClient = context.getAutofillClient()) == null) {
            return false;
        }
        ComponentName component = autofillClient.autofillClientGetComponentName();
        ArraySet<ComponentName> arraySet = this.whitelistedActivitiesForAugmentedAutofill;
        if (arraySet == null || arraySet.contains(component)) {
            return true;
        }
        return false;
    }

    public static AutofillOptions forWhitelistingItself() {
        ActivityThread at = ActivityThread.currentActivityThread();
        if (at != null) {
            String packageName = at.getApplication().getPackageName();
            if ("android.autofillservice.cts".equals(packageName)) {
                AutofillOptions options = new AutofillOptions(4, true);
                options.augmentedAutofillEnabled = true;
                String str = TAG;
                Log.i(str, "forWhitelistingItself(" + packageName + "): " + options);
                return options;
            }
            String str2 = TAG;
            Log.e(str2, "forWhitelistingItself(): called by " + packageName);
            throw new SecurityException("Thou shall not pass!");
        }
        throw new IllegalStateException("No ActivityThread");
    }

    public String toString() {
        return "AutofillOptions [loggingLevel=" + this.loggingLevel + ", compatMode=" + this.compatModeEnabled + ", augmentedAutofillEnabled=" + this.augmentedAutofillEnabled + "]";
    }

    public void dumpShort(PrintWriter pw) {
        pw.print("logLvl=");
        pw.print(this.loggingLevel);
        pw.print(", compatMode=");
        pw.print(this.compatModeEnabled);
        pw.print(", augmented=");
        pw.print(this.augmentedAutofillEnabled);
        if (this.whitelistedActivitiesForAugmentedAutofill != null) {
            pw.print(", whitelistedActivitiesForAugmentedAutofill=");
            pw.print(this.whitelistedActivitiesForAugmentedAutofill);
        }
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(this.loggingLevel);
        parcel.writeBoolean(this.compatModeEnabled);
        parcel.writeBoolean(this.augmentedAutofillEnabled);
        parcel.writeArraySet(this.whitelistedActivitiesForAugmentedAutofill);
    }
}
