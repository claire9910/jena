<!--
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
-->

<template>
  <div>
    <span>server status</span>
    <a class="d-inline-block" href="#" data-bs-toggle="tooltip" :title="serverStatusText">
      <svg id="status-icon" class="ms-2">
        <circle cx="1em" cy="1em" r="1em" stroke="transparent" stroke-width="0" fill="transparent" :class="isUp" />
      </svg>
    </a>
  </div>
</template>

<script>
// eslint-disable-next-line no-unused-vars
import ServerStatus from '@/model/server.status'

export default {
  name: 'ServerStatus',

  data () {
    return {
      /**
       * @type {ServerStatus}
       */
      serverStatus: null
    }
  },

  computed: {
    isUp () {
      if (this.serverStatus && this.serverStatus.online) {
        return {
          success: true
        }
      }
      return {
        failure: true
      }
    },
    serverStatusText () {
      if (this.serverStatus) {
        return this.serverStatus.message
      }
      return ''
    }
  },

  async created () {
    this.serverStatus = await this.$fusekiService.getServerStatus()
    this.interval = setInterval(async () => {
      this.serverStatus = await this.$fusekiService.getServerStatus()
    }, 5000)
  },

  beforeDestroy () {
    clearInterval(this.interval)
    this.interval = null
  }
}
</script>
