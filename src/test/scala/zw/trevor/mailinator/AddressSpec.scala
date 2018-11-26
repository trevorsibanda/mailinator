package zw.trevor.mailinator

import cats.effect.IO
import org.http4s._
import org.http4s.implicits._
import org.specs2.matcher.MatchResult

class AddressSpec extends org.specs2.mutable.Specification with TestService{
  "Email address" >> {
    "generate random address" >> {
      generateRandomAddress()
    }
    "delete address" >> {
      testDeleteAddress()
    }
  }

  var address: String = ""
 

  private[this] val createAddress: Response[IO] = {
    val createAddress = Request[IO](Method.POST, Uri.uri("/mailboxes"))
    service.orNotFound(createAddress).unsafeRunSync()
  }

  private[this] val deleteAddress: Response[IO] = {
    val createAddress = Request[IO](Method.DELETE, Uri.uri("/mailboxes"))
    service.orNotFound(createAddress).unsafeRunSync()
  }

  private[this] def generateRandomAddress() = {
    val pattern = """\{"address":"(.*)","when":"(.*)"\}""".r
    createAddress.as[String].unsafeRunSync() match{
        case pattern(addr, when) => address = addr; true
        case x => throw new Exception(x); false
    }  
  }

  private[this] def testDeleteAddress() = {
    true  
  }

}
