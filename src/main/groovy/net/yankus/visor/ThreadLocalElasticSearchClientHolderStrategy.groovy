package net.yankus.visor

class ThreadLocalElasticSearchClientHolderStrategy {
    
    ThreadLocal<ElasticSearchClientContext> context = new ThreadLocal<ElasticSearchClientContext>()

    def clear() {
        context.set(null)
    }

    def get() {
        if (!context.get()) {
            context.set(new ElasticSearchClientContext())
        }
        context.get()
    }

    def set(context) {
        this.context.set(context)
    }

}