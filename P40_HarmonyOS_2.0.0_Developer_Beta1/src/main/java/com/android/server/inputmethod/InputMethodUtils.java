package com.android.server.inputmethod;

import android.app.AppOpsManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.res.Resources;
import android.os.LocaleList;
import android.os.RemoteException;
import android.os.UserManagerInternal;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.IntArray;
import android.util.Pair;
import android.util.Printer;
import android.util.Slog;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodSubtype;
import android.view.inputmethod.InputMethodSystemProperty;
import android.view.textservice.SpellCheckerInfo;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.LocalServices;
import com.android.server.inputmethod.LocaleUtils;
import com.android.server.textservices.TextServicesManagerInternal;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

public class InputMethodUtils {
    private static final String DB_INPUT_METHOD_AUTO_CHANGE = "input_method_auto_change";
    public static final boolean DEBUG = false;
    private static final Locale ENGLISH_LOCALE = new Locale("en");
    private static final char INPUT_METHOD_SEPARATOR = ':';
    private static final char INPUT_METHOD_SUBTYPE_SEPARATOR = ';';
    static final int NOT_A_SUBTYPE_ID = -1;
    private static final String NOT_A_SUBTYPE_ID_STR = String.valueOf(-1);
    private static final Locale[] SEARCH_ORDER_OF_FALLBACK_LOCALES = {Locale.ENGLISH, Locale.US, Locale.UK};
    private static final String SUBTYPE_MODE_ANY = null;
    static final String SUBTYPE_MODE_KEYBOARD = "keyboard";
    private static final String TAG = "InputMethodUtils";
    private static final String TAG_ENABLED_WHEN_DEFAULT_IS_NOT_ASCII_CAPABLE = "EnabledWhenDefaultIsNotAsciiCapable";
    private static final Object sCacheLock = new Object();
    @GuardedBy({"sCacheLock"})
    private static InputMethodInfo sCachedInputMethodInfo;
    @GuardedBy({"sCacheLock"})
    private static ArrayList<InputMethodSubtype> sCachedResult;
    @GuardedBy({"sCacheLock"})
    private static LocaleList sCachedSystemLocales;
    private static final LocaleUtils.LocaleExtractor<InputMethodSubtype> sSubtypeToLocale = new LocaleUtils.LocaleExtractor<InputMethodSubtype>() {
        /* class com.android.server.inputmethod.InputMethodUtils.AnonymousClass1 */

        public Locale get(InputMethodSubtype source) {
            if (source != null) {
                return source.getLocaleObject();
            }
            return null;
        }
    };

    private InputMethodUtils() {
    }

    static String getApiCallStack() {
        String apiCallStack = "";
        try {
            throw new RuntimeException();
        } catch (RuntimeException e) {
            StackTraceElement[] frames = e.getStackTrace();
            for (int j = 1; j < frames.length; j++) {
                String tempCallStack = frames[j].toString();
                if (!TextUtils.isEmpty(apiCallStack) && tempCallStack.indexOf("Transact(") >= 0) {
                    break;
                }
                apiCallStack = tempCallStack;
            }
            return apiCallStack;
        }
    }

    /* access modifiers changed from: private */
    public static boolean isSystemImeThatHasSubtypeOf(InputMethodInfo imi, Context context, boolean checkDefaultAttribute, Locale requiredLocale, boolean checkCountry, String requiredSubtypeMode) {
        if (!imi.isSystem()) {
            return false;
        }
        if ((!checkDefaultAttribute || imi.isDefault(context)) && containsSubtypeOf(imi, requiredLocale, checkCountry, requiredSubtypeMode)) {
            return true;
        }
        return false;
    }

    private static Locale getFallbackLocaleForDefaultIme(ArrayList<InputMethodInfo> imis, Context context) {
        Locale[] localeArr = SEARCH_ORDER_OF_FALLBACK_LOCALES;
        for (Locale fallbackLocale : localeArr) {
            for (int i = 0; i < imis.size(); i++) {
                if (isSystemImeThatHasSubtypeOf(imis.get(i), context, true, fallbackLocale, true, SUBTYPE_MODE_KEYBOARD)) {
                    return fallbackLocale;
                }
            }
        }
        Locale[] localeArr2 = SEARCH_ORDER_OF_FALLBACK_LOCALES;
        for (Locale fallbackLocale2 : localeArr2) {
            for (int i2 = 0; i2 < imis.size(); i2++) {
                if (isSystemImeThatHasSubtypeOf(imis.get(i2), context, false, fallbackLocale2, true, SUBTYPE_MODE_KEYBOARD)) {
                    return fallbackLocale2;
                }
            }
        }
        Slog.w(TAG, "Found no fallback locale. imis=" + Arrays.toString(imis.toArray()));
        return null;
    }

    /* access modifiers changed from: private */
    public static boolean isSystemAuxilialyImeThatHasAutomaticSubtype(InputMethodInfo imi, Context context, boolean checkDefaultAttribute) {
        if (!imi.isSystem()) {
            return false;
        }
        if ((checkDefaultAttribute && !imi.isDefault(context)) || !imi.isAuxiliaryIme()) {
            return false;
        }
        int subtypeCount = imi.getSubtypeCount();
        for (int i = 0; i < subtypeCount; i++) {
            if (imi.getSubtypeAt(i).overridesImplicitlyEnabledSubtype()) {
                return true;
            }
        }
        return false;
    }

    private static Locale getSystemLocaleFromContext(Context context) {
        try {
            return context.getResources().getConfiguration().locale;
        } catch (Resources.NotFoundException e) {
            return null;
        }
    }

