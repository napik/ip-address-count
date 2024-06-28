package inapik

interface Subscriber<T> {
    // Method to process the next item
    fun onNext(item: T)
}
