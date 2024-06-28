package inapik

import java.nio.file.Path
import java.nio.file.Paths
import kotlin.time.Duration
import kotlin.time.measureTime

object IpAddrCount {

    data class CalcInfo(
        val countMeasureTime: Duration,
        val count: Long
    )

    @JvmStatic
    fun doCalculationUnique(filePath: Path): CalcInfo {
        val fileReader = FileReader.create(filePath)
        val countMeasureTime = measureTime { fileReader.run() }
        return CalcInfo(countMeasureTime = countMeasureTime, count = fileReader.count())
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val fileName = args.getOrNull(0)
        require(!fileName.isNullOrBlank()) {
            """Filename is empty.

Usage: java -jar ip-address-count.jar <filename>
<filename> - full path for the file containing IP addresses."""
        }

        val info = doCalculationUnique(Paths.get(fileName))

        println("Complete in ${info.countMeasureTime}")
        println("Number of unique IP addresses: ${info.count}")
    }
}
