package rest.client.basic;

import java.util.Map;
import java.util.function.BiFunction;

import org.springframework.classify.SubclassClassifier;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;

/**
 * The Class ReliantRestClientClassifier.
 */
public class ReliantRestClientClassifier extends SubclassClassifier<Throwable, RetryPolicy> {

    private static final long serialVersionUID = -8147985442831528565L;
    private BiFunction<Throwable, RetryPolicy, RetryPolicy> classifierFunction;

    /**
     * Instantiates a new reliant rest client classifier.
     *
     * <P>The classifierFunction recieves the last thrown Exception as the 1st. argument,
     * and the RetryPolicy configured in the typeMap as 2nd. argument.
     * <br> It should return the same or a other retry policy and do whatever you need to do
     * with the exception you received.
     *
     * @param typeMap the type map
     * @param classifierFuncion the classifier funcion, may be null.
     */
    public ReliantRestClientClassifier(
            final Map<Class<? extends Throwable>, RetryPolicy> typeMap
            , final BiFunction<Throwable, RetryPolicy, RetryPolicy> classifierFunction) {

        super(typeMap, new SimpleRetryPolicy(0));
        this.classifierFunction = classifierFunction;
    }


    @Override
    public RetryPolicy classify(final Throwable throwable) {

        RetryPolicy retryp = super.classify(throwable);


        if (this.classifierFunction != null) {
            retryp = this.classifierFunction.apply(throwable, retryp);
        }


        return retryp;
    }

}