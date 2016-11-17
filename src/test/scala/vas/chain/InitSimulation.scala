/*
 * Copyright (C) 2016 Mark Wigmans (mark.wigmans@ximedes.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package vas.chain

import io.gatling.core.Predef._
import io.gatling.http.Predef._

/**
  *
  */
class InitSimulation extends Simulation {

  val accountChain =
    repeat(Config.accounts.get) {
      exec(http("create accounts").post("account").body(StringBody("""{}""")).asJSON.check(status.in(202, 503)))
    }

  val merchantChain =
    repeat(Config.merchants.get) {
      exec(http("create merchants").post("account").body(StringBody("""{"overdraft" : 100000}""")).asJSON.check(status.in(202, 503)))
    }

  val scn = scenario("create").exec(merchantChain, accountChain)

  setUp(
    scn.inject(rampUsers(1) over (Config.rampUpInit)).protocols(Config.httpConf)
  )
}
