#CMakeLists.txt 内容如下：
# Sets the minimum version of CMake required to build your native library.
# This ensures that a certain set of CMake features is available to
# your build.

cmake_minimum_required(VERSION 3.4.1)

# Specifies a library name, specifies whether the library is STATIC or
# SHARED, and provides relative paths to the source code. You can
# define multiple libraries by adding multiple add.library() commands,
# and CMake builds them for you. When you build your app, Gradle
# automatically packages shared libraries with your APK.

add_library( # Specifies the name of the library.
			 PhoneRunning

			 # Sets the library as a shared library.
			 SHARED

			 # Provides a relative path to your source file(s).
			 src/main/cpp/PhoneRunning.cpp

		)
		
		add_library( # Specifies the name of the library.
				Utils

			 # Sets the library as a shared library.
				SHARED

			 # Provides a relative path to your source file(s).

				src/main/cpp/Utils.cpp
		)

# Specifies a path to native header files.
# include_directories(src/main/cpp/include/)


#如果要在CMakeList.txt 中添加NDK API, 或者其他已经存在的库，不是本文关注的重点，可参考： https://developer.android.com/studio/projects/add-native-code?hl=zh-cn#existing-project
