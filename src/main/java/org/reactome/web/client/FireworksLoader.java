package org.reactome.web.client;

import com.google.gwt.http.client.*;
import org.reactome.web.fireworks.client.FireworksFactory;
import org.reactome.web.fireworks.client.FireworksViewer;
import org.reactome.web.fireworks.util.Console;
import org.reactome.web.pwp.model.client.classes.DatabaseObject;
import org.reactome.web.pwp.model.client.classes.Species;
import org.reactome.web.pwp.model.client.common.ContentClientHandler;
import org.reactome.web.pwp.model.client.content.ContentClient;
import org.reactome.web.pwp.model.client.content.ContentClientError;
import org.reactome.web.pwp.model.client.util.LruCache;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class FireworksLoader implements ContentClientHandler.ObjectLoaded<DatabaseObject>,  RequestCallback {

    public interface Handler {
        void onFireworksViewerCreated(FireworksViewer fireworks);
        void onFireworksLoadError(Throwable e);
    }

    private final Handler handler;

    private LruCache<String, FireworksViewer> cache = new LruCache<>(5);
    private String target;

    public FireworksLoader(Handler handler) {
        this.handler = handler;
    }

    public void load(Long species) {
        target = "" + species;
        if(cache.keySet().contains(target)){
            handler.onFireworksViewerCreated(cache.get(target));
        } else {
            ContentClient.query(species, this);
        }
    }

    public String getTarget() {
        return target;
    }


    @Override
    public void onObjectLoaded(DatabaseObject databaseObject) {
        if (databaseObject instanceof Species) {
            Species species = (Species) databaseObject;
            loadFireworksJson(species);
        } else {
            target = null;
            Console.error("The provided identifier is not a valid species in Reactome");
        }
    }

    @Override
    public void onContentClientException(ContentClientHandler.Type type, String message) {
        Console.error(message);
    }

    @Override
    public void onContentClientError(ContentClientError error) {
        Console.error(error.getMessage());
    }

    private void loadFireworksJson(Species species){
        String name = species.getDisplayName().trim().replaceAll(" ", "_");
        String url = Fireworks.SERVER + "/download/current/fireworks/" + name + ".json?v=" + System.currentTimeMillis() ;
        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, url);
        requestBuilder.setHeader("Accept", "application/json");
        try {
            requestBuilder.sendRequest(null, this);
        } catch (RequestException ex) {
            handler.onFireworksLoadError(ex);
        }
    }

    @Override
    public void onResponseReceived(Request request, Response response) {
        switch (response.getStatusCode()){
            case Response.SC_OK:
                FireworksViewer fireworks = FireworksFactory.createFireworksViewer(response.getText());
                Console.info(target);
                cache.put(target, fireworks);
                handler.onFireworksViewerCreated(fireworks);
                return;
            default:
                handler.onFireworksLoadError(new Exception(response.getStatusText()));
        }
    }

    @Override
    public void onError(Request request, Throwable throwable) {
        handler.onFireworksLoadError(throwable);
    }

}
