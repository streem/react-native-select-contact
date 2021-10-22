
export function selectContact(textOptions? : TextOptions): Promise<Contact | null>;
export function selectContactPhone(textOptions? : TextOptions): Promise<ContactPhoneSelection | null>;
export function selectContactEmail(textOptions? : TextOptions): Promise<ContactEmailSelection | null>;
export function selectContactPostalAddress(textOptions? : TextOptions): Promise<ContactPostalAddressSelection | null>;

export interface PhoneEntry {
    number: string,
    type: string
}

export interface EmailEntry {
    address: string,
    type: string
}

export interface AddressEntry {
    formattedAddress: string, // android only
    type: string, // android only
    street: string,
    city: string,
    state: string,
    postalCode: string,
    isoCountryCode: string
}

export interface Contact {
    recordId: string,
    name: string,
    phones: PhoneEntry[],
    emails: EmailEntry[],
    postalAddresses: AddressEntry[]
}

export interface ContactPhoneSelection {
    contact: Contact,
    selectedPhone: PhoneEntry
}

export interface ContactEmailSelection {
    contact: Contact,
    selectedEmail: EmailEntry
}

export interface ContactPostalAddressSelection {
    contact: Contact,
    selectedAddress: AddressEntry
}

export interface TextOptions {
    cancel: String, 
    selectPhone: String, 
    selectPostalAddress: String,
    selectEmail: String,
    errorNoPhoneNumbersTitle: String, 
    errorNoPhoneNumbersBody: String,
    errorNoAddressTitle: String,
    errorNoAddressBody: String,
    errorNoEmailTitle: String,
    errorNoEmailBody: String,
    errorOpenTwice: String,
}