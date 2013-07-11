OpenMPD
=======

OpenMPD is a [Free Software](https://en.wikipedia.org/wiki/Free_software) Ministry Partner Development tool for Android.

The goal of OpenMPD is to automate as much of the data management involved in
MPD and MPM (Ministry Partner Maintenance) as possible. For releases 1 and 2,
we are primarily targeting MPM use cases, but hope to expand to a full MPD
toolkit as development continues.

For release 1, our design philosophy is ***"Zero User Input."*** The time we spend
anaysing donor data is much better done by a computer, so OpenMPD aims to
eliminate as much of this work as possible. The only exception is to configure the 
user's account(s) with TntMPD.DataServer instances provided by his missions 
organisations.


Release Goals
-------------
If you have suggestions or requests that would help you raise support or
manage your donor base, please feel free to let us know.

### Current Status:
- Much of the Release 1 features and heavy lifting are done.
- Much work remains on the User Interface side
- We will require substantial testing before a stable release

### Release 1 Goals
- [ ] **"Zero User Input" data analysis release**
- [x] Import donor data and donation histories from TntMPD.DataServer
- [ ] Support for multiple data sources (Currently only Power to Change is verified working)
- [x] Classify partners into monthly, regular (quarterly, semiannual, etc), annual, occasional or one-time
- [x] Evaluate monthly giving divided into monthly, regular, and special gifts
- [x] Intelligently display donor statuses
- [x] Automatic background updating of gifts & donor statuses
- [x] Notifications of changes in donor status (for new, late, delinquent, dropped)
- [x] Notifications of special gifts
- [ ] Beautiful, Holo-themed user interface
- [ ] Simple, discoverable UI


### Release 2 Goals
- [ ] **"MPM/Partner communications" release**
- [ ] Manage, track regular contact with regular partners
- [ ] Integration with MailChimp, etc to evaluate partner engagement?
- [ ] Social network integration?
    

### Release 3 Goals
- [ ] **"Let's Raise Support" release**
- [ ] Namestorming features
- [ ] Appointment scheduling
- [ ] Followup scheduling/automation

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
OpenMPD is licensed under the Gnu GPL v2.0 (or later, at your option). See
LICENSE.txt for details. This may be changed to GPL3 in the near future.

Depends
-------

- ActionBarSherlock (actionbarsherlock.com) - Apache License 2.0
- SlidingMenu ( https://github.com/jfeinstein10/SlidingMenu ) - Apache License 2.0 
- CardsUI (https://github.com/Androguide/cardsui-for-android) 
- Android support library v4 - Apache License 2.0
