package com.android.uiautomator.core;

import android.util.SparseArray;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.regex.Pattern;

@Deprecated
public class UiSelector {
    static final int SELECTOR_CHECKABLE = 30;
    static final int SELECTOR_CHECKED = 15;
    static final int SELECTOR_CHILD = 19;
    static final int SELECTOR_CLASS = 4;
    static final int SELECTOR_CLASS_REGEX = 26;
    static final int SELECTOR_CLICKABLE = 14;
    static final int SELECTOR_CONTAINER = 20;
    static final int SELECTOR_CONTAINS_DESCRIPTION = 7;
    static final int SELECTOR_CONTAINS_TEXT = 3;
    static final int SELECTOR_COUNT = 23;
    static final int SELECTOR_DESCRIPTION = 5;
    static final int SELECTOR_DESCRIPTION_REGEX = 27;
    static final int SELECTOR_ENABLED = 10;
    static final int SELECTOR_FOCUSABLE = 12;
    static final int SELECTOR_FOCUSED = 11;
    static final int SELECTOR_ID = 17;
    static final int SELECTOR_INDEX = 8;
    static final int SELECTOR_INSTANCE = 9;
    static final int SELECTOR_LONG_CLICKABLE = 24;
    static final int SELECTOR_NIL = 0;
    static final int SELECTOR_PACKAGE_NAME = 18;
    static final int SELECTOR_PACKAGE_NAME_REGEX = 28;
    static final int SELECTOR_PARENT = 22;
    static final int SELECTOR_PATTERN = 21;
    static final int SELECTOR_RESOURCE_ID = 29;
    static final int SELECTOR_RESOURCE_ID_REGEX = 31;
    static final int SELECTOR_SCROLLABLE = 13;
    static final int SELECTOR_SELECTED = 16;
    static final int SELECTOR_START_DESCRIPTION = 6;
    static final int SELECTOR_START_TEXT = 2;
    static final int SELECTOR_TEXT = 1;
    static final int SELECTOR_TEXT_REGEX = 25;
    private SparseArray<Object> mSelectorAttributes = new SparseArray();

    UiSelector(UiSelector selector) {
        this.mSelectorAttributes = selector.cloneSelector().mSelectorAttributes;
    }

    protected UiSelector cloneSelector() {
        UiSelector ret = new UiSelector();
        ret.mSelectorAttributes = this.mSelectorAttributes.clone();
        if (hasChildSelector()) {
            ret.mSelectorAttributes.put(SELECTOR_CHILD, new UiSelector(getChildSelector()));
        }
        if (hasParentSelector()) {
            ret.mSelectorAttributes.put(SELECTOR_PARENT, new UiSelector(getParentSelector()));
        }
        if (hasPatternSelector()) {
            ret.mSelectorAttributes.put(SELECTOR_PATTERN, new UiSelector(getPatternSelector()));
        }
        return ret;
    }

    static UiSelector patternBuilder(UiSelector selector) {
        if (selector.hasPatternSelector()) {
            return selector;
        }
        return new UiSelector().patternSelector(selector);
    }

    static UiSelector patternBuilder(UiSelector container, UiSelector pattern) {
        return new UiSelector(new UiSelector().containerSelector(container).patternSelector(pattern));
    }

    public UiSelector text(String text) {
        return buildSelector(1, text);
    }

    public UiSelector textMatches(String regex) {
        return buildSelector(SELECTOR_TEXT_REGEX, Pattern.compile(regex));
    }

    public UiSelector textStartsWith(String text) {
        return buildSelector(2, text);
    }

    public UiSelector textContains(String text) {
        return buildSelector(SELECTOR_CONTAINS_TEXT, text);
    }

    public UiSelector className(String className) {
        return buildSelector(SELECTOR_CLASS, className);
    }

    public UiSelector classNameMatches(String regex) {
        return buildSelector(SELECTOR_CLASS_REGEX, Pattern.compile(regex));
    }

