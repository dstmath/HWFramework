package android.view;

import android.graphics.Rect;

public abstract class ActionMode {
    public static final int DEFAULT_HIDE_DURATION = -1;
    public static final int TYPE_FLOATING = 1;
    public static final int TYPE_PRIMARY = 0;
    private Object mTag;
    private boolean mTitleOptionalHint;
    private int mType = 0;

    public interface Callback {
        boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem);

        boolean onCreateActionMode(ActionMode actionMode, Menu menu);

        void onDestroyActionMode(ActionMode actionMode);

        boolean onPrepareActionMode(ActionMode actionMode, Menu menu);
    }

    public static abstract class Callback2 implements Callback {
        public void onGetContentRect(ActionMode mode, View view, Rect outRect) {
            if (view != null) {
                outRect.set(0, 0, view.getWidth(), view.getHeight());
            } else {
                outRect.set(0, 0, 0, 0);
            }
        }
    }

    public abstract void finish();

    public abstract View getCustomView();

    public abstract Menu getMenu();

    public abstract MenuInflater getMenuInflater();

    public abstract CharSequence getSubtitle();

    public abstract CharSequence getTitle();

    public abstract void invalidate();

    public abstract void setCustomView(View view);

    public abstract void setSubtitle(int i);

    public abstract void setSubtitle(CharSequence charSequence);

    public abstract void setTitle(int i);

    public abstract void setTitle(CharSequence charSequence);

    public void setTag(Object tag) {
        this.mTag = tag;
    }

    public Object getTag() {
        return this.mTag;
    }

    public void setTitleOptionalHint(boolean titleOptional) {
        this.mTitleOptionalHint = titleOptional;
    }

    public boolean getTitleOptionalHint() {
        return this.mTitleOptionalHint;
    }

    public boolean isTitleOptional() {
        return false;
    }

    public void setType(int type) {
        this.mType = type;
    }

    public int getType() {
        return this.mType;
    }

    public void invalidateContentRect() {
    }

    public void hide(long duration) {
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
    }

    public boolean isUiFocusable() {
        return true;
    }
}
