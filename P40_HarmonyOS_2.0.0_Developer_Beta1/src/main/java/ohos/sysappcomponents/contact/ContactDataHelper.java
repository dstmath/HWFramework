package ohos.sysappcomponents.contact;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import ohos.aafwk.ability.DataAbilityHelper;
import ohos.aafwk.ability.DataAbilityOperation;
import ohos.aafwk.ability.DataAbilityRemoteException;
import ohos.aafwk.ability.DataAbilityResult;
import ohos.aafwk.ability.OperationExecuteException;
import ohos.app.Context;
import ohos.data.dataability.DataAbilityPredicates;
import ohos.data.resultset.ResultSet;
import ohos.net.UriConverter;
import ohos.sysappcomponents.contact.Attribute;
import ohos.sysappcomponents.contact.ContactAttributes;
import ohos.sysappcomponents.contact.ContactsCollection;
import ohos.sysappcomponents.contact.chain.Insertor;
import ohos.sysappcomponents.contact.chain.PortraitChain;
import ohos.sysappcomponents.contact.chain.PortraitInsertor;
import ohos.sysappcomponents.contact.collection.GroupsCollection;
import ohos.sysappcomponents.contact.creator.ContactCreator;
import ohos.sysappcomponents.contact.entity.Contact;
import ohos.sysappcomponents.contact.entity.Group;
import ohos.sysappcomponents.contact.entity.Holder;
import ohos.telephony.TelephoneNumberUtils;
import ohos.utils.net.Uri;

public class ContactDataHelper {
    private static final String DEFAULT_DIRECTORY = "1";
    private static final String EMPTY_STRING = "";
    private static final String FIELD_CONNECTOR = "=?";
    private static final String HOLDER = "directory";
    private static final long[] LOCAL_HOLDER_ID = {0, 1, Attribute.Holder.ENTERPRISE_DEFAULT, Attribute.Holder.ENTERPRISE_LOCAL_INVISIBLE};
    private static final String LOOKUP_PROFILE = "profile";
    private static final String NON_DEFAULT_DIRECTORY = "0";
    private static final String RAW_DEFAULT_DIRECTORY = "in_default_directory=?";
    private static final String RAW_RROFILE_FILTER = "contact_id>=?";
    private static final String TAG = ContactDataHelper.class.getSimpleName();
    private Context mContext;
    private Insertor mInsertor = Insertor.initChain();

    public boolean isMyCard(long j) {
        return j >= Attribute.Profile.MIN_ID;
    }

    public ContactDataHelper(Context context) {
        this.mContext = context;
    }

    public ContactsCollection queryContactsByPhoneNumber(String str, Holder holder, ContactAttributes contactAttributes) {
        String formatPhoneNumber = TelephoneNumberUtils.formatPhoneNumber(str);
        if (formatPhoneNumber == null || "".equals(formatPhoneNumber)) {
            return null;
        }
        return queryContactsByUri(Uri.appendEncodedPathToUri(Attribute.PhoneFinder.CONTENT_FILTER_URI, formatPhoneNumber).makeBuilder().appendDecodedQueryParam(HOLDER, String.valueOf(getHolderId(holder))).build(), null, contactAttributes, ContactsCollection.Type.PHONE_LOOKUP);
    }

    private ContactsCollection queryContactsByUri(Uri uri, DataAbilityPredicates dataAbilityPredicates, ContactAttributes contactAttributes, ContactsCollection.Type type) {
        ContactsCollection contactsCollection;
        Context context = this.mContext;
        if (context == null) {
            return null;
        }
        DataAbilityHelper creator = DataAbilityHelper.creator(context);
        try {
            contactsCollection = ContactsCollection.createContactsCollection(creator.query(uri, (String[]) null, dataAbilityPredicates), contactAttributes, this.mContext, type);
            try {
                LogUtil.info(TAG, "ContactDataHelper fetchContactsByUri end");
            } catch (DataAbilityRemoteException unused) {
            } catch (IllegalArgumentException unused2) {
                LogUtil.error(TAG, "fetchContactsByUri IllegalArgumentException error");
                clearEnvironment(creator, null);
                return contactsCollection;
            }
        } catch (DataAbilityRemoteException unused3) {
            contactsCollection = null;
            LogUtil.error(TAG, "fetchContactsByUri error");
            clearEnvironment(creator, null);
            return contactsCollection;
        } catch (IllegalArgumentException unused4) {
            contactsCollection = null;
            LogUtil.error(TAG, "fetchContactsByUri IllegalArgumentException error");
            clearEnvironment(creator, null);
            return contactsCollection;
        } catch (Throwable th) {
            clearEnvironment(creator, null);
            throw th;
        }
        clearEnvironment(creator, null);
        return contactsCollection;
    }

