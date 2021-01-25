package ohos.accessibility.adapter;

import ohos.agp.utils.Rect;

public class AccessibilityViewInfo {
    private int[] actionList;
    private int childCount;
    private int[] childIdList;
    private String className;
    private int colCount;
    private String componentInputType;
    private String description;
    private String error;
    private int height;
    private String hintText;
    private int id;
    private int inputType;
    private boolean isBarrierFreeFocused;
    private boolean isCheckable;
    private boolean isChecked;
    private boolean isClickable;
    private boolean isEditable;
    private boolean isEnabled;
    private boolean isExpanded;
    private boolean isFocusable;
    private boolean isFocused;
    private boolean isLongClickable;
    private boolean isMultiLine;
    private boolean isPassword;
    private boolean isScrollable;
    private boolean isSelected;
    private boolean isShowingHintText;
    private boolean isVisible;
    private int left;
    private ListInfo listInfo;
    private ListItemInfo listItemInfo;
    private int maxTextLength;
    private int parentId;
    private ProgressInfo progressInfo;
    private Rect rect;
    private String resourceName;
    private int rowCount;
    private String text;
    private int textSelectionEnd;
    private int textSelectionStart;
    private int top;
    private String viewType;
    private int width;
    private int windowId;

    public String getText() {
        return this.text;
    }

    public void setText(String str) {
        this.text = str;
    }

    public String getViewType() {
        return this.viewType;
    }

    public void setViewType(String str) {
        this.viewType = str;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int i) {
        this.id = i;
    }

    public int getLeft() {
        return this.left;
    }

    public void setLeft(int i) {
        this.left = i;
    }

    public int getTop() {
        return this.top;
    }

    public void setTop(int i) {
        this.top = i;
    }

    public int getWidth() {
        return this.width;
    }

    public void setWidth(int i) {
        this.width = i;
    }

    public int getHeight() {
        return this.height;
    }

    public void setHeight(int i) {
        this.height = i;
    }

    public int getParentId() {
        return this.parentId;
    }

    public void setParentId(int i) {
        this.parentId = i;
    }

    public int getInputType() {
        return this.inputType;
    }

    public String getComponentInputType() {
        return this.componentInputType;
    }

    public void setComponentInputType(String str) {
        this.componentInputType = str;
    }

    public void setInputType(int i) {
        this.inputType = i;
    }

    public int getWindowId() {
        return this.windowId;
    }

    public void setWindowId(int i) {
        this.windowId = i;
    }

    public boolean isChecked() {
        return this.isChecked;
    }

    public void setChecked(boolean z) {
        this.isChecked = z;
    }

    public boolean isFocused() {
        return this.isFocused;
    }

    public void setFocused(boolean z) {
        this.isFocused = z;
    }

    public boolean isSelected() {
        return this.isSelected;
    }

    public void setSelected(boolean z) {
        this.isSelected = z;
    }

    public boolean isEnabled() {
        return this.isEnabled;
    }

    public void setEnabled(boolean z) {
        this.isEnabled = z;
    }

    public boolean isBarrierFreeFocused() {
        return this.isBarrierFreeFocused;
    }

    public void setBarrierFreeFocused(boolean z) {
        this.isBarrierFreeFocused = z;
    }

    public boolean isCheckable() {
        return this.isCheckable;
    }

    public void setCheckable(boolean z) {
        this.isCheckable = z;
    }

    public boolean isClickable() {
        return this.isClickable;
    }

    public void setClickable(boolean z) {
        this.isClickable = z;
    }

    public boolean isFocusable() {
        return this.isFocusable;
    }

    public void setFocusable(boolean z) {
        this.isFocusable = z;
    }

    public boolean isScrollable() {
        return this.isScrollable;
    }

    public void setScrollable(boolean z) {
        this.isScrollable = z;
    }

    public boolean isLongClickable() {
        return this.isLongClickable;
    }

    public void setLongClickable(boolean z) {
        this.isLongClickable = z;
    }

    public boolean isPassword() {
        return this.isPassword;
    }

    public void setIsPassword(boolean z) {
        this.isPassword = z;
    }

