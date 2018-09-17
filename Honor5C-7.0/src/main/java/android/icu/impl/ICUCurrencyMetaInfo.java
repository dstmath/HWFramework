package android.icu.impl;

import android.icu.text.CurrencyMetaInfo;
import android.icu.text.CurrencyMetaInfo.CurrencyDigits;
import android.icu.text.CurrencyMetaInfo.CurrencyFilter;
import android.icu.text.CurrencyMetaInfo.CurrencyInfo;
import android.icu.util.Currency.CurrencyUsage;
import android.icu.util.UResourceBundle;
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
            return ICUCurrencyMetaInfo.Currency;
        }

        public List<String> getList() {
            return this.result.list();
        }
    }

    private static class InfoCollector implements Collector<CurrencyInfo> {
        private List<CurrencyInfo> result;

        private InfoCollector() {
            this.result = new ArrayList();
        }

        public void collect(String region, String currency, long from, long to, int priority, boolean tender) {
            this.result.add(new CurrencyInfo(region, currency, from, to, priority, tender));
        }

        public List<CurrencyInfo> getList() {
            return Collections.unmodifiableList(this.result);
        }

        public int collects() {
            return ICUCurrencyMetaInfo.Everything;
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
            return ICUCurrencyMetaInfo.Region;
        }

        public List<String> getList() {
            return this.result.list();
        }
    }

    private static class UniqueList<T> {
        private List<T> list;
        private Set<T> seen;

        private UniqueList() {
            this.seen = new HashSet();
            this.list = new ArrayList();
        }

        private static <T> UniqueList<T> create() {
            return new UniqueList();
        }

        void add(T value) {
            if (!this.seen.contains(value)) {
                this.list.add(value);
                this.seen.add(value);
            }
        }

        List<T> list() {
            return Collections.unmodifiableList(this.list);
        }
    }

    public ICUCurrencyMetaInfo() {
        ICUResourceBundle bundle = (ICUResourceBundle) UResourceBundle.getBundleInstance(ICUResourceBundle.ICU_CURR_BASE_NAME, "supplementalData", ICUResourceBundle.ICU_DATA_CLASS_LOADER);
        this.regionInfo = bundle.findTopLevel("CurrencyMap");
        this.digitInfo = bundle.findTopLevel("CurrencyMeta");
    }

    public List<CurrencyInfo> currencyInfo(CurrencyFilter filter) {
        return collect(new InfoCollector(), filter);
    }

    public List<String> currencies(CurrencyFilter filter) {
        return collect(new CurrencyCollector(), filter);
    }

    public List<String> regions(CurrencyFilter filter) {
        return collect(new RegionCollector(), filter);
    }

    public CurrencyDigits currencyDigits(String isoCode) {
        return currencyDigits(isoCode, CurrencyUsage.STANDARD);
    }

    public CurrencyDigits currencyDigits(String isoCode, CurrencyUsage currencyPurpose) {
        ICUResourceBundle b = this.digitInfo.findWithFallback(isoCode);
        if (b == null) {
            b = this.digitInfo.findWithFallback("DEFAULT");
        }
        int[] data = b.getIntVector();
        if (currencyPurpose == CurrencyUsage.CASH) {
            return new CurrencyDigits(data[Currency], data[3]);
        }
        if (currencyPurpose == CurrencyUsage.STANDARD) {
            return new CurrencyDigits(data[0], data[Region]);
        }
        return new CurrencyDigits(data[0], data[Region]);
    }

    private <T> List<T> collect(Collector<T> collector, CurrencyFilter filter) {
        if (filter == null) {
            filter = CurrencyFilter.all();
        }
        int needed = collector.collects();
        if (filter.region != null) {
            needed |= Region;
        }
        if (filter.currency != null) {
            needed |= Currency;
        }
        if (!(filter.from == Long.MIN_VALUE && filter.to == Long.MAX_VALUE)) {
            needed |= Date;
        }
        if (filter.tenderOnly) {
            needed |= Tender;
        }
        if (needed != 0) {
            if (filter.region != null) {
                ICUResourceBundle b = this.regionInfo.findWithFallback(filter.region);
                if (b != null) {
                    collectRegion(collector, filter, needed, b);
                }
            } else {
                for (int i = 0; i < this.regionInfo.getSize(); i += Region) {
                    collectRegion(collector, filter, needed, this.regionInfo.at(i));
                }
            }
        }
        return collector.getList();
    }

    private <T> void collectRegion(Collector<T> collector, CurrencyFilter filter, int needed, ICUResourceBundle b) {
        String region = b.getKey();
        if (needed == Region) {
            collector.collect(b.getKey(), null, 0, 0, -1, false);
            return;
        }
        for (int i = 0; i < b.getSize(); i += Region) {
            ICUResourceBundle r = b.at(i);
            if (r.getSize() != 0) {
                String str = null;
                long from = Long.MIN_VALUE;
                long to = Long.MAX_VALUE;
                boolean z = true;
                if ((needed & Currency) != 0) {
                    str = r.at("id").getString();
                    if (!(filter.currency == null || filter.currency.equals(str))) {
                    }
                }
                if ((needed & Date) != 0) {
                    from = getDate(r.at("from"), Long.MIN_VALUE, false);
                    to = getDate(r.at("to"), Long.MAX_VALUE, true);
                    if (filter.from <= to) {
                        if (filter.to < from) {
                        }
                    }
                }
                if ((needed & Tender) != 0) {
                    ICUResourceBundle tenderBundle = r.at("tender");
                    z = tenderBundle != null ? "true".equals(tenderBundle.getString()) : true;
                    if (filter.tenderOnly && !z) {
                    }
                }
                collector.collect(region, str, from, to, i, z);
            }
        }
    }

    private long getDate(ICUResourceBundle b, long defaultValue, boolean endOfDay) {
        if (b == null) {
            return defaultValue;
        }
        int[] values = b.getIntVector();
        return (((long) values[0]) << 32) | (((long) values[Region]) & MASK);
    }
}
