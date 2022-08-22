package com.hviet.capacitor.plugins.zalo;

import android.util.Log;

public class ZaloPlugin {

    public String echo(String value) {
        Log.i("Echo", value);
        return value;
    }
}
