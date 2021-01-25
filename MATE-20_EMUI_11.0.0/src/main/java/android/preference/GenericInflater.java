package android.preference;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.preference.GenericInflater.Parent;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.InflateException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

@Deprecated
abstract class GenericInflater<T, P extends Parent> {
    private static final Class[] mConstructorSignature = {Context.class, AttributeSet.class};
    private static final HashMap sConstructorMap = new HashMap();
    private final boolean DEBUG;
    private final Object[] mConstructorArgs;
    protected final Context mContext;
    private String mDefaultPackage;
    private Factory<T> mFactory;
    private boolean mFactorySet;

    public interface Factory<T> {
        T onCreateItem(String str, Context context, AttributeSet attributeSet);
    }

    public interface Parent<T> {
        void addItemFromInflater(T t);
    }

    public abstract GenericInflater cloneInContext(Context context);

    private static class FactoryMerger<T> implements Factory<T> {
        private final Factory<T> mF1;
        private final Factory<T> mF2;

        FactoryMerger(Factory<T> f1, Factory<T> f2) {
            this.mF1 = f1;
            this.mF2 = f2;
        }

        @Override // android.preference.GenericInflater.Factory
        public T onCreateItem(String name, Context context, AttributeSet attrs) {
            T v = this.mF1.onCreateItem(name, context, attrs);
            if (v != null) {
                return v;
            }
            return this.mF2.onCreateItem(name, context, attrs);
        }
    }

    protected GenericInflater(Context context) {
        this.DEBUG = false;
        this.mConstructorArgs = new Object[2];
        this.mContext = context;
    }

    protected GenericInflater(GenericInflater<T, P> original, Context newContext) {
        this.DEBUG = false;
        this.mConstructorArgs = new Object[2];
        this.mContext = newContext;
        this.mFactory = original.mFactory;
    }

    public void setDefaultPackage(String defaultPackage) {
        this.mDefaultPackage = defaultPackage;
    }

    public String getDefaultPackage() {
        return this.mDefaultPackage;
    }

    public Context getContext() {
        return this.mContext;
    }

    public final Factory<T> getFactory() {
        return this.mFactory;
    }

    public void setFactory(Factory<T> factory) {
        if (this.mFactorySet) {
            throw new IllegalStateException("A factory has already been set on this inflater");
        } else if (factory != null) {
            this.mFactorySet = true;
            Factory<T> factory2 = this.mFactory;
            if (factory2 == null) {
                this.mFactory = factory;
            } else {
                this.mFactory = new FactoryMerger(factory, factory2);
            }
        } else {
            throw new NullPointerException("Given factory can not be null");
        }
    }

    public T inflate(int resource, P root) {
        return inflate(resource, (int) root, root != null);
    }

    public T inflate(XmlPullParser parser, P root) {
        return inflate(parser, (XmlPullParser) root, root != null);
    }

