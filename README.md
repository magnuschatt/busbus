# BusBus Web App Description
![bus bus screenshot](https://i.imgur.com/EdHePQr.png)

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
The frontend could be hosted on a separate container (such as a Node.js),
but because Heroku's free account only allows for a single host,
the frontend is served by the backend.
The responsibilities of the frontend are:
* Register the geo location of the user.
* Connect to the backend and retrieve nearest stops + departure predictions.
* Present the information in a user friendly way.

Interesting files:
* **frontend.html** (The only HTML file. Runs compiled kotlin js files)
* **Main.kt** (Entry point of frontend code. Registers web-pages and
renders the one matching the current URL. I had hoped I had time for
more pages, as opposed to now where we only register a single one)
* **DeparturesPage.kt** (Constructs the departures page of the web app,
 by fetching bus data and generating HTML)

### Common
The common module contains code used by both the frontend and the backend.
The classes here define the API (including the JSON resources)
which the frontend & backend uses to communicate.
With the API clearly defined in a module like this,
we could with ease switch out the frontend the a more traditional
non-Kotlin approach.

## Design choices & take aways
#### Kotlin
I went with Kotlin in the hopes that I could shine a new light on web app
development. The common module proved to be very useful, especially when
modifying the API between them.

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

#### MongoDB
Mongo worked without a problem. I chose it for the following reasons:
* It has 2D geo spatial indexes.
* NoSQL is very easy to work with.
* It's built for scale (if that would become and issue).
* Eventual consistency is okay when dealing with this kind of non-critical bus data.

## Conclusion
I'm happy about the overall quality of the backend,
which is also where my experience lies. The frontend is not
as pretty. DeparturesPage.kt has too many responsibilities.
I underestimated the amount of problems I would have with the frontend.
In hindsight: combining limited CSS knowledge with experimental kotlin
javascript was as challenging as it was exciting.

If I had more time I would put my focus on automated tests
now that the architecture has settled place (other than
cleaning up the frontend).

#### Extra
As a side note also check out my separate Jbins framework.
It's an open source JVM framework that turns your PostgreSQL
(with JSONB) into a document store, so you get the ease-of-use
and flexibility from NoSQL combined with transactional data integrity
from Schema based databases.

It has the following features:
* Insert
* Replace
* Delete
* Select

You can create complex where filters easily. Supported operators are:
* AND / OR
* EQ / NEQ
* GT / GTE
* LT / GTE

Operators work with strings, numbers, and dates, no matter where they
in the JSON (e.g. object in array in object in array).

Under the hood a PostgreSQL database-function is dynamically created for
whatever query you give it. This allows for easy monitoring of which
queries are used most often, and the ability to create indexes
directly on the JSONB structure.

Please check out this file for a demo of this:
https://github.com/magnuschatt/jbins/blob/master/core/src/test/kotlin/chatt/jbins/test/Showcase.kt
(also demonstrates how I test if given more time)
