package com.github.j5ik2o.cacs.api

import scala.concurrent.duration.FiniteDuration

case class ApiConfig(host: String, port: Int = 443, timeoutForToStrict: FiniteDuration)
