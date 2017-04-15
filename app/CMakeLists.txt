# Defining the project
project(DnnAndroidClient CXX)

# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
             native-dnn-model

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             src/main/cpp/native-dnn-model.cpp )

# include the tiny-dnn static libray
# Todo: change this to be an in project lib that is a git submodul

include_directories(/home/nitai/boost_headers
    /home/nitai/project_b/DNN_project/DNNAndroidClient/app/src/main/cpp/tiny-dnn/)

add_library( tiny-dnn /home/nitai/project_b/DNN_project/DNNAndroidClient/app/src/main/cpp/tiny-dnn/tiny_dnn/tiny_dnn.h )
set_target_properties(tiny-dnn PROPERTIES LINKER_LANGUAGE CXX)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log)

#find_library(tiny-dnn-lib
#                NAMES tiny_dnn
#                PATHS /home/nitai/tiny-dnn)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
                       native-dnn-model

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib}
                       tiny-dnn)

#set(BOOST_INCLUDEDIR /home/nitai/boost)
#set(Boost_USE_STATIC_LIBS OFF)
#set(Boost_USE_MULTITHREADED ON)
#set(Boost_USE_STATIC_RUNTIME OFF)
#find_package(Boost 1.45.0 COMPONENTS progress timer REQUIRED)

#if(Boost_FOUND)
#    include_directories(${Boost_INCLUDE_DIRS})
#    target_link_libraries(native-dnn-model ${Boost_LIBRARIES})
#endif()