    /* access modifiers changed from: private */
    public static final class InputMethodListBuilder {
        private final LinkedHashSet<InputMethodInfo> mInputMethodSet;

        private InputMethodListBuilder() {
            this.mInputMethodSet = new LinkedHashSet<>();
        }

        /* access modifiers changed from: package-private */
        public InputMethodListBuilder fillImes(ArrayList<InputMethodInfo> imis, Context context, boolean checkDefaultAttribute, Locale locale, boolean checkCountry, String requiredSubtypeMode) {
            for (int i = 0; i < imis.size(); i++) {
                InputMethodInfo imi = imis.get(i);
                if (InputMethodUtils.isSystemImeThatHasSubtypeOf(imi, context, checkDefaultAttribute, locale, checkCountry, requiredSubtypeMode)) {
                    this.mInputMethodSet.add(imi);
                }
            }
            return this;
        }

        /* access modifiers changed from: package-private */
        public InputMethodListBuilder fillAuxiliaryImes(ArrayList<InputMethodInfo> imis, Context context) {
            Iterator<InputMethodInfo> it = this.mInputMethodSet.iterator();
            while (it.hasNext()) {
                if (it.next().isAuxiliaryIme()) {
                    return this;
                }
            }
            boolean added = false;
            for (int i = 0; i < imis.size(); i++) {
                InputMethodInfo imi = imis.get(i);
                if (InputMethodUtils.isSystemAuxilialyImeThatHasAutomaticSubtype(imi, context, true)) {
                    this.mInputMethodSet.add(imi);
                    added = true;
                }
            }
            if (added) {
                return this;
            }
            for (int i2 = 0; i2 < imis.size(); i2++) {
                InputMethodInfo imi2 = imis.get(i2);
                if (InputMethodUtils.isSystemAuxilialyImeThatHasAutomaticSubtype(imi2, context, false)) {
                    this.mInputMethodSet.add(imi2);
                }
            }
            return this;
        }

        public boolean isEmpty() {
            return this.mInputMethodSet.isEmpty();
        }

        public ArrayList<InputMethodInfo> build() {
            return new ArrayList<>(this.mInputMethodSet);
        }
    }

    private static InputMethodListBuilder getMinimumKeyboardSetWithSystemLocale(ArrayList<InputMethodInfo> imis, Context context, Locale systemLocale, Locale fallbackLocale) {
        InputMethodListBuilder builder = new InputMethodListBuilder();
        builder.fillImes(imis, context, true, systemLocale, true, SUBTYPE_MODE_KEYBOARD);
        if (!builder.isEmpty()) {
            return builder;
        }
        builder.fillImes(imis, context, true, systemLocale, false, SUBTYPE_MODE_KEYBOARD);
        if (!builder.isEmpty()) {
            return builder;
        }
        builder.fillImes(imis, context, true, fallbackLocale, true, SUBTYPE_MODE_KEYBOARD);
        if (!builder.isEmpty()) {
            return builder;
        }
        builder.fillImes(imis, context, true, fallbackLocale, false, SUBTYPE_MODE_KEYBOARD);
        if (!builder.isEmpty()) {
            return builder;
        }
        builder.fillImes(imis, context, false, fallbackLocale, true, SUBTYPE_MODE_KEYBOARD);
        if (!builder.isEmpty()) {
            return builder;
        }
        builder.fillImes(imis, context, false, fallbackLocale, false, SUBTYPE_MODE_KEYBOARD);
        if (!builder.isEmpty()) {
            return builder;
        }
        Slog.w(TAG, "No software keyboard is found. imis=" + Arrays.toString(imis.toArray()) + " systemLocale=" + systemLocale + " fallbackLocale=" + fallbackLocale);
        return builder;
    }

    static ArrayList<InputMethodInfo> getDefaultEnabledImes(Context context, ArrayList<InputMethodInfo> imis, boolean onlyMinimum) {
        Locale fallbackLocale = getFallbackLocaleForDefaultIme(imis, context);
        Locale systemLocale = getSystemLocaleFromContext(context);
        InputMethodListBuilder builder = getMinimumKeyboardSetWithSystemLocale(imis, context, systemLocale, fallbackLocale);
        if (!onlyMinimum) {
            builder.fillImes(imis, context, true, systemLocale, true, SUBTYPE_MODE_ANY).fillAuxiliaryImes(imis, context);
        }
        return builder.build();
    }

    static ArrayList<InputMethodInfo> getDefaultEnabledImes(Context context, ArrayList<InputMethodInfo> imis) {
        return getDefaultEnabledImes(context, imis, false);
    }

    static boolean containsSubtypeOf(InputMethodInfo imi, Locale locale, boolean checkCountry, String mode) {
        if (locale == null) {
            return false;
        }
        int N = imi.getSubtypeCount();
        for (int i = 0; i < N; i++) {
            InputMethodSubtype subtype = imi.getSubtypeAt(i);
            if (checkCountry) {
                Locale subtypeLocale = subtype.getLocaleObject();
                if (subtypeLocale == null) {
                    continue;
                } else if (!TextUtils.equals(subtypeLocale.getLanguage(), locale.getLanguage())) {
                    continue;
                } else if (!TextUtils.equals(subtypeLocale.getCountry(), locale.getCountry())) {
                    continue;
                }
            } else if (!TextUtils.equals(new Locale(getLanguageFromLocaleString(subtype.getLocale())).getLanguage(), locale.getLanguage())) {
                continue;
            }
            if (mode == SUBTYPE_MODE_ANY || TextUtils.isEmpty(mode) || mode.equalsIgnoreCase(subtype.getMode())) {
                return true;
            }
        }
        return false;
    }

