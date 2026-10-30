# customs-historic-statement-frontend

## About

This public-facing microservice is part of the Customs Declaration Service (CDS) Financials service.

It provides an interface for traders to request, view and download historic customs financial documents, including import VAT certificates (C79), cash account statements, postponed import VAT statements, notification of adjustment statements and duty deferment statements.

The service integrates with the [Customs Financials API](https://github.com/hmrc/customs-financials-api) to submit historic document requests and with the Secure Data Exchange Service (SDES) to retrieve available documents. It also uses the Customs Data Store and Customs Financials session cache.

| Key | Value |
|---|---|
| Digital service | CDS Financials |
| Local port | `9396` |
| Base path | `/customs/historic-statement` |
| Back-end | [customs-financials-api](https://github.com/hmrc/customs-financials-api) (port `9878`) |
| Acceptance tests | [customs-historic-statement-acceptance-tests](https://github.com/hmrc/customs-historic-statement-acceptance-tests) |
| Performance tests | [customs-historic-statement-performance-tests](https://github.com/hmrc/customs-historic-statement-performance-tests) |
| Stubs used in Local and Staging | [customs-financials-hods-stub](https://github.com/hmrc/customs-financials-hods-stub), [customs-financials-sdes-stub](https://github.com/hmrc/customs-financials-sdes-stub) |

## How to Run This Service

### Prerequisites

Use [Service Manager](#service-manager-profiles) to start all the services that are needed for local development:

```bash
sm2 --start CUSTOMS_FINANCIALS_ALL
```

### Running the service locally

To run this service from source (for example, to test your own changes), stop the instance started by Service Manager and run it with sbt:

```bash
sm2 --stop CUSTOMS_HISTORIC_STATEMENT_FRONTEND
sbt run
```

The service starts on port `9396`. To access it, [sign in through the auth login stub](https://github.com/hmrc/customs-historic-statement-acceptance-tests#enrolment-required), and you will be redirected to http://localhost:9876/customs/payment-records.

## How to Test This Service

### Local

#### Unit tests


```bash
sbt test
```

To run tests for a single file:

```bash
sbt "test:testOnly *TEST_FILE_NAME*"
```

#### Pre-push check
There is a script called `runAllChecks` that runs all tests, examines their coverage and checks if all the files are properly formatted.
It is good practice to run it just before pushing to GitHub.

```bash
sbt runAllChecks
```

#### Acceptance tests (smoke and regression)

The acceptance tests for this service are in [customs-historic-statement-acceptance-tests](https://github.com/hmrc/customs-historic-statement-acceptance-tests). For information about how to run them, and which environments they run against, see [README](https://github.com/hmrc/customs-historic-statement-acceptance-tests#how-to-run-the-tests).

#### Performance tests

The performance tests for this service are in [customs-historic-statement-performance-tests](https://github.com/hmrc/customs-historic-statement-performance-tests). For information about how to run them, and which environments they run against, see [README](https://github.com/hmrc/customs-historic-statement-performance-tests#how-to-run-the-tests).

### Manual Testing in QA and Staging

After the successful [deployment pipeline](https://build.tax.service.gov.uk/job/BordersAndTradeLiveServices/job/CDSFinancials/job/customs-historic-statement-frontend-pipeline/), manual testing is performed in the QA and/or Staging environments. For more details see [Manual Service Verification in QA and Staging](https://github.com/hmrc/customs-historic-statement-acceptance-tests#manual-testing-in-qa-and-staging).

## Service Catalogue

- [customs-historic-statement-frontend in the MDTP Catalogue](https://catalogue.tax.service.gov.uk/repositories/customs-historic-statement-frontend)

## Jenkins Pipeline
- Front End Build and Deployment Pipeline
    -  [Build pipeline](https://build.tax.service.gov.uk/job/BordersAndTradeLiveServices/job/CDSFinancials/job/customs-historic-statement-frontend/)
    -  [Deployment pipeline](https://build.tax.service.gov.uk/job/BordersAndTradeLiveServices/job/CDSFinancials/job/customs-historic-statement-frontend-pipeline/)
- The acceptance test Jenkins jobs (smoke and regression) are listed in the [customs-historic-statement-acceptance-tests README](https://github.com/hmrc/customs-historic-statement-acceptance-tests#jenkins-builds).
- The performance test Jenkins jobs are listed in the [customs-historic-statement-performance-tests README](https://github.com/hmrc/customs-historic-statement-performance-tests#jenkins-builds).


## Service Manager Profiles
These profiles are defined in [service-manager-config](https://github.com/hmrc/service-manager-config).

| Profile                  | Use                                         | Services started                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
|--------------------------|---------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `CUSTOMS_FINANCIALS_ALL` | Running and developing this service locally | `CUSTOMS_FINANCIALS_FRONTEND`,`CUSTOMS_FINANCIALS_DOCUMENTS_FRONTEND`,`CUSTOMS_FINANCIALS_SECURE_MESSAGING_FRONTEND`,`ENTITY_RESOLVER`, `CHANNEL_PREFERENCES`, `SECURE_MESSAGE_FRONTEND`, `SECURE_MESSAGE`, `SECURE_MESSAGE_STUB`, `MESSAGE`, `CUSTOMS_DUTY_DEFERMENT_FRONTEND`, `CUSTOMS_MANAGE_AUTHORITIES_FRONTEND`, `CUSTOMS_CASH_ACCOUNT_FRONTEND`, `CUSTOMS_GUARANTEE_ACCOUNT_FRONTEND`, `CUSTOMS_HISTORIC_STATEMENT_FRONTEND`, `CUSTOMS_FINANCIALS_API`, `CUSTOMS_FINANCIALS_EMAIL_THROTTLER`, `AUTH`, `AUTH_LOGIN_STUB`, `AUTH_LOGIN_API`, `CENTRALISED_AUTHORISATION_SERVER`, `SSO`, `USER_DETAILS`, `FEEDBACK_FRONTEND`, `CUSTOMS_FINANCIALS_HODS_STUB`, `CUSTOMS_FINANCIALS_SDES_STUB`, `CUSTOMS_FINANCIALS_SDDS_STUB`, `IDENTITY_VERIFICATION`, `BAS_STUBS`, `BAS_GATEWAY`, `BAS_PROXY`, `BAS_GATEWAY_FRONTEND`, `BAS_STUB_FRONTEND`, `TWO_STEP_VERIFICATION_RULE_ENGINE`, `TAR_FRONTEND`, `DATASTREAM`, `EMAIL`, `HMRC_EMAIL_RENDERER`, `CUSTOMS_DATA_STORE`, `CUSTOMS_FINANCIALS_SESSION_CACHE`, `TRACKING_CONSENT_FRONTEND`, `CONTACT_FRONTEND`, `ACCESSIBILITY_STATEMENT_FRONTEND` |


| Service | Port | Repository |
|---|---:|---|
| `CUSTOMS_HISTORIC_STATEMENT_FRONTEND` | `9396` | This service |
| `CUSTOMS_FINANCIALS_FRONTEND` | `9876` | [customs-financials-frontend](https://github.com/hmrc/customs-financials-frontend) |
| `CUSTOMS_FINANCIALS_API` | `9878` | [customs-financials-api](https://github.com/hmrc/customs-financials-api) |

## Endpoints

Application routes are defined in [`conf/app.routes`](https://github.com/hmrc/customs-historic-statement-frontend/blob/main/conf/app.routes). The application is mounted under `/customs/historic-statement`.

The `fileRole` path parameter supports the following values:

- `import-vat` — import VAT certificates (C79)
- `duty-deferment` — duty deferment statements
- `adjustments` — notification of adjustment statements
- `postponed-vat` — postponed import VAT statements
- `cash-statement` — cash account statements

The main journey routes require an authenticated user. See [Enrolment Required](https://github.com/hmrc/customs-historic-statement-acceptance-tests#enrolment-required) for the enrolment details.

| Method | Path | Purpose | Sample request (query / form body) | Response |
|---|---|---|---|---|
| GET | `/start-journey/:fileRole` | Start a journey for a non-duty-deferment document type | `/start-journey/import-vat` | `303` → `/import-vat/request-date` |
| GET | `/start-journey/duty-deferment/:linkId` | Start a duty deferment statement journey using an account link | `/start-journey/duty-deferment/:linkId` | `303` → `/duty-deferment/request-date`; missing session or link: redirect to the session-reset page |
| GET | `/:fileRole/request-date` | Display the date range form | `/import-vat/request-date` | `200` |
| POST | `/:fileRole/request-date` | Submit the requested date range | `start.month=3&start.year=2025&end.month=4&end.year=2025` | Valid: `303` → `/:fileRole/selected-statements`; invalid: `400` |
| GET | `/:fileRole/change-request-date` | Display the form to change the requested date range | `/import-vat/change-request-date` | `200` |
| POST | `/:fileRole/change-request-date` | Submit the changed date range | `start.month=3&start.year=2025&end.month=4&end.year=2025` | Valid: `303` → `/:fileRole/selected-statements`; invalid: `400` |
| GET | `/:fileRole/selected-statements` | Display the check-your-answers page | `/import-vat/selected-statements` | `200`; missing session data: `303` → session-reset page |
| POST | `/:fileRole/selected-statements` | Submit the historic document request | No form body; uses answers stored in the session | Success: `303` → `/:fileRole/confirmation`; failure: `303` → `/technical-difficulties` |
| GET | `/:fileRole/confirmation` | Display the request confirmation page | `/import-vat/confirmation` | `200` |
| GET | `/requested/:fileRole` | View requested historic documents for a document type | `/requested/import-vat` | `200` when the documents are retrieved successfully |
| GET | `/requested/duty-deferment/:linkId` | View requested duty deferment statements for an account link | `/requested/duty-deferment/:linkId` | `200` when the statements are retrieved successfully; missing session or link: redirect to session-reset page |
| GET | `/language/:lang` | Switch the language | `/language/english` | `303` redirect |
| GET | `/logout-survey` | Sign out through the feedback survey flow | – | `303` redirect |
| GET | `/logout` | Sign out without the feedback survey | – | `303` redirect |
| GET | `/verify-your-email` | Display the email verification page | – | `200` |
| GET | `/undeliverable-email` | Display the undeliverable email page | – | `200` |
| GET | `/this-service-has-been-reset` | Display the session-expired page | – | `200` |
| GET | `/unauthorised` | Display the unauthorised page | – | `200` |
| GET | `/technical-difficulties` | Display the technical difficulties page | – | `200` |
| GET | `/assets/*file` | Serve a static asset | For example, an asset path under `/assets/` | `200` if found |

The service's health routes are mounted separately at `/` through `conf/prod.routes`.

## Developer Notes

### Feature flags

This service uses feature flags to enable or disable functionality.

**Do not enable features in `application.conf`**, as this can enable them globally by default and risk exposing work-in-progress features in production.

To enable a feature locally, use:

```bash
sbt "run -Dfeatures.some-feature-name=true"
```

Available feature flags:

| Flag | Default | Description |
|---|---|---|
| `fixed-systemdate-for-tests` | `false` | Fixes the system date for running tests. |

To enable this flag locally:

```bash
sbt "run -Dfeatures.fixed-systemdate-for-tests=true"
```

### Scalafmt

The code is formatted with [sbt-scalafmt](https://scalameta.org/scalafmt/docs/installation.html#sbt), using the rules in `.scalafmt.conf`.

Check that all project files are formatted as expected:

```bash
sbt scalafmtCheckAll scalafmtSbtCheck
```

Format `*.sbt` and `project/*.scala` files:

```bash
sbt scalafmtSbt
```

Format all project files:

```bash
sbt scalafmtAll
```

## License

This code is open source software licensed under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html).
