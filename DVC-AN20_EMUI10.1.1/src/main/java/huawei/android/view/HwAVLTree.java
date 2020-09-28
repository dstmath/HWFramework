package huawei.android.view;

import android.util.Log;
import java.lang.Comparable;
import java.util.ArrayList;
import java.util.List;

public class HwAVLTree<T extends Comparable<T>> {
    private static final boolean IS_ANIMATOR_DBG = false;
    private static final String TAG = "HwAVLTree";
    private HwAVLTree<T>.HwAVLTreeNode mRoot = null;

    public class HwAVLTreeNode<T extends Comparable<T>> {
        private int height = 0;
        private T key;
        private HwAVLTree<T>.HwAVLTreeNode left;
        private HwAVLTree<T>.HwAVLTreeNode right;

        public HwAVLTreeNode(T key2, HwAVLTree<T>.HwAVLTreeNode left2, HwAVLTree<T>.HwAVLTreeNode right2) {
            this.key = key2;
            this.left = left2;
            this.right = right2;
        }

        public void setKey(T key2) {
            this.key = key2;
        }
    }

    private int getHeight(HwAVLTree<T>.HwAVLTreeNode tree) {
        if (tree != null) {
            return ((HwAVLTreeNode) tree).height;
        }
        return 0;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r0v0, resolved type: huawei.android.view.HwAVLTree<T>$HwAVLTreeNode<T extends java.lang.Comparable<T>> */
    /* JADX WARN: Multi-variable type inference failed */
    public int getHeight() {
        return getHeight(this.mRoot);
    }

    private void preOrder(HwAVLTree<T>.HwAVLTreeNode tree) {
        if (tree != null) {
            Log.d(TAG, "preOrder: " + ((HwAVLTreeNode) tree).key);
            preOrder(((HwAVLTreeNode) tree).left);
            preOrder(((HwAVLTreeNode) tree).right);
        }
    }

    public void preOrder() {
    }

    private void inOrder(HwAVLTree<T>.HwAVLTreeNode tree) {
        if (tree != null) {
            inOrder(((HwAVLTreeNode) tree).left);
            Log.d(TAG, "inOrder: " + ((HwAVLTreeNode) tree).key);
            inOrder(((HwAVLTreeNode) tree).right);
        }
    }

    public void inOrder() {
    }

    /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: java.util.List<T extends java.lang.Comparable<T>> */
    /* JADX DEBUG: Multi-variable search result rejected for r0v1, resolved type: java.lang.Comparable */
    /* JADX WARN: Multi-variable type inference failed */
    private void getInOrderNodes(HwAVLTree<T>.HwAVLTreeNode tree, List<T> result) {
        if (tree != null) {
            getInOrderNodes(((HwAVLTreeNode) tree).left, result);
            result.add(((HwAVLTreeNode) tree).key);
            getInOrderNodes(((HwAVLTreeNode) tree).right, result);
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r1v0, resolved type: huawei.android.view.HwAVLTree<T>$HwAVLTreeNode<T extends java.lang.Comparable<T>> */
    /* JADX WARN: Multi-variable type inference failed */
    public List<T> getInOrderNodes() {
        List<T> result = new ArrayList<>();
        getInOrderNodes(this.mRoot, result);
        return result;
    }

    private void postOrder(HwAVLTree<T>.HwAVLTreeNode tree) {
        if (tree != null) {
            postOrder(((HwAVLTreeNode) tree).left);
            postOrder(((HwAVLTreeNode) tree).right);
            Log.d(TAG, "postOrder: " + ((HwAVLTreeNode) tree).key);
        }
    }

    public void postOrder() {
    }

    /* JADX DEBUG: Type inference failed for r1v1. Raw type applied. Possible types: huawei.android.view.HwAVLTree$HwAVLTreeNode<T extends java.lang.Comparable<T>>, huawei.android.view.HwAVLTree<T>$HwAVLTreeNode<T extends java.lang.Comparable<T>> */
    /* JADX DEBUG: Type inference failed for r1v3. Raw type applied. Possible types: huawei.android.view.HwAVLTree$HwAVLTreeNode<T extends java.lang.Comparable<T>>, huawei.android.view.HwAVLTree<T>$HwAVLTreeNode<T extends java.lang.Comparable<T>> */
    private HwAVLTree<T>.HwAVLTreeNode search(HwAVLTree<T>.HwAVLTreeNode root, T key) {
        if (root == null) {
            return null;
        }
        int cmp = key.compareTo(((HwAVLTreeNode) root).key);
        return cmp < 0 ? (HwAVLTreeNode<T>) search(((HwAVLTreeNode) root).left, key) : cmp > 0 ? (HwAVLTreeNode<T>) search(((HwAVLTreeNode) root).right, key) : root;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r0v0, resolved type: huawei.android.view.HwAVLTree<T>$HwAVLTreeNode<T extends java.lang.Comparable<T>> */
    /* JADX DEBUG: Multi-variable search result rejected for r0v1, resolved type: huawei.android.view.HwAVLTree<T>$HwAVLTreeNode<T extends java.lang.Comparable<T>> */
    /* JADX WARN: Multi-variable type inference failed */
    public HwAVLTree<T>.HwAVLTreeNode search(T key) {
        return search(this.mRoot, key);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r5v0, resolved type: java.util.List<T extends java.lang.Comparable<T>> */
    /* JADX DEBUG: Multi-variable search result rejected for r1v2, resolved type: java.lang.Comparable */
    /* JADX WARN: Multi-variable type inference failed */
    private void searchAllMatchKey(HwAVLTree<T>.HwAVLTreeNode root, T key, List<T> result) {
        if (root != null) {
            if (key.compareTo(((HwAVLTreeNode) root).key) == 0) {
                result.add(((HwAVLTreeNode) root).key);
            }
            searchAllMatchKey(((HwAVLTreeNode) root).left, key, result);
            searchAllMatchKey(((HwAVLTreeNode) root).right, key, result);
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r1v0, resolved type: huawei.android.view.HwAVLTree<T>$HwAVLTreeNode<T extends java.lang.Comparable<T>> */
    /* JADX WARN: Multi-variable type inference failed */
    public List<T> searchAllMatchKey(T key) {
        List<T> result = new ArrayList<>();
        searchAllMatchKey(this.mRoot, key, result);
        return result;
    }

    private HwAVLTree<T>.HwAVLTreeNode maximum(HwAVLTree<T>.HwAVLTreeNode tree) {
        if (tree == null) {
            return null;
        }
        while (((HwAVLTreeNode) tree).right != null) {
            tree = ((HwAVLTreeNode) tree).right;
        }
        return tree;
    }

    private HwAVLTree<T>.HwAVLTreeNode leftLeftRotation(HwAVLTree<T>.HwAVLTreeNode root) {
        HwAVLTree<T>.HwAVLTreeNode leftChild = ((HwAVLTreeNode) root).left;
        ((HwAVLTreeNode) root).left = ((HwAVLTreeNode) leftChild).right;
        ((HwAVLTreeNode) leftChild).right = root;
        ((HwAVLTreeNode) root).height = Math.max(getHeight(((HwAVLTreeNode) root).left), getHeight(((HwAVLTreeNode) root).right)) + 1;
        ((HwAVLTreeNode) leftChild).height = Math.max(getHeight(((HwAVLTreeNode) leftChild).left), ((HwAVLTreeNode) root).height) + 1;
        return leftChild;
    }

    private HwAVLTree<T>.HwAVLTreeNode rightRightRotation(HwAVLTree<T>.HwAVLTreeNode root) {
        HwAVLTree<T>.HwAVLTreeNode rightChild = ((HwAVLTreeNode) root).right;
        ((HwAVLTreeNode) root).right = ((HwAVLTreeNode) rightChild).left;
        ((HwAVLTreeNode) rightChild).left = root;
        ((HwAVLTreeNode) root).height = Math.max(getHeight(((HwAVLTreeNode) root).left), getHeight(((HwAVLTreeNode) root).right)) + 1;
        ((HwAVLTreeNode) rightChild).height = Math.max(getHeight(((HwAVLTreeNode) rightChild).right), ((HwAVLTreeNode) root).height) + 1;
        return rightChild;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: huawei.android.view.HwAVLTree<T>$HwAVLTreeNode<T extends java.lang.Comparable<T>> */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX DEBUG: Type inference failed for r0v2. Raw type applied. Possible types: huawei.android.view.HwAVLTree$HwAVLTreeNode<T extends java.lang.Comparable<T>>, huawei.android.view.HwAVLTree<T>$HwAVLTreeNode<T extends java.lang.Comparable<T>> */
    private HwAVLTree<T>.HwAVLTreeNode leftRightRotation(HwAVLTree<T>.HwAVLTreeNode root) {
        ((HwAVLTreeNode) root).left = rightRightRotation(((HwAVLTreeNode) root).left);
        return (HwAVLTreeNode<T>) leftLeftRotation(root);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: huawei.android.view.HwAVLTree<T>$HwAVLTreeNode<T extends java.lang.Comparable<T>> */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX DEBUG: Type inference failed for r0v2. Raw type applied. Possible types: huawei.android.view.HwAVLTree$HwAVLTreeNode<T extends java.lang.Comparable<T>>, huawei.android.view.HwAVLTree<T>$HwAVLTreeNode<T extends java.lang.Comparable<T>> */
    private HwAVLTree<T>.HwAVLTreeNode rightLeftRotation(HwAVLTree<T>.HwAVLTreeNode root) {
        ((HwAVLTreeNode) root).right = leftLeftRotation(((HwAVLTreeNode) root).right);
        return (HwAVLTreeNode<T>) rightRightRotation(root);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r5v0, resolved type: huawei.android.view.HwAVLTree<T>$HwAVLTreeNode<T extends java.lang.Comparable<T>> */
    /* JADX DEBUG: Multi-variable search result rejected for r5v6, resolved type: huawei.android.view.HwAVLTree$HwAVLTreeNode<T extends java.lang.Comparable<T>> */
    /* JADX DEBUG: Multi-variable search result rejected for r5v7, resolved type: huawei.android.view.HwAVLTree$HwAVLTreeNode<T extends java.lang.Comparable<T>> */
    /* JADX DEBUG: Multi-variable search result rejected for r5v12, resolved type: huawei.android.view.HwAVLTree$HwAVLTreeNode<T extends java.lang.Comparable<T>> */
    /* JADX DEBUG: Multi-variable search result rejected for r5v13, resolved type: huawei.android.view.HwAVLTree$HwAVLTreeNode<T extends java.lang.Comparable<T>> */
    /* JADX DEBUG: Multi-variable search result rejected for r5v14, resolved type: huawei.android.view.HwAVLTree$HwAVLTreeNode<T extends java.lang.Comparable<T>> */
    /* JADX DEBUG: Multi-variable search result rejected for r5v15, resolved type: huawei.android.view.HwAVLTree$HwAVLTreeNode<T extends java.lang.Comparable<T>> */
    /* JADX DEBUG: Multi-variable search result rejected for r5v16, resolved type: huawei.android.view.HwAVLTree$HwAVLTreeNode<T extends java.lang.Comparable<T>> */
    /* JADX DEBUG: Multi-variable search result rejected for r5v17, resolved type: huawei.android.view.HwAVLTree$HwAVLTreeNode<T extends java.lang.Comparable<T>> */
    /* JADX DEBUG: Multi-variable search result rejected for r5v18, resolved type: huawei.android.view.HwAVLTree$HwAVLTreeNode<T extends java.lang.Comparable<T>> */
    /* JADX WARN: Multi-variable type inference failed */
    private HwAVLTree<T>.HwAVLTreeNode insert(HwAVLTree<T>.HwAVLTreeNode tree, T key) {
        HwAVLTree<T>.HwAVLTreeNode tree2;
        HwAVLTree<T>.HwAVLTreeNode tree3;
        if (tree == 0) {
            tree3 = new HwAVLTreeNode<>(key, null, null);
        } else {
            int cmp = key.compareTo(((HwAVLTreeNode) tree).key);
            if (cmp < 0) {
                ((HwAVLTreeNode) tree).left = insert(((HwAVLTreeNode) tree).left, key);
                int height = getHeight(((HwAVLTreeNode) tree).left) - getHeight(((HwAVLTreeNode) tree).right);
                tree3 = tree;
                if (height > 1) {
                    if (key.compareTo(((HwAVLTreeNode) tree).left.key) < 0) {
                        tree3 = leftLeftRotation(tree);
                    } else {
                        tree3 = leftRightRotation(tree);
                    }
                }
            } else if (cmp > 0) {
                ((HwAVLTreeNode) tree).right = insert(((HwAVLTreeNode) tree).right, key);
                int height2 = getHeight(((HwAVLTreeNode) tree).right) - getHeight(((HwAVLTreeNode) tree).left);
                tree3 = tree;
                if (height2 > 1) {
                    if (key.compareTo(((HwAVLTreeNode) tree).right.key) > 0) {
                        tree3 = rightRightRotation(tree);
                    } else {
                        tree3 = rightLeftRotation(tree);
                    }
                }
            } else {
                Log.e(TAG, "insert failed, same node");
                tree3 = tree;
            }
        }
        ((HwAVLTreeNode) tree2).height = Math.max(getHeight(((HwAVLTreeNode) tree2).left), getHeight(((HwAVLTreeNode) tree2).right)) + 1;
        return tree2;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r0v0, resolved type: huawei.android.view.HwAVLTree<T>$HwAVLTreeNode<T extends java.lang.Comparable<T>> */
    /* JADX DEBUG: Multi-variable search result rejected for r0v1, resolved type: huawei.android.view.HwAVLTree<T>$HwAVLTreeNode<T extends java.lang.Comparable<T>> */
    /* JADX WARN: Multi-variable type inference failed */
    public void insert(T key) {
        this.mRoot = insert(this.mRoot, key);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r5v0, resolved type: huawei.android.view.HwAVLTree<T>$HwAVLTreeNode<T extends java.lang.Comparable<T>> */
    /* JADX DEBUG: Multi-variable search result rejected for r6v0, resolved type: huawei.android.view.HwAVLTree<T>$HwAVLTreeNode<T extends java.lang.Comparable<T>> */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX DEBUG: Type inference failed for r5v2. Raw type applied. Possible types: huawei.android.view.HwAVLTree$HwAVLTreeNode<T extends java.lang.Comparable<T>>, huawei.android.view.HwAVLTree<T>$HwAVLTreeNode<T extends java.lang.Comparable<T>> */
    /* JADX DEBUG: Type inference failed for r5v3. Raw type applied. Possible types: huawei.android.view.HwAVLTree$HwAVLTreeNode<T extends java.lang.Comparable<T>>, huawei.android.view.HwAVLTree<T>$HwAVLTreeNode<T extends java.lang.Comparable<T>> */
    /* JADX DEBUG: Type inference failed for r5v4. Raw type applied. Possible types: huawei.android.view.HwAVLTree$HwAVLTreeNode<T extends java.lang.Comparable<T>>, huawei.android.view.HwAVLTree<T>$HwAVLTreeNode<T extends java.lang.Comparable<T>> */
    /* JADX DEBUG: Type inference failed for r5v5. Raw type applied. Possible types: huawei.android.view.HwAVLTree$HwAVLTreeNode<T extends java.lang.Comparable<T>>, huawei.android.view.HwAVLTree<T>$HwAVLTreeNode<T extends java.lang.Comparable<T>> */
    private HwAVLTree<T>.HwAVLTreeNode remove(HwAVLTree<T>.HwAVLTreeNode tree, HwAVLTree<T>.HwAVLTreeNode target) {
        if (tree == 0 || target == 0) {
            return null;
        }
        int cmp = ((HwAVLTreeNode) target).key.compareTo(((HwAVLTreeNode) tree).key);
        if (cmp < 0) {
            ((HwAVLTreeNode) tree).left = remove(((HwAVLTreeNode) tree).left, target);
            if (getHeight(((HwAVLTreeNode) tree).right) - getHeight(((HwAVLTreeNode) tree).left) != 2) {
                return tree;
            }
            HwAVLTree<T>.HwAVLTreeNode rightNode = ((HwAVLTreeNode) tree).right;
            return getHeight(((HwAVLTreeNode) rightNode).left) > getHeight(((HwAVLTreeNode) rightNode).right) ? (HwAVLTreeNode<T>) rightLeftRotation(tree) : (HwAVLTreeNode<T>) rightRightRotation(tree);
        } else if (cmp > 0) {
            ((HwAVLTreeNode) tree).right = remove(((HwAVLTreeNode) tree).right, target);
            if (getHeight(((HwAVLTreeNode) tree).left) - getHeight(((HwAVLTreeNode) tree).right) != 2) {
                return tree;
            }
            HwAVLTree<T>.HwAVLTreeNode leftNode = ((HwAVLTreeNode) tree).left;
            return getHeight(((HwAVLTreeNode) leftNode).right) > getHeight(((HwAVLTreeNode) leftNode).left) ? (HwAVLTreeNode<T>) leftRightRotation(tree) : (HwAVLTreeNode<T>) leftLeftRotation(tree);
        } else if (((HwAVLTreeNode) tree).left == null || ((HwAVLTreeNode) tree).right == null) {
            return ((HwAVLTreeNode) tree).left != null ? ((HwAVLTreeNode) tree).left : ((HwAVLTreeNode) tree).right;
        } else if (getHeight(((HwAVLTreeNode) tree).left) > getHeight(((HwAVLTreeNode) tree).right)) {
            HwAVLTreeNode<T> maximum = maximum(((HwAVLTreeNode) tree).left);
            ((HwAVLTreeNode) tree).key = ((HwAVLTreeNode) maximum).key;
            ((HwAVLTreeNode) tree).left = remove(((HwAVLTreeNode) tree).left, maximum);
            return tree;
        } else {
            HwAVLTreeNode<T> maximum2 = maximum(((HwAVLTreeNode) tree).right);
            ((HwAVLTreeNode) tree).key = ((HwAVLTreeNode) maximum2).key;
            ((HwAVLTreeNode) tree).right = remove(((HwAVLTreeNode) tree).right, maximum2);
            return tree;
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r0v0, resolved type: huawei.android.view.HwAVLTree<T>$HwAVLTreeNode<T extends java.lang.Comparable<T>> */
    /* JADX DEBUG: Multi-variable search result rejected for r1v0, resolved type: huawei.android.view.HwAVLTree<T>$HwAVLTreeNode<T extends java.lang.Comparable<T>> */
    /* JADX DEBUG: Multi-variable search result rejected for r1v1, resolved type: huawei.android.view.HwAVLTree<T>$HwAVLTreeNode<T extends java.lang.Comparable<T>> */
    /* JADX WARN: Multi-variable type inference failed */
    public void remove(T key) {
        HwAVLTreeNode search = search(this.mRoot, key);
        if (search != null) {
            this.mRoot = remove(this.mRoot, search);
        }
    }

    public boolean isEmpty() {
        return this.mRoot == null;
    }
}
