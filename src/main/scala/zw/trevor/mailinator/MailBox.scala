package zw.trevor.mailinator
import java.util.Date
import MailBox._

case class Email(val id: MailID, from: String, to: String, subject: String, body: String, received: Date)

abstract class MailBox(val address: Address){
    //create a new email
    def create(from: Address, subject: String, body: String): Option[MailID]

    //fetch a particular email
    def fetch(id: MailID): Option[Email]

    //fetch multiple email addresses
    def fetchMultiple(page: Page): PageResult[Email]

    //destroy this mailbox and all emails
    def purge: Unit

    //delete a particular email
    def delete(id: MailID): Option[Email]

}

object MailBox{
    type MailID = Int
    type Address = String
}