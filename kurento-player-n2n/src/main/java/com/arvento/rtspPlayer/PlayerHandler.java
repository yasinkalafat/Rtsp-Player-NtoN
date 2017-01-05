package com.arvento.rtspPlayer;

import java.io.IOException;

import org.kurento.client.EventListener;
import org.kurento.client.IceCandidate;
import org.kurento.client.IceCandidateFoundEvent;
import org.kurento.client.KurentoClient;
import org.kurento.client.MediaPipeline;
import org.kurento.client.MediaState;
import org.kurento.client.MediaStateChangedEvent;
import org.kurento.client.PlayerEndpoint;
import org.kurento.client.VideoInfo;
import org.kurento.client.WebRtcEndpoint;
import org.kurento.jsonrpc.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class PlayerHandler extends TextWebSocketHandler {

	@Autowired
	private KurentoClient kurento;

	private final Logger log = LoggerFactory.getLogger(PlayerHandler.class);
	private final Gson gson = new GsonBuilder().create();
	private final RTSPSourceManager manager = RTSPSourceManager.getInstance();
	private final SessionManager sessionManager = SessionManager.getInstance();

	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		JsonObject jsonMessage = gson.fromJson(message.getPayload(), JsonObject.class);
		String sessionId = session.getId();
		log.debug("Incoming message {} from sessionId", jsonMessage, sessionId);

		try {
			switch (jsonMessage.get("id").getAsString()) {
			case "start":
				start(session, jsonMessage);
				break;
			case "stop":
				stop(sessionId);
				break;
			case "pause":
				break;
			case "resume":
				break;
			case "doSeek":
				break;
			case "getPosition":
				break;
			case "onIceCandidate":
				onIceCandidate(sessionId, jsonMessage);
				break;
			default:
				sessionManager.sendError(session, "Invalid message with id " + jsonMessage.get("id").getAsString());
				break;
			}
		} catch (Throwable t) {
			log.error("Exception handling message {} in sessionId {}", jsonMessage, sessionId, t);
			sessionManager.sendError(session, t.getMessage());
		}
	}

	private void start(final WebSocketSession session, JsonObject jsonMessage) {
		// 1. Media pipeline
		final UserSession user = new UserSession(session);
		String videourl = jsonMessage.get("videourl").getAsString();
		RTSPSource source = manager.getRTSPSource(kurento, videourl);
		manager.addUserToSource(source, user);

		MediaPipeline pipeline = source.getPipeline();
		WebRtcEndpoint webRtcEndpoint = new WebRtcEndpoint.Builder(pipeline).build();
		final PlayerEndpoint playerEndpoint = source.getPlayerEndpoint();

		user.setWebRtcEndpoint(webRtcEndpoint);
		sessionManager.addUser(session.getId(), user);
		sessionManager.addSession(videourl, user);

		playerEndpoint.connect(webRtcEndpoint);

		// 2. WebRtcEndpoint
		// ICE candidates
		webRtcEndpoint.addIceCandidateFoundListener(new EventListener<IceCandidateFoundEvent>() {

			@Override
			public void onEvent(IceCandidateFoundEvent event) {
				JsonObject response = new JsonObject();
				response.addProperty("id", "iceCandidate");
				response.add("candidate", JsonUtils.toJsonObject(event.getCandidate()));
				try {
					synchronized (session) {
						session.sendMessage(new TextMessage(response.toString()));
					}
				} catch (IOException e) {
					log.debug(e.getMessage());
				}
			}
		});

		String sdpOffer = jsonMessage.get("sdpOffer").getAsString();
		String sdpAnswer = webRtcEndpoint.processOffer(sdpOffer);

		JsonObject response = new JsonObject();
		response.addProperty("id", "startResponse");
		response.addProperty("sdpAnswer", sdpAnswer);
		sessionManager.sendMessage(session, response.toString());

		webRtcEndpoint.addMediaStateChangedListener(new EventListener<MediaStateChangedEvent>() {
			@Override
			public void onEvent(MediaStateChangedEvent event) {

				if (event.getNewState() == MediaState.CONNECTED) {
					VideoInfo videoInfo = playerEndpoint.getVideoInfo();

					JsonObject response = new JsonObject();
					response.addProperty("id", "videoInfo");
					response.addProperty("isSeekable", videoInfo.getIsSeekable());
					response.addProperty("initSeekable", videoInfo.getSeekableInit());
					response.addProperty("endSeekable", videoInfo.getSeekableEnd());
					response.addProperty("videoDuration", videoInfo.getDuration());
					sessionManager.sendMessage(session, response.toString());
				}
			}
		});

		webRtcEndpoint.gatherCandidates();
	}

	private void stop(String sessionId) {
		UserSession user = sessionManager.removeUser(sessionId);

		if (user != null) {
			manager.removeUserFromSource(user);
		}
	}

	private void onIceCandidate(String sessionId, JsonObject jsonMessage) {
		UserSession user = sessionManager.getUser(sessionId);

		if (user != null) {
			JsonObject jsonCandidate = jsonMessage.get("candidate").getAsJsonObject();
			IceCandidate candidate = new IceCandidate(jsonCandidate.get("candidate").getAsString(),
					jsonCandidate.get("sdpMid").getAsString(), jsonCandidate.get("sdpMLineIndex").getAsInt());
			user.getWebRtcEndpoint().addIceCandidate(candidate);
		}
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		stop(session.getId());
	}
}
