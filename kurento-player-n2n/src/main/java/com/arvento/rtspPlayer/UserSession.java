package com.arvento.rtspPlayer;

import org.kurento.client.WebRtcEndpoint;

public class UserSession {
	private WebRtcEndpoint webRtcEndpoint;

	public UserSession() {
	}

	public WebRtcEndpoint getWebRtcEndpoint() {
		return webRtcEndpoint;
	}

	public void setWebRtcEndpoint(WebRtcEndpoint webRtcEndpoint) {
		this.webRtcEndpoint = webRtcEndpoint;
	}

}
