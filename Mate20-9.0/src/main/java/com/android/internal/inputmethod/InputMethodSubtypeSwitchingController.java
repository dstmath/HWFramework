package com.android.internal.inputmethod;

import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Printer;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodSubtype;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.inputmethod.InputMethodUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TreeMap;

public class InputMethodSubtypeSwitchingController {
    private static final boolean DEBUG = false;
    private static final int NOT_A_SUBTYPE_ID = -1;
    private static final String TAG = InputMethodSubtypeSwitchingController.class.getSimpleName();
    private ControllerImpl mController;
    private final InputMethodUtils.InputMethodSettings mSettings;
    private InputMethodAndSubtypeList mSubtypeList;

    @VisibleForTesting
    public static class ControllerImpl {
        private final DynamicRotationList mSwitchingAwareRotationList;
        private final StaticRotationList mSwitchingUnawareRotationList;

        public static ControllerImpl createFrom(ControllerImpl currentInstance, List<ImeSubtypeListItem> sortedEnabledItems) {
            DynamicRotationList switchingAwareRotationList = null;
            List<ImeSubtypeListItem> switchingAwareImeSubtypes = filterImeSubtypeList(sortedEnabledItems, true);
            if (!(currentInstance == null || currentInstance.mSwitchingAwareRotationList == null || !Objects.equals(currentInstance.mSwitchingAwareRotationList.mImeSubtypeList, switchingAwareImeSubtypes))) {
                switchingAwareRotationList = currentInstance.mSwitchingAwareRotationList;
            }
            if (switchingAwareRotationList == null) {
                switchingAwareRotationList = new DynamicRotationList(switchingAwareImeSubtypes);
            }
            StaticRotationList switchingUnawareRotationList = null;
            List<ImeSubtypeListItem> switchingUnawareImeSubtypes = filterImeSubtypeList(sortedEnabledItems, false);
            if (!(currentInstance == null || currentInstance.mSwitchingUnawareRotationList == null || !Objects.equals(currentInstance.mSwitchingUnawareRotationList.mImeSubtypeList, switchingUnawareImeSubtypes))) {
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

        public ImeSubtypeListItem getNextInputMethod(boolean onlyCurrentIme, InputMethodInfo imi, InputMethodSubtype subtype, boolean forward) {
            if (imi == null) {
                return null;
            }
            if (imi.supportsSwitchingToNextInputMethod()) {
                return this.mSwitchingAwareRotationList.getNextInputMethodLocked(onlyCurrentIme, imi, subtype, forward);
            }
            return this.mSwitchingUnawareRotationList.getNextInputMethodLocked(onlyCurrentIme, imi, subtype, forward);
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

    private static class DynamicRotationList {
        private static final String TAG = DynamicRotationList.class.getSimpleName();
        /* access modifiers changed from: private */
        public final List<ImeSubtypeListItem> mImeSubtypeList;
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
                int currentItemIndex = this.mUsageHistoryOfSubtypeListItemIndex[currentUsageRank];
                System.arraycopy(this.mUsageHistoryOfSubtypeListItemIndex, 0, this.mUsageHistoryOfSubtypeListItemIndex, 1, currentUsageRank);
                this.mUsageHistoryOfSubtypeListItemIndex[0] = currentItemIndex;
            }
        }

        public ImeSubtypeListItem getNextInputMethodLocked(boolean onlyCurrentIme, InputMethodInfo imi, InputMethodSubtype subtype, boolean forward) {
            int currentUsageRank = getUsageRank(imi, subtype);
            if (currentUsageRank < 0) {
                return null;
            }
            int N = this.mUsageHistoryOfSubtypeListItemIndex.length;
            for (int i = 1; i < N; i++) {
                ImeSubtypeListItem subtypeListItem = this.mImeSubtypeList.get(this.mUsageHistoryOfSubtypeListItemIndex[(currentUsageRank + (forward ? i : N - i)) % N]);
                if (!onlyCurrentIme || imi.equals(subtypeListItem.mImi)) {
                    return subtypeListItem;
                }
            }
            return null;
        }

        /* access modifiers changed from: protected */
        public void dump(Printer pw, String prefix) {
            for (int rank : this.mUsageHistoryOfSubtypeListItemIndex) {
                pw.println(prefix + "rank=" + rank + " item=" + this.mImeSubtypeList.get(i));
            }
        }
    }

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
                return (empty1) - (empty2);
            }
            return c1.toString().compareTo(c2.toString());
        }

