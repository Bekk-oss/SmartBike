# Smart Bike Using Regenerative Energy Harvesting — README

A capstone project that turns a conventional bicycle into a sensor-rich **smart bike** powered by **regenerative energy harvesting**. The system monitors speed, inclination, rider vitals, rear-vehicle proximity, and GPS location; streams data to an Android app over Bluetooth; and uses turn-signal LEDs for safer riding.&#x20;

---

## Table of Contents

* [Overview](#overview)
* [Core Features](#core-features)
* [System Architecture](#system-architecture)
* [Hardware](#hardware)
* [Software](#software)
* [Getting Started](#getting-started)
* [Building & Flashing](#building--flashing)
* [Testing](#testing)
* [Performance Highlights](#performance-highlights)
* [Project Structure](#project-structure)
* [Bill of Materials (Summary)](#bill-of-materials-summary)
* [Known Limitations & Future Work](#known-limitations--future-work)
* [License](#license)

---

## Overview

This project integrates an **Energy Harvesting System (EHS)**, an Arduino-based sensor suite, GPS, Bluetooth, and an Android companion app to create a holistic, sustainable cycling platform. The bicycle harvests kinetic energy from the rear wheel to charge an onboard 6 V SLA battery while supplying stable 3.3 V/5 V rails to electronics. Collected telemetry is sent via Bluetooth (HC-05) to an Android app for **real-time visualization and navigation**.&#x20;

---

## Core Features

* **Regenerative power** using a 12 V DC motor + gearing, buck/boost regulation, and an Arduino-controlled charge/drive board.&#x20;
* **Safety & awareness:** rear proximity alerts (3× HC-SR04), 40-pixel WS2812B LED **turn signals**, tilt/roll alerts (MPU6050).&#x20;
* **Rider vitals:** pulse sensor (BPM), ambient **temperature** (TMP36G).&#x20;
* **Speed & RPM:** hall-effect (A3144) with wheel magnet and interrupt-driven counting.&#x20;
* **Connectivity:** HC-05 Bluetooth SPP link to Android; simultaneous phone-to-speaker pairing supported.&#x20;
* **Navigation:** in-app live map using phone location with route planning and Google Assistant voice directions. (NEO-6M tested but phone GPS adopted for downtown reliability.)&#x20;

---

## System Architecture

**Mechanical/Electrical**

* Rear wheel → gear train → 12 V DC motor (generator) → boost (XL6009) → buck (LM2596) → 6 V SLA battery & regulated rails.
* Custom **EHS control PCB** (MOSFET switching via op-amp-buffered Arduino Nano) manages charge thresholds and “drive-assist” mode.&#x20;

**Embedded**

* **Arduino UNO** handles sensors, debounces turn-signal buttons, computes RPM/speed, and streams messages over UART→HC-05.&#x20;

**Mobile**

* **Android app** (Java/RxAndroid/Threads + Handler) pairs to HC-05, parses multiplexed sensor frames, renders UI tiles (speed, tilt, vitals, proximity), and launches map/navigation.&#x20;

---

## Hardware

* 12 V DC motor (Bühler 1.13.044.284.50), **6 V SLA battery** (Mighty Max ML4-6), **LM2596 buck**, **XL6009 boost** modules.
* **Arduino UNO**, **Arduino Nano** (on EHS PCB), **HC-05** BT module.
* Sensors: **MPU6050**, **HC-SR04 ×3**, **A3144**, **TMP36G**, **Pulse sensor**, **WS2812B 40-LED strip**.&#x20;

> CAD: motor/gear mounts designed in SolidWorks; EHS PCB in KiCad. Gearing/mounts were iterated for fit; final parts 3D-printed (PLA).&#x20;

---

## Software

### Firmware (Arduino)

* Libraries for NeoPixel, Pulse sensor, MPU6050, and TinyGPS++ (if using NEO-6M).
* Interrupt-based RPM counting; ultrasonic ranging (cm) with thresholded alert; BPM/temperature sampling; LED signaling patterns.&#x20;

### Android App

* Bluetooth permissions & pairing flow, **ConnectThread/ConnectedThread** socket handling, UI updates via **Handler**, multi-field parsing of serial frames.
* Map screen: live position markers + destination text field → open guided navigation with voice directions.&#x20;

---

## Getting Started

### Prerequisites

* **Hardware assembled** per schematics: motor+gears aligned; EHS PCB wired to battery, boost/buck, and Arduino; sensors mounted and labeled; HC-05 powered and level-shifted as needed.&#x20;
* **Android Studio** Flamingo+ (or compatible), **Arduino IDE** 2.x.

### Wiring (high level)

* HC-05: VCC→3.3–5 V per module, GND→GND, TXD↔RX (with divider if needed), RXD↔TX; EN→VCC for AT mode.&#x20;
* Sensors:

  * WS2812B → D9 (w/ 470 Ω inline), 5 V rail (ensure current budget).
  * A3144 → D2 interrupt (with 10 kΩ).
  * HC-SR04 ×3 → digital I/O (trig/echo),
  * TMP36G → A0, Pulse → A1,
  * MPU6050 → I²C (SCL/SDA).&#x20;

---

## Building & Flashing

1. **Firmware**

   * Open `arduino/SmartBike.ino` in Arduino IDE.
   * Install required libraries (Adafruit NeoPixel, MPU6050, PulseSensor, TinyGPS++ if used).
   * Disconnect HC-05 during upload; select correct board/port; **Upload**.&#x20;
2. **Android App**

   * Open `android/` in Android Studio.
   * Set min SDK and BT/location permissions in `AndroidManifest.xml`.
   * Build → Run on device (enable Developer Mode) or install release APK.&#x20;
3. **Pairing**

   * Phone Settings → Bluetooth → pair with `HC-05` (default pin often `1234`/`0000`).
   * Launch app → **Connect** → choose paired device → live data cards appear.&#x20;

---

## Testing

Replicate the project’s validation suite:

* **EHS**: motor rotation voltage check; converter chain set to \~7 V; PCB continuity and threshold tuning; on-bike charge test.&#x20;
* **Sensors**: gyro calibration & tilt, ultrasonic proximity (<100 cm triggers “Vehicle Behind!!”), turn-signal button hold, BPM on serial, hall-effect RPM→speed.&#x20;
* **Bluetooth**: loopback with Serial Bluetooth Terminal; simultaneous BT speaker + app.&#x20;
* **App**: map live tracking + route planning & voice directions; multi-device UI checks.&#x20;

---

## Performance Highlights

* **Charging threshold:** charging begins ≈ **12 km/h** wheel speed (motor ≥ \~3 V).
* **Generated current:** \~0.4 A at low speed up to **\~1.76 A at 50 km/h** (at motor rated speed).
* **Rails:** converters tuned near **7 V** for SLA charging (\~6.45 V nominal).&#x20;

**Sensor notes**

* MPU6050 responsive (≈0.2 s end-to-end with other tasks).
* HC-SR04 accurate to a few mm in range; default “no echo” treated as large distance.
* Pulse sensor may require per-user threshold tuning.
* Hall RPM reliable but coarser at low speeds; ensure correct interrupt pin (D2/D3).&#x20;

**GPS decision**

* NEO-6M worked outdoors but was inconsistent in dense downtown canyons; **phone GPS** (cellular-assisted) chosen for robust live tracking & navigation.&#x20;

---

## Project Structure

```
smart-bike/
├─ hardware/
│  ├─ cad/                 # SolidWorks gear & mount models
│  ├─ pcb/                 # KiCad EHS control board
│  └─ wiring/              # Connection diagrams & pinout
├─ arduino/
│  ├─ SmartBike.ino        # Main firmware
│  └─ libs/                # Library notes / versions
├─ android/
│  ├─ app/                 # Android Studio project
│  └─ docs/                # Permissions, pairing, APK
├─ tests/
│  ├─ procedures/          # Test plans T1.x–T5.x
│  └─ results/             # Logs, screenshots, plots
└─ docs/
   ├─ README.md            # This file
   └─ report.pdf           # Full capstone report
```

---

## Bill of Materials (Summary)

* **EHS subtotal:** ≈ **\$132.42** (motor, SLA battery, converters, PCB parts, hardware).
* **Sensors subtotal:** ≈ **\$172.44** (ultrasonic×3, WS2812B strip, hall, temp, pulse, MPU6050, buttons, brackets).
* **Bluetooth/GPS/Other:** HC-05 + UNO boards ≈ **\$60.17**; GPS options ≈ **\$52.55**; phone mount & speaker ≈ **\$115.97**.
* **Total project parts:** **≈ \$533.55** (bike + phone not included).&#x20;

---

## Known Limitations & Future Work

* **Electrical safety:** add fusing, keyed connectors, and heatsinks on high-current FETs. Mis-wiring once damaged the board; protective elements would mitigate this.&#x20;
* **Mechanical durability:** replace **PLA gears** with metal/stronger polymer for long-term wear resistance.&#x20;
* **App/UX:** reduce button-press latency (debounce/ISR redesign or offload to secondary MCU), refine multi-sensor parsing, add background logging & export.&#x20;
* **Sensing:** per-user BPM auto-calibration; improve hall RPM at very low speeds (Schmitt trigger or magnetic encoder).&#x20;
* **Power:** explore supercap buffer, MPPT-style control for generator, and telemetry of battery SoC on UI.&#x20;

---

## License

Academic/educational use. See `LICENSE` (or add one appropriate to your institution).

---

### Citation

Most details above—including architecture, components, tests, and measured results—are summarized from the project’s final report: *XF04: Smart Bike Using Regenerative Energy Harvesting* (2024).&#x20;

If you want this README tailored to your repo (folder names, code file names, screenshots), drop those in and I’ll align everything to match.
