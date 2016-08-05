TOP := ..
-include $(TOP)/build/env.mk
include $(TOP)/build/f.mk

LOCAL_DEP_PKG_S := t.typedef.droid
LOCAL_DEP_JAR_F := $(TOP)/common/out/pd-common.jar
LOCAL_DEP_RES_D := $(TOP)/common/out/res

LOCAL_DEP_JAR_F += $(TOP)/../typedef/out/typedef.jar

include $(TOP)/build/build_apk.mk
