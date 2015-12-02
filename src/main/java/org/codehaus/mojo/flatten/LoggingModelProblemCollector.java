package org.codehaus.mojo.flatten;

import org.apache.maven.model.building.ModelProblemCollector;
import org.apache.maven.model.building.ModelProblemCollectorRequest;
import org.apache.maven.plugin.logging.Log;

/**
 * This is an implementation of {@link ModelProblemCollector} that is logging all problems.
 *
 * @author Joerg Hohwiller (hohwille at users.sourceforge.net)
 * @since 1.0.0-beta-4
 */
public class LoggingModelProblemCollector
    implements ModelProblemCollector
{

    private final Log logger;

    /**
     * The constructor.
     *
     * @param logger is the {@link Log}.
     */
    public LoggingModelProblemCollector( Log logger )
    {
        super();
        this.logger = logger;
    }

    /**
     * {@inheritDoc}
     */
    public void add( ModelProblemCollectorRequest req )
    {
        this.logger.warn( req.getMessage(), req.getException() );
    }

}
