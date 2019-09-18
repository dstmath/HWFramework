package com.android.server.devicepolicy;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.ArrayMap;
import com.android.server.slice.SliceClientPermissions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class PolicyStruct {
    private static final int MAX_PATH_LENGTH = 200;
    private static final String PATH_REGEX = "^[_/]?[A-Za-z0-9]+([-_/]?[A-Za-z0-9]+)*/?$";
    /* access modifiers changed from: private */
    public static final String TAG = PolicyStruct.class.getSimpleName();
    private DevicePolicyPlugin mOwner = null;
    private ArrayMap<String, PolicyItem> policies = new ArrayMap<>();

    public static class PolicyItem {
        public static final int GLOBAL_POLICY_CHANGED = 1;
        public static final int GLOBAL_POLICY_NOT_SET = 0;
        public static final int GLOBAL_POLICY_NO_CHANGE = 2;
        public Bundle attributes = new Bundle();
        public int globalPolicyChanged = 0;
        public PolicyType itemType;
        public ArrayList<PolicyItem> leafItems = new ArrayList<>();
        public String policyName;

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
            String policyTag;
            if (TextUtils.isEmpty(this.policyName)) {
                return null;
            }
            if (this.policyName.indexOf(SliceClientPermissions.SliceAuthority.DELIMITER) == -1 || this.policyName.endsWith(SliceClientPermissions.SliceAuthority.DELIMITER)) {
                policyTag = this.policyName;
            } else {
                policyTag = this.policyName.substring(this.policyName.lastIndexOf(47) + 1, this.policyName.length());
            }
            return policyTag;
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
            if (this.attributes == null) {
                return null;
            }
            return this.attributes.keySet();
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
            Iterator it = otherItem.getAttributes().keySet().iterator();
            while (true) {
                ArrayList<PolicyItem> leafItems2 = null;
                if (it.hasNext()) {
                    String key = (String) it.next();
                    switch (otherItem.getItemType()) {
                        case STATE:
                            getAttributes().putBoolean(key, false);
                            break;
                        case CONFIGURATION:
                            getAttributes().putString(key, null);
                            break;
                        case LIST:
                            getAttributes().putStringArrayList(key, null);
                            break;
                    }
                } else {
                    while (true) {
                        ArrayList<PolicyItem> arrayList = leafItems2;
                        if (otherItem.hasLeafItems()) {
                            leafItems2 = otherItem.getChildItem();
                            Iterator<PolicyItem> it2 = leafItems2.iterator();
                            while (it2.hasNext()) {
                                PolicyItem leaf = it2.next();
                                PolicyItem newItem = new PolicyItem(leaf.getPolicyName());
                                setChildItem(newItem);
                                otherItem = leaf;
                                newItem.copyFrom(leaf);
                            }
                        } else {
                            return;
                        }
                    }
                }
            }
        }

        public void deepCopyFrom(PolicyItem otherItem) {
            this.policyName = otherItem.getPolicyName();
            this.itemType = otherItem.getItemType();
            for (String key : otherItem.getAttributes().keySet()) {
                switch (otherItem.getItemType()) {
                    case STATE:
                        getAttributes().putBoolean(key, otherItem.getAttributes().getBoolean(key));
                        break;
                    case CONFIGURATION:
                        getAttributes().putString(key, otherItem.getAttributes().getString(key));
                        break;
                    case LIST:
                        getAttributes().putStringArrayList(key, otherItem.getAttributes().getStringArrayList(key));
                        break;
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
            ArrayList<PolicyItem> leafItems2;
            Bundle oldBundle = getAttributes();
            Iterator it = oldBundle.keySet().iterator();
            while (true) {
                leafItems2 = null;
                if (!it.hasNext()) {
                    break;
                }
                String key = (String) it.next();
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
            while (true) {
                ArrayList<PolicyItem> arrayList = leafItems2;
                if (item.hasLeafItems()) {
                    leafItems2 = getChildItem();
                    Iterator<PolicyItem> it2 = leafItems2.iterator();
                    while (it2.hasNext()) {
                        PolicyItem leaf = it2.next();
                        item = leaf;
                        leaf.combineAttributes(bundle);
                    }
                } else {
                    return;
                }
            }
        }

        public void updateAttrValues(Bundle newData) {
            if (newData != null && !newData.isEmpty()) {
                PolicyItem item = this;
                Bundle attributes2 = item.getAttributes();
                for (String attrName : attributes2.keySet()) {
                    if (newData.get(attrName) != null) {
                        Object obj = newData.get(attrName);
                        switch (getItemType()) {
                            case STATE:
                                attributes2.putBoolean(attrName, newData.getBoolean(attrName));
                                break;
                            case CONFIGURATION:
                                attributes2.putString(attrName, newData.getString(attrName));
                                break;
                            case LIST:
                                attributes2.putStringArrayList(attrName, newData.getStringArrayList(attrName));
                                break;
                        }
                    }
                }
                while (item.hasLeafItems()) {
                    Iterator<PolicyItem> it = item.getChildItem().iterator();
                    while (it.hasNext()) {
                        item = it.next();
                        item.updateAttrValues(newData);
                    }
                }
            }
        }

        public void addAttrValues(PolicyItem rootItem, Bundle newData) {
            if (newData != null && !newData.isEmpty() && rootItem != null) {
                Bundle attributes2 = rootItem.getAttributes();
                for (String attrName : attributes2.keySet()) {
                    switch (getItemType()) {
                        case STATE:
                            attributes2.putBoolean(attrName, newData.getBoolean(attrName));
                            break;
                        case CONFIGURATION:
                            attributes2.putString(attrName, newData.getString(attrName));
                            break;
                        case LIST:
                            ArrayList<String> originalList = attributes2.getStringArrayList(attrName);
                            if (originalList == null) {
                                originalList = new ArrayList<>();
                            }
                            addListWithoutDuplicate(originalList, newData.getStringArrayList(attrName));
                            attributes2.putStringArrayList(attrName, originalList);
                            break;
                    }
                }
                Iterator<PolicyItem> it = rootItem.getChildItem().iterator();
                while (it.hasNext()) {
                    PolicyItem child = it.next();
                    child.addAttrValues(child, newData);
                }
            }
        }

        public void addListWithoutDuplicate(List<String> originalList, List<String> addList) {
            if (addList != null && originalList != null) {
                Set<String> set = new HashSet<>(originalList);
                for (String str : addList) {
                    if (!TextUtils.isEmpty(str) && set.add(str)) {
                        originalList.add(str);
                    }
                }
            }
        }

        public void removeAttrValues(PolicyItem rootItem, Bundle newData) {
            if (newData != null && !newData.isEmpty() && rootItem != null) {
                Bundle attributes2 = rootItem.getAttributes();
                for (String attrName : attributes2.keySet()) {
                    switch (getItemType()) {
                        case STATE:
                            attributes2.putBoolean(attrName, false);
                            break;
                        case CONFIGURATION:
                            attributes2.putString(attrName, null);
                            break;
                        case LIST:
                            ArrayList<String> originalList = attributes2.getStringArrayList(attrName);
                            if (originalList == null) {
                                originalList = new ArrayList<>();
                            }
                            removeItemsFromList(originalList, newData.getStringArrayList(attrName));
                            break;
                    }
                }
                Iterator<PolicyItem> it = rootItem.getChildItem().iterator();
                while (it.hasNext()) {
                    PolicyItem child = it.next();
                    child.removeAttrValues(child, newData);
                }
            }
        }

        public void removeItemsFromList(List<String> originalList, List<String> removeList) {
            if (originalList != null && removeList != null) {
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
            if (attrNames != null && attrNames.length != 0) {
                for (String attrName : attrNames) {
                    addAttribute(attrName, null);
                }
            }
        }

        public String getAttrValue(String attrName) {
            if (this.attributes == null) {
                return null;
            }
            return this.attributes.getString(attrName);
        }

        public void addAttribute(String attrName, String attrValue) {
            if (this.attributes == null) {
                this.attributes = new Bundle();
            }
            this.attributes.putString(attrName, attrValue);
        }

        public void removeAttrByName(String attrName) {
            if (this.attributes != null) {
                this.attributes.remove(attrName);
            }
        }

        public boolean isAttrValueExists(String attrName) {
            boolean z = false;
            if (this.attributes == null) {
                return false;
            }
            if (this.attributes.get(attrName) != null) {
                z = true;
            }
            return z;
        }

        public boolean isAttrExists(String attrName) {
            if (this.attributes != null) {
                for (String name : getAttrNames()) {
                    if (name != null && name.equals(attrName)) {
                        return true;
                    }
                }
            }
            return false;
        }

        public boolean hasAnyAttribute() {
            if (this.attributes == null) {
                return false;
            }
            return this.attributes.size() > 0;
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
            return this.leafItems != null && this.leafItems.size() > 0;
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
                String access$000 = PolicyStruct.TAG;
                StringBuilder sb = new StringBuilder();
                sb.append("isValidItem- item not correct: ");
                if (item == null) {
                    str = "item is null";
                } else if (!item.hasPolicyName()) {
                    str = "policyName is null";
                } else if (!item.hasPolicyType()) {
                    str = "policy type is null";
                } else {
                    str = " unbelievable result";
                }
                sb.append(str);
                HwLog.d(access$000, sb.toString());
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
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < level; i++) {
                buffer.append("    ");
            }
            buffer.append("|..");
            buffer.append("[" + level + "] ");
            buffer.append(getPolicyTag());
            StringBuilder sb = new StringBuilder();
            sb.append(", ");
            sb.append(getItemType() != null ? getItemType().name() : "no type");
            buffer.append(sb.toString());
            if (getAttributes().size() > 0) {
                buffer.append(", ");
                for (String attrName : getAttributes().keySet()) {
                    Object obj = getAttributes().get(attrName);
                    buffer.append("[" + attrName + ", " + obj + "] ");
                }
            }
            HwLog.d(PolicyStruct.TAG, buffer.toString());
            Iterator<PolicyItem> it = getChildItem().iterator();
            while (it.hasNext()) {
                PolicyItem item = it.next();
                if (item != null) {
                    item.traverse(level + 1);
                } else {
                    return;
                }
            }
        }

        public boolean hasTheSameAttrStruct(PolicyItem that) {
            if (this == that) {
                return true;
            }
            if (this.policyName == null ? that.policyName != null : !this.policyName.equals(that.policyName)) {
                return false;
            }
            if (this.itemType != that.itemType) {
                return false;
            }
            if ((this.attributes != null && that.attributes == null) || (this.attributes == null && that.attributes != null)) {
                return false;
            }
            if (this.attributes != null && this.attributes.size() != that.attributes.size()) {
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
            if (other == null || getClass() != other.getClass()) {
                return false;
            }
            PolicyItem that = (PolicyItem) other;
            if (!hasTheSameAttrStruct(that)) {
                return false;
            }
            if (this.attributes != null) {
                for (String attrName : this.attributes.keySet()) {
                    switch (this.itemType) {
                        case STATE:
                            if (this.attributes.getBoolean(attrName) == that.attributes.getBoolean(attrName)) {
                                break;
                            } else {
                                return false;
                            }
                        case CONFIGURATION:
                            String thisAttrValue = this.attributes.getString(attrName);
                            String thatAttrValue = that.attributes.getString(attrName);
                            if (thisAttrValue == null) {
                                if (thatAttrValue == null) {
                                    break;
                                }
                            } else if (thisAttrValue.equals(thatAttrValue)) {
                                break;
                            }
                            return false;
                        case LIST:
                            ArrayList<String> thisList = this.attributes.getStringArrayList(attrName);
                            ArrayList<String> thatList = that.attributes.getStringArrayList(attrName);
                            if (thisList == null) {
                                if (thatList == null) {
                                    break;
                                }
                            } else if (thisList.equals(thatList)) {
                                break;
                            }
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
                            if (i < thatItems.size()) {
                                if (thisItem.getPolicyName().equals(thatItems.get(i).getPolicyName())) {
                                    thisParent = thisItem;
                                    thatParent = thatItems.get(i);
                                    found = true;
                                    if (!thisItem.equals(thatItems.get(i))) {
                                        return false;
                                    }
                                } else {
                                    i++;
                                }
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

        public int hashCode() {
            int i = 0;
            int hashCode = 31 * (this.policyName != null ? this.policyName.hashCode() : 0);
            if (this.itemType != null) {
                i = this.itemType.hashCode();
            }
            return hashCode + i;
        }
    }

    public enum PolicyType {
        STATE,
        LIST,
        CONFIGURATION
    }

    public PolicyStruct(DevicePolicyPlugin owner) {
        this.mOwner = owner;
    }

    public PolicyStruct(DevicePolicyPlugin owner, PolicyType type, String path, String... attributes) {
        this.mOwner = owner;
        addStruct(path, type, attributes);
    }

    public boolean hasPolicy() {
        return this.policies != null && this.policies.size() > 0;
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
        if (this.policies == null || !this.policies.containsKey(name)) {
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
        PolicyItem root;
        if (path == null || path.isEmpty() || type == null || !path.matches(PATH_REGEX) || path.length() > 200) {
            String str = TAG;
            HwLog.e(str, "createTree-path is invalid: " + path);
            return null;
        } else if (path.indexOf(SliceClientPermissions.SliceAuthority.DELIMITER) == -1) {
            PolicyItem root2 = new PolicyItem(path, type);
            root2.addAttributes(attrNames);
            return root2;
        } else if (path.equals(SliceClientPermissions.SliceAuthority.DELIMITER)) {
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
            return root;
        }
    }

    public Collection<PolicyItem> getPolicyItems() {
        if (this.policies == null || this.policies.isEmpty()) {
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

    public void updateAttrValues(String policyName, Bundle newData) {
        PolicyItem item = getItemByPolicyName(policyName);
        if (item != null) {
            boolean hasNonNullAttrValue = false;
            if (newData != null && !newData.isEmpty()) {
                Iterator it = newData.keySet().iterator();
                while (true) {
                    if (it.hasNext()) {
                        if (newData.get((String) it.next()) != null) {
                            hasNonNullAttrValue = true;
                            break;
                        }
                    } else {
                        break;
                    }
                }
            }
            if (hasNonNullAttrValue) {
                item.updateAttrValues(newData);
                return;
            }
            String str = TAG;
            HwLog.d(str, "updateAttrValues: " + item.getPolicyName() + " nothing to update");
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00a3, code lost:
        if (r4.hasLeafItems() == false) goto L_0x00ab;
     */
    public PolicyItem addStruct(String path, PolicyType type, String... attrNames) {
        PolicyItem createdRoot = createTree(path, type, attrNames);
        if (createdRoot == null) {
            HwLog.e(TAG, "add struct error");
            return null;
        }
        PolicyItem oldRoot = getPolicyItem(createdRoot.getPolicyName());
        if (oldRoot == null) {
            addPolicyItem(createdRoot);
            return createdRoot;
        }
        if (!oldRoot.hasAnyAttribute() || createdRoot.hasAnyAttribute()) {
            oldRoot.setAttributes(createdRoot.getAttributes());
        }
        PolicyItem otherItem = createdRoot;
        PolicyItem otherParentItem = createdRoot;
        PolicyItem thisParentItem = oldRoot;
        PolicyItem thisItem = oldRoot;
        int sameDepth = 1;
        do {
            Iterator<PolicyItem> it = otherItem.getChildItem().iterator();
            while (true) {
                int i = 0;
                if (!it.hasNext()) {
                    break;
                }
                PolicyItem createdItem = it.next();
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
        } while (otherItem.hasLeafItems());
        int SingleLinkDepth = 1;
        PolicyItem sL = createdRoot;
        while (sL.hasLeafItems()) {
            sL = sL.getChildItem().get(0);
            SingleLinkDepth++;
        }
        if (sameDepth < SingleLinkDepth) {
            thisParentItem.setChildItem(otherParentItem.getChildItem().get(0));
        }
        return oldRoot;
    }
}
