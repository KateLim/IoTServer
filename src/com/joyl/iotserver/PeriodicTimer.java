package com.joyl.iotserver;

import java.util.Timer;
import java.util.TimerTask;

public class PeriodicTimer extends Thread {
	Timer timer;

	public PeriodicTimer(long controllerSessionTimeout,
			long sanodeSessionTimeout, long logTimeout,
			ControllerManager controllerManager, SANodeManager sanodeManager) {
		super();

		timer = new Timer();
		timer.schedule(new PeriodicTask(controllerSessionTimeout,
				controllerSessionTimeout, logTimeout, controllerManager,
				sanodeManager), 0, // initial delay
				1 * 1000); // subsequent rate
	}

	class PeriodicTask extends TimerTask {
		long controllerSessionTimeout;
		long sanodeSessionTimeout;
		long logTimeout;
		ControllerManager controllerManager;
		SANodeManager sanodeManager;

		public PeriodicTask(long controllerSessionTimeout,
				long sanodeSessionTimeout, long logTimeout,
				ControllerManager controllerManager, SANodeManager sanodeManager) {
			this.controllerSessionTimeout = controllerSessionTimeout;
			this.sanodeSessionTimeout = sanodeSessionTimeout;
			this.logTimeout = logTimeout;
			this.controllerManager = controllerManager;
			this.sanodeManager = sanodeManager;
		}

		public void run() {
			Logger.removeOldLog(logTimeout);
			controllerManager.removeOldSession(controllerSessionTimeout);
			sanodeManager.removeOldSession(sanodeSessionTimeout);
//			System.out.println("PeriodicTimer");
		}
	}

}
