package no.nav.k9brukerdialogapi.mellomlagring

data class Mellomlagring(
    val mellomlagring: String? = null
)

//Håndterer overgangen fra gammel til ny type. Eksisterende mellomlagring har lagret hele json, ny type har kun lagret verdien.
internal fun String.erGammelTypeMellomlagring(): Boolean = contains("\"mellomlagring\"")