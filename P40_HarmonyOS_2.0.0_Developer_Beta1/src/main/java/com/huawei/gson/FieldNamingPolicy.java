package com.huawei.gson;

import java.lang.reflect.Field;
import java.util.Locale;

public enum FieldNamingPolicy implements FieldNamingStrategy {
    IDENTITY {
        @Override // com.huawei.gson.FieldNamingStrategy
        public String translateName(Field f) {
            return f.getName();
        }
    },
    UPPER_CAMEL_CASE {
        @Override // com.huawei.gson.FieldNamingStrategy
        public String translateName(Field f) {
            return upperCaseFirstLetter(f.getName());
        }
    },
    UPPER_CAMEL_CASE_WITH_SPACES {
        @Override // com.huawei.gson.FieldNamingStrategy
        public String translateName(Field f) {
            return upperCaseFirstLetter(separateCamelCase(f.getName(), " "));
        }
    },
    LOWER_CASE_WITH_UNDERSCORES {
        @Override // com.huawei.gson.FieldNamingStrategy
        public String translateName(Field f) {
            return separateCamelCase(f.getName(), "_").toLowerCase(Locale.ENGLISH);
        }
    },
    LOWER_CASE_WITH_DASHES {
        @Override // com.huawei.gson.FieldNamingStrategy
        public String translateName(Field f) {
            return separateCamelCase(f.getName(), "-").toLowerCase(Locale.ENGLISH);
        }
    },
    LOWER_CASE_WITH_DOTS {
        @Override // com.huawei.gson.FieldNamingStrategy
        public String translateName(Field f) {
            return separateCamelCase(f.getName(), ".").toLowerCase(Locale.ENGLISH);
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
        int firstLetterIndex = 0;
        int limit = name.length() - 1;
        while (!Character.isLetter(name.charAt(firstLetterIndex)) && firstLetterIndex < limit) {
            firstLetterIndex++;
        }
        char firstLetter = name.charAt(firstLetterIndex);
        if (Character.isUpperCase(firstLetter)) {
            return name;
        }
        char uppercased = Character.toUpperCase(firstLetter);
        if (firstLetterIndex == 0) {
            return uppercased + name.substring(1);
        }
        return name.substring(0, firstLetterIndex) + uppercased + name.substring(firstLetterIndex + 1);
    }
}
