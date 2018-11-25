package zw.trevor.mailinator
import java.util.Date
import MailBox._

case class Email(val id: MailID, from: String, to: String, subject: String, body: String, received: Date)

abstract class MailBox(){
    //create a new email
    def create(from: String, subject: String, body: String): Int

    //fetch a particular email
    def fetch(id: MailID): Email

    //fetch multiple email addresses
    def fetchMultiple(page: Page): Iterable[Email]

    //destroy this mailbox and all emails
    def purge: Unit

    //delete a particular email
    def delete(id: MailID): Email

}

object MailBox{
    type MailID = Int
}