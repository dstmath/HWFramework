package android.content;

import android.util.Log;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class AbsIntentFilter {
    private static final int CATEGORY_LEN = 3;
    private static final int STATE_LEN = 2;
    protected List<ActionFilterEntry> mActionFilter = null;
    protected String mIdentifier = "";

    public void addActionFilter(String action, String actionFilterName, String actionFilterValue) {
        addActionFilter(new ActionFilterEntry(action, actionFilterName, actionFilterValue));
    }

    /* access modifiers changed from: protected */
    public void addActionFilter(ActionFilterEntry actionFilter) {
        if (this.mActionFilter == null) {
            this.mActionFilter = new ArrayList(1);
        }
        this.mActionFilter.add(actionFilter);
    }

    public int countActionFilters() {
        List<ActionFilterEntry> list = this.mActionFilter;
        if (list != null) {
            return list.size();
        }
        return 0;
    }

    public Iterator<ActionFilterEntry> actionFilterIterator() {
        List<ActionFilterEntry> list = this.mActionFilter;
        if (list != null) {
            return list.iterator();
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void parseReg(String category) {
        if (category != null) {
            String[] items = category.split("@", 3);
            if (items.length >= 3) {
                char c = 0;
                String action = items[0];
                String expandFlag = items[1];
                String value = items[2];
                if (!"hwBrExpand".equals(expandFlag)) {
                    Log.w("IntentFilter", "state flag error");
                    return;
                }
                String[] filters = value.split("\\|");
                int length = filters.length;
                int i = 0;
                while (i < length) {
                    String[] states = filters[i].split("=");
                    if (states != null) {
                        if (states.length != 2) {
                            Log.w("IntentFilter", "value format error");
                            return;
                        }
                        addActionFilter(action, states[c], states[1]);
                    }
                    i++;
                    c = 0;
                }
            }
        }
    }

    public void setIdentifier(String id) {
        if (id != null) {
            this.mIdentifier = id;
        }
    }

    public String getIdentifier() {
        return this.mIdentifier;
    }
}