    static ArrayList<InputMethodSubtype> getSubtypes(InputMethodInfo imi) {
        ArrayList<InputMethodSubtype> subtypes = new ArrayList<>();
        int subtypeCount = imi.getSubtypeCount();
        for (int i = 0; i < subtypeCount; i++) {
            subtypes.add(imi.getSubtypeAt(i));
        }
        return subtypes;
    }

    static InputMethodInfo getMostApplicableDefaultIME(List<InputMethodInfo> enabledImes) {
        if (enabledImes == null || enabledImes.isEmpty()) {
            return null;
        }
        int i = enabledImes.size();
        int firstFoundSystemIme = -1;
        while (i > 0) {
            i--;
            InputMethodInfo imi = enabledImes.get(i);
            if (!imi.isAuxiliaryIme()) {
                if (imi.isSystem() && containsSubtypeOf(imi, ENGLISH_LOCALE, false, SUBTYPE_MODE_KEYBOARD)) {
                    return imi;
                }
                if (firstFoundSystemIme < 0 && imi.isSystem()) {
                    firstFoundSystemIme = i;
                }
            }
        }
        return enabledImes.get(Math.max(firstFoundSystemIme, 0));
    }

    static boolean isValidSubtypeId(InputMethodInfo imi, int subtypeHashCode) {
        return getSubtypeIdFromHashCode(imi, subtypeHashCode) != -1;
    }

    static int getSubtypeIdFromHashCode(InputMethodInfo imi, int subtypeHashCode) {
        if (imi == null) {
            return -1;
        }
        int subtypeCount = imi.getSubtypeCount();
        for (int i = 0; i < subtypeCount; i++) {
            if (subtypeHashCode == imi.getSubtypeAt(i).hashCode()) {
                return i;
            }
        }
        return -1;
    }

    @VisibleForTesting
    static ArrayList<InputMethodSubtype> getImplicitlyApplicableSubtypesLocked(Resources res, InputMethodInfo imi) {
        LocaleList systemLocales = res.getConfiguration().getLocales();
        synchronized (sCacheLock) {
            if (!systemLocales.equals(sCachedSystemLocales) || sCachedInputMethodInfo != imi) {
                ArrayList<InputMethodSubtype> result = getImplicitlyApplicableSubtypesLockedImpl(res, imi);
                synchronized (sCacheLock) {
                    sCachedSystemLocales = systemLocales;
                    sCachedInputMethodInfo = imi;
                    sCachedResult = new ArrayList<>(result);
                }
                return result;
            }
            return new ArrayList<>(sCachedResult);
        }
    }

    private static ArrayList<InputMethodSubtype> getImplicitlyApplicableSubtypesLockedImpl(Resources res, InputMethodInfo imi) {
        List<InputMethodSubtype> subtypes = getSubtypes(imi);
        LocaleList systemLocales = res.getConfiguration().getLocales();
        String systemLocale = systemLocales.get(0).toString();
        if (TextUtils.isEmpty(systemLocale)) {
            return new ArrayList<>();
        }
        int numSubtypes = subtypes.size();
        ArrayMap<String, InputMethodSubtype> applicableModeAndSubtypesMap = new ArrayMap<>();
        for (int i = 0; i < numSubtypes; i++) {
            InputMethodSubtype subtype = subtypes.get(i);
            if (subtype.overridesImplicitlyEnabledSubtype()) {
                String mode = subtype.getMode();
                if (!applicableModeAndSubtypesMap.containsKey(mode)) {
                    applicableModeAndSubtypesMap.put(mode, subtype);
                }
            }
        }
        if (applicableModeAndSubtypesMap.size() > 0) {
            return new ArrayList<>(applicableModeAndSubtypesMap.values());
        }
        ArrayMap<String, ArrayList<InputMethodSubtype>> nonKeyboardSubtypesMap = new ArrayMap<>();
        ArrayList<InputMethodSubtype> keyboardSubtypes = new ArrayList<>();
        for (int i2 = 0; i2 < numSubtypes; i2++) {
            InputMethodSubtype subtype2 = subtypes.get(i2);
            String mode2 = subtype2.getMode();
            if (SUBTYPE_MODE_KEYBOARD.equals(mode2)) {
                keyboardSubtypes.add(subtype2);
            } else {
                if (!nonKeyboardSubtypesMap.containsKey(mode2)) {
                    nonKeyboardSubtypesMap.put(mode2, new ArrayList<>());
                }
                nonKeyboardSubtypesMap.get(mode2).add(subtype2);
            }
        }
        ArrayList<InputMethodSubtype> applicableSubtypes = new ArrayList<>();
        LocaleUtils.filterByLanguage(keyboardSubtypes, sSubtypeToLocale, systemLocales, applicableSubtypes);
        if (!applicableSubtypes.isEmpty()) {
            boolean hasAsciiCapableKeyboard = false;
            int numApplicationSubtypes = applicableSubtypes.size();
            int i3 = 0;
            while (true) {
                if (i3 >= numApplicationSubtypes) {
                    break;
                } else if (applicableSubtypes.get(i3).isAsciiCapable()) {
                    hasAsciiCapableKeyboard = true;
                    break;
                } else {
                    i3++;
                }
            }
            if (!hasAsciiCapableKeyboard) {
                int numKeyboardSubtypes = keyboardSubtypes.size();
                for (int i4 = 0; i4 < numKeyboardSubtypes; i4++) {
                    InputMethodSubtype subtype3 = keyboardSubtypes.get(i4);
                    if (SUBTYPE_MODE_KEYBOARD.equals(subtype3.getMode()) && subtype3.containsExtraValueKey(TAG_ENABLED_WHEN_DEFAULT_IS_NOT_ASCII_CAPABLE)) {
                        applicableSubtypes.add(subtype3);
                    }
                }
            }
        }
        if (applicableSubtypes.isEmpty()) {
            InputMethodSubtype lastResortKeyboardSubtype = findLastResortApplicableSubtypeLocked(res, subtypes, SUBTYPE_MODE_KEYBOARD, systemLocale, true);
            if (lastResortKeyboardSubtype != null) {
                applicableSubtypes.add(lastResortKeyboardSubtype);
            }
        }
        for (ArrayList<InputMethodSubtype> subtypeList : nonKeyboardSubtypesMap.values()) {
            LocaleUtils.filterByLanguage(subtypeList, sSubtypeToLocale, systemLocales, applicableSubtypes);
        }
        return applicableSubtypes;
    }

