package rest.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication(exclude = {
            DataSourceAutoConfiguration.class
            , DataSourceTransactionManagerAutoConfiguration.class
            , HibernateJpaAutoConfiguration.class
        }
        , scanBasePackages = { "rest.api" }
)
public class ErrorHdlApplication {

    private static SpringApplicationBuilder builder;

	public static void main(final String... args) {

	   builder = new SpringApplicationBuilder(ErrorHdlApplication.class);
	   builder.run(args);
	}


	public static void shutdown() {
	    SpringApplication.exit(builder.context(), () -> 0);
	}
}
