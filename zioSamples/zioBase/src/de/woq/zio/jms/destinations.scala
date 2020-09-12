package de.woq.zio.jms

import javax.jms.{Destination, JMSException, Session}
import zio._

case class JmsQueue(name : String) extends DestinationFactory {
  override def apply(s: Session): ZIO[Any, JMSException, Destination] =
    Task(s.createQueue(name)).refineOrDie { case e: JMSException => e }
}

case class JmsTopic(name : String) extends DestinationFactory {
  override def apply(s: Session): ZIO[Any, JMSException, Destination] =
    Task(s.createTopic(name)).refineOrDie { case e : JMSException => e}
}

case class TemporaryQueue() extends DestinationFactory {
  override def apply(s: Session): ZIO[Any, JMSException, Destination] =
    Task(s.createTemporaryQueue()).refineOrDie { case e : JMSException => e}
}

case class TemporaryTopic() extends DestinationFactory {
  override def apply(s: Session): ZIO[Any, JMSException, Destination] =
    Task(s.createTemporaryTopic()).refineOrDie { case e : JMSException => e}
}
