package com.arvento.rtspPlayer;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.kurento.client.KurentoClient;

public class RTSPSourceManager {

	private static RTSPSourceManager instance = null;
	private ConcurrentHashMap<RTSPSource, ArrayList<UserSession>> viewers;

	public static RTSPSourceManager getInstance() {
		if (instance == null)
			instance = new RTSPSourceManager();
		return instance;
	}

	private RTSPSourceManager() {
		viewers = new ConcurrentHashMap<RTSPSource, ArrayList<UserSession>>();
	}

	public RTSPSource getRTSPSource(KurentoClient kurento, String videourl) {
		for (RTSPSource source : viewers.keySet()) {
			if (source.getUrl().equals(videourl))
				return source;
		}
		return new RTSPSource(kurento, videourl);
	}

	public void addUserToSource(RTSPSource source, UserSession user) {
		if (!viewers.containsKey(source)) {
			viewers.put(source, new ArrayList<UserSession>());
		}
		viewers.get(source).add(user);
	}

	public void removeUserFromSource(UserSession user) {
		RTSPSource source = getSourceFromUser(user);
		if (source != null) {
			viewers.get(source).remove(user);
			if (viewers.get(source).size() == 0) {
				viewers.remove(source);
				source.stop();
			}
		}

	}

	private RTSPSource getSourceFromUser(UserSession value) {
		for (RTSPSource o : viewers.keySet()) {
			if (viewers.get(o).contains(value)) {
				return o;
			}
		}
		return null;
	}

	public void sendPlayEnd(String url) {
		SessionManager.getInstance().sendPlayEnd(url);
	}
}
