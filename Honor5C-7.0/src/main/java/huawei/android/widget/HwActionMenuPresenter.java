package huawei.android.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.WindowManagerGlobal;
import android.widget.ActionMenuPresenter;
import android.widget.ForwardingListener;
import android.widget.ListPopupWindow;
import com.android.internal.view.menu.MenuItemImpl;
import com.android.internal.view.menu.MenuPopup;
import com.android.internal.view.menu.MenuPopupHelper;
import com.android.internal.view.menu.SubMenuBuilder;

public class HwActionMenuPresenter extends ActionMenuPresenter {
    private OnPreDrawListener mOverflowMenuPreDrawListener;
    private boolean mShowOverflowMenuPending;

    /* renamed from: huawei.android.widget.HwActionMenuPresenter.2 */
    class AnonymousClass2 extends ForwardingListener {
        final /* synthetic */ MenuItemImpl val$item;

        AnonymousClass2(View $anonymous0, MenuItemImpl val$item) {
            this.val$item = val$item;
            super($anonymous0);
        }

        public ListPopupWindow getPopup() {
            if (HwActionMenuPresenter.this.getActionButtonPopup() != null) {
                MenuPopup mp = HwActionMenuPresenter.this.getActionButtonPopup().getPopup();
                if (mp != null) {
                    return mp.getMenuPopup();
                }
            }
            return null;
        }

        protected boolean onForwardingStarted() {
            return HwActionMenuPresenter.this.onSubMenuSelected((SubMenuBuilder) this.val$item.getSubMenu());
        }

        protected boolean onForwardingStopped() {
            return HwActionMenuPresenter.this.dismissPopupMenus();
        }

        public boolean onTouch(View v, MotionEvent event) {
            boolean z = false;
            if (this.mForwarding && getPopup() != null) {
                z = true;
            }
            this.mForwarding = z;
            return super.onTouch(v, event);
        }
    }

    private static class SavedState implements Parcelable {
        public static final Creator<SavedState> CREATOR = null;
        public int openSubMenuId;
        public int overflowMenuShownInt;
        public boolean rogEnable;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.android.widget.HwActionMenuPresenter.SavedState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.android.widget.HwActionMenuPresenter.SavedState.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: huawei.android.widget.HwActionMenuPresenter.SavedState.<clinit>():void");
        }

        SavedState() {
        }

        SavedState(Parcel in) {
            boolean z = false;
            this.openSubMenuId = in.readInt();
            this.overflowMenuShownInt = in.readInt();
            if (in.readInt() > 0) {
                z = true;
            }
            this.rogEnable = z;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.openSubMenuId);
            dest.writeInt(this.overflowMenuShownInt);
            dest.writeInt(this.rogEnable ? 1 : 0);
        }
    }

    public HwActionMenuPresenter(Context context) {
        super(context);
        this.mShowOverflowMenuPending = false;
        this.mOverflowMenuPreDrawListener = new OnPreDrawListener() {
            public boolean onPreDraw() {
                if (HwActionMenuPresenter.this.mShowOverflowMenuPending) {
                    HwActionMenuPresenter.this.mShowOverflowMenuPending = false;
                    HwActionMenuPresenter.this.showOverflowMenu();
                }
                return true;
            }
        };
    }

    public HwActionMenuPresenter(Context context, int menuLayout, int itemLayout) {
        super(context, menuLayout, itemLayout);
        this.mShowOverflowMenuPending = false;
        this.mOverflowMenuPreDrawListener = new OnPreDrawListener() {
            public boolean onPreDraw() {
                if (HwActionMenuPresenter.this.mShowOverflowMenuPending) {
                    HwActionMenuPresenter.this.mShowOverflowMenuPending = false;
                    HwActionMenuPresenter.this.showOverflowMenu();
                }
                return true;
            }
        };
    }

    public boolean isPopupMenuShowing() {
        return getActionButtonPopup() != null ? getActionButtonPopup().isShowing() : false;
    }

    public Parcelable onSaveInstanceState() {
        SavedState state = new SavedState();
        state.openSubMenuId = getOpenSubMenuId();
        state.overflowMenuShownInt = isOverflowMenuShowing() ? 1 : 0;
        state.rogEnable = WindowManagerGlobal.getInstance().getRogSwitchState();
        return state;
    }

    public void onRestoreInstanceState(Parcelable state) {
        boolean overflowMenuShown = false;
        SavedState saved = (SavedState) state;
        if (saved.openSubMenuId > 0 && this.mMenu != null) {
            MenuItem item = this.mMenu.findItem(saved.openSubMenuId);
            if (item != null && saved.rogEnable == WindowManagerGlobal.getInstance().getRogSwitchState()) {
                onSubMenuSelected((SubMenuBuilder) item.getSubMenu());
            }
        }
        if (saved.overflowMenuShownInt > 0) {
            overflowMenuShown = true;
        }
        if (overflowMenuShown) {
            showOverflowMenuPending();
        }
    }

    public void showOverflowMenuPending() {
        this.mShowOverflowMenuPending = true;
    }

    public void updateMenuView(boolean cleared) {
        super.updateMenuView(cleared);
        View over = getOverflowButton();
        if (over != null) {
            ViewTreeObserver vto = over.getViewTreeObserver();
            vto.removeOnPreDrawListener(this.mOverflowMenuPreDrawListener);
            vto.addOnPreDrawListener(this.mOverflowMenuPreDrawListener);
        }
    }

    public View getItemView(MenuItemImpl item, View convertView, ViewGroup parent) {
        View actionView = super.getItemView(item, convertView, parent);
        if (item.hasSubMenu()) {
            actionView.setOnTouchListener(new AnonymousClass2(actionView, item));
        } else {
            actionView.setOnTouchListener(null);
        }
        return actionView;
    }

    public void setOverflowIcon(Drawable icon) {
        if (!(getOverflowButton() instanceof HwOverflowMenuButton)) {
            super.setOverflowIcon(icon);
        }
    }

    public Drawable getOverflowIcon() {
        if (getOverflowButton() instanceof HwOverflowMenuButton) {
            return null;
        }
        return super.getOverflowIcon();
    }

    protected void setPopupGravity(MenuPopupHelper mph) {
        mph.setGravity(0);
    }

    protected int getMaxActionButtons(int maxItems) {
        return 5;
    }

    public void setPopupLocation(int start, int end) {
        this.mPopupStartLocation = start;
        this.mPopupEndLocation = end;
    }
}
