package rest.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
public class DemoApplication {

    private static SpringApplicationBuilder builder;

	public static void main(final String... args) {

	   builder = new SpringApplicationBuilder(DemoApplication.class)
		    .properties("server.port=9090");


	   builder.run(args);
	}


	public static void shutdown() {
	    SpringApplication.exit(builder.context(), () -> 0);
	}
}
