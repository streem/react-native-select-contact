
declare module "react-native-select-contact" {

    interface PhoneEntry {
        number: string,
        type?: string
    }

    interface EmailEntry {
        address: string,
        type?: string
    }

    interface Contact {
        name?: string,
        phones?: PhoneEntry[],
        emails?: EmailEntry[],
    }

    interface Contact {
        name?: string,
        phones?: PhoneEntry[],
        emails?: EmailEntry[]
    }

    interface ContactPhoneSelection {
        contact: Contact,
        selectedPhone: PhoneEntry
    }

    class SelectContact {
        static selectContactPhone(): Promise<ContactPhoneSelection | null>;
    }

    export = SelectContact;

}
