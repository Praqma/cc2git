package dslContext

@Grab('org.slf4j:slf4j-simple:1.7.7')
import groovy.util.logging.Slf4j
import migration.clearcase.Stream
import utils.StringExtensions

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
        def parseResult = StringExtensions.parseClearCaseName(name)
        this.stream = new Stream(parseResult.tag)
        log.info("Stream {} registered for migration.", stream.name)
        log.debug('Exiting StreamContext().')
    }

    /**
     * Sets the branch branch of the stream
     * @param target the branch name
     */
    def void branch(String branch) {
        log.debug('Entering branch().')
        stream.target = branch
        log.info('Set stream {} branch to {}', stream.name, stream.target)
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
        log.info('Added {} Steps to Stream {}.', stepsContext.filters.size(), stream.name)
        log.debug('Exiting migrationSteps().')
    }
}