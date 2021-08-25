#!/bin/bash

echo ""
echo "Applying migration HistoricDateRequestPage"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /historicDateRequestPage                  controllers.HistoricDateRequestPageController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /historicDateRequestPage                  controllers.HistoricDateRequestPageController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeHistoricDateRequestPage                        controllers.HistoricDateRequestPageController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeHistoricDateRequestPage                        controllers.HistoricDateRequestPageController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "historicDateRequestPage.title = HistoricDateRequestPage" >> ../conf/messages.en
echo "historicDateRequestPage.heading = HistoricDateRequestPage" >> ../conf/messages.en
echo "historicDateRequestPage.checkYourAnswersLabel = HistoricDateRequestPage" >> ../conf/messages.en
echo "historicDateRequestPage.error.required.all = Enter the historicDateRequestPage" >> ../conf/messages.en
echo "historicDateRequestPage.error.required.two = The historicDateRequestPage" must include {0} and {1} >> ../conf/messages.en
echo "historicDateRequestPage.error.required = The historicDateRequestPage must include {0}" >> ../conf/messages.en
echo "historicDateRequestPage.error.invalid = Enter a real HistoricDateRequestPage" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryHistoricDateRequestPageUserAnswersEntry: Arbitrary[(HistoricDateRequestPagePage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[HistoricDateRequestPagePage.type]";\
    print "        value <- arbitrary[Int].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryHistoricDateRequestPagePage: Arbitrary[HistoricDateRequestPagePage.type] =";\
    print "    Arbitrary(HistoricDateRequestPagePage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(HistoricDateRequestPagePage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class CheckYourAnswersHelper/ {\
     print;\
     print "";\
     print "  def historicDateRequestPage: Option[AnswerRow] = userAnswers.get(HistoricDateRequestPagePage) map {";\
     print "    x =>";\
     print "      AnswerRow(";\
     print "        HtmlFormat.escape(messages(\"historicDateRequestPage.checkYourAnswersLabel\")),";\
     print "        HtmlFormat.escape(x.format(dateFormatter)),";\
     print "        routes.HistoricDateRequestPageController.onPageLoad(CheckMode).url";\
     print "      )";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration HistoricDateRequestPage completed"
