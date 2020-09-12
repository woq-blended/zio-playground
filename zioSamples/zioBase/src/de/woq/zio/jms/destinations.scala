package de.woq.zio.jms

import javax.jms.{Destination, JMSException, Session}
import zio._

case class Queue[R](name : String) extends DestinationFactory[R] {
  override def apply(s: Session): ZIO[R, JMSException, Destination] =
    Task(s.createQueue(name)).refineOrDie { case e: JMSException => e }
}

case class Topic[R](name : String) extends DestinationFactory[R] {
  override def apply(s: Session): ZIO[R, JMSException, Destination] =
    Task(s.createTopic(name)).refineOrDie { case e : JMSException => e}
}

case class TemporaryQueue[R]() extends DestinationFactory[R] {
  override def apply(s: Session): ZIO[R, JMSException, Destination] =
    Task(s.createTemporaryQueue()).refineOrDie { case e : JMSException => e}
}

case class TemporaryTopic[R]() extends DestinationFactory[R] {
  override def apply(s: Session): ZIO[R, JMSException, Destination] =
    Task(s.createTemporaryTopic()).refineOrDie { case e : JMSException => e}
}
