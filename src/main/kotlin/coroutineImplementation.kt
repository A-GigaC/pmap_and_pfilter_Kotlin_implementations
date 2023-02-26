import kotlinx.coroutines.*

@OptIn(ExperimentalCoroutinesApi::class)
inline fun <T, R> Iterable<T>.cpmap(
    chunkSize: Int,
    coreNumber: Int,
    crossinline transform: (T) -> R
): Iterable<R> {
    val chunks = this.chunked(chunkSize)
    var result : MutableList<R> = mutableListOf<R>()
    val coroutines : MutableList<Deferred<List<R>>> = mutableListOf()
    runBlocking {
        chunks.forEach {
            coroutines.add(async(context = Dispatchers.Default.limitedParallelism(coreNumber), CoroutineStart.DEFAULT) {
                it.map(transform).toList()
            })
        }
        result = coroutines.awaitAll().flatten().toMutableList()
    }
    return result
}

@OptIn(ExperimentalCoroutinesApi::class)
inline fun <T> Iterable<T>.cpfilter(
    chunkSize: Int,
    coreNumber: Int,
    crossinline predicate: (T) -> Boolean
): Iterable<T> {
    val chunks = this.chunked(chunkSize)
    var result : MutableList<T> = mutableListOf<T>()
    val coroutines : MutableList<Deferred<List<T>>> = mutableListOf()
    runBlocking {
        chunks.forEach {
            coroutines.add(async(context = Dispatchers.Default.limitedParallelism(coreNumber), CoroutineStart.DEFAULT) {
                it.filter(predicate).toList()
            })
        }
        result = coroutines.awaitAll().flatten().toMutableList()
    }
    return result
}
