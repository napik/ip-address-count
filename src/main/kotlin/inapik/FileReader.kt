package inapik

import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.max
import kotlin.math.min

class FileReader private constructor(
    private val scanChunkSubscriber: Subscriber<ByteBuffer>,
    private val ipAddressVector: IpAddressVector,
    private val fileChannel: FileChannel,
) {
    // Buffer for reading data
    private val buffer: ByteBuffer = ByteBuffer.allocateDirect(IP_RECORD_BYTES)

    // Executor service for running tasks asynchronously
    private val executor: ExecutorService = Executors.newFixedThreadPool(CPU_CORE_NUMBER)

    // Size of the file
    private val size: Long = fileChannel.size()

    // Determine chunk size based on file size and CPU core number
    private val chunkSize: Int =
        max(CHUNK_THRESHOLD, min((this.size / CPU_CORE_NUMBER).toInt(), (Int.MAX_VALUE shr FOUR)))

    // Position of the last read line in the file
    private var lastEndLinePosition: Long = 0

    // Returns the number of unique IP addresses
    fun count(): Long = ipAddressVector.cardinality()

    @Throws(InterruptedException::class)
    fun run() {
        if (size == 0L) {
            // If the file is empty, close resources and return
            close()
            return
        }

        // Track the number of tasks
        val taskCount = AtomicInteger(0)
        // Latch to wait for all tasks to complete
        val latch = CountDownLatch(1)

        // Process file in chunks
        while (hasNextChunk()) {
            val next = nextChunk()
            // Run each chunk processing asynchronously
            CompletableFuture.runAsync({
                try {
                    taskCount.incrementAndGet()
                    scanChunkSubscriber.onNext(next)
                } finally {
                    // Decrement task count and signal latch if all tasks are done
                    if (taskCount.decrementAndGet() == 0) {
                        latch.countDown()
                    }
                }
            }, this.executor)
        }

        // Wait for all tasks to complete
        latch.await()
        close()
    }

    /**
     * Check if there are more chunks to read
     * @return true if a new chunk exists
     */
    private fun hasNextChunk(): Boolean = lastEndLinePosition < size

    /**
     * Get the next chunk of the file
     * @return ByteBuffer containing the next chunk
     */
    private fun nextChunk(): ByteBuffer {
        // Determine the end position of the current chunk
        var currentEndPosition = min(lastEndLinePosition + chunkSize, size)
        currentEndPosition = if (currentEndPosition >= size) {
            size
        } else {
            findLastLine(currentEndPosition)
        }

        return try {
            // Calculate the size to read
            val readSize = currentEndPosition - lastEndLinePosition
            // Map the chunk of the file into memory
            val byteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, lastEndLinePosition, readSize)
            // Update the last read position
            lastEndLinePosition = currentEndPosition
            byteBuffer
        } catch (e: IOException) {
            throw IllegalStateException("Error mapping file channel", e)
        }
    }

    /**
     * Find the position of the last line in the chunk
     * @param nextEnd Last chunk position
     * @return Corrected position by '\n' character
     */
    private fun findLastLine(nextEnd: Long): Long {
        val pos = nextEnd - IP_RECORD_BYTES
        try {
            buffer.clear()
            fileChannel.read(buffer, pos)
            for (i in 0 until buffer.capacity()) {
                if (buffer[i] == NEXT_LINE_CODE) {
                    return pos + i + 1
                }
            }
            throw IllegalArgumentException("IP address not correct")
        } catch (e: IOException) {
            throw IllegalStateException("File channel reading error", e)
        }
    }

    /**
     * Close resources
     */
    private fun close() {
        try {
            fileChannel.close()
            executor.shutdown()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val FOUR = 4
        private const val IP_RECORD_BYTES = 16
        private const val CHUNK_THRESHOLD = 1024
        private const val NEXT_LINE_CODE = '\n'.code.toByte()
        private val CPU_CORE_NUMBER = Runtime.getRuntime().availableProcessors()

        /**
         * Factory method to create a FileReader instance
         * @param filePath Path to the file
         * @return FileReader instance
         * @throws IOException if an I/O error occurs
         */
        @Throws(IOException::class)
        @JvmStatic
        fun create(filePath: Path): FileReader {
            val fileChannel = Files.newByteChannel(filePath, StandardOpenOption.READ) as FileChannel

            val ipAddressVector = IpAddressVector.create()
            val scanChunkSubscriber = ScanChunkSubscriber(ipAddressVector::set)

            return FileReader(scanChunkSubscriber, ipAddressVector, fileChannel)
        }
    }
}
