package io.github.afezeria.mybatispluswrapperext.processor

private val groupingRegex = Regex("[a-z]+|[0-9]+|[A-Z][a-z]+|[A-Z]++(?![a-z])|[A-Z]")

enum class NamingConvention {
    /**
     * twoWords
     */
    CAMEL_CASE {
        override fun convert(fieldName: String): String {
            return groupingRegex.findAll(fieldName).joinToString("") {
                it.value.lowercase().replaceFirstChar { it.uppercase() }
            }.replaceFirstChar { it.lowercase() }
        }
    },

    /**
     * two_words
     */
    SNAKE_CASE {
        override fun convert(fieldName: String): String {
            return groupingRegex.findAll(fieldName).joinToString("_") { it.value.lowercase() }
        }

    },

    /**
     * TwoWords
     */
    PASCAL_CASE {
        override fun convert(fieldName: String): String {
            return groupingRegex.findAll(fieldName).joinToString("") {
                it.value.lowercase().replaceFirstChar { it.uppercase() }
            }
        }
    },

    /**
     * TWO_WORDS
     */
    CONSTANT_CASE {
        override fun convert(fieldName: String): String {
            return groupingRegex.findAll(fieldName).joinToString("_") { it.value.uppercase() }
        }
    },

    /**
     * TWOWORDS
     */
    UPPER_CASE {
        override fun convert(fieldName: String): String {
            return groupingRegex.findAll(fieldName).joinToString("") { it.value.uppercase() }
        }
    },

    /**
     * 和字段名保持一致
     */
    FIELD_NAME {
        override fun convert(fieldName: String): String {
            return fieldName
        }
    },
    ;

    abstract fun convert(fieldName: String): String


}