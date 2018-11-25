package zw.trevor.mailinator

import scala.collection.mutable.{HashMap, SynchronizedMap, ArrayBuffer}
import scala.util.{Try, Success, Failure}

case class Page(cursor: Int, count: Int)
case class Cursor(prev: Int, next: Option[Int])
case class PageResult[A](cursor: Cursor, count: Int, results: Iterable[A])

trait IDGenerator[A]{
    def generate: A
}

trait Store[K, V]{
    def get(k: K): Option[V]
    def put(k: K, v: V): Option[K]
    def remove(k: K): Option[V]
}

trait Table[K, V] extends Store[K, V]{
    //synchronized map for thread safe access
    private[mailinator] val table: HashMap[K, V] = new HashMap[K, V] with SynchronizedMap[K, V]
    //ID Generator for sequential keys
    val idGen: IDGenerator[K]

    def get(k: K): Option[V] = table.get(k)
    def put(k: K, v: V): Option[K] = {
        this.put(k, v)
        Some(k)
    }
    def put(v: V): Option[K] = {
        val k = idGen.generate
        this.put(k, v)
    }
    def remove(k: K): Option[V] = table.remove(k)
    def keysIterator = table.keysIterator
    def valuesIterator = table.valuesIterator
}

trait FIFO[V] extends Store[Int, V]{
    val entries: ArrayBuffer[V] = new ArrayBuffer[V]()
    def get(k: Int): Option[V] = Try{ entries(k)} match{
        case Success(v) => Some(v)
        case Failure(_) => None
    }
    def put(k: Int, v: V): Option[Int] = ???

    def put(v: V): Option[Int] = {
        entries.append(v)
        Some(entries.length)
    }
    def remove(k: Int): Option[V] = {
        val v = get(k)
        entries.remove(k)
        v
    }
}

trait PageReader[V]{
    //implement this to read a page
    def read(page: Page): PageResult[V] 
    //Id[Page]
    def page(cursor: Int, count: Int): Page = new Page(cursor, count)
    //Id[PageResult[V]]
    def emptyResult = new PageResult[V](Cursor(0, None), 0, Nil)
    
    //take n items satisfying pred
    def takeNWhile[A](iter: Iterator[A], n: Int)(pred: A => Boolean): Iterable[A] = {
        def run(res: Iterable[A], counter: Int): Iterable[A] = {
            if(counter == n || !iter.hasNext)
                res
            else{
                val curr = iter.next
                pred(curr) match{
                    case true =>  run(res ++ Iterable[A](curr), counter+1)
                    case false => run(res, counter)
                }
            }
        }
        run(Nil, 0)
    }
}
