package com.felipecarrillo100.rapidproxyserver.controllers;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.security.Principal;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Controller
@ConditionalOnExpression("${ogc.proxy.enabled:false}")
@RequestMapping("${ogc.proxy.baseurl:/proxy}")
public class OGCProxy implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(OGCProxy.class);

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

    @Value("${ogc.proxy.baseurl:/proxy}")
    private String baseurl;

    @GetMapping("/{uid}/{index}")
    public void getMethod(@PathVariable String uid, @PathVariable String index, HttpServletRequest req, HttpServletResponse resp, Principal principal) {
        // logger.info(uid + "/" + index);
        forwardRequest(index, "GET", req, resp);
    }

    @DeleteMapping("/{uid}/{index}")
    public void deleteMethod(@PathVariable String uid, @PathVariable String index, HttpServletRequest req, HttpServletResponse resp, Principal principal) {
        // logger.info(uid + "/" + index);
        forwardRequest(index, "DELETE", req, resp);
    }

    @PostMapping("/{uid}/{index}")
    public void postMethod(@PathVariable String uid, @PathVariable String index, HttpServletRequest req, HttpServletResponse resp, Principal principal) {
        // logger.info(uid + "/" + index);
        forwardRequest(index, "POST", req, resp);
    }

    @PutMapping("/{uid}/{index}")
    public void putMethod(@PathVariable String uid, @PathVariable String index, HttpServletRequest req, HttpServletResponse resp, Principal principal) {
        // logger.info(uid + "/" + index);
        forwardRequest(index, "PUT", req, resp);
    }

    @PatchMapping("/{uid}/{index}")
    public void PatchMapping(@PathVariable String uid, @PathVariable String index, HttpServletRequest req, HttpServletResponse resp, Principal principal) {
        // logger.info(uid + "/" + index);
        forwardRequest(index, "PATCH", req, resp);
    }


//    @RequestMapping(value="/{uid}/{index}", method = RequestMethod.OPTIONS)
//    public void optionsMethod(@PathVariable String uid, @PathVariable String index, HttpServletRequest req, HttpServletResponse resp, Principal principal) {
//        // logger.info(uid + "/" + index);
//        String content = "{\"methods\":[\"POST\",\"GET\",\"OPTIONS\"]}";
//        resp.setStatus( 200 );
//
//        resp.addHeader( HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8" );
//        setCorsHeaders(resp);
//
//        resp.setContentLength(content.length());
//        try {
//            resp.getWriter().write(content);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return;
//    }

    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info("** Super proxy **");
        if (enabled) {
            logger.info(" * Proxy has been enabled at: " + baseurl + "/{uid}/{index}");
        }
    }

    private void forwardRequest(String index, String method, HttpServletRequest req, HttpServletResponse resp) {
        final boolean hasoutbody = (method.equals("POST") || method.equals("PATCH") || method.equals("PUT"));
        String targetUri = "";
        String queryString = req.getQueryString();

        ProxyRequest proxyRequest = new ProxyRequest(req);

        Iterator<Map.Entry<String, String>> iterator = proxyRequest.getRequestHeaders().entrySet().iterator();
        JSONObject targets = proxyRequest.getTargets();
        JSONObject urls = (JSONObject) targets.get("urls");
        if (urls != null) {
            String url = (String) urls.get(index);
            if (url != null) {
                targetUri = url;
            }
        }
        if (!securedKey.equals("")) {
            String myPasskey = (String) targets.get("key");
            if (myPasskey == null ) {
                targetUri = "";
            } else {
                 if(!securedKey.equals(myPasskey)) {
                     targetUri = "";
                 }
            }
        }
        if (targetUri.equals("")) {
            String content = "{\"log\":\"Invalid proxy request\"}";
            resp.setStatus( 400 );
            resp.addHeader( HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8" );
            resp.setContentLength(content.length());
            setCorsHeaders(resp);
            try {
                resp.getWriter().write(content);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        if (queryString != null && !queryString.trim().equals("")) targetUri += "?" + queryString;
        try {
            final URL url = new URL(targetUri);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            while (iterator.hasNext()) {
                Map.Entry<String, String> pair = (Map.Entry<String, String>) iterator.next();
                conn.addRequestProperty(pair.getKey(), pair.getValue());
            }
            conn.setRequestMethod(method);
            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(hasoutbody);
            conn.connect();

            // write BODY
            final byte[] buffer = new byte[16384];
            while (hasoutbody) {
                final int read;
                read = req.getInputStream().read(buffer);
                if (read <= 0) {
                    break;
                }
                conn.getOutputStream().write(buffer, 0, read);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode==401)
                resp.setStatus(403);  // prevents a credentials popup
            else
                resp.setStatus(responseCode);

            boolean isErrorResponse = responseCode >= HttpURLConnection.HTTP_BAD_REQUEST;
            InputStream responseStream = isErrorResponse ? conn.getErrorStream() : conn.getInputStream();

            Map<String, List<String>> map = conn.getHeaderFields();
            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                String headerName = entry.getKey();
                if (headerName != null) {
                    List<String> headerValues = entry.getValue();
                    for (String value : headerValues) {
                        if (headerName.toLowerCase().equals("transfer-encoding")) {
                            //  If header == trnasfer-encoding then do nothing
                        } else {
                            // Else pass the header to browser...
                            resp.addHeader(headerName, value);
                        }
                    }
                }
            }
            setCorsHeaders(resp);
            long fileSize = 0;
            while (true) {
                final int read = responseStream.read(buffer);
                fileSize += read;
                if (read <= 0) {
                    break;
                }
                resp.getOutputStream().write(buffer, 0, read);
            }
            // Added Writer flush.  Needed for POST messages or you will  no receive response at client side.
            resp.getOutputStream().flush();
        } catch (ProtocolException e) {
           // e.printStackTrace();
//            logger.error("Protocol Error");
        } catch (MalformedURLException e) {
          //  e.printStackTrace();
//            logger.error("MalformedURLException Error");
        } catch (IOException e) {
          //  e.printStackTrace();
//            logger.error("IOException Error");
            String content = "{\"log\":\"Server not reachable\"}";
            resp.setStatus( 404 );
            resp.addHeader( HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8" );
            setCorsHeaders(resp);
            resp.setContentLength(content.length());
            try {
                resp.getWriter().write(content);
            } catch (IOException ie) {
                e.printStackTrace();
            }
            return;
        }
    }

    void setCorsHeaders( HttpServletResponse resp) {
        if (CORS_ENABLED) {
            resp.setHeader( HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,ACCESS_CONTROL_ALLOW_ORIGIN);
            resp.setHeader( HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, ACCESS_CONTROL_ALLOW_HEADERS );
            resp.setHeader( HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, ACCESS_CONTROL_ALLOW_METHODS );
            resp.setHeader( HttpHeaders.ALLOW, ALLOW );
        }
    }

}
