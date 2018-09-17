package android.preference;

import android.R;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.hwcontrol.HwWidgetFactory;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.preference.Preference.BaseSavedState;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public final class PreferenceScreen extends PreferenceGroup implements OnItemClickListener, OnDismissListener {
    private Dialog mDialog;
    private Drawable mDividerDrawable;
    private boolean mDividerSpecified;
    private int mLayoutResId = 17367226;
    private ListView mListView;
    private ListAdapter mRootAdapter;

    private static class HwDialog extends Dialog {
        public HwDialog(Context context, int theme) {
            super(context, theme);
        }

        public boolean onMenuItemSelected(int featureId, MenuItem item) {
            if (item.getItemId() != R.id.home) {
                return super.onMenuItemSelected(featureId, item);
            }
            dismiss();
            return true;
        }

        public void show() {
            super.show();
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        Bundle dialogBundle;
        boolean isDialogShowing;

        public SavedState(Parcel source) {
            boolean z = true;
            super(source);
            if (source.readInt() != 1) {
                z = false;
            }
            this.isDialogShowing = z;
            this.dialogBundle = source.readBundle();
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.isDialogShowing ? 1 : 0);
            dest.writeBundle(this.dialogBundle);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }
    }

    public PreferenceScreen(Context context, AttributeSet attrs) {
        super(context, attrs, R.attr.preferenceScreenStyle);
        TypedArray a = context.obtainStyledAttributes(null, com.android.internal.R.styleable.PreferenceScreen, R.attr.preferenceScreenStyle, 0);
        this.mLayoutResId = a.getResourceId(1, this.mLayoutResId);
        if (a.hasValueOrEmpty(0)) {
            this.mDividerDrawable = a.getDrawable(0);
            this.mDividerSpecified = true;
        }
        a.recycle();
    }

    public ListAdapter getRootAdapter() {
        if (this.mRootAdapter == null) {
            this.mRootAdapter = onCreateRootAdapter();
        }
        return this.mRootAdapter;
    }

    protected ListAdapter onCreateRootAdapter() {
        return new PreferenceGroupAdapter(this);
    }

    public void bind(ListView listView) {
        listView.setOnItemClickListener(this);
        listView.setAdapter(getRootAdapter());
        onAttachedToActivity();
    }

    protected void onClick() {
        if (getIntent() == null && getFragment() == null && getPreferenceCount() != 0) {
            showDialog(null);
        }
    }

    private void showDialog(Bundle state) {
        Dialog dialog;
        Context context = getContext();
        if (this.mListView != null) {
            this.mListView.setAdapter(null);
        }
        View childPrefScreen = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(this.mLayoutResId, null);
        View titleView = childPrefScreen.findViewById(R.id.title);
        this.mListView = (ListView) childPrefScreen.findViewById(R.id.list);
        if (this.mDividerSpecified) {
            this.mListView.setDivider(this.mDividerDrawable);
        }
        bind(this.mListView);
        CharSequence title = getTitle();
        if (HwWidgetFactory.isHwTheme(context)) {
            dialog = new HwDialog(context, context.getThemeResId());
        } else {
            dialog = new Dialog(context, context.getThemeResId());
        }
        this.mDialog = dialog;
        if (TextUtils.isEmpty(title)) {
            if (titleView != null) {
                titleView.setVisibility(8);
            }
            dialog.getWindow().requestFeature(1);
        } else if (titleView instanceof TextView) {
            ((TextView) titleView).setText(title);
            titleView.setVisibility(0);
        } else {
            dialog.setTitle(title);
        }
        dialog.setContentView(childPrefScreen);
        dialog.setOnDismissListener(this);
        if (state != null) {
            dialog.onRestoreInstanceState(state);
        }
        getPreferenceManager().addPreferencesScreen(dialog);
        dialog.show();
    }

    public void onDismiss(DialogInterface dialog) {
        this.mDialog = null;
        getPreferenceManager().removePreferencesScreen(dialog);
    }

    public Dialog getDialog() {
        return this.mDialog;
    }

    public void onItemClick(AdapterView parent, View view, int position, long id) {
        if (parent instanceof ListView) {
            position -= ((ListView) parent).getHeaderViewsCount();
        }
        Preference item = getRootAdapter().getItem(position);
        if (item instanceof Preference) {
            item.performClick(this);
        }
    }

    protected boolean isOnSameScreenAsChildren() {
        return false;
    }

    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        Dialog dialog = this.mDialog;
        if (dialog == null || (dialog.isShowing() ^ 1) != 0) {
            return superState;
        }
        SavedState myState = new SavedState(superState);
        myState.isDialogShowing = true;
        myState.dialogBundle = dialog.onSaveInstanceState();
        return myState;
    }

    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || (state.getClass().equals(SavedState.class) ^ 1) != 0) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        if (myState.isDialogShowing) {
            showDialog(myState.dialogBundle);
        }
    }
}
