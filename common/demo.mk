########
# pd-demo
#

TOP := ..
include $(TOP)/build/def.mk
$(call expand_dep_jar, $(TOP)/common)

LOCAL_MODULE  := pd-demo
LOCAL_PACKAGE := t.typedef.droid.demo
LOCAL_SRC_DIR := ./demo/src
LOCAL_RES_DIR := ./demo/res
LOCAL_MANIFEST := ./demo/AndroidManifest.xml
include $(TOP)/build/apk.mk
