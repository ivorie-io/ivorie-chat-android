package com.ivoriechat.android.utils;

import android.content.Context;

import java.net.MalformedURLException;
import java.net.URL;

public class Utils {
    public static URL constructURL(String destination) {
        URL url = null;
        try {
            url = new URL(AppGeneral.SERVER_SECURE_PROTOCOL +
                    AppGeneral.SERVER_DOMAIN_NAME +
                    AppGeneral.COLON +
                    AppGeneral.SERVER_PORT_NUMBER +
                    AppGeneral.WEB_MODULE_PATH +
                    destination);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

}
