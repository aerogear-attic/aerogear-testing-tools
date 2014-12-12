package org.jboss.aerogear.unifiedpush.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gcm.server.Message;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Created by asaleh on 12/11/14.
 */
public class HttpMockingServerHandler extends SimpleChannelInboundHandler<Object> {

    private HttpRequest request;

    /** Buffer that stores the response content */
    private final StringBuilder buf = new StringBuilder();

    private final StringBuilder contentBuf = new StringBuilder();

    private static int multicast_id_counter = 1337;
    private static int message_id_counter = 1337;

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    String uri = null;

    private HashMap<String,Object> CreateResult (ArrayList<String> regIds){

        HashMap<String, Object> jsonResponse = new HashMap<String, Object>();

        ArrayList<HashMap<String,String>> out = new  ArrayList<HashMap<String,String>>();

        int success=0;
        int failures =0;

        for(String s : regIds){
           HashMap<String,String> hm = new HashMap<String, String>();

            if (s.toLowerCase().startsWith(Tokens.TOKEN_INVALIDATION_PREFIX)) {
                failures++;
                hm.put("error", "InvalidRegistration");
            } else {
                success++;
                hm.put("message_id","1:"+ new Integer(message_id_counter++).toString());
            }

           out.add(hm);
        }

        jsonResponse.put("success", new Integer(success));
        jsonResponse.put("multicast_id", new Integer(multicast_id_counter++));
        jsonResponse.put("failure", new Integer(failures));
        jsonResponse.put("results", out);

        jsonResponse.put("canonical_ids", new Integer(0));

        return jsonResponse;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof HttpRequest) {
            HttpRequest request = this.request = (HttpRequest) msg;
            uri = request.getUri();
            if (HttpHeaders.is100ContinueExpected(request)) {
                send100Continue(ctx);
            }




        }

        if (msg instanceof HttpContent) {
            HttpContent cnt = (HttpContent) msg;
            ByteBuf content =cnt.content();

            contentBuf.append(content.toString(CharsetUtil.UTF_8));
            if (msg instanceof LastHttpContent) {

                buf.setLength(0);
                if(uri.contains("gcm")) {
                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        GCMessage o = mapper.readValue(contentBuf.toString(), GCMessage.class);
                        SenderStatistics stats = new SenderStatistics();
                        Message.Builder gcmMessage = new Message.Builder();

                        gcmMessage.setData(o.data);
                        if(o.collapse_key!=null) {
                            gcmMessage.collapseKey(o.collapse_key);
                        }
                        if(o.delay_while_idle!=null){
                            gcmMessage.delayWhileIdle(o.delay_while_idle.booleanValue());
                        }
                        if(o.time_to_live!=null){
                            gcmMessage.timeToLive(o.time_to_live.intValue());
                        }

                        stats.gcmMessage = gcmMessage.build();
                        stats.deviceTokens = o.registration_ids;

                        SenderStatisticsEndpoint.setSenderStatistics(stats);
                        contentBuf.delete(0,contentBuf.length());
                        try {
                            buf.append(mapper.writeValueAsString(this.CreateResult(o.registration_ids)));
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }



                }


                writeResponse((HttpObject)msg,ctx);
            }
        }

    }


    private boolean writeResponse(HttpObject currentObj, ChannelHandlerContext ctx) {
        // Decide whether to close the connection or not.
        boolean keepAlive = HttpHeaders.isKeepAlive(request);
        // Build the response object.
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, currentObj.getDecoderResult().isSuccess()? OK : BAD_REQUEST,
                Unpooled.copiedBuffer(buf.toString(), CharsetUtil.UTF_8));

        response.headers().set(CONTENT_TYPE, "application/json");

        if (keepAlive) {
            // Add 'Content-Length' header only for a keep-alive connection.
            response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
            // Add keep alive header as per:
            // - http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
            response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        }

        // Write the response.
        ctx.write(response);

        return keepAlive;
    }

    private void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, CONTINUE);
        ctx.write(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
