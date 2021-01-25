package com.android.server.mtm.iaware.appmng.rule;

import android.util.ArrayMap;
import com.android.server.mtm.iaware.appmng.rule.RuleParserUtil;
import com.android.server.rms.iaware.appmng.AppStartPolicyCfg;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class RuleNode {
    private static final String TAG = "RuleNode";
    private StringIntegerMap mChildItems = new StringIntegerMap();
    private RuleParserUtil.TagEnum mChildType;
    private RuleParserUtil.TagEnum mCurrentType;
    private XmlValue mValue;

    public static class XmlValue {
        private int mHwStop = -1;
        private String mIndex;
        private int mIntValue;
        private boolean mIsString;
        private String mStringValue;
        private int mWeight;

        public XmlValue(String value) {
            this.mStringValue = value;
            this.mIsString = true;
        }

        public XmlValue(int value) {
            this.mIntValue = value;
            this.mIsString = false;
        }

        public void setWeight(int weight) {
            this.mWeight = weight;
        }

        public int getWeight() {
            return this.mWeight;
        }

        public void setIndex(String index) {
            this.mIndex = index;
        }

        public void setHwStop(int hwStop) {
            this.mHwStop = hwStop;
        }

        public String getIndex() {
            return this.mIndex;
        }

        public int getHwStop() {
            return this.mHwStop;
        }

        public String getStringValue() {
            return this.mStringValue;
        }

        public int getIntValue() {
            return this.mIntValue;
        }

        public boolean isString() {
            return this.mIsString;
        }

        public String toString() {
            if (this.mIsString) {
                return this.mStringValue;
            }
            return Integer.toString(this.mIntValue);
        }
    }

    public static class StringIntegerMap {
        private LinkedHashMap<Integer, RuleNode> mIntegerMap;
        boolean mIsStringMap;
        private ArrayList<Integer> mSortedList;
        private ArrayMap<String, RuleNode> mStringMap;

        public void put(String key, RuleNode item) {
            if (this.mStringMap == null) {
                this.mStringMap = new ArrayMap<>();
            }
            this.mStringMap.put(key, item);
            this.mIsStringMap = true;
        }

        public void put(int key, RuleNode item) {
            if (this.mIntegerMap == null) {
                this.mIntegerMap = new LinkedHashMap<>();
            }
            this.mIntegerMap.put(Integer.valueOf(key), item);
            this.mIsStringMap = false;
        }

        public RuleNode get(XmlValue key) {
            if (key == null) {
                return null;
            }
            if (key.isString()) {
                ArrayMap<String, RuleNode> arrayMap = this.mStringMap;
                if (arrayMap != null) {
                    return arrayMap.get(key.getStringValue());
                }
            } else {
                LinkedHashMap<Integer, RuleNode> linkedHashMap = this.mIntegerMap;
                if (linkedHashMap != null) {
                    return linkedHashMap.get(Integer.valueOf(key.getIntValue()));
                }
            }
            return null;
        }

        public boolean isEmpty() {
            ArrayMap<String, RuleNode> arrayMap = this.mStringMap;
            if (arrayMap != null) {
                return arrayMap.isEmpty();
            }
            LinkedHashMap<Integer, RuleNode> linkedHashMap = this.mIntegerMap;
            if (linkedHashMap == null || linkedHashMap.size() == 0) {
                return true;
            }
            return false;
        }

        public boolean isStringMap() {
            return this.mIsStringMap;
        }

        public ArrayMap<String, RuleNode> getStringMap() {
            return this.mStringMap;
        }

        public LinkedHashMap<Integer, RuleNode> getIntegerMap() {
            return this.mIntegerMap;
        }

        public ArrayList<Integer> getSortedList() {
            return this.mSortedList;
        }

        private boolean couldSort(RuleParserUtil.AppStartTag tag, int value) {
            int i = AnonymousClass1.$SwitchMap$com$android$server$mtm$iaware$appmng$rule$RuleParserUtil$AppStartTag[tag.ordinal()];
            if (i != 1) {
                if (i != 2) {
                    if (i != 3) {
                        if (i != 4 || value < 0 || value >= AppStartPolicyCfg.AppStartTargetType.values().length) {
                            return false;
                        }
                    } else if (value < 0 || value >= AppStartPolicyCfg.AppStartTargetStat.values().length) {
                        return false;
                    }
                } else if (value < 0 || value >= AppStartPolicyCfg.AppStartCallerStatus.values().length) {
                    return false;
                }
            } else if (value < 0 || value >= AppStartPolicyCfg.AppStartCallerAction.values().length) {
                return false;
            }
            return true;
        }

        private void addToExistSortList(RuleParserUtil.TagEnum tag, int value) {
            ArrayList<Integer> result = new ArrayList<>();
            boolean isAdded = false;
            int listSize = this.mSortedList.size();
            for (int i = 0; i < listSize; i++) {
                Integer val = this.mSortedList.get(i);
                if (!isAdded) {
                    int i2 = AnonymousClass1.$SwitchMap$com$android$server$mtm$iaware$appmng$rule$RuleParserUtil$AppStartTag[((RuleParserUtil.AppStartTag) tag).ordinal()];
                    if (i2 != 1) {
                        if (i2 != 2) {
                            if (i2 != 3) {
                                if (i2 != 4) {
                                    return;
                                }
                                if (AppStartPolicyCfg.AppStartTargetType.values()[value].getPriority() < AppStartPolicyCfg.AppStartTargetType.values()[val.intValue()].getPriority()) {
                                    result.add(Integer.valueOf(value));
                                    isAdded = true;
                                }
                            } else if (AppStartPolicyCfg.AppStartTargetStat.values()[value].getPriority() < AppStartPolicyCfg.AppStartTargetStat.values()[val.intValue()].getPriority()) {
                                result.add(Integer.valueOf(value));
                                isAdded = true;
                            }
                        } else if (AppStartPolicyCfg.AppStartCallerStatus.values()[value].getPriority() < AppStartPolicyCfg.AppStartCallerStatus.values()[val.intValue()].getPriority()) {
                            result.add(Integer.valueOf(value));
                            isAdded = true;
                        }
                    } else if (AppStartPolicyCfg.AppStartCallerAction.values()[value].getPriority() < AppStartPolicyCfg.AppStartCallerAction.values()[val.intValue()].getPriority()) {
                        result.add(Integer.valueOf(value));
                        isAdded = true;
                    }
                }
                result.add(val);
            }
            if (!isAdded) {
                result.add(Integer.valueOf(value));
            }
            this.mSortedList = result;
        }

        public void addToSortedList(RuleParserUtil.TagEnum tag, int value) {
            if (!(tag instanceof RuleParserUtil.AppStartTag) || !couldSort((RuleParserUtil.AppStartTag) tag, value)) {
                return;
            }
            if (this.mSortedList == null) {
                this.mSortedList = new ArrayList<>();
                this.mSortedList.add(Integer.valueOf(value));
                return;
            }
            addToExistSortList(tag, value);
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.server.mtm.iaware.appmng.rule.RuleNode$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$android$server$mtm$iaware$appmng$rule$RuleParserUtil$AppStartTag = new int[RuleParserUtil.AppStartTag.values().length];

        static {
            try {
                $SwitchMap$com$android$server$mtm$iaware$appmng$rule$RuleParserUtil$AppStartTag[RuleParserUtil.AppStartTag.CALLER_ACTION.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$server$mtm$iaware$appmng$rule$RuleParserUtil$AppStartTag[RuleParserUtil.AppStartTag.CALLER_STATUS.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$server$mtm$iaware$appmng$rule$RuleParserUtil$AppStartTag[RuleParserUtil.AppStartTag.TARGET_STATUS.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$android$server$mtm$iaware$appmng$rule$RuleParserUtil$AppStartTag[RuleParserUtil.AppStartTag.TARGET_TYPE.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    public RuleNode(RuleParserUtil.TagEnum currentType, XmlValue value) {
        this.mCurrentType = currentType;
        this.mValue = value;
    }

    private boolean setChildType(RuleParserUtil.TagEnum childType) {
        RuleParserUtil.TagEnum tagEnum = this.mChildType;
        if (tagEnum != null && !tagEnum.equals(childType)) {
            return false;
        }
        this.mChildType = childType;
        return true;
    }

    private boolean addChildItemInternal(RuleParserUtil.TagEnum childType, RuleNode item, boolean isSorted) {
        if (item == null || !setChildType(childType)) {
            return false;
        }
        XmlValue value = item.getValue();
        if (value == null) {
            return true;
        }
        if (value.isString()) {
            this.mChildItems.put(value.getStringValue(), item);
            return true;
        }
        this.mChildItems.put(value.getIntValue(), item);
        if (!isSorted) {
            return true;
        }
        this.mChildItems.addToSortedList(childType, value.getIntValue());
        return true;
    }

    public boolean addChildItem(RuleParserUtil.TagEnum childType, RuleNode item) {
        return addChildItemInternal(childType, item, false);
    }

    public boolean addChildItemSorted(RuleParserUtil.TagEnum childType, RuleNode item) {
        return addChildItemInternal(childType, item, true);
    }

    public XmlValue getValue() {
        return this.mValue;
    }

    public RuleNode getChild(XmlValue key) {
        return this.mChildItems.get(key);
    }

    public StringIntegerMap getChilds() {
        return this.mChildItems;
    }

    public RuleParserUtil.TagEnum getChildType() {
        return this.mChildType;
    }

    public boolean hasChild() {
        return !this.mChildItems.isEmpty();
    }

    public RuleParserUtil.TagEnum getCurrentType() {
        return this.mCurrentType;
    }
}
