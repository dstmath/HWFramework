package ohos.global.icu.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.global.icu.text.CurrencyMetaInfo;
import ohos.global.icu.util.Currency;

public class ICUCurrencyMetaInfo extends CurrencyMetaInfo {
    private static final int Currency = 2;
    private static final int Date = 4;
    private static final int Everything = Integer.MAX_VALUE;
    private static final long MASK = 4294967295L;
    private static final int Region = 1;
    private static final int Tender = 8;
    private ICUResourceBundle digitInfo;
    private ICUResourceBundle regionInfo;

    /* access modifiers changed from: private */
    public interface Collector<T> {
        void collect(String str, String str2, long j, long j2, int i, boolean z);

        int collects();

        List<T> getList();
    }

    public ICUCurrencyMetaInfo() {
        ICUResourceBundle bundleInstance = ICUResourceBundle.getBundleInstance(ICUData.ICU_CURR_BASE_NAME, "supplementalData", ICUResourceBundle.ICU_DATA_CLASS_LOADER);
        this.regionInfo = bundleInstance.findTopLevel("CurrencyMap");
        this.digitInfo = bundleInstance.findTopLevel("CurrencyMeta");
    }

    public List<CurrencyMetaInfo.CurrencyInfo> currencyInfo(CurrencyMetaInfo.CurrencyFilter currencyFilter) {
        return collect(new InfoCollector(), currencyFilter);
    }

    public List<String> currencies(CurrencyMetaInfo.CurrencyFilter currencyFilter) {
        return collect(new CurrencyCollector(), currencyFilter);
    }

    public List<String> regions(CurrencyMetaInfo.CurrencyFilter currencyFilter) {
        return collect(new RegionCollector(), currencyFilter);
    }

    public CurrencyMetaInfo.CurrencyDigits currencyDigits(String str) {
        return currencyDigits(str, Currency.CurrencyUsage.STANDARD);
    }

    public CurrencyMetaInfo.CurrencyDigits currencyDigits(String str, Currency.CurrencyUsage currencyUsage) {
        ICUResourceBundle findWithFallback = this.digitInfo.findWithFallback(str);
        if (findWithFallback == null) {
            findWithFallback = this.digitInfo.findWithFallback("DEFAULT");
        }
        int[] intVector = findWithFallback.getIntVector();
        if (currencyUsage == Currency.CurrencyUsage.CASH) {
            return new CurrencyMetaInfo.CurrencyDigits(intVector[2], intVector[3]);
        }
        if (currencyUsage == Currency.CurrencyUsage.STANDARD) {
            return new CurrencyMetaInfo.CurrencyDigits(intVector[0], intVector[1]);
        }
        return new CurrencyMetaInfo.CurrencyDigits(intVector[0], intVector[1]);
    }

    private <T> List<T> collect(Collector<T> collector, CurrencyMetaInfo.CurrencyFilter currencyFilter) {
        if (currencyFilter == null) {
            currencyFilter = CurrencyMetaInfo.CurrencyFilter.all();
        }
        int collects = collector.collects();
        if (currencyFilter.region != null) {
            collects |= 1;
        }
        if (currencyFilter.currency != null) {
            collects |= 2;
        }
        if (!(currencyFilter.from == Long.MIN_VALUE && currencyFilter.to == Long.MAX_VALUE)) {
            collects |= 4;
        }
        if (currencyFilter.tenderOnly) {
            collects |= 8;
        }
        if (collects != 0) {
            if (currencyFilter.region != null) {
                ICUResourceBundle findWithFallback = this.regionInfo.findWithFallback(currencyFilter.region);
                if (findWithFallback != null) {
                    collectRegion(collector, currencyFilter, collects, findWithFallback);
                }
            } else {
                for (int i = 0; i < this.regionInfo.getSize(); i++) {
                    collectRegion(collector, currencyFilter, collects, this.regionInfo.at(i));
                }
            }
        }
        return collector.getList();
    }

