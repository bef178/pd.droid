TOP := ..
-include $(TOP)/build/env.mk

include $(TOP)/build/clear_local_var.mk

LOCAL_DEP_JAR := $(TOP)/../typedef/out/typedef.jar
LOCAL_DEP_LIB := $(TOP)/common

include $(TOP)/build/core.mk
