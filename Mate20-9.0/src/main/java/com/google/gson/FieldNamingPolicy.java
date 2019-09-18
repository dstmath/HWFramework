package com.google.gson;

import java.lang.reflect.Field;
import java.util.Locale;

public enum FieldNamingPolicy implements FieldNamingStrategy {
    IDENTITY {
        public String translateName(Field f) {
            return f.getName();
        }
    },
    UPPER_CAMEL_CASE {
        public String translateName(Field f) {
            return upperCaseFirstLetter(f.getName());
        }
    },
    UPPER_CAMEL_CASE_WITH_SPACES {
        public String translateName(Field f) {
            return upperCaseFirstLetter(separateCamelCase(f.getName(), " "));
        }
    },
    LOWER_CASE_WITH_UNDERSCORES {
        public String translateName(Field f) {
            return separateCamelCase(f.getName(), "_").toLowerCase(Locale.ENGLISH);
        }
    },
    LOWER_CASE_WITH_DASHES {
        public String translateName(Field f) {
            return separateCamelCase(f.getName(), "-").toLowerCase(Locale.ENGLISH);
        }
    };

    static String separateCamelCase(String name, String separator) {
        StringBuilder translation = new StringBuilder();
        int length = name.length();
        for (int i = 0; i < length; i++) {
            char character = name.charAt(i);
            if (Character.isUpperCase(character) && translation.length() != 0) {
                translation.append(separator);
            }
            translation.append(character);
        }
        return translation.toString();
    }

    static String upperCaseFirstLetter(String name) {
        StringBuilder fieldNameBuilder = new StringBuilder();
        int index = 0;
        char firstCharacter = name.charAt(0);
        int length = name.length();
        while (index < length - 1 && !Character.isLetter(firstCharacter)) {
            fieldNameBuilder.append(firstCharacter);
            index++;
            firstCharacter = name.charAt(index);
        }
        if (!Character.isUpperCase(firstCharacter)) {
            return fieldNameBuilder.append(modifyString(Character.toUpperCase(firstCharacter), name, index + 1)).toString();
        }
        return name;
    }

    private static String modifyString(char firstCharacter, String srcString, int indexOfSubstring) {
        if (indexOfSubstring < srcString.length()) {
            return firstCharacter + srcString.substring(indexOfSubstring);
        }
        return String.valueOf(firstCharacter);
    }
}
