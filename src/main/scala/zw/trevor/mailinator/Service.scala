package zw.trevor.mailinator

import cats.effect.Effect
import io.circe.Json
import org.http4s.HttpService
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

import MailBox._

class Service[F[_]: Effect] extends Http4sDsl[F] {

  def stub = Ok("TODO")

  val service: HttpService[F] = {
    HttpService[F] {
      case GET    -> Root/ "mailboxes"/ email / "messages" / messageId => stub
      case GET    -> Root/ "mailboxes"/ email / "messages" / cursor / count => stub
      case GET    -> Root/ "mailboxes"/ email / "messages" => stub
      case POST   -> Root/ "mailboxes"   => stub
      case POST   -> Root/ "mailboxes"/ email / "messages" => stub
      case DELETE -> Root/ "mailboxes"/ email  => stub
      case DELETE -> Root/ "mailboxes"/ email / "messages" / messageId => stub
    }
  }
}
