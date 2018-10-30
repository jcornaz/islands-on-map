# Islands on a map
[Thanos Psaridis](https://github.com/ThanosFisherman) kindly published his work for an assessment he did when applying for a position: https://github.com/ThanosFisherman/islands-on-map

I found this interesting and decided to do the same task from scratch as a small personal exercise.

However, as I'm not applying for the job position, I can change the points of the tasks I don't like. For instance I'll go for [Ktor](https://github.com/ktorio/ktor) instead of SpringBoot.

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
* You can use any database of your choice. *(I will use mongodb)*
* You can use any library you like. *(I use many ktor artifacts, gson, koin, slf4j-simple, kotlinx.coroutines, junit5, mockito and kluent)*
  
##### Bonus tasks
  * Provide a Dockerfile which allows to run your app.
  * Add an API endpoint that returns the map in ASCII style. Output could be something like:
  ```
  #---x-- 
  #--xxx- 
  #----x-
  ```
  * *It wasn't asked, but I find interesting to actually deploy the app in cloud*

#### Endpoints to provide

* `POST /api/maps`
  * Should make a GET call to https://private-2e8649-advapi.apiary-mock.com/map
    and create in the database entries for: 
    * The given "Map" and the "Tiles" that belong to the map 
    * The islands that can be detected on the map with related tiles 
    * An island should belong to a map
  * *This specification is against rest verbs semantic IMHO. My implementation won't fetch any predefined URl, but require the map to be given as a json argument of the post request.*
* `GET /api/islands`
  * Should retrieve all islands with the tiles that belong to them.
* `GET /api/islands/:id`
  * Should retrieve just one island with the matching id. Also including related tiles.


*Since I changed the specification of `POST /api/maps`, I'll also provide the following endpoints to make it simpler to fill the database*
  * `POST /api/maps/fetch-requests`
    * Create a request for the server to fetch an URL and create map returned by the URL.
    * Returns the request ID
  * `GEt /api/maps/fetch-requests/:id`
    * Returns the state of the given request (pending, success or failed)
