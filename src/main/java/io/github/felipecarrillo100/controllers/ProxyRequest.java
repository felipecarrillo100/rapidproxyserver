package io.github.felipecarrillo100.controllers;

import org.json.simple.parser.JSONParser;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.security.Principal;

/**
 * Class ProxyRequest used to parse the proxy request present in the accept header.
 */
public class ProxyRequest {
    private static String tokenKey = "token";
    private JSONObject targets;
    private HashMap<String, String> requestHeaders;


    /**
     * ProxyRequest Constructor needed to create the request
     * @param req An HTTP request (HttpServletRequest)
     * @param index The index of the selected url
     * @param principal The principal in case of authentication required
     */
    public ProxyRequest(HttpServletRequest req, String index, Principal principal) {
        targets = new JSONObject();
        final Enumeration<String> headers = req.getHeaderNames();
        HashMap<String, String> requestHeaders = new HashMap<String, String>();;

        while (headers.hasMoreElements()) {
            final String header = headers.nextElement();
            final Enumeration<String> values = req.getHeaders(header);
            while (values.hasMoreElements()) {
                final String value = values.nextElement();
                if (header.toLowerCase().equals("accept")) {
                    String[] split = splitAccept(value);
                    String cleanValue = parseAndCleanAcceptValue(split);
                    requestHeaders.put(header, cleanValue);
                } else {
                    requestHeaders.put(header, value);
                }
            }
        }
        JSONObject authorization = (JSONObject) targets.get("authorization");
        if (authorization != null) {
            String authType = (String) authorization.get("type");
            if (authType!=null) {
                switch (authType.toLowerCase()) {
                    case "basic":
                        String value = (String) authorization.get("value");
                        SetAuthenticationWithType(requestHeaders, "Basic", value);
                        break;
                    case "bearer":
                        String bearer = (String) authorization.get("value");
                        SetAuthenticationWithType(requestHeaders, "Bearer", bearer);
                        break;
                    case "vault":
                        String borrowFrom = (String) authorization.get("value");
                        VaultAuthenticationValues authenticationValues = getValuesFromVault( req, index, principal, borrowFrom);
                        SetAuthenticationWithType(requestHeaders, authenticationValues.getMode(), authenticationValues.getValue());
                        break;
                }
            }
        }
        this.requestHeaders = requestHeaders;
    }

    protected VaultAuthenticationValues getValuesFromVault(HttpServletRequest req, String index, Principal principal, String borrowFrom) {
        String url = this.getUrlByIndex(index);
        return new VaultAuthenticationValues("","");
    }

    private void SetAuthenticationWithType(HashMap<String, String> requestHeaders, String type, String value) {
        requestHeaders.put("authorization", type + " " + value);
    }

    private String[] splitAccept(String value) {
        String[] parts = value.split(";");
        return parts;
    }

    private String parseAndCleanAcceptValue(String[] values) {
        List<String> entries = new ArrayList<String>(Arrays.asList(values));
        Iterator<String> iterator = entries.iterator();
        while(iterator.hasNext()){
            String currentEntry = iterator.next();
            if(currentEntry.startsWith(tokenKey+"=")){
                setTargets(parseTokenKey(currentEntry));
                iterator.remove();
            }
        }
        String value = String.join(";", entries);
        return value;
    }

    private JSONObject parseTokenKey(String currentEntry) {
        String encodedString = currentEntry.substring(tokenKey.length()+1);
        byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
        String jsonString = new String(decodedBytes);
        JSONObject json = new JSONObject();
        JSONParser parser = new JSONParser();

        try {
            json = (JSONObject) parser.parse(jsonString);
        } catch (ParseException e) {
            json = new JSONObject();
        }

        return json;
    }

    /**
     * getUrls  Retuens the list of URLS available in the encoded data in the accept header
     * @return A JSONObject with a list of urls
     */
    public JSONObject getUrls() {
        JSONObject targets = this.getTargets();
        JSONObject urls = (JSONObject) targets.get("urls");
        return urls;
    }

    public String getUrlByIndex(String index) {
        JSONObject urls = this.getUrls();
        String targetUri = null;
        if (urls != null) {
            String url = (String) urls.get(index);
            if (url != null) {
                targetUri = url;
            }
        }
        return targetUri;
    }

    /**
     * getTargets
     * @return A JSONObject with a list of urls
     */
    public JSONObject getTargets() {
        return targets;
    }

    protected void setTargets(JSONObject targets) {
        this.targets = targets;
    }

    /**
     * getRequestHeaders
     * @return a HashMap with the available heasders
     */
    public HashMap<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    protected void setRequestHeaders(HashMap<String, String> requestHeaders) {
        this.requestHeaders = requestHeaders;
    }
}
