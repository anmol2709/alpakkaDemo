

import java.util.UUID

import spray.json._
import DefaultJsonProtocol._
import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.elasticsearch.{ReadResult, WriteMessage, WriteResult}
import akka.stream.alpakka.elasticsearch.scaladsl.{ElasticsearchFlow, ElasticsearchSink, ElasticsearchSource}
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.fasterxml.jackson.annotation.ObjectIdGenerators.UUIDGenerator
import elasticSearchAlpakka.Company
import play.api.libs.json.Json

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object EsToEs extends App{
implicit val format: JsonFormat[Company] = jsonFormat2(Company)

import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient

implicit val client: RestClient = RestClient.builder(new HttpHost("localhost", 9200)).build()

// Implicit  implicit val _ = Json.format[Book]
// Actor System
implicit val system: ActorSystem = ActorSystem()

// Implicit Materializer
implicit val mat: ActorMaterializer = ActorMaterializer()

/*
  ElasticsearchSource
    .typed[Company](
    indexName = "sink1",
    typeName = "company",
    query = """{"match_all": {}}"""
  )
    .map { message: ReadResult[Company] =>
      WriteMessage.createIndexMessage(message.id, message.source)
    }
    .via(
      ElasticsearchFlow.create[Company](
        indexName = "sink2",
        typeName = "book"
      )

    ).runWith(Sink.ignore)
*/

  val f1 = ElasticsearchSource
    .typed[Company](
    indexName = "source",
    typeName = "_doc",
    query = """{"match_all": {}}"""
  )
    .map { message: ReadResult[Company] =>
      WriteMessage.createIndexMessage(message.id, message.source)
    }
    .runWith(
      ElasticsearchSink.create[Company](
        indexName = "sink2",
        typeName = "_doc"
      )
    )


}

/*
/* ElasticsearchSource
  .typed[Book](
  indexName = "sink8",
  typeName = "books",
  query = """{"match_all": {}}"""
)
  .map { message: ReadResult[Book] =>
    WriteMessage.createIndexMessage(message.id, message.source)
  }
  .via(
    ElasticsearchFlow.create[Book](
      indexName = "sink2",
      typeName = "book"
    )
  ).runWith(Sink.ignore)

*/
val requests = List[WriteMessage[Book, NotUsed]](
  WriteMessage.createIndexMessage(id = "00005", source = Book("Book 1")),
  WriteMessage.createUpsertMessage(id = "00006", source = Book("Book 2")),
  WriteMessage.createUpsertMessage(id = "00007", source = Book("Book 3")),
  //  WriteMessage.createUpdateMessage(id = "00004", source = Book("Book 4")),
  /*
    WriteMessage.createDeleteMessage(id = "00002")
  */
)

Source(requests)
  .via(
    ElasticsearchFlow.create[Book](
      "sink8",
      "books"
    )
  )
  .runWith(Sink.seq)

*/

