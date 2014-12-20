Big Ben clock effect adapted for Android Wear
---------------------------------------------

I made a watch face for Anrdoid Wear which is in the [Play Store](https://play.google.com/store/apps/details?id=net.jimblackler.bigben.watchface) now. It's inspired by the [clock tower at the Palace of Westminster, London](http://en.wikipedia.org/wiki/Big_Ben), better known as Big Ben.


Idea
----

Recently I was given an LG G Watch. As an introduction to Android Wear I loved it, I found the device much more useful than I was expecting. Not having to get my phone out of my pocket to check my notifications is surprisingly useful.

I also loved the ability to chose watch faces. I've [previously written about an animated Big Ben effect](http://jimblackler.net/blog/?p=258).This was written in SVG for use on the web, but I had a 'light bulb moment' when I realized that this existing could be adapted for Anrdoid Wear. Most of the difficult work (preparing the digital images and proving the concept would work) had been done.

Project
-------

The first job was to update to Android Studio (I've been a long-standing Eclipse user to date) and to learn how to code for Android Wear. I was pleased to discover that it's not too different to coding for a phone, it's basically Android on the device, slightly cut down for the form factor. The hardest part for me was realizing how the two app executable (APKs) you have to deliver (one for phone, one for app) work together and how deployment (e.g. to Play Store) was supposed to be done.

I studied the [design guidelines](http://developer.android.com/design/wear/watchfaces.html) and realized one adjustment would need to be made; introduction of an 'ambient mode' where the screen was mostly black. This meant drawing new stylized versions of the watch face and hands, which I did in Inkscape. I tried to capture the iconic Gothic style of the original while using outline effects so as to draw very little content in ambient mode. Because any flaws are very visible on the watch face I also scaled the assets for the watch resolution and neatened up the image to fill the entire frame without any visible gaps at all, and some slight perspective correction on the original image.

Code-wise it was then a matter of studying the watch face samples and producing my own version. I used Android canvas to scale, rotate and overlay the bitmap watch face elements.

Check it out
------------

The app can be download from the [Play Store](https://play.google.com/store/apps/details?id=net.jimblackler.bigben.watchface) now. It requires an Android Wear watch with Android 5 (Lollipop) or better.

Source for the project is available in [GitHub](https://github.com/jimblackler/BigBen).


License
-------

Original content (C) Jim Blackler licensed under [Apache License Version 2.0](http://www.apache.org/licenses/LICENSE-2.0) including modified content from [android-WatchFace](https://github.com/googlesamples/android-WatchFace) Copyright 2014 The Android Open
Source Project, Inc. licensed under [Apache License Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)

Digitally modified image content licensed under [CC BY-SA 2.0](https://creativecommons.org/licenses/by-sa/2.0/) after [Original image by Aldaron licensed under CC BY-SA 2.0](https://www.flickr.com/photos/aldaron/536362686)

[Image used in Play Store listing licensed under CC BY-SA 4.0](http://en.wikipedia.org/wiki/Big_Ben#mediaviewer/File:Palace_of_Westminster_from_the_dome_on_Methodist_Central_Hall.jpg)
