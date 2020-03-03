# react-native-select-contact

Originally branched from [react-native-contacts-wrapper](https://github.com/LynxITDigital/react-native-contacts-wrapper)

This is a simple wrapper for the native iOS and Android Contact Picker UIs, with some optional help for selecting specific fields from the contact.

### Installation

```
yarn add react-native-select-contact
```

For React Native => 0.59 only:
```
react-native link react-native-select-contact
```

Make sure your manifest files includes permission to read contacts
```
<uses-permission android:name="android.permission.READ_CONTACTS" />
```

### API

#### Methods

```
selectContact(): Promise<Contact | null>;
selectContactPhone(): Promise<ContactPhoneSelection | null>;
selectContactEmail(): Promise<ContactEmailSelection | null>;
selectContactPostalAddress(): Promise<ContactPostalAddressSelection | null>;
```

These methods all open up a separate ViewController (on IOS) or Activity (on Android) to select a contact.  See Types below.

For `selectContactPhone`, `selectContactEmail`, or `selectContactPostalAddress`, if there are more than one phone or email, an `ActionSheetIOS` is
shown for IOS, and the first entry is returned for Android.

A return value `null` may be because the user cancelled the contact selection.  You shouldn't need to worry about doing
anything if the promise resolves to `null`.

#### Optional Android ActionSheet

You can enable ActionSheet functionality for Android by installing an optional dependency:

```
yarn add react-native-action-sheet
```

For React Native => 0.59 only:
```
react-native link react-native-action-sheet
```

This will provide an `ActionSheetAndroid` native module that this library will pick up on and use
when there are more than one phone number or email on a selected contact.

#### Types

```typescript
interface PhoneEntry {
    number: string,
    type: string
}

interface EmailEntry {
    address: string,
    type: string
}

interface AddressEntry {
    formattedAddress: string, // android only
    type: string, // android only
    street: string,
    city: string,
    state: string,
    postalCode: string,
    isoCountryCode: string
}

interface Contact {
    name: string,
    phones: PhoneEntry[],
    emails: EmailEntry[],
    postalAddresses: AddressEntry[]
}

interface ContactPhoneSelection {
    contact: Contact,
    selectedPhone: PhoneEntry
}

interface ContactEmailSelection {
    contact: Contact,
    selectedEmail: EmailEntry
}

interface ContactPostalAddressSelection {
    contact: Contact,
    selectedAddress: AddressEntry
}
```

### Example

```javascript

import { selectContactPhone } from 'react-native-select-contact';

function getPhoneNumber() {
    return selectContactPhone()
        .then(selection => {
            if (!selection) {
                return null;
            }
            
            let { contact, selectedPhone } = selection;
            console.log(`Selected ${selectedPhone.type} phone number ${selectedPhone.number} from ${contact.name}`);
            return selectedPhone.number;
        });  
}


```
