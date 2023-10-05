package io.github.afezeria.mybatispluswrapperext.processor

import com.tschuchort.compiletesting.*
import io.github.afezeria.mybatispluswrapperext.processor.mapper.IgnoreTag
import io.github.afezeria.mybatispluswrapperext.processor.mapper.PersonMapper
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.reflect.KClass

/**
 *
 * @author afezeria
 */
class MainProcessorTest {
    @Test
    fun compileTest() {
        compile(
            PersonMapper::class,
            args = mutableMapOf("ignoreMarkerAnnotation" to IgnoreTag::class.qualifiedName!!)
        )
    }
}

fun compile(vararg clazz: KClass<*>, args: MutableMap<String, String> = mutableMapOf()) {

    val compilation = KotlinCompilation().apply {
        this.sources = clazz.map {
            SourceFile.fromPath(File("./src/test/kotlin/" + it.qualifiedName?.replace('.', '/') + ".kt"))
        }
        this.kspArgs = args
        symbolProcessorProviders = listOf(KspProcessor.Provider())
        inheritClassPath = true
    }
    val result = compilation.compile()
    assert(result.exitCode == KotlinCompilation.ExitCode.OK)
    compilation.kspSourcesDir.walk()
        .filter { it.isFile }
        .forEach {
            println("========")
            println(it.name)
            println(it.readText())

        }
    result.generatedFiles.forEach {
        println(it.name)
    }
    println()
//    val bytes = result.generatedFiles.find {
//        it.extension == "class" && it.nameWithoutExtension == clazz.simpleName + "Impl"
//    }!!.readBytes()
//    @Suppress("UNCHECKED_CAST")
//    return loadClass(bytes) as T
}
