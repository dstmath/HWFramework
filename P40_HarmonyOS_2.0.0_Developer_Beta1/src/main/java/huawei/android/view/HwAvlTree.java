package huawei.android.view;

import android.util.Log;
import java.lang.Comparable;
import java.util.ArrayList;
import java.util.List;

public class HwAvlTree<T extends Comparable<T>> {
    private static final boolean IS_ANIMATOR_DBG = false;
    private static final boolean IS_LEFT_FLAG = false;
    private static final int LIST_DEFAULT_SIZE = 10;
    private static final String TAG = "HwAvlTree";
    private static final int TREE_NODE_HEIGHT_TWO = 2;
    private HwAvlTree<T>.HwAvlTreeNode mRoot = null;

    public class HwAvlTreeNode<T extends Comparable<T>> {
        private int mHeight = 0;
        private T mKey;
        private HwAvlTree<T>.HwAvlTreeNode mLeft;
        private HwAvlTree<T>.HwAvlTreeNode mRight;

        public HwAvlTreeNode(T key, HwAvlTree<T>.HwAvlTreeNode left, HwAvlTree<T>.HwAvlTreeNode right) {
            this.mKey = key;
            this.mLeft = left;
            this.mRight = right;
        }

        public void setKey(T key) {
            this.mKey = key;
        }
    }

    private int getHeight(HwAvlTree<T>.HwAvlTreeNode tree) {
        if (tree != null) {
            return ((HwAvlTreeNode) tree).mHeight;
        }
        return 0;
    }

    public int getHeight() {
        return getHeight(this.mRoot);
    }

    private void preOrder(HwAvlTree<T>.HwAvlTreeNode tree) {
        if (tree != null) {
            Log.d(TAG, "preOrder: " + ((HwAvlTreeNode) tree).mKey);
            preOrder(((HwAvlTreeNode) tree).mLeft);
            preOrder(((HwAvlTreeNode) tree).mRight);
        }
    }

    public void preOrder() {
    }

    private void inOrder(HwAvlTree<T>.HwAvlTreeNode tree) {
        if (tree != null) {
            inOrder(((HwAvlTreeNode) tree).mLeft);
            Log.d(TAG, "inOrder: " + ((HwAvlTreeNode) tree).mKey);
            inOrder(((HwAvlTreeNode) tree).mRight);
        }
    }

    public void inOrder() {
    }

    /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: java.util.List<T extends java.lang.Comparable<T>> */
    /* JADX WARN: Multi-variable type inference failed */
    private void getInOrderNodes(HwAvlTree<T>.HwAvlTreeNode tree, List<T> results) {
        if (tree != null) {
            getInOrderNodes(((HwAvlTreeNode) tree).mLeft, results);
            results.add(((HwAvlTreeNode) tree).mKey);
            getInOrderNodes(((HwAvlTreeNode) tree).mRight, results);
        }
    }

    public List<T> getInOrderNodes() {
        List<T> results = new ArrayList<>((int) LIST_DEFAULT_SIZE);
        getInOrderNodes(this.mRoot, results);
        return results;
    }

    private void postOrder(HwAvlTree<T>.HwAvlTreeNode tree) {
        if (tree != null) {
            postOrder(((HwAvlTreeNode) tree).mLeft);
            postOrder(((HwAvlTreeNode) tree).mRight);
            Log.d(TAG, "postOrder: " + ((HwAvlTreeNode) tree).mKey);
        }
    }

    public void postOrder() {
    }

    /* JADX DEBUG: Type inference failed for r1v1. Raw type applied. Possible types: huawei.android.view.HwAvlTree$HwAvlTreeNode<T extends java.lang.Comparable<T>>, huawei.android.view.HwAvlTree<T>$HwAvlTreeNode<T extends java.lang.Comparable<T>> */
    /* JADX DEBUG: Type inference failed for r1v3. Raw type applied. Possible types: huawei.android.view.HwAvlTree$HwAvlTreeNode<T extends java.lang.Comparable<T>>, huawei.android.view.HwAvlTree<T>$HwAvlTreeNode<T extends java.lang.Comparable<T>> */
    private HwAvlTree<T>.HwAvlTreeNode search(HwAvlTree<T>.HwAvlTreeNode root, T key) {
        if (root == null) {
            return root;
        }
        int cmp = key.compareTo(((HwAvlTreeNode) root).mKey);
        return cmp < 0 ? (HwAvlTreeNode<T>) search(((HwAvlTreeNode) root).mLeft, key) : cmp > 0 ? (HwAvlTreeNode<T>) search(((HwAvlTreeNode) root).mRight, key) : root;
    }

