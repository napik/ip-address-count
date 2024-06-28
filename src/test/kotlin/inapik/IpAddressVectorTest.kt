package inapik

import io.kotest.core.spec.style.StringSpec
import org.junit.jupiter.api.Assertions.assertEquals
import java.util.stream.IntStream

class IpAddressVectorTest : StringSpec({
    val size = ((1L shl 32) ushr 5).toInt()

    "check non-concurrent IpAddressVector" {
        val intSet: IpAddressVector = IpAddressVector.create()
        (0..1).forEach { j ->
            IntStream.range(0, size).forEach(intSet::set)
        }
        assertEquals(intSet.cardinality(), size.toLong())
    }

    "check concurrent IpAddressVector" {
        val intSet: IpAddressVector = IpAddressVector.create()
        IntStream.range(0, size).parallel().forEach(intSet::set)
        assertEquals(size.toLong(), intSet.cardinality())
    }
})