    private static String getLanguageFromLocaleString(String locale) {
        int idx = locale.indexOf(95);
        if (idx < 0) {
            return locale;
        }
        return locale.substring(0, idx);
    }

    static InputMethodSubtype findLastResortApplicableSubtypeLocked(Resources res, List<InputMethodSubtype> subtypes, String mode, String locale, boolean canIgnoreLocaleAsLastResort) {
        if (subtypes == null || subtypes.size() == 0) {
            return null;
        }
        if (TextUtils.isEmpty(locale)) {
            locale = res.getConfiguration().locale.toString();
        }
        String language = getLanguageFromLocaleString(locale);
        boolean partialMatchFound = false;
        InputMethodSubtype applicableSubtype = null;
        InputMethodSubtype firstMatchedModeSubtype = null;
        int N = subtypes.size();
        int i = 0;
        while (true) {
            if (i >= N) {
                break;
            }
            InputMethodSubtype subtype = subtypes.get(i);
            String subtypeLocale = subtype.getLocale();
            String subtypeLanguage = getLanguageFromLocaleString(subtypeLocale);
            if (mode == null || subtypes.get(i).getMode().equalsIgnoreCase(mode)) {
                if (firstMatchedModeSubtype == null) {
                    firstMatchedModeSubtype = subtype;
                }
                if (locale.equals(subtypeLocale)) {
                    applicableSubtype = subtype;
                    break;
                } else if (!partialMatchFound && language.equals(subtypeLanguage)) {
                    applicableSubtype = subtype;
                    partialMatchFound = true;
                }
            }
            i++;
        }
        if (applicableSubtype != null || !canIgnoreLocaleAsLastResort) {
            return applicableSubtype;
        }
        return firstMatchedModeSubtype;
    }

    static boolean canAddToLastInputMethod(InputMethodSubtype subtype) {
        if (subtype == null) {
            return true;
        }
        return true ^ subtype.isAuxiliary();
    }

    static void setNonSelectedSystemImesDisabledUntilUsed(IPackageManager packageManager, List<InputMethodInfo> enabledImis, int userId, String callingPackage) {
        String[] systemImesDisabledUntilUsed = Resources.getSystem().getStringArray(17236010);
        if (!(systemImesDisabledUntilUsed == null || systemImesDisabledUntilUsed.length == 0)) {
            SpellCheckerInfo currentSpellChecker = TextServicesManagerInternal.get().getCurrentSpellCheckerForUser(userId);
            for (String packageName : systemImesDisabledUntilUsed) {
                boolean enabledIme = false;
                int j = 0;
                while (true) {
                    if (j >= enabledImis.size()) {
                        break;
                    } else if (packageName.equals(enabledImis.get(j).getPackageName())) {
                        enabledIme = true;
                        break;
                    } else {
                        j++;
                    }
                }
                if (!enabledIme && (currentSpellChecker == null || !packageName.equals(currentSpellChecker.getPackageName()))) {
                    try {
                        ApplicationInfo ai = packageManager.getApplicationInfo(packageName, 32768, userId);
                        if (ai != null) {
                            boolean isSystemPackage = true;
                            if ((ai.flags & 1) == 0) {
                                isSystemPackage = false;
                            }
                            if (isSystemPackage) {
                                setDisabledUntilUsed(packageManager, packageName, userId, callingPackage);
                            }
                        }
                    } catch (RemoteException e) {
                        Slog.w(TAG, "getApplicationInfo failed. packageName=" + packageName + " userId=" + userId, e);
                    }
                }
            }
        }
    }

