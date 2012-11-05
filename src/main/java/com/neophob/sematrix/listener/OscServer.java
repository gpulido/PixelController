package com.neophob.sematrix.listener;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import oscP5.OscEventListener;
import oscP5.OscMessage;
import oscP5.OscP5;
import oscP5.OscStatus;
import processing.core.PApplet;

import com.neophob.sematrix.properties.ValidCommands;

public class OscServer implements OscEventListener {

	/** The log. */
	private static final Logger LOG = Logger.getLogger(OscServer.class.getName());

	private int listeningPort;

	private OscP5 oscP5;

	/**
	 * 
	 * @param listeningPort
	 */
	public OscServer(PApplet papplet, int listeningPort) {
		this.listeningPort = listeningPort;
		LOG.log(Level.INFO,	"Start OSC Server at port {0}", new Object[] { listeningPort });
		this.oscP5 = new OscP5(papplet, this.listeningPort);
		this.oscP5.addListener(this);
		
		//log only error and warnings
		OscP5.setLogStatus(netP5.Logger.ALL, netP5.Logger.OFF);
		OscP5.setLogStatus(netP5.Logger.ERROR, netP5.Logger.ON);
		OscP5.setLogStatus(netP5.Logger.WARNING, netP5.Logger.ON);
	}

	/**
	 * 
	 * @param theOscMessage
	 */
	public void oscEvent(OscMessage theOscMessage) {
		//sanity check
		if (StringUtils.isBlank(theOscMessage.addrPattern())) {
			LOG.log(Level.INFO,	"Ignore empty OSC message...");
			return;
		}
		
		LOG.log(Level.INFO,	"Received an osc message. with address pattern {0} typetag {1}.", 
				new Object[] { theOscMessage.addrPattern(), theOscMessage.typetag() });		

		//address pattern -> internal message mapping
		String pattern = theOscMessage.addrPattern().trim().substring(1).toUpperCase();
		try {
			ValidCommands command = ValidCommands.valueOf(pattern);
			String[] msg = new String[1+command.getNrOfParams()];
			msg[0] = pattern;
			for (int i=0; i<command.getNrOfParams(); i++) {

				//parse osc message
				if (theOscMessage.checkTypetag("s")) {
					msg[i+1] = theOscMessage.get(i).stringValue();	
				} else
					if (theOscMessage.checkTypetag("i")) {
						msg[i+1] = ""+theOscMessage.get(i).intValue();	
					} else
						if (theOscMessage.checkTypetag("f")) {
							msg[i+1] = ""+theOscMessage.get(i).floatValue();	
						}				
			}
			MessageProcessor.processMsg(msg, true);
		} catch (Exception e) {
			LOG.log(Level.WARNING, "Failed to parse OSC Message", e);
			return;
		}		
	}

	@Override
	public void oscStatus(OscStatus arg0) {
		// TODO Auto-generated method stub
		
	}
	
	
}