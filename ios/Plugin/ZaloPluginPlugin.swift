import Foundation
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

    @objc public login(_ call: CAPPluginCall) {
        implementation.codeVerifier = generateCodeVerifier() ?? ""
        implementation.codeChallenge = generateCodeChallenge(codeVerifier: codeVerifier)
        DispatchQueue.main.async {
            ZaloSDK.sharedInstance().authenticateZalo(with: type, parentController: presentedViewController, codeChallenge: codeChallenege, extInfo: nil, handler: {(response) in

                if response?.isSucess == true {
                    implementation.oauthCode =  response?.oauthCode ?? ""
                    self.getAccessToken(oauthCode: oauthCode, codeVerifier: codeVerifier, completion: {(tokenResponse) in
                        if (tokenResponse?.isSucess == true) {
                            implementation.accessToken = tokenResponse?.accessToken ?? ""
                            implementation.refreshToken = tokenResponse?.refreshToken ?? ""
                            
                            call.resolve([
                                "success": true,
                                "oauthCode": implementation.oauthCode
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

    @objc public getProfile(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            ZaloSDK.sharedInstance().getZaloUserProfile(withAccessToken: tokenData.accessToken, callback: {(response) in
                if response!.errorCode == ZaloSDKErrorCode.sdkErrorCodeNoneError.rawValue {
                    call.resolve(response!.data)
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

    @objc public logout(_ call: CAPPluginCall) {
        ZaloSDK.sharedInstance().unauthenticate()
        call.resolve(["success", true])
    }
}
