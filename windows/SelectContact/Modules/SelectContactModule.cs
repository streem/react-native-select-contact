using CapeCode.SelectContact.Extensions;
using CapeCode.SelectContact.Helpers;
using ReactNative.Bridge;
using System;
using System.Linq;
using Windows.ApplicationModel.Contacts;

namespace CapeCode.SelectContact.Modules {

	public class SelectContactModule : ReactContextNativeModuleBase {

		public SelectContactModule(ReactContext reactContext)
			: base(reactContext) {
		}

		public override string Name => "SelectContact";

		[ReactMethod]
		public void openContactSelection(ContactFieldType? fieldType, IPromise promise) {
			try {
				this.RunOnDispatcher(async () => {
					try {
						var contactPicker = new ContactPicker();
						if (fieldType != null) {
							contactPicker.SelectionMode = ContactSelectionMode.Fields;
							contactPicker.DesiredFieldsWithContactFieldType.Add(fieldType.Value);
						}
						Contact contact = await contactPicker.PickContactAsync();
						promise.Resolve(contact == null ? null : new {
							recordId = contact.Id,
							name = contact.Name,
							givenName = contact.FirstName,
							middleName = contact.MiddleName,
							familyName = contact.LastName,
							phones = contact.Phones?.Select(x => new {
								number = x.Number,
								type = x.Kind.ToString(),
							}),
							emails = contact.Emails?.Select(x => new {
								address = x.Address,
								type = x.Kind.ToString(),
							}),
							postalAddresses = contact.Addresses?.Select(x => new {
								street = x.StreetAddress,
								city = x.Locality,
								state = x.Region,
								postalCode = x.PostalCode,
								isoCountryCode = CountryHelper.TryGetTwoLetterISORegionName(x.Country),
							}),
						});
					}
					catch (Exception ex) {
						promise.Reject(ex);
					}
				});
			}
			catch (Exception ex) {
				promise.Reject(ex);
			}
		}
	}
}