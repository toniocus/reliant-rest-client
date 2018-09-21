package rest.client;

import java.util.Map;

import org.springframework.classify.SubclassClassifier;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.web.client.ResourceAccessException;

/**
 * The Class ReliantRestClientClassifier.
 */
public class ReliantRestClientClassifier extends SubclassClassifier<Throwable, RetryPolicy> {

    private SimpleRetryPolicy simple3 = new SimpleRetryPolicy(3);

    /**
     * Constructor.
     */
    public ReliantRestClientClassifier() {
        super();
    }

    /**
     * Instantiates a new reliant rest client classifier.
     *
     * @param pTypeMap the type map
     */
    public ReliantRestClientClassifier(final Map<Class<? extends Throwable>, RetryPolicy> pTypeMap) {
        super(pTypeMap, new SimpleRetryPolicy(0));
    }

    /**
     * Constructor.
     *
     * @param pDefaultValue the default value
     */
    public ReliantRestClientClassifier(final RetryPolicy pDefaultValue) {
        super(new SimpleRetryPolicy(0));
    }


    @Override
    public RetryPolicy classify(final Throwable classifiable) {

        RetryPolicy retryp = super.classify(classifiable);

        if (classifiable instanceof ResourceAccessException) {

            boolean connTO = classifiable.getMessage().toLowerCase().contains("connect timed out");

            if (connTO) {
                retryp =  this.simple3;
            }
        }

        return retryp;
    }

}