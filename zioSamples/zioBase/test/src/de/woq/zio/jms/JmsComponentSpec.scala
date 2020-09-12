package de.woq.zio.jms

import javax.jms._

import zio._
import zio.blocking.Blocking
import zio.duration._

import zio.test._
import zio.test.Assertion._
import zio.test.TestAspect._

object JmsComponentSpec extends DefaultRunnableSpec with ConnectionAware {

  val connectionLayer: ZLayer[Blocking, JMSException, BlockingConnection] =
    ZLayer.fromManaged(amqConnection).passthrough

  override def spec = {
    suite("JMS Components")(
      testM("Messages should be sent and received in order of sending") {
        checkM(Gen.listOf(Gen.anyString)) { messages =>
          val received = jmsObjects("JmsSpec-1").use {
            case (s, p, mc, d) =>
              ZIO.foreach(messages)(send(p, s, d, _)) *> ZIO.foreach((1 to messages.size).toList)(_ => receiveText(mc))
          }.provideCustomLayer(connectionLayer)
          assertM(received)(equalTo(messages))
        }
      }
    ) @@ timeout(3.minute) @@ timed @@ sequential @@ around(brokerService)(stopBroker)
  }

  private def receive(consumer: MessageConsumer): ZIO[Any, JMSException, Message] =
    Task(consumer.receive()).refineToOrDie

  private def receiveText(consumer: MessageConsumer): ZIO[Any, JMSException, String] =
    receive(consumer).map(onlyText orElse nullableMessage)

  private def send(p: MessageProducer, s: Session, d: Destination, message: String) =
    Task(p.send(d, s.createTextMessage(message)))

  private def nullableMessage[T <: AnyRef]: PartialFunction[Message, T] = {
    case null => null.asInstanceOf[T]
  }

  private def jmsObjects(
    dest: String,
    transacted: Boolean = false,
    acknowledgementMode: Int = Session.AUTO_ACKNOWLEDGE
  ): ZManaged[BlockingConnection, JMSException, (Session, MessageProducer, MessageConsumer, Destination)] =
    for {
      c  <- amqConnection
      s  <- session(c, transacted, acknowledgementMode)
      d  <- JmsQueue(dest)(s).toManaged_
      p  <- producer(s)
      mc <- consumer(s, d)
    } yield (s, p, mc, d)
}
