package inapik

import java.util.concurrent.atomic.AtomicIntegerArray
import java.util.stream.IntStream

@JvmRecord
data class IpAddressVector(private val bitArray: AtomicIntegerArray) {

    /**
     * Calculates the number of unique IP addresses in the bit array.
     * @return The number of unique IP addresses.
     */
    fun cardinality(): Long {
        return IntStream.range(0, bitArray.length()).parallel()
            .mapToLong { value -> Integer.bitCount(bitArray.get(value)).toLong() }
            .sum()
    }

    /**
     * Sets the bit at the specified index, marking the presence of an IP address.
     * @param bitIndex The index of the bit to set.
     */
    fun set(bitIndex: Int) {
        val index = bitIndex ushr FIVE // Divide by 32
        val bitIndex1 = bitIndex and IPADDR_MASK;
        val bitMask = 1 shl bitIndex1 // Create a mask for the specific bit
        bitArray.updateAndGet(index) { it or bitMask } // Atomically set the bit
    }

    companion object {
        private const val FIVE: Int = 5 // Equivalent to dividing by 32
        private const val IPADDR_MASK: Int = (1 shl (FIVE + 1)) - 1 // Mask for the lower 5 bits (0-31)
        private const val CAPACITY: Int = ((1L shl 32) ushr 5).toInt() // Number of integers needed to cover 2^32 bits

        /**
         * Creates an instance of IpAddressVector with a capacity to hold 2^32 bits.
         * @return A new instance of IpAddressVector.
         */
        @JvmStatic
        fun create(): IpAddressVector {
            val bitArray = AtomicIntegerArray(CAPACITY)
            return IpAddressVector(bitArray)
        }
    }
}
