package huawei.android.widget.plume.action.interaction;

import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import huawei.android.widget.HwOnSearchEventListener;
import huawei.android.widget.plume.action.interaction.UnifiedInteraction;

public class SearchInteraction extends UnifiedInteraction {
    private static final String TAG = SearchInteraction.class.getSimpleName();
    private HwOnSearchEventListener mOnSearchListener = null;
    private UnifiedInteraction.InteractEvent mSearchEvent = null;

    public SearchInteraction(Context context, View view) {
        super(context, view);
    }

    @Override // huawei.android.widget.plume.action.interaction.UnifiedInteraction
    public void handleEvent(String eventName, String value) {
        if (eventName == null || value == null) {
            Log.e(TAG, "Plume: eventName or value is null");
        } else if (!(this.mTarget instanceof EditText) && !(this.mTarget instanceof AbsListView)) {
            String str = TAG;
            Log.e(str, "Plume: " + this.mTarget.toString() + " can not be cast to AbsListView or EditText, when executing handleEvent method.");
        } else if (this.mSearchEvent != null) {
            Log.e(TAG, "Plume: onSearch event already exists.");
        } else if (this.mOnSearchListener != null) {
            Log.e(TAG, "Plume: onSearch listener already exists.");
        } else {
            this.mSearchEvent = getInteractEvent(eventName, value, UnifiedInteractionConstants.TAG_SEARCH, new Class[]{Integer.TYPE, KeyEvent.class});
            if (this.mSearchEvent == null) {
                Log.e(TAG, "Plume: event is null.");
            } else {
                setOnListener();
            }
        }
    }

    private void setOnListener() {
        this.mOnSearchListener = new HwOnSearchEventListener() {
            /* class huawei.android.widget.plume.action.interaction.SearchInteraction.AnonymousClass1 */

            public boolean onSearch(int action, KeyEvent event) {
                SearchInteraction searchInteraction = SearchInteraction.this;
                return searchInteraction.handleCallback(searchInteraction.mSearchEvent, new Object[]{Integer.valueOf(action), event});
            }
        };
        if (this.mTarget instanceof EditText) {
            ((EditText) this.mTarget).setOnSearchEventListener(this.mOnSearchListener);
        } else if (this.mTarget instanceof AbsListView) {
            ((AbsListView) this.mTarget).setOnSearchEventListener(this.mOnSearchListener);
        } else {
            Log.e(TAG, "Plume: can not be cast to AbsListView or EditText.");
        }
    }
}
