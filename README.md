# Islands on a map
[Thanos Psaridis](https://github.com/ThanosFisherman) kindly published his work for an assessment he did when applying for a position: https://github.com/ThanosFisherman/islands-on-map

I found this interesting and decided to do the same task from scratch as a small personal exercise.

However, as I'm not applying for the job position, I can change the points of the tasks I don't like. For instance I'll go for [Ktor](https://github.com/ktorio/ktor) instead of SpringBoot.

### Status
This project is discontinued and I will no longer work on it.

**Here are the two main reasons:**

1. After working on it a little bit I realized how bad was the assignement:
    1. It lacks use-case. I had a hard time to guess or invent (even an imaginary) motivation for such service. Therefore it is very hard to end up with good design and good tests, since it is not possible to use BDD and be use-case oriented.
    2. Requested API is not RESTful, and it is hard to make it a good fit for a REST service while keeping the task interesting. (RPC would have been slightly better, but again I lack relevant use-case in order to be able to create a good design)
2. Besides theses problems, I also lost interest for the task. Somehow the problem is "solved" for me.
    1. I provided the main endpoints
    2. I experimented what I wanted with Ktor, Protobuf, Neo4J and Spek.
    3. I wrote the island dectection algorith, which is the only "complex" part. It pass the tests and I don't see how, nor have interest to improve it.
    4. My time is limited, and I have more interesting project/challenges to work on.

### Assignment
#### Concept
A *map* is a a collection of *tiles*

An *island* is formed by *tiles* of type "land" which are surrounded by "water" tiles. Two tiles of type "land" belong to the same island if they are *adjacent*.

Two given tiles are considered *adjacent* if they are directly vertically or horizontally next to each other. If they touch with their corners they are not *adjacent*.

#### Provided endpoint
https://private-2e8649-advapi.apiary-mock.com/map shall be called to get a map.

Here's a result example:
```json
{
    "data": {
        "id": "imaginary",
        "type": "map",
        "links": {
            "self": "https://private-2e8649-advapi.apiary-mock.com/map"
        }
    },
    "attributes": {
        "tiles": [
            { "x": 1, "y": 1, "type": "land" },
            { "x": 2, "y": 1, "type": "land" },
            { "x": 3, "y": 1, "type": "water" },
            { "x": 4, "y": 1, "type": "water" },
            { "x": 5, "y": 1, "type": "land" },
            { "x": 6, "y": 1, "type": "water" },
            { "x": 1, "y": 2, "type": "water" },
            { "x": 2, "y": 2, "type": "land" },
            { "x": 3, "y": 2, "type": "water" },
            { "x": 4, "y": 2, "type": "water" },
            { "x": 5, "y": 2, "type": "water" },
            { "x": 6, "y": 2, "type": "water" },
            { "x": 1, "y": 3, "type": "water" },
            { "x": 2, "y": 3, "type": "water" },
            { "x": 3, "y": 3, "type": "water" },
            { "x": 4, "y": 3, "type": "water" },
            { "x": 5, "y": 3, "type": "land" },
            { "x": 6, "y": 3, "type": "water" },
            { "x": 1, "y": 4, "type": "water" },
            { "x": 2, "y": 4, "type": "water" },
            { "x": 3, "y": 4, "type": "land" },
            { "x": 4, "y": 4, "type": "land" },
            { "x": 5, "y": 4, "type": "land" },
            { "x": 6, "y": 4, "type": "water" },
            { "x": 1, "y": 5, "type": "water" },
            { "x": 2, "y": 5, "type": "water" },
            { "x": 3, "y": 5, "type": "water" },
            { "x": 4, "y": 5, "type": "land" },
            { "x": 5, "y": 5, "type": "water" },
            { "x": 6, "y": 5, "type": "water" }
        ]
    }
}
```

#### Tasks
* Implement an API-only Spring Boot application. *(I use [Ktor](https://github.com/ktorio/ktor) instead)*
* You can use any database of your choice. *(I use Noe4J)*
* You can use any library you like. *(I use many ktor artifacts, slf4j-simple, kotlinx.coroutines, spek and kluent)*
* *Payload format wasn't specified by the task. I use protobuf.*
  
#### Endpoints to provide

* [x] `POST /api/maps`
  * Should make a GET call to https://private-2e8649-advapi.apiary-mock.com/map
    and create in the database entries for: 
    * The given "Map" and the "Tiles" that belong to the map 
    * The islands that can be detected on the map with related tiles 
    * An island should belong to a map
  * *This specification is against rest verbs semantic IMHO. My implementation won't fetch any predefined URl, but require the map to be given as an argument of the post request.*
  * *For consistency, and testability I will also provide the following endpoints:*
    * [x] *`GET /api/maps` Should retrieve all created maps*
    * [x] *`GET /api/maps/:id` Should retrieve the map matching the given id.*
* [x] `GET /api/islands`
  * Should retrieve all islands with the tiles that belong to them.
* [x] `GET /api/islands/:id`
  * Should retrieve just one island with the matching id. Also including related tiles.


*Since I changed the specification of `POST /api/maps`, I'll also provide the following endpoints to make it simpler to fill the database*
  * [x] `POST /api/maps/fetch-requests`
    * Create a request for the server to fetch an URL and create map returned by the URL.
    * Returns the request ID
  * [x] `GET /api/maps/fetch-requests/:id`
    * Returns the state of the given request (pending, success or failed)
  
##### Bonus tasks
  * [ ] Provide a Dockerfile which allows to run your app.
  * [ ] Add an API endpoint that returns the map in ASCII style. Output could be something like:
  ```
  #---x-- 
  #--xxx- 
  #----x-
  ```
  * [ ] *It wasn't asked, but I find interesting to actually deploy the app in cloud*
