package no.nav.k9brukerdialogapi.vedlegg

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import no.nav.helse.dusseldorf.ktor.auth.IdToken
import no.nav.k9brukerdialogapi.general.CallId
import java.net.URL

class VedleggService(
    private val k9MellomlagringGateway: K9MellomlagringGateway
) {
    suspend fun lagreVedlegg(
        vedlegg: Vedlegg,
        idToken: IdToken,
        callId: CallId
    ): String = k9MellomlagringGateway.lagreVedlegg(
        vedlegg = vedlegg,
        idToken = idToken,
        callId = callId
    )

    suspend fun hentVedlegg(
        vedleggId: String,
        idToken: IdToken,
        callId: CallId
    ): Vedlegg? = k9MellomlagringGateway.hentVedlegg(
        vedleggId = vedleggId,
        idToken = idToken,
        callId = callId
    )

    suspend fun hentVedlegg(
        vedleggUrls: List<URL>,
        idToken: IdToken,
        callId: CallId
    ): List<Vedlegg> {
        val vedlegg = coroutineScope {
            val futures = mutableListOf<Deferred<Vedlegg?>>()
            vedleggUrls.forEach {
                futures.add(async {
                    hentVedlegg(
                        vedleggId = it.vedleggId(),
                        idToken = idToken,
                        callId = callId
                    )
                })

            }
            futures.awaitAll().filter { it != null }
        }
        return vedlegg.requireNoNulls()
    }

    suspend fun slettVedlegg(
        vedleggId: String,
        idToken: IdToken,
        callId: CallId
    ): Boolean = k9MellomlagringGateway.slettVedlegg(
        vedleggId = vedleggId,
        idToken = idToken,
        callId = callId
    )

    internal suspend fun persisterVedlegg(
        vedleggsUrls: List<URL>,
        callId: CallId,
        eier: DokumentEier
    ) {
        val vedleggsId = vedleggsUrls.map { it.vedleggId() }

        k9MellomlagringGateway.persisterVedlegg(
            vedleggId = vedleggsId,
            callId = callId,
            eier = eier
        )
    }

    suspend fun fjernHoldPåPersistertVedlegg(
        vedleggsUrls: List<URL>,
        callId: CallId,
        eier: DokumentEier
    ) {
        val vedleggsId = vedleggsUrls.map { it.vedleggId() }

        k9MellomlagringGateway.fjernHoldPåPersistertVedlegg(
            vedleggId = vedleggsId,
            callId = callId,
            eier = eier
        )
    }

    suspend fun finnVedleggSomIkkeEksisterer(vedleggListe: VedleggListe, idToken: IdToken, callId: CallId): List<URL> {
        val vedleggSomIkkeEksisterer = mutableListOf<URL>()
        vedleggListe.vedleggUrl.forEach { vedleggUrl: URL ->
            val resultat = hentVedlegg(
                vedleggUrl.vedleggId(),
                idToken,
                callId
            )
            if (resultat == null) vedleggSomIkkeEksisterer.add(vedleggUrl)
        }
        return vedleggSomIkkeEksisterer
    }
}

fun URL.vedleggId(): String = this.toString().substringAfterLast("/")