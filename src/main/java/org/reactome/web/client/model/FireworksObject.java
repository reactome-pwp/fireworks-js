package org.reactome.web.client.model;

import com.google.gwt.core.client.JavaScriptObject;
import org.reactome.web.fireworks.model.Node;
import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.ExportPackage;
import org.timepedia.exporter.client.Exportable;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
@ExportPackage("Reactome")
@Export()
public class FireworksObject extends JavaScriptObject implements Exportable  {

    protected FireworksObject() {
    }

    public static FireworksObject create(Node node){
        FireworksObject obj = (FireworksObject) JavaScriptObject.createObject();
        obj.setStId(node.getStId());
        obj.setDisplayName(node.getName());
        obj.setSchemaClass(node.getClass().getSimpleName());
        return obj;
    }

    private native void setStId(String stId) /*-{
        this.stId = stId;
    }-*/;

    private native void setDisplayName(String displayName) /*-{
        this.displayName = displayName;
    }-*/;

    private native void setSchemaClass(String schemaClass) /*-{
        this.schemaClass = schemaClass;
    }-*/;
}
