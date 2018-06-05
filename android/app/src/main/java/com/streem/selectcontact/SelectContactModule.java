package com.streem.selectcontact;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Contacts.Entity;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

public class SelectContactModule extends ReactContextBaseJavaModule implements ActivityEventListener {

    private static final int CONTACT_REQUEST = 11112;
    public static final String E_CONTACT_CANCELLED = "E_CONTACT_CANCELLED";
    public static final String E_CONTACT_NO_DATA = "E_CONTACT_NO_DATA";
    public static final String E_CONTACT_EXCEPTION = "E_CONTACT_EXCEPTION";
    public static final String E_CONTACT_PERMISSION = "E_CONTACT_PERMISSION";
    private Promise mContactsPromise;
    private final ContentResolver contentResolver;

    public SelectContactModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.contentResolver = getReactApplicationContext().getContentResolver();
        reactContext.addActivityEventListener(this);
    }

    @Override
    public String getName() {
        return "SelectContact";
    }


    @ReactMethod
    public void openContactSelection(Promise contactsPromise) {
        launchPicker(contactsPromise, CONTACT_REQUEST);
    }

    /**
     * Lanch the contact picker, with the specified requestCode for returned data.
     *
     * @param contactsPromise - promise passed in from React Native.
     * @param requestCode     - request code to specify what contact data to return
     */
    private void launchPicker(Promise contactsPromise, int requestCode) {
        Cursor cursor = this.contentResolver.query(Contacts.CONTENT_URI, null, null, null, null);
        if (cursor != null) {
            mContactsPromise = contactsPromise;
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType(Contacts.CONTENT_TYPE);
            Activity activity = getCurrentActivity();
            if (intent.resolveActivity(activity.getPackageManager()) != null) {
                activity.startActivityForResult(intent, requestCode);
            }
            cursor.close();
        } else {
            mContactsPromise.reject(E_CONTACT_PERMISSION, "no permission");
        }
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent intent) {
        if (mContactsPromise == null || requestCode != CONTACT_REQUEST) {
            return;
        }

        //Request was cancelled
        if (resultCode != Activity.RESULT_OK) {
            mContactsPromise.reject(E_CONTACT_CANCELLED, "Cancelled");
            return;
        }

        // Retrieve all possible data about contact and return as a JS object
        WritableMap contactData = Arguments.createMap();

        try {
            String id = getContactId(intent.getData());
            Uri contactUri = buildContactUri(id);
            boolean foundData = false;

            Cursor cursor = openContactQuery(contactUri);
            if (cursor.moveToFirst()) {
                do {
                    String mime = cursor.getString(cursor.getColumnIndex(Entity.MIMETYPE));
                    switch (mime) {
                        case StructuredName.CONTENT_ITEM_TYPE:
                            addNameData(contactData, cursor);
                            foundData = true;
                            break;

                        case Phone.CONTENT_ITEM_TYPE:
                            addPhoneData(contactData, cursor, activity);
                            foundData = true;
                            break;

                        case Email.CONTENT_ITEM_TYPE:
                            addEmailData(contactData, cursor, activity);
                            foundData = true;
                            break;
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();

            if (foundData) {
                mContactsPromise.resolve(contactData);
            } else {
                mContactsPromise.reject(E_CONTACT_NO_DATA, "No data found for contact");
            }
        } catch (Exception e) {
            mContactsPromise.reject(E_CONTACT_EXCEPTION, e.getMessage());
        }
    }

    private String getContactId(Uri contactUri) throws SelectContactException {
        Cursor cursor = this.contentResolver.query(contactUri, null, null, null, null);
        if (cursor == null || !cursor.moveToFirst()) {
            throw new SelectContactException(E_CONTACT_NO_DATA, "Contact Data Not Found");
        }

        return cursor.getString(cursor.getColumnIndex(Contacts._ID));
    }

    private Uri buildContactUri(String id) {
        return Uri
                .withAppendedPath(Contacts.CONTENT_URI, id)
                .buildUpon()
                .appendPath(Entity.CONTENT_DIRECTORY)
                .build();
    }

    private Cursor openContactQuery(Uri contactUri) throws SelectContactException {
        String[] projection = {
                Entity.MIMETYPE,
                Entity.DATA1,
                Entity.DATA2,
                Entity.DATA3
        };
        String sortOrder = Entity.RAW_CONTACT_ID + " ASC";
        Cursor cursor = this.contentResolver.query(contactUri, projection, null, null, sortOrder);
        if (cursor == null) {
            throw new SelectContactException(E_CONTACT_EXCEPTION, "Could not query contacts data. Unable to create cursor.");
        }

        return cursor;
    }

    private void addNameData(WritableMap contactData, Cursor cursor) {
        int displayNameIndex = cursor.getColumnIndex(StructuredName.DISPLAY_NAME);
        contactData.putString("name", cursor.getString(displayNameIndex));
    }

    private void addPhoneData(WritableMap contactData, Cursor cursor, Activity activity) {
        String phoneNumber = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
        int phoneType = cursor.getInt(cursor.getColumnIndex(Phone.TYPE));
        String phoneLabel = cursor.getString(cursor.getColumnIndex(Phone.LABEL));
        CharSequence typeLabel = Phone.getTypeLabel(activity.getResources(), phoneType, phoneLabel);

        WritableMap phoneEntry = Arguments.createMap();
        phoneEntry.putString("number", phoneNumber);
        phoneEntry.putString("type", String.valueOf(typeLabel));

        if (!contactData.hasKey("phones")) {
            contactData.putArray("phones", Arguments.createArray());
        }

        WritableArray phoneNumbers = (WritableArray) contactData.getArray("phones");
        phoneNumbers.pushMap(phoneEntry);
    }

    private void addEmailData(WritableMap contactData, Cursor cursor, Activity activity) {
        String emailAddress = cursor.getString(cursor.getColumnIndex(Email.ADDRESS));
        int emailType = cursor.getInt(cursor.getColumnIndex(Email.TYPE));
        String emailLabel = cursor.getString(cursor.getColumnIndex(Email.LABEL));
        CharSequence typeLabel = Email.getTypeLabel(activity.getResources(), emailType, emailLabel);

        WritableMap emailEntry = Arguments.createMap();
        emailEntry.putString("address", emailAddress);
        emailEntry.putString("type", String.valueOf(typeLabel));

        if (!contactData.hasKey("emails")) {
            contactData.putArray("emails", Arguments.createArray());
        }

        WritableArray emails = (WritableArray) contactData.getArray("emails");
        emails.pushMap(emailEntry);
    }

    @Override
    public void onNewIntent(Intent intent) {

    }

    public static class SelectContactException extends Exception {
        private final String errorCode;

        public SelectContactException(String errorCode, String errorMessage) {
            super(errorMessage);
            this.errorCode = errorCode;
        }

        public String getErrorCode() {
            return errorCode;
        }
    }
}
