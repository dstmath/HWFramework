package libcore.tzdata.shared2;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DistroVersion {
    public static final int CURRENT_FORMAT_MAJOR_VERSION = 1;
    public static final int CURRENT_FORMAT_MINOR_VERSION = 1;
    public static final int DISTRO_VERSION_FILE_LENGTH = ((((FORMAT_VERSION_STRING_LENGTH + 1) + 5) + 1) + 3);
    private static final Pattern DISTRO_VERSION_PATTERN = Pattern.compile(FORMAT_VERSION_PATTERN.pattern() + "\\|" + RULES_VERSION_PATTERN.pattern() + "\\|" + REVISION_PATTERN.pattern() + ".*");
    private static final Pattern FORMAT_VERSION_PATTERN = Pattern.compile("(\\d{3})\\.(\\d{3})");
    private static final int FORMAT_VERSION_STRING_LENGTH = FULL_CURRENT_FORMAT_VERSION_STRING.length();
    private static final String FULL_CURRENT_FORMAT_VERSION_STRING = toFormatVersionString(1, 1);
    private static final int REVISION_LENGTH = 3;
    private static final Pattern REVISION_PATTERN = Pattern.compile("(\\d{3})");
    private static final int RULES_VERSION_LENGTH = 5;
    private static final Pattern RULES_VERSION_PATTERN = Pattern.compile("(\\d{4}\\w)");
    public final int formatMajorVersion;
    public final int formatMinorVersion;
    public final int revision;
    public final String rulesVersion;

    public DistroVersion(int formatMajorVersion, int formatMinorVersion, String rulesVersion, int revision) throws DistroException {
        this.formatMajorVersion = validate3DigitVersion(formatMajorVersion);
        this.formatMinorVersion = validate3DigitVersion(formatMinorVersion);
        if (RULES_VERSION_PATTERN.matcher(rulesVersion).matches()) {
            this.rulesVersion = rulesVersion;
            this.revision = validate3DigitVersion(revision);
            return;
        }
        throw new DistroException("Invalid rulesVersion: " + rulesVersion);
    }

    public static DistroVersion fromBytes(byte[] bytes) throws DistroException {
        String distroVersion = new String(bytes, StandardCharsets.US_ASCII);
        try {
            Matcher matcher = DISTRO_VERSION_PATTERN.matcher(distroVersion);
            if (matcher.matches()) {
                String formatMajorVersion = matcher.group(1);
                String formatMinorVersion = matcher.group(2);
                return new DistroVersion(from3DigitVersionString(formatMajorVersion), from3DigitVersionString(formatMinorVersion), matcher.group(3), from3DigitVersionString(matcher.group(4)));
            }
            throw new DistroException("Invalid distro version string: " + distroVersion);
        } catch (IndexOutOfBoundsException e) {
            throw new DistroException("Distro version string too short:" + distroVersion);
        }
    }

    public byte[] toBytes() {
        return toBytes(this.formatMajorVersion, this.formatMinorVersion, this.rulesVersion, this.revision);
    }

    public static byte[] toBytes(int majorFormatVersion, int minorFormatVerison, String rulesVersion, int revision) {
        return (toFormatVersionString(majorFormatVersion, minorFormatVerison) + "|" + rulesVersion + "|" + to3DigitVersionString(revision)).getBytes(StandardCharsets.US_ASCII);
    }

    public static boolean isCompatibleWithThisDevice(DistroVersion distroVersion) {
        if (1 == distroVersion.formatMajorVersion) {
            return 1 <= distroVersion.formatMinorVersion;
        } else {
            return false;
        }
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DistroVersion that = (DistroVersion) o;
        if (this.formatMajorVersion == that.formatMajorVersion && this.formatMinorVersion == that.formatMinorVersion && this.revision == that.revision) {
            return this.rulesVersion.equals(that.rulesVersion);
        }
        return false;
    }

    public int hashCode() {
        return (((((this.formatMajorVersion * 31) + this.formatMinorVersion) * 31) + this.rulesVersion.hashCode()) * 31) + this.revision;
    }

    public String toString() {
        return "DistroVersion{formatMajorVersion=" + this.formatMajorVersion + ", formatMinorVersion=" + this.formatMinorVersion + ", rulesVersion='" + this.rulesVersion + '\'' + ", revision=" + this.revision + '}';
    }

    private static String to3DigitVersionString(int version) {
        try {
            return String.format(Locale.ROOT, "%03d", new Object[]{Integer.valueOf(validate3DigitVersion(version))});
        } catch (DistroException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static int from3DigitVersionString(String versionString) throws DistroException {
        String parseErrorMessage = "versionString must be a zero padded, 3 digit, positive decimal integer";
        if (versionString.length() != 3) {
            throw new DistroException("versionString must be a zero padded, 3 digit, positive decimal integer");
        }
        try {
            return validate3DigitVersion(Integer.parseInt(versionString));
        } catch (NumberFormatException e) {
            throw new DistroException("versionString must be a zero padded, 3 digit, positive decimal integer", e);
        }
    }

    private static int validate3DigitVersion(int value) throws DistroException {
        if (value >= 0 && value <= 999) {
            return value;
        }
        throw new DistroException("Expected 0 <= value <= 999, was " + value);
    }

    private static String toFormatVersionString(int majorFormatVersion, int minorFormatVersion) {
        return to3DigitVersionString(majorFormatVersion) + "." + to3DigitVersionString(minorFormatVersion);
    }
}