    public int[] getChildIdList() {
        int[] iArr = this.childIdList;
        return iArr == null ? new int[0] : (int[]) iArr.clone();
    }

    public void setChildIdList(int[] iArr) {
        if (iArr != null) {
            this.childIdList = (int[]) iArr.clone();
        } else {
            this.childIdList = new int[0];
        }
    }

    public int[] getActionList() {
        int[] iArr = this.actionList;
        return iArr == null ? new int[0] : (int[]) iArr.clone();
    }

    public void setActionList(int[] iArr) {
        this.actionList = (int[]) iArr.clone();
    }

    public Rect getRect() {
        if (this.rect == null) {
            int i = this.left;
            int i2 = this.top;
            this.rect = new Rect(i, i2, this.width + i, this.height + i2);
        }
        return this.rect;
    }

    public String getHintText() {
        return this.hintText;
    }

    public void setHintText(String str) {
        this.hintText = str;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String str) {
        this.description = str;
    }

    public String getResourceName() {
        return this.resourceName;
    }

    public void setResourceName(String str) {
        this.resourceName = str;
    }

    public String getClassName() {
        return this.className;
    }

    public void setClassName(String str) {
        this.className = str;
    }

    public int getMaxTextLength() {
        return this.maxTextLength;
    }

    public void setMaxTextLength(int i) {
        this.maxTextLength = i;
    }

    public boolean isVisible() {
        return this.isVisible;
    }

    public void setVisible(boolean z) {
        this.isVisible = z;
    }

    public int getRowCount() {
        return this.rowCount;
    }

    public void setRowCount(int i) {
        this.rowCount = i;
    }

    public int getColCount() {
        return this.colCount;
    }

    public void setColCount(int i) {
        this.colCount = i;
    }

    public ListInfo getListInfo() {
        return this.listInfo;
    }

    public void setListInfo(ListInfo listInfo2) {
        this.listInfo = listInfo2;
    }

    public ListItemInfo getListItemInfo() {
        return this.listItemInfo;
    }

    public void setListItemInfo(ListItemInfo listItemInfo2) {
        this.listItemInfo = listItemInfo2;
    }

    public ProgressInfo getProgressInfo() {
        return this.progressInfo;
    }

    public void setProgressInfo(ProgressInfo progressInfo2) {
        this.progressInfo = progressInfo2;
    }

    public int getChildCount() {
        return this.childCount;
    }

    public void setChildCount(int i) {
        this.childCount = i;
    }

    public int getTextSelectionStart() {
        return this.textSelectionStart;
    }

    public void setTextSelectionStart(int i) {
        this.textSelectionStart = i;
    }

    public int getTextSelectionEnd() {
        return this.textSelectionEnd;
    }

    public void setTextSelectionEnd(int i) {
        this.textSelectionEnd = i;
    }

    public void setPassword(boolean z) {
        this.isPassword = z;
    }

    public boolean isEditable() {
        return this.isEditable;
    }

    public void setEditable(boolean z) {
        this.isEditable = z;
    }

    public boolean isMultiLine() {
        return this.isMultiLine;
    }

    public void setMultiLine(boolean z) {
        this.isMultiLine = z;
    }

    public boolean isShowingHintText() {
        return this.isShowingHintText;
    }

    public void setShowingHintText(boolean z) {
        this.isShowingHintText = z;
    }

    public boolean isExpanded() {
        return this.isExpanded;
    }

    public void setExpanded(boolean z) {
        this.isExpanded = z;
    }

    public String getError() {
        return this.error;
    }

