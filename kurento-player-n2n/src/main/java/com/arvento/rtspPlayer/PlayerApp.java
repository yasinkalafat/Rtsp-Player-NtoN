package com.arvento.rtspPlayer;

import org.kurento.client.KurentoClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@EnableWebSocket
@SpringBootApplication
public class PlayerApp implements WebSocketConfigurer {

	private static final String KMS_WS_URI_PROP = "kms.url";
	private static final String KMS_WS_URI_DEFAULT = "ws://192.168.1.109:8888/kurento";

	@Bean
	public PlayerHandler handler() {
		return new PlayerHandler();
	}

	@Bean
	public KurentoClient kurentoClient() {
		return KurentoClient.create(System.getProperty(KMS_WS_URI_PROP, KMS_WS_URI_DEFAULT));
	}

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(handler(), "/player");
	}

	public static void main(String[] args) throws Exception {
		new SpringApplication(PlayerApp.class).run(args);
	}
}