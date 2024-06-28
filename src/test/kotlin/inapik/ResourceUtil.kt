package inapik

import java.net.URISyntaxException
import java.nio.file.Path
import java.nio.file.Paths

object ResourceUtil {
    @JvmStatic
    fun testResourcePath(testFileName: String?): Path {
        try {
            val resource = ResourceUtil::class.java.classLoader.getResource(testFileName)
            return resource?.toURI()?.let { Paths.get(it) }
                ?: error("resource $testFileName not found")
        } catch (e: URISyntaxException) {
            throw IllegalStateException(e)
        }
    }
}
