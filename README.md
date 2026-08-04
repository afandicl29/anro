# ANRO

ANRO is a personal parental screen monitoring project built for learning and personal use.

## Overview

The project consists of three main components:

* **Android Child App**

  * Runs on the monitored Android device.
  * Maintains a secure connection with the server.
  * Receives remote screen-sharing requests.

* **Parent Web**

  * Browser-based interface for managing paired devices.
  * Displays available devices.
  * Initiates screen viewing sessions.

* **Fastify Server**

  * Device registration.
  * Device management.
  * WebSocket signaling.
  * Communication bridge between Parent and Child.

## Current Development Status

### Completed

* Fastify server setup
* Device registration
* Device discovery API
* WebSocket signaling
* Parent Web device list
* Parent → Child screen request flow
* Android foreground connection service
* ScreenCaptureService integration (initial stage)

### In Progress

* MediaProjection permission flow
* WebRTC signaling
* Screen streaming
* Parent Web live viewer

## Technology Stack

### Backend

* Fastify
* WebSocket
* Node.js

### Android

* Kotlin
* Android Foreground Service
* MediaProjection (in progress)

### Web

* HTML
* CSS
* JavaScript

## Project Structure

```text
ANRO
├── child/          # Android Child application
├── server/         # Fastify backend
└── docs/           # Documentation (optional)
```

## Development Roadmap

* [x] Device registration
* [x] Device management
* [x] Parent Web dashboard
* [x] WebSocket signaling
* [x] Screen request command
* [x] MediaProjection
* [x] WebRTC connection
* [x] Live screen streaming
* [x] Connection stability improvements

## Disclaimer

This project is developed for educational purposes and personal device management. It should only be used on devices that you own or for which you have explicit authorization.
