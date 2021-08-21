package io.github.felipecarrillo100.controllers;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.Principal;

public class ProxyRequestProvider {

    private static Constructor proxyRequestClass = null;

    public static ProxyRequest createProxyRequestProvider(HttpServletRequest req, String index, Principal principal) {
        try {
            Object i = proxyRequestClass.newInstance(req, index, principal);
            return (ProxyRequest) i;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isSet() {
        return proxyRequestClass != null;
    }

    public static void setProxyRequest(Class c) {
        try {
            Class[] cArg = new Class[3]; //Our constructor has 3 arguments
            cArg[0] = HttpServletRequest.class; //First argument is of *object* type Long
            cArg[1] = String.class; //First argument is of *object* type Long
            cArg[2] = Principal.class; //Second argument is of *object* type String

            Constructor ob = c.getDeclaredConstructor(cArg);
            ProxyRequestProvider.proxyRequestClass = ob;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
}