    private static void setDisabledUntilUsed(IPackageManager packageManager, String packageName, int userId, String callingPackage) {
        try {
            int state = packageManager.getApplicationEnabledSetting(packageName, userId);
            if (state == 0 || state == 1) {
                try {
                    packageManager.setApplicationEnabledSetting(packageName, 4, 0, userId, callingPackage);
                } catch (RemoteException e) {
                    Slog.w(TAG, "setApplicationEnabledSetting failed. packageName=" + packageName + " userId=" + userId + " callingPackage=" + callingPackage, e);
                }
            }
        } catch (RemoteException e2) {
            Slog.w(TAG, "getApplicationEnabledSetting failed. packageName=" + packageName + " userId=" + userId, e2);
        }
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0039: APUT  (r1v2 java.lang.CharSequence[]), (1 ??[boolean, int, float, short, byte, char]), (r3v3 java.lang.String) */
    static CharSequence getImeAndSubtypeDisplayName(Context context, InputMethodInfo imi, InputMethodSubtype subtype) {
        String str;
        CharSequence imiLabel = imi.loadLabel(context.getPackageManager());
        if (subtype == null) {
            return imiLabel;
        }
        CharSequence[] charSequenceArr = new CharSequence[2];
        charSequenceArr[0] = subtype.getDisplayName(context, imi.getPackageName(), imi.getServiceInfo().applicationInfo);
        if (TextUtils.isEmpty(imiLabel)) {
            str = "";
        } else {
            str = " - " + ((Object) imiLabel);
        }
        charSequenceArr[1] = str;
        return TextUtils.concat(charSequenceArr);
    }

    static boolean checkIfPackageBelongsToUid(AppOpsManager appOpsManager, int uid, String packageName) {
        try {
            appOpsManager.checkPackage(uid, packageName);
            return true;
        } catch (SecurityException e) {
            return false;
        }
    }

    public static class InputMethodSettings {
        private static final ArraySet<String> CLONE_TO_MANAGED_PROFILE = new ArraySet<>();
        private static final UserManagerInternal sUserManagerInternal = ((UserManagerInternal) LocalServices.getService(UserManagerInternal.class));
        private boolean mCopyOnWrite = false;
        private final ArrayMap<String, String> mCopyOnWriteDataStore = new ArrayMap<>();
        private int[] mCurrentProfileIds = new int[0];
        private int mCurrentUserId;
        private String mEnabledInputMethodsStrCache = "";
        private final TextUtils.SimpleStringSplitter mInputMethodSplitter = new TextUtils.SimpleStringSplitter(InputMethodUtils.INPUT_METHOD_SEPARATOR);
        private final ArrayMap<String, InputMethodInfo> mMethodMap;
        private final Resources mRes;
        private final ContentResolver mResolver;
        private final TextUtils.SimpleStringSplitter mSubtypeSplitter = new TextUtils.SimpleStringSplitter(InputMethodUtils.INPUT_METHOD_SUBTYPE_SEPARATOR);

        static {
            Settings.Secure.getCloneToManagedProfileSettings(CLONE_TO_MANAGED_PROFILE);
        }

        private static void buildEnabledInputMethodsSettingString(StringBuilder builder, Pair<String, ArrayList<String>> ime) {
            builder.append((String) ime.first);
            Iterator it = ((ArrayList) ime.second).iterator();
            while (it.hasNext()) {
                builder.append(InputMethodUtils.INPUT_METHOD_SUBTYPE_SEPARATOR);
                builder.append((String) it.next());
            }
        }

        private static List<Pair<String, ArrayList<String>>> buildInputMethodsAndSubtypeList(String enabledInputMethodsStr, TextUtils.SimpleStringSplitter inputMethodSplitter, TextUtils.SimpleStringSplitter subtypeSplitter) {
            ArrayList<Pair<String, ArrayList<String>>> imsList = new ArrayList<>();
            if (TextUtils.isEmpty(enabledInputMethodsStr)) {
                return imsList;
            }
            inputMethodSplitter.setString(enabledInputMethodsStr);
            while (inputMethodSplitter.hasNext()) {
                subtypeSplitter.setString(inputMethodSplitter.next());
                if (subtypeSplitter.hasNext()) {
                    ArrayList<String> subtypeHashes = new ArrayList<>();
                    String imeId = subtypeSplitter.next();
                    while (subtypeSplitter.hasNext()) {
                        subtypeHashes.add(subtypeSplitter.next());
                    }
                    imsList.add(new Pair<>(imeId, subtypeHashes));
                }
            }
            return imsList;
        }

        InputMethodSettings(Resources res, ContentResolver resolver, ArrayMap<String, InputMethodInfo> methodMap, int userId, boolean copyOnWrite) {
            this.mRes = res;
            this.mResolver = resolver;
            this.mMethodMap = methodMap;
            switchCurrentUser(userId, copyOnWrite);
        }

        /* access modifiers changed from: package-private */
        public void switchCurrentUser(int userId, boolean copyOnWrite) {
            if (!(this.mCurrentUserId == userId && this.mCopyOnWrite == copyOnWrite)) {
                this.mCopyOnWriteDataStore.clear();
                this.mEnabledInputMethodsStrCache = "";
            }
            this.mCurrentUserId = userId;
            this.mCopyOnWrite = copyOnWrite;
        }

        private void putString(String key, String str) {
            if (this.mCopyOnWrite) {
                this.mCopyOnWriteDataStore.put(key, str);
            } else {
                Settings.Secure.putStringForUser(this.mResolver, key, str, CLONE_TO_MANAGED_PROFILE.contains(key) ? sUserManagerInternal.getProfileParentId(this.mCurrentUserId) : this.mCurrentUserId);
            }
        }

        private String getString(String key, String defaultValue) {
            String result;
            if (!this.mCopyOnWrite || !this.mCopyOnWriteDataStore.containsKey(key)) {
                result = Settings.Secure.getStringForUser(this.mResolver, key, this.mCurrentUserId);
            } else {
                result = this.mCopyOnWriteDataStore.get(key);
            }
            return result != null ? result : defaultValue;
        }

        private void putInt(String key, int value) {
            if (this.mCopyOnWrite) {
                this.mCopyOnWriteDataStore.put(key, String.valueOf(value));
            } else {
                Settings.Secure.putIntForUser(this.mResolver, key, value, CLONE_TO_MANAGED_PROFILE.contains(key) ? sUserManagerInternal.getProfileParentId(this.mCurrentUserId) : this.mCurrentUserId);
            }
        }

        private int getInt(String key, int defaultValue) {
            if (!this.mCopyOnWrite || !this.mCopyOnWriteDataStore.containsKey(key)) {
                return Settings.Secure.getIntForUser(this.mResolver, key, defaultValue, this.mCurrentUserId);
            }
            String result = this.mCopyOnWriteDataStore.get(key);
            return result != null ? Integer.parseInt(result) : defaultValue;
        }

        private void putBoolean(String key, boolean value) {
            putInt(key, value ? 1 : 0);
        }

        private boolean getBoolean(String key, boolean defaultValue) {
            return getInt(key, defaultValue ? 1 : 0) == 1;
        }

        public void setCurrentProfileIds(int[] currentProfileIds) {
            synchronized (this) {
                this.mCurrentProfileIds = currentProfileIds;
            }
        }

        public boolean isCurrentProfile(int userId) {
            synchronized (this) {
                if (userId == this.mCurrentUserId) {
                    return true;
                }
                for (int i = 0; i < this.mCurrentProfileIds.length; i++) {
                    if (userId == this.mCurrentProfileIds[i]) {
                        return true;
                    }
                }
                return false;
            }
        }

        /* access modifiers changed from: package-private */
        public ArrayList<InputMethodInfo> getEnabledInputMethodListLocked() {
            return createEnabledInputMethodListLocked(getEnabledInputMethodsAndSubtypeListLocked());
        }

        /* access modifiers changed from: package-private */
        public List<InputMethodSubtype> getEnabledInputMethodSubtypeListLocked(Context context, InputMethodInfo imi, boolean allowsImplicitlySelectedSubtypes) {
            List<InputMethodSubtype> enabledSubtypes = getEnabledInputMethodSubtypeListLocked(imi);
            if (allowsImplicitlySelectedSubtypes && enabledSubtypes.isEmpty()) {
                enabledSubtypes = InputMethodUtils.getImplicitlyApplicableSubtypesLocked(context.getResources(), imi);
            }
            return InputMethodSubtype.sort(context, 0, imi, enabledSubtypes);
        }

        /* access modifiers changed from: package-private */
        public List<InputMethodSubtype> getEnabledInputMethodSubtypeListLocked(InputMethodInfo imi) {
            List<Pair<String, ArrayList<String>>> imsList = getEnabledInputMethodsAndSubtypeListLocked();
            ArrayList<InputMethodSubtype> enabledSubtypes = new ArrayList<>();
            if (imi != null) {
                Iterator<Pair<String, ArrayList<String>>> it = imsList.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    Pair<String, ArrayList<String>> imsPair = it.next();
                    InputMethodInfo info = this.mMethodMap.get(imsPair.first);
                    if (info != null && info.getId().equals(imi.getId())) {
                        int subtypeCount = info.getSubtypeCount();
                        for (int i = 0; i < subtypeCount; i++) {
                            InputMethodSubtype ims = info.getSubtypeAt(i);
                            Iterator it2 = ((ArrayList) imsPair.second).iterator();
                            while (it2.hasNext()) {
                                if (String.valueOf(ims.hashCode()).equals((String) it2.next())) {
                                    enabledSubtypes.add(ims);
                                }
                            }
                        }
                    }
                }
            }
            return enabledSubtypes;
        }

        /* access modifiers changed from: package-private */
        public List<Pair<String, ArrayList<String>>> getEnabledInputMethodsAndSubtypeListLocked() {
            return buildInputMethodsAndSubtypeList(getEnabledInputMethodsStr(), this.mInputMethodSplitter, this.mSubtypeSplitter);
        }

        /* access modifiers changed from: package-private */
        public void appendAndPutEnabledInputMethodLocked(String id, boolean reloadInputMethodStr) {
            if (reloadInputMethodStr) {
                getEnabledInputMethodsStr();
            }
            if (TextUtils.isEmpty(this.mEnabledInputMethodsStrCache)) {
                putEnabledInputMethodsStr(id);
                return;
            }
            putEnabledInputMethodsStr(this.mEnabledInputMethodsStrCache + InputMethodUtils.INPUT_METHOD_SEPARATOR + id);
        }

        /* access modifiers changed from: package-private */
        public boolean buildAndPutEnabledInputMethodsStrRemovingIdLocked(StringBuilder builder, List<Pair<String, ArrayList<String>>> imsList, String id) {
            boolean isRemoved = false;
            boolean needsAppendSeparator = false;
            for (Pair<String, ArrayList<String>> ims : imsList) {
                if (((String) ims.first).equals(id)) {
                    isRemoved = true;
                } else {
                    if (needsAppendSeparator) {
                        builder.append(InputMethodUtils.INPUT_METHOD_SEPARATOR);
                    } else {
                        needsAppendSeparator = true;
                    }
                    buildEnabledInputMethodsSettingString(builder, ims);
                }
            }
            if (isRemoved) {
                putEnabledInputMethodsStr(builder.toString());
            }
            return isRemoved;
        }

        private ArrayList<InputMethodInfo> createEnabledInputMethodListLocked(List<Pair<String, ArrayList<String>>> imsList) {
            ArrayList<InputMethodInfo> res = new ArrayList<>();
            for (Pair<String, ArrayList<String>> ims : imsList) {
                InputMethodInfo info = this.mMethodMap.get(ims.first);
                if (info != null && !info.isVrOnly()) {
                    res.add(info);
                }
            }
            return res;
        }

        /* access modifiers changed from: package-private */
        public void putEnabledInputMethodsStr(String str) {
            if (TextUtils.isEmpty(str)) {
                putString("enabled_input_methods", null);
            } else {
                putString("enabled_input_methods", str);
            }
            this.mEnabledInputMethodsStrCache = str != null ? str : "";
        }

        /* access modifiers changed from: package-private */
        public String getEnabledInputMethodsStr() {
            this.mEnabledInputMethodsStrCache = getString("enabled_input_methods", "");
            return this.mEnabledInputMethodsStrCache;
        }

        private void saveSubtypeHistory(List<Pair<String, String>> savedImes, String newImeId, String newSubtypeId) {
            StringBuilder builder = new StringBuilder();
            boolean isImeAdded = false;
            if (!TextUtils.isEmpty(newImeId) && !TextUtils.isEmpty(newSubtypeId)) {
                builder.append(newImeId);
                builder.append(InputMethodUtils.INPUT_METHOD_SUBTYPE_SEPARATOR);
                builder.append(newSubtypeId);
                isImeAdded = true;
            }
            for (Pair<String, String> ime : savedImes) {
                String imeId = (String) ime.first;
                String subtypeId = (String) ime.second;
                if (TextUtils.isEmpty(subtypeId)) {
                    subtypeId = InputMethodUtils.NOT_A_SUBTYPE_ID_STR;
                }
                if (isImeAdded) {
                    builder.append(InputMethodUtils.INPUT_METHOD_SEPARATOR);
                } else {
                    isImeAdded = true;
                }
                builder.append(imeId);
                builder.append(InputMethodUtils.INPUT_METHOD_SUBTYPE_SEPARATOR);
                builder.append(subtypeId);
            }
            putSubtypeHistoryStr(builder.toString());
        }

        private void addSubtypeToHistory(String imeId, String subtypeId) {
            List<Pair<String, String>> subtypeHistory = loadInputMethodAndSubtypeHistoryLocked();
            Iterator<Pair<String, String>> it = subtypeHistory.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                Pair<String, String> ime = it.next();
                if (((String) ime.first).equals(imeId)) {
                    subtypeHistory.remove(ime);
                    break;
                }
            }
            saveSubtypeHistory(subtypeHistory, imeId, subtypeId);
        }

