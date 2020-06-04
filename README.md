# Prediction-Assistant-Analyzer

This is a desktop application using JavaFX and Spring that connects to a MySQL database defined in application.properties
It is still in development but is in a usable state at the moment.

Features include:
-Displaying all Markets and their Contracts' share values.
-Tracking Markets, save/load for later use.
-Find currently 'moving' Contracts. Displays what shares have been active (changing in price) in the last XX minutes.
-Analyze Contract history. Shows full history of a Contract's buyYes share value and line chart of the price over time with options to overlay Simple Moving Average on the same chart.
-Ability to open URL/webpage of any Market/Contract being examined for ability to quickly buy/sell.
-And more!

To run this application, use the Maven Package tool to create a .jar and run the jar file. Once I feel like I've completed this application I will provide a better download link.

Current goal is to add more analyzers and push notifications for buy/sell signals based on user configuration, this is going to require more testing time to see what signals have any kind of accuracy etc.
I have left the deprecated neural network code I worked on as part of this project in case I decide to work on it more, I think it could be useful at some point but it just doesn't have enough data to train on yet and there are multiple ways I could setup the training data which I am still playing with. Stay tuned.
