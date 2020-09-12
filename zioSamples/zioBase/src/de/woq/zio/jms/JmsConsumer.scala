package de.woq.zio.jms

import javax.jms._
import zio._
import zio.blocking._
import zio.stream.ZStream

class JmsConsumer[A](consumer : MessageConsumer) {

  def consume(enrich : (Message, JmsConsumer[A]) => A) : ZStream[Blocking, JMSException, A] =
    ZStream.repeatEffect(effectBlockingInterrupt(enrich(consumer.receive(), this)).refineOrDie { case e : JMSException => e })
}

object JmsConsumer {

  def make[A] (
    dest : DestinationFactory,
    transacted : Boolean = false,
    ackMode : Int = Session.AUTO_ACKNOWLEDGE
  ) : ZManaged[BlockingConnection, JMSException, JmsConsumer[A]] =
    for {
      con     <- ZIO.service[Connection].toManaged_
      session <- session(con, transacted, ackMode)
      d       <- dest(session).toManaged_
      mc      <- consumer(session, d)
    } yield (new JmsConsumer[A](mc))
}
