package org.jboss.aerogear.unifiedpush.test.sender.gcm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gcm.server.Message;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.CharsetUtil;
import org.jboss.aerogear.unifiedpush.test.SenderStatistics;
import org.jboss.aerogear.unifiedpush.test.sender.SenderStatisticsEndpoint;
import org.jboss.aerogear.unifiedpush.test.Tokens;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Created by asaleh on 12/11/14.
 */
public class HttpMockingServerHandler extends SimpleChannelInboundHandler<Object> {

    private HttpRequest request;

    /**
     * Buffer that stores the response content
     */
    private final StringBuilder buf = new StringBuilder();

    private final StringBuilder requestContentBuffer = new StringBuilder();

    // TODO does this need to start at 1337?
    private static int multicastIdCounter = 1337;
    private static int messageIdCounter = 1337;

    private String uri = null;

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    private HashMap<String, Object> createResponse(ArrayList<String> regIds) {

        HashMap<String, Object> jsonResponse = new HashMap<String, Object>();

        ArrayList<HashMap<String, String>> out = new ArrayList<HashMap<String, String>>();

        int success = 0;
        int failures = 0;

        for (String s : regIds) {
            HashMap<String, String> hm = new HashMap<String, String>();

            if (s.toLowerCase().startsWith(Tokens.TOKEN_INVALIDATION_PREFIX)) {
                failures++;
                hm.put("error", "InvalidRegistration");
            } else {
                success++;
                hm.put("message_id", "1:" + messageIdCounter++);
            }

            out.add(hm);
        }

        jsonResponse.put("success", success);
        jsonResponse.put("multicast_id", multicastIdCounter++);
        jsonResponse.put("failure", failures);
        jsonResponse.put("results", out);

        jsonResponse.put("canonical_ids", 0);

        return jsonResponse;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, Object msg) {
        if (msg instanceof HttpRequest) {
            HttpRequest request = this.request = (HttpRequest) msg;
            uri = request.getUri();
            if (HttpHeaders.is100ContinueExpected(request)) {
                send100Continue(context);
            }
        }

        if (msg instanceof HttpContent) {
            HttpContent httpContent = (HttpContent) msg;
            ByteBuf content = httpContent.content();

            requestContentBuffer.append(content.toString(CharsetUtil.UTF_8));

            if (msg instanceof LastHttpContent) {
                buf.setLength(0);
                if (uri.contains("gcm")) {
                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        GCMMessage message = mapper.readValue(requestContentBuffer.toString(), GCMMessage.class);
                        SenderStatistics stats = new SenderStatistics();
                        Message.Builder gcmMessage = new Message.Builder();

                        gcmMessage.setData(message.data);
                        if (message.collapseKey != null) {
                            gcmMessage.collapseKey(message.collapseKey);
                        }
                        if (message.delayWhileIdle != null) {
                            gcmMessage.delayWhileIdle(message.delayWhileIdle);
                        }
                        if (message.timeToLive != null) {
                            gcmMessage.timeToLive(message.timeToLive);
                        }

                        stats.gcmMessage = gcmMessage.build();
                        stats.deviceTokens = message.registrationIds;

                        SenderStatisticsEndpoint.setSenderStatistics(stats);
                        requestContentBuffer.delete(0, requestContentBuffer.length());
                        try {
                            buf.append(mapper.writeValueAsString(this.createResponse(message.registrationIds)));
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }


                writeResponse((HttpObject) msg, context);
            }
        }

    }


    private boolean writeResponse(HttpObject currentObj, ChannelHandlerContext ctx) {
        // Decide whether to close the connection or not.
        boolean keepAlive = HttpHeaders.isKeepAlive(request);

        // Build the response object.
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, currentObj.getDecoderResult().isSuccess() ? OK : BAD_REQUEST,
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
