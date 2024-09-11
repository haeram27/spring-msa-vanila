package com.example.springwebex.util;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ServletUtil {
    public static String getClientIp(HttpServletRequest httpServletRequest) {
        String ip = httpServletRequest.getHeader("X-FORWARDED-FOR");
        if (StringUtil.isEmpty(ip)) {
            ip = httpServletRequest.getHeader("Proxy-Client-IP");
        }
        if (StringUtil.isEmpty(ip)) {
            ip = httpServletRequest.getHeader("WL-Proxy-Client-IP"); // WebLogic
        }
        if (StringUtil.isEmpty(ip)) {
            ip = httpServletRequest.getRemoteAddr();
        }
        return ip;
    }

    public static String getClientIp() {
        return getClientIp(
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest());
    }

    public static String getLoginId() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest()
                .getAttribute("adminId") != null
                        ? ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                                .getRequest().getAttribute("adminId").toString()
                        : "";
    }

    public static String getConnectedLoginIp() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest()
                .getAttribute("connectedAdminIp") != null
                        ? ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                                .getRequest().getAttribute("connectedAdminIp").toString()
                        : "";
    }

    private static String getAuthorizationHeader(HttpServletRequestWrapper header) {

        String accessToken = null;

        try {
            /* Warning : Browser encodes Authrization value using Base64 */
            accessToken = HttpHeaderUtil.getBase64DecodedToken(header.getHeaders("Authorization"));
        } catch (Exception e) {
            /* non Browser request */
            accessToken = header.getHeader("Authorization");

            if (StringUtil.isEmpty(accessToken)) {
                accessToken = header.getHeader("authorization");
            }
            if (!StringUtil.isEmpty(accessToken)) {
                accessToken = accessToken.replace("Bearer ", "").replace("bearer ", "");
            }
        }

        return accessToken;
    }

    public static String getLoginToken() {
        return getAuthorizationHeader(
                (HttpServletRequestWrapper) ((ServletRequestAttributes) RequestContextHolder
                        .currentRequestAttributes()).getRequest());
    }

    public static String getConnectServerIp(HttpServletRequest httpServletRequest) {
        return httpServletRequest.getLocalAddr();
    }
}
