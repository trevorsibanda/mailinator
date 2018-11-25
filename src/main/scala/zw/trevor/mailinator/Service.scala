package zw.trevor.mailinator

import cats.effect.Effect

import io.circe.Json
import io.circe.Json
import io.circe._
import io.circe.Encoder
import io.circe.parser._
import io.circe.syntax._
import io.circe.generic.auto._
import org.http4s.circe._
import cats.effect._

import org.http4s.HttpService
import org.http4s.dsl.Http4sDsl

import MailBox._

class Service[F[_]: Effect] extends Http4sDsl[F] {
  import Service._

  def stub = Ok("TODO")

  val service: HttpService[F] = {
    HttpService[F] {
      case GET    -> Root/ "mailboxes"/ email / "messages" / messageId => stub
      case GET    -> Root/ "mailboxes"/ email / "messages" / cursor / count => stub
      case GET    -> Root/ "mailboxes"/ email / "messages" => stub
      case POST   -> Root/ "mailboxes"   => stub
      case POST   -> Root/ "mailboxes"/ email / "messages" => stub
      case DELETE -> Root/ "mailboxes"/ email  => Ok(CreatedAddress("sibandatrevor@gmail.com").asJson)
      case DELETE -> Root/ "mailboxes"/ email / "messages" / messageId => stub
    }
  }
}


object Service{

  def now = java.util.Calendar.getInstance.getTime.toString
  case class CreatedAddress(address: Address, when: String = now)
  case class DestroyedAddress(address: Address, when: String = now)
  case class CreatedMail(id: MailID, when: String = now)
  case class DeletedMail(id: MailID, when: String = now)
  case class Err(error: String, reason: String, when: String = now)


  implicit val EmailEncoder: Encoder[Email] = Encoder.instance{email: Email =>
   Json.obj(
     "id" -> email.id.asJson,
     "from" -> email.from.asJson,
     "to" -> email.from.asJson,
     "subject" -> email.subject.asJson,
     "received" -> email.received.toString.asJson
   )
  }

  implicit val PageEncoder: Encoder[PageResult[Email]] = Encoder.instance{page: PageResult[Email] =>
   Json.obj(
     "cursor" -> Json.obj(
       "prev" -> page.cursor.prev.asJson,
       "next" ->page.cursor.next.asJson
     ),
     "count" -> page.count.asJson,
     "results" -> page.results.asJson
   )
  }  
}
