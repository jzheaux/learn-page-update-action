import * as core from '@actions/core'
import { run } from './main.js'

// Entry point for the action
run().catch(error => {
  core.setFailed(error.message)
})
