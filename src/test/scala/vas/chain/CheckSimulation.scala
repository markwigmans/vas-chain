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
class CheckSimulation extends Simulation {

  val userBatchSize = (Config.merchants + Config.accounts) / Config.initUsers
  val queryAccountsChain =
    repeat(userBatchSize, "accountId") {
      exec((s: Session) => {
        val id = userBatchSize * (s.userId % Config.initUsers) + s("accountId").as[Int]
        s.set("id", id)
      })
        .exec(http("query users").get("account/${id}").check(status.in(200, 404)))
    }

  val transferBatchSize = Config.transfers / Config.loadUsers
  val queryTransfersChain =
    repeat(transferBatchSize, "transferId") {
      exec((s: Session) => {
        val id = transferBatchSize * (s.userId % Config.loadUsers) + s("transferId").as[Int]
        s.set("id", id)
      })
        .exec(http("query transfers").get("transfer/${id}").check(status.in(200, 404)))
    }

  val accountScn = scenario("accounts").exec(queryAccountsChain)
  val transferScn = scenario("transfers").exec(queryTransfersChain)

  setUp(
    accountScn.inject(rampUsers(Config.initUsers) over (Config.rampUpInit)).protocols(Config.httpConf)
    , transferScn.inject(rampUsers(Config.loadUsers) over (Config.rampUpLoad)).protocols(Config.httpConf)
  )
}
