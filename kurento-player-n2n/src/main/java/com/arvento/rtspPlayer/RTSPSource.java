package com.arvento.rtspPlayer;

import org.kurento.client.EndOfStreamEvent;
import org.kurento.client.ErrorEvent;
import org.kurento.client.EventListener;
import org.kurento.client.KurentoClient;
import org.kurento.client.MediaPipeline;
import org.kurento.client.PlayerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RTSPSource {

	private final Logger log = LoggerFactory.getLogger(RTSPSource.class);
	private MediaPipeline pipeline;
	private PlayerEndpoint playerEndpoint;
	private String url;

	public RTSPSource(KurentoClient kurento, final String url) {
		this.setUrl(url);
		pipeline = kurento.createMediaPipeline();
		playerEndpoint = new PlayerEndpoint.Builder(pipeline, url).build();

		playerEndpoint.addErrorListener(new EventListener<ErrorEvent>() {
			@Override
			public void onEvent(ErrorEvent event) {
				log.info("ErrorEvent: {}", event.getDescription());
				RTSPSourceManager.getInstance().sendPlayEnd(url);
			}
		});

		playerEndpoint.addEndOfStreamListener(new EventListener<EndOfStreamEvent>() {
			@Override
			public void onEvent(EndOfStreamEvent event) {
				log.info("EndOfStreamEvent: {}", event.getTimestamp());
				RTSPSourceManager.getInstance().sendPlayEnd(url);
			}
		});

		playerEndpoint.play();
	}

	public MediaPipeline getPipeline() {
		return pipeline;
	}

	public void setPipeline(MediaPipeline pipeline) {
		this.pipeline = pipeline;
	}

	public PlayerEndpoint getPlayerEndpoint() {
		return playerEndpoint;
	}

	public void setPlayerEndpoint(PlayerEndpoint playerEndpoint) {
		this.playerEndpoint = playerEndpoint;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void stop() {
		this.playerEndpoint.stop();
		this.pipeline.release();
		log.debug("stopped RTSP Source" + getUrl());
	}

}