    public void setError(String str) {
        this.error = str;
    }

    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("id:" + this.id);
        stringBuffer.append("; viewType:" + this.viewType);
        stringBuffer.append("; description:" + this.description);
        stringBuffer.append("; resourceName:" + this.resourceName);
        stringBuffer.append("; maxTextLength:" + this.maxTextLength);
        stringBuffer.append("; left:" + this.left);
        stringBuffer.append("; top:" + this.top);
        stringBuffer.append("; width:" + this.width);
        stringBuffer.append("; height:" + this.height);
        stringBuffer.append("; rowCount:" + this.rowCount);
        stringBuffer.append("; colCount:" + this.colCount);
        stringBuffer.append("; parentId:" + this.parentId);
        stringBuffer.append("; isChecked:" + this.isChecked);
        stringBuffer.append("; isFocused:" + this.isFocused);
        stringBuffer.append("; isSelected:" + this.isSelected);
        stringBuffer.append("; isEnabled:" + this.isEnabled);
        stringBuffer.append("; isCheckable:" + this.isCheckable);
        stringBuffer.append("; isClickable:" + this.isClickable);
        stringBuffer.append("; isFocusable:" + this.isFocusable);
        stringBuffer.append("; isScrollable:" + this.isScrollable);
        stringBuffer.append("; isLongClickable:" + this.isLongClickable);
        stringBuffer.append("; isVisible:" + this.isVisible);
        stringBuffer.append("; isEditable:" + this.isEditable);
        stringBuffer.append("; isShowingHintText:" + this.isShowingHintText);
        stringBuffer.append("; isPassword:" + this.isPassword);
        int i = 0;
        if (this.childIdList != null) {
            stringBuffer.append("; childIds: [");
            int i2 = 0;
            while (true) {
                int[] iArr = this.childIdList;
                if (i2 >= iArr.length) {
                    break;
                }
                stringBuffer.append(iArr[i2]);
                if (i2 < this.childIdList.length - 1) {
                    stringBuffer.append(", ");
                }
                i2++;
            }
            stringBuffer.append("]");
        }
        if (this.actionList != null) {
            stringBuffer.append("; actionList: [");
            while (true) {
                int[] iArr2 = this.actionList;
                if (i >= iArr2.length) {
                    break;
                }
                stringBuffer.append(iArr2[i]);
                if (i < this.actionList.length - 1) {
                    stringBuffer.append(", ");
                }
                i++;
            }
            stringBuffer.append("]");
        }
        return stringBuffer.toString();
    }

    public static final class ListInfo {
        private int columnCount;
        private int rowCount;
        private int selectionMode;

        public int getRowCount() {
            return this.rowCount;
        }

        public void setRowCount(int i) {
            this.rowCount = i;
        }

        public int getColumnCount() {
            return this.columnCount;
        }

        public void setColumnCount(int i) {
            this.columnCount = i;
        }

        public int getSelectionMode() {
            return this.selectionMode;
        }

        public void setSelectionMode(int i) {
            this.selectionMode = i;
        }
    }

    public static final class ListItemInfo {
        private int columnIndex;
        private int columnSpan;
        private boolean isHeading;
        private String itemName;
        private int rowIndex;
        private int rowSpan;
        private boolean selected;

        public boolean isHeading() {
            return this.isHeading;
        }

        public void setHeading(boolean z) {
            this.isHeading = z;
        }

        public int getColumnIndex() {
            return this.columnIndex;
        }

        public void setColumnIndex(int i) {
            this.columnIndex = i;
        }

        public int getRowIndex() {
            return this.rowIndex;
        }

        public void setRowIndex(int i) {
            this.rowIndex = i;
        }

        public int getColumnSpan() {
            return this.columnSpan;
        }

        public void setColumnSpan(int i) {
            this.columnSpan = i;
        }

        public int getRowSpan() {
            return this.rowSpan;
        }

        public void setRowSpan(int i) {
            this.rowSpan = i;
        }

        public boolean isSelected() {
            return this.selected;
        }

        public void setSelected(boolean z) {
            this.selected = z;
        }

        public String getItemName() {
            return this.itemName;
        }

        public void setItemName(String str) {
            this.itemName = str;
        }
    }

    public static final class ProgressInfo {
        private int max;
        private int min;
        private int value;

        public int getMin() {
            return this.min;
        }

        public void setMin(int i) {
            this.min = i;
        }

        public int getMax() {
            return this.max;
        }

        public void setMax(int i) {
            this.max = i;
        }

        public int getValue() {
            return this.value;
        }

        public void setValue(int i) {
            this.value = i;
        }
    }
}
