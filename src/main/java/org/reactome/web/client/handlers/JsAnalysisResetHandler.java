package org.reactome.web.client.handlers;

import org.timepedia.exporter.client.ExportClosure;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
@ExportClosure
public interface JsAnalysisResetHandler extends JsFireworksHandler {
    void analysisReset();
}
