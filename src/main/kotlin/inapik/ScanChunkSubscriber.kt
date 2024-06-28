package inapik

import java.nio.ByteBuffer

class ScanChunkSubscriber(private val consumer: (Int) -> Unit) : Subscriber<ByteBuffer> {

    // Method called when a new ByteBuffer chunk is available
    override fun onNext(item: ByteBuffer) {
        // Delegate chunk scanning to internal method
        scanChunk(item, consumer)
    }

    // Inline function to scan the ByteBuffer chunk
    private inline fun scanChunk(buffer: ByteBuffer, action: (Int) -> Unit) {
        var ipAddress = 0
        var segment = 0

        // Process each byte in the ByteBuffer
        while (buffer.hasRemaining()) {
            when (val byte = buffer.get()) {
                // If the byte is a digit, update the segment value
                in ZERO_CODE..NINE_CODE -> {
                    segment = segment * TEN + (byte - ZERO_CODE)
                }

                // If the byte is a dot, shift the IP address and reset the segment
                DOT_CODE -> {
                    ipAddress = (ipAddress shl SEGMENT_SIZE) or segment
                    segment = 0
                }

                // If the byte is a newline, finalize the IP address
                NEWLINE_CODE -> {
                    ipAddress = (ipAddress shl SEGMENT_SIZE) or segment
                    // Perform action with IP address, considering endianness
                    action(ipAddress)
                    segment = 0
                    ipAddress = 0
                }
            }
        }
    }

    companion object {
        private const val TEN = 10
        private const val SEGMENT_SIZE = 8
        private const val ZERO_CODE: Byte = '0'.code.toByte()
        private const val NINE_CODE: Byte = '9'.code.toByte()
        private const val DOT_CODE: Byte = '.'.code.toByte()
        private const val NEWLINE_CODE: Byte = '\n'.code.toByte()
    }
}
