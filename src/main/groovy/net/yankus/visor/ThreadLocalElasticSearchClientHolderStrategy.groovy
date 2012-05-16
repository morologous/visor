package net.yankus.visor

class ThreadLocalElasticSearchClientHolderStrategy {
    
    ThreadLocal<ElasticSearchClientRegistry> context = new ThreadLocal<ElasticSearchClientRegistry>()

    def clear() {
        context.set(null)
    }

    def get() {
        if (!context.get()) {
            context.set(new ElasticSearchClientRegistry())
        }
        context.get()
    }

    def set(context) {
        this.context.set(context)
    }

}