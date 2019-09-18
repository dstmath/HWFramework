package android.icu.impl;

import android.icu.text.CurrencyMetaInfo;
import android.icu.util.Currency;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ICUCurrencyMetaInfo extends CurrencyMetaInfo {
    private static final int Currency = 2;
    private static final int Date = 4;
    private static final int Everything = Integer.MAX_VALUE;
    private static final long MASK = 4294967295L;
    private static final int Region = 1;
    private static final int Tender = 8;
    private ICUResourceBundle digitInfo;
    private ICUResourceBundle regionInfo;

    private interface Collector<T> {
        void collect(String str, String str2, long j, long j2, int i, boolean z);

        int collects();

        List<T> getList();
    }

    private static class CurrencyCollector implements Collector<String> {
        private final UniqueList<String> result;

        private CurrencyCollector() {
            this.result = UniqueList.create();
        }

        public void collect(String region, String currency, long from, long to, int priority, boolean tender) {
            this.result.add(currency);
        }

        public int collects() {
            return 2;
        }

        public List<String> getList() {
            return this.result.list();
        }
    }

    private static class InfoCollector implements Collector<CurrencyMetaInfo.CurrencyInfo> {
        private List<CurrencyMetaInfo.CurrencyInfo> result;

        private InfoCollector() {
            this.result = new ArrayList();
        }

        public void collect(String region, String currency, long from, long to, int priority, boolean tender) {
            List<CurrencyMetaInfo.CurrencyInfo> list = this.result;
            CurrencyMetaInfo.CurrencyInfo currencyInfo = new CurrencyMetaInfo.CurrencyInfo(region, currency, from, to, priority, tender);
            list.add(currencyInfo);
        }

        public List<CurrencyMetaInfo.CurrencyInfo> getList() {
            return Collections.unmodifiableList(this.result);
        }

        public int collects() {
            return Integer.MAX_VALUE;
        }
    }

    private static class RegionCollector implements Collector<String> {
        private final UniqueList<String> result;

        private RegionCollector() {
            this.result = UniqueList.create();
        }

        public void collect(String region, String currency, long from, long to, int priority, boolean tender) {
            this.result.add(region);
        }

        public int collects() {
            return 1;
        }

        public List<String> getList() {
            return this.result.list();
        }
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
        public void add(T value) {
            if (!this.seen.contains(value)) {
                this.list.add(value);
                this.seen.add(value);
            }
        }

        /* access modifiers changed from: package-private */
        public List<T> list() {
            return Collections.unmodifiableList(this.list);
        }
    }

    public ICUCurrencyMetaInfo() {
        ICUResourceBundle bundle = (ICUResourceBundle) ICUResourceBundle.getBundleInstance(ICUData.ICU_CURR_BASE_NAME, "supplementalData", ICUResourceBundle.ICU_DATA_CLASS_LOADER);
        this.regionInfo = bundle.findTopLevel("CurrencyMap");
        this.digitInfo = bundle.findTopLevel("CurrencyMeta");
    }

    public List<CurrencyMetaInfo.CurrencyInfo> currencyInfo(CurrencyMetaInfo.CurrencyFilter filter) {
        return collect(new InfoCollector(), filter);
    }

    public List<String> currencies(CurrencyMetaInfo.CurrencyFilter filter) {
        return collect(new CurrencyCollector(), filter);
    }

    public List<String> regions(CurrencyMetaInfo.CurrencyFilter filter) {
        return collect(new RegionCollector(), filter);
    }

    public CurrencyMetaInfo.CurrencyDigits currencyDigits(String isoCode) {
        return currencyDigits(isoCode, Currency.CurrencyUsage.STANDARD);
    }

    public CurrencyMetaInfo.CurrencyDigits currencyDigits(String isoCode, Currency.CurrencyUsage currencyPurpose) {
        ICUResourceBundle b = this.digitInfo.findWithFallback(isoCode);
        if (b == null) {
            b = this.digitInfo.findWithFallback("DEFAULT");
        }
        int[] data = b.getIntVector();
        if (currencyPurpose == Currency.CurrencyUsage.CASH) {
            return new CurrencyMetaInfo.CurrencyDigits(data[2], data[3]);
        }
        if (currencyPurpose == Currency.CurrencyUsage.STANDARD) {
            return new CurrencyMetaInfo.CurrencyDigits(data[0], data[1]);
        }
        return new CurrencyMetaInfo.CurrencyDigits(data[0], data[1]);
    }

    private <T> List<T> collect(Collector<T> collector, CurrencyMetaInfo.CurrencyFilter filter) {
        if (filter == null) {
            filter = CurrencyMetaInfo.CurrencyFilter.all();
        }
        int needed = collector.collects();
        if (filter.region != null) {
            needed |= 1;
        }
        if (filter.currency != null) {
            needed |= 2;
        }
        if (!(filter.from == Long.MIN_VALUE && filter.to == Long.MAX_VALUE)) {
            needed |= 4;
        }
        if (filter.tenderOnly) {
            needed |= 8;
        }
        if (needed != 0) {
            if (filter.region != null) {
                ICUResourceBundle b = this.regionInfo.findWithFallback(filter.region);
                if (b != null) {
                    collectRegion(collector, filter, needed, b);
                }
            } else {
                for (int i = 0; i < this.regionInfo.getSize(); i++) {
                    collectRegion(collector, filter, needed, this.regionInfo.at(i));
                }
            }
        }
        return collector.getList();
    }

    /* JADX WARNING: Removed duplicated region for block: B:28:0x00a8  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00ca  */
    private <T> void collectRegion(Collector<T> collector, CurrencyMetaInfo.CurrencyFilter filter, int needed, ICUResourceBundle b) {
        boolean z;
        int i;
        long from;
        long to;
        boolean z2;
        boolean tender;
        CurrencyMetaInfo.CurrencyFilter currencyFilter = filter;
        int i2 = needed;
        String region = b.getKey();
        boolean z3 = true;
        if (i2 == 1) {
            collector.collect(b.getKey(), null, 0, 0, -1, false);
            return;
        }
        boolean z4 = false;
        int i3 = 0;
        while (true) {
            int i4 = i3;
            if (i4 < b.getSize()) {
                ICUResourceBundle r = b.at(i4);
                if (r.getSize() == 0) {
                    z = z3;
                    i = i4;
                } else {
                    String currency = null;
                    if ((i2 & 2) != 0) {
                        currency = r.at("id").getString();
                        if (currencyFilter.currency != null && !currencyFilter.currency.equals(currency)) {
                            i = i4;
                            z = true;
                        }
                    }
                    String currency2 = currency;
                    if ((i2 & 4) != 0) {
                        long from2 = getDate(r.at("from"), Long.MIN_VALUE, z4);
                        i = i4;
                        z2 = true;
                        long to2 = getDate(r.at("to"), Long.MAX_VALUE, true);
                        if (currencyFilter.from <= to2 && currencyFilter.to >= from2) {
                            from = from2;
                            to = to2;
                            if ((i2 & 8) == 0) {
                                ICUResourceBundle tenderBundle = r.at("tender");
                                boolean tender2 = (tenderBundle == null || "true".equals(tenderBundle.getString())) ? z2 : false;
                                if (!currencyFilter.tenderOnly || tender2) {
                                    tender = tender2;
                                }
                            } else {
                                tender = true;
                            }
                            z = z2;
                            ICUResourceBundle iCUResourceBundle = r;
                            collector.collect(region, currency2, from, to, i, tender);
                        }
                    } else {
                        from = Long.MIN_VALUE;
                        i = i4;
                        z2 = true;
                        to = Long.MAX_VALUE;
                        if ((i2 & 8) == 0) {
                        }
                        z = z2;
                        ICUResourceBundle iCUResourceBundle2 = r;
                        collector.collect(region, currency2, from, to, i, tender);
                    }
                    z = z2;
                }
                i3 = i + 1;
                z3 = z;
                z4 = false;
            } else {
                return;
            }
        }
    }

    private long getDate(ICUResourceBundle b, long defaultValue, boolean endOfDay) {
        if (b == null) {
            return defaultValue;
        }
        int[] values = b.getIntVector();
        return (((long) values[0]) << 32) | (((long) values[1]) & MASK);
    }
}
