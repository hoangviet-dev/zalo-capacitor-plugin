package com.hviet.capacitor.plugins.zalo;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;
import android.util.Log;

import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.HttpResponse;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.HttpClient;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.methods.HttpGet;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class ZaloPlugin {

    private String codeChallenge;
    private String codeVerifier;
    private String hashKey = null;

    public String AccessToken = null;
    public String OauthCode = null;
    public String RefreshToken = null;

    public String getApplicationHashKey(Context ctx) {
        if (hashKey != null) {
            return hashKey;
        }
        try {
            PackageInfo info = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String sig = Base64.encodeToString(md.digest(), Base64.DEFAULT).trim();
                if (sig.trim().length() > 0) {
                    hashKey = sig;
                    return sig;
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
    
    public String genCodeVerifier() {
        SecureRandom sr = new SecureRandom();
        byte[] code = new byte[32];
        sr.nextBytes(code);
        return Base64.encodeToString(code, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
    }

    public String genCodeChallenge() {
        codeVerifier = genCodeVerifier();
        String result = null;
        try {
            byte[] bytes = codeVerifier.getBytes("ASCII");
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(bytes, 0, bytes.length);
            byte[] digest = md.digest();
            result = Base64.encodeToString(digest, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
        } catch (Exception ex) {
            Log.e(ex.getMessage(), ex.toString());
        }
        codeChallenge = result;
        Log.d("codeChallenge", codeChallenge);
        return result;
    }

    public String getCodeVerifier() {
        return codeVerifier;
    }

    public String getCodeChallenge() {
        return codeChallenge;
    }

    public String Authorization(Long appId, String pkgName, String signKey, String codeChallenge, String state) {
        String url = String.format(
                "https://oauth.zaloapp.com/v4/permission?app_id=%d&pkg_name=%s&sign_key=%s&code_challenge=%s&state=%s&os=android",
                appId,
                pkgName,
                signKey,
                codeChallenge,
                state
        );
        return getRequest(url);
    }

    public static String getRequest(String url) {
        StringBuffer stringBuffer = new StringBuffer("");
        BufferedReader bufferedReader = null;
        try {
            HttpClient httpClient = HttpClientBuilder.create().build();;
            HttpGet httpGet = new HttpGet();

            URI uri = new URI(url);
            httpGet.setURI(uri);

            HttpResponse httpResponse = httpClient.execute(httpGet);
            InputStream inputStream = httpResponse.getEntity().getContent();
            bufferedReader = new BufferedReader(new InputStreamReader(
                    inputStream));

            String readLine = bufferedReader.readLine();
            while (readLine != null) {
                stringBuffer.append(readLine);
                stringBuffer.append("\n");
                readLine = bufferedReader.readLine();
            }
        } catch (Exception e) {
            // TODO: handle exception
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    // TODO: handle exception
                }
            }
        }
        return stringBuffer.toString();
    }
}
