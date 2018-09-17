package com.android.uiautomator.core;

import android.app.UiAutomation.OnAccessibilityEventListener;
import android.os.SystemClock;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

class QueryController {
    private static final boolean DEBUG = Log.isLoggable(LOG_TAG, 3);
    private static final String LOG_TAG = QueryController.class.getSimpleName();
    private static final boolean VERBOSE = Log.isLoggable(LOG_TAG, 2);
    private String mLastActivityName = null;
    private String mLastTraversedText = "";
    private final Object mLock = new Object();
    private int mLogIndent = 0;
    private int mLogParentIndent = 0;
    private int mPatternCounter = 0;
    private int mPatternIndexer = 0;
    private final UiAutomatorBridge mUiAutomatorBridge;

    public QueryController(UiAutomatorBridge bridge) {
        this.mUiAutomatorBridge = bridge;
        bridge.setOnAccessibilityEventListener(new OnAccessibilityEventListener() {
            public void onAccessibilityEvent(AccessibilityEvent event) {
                synchronized (QueryController.this.mLock) {
                    switch (event.getEventType()) {
                        case 32:
                            if (!(event.getText() == null || event.getText().size() <= 0 || event.getText().get(0) == null)) {
                                QueryController.this.mLastActivityName = ((CharSequence) event.getText().get(0)).toString();
                                break;
                            }
                        case 131072:
                            if (!(event.getText() == null || event.getText().size() <= 0 || event.getText().get(0) == null)) {
                                QueryController.this.mLastTraversedText = ((CharSequence) event.getText().get(0)).toString();
                            }
                            if (QueryController.DEBUG) {
                                Log.d(QueryController.LOG_TAG, "Last text selection reported: " + QueryController.this.mLastTraversedText);
                                break;
                            }
                            break;
                    }
                    QueryController.this.mLock.notifyAll();
                }
            }
        });
    }

    public String getLastTraversedText() {
        this.mUiAutomatorBridge.waitForIdle();
        synchronized (this.mLock) {
            if (this.mLastTraversedText.length() > 0) {
                String str = this.mLastTraversedText;
                return str;
            }
            return null;
        }
    }

    public void clearLastTraversedText() {
        this.mUiAutomatorBridge.waitForIdle();
        synchronized (this.mLock) {
            this.mLastTraversedText = "";
        }
    }

    private void initializeNewSearch() {
        this.mPatternCounter = 0;
        this.mPatternIndexer = 0;
        this.mLogIndent = 0;
        this.mLogParentIndent = 0;
    }

    public int getPatternCount(UiSelector selector) {
        findAccessibilityNodeInfo(selector, true);
        return this.mPatternCounter;
    }

    public AccessibilityNodeInfo findAccessibilityNodeInfo(UiSelector selector) {
        return findAccessibilityNodeInfo(selector, false);
    }

    protected AccessibilityNodeInfo findAccessibilityNodeInfo(UiSelector selector, boolean isCounting) {
        this.mUiAutomatorBridge.waitForIdle();
        initializeNewSearch();
        if (DEBUG) {
            Log.d(LOG_TAG, "Searching: " + selector);
        }
        synchronized (this.mLock) {
            AccessibilityNodeInfo rootNode = getRootNode();
            if (rootNode == null) {
                Log.e(LOG_TAG, "Cannot proceed when root node is null. Aborted search");
                return null;
            }
            AccessibilityNodeInfo translateCompoundSelector = translateCompoundSelector(new UiSelector(selector), rootNode, isCounting);
            return translateCompoundSelector;
        }
    }

    protected AccessibilityNodeInfo getRootNode() {
        AccessibilityNodeInfo rootNode = null;
        for (int x = 0; x < 4; x++) {
            rootNode = this.mUiAutomatorBridge.getRootInActiveWindow();
            if (rootNode != null) {
                return rootNode;
            }
            if (x < 3) {
                Log.e(LOG_TAG, "Got null root node from accessibility - Retrying...");
                SystemClock.sleep(250);
            }
        }
        return rootNode;
    }

