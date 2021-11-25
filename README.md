
# customs-historic-statement-frontend

A frontend component for the CDS Financials project which aims to allow the user to download and view historic documents.

| Path                                                                   | Description                                                                                       |
| ---------------------------------------------------------------------  | ------------------------------------------------------------------------------------------------- |
| GET  /start-journey/duty-deferment/:linkId                             | Start of journey for viewing historic documents                                                           |                

The application has the following runtime dependencies:

* `ASSETS_FRONTEND`
* `AUTH`
* `AUTH_LOGIN_STUB`
* `AUTH_LOGIN_API`
* `BAS_GATEWAY`
* `CA_FRONTEND`
* `SSO`
* `USER_DETAILS`
* `CUSTOMS_FINANCIALS_API`
* `CUSTOMS_DATA_STORE`
* `CUSTOMS_FINANCIALS_HODS_STUB`
* `CUSTOMS_FINANCIALS_SDES_STUB`
* `CUSTOMS_FINANCIALS_SESSION_CACHE`
* `CONTACT_FRONTEND`

## Development

You'll need [Service Manager](https://github.com/hmrc/service-manager) to develop locally.

#### Service Manager Commands

Start the required development services (make sure your service-manager-config folder is up to date)

    sm --start CUSTOMS_FINANCIALS_ALL -f
    
Once these services are running, use the following command to start the service `sbt "run 9396"`
    