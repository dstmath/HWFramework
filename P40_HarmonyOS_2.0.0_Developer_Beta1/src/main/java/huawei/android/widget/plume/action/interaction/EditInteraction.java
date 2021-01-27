package huawei.android.widget.plume.action.interaction;

import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AbsListView;
import huawei.android.widget.HwOnEditEventListener;
import huawei.android.widget.plume.action.interaction.UnifiedInteraction;

public class EditInteraction extends UnifiedInteraction {
    private static final String TAG = EditInteraction.class.getSimpleName();
    private UnifiedInteraction.InteractEvent mCopyEvent = null;
    private UnifiedInteraction.InteractEvent mCutEvent = null;
    private UnifiedInteraction.InteractEvent mDeleteEvent = null;
    private HwOnEditEventListener mOnEditListener = null;
    private UnifiedInteraction.InteractEvent mPasteEvent = null;
    private UnifiedInteraction.InteractEvent mSelectAllEvent = null;
    private UnifiedInteraction.InteractEvent mUndoEvent = null;

    public EditInteraction(Context context, View view) {
        super(context, view);
    }

    @Override // huawei.android.widget.plume.action.interaction.UnifiedInteraction
    public void handleEvent(String eventName, String value) {
        if (eventName == null || value == null) {
            Log.e(TAG, "Plume: eventName or value is null");
        } else if (!(this.mTarget instanceof AbsListView)) {
            Log.e(TAG, "Plume: " + this.mTarget.toString() + " can not be cast to AbsListView, when executing handleEvent method.");
        } else {
            char c = 65535;
            switch (eventName.hashCode()) {
                case -1746790426:
                    if (eventName.equals(UnifiedInteractionConstants.EVENT_SELECT_ALL)) {
                        c = 3;
                        break;
                    }
                    break;
                case -1340214284:
                    if (eventName.equals(UnifiedInteractionConstants.EVENT_PASTE)) {
                        c = 1;
                        break;
                    }
                    break;
                case -1013437964:
                    if (eventName.equals(UnifiedInteractionConstants.EVENT_COPY)) {
                        c = 0;
                        break;
                    }
                    break;
                case -1012903069:
                    if (eventName.equals(UnifiedInteractionConstants.EVENT_UNDO)) {
                        c = 4;
                        break;
                    }
                    break;
                case 105855971:
                    if (eventName.equals(UnifiedInteractionConstants.EVENT_CUT)) {
                        c = 2;
                        break;
                    }
                    break;
                case 1062952042:
                    if (eventName.equals(UnifiedInteractionConstants.EVENT_DELETE)) {
                        c = 5;
                        break;
                    }
                    break;
            }
            if (c == 0) {
                handleCopyEvent(eventName, value, UnifiedInteractionConstants.TAG_COPY);
            } else if (c == 1) {
                handlePasteEvent(eventName, value, UnifiedInteractionConstants.TAG_PASTE);
            } else if (c == 2) {
                handleCutEvent(eventName, value, UnifiedInteractionConstants.TAG_CUT);
            } else if (c == 3) {
                handleSelectAllEvent(eventName, value, UnifiedInteractionConstants.TAG_SELECT_ALL);
            } else if (c == 4) {
                handleUndoEvent(eventName, value, UnifiedInteractionConstants.TAG_UNDO);
            } else if (c != 5) {
                Log.e(TAG, "Plume: fail to execute handleEvent.");
            } else {
                handleDeleteEvent(eventName, value, UnifiedInteractionConstants.TAG_DELETE);
            }
        }
    }

    private void handlePasteEvent(String eventName, String value, String tagName) {
        if (this.mPasteEvent != null) {
            Log.e(TAG, "Plume: onPaste already exists.");
            return;
        }
        this.mPasteEvent = getInteractEvent(eventName, value, tagName, new Class[]{Integer.TYPE, KeyEvent.class});
        if (this.mPasteEvent == null) {
            Log.e(TAG, "Plume: mPasteEvent is null.");
        } else if (this.mOnEditListener == null) {
            setOnListener();
        }
    }

