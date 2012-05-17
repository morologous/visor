package net.yankus.visor

class ThreadLocalElasticSearchClientHolderStrategy {
    
    ThreadLocal<ElasticSearchClientRegistry> registry = new ThreadLocal<ElasticSearchClientRegistry>()

    def clear() {
        registry.set(null)
    }

    def get() {
        if (!registry.get()) {
            registry.set(new ElasticSearchClientRegistry())
        }
        registry.get()
    }

    def set(registry) {
        this.registry.set(registry)
    }

}