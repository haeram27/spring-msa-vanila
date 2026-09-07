package com.example.springwebex.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/*-
 * HandlerInterceptor SHOULD be wrapped by MappedInterceptor and added as Bean in @Configuration.
 */
public class TestServletInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
            Object handler) throws Exception {
        // String url = request.getRequestURL().toString();
        // String queryString = request.getQueryString();
        // System.out.println("interceptor::preHandle::request::url=" + url);
        // System.out.println("interceptor::preHandle::request::queryString=" + queryString);

        // int code = response.getStatus();
        // System.out.println("interceptor::preHandle::request::code=" + code);

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            @Nullable ModelAndView modelAndView) throws Exception {
        // String url = request.getRequestURL().toString();
        // String queryString = request.getQueryString();
        // System.out.println("interceptor::postHandle::request::url=" + url);
        // System.out.println("interceptor::postHandle::request::queryString=" + queryString);

        // int code = response.getStatus();
        // System.out.println("interceptor::postHandle::request::code=" + code);
        // response.addHeader("X-TEST-HEADER", "test header");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
            Object handler, @Nullable Exception ex) throws Exception {
        // String url = request.getRequestURL().toString();
        // String queryString = request.getQueryString();
        // System.out.println("interceptor::afterCompletion::request::url=" + url);
        // System.out.println("interceptor::afterCompletion::request::queryString=" + queryString);

        // int code = response.getStatus();
        // System.out.println("interceptor::afterCompletion::request::code=" + code);
    }

}
