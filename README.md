# zalo-capacitor-plugin

Plugin đăng nhập và lấy thông tin người dùng zalo cho capacitor

## Install

```bash
npm install zalo-capacitor-plugin
npx cap sync
```

## Configuration 
Theo [ https://developers.zalo.me/docs/sdk]( https://developers.zalo.me/docs/sdk " https://developers.zalo.me/docs/sdk")
### &bull; Android 
Vào trang web http://developers.zalo.me tạo ứng dụng cho android và điền thông tin theo yêu cầu.

**Lưu ý:**
* Chạy lệnh `ZaloPlugin.getHashKeyAndroid()` trong ứng dụng typescript của bạn để lấy hash key (Base64 của SHA1) cho thiết bị android

```typescript
ZaloPlugin.getHashKeyAndroid()
.then((res: HashKeyAndroid) => {
	console.log(res.hashKey);
})
```

##### 1. Trong file `android/app/src/build.gradle` của *app* thêm các *dependencies* sau:
```java
implementation "me.zalo:sdk-core:+"
implementation "me.zalo:sdk-auth:+"
implementation "me.zalo:sdk-openapi:+"
```

##### 2.  Cấu hình trong file `android/app/src/main/res/values/strings.xml`
```xml
<string name="zaloAppId">APP_ID</string>
<string name="zaloLoginProtocolScheme">zalo-APP_ID</string>
```
Trong đó **APP_ID** được lấy từ trang *http://developers.zalo.me* 

##### 3. Cấu hình trong `android/app/src/main/AndroidManifest.xml`
```xml
<application android:name="com.zing.zalo.zalosdk.oauth.ZaloSDKApplication" 
	...
>
	...
	 <meta-data
		  android:name="com.zing.zalo.zalosdk.appID"
		  android:value="@string/zaloAppId" />
		  
	<activity
		android:name="com.zing.zalo.zalosdk.oauth.BrowserLoginActivity"
		android:exported="true">
		<intent-filter>
			<action android:name="android.intent.action.VIEW" />
			<category android:name="android.intent.category.DEFAULT" />
			<category android:name="android.intent.category.BROWSABLE" />
			<data android:scheme="@string/zaloLoginProtocolScheme"  />
		</intent-filter>
	</activity>
	
      ...
</application>
<queries>
	<package android:name="com.zing.zalo" />
</queries>
```
**Lưu ý:** 
* **AppID** cần được thêm vào `strings.xml` theo hướng dẫn ở trên, không gán trực tiếp chuỗi appID trong thẻ metaData sẽ gây ra lỗi ZaloSDK không nhận dạng được appID.

**Chú thích**
* Đối với Android 11 (API >= 30) cần thêm thông tin sau cho phép mở ứng dụng Zalo:
```xml
<queries>
	<package android:name="com.zing.zalo" />
</queries>
```
* Đối với Android 12 (API >= 31) thêm `android:exported="true"` cho việc gọi lại ứng dụng khi đăng nhập bằng trình duyệt.

##### 4. Trong file `android/app/src/main/java/..../MainActivity.java`
```java
import com.zing.zalo.zalosdk.oauth.ZaloSDK;

public class MainActivity extends BridgeActivity {
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		ZaloSDK.Instance.onActivityResult(this, requestCode, resultCode, data);
	}
 }
```

##### 5. Tạo file `MyApplication.java` cùng thư mục với 	`MainActivity.java` và có nội dung như sau:
```java

import android.app.Application;
import com.zing.zalo.zalosdk.oauth.ZaloSDKApplication;

public class MyApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		ZaloSDKApplication.wrap(this);
	}
}
```

###  &bull;  IOS
Vào trang web http://developers.zalo.me tạo ứng dụng cho ios và điền thông tin theo yêu cầu.
##### 1. Trong `ios/App/Podfile` thêm:
```
target 'App' do
	capacitor_pods
	# Add your Pods here
	...
	pod 'ZaloSDK'
	...
end
```

##### 2. Trong file `ios/App/App/AppDelegate.swift`

```swift
import UIKit
import ZaloSDK

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {
	var window: UIWindow?

	func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplicationLaunchOptionsKey: Any]?) -> Bool {
	...
		/// 0a. Init zalo sdk
		ZaloSDK.sharedInstance().initialize(withAppId: "ZALO_APP_ID")
		return true
	}

	func application(_ app: UIApplication, open url: URL, options: [UIApplicationOpenURLOptionsKey : Any] = [:]) -> Bool {
		/// 0b. Receive callback from zalo
		return ZDKApplicationDelegate.sharedInstance().application(app, open: url, options: options)
	}
	...
}
```

**Chú ý**
* Thay **ZALO_APP_ID** được lấy từ http://developers.zalo.me

##### 3. Trong `ios/App/App/Info.plist`

```xml
...
 	<key>LSApplicationQueriesSchemes</key>
	<array>
		<string>zalosdk</string>
	</array>
...
```
**Chú ý**
* Thay **ZALO_APP_ID** được lấy từ http://developers.zalo.me

##### 4. Thêm URL Types để Zalo app gọi lại
Vào target setting -> info -> URK types -> click + để thêm URL Types với:
* Identifier: `zalo`
* URL Schemes: `zalo-ZALO_APP_ID` (Thay **ZALO_APP_ID** được lấy từ http://developers.zalo.me )

<img src="./images/url_type.png" />


## Sử dụng dịch vụ Zalo trong ứng dụng của bạn

```typescript
import { UserProfile, ZaloPlugin } from 'zalo-capacitor-plugin';

const MyApp = () => {
	useEffect(() => {

		//Lắng nghe sự kiện
		ZaloPlugin.addListener("onEvent", (result) => {
			console.log(result);
		});

		//Lấy hash key của ứng dụng android
		ZaloPlugin.getHashKeyAndroid().then(
			(res: HashKeyAndroid) => {
				console.log(res.hashKey);
			}
		);

		//Đăng nhập với Zalo
		ZaloPlugin.login()
		.then((res: LoginResponse) => {
			if (res.success) {
				//Sau khi đăng nhập thành công thì lấy dữ liệu người dùng
				ZaloPlugin.getProfile()
				.then((data: UserProfile) => {
					console.log('ID:  ', data.id);
					console.log('NAME:  ', data.name);
					console.log('GENDER:  ', data.gender);
					console.log('BIRTHDAY:  ', data.birthday);
					console.log('PICTURE: ', data?.picture);
				});
			}  else {
				//không thể đăng nhập
				console.log('lỗi: ', res.error);
			}
		});

		//Đăng xuất Zalo
		ZaloPlugin.logout();
	}, []);
}

```

## API
<docgen-index>

* [`getProfile()`](#getprofile)
* [`login()`](#login)
* [`logout()`](#logout)
* [`getHashKeyAndroid()`](#gethashkeyandroid)
* [`addListener('onEvent', ...)`](#addlisteneronevent)
* [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### getProfile()

```typescript
getProfile() => Promise<UserProfile>
```

**Returns:** <code>Promise&lt;<a href="#userprofile">UserProfile</a>&gt;</code>

--------------------


### login()

```typescript
login() => Promise<LoginResponse>
```

**Returns:** <code>Promise&lt;<a href="#loginresponse">LoginResponse</a>&gt;</code>

--------------------


### logout()

```typescript
logout() => Promise<void>
```

--------------------


### getHashKeyAndroid()

```typescript
getHashKeyAndroid() => Promise<HashKeyAndroid>
```

**Returns:** <code>Promise&lt;<a href="#hashkeyandroid">HashKeyAndroid</a>&gt;</code>

--------------------


### addListener('onEvent', ...)

```typescript
addListener(eventName: 'onEvent', listenerFunc: (result: any) => void) => Promise<PluginListenerHandle> & PluginListenerHandle
```

| Param              | Type                                  |
| ------------------ | ------------------------------------- |
| **`eventName`**    | <code>'onEvent'</code>                |
| **`listenerFunc`** | <code>(result: any) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt; & <a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

--------------------


### Interfaces


#### UserProfile

| Prop           | Type                                                         |
| -------------- | ------------------------------------------------------------ |
| **`success`**  | <code>boolean</code>                                         |
| **`id`**       | <code>string</code>                                          |
| **`name`**     | <code>string</code>                                          |
| **`gender`**   | <code>string</code>                                          |
| **`birthday`** | <code>string</code>                                          |
| **`picture`**  | <code>{ readonly data?: { readonly url?: string; }; }</code> |


#### LoginResponse

| Prop            | Type                 |
| --------------- | -------------------- |
| **`success`**   | <code>boolean</code> |
| **`oauthCode`** | <code>string</code>  |


#### HashKeyAndroid

| Prop          | Type                 |
| ------------- | -------------------- |
| **`success`** | <code>boolean</code> |
| **`hashKey`** | <code>string</code>  |


#### PluginListenerHandle

| Prop         | Type                                      |
| ------------ | ----------------------------------------- |
| **`remove`** | <code>() =&gt; Promise&lt;void&gt;</code> |

</docgen-api>

## VERSION

| phiên bản | mô tả |
|---|---|
|0.0.8| sửa một số lỗi trên ios|
|0.0.6| bổ sung cho ios|
|0.0.4| sửa một số lỗi trên android|
|0.0.3| bổ sung cho android|