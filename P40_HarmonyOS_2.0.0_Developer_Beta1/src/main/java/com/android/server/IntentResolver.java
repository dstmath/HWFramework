package com.android.server;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.FastImmutableArraySet;
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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public abstract class IntentResolver<F extends IntentFilter, R> {
    private static final boolean DEBUG = false;
    private static final String TAG = "IntentResolver";
    private static final boolean localLOGV = false;
    private static final boolean localVerificationLOGV = false;
    private static final Comparator mResolvePrioritySorter = new Comparator() {
        /* class com.android.server.IntentResolver.AnonymousClass1 */

        @Override // java.util.Comparator
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
        F cur;
        ArrayList<F> res = null;
        if (array != null) {
            int i = 0;
            while (i < array.length && (cur = array[i]) != null) {
                if (filterEquals(cur, matching)) {
                    if (res == null) {
                        res = new ArrayList<>();
                    }
                    res.add(cur);
                }
                i++;
            }
        }
        return res;
    }

    public ArrayList<F> findFilters(IntentFilter matching) {
        if (matching.countDataSchemes() == 1) {
            return collectFilters(this.mSchemeToFilter.get(matching.getDataScheme(0)), matching);
        }
        if (matching.countDataTypes() != 0 && matching.countActions() == 1) {
            return collectFilters(this.mTypedActionToFilter.get(matching.getAction(0)), matching);
        }
        if (matching.countDataTypes() == 0 && matching.countDataSchemes() == 0 && matching.countActions() == 1) {
            return collectFilters(this.mActionToFilter.get(matching.getAction(0)), matching);
        }
        ArrayList<F> res = null;
        Iterator<F> it = this.mFilters.iterator();
        while (it.hasNext()) {
            F cur = it.next();
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
            unregister_intent_filter(f, f.actionsIterator(), this.mActionToFilter, "      Action: ");
        }
        if (numT != 0) {
            unregister_intent_filter(f, f.actionsIterator(), this.mTypedActionToFilter, "      TypedAction: ");
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX INFO: Multiple debug info for r12v16 'filter'  F extends android.content.IntentFilter: [D('printer' android.util.Printer), D('filter' F extends android.content.IntentFilter)] */
    /* JADX WARN: Type inference failed for: r0v3, types: [com.android.server.IntentResolver] */
    /* access modifiers changed from: package-private */
    public boolean dumpMap(PrintWriter out, String titlePrefix, String title, String prefix, ArrayMap<String, F[]> map, String packageName, boolean printFilter, boolean collapseDuplicates) {
        String str;
        String str2;
        Printer printer;
        String str3;
        boolean printedSomething;
        Printer printer2;
        boolean filter;
        String str4;
        boolean printedSomething2;
        boolean printedHeader;
        Printer printer3;
        F filter2;
        IntentResolver<F, R> intentResolver = this;
        PrintWriter printWriter = out;
        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        String str5 = "  ";
        sb.append(str5);
        String eprefix = sb.toString();
        String fprefix = prefix + "    ";
        ArrayMap<Object, MutableInt> found = new ArrayMap<>();
        boolean printedSomething3 = false;
        int mapi = 0;
        Printer printer4 = null;
        String title2 = title;
        while (mapi < map.size()) {
            F[] a = map.valueAt(mapi);
            int N = a.length;
            boolean printedHeader2 = false;
            if (!collapseDuplicates || printFilter) {
                String str6 = str5;
                boolean printedSomething4 = printedSomething3;
                Printer printer5 = printer4;
                boolean printedHeader3 = false;
                int i = 0;
                String title3 = title2;
                while (true) {
                    if (i >= N) {
                        str = str6;
                        break;
                    }
                    F filter3 = a[i];
                    if (filter3 == null) {
                        str = str6;
                        break;
                    }
                    if (packageName == null || intentResolver.isPackageForFilter(packageName, filter3)) {
                        if (title3 != null) {
                            out.print(titlePrefix);
                            printWriter.println(title3);
                            title3 = null;
                        }
                        if (!printedHeader3) {
                            printWriter.print(eprefix);
                            printWriter.print(map.keyAt(mapi));
                            printWriter.println(":");
                            printedHeader3 = true;
                        }
                        intentResolver.dumpFilter(printWriter, fprefix, filter3);
                        if (printFilter) {
                            if (printer5 == null) {
                                printer = new PrintWriterPrinter(printWriter);
                            } else {
                                printer = printer5;
                            }
                            StringBuilder sb2 = new StringBuilder();
                            sb2.append(fprefix);
                            str2 = str6;
                            sb2.append(str2);
                            filter3.dump(printer, sb2.toString());
                            printedSomething4 = true;
                            printer5 = printer;
                        } else {
                            str2 = str6;
                            printedSomething4 = true;
                        }
                    } else {
                        str2 = str6;
                    }
                    i++;
                    intentResolver = this;
                    str6 = str2;
                    printWriter = out;
                }
                title2 = title3;
                printer4 = printer5;
                printedSomething3 = printedSomething4;
            } else {
                found.clear();
                int i2 = 0;
                while (true) {
                    if (i2 >= N) {
                        str3 = str5;
                        printedSomething = printedSomething3;
                        printer2 = printer4;
                        filter = printedHeader2;
                        break;
                    }
                    F filter4 = a[i2];
                    if (filter4 == null) {
                        str3 = str5;
                        printedSomething = printedSomething3;
                        printer2 = printer4;
                        filter = printedHeader2;
                        break;
                    }
                    if (packageName != null) {
                        printer3 = printer4;
                        filter2 = filter4;
                        if (!intentResolver.isPackageForFilter(packageName, filter2)) {
                            str4 = str5;
                            printedSomething2 = printedSomething3;
                            printedHeader = printedHeader2;
                            i2++;
                            printer4 = printer3;
                            printedHeader2 = printedHeader;
                            printedSomething3 = printedSomething2;
                            str5 = str4;
                        }
                    } else {
                        printer3 = printer4;
                        filter2 = filter4;
                    }
                    printedHeader = printedHeader2;
                    Object label = intentResolver.filterToLabel(filter2);
                    int index = found.indexOfKey(label);
                    printedSomething2 = printedSomething3;
                    if (index < 0) {
                        str4 = str5;
                        found.put(label, new MutableInt(1));
                    } else {
                        str4 = str5;
                        found.valueAt(index).value++;
                    }
                    i2++;
                    printer4 = printer3;
                    printedHeader2 = printedHeader;
                    printedSomething3 = printedSomething2;
                    str5 = str4;
                }
                String title4 = title2;
                for (int i3 = 0; i3 < found.size(); i3++) {
                    if (title4 != null) {
                        out.print(titlePrefix);
                        printWriter.println(title4);
                        title4 = null;
                    }
                    if (!filter) {
                        printWriter.print(eprefix);
                        printWriter.print(map.keyAt(mapi));
                        printWriter.println(":");
                        filter = true;
                    }
                    printedSomething = true;
                    intentResolver.dumpFilterLabel(printWriter, fprefix, found.keyAt(i3), found.valueAt(i3).value);
                }
                title2 = title4;
                printer4 = printer2;
                printedSomething3 = printedSomething;
                str = str3;
            }
            mapi++;
            intentResolver = this;
            str5 = str;
            printWriter = out;
        }
        return printedSomething3;
    }

    /* access modifiers changed from: package-private */
    public void writeProtoMap(ProtoOutputStream proto, long fieldId, ArrayMap<String, F[]> map) {
        int N = map.size();
        for (int mapi = 0; mapi < N; mapi++) {
            long token = proto.start(fieldId);
            proto.write(1138166333441L, map.keyAt(mapi));
            F[] valueAt = map.valueAt(mapi);
            for (F f : valueAt) {
                if (f != null) {
                    proto.write(2237677961218L, f.toString());
                }
            }
            proto.end(token);
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

    private class IteratorWrapper implements Iterator<F> {
        private F mCur;
        private final Iterator<F> mI;

        IteratorWrapper(Iterator<F> it) {
            this.mI = it;
        }

        @Override // java.util.Iterator
        public boolean hasNext() {
            return this.mI.hasNext();
        }

        /* JADX WARN: Type inference failed for: r0v2, types: [F, F extends android.content.IntentFilter, android.content.IntentFilter] */
        @Override // java.util.Iterator
        public F next() {
            F next = this.mI.next();
            this.mCur = next;
            return next;
        }

        /* JADX DEBUG: Multi-variable search result rejected for r1v0, resolved type: com.android.server.IntentResolver */
        /* JADX WARN: Multi-variable type inference failed */
        @Override // java.util.Iterator
        public void remove() {
            F f = this.mCur;
            if (f != null) {
                IntentResolver.this.removeFilterInternal(f);
            }
            this.mI.remove();
        }
    }

    public Iterator<F> filterIterator() {
        return new IteratorWrapper(this.mFilters.iterator());
    }

    public Set<F> filterSet() {
        return Collections.unmodifiableSet(this.mFilters);
    }

    public List<R> queryIntentFromList(Intent intent, String resolvedType, boolean defaultOnly, ArrayList<F[]> listCut, int userId) {
        ArrayList<R> resultList = new ArrayList<>();
        boolean debug = (intent.getFlags() & 8) != 0;
        FastImmutableArraySet<String> categories = getFastIntentCategories(intent);
        String scheme = intent.getScheme();
        int N = listCut.size();
        for (int i = 0; i < N; i++) {
            buildResolveList(intent, categories, debug, defaultOnly, resolvedType, scheme, listCut.get(i), resultList, userId);
        }
        filterResults(resultList);
        sortResults(resultList);
        return resultList;
    }

    public List<R> queryAllByActionPrefix(int userId, String match) {
        ArrayList<R> resultList = new ArrayList<>();
        Iterator<F> it = this.mFilters.iterator();
        while (it.hasNext()) {
            F cur = it.next();
            int actionNum = cur.countActions();
            int index = 0;
            while (true) {
                if (index >= actionNum) {
                    break;
                }
                if (cur.getAction(index) != null && cur.getAction(index).startsWith(match)) {
                    if (!allowFilterResult(cur, resultList)) {
                        break;
                    }
                    R oneResult = newResult(cur, 0, userId);
                    if (oneResult != null) {
                        resultList.add(oneResult);
                        break;
                    }
                }
                index++;
            }
        }
        filterResults(resultList);
        sortResults(resultList);
        return resultList;
    }

    /* JADX WARNING: Removed duplicated region for block: B:43:0x018c  */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x01c9  */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x01e9  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x01ff  */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x0215  */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x022b  */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x0247  */
    public List<R> queryIntent(Intent intent, String resolvedType, boolean defaultOnly, int userId) {
        F[] schemeCut;
        F[] thirdTypeCut;
        F[] secondTypeCut;
        F[] firstTypeCut;
        F[] firstTypeCut2;
        F[] secondTypeCut2;
        String scheme = intent.getScheme();
        ArrayList<R> finalList = new ArrayList<>();
        boolean debug = (intent.getFlags() & 8) != 0;
        if (debug) {
            Slog.v(TAG, "Resolving type=" + resolvedType + " scheme=" + scheme + " defaultOnly=" + defaultOnly + " userId=" + userId + " of " + intent);
        }
        if (resolvedType != null) {
            int slashpos = resolvedType.indexOf(47);
            if (slashpos > 0) {
                String baseType = resolvedType.substring(0, slashpos);
                if (!baseType.equals("*")) {
                    schemeCut = null;
                    if (resolvedType.length() == slashpos + 2 && resolvedType.charAt(slashpos + 1) == '*') {
                        firstTypeCut = this.mBaseTypeToFilter.get(baseType);
                        if (debug) {
                            Slog.v(TAG, "First type cut: " + Arrays.toString(firstTypeCut));
                        }
                        secondTypeCut2 = this.mWildTypeToFilter.get(baseType);
                        if (debug) {
                            Slog.v(TAG, "Second type cut: " + Arrays.toString(secondTypeCut2));
                        }
                    } else {
                        firstTypeCut = this.mTypeToFilter.get(resolvedType);
                        if (debug) {
                            Slog.v(TAG, "First type cut: " + Arrays.toString(firstTypeCut));
                        }
                        secondTypeCut2 = this.mWildTypeToFilter.get(baseType);
                        if (debug) {
                            Slog.v(TAG, "Second type cut: " + Arrays.toString(secondTypeCut2));
                        }
                    }
                    F[] thirdTypeCut2 = this.mWildTypeToFilter.get("*");
                    if (debug) {
                        Slog.v(TAG, "Third type cut: " + Arrays.toString(thirdTypeCut2));
                    }
                    secondTypeCut = secondTypeCut2;
                    thirdTypeCut = thirdTypeCut2;
                } else {
                    firstTypeCut2 = null;
                    secondTypeCut = null;
                    thirdTypeCut = null;
                    schemeCut = null;
                    if (intent.getAction() != null) {
                        firstTypeCut = this.mTypedActionToFilter.get(intent.getAction());
                        if (debug) {
                            Slog.v(TAG, "Typed Action list: " + Arrays.toString(firstTypeCut));
                        }
                    }
                }
                if (scheme != null) {
                    F[] schemeCut2 = this.mSchemeToFilter.get(scheme);
                    if (debug) {
                        Slog.v(TAG, "Scheme list: " + Arrays.toString(schemeCut2));
                    }
                    schemeCut = schemeCut2;
                }
                if (resolvedType == null && scheme == null && intent.getAction() != null) {
                    firstTypeCut = this.mActionToFilter.get(intent.getAction());
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
                        Slog.v(TAG, "  " + ((Object) finalList.get(i)));
                    }
                }
                return finalList;
            }
            firstTypeCut2 = null;
            secondTypeCut = null;
            thirdTypeCut = null;
            schemeCut = null;
        } else {
            firstTypeCut2 = null;
            secondTypeCut = null;
            thirdTypeCut = null;
            schemeCut = null;
        }
        firstTypeCut = firstTypeCut2;
        if (scheme != null) {
        }
        firstTypeCut = this.mActionToFilter.get(intent.getAction());
        if (debug) {
        }
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

    /* JADX DEBUG: Multi-variable search result rejected for r1v0, resolved type: F extends android.content.IntentFilter */
    /* JADX WARN: Multi-variable type inference failed */
    /* access modifiers changed from: protected */
    public R newResult(F filter, int match, int userId) {
        return filter;
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
        F[] array = map.get(name);
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
        if (i == null) {
            return 0;
        }
        int num = 0;
        while (i.hasNext()) {
            num++;
            addFilter(dest, i.next(), filter);
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
            remove_all_objects(dest, i.next(), filter);
        }
        return num;
    }

    private final void remove_all_objects(ArrayMap<String, F[]> map, String name, Object object) {
        F[] array = map.get(name);
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
        return new FastImmutableArraySet<>((String[]) categories.toArray(new String[categories.size()]));
    }

    /* JADX INFO: Multiple debug info for r12v6 'logPrintWriter'  java.io.PrintWriter: [D('logPrintWriter' java.io.PrintWriter), D('data' android.net.Uri)] */
    /* JADX INFO: Multiple debug info for r13v6 'logPrinter'  android.util.Printer: [D('logPrinter' android.util.Printer), D('packageName' java.lang.String)] */
    private void buildResolveList(Intent intent, FastImmutableArraySet<String> categories, boolean debug, boolean defaultOnly, String resolvedType, String scheme, F[] src, List<R> dest, int userId) {
        Printer logPrinter;
        PrintWriter logPrintWriter;
        String packageName;
        Uri data;
        int N;
        int i;
        String action;
        Printer logPrinter2;
        PrintWriter logPrintWriter2;
        String reason;
        F[] fArr = src;
        String action2 = intent.getAction();
        Uri data2 = intent.getData();
        String packageName2 = intent.getPackage();
        boolean excludingStopped = intent.isExcludingStopped();
        if (debug) {
            Printer logPrinter3 = new LogPrinter(2, TAG, 3);
            logPrinter = logPrinter3;
            logPrintWriter = new FastPrintWriter(logPrinter3);
        } else {
            logPrinter = null;
            logPrintWriter = null;
        }
        int N2 = fArr != null ? fArr.length : 0;
        boolean hasNonDefaults = false;
        int i2 = 0;
        while (true) {
            if (i2 >= N2) {
                break;
            }
            F filter = fArr[i2];
            if (filter == null) {
                break;
            }
            if (debug) {
                Slog.v(TAG, "Matching against filter " + filter);
            }
            if (!excludingStopped || !isFilterStopped(filter, userId)) {
                if (packageName2 == null || isPackageForFilter(packageName2, filter)) {
                    if (filter.getAutoVerify() && debug) {
                        Slog.v(TAG, "  Filter verified: " + isFilterVerified(filter));
                        int z = 0;
                        for (int authorities = filter.countDataAuthorities(); z < authorities; authorities = authorities) {
                            Slog.v(TAG, "   " + filter.getDataAuthority(z).getHost());
                            z++;
                        }
                    }
                    if (allowFilterResult(filter, dest)) {
                        action = action2;
                        i = i2;
                        N = N2;
                        data = data2;
                        logPrintWriter2 = logPrintWriter;
                        packageName = packageName2;
                        logPrinter2 = logPrinter;
                        int match = filter.match(action2, resolvedType, scheme, data2, categories, TAG);
                        if (match >= 0) {
                            if (debug) {
                                Slog.v(TAG, "  Filter matched!  match=0x" + Integer.toHexString(match) + " hasDefault=" + filter.hasCategory("android.intent.category.DEFAULT"));
                            }
                            if (!defaultOnly || filter.hasCategory("android.intent.category.DEFAULT")) {
                                R oneResult = newResult(filter, match, userId);
                                if (debug) {
                                    Slog.v(TAG, "    Created result: " + ((Object) oneResult));
                                }
                                if (oneResult != null) {
                                    dest.add(oneResult);
                                    if (debug) {
                                        dumpFilter(logPrintWriter2, "    ", filter);
                                        logPrintWriter2.flush();
                                        filter.dump(logPrinter2, "    ");
                                    }
                                }
                            } else {
                                hasNonDefaults = true;
                            }
                        } else if (debug) {
                            if (match == -4) {
                                reason = "category";
                            } else if (match == -3) {
                                reason = HwBroadcastRadarUtil.KEY_ACTION;
                            } else if (match == -2) {
                                reason = "data";
                            } else if (match != -1) {
                                reason = "unknown reason";
                            } else {
                                reason = DatabaseHelper.SoundModelContract.KEY_TYPE;
                            }
                            Slog.v(TAG, "  Filter did not match: " + reason);
                        }
                    } else if (debug) {
                        Slog.v(TAG, "  Filter's target already added");
                        i = i2;
                        N = N2;
                        action = action2;
                        data = data2;
                        packageName = packageName2;
                        logPrintWriter2 = logPrintWriter;
                        logPrinter2 = logPrinter;
                    } else {
                        i = i2;
                        N = N2;
                        action = action2;
                        data = data2;
                        packageName = packageName2;
                        logPrintWriter2 = logPrintWriter;
                        logPrinter2 = logPrinter;
                    }
                } else if (debug) {
                    Slog.v(TAG, "  Filter is not from package " + packageName2 + "; skipping");
                    i = i2;
                    N = N2;
                    action = action2;
                    data = data2;
                    packageName = packageName2;
                    logPrintWriter2 = logPrintWriter;
                    logPrinter2 = logPrinter;
                } else {
                    i = i2;
                    N = N2;
                    action = action2;
                    data = data2;
                    packageName = packageName2;
                    logPrintWriter2 = logPrintWriter;
                    logPrinter2 = logPrinter;
                }
            } else if (debug) {
                Slog.v(TAG, "  Filter's target is stopped; skipping");
                i = i2;
                N = N2;
                action = action2;
                data = data2;
                packageName = packageName2;
                logPrintWriter2 = logPrintWriter;
                logPrinter2 = logPrinter;
            } else {
                i = i2;
                N = N2;
                action = action2;
                data = data2;
                packageName = packageName2;
                logPrintWriter2 = logPrintWriter;
                logPrinter2 = logPrinter;
            }
            i2 = i + 1;
            fArr = src;
            logPrintWriter = logPrintWriter2;
            logPrinter = logPrinter2;
            action2 = action;
            N2 = N;
            data2 = data;
            packageName2 = packageName;
        }
        if (debug && hasNonDefaults) {
            if (dest.size() == 0) {
                Slog.v(TAG, "resolveIntent failed: found match, but none with CATEGORY_DEFAULT");
            } else if (dest.size() > 1) {
                Slog.v(TAG, "resolveIntent: multiple matches, only some with CATEGORY_DEFAULT");
            }
        }
    }
}
