package dslContext

import dslContext.base.Context
import dslContext.base.DslContext
import groovy.util.logging.Slf4j
import migration.clearcase.Component

import static dslContext.ContextHelper.executeInContext

@Slf4j
class ComponentContext implements Context {
    Component component


    /**
     * ComponentContext constructor
     * @param name the Component name
     */
    public ComponentContext(String name) {
        log.debug('Entering ComponentContext().')
        component = new Component(name)
        log.trace("Component {} registered for migration.", component.name)
        log.debug('Exiting ComponentContext().')
    }

    /**
     * Adds a Stream to the component for migration
     * @param name The name of the Stream
     * @param closure The configuration of the Stream
     */
    def void stream(String name, @DslContext(StreamContext) Closure closure) {
        log.debug('Entering stream().')
        def streamContext = new StreamContext(name)
        executeInContext(closure, streamContext)
        component.streams.add(streamContext.stream)
        log.trace('Added Stream {} to Component {}.', streamContext.stream.name, component.name)
        log.debug('Exiting stream().')
    }

    /**
     * Sets migration options for the Component
     * @param closure the migration options to set
     */
    def void migrationOptions(@DslContext(MigrationOptionsContext) Closure closure) {
        log.debug('Entering migrationOptions().')
        def migrationOptionsContext = new MigrationOptionsContext()
        executeInContext(closure, migrationOptionsContext)
        component.migrationOptions = migrationOptionsContext.migrationOptions
        log.trace('Configured migration options for Component {}.', component.name)
        log.debug('Exiting migrationOptions().')
    }
}
