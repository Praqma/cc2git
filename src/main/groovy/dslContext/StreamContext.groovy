package dslContext

import dslContext.base.Context
import dslContext.base.DslContext
import groovy.util.logging.Slf4j
import migration.clearcase.Stream

import static dslContext.ContextHelper.executeInContext

@Slf4j
class StreamContext implements Context {
    Stream stream

    /**
     * Constructor for Stream
     * @param name The name of the Stream (Doesn't require fully qualified name)
     */
    public StreamContext(String name) {
        log.debug('Entering StreamContext().')
        stream = new Stream(name)
        log.trace("Stream {} registered for migration.", stream.name)
        log.debug('Exiting StreamContext().')
    }

    /**
     * Sets the branch branch of the stream
     * @param target the branch name
     */
    def void branch(String branch) {
        log.debug('Entering branch().')
        stream.target = branch
        log.trace('Set branch to {} for stream', stream.target, stream.name)
        log.debug('Exiting branch().')
    }

    /**
     * Configures the migration steps
     * @param closure the migration step configuration
     */
    def void migrationSteps(@DslContext(MigrationStepsContext) Closure closure) {
        log.debug('Entering migrationSteps().')
        def stepsContext = new MigrationStepsContext()
        executeInContext(closure, stepsContext)
        stream.filters.addAll(stepsContext.filters)
        log.trace('Added {} filters to stream {}.', stepsContext.filters.size(), stream.name)
        log.debug('Exiting migrationSteps().')
    }
}
