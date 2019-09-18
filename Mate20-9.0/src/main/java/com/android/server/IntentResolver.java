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
import android.util.proto.ProtoOutputStream;
import com.android.internal.util.FastPrintWriter;
import com.android.server.am.HwBroadcastRadarUtil;
import com.android.server.voiceinteraction.DatabaseHelper;
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
    private static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
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
    private final ArrayMap<String, F[]> mActionToFilter = new ArrayMap<>();
    private final ArrayMap<String, F[]> mBaseTypeToFilter = new ArrayMap<>();
    private final ArraySet<F> mFilters = new ArraySet<>();
    private final ArrayMap<String, F[]> mSchemeToFilter = new ArrayMap<>();
    private final ArrayMap<String, F[]> mTypeToFilter = new ArrayMap<>();
    private final ArrayMap<String, F[]> mTypedActionToFilter = new ArrayMap<>();
    private final ArrayMap<String, F[]> mWildTypeToFilter = new ArrayMap<>();

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
            F f = (IntentFilter) this.mI.next();
            this.mCur = f;
            return f;
        }

        public void remove() {
            if (this.mCur != null) {
                IntentResolver.this.removeFilterInternal(this.mCur);
            }
            this.mI.remove();
        }
    }

    /* access modifiers changed from: protected */
    public abstract boolean isPackageForFilter(String str, F f);

    /* access modifiers changed from: protected */
    public abstract F[] newArray(int i);

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

    public static boolean filterEquals(IntentFilter f1, IntentFilter f2) {
        int s1 = f1.countActions();
        if (s1 != f2.countActions()) {
            return false;
        }
        for (int i = 0; i < s1; i++) {
            if (!f2.hasAction(f1.getAction(i))) {
                return false;
            }
        }
        int s12 = f1.countCategories();
        if (s12 != f2.countCategories()) {
            return false;
        }
        for (int i2 = 0; i2 < s12; i2++) {
            if (!f2.hasCategory(f1.getCategory(i2))) {
                return false;
            }
        }
        int s13 = f1.countDataTypes();
        if (s13 != f2.countDataTypes()) {
            return false;
        }
        for (int i3 = 0; i3 < s13; i3++) {
            if (!f2.hasExactDataType(f1.getDataType(i3))) {
                return false;
            }
        }
        int s14 = f1.countDataSchemes();
        if (s14 != f2.countDataSchemes()) {
            return false;
        }
        for (int i4 = 0; i4 < s14; i4++) {
            if (!f2.hasDataScheme(f1.getDataScheme(i4))) {
                return false;
            }
        }
        int s15 = f1.countDataAuthorities();
        if (s15 != f2.countDataAuthorities()) {
            return false;
        }
        for (int i5 = 0; i5 < s15; i5++) {
            if (!f2.hasDataAuthority(f1.getDataAuthority(i5))) {
                return false;
            }
        }
        int s16 = f1.countDataPaths();
        if (s16 != f2.countDataPaths()) {
            return false;
        }
        for (int i6 = 0; i6 < s16; i6++) {
            if (!f2.hasDataPath(f1.getDataPath(i6))) {
                return false;
            }
        }
        int s17 = f1.countDataSchemeSpecificParts();
        if (s17 != f2.countDataSchemeSpecificParts()) {
            return false;
        }
        for (int i7 = 0; i7 < s17; i7++) {
            if (!f2.hasDataSchemeSpecificPart(f1.getDataSchemeSpecificPart(i7))) {
                return false;
            }
        }
        return true;
    }

    private ArrayList<F> collectFilters(F[] array, IntentFilter matching) {
        ArrayList<F> res = null;
        if (array != null) {
            for (F cur : array) {
                if (cur == null) {
                    break;
                }
                if (filterEquals(cur, matching)) {
                    if (res == null) {
                        res = new ArrayList<>();
                    }
                    res.add(cur);
                }
            }
        }
        return res;
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
        Iterator<F> it = this.mFilters.iterator();
        while (it.hasNext()) {
            F cur = (IntentFilter) it.next();
            if (filterEquals(cur, matching)) {
                if (res == null) {
                    res = new ArrayList<>();
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

    /* access modifiers changed from: package-private */
    public void removeFilterInternal(F f) {
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

    /* access modifiers changed from: package-private */
    public boolean dumpMap(PrintWriter out, String titlePrefix, String title, String prefix, ArrayMap<String, F[]> map, String packageName, boolean printFilter, boolean collapseDuplicates) {
        boolean printedSomething;
        Printer printer;
        String title2;
        Printer printer2;
        boolean printedSomething2;
        Printer printer3;
        String title3;
        F filter;
        IntentResolver intentResolver = this;
        PrintWriter printWriter = out;
        String str = prefix;
        ArrayMap<String, F[]> arrayMap = map;
        String str2 = packageName;
        String eprefix = str + "  ";
        String fprefix = str + "    ";
        ArrayMap<Object, MutableInt> found = new ArrayMap<>();
        String title4 = title;
        Printer printer4 = null;
        boolean printedSomething3 = false;
        int mapi = 0;
        while (mapi < map.size()) {
            F[] a = (IntentFilter[]) arrayMap.valueAt(mapi);
            int N = a.length;
            boolean printedHeader = false;
            if (!collapseDuplicates || printFilter) {
                printedSomething = printedSomething3;
                printer = printer4;
                title2 = title4;
                int i = 0;
                while (i < N) {
                    F f = a[i];
                    F filter2 = f;
                    if (f == null) {
                        break;
                    }
                    if (str2 == null || intentResolver.isPackageForFilter(str2, filter2)) {
                        if (title2 != null) {
                            out.print(titlePrefix);
                            printWriter.println(title2);
                            title2 = null;
                        }
                        if (!printedHeader) {
                            printWriter.print(eprefix);
                            printWriter.print(arrayMap.keyAt(mapi));
                            printWriter.println(":");
                            printedHeader = true;
                        }
                        intentResolver.dumpFilter(printWriter, fprefix, filter2);
                        if (printFilter) {
                            if (printer == null) {
                                printer2 = new PrintWriterPrinter(printWriter);
                            } else {
                                printer2 = printer;
                            }
                            filter2.dump(printer2, fprefix + "  ");
                            printedSomething = true;
                            printer = printer2;
                        } else {
                            printedSomething = true;
                        }
                    }
                    i++;
                    intentResolver = this;
                    printWriter = out;
                }
            } else {
                found.clear();
                int i2 = 0;
                while (true) {
                    int i3 = i2;
                    if (i3 >= N) {
                        break;
                    }
                    F f2 = a[i3];
                    F filter3 = f2;
                    if (f2 == null) {
                        break;
                    }
                    if (str2 != null) {
                        filter = filter3;
                        if (!intentResolver.isPackageForFilter(str2, filter)) {
                            F f3 = filter;
                            printedSomething2 = printedSomething3;
                            title3 = title4;
                            printer3 = printer4;
                            i2 = i3 + 1;
                            title4 = title3;
                            printer4 = printer3;
                            printedSomething3 = printedSomething2;
                            String str3 = prefix;
                        }
                    } else {
                        filter = filter3;
                    }
                    title3 = title4;
                    Object label = intentResolver.filterToLabel(filter);
                    F f4 = filter;
                    int index = found.indexOfKey(label);
                    printer3 = printer4;
                    if (index < 0) {
                        printedSomething2 = printedSomething3;
                        found.put(label, new MutableInt(1));
                    } else {
                        printedSomething2 = printedSomething3;
                        int i4 = index;
                        found.valueAt(index).value++;
                    }
                    i2 = i3 + 1;
                    title4 = title3;
                    printer4 = printer3;
                    printedSomething3 = printedSomething2;
                    String str32 = prefix;
                }
                printedSomething = printedSomething3;
                printer = printer4;
                title2 = title4;
                for (int i5 = 0; i5 < found.size(); i5++) {
                    if (title2 != null) {
                        out.print(titlePrefix);
                        printWriter.println(title2);
                        title2 = null;
                    }
                    if (!printedHeader) {
                        printWriter.print(eprefix);
                        printWriter.print(arrayMap.keyAt(mapi));
                        printWriter.println(":");
                        printedHeader = true;
                    }
                    printedSomething = true;
                    intentResolver.dumpFilterLabel(printWriter, fprefix, found.keyAt(i5), found.valueAt(i5).value);
                }
            }
            title4 = title2;
            printer4 = printer;
            printedSomething3 = printedSomething;
            mapi++;
            intentResolver = this;
            printWriter = out;
            String str4 = prefix;
        }
        String str5 = title4;
        Printer printer5 = printer4;
        return printedSomething3;
    }

    /* access modifiers changed from: package-private */
    public void writeProtoMap(ProtoOutputStream proto, long fieldId, ArrayMap<String, F[]> map) {
        ProtoOutputStream protoOutputStream = proto;
        ArrayMap<String, F[]> arrayMap = map;
        int N = map.size();
        for (int mapi = 0; mapi < N; mapi++) {
            long token = proto.start(fieldId);
            protoOutputStream.write(1138166333441L, arrayMap.keyAt(mapi));
            for (F f : (IntentFilter[]) arrayMap.valueAt(mapi)) {
                if (f != null) {
                    protoOutputStream.write(2237677961218L, f.toString());
                }
            }
            protoOutputStream.end(token);
        }
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        writeProtoMap(proto, 2246267895809L, this.mTypeToFilter);
        writeProtoMap(proto, 2246267895810L, this.mBaseTypeToFilter);
        writeProtoMap(proto, 2246267895811L, this.mWildTypeToFilter);
        writeProtoMap(proto, 2246267895812L, this.mSchemeToFilter);
        writeProtoMap(proto, 2246267895813L, this.mActionToFilter);
        writeProtoMap(proto, 2246267895814L, this.mTypedActionToFilter);
        proto.end(token);
    }

    public boolean dump(PrintWriter out, String title, String prefix, String packageName, boolean printFilter, boolean collapseDuplicates) {
        String str = prefix;
        String innerPrefix = str + "  ";
        String sepPrefix = "\n" + str;
        String curPrefix = title + "\n" + str;
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
        ArrayList<R> resultList = new ArrayList<>();
        int i = 0;
        boolean debug = (intent.getFlags() & 8) != 0;
        FastImmutableArraySet<String> categories = getFastIntentCategories(intent);
        String scheme = intent.getScheme();
        int N = listCut.size();
        while (true) {
            int i2 = i;
            if (i2 < N) {
                buildResolveList(intent, categories, debug, defaultOnly, resolvedType, scheme, (IntentFilter[]) listCut.get(i2), resultList, userId);
                i = i2 + 1;
            } else {
                filterResults(resultList);
                sortResults(resultList);
                return resultList;
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:39:0x0145  */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0162  */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x01a1  */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x01c8  */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x01e3  */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x0205  */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x0217  */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x022b  */
    /* JADX WARNING: Removed duplicated region for block: B:70:0x023e  */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x0258  */
    public List<R> queryIntent(Intent intent, String resolvedType, boolean defaultOnly, int userId) {
        boolean z;
        Intent intent2;
        F[] secondTypeCut;
        F[] thirdTypeCut;
        F[] schemeCut;
        F[] firstTypeCut;
        F[] secondTypeCut2;
        F[] firstTypeCut2;
        F[] firstTypeCut3;
        F[] firstTypeCut4;
        F[] firstTypeCut5;
        String str = resolvedType;
        String scheme = intent.getScheme();
        ArrayList<R> finalList = new ArrayList<>();
        boolean debug = (intent.getFlags() & 8) != 0;
        if (debug) {
            StringBuilder sb = new StringBuilder();
            sb.append("Resolving type=");
            sb.append(str);
            sb.append(" scheme=");
            sb.append(scheme);
            sb.append(" defaultOnly=");
            z = defaultOnly;
            sb.append(z);
            sb.append(" userId=");
            sb.append(userId);
            sb.append(" of ");
            intent2 = intent;
            sb.append(intent2);
            Slog.v(TAG, sb.toString());
        } else {
            intent2 = intent;
            z = defaultOnly;
            int i = userId;
        }
        F[] firstTypeCut6 = null;
        if (str != null) {
            int slashpos = str.indexOf(47);
            if (slashpos > 0) {
                String baseType = str.substring(0, slashpos);
                if (!baseType.equals("*")) {
                    if (resolvedType.length() == slashpos + 2) {
                        if (str.charAt(slashpos + 1) == '*') {
                            F[] firstTypeCut7 = (IntentFilter[]) this.mBaseTypeToFilter.get(baseType);
                            if (debug) {
                                Slog.v(TAG, "First type cut: " + Arrays.toString(firstTypeCut7));
                            }
                            secondTypeCut2 = (IntentFilter[]) this.mWildTypeToFilter.get(baseType);
                            if (debug) {
                                StringBuilder sb2 = new StringBuilder();
                                firstTypeCut5 = firstTypeCut7;
                                sb2.append("Second type cut: ");
                                sb2.append(Arrays.toString(secondTypeCut2));
                                Slog.v(TAG, sb2.toString());
                            } else {
                                firstTypeCut5 = firstTypeCut7;
                            }
                            firstTypeCut2 = firstTypeCut5;
                            F[] thirdTypeCut2 = (IntentFilter[]) this.mWildTypeToFilter.get("*");
                            if (!debug) {
                                StringBuilder sb3 = new StringBuilder();
                                firstTypeCut3 = firstTypeCut2;
                                sb3.append("Third type cut: ");
                                sb3.append(Arrays.toString(thirdTypeCut2));
                                Slog.v(TAG, sb3.toString());
                            } else {
                                firstTypeCut3 = firstTypeCut2;
                            }
                            secondTypeCut = secondTypeCut2;
                            thirdTypeCut = thirdTypeCut2;
                            firstTypeCut6 = firstTypeCut3;
                            if (scheme == null) {
                                F[] schemeCut2 = (IntentFilter[]) this.mSchemeToFilter.get(scheme);
                                if (debug) {
                                    Slog.v(TAG, "Scheme list: " + Arrays.toString(schemeCut2));
                                }
                                schemeCut = schemeCut2;
                            } else {
                                schemeCut = null;
                            }
                            if (str == null && scheme == null && intent.getAction() != null) {
                                firstTypeCut6 = (IntentFilter[]) this.mActionToFilter.get(intent.getAction());
                                if (debug) {
                                    Slog.v(TAG, "Action list: " + Arrays.toString(firstTypeCut6));
                                }
                            }
                            firstTypeCut = firstTypeCut6;
                            FastImmutableArraySet<String> categories = getFastIntentCategories(intent);
                            if (firstTypeCut != null) {
                                buildResolveList(intent2, categories, debug, z, str, scheme, firstTypeCut, finalList, userId);
                            }
                            if (secondTypeCut != null) {
                                buildResolveList(intent, categories, debug, defaultOnly, str, scheme, secondTypeCut, finalList, userId);
                            }
                            if (thirdTypeCut != null) {
                                buildResolveList(intent, categories, debug, defaultOnly, str, scheme, thirdTypeCut, finalList, userId);
                            }
                            if (schemeCut != null) {
                                buildResolveList(intent, categories, debug, defaultOnly, str, scheme, schemeCut, finalList, userId);
                            }
                            filterResults(finalList);
                            sortResults(finalList);
                            if (debug) {
                                Slog.v(TAG, "Final result list:");
                                int i2 = 0;
                                while (true) {
                                    int i3 = i2;
                                    if (i3 >= finalList.size()) {
                                        break;
                                    }
                                    Slog.v(TAG, "  " + finalList.get(i3));
                                    i2 = i3 + 1;
                                }
                            }
                            return finalList;
                        }
                    }
                    F[] firstTypeCut8 = (IntentFilter[]) this.mTypeToFilter.get(str);
                    if (debug) {
                        Slog.v(TAG, "First type cut: " + Arrays.toString(firstTypeCut8));
                    }
                    secondTypeCut2 = (IntentFilter[]) this.mWildTypeToFilter.get(baseType);
                    if (debug) {
                        StringBuilder sb4 = new StringBuilder();
                        firstTypeCut4 = firstTypeCut8;
                        sb4.append("Second type cut: ");
                        sb4.append(Arrays.toString(secondTypeCut2));
                        Slog.v(TAG, sb4.toString());
                    } else {
                        firstTypeCut4 = firstTypeCut8;
                    }
                    firstTypeCut2 = firstTypeCut4;
                    F[] thirdTypeCut22 = (IntentFilter[]) this.mWildTypeToFilter.get("*");
                    if (!debug) {
                    }
                    secondTypeCut = secondTypeCut2;
                    thirdTypeCut = thirdTypeCut22;
                    firstTypeCut6 = firstTypeCut3;
                    if (scheme == null) {
                    }
                    firstTypeCut6 = (IntentFilter[]) this.mActionToFilter.get(intent.getAction());
                    if (debug) {
                    }
                    firstTypeCut = firstTypeCut6;
                    FastImmutableArraySet<String> categories2 = getFastIntentCategories(intent);
                    if (firstTypeCut != null) {
                    }
                    if (secondTypeCut != null) {
                    }
                    if (thirdTypeCut != null) {
                    }
                    if (schemeCut != null) {
                    }
                    filterResults(finalList);
                    sortResults(finalList);
                    if (debug) {
                    }
                    return finalList;
                }
                secondTypeCut = null;
                if (intent.getAction() != null) {
                    firstTypeCut6 = (IntentFilter[]) this.mTypedActionToFilter.get(intent.getAction());
                    if (debug) {
                        Slog.v(TAG, "Typed Action list: " + Arrays.toString(firstTypeCut6));
                    }
                }
                thirdTypeCut = null;
                if (scheme == null) {
                }
                firstTypeCut6 = (IntentFilter[]) this.mActionToFilter.get(intent.getAction());
                if (debug) {
                }
                firstTypeCut = firstTypeCut6;
                FastImmutableArraySet<String> categories22 = getFastIntentCategories(intent);
                if (firstTypeCut != null) {
                }
                if (secondTypeCut != null) {
                }
                if (thirdTypeCut != null) {
                }
                if (schemeCut != null) {
                }
                filterResults(finalList);
                sortResults(finalList);
                if (debug) {
                }
                return finalList;
            }
        }
        secondTypeCut = null;
        thirdTypeCut = null;
        if (scheme == null) {
        }
        firstTypeCut6 = (IntentFilter[]) this.mActionToFilter.get(intent.getAction());
        if (debug) {
        }
        firstTypeCut = firstTypeCut6;
        FastImmutableArraySet<String> categories222 = getFastIntentCategories(intent);
        if (firstTypeCut != null) {
        }
        if (secondTypeCut != null) {
        }
        if (thirdTypeCut != null) {
        }
        if (schemeCut != null) {
        }
        filterResults(finalList);
        sortResults(finalList);
        if (debug) {
        }
        return finalList;
    }

    /* access modifiers changed from: protected */
    public boolean allowFilterResult(F f, List<R> list) {
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean isFilterStopped(F f, int userId) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isFilterVerified(F filter) {
        return filter.isVerified();
    }

    /* JADX WARNING: type inference failed for: r1v0, types: [R, F] */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Unknown variable types count: 1 */
    public R newResult(F r1, int match, int userId) {
        return r1;
    }

    /* access modifiers changed from: protected */
    public void sortResults(List<R> results) {
        Collections.sort(results, mResolvePrioritySorter);
    }

    /* access modifiers changed from: protected */
    public void filterResults(List<R> list) {
    }

    /* access modifiers changed from: protected */
    public void dumpFilter(PrintWriter out, String prefix, F filter) {
        out.print(prefix);
        out.println(filter);
    }

    /* access modifiers changed from: protected */
    public Object filterToLabel(F f) {
        return "IntentFilter";
    }

    /* access modifiers changed from: protected */
    public void dumpFilterLabel(PrintWriter out, String prefix, Object label, int count) {
        out.print(prefix);
        out.print(label);
        out.print(": ");
        out.println(count);
    }

    private final void addFilter(ArrayMap<String, F[]> map, String name, F filter) {
        F[] array = (IntentFilter[]) map.get(name);
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
            String name = i.next();
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
            String name = i.next();
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
        int num = 0;
        if (i == null) {
            return 0;
        }
        while (i.hasNext()) {
            num++;
            addFilter(dest, i.next(), filter);
        }
        return num;
    }

    private final int unregister_intent_filter(F filter, Iterator<String> i, ArrayMap<String, F[]> dest, String prefix) {
        int num = 0;
        if (i == null) {
            return 0;
        }
        while (i.hasNext()) {
            num++;
            remove_all_objects(dest, i.next(), filter);
        }
        return num;
    }

    private final void remove_all_objects(ArrayMap<String, F[]> map, String name, Object object) {
        F[] array = (IntentFilter[]) map.get(name);
        if (array != null) {
            int LAST = array.length - 1;
            while (LAST >= 0 && array[LAST] == null) {
                LAST--;
            }
            int LAST2 = LAST;
            while (LAST >= 0) {
                if (array[LAST] == object) {
                    int remain = LAST2 - LAST;
                    if (remain > 0) {
                        System.arraycopy(array, LAST + 1, array, LAST, remain);
                    }
                    array[LAST2] = null;
                    LAST2--;
                }
                LAST--;
            }
            if (LAST2 < 0) {
                map.remove(name);
            } else if (LAST2 < array.length / 2) {
                F[] newa = newArray(LAST2 + 2);
                System.arraycopy(array, 0, newa, 0, LAST2 + 1);
                map.put(name, newa);
            }
        }
    }

    private static FastImmutableArraySet<String> getFastIntentCategories(Intent intent) {
        Set<String> categories = intent.getCategories();
        if (categories == null) {
            return null;
        }
        return new FastImmutableArraySet<>((String[]) categories.toArray(new String[categories.size()]));
    }

    private void buildResolveList(Intent intent, FastImmutableArraySet<String> categories, boolean debug, boolean defaultOnly, String resolvedType, String scheme, F[] src, List<R> dest, int userId) {
        Printer logPrinter;
        FastPrintWriter fastPrintWriter;
        int i;
        String packageName;
        Uri data;
        String action;
        int N;
        int i2;
        Printer logPrinter2;
        FastPrintWriter fastPrintWriter2;
        String reason;
        F[] fArr = src;
        List<R> list = dest;
        int i3 = userId;
        String filter = intent.getAction();
        Uri data2 = intent.getData();
        String packageName2 = intent.getPackage();
        boolean excludingStopped = intent.isExcludingStopped();
        if (debug) {
            logPrinter = new LogPrinter(2, TAG, 3);
            fastPrintWriter = new FastPrintWriter(logPrinter);
        } else {
            logPrinter = null;
            fastPrintWriter = null;
        }
        Printer logPrinter3 = logPrinter;
        FastPrintWriter fastPrintWriter3 = fastPrintWriter;
        int N2 = fArr != null ? fArr.length : 0;
        boolean hasNonDefaults = false;
        int i4 = 0;
        while (true) {
            i = i4;
            if (i < N2) {
                F f = fArr[i];
                F filter2 = f;
                if (f != null) {
                    if (debug && HWFLOW) {
                        Slog.v(TAG, "Matching against filter " + filter2);
                    }
                    if (!excludingStopped || !isFilterStopped(filter2, i3)) {
                        if (packageName2 == null || isPackageForFilter(packageName2, filter2)) {
                            if (filter2.getAutoVerify() && debug) {
                                Slog.v(TAG, "  Filter verified: " + isFilterVerified(filter2));
                                int authorities = filter2.countDataAuthorities();
                                int z = 0;
                                while (z < authorities) {
                                    Slog.v(TAG, "   " + filter2.getDataAuthority(z).getHost());
                                    z++;
                                    authorities = authorities;
                                    i = i;
                                }
                            }
                            int i5 = i;
                            if (!allowFilterResult(filter2, list)) {
                                if (debug) {
                                    Slog.v(TAG, "  Filter's target already added");
                                }
                                action = filter;
                                data = data2;
                                packageName = packageName2;
                                i2 = i5;
                                N = N2;
                                fastPrintWriter2 = fastPrintWriter3;
                                logPrinter2 = logPrinter3;
                                i4 = i2 + 1;
                                fastPrintWriter3 = fastPrintWriter2;
                                logPrinter3 = logPrinter2;
                                N2 = N;
                                filter = action;
                                data2 = data;
                                packageName2 = packageName;
                                fArr = src;
                            } else {
                                action = filter;
                                i2 = i5;
                                F filter3 = filter2;
                                N = N2;
                                Uri uri = data2;
                                data = data2;
                                fastPrintWriter2 = fastPrintWriter3;
                                packageName = packageName2;
                                logPrinter2 = logPrinter3;
                                int match = filter2.match(filter, resolvedType, scheme, uri, categories, TAG);
                                if (match >= 0) {
                                    if (debug) {
                                        Slog.v(TAG, "  Filter matched!  match=0x" + Integer.toHexString(match) + " hasDefault=" + filter3.hasCategory("android.intent.category.DEFAULT"));
                                    }
                                    if (!defaultOnly || filter3.hasCategory("android.intent.category.DEFAULT")) {
                                        R oneResult = newResult(filter3, match, i3);
                                        if (oneResult != null) {
                                            list.add(oneResult);
                                            if (debug) {
                                                dumpFilter(fastPrintWriter2, "    ", filter3);
                                                fastPrintWriter2.flush();
                                                filter3.dump(logPrinter2, "    ");
                                            }
                                        }
                                    } else {
                                        hasNonDefaults = true;
                                    }
                                } else if (debug && HWFLOW) {
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
                                            reason = DatabaseHelper.SoundModelContract.KEY_TYPE;
                                            break;
                                        default:
                                            reason = "unknown reason";
                                            break;
                                    }
                                    Slog.v(TAG, "  Filter did not match: " + reason);
                                }
                                i4 = i2 + 1;
                                fastPrintWriter3 = fastPrintWriter2;
                                logPrinter3 = logPrinter2;
                                N2 = N;
                                filter = action;
                                data2 = data;
                                packageName2 = packageName;
                                fArr = src;
                            }
                        } else if (debug) {
                            Slog.v(TAG, "  Filter is not from package " + packageName2 + "; skipping");
                        }
                    } else if (debug) {
                        Slog.v(TAG, "  Filter's target is stopped; skipping");
                    }
                    i2 = i;
                    N = N2;
                    action = filter;
                    data = data2;
                    packageName = packageName2;
                    fastPrintWriter2 = fastPrintWriter3;
                    logPrinter2 = logPrinter3;
                    i4 = i2 + 1;
                    fastPrintWriter3 = fastPrintWriter2;
                    logPrinter3 = logPrinter2;
                    N2 = N;
                    filter = action;
                    data2 = data;
                    packageName2 = packageName;
                    fArr = src;
                }
            }
        }
        int i6 = i;
        int i7 = N2;
        String str = filter;
        Uri uri2 = data2;
        String str2 = packageName2;
        FastPrintWriter fastPrintWriter4 = fastPrintWriter3;
        Printer printer = logPrinter3;
        if (debug && hasNonDefaults) {
            if (dest.size() == 0) {
                Slog.v(TAG, "resolveIntent failed: found match, but none with CATEGORY_DEFAULT");
            } else if (dest.size() > 1) {
                Slog.v(TAG, "resolveIntent: multiple matches, only some with CATEGORY_DEFAULT");
            }
        }
    }
}
