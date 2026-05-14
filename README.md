# BrightDate Android Widget

![https://github.com/Digital-Defiance/brightdate-android-widget/blob/main/brightdate-feature-graphic.png](https://github.com/Digital-Defiance/brightdate-android-widget/blob/main/brightdate-feature-graphic.png)

A minimal Android home-screen widget that displays the current
[BrightDate](https://brightdate.org) value — a single scalar
count of SI days since the J2000.0 astronomical epoch.

```
BRIGHTDATE
9627.47168
```

## Layout

- `app/` — single-module Android app, Kotlin, AGP 8.7 / Kotlin 2.0, minSdk 26.
- `brightdate/` — symlink to the upstream BrightDate reference repo (read-only;
  used here only as documentation for the J2000.0 / TAI math).

## Build

This project does not commit the Gradle wrapper jar. Generate it once:

```bash
gradle wrapper --gradle-version 8.10.2
```

Then open the project in Android Studio (Koala / Ladybug or newer) and run the
`app` configuration, or from the command line:

```bash
./gradlew :app:assembleDebug
./gradlew :app:installDebug
```

After install, long-press the home screen → Widgets → BrightDate Widget.

## Math

BrightDate is computed exactly as the upstream library defines it
(`app/src/main/java/org/brightchain/brightdate/widget/BrightDate.kt`):

```
brightdate = (taiUnixSeconds − 946_727_967.816) / 86_400
taiUnixSeconds = unixSeconds + (TAI − UTC)
```

The TAI−UTC offset is hard-coded to **37 seconds** (the value in force since
2017-01-01). If the IERS announces a future leap second, bump
`TAI_MINUS_UTC_SECONDS` in `BrightDate.kt`.

## Update cadence

The Android `AppWidgetProviderInfo.updatePeriodMillis` minimum is 30 minutes,
which is unusably slow for a live time display. We instead run a repeating
inexact `AlarmManager` tick (default **30 s**) while at least one widget
instance exists, re-armed in `onUpdate`, `onEnabled`, and `BootReceiver`.

5 fractional digits = ~0.864 s resolution, so the last digit or two will visibly
jump between updates. Lower the interval in `BrightDateWidgetProvider.UPDATE_INTERVAL_MS`
if you want smoother ticks at the cost of battery.
