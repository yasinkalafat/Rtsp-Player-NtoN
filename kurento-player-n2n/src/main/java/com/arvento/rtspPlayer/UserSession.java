package com.arvento.rtspPlayer;

import org.kurento.client.WebRtcEndpoint;
import org.springframework.web.socket.WebSocketSession;

public class UserSession {
	private WebRtcEndpoint webRtcEndpoint;
	private WebSocketSession wsSession;

	public UserSession(WebSocketSession session) {
		this.wsSession = session;
	}

	public WebRtcEndpoint getWebRtcEndpoint() {
		return webRtcEndpoint;
	}

	public void setWebRtcEndpoint(WebRtcEndpoint webRtcEndpoint) {
		this.webRtcEndpoint = webRtcEndpoint;
	}

	public WebSocketSession getWsSession() {
		return wsSession;
	}

	public void setWsSession(WebSocketSession wsSession) {
		this.wsSession = wsSession;
	}

}