    public <T> UiSelector className(Class<T> type) {
        return buildSelector(SELECTOR_CLASS, type.getName());
    }

    public UiSelector description(String desc) {
        return buildSelector(SELECTOR_DESCRIPTION, desc);
    }

    public UiSelector descriptionMatches(String regex) {
        return buildSelector(SELECTOR_DESCRIPTION_REGEX, Pattern.compile(regex));
    }

    public UiSelector descriptionStartsWith(String desc) {
        return buildSelector(SELECTOR_START_DESCRIPTION, desc);
    }

    public UiSelector descriptionContains(String desc) {
        return buildSelector(SELECTOR_CONTAINS_DESCRIPTION, desc);
    }

    public UiSelector resourceId(String id) {
        return buildSelector(SELECTOR_RESOURCE_ID, id);
    }

    public UiSelector resourceIdMatches(String regex) {
        return buildSelector(SELECTOR_RESOURCE_ID_REGEX, Pattern.compile(regex));
    }

    public UiSelector index(int index) {
        return buildSelector(SELECTOR_INDEX, Integer.valueOf(index));
    }

    public UiSelector instance(int instance) {
        return buildSelector(SELECTOR_INSTANCE, Integer.valueOf(instance));
    }

    public UiSelector enabled(boolean val) {
        return buildSelector(SELECTOR_ENABLED, Boolean.valueOf(val));
    }

    public UiSelector focused(boolean val) {
        return buildSelector(SELECTOR_FOCUSED, Boolean.valueOf(val));
    }

    public UiSelector focusable(boolean val) {
        return buildSelector(SELECTOR_FOCUSABLE, Boolean.valueOf(val));
    }

    public UiSelector scrollable(boolean val) {
        return buildSelector(SELECTOR_SCROLLABLE, Boolean.valueOf(val));
    }

    public UiSelector selected(boolean val) {
        return buildSelector(SELECTOR_SELECTED, Boolean.valueOf(val));
    }

    public UiSelector checked(boolean val) {
        return buildSelector(SELECTOR_CHECKED, Boolean.valueOf(val));
    }

    public UiSelector clickable(boolean val) {
        return buildSelector(SELECTOR_CLICKABLE, Boolean.valueOf(val));
    }

    public UiSelector checkable(boolean val) {
        return buildSelector(SELECTOR_CHECKABLE, Boolean.valueOf(val));
    }

    public UiSelector longClickable(boolean val) {
        return buildSelector(SELECTOR_LONG_CLICKABLE, Boolean.valueOf(val));
    }

    public UiSelector childSelector(UiSelector selector) {
        return buildSelector(SELECTOR_CHILD, selector);
    }

    private UiSelector patternSelector(UiSelector selector) {
        return buildSelector(SELECTOR_PATTERN, selector);
    }

    private UiSelector containerSelector(UiSelector selector) {
        return buildSelector(SELECTOR_CONTAINER, selector);
    }

    public UiSelector fromParent(UiSelector selector) {
        return buildSelector(SELECTOR_PARENT, selector);
    }

    public UiSelector packageName(String name) {
        return buildSelector(SELECTOR_PACKAGE_NAME, name);
    }

    public UiSelector packageNameMatches(String regex) {
        return buildSelector(SELECTOR_PACKAGE_NAME_REGEX, Pattern.compile(regex));
    }

    private UiSelector buildSelector(int selectorId, Object selectorValue) {
        UiSelector selector = new UiSelector(this);
        if (selectorId == SELECTOR_CHILD || selectorId == SELECTOR_PARENT) {
            selector.getLastSubSelector().mSelectorAttributes.put(selectorId, selectorValue);
        } else {
            selector.mSelectorAttributes.put(selectorId, selectorValue);
        }
        return selector;
    }

    UiSelector getChildSelector() {
        UiSelector selector = (UiSelector) this.mSelectorAttributes.get(SELECTOR_CHILD, null);
        if (selector != null) {
            return new UiSelector(selector);
        }
        return null;
    }

