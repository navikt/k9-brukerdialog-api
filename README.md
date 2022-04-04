# K9 Brukerdialog API

API som understøtter søknadsprosessen for ytelser i folketrygdloven kapittel 9.

Støtte for følgende ytelser:

| Ytelse | Prod | Prosesseringstjeneste | Frontend repo |
| --- | -- | -- | -- |
| Omsorgspenger utvidet rett | Ja | https://github.com/navikt/omsorgspengesoknad-prosessering | https://github.com/navikt/omsorgspengesoknad |
| Omsorgspenger midlertidig alene | Ja | https://github.com/navikt/omsorgspenger-midlertidig-alene-prosessering | https://github.com/navikt/omsorgspenger-midlertidig-alene-dialog |
| Ettersending | Ja | https://github.com/navikt/k9-ettersending-prosessering | https://github.com/navikt/sif-ettersending |
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
Mellomlagring (skjemadata fra frontend) gjøres i en Redis instans og lagres i 72 timer om gangen. Man må spesifisere
hvilken [ytelse](src/main/kotlin/no/nav/k9brukerdialogapi/ytelse/Ytelse.kt) det gjelder som route parameter. 

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
