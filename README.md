Mobile CNN
==========

This project represents a method of running convolutional neural networks on an Android device. 
Much of the code in this project is based on [caffe-android-demo](https://github.com/sh1r0/caffe-android-demo) by [sh1r0](https://github.com/sh1r0), to whom many thanks are given.

##Project
The project is an android application built using Android-Studio. 
Importing the project into Android-Studio should build the application using gradle and allow for on-device debugging with connected devices.

##Models
The models used for this project were in [caffe](https://github.com/BVLC/caffe). 
In order to get mobile caffe built into the libraris found in the `/jni` folder, steps were taken following another project by sh1r0 in conjunction with the first: [caffe-android-lib](https://github.com/sh1r0/caffe-android-lib). 
Instead of using the online models, the weights for the smaller trained model are compressed in `models/weights.7z`, along with the required model definition for deployment. 
These two files must be copied onto the sdcard of the android device to get the project working.

Also present in the models folder are the `.prototxt` files used to train the network. 
`mobilecnn.prototxt` was used in conjunction with `mobilecnn_solver.prototxt` to originally train the network off of the AlexNet network. 
This network should be initialized in caffe using the weights from the trained `bvlc_alexnet` model. 
Once training has been satisfactorily completed, weights from the resulting network can be used to initialize training on `mobilecnn_small.prototxt` and `mobilecnn_solver_small.prototxt` to connect and fine tune the network.
