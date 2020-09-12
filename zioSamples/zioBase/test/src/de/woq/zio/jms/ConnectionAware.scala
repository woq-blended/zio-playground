package de.woq.zio.jms

import javax.jms.{Connection, ConnectionFactory, JMSException}
import org.apache.activemq.ActiveMQConnectionFactory
import org.apache.activemq.broker.BrokerService
import zio._
import zio.blocking.Blocking

trait ConnectionAware {

  val brokerName : String = "zio"
  val connectionFactory : ConnectionFactory = new ActiveMQConnectionFactory(s"vm://$brokerName?create=false")
  val amqConnection : ZManaged[Blocking, JMSException, Connection] = connection(connectionFactory)

  val brokerService : ZIO[Any, Throwable, BrokerService] = Task {
    val b : BrokerService = new BrokerService()
    b.setBrokerName(brokerName)
    b.setUseJmx(false)
    b.setPersistent(false)
    b.setUseShutdownHook(true)
    b.start()
    b
  }

  val stopBroker : BrokerService => ZIO[Any, Nothing, Unit] = broker => UIO(broker.stop())
}
