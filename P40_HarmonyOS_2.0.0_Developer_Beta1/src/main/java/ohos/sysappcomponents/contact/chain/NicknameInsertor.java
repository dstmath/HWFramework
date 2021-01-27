package ohos.sysappcomponents.contact.chain;

import java.util.ArrayList;
import java.util.stream.Stream;
import ohos.aafwk.ability.DataAbilityOperation;
import ohos.sysappcomponents.contact.Attribute;
import ohos.sysappcomponents.contact.chain.Insertor;
import ohos.sysappcomponents.contact.entity.Contact;
import ohos.sysappcomponents.contact.entity.NickName;

public class NicknameInsertor extends Insertor {
    @Override // ohos.sysappcomponents.contact.chain.Insertor
    public void fillOperation(Contact contact, ArrayList<DataAbilityOperation> arrayList, Insertor.OperationType operationType) {
        if (!Stream.of(contact, arrayList).anyMatch($$Lambda$NicknameInsertor$wLIh0GiBW9398cTP8uaTH8KoGwo.INSTANCE)) {
            if (contact.getNickName() == null) {
                fillNextItem(contact, arrayList, operationType);
                return;
            }
            NickName nickName = contact.getNickName();
            DataAbilityOperation.Builder predicatesBuilder = getPredicatesBuilder(Attribute.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE, operationType, nickName.getId());
            if (!isEmpty(contact.getNickName())) {
                fillStringContent(predicatesBuilder, "data1", nickName.getNickName());
            }
            arrayList.add(predicatesBuilder.build());
            fillNextItem(contact, arrayList, operationType);
        }
    }
}
