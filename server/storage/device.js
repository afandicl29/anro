'use strict'

const devices = new Map()

function register(device) {
  device.lastSeen = Date.now()
  device.status = 'online'

  devices.set(device.deviceId, device)

  return device
}

function get(deviceId) {
  return devices.get(deviceId)
}

function getAll() {
  return Array.from(devices.values())
}

function remove(deviceId) {
  return devices.delete(deviceId)
}

module.exports = {
  register,
  get,
  getAll,
  remove
}