    public HwAvlTree<T>.HwAvlTreeNode search(T key) {
        return search(this.mRoot, key);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r5v0, resolved type: java.util.List<T extends java.lang.Comparable<T>> */
    /* JADX WARN: Multi-variable type inference failed */
    private void searchAllMatchKey(HwAvlTree<T>.HwAvlTreeNode root, T key, List<T> result) {
        if (root != null) {
            if (key.compareTo(((HwAvlTreeNode) root).mKey) == 0) {
                result.add(((HwAvlTreeNode) root).mKey);
            }
            searchAllMatchKey(((HwAvlTreeNode) root).mLeft, key, result);
            searchAllMatchKey(((HwAvlTreeNode) root).mRight, key, result);
        }
    }

    public List<T> searchAllMatchKey(T key) {
        List<T> results = new ArrayList<>((int) LIST_DEFAULT_SIZE);
        searchAllMatchKey(this.mRoot, key, results);
        return results;
    }

    private HwAvlTree<T>.HwAvlTreeNode maximum(HwAvlTree<T>.HwAvlTreeNode tree) {
        HwAvlTree<T>.HwAvlTreeNode treeNode = tree;
        if (treeNode == null) {
            return treeNode;
        }
        while (((HwAvlTreeNode) treeNode).mRight != null) {
            treeNode = ((HwAvlTreeNode) treeNode).mRight;
        }
        return treeNode;
    }

    private HwAvlTree<T>.HwAvlTreeNode leftLeftRotation(HwAvlTree<T>.HwAvlTreeNode root) {
        HwAvlTree<T>.HwAvlTreeNode leftChild = ((HwAvlTreeNode) root).mLeft;
        ((HwAvlTreeNode) root).mLeft = ((HwAvlTreeNode) leftChild).mRight;
        ((HwAvlTreeNode) leftChild).mRight = root;
        ((HwAvlTreeNode) root).mHeight = Math.max(getHeight(((HwAvlTreeNode) root).mLeft), getHeight(((HwAvlTreeNode) root).mRight)) + 1;
        ((HwAvlTreeNode) leftChild).mHeight = Math.max(getHeight(((HwAvlTreeNode) leftChild).mLeft), ((HwAvlTreeNode) root).mHeight) + 1;
        return leftChild;
    }

    private HwAvlTree<T>.HwAvlTreeNode rightRightRotation(HwAvlTree<T>.HwAvlTreeNode root) {
        HwAvlTree<T>.HwAvlTreeNode rightChild = ((HwAvlTreeNode) root).mRight;
        ((HwAvlTreeNode) root).mRight = ((HwAvlTreeNode) rightChild).mLeft;
        ((HwAvlTreeNode) rightChild).mLeft = root;
        ((HwAvlTreeNode) root).mHeight = Math.max(getHeight(((HwAvlTreeNode) root).mLeft), getHeight(((HwAvlTreeNode) root).mRight)) + 1;
        ((HwAvlTreeNode) rightChild).mHeight = Math.max(getHeight(((HwAvlTreeNode) rightChild).mRight), ((HwAvlTreeNode) root).mHeight) + 1;
        return rightChild;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: huawei.android.view.HwAvlTree<T>$HwAvlTreeNode<T extends java.lang.Comparable<T>> */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX DEBUG: Type inference failed for r0v2. Raw type applied. Possible types: huawei.android.view.HwAvlTree$HwAvlTreeNode<T extends java.lang.Comparable<T>>, huawei.android.view.HwAvlTree<T>$HwAvlTreeNode<T extends java.lang.Comparable<T>> */
    private HwAvlTree<T>.HwAvlTreeNode leftRightRotation(HwAvlTree<T>.HwAvlTreeNode root) {
        ((HwAvlTreeNode) root).mLeft = rightRightRotation(((HwAvlTreeNode) root).mLeft);
        return (HwAvlTreeNode<T>) leftLeftRotation(root);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: huawei.android.view.HwAvlTree<T>$HwAvlTreeNode<T extends java.lang.Comparable<T>> */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX DEBUG: Type inference failed for r0v2. Raw type applied. Possible types: huawei.android.view.HwAvlTree$HwAvlTreeNode<T extends java.lang.Comparable<T>>, huawei.android.view.HwAvlTree<T>$HwAvlTreeNode<T extends java.lang.Comparable<T>> */
    private HwAvlTree<T>.HwAvlTreeNode rightLeftRotation(HwAvlTree<T>.HwAvlTreeNode root) {
        ((HwAvlTreeNode) root).mRight = leftLeftRotation(((HwAvlTreeNode) root).mRight);
        return (HwAvlTreeNode<T>) rightRightRotation(root);
    }

    public void insert(T key) {
        this.mRoot = insert(this.mRoot, key);
    }

    private HwAvlTree<T>.HwAvlTreeNode insert(HwAvlTree<T>.HwAvlTreeNode tree, T key) {
        HwAvlTree<T>.HwAvlTreeNode treeNode = tree;
        if (treeNode == null) {
            treeNode = new HwAvlTreeNode<>(key, null, null);
        } else {
            int cmp = key.compareTo(((HwAvlTreeNode) treeNode).mKey);
            if (cmp < 0) {
                ((HwAvlTreeNode) treeNode).mLeft = insert(((HwAvlTreeNode) treeNode).mLeft, key);
                treeNode = adjustBalance(treeNode, key, true);
            } else if (cmp > 0) {
                ((HwAvlTreeNode) treeNode).mRight = insert(((HwAvlTreeNode) treeNode).mRight, key);
                treeNode = adjustBalance(treeNode, key, false);
            } else {
                Log.e(TAG, "insert failed, same node");
            }
        }
        ((HwAvlTreeNode) treeNode).mHeight = Math.max(getHeight(((HwAvlTreeNode) treeNode).mLeft), getHeight(((HwAvlTreeNode) treeNode).mRight)) + 1;
        return treeNode;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r5v0, resolved type: huawei.android.view.HwAvlTree<T>$HwAvlTreeNode<T extends java.lang.Comparable<T>> */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX DEBUG: Type inference failed for r0v1. Raw type applied. Possible types: huawei.android.view.HwAvlTree$HwAvlTreeNode<T extends java.lang.Comparable<T>>, huawei.android.view.HwAvlTree<T>$HwAvlTreeNode<T extends java.lang.Comparable<T>> */
    /* JADX DEBUG: Type inference failed for r0v2. Raw type applied. Possible types: huawei.android.view.HwAvlTree$HwAvlTreeNode<T extends java.lang.Comparable<T>>, huawei.android.view.HwAvlTree<T>$HwAvlTreeNode<T extends java.lang.Comparable<T>> */
    /* JADX DEBUG: Type inference failed for r0v3. Raw type applied. Possible types: huawei.android.view.HwAvlTree$HwAvlTreeNode<T extends java.lang.Comparable<T>>, huawei.android.view.HwAvlTree<T>$HwAvlTreeNode<T extends java.lang.Comparable<T>> */
    /* JADX DEBUG: Type inference failed for r0v4. Raw type applied. Possible types: huawei.android.view.HwAvlTree$HwAvlTreeNode<T extends java.lang.Comparable<T>>, huawei.android.view.HwAvlTree<T>$HwAvlTreeNode<T extends java.lang.Comparable<T>> */
    private HwAvlTree<T>.HwAvlTreeNode adjustBalance(HwAvlTree<T>.HwAvlTreeNode tree, T key, boolean isLeft) {
        if (isLeft) {
            if (getHeight(((HwAvlTreeNode) tree).mLeft) - getHeight(((HwAvlTreeNode) tree).mRight) > 1) {
                return key.compareTo(((HwAvlTreeNode) tree).mLeft.mKey) < 0 ? (HwAvlTreeNode<T>) leftLeftRotation(tree) : (HwAvlTreeNode<T>) leftRightRotation(tree);
            }
            return tree;
        } else if (getHeight(((HwAvlTreeNode) tree).mRight) - getHeight(((HwAvlTreeNode) tree).mLeft) > 1) {
            return key.compareTo(((HwAvlTreeNode) tree).mRight.mKey) > 0 ? (HwAvlTreeNode<T>) rightRightRotation(tree) : (HwAvlTreeNode<T>) rightLeftRotation(tree);
        } else {
            return tree;
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r6v0, resolved type: huawei.android.view.HwAvlTree<T>$HwAvlTreeNode<T extends java.lang.Comparable<T>> */
    /* JADX DEBUG: Multi-variable search result rejected for r7v0, resolved type: huawei.android.view.HwAvlTree<T>$HwAvlTreeNode<T extends java.lang.Comparable<T>> */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX DEBUG: Type inference failed for r0v2. Raw type applied. Possible types: huawei.android.view.HwAvlTree$HwAvlTreeNode<T extends java.lang.Comparable<T>>, huawei.android.view.HwAvlTree<T>$HwAvlTreeNode<T extends java.lang.Comparable<T>> */
    /* JADX DEBUG: Type inference failed for r0v3. Raw type applied. Possible types: huawei.android.view.HwAvlTree$HwAvlTreeNode<T extends java.lang.Comparable<T>>, huawei.android.view.HwAvlTree<T>$HwAvlTreeNode<T extends java.lang.Comparable<T>> */
    /* JADX DEBUG: Type inference failed for r0v4. Raw type applied. Possible types: huawei.android.view.HwAvlTree$HwAvlTreeNode<T extends java.lang.Comparable<T>>, huawei.android.view.HwAvlTree<T>$HwAvlTreeNode<T extends java.lang.Comparable<T>> */
    /* JADX DEBUG: Type inference failed for r0v5. Raw type applied. Possible types: huawei.android.view.HwAvlTree$HwAvlTreeNode<T extends java.lang.Comparable<T>>, huawei.android.view.HwAvlTree<T>$HwAvlTreeNode<T extends java.lang.Comparable<T>> */
    private HwAvlTree<T>.HwAvlTreeNode remove(HwAvlTree<T>.HwAvlTreeNode tree, HwAvlTree<T>.HwAvlTreeNode target) {
        if (tree == 0 || target == 0) {
            return tree;
        }
        int cmp = ((HwAvlTreeNode) target).mKey.compareTo(((HwAvlTreeNode) tree).mKey);
        if (cmp < 0) {
            ((HwAvlTreeNode) tree).mLeft = remove(((HwAvlTreeNode) tree).mLeft, target);
            if (getHeight(((HwAvlTreeNode) tree).mRight) - getHeight(((HwAvlTreeNode) tree).mLeft) != 2) {
                return tree;
            }
            HwAvlTree<T>.HwAvlTreeNode rightNode = ((HwAvlTreeNode) tree).mRight;
            return getHeight(((HwAvlTreeNode) rightNode).mLeft) > getHeight(((HwAvlTreeNode) rightNode).mRight) ? (HwAvlTreeNode<T>) rightLeftRotation(tree) : (HwAvlTreeNode<T>) rightRightRotation(tree);
        } else if (cmp > 0) {
            ((HwAvlTreeNode) tree).mRight = remove(((HwAvlTreeNode) tree).mRight, target);
            if (getHeight(((HwAvlTreeNode) tree).mLeft) - getHeight(((HwAvlTreeNode) tree).mRight) != 2) {
                return tree;
            }
            HwAvlTree<T>.HwAvlTreeNode leftNode = ((HwAvlTreeNode) tree).mLeft;
            return getHeight(((HwAvlTreeNode) leftNode).mRight) > getHeight(((HwAvlTreeNode) leftNode).mLeft) ? (HwAvlTreeNode<T>) leftRightRotation(tree) : (HwAvlTreeNode<T>) leftLeftRotation(tree);
        } else if (((HwAvlTreeNode) tree).mLeft == null || ((HwAvlTreeNode) tree).mRight == null) {
            return ((HwAvlTreeNode) tree).mLeft != null ? ((HwAvlTreeNode) tree).mLeft : ((HwAvlTreeNode) tree).mRight;
        } else if (getHeight(((HwAvlTreeNode) tree).mLeft) > getHeight(((HwAvlTreeNode) tree).mRight)) {
            HwAvlTreeNode<T> maximum = maximum(((HwAvlTreeNode) tree).mLeft);
            ((HwAvlTreeNode) tree).mKey = ((HwAvlTreeNode) maximum).mKey;
            ((HwAvlTreeNode) tree).mLeft = remove(((HwAvlTreeNode) tree).mLeft, maximum);
            return tree;
        } else {
            HwAvlTreeNode<T> maximum2 = maximum(((HwAvlTreeNode) tree).mRight);
            ((HwAvlTreeNode) tree).mKey = ((HwAvlTreeNode) maximum2).mKey;
            ((HwAvlTreeNode) tree).mRight = remove(((HwAvlTreeNode) tree).mRight, maximum2);
            return tree;
        }
    }

    public void remove(T key) {
        HwAvlTreeNode<T> search = search(this.mRoot, key);
        if (search != null) {
            this.mRoot = remove(this.mRoot, search);
        }
    }

    public boolean isEmpty() {
        return this.mRoot == null;
    }
}
