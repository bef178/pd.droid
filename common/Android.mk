# common utility

LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res
LOCAL_MODULE := th-common
LOCAL_SDK_VERSION := 19

include $(BUILD_STATIC_JAVA_LIBRARY)
