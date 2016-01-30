package org.reactome.web.client.handlers;

import org.reactome.web.client.model.FireworksObject;
import org.timepedia.exporter.client.ExportClosure;
import org.timepedia.exporter.client.Exportable;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
@ExportClosure
public interface JsNodeHoveredHandler extends JsFireworksHandler, Exportable {
    void hovered(FireworksObject object);
}
