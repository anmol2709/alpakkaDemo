package elasticSearchAlpakka

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import akka.stream.alpakka.elasticsearch.WriteMessage
import akka.stream.alpakka.elasticsearch.scaladsl.ElasticsearchSink
import akka.stream.scaladsl.{Flow, Source}
import org.apache.http.HttpHost
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}
import org.elasticsearch.client.RestClient
import play.api.libs.json.Json
import spray.json.DefaultJsonProtocol.jsonFormat2
import spray.json.DefaultJsonProtocol._
import spray.json.JsonFormat

object KafkaToEs extends App {
  implicit val system = ActorSystem.create()

  implicit val mat = ActorMaterializer()
  implicit val _ = Json.format[Company]

  implicit val client: RestClient = RestClient.builder(new HttpHost("localhost", 9200)).build()


  implicit val format: JsonFormat[Company] = jsonFormat2(Company)


  val intermediateFlow = Flow[ConsumerRecord[Array[Byte], String]].map { kafkaMessage =>

    // Parsing the record as Company Object
    val company = Json.parse(kafkaMessage.value()).as[Company]
    val loc = company.location


    // Transform message so that we can write to elastic

    WriteMessage.createIndexMessage(loc, company)
  }


  val consumerSettings = ConsumerSettings(system, new ByteArrayDeserializer, new StringDeserializer)
    .withBootstrapServers("localhost:9092")
    .withGroupId("akka-stream-kafka-test")
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  val kafkaSource: Source[ConsumerRecord[Array[Byte], String], Consumer.Control] =
    Consumer.plainSource(consumerSettings, Subscriptions.topics("topic221"))


  val esSink = ElasticsearchSink.create[Company](
    indexName = "sink1",
    typeName = "company"
  )


  kafkaSource
    .via(intermediateFlow)
    .runWith(esSink)
}
