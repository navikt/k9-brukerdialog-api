package no.nav.k9brukerdialogapi.utils

object StringUtils {
    private const val ANFØRSELSTEGN = "\\u2018\\u2019\\u201a\\u201b\\u201c\\u201d\\u201e\\u201f"
    private const val AKSENTTEGN = "\\u00b4"
    private const val PUNKTTEGN = "\\u2026"

    const val FritekstPattern: String = "^[\\p{Punct}\\p{L}\\p{M}\\p{N}\\p{Sc}\\p{Space}«»–§�$ANFØRSELSTEGN$AKSENTTEGN$PUNKTTEGN]*$"
    val FRITEKST_REGEX = Regex(FritekstPattern)

    fun saniter(str: String): String {
        val original = arrayOf("á", "é", "í", "ó", "ú", "ý", "Á", "É", "Í", "Ó", "Ú", "Ý", "ä", "ë", "ï", "ö", "ü", "ÿ", "Ä", "Ë", "Ï", "Ö", "Ü", "Ÿ", "à", "è", "ì", "ò", "ù", "À", "È", "Ì", "Ò", "Ù", "â", "ê", "î", "ô", "û", "Â", "Ê", "Î", "Ô", "Û", "ã", "ñ", "õ", "Ã", "Ñ", "Õ", "ç", "Ç", "þ", "Þ", "ð", "Ð", "ß", "š", "Š", "ž", "Ž", "µ", "ł", "Ł", "ŕ", "Ŕ", "\u2019", "\u201C", "\u201D")
        val ascii = arrayOf( "a", "e", "i", "o", "u", "y", "A", "E", "I", "O", "U", "Y", "a", "e", "i", "o", "u", "y", "A", "E", "I", "O", "U", "Y", "a", "e", "i", "o", "u", "A", "E", "I", "O", "U", "a", "e", "i", "o", "u", "A", "E", "I", "O", "U", "a", "n", "o", "A", "N", "O", "c","C","b","B","d","D","ss","s","S","z","Z","u","l","L","r","R", "'", "\"", "\"")
        var output = str
        for (i in original.indices) {
            output = output.replace(original[i], ascii[i])
        }
        return output
    }
}
