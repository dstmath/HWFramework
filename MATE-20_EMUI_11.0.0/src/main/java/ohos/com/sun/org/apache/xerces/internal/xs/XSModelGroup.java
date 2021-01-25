package ohos.com.sun.org.apache.xerces.internal.xs;

public interface XSModelGroup extends XSTerm {
    public static final short COMPOSITOR_ALL = 3;
    public static final short COMPOSITOR_CHOICE = 2;
    public static final short COMPOSITOR_SEQUENCE = 1;

    XSAnnotation getAnnotation();

    XSObjectList getAnnotations();

    short getCompositor();

    XSObjectList getParticles();
}
