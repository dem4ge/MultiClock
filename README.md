# MultiClock

Multiplayer chess clock for board games.
Supports 2–6 players with simple, fast interaction.

## Features

* Multiple players (2 to 6)
* Turn-based timer
* Tap to start, tap again to pass turn clockwise
* Drag & drop to reorder players
* Pause without losing current player
* Player names
* Shared configurable time (default: 1 hour)
* Reverse mode (count up instead of countdown)
* Clean UI with color-coded players

## How it works

* All players start inactive
* First tap activates a player and starts their timer
* Tapping the active player switches turn to the next player
* Only one timer runs at a time
* Timer never goes below 00:00

## Controls

* Tap player → start / pass turn
* Long press + drag → reorder players
* Pause button → stop timer, keep current player active
* Settings → configure time, names, player count

## Tech

* Kotlin
* Jetpack Compose
* Material3
* MVVM
* Coroutines

## Project structure

```
app/
 ├── ui/
 ├── domain/
 ├── data/
 ├── MainActivity.kt
```

## Build

```
./gradlew build
```

## Run

Open in Android Studio and run on emulator or device.

## Tests

```
./gradlew test
```

## License

MIT
