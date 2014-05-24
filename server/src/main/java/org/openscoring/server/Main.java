/*
 * Copyright (c) 2014 Villu Ruusmann
 *
 * This file is part of Openscoring
 *
 * Openscoring is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Openscoring is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Openscoring.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.openscoring.server;

import java.net.*;

import org.openscoring.service.*;

import org.eclipse.jetty.server.*;
import org.eclipse.jetty.servlet.*;

import com.beust.jcommander.*;

import org.glassfish.hk2.utilities.*;
import org.glassfish.hk2.utilities.binding.*;
import org.glassfish.jersey.server.*;
import org.glassfish.jersey.servlet.*;

public class Main {

	@Parameter (
		names = {"--host"},
		description = "Host"
	)
	private String host = "localhost";

	@Parameter (
		names = {"--port"},
		description = "Port"
	)
	private int port = 8080;

	@Parameter (
		names = {"--context-path"},
		description = "Context path"
	)
	private String contextPath = "/openscoring";

	@Parameter (
		names = {"--help"},
		description = "Show the list of configuration options and exit",
		help = true
	)
	private boolean help = false;


	static
	public void main(String... args) throws Exception {
		Main main = new Main();

		JCommander commander = new JCommander(main);
		commander.setProgramName(Main.class.getName());

		try {
			commander.parse(args);
		} catch(ParameterException pe){
			commander.usage();

			System.exit(-1);
		}

		if(main.help){
			commander.usage();

			System.exit(0);
		}

		main.run();
	}

	private void run() throws Exception {
		InetSocketAddress address = new InetSocketAddress(this.host, this.port);

		Server server = new Server(address);

		ServletContextHandler contextHandler = new ServletContextHandler();
		contextHandler.setContextPath(this.contextPath);

		Binder binder = new AbstractBinder(){

			@Override
			protected void configure(){
				bind(new ModelRegistry()).to(ModelRegistry.class);
			}
		};

		ResourceConfig config = new ResourceConfig(ModelService.class);
		config.register(binder);

		ServletContainer servletContainer = new ServletContainer(config);

		ServletHolder servletHolder = new ServletHolder(servletContainer);
		contextHandler.addServlet(servletHolder, "/*");

		contextHandler.addServlet(DefaultServlet.class, "/");

		server.setHandler(contextHandler);

		server.start();
		server.join();
	}
}