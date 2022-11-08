package rest.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class ReliantDemoApplication {

    private static SpringApplicationBuilder builder;

	public static void main(final String... args) {

	   builder = new SpringApplicationBuilder(ReliantDemoApplication.class)
		    .properties("server.port=9090");

	   builder.run(args);
	}


	public static ApplicationContext getContext() {
	    return builder.context();
	}

	public static void shutdown() {
	    SpringApplication.exit(builder.context(), () -> 0);
	}
}
