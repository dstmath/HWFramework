package ohos.sysappcomponents.contact.chain;

import java.util.ArrayList;
import java.util.stream.Stream;
import ohos.aafwk.ability.DataAbilityOperation;
import ohos.sysappcomponents.contact.Attribute;
import ohos.sysappcomponents.contact.chain.Insertor;
import ohos.sysappcomponents.contact.entity.Contact;
import ohos.sysappcomponents.contact.entity.Note;

public class NoteInsertor extends Insertor {
    @Override // ohos.sysappcomponents.contact.chain.Insertor
    public void fillOperation(Contact contact, ArrayList<DataAbilityOperation> arrayList, Insertor.OperationType operationType) {
        if (!Stream.of(contact, arrayList).anyMatch($$Lambda$NoteInsertor$wLIh0GiBW9398cTP8uaTH8KoGwo.INSTANCE)) {
            if (contact.getNote() == null) {
                fillNextItem(contact, arrayList, operationType);
                return;
            }
            Note note = contact.getNote();
            DataAbilityOperation.Builder predicatesBuilder = getPredicatesBuilder(Attribute.CommonDataKinds.Note.CONTENT_ITEM_TYPE, operationType, note.getId());
            if (!isEmpty(note.getNoteContent())) {
                fillStringContent(predicatesBuilder, "data1", note.getNoteContent());
            }
            arrayList.add(predicatesBuilder.build());
            fillNextItem(contact, arrayList, operationType);
        }
    }
}
