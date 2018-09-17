package android.provider;

import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.net.Uri;
import android.os.Process;
import android.os.UserHandle;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Directory;
import android.text.TextUtils;
import android.widget.Toast;
import com.android.internal.R;
import java.util.List;

public class ContactsInternal {
    private static final int CONTACTS_URI_LOOKUP = 1001;
    private static final int CONTACTS_URI_LOOKUP_ID = 1000;
    private static final UriMatcher sContactsUriMatcher = new UriMatcher(-1);

    private ContactsInternal() {
    }

    static {
        UriMatcher matcher = sContactsUriMatcher;
        matcher.addURI("com.android.contacts", "contacts/lookup/*", 1001);
        matcher.addURI("com.android.contacts", "contacts/lookup/*/#", 1000);
    }

    public static void startQuickContactWithErrorToast(Context context, Intent intent) {
        switch (sContactsUriMatcher.match(intent.getData())) {
            case 1000:
            case 1001:
                if (maybeStartManagedQuickContact(context, intent)) {
                    return;
                }
                break;
        }
        startQuickContactWithErrorToastForUser(context, intent, Process.myUserHandle());
    }

    public static void startQuickContactWithErrorToastForUser(Context context, Intent intent, UserHandle user) {
        try {
            context.startActivityAsUser(intent, user);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, (int) R.string.quick_contacts_not_available, 0).show();
        }
    }

    private static boolean maybeStartManagedQuickContact(Context context, Intent originalIntent) {
        long contactId;
        long directoryId;
        Uri uri = originalIntent.getData();
        List<String> pathSegments = uri.getPathSegments();
        boolean isContactIdIgnored = pathSegments.size() < 4;
        if (isContactIdIgnored) {
            contactId = Contacts.ENTERPRISE_CONTACT_ID_BASE;
        } else {
            contactId = ContentUris.parseId(uri);
        }
        String lookupKey = (String) pathSegments.get(2);
        String directoryIdStr = uri.getQueryParameter(ContactsContract.DIRECTORY_PARAM_KEY);
        if (directoryIdStr == null) {
            directoryId = 1000000000;
        } else {
            directoryId = Long.parseLong(directoryIdStr);
        }
        if (TextUtils.isEmpty(lookupKey) || (lookupKey.startsWith(Contacts.ENTERPRISE_CONTACT_LOOKUP_PREFIX) ^ 1) != 0) {
            return false;
        }
        if (!Contacts.isEnterpriseContactId(contactId)) {
            throw new IllegalArgumentException("Invalid enterprise contact id: " + contactId);
        } else if (Directory.isEnterpriseDirectoryId(directoryId)) {
            ((DevicePolicyManager) context.getSystemService(DevicePolicyManager.class)).startManagedQuickContact(lookupKey.substring(Contacts.ENTERPRISE_CONTACT_LOOKUP_PREFIX.length()), contactId - Contacts.ENTERPRISE_CONTACT_ID_BASE, isContactIdIgnored, directoryId - 1000000000, originalIntent);
            return true;
        } else {
            throw new IllegalArgumentException("Invalid enterprise directory id: " + directoryId);
        }
    }
}
