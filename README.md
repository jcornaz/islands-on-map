# Islands on a map
[Thanos Psaridis](https://github.com/ThanosFisherman) kindly published his work for an assessment he did when applying for a position in switzerland: https://github.com/ThanosFisherman/Advanon-SpringBoot-Task

I found this interesting and decided to do the same task from scratch as a small personal exercise.

However, as I'm not applying for the job position, I can change the points of the tasks I don't like. For instance I'll go for [Ktor](https://github.com/ktorio/ktor) instead of SpringBoot, and I'll not provide the endpoint `POST api/maps` because it does not respect the semantic of REST verbs IMO.

### Assignment
#### Concept
An *island* is formed by *tiles* of type "land" which are surrounded by "water" tiles. Two tiles belong to the same island if they touch.

A *map* is a an collection of *tiles*

Two given tiles are considered *adjacent* if they are directly vertically or horizontally next to each other. If they touch with their corners they do not count as *adjacent*.  

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
* Implement an API-only Spring Boot application. *(I will use [Ktor](https://github.com/ktorio/ktor) instead)*
* You can use any database of your choice. *(I will use mongodb)*
* You can use any libs you like.
* Use a local git repository and send us a tarball archive with your solution by email.
* Don't put your solution on a publicly accessible repository.
	* *I don't consider my code as a solution of the given task, because:*
		* *I don't use spring boot as requested*
		* *I do not provide the requested endpoint `POST api/maps`*
		* *I'm not applying for the job position*
  
##### Bonus tasks
  * Provide a Dockerfile which allows to run your app
  * Add an API endpoint that returns the map in ASCII style. Output could be something like:
  ```
  #---x-- 
  #--xxx- 
  #----x-
  ```

#### Endpoints to provide

* `POST api/maps`
  * Should make a GET call to https://private-2e8649-advapi.apiary-mock.com/map
    and create in the database entries for: 
    * The given "Map" and the "Tiles" that belong to the map 
    * The islands that can be detected on the map with related tiles 
    * An island should belong to a map
  * *This is a silly design IMO. A `POST` request should create the entity which has been given in the post arguments. So I decided to not provide this endpoint. Instead my service will initialize its database by fetching  https://private-2e8649-advapi.apiary-mock.com/map at startup*.
* `GET api/islands`
  * Should retrieve all islands with the tiles that belong to them
* `GET api/islands/:id`
  * Should retrieve just one island with the matching id. Also including related tiles
