OpenMPD
=======

OpenMPD is a [Free Software](https://en.wikipedia.org/wiki/Free_software) Ministry Partner Development tool for Android.

[Download OpenMPD on Google Play](https://play.google.com/store/apps/details?id=net.bradmont.openmpd)

![The Home Screen](http://i.imgur.com/QnmSgALl.png)
![The Notifications Screen](http://i.imgur.com/Eh2PUmwl.png)

The goal of OpenMPD is to automate as much of the data management involved in
MPD and MPM (Ministry Partner Maintenance) as possible, and to mine your donor
data to help you more deeply understand, and serve, your partnership base. For
the time being, we are primarily targeting MPM use cases, but may eventually expand 
to a full MPD toolkit as development continues.

Our design philosophy is ***"Zero User Input."*** The time we spend
anaysing donor data is much better done by a computer, so OpenMPD aims to
eliminate as much of this work as possible. The only exception is to configure the 
user's account(s) with TntMPD.DataServer instances provided by his missions 
organisations.



### Current Status:
- **Version 1.1 is released!** You can [download OpenMPD on Google Play](https://play.google.com/store/apps/details?id=net.bradmont.openmpd).

As it stands, OpenMPD is fully functional as an MPM tool. I've got some neat plans for version 2, but right now I'm doing some significant refactoring before starting on them. I'm currently replacing the old home-grown ORM backend with GreenDao, so expect significant breakage on this branch (the v1.1 branch still runs fine).

My refactoring goals before starting any new features are:
- [ ] Switching to a better (not homebrewed) DAO library
- [ ] Using Bayesing matching for partner evaluation


Get Involved
------------
There are a number of ways you can contribute to this project. If you are 
interested, please contact @Brad. Here's how you can help:
- Code
- Graphics assets/UI design work
- User testing
- [Financial support](http://p2c.com/bcstewart) (I'm a missionary too!)

License
-------
OpenMPD is licensed under the Gnu GPL version 3. See LICENSE.txt for details. 

