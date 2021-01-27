package ohos.com.sun.org.apache.xalan.internal.xsltc.dom;

import java.text.Collator;
import java.util.Locale;
import ohos.com.sun.org.apache.xalan.internal.utils.ObjectFactory;
import ohos.com.sun.org.apache.xalan.internal.utils.SecuritySupport;
import ohos.com.sun.org.apache.xalan.internal.xsltc.CollatorFactory;
import ohos.com.sun.org.apache.xalan.internal.xsltc.DOM;
import ohos.com.sun.org.apache.xalan.internal.xsltc.TransletException;
import ohos.com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import ohos.com.sun.org.apache.xml.internal.utils.StringComparable;

public abstract class NodeSortRecord {
    public static final int COMPARE_ASCENDING = 0;
    public static final int COMPARE_DESCENDING = 1;
    public static final int COMPARE_NUMERIC = 1;
    public static final int COMPARE_STRING = 0;
    private static final Collator DEFAULT_COLLATOR = Collator.getInstance();
    protected Collator _collator;
    protected CollatorFactory _collatorFactory;
    protected Collator[] _collators;
    private DOM _dom;
    private int _last;
    protected Locale _locale;
    private int _node;
    private int _scanned;
    protected SortSettings _settings;
    private Object[] _values;

    public abstract String extractValueFromDOM(DOM dom, int i, int i2, AbstractTranslet abstractTranslet, int i3);

    public NodeSortRecord(int i) {
        this._collator = DEFAULT_COLLATOR;
        this._dom = null;
        this._last = 0;
        this._scanned = 0;
        this._node = i;
    }

    public NodeSortRecord() {
        this(0);
    }

    /* JADX WARN: Type inference failed for: r4v6, types: [java.lang.Throwable, ohos.com.sun.org.apache.xalan.internal.xsltc.TransletException] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public final void initialize(int i, int i2, DOM dom, SortSettings sortSettings) throws TransletException {
        String str;
        this._dom = dom;
        this._node = i;
        this._last = i2;
        this._settings = sortSettings;
        int length = sortSettings.getSortOrders().length;
        this._values = new Object[length];
        try {
            str = SecuritySupport.getSystemProperty("com.sun.org.apache.xalan.internal.xsltc.COLLATOR_FACTORY");
        } catch (SecurityException unused) {
            str = null;
        }
        if (str != null) {
            try {
                this._collatorFactory = (CollatorFactory) ObjectFactory.findProviderClass(str, true);
                Locale[] locales = sortSettings.getLocales();
                this._collators = new Collator[length];
                for (int i3 = 0; i3 < length; i3++) {
                    this._collators[i3] = this._collatorFactory.getCollator(locales[i3]);
                }
                this._collator = this._collators[0];
            } catch (ClassNotFoundException e) {
                throw new TransletException(e);
            }
        } else {
            this._collators = sortSettings.getCollators();
            this._collator = this._collators[0];
        }
    }

    public final int getNode() {
        return this._node;
    }

    public final int compareDocOrder(NodeSortRecord nodeSortRecord) {
        return this._node - nodeSortRecord._node;
    }

    private final Comparable stringValue(int i) {
        if (this._scanned > i) {
            return (Comparable) this._values[i];
        }
        Comparable comparator = StringComparable.getComparator(extractValueFromDOM(this._dom, this._node, i, this._settings.getTranslet(), this._last), this._settings.getLocales()[i], this._collators[i], this._settings.getCaseOrders()[i]);
        Object[] objArr = this._values;
        int i2 = this._scanned;
        this._scanned = i2 + 1;
        objArr[i2] = comparator;
        return comparator;
    }

    private final Double numericValue(int i) {
        Double d;
        if (this._scanned > i) {
            return (Double) this._values[i];
        }
        try {
            d = new Double(extractValueFromDOM(this._dom, this._node, i, this._settings.getTranslet(), this._last));
        } catch (NumberFormatException unused) {
            d = new Double(Double.NEGATIVE_INFINITY);
        }
        Object[] objArr = this._values;
        int i2 = this._scanned;
        this._scanned = i2 + 1;
        objArr[i2] = d;
        return d;
    }

    public int compareTo(NodeSortRecord nodeSortRecord) {
        int i;
        int[] sortOrders = this._settings.getSortOrders();
        int length = this._settings.getSortOrders().length;
        int[] types = this._settings.getTypes();
        for (int i2 = 0; i2 < length; i2++) {
            if (types[i2] == 1) {
                i = numericValue(i2).compareTo(nodeSortRecord.numericValue(i2));
            } else {
                i = stringValue(i2).compareTo(nodeSortRecord.stringValue(i2));
            }
            if (i != 0) {
                return sortOrders[i2] == 1 ? 0 - i : i;
            }
        }
        return this._node - nodeSortRecord._node;
    }

    public Collator[] getCollator() {
        return this._collators;
    }
}
