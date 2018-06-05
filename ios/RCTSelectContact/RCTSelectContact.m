//
//  RCTSelectContact.m
//  RCTSelectContact
//

@import Foundation;
#import "RCTSelectContact.h"
@interface RCTSelectContact()

@property(nonatomic, retain) RCTPromiseResolveBlock _resolve;
@property(nonatomic, retain) RCTPromiseRejectBlock _reject;

@end


@implementation RCTSelectContact

RCT_EXPORT_MODULE(SelectContact);

RCT_EXPORT_METHOD(openContactSelection:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
  self._resolve = resolve;
  self._reject = reject;

  UIViewController *picker = [[CNContactPickerViewController alloc] init];
  ((CNContactPickerViewController *)picker).delegate = self;

  // Launch Contact Picker
  UIViewController *root = [[[UIApplication sharedApplication] delegate] window].rootViewController;
  BOOL modalPresent = (BOOL) (root.presentedViewController);
  if (modalPresent) {
    UIViewController *parent = root.presentedViewController;
    [parent presentViewController:picker animated:YES completion:nil];
  } else {
    [root presentViewController:picker animated:YES completion:nil];
  }
}

- (NSMutableDictionary *) emptyContactDict {
  NSMutableArray *phones = [[NSMutableArray alloc] init];
  NSMutableArray *emails = [[NSMutableArray alloc] init];
  return [[NSMutableDictionary alloc] initWithObjects:@[@"", phones, emails] forKeys:@[@"name", @"phones", @"emails"]];
}

#pragma mark - CNContactPickerDelegate
- (void)contactPicker:(CNContactPickerViewController *)picker didSelectContact:(CNContact *)contact {

  /* Return NSDictionary ans JS Object to RN, containing basic contact data
   This is a starting point, in future more fields should be added, as required.
   */
  NSMutableDictionary *contactData = [self emptyContactDict];

  //Return full name
  NSString *fullName = [self getFullNameForFirst:contact.givenName middle:contact.middleName last:contact.familyName ];
  [contactData setValue:fullName forKey:@"name"];

  //Return phone numbers
  NSMutableArray* phoneEntries = [contactData valueForKey:@"phones"];
  for (CNLabeledValue<CNPhoneNumber*> *phone in contact.phoneNumbers) {
    CNPhoneNumber* phoneNumber = [phone value];
    NSString* phoneLabel = [phone label];
    NSMutableDictionary<NSString*, NSString*>* phoneEntry = [[NSMutableDictionary alloc] initWithCapacity:2];
    [phoneEntry setValue:[phoneNumber stringValue] forKey:@"number"];
    [phoneEntry setValue:[CNLabeledValue localizedStringForLabel:phoneLabel] forKey:@"type"];
    [phoneEntries addObject:phoneEntry];
  }

  //Return email addresses
  NSMutableArray* emailEntries = [contactData valueForKey:@"emails"];
  for (CNLabeledValue<NSString*> *email in contact.emailAddresses) {
    NSString* emailAddress = [email value];
    NSString* emailLabel = [email label];
    NSMutableDictionary<NSString*, NSString*>* emailEntry = [[NSMutableDictionary alloc] initWithCapacity:2];
    [emailEntry setValue:emailAddress forKey:@"address"];
    [emailEntry setValue:[CNLabeledValue localizedStringForLabel:emailLabel] forKey:@"type"];
    [emailEntries addObject:emailEntry];
  }

  self._resolve(contactData);
}

-(NSString *) getFullNameForFirst:(NSString *)fName middle:(NSString *)mName last:(NSString *)lName {
  //Check whether to include middle name or not
  NSArray *names = (mName.length > 0) ? [NSArray arrayWithObjects:fName, mName, lName, nil] : [NSArray arrayWithObjects:fName, lName, nil];
  return [names componentsJoinedByString:@" "];
}

- (void)contactPickerDidCancel:(CNContactPickerViewController *)picker {
  self._reject(@"E_CONTACT_CANCELLED", @"Cancelled", nil);
}

@end
