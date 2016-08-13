########
#
# - lib's R is used only for compiling its src
# - app generates its own R for both itself and lib
# - lib's package name is required when app generating lib's R
#

TOP := ..
-include $(TOP)/build/env.mk
include $(TOP)/build/f.mk

include $(TOP)/build/clear_local_var.mk

LOCAL_RES_D := $(wildcard ./mod_*/res)
LOCAL_SRC_D := ./src $(wildcard ./mod_*/src)

LOCAL_IS_LIB := true
LOCAL_SIGNOUT_WITH_TSA := true

include $(TOP)/build/core.mk
