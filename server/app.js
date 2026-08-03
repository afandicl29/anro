'use strict'

const path = require('node:path')
const AutoLoad = require('@fastify/autoload')
const fastifyStatic = require('@fastify/static')

const options = {}

module.exports = async function (fastify, opts) {


  // Static Parent Web
  fastify.register(fastifyStatic, {
    root: path.join(__dirname, 'public'),
    prefix: '/'
  })


  // Load plugins
  fastify.register(AutoLoad, {
    dir: path.join(__dirname, 'plugins'),
    options: Object.assign({}, opts)
  })


  // Load routes
  fastify.register(AutoLoad, {
    dir: path.join(__dirname, 'routes'),
    options: Object.assign({}, opts)
  })

}

module.exports.options = options
