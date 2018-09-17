package java.text;

import java.text.Format.Field;
import java.util.ArrayList;

class CharacterIteratorFieldDelegate implements FieldDelegate {
    private ArrayList<AttributedString> attributedStrings = new ArrayList();
    private int size;

    CharacterIteratorFieldDelegate() {
    }

    public void formatted(Field attr, Object value, int start, int end, StringBuffer buffer) {
        if (start != end) {
            if (start < this.size) {
                int index = this.size;
                int asIndex = this.attributedStrings.size() - 1;
                while (start < index) {
                    int asIndex2 = asIndex - 1;
                    AttributedString as = (AttributedString) this.attributedStrings.get(asIndex);
                    int newIndex = index - as.length();
                    int aStart = Math.max(0, start - newIndex);
                    as.addAttribute(attr, value, aStart, Math.min(end - start, as.length() - aStart) + aStart);
                    index = newIndex;
                    asIndex = asIndex2;
                }
            }
            if (this.size < start) {
                this.attributedStrings.add(new AttributedString(buffer.substring(this.size, start)));
                this.size = start;
            }
            if (this.size < end) {
                AttributedString string = new AttributedString(buffer.substring(Math.max(start, this.size), end));
                string.addAttribute(attr, value);
                this.attributedStrings.add(string);
                this.size = end;
            }
        }
    }

    public void formatted(int fieldID, Field attr, Object value, int start, int end, StringBuffer buffer) {
        formatted(attr, value, start, end, buffer);
    }

    public AttributedCharacterIterator getIterator(String string) {
        if (string.length() > this.size) {
            this.attributedStrings.add(new AttributedString(string.substring(this.size)));
            this.size = string.length();
        }
        int iCount = this.attributedStrings.size();
        AttributedCharacterIterator[] iterators = new AttributedCharacterIterator[iCount];
        for (int counter = 0; counter < iCount; counter++) {
            iterators[counter] = ((AttributedString) this.attributedStrings.get(counter)).getIterator();
        }
        return new AttributedString(iterators).getIterator();
    }
}
