# Android-Wifi


## Usage

### Initialization

```java
   Wifi wifi = new Wifi(this);
```

### Client

#### Connect
```java
   wifi.getClient().connect(ssid, pass)
      .subscribe({}, 
         err -> {
            //Something went wrong connecting
         }, () -> {
            //Connected
         });
```

#### Disconnect
```java
   wifi.getClient().disconnect()
      .subscribe({},
         err -> {
            //Error disconnecting
         }, () -> {
            //Disconnected
         }
      );
```

### Station


#### Start
```java
   wifi.getStation().start().subscribe(
      wifiP2pGroup -> {
         //Station info
      }, err -> {
         //Error starting station
      }
   );
```

#### Stop
```java
   wifi.getStation().stop().subscribe(
      {},
      err -> {
         //Error stopping station
      }, () -> {
         //Station stopped
      }
   );
```
