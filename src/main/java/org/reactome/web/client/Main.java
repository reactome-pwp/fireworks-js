package org.reactome.web.client;

import com.google.gwt.core.client.EntryPoint;
import org.timepedia.exporter.client.ExporterUtil;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class Main implements EntryPoint {

    @Override
    public void onModuleLoad() {
        // Export all Exportable classes
        ExporterUtil.exportAll();

        //For test purposes ONLY!
        init();
    }

    private static native void init() /*-{
        if($wnd.onReactomeFireworksReady){
            $wnd.onReactomeFireworksReady();
        }
    }-*/;
}
