package codegen

import org.jetbrains.kotlin.util.capitalizeDecapitalize.toUpperCaseAsciiOnly
import org.junit.jupiter.api.Test

class IterableTest {

    @Test
    fun `convert type`(){
        val iterable: Iterable<String> = listOf()
        iterable.mapNotNull { it.toUpperCaseAsciiOnly() }



    }


}