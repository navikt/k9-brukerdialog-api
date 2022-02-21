# K9 Brukerdialog API

API som undersøttter søknadsprosessen for flere ytelser i folketrygdloven kapittel 9.

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



## Drift og støtte

## Alarmer
Vi bruker [nais-alerts](https://doc.nais.io/observability/alerts) for å sette opp alarmer. Disse finner man konfigurert i [nais/alerterator.yml](nais/alerterator.yml).
## Henvendelser

Spørsmål knyttet til koden eller prosjektet kan stilles som issues her på GitHub.

Interne henvendelser kan sendes via Slack i kanalen #sif-brukerdialog
