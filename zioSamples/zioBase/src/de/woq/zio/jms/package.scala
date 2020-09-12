package de.woq.zio

import javax.jms._
import zio._
import zio.blocking._

package object jms {

  type BlockingConnection = Blocking with Has[Connection]
  type DestinationFactory[R] = Session => ZIO[R, JMSException, Destination]
  type MessageFactory[T]  = (T, Session) => Message

  def connection(cf : ConnectionFactory, creds : Option[(String, String)] = None) : ZManaged[BlockingConnection, JMSException, Connection] = {

    val acquire : ZIO[BlockingConnection, JMSException, Connection] = effectBlockingInterrupt {
      val connection = creds match {
        case None => cf.createConnection()
        case Some((u,p)) => cf.createConnection(u,p)
      }
      connection.start()
      connection
    }.refineOrDie[JMSException]{ case e : JMSException => e }

    val release : Connection => ZIO[BlockingConnection, Nothing, Any] = c => Task(c.close()).ignore

    Managed.make(acquire)(release)
  }

  def session(con : Connection, transacted: Boolean, ackMode : Int) : ZManaged[BlockingConnection, JMSException, Session] = {

    val acquire : ZIO[Blocking, JMSException, Session] = effectBlockingInterrupt {
      con.createSession(transacted, ackMode)
    }.refineOrDie[JMSException]{ case e : JMSException => e }

    val release : Session => ZIO[BlockingConnection, Nothing, Any] = s => Task(s.close()).ignore

    Managed.make(acquire)(release)
  }

  def producer(session : Session) : ZManaged[BlockingConnection, JMSException, MessageProducer] = {
    val acquire : ZIO[BlockingConnection, JMSException, MessageProducer] = effectBlockingInterrupt {
      session.createProducer(null)
    }.refineOrDie[JMSException]{ case e : JMSException => e }

    val release : MessageProducer => ZIO[BlockingConnection, Nothing, Any] = p => Task(p.close()).ignore

    Managed.make(acquire)(release)
  }

  def consumer(session : Session, dest : Destination) : ZManaged[BlockingConnection, JMSException, MessageConsumer] = {

    val acquire : ZIO[BlockingConnection, JMSException, MessageConsumer] = effectBlockingInterrupt {
      session.createConsumer(dest)
    }.refineOrDie[JMSException]{ case e : JMSException => e }

    val release : MessageConsumer => ZIO[BlockingConnection, Nothing, Any] = c => Task(c.close()).ignore
    Managed.make(acquire)(release)
  }
}
