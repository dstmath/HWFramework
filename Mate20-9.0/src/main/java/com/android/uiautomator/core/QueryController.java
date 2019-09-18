package com.android.uiautomator.core;

import android.app.UiAutomation;
import android.os.SystemClock;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

class QueryController {
    /* access modifiers changed from: private */
    public static final boolean DEBUG = Log.isLoggable(LOG_TAG, 3);
    /* access modifiers changed from: private */
    public static final String LOG_TAG = QueryController.class.getSimpleName();
    private static final boolean VERBOSE = Log.isLoggable(LOG_TAG, 2);
    /* access modifiers changed from: private */
    public String mLastActivityName = null;
    /* access modifiers changed from: private */
    public String mLastTraversedText = "";
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    private int mLogIndent = 0;
    private int mLogParentIndent = 0;
    private int mPatternCounter = 0;
    private int mPatternIndexer = 0;
    private final UiAutomatorBridge mUiAutomatorBridge;

    public QueryController(UiAutomatorBridge bridge) {
        this.mUiAutomatorBridge = bridge;
        bridge.setOnAccessibilityEventListener(new UiAutomation.OnAccessibilityEventListener() {
            public void onAccessibilityEvent(AccessibilityEvent event) {
                synchronized (QueryController.this.mLock) {
                    int eventType = event.getEventType();
                    if (eventType != 32) {
                        if (eventType == 131072) {
                            if (!(event.getText() == null || event.getText().size() <= 0 || event.getText().get(0) == null)) {
                                String unused = QueryController.this.mLastTraversedText = ((CharSequence) event.getText().get(0)).toString();
                            }
                            if (QueryController.DEBUG) {
                                String access$400 = QueryController.LOG_TAG;
                                Log.d(access$400, "Last text selection reported: " + QueryController.this.mLastTraversedText);
                            }
                        }
                    } else if (!(event.getText() == null || event.getText().size() <= 0 || event.getText().get(0) == null)) {
                        String unused2 = QueryController.this.mLastActivityName = ((CharSequence) event.getText().get(0)).toString();
                    }
                    QueryController.this.mLock.notifyAll();
                }
            }
        });
    }

    public String getLastTraversedText() {
        this.mUiAutomatorBridge.waitForIdle();
        synchronized (this.mLock) {
            if (this.mLastTraversedText.length() <= 0) {
                return null;
            }
            String str = this.mLastTraversedText;
            return str;
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

    /* access modifiers changed from: protected */
    public AccessibilityNodeInfo findAccessibilityNodeInfo(UiSelector selector, boolean isCounting) {
        this.mUiAutomatorBridge.waitForIdle();
        initializeNewSearch();
        if (DEBUG) {
            String str = LOG_TAG;
            Log.d(str, "Searching: " + selector);
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

    /* access modifiers changed from: protected */
    public AccessibilityNodeInfo getRootNode() {
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
        AccessibilityNodeInfo fromNode2;
        if (!selector.hasContainerSelector()) {
            fromNode2 = translateReqularSelector(selector, fromNode);
        } else if (selector.getContainerSelector().hasContainerSelector()) {
            fromNode2 = translateCompoundSelector(selector.getContainerSelector(), fromNode, false);
            initializeNewSearch();
        } else {
            fromNode2 = translateReqularSelector(selector.getContainerSelector(), fromNode);
        }
        if (fromNode2 == null) {
            if (DEBUG) {
                String str = LOG_TAG;
                Log.d(str, "Container selector not found: " + selector.dumpToString(false));
            }
            return null;
        }
        if (selector.hasPatternSelector()) {
            fromNode2 = translatePatternSelector(selector.getPatternSelector(), fromNode2, isCounting);
            if (isCounting) {
                Log.i(LOG_TAG, String.format("Counted %d instances of: %s", new Object[]{Integer.valueOf(this.mPatternCounter), selector}));
                return null;
            } else if (fromNode2 == null) {
                if (DEBUG) {
                    String str2 = LOG_TAG;
                    Log.d(str2, "Pattern selector not found: " + selector.dumpToString(false));
                }
                return null;
            }
        }
        if ((selector.hasContainerSelector() || selector.hasPatternSelector()) && (selector.hasChildSelector() || selector.hasParentSelector())) {
            fromNode2 = translateReqularSelector(selector, fromNode2);
        }
        if (fromNode2 == null) {
            if (DEBUG) {
                String str3 = LOG_TAG;
                Log.d(str3, "Object Not Found for selector " + selector);
            }
            return null;
        }
        Log.i(LOG_TAG, String.format("Matched selector: %s <<==>> [%s]", new Object[]{selector, fromNode2}));
        return fromNode2;
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
            UiSelector subSelector2 = subSelector.getPatternSelector();
            if (subSelector2 == null) {
                Log.e(LOG_TAG, "Pattern portion of the selector is null or not defined");
                return null;
            }
            int i = this.mLogIndent + 1;
            this.mLogIndent = i;
            this.mLogParentIndent = i;
            return findNodePatternRecursive(subSelector2, fromNode, 0, subSelector2);
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
        this.mUiAutomatorBridge.waitForIdle();
        AccessibilityNodeInfo rootNode = getRootNode();
        String str = null;
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
