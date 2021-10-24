[![BusNusantara Pull Request & Master CI](https://github.com/SenseiSoni/busnusantara-android/actions/workflows/android-master.yml/badge.svg)](https://github.com/SenseiSoni/busnusantara-android/actions/workflows/android-master.yml)
# Bus Nusantara
Bus Nusantara is an Android mobile app that aims to make intercity bus travel across Indonesia faster and better by reducing travel times for passengers and bus drivers. 

It is the 2nd Prize winner for the 2021 DRP Group Project by the Department of Computing at Imperial College London.

## Why was Bus Nusantara made?

Bus passengers, bus drivers as well as bus station ticket agents often found it extremely difficult to gauge the timings of bus arrivals, since there is no formal mechanism in place especially in the case of intercity buses. What could be an hour's wait often could easily transform into a 2 or 3 hour wait for a passenger, due to unforeseen stops, traffic and other issues leading to a severe pain point for bus passengers. This is on top of an already double-digit travel time purely due to distance, as well as time for meal breaks, toilet stops, as well as detours to stations that may not even have passengers waiting for the buses causing further delay.

In fact, after liaising with over 185 bus passengers and 53 bus drivers, we found:
- 45.9% complained the bus timings to be extremely unreliable
- 32.4% complained the duration of the journey too long
- 15.7% complained of having too many stops in the middle of the journey
- Average bus waiting time was 50 minutes

After making this app, over 45 minutes were saved per passenger's journey. Projecting using the intercity bus passenger count of 23 million just during the time of Idul Fitri, over 17.25 million hours collectively could have been saved in journey time.

## What does Bus Nusantara do?

The people we researched with informed us that they would really appreciate being about to know:
- the location of the bus at present
- it's estimated time of arrival
- knowing the journey duration

This is what Bus Nusantara aimed for since its beginning. The app allows bus passengers to scan their bought tickets and are made aware of their bus's location, time of arrival, the stops it will take (at present calculation) and the amenities available on the bus, as well as some other additional information. This is important so the bus passengers know when they need to arrive at the bus stop to board the bus.

Bus drivers have a larger screen for tablets to accommodate for built-in dashboards on the bus. They can easily view bus stops, the route, expected time of arrivals, current number of passengers waiting at the bus stops, and whether any currently onboarded passenger would like to request a stop. These are important to make the bus journey more comfortable for the bus driver without passengers interfering, and avoid bus stops that are not required if there are no passengers waiting.

<!-- ![Bus Driver View of Bus Nusantara](https://user-images.githubusercontent.com/26791244/138616127-33ce03b4-770e-4af4-b099-d1891c2dbaf3.png)
![image](https://user-images.githubusercontent.com/26791244/138616234-703274f0-d207-4cce-970f-d2aac7b4beb6.png) -->

<p float="middle">
  <img src="https://user-images.githubusercontent.com/26791244/138616127-33ce03b4-770e-4af4-b099-d1891c2dbaf3.png" width="600" />
  <img src="https://user-images.githubusercontent.com/26791244/138616234-703274f0-d207-4cce-970f-d2aac7b4beb6.png" width="300" /> 
</p>


## Engineerings

Bus Nusantara is an Kotlin-based android app built on Android Studio with dual language functionality in English and Bahasa Indonesia. It leverages Firebase as its backend due to its seamless integrations, NoSQL database, scalability and extensive testing support. Additionally, Google Cloud APIs are used using the Distance Matrix API to calculate distances between locations on Google Maps and Directions API to calculate the optimal route between any two given locations. Phones locations are tracked the user locations representing the bus driver to calculate the required information from the bus to the passenger or bus stop. Data is synced quickly between different phones due to concurrency to reduce power wastage and cellular data usage on phones, and event listeners attached to important database collections.


