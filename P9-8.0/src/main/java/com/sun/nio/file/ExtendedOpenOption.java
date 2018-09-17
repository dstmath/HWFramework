package com.sun.nio.file;

import java.nio.file.OpenOption;

public enum ExtendedOpenOption implements OpenOption {
    NOSHARE_READ,
    NOSHARE_WRITE,
    NOSHARE_DELETE
}
