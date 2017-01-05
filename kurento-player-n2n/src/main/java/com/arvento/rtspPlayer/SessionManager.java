package com.arvento.rtspPlayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.google.gson.JsonObject;

public class SessionManager {

	private static SessionManager instance = null;
	private final Logger log = LoggerFactory.getLogger(SessionManager.class);

	private final ConcurrentHashMap<String, UserSession> users;
	private final ConcurrentHashMap<String, ArrayList<UserSession>> sessions;

	public static SessionManager getInstance() {
		if (instance == null)
			instance = new SessionManager();
		return instance;
	}

	private SessionManager() {
		users = new ConcurrentHashMap<String, UserSession>();
		sessions = new ConcurrentHashMap<String, ArrayList<UserSession>>();
	}

	public void addUser(String id, UserSession user) {
		users.put(id, user);
	}

	public UserSession removeUser(String sessionId) {
		return users.remove(sessionId);
	}

	public UserSession getUser(String sessionId) {
		return users.get(sessionId);
	}

	public void addSession(String videoUrl, UserSession session) {
		if (!sessions.containsKey(videoUrl)) {
			sessions.put(videoUrl, new ArrayList<UserSession>());
		}
		sessions.get(videoUrl).add(session);
	}

	public void sendPlayEnd(String videoUrl) {
		if (sessions.containsKey(videoUrl)) {
			for (UserSession session : sessions.get(videoUrl)) {
				if (users.containsKey(session.getWsSession().getId())) {
					JsonObject response = new JsonObject();
					response.addProperty("id", "playEnd");
					sendMessage(session.getWsSession(), response.toString());
				}
			}
		}
		log.debug("sendPlayEnd " + videoUrl);
	}

	public void sendError(String videoUrl, String message) {
		if (sessions.containsKey(videoUrl)) {
			for (UserSession session : sessions.get(videoUrl)) {
				if (users.containsKey(session.getWsSession().getId())) {
					JsonObject response = new JsonObject();
					response.addProperty("id", "error");
					response.addProperty("message", message);
					sendMessage(session.getWsSession(), response.toString());
				}
			}
		}
		log.debug("sendPlayEnd " + videoUrl + " " + message);
	}

	public synchronized void sendMessage(WebSocketSession session, String message) {
		try {
			session.sendMessage(new TextMessage(message));
		} catch (IOException e) {
			log.error("Exception sending message", e);
		}
	}

	public void sendError(WebSocketSession session, String message) {
		if (users.containsKey(session.getId())) {
			JsonObject response = new JsonObject();
			response.addProperty("id", "error");
			response.addProperty("message", message);
			sendMessage(session, response.toString());
		}
	}
}
