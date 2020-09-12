package de.woq.zio.jms

import zio._
import javax.jms._
import zio.stream.ZSink

class JmsProducer[E >: JMSException, A](sender: A => ZIO[BlockingConnection, E, Message]) {
  def produce(in: A): ZIO[BlockingConnection, E, (A, Message)] = sender(in).map(msg => in -> msg)
}

object JmsProducer {

  def sink[E >: JMSException, A](
    destination: DestinationFactory,
    encoder: (A, Session) => ZIO[Any, E, Message],
    transacted: Boolean = false,
    acknowledgementMode: Int = Session.AUTO_ACKNOWLEDGE
  ): ZSink[BlockingConnection, E, A, A, Unit] =
    ZSink.managed[BlockingConnection, E, A, JmsProducer[E, A], A, Unit](
      make(destination, encoder, transacted, acknowledgementMode)
    ) { jmsProducer =>
      ZSink.foreach(message => jmsProducer.produce(message))
    }

  def make[Blocking, E >: JMSException, A](
    dest : DestinationFactory,
    encoder : (A, Session) => ZIO[Any, E, Message],
    transacted : Boolean = false,
    ackMode : Int = Session.AUTO_ACKNOWLEDGE
  ) : ZManaged[BlockingConnection, JMSException, JmsProducer[E, A]] = {
    for {
      con     <- ZIO.service[Connection].toManaged_
      session <- session(con, transacted, ackMode)
      d       <- dest(session).toManaged_
      mp      <- producer(session)
    } yield (
      new JmsProducer[E,A](
        msg => encoder(msg, session).map { enc =>
          mp.send(d, enc)
          enc
        }
      )
    )
  }
}