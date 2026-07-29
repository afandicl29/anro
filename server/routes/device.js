'use strict'

const deviceStore = require('../storage/device')

module.exports = async function (fastify, opts) {

  // Register Device
  fastify.post('/api/device/register', async function (request, reply) {

    const {
      deviceId,
      deviceName,
      androidVersion,
      appVersion
    } = request.body

    if (!deviceId || !deviceName) {
      return reply.code(400).send({
        success: false,
        message: 'deviceId and deviceName required'
      })
    }

    const device = {
      deviceId,
      deviceName,
      androidVersion,
      appVersion
    }

    const savedDevice = deviceStore.register(device)

    return {
      success: true,
      message: 'Device registered',
      device: savedDevice
    }
  })


  // List Device
  fastify.get('/api/device/list', async function () {

    const devices = deviceStore.getAll()

    return {
      total: devices.length,
      devices
    }

  })

}

