# SPDX-License-Identifier: Apache-2.0
#
# Copyright © 2017-2023 WireGuard LLC. All Rights Reserved.

cmake_minimum_required(VERSION 3.4.1)
project("WireGuard")
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY "${CMAKE_LIBRARY_OUTPUT_DIRECTORY}")
add_link_options(LINKER:--build-id=none)

file(GLOB WG_SOURCES wireguard-tools/src/*.c ndk-compat/compat.c)
add_executable(libwg.so ${WG_SOURCES})
target_link_options(libwg.so PRIVATE "-Wl,-z,max-page-size=16384")
target_include_directories(libwg.so PUBLIC "${CMAKE_CURRENT_SOURCE_DIR}/wireguard-tools/src/uapi/linux/" "${CMAKE_CURRENT_SOURCE_DIR}/wireguard-tools/src/")
target_compile_options(libwg.so PUBLIC -std=gnu11 -include ${CMAKE_CURRENT_SOURCE_DIR}/ndk-compat/compat.h -DRUNSTATEDIR=\"/data/data/${ANDROID_PACKAGE_NAME}/cache\")

add_custom_target(libwg-go.so
    WORKING_DIRECTORY "${CMAKE_CURRENT_SOURCE_DIR}/libwg-go"
    COMMENT "Building wireguard-go"
    VERBATIM
    COMMAND python "${CMAKE_CURRENT_SOURCE_DIR}/libwg-go/build-wg-go.py"
        "${ANDROID_ARCH_NAME}"
        "${CMAKE_LIBRARY_OUTPUT_DIRECTORY}"
        "${ANDROID_PACKAGE_NAME}"
)
