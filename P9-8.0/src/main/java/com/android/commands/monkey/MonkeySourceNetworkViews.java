package com.android.commands.monkey;

import android.app.UiAutomation;
import android.app.UiAutomationConnection;
import android.content.pm.IPackageManager;
import android.content.pm.IPackageManager.Stub;
import android.graphics.Rect;
import android.os.HandlerThread;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.view.accessibility.AccessibilityInteractionClient;
import android.view.accessibility.AccessibilityNodeInfo;
import com.android.commands.monkey.MonkeySourceNetwork.CommandQueue;
import com.android.commands.monkey.MonkeySourceNetwork.MonkeyCommand;
import com.android.commands.monkey.MonkeySourceNetwork.MonkeyCommandReturn;
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
    private static IPackageManager sPm = Stub.asInterface(ServiceManager.getService("package"));
    protected static UiAutomation sUiTestAutomationBridge;

    private interface ViewIntrospectionCommand {
        MonkeyCommandReturn query(AccessibilityNodeInfo accessibilityNodeInfo, List<String> list);
    }

    public static class GetAccessibilityIds implements ViewIntrospectionCommand {
        public MonkeyCommandReturn query(AccessibilityNodeInfo node, List<String> args) {
            if (args.size() != 0) {
                return MonkeySourceNetwork.EARG;
            }
            try {
                Field field = node.getClass().getDeclaredField("mAccessibilityViewId");
                field.setAccessible(true);
                return new MonkeyCommandReturn(true, node.getWindowId() + " " + ((Integer) field.get(node)).intValue());
            } catch (NoSuchFieldException e) {
                return new MonkeyCommandReturn(false, MonkeySourceNetworkViews.NO_NODE);
            } catch (IllegalAccessException e2) {
                return new MonkeyCommandReturn(false, "Access exception");
            }
        }
    }

    public static class GetChecked implements ViewIntrospectionCommand {
        public MonkeyCommandReturn query(AccessibilityNodeInfo node, List<String> args) {
            if (args.size() == 0) {
                return new MonkeyCommandReturn(true, Boolean.toString(node.isChecked()));
            }
            return MonkeySourceNetwork.EARG;
        }
    }

    public static class GetChildren implements ViewIntrospectionCommand {
        public MonkeyCommandReturn query(AccessibilityNodeInfo node, List<String> args) {
            if (args.size() != 0) {
                return MonkeySourceNetwork.EARG;
            }
            ViewIntrospectionCommand idGetter = new GetAccessibilityIds();
            List<String> emptyArgs = new ArrayList();
            StringBuilder ids = new StringBuilder();
            int totalChildren = node.getChildCount();
            for (int i = 0; i < totalChildren; i++) {
                MonkeyCommandReturn result = idGetter.query(node.getChild(i), emptyArgs);
                if (!result.wasSuccessful()) {
                    return result;
                }
                ids.append(result.getMessage()).append(" ");
            }
            return new MonkeyCommandReturn(true, ids.toString());
        }
    }

    public static class GetClass implements ViewIntrospectionCommand {
        public MonkeyCommandReturn query(AccessibilityNodeInfo node, List<String> args) {
            if (args.size() == 0) {
                return new MonkeyCommandReturn(true, node.getClassName().toString());
            }
            return MonkeySourceNetwork.EARG;
        }
    }

    public static class GetEnabled implements ViewIntrospectionCommand {
        public MonkeyCommandReturn query(AccessibilityNodeInfo node, List<String> args) {
            if (args.size() == 0) {
                return new MonkeyCommandReturn(true, Boolean.toString(node.isEnabled()));
            }
            return MonkeySourceNetwork.EARG;
        }
    }

    public static class GetFocused implements ViewIntrospectionCommand {
        public MonkeyCommandReturn query(AccessibilityNodeInfo node, List<String> args) {
            if (args.size() == 0) {
                return new MonkeyCommandReturn(true, Boolean.toString(node.isFocused()));
            }
            return MonkeySourceNetwork.EARG;
        }
    }

    public static class GetLocation implements ViewIntrospectionCommand {
        public MonkeyCommandReturn query(AccessibilityNodeInfo node, List<String> args) {
            if (args.size() != 0) {
                return MonkeySourceNetwork.EARG;
            }
            Rect nodePosition = new Rect();
            node.getBoundsInScreen(nodePosition);
            StringBuilder positions = new StringBuilder();
            positions.append(nodePosition.left).append(" ").append(nodePosition.top);
            positions.append(" ").append(nodePosition.right - nodePosition.left).append(" ");
            positions.append(nodePosition.bottom - nodePosition.top);
            return new MonkeyCommandReturn(true, positions.toString());
        }
    }

    public static class GetParent implements ViewIntrospectionCommand {
        public MonkeyCommandReturn query(AccessibilityNodeInfo node, List<String> args) {
            if (args.size() != 0) {
                return MonkeySourceNetwork.EARG;
            }
            AccessibilityNodeInfo parent = node.getParent();
            if (parent == null) {
                return new MonkeyCommandReturn(false, "Given node has no parent");
            }
            return new GetAccessibilityIds().query(parent, new ArrayList());
        }
    }

    public static class GetRootViewCommand implements MonkeyCommand {
        public MonkeyCommandReturn translateCommand(List<String> list, CommandQueue queue) {
            return new GetAccessibilityIds().query(MonkeySourceNetworkViews.sUiTestAutomationBridge.getRootInActiveWindow(), new ArrayList());
        }
    }

    public static class GetSelected implements ViewIntrospectionCommand {
        public MonkeyCommandReturn query(AccessibilityNodeInfo node, List<String> args) {
            if (args.size() == 0) {
                return new MonkeyCommandReturn(true, Boolean.toString(node.isSelected()));
            }
            return MonkeySourceNetwork.EARG;
        }
    }

    public static class GetText implements ViewIntrospectionCommand {
        public MonkeyCommandReturn query(AccessibilityNodeInfo node, List<String> args) {
            if (args.size() != 0) {
                return MonkeySourceNetwork.EARG;
            }
            if (node.isPassword()) {
                return new MonkeyCommandReturn(false, "Node contains a password");
            }
            if (node.getText() == null) {
                return new MonkeyCommandReturn(true, "");
            }
            return new MonkeyCommandReturn(true, node.getText().toString());
        }
    }

    public static class GetViewsWithTextCommand implements MonkeyCommand {
        public MonkeyCommandReturn translateCommand(List<String> command, CommandQueue queue) {
            if (command.size() != 2) {
                return MonkeySourceNetwork.EARG;
            }
            String text = (String) command.get(1);
            List<AccessibilityNodeInfo> nodes = AccessibilityInteractionClient.getInstance().findAccessibilityNodeInfosByText(MonkeySourceNetworkViews.sUiTestAutomationBridge.getConnectionId(), Integer.MAX_VALUE, AccessibilityNodeInfo.ROOT_NODE_ID, text);
            ViewIntrospectionCommand idGetter = new GetAccessibilityIds();
            List<String> emptyArgs = new ArrayList();
            StringBuilder ids = new StringBuilder();
            for (AccessibilityNodeInfo node : nodes) {
                MonkeyCommandReturn result = idGetter.query(node, emptyArgs);
                if (!result.wasSuccessful()) {
                    return result;
                }
                ids.append(result.getMessage()).append(" ");
            }
            return new MonkeyCommandReturn(true, ids.toString());
        }
    }

    public static class ListViewsCommand implements MonkeyCommand {
        public MonkeyCommandReturn translateCommand(List<String> list, CommandQueue queue) {
            AccessibilityNodeInfo node = MonkeySourceNetworkViews.sUiTestAutomationBridge.getRootInActiveWindow();
            if (node == null) {
                return new MonkeyCommandReturn(false, MonkeySourceNetworkViews.NO_ACCESSIBILITY_EVENT);
            }
            String packageName = node.getPackageName().toString();
            try {
                Class<?> klass = MonkeySourceNetworkViews.getIdClass(packageName, MonkeySourceNetworkViews.sPm.getApplicationInfo(packageName, 0, UserHandle.myUserId()).sourceDir);
                StringBuilder fieldBuilder = new StringBuilder();
                for (Field field : klass.getFields()) {
                    fieldBuilder.append(field.getName()).append(" ");
                }
                return new MonkeyCommandReturn(true, fieldBuilder.toString());
            } catch (RemoteException e) {
                return new MonkeyCommandReturn(false, MonkeySourceNetworkViews.REMOTE_ERROR);
            } catch (ClassNotFoundException e2) {
                return new MonkeyCommandReturn(false, MonkeySourceNetworkViews.CLASS_NOT_FOUND);
            }
        }
    }

    public static class QueryViewCommand implements MonkeyCommand {
        public MonkeyCommandReturn translateCommand(List<String> command, CommandQueue queue) {
            if (command.size() <= 2) {
                return MonkeySourceNetwork.EARG;
            }
            AccessibilityNodeInfo node;
            String viewQuery;
            List<String> args;
            String idType = (String) command.get(1);
            if ("viewid".equals(idType)) {
                try {
                    node = MonkeySourceNetworkViews.getNodeByViewId((String) command.get(2));
                    viewQuery = (String) command.get(3);
                    args = command.subList(4, command.size());
                } catch (MonkeyViewException e) {
                    return new MonkeyCommandReturn(false, e.getMessage());
                }
            } else if (!idType.equals("accessibilityids")) {
                return MonkeySourceNetwork.EARG;
            } else {
                try {
                    node = MonkeySourceNetworkViews.getNodeByAccessibilityIds((String) command.get(2), (String) command.get(3));
                    viewQuery = (String) command.get(4);
                    args = command.subList(5, command.size());
                } catch (NumberFormatException e2) {
                    return MonkeySourceNetwork.EARG;
                }
            }
            if (node == null) {
                return new MonkeyCommandReturn(false, MonkeySourceNetworkViews.NO_NODE);
            }
            ViewIntrospectionCommand getter = (ViewIntrospectionCommand) MonkeySourceNetworkViews.COMMAND_MAP.get(viewQuery);
            if (getter != null) {
                return getter.query(node, args);
            }
            return MonkeySourceNetwork.EARG;
        }
    }

    public static class SetFocused implements ViewIntrospectionCommand {
        public MonkeyCommandReturn query(AccessibilityNodeInfo node, List<String> args) {
            if (args.size() != 1) {
                return MonkeySourceNetwork.EARG;
            }
            boolean actionPerformed;
            if (Boolean.valueOf((String) args.get(0)).booleanValue()) {
                actionPerformed = node.performAction(1);
            } else if (Boolean.valueOf((String) args.get(0)).booleanValue()) {
                return MonkeySourceNetwork.EARG;
            } else {
                actionPerformed = node.performAction(2);
            }
            return new MonkeyCommandReturn(actionPerformed);
        }
    }

    public static class SetSelected implements ViewIntrospectionCommand {
        public MonkeyCommandReturn query(AccessibilityNodeInfo node, List<String> args) {
            if (args.size() != 1) {
                return MonkeySourceNetwork.EARG;
            }
            boolean actionPerformed;
            if (Boolean.valueOf((String) args.get(0)).booleanValue()) {
                actionPerformed = node.performAction(4);
            } else if (Boolean.valueOf((String) args.get(0)).booleanValue()) {
                return MonkeySourceNetwork.EARG;
            } else {
                actionPerformed = node.performAction(8);
            }
            return new MonkeyCommandReturn(actionPerformed);
        }
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

    private static Class<?> getIdClass(String packageName, String sourceDir) throws ClassNotFoundException {
        Class<?> klass = (Class) sClassMap.get(packageName);
        if (klass != null) {
            return klass;
        }
        klass = new DexClassLoader(sourceDir, "/data/local/tmp", null, ClassLoader.getSystemClassLoader()).loadClass(packageName + ".R$id");
        sClassMap.put(packageName, klass);
        return klass;
    }

    private static AccessibilityNodeInfo getNodeByAccessibilityIds(String windowString, String viewString) {
        int windowId = Integer.parseInt(windowString);
        int viewId = Integer.parseInt(viewString);
        return AccessibilityInteractionClient.getInstance().findAccessibilityNodeInfoByAccessibilityId(sUiTestAutomationBridge.getConnectionId(), windowId, (long) viewId, false, 0, null);
    }

    private static AccessibilityNodeInfo getNodeByViewId(String viewId) throws MonkeyViewException {
        List<AccessibilityNodeInfo> infos = AccessibilityInteractionClient.getInstance().findAccessibilityNodeInfosByViewId(sUiTestAutomationBridge.getConnectionId(), Integer.MAX_VALUE, AccessibilityNodeInfo.ROOT_NODE_ID, viewId);
        return !infos.isEmpty() ? (AccessibilityNodeInfo) infos.get(0) : null;
    }
}
