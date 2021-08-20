package com.felipecarrillo100.rapidproxyserver.components;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class HandleOptionsFilter implements Filter {

    @Value("${ogc.proxy.baseurl:/proxy}")
    private String baseurl;

    @Value("${ogc.proxy.baseurl3d:/proxy3d}")
    private String baseurl3d;

    @Value("${ogc.proxy.cors.enabled:true}")
    private Boolean CORS_ENABLED;

    @Value("${ogc.proxy.cors.ACCESS_CONTROL_ALLOW_ORIGIN:*}")
    private String ACCESS_CONTROL_ALLOW_ORIGIN;

    //   @Value("${ogc.proxy.cors.ACCESS_CONTROL_ALLOW_HEADERS:X-API-KEY, Origin, X-Requested-With, Content-Type, Accept, Access-Control-Request-Method}")
    @Value("${ogc.proxy.cors.ACCESS_CONTROL_ALLOW_HEADERS:*}")
    private String ACCESS_CONTROL_ALLOW_HEADERS;

    @Value("${ogc.proxy.cors.ACCESS_CONTROL_ALLOW_METHODS:GET, POST, OPTIONS, PUT, PATCH, DELETE}")
    private String ACCESS_CONTROL_ALLOW_METHODS;

    @Value("${ogc.proxy.cors.ALLOW:GET, POST, OPTIONS, PUT, PATCH, DELETE}")
    private String ALLOW;

    @Value("${ogc.proxy.securedkey:}")
    private String securedKey;

    @Value("${ogc.proxy.enabled:false}")
    private Boolean enabled;

    /**
     * doFilter:  Takes care of answering the options request (preflight) required by CORS
     * @param req
     * @param res
     * @param chain
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest r = (HttpServletRequest) req;
        String url = r.getServletPath();

        if (CORS_ENABLED && "OPTIONS".equalsIgnoreCase(((HttpServletRequest) req).getMethod()) && (url.startsWith(baseurl) || url.startsWith(baseurl3d))) {
            // Getting servlet request URL
            int maxNest = 4;
            String s;
            if (url.startsWith(baseurl3d) ) {
                s = url.substring(baseurl3d.length());
                maxNest = 10;
            } else {
                s = url.substring(baseurl.length());
            }
            String[] parts = s.split("/");
            if ( 1 < parts.length  && parts.length < maxNest) {
                final HttpServletResponse response = (HttpServletResponse) res;
                response.setHeader("Access-Control-Allow-Origin", ACCESS_CONTROL_ALLOW_ORIGIN);
                response.setHeader("Access-Control-Allow-Methods", ACCESS_CONTROL_ALLOW_METHODS);
                response.setHeader("Access-Control-Allow-Headers", ACCESS_CONTROL_ALLOW_HEADERS);
                response.setHeader("Access-Control-Max-Age", "3600");
                response.setStatus(HttpServletResponse.SC_OK);
            }
            return;
        }
        chain.doFilter(req, res);
    }
    @Override
    public void destroy() {
    }
    @Override
    public void init(FilterConfig config) throws ServletException {
    }
}