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

package utils

import viewmodels.PdfLink

import java.time.LocalDate

object TestData {
  val currentDate: LocalDate = LocalDate.now()
  val currentYear: String    = currentDate.getYear.toString
  val currentMonth: String   = currentDate.getMonthValue.toString

  val year17 = 2014
  val year   = 2018
  val year2  = 2019
  val year3  = 2020
  val year4  = 2011
  val year5  = 2012

  val periodStartYear    = 2017
  val periodStartMonth   = 11
  val periodStartMonth_1 = 10
  val periodStartMonth_2 = 10
  val periodStartDay     = 1
  val periodEndYear      = 2017
  val periodEndMonth     = 11
  val periodEndMonth_2   = 12
  val periodEndDay       = 8
  val fileSize           = 500L

  val month   = 3
  val month_2 = 3
  val day     = 14
  val day2    = 28
  val hour    = 2
  val minute  = 23

  val date: LocalDate      = LocalDate.of(year, month, day)
  val startDate: LocalDate = LocalDate.of(year, month, day)
  val endDate: LocalDate   = LocalDate.of(year, month, day2)

  lazy val january = 1
  lazy val march   = 3

  val one          = 1
  val two          = 2
  val three        = 3
  val four         = 4
  val eleven       = 11
  val twelve       = 12
  val twentyEight  = 28
  val fiveHundread = 500L
  val ninetynine   = 99L

  val offset     = 10
  val size       = 1024L
  val yearLength = 4

  val eori                              = "GB11111"
  val emailId                           = "test@test.com"
  val email: Option[String]             = Some("test@test.com")
  val dan                               = "1234"
  val someRequestId: Option[String]     = Some("Ab1234")
  val requestId                         = "Ab1234"
  val someAccountNumber: Option[String] = Some("123456789")
  val accountNumber                     = "12345678"
  val csv                               = "csv"
  val pdfLink: PdfLink                  = PdfLink("file.pdf", "1MB", "Download PDF")
  val pdfFileName                       = "2018_03_01-08.pdf"
  val pdfUrl                            = "url.pdf"
  val pdfSize                           = 1024L
  val fileName                          = "test-file.csv"
  val downloadUrl                       = "https://second.com/"
  val url                               = "test_url.com"
  val id                                = "value"
  val test_Id                           = "testId"
  val pId                               = "test_pid"
  val location                          = "test_location"
  val testClass                         = "test_link_class"
  val msgKey                            = "test_message_key"

  val test_title   = "test_title"
  val test_heading = "test_heading"
  val test_message = "test_msg"

  lazy val belowKbThreshold = 100
  lazy val kbValue          = 30567
  lazy val mbValue          = 20567567

  lazy val kbThreshold      = 1024
  lazy val mbThreshold: Int = 1024 * 1024

  lazy val startKey   = "start"
  lazy val endKey     = "end"
  lazy val defaultKey = "default"

  val linkMessage            = "go to test page"
  val href                   = "www.test.com"
  val preLinkMessage         = "test_pre_link_message"
  val postLinkMessage        = "test_post_link_message"
  val classes                = "govuk-!-margin-bottom-7"
  val defaultClasses: String = "govuk-body"
}
