package workshop

import java.io.FileInputStream

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import java.util.{Properties, UUID}
import io.gatling.core.config.GatlingConfiguration.configuration


class WorkshopTest extends Simulation{
  val prop = new Properties()
  prop.load(new FileInputStream(sys.env("GATLING_HOME") + "/" + configuration.core.directory.data + "/workshop.properties"))
  val throughput = prop.getProperty("throughput").toInt
  val duration = prop.getProperty("duration").toInt
  val rampUpUsers = prop.getProperty("rampUpUsers").toInt
  val rampUpDuration = prop.getProperty("rampUpDuration").toInt
  val maxResponseTime = prop.getProperty("maxResponseTime").toInt
  val percentSuccessulRequest = prop.getProperty("percentSuccessulRequest").toInt
  val loadBalancer = prop.getProperty("loadBalancer")



  val httpProtocol = http
    .baseURL(loadBalancer)


  val scn = scenario("WorkshopTest")
    .forever()
  {
    group("Test") {
      exec(http("Send Request")
        .get("/")
        .check(status.is(304))
      )
    }
  }.inject(constantUsersPerSec(rampUpUsers) during(rampUpDuration seconds))
    .throttle(jumpToRps(throughput), holdFor(duration minute))


  setUp(scn
  )
    .assertions(
      global.responseTime.percentile4.lessThan(maxResponseTime),
      global.successfulRequests.percent.greaterThan(percentSuccessulRequest)
    ).protocols(httpProtocol)


}