    UiSelector getPatternSelector() {
        UiSelector selector = (UiSelector) this.mSelectorAttributes.get(SELECTOR_PATTERN, null);
        if (selector != null) {
            return new UiSelector(selector);
        }
        return null;
    }

    UiSelector getContainerSelector() {
        UiSelector selector = (UiSelector) this.mSelectorAttributes.get(SELECTOR_CONTAINER, null);
        if (selector != null) {
            return new UiSelector(selector);
        }
        return null;
    }

    UiSelector getParentSelector() {
        UiSelector selector = (UiSelector) this.mSelectorAttributes.get(SELECTOR_PARENT, null);
        if (selector != null) {
            return new UiSelector(selector);
        }
        return null;
    }

    int getInstance() {
        return getInt(SELECTOR_INSTANCE);
    }

    String getString(int criterion) {
        return (String) this.mSelectorAttributes.get(criterion, null);
    }

    boolean getBoolean(int criterion) {
        return ((Boolean) this.mSelectorAttributes.get(criterion, Boolean.valueOf(false))).booleanValue();
    }

    int getInt(int criterion) {
        return ((Integer) this.mSelectorAttributes.get(criterion, Integer.valueOf(SELECTOR_NIL))).intValue();
    }

    Pattern getPattern(int criterion) {
        return (Pattern) this.mSelectorAttributes.get(criterion, null);
    }

