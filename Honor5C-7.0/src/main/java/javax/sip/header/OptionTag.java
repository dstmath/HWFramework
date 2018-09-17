package javax.sip.header;

import java.text.ParseException;

public interface OptionTag {
    String getOptionTag();

    void setOptionTag(String str) throws ParseException;
}
