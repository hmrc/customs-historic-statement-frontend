/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package services

import java.time.{LocalDateTime, ZoneId}
import base.SpecBase
import org.mockito.Mockito.when

class DateTimeServiceSpec extends SpecBase {

  "DateTimeService" should {

    "return system date and time for UTC" in new Setup {
      val systemDateTime = dateTimeService.systemDateTime(ZoneId.of("UTC"))
      Option(systemDateTime) must not be empty
    }

    "return system date and time for Europe/London" in new Setup {
      val systemDateTime = dateTimeService.systemDateTime(ZoneId.of("Europe/London"))
      Option(systemDateTime) must not be empty
    }

    "return fixed date and time when fixedDateTime is true" in new Setup {
      when(mockAppConfig.fixedDateTime).thenReturn(true)
      val fixedDateTime = dateTimeService.systemDateTime(ZoneId.of("UTC"))
      fixedDateTime mustBe LocalDateTime.of(year, month, day, hour, minute)
    }

    "return UTC date and time for utcDateTime" in new Setup {
      val utcDateTime = dateTimeService.utcDateTime()
      Option(utcDateTime) must not be empty
    }

    "return Europe/London date and time for localDateTime" in new Setup {
      val londonDateTime = dateTimeService.localDateTime()
      Option(londonDateTime) must not be empty
    }
  }

  trait Setup {

    val year   = 2027
    val month  = 12
    val day    = 20
    val hour   = 12
    val minute = 30

    when(mockAppConfig.fixedDateTime).thenReturn(false)
    val dateTimeService: DateTimeService = new DateTimeService(mockAppConfig)
  }
}
