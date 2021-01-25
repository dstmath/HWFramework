package com.android.commands.monkey;

import android.app.ActivityManager;
import android.app.UiAutomation;
import android.app.UiAutomationConnection;
import android.content.pm.IPackageManager;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.view.accessibility.AccessibilityInteractionClient;
import android.view.accessibility.AccessibilityNodeInfo;
import com.android.commands.monkey.MonkeySourceNetwork;
import dalvik.system.DexClassLoader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MonkeySourceNetworkViews {
    private static final String CLASS_NOT_FOUND = "Error retrieving class information";
    private static final Map<String, ViewIntrospectionCommand> COMMAND_MAP = new HashMap();
    private static final String HANDLER_THREAD_NAME = "UiAutomationHandlerThread";
    private static final String NO_ACCESSIBILITY_EVENT = "No accessibility event has occured yet";
    private static final String NO_CONNECTION = "Failed to connect to AccessibilityService, try restarting Monkey";
    private static final String NO_NODE = "Node with given ID does not exist";
    private static final String REMOTE_ERROR = "Unable to retrieve application info from PackageManager";
    private static Map<String, Class<?>> sClassMap = new HashMap();
    private static final HandlerThread sHandlerThread = new HandlerThread(HANDLER_THREAD_NAME);
    private static IPackageManager sPm = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
    protected static UiAutomation sUiTestAutomationBridge;

    /* access modifiers changed from: private */
    public interface ViewIntrospectionCommand {
        MonkeySourceNetwork.MonkeyCommandReturn query(AccessibilityNodeInfo accessibilityNodeInfo, List<String> list);
    }

    static {
        COMMAND_MAP.put("getlocation", new GetLocation());
        COMMAND_MAP.put("gettext", new GetText());
        COMMAND_MAP.put("getclass", new GetClass());
        COMMAND_MAP.put("getchecked", new GetChecked());
        COMMAND_MAP.put("getenabled", new GetEnabled());
        COMMAND_MAP.put("getselected", new GetSelected());
        COMMAND_MAP.put("setselected", new SetSelected());
        COMMAND_MAP.put("getfocused", new GetFocused());
        COMMAND_MAP.put("setfocused", new SetFocused());
        COMMAND_MAP.put("getparent", new GetParent());
        COMMAND_MAP.put("getchildren", new GetChildren());
        COMMAND_MAP.put("getaccessibilityids", new GetAccessibilityIds());
    }

    public static void setup() {
        sHandlerThread.setDaemon(true);
        sHandlerThread.start();
        sUiTestAutomationBridge = new UiAutomation(sHandlerThread.getLooper(), new UiAutomationConnection());
        sUiTestAutomationBridge.connect();
    }

    public static void teardown() {
        sHandlerThread.quit();
    }

    /* access modifiers changed from: private */
    public static Class<?> getIdClass(String packageName, String sourceDir) throws ClassNotFoundException {
        Class<?> klass = sClassMap.get(packageName);
        if (klass != null) {
            return klass;
        }
        DexClassLoader classLoader = new DexClassLoader(sourceDir, "/data/local/tmp", null, ClassLoader.getSystemClassLoader());
        Class<?> klass2 = classLoader.loadClass(packageName + ".R$id");
        sClassMap.put(packageName, klass2);
        return klass2;
    }

    /* access modifiers changed from: private */
    public static AccessibilityNodeInfo getNodeByAccessibilityIds(String windowString, String viewString) {
        int windowId = Integer.parseInt(windowString);
        int viewId = Integer.parseInt(viewString);
        return AccessibilityInteractionClient.getInstance().findAccessibilityNodeInfoByAccessibilityId(sUiTestAutomationBridge.getConnectionId(), windowId, (long) viewId, false, 0, (Bundle) null);
    }

    /* access modifiers changed from: private */
    public static AccessibilityNodeInfo getNodeByViewId(String viewId) throws MonkeyViewException {
        List<AccessibilityNodeInfo> infos = AccessibilityInteractionClient.getInstance().findAccessibilityNodeInfosByViewId(sUiTestAutomationBridge.getConnectionId(), Integer.MAX_VALUE, AccessibilityNodeInfo.ROOT_NODE_ID, viewId);
        if (!infos.isEmpty()) {
            return infos.get(0);
        }
        return null;
    }

    public static class ListViewsCommand implements MonkeySourceNetwork.MonkeyCommand {
        @Override // com.android.commands.monkey.MonkeySourceNetwork.MonkeyCommand
        public MonkeySourceNetwork.MonkeyCommandReturn translateCommand(List<String> list, MonkeySourceNetwork.CommandQueue queue) {
            AccessibilityNodeInfo node = MonkeySourceNetworkViews.sUiTestAutomationBridge.getRootInActiveWindow();
            if (node == null) {
                return new MonkeySourceNetwork.MonkeyCommandReturn(false, MonkeySourceNetworkViews.NO_ACCESSIBILITY_EVENT);
            }
            String packageName = node.getPackageName().toString();
            try {
                Class<?> klass = MonkeySourceNetworkViews.getIdClass(packageName, MonkeySourceNetworkViews.sPm.getApplicationInfo(packageName, 0, ActivityManager.getCurrentUser()).sourceDir);
                StringBuilder fieldBuilder = new StringBuilder();
                Field[] fields = klass.getFields();
                for (Field field : fields) {
                    fieldBuilder.append(field.getName() + " ");
                }
                return new MonkeySourceNetwork.MonkeyCommandReturn(true, fieldBuilder.toString());
            } catch (RemoteException e) {
                return new MonkeySourceNetwork.MonkeyCommandReturn(false, MonkeySourceNetworkViews.REMOTE_ERROR);
            } catch (ClassNotFoundException e2) {
                return new MonkeySourceNetwork.MonkeyCommandReturn(false, MonkeySourceNetworkViews.CLASS_NOT_FOUND);
            }
        }
    }

    public static class QueryViewCommand implements MonkeySourceNetwork.MonkeyCommand {
        @Override // com.android.commands.monkey.MonkeySourceNetwork.MonkeyCommand
        public MonkeySourceNetwork.MonkeyCommandReturn translateCommand(List<String> command, MonkeySourceNetwork.CommandQueue queue) {
            List<String> args;
            String viewQuery;
            AccessibilityNodeInfo node;
            if (command.size() <= 2) {
                return MonkeySourceNetwork.EARG;
            }
            String idType = command.get(1);
            if ("viewid".equals(idType)) {
                try {
                    node = MonkeySourceNetworkViews.getNodeByViewId(command.get(2));
                    viewQuery = command.get(3);
                    args = command.subList(4, command.size());
                } catch (MonkeyViewException e) {
                    return new MonkeySourceNetwork.MonkeyCommandReturn(false, e.getMessage());
                }
            } else if (!idType.equals("accessibilityids")) {
                return MonkeySourceNetwork.EARG;
            } else {
                try {
                    node = MonkeySourceNetworkViews.getNodeByAccessibilityIds(command.get(2), command.get(3));
                    viewQuery = command.get(4);
                    args = command.subList(5, command.size());
                } catch (NumberFormatException e2) {
                    return MonkeySourceNetwork.EARG;
                }
            }
            if (node == null) {
                return new MonkeySourceNetwork.MonkeyCommandReturn(false, MonkeySourceNetworkViews.NO_NODE);
            }
            ViewIntrospectionCommand getter = (ViewIntrospectionCommand) MonkeySourceNetworkViews.COMMAND_MAP.get(viewQuery);
            if (getter != null) {
                return getter.query(node, args);
            }
            return MonkeySourceNetwork.EARG;
        }
    }

    public static class GetRootViewCommand implements MonkeySourceNetwork.MonkeyCommand {
        @Override // com.android.commands.monkey.MonkeySourceNetwork.MonkeyCommand
        public MonkeySourceNetwork.MonkeyCommandReturn translateCommand(List<String> list, MonkeySourceNetwork.CommandQueue queue) {
            return new GetAccessibilityIds().query(MonkeySourceNetworkViews.sUiTestAutomationBridge.getRootInActiveWindow(), new ArrayList());
        }
    }

    public static class GetViewsWithTextCommand implements MonkeySourceNetwork.MonkeyCommand {
        @Override // com.android.commands.monkey.MonkeySourceNetwork.MonkeyCommand
        public MonkeySourceNetwork.MonkeyCommandReturn translateCommand(List<String> command, MonkeySourceNetwork.CommandQueue queue) {
            if (command.size() != 2) {
                return MonkeySourceNetwork.EARG;
            }
            int connectionId = MonkeySourceNetworkViews.sUiTestAutomationBridge.getConnectionId();
            List<AccessibilityNodeInfo> nodes = AccessibilityInteractionClient.getInstance().findAccessibilityNodeInfosByText(connectionId, Integer.MAX_VALUE, AccessibilityNodeInfo.ROOT_NODE_ID, command.get(1));
            ViewIntrospectionCommand idGetter = new GetAccessibilityIds();
            List<String> emptyArgs = new ArrayList<>();
            StringBuilder ids = new StringBuilder();
            for (AccessibilityNodeInfo node : nodes) {
                MonkeySourceNetwork.MonkeyCommandReturn result = idGetter.query(node, emptyArgs);
                if (!result.wasSuccessful()) {
                    return result;
                }
                ids.append(result.getMessage());
                ids.append(" ");
            }
            return new MonkeySourceNetwork.MonkeyCommandReturn(true, ids.toString());
        }
    }

    public static class GetLocation implements ViewIntrospectionCommand {
        @Override // com.android.commands.monkey.MonkeySourceNetworkViews.ViewIntrospectionCommand
        public MonkeySourceNetwork.MonkeyCommandReturn query(AccessibilityNodeInfo node, List<String> args) {
            if (args.size() != 0) {
                return MonkeySourceNetwork.EARG;
            }
            Rect nodePosition = new Rect();
            node.getBoundsInScreen(nodePosition);
            return new MonkeySourceNetwork.MonkeyCommandReturn(true, nodePosition.left + " " + nodePosition.top + " " + (nodePosition.right - nodePosition.left) + " " + (nodePosition.bottom - nodePosition.top));
        }
    }

    public static class GetText implements ViewIntrospectionCommand {
        @Override // com.android.commands.monkey.MonkeySourceNetworkViews.ViewIntrospectionCommand
        public MonkeySourceNetwork.MonkeyCommandReturn query(AccessibilityNodeInfo node, List<String> args) {
            if (args.size() != 0) {
                return MonkeySourceNetwork.EARG;
            }
            if (node.isPassword()) {
                return new MonkeySourceNetwork.MonkeyCommandReturn(false, "Node contains a password");
            }
            if (node.getText() == null) {
                return new MonkeySourceNetwork.MonkeyCommandReturn(true, "");
            }
            return new MonkeySourceNetwork.MonkeyCommandReturn(true, node.getText().toString());
        }
    }

    public static class GetClass implements ViewIntrospectionCommand {
        @Override // com.android.commands.monkey.MonkeySourceNetworkViews.ViewIntrospectionCommand
        public MonkeySourceNetwork.MonkeyCommandReturn query(AccessibilityNodeInfo node, List<String> args) {
            if (args.size() == 0) {
                return new MonkeySourceNetwork.MonkeyCommandReturn(true, node.getClassName().toString());
            }
            return MonkeySourceNetwork.EARG;
        }
    }

    public static class GetChecked implements ViewIntrospectionCommand {
        @Override // com.android.commands.monkey.MonkeySourceNetworkViews.ViewIntrospectionCommand
        public MonkeySourceNetwork.MonkeyCommandReturn query(AccessibilityNodeInfo node, List<String> args) {
            if (args.size() == 0) {
                return new MonkeySourceNetwork.MonkeyCommandReturn(true, Boolean.toString(node.isChecked()));
            }
            return MonkeySourceNetwork.EARG;
        }
    }

    public static class GetEnabled implements ViewIntrospectionCommand {
        @Override // com.android.commands.monkey.MonkeySourceNetworkViews.ViewIntrospectionCommand
        public MonkeySourceNetwork.MonkeyCommandReturn query(AccessibilityNodeInfo node, List<String> args) {
            if (args.size() == 0) {
                return new MonkeySourceNetwork.MonkeyCommandReturn(true, Boolean.toString(node.isEnabled()));
            }
            return MonkeySourceNetwork.EARG;
        }
    }

    public static class GetSelected implements ViewIntrospectionCommand {
        @Override // com.android.commands.monkey.MonkeySourceNetworkViews.ViewIntrospectionCommand
        public MonkeySourceNetwork.MonkeyCommandReturn query(AccessibilityNodeInfo node, List<String> args) {
            if (args.size() == 0) {
                return new MonkeySourceNetwork.MonkeyCommandReturn(true, Boolean.toString(node.isSelected()));
            }
            return MonkeySourceNetwork.EARG;
        }
    }

    public static class SetSelected implements ViewIntrospectionCommand {
        @Override // com.android.commands.monkey.MonkeySourceNetworkViews.ViewIntrospectionCommand
        public MonkeySourceNetwork.MonkeyCommandReturn query(AccessibilityNodeInfo node, List<String> args) {
            boolean actionPerformed;
            if (args.size() != 1) {
                return MonkeySourceNetwork.EARG;
            }
            if (Boolean.valueOf(args.get(0)).booleanValue()) {
                actionPerformed = node.performAction(4);
            } else if (Boolean.valueOf(args.get(0)).booleanValue()) {
                return MonkeySourceNetwork.EARG;
            } else {
                actionPerformed = node.performAction(8);
            }
            return new MonkeySourceNetwork.MonkeyCommandReturn(actionPerformed);
        }
    }

    public static class GetFocused implements ViewIntrospectionCommand {
        @Override // com.android.commands.monkey.MonkeySourceNetworkViews.ViewIntrospectionCommand
        public MonkeySourceNetwork.MonkeyCommandReturn query(AccessibilityNodeInfo node, List<String> args) {
            if (args.size() == 0) {
                return new MonkeySourceNetwork.MonkeyCommandReturn(true, Boolean.toString(node.isFocused()));
            }
            return MonkeySourceNetwork.EARG;
        }
    }

    public static class SetFocused implements ViewIntrospectionCommand {
        @Override // com.android.commands.monkey.MonkeySourceNetworkViews.ViewIntrospectionCommand
        public MonkeySourceNetwork.MonkeyCommandReturn query(AccessibilityNodeInfo node, List<String> args) {
            boolean actionPerformed;
            if (args.size() != 1) {
                return MonkeySourceNetwork.EARG;
            }
            if (Boolean.valueOf(args.get(0)).booleanValue()) {
                actionPerformed = node.performAction(1);
            } else if (Boolean.valueOf(args.get(0)).booleanValue()) {
                return MonkeySourceNetwork.EARG;
            } else {
                actionPerformed = node.performAction(2);
            }
            return new MonkeySourceNetwork.MonkeyCommandReturn(actionPerformed);
        }
    }

    public static class GetAccessibilityIds implements ViewIntrospectionCommand {
        @Override // com.android.commands.monkey.MonkeySourceNetworkViews.ViewIntrospectionCommand
        public MonkeySourceNetwork.MonkeyCommandReturn query(AccessibilityNodeInfo node, List<String> args) {
            if (args.size() != 0) {
                return MonkeySourceNetwork.EARG;
            }
            try {
                Field field = node.getClass().getDeclaredField("mAccessibilityViewId");
                field.setAccessible(true);
                int viewId = ((Integer) field.get(node)).intValue();
                return new MonkeySourceNetwork.MonkeyCommandReturn(true, node.getWindowId() + " " + viewId);
            } catch (NoSuchFieldException e) {
                return new MonkeySourceNetwork.MonkeyCommandReturn(false, MonkeySourceNetworkViews.NO_NODE);
            } catch (IllegalAccessException e2) {
                return new MonkeySourceNetwork.MonkeyCommandReturn(false, "Access exception");
            }
        }
    }

    public static class GetParent implements ViewIntrospectionCommand {
        @Override // com.android.commands.monkey.MonkeySourceNetworkViews.ViewIntrospectionCommand
        public MonkeySourceNetwork.MonkeyCommandReturn query(AccessibilityNodeInfo node, List<String> args) {
            if (args.size() != 0) {
                return MonkeySourceNetwork.EARG;
            }
            AccessibilityNodeInfo parent = node.getParent();
            if (parent == null) {
                return new MonkeySourceNetwork.MonkeyCommandReturn(false, "Given node has no parent");
            }
            return new GetAccessibilityIds().query(parent, new ArrayList());
        }
    }

    public static class GetChildren implements ViewIntrospectionCommand {
        @Override // com.android.commands.monkey.MonkeySourceNetworkViews.ViewIntrospectionCommand
        public MonkeySourceNetwork.MonkeyCommandReturn query(AccessibilityNodeInfo node, List<String> args) {
            if (args.size() != 0) {
                return MonkeySourceNetwork.EARG;
            }
            ViewIntrospectionCommand idGetter = new GetAccessibilityIds();
            List<String> emptyArgs = new ArrayList<>();
            StringBuilder ids = new StringBuilder();
            int totalChildren = node.getChildCount();
            for (int i = 0; i < totalChildren; i++) {
                MonkeySourceNetwork.MonkeyCommandReturn result = idGetter.query(node.getChild(i), emptyArgs);
                if (!result.wasSuccessful()) {
                    return result;
                }
                ids.append(result.getMessage());
                ids.append(" ");
            }
            return new MonkeySourceNetwork.MonkeyCommandReturn(true, ids.toString());
        }
    }
}
