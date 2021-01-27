package huawei.android.widget.plume.action.interaction;

import android.content.Context;
import android.util.Log;
import android.view.View;
import huawei.android.widget.plume.action.PlumeAction;

public class ListenerAction extends PlumeAction {
    private static final String TAG = ListenerAction.class.getSimpleName();
    private EditInteraction mEditInteraction = null;
    private SearchInteraction mSearchInteraction = null;
    private ZoomInteraction mZoomInteraction = null;

    public ListenerAction(Context context, View view) {
        super(context, view);
    }

    @Override // huawei.android.widget.plume.action.PlumeAction
    public void apply(String attrName, String value) {
        if (attrName == null || value == null) {
            Log.e(TAG, "Plume: attrName or value is null");
            return;
        }
        char c = 65535;
        switch (attrName.hashCode()) {
            case -1746790426:
                if (attrName.equals(UnifiedInteractionConstants.EVENT_SELECT_ALL)) {
                    c = 5;
                    break;
                }
                break;
            case -1340214284:
                if (attrName.equals(UnifiedInteractionConstants.EVENT_PASTE)) {
                    c = 3;
                    break;
                }
                break;
            case -1013437964:
                if (attrName.equals(UnifiedInteractionConstants.EVENT_COPY)) {
                    c = 2;
                    break;
                }
                break;
            case -1012903069:
                if (attrName.equals(UnifiedInteractionConstants.EVENT_UNDO)) {
                    c = 6;
                    break;
                }
                break;
            case -1012752814:
                if (attrName.equals(UnifiedInteractionConstants.EVENT_ZOOM)) {
                    c = 0;
                    break;
                }
                break;
            case 105855971:
                if (attrName.equals(UnifiedInteractionConstants.EVENT_CUT)) {
                    c = 4;
                    break;
                }
                break;
            case 1062952042:
                if (attrName.equals(UnifiedInteractionConstants.EVENT_DELETE)) {
                    c = 7;
                    break;
                }
                break;
            case 1492073575:
                if (attrName.equals(UnifiedInteractionConstants.EVENT_SEARCH)) {
                    c = 1;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                handleZoomEvent(attrName, value);
                return;
            case 1:
                handleSearchEvent(attrName, value);
                return;
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
                handleEditEvent(attrName, value);
                return;
            default:
                Log.e(TAG, "Plume: fail to execute ListenerAction.");
                return;
        }
    }

    private void handleEditEvent(String attrName, String value) {
        if (this.mEditInteraction == null) {
            this.mEditInteraction = new EditInteraction(this.mContext, this.mTarget);
        }
        this.mEditInteraction.handleEvent(attrName, value);
    }

    private void handleSearchEvent(String attrName, String value) {
        if (this.mSearchInteraction == null) {
            this.mSearchInteraction = new SearchInteraction(this.mContext, this.mTarget);
        }
        this.mSearchInteraction.handleEvent(attrName, value);
    }

    private void handleZoomEvent(String attrName, String value) {
        if (this.mZoomInteraction == null) {
            this.mZoomInteraction = new ZoomInteraction(this.mContext, this.mTarget);
        }
        this.mZoomInteraction.handleEvent(attrName, value);
    }
}
