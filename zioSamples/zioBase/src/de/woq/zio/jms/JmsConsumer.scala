package de.woq.zio.jms

import javax.jms._
import zio._
import zio.blocking._
import zio.stream.ZStream

class JmsConsumer[R, A](consumer : MessageConsumer) {

  def consume(enrich : (Message, JmsConsumer[R, A]) => A) : ZStream[R with BlockingConnection, JMSException, A] =
    ZStream.repeatEffect(effectBlockingInterrupt(enrich(consumer.receive(), this)).refineOrDie { case e : JMSException => e })
}

object JmsConsumer {

  def make[R, A] (
    dest : DestinationFactory[R with BlockingConnection],
    transacted : Boolean = false,
    ackMode : Int = Session.AUTO_ACKNOWLEDGE
  ) : ZManaged[R with BlockingConnection, JMSException, JmsConsumer[R, A]] =
    for {
      con     <- ZIO.service[Connection].toManaged_
      session <- session(con, transacted, ackMode)
      d       <- dest(session).toManaged_
      mc      <- consumer(session, d)
    } yield (new JmsConsumer[R, A](mc))
}
