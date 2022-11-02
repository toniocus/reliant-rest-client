package rest.client.retrofit;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Call;
import okhttp3.EventListener;

public class OkHttpListener extends EventListener {

    AtomicInteger counter = new AtomicInteger();

    @Override
    public void callStart(final Call call) {
        this.counter.incrementAndGet();
    }

    @Override
    public void callEnd(final Call call) {
        this.counter.decrementAndGet();
    }

    @Override
    public void callFailed(final Call call, final IOException ioe) {
        this.counter.decrementAndGet();
    }

    public int getCounter() {
        return this.counter.get();
    }
}