        public int compareTo(ImeSubtypeListItem other) {
            int result = compareNullableCharSequences(this.mImeName, other.mImeName);
            if (result != 0) {
                return result;
            }
            int i = 0;
            int result2 = (this.mIsSystemLocale ? -1 : 0) - (other.mIsSystemLocale ? -1 : 0);
            if (result2 != 0) {
                return result2;
            }
            int i2 = this.mIsSystemLanguage ? -1 : 0;
            if (other.mIsSystemLanguage) {
                i = -1;
            }
            int result3 = i2 - i;
            if (result3 != 0) {
                return result3;
            }
            return compareNullableCharSequences(this.mSubtypeName, other.mSubtypeName);
        }

        public String toString() {
            return "ImeSubtypeListItem{mImeName=" + this.mImeName + " mSubtypeName=" + this.mSubtypeName + " mSubtypeId=" + this.mSubtypeId + " mIsSystemLocale=" + this.mIsSystemLocale + " mIsSystemLanguage=" + this.mIsSystemLanguage + "}";
        }

        public boolean equals(Object o) {
            boolean z = true;
            if (o == this) {
                return true;
            }
            if (!(o instanceof ImeSubtypeListItem)) {
                return false;
            }
            ImeSubtypeListItem that = (ImeSubtypeListItem) o;
            if (!Objects.equals(this.mImi, that.mImi) || this.mSubtypeId != that.mSubtypeId) {
                z = false;
            }
            return z;
        }
    }

    private static class InputMethodAndSubtypeList {
        private final Context mContext;
        /* access modifiers changed from: private */
        public final PackageManager mPm;
        private final InputMethodUtils.InputMethodSettings mSettings;
        private final TreeMap<InputMethodInfo, List<InputMethodSubtype>> mSortedImmis = new TreeMap<>(new Comparator<InputMethodInfo>() {
            public int compare(InputMethodInfo imi1, InputMethodInfo imi2) {
                if (imi2 == null) {
                    return 0;
                }
                if (imi1 == null) {
                    return 1;
                }
                if (InputMethodAndSubtypeList.this.mPm == null) {
                    return imi1.getId().compareTo(imi2.getId());
                }
                return (imi1.loadLabel(InputMethodAndSubtypeList.this.mPm) + "/" + imi1.getId()).toString().compareTo((imi2.loadLabel(InputMethodAndSubtypeList.this.mPm) + "/" + imi2.getId()).toString());
            }
        });
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
            HashMap<InputMethodInfo, List<InputMethodSubtype>> immis;
            int j;
            HashMap<InputMethodInfo, List<InputMethodSubtype>> immis2;
            int subtypeCount;
            CharSequence displayName;
            ArrayList<ImeSubtypeListItem> imList = new ArrayList<>();
            HashMap<InputMethodInfo, List<InputMethodSubtype>> immis3 = this.mSettings.getExplicitlyOrImplicitlyEnabledInputMethodsAndSubtypeListLocked(this.mContext);
            if (immis3 == null) {
            } else if (immis3.size() == 0) {
                HashMap<InputMethodInfo, List<InputMethodSubtype>> hashMap = immis3;
            } else {
                if (!isScreenLocked || !includeAuxiliarySubtypes) {
                    includeAuxiliarySubtypes2 = includeAuxiliarySubtypes;
                } else {
                    includeAuxiliarySubtypes2 = false;
                }
                this.mSortedImmis.clear();
                this.mSortedImmis.putAll(immis3);
                for (InputMethodInfo imi : this.mSortedImmis.keySet()) {
                    if (imi != null) {
                        HashSet hashSet = new HashSet();
                        for (InputMethodSubtype subtype : immis3.get(imi)) {
                            hashSet.add(String.valueOf(subtype.hashCode()));
                        }
                        CharSequence imeLabel = imi.loadLabel(this.mPm);
                        if (hashSet.size() > 0) {
                            int subtypeCount2 = imi.getSubtypeCount();
                            int j2 = 0;
                            while (true) {
                                int j3 = j2;
                                if (j3 >= subtypeCount2) {
                                    break;
                                }
                                InputMethodSubtype subtype2 = imi.getSubtypeAt(j3);
                                String subtypeHashCode = String.valueOf(subtype2.hashCode());
                                if (!hashSet.contains(subtypeHashCode)) {
                                    immis2 = immis3;
                                    j = j3;
                                    subtypeCount = subtypeCount2;
                                } else if (includeAuxiliarySubtypes2 || !subtype2.isAuxiliary()) {
                                    if (subtype2.overridesImplicitlyEnabledSubtype()) {
                                        displayName = null;
                                    } else {
                                        displayName = subtype2.getDisplayName(this.mContext, imi.getPackageName(), imi.getServiceInfo().applicationInfo);
                                    }
                                    CharSequence subtypeLabel = displayName;
                                    String locale = subtype2.getLocale();
                                    immis2 = immis3;
                                    ImeSubtypeListItem imeSubtypeListItem = r7;
                                    InputMethodSubtype inputMethodSubtype = subtype2;
                                    int i = j3;
                                    j = j3;
                                    String str = locale;
                                    subtypeCount = subtypeCount2;
                                    ImeSubtypeListItem imeSubtypeListItem2 = new ImeSubtypeListItem(imeLabel, subtypeLabel, imi, i, str, this.mSystemLocaleStr);
                                    imList.add(imeSubtypeListItem);
                                    hashSet.remove(subtypeHashCode);
                                } else {
                                    immis2 = immis3;
                                    j = j3;
                                    subtypeCount = subtypeCount2;
                                }
                                j2 = j + 1;
                                subtypeCount2 = subtypeCount;
                                immis3 = immis2;
                            }
                            immis = immis3;
                        } else {
                            immis = immis3;
                            ImeSubtypeListItem imeSubtypeListItem3 = new ImeSubtypeListItem(imeLabel, null, imi, -1, null, this.mSystemLocaleStr);
                            imList.add(imeSubtypeListItem3);
                        }
                        immis3 = immis;
                    }
                }
                Collections.sort(imList);
                return imList;
            }
            return Collections.emptyList();
        }
    }

    private static class StaticRotationList {
        /* access modifiers changed from: private */
        public final List<ImeSubtypeListItem> mImeSubtypeList;

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

        public ImeSubtypeListItem getNextInputMethodLocked(boolean onlyCurrentIme, InputMethodInfo imi, InputMethodSubtype subtype, boolean forward) {
            if (imi == null) {
                return null;
            }
            if (this.mImeSubtypeList.size() <= 1) {
                return null;
            }
            int currentIndex = getIndex(imi, subtype);
            if (currentIndex < 0) {
                return null;
            }
            int N = this.mImeSubtypeList.size();
            for (int i = 1; i < N; i++) {
                ImeSubtypeListItem candidate = this.mImeSubtypeList.get((currentIndex + (forward ? i : N - i)) % N);
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
    public static int calculateSubtypeId(InputMethodInfo imi, InputMethodSubtype subtype) {
        if (subtype != null) {
            return InputMethodUtils.getSubtypeIdFromHashCode(imi, subtype.hashCode());
        }
        return -1;
    }

    private InputMethodSubtypeSwitchingController(InputMethodUtils.InputMethodSettings settings, Context context) {
        this.mSettings = settings;
        resetCircularListLocked(context);
    }

    public static InputMethodSubtypeSwitchingController createInstanceLocked(InputMethodUtils.InputMethodSettings settings, Context context) {
        return new InputMethodSubtypeSwitchingController(settings, context);
    }

    public void onUserActionLocked(InputMethodInfo imi, InputMethodSubtype subtype) {
        if (this.mController != null) {
            this.mController.onUserActionLocked(imi, subtype);
        }
    }

    public void resetCircularListLocked(Context context) {
        this.mSubtypeList = new InputMethodAndSubtypeList(context, this.mSettings);
        this.mController = ControllerImpl.createFrom(this.mController, this.mSubtypeList.getSortedInputMethodAndSubtypeList(false, false));
    }

    public ImeSubtypeListItem getNextInputMethodLocked(boolean onlyCurrentIme, InputMethodInfo imi, InputMethodSubtype subtype, boolean forward) {
        if (this.mController == null) {
            return null;
        }
        return this.mController.getNextInputMethod(onlyCurrentIme, imi, subtype, forward);
    }

    public List<ImeSubtypeListItem> getSortedInputMethodAndSubtypeListLocked(boolean includingAuxiliarySubtypes, boolean isScreenLocked) {
        return this.mSubtypeList.getSortedInputMethodAndSubtypeList(includingAuxiliarySubtypes, isScreenLocked);
    }

    public void dump(Printer pw) {
        if (this.mController != null) {
            this.mController.dump(pw);
        } else {
            pw.println("    mController=null");
        }
    }
}
