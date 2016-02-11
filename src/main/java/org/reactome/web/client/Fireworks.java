package org.reactome.web.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import org.reactome.web.client.handlers.*;
import org.reactome.web.client.model.FireworksObject;
import org.reactome.web.client.model.JsProperties;
import org.reactome.web.fireworks.client.FireworksFactory;
import org.reactome.web.fireworks.client.FireworksViewer;
import org.reactome.web.fireworks.events.CanvasNotSupportedEvent;
import org.reactome.web.fireworks.events.FireworksLoadedEvent;
import org.reactome.web.fireworks.events.NodeHoverEvent;
import org.reactome.web.fireworks.events.NodeSelectedEvent;
import org.reactome.web.fireworks.handlers.*;
import org.reactome.web.pwp.model.client.RESTFulClient;
import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.ExportPackage;
import org.timepedia.exporter.client.Exportable;
import org.timepedia.exporter.client.NoExport;

import java.util.HashSet;
import java.util.Set;


/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
@SuppressWarnings("unused")
@ExportPackage("Reactome")
@Export("Fireworks")
public class Fireworks implements FireworksLoader.Handler, Exportable {

    protected static final String SERVER = "http://reactome.org";

    private static String holder;

    private static FireworksViewer viewer;
    private static FireworksLoader loader;
    private static HTMLPanel container;

    private static Integer width, height;

    private Set<JsFireworksHandler> handlers;

    private Fireworks() {
        handlers = new HashSet<>();
    }

    public static Fireworks create(JavaScriptObject input) {
        JsProperties jsProp = new JsProperties(input);
        return create(jsProp.get("placeHolder"), jsProp.getInt("species", 48887), jsProp.get("proxyPrefix", ""), jsProp.getInt("width", 500), jsProp.getInt("height", 400));
    }

    public static Fireworks create(String placeHolder, int species, int width, int height) {
        return create(placeHolder, species, "", width, height);
    }

    public static Fireworks create(String placeHolder, int species, String server, final int width, final int height) {
        final Element element = Document.get().getElementById(placeHolder);
        if (element == null)
            throw new RuntimeException("Reactome fireworks cannot be initialised. Please provide a valid 'placeHolder' (\"" + placeHolder + "\" invalid place holder).");

        Fireworks.width = width;
        Fireworks.height = height;

        container = HTMLPanel.wrap(element);
        container.clear();
        container.add(new Label("Loading Reactome's Pathways Overview. Please wait..."));
        Fireworks fireworks = new Fireworks();

        if (loader == null) {
            RESTFulClient.SERVER = server;
            FireworksFactory.ILLUSTRATION_SERVER = SERVER;
            FireworksFactory.CONSOLE_VERBOSE = false;
            FireworksFactory.OPEN_NODE_ACTION = false;
            FireworksFactory.SHOW_DIAGRAM_BTN = false;
            loader = new FireworksLoader(fireworks);
            loader.load((long) species);
            return fireworks;
        }

        if (holder.equals(placeHolder)) {
            return fireworks; //If the place holder is the same, then we return the very same object
        } else {
            holder = placeHolder;
            //When the place holder is different, we will use the same object but removing it from its previous holder
            HTMLPanel oldHolder = HTMLPanel.wrap(Document.get().getElementById(holder));
            oldHolder.clear();
        }
        fireworks.update(viewer);

        return fireworks;
    }

    public void highlightNode(String stableIdentifier) {
        viewer.highlightNode(stableIdentifier);
    }

    public void highlightNode(Long dbIdentifier) {
        viewer.highlightNode(dbIdentifier);
    }

    public void loadSpecies(Long dbId) {
        loader.load(dbId);
    }

    public void onAnalysisReset(final JsAnalysisResetHandler handler) {
        if (!addHandler(handler)) handlers.add(handler);
    }

    public void onCanvasNotSupported(final JsCanvasNotSupported handler) {
        if (!addHandler(handler)) handlers.add(handler);
    }

    public void onFireworksLoaded(final JsFireworksLoadedHandler handler) {
        if (!addHandler(handler)) handlers.add(handler);
    }

