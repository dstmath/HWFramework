package ohos.agp.components;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;
import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.components.ComponentContainer;
import ohos.agp.styles.attributes.ViewAttrsConstants;
import ohos.app.Context;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.global.resource.NotExistException;
import ohos.global.resource.ResourceManager;
import ohos.global.resource.WrongTypeException;
import ohos.global.resource.solidxml.Node;
import ohos.global.resource.solidxml.SolidXml;
import ohos.global.resource.solidxml.TypedAttribute;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class LayoutScatter {
    private static final int DEFAULT_ID = -1;
    private static final Integer FIRST_INDEX = 0;
    private static final int MAX_DEPTH = 1000;
    private static final int MAX_VIEW_COUNT = 20000;
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogDomain.END, "AGP_LayoutScatter");
    private Context mContext;
    private Stack<Integer> mIndexStack = new Stack<>();
    private int mParseDepth = 0;
    private int mParseViewCount = 0;
    private Picker mPicker;
    private HashMap<String, Boolean> mPickerMap = new HashMap<>();
    private ResourceManager mResourceManager;
    private final List<String> mResourceNames = new ArrayList(Arrays.asList(".resource.Resource", ".hap.ResourceTable", ".resource.ResourceTable", ".ResourceTable", ".resource.Id", ".Resource.Id"));
    private Class<?> mRsClass;
    private final Map<String, Constructor<? extends Component>> mViewConstructorMap = new HashMap();

    public interface Picker {
        boolean isLoadClass(Class<?> cls);
    }

    public LayoutScatter(Context context) {
        if (context != null) {
            this.mResourceManager = context.getResourceManager();
            if (this.mResourceManager != null) {
                this.mContext = context;
                return;
            }
            throw new LayoutScatterException("get resourceManager failed");
        }
    }

    private LayoutScatter(Context context, ResourceManager resourceManager) {
        this.mContext = context;
        this.mResourceManager = resourceManager;
        if (this.mResourceManager == null) {
            throw new LayoutScatterException("get resourceManager failed");
        }
    }

    public static LayoutScatter getInstance(Context context) {
        if (context != null) {
            Object layoutScatter = context.getLayoutScatter();
            if (layoutScatter instanceof LayoutScatter) {
                return (LayoutScatter) layoutScatter;
            }
            throw new LayoutScatterException("Context has not initialized LayoutScatter");
        }
        throw new LayoutScatterException("get context is null");
    }

    public LayoutScatter clone(Context context, ResourceManager resourceManager) {
        return new LayoutScatter(context, resourceManager);
    }

    public Component parse(int i, ComponentContainer componentContainer, boolean z) {
        ResourceManager resourceManager = this.mResourceManager;
        if (resourceManager != null) {
            try {
                SolidXml solidXml = resourceManager.getSolidXml(i);
                this.mParseDepth = 0;
                this.mParseViewCount = 0;
                return parseSolidXml(solidXml, componentContainer, z);
            } catch (NotExistException unused) {
                throw new LayoutScatterException("Can't open solid xml: file not exist: " + i);
            } catch (IOException unused2) {
                throw new LayoutScatterException("Can't open solid xml: io exception: " + i);
            } catch (WrongTypeException unused3) {
                throw new LayoutScatterException("Can't open solid xml: wrong type: " + i);
            }
        } else {
            throw new LayoutScatterException("LayoutScatter should init Context first.");
        }
    }

    public Picker getPicker() {
        return this.mPicker;
    }

    public void setPicker(Picker picker) {
        this.mPicker = picker;
        if (picker != null) {
            this.mPickerMap = new HashMap<>();
        }
    }

    public Component createComponentElement(String str, AttrSet attrSet) {
        try {
            return createViewByReflection(str, attrSet);
        } catch (LayoutScatterException e) {
            HiLog.error(TAG, "Create component failed: %{public}s", new Object[]{e.getMessage()});
            return null;
        }
    }

    private Component createViewElement(String str, AttrSet attrSet) {
        try {
            if (str.contains(".")) {
                return createViewByReflection(str, attrSet);
            }
            if ("View".equals(str)) {
                return createViewByReflection("ohos.agp.view." + str, attrSet);
            } else if ("SurfaceProvider".equals(str)) {
                return createViewByReflection("ohos.agp.components.surfaceprovider." + str, attrSet);
            } else {
                return createViewByReflection("ohos.agp.components." + str, attrSet);
            }
        } catch (LayoutScatterException e) {
            HiLog.error(TAG, "Create view failed: %{public}s", new Object[]{e.getMessage()});
            return null;
        }
    }

    private Component createViewByReflection(String str, AttrSet attrSet) {
        Constructor<? extends Component> constructor = this.mViewConstructorMap.get(str);
        if (constructor == null) {
            try {
                Class<? extends U> asSubclass = Class.forName(str, false, this.mContext.getClassloader()).asSubclass(Component.class);
                filter(str, asSubclass);
                constructor = asSubclass.getConstructor(Context.class, AttrSet.class);
                constructor.setAccessible(true);
                this.mViewConstructorMap.put(str, constructor);
            } catch (ClassNotFoundException e) {
                throw new LayoutScatterException("Can't not find the class: " + str, e);
            } catch (NoSuchMethodException e2) {
                throw new LayoutScatterException("Can't not find the class constructor: " + str, e2);
            }
        } else {
            filter(str, null);
        }
        try {
            return (Component) constructor.newInstance(this.mContext, attrSet);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e3) {
            throw new LayoutScatterException("Can't create the view: " + str, e3);
        }
    }

    private AttrSet convertAttr(List<TypedAttribute> list) {
        AttrSetImpl attrSetImpl = new AttrSetImpl();
        for (TypedAttribute typedAttribute : list) {
            String name = typedAttribute.getName();
            if (name == null) {
                HiLog.error(TAG, " attribute content is invalid!", new Object[0]);
            } else {
                attrSetImpl.addAttr(new AttrImpl(name, typedAttribute, this.mContext));
            }
        }
        return attrSetImpl;
    }

    private void filter(String str, Class<? extends Component> cls) {
        if (this.mPicker != null) {
            try {
                Boolean bool = this.mPickerMap.get(str);
                if (bool == null) {
                    if (cls == null) {
                        cls = Class.forName(str, false, this.mContext.getClassloader()).asSubclass(Component.class);
                    }
                    bool = Boolean.valueOf(this.mPicker.isLoadClass(cls));
                    this.mPickerMap.put(str, bool);
                }
                if (bool != null && !bool.booleanValue()) {
                    failNotAllowed(str);
                }
            } catch (ClassNotFoundException e) {
                throw new LayoutScatterException("Can't not find the class: " + str, e);
            }
        }
    }

    public Component parseSolidXml(SolidXml solidXml, ComponentContainer componentContainer, boolean z) {
        Node child;
        if (solidXml != null) {
            Node root = solidXml.getRoot();
            if (root != null) {
                String name = root.getName();
                if (name == null || name.length() == 0) {
                    throw new LayoutScatterException("Solid XML root node has no name!");
                }
                AttrSet convertAttr = convertAttr(root.getTypedAttributes(this.mResourceManager));
                Component createViewElement = createViewElement(name, convertAttr);
                if (createViewElement != null) {
                    if ((createViewElement instanceof ComponentContainer) && (child = root.getChild()) != null) {
                        this.mIndexStack.push(FIRST_INDEX);
                        parseSolidXmlNode((ComponentContainer) createViewElement, child);
                    }
                    if (componentContainer == null) {
                        createViewElement.setLayoutConfig(new ComponentContainer.LayoutConfig(this.mContext, convertAttr));
                        return createViewElement;
                    } else if (!z) {
                        createViewElement.setLayoutConfig(componentContainer.createLayoutConfig(this.mContext, convertAttr));
                        return createViewElement;
                    } else {
                        createViewElement.setLayoutConfig(componentContainer.createLayoutConfig(this.mContext, convertAttr));
                        componentContainer.addComponent(createViewElement);
                        return componentContainer;
                    }
                } else {
                    throw new LayoutScatterException("Create component " + name + " failed!");
                }
            } else {
                throw new LayoutScatterException("Solid XML has no root node!");
            }
        } else {
            throw new LayoutScatterException("Can't open solid include XML!");
        }
    }

    private Class<?> getResClass(List<String> list) {
        Object hostContext = this.mContext.getHostContext();
        if (hostContext == null) {
            return null;
        }
        if (!(hostContext instanceof android.content.Context)) {
            HiLog.error(TAG, "resource-id: host context is not context of android.", new Object[0]);
            return null;
        }
        android.content.Context context = (android.content.Context) hostContext;
        Iterator<String> it = list.iterator();
        while (it.hasNext()) {
            String str = context.getPackageName() + it.next();
            try {
                return Class.forName(str, false, context.getClassLoader());
            } catch (ClassNotFoundException unused) {
                HiLog.debug(TAG, "resource-id: class not found from path[%{public}s].", new Object[]{str});
            }
        }
        HiLog.debug(TAG, "resource-id: class not found from all input path.", new Object[0]);
        return null;
    }

    private String getIdName(int i, Class<?> cls) {
        if (cls == null) {
            HiLog.error(TAG, "resource-id: input clazz is null.", new Object[0]);
            return "";
        }
        Field[] fields = cls.getFields();
        for (Field field : fields) {
            if ("int".equals(field.getType().getName())) {
                try {
                    if (field.getInt(cls) == i) {
                        return field.getName();
                    }
                } catch (IllegalAccessException unused) {
                    HiLog.error(TAG, "resource-id: IllegalAccessException happened", new Object[0]);
                }
            }
        }
        HiLog.error(TAG, "resource-id: can not get name of id[%{public}d] from clazz", new Object[]{Integer.valueOf(i)});
        return null;
    }

    private String getIdentifier(int i) {
        if (this.mRsClass == null) {
            this.mRsClass = getResClass(this.mResourceNames);
        }
        return getIdName(i, this.mRsClass);
    }

    private String getIdentifier(List<TypedAttribute> list) {
        for (TypedAttribute typedAttribute : list) {
            if ("id".equals(typedAttribute.getName())) {
                try {
                    return getIdentifier(typedAttribute.getIntegerValue());
                } catch (IOException | NotExistException | WrongTypeException unused) {
                    HiLog.error(TAG, "resource-id: get integer value failed for exception happened!", new Object[0]);
                    return "";
                }
            }
        }
        HiLog.debug(TAG, "resource-id: get integer value failed for no attribute of id!", new Object[0]);
        return "";
    }

    private void parseSolidXmlNode(ComponentContainer componentContainer, Node node) {
        if (this.mParseDepth > 1000 || this.mParseViewCount > MAX_VIEW_COUNT) {
            throw new LayoutScatterException(String.format(Locale.ROOT, "Exceeded the depth limit: %d Or the view count limit: %d", 1000, Integer.valueOf((int) MAX_VIEW_COUNT)));
        }
        String name = node.getName();
        if (name != null && name.length() != 0) {
            Component component = null;
            if (Constants.ELEMNAME_INCLUDE_STRING.equals(name)) {
                parseIncludeXml(componentContainer, node);
                this.mIndexStack = (Stack) this.mIndexStack.clone();
            } else if ("ComponentHolder".equals(name)) {
                saveComponentHolder(componentContainer, node);
            } else {
                component = createComponent(componentContainer, node, name);
            }
            this.mParseViewCount++;
            Node child = node.getChild();
            if (child != null) {
                this.mIndexStack.push(FIRST_INDEX);
                if (component instanceof ComponentContainer) {
                    this.mParseDepth++;
                    parseSolidXmlNode((ComponentContainer) component, child);
                } else {
                    HiLog.warn(TAG, "Parent node is not a ViewGroup.", new Object[0]);
                }
            }
            Node sibling = node.getSibling();
            if (sibling != null) {
                if (!this.mIndexStack.isEmpty()) {
                    this.mIndexStack.push(Integer.valueOf(this.mIndexStack.pop().intValue() + 1));
                }
                parseSolidXmlNode(componentContainer, sibling);
            }
            if (!this.mIndexStack.isEmpty()) {
                this.mIndexStack.pop();
            }
        }
    }

    private Component createComponent(ComponentContainer componentContainer, Node node, String str) {
        List<TypedAttribute> typedAttributes = node.getTypedAttributes(this.mResourceManager);
        String identifier = getIdentifier(typedAttributes);
        AttrSet convertAttr = convertAttr(typedAttributes);
        Component createViewElement = createViewElement(str, convertAttr);
        if (createViewElement != null) {
            createViewElement.setLayoutConfig(componentContainer.createLayoutConfig(this.mContext, convertAttr));
            createViewElement.setName(identifier);
            componentContainer.addComponent(createViewElement);
        }
        return createViewElement;
    }

    /* JADX WARNING: Removed duplicated region for block: B:24:0x005f  */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0093 A[SYNTHETIC, Splitter:B:40:0x0093] */
    private void parseIncludeXml(ComponentContainer componentContainer, Node node) {
        boolean z;
        List<TypedAttribute> typedAttributes = node.getTypedAttributes(this.mResourceManager);
        if (!typedAttributes.isEmpty()) {
            SolidXml solidXml = null;
            int i = -1;
            int i2 = -1;
            for (TypedAttribute typedAttribute : typedAttributes) {
                if (typedAttribute != null) {
                    String name = typedAttribute.getName();
                    int hashCode = name.hashCode();
                    if (hashCode != -1109722326) {
                        if (hashCode != 3355) {
                            if (hashCode == 1941332754 && name.equals(ViewAttrsConstants.VISIBILITY)) {
                                z = true;
                                if (z) {
                                    try {
                                        i = typedAttribute.getIntegerValue();
                                    } catch (IOException | NotExistException | WrongTypeException unused) {
                                        HiLog.error(TAG, "get include XML id failed!", new Object[0]);
                                        return;
                                    }
                                } else if (z) {
                                    try {
                                        i2 = typedAttribute.getIntegerValue();
                                    } catch (IOException | NotExistException | WrongTypeException unused2) {
                                        HiLog.error(TAG, "get include visibility id failed!", new Object[0]);
                                        return;
                                    }
                                } else if (!z) {
                                    continue;
                                } else if (typedAttribute.getType() != 5) {
                                    HiLog.error(TAG, " invalid layout format!", new Object[0]);
                                    return;
                                } else {
                                    try {
                                        solidXml = typedAttribute.getLayoutValue();
                                    } catch (IOException | NotExistException | WrongTypeException unused3) {
                                        HiLog.error(TAG, "get include XML layout failed!", new Object[0]);
                                        return;
                                    }
                                }
                            }
                        } else if (name.equals("id")) {
                            z = false;
                            if (z) {
                            }
                        }
                    } else if (name.equals("layout")) {
                        z = true;
                        if (z) {
                        }
                    }
                    z = true;
                    if (z) {
                    }
                } else {
                    throw new LayoutScatterException(" include Attribute is invalid!");
                }
            }
            Component parseSolidXml = parseSolidXml(solidXml, null, true);
            if (parseSolidXml != null) {
                if (i != -1) {
                    parseSolidXml.setId(i);
                    parseSolidXml.setName(getIdentifier(i));
                }
                parseSolidXml.setLayoutConfig(componentContainer.createLayoutConfig(this.mContext, convertAttr(typedAttributes)));
                if (i2 == 0 || i2 == 1 || i2 == 2) {
                    HiLog.debug(TAG, " set include visibility %{public}d", new Object[]{Integer.valueOf(i2)});
                    parseSolidXml.setVisibility(i2);
                }
                componentContainer.addComponent(parseSolidXml);
                return;
            }
            throw new LayoutScatterException(" create view failed!");
        }
        throw new LayoutScatterException("include xml has no attribute!");
    }

    private void saveComponentHolder(ComponentContainer componentContainer, Node node) {
        HiLog.debug(TAG, "find componentHolder", new Object[0]);
        SolidXml solidXml = null;
        int i = -1;
        for (TypedAttribute typedAttribute : node.getTypedAttributes(this.mResourceManager)) {
            try {
                if ("id".equals(typedAttribute.getName())) {
                    i = typedAttribute.getIntegerValue();
                    HiLog.debug(TAG, "read ComponentHolder id: %{public}d", new Object[]{Integer.valueOf(i)});
                }
                if ("layout".equals(typedAttribute.getName())) {
                    HiLog.debug(TAG, "get component holder", new Object[0]);
                    solidXml = typedAttribute.getLayoutValue();
                }
            } catch (IOException | NotExistException | WrongTypeException unused) {
                HiLog.error(TAG, "save component holder failed.", new Object[0]);
                return;
            }
        }
        if (i == -1) {
            HiLog.error(TAG, "componentHolder's resId not set", new Object[0]);
        } else if (solidXml == null) {
            HiLog.error(TAG, "componentHolder layout params is wrong", new Object[0]);
        } else if (this.mIndexStack.isEmpty()) {
            HiLog.error(TAG, "save componentHolder failed.", new Object[0]);
        } else {
            componentContainer.mComponentHolderMap.put(Integer.valueOf(i), new ComponentHolder(i, componentContainer, this.mIndexStack.peek().intValue(), solidXml, this));
        }
    }

    private void failNotAllowed(String str) {
        throw new LayoutScatterException(str + ": Class not allowed to be inflated ");
    }
}
