using Newtonsoft.Json.Linq;
using ReactNative.Bridge;
using ReactNative.Modules.Core;
using System;
using Windows.ApplicationModel.Core;
using Windows.UI.Core;

namespace CapeCode.SelectContact.Extensions {

	public static class ModuleExtensions {

		public static void SendEvent(this ReactContextNativeModuleBase module, string eventName, JObject parameters) {
			module.Context.GetJavaScriptModule<RCTDeviceEventEmitter>()
				.emit(eventName, parameters);
		}

		public static async void RunOnDispatcher(this ReactContextNativeModuleBase module, DispatchedHandler action) {
			await CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(CoreDispatcherPriority.Normal, action).AsTask().ConfigureAwait(false);
		}
	}
}