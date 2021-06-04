[![BusNusantara Pull Request & Master CI](https://github.com/SenseiSoni/busnusantara-android/actions/workflows/android-master.yml/badge.svg)](https://github.com/SenseiSoni/busnusantara-android/actions/workflows/android-master.yml)
# Bus Nusantara Docs
Aiming to make Intercity bus travel across Indonesia faster and better by reducing travel times for passengers and bus drivers

### Database APIs:

#### Saving to Database

```Kotlin
fun saveRouteToDB(destination: String, stops: List<String>): String?
```

```Kotlin
fun saveOrderToDB(
    routeID: String,
    agentID: String,
    price: Double,
    pickupLocation: String
): String?
```

```Kotlin
fun saveAgentToDB(
    name: String, locationBased: String
): String?
```

#### Querying from database
Make sure to write your code logic with the query result within addOnSuccessListener. It does not work outside, even if you try return the result. Also do specify the Collection, such as Collections.ROUTES.toString() or Collections.ORDERS.toString() etc.

```Kotlin
Firebase.firestore.collection(Collections.ROUTES.toString())
                    .whereEqualTo("destination", destination)
                    .get()
                    .addOnSuccessListener { documents ->
                    
                    // Your Code Logic Here
                  }
```

