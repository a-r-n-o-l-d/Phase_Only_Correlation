# Phase_Only_Correlation ImageJ plugin

## Description

This ImageJ plugin computes the [phase only correlation](https://en.wikipedia.org/wiki/Phase_correlation) between two
images. From phase only correlation, we can estimate the translation between two images and then perform basic image
registration. This plugin estimates translation with a subpixel accuracy [1].

[1]Foroosh, Zerubia, and Berthod, “Extension of Phase Correlation to Subpixel Registration.”

## Installation

Download the [plugin](https://github.com/a-r-n-o-l-d/Phase_Only_Correlation/tree/master/lib/build/libs), and put it in
the plugins folder of ImageJ.

## Usage

In the dialog window :
1. choose two images (same size and size must be a power a two)
2. choose an [apodization window](https://en.wikipedia.org/wiki/Window_function)

![usage](/assets/images/Screenshot.jpg)