        private void putSubtypeHistoryStr(String str) {
            if (TextUtils.isEmpty(str)) {
                putString("input_methods_subtype_history", null);
            } else {
                putString("input_methods_subtype_history", str);
            }
        }

        /* access modifiers changed from: package-private */
        public Pair<String, String> getLastInputMethodAndSubtypeLocked() {
            return getLastSubtypeForInputMethodLockedInternal(null);
        }

        /* access modifiers changed from: package-private */
        public String getLastSubtypeForInputMethodLocked(String imeId) {
            Pair<String, String> ime = getLastSubtypeForInputMethodLockedInternal(imeId);
            if (ime != null) {
                return (String) ime.second;
            }
            return null;
        }

        private Pair<String, String> getLastSubtypeForInputMethodLockedInternal(String imeId) {
            List<Pair<String, ArrayList<String>>> enabledImes = getEnabledInputMethodsAndSubtypeListLocked();
            for (Pair<String, String> imeAndSubtype : loadInputMethodAndSubtypeHistoryLocked()) {
                String imeInTheHistory = (String) imeAndSubtype.first;
                if (TextUtils.isEmpty(imeId) || imeInTheHistory.equals(imeId)) {
                    String subtypeHashCode = getEnabledSubtypeHashCodeForInputMethodAndSubtypeLocked(enabledImes, imeInTheHistory, (String) imeAndSubtype.second);
                    if (!TextUtils.isEmpty(subtypeHashCode)) {
                        return new Pair<>(imeInTheHistory, subtypeHashCode);
                    }
                }
            }
            return null;
        }

