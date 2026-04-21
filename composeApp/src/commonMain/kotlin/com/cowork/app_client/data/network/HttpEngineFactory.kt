package com.cowork.app_client.data.network

import io.ktor.client.engine.HttpClientEngine

expect fun createHttpEngine(): HttpClientEngine
