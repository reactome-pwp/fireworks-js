package org.reactome.web.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import org.reactome.web.analysis.client.AnalysisClient;
import org.reactome.web.analysis.client.filter.ResultFilter;
import org.reactome.web.client.handlers.*;
import org.reactome.web.client.model.FireworksObject;
import org.reactome.web.client.model.JsProperties;
import org.reactome.web.fireworks.client.FireworksFactory;
import org.reactome.web.fireworks.client.FireworksViewer;
import org.reactome.web.pwp.model.client.content.ContentClient;
import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.ExportPackage;
import org.timepedia.exporter.client.Exportable;
import org.timepedia.exporter.client.NoExport;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * @author Guilherme Viteri <gviteri@ebi.ac.uk>
 * @author Kostas sidiropoulos <ksidiro@ebi.ac.uk>
 * @author Antonio Fabregat
 */
@SuppressWarnings({"unused", "WeakerAccess"})
@ExportPackage("Reactome")
@Export("Fireworks")
public class Fireworks implements FireworksLoader.Handler, Exportable {

    protected static final String SERVER = "https://reactome.org";

    private static String holder;

    private static FireworksViewer viewer;
    private static FireworksLoader loader;
    private static HTMLPanel container;

    private static Integer width, height;

    private Set<JsFireworksHandler> handlers;

    private String analysisToken, analysisResource;

    private Fireworks() {
        handlers = new HashSet<>();
    }

    public static Fireworks create(JavaScriptObject input) {
        JsProperties jsProp = new JsProperties(input);
        return create(
                jsProp.get("placeHolder"),
                jsProp.getInt("species", 48887),
                jsProp.get("proxyPrefix", SERVER),
                jsProp.getInt("width", 500),
                jsProp.getInt("height", 400)
        );
    }

    public static Fireworks create(String placeHolder, int species, int width, int height) {
        return create(placeHolder, species, SERVER, width, height);
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
            ContentClient.SERVER = server;
            AnalysisClient.SERVER = server;
            FireworksFactory.SERVER = server;
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

    public void flagItems(String identifier, boolean includeInteractors) {
        viewer.flagItems(identifier, includeInteractors);
    }

    public void highlightNode(String stableIdentifier) {
        viewer.highlightNode(stableIdentifier);
    }

    public void highlightNode(long dbIdentifier) {
        viewer.highlightNode(dbIdentifier);
    }

    public void loadSpecies(long dbId) {
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

    public void onNodesFlaggedReset(final JsNodesFlaggedResetHandler handler){
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
        analysisToken = analysisResource = null;
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

    public void selectNode(long dbIdentifier) {
        viewer.selectNode(dbIdentifier);
    }

    public void setAnalysisToken(String token, String resource) {
        if (viewer != null) {
            ResultFilter resultFilter = new ResultFilter();
            resultFilter.setResource(resource);
            viewer.setAnalysisToken(token, resultFilter);
        } else {
            this.analysisToken = token;
            this.analysisResource = resource;
        }
    }

    public List<String> getAvailableColorProfiles() {
        return viewer.getAvailableColorProfiles();
    }

    public void setColorProfile(String colorProfile) {
        viewer.setColorProfile(colorProfile);
    }

    public void showAll() {
        viewer.showAll();
    }

    @NoExport
    @Override
    public void onFireworksViewerCreated(FireworksViewer viewer) {
        update(viewer);
        for (JsFireworksHandler handler : handlers) {
            addHandler(handler);
        }
        if (analysisToken != null && analysisResource != null) {
            ResultFilter rf = new ResultFilter();
            rf.setResource(analysisResource);
            viewer.setAnalysisToken(analysisToken, rf);
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
            viewer.addCanvasNotSupportedHandler(event -> aux.canvasNotSupported());
        } else if (handler instanceof JsFireworksLoadedHandler) {
            final JsFireworksLoadedHandler aux = (JsFireworksLoadedHandler) handler;
            viewer.addFireworksLoaded(event -> aux.loaded(event.getSpeciesId()));
        } else if (handler instanceof JsNodeSelectedHandler) {
            final JsNodeSelectedHandler aux = (JsNodeSelectedHandler) handler;
            viewer.addNodeSelectedHandler(event -> aux.selected(FireworksObject.create(event.getNode())));
            //Also adding the node selected reset returning null
            viewer.addNodeSelectedResetHandler(() -> aux.selected(null));
        } else if (handler instanceof JsNodeHoveredHandler) {
            final JsNodeHoveredHandler aux = (JsNodeHoveredHandler) handler;
            viewer.addNodeHoverHandler(event -> aux.hovered(FireworksObject.create(event.getNode())));
            //Also adding the node hovered reset returning null
            viewer.addNodeHoverResetHandler(() -> aux.hovered(null));
        } else if (handler instanceof JsAnalysisResetHandler) {
            final JsAnalysisResetHandler aux = (JsAnalysisResetHandler) handler;
            viewer.addAnalysisResetHandler(aux::analysisReset);
        } else if (handler instanceof JsNodesFlaggedResetHandler) {
            final JsNodesFlaggedResetHandler aux = (JsNodesFlaggedResetHandler) handler;
            viewer.addNodeFlaggedResetHandler(aux::flaggedReset);
        } else {
            return false;
        }
        return true;
    }
}