    public T inflate(int resource, P root, boolean attachToRoot) {
        XmlResourceParser parser = getContext().getResources().getXml(resource);
        try {
            return inflate((XmlPullParser) parser, (XmlResourceParser) root, attachToRoot);
        } finally {
            parser.close();
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r7v0, resolved type: android.preference.GenericInflater<T, P extends android.preference.GenericInflater$Parent> */
    /* JADX WARN: Multi-variable type inference failed */
    public T inflate(XmlPullParser parser, P root, boolean attachToRoot) {
        int type;
        T result;
        synchronized (this.mConstructorArgs) {
            AttributeSet attrs = Xml.asAttributeSet(parser);
            this.mConstructorArgs[0] = this.mContext;
            while (true) {
                try {
                    type = parser.next();
                } catch (InflateException e) {
                    throw e;
                } catch (XmlPullParserException e2) {
                    InflateException ex = new InflateException(e2.getMessage());
                    ex.initCause(e2);
                    throw ex;
                } catch (IOException e3) {
                    InflateException ex2 = new InflateException(parser.getPositionDescription() + ": " + e3.getMessage());
                    ex2.initCause(e3);
                    throw ex2;
                }
                if (type == 2 || type == 1) {
                    break;
                }
            }
            if (type == 2) {
                result = (T) onMergeRoots(root, attachToRoot, createItemFromTag(parser, parser.getName(), attrs));
                rInflate(parser, result, attrs);
            } else {
                throw new InflateException(parser.getPositionDescription() + ": No start tag found!");
            }
        }
        return result;
    }

    /* JADX INFO: Multiple debug info for r3v0 java.lang.Object[]: [D('args' java.lang.Object[]), D('clazz' java.lang.Class)] */
    public final T createItem(String name, String prefix, AttributeSet attrs) throws ClassNotFoundException, InflateException {
        String str;
        String str2;
        Constructor constructor = (Constructor) sConstructorMap.get(name);
        if (constructor == null) {
            try {
                ClassLoader classLoader = this.mContext.getClassLoader();
                if (prefix != null) {
                    str2 = prefix + name;
                } else {
                    str2 = name;
                }
                constructor = classLoader.loadClass(str2).getConstructor(mConstructorSignature);
                constructor.setAccessible(true);
                sConstructorMap.put(name, constructor);
            } catch (NoSuchMethodException e) {
                StringBuilder sb = new StringBuilder();
                sb.append(attrs.getPositionDescription());
                sb.append(": Error inflating class ");
                if (prefix != null) {
                    str = prefix + name;
                } else {
                    str = name;
                }
                sb.append(str);
                InflateException ie = new InflateException(sb.toString());
                ie.initCause(e);
                throw ie;
            } catch (ClassNotFoundException e2) {
                throw e2;
            } catch (Exception e3) {
                InflateException ie2 = new InflateException(attrs.getPositionDescription() + ": Error inflating class " + constructor.getClass().getName());
                ie2.initCause(e3);
                throw ie2;
            }
        }
        Object[] args = this.mConstructorArgs;
        args[1] = attrs;
        return constructor.newInstance(args);
    }

    /* access modifiers changed from: protected */
    public T onCreateItem(String name, AttributeSet attrs) throws ClassNotFoundException {
        return createItem(name, this.mDefaultPackage, attrs);
    }

    private final T createItemFromTag(XmlPullParser parser, String name, AttributeSet attrs) {
        try {
            T item = this.mFactory == null ? null : this.mFactory.onCreateItem(name, this.mContext, attrs);
            if (item != null) {
                return item;
            }
            if (-1 == name.indexOf(46)) {
                return onCreateItem(name, attrs);
            }
            return createItem(name, null, attrs);
        } catch (InflateException e) {
            throw e;
        } catch (ClassNotFoundException e2) {
            InflateException ie = new InflateException(attrs.getPositionDescription() + ": Error inflating class " + name);
            ie.initCause(e2);
            throw ie;
        } catch (Exception e3) {
            InflateException ie2 = new InflateException(attrs.getPositionDescription() + ": Error inflating class " + name);
            ie2.initCause(e3);
            throw ie2;
        }
    }

    private void rInflate(XmlPullParser parser, T parent, AttributeSet attrs) throws XmlPullParserException, IOException {
        int depth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if ((type == 3 && parser.getDepth() <= depth) || type == 1) {
                return;
            }
            if (type == 2 && !onCreateCustomFromTag(parser, parent, attrs)) {
                T item = createItemFromTag(parser, parser.getName(), attrs);
                parent.addItemFromInflater(item);
                rInflate(parser, item, attrs);
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean onCreateCustomFromTag(XmlPullParser parser, T t, AttributeSet attrs) throws XmlPullParserException {
        return false;
    }

    /* access modifiers changed from: protected */
    public P onMergeRoots(P p, boolean attachToGivenRoot, P xmlRoot) {
        return xmlRoot;
    }
}
