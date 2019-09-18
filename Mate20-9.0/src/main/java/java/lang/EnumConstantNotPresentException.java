package java.lang;

public class EnumConstantNotPresentException extends RuntimeException {
    private static final long serialVersionUID = -6046998521960521108L;
    private String constantName;
    private Class<? extends Enum> enumType;

    public EnumConstantNotPresentException(Class<? extends Enum> enumType2, String constantName2) {
        super(enumType2.getName() + "." + constantName2);
        this.enumType = enumType2;
        this.constantName = constantName2;
    }

    public Class<? extends Enum> enumType() {
        return this.enumType;
    }

    public String constantName() {
        return this.constantName;
    }
}