    private <T> void collectRegion(Collector<T> collector, CurrencyMetaInfo.CurrencyFilter currencyFilter, int i, ICUResourceBundle iCUResourceBundle) {
        boolean z;
        String key = iCUResourceBundle.getKey();
        boolean z2 = true;
        if (i == 1) {
            collector.collect(iCUResourceBundle.getKey(), null, 0, 0, -1, false);
            return;
        }
        boolean z3 = false;
        int i2 = 0;
        while (i2 < iCUResourceBundle.getSize()) {
            ICUResourceBundle at = iCUResourceBundle.at(i2);
            if (at.getSize() != 0) {
                String str = null;
                if ((i & 2) != 0) {
                    str = at.at("id").getString();
                    if (currencyFilter.currency != null && !currencyFilter.currency.equals(str)) {
                    }
                }
                long j = Long.MAX_VALUE;
                long j2 = Long.MIN_VALUE;
                if ((i & 4) != 0) {
                    j2 = getDate(at.at(Constants.ATTRNAME_FROM), Long.MIN_VALUE, z3);
                    j = getDate(at.at("to"), Long.MAX_VALUE, z2);
                    if (currencyFilter.from <= j) {
                        if (currencyFilter.to < j2) {
                        }
                    }
                }
                if ((i & 8) != 0) {
                    ICUResourceBundle at2 = at.at("tender");
                    boolean z4 = at2 == null || "true".equals(at2.getString());
                    if (!currencyFilter.tenderOnly || z4) {
                        z = z4;
                    }
                } else {
                    z = true;
                }
                collector.collect(key, str, j2, j, i2, z);
            }
            i2++;
            z2 = true;
            z3 = false;
        }
    }

    private long getDate(ICUResourceBundle iCUResourceBundle, long j, boolean z) {
        if (iCUResourceBundle == null) {
            return j;
        }
        int[] intVector = iCUResourceBundle.getIntVector();
        return (((long) intVector[0]) << 32) | (((long) intVector[1]) & MASK);
    }

    private static class UniqueList<T> {
        private List<T> list = new ArrayList();
        private Set<T> seen = new HashSet();

        private UniqueList() {
        }

        /* access modifiers changed from: private */
        public static <T> UniqueList<T> create() {
            return new UniqueList<>();
        }

        /* access modifiers changed from: package-private */
        public void add(T t) {
            if (!this.seen.contains(t)) {
                this.list.add(t);
                this.seen.add(t);
            }
        }

        /* access modifiers changed from: package-private */
        public List<T> list() {
            return Collections.unmodifiableList(this.list);
        }
    }

    private static class InfoCollector implements Collector<CurrencyMetaInfo.CurrencyInfo> {
        private List<CurrencyMetaInfo.CurrencyInfo> result;

        @Override // ohos.global.icu.impl.ICUCurrencyMetaInfo.Collector
        public int collects() {
            return Integer.MAX_VALUE;
        }

        private InfoCollector() {
            this.result = new ArrayList();
        }

        @Override // ohos.global.icu.impl.ICUCurrencyMetaInfo.Collector
        public void collect(String str, String str2, long j, long j2, int i, boolean z) {
            this.result.add(new CurrencyMetaInfo.CurrencyInfo(str, str2, j, j2, i, z));
        }

        @Override // ohos.global.icu.impl.ICUCurrencyMetaInfo.Collector
        public List<CurrencyMetaInfo.CurrencyInfo> getList() {
            return Collections.unmodifiableList(this.result);
        }
    }

    private static class RegionCollector implements Collector<String> {
        private final UniqueList<String> result;

        @Override // ohos.global.icu.impl.ICUCurrencyMetaInfo.Collector
        public int collects() {
            return 1;
        }

        private RegionCollector() {
            this.result = UniqueList.create();
        }

        @Override // ohos.global.icu.impl.ICUCurrencyMetaInfo.Collector
        public void collect(String str, String str2, long j, long j2, int i, boolean z) {
            this.result.add(str);
        }

        @Override // ohos.global.icu.impl.ICUCurrencyMetaInfo.Collector
        public List<String> getList() {
            return this.result.list();
        }
    }

    private static class CurrencyCollector implements Collector<String> {
        private final UniqueList<String> result;

        @Override // ohos.global.icu.impl.ICUCurrencyMetaInfo.Collector
        public int collects() {
            return 2;
        }

        private CurrencyCollector() {
            this.result = UniqueList.create();
        }

        @Override // ohos.global.icu.impl.ICUCurrencyMetaInfo.Collector
        public void collect(String str, String str2, long j, long j2, int i, boolean z) {
            this.result.add(str2);
        }

        @Override // ohos.global.icu.impl.ICUCurrencyMetaInfo.Collector
        public List<String> getList() {
            return this.result.list();
        }
    }
}
