# BusBus Web App Description
<!-- ![bus bus screenshot](https://i.imgur.com/EdHePQr.png) -->

This web app shows bus departures near you in San Francisco.
To see a live version please go to: https://busbus.herokuapp.com/

It consists of three modules all written in Kotlin:
* backend
* frontend
* common

## Module overview
### Backend
The backend is built using the **Ktor framework** and below it is a **Mongo Database**.
The responsibilities of the backend are:
* Connect to the public NextBus API and retrieve the necessary Bus data.
* Store bus stop locations such that 'search-by-nearest' queries are done quickly.
* Expose easy to use REST endpoint for the frontend.

Interesting files:
* **Application.kt** (Initializes the Ktor engine. Exposes GET endpoint
for bus stop data + departure predictions using BusDataService.kt)
* **BusDataService.kt** (Responsible for all tasks to do with bus data.
Uses both NextBusClient.kt and BusDatabase.kt)
* **NextBusClient.kt** (Responsible for all communication with the public
NextBus API. Converts incoming XML to internal data model.)
* **BusDatabase.kt** (Exposes the data persistence layer. If the datbase
were to change (such as to a PostgreSQL) this interface would have
to be reimplemented)
* **MongoBusDatabase.kt** (Mongo implementation of the BusDatabase interface)

### Frontend
The frontend is a SPA (Single-Page-App) built using a homemade framework.
The frontend could have been hosted on a separate container (such as a Node.js),
but because Heroku's free account only allows for a single host, the frontend
is served by the backend.
The responsibilities of the frontend are:
* Register the geo location of the user.
* Connect to the backend and retrieve nearest stops + departure predictions.
* Present the information in a user friendly way.

Interesting files:
* **frontend.html** (Only HTML file. Runs compiled kotlin js files)
* **Main.kt** (Entry point of frontend code. Registers web-pages and
renders the one matching the current URL)
* **DeparturesPage** (This the main page of the web app, fetching and
displaying the departure info)

### Common
Because everything is written in Kotlin we can share between the frontend
and backend.
The common module contains code used by both the frontend and the backend.
The classes here define the API (including the JSON resources)
which the frontend & backend uses to communicate.

## Design choices & take aways
#### Kotlin
I went with Kotlin in the hopes that I could shine a new light on web app
development. I hadn't previously tried sharing code between a
frontend and a backend but it proved to be very useful, especially when
modifying the API between them. The language itself is as always very
handy, and makes for quick development and clean, readable, and safe code.
With Kotlins coroutines I spent less that 2 minutes making the calls
to NextBus API multithreaded.

Choosing Kotlin did of course have it's downsides:
Kotlin on the JVM is working flawlessly, but the JS part is still
experimental and it shows. There are little or no
existing frameworks. Setting up a the dev environment for javascript
was challenging, but once I had it figured out it worked well, with instant
redeploy. Kotlin javascript best practices isn't yet settled,
which would make it extra challenging to hire new developers for
a Kotlin project.

#### Ktor
With the limited functionality requirements I went with a lightweight
backend framework (Ktor) which could with very little setup expose
the necessary endpoints. It has close to instant redeploy which allowed
for quick development. Ktor worked without problems.

Spring was a close alternative, and would probably be a better choice
if the project were to grow.

#### MongoDB
Mongo worked without a problem. I chose it for the following reasons:
* It has 2D geo spatial indexes.
* NoSQL is very easy to work with.
* It's built for scale (if that would become and issue).
* Eventual consistency is okay when dealing with this kind of non-critical bus data.



