'use strict'

const clients = new Map()

module.exports = async function (fastify) {

  fastify.get('/ws', { websocket: true }, (socket) => {

    console.log('WebSocket connected')


    socket.on('message', message => {

      try {

        const data = JSON.parse(message.toString())

        console.log('WS:', data)


        // REGISTER DEVICE
        if (data.type === 'register') {

          clients.set(data.deviceId, {
            socket,
            role: data.role
          })

          socket.deviceId = data.deviceId

          socket.send(JSON.stringify({
            type: 'registered',
            deviceId: data.deviceId,
            role: data.role
          }))

          return
        }


        // FORWARD SIGNALING
        if (
          data.type === 'screen_request' ||
          data.type === 'webrtc_offer' ||
          data.type === 'webrtc_answer' ||
          data.type === 'ice_candidate'
        ) {

          const target = clients.get(data.target)

          if (target) {

            target.socket.send(
              JSON.stringify(data)
            )

          } else {

            socket.send(JSON.stringify({
              type: 'error',
              message: 'Target device offline'
            }))

          }

        }


      } catch (err) {

        console.error(
          'WS Error:',
          err
        )

      }

    })


    socket.on('close', () => {

      if (socket.deviceId) {

        clients.delete(socket.deviceId)

        console.log(
          'Disconnected:',
          socket.deviceId
        )

      }

    })

  })

}
