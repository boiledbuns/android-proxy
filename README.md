# Android-Proxy

## What is this?
This is a proof-of-concept that hopes to enable the development of third party apps that rely on information from other apps. 

By creating a "local" ssl proxy, we can capture and interact with all outgoing/incoming network traffic. This information can be used by third party applications that rely on information from another application to function.

Although this doesn't allow apps to "truly" read all information from another app since this only captures network communication, the intention to build a framework to enable third party applications that can interact with other installed apps as well as the network traffic!

Some potential uses:
- Debugging applications: reading and manipulating http requests are they come (think Charles but completely local to your Android device)
- Ad block: disabling requests to ad servers (will have to look more into this)
- Hearthstone deck tracker: read socket traffic between the game and servers to display deck contents and updates

## Motivation
Mobile apps are sandboxed in such a way that the only way to communicate between apps is through either:
a) Intents
b) Content Providers
c) Stored app data

However, these methods mean that the only way other apps can interact with an app is through the commmunication routes it deliberately exposes.

Compared to desktop operating systems, this is can be very restrictive in some situation. It forces developers of an app to implement and expose the functionality for any third party wants to communicate with it. Third party app developer intent on building an app that relies on information from another app can only rely on contacting the developers to expose this functionality. Usually, this is not feasible as it the additional work required on the end of the app developer will usually mean it is never exposed.

This idea was originally conceived with specific applications in mind - [Hearthstone-Deck-Tracker](https://github.com/HearthSim/Hearthstone-Deck-Tracker) - so I'll use it to illustrate the problem!

To give a bit of context, Hearthstone is a digital card game that runs on both desktop and mobile platforms and HSDeckTracker is a tool that displays the current cards in your deck, cards shuffled in, and a variety of additional information in real time.

On desktop, Hearthstone tracks the game state in a log file which HSDecktracker inspects to display game information. On mobile, a similar app [https://github.com/HearthSim/Arcane-Tracker] exists which does the same thing using a log file exposed on external storage. However, there are things (like rank) which aren't exposed through the logs which means it's forced to record the screen to do so. This is extremely draining on the device's performance as well as iits battery life. What if we could do this more efficiently and leverage it as a generic framework?

## Challenges
HTTPS: Android defaults to https which means our proxy must be an SSL/TLS proxy. We need to implement the SSL negotiation between the client and our server as well as our server and the destination. 

## Goals and Roadmap
- [ ] Parse IP Packets 
- [ ] Parse TCP Packets
- [ ] Implement the SSL proxy server <- needs to be broken down more 
- [ ] Parse UDP Packets
