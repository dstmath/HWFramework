package android.support.v4.view;

import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.view.accessibility.AccessibilityNodeProviderCompat;
import android.view.View;
import android.view.View.AccessibilityDelegate;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeProvider;

public class AccessibilityDelegateCompat {
    private static final AccessibilityDelegate DEFAULT_DELEGATE = new AccessibilityDelegate();
    private static final AccessibilityDelegateBaseImpl IMPL;
    final AccessibilityDelegate mBridge = IMPL.newAccessibilityDelegateBridge(this);

    static class AccessibilityDelegateBaseImpl {
        AccessibilityDelegateBaseImpl() {
        }

        public AccessibilityDelegate newAccessibilityDelegateBridge(final AccessibilityDelegateCompat compat) {
            return new AccessibilityDelegate() {
                public boolean dispatchPopulateAccessibilityEvent(View host, AccessibilityEvent event) {
                    return compat.dispatchPopulateAccessibilityEvent(host, event);
                }

                public void onInitializeAccessibilityEvent(View host, AccessibilityEvent event) {
                    compat.onInitializeAccessibilityEvent(host, event);
                }

                public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
                    compat.onInitializeAccessibilityNodeInfo(host, AccessibilityNodeInfoCompat.wrap(info));
                }

                public void onPopulateAccessibilityEvent(View host, AccessibilityEvent event) {
                    compat.onPopulateAccessibilityEvent(host, event);
                }

                public boolean onRequestSendAccessibilityEvent(ViewGroup host, View child, AccessibilityEvent event) {
                    return compat.onRequestSendAccessibilityEvent(host, child, event);
                }

                public void sendAccessibilityEvent(View host, int eventType) {
                    compat.sendAccessibilityEvent(host, eventType);
                }

                public void sendAccessibilityEventUnchecked(View host, AccessibilityEvent event) {
                    compat.sendAccessibilityEventUnchecked(host, event);
                }
            };
        }

        public AccessibilityNodeProviderCompat getAccessibilityNodeProvider(AccessibilityDelegate delegate, View host) {
            return null;
        }

        public boolean performAccessibilityAction(AccessibilityDelegate delegate, View host, int action, Bundle args) {
            return false;
        }
    }

    @RequiresApi(16)
    static class AccessibilityDelegateApi16Impl extends AccessibilityDelegateBaseImpl {
        AccessibilityDelegateApi16Impl() {
        }

        public AccessibilityDelegate newAccessibilityDelegateBridge(final AccessibilityDelegateCompat compat) {
            return new AccessibilityDelegate() {
                public boolean dispatchPopulateAccessibilityEvent(View host, AccessibilityEvent event) {
                    return compat.dispatchPopulateAccessibilityEvent(host, event);
                }

                public void onInitializeAccessibilityEvent(View host, AccessibilityEvent event) {
                    compat.onInitializeAccessibilityEvent(host, event);
                }

                public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
                    compat.onInitializeAccessibilityNodeInfo(host, AccessibilityNodeInfoCompat.wrap(info));
                }

                public void onPopulateAccessibilityEvent(View host, AccessibilityEvent event) {
                    compat.onPopulateAccessibilityEvent(host, event);
                }

                public boolean onRequestSendAccessibilityEvent(ViewGroup host, View child, AccessibilityEvent event) {
                    return compat.onRequestSendAccessibilityEvent(host, child, event);
                }

                public void sendAccessibilityEvent(View host, int eventType) {
                    compat.sendAccessibilityEvent(host, eventType);
                }

                public void sendAccessibilityEventUnchecked(View host, AccessibilityEvent event) {
                    compat.sendAccessibilityEventUnchecked(host, event);
                }

                public AccessibilityNodeProvider getAccessibilityNodeProvider(View host) {
                    AccessibilityNodeProviderCompat provider = compat.getAccessibilityNodeProvider(host);
                    if (provider != null) {
                        return (AccessibilityNodeProvider) provider.getProvider();
                    }
                    return null;
                }

                public boolean performAccessibilityAction(View host, int action, Bundle args) {
                    return compat.performAccessibilityAction(host, action, args);
                }
            };
        }

        public AccessibilityNodeProviderCompat getAccessibilityNodeProvider(AccessibilityDelegate delegate, View host) {
            Object provider = delegate.getAccessibilityNodeProvider(host);
            if (provider != null) {
                return new AccessibilityNodeProviderCompat(provider);
            }
            return null;
        }

        public boolean performAccessibilityAction(AccessibilityDelegate delegate, View host, int action, Bundle args) {
            return delegate.performAccessibilityAction(host, action, args);
        }
    }

    static {
        if (VERSION.SDK_INT >= 16) {
            IMPL = new AccessibilityDelegateApi16Impl();
        } else {
            IMPL = new AccessibilityDelegateBaseImpl();
        }
    }

    AccessibilityDelegate getBridge() {
        return this.mBridge;
    }

    public void sendAccessibilityEvent(View host, int eventType) {
        DEFAULT_DELEGATE.sendAccessibilityEvent(host, eventType);
    }

    public void sendAccessibilityEventUnchecked(View host, AccessibilityEvent event) {
        DEFAULT_DELEGATE.sendAccessibilityEventUnchecked(host, event);
    }

    public boolean dispatchPopulateAccessibilityEvent(View host, AccessibilityEvent event) {
        return DEFAULT_DELEGATE.dispatchPopulateAccessibilityEvent(host, event);
    }

    public void onPopulateAccessibilityEvent(View host, AccessibilityEvent event) {
        DEFAULT_DELEGATE.onPopulateAccessibilityEvent(host, event);
    }

    public void onInitializeAccessibilityEvent(View host, AccessibilityEvent event) {
        DEFAULT_DELEGATE.onInitializeAccessibilityEvent(host, event);
    }

    public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfoCompat info) {
        DEFAULT_DELEGATE.onInitializeAccessibilityNodeInfo(host, info.unwrap());
    }

    public boolean onRequestSendAccessibilityEvent(ViewGroup host, View child, AccessibilityEvent event) {
        return DEFAULT_DELEGATE.onRequestSendAccessibilityEvent(host, child, event);
    }

    public AccessibilityNodeProviderCompat getAccessibilityNodeProvider(View host) {
        return IMPL.getAccessibilityNodeProvider(DEFAULT_DELEGATE, host);
    }

    public boolean performAccessibilityAction(View host, int action, Bundle args) {
        return IMPL.performAccessibilityAction(DEFAULT_DELEGATE, host, action, args);
    }
}
