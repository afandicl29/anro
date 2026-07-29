'use strict'

module.exports = async function (fastify, opts) {

  fastify.get('/api/config', async function (request, reply) {

    return {
      appName: 'ANRO',
      version: '1.0.0',
      websocket: '/ws',
      stun: [
        'stun:stun.l.google.com:19302'
      ]
    }

  })

}
