//
//  RCTSelectContact.h
//  RCTSelectContact
//

#import <React/RCTBridgeModule.h>

@import Contacts;
@import ContactsUI;

@interface RCTSelectContact : NSObject <RCTBridgeModule, CNContactPickerDelegate>

@end
