package de.woq.zio.jms

import zio._
import javax.jms._
import zio.stream.ZSink

class JmsProducer[R, E >: JMSException, A](sender: A => ZIO[R, E, Message]) {
  def produce(in: A): ZIO[R, E, (A, Message)] = sender(in).map(msg => in -> msg)
}

object JmsProducer {

  def sink[R, E >: JMSException, A](
    destination: DestinationFactory[R with BlockingConnection],
    encoder: (A, Session) => ZIO[R, E, Message],
    transacted: Boolean = false,
    acknowledgementMode: Int = Session.AUTO_ACKNOWLEDGE
  ): ZSink[R with BlockingConnection, E, A, A, Unit] =
    ZSink.managed[R with BlockingConnection, E, A, JmsProducer[R, E, A], A, Unit](
      make(destination, encoder, transacted, acknowledgementMode)
    ) { jmsProducer =>
      ZSink.foreach(message => jmsProducer.produce(message))
    }

  def make[R, E >: JMSException, A](
    dest : DestinationFactory[R with BlockingConnection],
    encoder : (A, Session) => ZIO[R, E, Message],
    transacted : Boolean = false,
    ackMode : Int = Session.AUTO_ACKNOWLEDGE
  ) : ZManaged[R with BlockingConnection, JMSException, JmsProducer[R, E, A]] = {
    for {
      con     <- ZIO.service[Connection].toManaged_
      session <- session(con, transacted, ackMode)
      d       <- dest(session).toManaged_
      mp      <- producer(session)
    } yield (
      new JmsProducer[R,E,A](
        msg => encoder(msg, session).map { enc =>
          mp.send(d, enc)
          enc
        }
      )
    )
  }
}