        private String getEnabledSubtypeHashCodeForInputMethodAndSubtypeLocked(List<Pair<String, ArrayList<String>>> enabledImes, String imeId, String subtypeHashCode) {
            List<InputMethodSubtype> implicitlySelectedSubtypes;
            for (Pair<String, ArrayList<String>> enabledIme : enabledImes) {
                if (((String) enabledIme.first).equals(imeId)) {
                    ArrayList<String> explicitlyEnabledSubtypes = (ArrayList) enabledIme.second;
                    InputMethodInfo imi = this.mMethodMap.get(imeId);
                    if (explicitlyEnabledSubtypes.size() != 0) {
                        Iterator<String> it = explicitlyEnabledSubtypes.iterator();
                        while (it.hasNext()) {
                            String s = it.next();
                            if (s.equals(subtypeHashCode)) {
                                try {
                                    if (InputMethodUtils.isValidSubtypeId(imi, Integer.parseInt(subtypeHashCode))) {
                                        return s;
                                    }
                                    return InputMethodUtils.NOT_A_SUBTYPE_ID_STR;
                                } catch (NumberFormatException e) {
                                    return InputMethodUtils.NOT_A_SUBTYPE_ID_STR;
                                }
                            }
                        }
                    } else if (!(imi == null || imi.getSubtypeCount() <= 0 || (implicitlySelectedSubtypes = InputMethodUtils.getImplicitlyApplicableSubtypesLocked(this.mRes, imi)) == null)) {
                        int N = implicitlySelectedSubtypes.size();
                        for (int i = 0; i < N; i++) {
                            if (String.valueOf(implicitlySelectedSubtypes.get(i).hashCode()).equals(subtypeHashCode)) {
                                return subtypeHashCode;
                            }
                        }
                    }
                    return InputMethodUtils.NOT_A_SUBTYPE_ID_STR;
                }
            }
            return null;
        }

