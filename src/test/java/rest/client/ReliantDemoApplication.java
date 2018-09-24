package rest.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class ReliantDemoApplication {

    private static SpringApplicationBuilder builder;

	public static void main(final String... args) {

	   builder = new SpringApplicationBuilder(ReliantDemoApplication.class)
		    .properties("server.port=9090");


	   builder.run(args);
	}


	public static void shutdown() {
	    SpringApplication.exit(builder.context(), () -> 0);
	}
}
