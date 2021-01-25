package android.test.mock;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Movie;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import java.io.InputStream;

@Deprecated
public class MockResources extends Resources {
    public MockResources() {
        super(new AssetManager(), null, null);
    }

    @Override // android.content.res.Resources
    public void updateConfiguration(Configuration config, DisplayMetrics metrics) {
    }

    @Override // android.content.res.Resources
    public CharSequence getText(int id) throws Resources.NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }

    @Override // android.content.res.Resources
    public CharSequence getQuantityText(int id, int quantity) throws Resources.NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }

    @Override // android.content.res.Resources
    public String getString(int id) throws Resources.NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }

    @Override // android.content.res.Resources
    public String getString(int id, Object... formatArgs) throws Resources.NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }

    @Override // android.content.res.Resources
    public String getQuantityString(int id, int quantity, Object... formatArgs) throws Resources.NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }

    @Override // android.content.res.Resources
    public String getQuantityString(int id, int quantity) throws Resources.NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }

    @Override // android.content.res.Resources
    public CharSequence getText(int id, CharSequence def) {
        throw new UnsupportedOperationException("mock object, not implemented");
    }

    @Override // android.content.res.Resources
    public CharSequence[] getTextArray(int id) throws Resources.NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }

    @Override // android.content.res.Resources
    public String[] getStringArray(int id) throws Resources.NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }

    @Override // android.content.res.Resources
    public int[] getIntArray(int id) throws Resources.NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }

    @Override // android.content.res.Resources
    public TypedArray obtainTypedArray(int id) throws Resources.NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }

    @Override // android.content.res.Resources
    public float getDimension(int id) throws Resources.NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }

    @Override // android.content.res.Resources
    public int getDimensionPixelOffset(int id) throws Resources.NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }

    @Override // android.content.res.Resources
    public int getDimensionPixelSize(int id) throws Resources.NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }

    @Override // android.content.res.Resources
    public Drawable getDrawable(int id) throws Resources.NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }

    @Override // android.content.res.Resources
    public Movie getMovie(int id) throws Resources.NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }

    @Override // android.content.res.Resources
    public int getColor(int id) throws Resources.NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }

    @Override // android.content.res.Resources
    public ColorStateList getColorStateList(int id) throws Resources.NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }

    @Override // android.content.res.Resources
    public int getInteger(int id) throws Resources.NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }

    @Override // android.content.res.Resources
    public XmlResourceParser getLayout(int id) throws Resources.NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }

    @Override // android.content.res.Resources
    public XmlResourceParser getAnimation(int id) throws Resources.NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }

    @Override // android.content.res.Resources
    public XmlResourceParser getXml(int id) throws Resources.NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }

    @Override // android.content.res.Resources
    public InputStream openRawResource(int id) throws Resources.NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }

    @Override // android.content.res.Resources
    public AssetFileDescriptor openRawResourceFd(int id) throws Resources.NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }

    @Override // android.content.res.Resources
    public void getValue(int id, TypedValue outValue, boolean resolveRefs) throws Resources.NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }

    @Override // android.content.res.Resources
    public void getValue(String name, TypedValue outValue, boolean resolveRefs) throws Resources.NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }

    @Override // android.content.res.Resources
    public TypedArray obtainAttributes(AttributeSet set, int[] attrs) {
        throw new UnsupportedOperationException("mock object, not implemented");
    }

    @Override // android.content.res.Resources
    public DisplayMetrics getDisplayMetrics() {
        throw new UnsupportedOperationException("mock object, not implemented");
    }

    @Override // android.content.res.Resources
    public Configuration getConfiguration() {
        throw new UnsupportedOperationException("mock object, not implemented");
    }

    @Override // android.content.res.Resources
    public int getIdentifier(String name, String defType, String defPackage) {
        throw new UnsupportedOperationException("mock object, not implemented");
    }

    @Override // android.content.res.Resources
    public String getResourceName(int resid) throws Resources.NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }

    @Override // android.content.res.Resources
    public String getResourcePackageName(int resid) throws Resources.NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }

    @Override // android.content.res.Resources
    public String getResourceTypeName(int resid) throws Resources.NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }

    @Override // android.content.res.Resources
    public String getResourceEntryName(int resid) throws Resources.NotFoundException {
        throw new UnsupportedOperationException("mock object, not implemented");
    }
}
