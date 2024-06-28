package inapik

import io.kotest.core.spec.style.StringSpec
import io.kotest.data.forAll
import io.kotest.data.row
import org.junit.jupiter.api.Assertions

class IpCounterTest : StringSpec({
    "calculate unique count" {
        forAll(
            row("set_50", 50),
            row("set_single", 1),
            row("set_empty", 0)
        ) { testFileName, expected ->
            val path = ResourceUtil.testResourcePath(testFileName)
            val count = IpAddrCount.doCalculationUnique(path).count
            Assertions.assertEquals(expected.toLong(), count)
        }
    }
})
