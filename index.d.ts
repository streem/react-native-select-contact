
declare module "react-native-select-contact" {

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

    class SelectContact {
        static selectContact(): Promise<Contact | null>;
        static selectContactPhone(): Promise<ContactPhoneSelection | null>;
        static selectContactEmail(): Promise<ContactEmailSelection | null>;
    }

    export = SelectContact;
}
