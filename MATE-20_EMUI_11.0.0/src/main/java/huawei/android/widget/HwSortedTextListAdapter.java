package huawei.android.widget;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import huawei.android.hwutil.SectionLocaleUtils;
import java.text.CollationKey;
import java.text.Collator;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HwSortedTextListAdapter extends BaseAdapter {
    private static final String EMPTY_SECTION = "";
    private static final int HASH_MAP_LENGTH = 16;
    private static final int SORTING_NUM = -1;
    private static final String TAG = "HwSortedTextListAdapter";
    private Context mContext;
    private List<? extends Map<String, ?>> mData;
    private MySectionIndexer mIndexer;
    private LayoutInflater mInflater;
    private boolean mIsDigitLast;
    private int mItemResource;
    private final Object mLock;
    private Map<String, String> mSectionMap;
    private int mSortKeyId;
    private Map<String, CollationKey> mSortKeyMap;
    private String mSortKeyName;

    public HwSortedTextListAdapter(Context context, int itemLayoutResource, List<? extends Map<String, ?>> objects, String sortKeyName) {
        this(context, itemLayoutResource, 0, 0, objects, sortKeyName, false);
    }

    public HwSortedTextListAdapter(Context context, int itemLayoutResource, int textViewResourceId, List<? extends Map<String, ?>> objects, String sortKeyName, boolean isDigitLast) {
        this(context, itemLayoutResource, textViewResourceId, 0, objects, sortKeyName, isDigitLast);
    }

    public HwSortedTextListAdapter(Context context, int itemLayoutResource, int textViewResourceId, int groupLabelId, List<? extends Map<String, ?>> objects, String sortKeyName) {
        this(context, itemLayoutResource, textViewResourceId, groupLabelId, objects, sortKeyName, false);
    }

    public HwSortedTextListAdapter(Context context, int itemLayoutResource, int textViewResourceId, int groupLabelId, List<? extends Map<String, ?>> objects, String sortKeyName, boolean isDigitLast) {
        this.mSortKeyId = 0;
        this.mLock = new Object();
        this.mIsDigitLast = false;
        this.mContext = context;
        this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
        this.mItemResource = itemLayoutResource;
        this.mSortKeyId = textViewResourceId;
        this.mIsDigitLast = isDigitLast;
        this.mData = objects;
        this.mSortKeyName = sortKeyName;
        buildSectionIndexer();
    }

    @Override // android.widget.Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, this.mItemResource);
    }

    private View createViewFromResource(int position, View convertView, ViewGroup parent, int resource) {
        View view = convertView;
        if (view == null) {
            view = this.mInflater.inflate(resource, parent, false);
        }
        TextView text = null;
        try {
            if (this.mSortKeyId != 0) {
                text = (TextView) view.findViewById(this.mSortKeyId);
            } else if (view instanceof TextView) {
                text = (TextView) view;
            }
            Object item = getItem(position);
            if (text != null) {
                if (item instanceof CharSequence) {
                    text.setText((CharSequence) item);
                } else {
                    text.setText(String.valueOf(item));
                }
            }
            return view;
        } catch (ClassCastException e) {
            Log.e(TAG, "You must supply a resource ID for a TextView");
            throw new IllegalStateException("HwSortedTextListAdapter requires the resource ID to be a TextView", e);
        }
    }

    public void sort(final Comparator<Object> comparator) {
        synchronized (this.mLock) {
            Collections.sort(this.mData, new Comparator<Map<String, ?>>() {
                /* class huawei.android.widget.HwSortedTextListAdapter.AnonymousClass1 */

                public int compare(Map<String, ?> map1, Map<String, ?> map2) {
                    int result;
                    String sortKey1 = map1.get(HwSortedTextListAdapter.this.mSortKeyName).toString();
                    String sortKey2 = map2.get(HwSortedTextListAdapter.this.mSortKeyName).toString();
                    String section1 = (String) HwSortedTextListAdapter.this.mSectionMap.get(sortKey1);
                    String section2 = (String) HwSortedTextListAdapter.this.mSectionMap.get(sortKey2);
                    if (HwSortedTextListAdapter.EMPTY_SECTION.equals(section1) && HwSortedTextListAdapter.EMPTY_SECTION.equals(section2)) {
                        return comparator.compare(sortKey1, sortKey2);
                    }
                    if (HwSortedTextListAdapter.EMPTY_SECTION.equals(section1) && !HwSortedTextListAdapter.EMPTY_SECTION.equals(section2)) {
                        return 1;
                    }
                    if (HwSortedTextListAdapter.EMPTY_SECTION.equals(section2) && !HwSortedTextListAdapter.EMPTY_SECTION.equals(section1)) {
                        return -1;
                    }
                    if (HwSortedTextListAdapter.this.mIsDigitLast && !section1.equals(section2)) {
                        if (AlphaIndexerListView.DIGIT_LABEL.equals(section1)) {
                            return 1;
                        }
                        if (AlphaIndexerListView.DIGIT_LABEL.equals(section2)) {
                            return -1;
                        }
                    }
                    if ("zh".equals(Locale.getDefault().getLanguage()) && "Hant".equals(Locale.getDefault().getScript()) && (result = HwSortedTextListAdapter.this.getStrokeSort(section1, section2)) != 0) {
                        return result;
                    }
                    int labelResult = comparator.compare(section1, section2);
                    return labelResult == 0 ? comparator.compare(sortKey1, sortKey2) : labelResult;
                }
            });
        }
        notifyDataSetChanged();
    }

    private void buildSectionIndexer() {
        SectionLocaleUtils util = SectionLocaleUtils.getInstance();
        Map<String, Integer> sectionMapCounts = new LinkedHashMap<>(16);
        this.mSectionMap = new HashMap(16);
        this.mSortKeyMap = new HashMap(16);
        Locale locale = Locale.getDefault();
        if ("zh-Hant-TW".equals(locale.toLanguageTag())) {
            locale = Locale.forLanguageTag(locale.toLanguageTag() + "-u-co-zhuyin");
        }
        Collator comparator = Collator.getInstance(locale);
        int size = this.mData.size();
        for (int i = 0; i < size; i++) {
            String title = ((Map) this.mData.get(i)).get(this.mSortKeyName).toString();
            this.mSortKeyMap.put(title, comparator.getCollationKey(title));
            String section = TextUtils.isEmpty(title) ? EMPTY_SECTION : util.getLabel(title);
            this.mSectionMap.put(title, section);
            if (sectionMapCounts.containsKey(section)) {
                sectionMapCounts.put(section, Integer.valueOf(sectionMapCounts.get(section).intValue() + 1));
            } else {
                sectionMapCounts.put(section, 1);
            }
        }
        String[] sections = (String[]) sectionMapCounts.keySet().toArray(new String[0]);
        int len = sections.length;
        arraySort(sections, comparator);
        int[] counts = new int[len];
        for (int i2 = 0; i2 < len; i2++) {
            counts[i2] = sectionMapCounts.get(sections[i2]).intValue();
        }
        this.mIndexer = new MySectionIndexer(sections, counts);
        sort(comparator);
    }

    private void arraySort(String[] sections, final Collator comparator) {
        Arrays.sort(sections, new Comparator<String>() {
            /* class huawei.android.widget.HwSortedTextListAdapter.AnonymousClass2 */

            public int compare(String str1, String str2) {
                int result;
                if (HwSortedTextListAdapter.EMPTY_SECTION.equals(str1)) {
                    return 1;
                }
                if (HwSortedTextListAdapter.EMPTY_SECTION.equals(str2)) {
                    return -1;
                }
                if (HwSortedTextListAdapter.this.mIsDigitLast) {
                    if (AlphaIndexerListView.DIGIT_LABEL.equals(str1)) {
                        return 1;
                    }
                    if (AlphaIndexerListView.DIGIT_LABEL.equals(str2)) {
                        return -1;
                    }
                }
                if (!"zh".equals(Locale.getDefault().getLanguage()) || !"Hant".equals(Locale.getDefault().getScript()) || (result = HwSortedTextListAdapter.this.getStrokeSort(str1, str2)) == 0) {
                    return comparator.compare(str1, str2);
                }
                return result;
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getStrokeSort(String sortBefore, String sortAfter) {
        if (sortBefore == null || sortAfter == null) {
            return 0;
        }
        int beforeLength = sortBefore.length();
        int afterLength = sortAfter.length();
        if (beforeLength > afterLength) {
            return 1;
        }
        if (beforeLength < afterLength) {
            return -1;
        }
        return 0;
    }

    @Override // android.widget.BaseAdapter
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    public Context getContext() {
        return this.mContext;
    }

    public LayoutInflater getInflater() {
        return this.mInflater;
    }

    @Override // android.widget.Adapter
    public int getCount() {
        return this.mData.size();
    }

    @Override // android.widget.Adapter
    public Object getItem(int position) {
        if (this.mData != null && position >= 0 && position < getCount()) {
            return ((Map) this.mData.get(position)).get(this.mSortKeyName);
        }
        return null;
    }

    public int getPosition(Map<String, ?> item) {
        return this.mData.indexOf(item);
    }

    @Override // android.widget.Adapter
    public long getItemId(int position) {
        return (long) position;
    }

    public void setViewImage(ImageView view, String value) {
        try {
            view.setImageResource(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            view.setImageURI(Uri.parse(value));
        }
    }

    public void setViewImage(ImageView view, int value) {
        view.setImageResource(value);
    }

    public void setViewText(TextView view, String text) {
        view.setText(text);
    }

    public void setSortKeyName(String name) {
        this.mSortKeyName = name;
    }

    public String getSortKeyName() {
        return this.mSortKeyName;
    }

    public boolean isDigitLast() {
        return this.mIsDigitLast;
    }

    public Object[] getSections() {
        return this.mIndexer.getSections();
    }

    public int getPositionForSection(int section) {
        return this.mIndexer.getPositionForSection(section);
    }

    public int getSectionForPosition(int position) {
        return this.mIndexer.getSectionForPosition(position);
    }

    public Object getSectionNameForPosition(int position) {
        return this.mIndexer.getSections()[getSectionForPosition(position)];
    }
}
