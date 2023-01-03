# K9 Brukerdialog API
![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=navikt_k9-brukerdialog-api&metric=ncloc)
![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=navikt_k9-brukerdialog-api&metric=alert_status)
![Coverage](https://sonarcloud.io/api/project_badges/measure?project=navikt_k9-brukerdialog-api&metric=coverage)
![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=navikt_k9-brukerdialog-api&metric=code_smells)
![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=navikt_k9-brukerdialog-api&metric=sqale_index)
![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=navikt_k9-brukerdialog-api&metric=duplicated_lines_density)
![Bugs](https://sonarcloud.io/api/project_badges/measure?project=navikt_k9-brukerdialog-api&metric=bugs)

API som understøtter søknadsprosessen for ytelser i folketrygdloven kapittel 9.

Støtte for følgende ytelser:

| Ytelse | Prod | Prosesseringstjeneste | Frontend repo |
| --- | -- | -- | -- |
| Omsorgspenger utvidet rett | Ja | https://github.com/navikt/omsorgspengesoknad-prosessering | https://github.com/navikt/omsorgspengesoknad |
| Omsorgspenger midlertidig alene | Ja | https://github.com/navikt/omsorgspenger-midlertidig-alene-prosessering | https://github.com/navikt/omsorgspenger-midlertidig-alene-dialog |
| Ettersending | Ja | https://github.com/navikt/k9-ettersending-prosessering | https://github.com/navikt/sif-ettersending |
| Omsorgsdager aleneomsorg | Ja | https://github.com/navikt/omsorgsdager-aleneomsorg-prosessering | https://github.com/navikt/omsorgsdager-aleneomsorg-dialog |
| Omsorgspenger utbetaling arbeidstaker | Ja | https://github.com/navikt/omsorgspengerutbetalingsoknad-arbeidstaker-prosessering | https://github.com/navikt/omsorgspengerutbetaling-arbeidstaker-soknad |
| Omsorgspenger utbetaling snf | Ja | https://github.com/navikt/omsorgspengerutbetalingsoknad-prosessering | https://github.com/navikt/omsorgspengerutbetaling-soknad |
| Pleiepenger livets sluttfase | Ja | https://github.com/navikt/pleiepenger-livets-sluttfase-prosessering | https://github.com/navikt/pleiepenger-i-livets-sluttfase-soknad |
| | | | |


### Grafana
https://grafana.nais.io/d/fUOECBs7k/k9-brukerdialog-api?orgId=1

## Oppslag

#### Søker

```
  GET /oppslag/soker
```

#### Barn

```
  GET /oppslag/barn
```

#### Arbeidsgivere

```
  GET /oppslag/arbeidsgiver
```

| Parameter | Type     | Beskrivelse                |
| :-------- | :------- | :------------------------- |
| `fra_og_med` | `string` | **Påbudt**. Periode fra og med for ansettelseforhold. Eks fra_og_med=2021-01-01 |
| `til_og_med` | `string` | **Påbudt** Periode til og med for ansettelseforhold. Eks til_og_med=2021-01-02 |
| `frilansoppdrag` | `boolean` | **Valgfri** Må settes til true dersom man ønsker oppslag av frilansoppdrag.|
| `private_arbeidsgivere` | `boolean` | **Valgfri**  Må settes til true dersom man ønsker oppslag av private arbeidsgivere.|


## Mellomlagring
Bruker [k9-brukerdialog-cache](https://github.com/navikt/k9-brukerdialog-cache) for å mellomlagre søknad i 72 timer.
Man må spesifisere hvilken [ytelse](src/main/kotlin/no/nav/k9brukerdialogapi/ytelse/Ytelse.kt) det gjelder som route parameter.

### Legge til mellomlagring 
```
  POST /mellomlagring/{ytelse}
```
### Oppdatere mellomlagring 
```
  PUT /mellomlagring/{ytelse}
```
### Hente mellomlagring 
```
  GET /mellomlagring/{ytelse}
```
### Slette mellomlagring 
```
  DELETE /mellomlagring/{ytelse}
```

## Vedlegg

### Lagre vedlegg
```
  POST /vedlegg
```
### Hente vedlegg
```
  GET /vedlegg/{vedleggId}
```
### Slette vedlegg
```
  DELETE /vedlegg/{vedleggId}
```

## Drift og støtte

## Alarmer
Vi bruker [nais-alerts](https://doc.nais.io/observability/alerts) for å sette opp alarmer. Disse finner man konfigurert i [nais/alerterator.yml](nais/alerterator.yml).

## Grafana
https://grafana.nais.io/d/qVOaNvfnk/k9-brukerdialog-api

## Henvendelser
Spørsmål knyttet til koden eller prosjektet kan stilles som issues her på GitHub.

Interne henvendelser kan sendes via Slack i kanalen #sif-brukerdialog
