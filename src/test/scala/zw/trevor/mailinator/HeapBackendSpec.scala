package zw.trevor.mailinator

import cats.effect.IO
import org.http4s._
import org.http4s.implicits._
import org.specs2.matcher.MatchResult

class HeapBackendSpec extends org.specs2.mutable.Specification {
  "Heap Backend" >> {
    "create MailBox" >> {
      stub
    }
    "destroy MailBox" >> {
      stub
    }
    "create Email" >> {
      stub
    }
    "delete Email" >> {
      stub
    }
    "read Email" >> {
      stub
    }
    "read Next Cursor" >> {
      stub
    }
    "read Previous Cursor" >> {
      stub
    }
    "Quick test eviction policy" >> {
      stub
    }
  }

  def stub = {
      true
  }

}