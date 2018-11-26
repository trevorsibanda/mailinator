package zw.trevor.mailinator

import cats.effect.Effect

import org.http4s.EntityDecoder
import io.circe.Json
import io.circe._
import io.circe.Encoder
import io.circe.parser._
import io.circe.syntax._
import io.circe.generic.auto._
import org.http4s.circe._
import org.http4s.dsl.io._
import cats.effect._

import org.http4s.HttpService
import org.http4s.dsl.Http4sDsl

import MailBox._

class Service[F[_]: Effect] extends Http4sDsl[F] {
  import Service._

  def stub = Ok("TODO")

  implicit val CreateEmailDecoder = jsonOf[F, CreateNewEmail]

  val service: HttpService[F] = {
    HttpService[F] {
      case GET-> Root/"mailboxes"/email/"messages"/IntVar(messageId) => 
             getMessage(email, messageId).getOrElse(Ok(Err("get_mail", "Mailbox or message does not exist").asJson))
      
      case GET-> Root/"mailboxes"/email/"messages"/IntVar(cursor)/IntVar(count) => 
             getMessages(email, cursor.toInt, count).getOrElse(Ok(Err("get_mail", "Mailbox does not exist").asJson))
      
      case GET-> Root/"mailboxes"/email/"messages" => 
             getMessages(email, 0, 10).getOrElse(Ok(Err("get_mail", "Mailbox does not exist").asJson))
      
      case POST-> Root/"mailboxes" => 
             generateAddress.getOrElse(Ok(Err("create_address", "Failed to create mailbox").asJson))
      
      case req @ POST-> Root/"mailboxes"/address/"messages" => 
            req.decode[CreateNewEmail]{ cne =>
              createMessage(address, cne).getOrElse(Ok(Err("create_mail", "Address does not exist").asJson))
            } 

      case DELETE -> Root/"mailboxes"/email => 
             deleteAddress(email).getOrElse(Ok(Err("delete_address", "Mailbox does not exist").asJson)) 
      
      case DELETE -> Root/"mailboxes"/email/"messages"/IntVar(messageId) => 
             deleteMessage(email, messageId).getOrElse(Ok(Err("delete_mail", "Mailbox or message does not exist").asJson))
    }
  }


  val postOffice = new HeapBackend()
  
  def getMessage(addr: Address, id: MailID) = for{
    mail <- postOffice.fetch(addr)(id)
  } yield Ok(mail.asJson)

  def getMessages(addr: Address, cursor: MailID, count: Int) =  for{
    page <- Some(Page(cursor, count))
    results <- postOffice.fetchMultiple(addr)(page) 
  } yield Ok(results.asJson)

  def generateAddress() = for {
    addr <- postOffice.createRandom
  } yield Ok(CreatedAddress(addr).asJson)

  def createMessage(addr: Address, cne: CreateNewEmail) = for{
    mbox <- postOffice.lookup(addr)
    mailId <- mbox.create(cne.from, cne.subject, cne.body)
  } yield Ok(CreatedMail(mailId).asJson)

  def deleteAddress(addr: Address) = for{
    addr <- Option(addr)
    mbox <- postOffice.lookup(addr)
    resp <- postOffice.destroyBox(addr)
  } yield Ok(DestroyedAddress(resp).asJson)

  def deleteMessage(addr: Address, id: MailID) = for{
    mbox <- postOffice.lookup(addr)
    resp <- mbox.delete(id)
  } yield Ok(DeletedMail(resp).asJson)

}


object Service{

  def now = java.util.Calendar.getInstance.getTime.toString
  case class CreatedAddress(address: Address, when: String = now)
  case class DestroyedAddress(address: Address, when: String = now)
  case class CreatedMail(id: MailID, when: String = now)
  case class DeletedMail(id: MailID, when: String = now)
  case class Err(error: String, reason: String, when: String = now)

  case class CreateNewEmail(from: Address, subject: String, body: String)


  implicit val EmailEncoder: Encoder[Email] = Encoder.instance{email: Email =>
   Json.obj(
     "id" -> email.id.asJson,
     "from" -> email.from.asJson,
     "to" -> email.to.asJson,
     "body" -> email.body.asJson,
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