    public HoldersCollection queryHolders() {
        HoldersCollection holdersCollection;
        Context context = this.mContext;
        if (context == null) {
            return null;
        }
        DataAbilityHelper creator = DataAbilityHelper.creator(context);
        try {
            holdersCollection = new HoldersCollection(creator.query(Attribute.Holder.CONTENT_URI, (String[]) null, (DataAbilityPredicates) null));
            try {
                LogUtil.info(TAG, "ContactDataHelper fetchHoldersByUri end");
            } catch (DataAbilityRemoteException unused) {
            }
        } catch (DataAbilityRemoteException unused2) {
            holdersCollection = null;
            try {
                LogUtil.error(TAG, "fetchHoldersByUri error");
                clearEnvironment(creator, null);
                return holdersCollection;
            } catch (Throwable th) {
                clearEnvironment(creator, null);
                throw th;
            }
        }
        clearEnvironment(creator, null);
        return holdersCollection;
    }

    public ContactsCollection queryContactsByEmail(String str, Holder holder, ContactAttributes contactAttributes) {
        if (str == null || this.mContext == null) {
            return null;
        }
        DataAbilityPredicates dataAbilityPredicates = new DataAbilityPredicates("raw_contact_id in (select raw_contact_id from data where mimetype_id = ? and data1 = ?)");
        ArrayList arrayList = new ArrayList();
        arrayList.add("1");
        arrayList.add(str);
        dataAbilityPredicates.setWhereArgs(arrayList);
        dataAbilityPredicates.setOrder("contact_id");
        return queryContactsByUri(Attribute.Data.CONTENT_URI.makeBuilder().appendDecodedQueryParam(HOLDER, String.valueOf(getHolderId(holder))).build(), dataAbilityPredicates, contactAttributes, ContactsCollection.Type.DATA_CONTACT);
    }

    public ContactsCollection queryContacts(Holder holder, ContactAttributes contactAttributes) {
        long holderId = getHolderId(holder);
        DataAbilityPredicates dataAbilityPredicates = new DataAbilityPredicates(RAW_DEFAULT_DIRECTORY);
        ArrayList arrayList = new ArrayList();
        if (Attribute.Holder.isRemoteHolderId(holderId)) {
            arrayList.add("0");
        } else {
            arrayList.add("1");
        }
        dataAbilityPredicates.setWhereArgs(arrayList);
        dataAbilityPredicates.setOrder("contact_id");
        return queryContactsByUri(Attribute.Data.CONTENT_URI.makeBuilder().appendDecodedQueryParam(HOLDER, String.valueOf(holderId)).build(), dataAbilityPredicates, contactAttributes, ContactsCollection.Type.DATA_CONTACT);
    }

    /* JADX INFO: finally extract failed */
    public long addContact(Contact contact) {
        DataAbilityResult[] dataAbilityResultArr;
        long j = -1;
        if (Stream.of(this.mContext, contact).anyMatch($$Lambda$ContactDataHelper$wLIh0GiBW9398cTP8uaTH8KoGwo.INSTANCE)) {
            return -1;
        }
        ArrayList<DataAbilityOperation> createOperations = ContactCreator.createOperations(contact, this.mInsertor);
        DataAbilityHelper creator = DataAbilityHelper.creator(this.mContext);
        try {
            dataAbilityResultArr = creator.executeBatch(Attribute.CommonDataKinds.AUTHORITY, createOperations);
            clearEnvironment(creator, null);
        } catch (DataAbilityRemoteException | OperationExecuteException unused) {
            LogUtil.error(TAG, "createContact error");
            clearEnvironment(creator, null);
            dataAbilityResultArr = null;
        } catch (Throwable th) {
            clearEnvironment(creator, null);
            throw th;
        }
        if (dataAbilityResultArr != null && dataAbilityResultArr.length > 0) {
            j = (long) dataAbilityResultArr[0].getCount().intValue();
        }
        if (!(contact.getPortrait() == null || contact.getPortrait().getUri() == null)) {
            PortraitInsertor.savePortrait(j, UriConverter.convertToAndroidUri(contact.getPortrait().getUri()), this.mContext);
        }
        return j;
    }

    public Contact queryMyCard(ContactAttributes contactAttributes) {
        DataAbilityPredicates dataAbilityPredicates = new DataAbilityPredicates(RAW_RROFILE_FILTER);
        ArrayList arrayList = new ArrayList();
        arrayList.add(String.valueOf((long) Attribute.Profile.MIN_ID));
        dataAbilityPredicates.setWhereArgs(arrayList);
        dataAbilityPredicates.orderByAsc("contact_id");
        ContactsCollection queryContactsByUri = queryContactsByUri(Uri.appendEncodedPathToUri(Attribute.Profile.CONTENT_URI, "data"), dataAbilityPredicates, contactAttributes, ContactsCollection.Type.DATA_CONTACT);
        if (queryContactsByUri != null) {
            return queryContactsByUri.next();
        }
        return null;
    }

