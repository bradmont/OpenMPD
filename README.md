OpenMPD
=======

OpenMPD is a [Free Software](https://en.wikipedia.org/wiki/Free_software) Ministry Partner Development tool for Android.

![The Home Screen](https://lh3.ggpht.com/2dLOo8IgWtc4VrOYCldjVn0crxTP9cMn-RLv3p-x7qCnsoTIV1hkp3kGxDAwFYsqtA=h900)

The goal of OpenMPD is to automate as much of the data management involved in
MPD and MPM (Ministry Partner Maintenance) as possible, and to mine your donor
data to help you more deeply understand, and serve, your partnership base. For 
releases 1 and 2, we are primarily targeting MPM use cases, but hope to expand 
to a full MPD toolkit as development continues.

For release 1, our design philosophy is ***"Zero User Input."*** The time we spend
anaysing donor data is much better done by a computer, so OpenMPD aims to
eliminate as much of this work as possible. The only exception is to configure the 
user's account(s) with TntMPD.DataServer instances provided by his missions 
organisations.


Milestone Goals
---------------
If you have suggestions or requests that would help you raise support or
manage your donor base, please feel free to let us know.

### Current Status:
- Most of the Milestone 1 features and heavy lifting are done. App is fully functional (minus a few bugs).
- We are in closed beta. If you want to test, please contact me.
- **We are feature complete for beta 1**. I'm going to ask a few friends to test the app before moving ot open beta. Should be soon now.

#### Beta 1:
If you are interested in helping test OpenMPD, please contact me. Twitter is probably the easiest way (@bradmont). If you use other organisations besides Power to Change and CCC, I would appreciate your help as well, as I can't test on other organisations' servers.

#### Left to do before Release 1:
- [/] Design: Contact List View (I'd like this to be prettier, but can't think of how)
- [x] Design: Contact Detail View
- [/] Design/UI: Accounts configuration/add view (Ok for now, would like to improve)
- [x] Searching & filtering contacts (by name, partner type, giving amount, etc).
- [x] Verification of service accounts when you first add them
- [x] Build label support into BarGraph
- [ ] Build swipe support into BarGraph to view past history
- [x] Help screens & cleaner onboarding

### Milestone 1 Goals
- **"Zero User Input" data analysis release**
- [x] Import donor data and donation histories from TntMPD.DataServer
- [x] Support for multiple data sources (p2c & Cru working; adding others should be fairly easy)
- [x] Classify partners into monthly, regular (quarterly, semiannual, etc), annual, occasional or one-time
- [x] Evaluate monthly giving divided into monthly, regular, and special gifts
- [x] Intelligently display donor statuses
- [x] Automatic background updating of gifts & donor statuses
- [x] Notifications of changes in donor status (for new, late, delinquent, dropped)
- [x] Notifications of special gifts
- [x] Simple Onboarding walkthrough
- [x] Beautiful, Holo-themed user interface
- [x] Simple, discoverable UI


### Milestone 2 Goals
- [ ] **"MPM/Partner communications" release**
- [ ] Manage, track regular contact with regular partners
- [ ] Integration with MailChimp, etc to evaluate partner engagement
- [ ] Social network integration
    

### Milestone 3 Goals
- [ ] **MPD release**
- [ ] Namestorming features
- [ ] Appointment scheduling
- [ ] Followup scheduling/automation
- [ ] Web beacon support for emails

### Other development goals
- [ ] Networked Use, to coordinate couples or groups developing support together

Get Involved
------------
There are a number of ways you can contribute to this project. If you are 
interested, please contact @Brad. Here's how you can help:
- Code
- Graphics assets/UI design work
- User testing
- Financial support (I'm a missionary too!)

License
-------
OpenMPD is licensed under the Gnu GPL version 3. See LICENSE.txt for details. 

Depends
-------

- ActionBarSherlock (actionbarsherlock.com) - Apache License 2.0
- SlidingMenu ( https://github.com/jfeinstein10/SlidingMenu ) - Apache License 2.0 
- CardsUI (https://github.com/Androguide/cardsui-for-android) 
- Android support library v4 - Apache License 2.0