    private void handleCopyEvent(String eventName, String value, String tagName) {
        if (this.mCopyEvent != null) {
            Log.e(TAG, "Plume: onCopy already exists.");
            return;
        }
        this.mCopyEvent = getInteractEvent(eventName, value, tagName, new Class[]{Integer.TYPE, KeyEvent.class});
        if (this.mCopyEvent == null) {
            Log.e(TAG, "Plume: mCopyEvent is null.");
        } else if (this.mOnEditListener == null) {
            setOnListener();
        }
    }

    private void handleCutEvent(String eventName, String value, String tagName) {
        if (this.mCutEvent != null) {
            Log.e(TAG, "Plume: onCut event already exists.");
            return;
        }
        this.mCutEvent = getInteractEvent(eventName, value, tagName, new Class[]{Integer.TYPE, KeyEvent.class});
        if (this.mCutEvent == null) {
            Log.e(TAG, "Plume: mCutEvent is null.");
        } else if (this.mOnEditListener == null) {
            setOnListener();
        }
    }

    private void handleSelectAllEvent(String eventName, String value, String tagName) {
        if (this.mSelectAllEvent != null) {
            Log.e(TAG, "Plume: onSelectAll event already exists.");
            return;
        }
        this.mSelectAllEvent = getInteractEvent(eventName, value, tagName, new Class[]{Integer.TYPE, KeyEvent.class});
        if (this.mSelectAllEvent == null) {
            Log.e(TAG, "Plume: mSelectAllEvent is null.");
        } else if (this.mOnEditListener == null) {
            setOnListener();
        }
    }

    private void handleUndoEvent(String eventName, String value, String tagName) {
        if (this.mUndoEvent != null) {
            Log.e(TAG, "Plume: onUndo event already exists.");
            return;
        }
        this.mUndoEvent = getInteractEvent(eventName, value, tagName, new Class[]{Integer.TYPE, KeyEvent.class});
        if (this.mUndoEvent == null) {
            Log.e(TAG, "Plume: event is null.");
        } else if (this.mOnEditListener == null) {
            setOnListener();
        }
    }

    private void handleDeleteEvent(String eventName, String value, String tagName) {
        if (this.mDeleteEvent != null) {
            Log.e(TAG, "Plume: onDelete event already exists.");
            return;
        }
        this.mDeleteEvent = getInteractEvent(eventName, value, tagName, new Class[]{Integer.TYPE, KeyEvent.class});
        if (this.mDeleteEvent == null) {
            Log.e(TAG, "Plume: event is null.");
        } else if (this.mOnEditListener == null) {
            setOnListener();
        }
    }

    private void setOnListener() {
        this.mOnEditListener = new HwOnEditEventListener() {
            /* class huawei.android.widget.plume.action.interaction.EditInteraction.AnonymousClass1 */

            public boolean onCopy(int action, KeyEvent event) {
                EditInteraction editInteraction = EditInteraction.this;
                return editInteraction.handleCallback(editInteraction.mCopyEvent, new Object[]{Integer.valueOf(action), event});
            }

            public boolean onPaste(int action, KeyEvent event) {
                EditInteraction editInteraction = EditInteraction.this;
                return editInteraction.handleCallback(editInteraction.mPasteEvent, new Object[]{Integer.valueOf(action), event});
            }

            public boolean onCut(int action, KeyEvent event) {
                EditInteraction editInteraction = EditInteraction.this;
                return editInteraction.handleCallback(editInteraction.mCutEvent, new Object[]{Integer.valueOf(action), event});
            }

            public boolean onSelectAll(int action, KeyEvent event) {
                EditInteraction editInteraction = EditInteraction.this;
                return editInteraction.handleCallback(editInteraction.mSelectAllEvent, new Object[]{Integer.valueOf(action), event});
            }

            public boolean onUndo(int action, KeyEvent event) {
                EditInteraction editInteraction = EditInteraction.this;
                return editInteraction.handleCallback(editInteraction.mUndoEvent, new Object[]{Integer.valueOf(action), event});
            }

            public boolean onDelete(int action, KeyEvent event) {
                EditInteraction editInteraction = EditInteraction.this;
                return editInteraction.handleCallback(editInteraction.mDeleteEvent, new Object[]{Integer.valueOf(action), event});
            }
        };
        if (this.mTarget instanceof AbsListView) {
            ((AbsListView) this.mTarget).setOnEditEventListener(this.mOnEditListener);
        }
    }
}