    boolean isMatchFor(AccessibilityNodeInfo node, int index) {
        int size = this.mSelectorAttributes.size();
        for (int x = SELECTOR_NIL; x < size; x++) {
            int criterion = this.mSelectorAttributes.keyAt(x);
            CharSequence s;
            switch (criterion) {
                case 1:
                    s = node.getText();
                    if (s != null && (s.toString().contentEquals(getString(criterion)) ^ 1) == 0) {
                        break;
                    }
                    return false;
                case 2:
                    s = node.getText();
                    if (s != null && (s.toString().toLowerCase().startsWith(getString(criterion).toLowerCase()) ^ 1) == 0) {
                        break;
                    }
                    return false;
                case SELECTOR_CONTAINS_TEXT /*3*/:
                    s = node.getText();
                    if (s != null && (s.toString().toLowerCase().contains(getString(criterion).toLowerCase()) ^ 1) == 0) {
                        break;
                    }
                    return false;
                case SELECTOR_CLASS /*4*/:
                    s = node.getClassName();
                    if (s != null && (s.toString().contentEquals(getString(criterion)) ^ 1) == 0) {
                        break;
                    }
                    return false;
                case SELECTOR_DESCRIPTION /*5*/:
                    s = node.getContentDescription();
                    if (s != null && (s.toString().contentEquals(getString(criterion)) ^ 1) == 0) {
                        break;
                    }
                    return false;
                case SELECTOR_START_DESCRIPTION /*6*/:
                    s = node.getContentDescription();
                    if (s != null && (s.toString().toLowerCase().startsWith(getString(criterion).toLowerCase()) ^ 1) == 0) {
                        break;
                    }
                    return false;
                case SELECTOR_CONTAINS_DESCRIPTION /*7*/:
                    s = node.getContentDescription();
                    if (s != null && (s.toString().toLowerCase().contains(getString(criterion).toLowerCase()) ^ 1) == 0) {
                        break;
                    }
                    return false;
                case SELECTOR_INDEX /*8*/:
                    if (index == getInt(criterion)) {
                        break;
                    }
                    return false;
                case SELECTOR_ENABLED /*10*/:
                    if (node.isEnabled() == getBoolean(criterion)) {
                        break;
                    }
                    return false;
                case SELECTOR_FOCUSED /*11*/:
                    if (node.isFocused() == getBoolean(criterion)) {
                        break;
                    }
                    return false;
                case SELECTOR_FOCUSABLE /*12*/:
                    if (node.isFocusable() == getBoolean(criterion)) {
                        break;
                    }
                    return false;
                case SELECTOR_SCROLLABLE /*13*/:
                    if (node.isScrollable() == getBoolean(criterion)) {
                        break;
                    }
                    return false;
                case SELECTOR_CLICKABLE /*14*/:
                    if (node.isClickable() == getBoolean(criterion)) {
                        break;
                    }
                    return false;
                case SELECTOR_CHECKED /*15*/:
                    if (node.isChecked() == getBoolean(criterion)) {
                        break;
                    }
                    return false;
                case SELECTOR_SELECTED /*16*/:
                    if (node.isSelected() == getBoolean(criterion)) {
                        break;
                    }
                    return false;
                case SELECTOR_PACKAGE_NAME /*18*/:
                    s = node.getPackageName();
                    if (s != null && (s.toString().contentEquals(getString(criterion)) ^ 1) == 0) {
                        break;
                    }
                    return false;
                case SELECTOR_LONG_CLICKABLE /*24*/:
                    if (node.isLongClickable() == getBoolean(criterion)) {
                        break;
                    }
                    return false;
                case SELECTOR_TEXT_REGEX /*25*/:
                    s = node.getText();
                    if (s != null && (getPattern(criterion).matcher(s).matches() ^ 1) == 0) {
                        break;
                    }
                    return false;
                case SELECTOR_CLASS_REGEX /*26*/:
                    s = node.getClassName();
                    if (s != null && (getPattern(criterion).matcher(s).matches() ^ 1) == 0) {
                        break;
                    }
                    return false;
                    break;
                case SELECTOR_DESCRIPTION_REGEX /*27*/:
                    s = node.getContentDescription();
                    if (s != null && (getPattern(criterion).matcher(s).matches() ^ 1) == 0) {
                        break;
                    }
                    return false;
                    break;
                case SELECTOR_PACKAGE_NAME_REGEX /*28*/:
                    s = node.getPackageName();
                    if (s != null && (getPattern(criterion).matcher(s).matches() ^ 1) == 0) {
                        break;
                    }
                    return false;
                case SELECTOR_RESOURCE_ID /*29*/:
                    s = node.getViewIdResourceName();
                    if (s != null && (s.toString().contentEquals(getString(criterion)) ^ 1) == 0) {
                        break;
                    }
                    return false;
                case SELECTOR_CHECKABLE /*30*/:
                    if (node.isCheckable() == getBoolean(criterion)) {
                        break;
                    }
                    return false;
                case SELECTOR_RESOURCE_ID_REGEX /*31*/:
                    s = node.getViewIdResourceName();
                    if (s != null && (getPattern(criterion).matcher(s).matches() ^ 1) == 0) {
                        break;
                    }
                    return false;
                    break;
                default:
                    break;
            }
        }
        return matchOrUpdateInstance();
    }

    private boolean matchOrUpdateInstance() {
        int currentSelectorCounter = SELECTOR_NIL;
        int currentSelectorInstance = SELECTOR_NIL;
        if (this.mSelectorAttributes.indexOfKey(SELECTOR_INSTANCE) >= 0) {
            currentSelectorInstance = ((Integer) this.mSelectorAttributes.get(SELECTOR_INSTANCE)).intValue();
        }
        if (this.mSelectorAttributes.indexOfKey(SELECTOR_COUNT) >= 0) {
            currentSelectorCounter = ((Integer) this.mSelectorAttributes.get(SELECTOR_COUNT)).intValue();
        }
        if (currentSelectorInstance == currentSelectorCounter) {
            return true;
        }
        if (currentSelectorInstance > currentSelectorCounter) {
            this.mSelectorAttributes.put(SELECTOR_COUNT, Integer.valueOf(currentSelectorCounter + 1));
        }
        return false;
    }

