package com.equilend.simulator.configurator;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConfigurationServer implements Runnable {

	private static final Logger logger = LogManager.getLogger(ConfigurationServer.class.getName());
	private final Config config;

	private ServerSocket serverSocket;
	private Socket clientSocket;
	private PrintWriter out;
	private BufferedReader in;

	public ConfigurationServer() {
		this.config = Config.getInstance();
	}

	@Override
	public void run() {

		try {
			serverSocket = new ServerSocket(config.getServerPort());

			while (true) {
				try {
					clientSocket = serverSocket.accept();
					out = new PrintWriter(clientSocket.getOutputStream(), true);
					in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
					String inputLine;
					while ((inputLine = in.readLine()) != null) {
						if ("config".equalsIgnoreCase(inputLine)) {
							Properties props = config.getProperties();
							out.println("--------------Simulator Config--------------");
							out.println("[           Party Id]: " + props.getProperty("bot.party_id"));
							out.println("[            User Id]: " + props.getProperty("1source.keycloak.username"));
							out.println("[     Settlement BIC]: " + props.getProperty("bot.settlement_bic"));
							out.println("[    Local Agent BIC]: " + props.getProperty("bot.local_agent_bic"));
							out.println("[   Local Agent Name]: " + props.getProperty("bot.local_agent_name"));
							out.println("[Local Agent Account]: " + props.getProperty("bot.local_agent_acct"));
							out.println("[  DTC Participant #]: " + props.getProperty("bot.dtc_partipant_number"));
						}
					}
				} catch (Exception e) {
					logger.error("Unrecoverable exception with config server", e);
					break;
				} finally {
					out.close();
				}
			}
		} catch (Exception e) {
			logger.error("Could not start config server", e);
		}

	}
}
