package android.view;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.BlendMode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import android.view.MenuItem;
import com.android.internal.R;
import com.android.internal.view.menu.MenuItemImpl;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class MenuInflater {
    private static final Class<?>[] ACTION_PROVIDER_CONSTRUCTOR_SIGNATURE = ACTION_VIEW_CONSTRUCTOR_SIGNATURE;
    private static final Class<?>[] ACTION_VIEW_CONSTRUCTOR_SIGNATURE = {Context.class};
    private static final String LOG_TAG = "MenuInflater";
    private static final int NO_ID = 0;
    private static final String XML_GROUP = "group";
    private static final String XML_ITEM = "item";
    private static final String XML_MENU = "menu";
    private final Object[] mActionProviderConstructorArguments = this.mActionViewConstructorArguments;
    private final Object[] mActionViewConstructorArguments;
    private Context mContext;
    private Object mRealOwner;

    public MenuInflater(Context context) {
        this.mContext = context;
        this.mActionViewConstructorArguments = new Object[]{context};
    }

    public MenuInflater(Context context, Object realOwner) {
        this.mContext = context;
        this.mRealOwner = realOwner;
        this.mActionViewConstructorArguments = new Object[]{context};
    }

    public void inflate(int menuRes, Menu menu) {
        XmlResourceParser parser = null;
        try {
            parser = this.mContext.getResources().getLayout(menuRes);
            parseMenu(parser, Xml.asAttributeSet(parser), menu);
            if (parser != null) {
                parser.close();
            }
        } catch (XmlPullParserException e) {
            throw new InflateException("Error inflating menu XML", e);
        } catch (IOException e2) {
            throw new InflateException("Error inflating menu XML", e2);
        } catch (Throwable th) {
            if (parser != null) {
                parser.close();
            }
            throw th;
        }
    }

    private void parseMenu(XmlPullParser parser, AttributeSet attrs, Menu menu) throws XmlPullParserException, IOException {
        MenuState menuState = new MenuState(menu);
        int eventType = parser.getEventType();
        boolean lookingForEndOfUnknownTag = false;
        String unknownTagName = null;
        while (true) {
            if (eventType != 2) {
                eventType = parser.next();
                if (eventType == 1) {
                    break;
                }
            } else {
                String tagName = parser.getName();
                if (tagName.equals(XML_MENU)) {
                    eventType = parser.next();
                } else {
                    throw new RuntimeException("Expecting menu, got " + tagName);
                }
            }
        }
        boolean reachedEndOfMenu = false;
        while (!reachedEndOfMenu) {
            if (eventType != 1) {
                if (eventType != 2) {
                    if (eventType == 3) {
                        String tagName2 = parser.getName();
                        if (lookingForEndOfUnknownTag && tagName2.equals(unknownTagName)) {
                            lookingForEndOfUnknownTag = false;
                            unknownTagName = null;
                        } else if (tagName2.equals("group")) {
                            menuState.resetGroup();
                        } else if (tagName2.equals("item")) {
                            if (!menuState.hasAddedItem()) {
                                if (menuState.itemActionProvider == null || !menuState.itemActionProvider.hasSubMenu()) {
                                    registerMenu(menuState.addItem(), attrs);
                                } else {
                                    registerMenu(menuState.addSubMenuItem(), attrs);
                                }
                            }
                        } else if (tagName2.equals(XML_MENU)) {
                            reachedEndOfMenu = true;
                        }
                    }
                } else if (!lookingForEndOfUnknownTag) {
                    String tagName3 = parser.getName();
                    if (tagName3.equals("group")) {
                        menuState.readGroup(attrs);
                    } else if (tagName3.equals("item")) {
                        menuState.readItem(attrs);
                    } else if (tagName3.equals(XML_MENU)) {
                        SubMenu subMenu = menuState.addSubMenuItem();
                        registerMenu(subMenu, attrs);
                        parseMenu(parser, attrs, subMenu);
                    } else {
                        lookingForEndOfUnknownTag = true;
                        unknownTagName = tagName3;
                    }
                }
                eventType = parser.next();
            } else {
                throw new RuntimeException("Unexpected end of document");
            }
        }
    }

    private void registerMenu(MenuItem item, AttributeSet set) {
    }

    private void registerMenu(SubMenu subMenu, AttributeSet set) {
    }

    /* access modifiers changed from: package-private */
    public Context getContext() {
        return this.mContext;
    }

    /* access modifiers changed from: private */
    public static class InflatedOnMenuItemClickListener implements MenuItem.OnMenuItemClickListener {
        private static final Class<?>[] PARAM_TYPES = {MenuItem.class};
        private Method mMethod;
        private Object mRealOwner;

        public InflatedOnMenuItemClickListener(Object realOwner, String methodName) {
            this.mRealOwner = realOwner;
            Class<?> c = realOwner.getClass();
            try {
                this.mMethod = c.getMethod(methodName, PARAM_TYPES);
            } catch (Exception e) {
                InflateException ex = new InflateException("Couldn't resolve menu item onClick handler " + methodName + " in class " + c.getName());
                ex.initCause(e);
                throw ex;
            }
        }

        @Override // android.view.MenuItem.OnMenuItemClickListener
        public boolean onMenuItemClick(MenuItem item) {
            try {
                if (this.mMethod.getReturnType() == Boolean.TYPE) {
                    return ((Boolean) this.mMethod.invoke(this.mRealOwner, item)).booleanValue();
                }
                this.mMethod.invoke(this.mRealOwner, item);
                return true;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Object getRealOwner() {
        if (this.mRealOwner == null) {
            this.mRealOwner = findRealOwner(this.mContext);
        }
        return this.mRealOwner;
    }

    private Object findRealOwner(Object owner) {
        if (!(owner instanceof Activity) && (owner instanceof ContextWrapper)) {
            return findRealOwner(((ContextWrapper) owner).getBaseContext());
        }
        return owner;
    }

    /* access modifiers changed from: private */
    public class MenuState {
        private static final int defaultGroupId = 0;
        private static final int defaultItemCategory = 0;
        private static final int defaultItemCheckable = 0;
        private static final boolean defaultItemChecked = false;
        private static final boolean defaultItemEnabled = true;
        private static final int defaultItemId = 0;
        private static final int defaultItemOrder = 0;
        private static final boolean defaultItemVisible = true;
        private int groupCategory;
        private int groupCheckable;
        private boolean groupEnabled;
        private int groupId;
        private int groupOrder;
        private boolean groupVisible;
        private ActionProvider itemActionProvider;
        private String itemActionProviderClassName;
        private String itemActionViewClassName;
        private int itemActionViewLayout;
        private boolean itemAdded;
        private int itemAlphabeticModifiers;
        private char itemAlphabeticShortcut;
        private int itemCategoryOrder;
        private int itemCheckable;
        private boolean itemChecked;
        private CharSequence itemContentDescription;
        private boolean itemEnabled;
        private int itemIconResId;
        private ColorStateList itemIconTintList = null;
        private int itemId;
        private String itemListenerMethodName;
        private int itemNumericModifiers;
        private char itemNumericShortcut;
        private int itemShowAsAction;
        private CharSequence itemTitle;
        private CharSequence itemTitleCondensed;
        private CharSequence itemTooltipText;
        private boolean itemVisible;
        private BlendMode mItemIconBlendMode = null;
        private Menu menu;

        public MenuState(Menu menu2) {
            this.menu = menu2;
            resetGroup();
        }

        public void resetGroup() {
            this.groupId = 0;
            this.groupCategory = 0;
            this.groupOrder = 0;
            this.groupCheckable = 0;
            this.groupVisible = true;
            this.groupEnabled = true;
        }

        public void readGroup(AttributeSet attrs) {
            TypedArray a = MenuInflater.this.mContext.obtainStyledAttributes(attrs, R.styleable.MenuGroup);
            this.groupId = a.getResourceId(1, 0);
            this.groupCategory = a.getInt(3, 0);
            this.groupOrder = a.getInt(4, 0);
            this.groupCheckable = a.getInt(5, 0);
            this.groupVisible = a.getBoolean(2, true);
            this.groupEnabled = a.getBoolean(0, true);
            a.recycle();
        }

        public void readItem(AttributeSet attrs) {
            TypedArray a = MenuInflater.this.mContext.obtainStyledAttributes(attrs, R.styleable.MenuItem);
            this.itemId = a.getResourceId(2, 0);
            this.itemCategoryOrder = (-65536 & a.getInt(5, this.groupCategory)) | (65535 & a.getInt(6, this.groupOrder));
            this.itemTitle = a.getText(7);
            this.itemTitleCondensed = a.getText(8);
            this.itemIconResId = a.getResourceId(0, 0);
            if (a.hasValue(22)) {
                this.mItemIconBlendMode = Drawable.parseBlendMode(a.getInt(22, -1), this.mItemIconBlendMode);
            } else {
                this.mItemIconBlendMode = null;
            }
            if (a.hasValue(21)) {
                this.itemIconTintList = a.getColorStateList(21);
            } else {
                this.itemIconTintList = null;
            }
            this.itemAlphabeticShortcut = getShortcut(a.getString(9));
            this.itemAlphabeticModifiers = a.getInt(19, 4096);
            this.itemNumericShortcut = getShortcut(a.getString(10));
            this.itemNumericModifiers = a.getInt(20, 4096);
            if (a.hasValue(11)) {
                this.itemCheckable = a.getBoolean(11, false) ? 1 : 0;
            } else {
                this.itemCheckable = this.groupCheckable;
            }
            this.itemChecked = a.getBoolean(3, false);
            this.itemVisible = a.getBoolean(4, this.groupVisible);
            boolean hasActionProvider = true;
            this.itemEnabled = a.getBoolean(1, this.groupEnabled);
            this.itemShowAsAction = a.getInt(14, -1);
            this.itemListenerMethodName = a.getString(12);
            this.itemActionViewLayout = a.getResourceId(15, 0);
            this.itemActionViewClassName = a.getString(16);
            this.itemActionProviderClassName = a.getString(17);
            if (this.itemActionProviderClassName == null) {
                hasActionProvider = false;
            }
            if (hasActionProvider && this.itemActionViewLayout == 0 && this.itemActionViewClassName == null) {
                this.itemActionProvider = (ActionProvider) newInstance(this.itemActionProviderClassName, MenuInflater.ACTION_PROVIDER_CONSTRUCTOR_SIGNATURE, MenuInflater.this.mActionProviderConstructorArguments);
            } else {
                if (hasActionProvider) {
                    Log.w(MenuInflater.LOG_TAG, "Ignoring attribute 'actionProviderClass'. Action view already specified.");
                }
                this.itemActionProvider = null;
            }
            this.itemContentDescription = a.getText(13);
            this.itemTooltipText = a.getText(18);
            a.recycle();
            this.itemAdded = false;
        }

        private char getShortcut(String shortcutString) {
            if (shortcutString == null) {
                return 0;
            }
            return shortcutString.charAt(0);
        }

        private void setItem(MenuItem item) {
            item.setChecked(this.itemChecked).setVisible(this.itemVisible).setEnabled(this.itemEnabled).setCheckable(this.itemCheckable >= 1).setTitleCondensed(this.itemTitleCondensed).setIcon(this.itemIconResId).setAlphabeticShortcut(this.itemAlphabeticShortcut, this.itemAlphabeticModifiers).setNumericShortcut(this.itemNumericShortcut, this.itemNumericModifiers);
            int i = this.itemShowAsAction;
            if (i >= 0) {
                item.setShowAsAction(i);
            }
            BlendMode blendMode = this.mItemIconBlendMode;
            if (blendMode != null) {
                item.setIconTintBlendMode(blendMode);
            }
            ColorStateList colorStateList = this.itemIconTintList;
            if (colorStateList != null) {
                item.setIconTintList(colorStateList);
            }
            if (this.itemListenerMethodName != null) {
                if (!MenuInflater.this.mContext.isRestricted()) {
                    item.setOnMenuItemClickListener(new InflatedOnMenuItemClickListener(MenuInflater.this.getRealOwner(), this.itemListenerMethodName));
                } else {
                    throw new IllegalStateException("The android:onClick attribute cannot be used within a restricted context");
                }
            }
            if (item instanceof MenuItemImpl) {
                MenuItemImpl impl = (MenuItemImpl) item;
                if (this.itemCheckable >= 2) {
                    impl.setExclusiveCheckable(true);
                }
            }
            boolean actionViewSpecified = false;
            String str = this.itemActionViewClassName;
            if (str != null) {
                item.setActionView((View) newInstance(str, MenuInflater.ACTION_VIEW_CONSTRUCTOR_SIGNATURE, MenuInflater.this.mActionViewConstructorArguments));
                actionViewSpecified = true;
            }
            int i2 = this.itemActionViewLayout;
            if (i2 > 0) {
                if (!actionViewSpecified) {
                    item.setActionView(i2);
                } else {
                    Log.w(MenuInflater.LOG_TAG, "Ignoring attribute 'itemActionViewLayout'. Action view already specified.");
                }
            }
            ActionProvider actionProvider = this.itemActionProvider;
            if (actionProvider != null) {
                item.setActionProvider(actionProvider);
            }
            item.setContentDescription(this.itemContentDescription);
            item.setTooltipText(this.itemTooltipText);
        }

        public MenuItem addItem() {
            this.itemAdded = true;
            MenuItem item = this.menu.add(this.groupId, this.itemId, this.itemCategoryOrder, this.itemTitle);
            setItem(item);
            return item;
        }

        public SubMenu addSubMenuItem() {
            this.itemAdded = true;
            SubMenu subMenu = this.menu.addSubMenu(this.groupId, this.itemId, this.itemCategoryOrder, this.itemTitle);
            setItem(subMenu.getItem());
            return subMenu;
        }

        public boolean hasAddedItem() {
            return this.itemAdded;
        }

        private <T> T newInstance(String className, Class<?>[] constructorSignature, Object[] arguments) {
            try {
                Constructor<?> constructor = MenuInflater.this.mContext.getClassLoader().loadClass(className).getConstructor(constructorSignature);
                constructor.setAccessible(true);
                return (T) constructor.newInstance(arguments);
            } catch (Exception e) {
                Log.w(MenuInflater.LOG_TAG, "Cannot instantiate class: " + className, e);
                return null;
            }
        }
    }
}
