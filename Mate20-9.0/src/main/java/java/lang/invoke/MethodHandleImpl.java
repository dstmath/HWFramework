package java.lang.invoke;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;

public class MethodHandleImpl extends MethodHandle implements Cloneable {
    private HandleInfo info;

    static class HandleInfo implements MethodHandleInfo {
        private final MethodHandle handle;
        private final Member member;

        HandleInfo(Member member2, MethodHandle handle2) {
            this.member = member2;
            this.handle = handle2;
        }

        public int getReferenceKind() {
            int handleKind = this.handle.getHandleKind();
            switch (handleKind) {
                case 0:
                    if (this.member.getDeclaringClass().isInterface()) {
                        return 9;
                    }
                    return 5;
                case 1:
                    return 7;
                case 2:
                    if (this.member instanceof Constructor) {
                        return 8;
                    }
                    return 7;
                case 3:
                    return 6;
                default:
                    switch (handleKind) {
                        case 9:
                            return 1;
                        case 10:
                            return 3;
                        case 11:
                            return 2;
                        case 12:
                            return 4;
                        default:
                            throw new AssertionError((Object) "Unexpected handle kind: " + this.handle.getHandleKind());
                    }
            }
        }

        public Class<?> getDeclaringClass() {
            return this.member.getDeclaringClass();
        }

        public String getName() {
            if (this.member instanceof Constructor) {
                return "<init>";
            }
            return this.member.getName();
        }

        public MethodType getMethodType() {
            MethodType handleType = this.handle.type();
            boolean omitLeadingParam = false;
            if (this.member instanceof Constructor) {
                handleType = handleType.changeReturnType(Void.TYPE);
                omitLeadingParam = true;
            }
            int handleKind = this.handle.getHandleKind();
            if (handleKind != 4) {
                switch (handleKind) {
                    case 0:
                    case 1:
                    case 2:
                        break;
                    default:
                        switch (handleKind) {
                            case 9:
                            case 10:
                                break;
                        }
                }
            }
            omitLeadingParam = true;
            return omitLeadingParam ? handleType.dropParameterTypes(0, 1) : handleType;
        }

        public <T extends Member> T reflectAs(Class<T> cls, MethodHandles.Lookup lookup) {
            try {
                lookup.checkAccess(this.member.getDeclaringClass(), this.member.getDeclaringClass(), this.member.getModifiers(), this.member.getName());
                return this.member;
            } catch (IllegalAccessException exception) {
                throw new IllegalArgumentException("Unable to access member.", exception);
            }
        }

        public int getModifiers() {
            return this.member.getModifiers();
        }
    }

    public native Member getMemberInternal();

    MethodHandleImpl(long artFieldOrMethod, int handleKind, MethodType type) {
        super(artFieldOrMethod, handleKind, type);
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /* access modifiers changed from: package-private */
    public MethodHandleInfo reveal() {
        if (this.info == null) {
            this.info = new HandleInfo(getMemberInternal(), this);
        }
        return this.info;
    }
}
