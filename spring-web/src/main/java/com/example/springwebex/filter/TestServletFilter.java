package com.example.springwebex.filter;

import java.io.IOException;
import org.springframework.stereotype.Component;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

@Component
public class TestServletFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // do nothing
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        /*-
         * this context run before controller processing
         */
        // if (request instanceof HttpServletRequest) {
        //     String url = ((HttpServletRequest) request).getRequestURL().toString();
        //     String queryString = ((HttpServletRequest) request).getQueryString();
        //     System.out.println("filter::request::url=" + url);
        //     System.out.println("filter::request::queryString=" + queryString);
        // }

        // // "X-TEST-HEADER" will be added by TestServletInterceptor.postHandle()
        // if (response instanceof HttpServletResponse) {
        //     System.out.println("filter::response::modified-header="
        //             + ((HttpServletResponse) response).getHeader("X-TEST-HEADER"));
        // }

        /*-
         * processing next filter chain and spring controller
         */
        chain.doFilter(request, response);

        /*-
        * this context run after controller processing
        */
        // if (response instanceof HttpServletResponse) {
        // int code = ((HttpServletResponse) response).getStatus();
        // System.out.println("filter::response::code=" + code);

        // // "X-TEST-HEADER" should be added by TestServletInterceptor.postHandle()
        // System.out.println("filter::response::modified-header="
        //         + ((HttpServletResponse) response).getHeader("X-TEST-HEADER"));
        // }
    }

    @Override
    public void destroy() {
        // do nothing
    }

}


