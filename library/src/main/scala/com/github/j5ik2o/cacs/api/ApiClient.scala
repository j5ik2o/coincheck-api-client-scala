package com.github.j5ik2o.cacs.api

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import com.github.j5ik2o.cacs.model._
import io.circe.{Decoder, Json}
import io.circe.parser._

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}

case class JsonParsingException(message: String) extends Exception(message)

case class JsonDecodingException(message: String) extends Exception(message)

class ApiClient(config: ApiConfig)(implicit system: ActorSystem) {

  import io.circe.generic.auto._

  private implicit val materializer = ActorMaterializer()
  private val poolClientFlow = Http().cachedHostConnectionPoolHttps[Int](config.host, config.port)
  private val timeout: FiniteDuration = config.timeoutForToStrict

  private def toJson(jsonString: String): Future[Json] = {
    parse(jsonString) match {
      case Right(r) => Future.successful(r)
      case Left(error) => Future.failed(JsonParsingException(error.message))
    }
  }

  private def toModel[A](json: Json)(implicit d: Decoder[A]): Future[A] = {
    json.as[A] match {
      case Right(r) => Future.successful(r)
      case Left(error) => Future.failed(JsonDecodingException(error.message))
    }
  }

  private def responseToModel[A](responseFuture: Future[HttpResponse])(implicit d: Decoder[A], ec: ExecutionContext): Future[A] = {
    for {
      httpResponse <- responseFuture
      httpEntity <- httpResponse.entity.toStrict(timeout)
      json <- toJson(httpEntity.data.utf8String)
      model <- toModel(json)
    } yield model
  }

  private def responseToModelEither[A](responseFuture: Future[HttpResponse])(implicit d: Decoder[A], ec: ExecutionContext): Future[Either[ResponseFailure, A]] = {
    for {
      httpResponse <- responseFuture
      httpEntity <- httpResponse.entity.toStrict(timeout)
      json <- toJson(httpEntity.data.utf8String)
      model <- if (json.hcursor.downField("success").as[Boolean].right.get)
        toModel[A](json).map(Right(_)) else toModel[ResponseFailure](json).map(Left(_))
    } yield model
  }

  def getTicker()(implicit ec: ExecutionContext): Future[Ticker] = {
    val url = "/api/ticker"
    val responseFuture = Source.single(HttpRequest(uri = url) -> 1).via(poolClientFlow).runWith(Sink.head)
    responseFuture.flatMap {
      case (triedResponse, _) =>
        responseToModel[Ticker](Future.fromTry(triedResponse))
    }
  }

  def getTrades()(implicit ec: ExecutionContext): Future[Either[ResponseFailure, Trades]] = {
    val url = "/api/trades?pair=btc_jpy"
    val responseFuture = Source.single(HttpRequest(uri = url) -> 1).via(poolClientFlow).runWith(Sink.head)
    responseFuture.flatMap {
      case (triedResponse, _) =>
        responseToModelEither[Trades](Future.fromTry(triedResponse))
    }
  }

  def getOrderBooks()(implicit ec: ExecutionContext): Future[OrderBooks] = {
    val url = "/api/order_books"
    val responseFuture = Source.single(HttpRequest(uri = url) -> 1).via(poolClientFlow).runWith(Sink.head)
    responseFuture.flatMap {
      case (triedResponse, _) =>
        responseToModel[OrderBooks](Future.fromTry(triedResponse))
    }
  }

  def getRate()(implicit ec: ExecutionContext): Future[Either[ResponseFailure, Rate]] = {
    val url = "/api/exchange/orders/rate"
    val responseFuture = Source.single(HttpRequest(uri = url) -> 1).via(poolClientFlow).runWith(Sink.head)
    responseFuture.flatMap {
      case (triedResponse, _) =>
        responseToModelEither[Rate](Future.fromTry(triedResponse))
    }
  }

  def getSaleRate(pair: String)(implicit ec: ExecutionContext): Future[SaleRate] = {
    val url = s"/api/rate/$pair"
    val responseFuture = Source.single(HttpRequest(uri = url) -> 1).via(poolClientFlow).runWith(Sink.head)
    responseFuture.flatMap {
      case (triedResponse, _) =>
        responseToModel[SaleRate](Future.fromTry(triedResponse))
    }
  }

}
