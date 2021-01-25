package ohos.aafwk.abilityjet.databinding;

import java.io.File;
import java.io.IOException;

public class DataBindingUtil {
    private static final String XML_SUFFIX = ".xml";

    private DataBindingUtil() {
    }

    public static <T extends DataBinding> T createBinding(String str, ClassLoader classLoader) throws IOException {
        if (str != null) {
            File file = new File(str.trim());
            if (!file.isFile()) {
                throw new IllegalArgumentException("Layout path is not a file.");
            } else if (file.getCanonicalPath().endsWith(XML_SUFFIX)) {
                String name = file.getName();
                return (T) DataBindingFactory.create(upperFirstLetter(name.substring(0, name.lastIndexOf(XML_SUFFIX))), classLoader);
            } else {
                throw new IllegalArgumentException("Layout path is not a xml file.");
            }
        } else {
            throw new IllegalArgumentException("Layout path is null.");
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
