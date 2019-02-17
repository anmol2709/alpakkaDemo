

import elasticSearchAlpakka.Company
import play.api.libs.json.Json


object JsonProducer extends App {
  implicit val _ = Json.format[Company]

  val a = Company("1", "hi")
  print(Json.toJson[Company](a))
}
