package com.android.server;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.FastImmutableArraySet;
import android.util.Log;
import android.util.LogPrinter;
import android.util.MutableInt;
import android.util.PrintWriterPrinter;
import android.util.Printer;
import android.util.Slog;
import com.android.internal.util.FastPrintWriter;
import com.android.server.am.HwBroadcastRadarUtil;
import com.android.server.voiceinteraction.DatabaseHelper.SoundModelContract;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public abstract class IntentResolver<F extends IntentFilter, R> {
    private static final boolean DEBUG = false;
    private static final boolean HWFLOW;
    private static final String TAG = "IntentResolver";
    private static final boolean localLOGV = false;
    private static final boolean localVerificationLOGV = false;
    private static final Comparator mResolvePrioritySorter = new Comparator() {
        public int compare(Object o1, Object o2) {
            int q1 = ((IntentFilter) o1).getPriority();
            int q2 = ((IntentFilter) o2).getPriority();
            if (q1 > q2) {
                return -1;
            }
            return q1 < q2 ? 1 : 0;
        }
    };
    private final ArrayMap<String, F[]> mActionToFilter = new ArrayMap();
    private final ArrayMap<String, F[]> mBaseTypeToFilter = new ArrayMap();
    private final ArraySet<F> mFilters = new ArraySet();
    private final ArrayMap<String, F[]> mSchemeToFilter = new ArrayMap();
    private final ArrayMap<String, F[]> mTypeToFilter = new ArrayMap();
    private final ArrayMap<String, F[]> mTypedActionToFilter = new ArrayMap();
    private final ArrayMap<String, F[]> mWildTypeToFilter = new ArrayMap();

    private class IteratorWrapper implements Iterator<F> {
        private F mCur;
        private final Iterator<F> mI;

        IteratorWrapper(Iterator<F> it) {
            this.mI = it;
        }

        public boolean hasNext() {
            return this.mI.hasNext();
        }

        public F next() {
            IntentFilter intentFilter = (IntentFilter) this.mI.next();
            this.mCur = intentFilter;
            return intentFilter;
        }

        public void remove() {
            if (this.mCur != null) {
                IntentResolver.this.removeFilterInternal(this.mCur);
            }
            this.mI.remove();
        }
    }

    protected abstract boolean isPackageForFilter(String str, F f);

    protected abstract F[] newArray(int i);

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        HWFLOW = isLoggable;
    }

    public void addFilter(F f) {
        this.mFilters.add(f);
        int numS = register_intent_filter(f, f.schemesIterator(), this.mSchemeToFilter, "      Scheme: ");
        int numT = register_mime_types(f, "      Type: ");
        if (numS == 0 && numT == 0) {
            register_intent_filter(f, f.actionsIterator(), this.mActionToFilter, "      Action: ");
        }
        if (numT != 0) {
            register_intent_filter(f, f.actionsIterator(), this.mTypedActionToFilter, "      TypedAction: ");
        }
    }

    private boolean filterEquals(IntentFilter f1, IntentFilter f2) {
        int s1 = f1.countActions();
        if (s1 != f2.countActions()) {
            return false;
        }
        int i;
        for (i = 0; i < s1; i++) {
            if (!f2.hasAction(f1.getAction(i))) {
                return false;
            }
        }
        s1 = f1.countCategories();
        if (s1 != f2.countCategories()) {
            return false;
        }
        for (i = 0; i < s1; i++) {
            if (!f2.hasCategory(f1.getCategory(i))) {
                return false;
            }
        }
        s1 = f1.countDataTypes();
        if (s1 != f2.countDataTypes()) {
            return false;
        }
        for (i = 0; i < s1; i++) {
            if (!f2.hasExactDataType(f1.getDataType(i))) {
                return false;
            }
        }
        s1 = f1.countDataSchemes();
        if (s1 != f2.countDataSchemes()) {
            return false;
        }
        for (i = 0; i < s1; i++) {
            if (!f2.hasDataScheme(f1.getDataScheme(i))) {
                return false;
            }
        }
        s1 = f1.countDataAuthorities();
        if (s1 != f2.countDataAuthorities()) {
            return false;
        }
        for (i = 0; i < s1; i++) {
            if (!f2.hasDataAuthority(f1.getDataAuthority(i))) {
                return false;
            }
        }
        s1 = f1.countDataPaths();
        if (s1 != f2.countDataPaths()) {
            return false;
        }
        for (i = 0; i < s1; i++) {
            if (!f2.hasDataPath(f1.getDataPath(i))) {
                return false;
            }
        }
        s1 = f1.countDataSchemeSpecificParts();
        if (s1 != f2.countDataSchemeSpecificParts()) {
            return false;
        }
        for (i = 0; i < s1; i++) {
            if (!f2.hasDataSchemeSpecificPart(f1.getDataSchemeSpecificPart(i))) {
                return false;
            }
        }
        return true;
    }

    private ArrayList<F> collectFilters(F[] array, IntentFilter matching) {
        ArrayList<F> arrayList = null;
        if (array != null) {
            for (F cur : array) {
                if (cur == null) {
                    break;
                }
                if (filterEquals(cur, matching)) {
                    ArrayList arrayList2;
                    if (arrayList2 == null) {
                        arrayList2 = new ArrayList();
                    }
                    arrayList2.add(cur);
                }
            }
        }
        return arrayList;
    }

    public ArrayList<F> findFilters(IntentFilter matching) {
        if (matching.countDataSchemes() == 1) {
            return collectFilters((IntentFilter[]) this.mSchemeToFilter.get(matching.getDataScheme(0)), matching);
        }
        if (matching.countDataTypes() != 0 && matching.countActions() == 1) {
            return collectFilters((IntentFilter[]) this.mTypedActionToFilter.get(matching.getAction(0)), matching);
        }
        if (matching.countDataTypes() == 0 && matching.countDataSchemes() == 0 && matching.countActions() == 1) {
            return collectFilters((IntentFilter[]) this.mActionToFilter.get(matching.getAction(0)), matching);
        }
        ArrayList<F> res = null;
        for (IntentFilter cur : this.mFilters) {
            if (filterEquals(cur, matching)) {
                if (res == null) {
                    res = new ArrayList();
                }
                res.add(cur);
            }
        }
        return res;
    }

    public void removeFilter(F f) {
        removeFilterInternal(f);
        this.mFilters.remove(f);
    }

    void removeFilterInternal(F f) {
        int numS = unregister_intent_filter(f, f.schemesIterator(), this.mSchemeToFilter, "      Scheme: ");
        int numT = unregister_mime_types(f, "      Type: ");
        if (numS == 0 && numT == 0) {
            try {
                unregister_intent_filter(f, f.actionsIterator(), this.mActionToFilter, "      Action: ");
            } catch (ConcurrentModificationException e) {
                Slog.e(TAG, "Failed to Removing filter: " + f + " while unregistering action error!", e);
            }
        }
        if (numT != 0) {
            unregister_intent_filter(f, f.actionsIterator(), this.mTypedActionToFilter, "      TypedAction: ");
        }
    }

    boolean dumpMap(PrintWriter out, String titlePrefix, String title, String prefix, ArrayMap<String, F[]> map, String packageName, boolean printFilter, boolean collapseDuplicates) {
        String eprefix = prefix + "  ";
        String fprefix = prefix + "    ";
        ArrayMap<Object, MutableInt> found = new ArrayMap();
        boolean printedSomething = false;
        Printer printer = null;
        for (int mapi = 0; mapi < map.size(); mapi++) {
            IntentFilter[] a = (IntentFilter[]) map.valueAt(mapi);
            boolean printedHeader = false;
            int i;
            if (!collapseDuplicates || (printFilter ^ 1) == 0) {
                for (F filter : a) {
                    if (filter == null) {
                        break;
                    }
                    if (packageName == null || (isPackageForFilter(packageName, filter) ^ 1) == 0) {
                        if (title != null) {
                            out.print(titlePrefix);
                            out.println(title);
                            title = null;
                        }
                        if (!printedHeader) {
                            out.print(eprefix);
                            out.print((String) map.keyAt(mapi));
                            out.println(":");
                            printedHeader = true;
                        }
                        printedSomething = true;
                        dumpFilter(out, fprefix, filter);
                        if (printFilter) {
                            if (printer == null) {
                                Printer printWriterPrinter = new PrintWriterPrinter(out);
                            }
                            filter.dump(printer, fprefix + "  ");
                        }
                    }
                }
            } else {
                found.clear();
                for (F filter2 : a) {
                    if (filter2 == null) {
                        break;
                    }
                    if (packageName == null || (isPackageForFilter(packageName, filter2) ^ 1) == 0) {
                        Object label = filterToLabel(filter2);
                        int index = found.indexOfKey(label);
                        if (index < 0) {
                            found.put(label, new MutableInt(1));
                        } else {
                            MutableInt mutableInt = (MutableInt) found.valueAt(index);
                            mutableInt.value++;
                        }
                    }
                }
                for (i = 0; i < found.size(); i++) {
                    if (title != null) {
                        out.print(titlePrefix);
                        out.println(title);
                        title = null;
                    }
                    if (!printedHeader) {
                        out.print(eprefix);
                        out.print((String) map.keyAt(mapi));
                        out.println(":");
                        printedHeader = true;
                    }
                    printedSomething = true;
                    dumpFilterLabel(out, fprefix, found.keyAt(i), ((MutableInt) found.valueAt(i)).value);
                }
            }
        }
        return printedSomething;
    }

    public boolean dump(PrintWriter out, String title, String prefix, String packageName, boolean printFilter, boolean collapseDuplicates) {
        String innerPrefix = prefix + "  ";
        String sepPrefix = "\n" + prefix;
        String curPrefix = title + "\n" + prefix;
        if (dumpMap(out, curPrefix, "Full MIME Types:", innerPrefix, this.mTypeToFilter, packageName, printFilter, collapseDuplicates)) {
            curPrefix = sepPrefix;
        }
        if (dumpMap(out, curPrefix, "Base MIME Types:", innerPrefix, this.mBaseTypeToFilter, packageName, printFilter, collapseDuplicates)) {
            curPrefix = sepPrefix;
        }
        if (dumpMap(out, curPrefix, "Wild MIME Types:", innerPrefix, this.mWildTypeToFilter, packageName, printFilter, collapseDuplicates)) {
            curPrefix = sepPrefix;
        }
        if (dumpMap(out, curPrefix, "Schemes:", innerPrefix, this.mSchemeToFilter, packageName, printFilter, collapseDuplicates)) {
            curPrefix = sepPrefix;
        }
        if (dumpMap(out, curPrefix, "Non-Data Actions:", innerPrefix, this.mActionToFilter, packageName, printFilter, collapseDuplicates)) {
            curPrefix = sepPrefix;
        }
        if (dumpMap(out, curPrefix, "MIME Typed Actions:", innerPrefix, this.mTypedActionToFilter, packageName, printFilter, collapseDuplicates)) {
            curPrefix = sepPrefix;
        }
        return curPrefix == sepPrefix;
    }

    public Iterator<F> filterIterator() {
        return new IteratorWrapper(this.mFilters.iterator());
    }

    public Set<F> filterSet() {
        return Collections.unmodifiableSet(this.mFilters);
    }

    public List<R> queryIntentFromList(Intent intent, String resolvedType, boolean defaultOnly, ArrayList<F[]> listCut, int userId) {
        ArrayList<R> resultList = new ArrayList();
        boolean debug = (intent.getFlags() & 8) != 0;
        FastImmutableArraySet<String> categories = getFastIntentCategories(intent);
        String scheme = intent.getScheme();
        int N = listCut.size();
        for (int i = 0; i < N; i++) {
            buildResolveList(intent, categories, debug, defaultOnly, resolvedType, scheme, (IntentFilter[]) listCut.get(i), resultList, userId);
        }
        filterResults(resultList);
        sortResults(resultList);
        return resultList;
    }

    /* JADX WARNING: Removed duplicated region for block: B:23:0x0104  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public List<R> queryIntent(Intent intent, String resolvedType, boolean defaultOnly, int userId) {
        String scheme = intent.getScheme();
        ArrayList<R> finalList = new ArrayList();
        boolean debug = (intent.getFlags() & 8) != 0;
        if (debug) {
            Slog.v(TAG, "Resolving type=" + resolvedType + " scheme=" + scheme + " defaultOnly=" + defaultOnly + " userId=" + userId + " of " + intent);
        }
        F[] firstTypeCut = null;
        F[] secondTypeCut = null;
        F[] thirdTypeCut = null;
        F[] schemeCut = null;
        if (resolvedType != null) {
            int slashpos = resolvedType.indexOf(47);
            if (slashpos > 0) {
                String baseType = resolvedType.substring(0, slashpos);
                IntentFilter[] firstTypeCut2;
                if (!baseType.equals("*")) {
                    if (resolvedType.length() == slashpos + 2) {
                        if (resolvedType.charAt(slashpos + 1) == '*') {
                            firstTypeCut2 = (IntentFilter[]) this.mBaseTypeToFilter.get(baseType);
                            if (debug) {
                                Slog.v(TAG, "First type cut: " + Arrays.toString(firstTypeCut2));
                            }
                            IntentFilter[] secondTypeCut2 = (IntentFilter[]) this.mWildTypeToFilter.get(baseType);
                            if (debug) {
                                Slog.v(TAG, "Second type cut: " + Arrays.toString(secondTypeCut2));
                            }
                            thirdTypeCut = (IntentFilter[]) this.mWildTypeToFilter.get("*");
                            if (debug) {
                                Slog.v(TAG, "Third type cut: " + Arrays.toString(thirdTypeCut));
                            }
                        }
                    }
                    firstTypeCut = (IntentFilter[]) this.mTypeToFilter.get(resolvedType);
                    if (debug) {
                        Slog.v(TAG, "First type cut: " + Arrays.toString(firstTypeCut));
                    }
                    secondTypeCut = (IntentFilter[]) this.mWildTypeToFilter.get(baseType);
                    if (debug) {
                        Slog.v(TAG, "Second type cut: " + Arrays.toString(secondTypeCut));
                    }
                    thirdTypeCut = (IntentFilter[]) this.mWildTypeToFilter.get("*");
                    if (debug) {
                    }
                } else if (intent.getAction() != null) {
                    firstTypeCut2 = (IntentFilter[]) this.mTypedActionToFilter.get(intent.getAction());
                    if (debug) {
                        Slog.v(TAG, "Typed Action list: " + Arrays.toString(firstTypeCut2));
                    }
                }
            }
        }
        if (scheme != null) {
            schemeCut = (IntentFilter[]) this.mSchemeToFilter.get(scheme);
            if (debug) {
                Slog.v(TAG, "Scheme list: " + Arrays.toString(schemeCut));
            }
        }
        if (resolvedType == null && scheme == null && intent.getAction() != null) {
            firstTypeCut = (IntentFilter[]) this.mActionToFilter.get(intent.getAction());
            if (debug) {
                Slog.v(TAG, "Action list: " + Arrays.toString(firstTypeCut));
            }
        }
        FastImmutableArraySet<String> categories = getFastIntentCategories(intent);
        if (firstTypeCut != null) {
            buildResolveList(intent, categories, debug, defaultOnly, resolvedType, scheme, firstTypeCut, finalList, userId);
        }
        if (secondTypeCut != null) {
            buildResolveList(intent, categories, debug, defaultOnly, resolvedType, scheme, secondTypeCut, finalList, userId);
        }
        if (thirdTypeCut != null) {
            buildResolveList(intent, categories, debug, defaultOnly, resolvedType, scheme, thirdTypeCut, finalList, userId);
        }
        if (schemeCut != null) {
            buildResolveList(intent, categories, debug, defaultOnly, resolvedType, scheme, schemeCut, finalList, userId);
        }
        filterResults(finalList);
        sortResults(finalList);
        if (debug) {
            Slog.v(TAG, "Final result list:");
            for (int i = 0; i < finalList.size(); i++) {
                Slog.v(TAG, "  " + finalList.get(i));
            }
        }
        return finalList;
    }

    protected boolean allowFilterResult(F f, List<R> list) {
        return true;
    }

    protected boolean isFilterStopped(F f, int userId) {
        return false;
    }

    protected boolean isFilterVerified(F filter) {
        return filter.isVerified();
    }

    protected R newResult(F filter, int match, int userId) {
        return filter;
    }

    protected void sortResults(List<R> results) {
        Collections.sort(results, mResolvePrioritySorter);
    }

    protected void filterResults(List<R> list) {
    }

    protected void dumpFilter(PrintWriter out, String prefix, F filter) {
        out.print(prefix);
        out.println(filter);
    }

    protected Object filterToLabel(F f) {
        return "IntentFilter";
    }

    protected void dumpFilterLabel(PrintWriter out, String prefix, Object label, int count) {
        out.print(prefix);
        out.print(label);
        out.print(": ");
        out.println(count);
    }

    private final void addFilter(ArrayMap<String, F[]> map, String name, F filter) {
        IntentFilter[] array = (IntentFilter[]) map.get(name);
        if (array == null) {
            F[] array2 = newArray(2);
            map.put(name, array2);
            array2[0] = filter;
            return;
        }
        int N = array.length;
        int i = N;
        while (i > 0 && array[i - 1] == null) {
            i--;
        }
        if (i < N) {
            array[i] = filter;
            return;
        }
        F[] newa = newArray((N * 3) / 2);
        System.arraycopy(array, 0, newa, 0, N);
        newa[N] = filter;
        map.put(name, newa);
    }

    private final int register_mime_types(F filter, String prefix) {
        Iterator<String> i = filter.typesIterator();
        if (i == null) {
            return 0;
        }
        int num = 0;
        while (i.hasNext()) {
            String name = (String) i.next();
            num++;
            String baseName = name;
            int slashpos = name.indexOf(47);
            if (slashpos > 0) {
                baseName = name.substring(0, slashpos).intern();
            } else {
                name = name + "/*";
            }
            addFilter(this.mTypeToFilter, name, filter);
            if (slashpos > 0) {
                addFilter(this.mBaseTypeToFilter, baseName, filter);
            } else {
                addFilter(this.mWildTypeToFilter, baseName, filter);
            }
        }
        return num;
    }

    private final int unregister_mime_types(F filter, String prefix) {
        Iterator<String> i = filter.typesIterator();
        if (i == null) {
            return 0;
        }
        int num = 0;
        while (i.hasNext()) {
            String name = (String) i.next();
            num++;
            String baseName = name;
            int slashpos = name.indexOf(47);
            if (slashpos > 0) {
                baseName = name.substring(0, slashpos).intern();
            } else {
                name = name + "/*";
            }
            remove_all_objects(this.mTypeToFilter, name, filter);
            if (slashpos > 0) {
                remove_all_objects(this.mBaseTypeToFilter, baseName, filter);
            } else {
                remove_all_objects(this.mWildTypeToFilter, baseName, filter);
            }
        }
        return num;
    }

    private final int register_intent_filter(F filter, Iterator<String> i, ArrayMap<String, F[]> dest, String prefix) {
        if (i == null) {
            return 0;
        }
        int num = 0;
        while (i.hasNext()) {
            num++;
            addFilter(dest, (String) i.next(), filter);
        }
        return num;
    }

    private final int unregister_intent_filter(F filter, Iterator<String> i, ArrayMap<String, F[]> dest, String prefix) {
        if (i == null) {
            return 0;
        }
        int num = 0;
        while (i.hasNext()) {
            num++;
            remove_all_objects(dest, (String) i.next(), filter);
        }
        return num;
    }

    private final void remove_all_objects(ArrayMap<String, F[]> map, String name, Object object) {
        IntentFilter[] array = (IntentFilter[]) map.get(name);
        if (array != null) {
            int LAST = array.length - 1;
            while (LAST >= 0 && array[LAST] == null) {
                LAST--;
            }
            for (int idx = LAST; idx >= 0; idx--) {
                if (array[idx] == object) {
                    int remain = LAST - idx;
                    if (remain > 0) {
                        System.arraycopy(array, idx + 1, array, idx, remain);
                    }
                    array[LAST] = null;
                    LAST--;
                }
            }
            if (LAST < 0) {
                map.remove(name);
            } else if (LAST < array.length / 2) {
                F[] newa = newArray(LAST + 2);
                System.arraycopy(array, 0, newa, 0, LAST + 1);
                map.put(name, newa);
            }
        }
    }

    private static FastImmutableArraySet<String> getFastIntentCategories(Intent intent) {
        Set<String> categories = intent.getCategories();
        if (categories == null) {
            return null;
        }
        return new FastImmutableArraySet((String[]) categories.toArray(new String[categories.size()]));
    }

    private void buildResolveList(Intent intent, FastImmutableArraySet<String> categories, boolean debug, boolean defaultOnly, String resolvedType, String scheme, F[] src, List<R> dest, int userId) {
        PrintWriter logPrintWriter;
        Printer logPrinter;
        String action = intent.getAction();
        Uri data = intent.getData();
        String packageName = intent.getPackage();
        boolean excludingStopped = intent.isExcludingStopped();
        if (debug) {
            Printer logPrinter2 = new LogPrinter(2, TAG, 3);
            logPrintWriter = new FastPrintWriter(logPrinter2);
        } else {
            logPrinter = null;
            logPrintWriter = null;
        }
        int N = src != null ? src.length : 0;
        boolean hasNonDefaults = false;
        int i = 0;
        while (i < N) {
            F filter = src[i];
            if (filter != null) {
                if (debug && HWFLOW) {
                    Slog.v(TAG, "Matching against filter " + filter);
                }
                if (excludingStopped && isFilterStopped(filter, userId)) {
                    if (debug) {
                        Slog.v(TAG, "  Filter's target is stopped; skipping");
                    }
                } else if (packageName == null || (isPackageForFilter(packageName, filter) ^ 1) == 0) {
                    if (filter.getAutoVerify() && debug) {
                        Slog.v(TAG, "  Filter verified: " + isFilterVerified(filter));
                        int authorities = filter.countDataAuthorities();
                        for (int z = 0; z < authorities; z++) {
                            Slog.v(TAG, "   " + filter.getDataAuthority(z).getHost());
                        }
                    }
                    if (allowFilterResult(filter, dest)) {
                        int match = filter.match(action, resolvedType, scheme, data, categories, TAG);
                        if (match >= 0) {
                            if (debug) {
                                Slog.v(TAG, "  Filter matched!  match=0x" + Integer.toHexString(match) + " hasDefault=" + filter.hasCategory("android.intent.category.DEFAULT"));
                            }
                            if (!defaultOnly || filter.hasCategory("android.intent.category.DEFAULT")) {
                                R oneResult = newResult(filter, match, userId);
                                if (oneResult != null) {
                                    dest.add(oneResult);
                                    if (debug) {
                                        dumpFilter(logPrintWriter, "    ", filter);
                                        logPrintWriter.flush();
                                        filter.dump(logPrinter, "    ");
                                    }
                                }
                            } else {
                                hasNonDefaults = true;
                            }
                        } else if (debug && HWFLOW) {
                            String reason;
                            switch (match) {
                                case -4:
                                    reason = "category";
                                    break;
                                case -3:
                                    reason = HwBroadcastRadarUtil.KEY_ACTION;
                                    break;
                                case -2:
                                    reason = "data";
                                    break;
                                case -1:
                                    reason = SoundModelContract.KEY_TYPE;
                                    break;
                                default:
                                    reason = "unknown reason";
                                    break;
                            }
                            Slog.v(TAG, "  Filter did not match: " + reason);
                        }
                    } else if (debug) {
                        Slog.v(TAG, "  Filter's target already added");
                    }
                } else if (debug) {
                    Slog.v(TAG, "  Filter is not from package " + packageName + "; skipping");
                }
                i++;
            } else if (debug && hasNonDefaults) {
                if (dest.size() == 0) {
                    Slog.v(TAG, "resolveIntent failed: found match, but none with CATEGORY_DEFAULT");
                    return;
                } else if (dest.size() > 1) {
                    Slog.v(TAG, "resolveIntent: multiple matches, only some with CATEGORY_DEFAULT");
                    return;
                } else {
                    return;
                }
            }
        }
        if (debug) {
        }
    }
}
