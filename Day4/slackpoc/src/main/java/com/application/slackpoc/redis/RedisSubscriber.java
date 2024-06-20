package com.application.slackpoc.redis;

import com.application.slackpoc.model.ChatMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RedisSubscriber implements MessageListener {

    private final RedisTemplate<String, String> redisTemplate;
    private static final Set<String> subscribedChannels = new HashSet<>();
    private static final ConcurrentHashMap<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, List<String>> channelToUsersMap = new ConcurrentHashMap<>();

    public RedisSubscriber(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String jsonMessage = new String(message.getBody());
        System.out.println("Redis Subscriber::: Received message: " + jsonMessage);
        // Parse JSON message
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            ChatMessage receivedMessage = objectMapper.readValue(jsonMessage, ChatMessage.class);
            String channel = String.valueOf(receivedMessage.getChannel());

            // Determine WebSocket channel based on Redis channel
            // Send message to appropriate WebSocket channel
            sendMessageToWebSocketChannel(jsonMessage, channel);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Broadcasts messages to all users irrespective of their channels
        // sendMessageToAllSubscribers(jsonMessage);
    }


    public void subscribe(String channel) {
        redisTemplate.getConnectionFactory().getConnection().subscribe(this, channel.getBytes(StandardCharsets.UTF_8));
    }


    public void sendMessageToAllSubscribers(String message) {
        System.out.println("Broadcasting message: " + message);

        // Extract userId from the message
        ObjectMapper objectMapper = new ObjectMapper();
        String userId = null;
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            userId = jsonNode.get("userId").asText();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        final String senderUserId = userId;

        new Thread(() -> {
            for (Map.Entry<String, WebSocketSession> entry : userSessions.entrySet()) {
                String sessionId = entry.getKey();
                WebSocketSession session = entry.getValue();
                try {
                    // Skip sending the message to the sender
                    if (session.isOpen() && !sessionId.equals(senderUserId)) {
                        session.sendMessage(new TextMessage(message));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    public void sendMessageToWebSocketChannel(String message, String channel) {
        System.out.println("Broadcasting message: " + message);

        // Extract userId from the message
        ObjectMapper objectMapper = new ObjectMapper();
        String userId = null;
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            userId = jsonNode.get("userId").asText();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        final String senderUserId = userId;

        List<String> usersInChannel = channelToUsersMap.get(channel);
        if (usersInChannel != null) {
            new Thread(() -> {
                for (String userIdInChannel : usersInChannel) {
                    WebSocketSession session = userSessions.get(userIdInChannel);
                    try {
                        // Skip sending the message to the sender
                        if (session != null && session.isOpen() && !userIdInChannel.equals(senderUserId)) {
                            session.sendMessage(new TextMessage(message));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }


    public static Map<String, WebSocketSession> getUserSessions() {
        return userSessions;
    }

    public static Set<String> getSubscribedChannels() {
        return subscribedChannels;
    }

    public static Map<String, List<String>> getChannelToUsersMap() {
        return channelToUsersMap;
    }
}