    public Contact queryContact(String str, Holder holder, ContactAttributes contactAttributes) {
        if (str == null) {
            return null;
        }
        if ("profile".equals(str)) {
            return queryMyCard(contactAttributes);
        }
        ContactsCollection queryContactsByUri = queryContactsByUri(Uri.appendEncodedPathToUri(Uri.appendEncodedPathToUri(Attribute.Contacts.CONTENT_LOOKUP_URI, str), "data").makeBuilder().appendDecodedQueryParam(HOLDER, String.valueOf(getHolderId(holder))).build(), null, contactAttributes, ContactsCollection.Type.DATA_CONTACT);
        if (queryContactsByUri != null) {
            return queryContactsByUri.next();
        }
        return null;
    }

    /* JADX WARNING: Removed duplicated region for block: B:19:0x0052 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0053 A[RETURN] */
    public boolean deleteContact(String str) {
        int i;
        if (str == null || this.mContext == null) {
            return false;
        }
        Uri appendEncodedPathToUri = Uri.appendEncodedPathToUri(Attribute.Contacts.CONTENT_LOOKUP_URI, str);
        DataAbilityHelper creator = DataAbilityHelper.creator(this.mContext);
        DataAbilityPredicates dataAbilityPredicates = new DataAbilityPredicates("lookup=?");
        ArrayList arrayList = new ArrayList();
        arrayList.add(str);
        dataAbilityPredicates.setWhereArgs(arrayList);
        try {
            i = creator.delete(appendEncodedPathToUri, dataAbilityPredicates);
            try {
                LogUtil.info(TAG, "ContactDataHelper deleteContactByKey end, result deleteNumber = " + i);
            } catch (DataAbilityRemoteException unused) {
            }
        } catch (DataAbilityRemoteException unused2) {
            i = 0;
            try {
                LogUtil.error(TAG, "deleteContactByKey error");
                clearEnvironment(creator, null);
                if (i != 0) {
                }
            } catch (Throwable th) {
                clearEnvironment(creator, null);
                throw th;
            }
        }
        clearEnvironment(creator, null);
        if (i != 0) {
            return false;
        }
        return true;
    }

    public boolean updateContact(Contact contact, ContactAttributes contactAttributes) {
        if (contact == null) {
            return false;
        }
        Insertor createChain = Insertor.createChain(contactAttributes);
        ArrayList<DataAbilityOperation> arrayList = new ArrayList<>();
        createChain.fillOperation(contact, arrayList, Insertor.OperationType.UPDATE);
        if (contactAttributes == null || contactAttributes.isValid(ContactAttributes.Attribute.ATTR_PORTRAIT)) {
            new PortraitChain(this.mContext).fillOperation(contact, arrayList, Insertor.OperationType.UPDATE);
        }
        DataAbilityHelper creator = DataAbilityHelper.creator(this.mContext);
        DataAbilityResult[] dataAbilityResultArr = null;
        try {
            DataAbilityResult[] executeBatch = creator.executeBatch(Attribute.CommonDataKinds.AUTHORITY, arrayList);
            clearEnvironment(creator, null);
            dataAbilityResultArr = executeBatch;
        } catch (DataAbilityRemoteException | OperationExecuteException unused) {
            LogUtil.error(TAG, "updateContact error");
            clearEnvironment(creator, null);
        } catch (Throwable th) {
            clearEnvironment(creator, null);
            throw th;
        }
        return isSucceed(dataAbilityResultArr);
    }

    private boolean isSucceed(DataAbilityResult[] dataAbilityResultArr) {
        if (dataAbilityResultArr == null || dataAbilityResultArr.length <= 0) {
            return false;
        }
        int i = 0;
        while (true) {
            boolean z = true;
            if (i >= dataAbilityResultArr.length) {
                return true;
            }
            if (dataAbilityResultArr[i] == null || dataAbilityResultArr[i].getCount().intValue() <= 0) {
                z = false;
            }
            if (!z) {
                return false;
            }
            i++;
        }
    }

    public static boolean isEnterpriseContactId(long j) {
        return Attribute.Contacts.isEnterpriseContactId(j);
    }

