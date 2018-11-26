package zw.trevor.mailinator


import cats.effect.IO
import org.http4s._
import org.http4s.implicits._
import org.specs2.matcher.MatchResult


trait TestService{
    val service = TestService.service
}

object TestService{
    val service = new Service[IO].service
}