    public void onNodeSelected(final JsNodeSelectedHandler handler) {
        if (!addHandler(handler)) handlers.add(handler);
    }

    public void onNodeHovered(final JsNodeHoveredHandler handler) {
        if (!addHandler(handler)) handlers.add(handler);
    }

    public void resetAnalysis() {
        viewer.resetAnalysis();
    }

    public void resetHighlight() {
        viewer.resetHighlight();
    }

    public void resetSelection() {
        viewer.resetSelection();
    }

    public void resize(int width, int height) {
        Fireworks.width = width;
        Fireworks.height = height;
        viewer.asWidget().setWidth(width + "px");
        viewer.asWidget().setHeight(height + "px");
        viewer.onResize();
    }

    public void selectNode(String stableIdentifier) {
        viewer.selectNode(stableIdentifier);
    }

    public void selectNode(Long dbIdentifier) {
        viewer.selectNode(dbIdentifier);
    }

    public void setAnalysisToken(String token, String resource) {
        viewer.setAnalysisToken(token, resource);
    }

    public void showAll(){
        viewer.showAll();
    }

    @NoExport
    @Override
    public void onFireworksViewerCreated(FireworksViewer viewer) {
        update(viewer);
        for (JsFireworksHandler handler : handlers) {
            addHandler(handler);
        }
    }

    @NoExport
    @Override
    public void onFireworksLoadError(Throwable e) {
        //TODO
    }

    private void update(FireworksViewer fireworks) {
        viewer = fireworks;
        container.clear();
        viewer.asWidget().removeFromParent();
        container.add(viewer);
        viewer.asWidget().getElement().getStyle().setProperty("height", "inherit");

        resize(width, height);
    }

    private boolean addHandler(JsFireworksHandler handler) {
        if (viewer == null) return false;

        if (handler instanceof JsCanvasNotSupported) {
            final JsCanvasNotSupported aux = (JsCanvasNotSupported) handler;
            viewer.addCanvasNotSupportedHandler(new CanvasNotSupportedHandler() {
                @Override
                public void onCanvasNotSupported(CanvasNotSupportedEvent event) {
                    aux.canvasNotSupported();
                }
            });
        } else if (handler instanceof JsFireworksLoadedHandler) {
            final JsFireworksLoadedHandler aux = (JsFireworksLoadedHandler) handler;
            viewer.addFireworksLoaded(new FireworksLoadedHandler() {
                @Override
                public void onFireworksLoaded(FireworksLoadedEvent event) {
                    aux.loaded(event.getSpeciesId());
                }
            });
        } else if (handler instanceof JsNodeSelectedHandler) {
            final JsNodeSelectedHandler aux = (JsNodeSelectedHandler) handler;
            viewer.addNodeSelectedHandler(new NodeSelectedHandler() {
                @Override
                public void onNodeSelected(NodeSelectedEvent event) {
                    aux.selected(FireworksObject.create(event.getNode()));
                }
            });
            //Also adding the node selected reset returning null
            viewer.addNodeSelectedResetHandler(new NodeSelectedResetHandler() {
                @Override
                public void onNodeSelectionReset() {
                    aux.selected(null);
                }
            });
        } else if (handler instanceof JsNodeHoveredHandler) {
            final JsNodeHoveredHandler aux = (JsNodeHoveredHandler) handler;
            viewer.addNodeHoverHandler(new NodeHoverHandler() {
                @Override
                public void onNodeHover(NodeHoverEvent event) {
                    aux.hovered(FireworksObject.create(event.getNode()));
                }
            });
            //Also adding the node hovered reset returning null
            viewer.addNodeHoverResetHandler(new NodeHoverResetHandler() {
                @Override
                public void onNodeHoverReset() {
                    aux.hovered(null);
                }
            });
        } else if (handler instanceof JsAnalysisResetHandler) {
            final JsAnalysisResetHandler aux = (JsAnalysisResetHandler) handler;
            viewer.addAnalysisResetHandler(new AnalysisResetHandler() {
                @Override
                public void onAnalysisReset() {
                    aux.analysisReset();
                }
            });
        } else {
            return false;
        }
        return true;
    }
}
