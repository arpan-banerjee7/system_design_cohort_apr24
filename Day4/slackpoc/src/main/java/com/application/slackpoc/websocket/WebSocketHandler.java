package com.application.slackpoc.websocket;

import com.application.slackpoc.model.ChatMessage;
import com.application.slackpoc.redis.RedisPublisher;
import com.application.slackpoc.redis.RedisSubscriber;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.ArrayList;
import java.util.List;


@Component
public class WebSocketHandler extends TextWebSocketHandler {

    private final RedisPublisher redisPublisher;
    private final RedisSubscriber redisSubscriber;
    private final ObjectMapper objectMapper;

    public WebSocketHandler(RedisPublisher redisPublisher, RedisSubscriber redisSubscriber) {
        this.redisPublisher = redisPublisher;
        this.redisSubscriber = redisSubscriber;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = getUserIdFromSession(session);
        String channel = getChannelFromSession(session);
        RedisSubscriber.getUserSessions().put(userId, session);
        RedisSubscriber.getChannelToUsersMap().computeIfAbsent(channel, k->new ArrayList<>()).add(userId);
        subscribeUserToChannel("common", session);
    }


    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        ObjectMapper objectMapper = new ObjectMapper();

        // Deserialize JSON payload to Message object
        ChatMessage messageObject = objectMapper.readValue(payload, ChatMessage.class);

        if (messageObject.getUserId() != null && messageObject.getMessage() != null) {
            String formattedMessage = messageObject.getUserId() + ": " + messageObject.getMessage();
            System.out.println("Publishing msg: " + formattedMessage);
            redisPublisher.publish(messageObject);
        } else {
            System.err.println("Received message with missing fields: " + payload);
        }
    }


    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        session.close(CloseStatus.SERVER_ERROR);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        unsubscribeUserFromAllChannels(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    public void subscribeUserToChannel(String channel, WebSocketSession session) {
        if (!RedisSubscriber.getSubscribedChannels().contains(channel)) {
            RedisSubscriber.getSubscribedChannels().add(channel);
            redisSubscriber.subscribe(channel);
        }
    }

    private void unsubscribeUserFromAllChannels(WebSocketSession session) {
        String userId = getUserIdFromSession(session);
        RedisSubscriber.getUserSessions().remove(userId);
        String channel = getChannelFromSession(session);
        removeUserFromChannel(userId, channel);
    }

    private void removeUserFromChannel(String userId, String channel) {
        List<String> users = RedisSubscriber.getChannelToUsersMap().get(channel);

        if (users != null) {
            users.remove(userId);
            if (users.isEmpty()) {
                RedisSubscriber.getChannelToUsersMap().remove(channel);
            }
        }
    }
    private static String getUserIdFromSession(WebSocketSession session) {
        String query = session.getUri().getQuery();
        String userId = null;

        if (query != null) {
            // Split the query parameter string by "&" to get individual key-value pairs
            String[] queryParams = query.split("&");

            // Iterate over each key-value pair to find the one with key "userId"
            for (String param : queryParams) {
                String[] keyValue = param.split("=");

                // Check if the key is "userId"
                if (keyValue.length == 2 && keyValue[0].equals("userId")) {
                    // Extract the value associated with "userId" key
                    userId = keyValue[1];
                    break;
                }
            }
        }
        return userId;
    }

    private String getChannelFromSession(WebSocketSession session) {
        String query = session.getUri().getQuery();
        String channel = null;

        if (query != null) {
            String[] queryParams = query.split("&");
            for (String param : queryParams) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2 && keyValue[0].equals("channel")) {
                    channel = keyValue[1];
                    break;
                }
            }
        }
        return channel;
    }

}
