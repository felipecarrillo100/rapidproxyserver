package com.felipecarrillo100.rapidproxyserver.controllers;

import org.json.simple.parser.JSONParser;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

public class ProxyRequest {
    private static String tokenKey = "token";
    private JSONObject targets;
    private HashMap<String, String> requestHeaders;

    public ProxyRequest(HttpServletRequest req) {
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
                }
            }
        }
        this.requestHeaders = requestHeaders;
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

    public JSONObject getTargets() {
        return targets;
    }

    public void setTargets(JSONObject targets) {
        this.targets = targets;
    }

    public HashMap<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(HashMap<String, String> requestHeaders) {
        this.requestHeaders = requestHeaders;
    }
}
