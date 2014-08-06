package org.jboss.aerogear.test.api;

import com.jayway.restassured.path.json.JsonPath;
import org.jboss.aerogear.test.Session;
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.List;

public interface UPSWorker<
        ENTITY,
        ENTITY_ID,
        BLUEPRINT extends ENTITY,
        EDITOR extends ENTITY,
        PARENT,
        CONTEXT extends UPSContext<ENTITY, ENTITY_ID, BLUEPRINT, EDITOR, PARENT, WORKER, CONTEXT>,
        WORKER extends UPSWorker<ENTITY, ENTITY_ID, BLUEPRINT, EDITOR, PARENT, CONTEXT, WORKER>> {

    CONTEXT createContext(Session session, PARENT parent);

    JSONObject marshall(ENTITY entity);

    EDITOR demarshall(CONTEXT context, JsonPath json);

    List<EDITOR> create(CONTEXT context, Collection<? extends BLUEPRINT> blueprints);

    List<EDITOR> readAll(CONTEXT context);

    EDITOR read(CONTEXT context, ENTITY_ID id);

    void update(CONTEXT context, Collection<? extends ENTITY> entities);

    void delete(CONTEXT context, Collection<? extends ENTITY> entities);

    void deleteById(CONTEXT context, ENTITY_ID id);
}