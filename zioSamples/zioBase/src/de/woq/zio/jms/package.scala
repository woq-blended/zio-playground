package de.woq.zio

import javax.jms._
import zio._
import zio.blocking._

package object jms {

  type BlockingConnection = Blocking with Has[Connection]
  type DestinationFactory = Session => ZIO[Any, JMSException, Destination]
  type MessageFactory[T]  = (T, Session) => Message

  def connection(cf : ConnectionFactory, creds : Option[(String, String)] = None) : ZManaged[Blocking, JMSException, Connection] = {

    val acquire : ZIO[Blocking, JMSException, Connection] = effectBlockingInterrupt {
      val connection = creds match {
        case None => cf.createConnection()
        case Some((u,p)) => cf.createConnection(u,p)
      }
      connection.start()
      connection
    }.refineOrDie[JMSException]{ case e : JMSException => e }

    val release : Connection => ZIO[Blocking, Nothing, Any] = c => Task(c.close()).ignore

    Managed.make(acquire)(release)
  }

  def session(con : Connection, transacted: Boolean, ackMode : Int) : ZManaged[Blocking, JMSException, Session] = {

    val acquire : ZIO[Blocking, JMSException, Session] = effectBlockingInterrupt {
      con.createSession(transacted, ackMode)
    }.refineOrDie[JMSException]{ case e : JMSException => e }

    val release : Session => ZIO[Blocking, Nothing, Any] = s => Task(s.close()).ignore

    Managed.make(acquire)(release)
  }

  def producer(session : Session) : ZManaged[Blocking, JMSException, MessageProducer] = {
    val acquire : ZIO[Blocking, JMSException, MessageProducer] = effectBlockingInterrupt {
      session.createProducer(null)
    }.refineOrDie[JMSException]{ case e : JMSException => e }

    val release : MessageProducer => ZIO[Blocking, Nothing, Any] = p => Task(p.close()).ignore

    Managed.make(acquire)(release)
  }

  def consumer(session : Session, dest : Destination) : ZManaged[Blocking, JMSException, MessageConsumer] = {

    val acquire : ZIO[Blocking, JMSException, MessageConsumer] = effectBlockingInterrupt {
      session.createConsumer(dest)
    }.refineOrDie[JMSException]{ case e : JMSException => e }

    val release : MessageConsumer => ZIO[Blocking, Nothing, Any] = c => Task(c.close()).ignore
    Managed.make(acquire)(release)
  }

  def onlyText: PartialFunction[Message, String] = {
    case text: TextMessage => text.getText
  }
}
