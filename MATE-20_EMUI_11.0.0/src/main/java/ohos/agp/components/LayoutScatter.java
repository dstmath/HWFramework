package ohos.agp.components;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.styles.attributes.ViewAttrsConstants;
import ohos.app.Context;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.global.resource.NotExistException;
import ohos.global.resource.ResourceManager;
import ohos.global.resource.WrongTypeException;
import ohos.global.resource.solidxml.Node;
import ohos.global.resource.solidxml.Pattern;
import ohos.global.resource.solidxml.SolidXml;
import ohos.global.resource.solidxml.TypedAttribute;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class LayoutScatter {
    private static final int MAX_DEPTH = 1000;
    private static final int MAX_VIEW_COUNT = 20000;
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogDomain.END, "AGP_LayoutScatter");
    private static final String THEME_TAG = "theme";
    private static volatile LayoutScatter sLayoutScatter;
    private Context mContext;
    private Factory mFactory;
    private int mParseDepth = 0;
    private int mParseViewCount = 0;
    private ResourceManager mResourceManager;
    private Map<String, Constructor<? extends Component>> mViewConstructorMap = new HashMap();

    public interface Factory {
        Component onCreateView(String str, AttrSet attrSet);
    }

    private LayoutScatter(Context context) {
        if (context != null) {
            this.mResourceManager = context.getResourceManager();
            if (this.mResourceManager != null) {
                this.mContext = context;
                return;
            }
            throw new LayoutScatterException("get resourceManager failed");
        }
    }

    private LayoutScatter(LayoutScatter layoutScatter, Context context, ResourceManager resourceManager) {
        this.mContext = context;
        this.mResourceManager = resourceManager;
        if (this.mResourceManager != null) {
            this.mFactory = layoutScatter.mFactory;
            return;
        }
        throw new LayoutScatterException("get resourceManager failed");
    }

    public static LayoutScatter getInstance(Context context) {
        if (sLayoutScatter == null || sLayoutScatter.mContext == null || !sLayoutScatter.mContext.equals(context)) {
            synchronized (LayoutScatter.class) {
                if (sLayoutScatter == null || sLayoutScatter.mContext == null || !sLayoutScatter.mContext.equals(context)) {
                    sLayoutScatter = new LayoutScatter(context);
                }
            }
        }
        return sLayoutScatter;
    }

    public LayoutScatter clone(Context context, ResourceManager resourceManager) {
        return new LayoutScatter(this, context, resourceManager);
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

    public void setFactory(Factory factory) {
        this.mFactory = factory;
    }

    private Component createViewElement(String str, AttrSet attrSet) {
        Component component;
        Factory factory = this.mFactory;
        Component onCreateView = factory != null ? factory.onCreateView(str, attrSet) : null;
        if (onCreateView != null) {
            return onCreateView;
        }
        try {
            if (str.contains(".")) {
                component = createViewByReflection(str, attrSet);
            } else if ("View".equals(str)) {
                component = createViewByReflection("ohos.agp.view." + str, attrSet);
            } else if ("SurfaceProvider".equals(str)) {
                component = createViewByReflection("ohos.agp.components.surfaceprovider." + str, attrSet);
            } else {
                component = createViewByReflection("ohos.agp.components." + str, attrSet);
            }
            return component;
        } catch (LayoutScatterException e) {
            HiLog.error(TAG, "Create view failed: %{public}s", new Object[]{e.getMessage()});
            return onCreateView;
        }
    }

    private Component createViewByReflection(String str, AttrSet attrSet) {
        Constructor<? extends Component> constructor = this.mViewConstructorMap.get(str);
        if (constructor == null) {
            try {
                constructor = Class.forName(str, false, this.mContext.getClassloader()).asSubclass(Component.class).getConstructor(Context.class, AttrSet.class);
                constructor.setAccessible(true);
                this.mViewConstructorMap.put(str, constructor);
            } catch (ClassNotFoundException e) {
                throw new LayoutScatterException("Can't not find the class: " + str, e);
            } catch (NoSuchMethodException e2) {
                throw new LayoutScatterException("Can't not find the class constructor: " + str, e2);
            }
        }
        try {
            return (Component) constructor.newInstance(this.mContext, attrSet);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e3) {
            throw new LayoutScatterException("Can't create the view: " + str, e3);
        }
    }

    private AttrSet convertAttr(List<TypedAttribute> list) {
        HiLog.debug(TAG, "convertAttr:attr size: %{public}d", new Object[]{Integer.valueOf(list.size())});
        AttrSetImpl attrSetImpl = new AttrSetImpl();
        Pattern pattern = null;
        for (TypedAttribute typedAttribute : list) {
            if (typedAttribute == null) {
                HiLog.error(TAG, " attribute is null!", new Object[0]);
            } else {
                String name = typedAttribute.getName();
                HiLog.debug(TAG, "read attr : %{public}s", new Object[]{name});
                if (name == null) {
                    HiLog.error(TAG, " attribute content is invalid!", new Object[0]);
                } else if (THEME_TAG.equals(name)) {
                    try {
                        pattern = typedAttribute.getPatternValue();
                    } catch (IOException | NotExistException | WrongTypeException unused) {
                        HiLog.error(TAG, "convertAttr catch error", new Object[0]);
                    }
                } else {
                    attrSetImpl.addAttr(new AttrImpl(name, typedAttribute, this.mContext));
                }
            }
        }
        if (pattern == null) {
            HiLog.debug(TAG, " no custom theme. ", new Object[0]);
            return attrSetImpl;
        }
        HiLog.debug(TAG, " read custom theme! ", new Object[0]);
        HashMap patternHash = pattern.getPatternHash();
        if (patternHash == null) {
            HiLog.error(TAG, " attrMap is null, do not use it", new Object[0]);
            return attrSetImpl;
        }
        patternHash.forEach(new BiConsumer(attrSetImpl) {
            /* class ohos.agp.components.$$Lambda$LayoutScatter$vNLkCrSGXceVNrjqoiHQPH6x2M */
            private final /* synthetic */ AttrSetImpl f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.util.function.BiConsumer
            public final void accept(Object obj, Object obj2) {
                LayoutScatter.this.lambda$convertAttr$0$LayoutScatter(this.f$1, (String) obj, (TypedAttribute) obj2);
            }
        });
        return attrSetImpl;
    }

    public /* synthetic */ void lambda$convertAttr$0$LayoutScatter(AttrSetImpl attrSetImpl, String str, TypedAttribute typedAttribute) {
        if (str != null && typedAttribute != null) {
            HiLog.debug(TAG, "attrName: %{public}s , value: %{public}s", new Object[]{str, typedAttribute.getOriginalValue()});
            if (!attrSetImpl.getAttr(str).isPresent()) {
                HiLog.debug(TAG, "add attr: %{public}s defined in theme", new Object[]{str});
                attrSetImpl.addAttr(new AttrImpl(str, typedAttribute, this.mContext));
            }
        }
    }

    private Component parseSolidXml(SolidXml solidXml, ComponentContainer componentContainer, boolean z) {
        Node child;
        if (solidXml != null) {
            Node root = solidXml.getRoot();
            if (root != null) {
                String name = root.getName();
                HiLog.debug(TAG, " parseSolidXml: %{public}s", new Object[]{name});
                if (name == null || name.length() == 0) {
                    throw new LayoutScatterException("Solid XML root node has no name!");
                }
                AttrSet convertAttr = convertAttr(root.getTypedAttribute(this.mResourceManager));
                Component createViewElement = createViewElement(name, convertAttr);
                if ((createViewElement instanceof ComponentContainer) && (child = root.getChild()) != null) {
                    HiLog.debug(TAG, " parseSolidXmlNode: %{public}s", new Object[]{child.getName()});
                    parseSolidXmlNode((ComponentContainer) createViewElement, child);
                }
                if (componentContainer == null || !z) {
                    return createViewElement;
                }
                createViewElement.setLayoutConfig(componentContainer.createLayoutConfig(this.mContext, convertAttr));
                componentContainer.addComponent(createViewElement);
                return componentContainer;
            }
            throw new LayoutScatterException("Solid XML has no root node!");
        }
        throw new LayoutScatterException("Can't open solid include XML!");
    }

    private Class<?> getResClass(List<String> list) {
        Object hostContext = this.mContext.getHostContext();
        if (!(hostContext instanceof android.content.Context)) {
            HiLog.error(TAG, "resource-id: host context is not context of android.", new Object[0]);
            return null;
        }
        android.content.Context context = (android.content.Context) hostContext;
        Iterator<String> it = list.iterator();
        while (it.hasNext()) {
            String str = context.getPackageName() + it.next();
            try {
                Class<?> cls = Class.forName(str, false, context.getClassLoader());
                if (cls != null) {
                    return cls;
                }
                HiLog.error(TAG, "resource-id: Class.forName for classname[%{public}s] return null.", new Object[]{str});
            } catch (ClassNotFoundException unused) {
                HiLog.error(TAG, "resource-id: class not found from path[%{public}s].", new Object[]{str});
            }
        }
        HiLog.error(TAG, "resource-id: class not found from all input path.", new Object[0]);
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
                        String name = field.getName();
                        if (name == null) {
                            HiLog.error(TAG, "resource-id: get name[null] from id[%{public}d] failed.", new Object[]{Integer.valueOf(i)});
                        }
                        return name;
                    }
                } catch (IllegalAccessException unused) {
                    HiLog.error(TAG, "resource-id: IllegalAccessException happended", new Object[0]);
                }
            }
        }
        HiLog.error(TAG, "resource-id: can not get name of id[%{public}d] from clazz", new Object[]{Integer.valueOf(i)});
        return null;
    }

    private String getIdentifier(int i) {
        ArrayList arrayList = new ArrayList();
        arrayList.add(".resource.Resource");
        arrayList.add(".hap.ResourceTable");
        arrayList.add(".resource.ResourceTable");
        arrayList.add(".ResourceTable");
        arrayList.add(".resource.Id");
        arrayList.add(".Resource.Id");
        return getIdName(i, getResClass(arrayList));
    }

    private String getIdentifier(List<TypedAttribute> list) {
        for (TypedAttribute typedAttribute : list) {
            String name = typedAttribute.getName();
            HiLog.debug(TAG, "resource-id: attribute of name[%{public}s].", new Object[]{name});
            if ("id".equals(name)) {
                try {
                    return getIdentifier(typedAttribute.getIntegerValue());
                } catch (IOException | NotExistException | WrongTypeException unused) {
                    HiLog.error(TAG, "resource-id: get integer value failed for exception happended!", new Object[0]);
                    return "";
                }
            }
        }
        HiLog.error(TAG, "resource-id: get integer value failed for no attribute of id!", new Object[0]);
        return "";
    }

    private void parseSolidXmlNode(ComponentContainer componentContainer, Node node) {
        Component component;
        int i = this.mParseDepth;
        if (i > 1000 || this.mParseViewCount > MAX_VIEW_COUNT) {
            throw new LayoutScatterException(String.format(Locale.ROOT, "Exceeded the depth limit: %d Or the view count limit: %d", 1000, Integer.valueOf((int) MAX_VIEW_COUNT)));
        }
        HiLog.debug(TAG, "parseSolidXmlNode current depth: %{public}d", new Object[]{Integer.valueOf(i)});
        String name = node.getName();
        HiLog.debug(TAG, "parseSolidXmlNode: %{public}s: %{public}s", new Object[]{componentContainer, name});
        if (name != null && name.length() != 0) {
            if (Constants.ELEMNAME_INCLUDE_STRING.equals(name)) {
                parseIncludeXml(componentContainer, node);
                component = null;
            } else {
                List<TypedAttribute> typedAttribute = node.getTypedAttribute(this.mResourceManager);
                String identifier = getIdentifier(typedAttribute);
                AttrSet convertAttr = convertAttr(typedAttribute);
                component = createViewElement(name, convertAttr);
                if (component != null) {
                    component.setLayoutConfig(componentContainer.createLayoutConfig(this.mContext, convertAttr));
                    component.setName(identifier);
                    componentContainer.addComponent(component);
                }
            }
            this.mParseViewCount++;
            Node child = node.getChild();
            if (child != null) {
                if (component instanceof ComponentContainer) {
                    this.mParseDepth++;
                    HiLog.debug(TAG, "parseSolidXmlNode: child: %{public}s: %{public}s", new Object[]{component, child.getName()});
                    parseSolidXmlNode((ComponentContainer) component, child);
                } else {
                    HiLog.warn(TAG, "Parent node is not a ViewGroup.", new Object[0]);
                }
            }
            Node sibling = node.getSibling();
            if (sibling != null) {
                HiLog.debug(TAG, "parseSolidXmlNode: sibling: %{public}s: %{public}s", new Object[]{componentContainer, sibling.getName()});
                parseSolidXmlNode(componentContainer, sibling);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:24:0x0077  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x00cd  */
    private void parseIncludeXml(ComponentContainer componentContainer, Node node) {
        boolean z;
        HiLog.debug(TAG, " parseIncludeXml", new Object[0]);
        List<TypedAttribute> typedAttribute = node.getTypedAttribute(this.mResourceManager);
        if (!typedAttribute.isEmpty()) {
            SolidXml solidXml = null;
            int i = -1;
            int i2 = -1;
            for (TypedAttribute typedAttribute2 : typedAttribute) {
                if (typedAttribute2 != null) {
                    HiLog.debug(TAG, " parseIncludeXml: %{public}s", new Object[]{typedAttribute2.getName()});
                    String name = typedAttribute2.getName();
                    int hashCode = name.hashCode();
                    if (hashCode != -1109722326) {
                        if (hashCode != 3355) {
                            if (hashCode == 1941332754 && name.equals(ViewAttrsConstants.VISIBILITY)) {
                                z = true;
                                if (z) {
                                    HiLog.debug(TAG, " parse include id", new Object[0]);
                                    try {
                                        i = typedAttribute2.getIntegerValue();
                                        HiLog.debug(TAG, " get include id is %{public}d", new Object[]{Integer.valueOf(i)});
                                    } catch (IOException | NotExistException | WrongTypeException unused) {
                                        HiLog.error(TAG, "get include XML id failed!", new Object[0]);
                                        return;
                                    }
                                } else if (z) {
                                    HiLog.debug(TAG, " parse include visibility", new Object[0]);
                                    try {
                                        i2 = typedAttribute2.getIntegerValue();
                                        HiLog.debug(TAG, " include visibility is %{public}d", new Object[]{Integer.valueOf(i2)});
                                    } catch (IOException | NotExistException | WrongTypeException unused2) {
                                        HiLog.error(TAG, "get include visibility id failed!", new Object[0]);
                                        return;
                                    }
                                } else if (!z) {
                                    continue;
                                } else {
                                    HiLog.debug(TAG, " parse include layout", new Object[0]);
                                    if (typedAttribute2.getType() != 5) {
                                        HiLog.error(TAG, " invalid layout format!", new Object[0]);
                                        return;
                                    }
                                    try {
                                        solidXml = typedAttribute2.getLayoutValue();
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
                parseSolidXml.setLayoutConfig(componentContainer.createLayoutConfig(this.mContext, convertAttr(typedAttribute)));
                if (i2 == 0 || i2 == 4 || i2 == 8) {
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
}
