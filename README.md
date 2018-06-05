# react-native-select-contact

Originally branched from [react-native-contacts-wrapper](https://github.com/LynxITDigital/react-native-contacts-wrapper)

![alt tag](https://github.com/LynxITDigital/Screenshots/blob/master/RN%20Contacts%20Wrapper%20example.gif)

This is a simple wrapper for the native iOS and Android Contact Picker UIs, with some optional help for selecting specific fields from the contact.

## Installation

```
yarn add react-native-select-contact
./node_modules/.bin/react-native link react-native-select-contact
```

## API

```
selectContact(): Promise<Contact | null>;
selectContactPhone(): Promise<ContactPhoneSelection | null>;
selectContactEmail(): Promise<ContactEmailSelection | null>;
```

Types:

```typescript
interface PhoneEntry {
    number: string,
    type: string
}

interface EmailEntry {
    address: string,
    type: string
}

interface Contact {
    name: string,
    phones: PhoneEntry[],
    emails: EmailEntry[]
}

interface ContactPhoneSelection {
    contact: Contact,
    selectedPhone: PhoneEntry
}

interface ContactEmailSelection {
    contact: Contact,
    selectedEmail: EmailEntry
}
```

###Example

An example project can be found in this repo: https://github.com/LynxITDigital/react-native-contacts-wrapper-example/tree/master

TODO: Update example:

```
import ContactsWrapper from 'react-native-contacts-wrapper';
...
if (!this.importingContactInfo) {
  this.importingContactInfo = true;

  ContactsWrapper.getEmail()
  .then((email) => {
    this.importingContactInfo = false;
    console.log("email is", email);
    })
    .catch((error) => {
      this.importingContactInfo = false;
      console.log("ERROR CODE: ", error.code);
      console.log("ERROR MESSAGE: ", error.message);
      });
```
