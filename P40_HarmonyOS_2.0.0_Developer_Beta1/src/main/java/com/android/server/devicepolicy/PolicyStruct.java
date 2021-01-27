package com.android.server.devicepolicy;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.ArrayMap;
import com.android.server.devicepolicy.DevicePolicyPlugin;
import com.android.server.slice.SliceClientPermissions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class PolicyStruct {
    private static final int MAX_PATH_LENGTH = 200;
    private static final String PATH_REGEX = "^[_/]?[A-Za-z0-9]+([-_/]?[A-Za-z0-9]+)*/?$";
    private static final String TAG = PolicyStruct.class.getSimpleName();
    private DevicePolicyPlugin mOwner = null;
    private ArrayMap<String, PolicyItem> policies = new ArrayMap<>();

    public enum PolicyType {
        STATE,
        LIST,
        CONFIGURATION,
        CONFIGLIST
    }

    public PolicyStruct(DevicePolicyPlugin owner) {
        this.mOwner = owner;
    }

    public PolicyStruct(DevicePolicyPlugin owner, PolicyType type, String path, String... attributes) {
        this.mOwner = owner;
        addStruct(path, type, attributes);
    }

    public boolean hasPolicy() {
        ArrayMap<String, PolicyItem> arrayMap = this.policies;
        return arrayMap != null && arrayMap.size() > 0;
    }

    public PolicyItem getPolicyItem(String policyName) {
        if (this.policies == null) {
            this.policies = new ArrayMap<>();
        }
        return this.policies.get(policyName);
    }

    public boolean addPolicyItem(PolicyItem item) {
        if (this.policies == null) {
            this.policies = new ArrayMap<>();
        }
        if (!PolicyItem.isValidItem(item)) {
            return false;
        }
        this.policies.put(item.getPolicyName(), item);
        return true;
    }

    public DevicePolicyPlugin getOwner() {
        return this.mOwner;
    }

    public boolean containsPolicyName(String name) {
        ArrayMap<String, PolicyItem> arrayMap = this.policies;
        if (arrayMap == null || !arrayMap.containsKey(name)) {
            return false;
        }
        return true;
    }

    public ArrayMap<String, PolicyItem> getPolicyMap() {
        if (this.policies == null) {
            this.policies = new ArrayMap<>();
        }
        return this.policies;
    }

    public static PolicyItem createTree(String path, PolicyType type, String... attrNames) {
        return createTree(path, type, false, attrNames);
    }

    public static PolicyItem createTree(String path, PolicyType type, boolean supportMultipleUsers, String... attrNames) {
        PolicyItem root;
        if (path == null || path.isEmpty() || type == null || !path.matches(PATH_REGEX) || path.length() > 200) {
            String str = TAG;
            HwLog.e(str, "createTree-path is invalid: " + path);
            return null;
        } else if (path.indexOf(SliceClientPermissions.SliceAuthority.DELIMITER) == -1) {
            PolicyItem root2 = new PolicyItem(path, type);
            root2.addAttributes(attrNames);
            root2.setSuppportMultipleUsers(supportMultipleUsers);
            return root2;
        } else if (SliceClientPermissions.SliceAuthority.DELIMITER.equals(path)) {
            return null;
        } else {
            if (path.startsWith(SliceClientPermissions.SliceAuthority.DELIMITER)) {
                path = path.substring(1);
                if (path.contains(SliceClientPermissions.SliceAuthority.DELIMITER)) {
                    root = new PolicyItem(path.substring(0, path.indexOf(SliceClientPermissions.SliceAuthority.DELIMITER)), type);
                } else {
                    root = new PolicyItem(path, type);
                }
            } else {
                root = new PolicyItem(path.substring(0, path.indexOf(SliceClientPermissions.SliceAuthority.DELIMITER)), type);
            }
            PolicyItem node = root;
            for (int i = path.indexOf(SliceClientPermissions.SliceAuthority.DELIMITER) + 1; i < path.length(); i++) {
                if (path.charAt(i) == '/') {
                    PolicyItem newItem = new PolicyItem(path.substring(0, i), type);
                    node.setChildItem(newItem);
                    node = newItem;
                }
            }
            if (path.lastIndexOf(SliceClientPermissions.SliceAuthority.DELIMITER) != path.length() - 1) {
                PolicyItem lastNode = new PolicyItem(path, type);
                lastNode.addAttributes(attrNames);
                node.setChildItem(lastNode);
            } else {
                node.addAttributes(attrNames);
            }
            root.setSuppportMultipleUsers(supportMultipleUsers);
            return root;
        }
    }

    public Collection<PolicyItem> getPolicyItems() {
        ArrayMap<String, PolicyItem> arrayMap = this.policies;
        if (arrayMap == null || arrayMap.isEmpty()) {
            return null;
        }
        return this.policies.values();
    }

    public PolicyItem getItemByPolicyName(String policyName) {
        if (containsPolicyName(policyName)) {
            return this.policies.get(policyName);
        }
        return null;
    }

    public Bundle getAttributesByPolicyName(String policyName) {
        PolicyItem item = getItemByPolicyName(policyName);
        if (item != null) {
            return item.getAttributes();
        }
        return null;
    }

    public PolicyItem addStruct(String path, PolicyType type, String... attrNames) {
        return addStruct(path, type, false, attrNames);
    }

    public PolicyItem addStruct(String path, PolicyType type, boolean supportMultipleUsers, String... attrNames) {
        PolicyItem createdRoot = createTree(path, type, supportMultipleUsers, attrNames);
        if (createdRoot == null) {
            HwLog.e(TAG, "add struct error");
            return null;
        }
        createdRoot.setPolicyStruct(this);
        PolicyItem oldRoot = getPolicyItem(createdRoot.getPolicyName());
        if (oldRoot == null) {
            addPolicyItem(createdRoot);
            return createdRoot;
        }
        if (!oldRoot.hasAnyAttribute() || createdRoot.hasAnyAttribute()) {
            oldRoot.setAttributes(createdRoot.getAttributes());
        }
        PolicyItem thisItem = oldRoot;
        PolicyItem thisParentItem = oldRoot;
        PolicyItem otherItem = createdRoot;
        PolicyItem otherParentItem = createdRoot;
        int sameDepth = 1;
        do {
            Iterator<PolicyItem> it = otherItem.getChildItem().iterator();
            while (it.hasNext()) {
                PolicyItem createdItem = it.next();
                int i = 0;
                while (true) {
                    if (i >= thisItem.getChildItem().size()) {
                        break;
                    }
                    PolicyItem oldItem = thisItem.getChildItem().get(i);
                    if (oldItem.getPolicyName().equals(createdItem.getPolicyName())) {
                        sameDepth++;
                        if (!oldItem.hasAnyAttribute() || createdItem.hasAnyAttribute()) {
                            oldItem.setAttributes(createdItem.getAttributes());
                        }
                        thisParentItem = thisItem;
                        thisItem = oldItem;
                        otherParentItem = otherItem;
                        otherItem = createdItem;
                    } else if (i == thisItem.getChildItem().size() - 1) {
                        thisParentItem.setChildItem(createdItem);
                        return oldRoot;
                    } else {
                        i++;
                    }
                }
            }
            if (!thisItem.hasLeafItems()) {
                break;
            }
        } while (otherItem.hasLeafItems());
        updateParentItem(createdRoot, thisParentItem, otherParentItem, sameDepth);
        return oldRoot;
    }

    private void updateParentItem(PolicyItem createdRoot, PolicyItem thisParentItem, PolicyItem otherParentItem, int sameDepth) {
        int singleLinkDepth = 1;
        PolicyItem rootItem = createdRoot;
        while (rootItem.hasLeafItems()) {
            rootItem = rootItem.getChildItem().get(0);
            singleLinkDepth++;
        }
        if (sameDepth < singleLinkDepth) {
            thisParentItem.setChildItem(otherParentItem.getChildItem().get(0));
        }
    }

    public static class PolicyItem {
        public static final int GLOBAL_POLICY_CHANGED = 1;
        public static final int GLOBAL_POLICY_NOT_SET = 0;
        public static final int GLOBAL_POLICY_NO_CHANGE = 2;
        public Bundle attributes = new Bundle();
        public int globalPolicyChanged = 0;
        public PolicyType itemType;
        public ArrayList<PolicyItem> leafItems = new ArrayList<>();
        private PolicyStruct mWhoCreateMe = null;
        public String policyName;
        public boolean suppportMultipleUsers = false;

        public PolicyItem(String name) {
            this.policyName = name;
        }

        public PolicyItem(String policyName2, PolicyType itemType2) {
            if (policyName2 != null && policyName2.endsWith(SliceClientPermissions.SliceAuthority.DELIMITER)) {
                policyName2 = policyName2.substring(0, policyName2.length() - 1);
            }
            this.policyName = policyName2;
            this.itemType = itemType2;
        }

        public PolicyItem(String policyName2, PolicyType itemType2, PolicyStruct policyStruct) {
            if (policyName2 != null && policyName2.endsWith(SliceClientPermissions.SliceAuthority.DELIMITER)) {
                policyName2 = policyName2.substring(0, policyName2.length() - 1);
            }
            this.policyName = policyName2;
            this.itemType = itemType2;
            this.mWhoCreateMe = policyStruct;
        }

        public void setPolicyStruct(PolicyStruct policyStruct) {
            this.mWhoCreateMe = policyStruct;
        }

        public PolicyStruct getPolicyStruct() {
            return this.mWhoCreateMe;
        }

        public boolean isSuppportMultipleUsers() {
            return this.suppportMultipleUsers;
        }

        public void setSuppportMultipleUsers(boolean suppportMultipleUsers2) {
            this.suppportMultipleUsers = suppportMultipleUsers2;
        }

        public boolean isGlobalPolicySet() {
            return this.globalPolicyChanged != 0;
        }

        public boolean isGlobalPolicyChanged() {
            return this.globalPolicyChanged == 1;
        }

        public void setGlobalPolicyChanged(int changeState) {
            this.globalPolicyChanged = changeState;
        }

        public String getPolicyName() {
            return this.policyName;
        }

        public String getPolicyTag() {
            if (TextUtils.isEmpty(this.policyName)) {
                return null;
            }
            if (this.policyName.indexOf(SliceClientPermissions.SliceAuthority.DELIMITER) == -1 || this.policyName.endsWith(SliceClientPermissions.SliceAuthority.DELIMITER)) {
                return this.policyName;
            }
            String str = this.policyName;
            return str.substring(str.lastIndexOf(47) + 1, this.policyName.length());
        }

        public PolicyType getItemType() {
            return this.itemType;
        }

        public Bundle getAttributes() {
            if (this.attributes == null) {
                this.attributes = new Bundle();
            }
            return this.attributes;
        }

        public Set<String> getAttrNames() {
            Bundle bundle = this.attributes;
            if (bundle == null) {
                return null;
            }
            return bundle.keySet();
        }

        public ArrayList<PolicyItem> getChildItem() {
            if (this.leafItems == null) {
                this.leafItems = new ArrayList<>();
            }
            return this.leafItems;
        }

        public void setChildItem(PolicyItem child) {
            if (this.leafItems == null) {
                this.leafItems = new ArrayList<>();
            }
            this.leafItems.add(child);
        }

        public void setAttributes(Bundle attributes2) {
            this.attributes = attributes2;
        }

        public void copyFrom(PolicyItem otherItem) {
            this.policyName = otherItem.getPolicyName();
            this.itemType = otherItem.getItemType();
            this.suppportMultipleUsers = otherItem.isSuppportMultipleUsers();
            for (String key : otherItem.getAttributes().keySet()) {
                int i = AnonymousClass1.$SwitchMap$com$android$server$devicepolicy$PolicyStruct$PolicyType[otherItem.getItemType().ordinal()];
                if (i == 1) {
                    getAttributes().putBoolean(key, false);
                } else if (i == 2) {
                    getAttributes().putString(key, null);
                } else if (i == 3 || i == 4) {
                    getAttributes().putStringArrayList(key, null);
                }
            }
            while (otherItem.hasLeafItems()) {
                Iterator<PolicyItem> it = otherItem.getChildItem().iterator();
                while (it.hasNext()) {
                    PolicyItem leaf = it.next();
                    PolicyItem newItem = new PolicyItem(leaf.getPolicyName());
                    setChildItem(newItem);
                    otherItem = leaf;
                    newItem.copyFrom(leaf);
                }
            }
        }

        public void deepCopyFrom(PolicyItem otherItem) {
            this.policyName = otherItem.getPolicyName();
            this.itemType = otherItem.getItemType();
            this.suppportMultipleUsers = otherItem.isSuppportMultipleUsers();
            for (String key : otherItem.getAttributes().keySet()) {
                int i = AnonymousClass1.$SwitchMap$com$android$server$devicepolicy$PolicyStruct$PolicyType[otherItem.getItemType().ordinal()];
                if (i == 1) {
                    getAttributes().putBoolean(key, otherItem.getAttributes().getBoolean(key));
                } else if (i == 2) {
                    getAttributes().putString(key, otherItem.getAttributes().getString(key));
                } else if (i == 3 || i == 4) {
                    ArrayList<String> otherItemStringArrayList = new ArrayList<>();
                    otherItemStringArrayList.addAll(otherItem.getAttributes().getStringArrayList(key));
                    getAttributes().putStringArrayList(key, otherItemStringArrayList);
                }
            }
            while (otherItem.hasLeafItems()) {
                Iterator<PolicyItem> it = otherItem.getChildItem().iterator();
                while (it.hasNext()) {
                    PolicyItem leaf = it.next();
                    PolicyItem newItem = new PolicyItem(leaf.getPolicyName());
                    setChildItem(newItem);
                    otherItem = leaf;
                    newItem.deepCopyFrom(leaf);
                }
            }
        }

        public Bundle combineAllAttributes() {
            Bundle bundle = new Bundle();
            combineAttributes(bundle);
            return bundle;
        }

        private void combineAttributes(Bundle bundle) {
            Bundle oldBundle = getAttributes();
            for (String key : oldBundle.keySet()) {
                Object obj = oldBundle.get(key);
                if (obj instanceof ArrayList) {
                    bundle.putStringArrayList(key, (ArrayList) obj);
                } else if (obj instanceof String) {
                    bundle.putString(key, (String) obj);
                } else if (obj instanceof Boolean) {
                    bundle.putBoolean(key, ((Boolean) obj).booleanValue());
                } else {
                    bundle.putString(key, null);
                }
            }
            PolicyItem item = this;
            while (item.hasLeafItems()) {
                Iterator<PolicyItem> it = getChildItem().iterator();
                while (it.hasNext()) {
                    PolicyItem leaf = it.next();
                    item = leaf;
                    leaf.combineAttributes(bundle);
                }
            }
        }

        public void addAttrValues(PolicyItem rootItem, Bundle newData) {
            if (!(newData == null || newData.isEmpty() || rootItem == null)) {
                Bundle rootAttributes = rootItem.getAttributes();
                for (String attrName : rootAttributes.keySet()) {
                    int i = AnonymousClass1.$SwitchMap$com$android$server$devicepolicy$PolicyStruct$PolicyType[getItemType().ordinal()];
                    if (i == 1) {
                        rootAttributes.putBoolean(attrName, newData.getBoolean(attrName));
                    } else if (i == 2) {
                        rootAttributes.putString(attrName, newData.getString(attrName));
                    } else if (i == 3) {
                        ArrayList<String> originalList = rootAttributes.getStringArrayList(attrName);
                        if (originalList == null) {
                            originalList = new ArrayList<>();
                        }
                        addListWithoutDuplicate(originalList, newData.getStringArrayList(attrName));
                        rootAttributes.putStringArrayList(attrName, originalList);
                    } else if (i == 4) {
                        ArrayList<String> currentConfigurationList = rootAttributes.getStringArrayList(attrName);
                        if (currentConfigurationList == null) {
                            currentConfigurationList = new ArrayList<>();
                        }
                        addAndUpdateConfigurationList(currentConfigurationList, newData.getStringArrayList(attrName));
                        rootAttributes.putStringArrayList(attrName, currentConfigurationList);
                    }
                }
                Iterator<PolicyItem> it = rootItem.getChildItem().iterator();
                while (it.hasNext()) {
                    PolicyItem child = it.next();
                    child.mWhoCreateMe = rootItem.mWhoCreateMe;
                    child.addAttrValues(child, newData);
                }
            }
        }

        public void addAndUpdateConfigurationList(List<String> originalList, List<String> addList) {
            if (!(addList == null || originalList == null)) {
                if (this.mWhoCreateMe != null) {
                    HashMap originalKeyMap = new HashMap();
                    HashMap addedKeyMap = new HashMap();
                    DevicePolicyPlugin.IPolicyItemKeyGetter keyGetter = this.mWhoCreateMe.getOwner().getKeyGetter(getPolicyName());
                    if (keyGetter != null) {
                        for (String item : originalList) {
                            originalKeyMap.put(keyGetter.getKey(item), item);
                        }
                        for (String item2 : addList) {
                            addedKeyMap.put(keyGetter.getKey(item2), item2);
                        }
                        HashMap combinedResult = new HashMap();
                        combinedResult.putAll(originalKeyMap);
                        combinedResult.putAll(addedKeyMap);
                        originalList.clear();
                        originalList.addAll(combinedResult.values());
                        return;
                    }
                    HwLog.e(PolicyStruct.TAG, "addAndUpdateConfigurationList error: no valid keyGetter");
                    throw new RuntimeException("addAndUpdateConfigurationList error:no valid keyGetter");
                }
                HwLog.e(PolicyStruct.TAG, "addAndUpdateConfigurationList error: instance of policystruct not set");
                throw new RuntimeException("addAndUpdateConfigurationList instance of policystruct not set");
            }
        }

        public void addListWithoutDuplicate(List<String> originalList, List<String> addList) {
            if (!(addList == null || originalList == null)) {
                Set<String> set = new HashSet<>(originalList);
                for (String str : addList) {
                    if (!TextUtils.isEmpty(str) && set.add(str)) {
                        originalList.add(str);
                    }
                }
            }
        }

        public void removeAttrValues(PolicyItem rootItem, Bundle newData) {
            if (!(newData == null || newData.isEmpty() || rootItem == null)) {
                Bundle rootAttributes = rootItem.getAttributes();
                for (String attrName : rootAttributes.keySet()) {
                    int i = AnonymousClass1.$SwitchMap$com$android$server$devicepolicy$PolicyStruct$PolicyType[getItemType().ordinal()];
                    if (i == 1) {
                        rootAttributes.putBoolean(attrName, false);
                    } else if (i == 2) {
                        rootAttributes.putString(attrName, null);
                    } else if (i == 3) {
                        ArrayList<String> originalList = rootAttributes.getStringArrayList(attrName);
                        if (originalList == null) {
                            originalList = new ArrayList<>();
                        }
                        removeItemsFromList(originalList, newData.getStringArrayList(attrName));
                    } else if (i == 4) {
                        ArrayList<String> currentList = rootAttributes.getStringArrayList(attrName);
                        if (currentList == null) {
                            currentList = new ArrayList<>();
                        }
                        removeItemsByKey(currentList, newData.getStringArrayList(attrName));
                    }
                }
                Iterator<PolicyItem> it = rootItem.getChildItem().iterator();
                while (it.hasNext()) {
                    PolicyItem child = it.next();
                    child.mWhoCreateMe = this.mWhoCreateMe;
                    child.removeAttrValues(child, newData);
                }
            }
        }

        private void removeItemsByKey(List<String> originalList, List<String> removeList) {
            if (!(removeList == null || originalList == null)) {
                if (this.mWhoCreateMe != null) {
                    HashMap originalKeyMap = new HashMap();
                    DevicePolicyPlugin.IPolicyItemKeyGetter keyGetter = this.mWhoCreateMe.getOwner().getKeyGetter(getPolicyName());
                    if (keyGetter != null) {
                        for (String item : originalList) {
                            originalKeyMap.put(keyGetter.getKey(item), item);
                        }
                        for (String item2 : removeList) {
                            originalKeyMap.remove(keyGetter.getKey(item2));
                        }
                        originalList.clear();
                        originalList.addAll(originalKeyMap.values());
                        return;
                    }
                    HwLog.e(PolicyStruct.TAG, "removeItemsByKey error: no valid keyGetter");
                    throw new RuntimeException("removeItemsByKey error:no valid keyGetter");
                }
                HwLog.e(PolicyStruct.TAG, "removeItemsByKey error: instance of policystruct not set");
                throw new RuntimeException("removeItemsByKey instance of policystruct not set");
            }
        }

        public void removeItemsFromList(List<String> originalList, List<String> removeList) {
            if (!(originalList == null || removeList == null)) {
                Set<String> removeSet = new HashSet<>(removeList);
                List<String> newList = new ArrayList<>();
                for (String str : originalList) {
                    if (!removeSet.contains(str)) {
                        newList.add(str);
                    }
                }
                originalList.clear();
                originalList.addAll(newList);
            }
        }

        public void addAttributes(String... attrNames) {
            if (!(attrNames == null || attrNames.length == 0)) {
                for (String attrName : attrNames) {
                    addAttribute(attrName, null);
                }
            }
        }

        public String getAttrValue(String attrName) {
            Bundle bundle = this.attributes;
            if (bundle == null) {
                return null;
            }
            return bundle.getString(attrName);
        }

        public void addAttribute(String attrName, String attrValue) {
            if (this.attributes == null) {
                this.attributes = new Bundle();
            }
            this.attributes.putString(attrName, attrValue);
        }

        public void removeAttrByName(String attrName) {
            Bundle bundle = this.attributes;
            if (bundle != null) {
                bundle.remove(attrName);
            }
        }

        public boolean isAttrValueExists(String attrName) {
            Bundle bundle = this.attributes;
            if (bundle == null || bundle.get(attrName) == null) {
                return false;
            }
            return true;
        }

        public boolean isAttrExists(String attrName) {
            if (this.attributes == null) {
                return false;
            }
            for (String name : getAttrNames()) {
                if (name != null && name.equals(attrName)) {
                    return true;
                }
            }
            return false;
        }

        public boolean hasAnyAttribute() {
            Bundle bundle = this.attributes;
            if (bundle == null) {
                return false;
            }
            return bundle.size() > 0;
        }

        public boolean hasAnyNonNullAttribute() {
            PolicyItem item = this;
            Bundle attrs = item.getAttributes();
            for (String key : attrs.keySet()) {
                if (attrs.get(key) != null) {
                    return true;
                }
            }
            while (item.hasLeafItems()) {
                Iterator<PolicyItem> it = item.getChildItem().iterator();
                while (true) {
                    if (it.hasNext()) {
                        item = it.next();
                        if (item.hasAnyNonNullAttribute()) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        public boolean hasLeafItems() {
            ArrayList<PolicyItem> arrayList = this.leafItems;
            return arrayList != null && arrayList.size() > 0;
        }

        public boolean hasPolicyName() {
            return getPolicyName() != null && !getPolicyName().isEmpty();
        }

        public boolean hasPolicyType() {
            return this.itemType != null;
        }

        public static boolean isValidItem(PolicyItem item) {
            String str;
            if (item == null || !item.hasPolicyName() || !item.hasPolicyType()) {
                String str2 = PolicyStruct.TAG;
                StringBuilder sb = new StringBuilder();
                sb.append("isValidItem- item not correct: ");
                if (item == null) {
                    str = "item is null";
                } else if (!item.hasPolicyName()) {
                    str = "policyName is null";
                } else {
                    str = !item.hasPolicyType() ? "policy type is null" : " unbelievable result";
                }
                sb.append(str);
                HwLog.d(str2, sb.toString());
                return false;
            }
            PolicyItem node = item;
            while (node.hasLeafItems()) {
                Iterator<PolicyItem> it = node.getChildItem().iterator();
                while (true) {
                    if (it.hasNext()) {
                        node = it.next();
                        if (!isValidItem(node)) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }

        public void traverse(int level) {
            PolicyItem item;
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < level; i++) {
                buffer.append("    ");
            }
            buffer.append("|..");
            String levelStr = "[" + level + "] ";
            StringBuilder sb = new StringBuilder();
            sb.append(", ");
            sb.append(getItemType() != null ? getItemType().name() : "no type");
            String typeStr = sb.toString();
            buffer.append(levelStr);
            buffer.append(getPolicyTag());
            buffer.append(typeStr);
            if (getAttributes().size() > 0) {
                buffer.append(", ");
                for (String attrName : getAttributes().keySet()) {
                    buffer.append("[" + attrName + ", " + getAttributes().get(attrName) + "] ");
                }
            }
            HwLog.d(PolicyStruct.TAG, buffer.toString());
            Iterator<PolicyItem> it = getChildItem().iterator();
            while (it.hasNext() && (item = it.next()) != null) {
                item.traverse(level + 1);
            }
        }

        public boolean hasTheSameAttrStruct(PolicyItem that) {
            if (this == that) {
                return true;
            }
            String str = this.policyName;
            if (str == null ? that.policyName != null : !str.equals(that.policyName)) {
                return false;
            }
            if (this.itemType != that.itemType) {
                return false;
            }
            if ((this.attributes != null && that.attributes == null) || (this.attributes == null && that.attributes != null)) {
                return false;
            }
            Bundle bundle = this.attributes;
            if (!(bundle == null || bundle.size() == that.attributes.size())) {
                return false;
            }
            if (this.attributes != null) {
                for (String thatAttrName : that.attributes.keySet()) {
                    if (!this.attributes.containsKey(thatAttrName)) {
                        return false;
                    }
                }
            }
            ArrayList<PolicyItem> thisItems = getChildItem();
            ArrayList<PolicyItem> thatItems = that.getChildItem();
            if (thisItems.size() != thatItems.size()) {
                return false;
            }
            PolicyItem thisParent = this;
            PolicyItem thatParent = that;
            while (thisParent.hasLeafItems() && thatParent.hasLeafItems()) {
                Iterator<PolicyItem> it = thisItems.iterator();
                while (true) {
                    if (it.hasNext()) {
                        PolicyItem thisItem = it.next();
                        boolean found = false;
                        int i = 0;
                        while (true) {
                            if (i >= thatItems.size()) {
                                break;
                            } else if (thisItem.getPolicyName().equals(thatItems.get(i).getPolicyName())) {
                                thisParent = thisItem;
                                thatParent = thatItems.get(i);
                                found = true;
                                if (!thisItem.hasTheSameAttrStruct(thatItems.get(i))) {
                                    return false;
                                }
                            } else {
                                i++;
                            }
                        }
                        if (i == thatItems.size() - 1 && !found) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }

        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (other == null || getClass() != other.getClass() || !(other instanceof PolicyItem)) {
                return false;
            }
            PolicyItem that = (PolicyItem) other;
            if (!hasTheSameAttrStruct(that)) {
                return false;
            }
            Bundle bundle = this.attributes;
            if (bundle != null) {
                for (String attrName : bundle.keySet()) {
                    int i = AnonymousClass1.$SwitchMap$com$android$server$devicepolicy$PolicyStruct$PolicyType[this.itemType.ordinal()];
                    if (i != 1) {
                        if (i == 2) {
                            String thisAttrValue = this.attributes.getString(attrName);
                            String thatAttrValue = that.attributes.getString(attrName);
                            if (thisAttrValue != null) {
                                if (!thisAttrValue.equals(thatAttrValue)) {
                                }
                            } else if (thatAttrValue != null) {
                            }
                            return false;
                        } else if (i == 3 || i == 4) {
                            ArrayList<String> thisList = this.attributes.getStringArrayList(attrName);
                            ArrayList<String> thatList = that.attributes.getStringArrayList(attrName);
                            if (thisList != null) {
                                if (!thisList.equals(thatList)) {
                                }
                            } else if (thatList != null) {
                            }
                            return false;
                        }
                    } else if (this.attributes.getBoolean(attrName) != that.attributes.getBoolean(attrName)) {
                        return false;
                    }
                }
            }
            ArrayList<PolicyItem> thisItems = getChildItem();
            ArrayList<PolicyItem> thatItems = that.getChildItem();
            if (thisItems.size() != thatItems.size()) {
                return false;
            }
            PolicyItem thisParent = this;
            PolicyItem thatParent = that;
            while (thisParent.hasLeafItems() && thatParent.hasLeafItems()) {
                Iterator<PolicyItem> it = thisItems.iterator();
                while (true) {
                    if (it.hasNext()) {
                        PolicyItem thisItem = it.next();
                        boolean found = false;
                        int i2 = 0;
                        while (true) {
                            if (i2 >= thatItems.size()) {
                                break;
                            } else if (thisItem.getPolicyName().equals(thatItems.get(i2).getPolicyName())) {
                                thisParent = thisItem;
                                thatParent = thatItems.get(i2);
                                found = true;
                                if (!thisItem.equals(thatItems.get(i2))) {
                                    return false;
                                }
                            } else {
                                i2++;
                            }
                        }
                        if (i2 == thatItems.size() - 1 && !found) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }

        public int hashCode() {
            String str = this.policyName;
            int i = 0;
            int hashCode = (str != null ? str.hashCode() : 0) * 31;
            PolicyType policyType = this.itemType;
            if (policyType != null) {
                i = policyType.hashCode();
            }
            return hashCode + i;
        }
    }

    /* renamed from: com.android.server.devicepolicy.PolicyStruct$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$android$server$devicepolicy$PolicyStruct$PolicyType = new int[PolicyType.values().length];

        static {
            try {
                $SwitchMap$com$android$server$devicepolicy$PolicyStruct$PolicyType[PolicyType.STATE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$server$devicepolicy$PolicyStruct$PolicyType[PolicyType.CONFIGURATION.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$server$devicepolicy$PolicyStruct$PolicyType[PolicyType.LIST.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$android$server$devicepolicy$PolicyStruct$PolicyType[PolicyType.CONFIGLIST.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }
}
