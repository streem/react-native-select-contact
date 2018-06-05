
export function selectContact(): Promise<Contact | null>;
export function selectContactPhone(): Promise<ContactPhoneSelection | null>;
export function selectContactEmail(): Promise<ContactEmailSelection | null>;

export interface PhoneEntry {
    number: string,
    type: string
}

export interface EmailEntry {
    address: string,
    type: string
}

export interface Contact {
    name: string,
    phones: PhoneEntry[],
    emails: EmailEntry[]
}

export interface ContactPhoneSelection {
    contact: Contact,
    selectedPhone: PhoneEntry
}

export interface ContactEmailSelection {
    contact: Contact,
    selectedEmail: EmailEntry
}