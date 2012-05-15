package net.yankus.visor

final class ElasticSearchClientHolder {
   
    private static final def INSTANCE = new ElasticSearchClientHolder()

    private def strategy = new ThreadLocalElasticSearchClientHolderStrategy()

    public ElasticSearchClientHolder() { }

    public ElasticSearchClientHolder(strategy) {
        this.strategy = strategy
    }

    def clear() {
        strategy.clear()
    }

    def get() {
        strategy.get()
    }

    def set(ElasticSearchClientContext context) {
        strategy.set(context)
    }
}