mport Foundation
import Capacitor
import ZaloSDK

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(ZaloPluginPlugin)
public class ZaloPluginPlugin: CAPPlugin {
    private let implementation = ZaloPlugin()

    // @objc func echo(_ call: CAPPluginCall) {
    //     let value = call.getString("value") ?? ""
    //     call.resolve([
    //         "value": implementation.echo(value)
    //     ])
    // }

    private func getAccessToken(oauthCode: String, codeVerifier: String, completion: @escaping (ZOTokenResponseObject?) -> Void) {
        ZaloSDK.sharedInstance().getAccessToken(withOAuthCode: oauthCode, codeVerifier: codeVerifier, completionHandler: { (response) in
            completion(response)
        })
    }

    @objc public func login(_ call: CAPPluginCall) {
        self.implementation.codeVerifier = generateCodeVerifier() ?? ""
        self.implementation.codeChallenge = generateCodeChallenge(codeVerifier: self.implementation.codeVerifier) ?? ""
        DispatchQueue.main.async {
            ZaloSDK.sharedInstance().authenticateZalo(with: ZAZAloSDKAuthenTypeViaZaloAppAndWebView, parentController: self.bridge?.viewController, codeChallenge: self.implementation.codeChallenge, extInfo: nil, handler: {(response) in

                if response?.isSucess == true {
                    self.implementation.oauthCode =  response?.oauthCode ?? ""
                    self.getAccessToken(oauthCode: self.implementation.oauthCode, codeVerifier: self.implementation.codeVerifier, completion: {(tokenResponse) in
                        if (tokenResponse?.isSucess == true) {
                            self.implementation.accessToken = tokenResponse?.accessToken ?? ""
                            self.implementation.refreshToken = tokenResponse?.refreshToken ?? ""
                            
                            call.resolve([
                                "success": true,
                                "oauthCode": self.implementation.oauthCode
                            ])
                        } else {
                            call.resolve([
                                "success": false,
                                "error": [
                                    "code":"ERROR_001",
                                    "message":"Authenticate failed. Progress dismissed"
                                ]
                            ])
                        }
                    })

                } else {
                    call.resolve([
                        "success": false,
                        "error": [
                            "code":"ERROR_001",
                            "message":"Authenticate failed. Progress dismissed"
                        ]
                    ])
                }
            })
        }
    }

    @objc func getProfile(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            ZaloSDK.sharedInstance().getZaloUserProfile(withAccessToken: self.implementation.accessToken, callback: {(response) in
                if response!.errorCode == ZaloSDKErrorCode.sdkErrorCodeNoneError.rawValue {
                    call.resolve(
                        [
                            "id": response?.data["id"],
                            "name": response?.data["name"],
                            "gender": response?.data["gender"],
                            "birthday": response?.data["birthday"],
                            "picture": response?.data["picture"],
                        ]
                    )
                } else {
                    call.resolve([
                        "success": false,
                        "error": [
                            "code": "ERROR_002",
                            "message": "OpenAPI error"
                        ]
                    ])
                }
            })
        }
    }

    @objc func logout(_ call: CAPPluginCall) {
        ZaloSDK.sharedInstance().unauthenticate()
        call.resolve(["success": true])
    }
}