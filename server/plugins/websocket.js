'use strict'

const fp = require('fastify-plugin')
const websocket = require('@fastify/websocket')

module.exports = fp(async function (fastify) {
  await fastify.register(websocket)
})