        private List<Pair<String, String>> loadInputMethodAndSubtypeHistoryLocked() {
            ArrayList<Pair<String, String>> imsList = new ArrayList<>();
            String subtypeHistoryStr = getSubtypeHistoryStr();
            if (TextUtils.isEmpty(subtypeHistoryStr)) {
                return imsList;
            }
            this.mInputMethodSplitter.setString(subtypeHistoryStr);
            while (this.mInputMethodSplitter.hasNext()) {
                this.mSubtypeSplitter.setString(this.mInputMethodSplitter.next());
                if (this.mSubtypeSplitter.hasNext()) {
                    String subtypeId = InputMethodUtils.NOT_A_SUBTYPE_ID_STR;
                    String imeId = this.mSubtypeSplitter.next();
                    if (this.mSubtypeSplitter.hasNext()) {
                        subtypeId = this.mSubtypeSplitter.next();
                    }
                    imsList.add(new Pair<>(imeId, subtypeId));
                }
            }
            return imsList;
        }

        private String getSubtypeHistoryStr() {
            return getString("input_methods_subtype_history", "");
        }

        /* access modifiers changed from: package-private */
        public void putSelectedInputMethod(String imeId) {
            putString("default_input_method", imeId);
        }

        /* access modifiers changed from: package-private */
        public void putSelectedSubtype(int subtypeId) {
            putInt("selected_input_method_subtype", subtypeId);
        }

        public boolean getIsWriteInputEnable() {
            if (Settings.System.getInt(this.mResolver, InputMethodUtils.DB_INPUT_METHOD_AUTO_CHANGE, 1) == 1) {
                return true;
            }
            return false;
        }

        /* access modifiers changed from: package-private */
        public String getSelectedInputMethod() {
            return getString("default_input_method", null);
        }

        /* access modifiers changed from: package-private */
        public boolean isSubtypeSelected() {
            return getSelectedInputMethodSubtypeHashCode() != -1;
        }

        private int getSelectedInputMethodSubtypeHashCode() {
            return getInt("selected_input_method_subtype", -1);
        }

        /* access modifiers changed from: package-private */
        public boolean isShowImeWithHardKeyboardEnabled() {
            return getBoolean("show_ime_with_hard_keyboard", false);
        }

        /* access modifiers changed from: package-private */
        public void setShowImeWithHardKeyboard(boolean show) {
            putBoolean("show_ime_with_hard_keyboard", show);
        }

        public int getCurrentUserId() {
            return this.mCurrentUserId;
        }

        /* access modifiers changed from: package-private */
        public int getSelectedInputMethodSubtypeId(String selectedImiId) {
            InputMethodInfo imi = this.mMethodMap.get(selectedImiId);
            if (imi == null) {
                return -1;
            }
            return InputMethodUtils.getSubtypeIdFromHashCode(imi, getSelectedInputMethodSubtypeHashCode());
        }

        /* access modifiers changed from: package-private */
        public void saveCurrentInputMethodAndSubtypeToHistory(String curMethodId, InputMethodSubtype currentSubtype) {
            String subtypeId = InputMethodUtils.NOT_A_SUBTYPE_ID_STR;
            if (currentSubtype != null) {
                subtypeId = String.valueOf(currentSubtype.hashCode());
            }
            if (InputMethodUtils.canAddToLastInputMethod(currentSubtype)) {
                addSubtypeToHistory(curMethodId, subtypeId);
            }
        }

        public void dumpLocked(Printer pw, String prefix) {
            pw.println(prefix + "mCurrentUserId=" + this.mCurrentUserId);
            pw.println(prefix + "mCurrentProfileIds=" + Arrays.toString(this.mCurrentProfileIds));
            pw.println(prefix + "mCopyOnWrite=" + this.mCopyOnWrite);
            pw.println(prefix + "mEnabledInputMethodsStrCache=" + this.mEnabledInputMethodsStrCache);
        }
    }

    static boolean isSoftInputModeStateVisibleAllowed(int targetSdkVersion, int startInputFlags) {
        if (targetSdkVersion < 28) {
            return true;
        }
        if ((startInputFlags & 1) == 0 || (startInputFlags & 2) == 0) {
            return false;
        }
        return true;
    }

    static int[] resolveUserId(int userIdToBeResolved, int currentUserId, PrintWriter warningWriter) {
        int sourceUserId;
        UserManagerInternal userManagerInternal = (UserManagerInternal) LocalServices.getService(UserManagerInternal.class);
        if (userIdToBeResolved != -1) {
            if (userIdToBeResolved == -2) {
                sourceUserId = currentUserId;
            } else if (userIdToBeResolved < 0) {
                if (warningWriter != null) {
                    warningWriter.print("Pseudo user ID ");
                    warningWriter.print(userIdToBeResolved);
                    warningWriter.println(" is not supported.");
                }
                return new int[0];
            } else if (userManagerInternal.exists(userIdToBeResolved)) {
                sourceUserId = userIdToBeResolved;
            } else {
                if (warningWriter != null) {
                    warningWriter.print("User #");
                    warningWriter.print(userIdToBeResolved);
                    warningWriter.println(" does not exit.");
                }
                return new int[0];
            }
            return new int[]{InputMethodSystemProperty.PER_PROFILE_IME_ENABLED ? sourceUserId : userManagerInternal.getProfileParentId(sourceUserId)};
        } else if (InputMethodSystemProperty.PER_PROFILE_IME_ENABLED) {
            return userManagerInternal.getUserIds();
        } else {
            IntArray result = new IntArray();
            for (int userId : userManagerInternal.getUserIds()) {
                int parentUserId = userManagerInternal.getProfileParentId(userId);
                if (result.indexOf(parentUserId) < 0) {
                    result.add(parentUserId);
                }
            }
            return result.toArray();
        }
    }
}
