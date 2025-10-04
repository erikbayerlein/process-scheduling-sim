package com.api.process_scheduling.controller;

import com.api.process_scheduling.dto.ScheduleResponse;
import com.api.process_scheduling.entities.Process;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProcessSchedulerControllerImplTest {

    @LocalServerPort
    private int port;

    private WebSocketStompClient stompClient;
    private String wsUrl;

    @BeforeEach
    void setUp() {
        wsUrl = "ws://localhost:" + port + "/ws";

        SockJsClient sockJsClient = new SockJsClient(
                Collections.singletonList(new WebSocketTransport(new StandardWebSocketClient()))
        );

        stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }

    @Test
    void testScheduleProcesses() throws Exception {
        CompletableFuture<String> resultFuture = new CompletableFuture<>();

        StompSessionHandler sessionHandler = new MyStompSessionHandler();
        StompSession session = stompClient.connectAsync(wsUrl, sessionHandler).get(5, TimeUnit.SECONDS);

        session.subscribe("/process-scheduler/results", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ScheduleResponse.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                ScheduleResponse response = (ScheduleResponse) payload;
                resultFuture.complete(response.getMessage());
            }
        });

        Process testProcess = new Process();

        session.send("/app/schedule", testProcess);

        String result = resultFuture.get(10, TimeUnit.SECONDS);
        assertNotNull(result);

        session.disconnect();
    }

    private static class MyStompSessionHandler extends StompSessionHandlerAdapter {
        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            System.out.println("Connected to WebSocket");
        }

        @Override
        public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
            exception.printStackTrace();
        }
    }
}