    private AccessibilityNodeInfo translateCompoundSelector(UiSelector selector, AccessibilityNodeInfo fromNode, boolean isCounting) {
        if (!selector.hasContainerSelector()) {
            fromNode = translateReqularSelector(selector, fromNode);
        } else if (selector.getContainerSelector().hasContainerSelector()) {
            fromNode = translateCompoundSelector(selector.getContainerSelector(), fromNode, false);
            initializeNewSearch();
        } else {
            fromNode = translateReqularSelector(selector.getContainerSelector(), fromNode);
        }
        if (fromNode == null) {
            if (DEBUG) {
                Log.d(LOG_TAG, "Container selector not found: " + selector.dumpToString(false));
            }
            return null;
        }
        if (selector.hasPatternSelector()) {
            fromNode = translatePatternSelector(selector.getPatternSelector(), fromNode, isCounting);
            if (isCounting) {
                Log.i(LOG_TAG, String.format("Counted %d instances of: %s", new Object[]{Integer.valueOf(this.mPatternCounter), selector}));
                return null;
            } else if (fromNode == null) {
                if (DEBUG) {
                    Log.d(LOG_TAG, "Pattern selector not found: " + selector.dumpToString(false));
                }
                return null;
            }
        }
        if ((selector.hasContainerSelector() || selector.hasPatternSelector()) && (selector.hasChildSelector() || selector.hasParentSelector())) {
            fromNode = translateReqularSelector(selector, fromNode);
        }
        if (fromNode == null) {
            if (DEBUG) {
                Log.d(LOG_TAG, "Object Not Found for selector " + selector);
            }
            return null;
        }
        Log.i(LOG_TAG, String.format("Matched selector: %s <<==>> [%s]", new Object[]{selector, fromNode}));
        return fromNode;
    }

    private AccessibilityNodeInfo translateReqularSelector(UiSelector selector, AccessibilityNodeInfo fromNode) {
        return findNodeRegularRecursive(selector, fromNode, 0);
    }

    private AccessibilityNodeInfo findNodeRegularRecursive(UiSelector subSelector, AccessibilityNodeInfo fromNode, int index) {
        if (subSelector.isMatchFor(fromNode, index)) {
            if (DEBUG) {
                Log.d(LOG_TAG, formatLog(String.format("%s", new Object[]{subSelector.dumpToString(false)})));
            }
            if (subSelector.isLeaf()) {
                return fromNode;
            }
            if (subSelector.hasChildSelector()) {
                this.mLogIndent++;
                subSelector = subSelector.getChildSelector();
                if (subSelector == null) {
                    Log.e(LOG_TAG, "Error: A child selector without content");
                    return null;
                }
            } else if (subSelector.hasParentSelector()) {
                this.mLogIndent++;
                subSelector = subSelector.getParentSelector();
                if (subSelector == null) {
                    Log.e(LOG_TAG, "Error: A parent selector without content");
                    return null;
                }
                fromNode = fromNode.getParent();
                if (fromNode == null) {
                    return null;
                }
            }
        }
        int childCount = fromNode.getChildCount();
        boolean hasNullChild = false;
        for (int i = 0; i < childCount; i++) {
            AccessibilityNodeInfo childNode = fromNode.getChild(i);
            if (childNode == null) {
                Log.w(LOG_TAG, String.format("AccessibilityNodeInfo returned a null child (%d of %d)", new Object[]{Integer.valueOf(i), Integer.valueOf(childCount)}));
                if (!hasNullChild) {
                    Log.w(LOG_TAG, String.format("parent = %s", new Object[]{fromNode.toString()}));
                }
                hasNullChild = true;
            } else if (childNode.isVisibleToUser()) {
                AccessibilityNodeInfo retNode = findNodeRegularRecursive(subSelector, childNode, i);
                if (retNode != null) {
                    return retNode;
                }
            } else if (VERBOSE) {
                Log.v(LOG_TAG, String.format("Skipping invisible child: %s", new Object[]{childNode.toString()}));
            }
        }
        return null;
    }

    private AccessibilityNodeInfo translatePatternSelector(UiSelector subSelector, AccessibilityNodeInfo fromNode, boolean isCounting) {
        if (subSelector.hasPatternSelector()) {
            if (isCounting) {
                this.mPatternIndexer = -1;
            } else {
                this.mPatternIndexer = subSelector.getInstance();
            }
            subSelector = subSelector.getPatternSelector();
            if (subSelector == null) {
                Log.e(LOG_TAG, "Pattern portion of the selector is null or not defined");
                return null;
            }
            int i = this.mLogIndent + 1;
            this.mLogIndent = i;
            this.mLogParentIndent = i;
            return findNodePatternRecursive(subSelector, fromNode, 0, subSelector);
        }
        Log.e(LOG_TAG, "Selector must have a pattern selector defined");
        return null;
    }

