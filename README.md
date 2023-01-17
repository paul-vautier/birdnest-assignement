# PROJECT BIRDNEST

- 1 : Introduction
- 2 : Presentation of the project
## 1 : Introduction 
A rare and endangered Monadikuikka has been spotted nesting at a local lake.

Unfortunately some enthusiasts have been a little too curious about this elusive bird species, flying their drones very close to the nest for rare photos and bothering the birds in the process.

To preserve the nesting peace, authorities have declared the area within 100 meters of the nest a no drone zone (NDZ), but suspect some pilots may still be violating this rule.

The authorities have set up drone monitoring equipment to capture the identifying information broadcasted by the drones in the area, and have given you access to a national drone pilot registry. They now need your help in tracking violations and getting in touch with the offenders.

## Data
### Drone positions

`GET assignments.reaktor.com/birdnest/drones`

The monitoring equipment endpoint above provides a snapshot of all the drones within a 500 by 500 meter square and is updated about once every 2 seconds. The equipment is set up right next to the nest.

This snapshot is in XML format and contains, among other things, the position and serial number of each drone in the area.

- The position of the drones are reported as X and Y coordinates, both floating point numbers between 0-500000
- The no-fly zone is a circle with a 100 meter radius, origin at position 250000,250000

### Pilot information

`GET assignments.reaktor.com/birdnest/pilots/:serialNumber`

The national drone registry endpoint above will provide you the name, contact information and other details for a drone's registered owner in JSON format, based on the given serial number. Please note on a rare occasion pilot information may not be found, indicated by a 404 status code.

In order to protect the privacy of well behaved pilots keeping appropriate distance, you may only query this information for the drones violating the NDZ.
## Objective

Build and deploy a web application which lists all the pilots who recently violated the NDZ perimeter.

**Requirements :**

- Persist the pilot information for 10 minutes since their drone was last seen by the equipment
- Display the closest confirmed distance to the nest
- Contain the pilot name, email address and phone number
- Immediately show the information from the last 10 minutes to anyone opening the application
- Not require the user to manually refresh the view to see up-to-date information

## Presentation of the project

The project structure is a standard Spring Boot 3-tier architecture, with a Presentation (controller folder), Business (service folder) and persistance (repository) layer

```
├── java/
│   └── org.pepdev.birdnest/
│       ├── config/
│       │   └── ...
│       ├── controller/
│       │   └── ...
│       ├── repository/
│       │   └── ...
│       ├── model/
│       │   └── ...
│       ├── service/
│       │   └── ...
│       └── BirdnestApplication.java
└── resources
```

To start the application, you must have a running redis database, or have Docker installed on your computer and run `docker compose up` (may change depending on your system).

You must then configure you keyspace notification in the redis-cli using : `config set notify-keyspace-events KEhx`, enabling notifications on key expiration and HSet modifications 

Application lifecycle :
- The app schedules calls every 2 seconds to the Birdnest API. 
- The data is then stored in the Redis Database.
- The app creates a WebSocket broker where the client will wait for the drones data (they can also be queried in a JSON format at the endpoint `/drones`
- A subscriber to the Redis notifications will automatically send data through the WebSocket on key expiration or data modification 
