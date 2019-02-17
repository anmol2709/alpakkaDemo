package kafkaAlpakka

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, ProducerSettings, Subscriptions, scaladsl}
import akka.stream.{ActorMaterializer, Materializer}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

object KafkaToKafka extends App{
    println("Hello World")

    implicit val system: ActorSystem = ActorSystem("consumer-sample")
    implicit val materializer: Materializer = ActorMaterializer()

    val consumerSettings =
      ConsumerSettings(system, new StringDeserializer, new StringDeserializer).withBootstrapServers("localhost:9092")
        .withGroupId("akka-stream-kafka-test")
        .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")


  val producerSettings =
    ProducerSettings(system, new StringSerializer, new StringSerializer)
      .withBootstrapServers("localhost:9092")

    val done = Consumer
      .plainSource(consumerSettings, Subscriptions.topics("topic1010"))
      .map(record => new ProducerRecord[String, String]("topic1030",  record.value()))
      .runWith(scaladsl.Producer.plainSink(producerSettings))

    implicit val ec: ExecutionContextExecutor = system.dispatcher
    done onComplete  {
      case Success(_) => println("Done"); system.terminate()
      case Failure(err) => println(err.toString); system.terminate()
    }
  }
