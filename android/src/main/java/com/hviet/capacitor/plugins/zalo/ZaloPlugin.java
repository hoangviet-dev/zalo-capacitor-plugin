package com.hviet.capacitor.plugins.zalo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

//import com.zing.zalo.zalosdk.core.helper.Base64;

import com.zing.zalo.zalosdk.oauth.ZaloSDK;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

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
        PackageInfo info = null;
        try {
            info = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md;
                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String sig  = new String(android.util.Base64.encode(md.digest(), 0)).trim();
                //String something = new String(Base64.encodeBytes(md.digest()));
                Log.e("hash key", sig);
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
        String verifier = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            verifier = Base64.getUrlEncoder().withoutPadding().encodeToString(code);
        } else {
//            RandomString gen = new RandomString(43, ThreadLocalRandom.current());
//            verifier = gen.nextString();
            verifier = android.util.Base64.encodeToString(code, 0);
        }
//        RandomString gen = new RandomString(43, ThreadLocalRandom.current());
//        return gen.nextString();
        return verifier;
    }

    public String genCodeChallenge() {
        codeVerifier = genCodeVerifier();
        String result = null;
        try {
            byte[] bytes = codeVerifier.getBytes("ASCII");
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(bytes, 0, bytes.length);
            byte[] digest = md.digest();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                result = Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
            } else {
                result = android.util.Base64.encodeToString(digest, 0);
            }
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
        String command = String.format(
                "curl --location --request GET 'https://oauth.zaloapp.com/v4/permission?app_id=%d&pkg_name=%s&sign_key=%s&code_challenge=%s&state=%s&os=android'",
                appId,
                pkgName,
                signKey,
                codeChallenge,
                state
        );
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new
                    InputStreamReader(process.getInputStream()));
            String line;
            String response = "";
            while ((line = reader.readLine()) != null) {
                response += line;
            }
            return response;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

class RandomString {
    /**
     * Generate a random string.
     */
    public String nextString() {
        for (int idx = 0; idx < buf.length; ++idx)
            buf[idx] = symbols[random.nextInt(symbols.length)];
        return new String(buf);
    }

    public static final String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static final String lower = upper.toLowerCase(Locale.ROOT);

    public static final String digits = "0123456789";

    public static final String alphanum = upper + lower + digits;

    private final Random random;

    private final char[] symbols;

    private final char[] buf;

    public RandomString(int length, Random random, String symbols) {
        if (length < 1) throw new IllegalArgumentException();
        if (symbols.length() < 2) throw new IllegalArgumentException();
        this.random = Objects.requireNonNull(random);
        this.symbols = symbols.toCharArray();
        this.buf = new char[length];
    }

    /**
     * Create an alphanumeric string generator.
     */
    public RandomString(int length, Random random) {
        this(length, random, alphanum);
    }

    /**
     * Create an alphanumeric strings from a secure generator.
     */
    public RandomString(int length) {
        this(length, new SecureRandom());
    }

    /**
     * Create session identifiers.
     */
    public RandomString() {
        this(21);
    }

}