    boolean isLeaf() {
        if (this.mSelectorAttributes.indexOfKey(SELECTOR_CHILD) >= 0 || this.mSelectorAttributes.indexOfKey(SELECTOR_PARENT) >= 0) {
            return false;
        }
        return true;
    }

    boolean hasChildSelector() {
        if (this.mSelectorAttributes.indexOfKey(SELECTOR_CHILD) < 0) {
            return false;
        }
        return true;
    }

    boolean hasPatternSelector() {
        if (this.mSelectorAttributes.indexOfKey(SELECTOR_PATTERN) < 0) {
            return false;
        }
        return true;
    }

    boolean hasContainerSelector() {
        if (this.mSelectorAttributes.indexOfKey(SELECTOR_CONTAINER) < 0) {
            return false;
        }
        return true;
    }

    boolean hasParentSelector() {
        if (this.mSelectorAttributes.indexOfKey(SELECTOR_PARENT) < 0) {
            return false;
        }
        return true;
    }

    private UiSelector getLastSubSelector() {
        if (this.mSelectorAttributes.indexOfKey(SELECTOR_CHILD) >= 0) {
            UiSelector child = (UiSelector) this.mSelectorAttributes.get(SELECTOR_CHILD);
            if (child.getLastSubSelector() == null) {
                return child;
            }
            return child.getLastSubSelector();
        } else if (this.mSelectorAttributes.indexOfKey(SELECTOR_PARENT) < 0) {
            return this;
        } else {
            UiSelector parent = (UiSelector) this.mSelectorAttributes.get(SELECTOR_PARENT);
            if (parent.getLastSubSelector() == null) {
                return parent;
            }
            return parent.getLastSubSelector();
        }
    }

    public String toString() {
        return dumpToString(true);
    }