    public List<Group> queryGroups(Holder holder) {
        Throwable th;
        GroupsCollection groupsCollection;
        DataAbilityHelper dataAbilityHelper;
        ArrayList arrayList;
        Context context = this.mContext;
        if (context == null) {
            return null;
        }
        try {
            dataAbilityHelper = DataAbilityHelper.creator(context);
            try {
                Attribute.Contacts.CONTENT_URI.makeBuilder().appendDecodedQueryParam(HOLDER, String.valueOf(getHolderId(holder))).build();
                groupsCollection = new GroupsCollection(dataAbilityHelper.query(Attribute.Groups.CONTENT_URI, (String[]) null, (DataAbilityPredicates) null));
                try {
                    arrayList = new ArrayList();
                    while (!groupsCollection.isEmpty()) {
                        try {
                            Group next = groupsCollection.next();
                            if (next != null) {
                                arrayList.add(next);
                            }
                        } catch (DataAbilityRemoteException unused) {
                            try {
                                LogUtil.error(TAG, "queryGroups error");
                                clearEnvironment(dataAbilityHelper, null);
                                groupsCollection.release();
                                return arrayList;
                            } catch (Throwable th2) {
                                th = th2;
                                clearEnvironment(dataAbilityHelper, null);
                                groupsCollection.release();
                                throw th;
                            }
                        }
                    }
                } catch (DataAbilityRemoteException unused2) {
                    arrayList = null;
                    LogUtil.error(TAG, "queryGroups error");
                    clearEnvironment(dataAbilityHelper, null);
                    groupsCollection.release();
                    return arrayList;
                }
            } catch (DataAbilityRemoteException unused3) {
                arrayList = null;
                groupsCollection = null;
                LogUtil.error(TAG, "queryGroups error");
                clearEnvironment(dataAbilityHelper, null);
                groupsCollection.release();
                return arrayList;
            } catch (Throwable th3) {
                th = th3;
                groupsCollection = null;
                clearEnvironment(dataAbilityHelper, null);
                groupsCollection.release();
                throw th;
            }
        } catch (DataAbilityRemoteException unused4) {
            arrayList = null;
            dataAbilityHelper = null;
            groupsCollection = null;
            LogUtil.error(TAG, "queryGroups error");
            clearEnvironment(dataAbilityHelper, null);
            groupsCollection.release();
            return arrayList;
        } catch (Throwable th4) {
            th = th4;
            dataAbilityHelper = null;
            groupsCollection = null;
            clearEnvironment(dataAbilityHelper, null);
            groupsCollection.release();
            throw th;
        }
        clearEnvironment(dataAbilityHelper, null);
        groupsCollection.release();
        return arrayList;
    }

    public String queryKey(long j, Holder holder) {
        Throwable th;
        DataAbilityHelper dataAbilityHelper;
        ResultSet resultSet;
        ResultSet resultSet2 = null;
        if (this.mContext == null) {
            return null;
        }
        Uri.Builder makeBuilder = Attribute.Contacts.CONTENT_URI.makeBuilder();
        try {
            dataAbilityHelper = DataAbilityHelper.creator(this.mContext);
            try {
                Uri build = makeBuilder.appendDecodedQueryParam(HOLDER, String.valueOf(getHolderId(holder))).build();
                DataAbilityPredicates dataAbilityPredicates = new DataAbilityPredicates("name_raw_contact_id=?");
                ArrayList arrayList = new ArrayList();
                arrayList.add(String.valueOf(j));
                dataAbilityPredicates.setWhereArgs(arrayList);
                resultSet = dataAbilityHelper.query(build, (String[]) null, dataAbilityPredicates);
                try {
                    resultSet2 = ContactCreator.getKey(resultSet);
                } catch (DataAbilityRemoteException unused) {
                }
            } catch (DataAbilityRemoteException unused2) {
                resultSet = null;
                try {
                    LogUtil.error(TAG, "queryKey error");
                    clearEnvironment(dataAbilityHelper, resultSet);
                    return resultSet2;
                } catch (Throwable th2) {
                    th = th2;
                    resultSet2 = resultSet;
                    clearEnvironment(dataAbilityHelper, resultSet2);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                clearEnvironment(dataAbilityHelper, resultSet2);
                throw th;
            }
        } catch (DataAbilityRemoteException unused3) {
            resultSet = null;
            dataAbilityHelper = null;
            LogUtil.error(TAG, "queryKey error");
            clearEnvironment(dataAbilityHelper, resultSet);
            return resultSet2;
        } catch (Throwable th4) {
            th = th4;
            dataAbilityHelper = null;
            clearEnvironment(dataAbilityHelper, resultSet2);
            throw th;
        }
        clearEnvironment(dataAbilityHelper, resultSet);
        return resultSet2;
    }

    public boolean isLocalContact(long j) {
        for (long j2 : LOCAL_HOLDER_ID) {
            if (queryKey(j, new Holder(j2)) != null) {
                return true;
            }
        }
        return false;
    }

    private void clearEnvironment(DataAbilityHelper dataAbilityHelper, ResultSet resultSet) {
        if (dataAbilityHelper != null) {
            dataAbilityHelper.release();
        }
        if (resultSet != null) {
            resultSet.close();
        }
    }

    private long getHolderId(Holder holder) {
        if (holder != null) {
            return holder.getHolderId();
        }
        return 0;
    }
}
