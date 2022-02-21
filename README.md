# K9 Brukerdialog API

API som understøtter søknadsprosessen for flere ytelser i folketrygdloven kapittel 9.

Foreløpig støtte for følgende ytelser:

*

## Oppslag

#### Søker

```http
  GET /oppslag/soker
```

#### Barn

```http
  GET /oppslag/barn
```

#### Arbeidsgivere

```http
  GET /oppslag/arbeidsgiver
```

| Parameter | Type     | Beskrivelse                |
| :-------- | :------- | :------------------------- |
| `fra_og_med` | `string` | **Påbudt**. Periode fra og med for ansettelseforhold |
| `til_og_med` | `string` | **Påbudt** Periode til og med for ansettelseforhold |
| `frilansoppdrag` | `boolean` | **Valgfri** Må settes til true dersom man ønsker oppslag av frilansoppdrag|
| `private_arbeidsgivere` | `boolean` | **Valgfri**  Må settes til true dersom man ønsker oppslag av private arbeidsgivere|



## Drift og støtte

## Alarmer
Vi bruker [nais-alerts](https://doc.nais.io/observability/alerts) for å sette opp alarmer. Disse finner man konfigurert i [nais/alerterator.yml](nais/alerterator.yml).
## Henvendelser

Spørsmål knyttet til koden eller prosjektet kan stilles som issues her på GitHub.

Interne henvendelser kan sendes via Slack i kanalen #sif-brukerdialog
