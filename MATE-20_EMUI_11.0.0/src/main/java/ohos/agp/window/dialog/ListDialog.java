package ohos.agp.window.dialog;

import java.util.ArrayList;
import java.util.Arrays;
import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.colors.RgbColor;
import ohos.agp.colors.RgbPalette;
import ohos.agp.components.BaseItemProvider;
import ohos.agp.components.Button;
import ohos.agp.components.Checkbox;
import ohos.agp.components.Component;
import ohos.agp.components.ComponentContainer;
import ohos.agp.components.ListContainer;
import ohos.agp.components.RadioButton;
import ohos.agp.components.Text;
import ohos.agp.components.element.ShapeElement;
import ohos.agp.utils.Color;
import ohos.agp.window.dialog.IDialog;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class ListDialog extends CommonDialog {
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) LogDomain.END, "ListDialog");
    private static final int LIST_MAX_ID = 10000;
    public static final int MULTI = 103;
    public static final int NORMAL = 101;
    public static final int SINGLE = 102;
    private BaseItemProvider mAdapter;
    private int mBackColor;
    private int mCheckId;
    private ListContainer.ItemClickedListener mClickListener;
    private IDialog mDialog;
    private ArrayList<String> mItemList;
    private ListContainer.ItemLongClickedListener mLongClicklistener;
    private IDialog.CheckBoxClickedListener mMultiListener;
    private boolean[] mSelectedItems;
    private ListContainer.ItemSelectedListener mSelectedlistener;
    private IDialog.ClickedListener mSingleListener;
    private int mType;
    private ArrayList<Component> mViewList;

    public ListDialog(Context context) {
        this(context, 101);
    }

    public ListDialog(Context context, int i) {
        super(context);
        this.mCheckId = -1;
        this.mType = i;
        this.mDialog = this;
        if (this.mDeviceWidth > 0) {
            this.mWidth = this.mDeviceWidth >> 1;
        }
        this.mHeight = -2;
    }

    public void setOnSingleSelectListener(IDialog.ClickedListener clickedListener) {
        this.mSingleListener = clickedListener;
    }

    public void setOnMultiSelectListener(IDialog.CheckBoxClickedListener checkBoxClickedListener) {
        this.mMultiListener = checkBoxClickedListener;
    }

    public ListDialog addItem(String str) {
        ArrayList<String> arrayList;
        HiLog.debug(LABEL, "addItem", new Object[0]);
        if (str == null || (arrayList = this.mItemList) == null) {
            HiLog.error(LABEL, "addItem item or mItemList is null", new Object[0]);
            return this;
        }
        arrayList.add(str);
        return this;
    }

    public ListDialog removeItem(String str) {
        ArrayList<String> arrayList;
        HiLog.debug(LABEL, "removeItem", new Object[0]);
        if (!(str == null || (arrayList = this.mItemList) == null)) {
            arrayList.remove(str);
        }
        return this;
    }

    public ListDialog setItems(String[] strArr) {
        HiLog.debug(LABEL, "setItems", new Object[0]);
        ArrayList<String> arrayList = this.mItemList;
        if (arrayList != null) {
            arrayList.clear();
        }
        this.mItemList = new ArrayList<>(Arrays.asList(strArr));
        this.mType = 101;
        return this;
    }

    public ListDialog setSingleSelectItems(String[] strArr, int i) {
        HiLog.debug(LABEL, "setSingleSelectItems", new Object[0]);
        ArrayList<String> arrayList = this.mItemList;
        if (arrayList != null) {
            arrayList.clear();
        }
        this.mItemList = new ArrayList<>(Arrays.asList(strArr));
        this.mCheckId = i;
        this.mType = 102;
        return this;
    }

    public ListDialog setMultiSelectItems(String[] strArr, boolean[] zArr) {
        HiLog.debug(LABEL, "setMultiSelectItems", new Object[0]);
        ArrayList<String> arrayList = this.mItemList;
        if (arrayList != null) {
            arrayList.clear();
        }
        this.mItemList = new ArrayList<>(Arrays.asList(strArr));
        this.mSelectedItems = new boolean[zArr.length];
        System.arraycopy(zArr, 0, this.mSelectedItems, 0, zArr.length);
        this.mType = 103;
        return this;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.window.dialog.CommonDialog, ohos.agp.window.dialog.BaseDialog
    public void onShow() {
        setContentCustomComponent(createList());
        super.onShow();
    }

    @Override // ohos.agp.window.dialog.BaseDialog, ohos.agp.window.dialog.IDialog
    public void show() {
        HiLog.debug(LABEL, "show", new Object[0]);
        ArrayList<String> arrayList = this.mItemList;
        if (arrayList != null && arrayList.size() != 0) {
            super.show();
        }
    }

    @Override // ohos.agp.window.dialog.BaseDialog, ohos.agp.window.dialog.IDialog
    public void destroy() {
        HiLog.debug(LABEL, "destroy", new Object[0]);
        ArrayList<String> arrayList = this.mItemList;
        if (arrayList != null) {
            arrayList.clear();
        }
        ArrayList<Component> arrayList2 = this.mViewList;
        if (arrayList2 != null) {
            arrayList2.clear();
        }
        BaseItemProvider baseItemProvider = this.mAdapter;
        if (baseItemProvider != null) {
            baseItemProvider.notifyDataSetInvalidated();
        }
        super.destroy();
    }

    public void setAdapter(BaseItemProvider baseItemProvider) {
        this.mAdapter = baseItemProvider;
    }

    public void setListener(ListContainer.ItemClickedListener itemClickedListener, ListContainer.ItemLongClickedListener itemLongClickedListener, ListContainer.ItemSelectedListener itemSelectedListener) {
        this.mClickListener = itemClickedListener;
        this.mLongClicklistener = itemLongClickedListener;
        this.mSelectedlistener = itemSelectedListener;
    }

    public void setBackColor(int i) {
        this.mBackColor = i;
    }

    public BaseItemProvider getAdapter() {
        return this.mAdapter;
    }

    public Component getItemComponent(int i) {
        ArrayList<Component> arrayList = this.mViewList;
        if (arrayList != null && i >= 0) {
            return arrayList.get(i);
        }
        HiLog.error(LABEL, "getItemView failed for position or mViewList error.", new Object[0]);
        return null;
    }

    private void buttonSetChecked(int i, boolean z) {
        ArrayList<Component> arrayList = this.mViewList;
        if (arrayList == null) {
            HiLog.error(LABEL, "buttonSetChecked failed for mViewList is null.", new Object[0]);
            return;
        }
        Component component = arrayList.get(i);
        if (component instanceof RadioButton) {
            ((RadioButton) component).setChecked(z);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onSelect(int i) {
        ArrayList<Component> arrayList;
        if (i >= 0 && i <= 10000) {
            int i2 = this.mType;
            if (i2 == 103) {
                if (this.mMultiListener != null && (arrayList = this.mViewList) != null) {
                    Component component = arrayList.get(i);
                    if (component instanceof Checkbox) {
                        boolean isChecked = ((Checkbox) component).isChecked();
                        this.mSelectedItems[i] = isChecked;
                        this.mMultiListener.onClick(this.mDialog, i, isChecked);
                    }
                }
            } else if (this.mSingleListener != null) {
                if (i2 == 102) {
                    if (this.mCheckId != i) {
                        buttonSetChecked(i, true);
                        int i3 = this.mCheckId;
                        if (i3 >= 0) {
                            buttonSetChecked(i3, false);
                        }
                        this.mCheckId = i;
                    } else {
                        return;
                    }
                }
                this.mSingleListener.onClick(this.mDialog, i);
            }
        }
    }

    private void setItemClickListener(ListContainer listContainer) {
        listContainer.setItemClickedListener(new ListContainer.ItemClickedListener() {
            /* class ohos.agp.window.dialog.ListDialog.AnonymousClass1 */

            @Override // ohos.agp.components.ListContainer.ItemClickedListener
            public void onItemClicked(ListContainer listContainer, Component component, int i, long j) {
                if (component != null) {
                    for (int i2 = 0; i2 < ListDialog.this.mViewList.size(); i2++) {
                        if (component.equals(ListDialog.this.mViewList.get(i2))) {
                            ListDialog.this.onSelect(i2);
                            return;
                        }
                    }
                }
            }
        });
    }

    private Component createList() {
        this.mViewList = new ArrayList<>();
        ComponentContainer.LayoutConfig layoutConfig = new ComponentContainer.LayoutConfig(this.mWidth, this.mHeight);
        ListContainer listContainer = new ListContainer(this.mContext);
        listContainer.setLayoutConfig(layoutConfig);
        ShapeElement shapeElement = new ShapeElement();
        if (this.mAdapter == null) {
            this.mAdapter = new ListDialogAdapter(this.mItemList);
            setItemClickListener(listContainer);
            shapeElement.setRgbColor(RgbPalette.WHITE);
        } else {
            HiLog.debug(LABEL, "This is adapter set by user.", new Object[0]);
            ListContainer.ItemClickedListener itemClickedListener = this.mClickListener;
            if (itemClickedListener != null) {
                listContainer.setItemClickedListener(itemClickedListener);
            }
            ListContainer.ItemLongClickedListener itemLongClickedListener = this.mLongClicklistener;
            if (itemLongClickedListener != null) {
                listContainer.setItemLongClickedListener(itemLongClickedListener);
            }
            ListContainer.ItemSelectedListener itemSelectedListener = this.mSelectedlistener;
            if (itemSelectedListener != null) {
                listContainer.setItemSelectedListener(itemSelectedListener);
            }
            int i = this.mBackColor;
            if (i != 0) {
                shapeElement.setRgbColor(RgbColor.fromArgbInt(i));
            } else {
                shapeElement.setRgbColor(RgbPalette.WHITE);
            }
        }
        listContainer.setItemProvider(this.mAdapter);
        listContainer.setBackground(shapeElement);
        return listContainer;
    }

    /* access modifiers changed from: package-private */
    public class ListDialogAdapter extends BaseItemProvider {
        private static final int DEF_ITEM_HEIGHT = 100;
        private static final int DEF_ITEM_WIDTH = 700;
        private static final int DEF_TEXT_SIZE = 50;
        private int mDefItemHeight;
        private int mDefItemWidth;
        private ArrayList<String> poolData;

        @Override // ohos.agp.components.BaseItemProvider
        public long getItemId(int i) {
            return (long) i;
        }

        public ListDialogAdapter(ArrayList<String> arrayList) {
            this.poolData = arrayList;
            if (ListDialog.this.mWidth > 0) {
                this.mDefItemWidth = ListDialog.this.mWidth;
            } else {
                this.mDefItemWidth = 700;
            }
            this.mDefItemHeight = 100;
        }

        @Override // ohos.agp.components.BaseItemProvider
        public int getCount() {
            ArrayList<String> arrayList = this.poolData;
            if (arrayList != null) {
                return arrayList.size();
            }
            HiLog.error(ListDialog.LABEL, "getCount() poolData is null.", new Object[0]);
            return 0;
        }

        @Override // ohos.agp.components.BaseItemProvider
        public Object getItem(int i) {
            ArrayList<String> arrayList = this.poolData;
            if (arrayList != null && i >= 0 && i < arrayList.size()) {
                return this.poolData.get(i);
            }
            HiLog.error(ListDialog.LABEL, "Get item failed bacause position out of boundary.", new Object[0]);
            return null;
        }

        /* JADX DEBUG: Failed to insert an additional move for type inference into block B:0:0x0000 */
        /* JADX WARN: Multi-variable type inference failed */
        /* JADX WARN: Type inference failed for: r2v0, types: [ohos.agp.components.Component] */
        /* JADX WARN: Type inference failed for: r2v1, types: [ohos.agp.components.Component] */
        /* JADX WARN: Type inference failed for: r2v2, types: [ohos.agp.components.Button, java.lang.Object] */
        @Override // ohos.agp.components.BaseItemProvider
        public Component getComponent(int i, Component component, ComponentContainer componentContainer) {
            if (component == 0 && i >= 0 && i < 10000) {
                component = createButtonItem(i);
                component.setWidth(this.mDefItemWidth);
                component.setHeight(this.mDefItemHeight);
                component.setText((String) ListDialog.this.mItemList.get(i));
                component.setTextSize(50);
                component.setTextColor(Color.BLACK);
                component.setMultipleLine(false);
                component.setTruncationMode(Text.TruncationMode.ELLIPSIS_AT_END);
                ShapeElement shapeElement = new ShapeElement();
                shapeElement.setRgbColor(RgbPalette.WHITE);
                component.setBackground(shapeElement);
                if (ListDialog.this.mViewList != null) {
                    ListDialog.this.mViewList.add(component);
                }
            }
            return component;
        }

        private Button createButtonItem(int i) {
            int i2 = ListDialog.this.mType;
            if (i2 == 102) {
                RadioButton radioButton = new RadioButton(ListDialog.this.mContext);
                if (ListDialog.this.mCheckId != i) {
                    return radioButton;
                }
                radioButton.setChecked(true);
                return radioButton;
            } else if (i2 != 103) {
                return new Button(ListDialog.this.mContext);
            } else {
                Checkbox checkbox = new Checkbox(ListDialog.this.mContext);
                if (!ListDialog.this.mSelectedItems[i]) {
                    return checkbox;
                }
                checkbox.setChecked(true);
                return checkbox;
            }
        }
    }
}