    String dumpToString(boolean all) {
        StringBuilder builder = new StringBuilder();
        builder.append(UiSelector.class.getSimpleName()).append("[");
        int criterionCount = this.mSelectorAttributes.size();
        for (int i = SELECTOR_NIL; i < criterionCount; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            int criterion = this.mSelectorAttributes.keyAt(i);
            switch (criterion) {
                case 1:
                    builder.append("TEXT=").append(this.mSelectorAttributes.valueAt(i));
                    break;
                case 2:
                    builder.append("START_TEXT=").append(this.mSelectorAttributes.valueAt(i));
                    break;
                case SELECTOR_CONTAINS_TEXT /*3*/:
                    builder.append("CONTAINS_TEXT=").append(this.mSelectorAttributes.valueAt(i));
                    break;
                case SELECTOR_CLASS /*4*/:
                    builder.append("CLASS=").append(this.mSelectorAttributes.valueAt(i));
                    break;
                case SELECTOR_DESCRIPTION /*5*/:
                    builder.append("DESCRIPTION=").append(this.mSelectorAttributes.valueAt(i));
                    break;
                case SELECTOR_START_DESCRIPTION /*6*/:
                    builder.append("START_DESCRIPTION=").append(this.mSelectorAttributes.valueAt(i));
                    break;
                case SELECTOR_CONTAINS_DESCRIPTION /*7*/:
                    builder.append("CONTAINS_DESCRIPTION=").append(this.mSelectorAttributes.valueAt(i));
                    break;
                case SELECTOR_INDEX /*8*/:
                    builder.append("INDEX=").append(this.mSelectorAttributes.valueAt(i));
                    break;
                case SELECTOR_INSTANCE /*9*/:
                    builder.append("INSTANCE=").append(this.mSelectorAttributes.valueAt(i));
                    break;
                case SELECTOR_ENABLED /*10*/:
                    builder.append("ENABLED=").append(this.mSelectorAttributes.valueAt(i));
                    break;
                case SELECTOR_FOCUSED /*11*/:
                    builder.append("FOCUSED=").append(this.mSelectorAttributes.valueAt(i));
                    break;
                case SELECTOR_FOCUSABLE /*12*/:
                    builder.append("FOCUSABLE=").append(this.mSelectorAttributes.valueAt(i));
                    break;
                case SELECTOR_SCROLLABLE /*13*/:
                    builder.append("SCROLLABLE=").append(this.mSelectorAttributes.valueAt(i));
                    break;
                case SELECTOR_CLICKABLE /*14*/:
                    builder.append("CLICKABLE=").append(this.mSelectorAttributes.valueAt(i));
                    break;
                case SELECTOR_CHECKED /*15*/:
                    builder.append("CHECKED=").append(this.mSelectorAttributes.valueAt(i));
                    break;
                case SELECTOR_SELECTED /*16*/:
                    builder.append("SELECTED=").append(this.mSelectorAttributes.valueAt(i));
                    break;
                case SELECTOR_ID /*17*/:
                    builder.append("ID=").append(this.mSelectorAttributes.valueAt(i));
                    break;
                case SELECTOR_PACKAGE_NAME /*18*/:
                    builder.append("PACKAGE NAME=").append(this.mSelectorAttributes.valueAt(i));
                    break;
                case SELECTOR_CHILD /*19*/:
                    if (!all) {
                        builder.append("CHILD[..]");
                        break;
                    }
                    builder.append("CHILD=").append(this.mSelectorAttributes.valueAt(i));
                    break;
                case SELECTOR_CONTAINER /*20*/:
                    if (!all) {
                        builder.append("CONTAINER[..]");
                        break;
                    }
                    builder.append("CONTAINER=").append(this.mSelectorAttributes.valueAt(i));
                    break;
                case SELECTOR_PATTERN /*21*/:
                    if (!all) {
                        builder.append("PATTERN[..]");
                        break;
                    }
                    builder.append("PATTERN=").append(this.mSelectorAttributes.valueAt(i));
                    break;
                case SELECTOR_PARENT /*22*/:
                    if (!all) {
                        builder.append("PARENT[..]");
                        break;
                    }
                    builder.append("PARENT=").append(this.mSelectorAttributes.valueAt(i));
                    break;
                case SELECTOR_COUNT /*23*/:
                    builder.append("COUNT=").append(this.mSelectorAttributes.valueAt(i));
                    break;
                case SELECTOR_LONG_CLICKABLE /*24*/:
                    builder.append("LONG_CLICKABLE=").append(this.mSelectorAttributes.valueAt(i));
                    break;
                case SELECTOR_TEXT_REGEX /*25*/:
                    builder.append("TEXT_REGEX=").append(this.mSelectorAttributes.valueAt(i));
                    break;
                case SELECTOR_CLASS_REGEX /*26*/:
                    builder.append("CLASS_REGEX=").append(this.mSelectorAttributes.valueAt(i));
                    break;
                case SELECTOR_DESCRIPTION_REGEX /*27*/:
                    builder.append("DESCRIPTION_REGEX=").append(this.mSelectorAttributes.valueAt(i));
                    break;
                case SELECTOR_PACKAGE_NAME_REGEX /*28*/:
                    builder.append("PACKAGE_NAME_REGEX=").append(this.mSelectorAttributes.valueAt(i));
                    break;
                case SELECTOR_RESOURCE_ID /*29*/:
                    builder.append("RESOURCE_ID=").append(this.mSelectorAttributes.valueAt(i));
                    break;
                case SELECTOR_CHECKABLE /*30*/:
                    builder.append("CHECKABLE=").append(this.mSelectorAttributes.valueAt(i));
                    break;
                case SELECTOR_RESOURCE_ID_REGEX /*31*/:
                    builder.append("RESOURCE_ID_REGEX=").append(this.mSelectorAttributes.valueAt(i));
                    break;
                default:
                    builder.append("UNDEFINED=").append(criterion).append(" ").append(this.mSelectorAttributes.valueAt(i));
                    break;
            }
        }
        builder.append("]");
        return builder.toString();
    }
}
