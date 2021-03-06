package net.yankus.visor

import groovy.util.logging.Log4j 

@Log4j
class AnnotationDefaultClosureLogger {
    
    static def trace(def message) {
        log.trace message
    }

    static def debug(def message) {
        log.debug message
    }

    static def info(def message) {
        log.info message
    }
}
