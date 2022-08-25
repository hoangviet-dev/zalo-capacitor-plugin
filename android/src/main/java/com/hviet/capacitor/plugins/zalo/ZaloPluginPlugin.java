package com.hviet.capacitor.plugins.zalo;

import android.util.Log;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.zing.zalo.zalosdk.oauth.LoginVia;
import com.zing.zalo.zalosdk.oauth.OAuthCompleteListener;
import com.zing.zalo.zalosdk.oauth.OauthResponse;
import com.zing.zalo.zalosdk.oauth.ZaloOpenAPICallback;
import com.zing.zalo.zalosdk.oauth.ZaloSDK;
import com.zing.zalo.zalosdk.oauth.model.ErrorResponse;

import org.json.JSONException;
import org.json.JSONObject;

@CapacitorPlugin(name = "ZaloPlugin")
public class ZaloPluginPlugin extends Plugin {

    private ZaloPlugin implementation = new ZaloPlugin();

    @PluginMethod
    public void getProfile(PluginCall call) {
        JSObject ret = new JSObject();
        ZaloOpenAPICallback listener = new ZaloOpenAPICallback() {
            @Override
            public void onResult(JSONObject data) {
                try {
                    ret.put("id", data.getString("id"));
                    ret.put("name", data.getString("name"));
                    ret.put("gender", data.getString("gender"));
                    ret.put("birthday", data.getString("birthday"));
                    ret.put("picture", data.getString("picture"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                call.resolve(ret);
            }
        };

        String[] fields = {"id", "name", "gender", "birthday", "picture"};

        ZaloSDK.Instance.getProfile(
                getContext(),
                implementation.AccessToken,
                listener,
                fields
        );
    }

    @PluginMethod
    public void login(PluginCall call) {
        final JSObject ret = new JSObject();
        OAuthCompleteListener listener = new OAuthCompleteListener() {
            @Override
            public void onAuthenError(ErrorResponse errorResponse) {
                ret.put("success", false);
                ret.put("error", new ErrorType("ERROR_001", "Authenticate failed. Progress dismissed"));
            }

            @Override
            public void onGetOAuthComplete(OauthResponse response) {
                String code = response.getOauthCode();

                implementation.OauthCode = code;

                ZaloSDK.Instance.getAccessTokenByOAuthCode(
                        getContext(),
                        code,
                        implementation.getCodeVerifier(),
                        new ZaloOpenAPICallback() {
                            @Override
                            public void onResult(JSONObject data) {
                                int err = data.optInt("error");
                                if (err == 0) {
                                    //clearOauthCodeInfo(); //clear used oacode

                                    implementation.AccessToken = data.optString("access_token");
                                    implementation.RefreshToken = data.optString("refresh_token");
                                    long expires_in = Long.parseLong(data.optString("expires_in"));

                                    ret.put("success", true);
                                    ret.put("oauthCode", code);
                                    call.resolve(ret);
                                } else {
                                    ret.put("success", false);
                                    ret.put("error", new ErrorType("ERROR_001", "Authenticate failed. Progress dismissed"));
                                    call.resolve(ret);
                                }
                            }
                        }
                );

            }
        };

        implementation.genCodeChallenge();
        if (implementation.getCodeChallenge() != null) {
//            Log.d("Authorization", res);
            ZaloSDK.Instance.authenticateZaloWithAuthenType(this.getActivity(), LoginVia.APP_OR_WEB, implementation.getCodeChallenge(), listener);
        }

    }

    @PluginMethod
    public void logout(PluginCall call) {
        JSObject ret = new JSObject();
        ZaloSDK.Instance.unauthenticate();
        ret.put("success", true);
        call.resolve(ret);
    }

    @PluginMethod
    public void getHashKeyAndroid(PluginCall call) {
        JSObject ret = new JSObject();
        String hashKey = implementation.getApplicationHashKey(getContext());
        Log.d("hashKey", hashKey);
        ret.put("hashKey", hashKey);
        call.resolve(ret);
    }
}

class ErrorType {

    private String code;
    private String message;

    public ErrorType(String code, String message) {
        this.code = code;
        this.message = message;
    }
}