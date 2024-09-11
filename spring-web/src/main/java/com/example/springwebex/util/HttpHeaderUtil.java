package com.example.springwebex.util;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Enumeration;

public class HttpHeaderUtil {

    private static final String BEARER_TYPE = "Bearer";

    /**
     * get Autorizaion header
     */
    public static String getToken(Enumeration<String> authorization) {
        while (authorization.hasMoreElements()) {
            String value = authorization.nextElement();
            if (StringUtil.toLowerCase(value).startsWith(StringUtil.toLowerCase(BEARER_TYPE))) {
                String authHeaderValue = value.substring(BEARER_TYPE.length()).trim();
                int commaIndex = authHeaderValue.indexOf(',');
                if (commaIndex > 0) {
                    authHeaderValue = authHeaderValue.substring(0, commaIndex);
                }
                return authHeaderValue;
            }
        }
        return null;
    }

    /**
     * get Autorizaion header after base64 decoding
     */
    public static String getBase64DecodedToken(Enumeration<String> authorization) throws UnsupportedEncodingException {
        while (authorization.hasMoreElements()) {
            String value = authorization.nextElement();
            if (StringUtil.toLowerCase(value).startsWith(StringUtil.toLowerCase(BEARER_TYPE))) {
                String authHeaderValue = value.substring(BEARER_TYPE.length()).trim();
                int commaIndex = authHeaderValue.indexOf(',');
                if (commaIndex > 0) {
                    authHeaderValue = authHeaderValue.substring(0, commaIndex);
                }
                return new String(Base64.getDecoder().decode(authHeaderValue.getBytes(StandardCharsets.UTF_8)),
                        StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    /**
     * get Autorizaion header after base64 decoding
     */
    public static String getBase64DecodedToken(String authorization) throws UnsupportedEncodingException {
        if (StringUtil.toLowerCase(authorization).startsWith(StringUtil.toLowerCase(BEARER_TYPE))) {
            String authHeaderValue = authorization.substring(BEARER_TYPE.length()).trim();
            int commaIndex = authHeaderValue.indexOf(',');
            if (commaIndex > 0) {
                authHeaderValue = authHeaderValue.substring(0, commaIndex);
            }
            return new String(Base64.getDecoder().decode(authHeaderValue.getBytes(StandardCharsets.UTF_8)),
                    StandardCharsets.UTF_8);
        }
        return null;
    }

}