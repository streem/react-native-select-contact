using CapeCode.SelectContact.Modules;
using ReactNative.Bridge;
using ReactNative.Modules.Core;
using ReactNative.UIManager;
using System.Collections.Generic;

namespace CapeCode.SelectContact {
	public class SelectContactReactPackage : IReactPackage {
		public IReadOnlyList<INativeModule> CreateNativeModules(ReactContext reactContext) {
			return new List<INativeModule> {
				new SelectContactModule(reactContext),
			};
		}

		public IReadOnlyList<IViewManager> CreateViewManagers(ReactContext reactContext) {
			return new List<IViewManager>(0);
		}
	}
}