    private AccessibilityNodeInfo findNodePatternRecursive(UiSelector subSelector, AccessibilityNodeInfo fromNode, int index, UiSelector originalPattern) {
        if (subSelector.isMatchFor(fromNode, index)) {
            if (!subSelector.isLeaf()) {
                if (DEBUG) {
                    Log.d(LOG_TAG, formatLog(String.format("%s", new Object[]{subSelector.dumpToString(false)})));
                }
                if (subSelector.hasChildSelector()) {
                    this.mLogIndent++;
                    subSelector = subSelector.getChildSelector();
                    if (subSelector == null) {
                        Log.e(LOG_TAG, "Error: A child selector without content");
                        return null;
                    }
                } else if (subSelector.hasParentSelector()) {
                    this.mLogIndent++;
                    subSelector = subSelector.getParentSelector();
                    if (subSelector == null) {
                        Log.e(LOG_TAG, "Error: A parent selector without content");
                        return null;
                    }
                    fromNode = fromNode.getParent();
                    if (fromNode == null) {
                        return null;
                    }
                }
            } else if (this.mPatternIndexer == 0) {
                if (DEBUG) {
                    Log.d(LOG_TAG, formatLog(String.format("%s", new Object[]{subSelector.dumpToString(false)})));
                }
                return fromNode;
            } else {
                if (DEBUG) {
                    Log.d(LOG_TAG, formatLog(String.format("%s", new Object[]{subSelector.dumpToString(false)})));
                }
                this.mPatternCounter++;
                this.mPatternIndexer--;
                subSelector = originalPattern;
                this.mLogIndent = this.mLogParentIndent;
            }
        }
        int childCount = fromNode.getChildCount();
        boolean hasNullChild = false;
        for (int i = 0; i < childCount; i++) {
            AccessibilityNodeInfo childNode = fromNode.getChild(i);
            if (childNode == null) {
                Log.w(LOG_TAG, String.format("AccessibilityNodeInfo returned a null child (%d of %d)", new Object[]{Integer.valueOf(i), Integer.valueOf(childCount)}));
                if (!hasNullChild) {
                    Log.w(LOG_TAG, String.format("parent = %s", new Object[]{fromNode.toString()}));
                }
                hasNullChild = true;
            } else if (childNode.isVisibleToUser()) {
                AccessibilityNodeInfo retNode = findNodePatternRecursive(subSelector, childNode, i, originalPattern);
                if (retNode != null) {
                    return retNode;
                }
            } else if (DEBUG) {
                Log.d(LOG_TAG, String.format("Skipping invisible child: %s", new Object[]{childNode.toString()}));
            }
        }
        return null;
    }

    public AccessibilityNodeInfo getAccessibilityRootNode() {
        return this.mUiAutomatorBridge.getRootInActiveWindow();
    }

    @Deprecated
    public String getCurrentActivityName() {
        String str;
        this.mUiAutomatorBridge.waitForIdle();
        synchronized (this.mLock) {
            str = this.mLastActivityName;
        }
        return str;
    }

    public String getCurrentPackageName() {
        String str = null;
        this.mUiAutomatorBridge.waitForIdle();
        AccessibilityNodeInfo rootNode = getRootNode();
        if (rootNode == null) {
            return null;
        }
        if (rootNode.getPackageName() != null) {
            str = rootNode.getPackageName().toString();
        }
        return str;
    }

    private String formatLog(String str) {
        StringBuilder l = new StringBuilder();
        for (int space = 0; space < this.mLogIndent; space++) {
            l.append(". . ");
        }
        if (this.mLogIndent > 0) {
            l.append(String.format(". . [%d]: %s", new Object[]{Integer.valueOf(this.mPatternCounter), str}));
        } else {
            l.append(String.format(". . [%d]: %s", new Object[]{Integer.valueOf(this.mPatternCounter), str}));
        }
        return l.toString();
    }
}
