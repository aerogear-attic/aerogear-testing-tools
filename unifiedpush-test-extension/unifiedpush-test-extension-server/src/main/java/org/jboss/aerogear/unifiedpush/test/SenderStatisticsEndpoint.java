package org.jboss.aerogear.unifiedpush.test;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by asaleh on 12/11/14.
 */
@Stateless
@TransactionAttribute
@Path("/senderStats")
// FIXME move to src/main
public class SenderStatisticsEndpoint {

    private static SenderStatistics senderStatistics = new SenderStatistics();

    public static void setSenderStatistics(SenderStatistics statistics){
        senderStatistics = statistics;
    }

    public static void clearSenderStatistics(){
        senderStatistics = new SenderStatistics();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listAllStatistics() {
        return Response.ok(senderStatistics).build();
    }
    @DELETE
    public Response resetStatistics() {
        clearSenderStatistics();
        return Response.noContent().build();
    }
}
