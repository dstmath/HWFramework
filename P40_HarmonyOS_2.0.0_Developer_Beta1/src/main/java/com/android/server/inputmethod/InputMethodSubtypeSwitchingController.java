package com.android.server.inputmethod;

import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Printer;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodSubtype;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.inputmethod.InputMethodUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class InputMethodSubtypeSwitchingController {
    private static final boolean DEBUG = false;
    private static final int NOT_A_SUBTYPE_ID = -1;
    private static final String TAG = InputMethodSubtypeSwitchingController.class.getSimpleName();
    private ControllerImpl mController;
    private final InputMethodUtils.InputMethodSettings mSettings;
    private InputMethodAndSubtypeList mSubtypeList;

    public static class ImeSubtypeListItem implements Comparable<ImeSubtypeListItem> {
        public final CharSequence mImeName;
        public final InputMethodInfo mImi;
        public final boolean mIsSystemLanguage;
        public final boolean mIsSystemLocale;
        public final int mSubtypeId;
        public final CharSequence mSubtypeName;

        public ImeSubtypeListItem(CharSequence imeName, CharSequence subtypeName, InputMethodInfo imi, int subtypeId, String subtypeLocale, String systemLocale) {
            this.mImeName = imeName;
            this.mSubtypeName = subtypeName;
            this.mImi = imi;
            this.mSubtypeId = subtypeId;
            boolean z = false;
            if (TextUtils.isEmpty(subtypeLocale)) {
                this.mIsSystemLocale = false;
                this.mIsSystemLanguage = false;
                return;
            }
            this.mIsSystemLocale = subtypeLocale.equals(systemLocale);
            if (this.mIsSystemLocale) {
                this.mIsSystemLanguage = true;
                return;
            }
            String systemLanguage = parseLanguageFromLocaleString(systemLocale);
            String subtypeLanguage = parseLanguageFromLocaleString(subtypeLocale);
            if (systemLanguage.length() >= 2 && systemLanguage.equals(subtypeLanguage)) {
                z = true;
            }
            this.mIsSystemLanguage = z;
        }

        private static String parseLanguageFromLocaleString(String locale) {
            int idx = locale.indexOf(95);
            if (idx < 0) {
                return locale;
            }
            return locale.substring(0, idx);
        }

        private static int compareNullableCharSequences(CharSequence c1, CharSequence c2) {
            boolean empty1 = TextUtils.isEmpty(c1);
            boolean empty2 = TextUtils.isEmpty(c2);
            if (empty1 || empty2) {
                return (empty1 ? 1 : 0) - (empty2 ? 1 : 0);
            }
            return c1.toString().compareTo(c2.toString());
        }

        public int compareTo(ImeSubtypeListItem other) {
            int result = compareNullableCharSequences(this.mImeName, other.mImeName);
            if (result != 0) {
                return result;
            }
            int i = -1;
            int result2 = (this.mIsSystemLocale ? -1 : 0) - (other.mIsSystemLocale ? -1 : 0);
            if (result2 != 0) {
                return result2;
            }
            int i2 = this.mIsSystemLanguage ? -1 : 0;
            if (!other.mIsSystemLanguage) {
                i = 0;
            }
            int result3 = i2 - i;
            if (result3 != 0) {
                return result3;
            }
            int result4 = compareNullableCharSequences(this.mSubtypeName, other.mSubtypeName);
            if (result4 != 0) {
                return result4;
            }
            return this.mImi.getId().compareTo(other.mImi.getId());
        }

        @Override // java.lang.Object
        public String toString() {
            return "ImeSubtypeListItem{mImeName=" + ((Object) this.mImeName) + " mSubtypeName=" + ((Object) this.mSubtypeName) + " mSubtypeId=" + this.mSubtypeId + " mIsSystemLocale=" + this.mIsSystemLocale + " mIsSystemLanguage=" + this.mIsSystemLanguage + "}";
        }

        @Override // java.lang.Object
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof ImeSubtypeListItem)) {
                return false;
            }
            ImeSubtypeListItem that = (ImeSubtypeListItem) o;
            if (!Objects.equals(this.mImi, that.mImi) || this.mSubtypeId != that.mSubtypeId) {
                return false;
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    public static class InputMethodAndSubtypeList {
        private final Context mContext;
        private final PackageManager mPm;
        private final InputMethodUtils.InputMethodSettings mSettings;
        private final String mSystemLocaleStr;

        public InputMethodAndSubtypeList(Context context, InputMethodUtils.InputMethodSettings settings) {
            this.mContext = context;
            this.mSettings = settings;
            this.mPm = context.getPackageManager();
            Locale locale = context.getResources().getConfiguration().locale;
            this.mSystemLocaleStr = locale != null ? locale.toString() : "";
        }

        public List<ImeSubtypeListItem> getSortedInputMethodAndSubtypeList(boolean includeAuxiliarySubtypes, boolean isScreenLocked) {
            boolean includeAuxiliarySubtypes2;
            boolean includeAuxiliarySubtypes3;
            ArrayList<InputMethodInfo> imis;
            boolean includeAuxiliarySubtypes4;
            int j;
            ArrayList<InputMethodInfo> imis2;
            int subtypeCount;
            ArrayList<InputMethodInfo> imis3 = this.mSettings.getEnabledInputMethodListLocked();
            if (imis3.isEmpty()) {
                return Collections.emptyList();
            }
            if (!isScreenLocked || !includeAuxiliarySubtypes) {
                includeAuxiliarySubtypes2 = includeAuxiliarySubtypes;
            } else {
                includeAuxiliarySubtypes2 = false;
            }
            ArrayList<ImeSubtypeListItem> imList = new ArrayList<>();
            int numImes = imis3.size();
            int i = 0;
            while (i < numImes) {
                InputMethodInfo imi = imis3.get(i);
                List<InputMethodSubtype> explicitlyOrImplicitlyEnabledSubtypeList = this.mSettings.getEnabledInputMethodSubtypeListLocked(this.mContext, imi, true);
                ArraySet<String> enabledSubtypeSet = new ArraySet<>();
                for (InputMethodSubtype subtype : explicitlyOrImplicitlyEnabledSubtypeList) {
                    enabledSubtypeSet.add(String.valueOf(subtype.hashCode()));
                }
                CharSequence imeLabel = imi.loadLabel(this.mPm);
                if (enabledSubtypeSet.size() > 0) {
                    int subtypeCount2 = imi.getSubtypeCount();
                    int j2 = 0;
                    while (j2 < subtypeCount2) {
                        InputMethodSubtype subtype2 = imi.getSubtypeAt(j2);
                        String subtypeHashCode = String.valueOf(subtype2.hashCode());
                        if (!enabledSubtypeSet.contains(subtypeHashCode)) {
                            imis2 = imis3;
                            includeAuxiliarySubtypes4 = includeAuxiliarySubtypes2;
                            j = j2;
                            subtypeCount = subtypeCount2;
                        } else if (includeAuxiliarySubtypes2 || !subtype2.isAuxiliary()) {
                            imis2 = imis3;
                            includeAuxiliarySubtypes4 = includeAuxiliarySubtypes2;
                            j = j2;
                            subtypeCount = subtypeCount2;
                            imList.add(new ImeSubtypeListItem(imeLabel, subtype2.overridesImplicitlyEnabledSubtype() ? null : subtype2.getDisplayName(this.mContext, imi.getPackageName(), imi.getServiceInfo().applicationInfo), imi, j2, subtype2.getLocale(), this.mSystemLocaleStr));
                            enabledSubtypeSet.remove(subtypeHashCode);
                        } else {
                            imis2 = imis3;
                            includeAuxiliarySubtypes4 = includeAuxiliarySubtypes2;
                            j = j2;
                            subtypeCount = subtypeCount2;
                        }
                        j2 = j + 1;
                        includeAuxiliarySubtypes2 = includeAuxiliarySubtypes4;
                        subtypeCount2 = subtypeCount;
                        imis3 = imis2;
                    }
                    imis = imis3;
                    includeAuxiliarySubtypes3 = includeAuxiliarySubtypes2;
                } else {
                    imis = imis3;
                    includeAuxiliarySubtypes3 = includeAuxiliarySubtypes2;
                    imList.add(new ImeSubtypeListItem(imeLabel, null, imi, -1, null, this.mSystemLocaleStr));
                }
                i++;
                includeAuxiliarySubtypes2 = includeAuxiliarySubtypes3;
                imis3 = imis;
            }
            Collections.sort(imList);
            return imList;
        }
    }

    /* access modifiers changed from: private */
    public static int calculateSubtypeId(InputMethodInfo imi, InputMethodSubtype subtype) {
        if (subtype != null) {
            return InputMethodUtils.getSubtypeIdFromHashCode(imi, subtype.hashCode());
        }
        return -1;
    }

    /* access modifiers changed from: private */
    public static class StaticRotationList {
        private final List<ImeSubtypeListItem> mImeSubtypeList;

        public StaticRotationList(List<ImeSubtypeListItem> imeSubtypeList) {
            this.mImeSubtypeList = imeSubtypeList;
        }

        private int getIndex(InputMethodInfo imi, InputMethodSubtype subtype) {
            int currentSubtypeId = InputMethodSubtypeSwitchingController.calculateSubtypeId(imi, subtype);
            int N = this.mImeSubtypeList.size();
            for (int i = 0; i < N; i++) {
                ImeSubtypeListItem isli = this.mImeSubtypeList.get(i);
                if (imi.equals(isli.mImi) && isli.mSubtypeId == currentSubtypeId) {
                    return i;
                }
            }
            return -1;
        }

        public ImeSubtypeListItem getNextInputMethodLocked(boolean onlyCurrentIme, InputMethodInfo imi, InputMethodSubtype subtype) {
            int currentIndex;
            if (imi == null || this.mImeSubtypeList.size() <= 1 || (currentIndex = getIndex(imi, subtype)) < 0) {
                return null;
            }
            int N = this.mImeSubtypeList.size();
            for (int offset = 1; offset < N; offset++) {
                ImeSubtypeListItem candidate = this.mImeSubtypeList.get((currentIndex + offset) % N);
                if (!onlyCurrentIme || imi.equals(candidate.mImi)) {
                    return candidate;
                }
            }
            return null;
        }

        /* access modifiers changed from: protected */
        public void dump(Printer pw, String prefix) {
            int N = this.mImeSubtypeList.size();
            for (int i = 0; i < N; i++) {
                pw.println(prefix + "rank=" + i + " item=" + this.mImeSubtypeList.get(i));
            }
        }
    }

    /* access modifiers changed from: private */
    public static class DynamicRotationList {
        private static final String TAG = DynamicRotationList.class.getSimpleName();
        private final List<ImeSubtypeListItem> mImeSubtypeList;
        private final int[] mUsageHistoryOfSubtypeListItemIndex;

        private DynamicRotationList(List<ImeSubtypeListItem> imeSubtypeListItems) {
            this.mImeSubtypeList = imeSubtypeListItems;
            this.mUsageHistoryOfSubtypeListItemIndex = new int[this.mImeSubtypeList.size()];
            int N = this.mImeSubtypeList.size();
            for (int i = 0; i < N; i++) {
                this.mUsageHistoryOfSubtypeListItemIndex[i] = i;
            }
        }

        private int getUsageRank(InputMethodInfo imi, InputMethodSubtype subtype) {
            int currentSubtypeId = InputMethodSubtypeSwitchingController.calculateSubtypeId(imi, subtype);
            int N = this.mUsageHistoryOfSubtypeListItemIndex.length;
            for (int usageRank = 0; usageRank < N; usageRank++) {
                ImeSubtypeListItem subtypeListItem = this.mImeSubtypeList.get(this.mUsageHistoryOfSubtypeListItemIndex[usageRank]);
                if (subtypeListItem.mImi.equals(imi) && subtypeListItem.mSubtypeId == currentSubtypeId) {
                    return usageRank;
                }
            }
            return -1;
        }

        public void onUserAction(InputMethodInfo imi, InputMethodSubtype subtype) {
            int currentUsageRank = getUsageRank(imi, subtype);
            if (currentUsageRank > 0) {
                int[] iArr = this.mUsageHistoryOfSubtypeListItemIndex;
                int currentItemIndex = iArr[currentUsageRank];
                System.arraycopy(iArr, 0, iArr, 1, currentUsageRank);
                this.mUsageHistoryOfSubtypeListItemIndex[0] = currentItemIndex;
            }
        }

        public ImeSubtypeListItem getNextInputMethodLocked(boolean onlyCurrentIme, InputMethodInfo imi, InputMethodSubtype subtype) {
            int currentUsageRank = getUsageRank(imi, subtype);
            if (currentUsageRank < 0) {
                return null;
            }
            int N = this.mUsageHistoryOfSubtypeListItemIndex.length;
            for (int i = 1; i < N; i++) {
                ImeSubtypeListItem subtypeListItem = this.mImeSubtypeList.get(this.mUsageHistoryOfSubtypeListItemIndex[(currentUsageRank + i) % N]);
                if (!onlyCurrentIme || imi.equals(subtypeListItem.mImi)) {
                    return subtypeListItem;
                }
            }
            return null;
        }

        /* access modifiers changed from: protected */
        public void dump(Printer pw, String prefix) {
            int i = 0;
            while (true) {
                int[] iArr = this.mUsageHistoryOfSubtypeListItemIndex;
                if (i < iArr.length) {
                    int rank = iArr[i];
                    pw.println(prefix + "rank=" + rank + " item=" + this.mImeSubtypeList.get(i));
                    i++;
                } else {
                    return;
                }
            }
        }
    }

    @VisibleForTesting
    public static class ControllerImpl {
        private final DynamicRotationList mSwitchingAwareRotationList;
        private final StaticRotationList mSwitchingUnawareRotationList;

        public static ControllerImpl createFrom(ControllerImpl currentInstance, List<ImeSubtypeListItem> sortedEnabledItems) {
            StaticRotationList staticRotationList;
            DynamicRotationList dynamicRotationList;
            DynamicRotationList switchingAwareRotationList = null;
            List<ImeSubtypeListItem> switchingAwareImeSubtypes = filterImeSubtypeList(sortedEnabledItems, true);
            if (!(currentInstance == null || (dynamicRotationList = currentInstance.mSwitchingAwareRotationList) == null || !Objects.equals(dynamicRotationList.mImeSubtypeList, switchingAwareImeSubtypes))) {
                switchingAwareRotationList = currentInstance.mSwitchingAwareRotationList;
            }
            if (switchingAwareRotationList == null) {
                switchingAwareRotationList = new DynamicRotationList(switchingAwareImeSubtypes);
            }
            StaticRotationList switchingUnawareRotationList = null;
            List<ImeSubtypeListItem> switchingUnawareImeSubtypes = filterImeSubtypeList(sortedEnabledItems, false);
            if (!(currentInstance == null || (staticRotationList = currentInstance.mSwitchingUnawareRotationList) == null || !Objects.equals(staticRotationList.mImeSubtypeList, switchingUnawareImeSubtypes))) {
                switchingUnawareRotationList = currentInstance.mSwitchingUnawareRotationList;
            }
            if (switchingUnawareRotationList == null) {
                switchingUnawareRotationList = new StaticRotationList(switchingUnawareImeSubtypes);
            }
            return new ControllerImpl(switchingAwareRotationList, switchingUnawareRotationList);
        }

        private ControllerImpl(DynamicRotationList switchingAwareRotationList, StaticRotationList switchingUnawareRotationList) {
            this.mSwitchingAwareRotationList = switchingAwareRotationList;
            this.mSwitchingUnawareRotationList = switchingUnawareRotationList;
        }

        public ImeSubtypeListItem getNextInputMethod(boolean onlyCurrentIme, InputMethodInfo imi, InputMethodSubtype subtype) {
            if (imi == null) {
                return null;
            }
            if (imi.supportsSwitchingToNextInputMethod()) {
                return this.mSwitchingAwareRotationList.getNextInputMethodLocked(onlyCurrentIme, imi, subtype);
            }
            return this.mSwitchingUnawareRotationList.getNextInputMethodLocked(onlyCurrentIme, imi, subtype);
        }

        public void onUserActionLocked(InputMethodInfo imi, InputMethodSubtype subtype) {
            if (imi != null && imi.supportsSwitchingToNextInputMethod()) {
                this.mSwitchingAwareRotationList.onUserAction(imi, subtype);
            }
        }

        private static List<ImeSubtypeListItem> filterImeSubtypeList(List<ImeSubtypeListItem> items, boolean supportsSwitchingToNextInputMethod) {
            ArrayList<ImeSubtypeListItem> result = new ArrayList<>();
            int ALL_ITEMS_COUNT = items.size();
            for (int i = 0; i < ALL_ITEMS_COUNT; i++) {
                ImeSubtypeListItem item = items.get(i);
                if (item.mImi.supportsSwitchingToNextInputMethod() == supportsSwitchingToNextInputMethod) {
                    result.add(item);
                }
            }
            return result;
        }

        /* access modifiers changed from: protected */
        public void dump(Printer pw) {
            pw.println("    mSwitchingAwareRotationList:");
            this.mSwitchingAwareRotationList.dump(pw, "      ");
            pw.println("    mSwitchingUnawareRotationList:");
            this.mSwitchingUnawareRotationList.dump(pw, "      ");
        }
    }

    private InputMethodSubtypeSwitchingController(InputMethodUtils.InputMethodSettings settings, Context context) {
        this.mSettings = settings;
        resetCircularListLocked(context);
    }

    public static InputMethodSubtypeSwitchingController createInstanceLocked(InputMethodUtils.InputMethodSettings settings, Context context) {
        return new InputMethodSubtypeSwitchingController(settings, context);
    }

    public void onUserActionLocked(InputMethodInfo imi, InputMethodSubtype subtype) {
        ControllerImpl controllerImpl = this.mController;
        if (controllerImpl != null) {
            controllerImpl.onUserActionLocked(imi, subtype);
        }
    }

    public void resetCircularListLocked(Context context) {
        this.mSubtypeList = new InputMethodAndSubtypeList(context, this.mSettings);
        this.mController = ControllerImpl.createFrom(this.mController, this.mSubtypeList.getSortedInputMethodAndSubtypeList(false, false));
    }

    public ImeSubtypeListItem getNextInputMethodLocked(boolean onlyCurrentIme, InputMethodInfo imi, InputMethodSubtype subtype) {
        ControllerImpl controllerImpl = this.mController;
        if (controllerImpl == null) {
            return null;
        }
        return controllerImpl.getNextInputMethod(onlyCurrentIme, imi, subtype);
    }

    public List<ImeSubtypeListItem> getSortedInputMethodAndSubtypeListLocked(boolean includingAuxiliarySubtypes, boolean isScreenLocked) {
        return this.mSubtypeList.getSortedInputMethodAndSubtypeList(includingAuxiliarySubtypes, isScreenLocked);
    }

    public void dump(Printer pw) {
        ControllerImpl controllerImpl = this.mController;
        if (controllerImpl != null) {
            controllerImpl.dump(pw);
        } else {
            pw.println("    mController=null");
        }
    }
}
