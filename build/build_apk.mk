TOP := ..
-include $(TOP)/build/env.mk

include $(TOP)/build/clear_local_var.mk

LOCAL_DEP_JAR := $(TOP)/../typedef/out/t.typedef.jar
LOCAL_DEP_LIB := $(TOP)/droid

include $(TOP)/build/core.mk
