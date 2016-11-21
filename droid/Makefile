########
#
# - lib's R is used only for compiling its src
# - app generates its own R for both itself and lib
# - lib's package name is required when app generating lib's R
#

TOP := ..
-include $(TOP)/build/env.mk

include $(TOP)/build/clear_local_var.mk

LOCAL_DEP_JAR := $(ANDROID_HOME)/extras/android/support/v4/android-support-v4.jar

LOCAL_RES_D := $(wildcard ./mod_*/res)
LOCAL_SRC_D := ./src $(wildcard ./mod_*/src)

LOCAL_IS_LIB := true

include $(TOP)/build/core.mk
