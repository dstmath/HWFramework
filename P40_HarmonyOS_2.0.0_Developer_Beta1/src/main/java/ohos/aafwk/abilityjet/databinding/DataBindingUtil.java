package ohos.aafwk.abilityjet.databinding;

import java.io.File;
import java.io.IOException;
import ohos.app.Context;
import ohos.global.resource.NotExistException;
import ohos.global.resource.ResourceManager;
import ohos.global.resource.WrongTypeException;
import ohos.hiviewdfx.HiLogLabel;

public class DataBindingUtil {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218108672, "DataBindingUtil:");
    private static final String XML_SUFFIX = ".sxml";

    private DataBindingUtil() {
    }

    public static <T extends DataBinding> T createBinding(int i, Context context) throws IOException {
        if (context != null) {
            ResourceManager resourceManager = context.getResourceManager();
            if (resourceManager != null) {
                try {
                    String string = resourceManager.getElement(i).getString();
                    if (string.endsWith(XML_SUFFIX)) {
                        String name = new File(string).getName();
                        return (T) DataBindingFactory.create(upperFirstLetter(name.substring(0, name.lastIndexOf(XML_SUFFIX))), context.getClassloader());
                    }
                    throw new IllegalArgumentException("layoutId is invalid.");
                } catch (NotExistException unused) {
                    throw new IllegalArgumentException("layoutId is not exist");
                } catch (WrongTypeException unused2) {
                    throw new IllegalArgumentException("layoutId is wrong type.");
                }
            } else {
                throw new IllegalArgumentException("resource manager is null.");
            }
        } else {
            throw new IllegalArgumentException("context is null.");
        }
    }

    private static String upperFirstLetter(String str) {
        char[] charArray = str.toCharArray();
        if (charArray[0] >= 'a' && charArray[0] <= 'z') {
            charArray[0] = (char) (charArray[0] - ' ');
        }
        return String.valueOf(charArray);
    }
}
