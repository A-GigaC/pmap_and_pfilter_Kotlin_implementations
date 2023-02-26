class MapParallelTransform <T, R> (
    private val dataForTransform: List<T>,
    private val transform: (T) -> R
) : Thread() {
    val transformed: MutableList<R> = mutableListOf()
    override fun run () {
        for (element in dataForTransform) {
            transformed.add(transform(element))
        }
    }
}

inline fun <T, reified R> Iterable<T>.pmap(
    chunkSize: Int,
    coreNumber: Int,
    noinline transform: (T) -> R
): Iterable<R> {
    val chunks: List<List<List<T>>> = this.chunked(chunkSize).chunked(coreNumber)
    val threadsCollections = chunks.map { coreGroup ->
        coreGroup.map {
            MapParallelTransform <T, R> (it, transform)
        }.toList()
    }.toList()

    return threadsCollections.map { coreGroup ->
        coreGroup.forEach(Thread::start)
        coreGroup.forEach(Thread::join)
        coreGroup.map {
            it.transformed
        }.toList()
    }.flatten()
        .flatten()
}

class FilterParallelTransform <T> (
    private val dataForTransform: List<T>,
    private val predicate: (T) -> Boolean
) : Thread() {
    val transformed: MutableList<T> = mutableListOf()
    override fun run () {
        for (element in dataForTransform) {
            if (predicate(element)) {
                transformed.add(element)
            }
        }
    }
}

inline fun <T> Iterable<T>.pfilter(
    chunkSize: Int,
    coreNumber: Int,
    noinline predicate: (T) -> Boolean
): Iterable<T> {
    val chunks: List<List<List<T>>> = this.chunked(chunkSize).chunked(coreNumber)
    val threadsCollections = chunks.map { coreGroup ->
        coreGroup.map {
            FilterParallelTransform <T> (it, predicate)
        }.toList()
    }.toList()

    return threadsCollections.map { coreGroup ->
        coreGroup.forEach(Thread::start)
        coreGroup.forEach(Thread::join)
        coreGroup.map {
            it.transformed
        }.toList()
    }.flatten()
        .flatten()
}