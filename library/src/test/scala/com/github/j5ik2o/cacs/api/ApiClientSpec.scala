package com.github.j5ik2o.cacs.api

import akka.actor.ActorSystem
import akka.testkit.TestKit
import org.scalatest.{BeforeAndAfterAll, FreeSpecLike}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}

import scala.concurrent.duration._

class ApiClientSpec extends TestKit(ActorSystem("ApiClientSpec")) with FreeSpecLike with BeforeAndAfterAll with ScalaFutures {

  override implicit def patienceConfig: PatienceConfig =
    PatienceConfig(timeout = scaled(Span(10, Seconds)), interval = scaled(Span(1, Seconds)))

  override def beforeAll(): Unit = {
    super.beforeAll()

  }

  override def afterAll(): Unit = {
    super.afterAll()
    TestKit.shutdownActorSystem(system)
  }

  val apiClient = new ApiClient(ApiConfig("coincheck.com", 443, 3 seconds))

  import system.dispatcher

  "ApiClient" - {

    "getTicker" in {
      val result = apiClient.getTicker().futureValue
      println(result)
    }

    "getTrades" in {
      val result = apiClient.getTrades().futureValue
      println(result)
    }

    "getOrderBooks" in {
      val result = apiClient.getOrderBooks().futureValue
      println(result)
    }

    "getRate" in {
      val result = apiClient.getRate().futureValue
      println(result)
    }

    "getSaleRate" in {
      val result = apiClient.getSaleRate("btc_jpy").futureValue
      println(result)
    }

  }

}
