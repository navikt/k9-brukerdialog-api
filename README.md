# K9 Brukerdialog API

API som understøtter søknadsprosessen for flere ytelser i folketrygdloven kapittel 9.

Foreløpig støtte for følgende ytelser:

* Omsorgspenger utvidet rett (Brukes ikke enda)

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


## Mellomlagring
Mellomlagring (skjemadata fra frontend) gjøres i en Redis instans og lagres i 72 timer om gangen. Man må spesifisere
hvilken ytelse det gjelder som route parameter.

### Legge til mellomlagring 
```http
  POST /mellomlagring/{ytelse}
```
### Oppdatere mellomlagring 
```http
  put /mellomlagring/{ytelse}
```
### Hente mellomlagring 
```http
  get /mellomlagring/{ytelse}
```
### Slette mellomlagring 
```http
  delete /mellomlagring/{ytelse}
```

## Vedlegg

### Lagre vedlegg
```http
  POST /vedlegg
```
### Hente vedlegg
```http
  GET /vedlegg/{vedleggId}
```
### Slette vedlegg
```http
  DELETE /vedlegg/{vedleggId}
```

## Drift og støtte

## Alarmer
Vi bruker [nais-alerts](https://doc.nais.io/observability/alerts) for å sette opp alarmer. Disse finner man konfigurert i [nais/alerterator.yml](nais/alerterator.yml).
## Henvendelser

Spørsmål knyttet til koden eller prosjektet kan stilles som issues her på GitHub.

Interne henvendelser kan sendes via Slack i kanalen #sif-brukerdialog
