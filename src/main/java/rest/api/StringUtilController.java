package rest.api;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("su")
public class StringUtilController {

    private final AtomicInteger counter = new AtomicInteger();

    @GetMapping("capitalize/{name}")
    public String capitalize(@PathVariable("name") final String name) throws InterruptedException {
        try {
            int concurrent = this.counter.incrementAndGet();

            if ("paris".equals(name)) {
                throw new RuntimeException("Te falta el acento papito");
            }

            if (concurrent == 1) {
                System.out.println("============================================================================");
            }

            if (concurrent > 2) {
                System.out.println("CONCURRENT CALLS: " + concurrent);
            }
            Thread.sleep(300L);
            return StringUtils.capitalize(name);
        }
        finally {
            this.counter.decrementAndGet();
        }
    }

    @GetMapping("quote/{name}")
    public String quote(@PathVariable("name") final String name) throws InterruptedException {
            Thread.sleep(200L);
            return StringUtils.quote(name);
    }


}
