package org.reactome.web.client.model;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class JsProperties {
    JavaScriptObject prop = null;

    public JsProperties(JavaScriptObject properties) {
        this.prop = properties;
    }

    public String get(String name) {
        return getImpl(this.prop, name);
    }

    public String get(String name, String defaultValue){
        String value = get(name);
        return value == null ? defaultValue : value;
    }

    public int getInt(String name) {
        String val = get(name);
        return val == null ? 0 : Integer.valueOf(val);
    }

    public int getInt(String name, int defaultValue){
        String value = get(name);
        return value == null ? defaultValue : Double.valueOf(value).intValue();
    }

    private static native String getImpl(JavaScriptObject p, String name) /*-{
        return p[name] ? p[name].toString() : p[name] === false ? "false" : null;
    }-*/;
}
