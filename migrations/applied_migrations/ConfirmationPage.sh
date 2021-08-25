#!/bin/bash

echo ""
echo "Applying migration ConfirmationPage"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /confirmationPage                       controllers.ConfirmationPageController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "confirmationPage.title = confirmationPage" >> ../conf/messages.en
echo "confirmationPage.heading = confirmationPage" >> ../conf/messages.en

echo "Migration ConfirmationPage completed"
